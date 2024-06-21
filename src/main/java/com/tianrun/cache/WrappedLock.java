package com.tianrun.cache;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 *
 * @author Nikita Koksharov
 *
 */
public class WrappedLock {

    private final Lock lock = new ReentrantLock();

    public void execute(Runnable r) {
        lock.lock();
        try {
            r.run();
        } finally {
            lock.unlock();
        }
    }

    public <T> T execute(Supplier<T> r) {
        lock.lock();
        try {
            return r.get();
        } finally {
            lock.unlock();
        }
    }

}
