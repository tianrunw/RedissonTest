package com.tianrun.cache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class UnsafeQueue<T> {
    private static final Logger log = LogManager.getLogger();

    public static final ThreadLocal<Integer> CURR_THREAD = new ThreadLocal<>();

    private final Set<T> queue = Collections.synchronizedSet(new LinkedHashSet<>());
    private final ReentrantLock lock = new ReentrantLock();

    private final int maxSize;
    private final AtomicInteger addCount = new AtomicInteger();
    private final AtomicInteger removeCount = new AtomicInteger();

    public UnsafeQueue(int maxSize) {
        this.maxSize = maxSize;
    }

    public int size() {
        return queue.size();
    }

    // @NotThreadSafe
    public void add(T value) {
        if (queue.size() >= maxSize) {
            onFullLock();
        }
        queue.add(value);
        addCount.addAndGet(1);
    }

    private void onFullSync() {
        synchronized (queue) {
            Iterator<T> iter = queue.iterator();
            if (iter.hasNext()) {
                iter.next();
                iter.remove();
            }
        }
    }

    private void onFullLock() {
        lock.lock();
        try {
            Iterator<T> iter = queue.iterator();
            if (iter.hasNext()) {
                iter.next();
                iter.remove();
                removeCount.addAndGet(1);
            }
        } catch (Exception e) {
            log.error("Thread {} failed, queue size: {}, lock hold: {}, lock queue: {}, addCount: {}, removeCount: {}",
                    CURR_THREAD.get(), queue.size(), lock.getHoldCount(), lock.getQueueLength(), addCount.get(), removeCount.get(), e);
            throw e;
        } finally {
            lock.unlock();
        }
    }

    private void onFullNonSync() {
        Iterator<T> iter = queue.iterator();
        if (iter.hasNext()) {
            iter.next();
            iter.remove();
        }
    }

    private static void randomSleep() {
        try {
            int randomMillis = ThreadLocalRandom.current().nextInt(1, 11);
            Thread.sleep(randomMillis);
        } catch (InterruptedException e) {
            log.error("Failed to sleep", e);
        }
    }
}
