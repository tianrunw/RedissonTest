package com.tianrun.cache;

import com.tianrun.Customer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JacksonCodec;
import org.redisson.codec.JsonCodecWrapper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.tianrun.cache.UnsafeQueue.CURR_THREAD;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UnsafeQueueTest {
    static final Logger log = LogManager.getLogger();
    static final JacksonCodec<Customer> CUSTOMER_CODEC = new JacksonCodec<>(Customer.class);

    static final int QUEUE_SIZE = 10;
    static final int NUM_THREADS = 10;

    @Test
    void testSingleThreaded() {
        UnsafeQueue<Codec> queue = new UnsafeQueue<>(QUEUE_SIZE);
        addLoop(queue);
        assertEquals(QUEUE_SIZE, queue.size());
    }

    @Test
    void testMultiThreaded() throws InterruptedException {
        UnsafeQueue<Codec> queue = new UnsafeQueue<>(QUEUE_SIZE);
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        for (int i = 0; i < NUM_THREADS; i++) {
            final int threadNumber = i;
            executor.submit(() -> addInfiniteLoop(queue, threadNumber));
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        assertEquals(QUEUE_SIZE, queue.size());
    }

    static void addLoop(UnsafeQueue<Codec> queue) {
        for (int i = 0; i < QUEUE_SIZE * 2; i++) {
            try {
                queue.add(new JsonCodecWrapper(CUSTOMER_CODEC));
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    static void addInfiniteLoop(UnsafeQueue<Codec> queue, int threadNumber) {
        CURR_THREAD.set(threadNumber);
        while (true) {
            try {
                queue.add(new JsonCodecWrapper(CUSTOMER_CODEC));
            } catch (Exception e) {
                log.error(e);
            }
        }
    }
}