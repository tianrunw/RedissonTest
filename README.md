# RedissonTest

## Versions

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

## Change

Redisson change log: https://github.com/redisson/redisson/blob/master/CHANGELOG.md

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
