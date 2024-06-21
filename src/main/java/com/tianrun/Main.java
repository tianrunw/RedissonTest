package com.tianrun;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.redisson.Redisson;
import org.redisson.api.RJsonBucket;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JacksonCodec;
import org.redisson.config.Config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final Logger log = LogManager.getLogger();

    private static final int NUM_THREADS = 10;
    private static final JacksonCodec<Customer> CUSTOMER_CODEC = new JacksonCodec<>(Customer.class);

    public static void main(String[] args) throws InterruptedException {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");

        RedissonClient redisson = Redisson.create(config);
        setBucket(redisson);

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        try {
            log.info("Execution start");

            for (int i = 0; i < NUM_THREADS; i++) {
                final int threadNumber = i;
                executor.submit(() -> {
                    try {
                        getBucket(redisson, threadNumber);
                    } catch (Exception e) {
                        log.error("Thread {} failed: ", threadNumber, e);
                        throw new RuntimeException(e);
                    } finally {
                        log.info("Thread {} finished", threadNumber);
                    }
                });
            }

            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.HOURS);

        } finally {
            redisson.shutdown();
        }
    }

    private static void getBucket(RedissonClient redisson, int threadNumber) throws InterruptedException {
        long iter = 0;
        while (true) {
            try {
                RJsonBucket<Customer> bucket = redisson.getJsonBucket("w", CUSTOMER_CODEC);
                Customer customer = bucket.get();
                if (iter % 10000 == 0) {
                    log.info("Thread: {}, iter: {}, customer: {}", threadNumber, iter, customer);
                }
            } catch (Exception e) {
                log.error("bucket.get() failed in thread {}:", threadNumber, e);
            }
            iter++;
            Thread.sleep(1);
        }
    }

    private static void setBucket(RedissonClient redisson) {
        RJsonBucket<Customer> bucket = redisson.getJsonBucket("w", CUSTOMER_CODEC);
        bucket.set(new Customer("Tianrun", "Wang"));
    }
}
