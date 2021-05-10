package com.redis.service.lock.lua;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * The type Lua redis lock.
 */
@Service
public class LuaRedisLock {
    private final Logger logger = LoggerFactory.getLogger(LuaRedisLock.class);

    private static final ThreadLocal<String> LOCAL_REQUEST_IDS = new InheritableThreadLocal<>();

    private static final ThreadLocal<String> LOCAL_KEYS = new InheritableThreadLocal<>();

    private static final String LOCK_PREFIX = "lua_lock_";

    private static final Long LOCK_SUCCESS = 1L;

    private static final Long RELEASE_SUCCESS = 1L;

    private static final Long LOCK_FAIL = 0L;

    private static final Long LOCK_EXPIRED = -1L;

    private final RedisTemplate redisTemplate;

    /**
     * Instantiates a new Lua redis lock.
     * @param redisTemplate the redis template
     */
    public LuaRedisLock(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 定义释放锁的lua脚本
    private final static DefaultRedisScript<Long> UNLOCK_LUA_SCRIPT = new DefaultRedisScript<>(
            "if redis.call(\"get\",KEYS[1]) == KEYS[2] then return redis.call(\"del\",KEYS[1]) else return -1 end"
            , Long.class
    );

    // 定义获取锁的lua脚本
    private final static DefaultRedisScript<Long> LOCK_LUA_SCRIPT = new DefaultRedisScript<>(
            "if redis.call(\"setnx\", KEYS[1], KEYS[2]) == 1 then return redis.call(\"pexpire\", KEYS[1], KEYS[3]) else return 0 end"
            , Long.class
    );

    /**
     * 获取RedisKey
     * @param key 原始KEY，如果为空，自动生成随机KEY
     * @return KEY
     */
    private String getRedisKey(String key) {
        // 如果Key为空且线程已经保存，直接用，异常保护
        if (StringUtils.isEmpty(key) && !StringUtils.isEmpty(LOCAL_KEYS.get())) {
            return LOCAL_KEYS.get();
        }
        //如果都是空那就抛出异常
        if (StringUtils.isEmpty(key) && StringUtils.isEmpty(LOCAL_KEYS.get())) {
            throw new RuntimeException("key is null");
        }
        return LOCK_PREFIX + key;
    }

    /**
     * 获取随机请求ID
     * @return getRequestId
     */
    private String getRequestId() {
        return Thread.currentThread().getId() + "#" + UUID.randomUUID().toString();
    }

    /**
     * 加锁
     * @param key         Key
     * @param expiredTime 过期时间
     * @param retryTimes  重试次数
     * @return boolean boolean
     */
    public boolean lock(String key, long expiredTime, int retryTimes) {
        try {
            final String redisKey = this.getRedisKey(key);
            final String requestId = this.getRequestId();
            logger.info("lock :::: requestid:{},redisKey:{}", requestId, redisKey);
            // 组装lua脚本参数,lua必须传入数组、列表
            List<String> keys = Arrays.asList(redisKey, requestId, String.valueOf(expiredTime));
            // 执行脚本
            Object result = redisTemplate.execute(LOCK_LUA_SCRIPT, keys);
            // 存储本地变量
            if (LOCK_SUCCESS.equals(result)) {
                LOCAL_REQUEST_IDS.set(requestId);
                LOCAL_KEYS.set(redisKey);
                logger.info("success to acquire lock:{}, Status code reply:{}", Thread.currentThread().getName(), result);
                return true;
            } else if (retryTimes == 0) {
                // 重试次数为0直接返回失败
                return false;
            } else {
                // 重试获取锁
                logger.info("{} retry to acquire lock, Status code reply:{}", Thread.currentThread().getName(), result);
                int count = 0;
                while (true) {
                    try {
                        // 休眠一定时间后再获取锁，这里时间可以通过外部设置
                        Thread.sleep(100);
                        result = redisTemplate.execute(LOCK_LUA_SCRIPT, keys);
                        if (LOCK_SUCCESS.equals(result)) {
                            LOCAL_REQUEST_IDS.set(requestId);
                            LOCAL_KEYS.set(redisKey);
                            logger.info("{} success to acquire lock, Status code reply:{}", Thread.currentThread().getName(), result);
                            return true;
                        } else {
                            count++;
                            if (retryTimes == count) {
                                logger.info("{} fail to acquire lock, Status code reply: {}", Thread.currentThread().getName(), result);
                                return false;
                            } else {
                                logger.info("{} times try to acquire lock for {}, Status code reply: {}", count, Thread.currentThread().getName(), result);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("{} acquire redis occured an exception", Thread.currentThread().getName(), e);
                        break;
                    }
                }
            }
        } catch (Exception e1) {
            logger.error("{} acquire redis occured an exception:", Thread.currentThread().getName(), e1);
        }
        return false;
    }

    /**
     * 释放KEY
     * @param key the key
     * @return boolean boolean
     */
    public boolean unlock(String key) {
        try {
            String localKey = LOCAL_KEYS.get();
            // 如果本地线程没有KEY，说明还没加锁，不能释放
            if (StringUtils.isEmpty(localKey)) {
                logger.error("release lock occured an error: lock key not found");
                return false;
            }
            String redisKey = getRedisKey(key);
            // 判断KEY是否正确，不能释放其他线程的KEY
            if (!StringUtils.isEmpty(localKey) && !localKey.equals(redisKey)) {
                logger.error("release lock occured an error: illegal key:" + key);
                return false;
            }
            logger.info("unlock :::: redisKey:{},requestid:{}", redisKey, LOCAL_REQUEST_IDS.get());
            // 组装lua脚本参数
            List<String> keys = Arrays.asList(redisKey, LOCAL_REQUEST_IDS.get());
            // 使用lua脚本删除redis中匹配value的key，可以避免由于方法执行时间过长而redis锁自动过期失效的时候误删其他线程的锁
            Object result = redisTemplate.execute(UNLOCK_LUA_SCRIPT, keys);
            // 如果这里抛异常，后续锁无法释放
            if (result == RELEASE_SUCCESS) {
                logger.info("{} release lock success, Status code reply:{}", Thread.currentThread().getName(), result);
                return true;
            } else if (result == LOCK_EXPIRED) {
                // 返回-1说明获取到的KEY值与requestId不一致或者KEY不存在，可能已经过期或被其他线程加锁
                // 一般发生在key的过期时间短于业务处理时间，一般属于正常可接受情况,涉及严格事务则不满足
                logger.info("{} release lock exception, key has expired or released. Status code reply:{}", Thread.currentThread().getName(), result);
            } else {
                // 其他情况，一般是删除KEY失败，返回0
                logger.error("{}:release lock failed, del key failed. Status code reply:{}", Thread.currentThread().getName(), result);
            }
        } catch (Exception e) {
            logger.error("release lock occured an exception", e);
        } finally {
            //清除本地变量
            this.clean();
        }
        return false;
    }

    /**
     * 清除本地线程变量，防止内存泄露
     */
    private void clean() {
        LOCAL_REQUEST_IDS.remove();
        LOCAL_KEYS.remove();
    }
}
