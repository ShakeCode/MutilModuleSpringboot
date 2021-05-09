package com.redis.service.config;

import com.redis.service.config.redission.RedisClusterProperties;
import com.redis.service.config.redission.RedisPoolProperties;
import com.redis.service.config.redission.RedisSentinelProperties;
import com.redis.service.config.redission.RedisSingleProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.redis", ignoreUnknownFields = false)
@Data
public class RedisProperties {
    private String host;

    private int maxRedirects;

    private int database;

    /**
     * 等待节点回复命令的时间。该时间从命令发送成功时开始计时
     */
    private int timeout;

    private String password;

    private String mode;

    /**
     * 池配置
     */
    private RedisPoolProperties pool;

    /**
     * 单机信息配置
     */
    private RedisSingleProperties single;

    /**
     * 集群 信息配置
     */
    private RedisClusterProperties cluster;

    /**
     * 哨兵配置
     */

    private RedisSentinelProperties sentinel;
}
