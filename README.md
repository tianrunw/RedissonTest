# RedissonTest

This repo reproduces a memory leak issue in Redisson library when `RJsonBucket.get()` is called concurrently.

## Versions
```
3.26.1 (bad)
3.26.0
3.25.2 (bad)
3.25.1 (bad, breaking change, memory leak on RJsonBucket.get() when called concurrently)
3.25.0 (good)
3.24.3 (good)
3.24.2
3.24.1
3.24.0
3.23.5
3.23.4
3.23.3 (good)
```

## Change

Redisson change log: https://github.com/redisson/redisson/blob/master/CHANGELOG.md

Diff between 3.25.0 and 3.25.1: https://github.com/redisson/redisson/compare/redisson-3.25.0...redisson-3.25.1

18-Dec-2023 - 3.25.1 released

Improvement - JDK21 Virtual Threads compatibility

Fixed - EvictionTask keeps running even after destroy() method called
Fixed - Sprint Data Redis throws Subscription registration timeout exceeded
Fixed - Sprint Data Redis RedisMessageListenerContainer.addMessageListener() method hangs if called after container start
Fixed - NPE is thrown if lazyInitialization = true
Fixed - PriorityQueue methods may hang due to unreleased lock after exception
Fixed - RMap.getAll() method throws IndexOutOfBoundsException
Fixed - natMapper isn't applied to slaves and master nodes at start in Sentinel mode
Fixed - method invocation hangs after failover if retryInterval = 0
Fixed - transactional Map and MapCache keySet method returns inconsistent state
Fixed - Multilock lock method doesn't work properly with non-MILLISECONDS TimeUnit


## Steps to reproduce

1. Download and run Redis Stack with Docker. https://redis.io/docs/latest/operate/oss_and_stack/install/install-stack/docker/
2. Run the Main class with IntelliJ Profiler.


## Logs

### UnsafeQueue with onFullLock()

```
2024-06-23 13:49:51.436 [pool-2-thread-8] ERROR com.tianrun.cache.UnsafeQueue - Thread 7 failed, queue size: 12871601, lock hold: 1, lock queue: 9, addCount: 12873019, removeCount: 12872994
java.util.ConcurrentModificationException: null
	at java.util.LinkedHashMap$LinkedHashIterator.nextNode(LinkedHashMap.java:756) ~[?:?]
	at java.util.LinkedHashMap$LinkedKeyIterator.next(LinkedHashMap.java:778) ~[?:?]
	at com.tianrun.cache.UnsafeQueue.onFullLock(UnsafeQueue.java:68) ~[classes/:?]
	at com.tianrun.cache.UnsafeQueue.add(UnsafeQueue.java:37) ~[classes/:?]
	at com.tianrun.cache.UnsafeQueueTest.addInfiniteLoop(UnsafeQueueTest.java:62) ~[test-classes/:?]
	at com.tianrun.cache.UnsafeQueueTest.lambda$testMultiThreaded$0(UnsafeQueueTest.java:39) ~[test-classes/:?]
	at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:539) [?:?]
	at java.util.concurrent.FutureTask.run(FutureTask.java:264) [?:?]
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136) [?:?]
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635) [?:?]
	at java.lang.Thread.run(Thread.java:840) [?:?]
2024-06-23 13:49:51.439 [pool-2-thread-6] ERROR com.tianrun.cache.UnsafeQueue - Thread 5 failed, queue size: 12875336, lock hold: 1, lock queue: 5, addCount: 12876754, removeCount: 12876732
java.util.ConcurrentModificationException: null
	at java.util.LinkedHashMap$LinkedHashIterator.remove(LinkedHashMap.java:769) ~[?:?]
	at com.tianrun.cache.UnsafeQueue.onFullLock(UnsafeQueue.java:69) ~[classes/:?]
	at com.tianrun.cache.UnsafeQueue.add(UnsafeQueue.java:37) ~[classes/:?]
	at com.tianrun.cache.UnsafeQueueTest.addInfiniteLoop(UnsafeQueueTest.java:62) ~[test-classes/:?]
	at com.tianrun.cache.UnsafeQueueTest.lambda$testMultiThreaded$0(UnsafeQueueTest.java:39) ~[test-classes/:?]
	at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:539) [?:?]
	at java.util.concurrent.FutureTask.run(FutureTask.java:264) [?:?]
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136) [?:?]
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635) [?:?]
	at java.lang.Thread.run(Thread.java:840) [?:?]
```
