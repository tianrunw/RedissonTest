package com.tianrun.cache.bad;

import com.tianrun.cache.CachedValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Version 3.25.1
 */
public class LRUCacheMap<K, V> extends AbstractCacheMap<K, V> {
    private static final Logger log = LogManager.getLogger();

    public static final ThreadLocal<Integer> CURR_THREAD = new ThreadLocal<>();

    private static final int NUM_QUEUES = 1;
    private final AtomicLong index = new AtomicLong();
    private final List<Collection<CachedValue<K, V>>> queues = new ArrayList<>();
    private final Map<Collection<CachedValue<K, V>>, Lock> queueLocks = new IdentityHashMap<>();

    public LRUCacheMap(int size, long timeToLiveInMillis, long maxIdleInMillis) {
        super(size, timeToLiveInMillis, maxIdleInMillis);

        // NUM_QUEUES was 2 * available processors in original
        for (int i = 0; i < NUM_QUEUES; i++) {
            Set<CachedValue<K, V>> instance = Collections.synchronizedSet(new LinkedHashSet<>());
            queues.add(instance);
            queueLocks.put(instance, new ReentrantLock());
        }
    }

    @Override
    protected void onValueCreate(CachedValue<K, V> value) {
        Collection<CachedValue<K, V>> queue = getQueue(value);
        queue.add(value);
    }

    private Collection<CachedValue<K, V>> getQueue(CachedValue<K, V> value) {
        return queues.get(Math.abs(value.hashCode() % queues.size()));
    }

    @Override
    protected void onValueRemove(CachedValue<K, V> value) {
        Collection<CachedValue<K, V>> queue = getQueue(value);
        queue.remove(value);
    }

    @Override
    protected void onValueRead(CachedValue<K, V> value) {
        Collection<CachedValue<K, V>> queue = getQueue(value);
        // move value to the tail of the queue
        if (queue.remove(value)) {
            queue.add(value);
        }
    }

    @Override
    protected void onMapFull() {
        int startIndex = -1;
        while (true) {
            int queueIndex = (int) Math.abs(index.incrementAndGet() % queues.size());
            if (queueIndex == startIndex) {
                return;
            }
            if (startIndex == -1) {
                startIndex = queueIndex;
            }

            Collection<CachedValue<K, V>> queue = queues.get(queueIndex);
            CachedValue<K, V> removedValue = null;
            Lock lock = queueLocks.get(queue);
            lock.lock();
            try {
                Iterator<CachedValue<K, V>> iter = queue.iterator();
                if (iter.hasNext()) {
                    removedValue = iter.next();
                    iter.remove();
                }
            } catch (Exception e) { // Catch block not in original
                log.error("Thread {} Queue {} failed, map size: {}, queue sizes: {}", CURR_THREAD.get(),
                        queueIndex, map.size(), getQueueInfo(), e);
                throw e;
            } finally {
                lock.unlock();
            }

            if (removedValue != null) {
                map.remove(removedValue.getKey(), removedValue);
                return;
            }
        }
    }

    @Override
    public void clear() {
        for (Collection<CachedValue<K, V>> collection : queues) {
            collection.clear();
        }
        super.clear();
    }

    // Not in original
    private String getQueueInfo() {
        StringBuilder info = new StringBuilder();
        for (int i = 0; i < queues.size(); i ++) {
            Collection<CachedValue<K, V>> queue = queues.get(i);
            info.append("Queue ").append(i).append(": ").append(queue.size()).append(" ");
        }
        return info.toString();
    }
}
