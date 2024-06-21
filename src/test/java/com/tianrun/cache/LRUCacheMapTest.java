package com.tianrun.cache;

import com.tianrun.Customer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.redisson.cache.LRUCacheMap;
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
    static final int NUM_THREADS = 10;

    @Test
    void testSingleThreaded() {
        final int cacheSize = 5;
        Map<Codec, Codec> lruCacheMap = new LRUCacheMap<>(5, 0, 0);

        for (int i = 0; i < 10; i++) {
            lruCacheMap.put(new JsonCodecWrapper(CUSTOMER_CODEC), new JsonCodecWrapper(CUSTOMER_CODEC));
        }

        assertEquals(5, lruCacheMap.size());
    }

    @Test
    void testMultiThreaded() throws InterruptedException {
        final int cacheSize = 5;
        Map<Codec, Codec> lruCacheMap = new LRUCacheMap<>(cacheSize, 0, 0);

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        for (int i = 0; i < NUM_THREADS; i++) {
            final int threadNumber = i;
            executor.submit(() -> {
                try {
                    lruCacheMap.put(new JsonCodecWrapper(CUSTOMER_CODEC), new JsonCodecWrapper(CUSTOMER_CODEC));
                } catch (Exception e) {
                    log.error("Thread {} failed: ", threadNumber, e);
                    throw e;
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        assertEquals(cacheSize, lruCacheMap.size());
    }
}