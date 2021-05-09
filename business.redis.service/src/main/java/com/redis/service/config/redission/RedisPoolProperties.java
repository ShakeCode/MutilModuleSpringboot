package com.redis.service.config.redission;

import lombok.Data;

/**
 * The type Redis pool properties.
 */
@Data
public class RedisPoolProperties {
    private int maxIdle;

    private int minIdle;

    private int maxActive;

    private int maxWait;

    private int connTimeout;

    private int soTimeout;

    private int size;
}
