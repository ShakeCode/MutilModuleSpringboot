package com.redis.service.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * The interface Redis lock.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RedisLock {

    /**
     * Prefix string.
     * @return the string
     */
    String prefix();

    /**
     * Lock key string.
     * @return the string
     */
    String lockKey();

    /**
     * Wait time long.
     * @return the long
     */
    long waitTime() default 6000;

    /**
     * Lease time long.
     * @return the long
     */
    long leaseTime() default 60000;

    /**
     * Time unit time unit.
     * @return the time unit
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

}