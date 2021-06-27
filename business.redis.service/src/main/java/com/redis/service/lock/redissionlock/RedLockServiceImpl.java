package com.redis.service.lock.redissionlock;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * The type Red lock service.
 */
@Service
public class RedLockServiceImpl implements RedLockService {

    private final RedissonClient redissonClient;

    /**
     * Instantiates a new Red lock service.
     * @param redissonClient the redisson client
     */
    public RedLockServiceImpl(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 可重入,必须手动解锁!线程不主动解锁将会永远存在! 慎用
     * Acquires the lock.
     *
     * <p>If the lock is not available then the current thread becomes
     * disabled for thread scheduling purposes and lies dormant until the
     * lock has been acquired.
     *
     * <p><b>Implementation Considerations</b>
     *
     * <p>A {@code Lock} implementation may be able to detect erroneous use
     * of the lock, such as an invocation that would cause deadlock, and
     * may throw an (unchecked) exception in such circumstances.  The
     * circumstances and the exception type must be documented by that
     * {@code Lock} implementation.
     * @param lockKey
     */
    @Override
    public void lock(String lockKey) {
        RLock lock1 = redissonClient.getLock(lockKey);
        redissonClient.getRedLock(lock1).lock();
    }

    /**
     * 可重入,必须手动解锁或等待leaseTime超时
     * Acquires the lock.
     *
     * <p>If the lock is not available then the current thread becomes
     * disabled for thread scheduling purposes and lies dormant until the
     * lock has been acquired.
     * <p>
     * If the lock is acquired, it is held until <code>unlock</code> is invoked,
     * or until leaseTime milliseconds have passed
     * since the lock was granted - whichever comes first.
     * @param lockKey
     * @param leaseTime MILLISECONDS the maximum time to hold the lock after granting it,
     *                  before automatically releasing it if it hasn't already been released by invoking <code>unlock</code>.
     */
    @Override
    public void lock(String lockKey, long leaseTime, TimeUnit timeUnit) {
        RLock lock1 = redissonClient.getLock(lockKey);
        redissonClient.getRedLock(lock1).lock(leaseTime, timeUnit);
    }

    /**
     * Returns <code>true</code> as soon as the lock is acquired.
     * If the lock is currently held by another thread in this or any
     * other process in the distributed system this method keeps trying
     * to acquire the lock for up to <code>waitTime</code> before
     * giving up and returning <code>false</code>. If the lock is acquired,
     * it is held until <code>unlock</code> is invoked, or until <code>leaseTime</code>
     * have passed since the lock was granted - whichever comes first.
     * @param lockKey
     * @param waitTime  the maximum time to aquire the lock  MILLISECONDS
     * @param leaseTime lease time MILLISECONDS
     * @return <code>true</code> if lock has been successfully acquired
     * @throws InterruptedException - if the thread is interrupted before or during this method.
     */
    @Override
    public boolean tryLockTimeout(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) throws InterruptedException {
        RLock lock1 = redissonClient.getLock(lockKey);
        return redissonClient.getRedLock(lock1).tryLock(waitTime, leaseTime, timeUnit);
    }

    /**
     * Releases the lock.
     *
     * <p><b>Implementation Considerations</b>
     *
     * <p>A {@code Lock} implementation will usually impose
     * restrictions on which thread can release a lock (typically only the
     * holder of the lock can release it) and may throw
     * an (unchecked) exception if the restriction is violated.
     * Any restrictions and the exception
     * type must be documented by that {@code Lock} implementation.
     * @param lockKey
     */
    @Override
    public void unLock(String lockKey) {
        RLock lock1 = redissonClient.getLock(lockKey);
        redissonClient.getRedLock(lock1).unlock();
    }
}
