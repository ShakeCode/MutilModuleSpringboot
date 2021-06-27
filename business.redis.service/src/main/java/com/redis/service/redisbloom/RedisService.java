package com.redis.service.redisbloom;

import com.google.common.base.Preconditions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * The type Redis service.
 */
@Service
public class RedisService {


    private final RedisTemplate redisTemplate;

    /**
     * Instantiates a new Redis service.
     * @param redisTemplate the redis template
     */
    public RedisService(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Add value according to the given Bloom filter
     * @param <T>               the type parameter
     * @param bloomFilterHelper the bloom filter helper
     * @param key               the key
     * @param value             the value
     */
    public <T> void addByBloomFilter(BloomFilterHelper<T> bloomFilterHelper, String key, T value) {
        Preconditions.checkArgument(bloomFilterHelper != null, "bloomFilterHelper cannot be empty");
        int[] offset = bloomFilterHelper.murmurHashOffset(value);
        for (int i : offset) {
            //System.out.println("key : " + key + " " + "value : " + i);
            redisTemplate.opsForValue().setBit(key, i, true);
        }
    }

    /**
     * Determine whether the value exists according to the given Bloom filter
     * @param <T>               the type parameter
     * @param bloomFilterHelper the bloom filter helper
     * @param key               the key
     * @param value             the value
     * @return the boolean
     */
    public <T> boolean includeByBloomFilter(BloomFilterHelper<T> bloomFilterHelper, String key, T value) {
        Preconditions.checkArgument(bloomFilterHelper != null, "bloomFilterHelper cannot be empty");
        int[] offset = bloomFilterHelper.murmurHashOffset(value);
        for (int i : offset) {
            //System.out.println("key : " + key + " " + "value : " + i);
            if (!redisTemplate.opsForValue().getBit(key, i)) {
                return false;
            }
        }
        return true;
    }
}
