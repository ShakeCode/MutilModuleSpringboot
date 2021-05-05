package com.redis.service.config;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;

/**
 * The type Redis cache config.
 */
@Configuration
public class RedisCacheConfig {

    /**
     * 设置cache内数据的缓存时间，只会影响注解生成的数据比如@Cacheable，并不会对自己创建的K-V产生影响，
     * @param redisTemplate the redis template
     * @return redis cache manager
     */
    @Bean(name = "redisCacheManager")
    public RedisCacheManager redisCacheManager(RedisTemplate redisTemplate) {
        RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate);
        //默认使用cacheNames作为key的前缀
        cacheManager.setUsePrefix(true);
        //设置缓存过期时间(秒)
        cacheManager.setDefaultExpiration(30);
        return cacheManager;
    }

    /**
     * 配置cache的key的生成规则
     * @return key generator
     */
    @Bean("myKeyGenerator")
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> method.getName() + "[" + Arrays.asList(params).toString() + "]";
    }
}
