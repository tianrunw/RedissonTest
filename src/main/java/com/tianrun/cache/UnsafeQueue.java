package com.tianrun.cache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class UnsafeQueue<T> {
    private static final Logger log = LogManager.getLogger();

    public static final ThreadLocal<Integer> CURR_THREAD = new ThreadLocal<>();

    private final Set<T> queue = Collections.synchronizedSet(new LinkedHashSet<>());
    private final ReentrantLock lock = new ReentrantLock();
    private final int maxSize;

    public UnsafeQueue(int maxSize) {
        this.maxSize = maxSize;
    }

    public int size() {
        return queue.size();
    }

    // @NotThreadSafe
    public void add(T value) {
        try {
            if (queue.size() >= maxSize) {
                onFullLock();
            }
            queue.add(value);

        } catch (Exception e) {
            log.error("Thread {} failed, queue size: {}, lock hold: {}, lock queue: {}", CURR_THREAD.get(),
                    queue.size(), lock.getHoldCount(), lock.getQueueLength(), e);
        }
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
            }
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
}
