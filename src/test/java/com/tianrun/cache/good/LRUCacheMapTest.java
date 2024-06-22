package com.tianrun.cache.good;

import com.tianrun.Customer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JacksonCodec;
import org.redisson.codec.JsonCodecWrapper;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class LRUCacheMapTest {
    static final Logger log = LogManager.getLogger();
    static final JacksonCodec<Customer> CUSTOMER_CODEC = new JacksonCodec<>(Customer.class);

    static final int CACHE_SIZE = 10;
    static final int NUM_THREADS = 10;

    @Test
    void testSingleThreaded() {
        Map<Codec, Codec> lruCacheMap = new LRUCacheMap<>(CACHE_SIZE, 0, 0);
        putLoop(lruCacheMap, 0);
        assertEquals(CACHE_SIZE, lruCacheMap.size());
    }

    @Test
    void testMultiThreaded() throws InterruptedException {
        Map<Codec, Codec> lruCacheMap = new LRUCacheMap<>(CACHE_SIZE, 0, 0);
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        for (int i = 0; i < NUM_THREADS; i++) {
            final int threadNumber = i;
            executor.submit(() -> putInfiniteLoop(lruCacheMap, threadNumber));
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        assertEquals(CACHE_SIZE, lruCacheMap.size());
    }

    static void putLoop(Map<Codec, Codec> lruCacheMap, int threadNumber) {
        for (int i = 0; i < CACHE_SIZE * 2; i++) {
            try {
                lruCacheMap.put(new JsonCodecWrapper(CUSTOMER_CODEC), new JsonCodecWrapper(CUSTOMER_CODEC));
            } catch (Exception e) {
                log.error("Thread {} failed: ", threadNumber, e);
            }
        }
    }

    static void putInfiniteLoop(Map<Codec, Codec> lruCacheMap, int threadNumber) {
        while (true) {
            try {
                lruCacheMap.put(new JsonCodecWrapper(CUSTOMER_CODEC), new JsonCodecWrapper(CUSTOMER_CODEC));
            } catch (Exception e) {
                log.error("Thread {} failed: ", threadNumber, e);
            }
        }
    }
}