package com.redis.service.lock.lua;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * The type Lua redis lock util.
 */
@Component
public class LuaRedisLockUtil {
    private final static Logger logger = LoggerFactory.getLogger(LuaRedisLockUtil.class);

    /**
     * The Redis template.
     */
    public final StringRedisTemplate redisTemplate;

    private static final Long SUCCESS = 1L;

    /**
     * 锁的过期时间(s)
     */
    private static final int LOCK_EXPIRE_TIME = 10;

    /**
     * 最大尝试次数
     */
    private static final int MAX_ATTEMPTS = 10;

    private static final String KEY_PRE = "REDIS_LOCK_";

    private static final DateFormat DF = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    /**
     * Instantiates a new Lua redis lock util.
     * @param redisTemplate the redis template
     */
    public LuaRedisLockUtil(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Get string.
     * @param key the key
     * @return the string
     */
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Set.
     * @param key   the key
     * @param value the value
     */
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 尝试加锁
     * @param key the key
     * @return string string
     */
    public String tryLock(String key) {
        return tryLock(key, MAX_ATTEMPTS);
    }

    /**
     * 尝试加锁
     * @param key          the key
     * @param max_attempts the max attempts
     * @return string string
     */
    public String tryLock(String key, Integer max_attempts) {
        int attempt_counter = 0;
        key = KEY_PRE + key;
        String value = fetchLockValue();
        while (attempt_counter < max_attempts) {
            if (SUCCESS.equals(lockSet(key, value))) {
                System.out.println("Redis Lock key : " + key + ", value : " + value);
                return value;
            }
            System.out.println("Redis lock failure, waiting try next, " + key);
            attempt_counter++;
            if (attempt_counter >= max_attempts) {
                logger.error("Redis lock failure, " + key);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.error("try lock fail");
            }
        }
        return null;
    }

    /**
     * Lock set long.
     * @param key   the key
     * @param value the value
     * @return the long
     */
    public Long lockSet(String key, String value) {
        Long result = null;
        try {
            String script = "if redis.call('setNx',KEYS[1],KEYS[2]) then if redis.call('get',KEYS[1])==KEYS[2] then return redis.call('expire',KEYS[1],KEYS[3]) else return 0 end end";
            RedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
            List<String> keys = Arrays.asList(key, value, String.valueOf(LOCK_EXPIRE_TIME));
            result = redisTemplate.execute(redisScript, keys);
        } catch (Exception e) {
            logger.error("lock set fail");
        }
        return result;
    }

    /**
     * 解锁
     * @param key   the key
     * @param value the value
     * @return boolean boolean
     */
    public boolean unLock(String key, String value) {
        key = KEY_PRE + key;
        try {
            String script = "if redis.call('get', KEYS[1]) == KEYS[2] then return redis.call('del', KEYS[1]) else return 0 end";
            RedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
            List<String> keys = Arrays.asList(key, value);
            Object result = redisTemplate.execute(redisScript, keys);
            if (SUCCESS.equals(result)) {
                System.out.println("Redis unLock key : " + key + ", value : " + value);
                return true;
            }
        } catch (Exception e) {
            logger.error("unlock fail");
        }

        return false;
    }

    /**
     * 生成加锁的唯一字符串
     * @return 唯一字符串
     */
    private String fetchLockValue() {
        return UUID.randomUUID().toString() + "_" + DF.format(new Date());
    }
}
