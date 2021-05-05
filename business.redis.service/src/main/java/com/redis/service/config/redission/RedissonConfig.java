package com.redis.service.config.redission;

import com.redis.service.config.RedisProperties;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.ReadMode;
import org.redisson.config.SentinelServersConfig;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 官网地址：
 * https://github.com/redisson/redisson/wiki/2.-%E9%85%8D%E7%BD%AE%E6%96%B9%E6%B3%95#23-%E5%B8%B8%E7%94%A8%E8%AE%BE%E7%BD%AE
 * <p>
 * 哨兵模式：
 * http://redis.cn/topics/sentinel.html
 * <p>
 * 没有密码设置需要设置redis.config 关闭保护模式：protected-mode no
 * <p>
 * 修改集群启动：（否则报错：Can't connect to servers! ERR This instance has cluster support disabled）
 * 找到文档redis.config 去掉前面的#号
 * # cluster-enabled yes
 * <p>
 * 挂载配置文件启动：redis-server.exe  redis.windows.conf
 * <p>
 * https://blog.csdn.net/Sibylsf/article/details/105820463
 *
 * https://blog.csdn.net/weixin_34418883/article/details/88114907
 *
 * <p>
 * The type Redisson config.
 */
@EnableConfigurationProperties(RedisProperties.class)
@Configuration
@ConditionalOnClass({Redisson.class})
@ConditionalOnExpression("'${spring.redis.mode}'=='single' or '${spring.redis.mode}'=='cluster' or '${spring.redis.mode}'=='sentinel'")
public class RedissonConfig {
    private final RedisProperties redisProperties;

    /**
     * Instantiates a new Redisson config.
     * @param redisProperties the redis properties
     */
    public RedissonConfig(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    @Bean
    @ConditionalOnProperty(name = "spring.redis.mode", havingValue = "single")
    RedissonClient redissonSingle() {
        Config config = new Config();
        String node = redisProperties.getSingle().getAddress();
        node = node.startsWith("redis://") ? node : "redis://" + node;
        SingleServerConfig serverConfig = config.useSingleServer()
                .setAddress(node)
                .setTimeout(redisProperties.getPool().getConnTimeout())
                .setConnectionPoolSize(redisProperties.getPool().getSize())
                .setConnectionMinimumIdleSize(redisProperties.getPool().getMinIdle());
        if (StringUtils.isNotBlank(redisProperties.getPassword())) {
            serverConfig.setPassword(redisProperties.getPassword());
        }
        return Redisson.create(config);
    }


    @Bean
    @ConditionalOnProperty(name = "spring.redis.mode", havingValue = "cluster")
    RedissonClient redissonCluster() {
        System.out.println("cluster redisProperties:" + redisProperties.getCluster());
        Config config = new Config();
        String[] nodes = redisProperties.getCluster().getNodes().split(",");
        List<String> newNodes = new ArrayList(nodes.length);
        Arrays.stream(nodes).forEach((index) -> newNodes.add(
                index.startsWith("redis://") ? index : "redis://" + index));
         config.useClusterServers()
                .addNodeAddress(newNodes.toArray(new String[0]))
                .setScanInterval(
                        redisProperties.getCluster().getScanInterval())
                .setIdleConnectionTimeout(
                        redisProperties.getPool().getSoTimeout())
                .setConnectTimeout(
                        redisProperties.getPool().getConnTimeout())
                .setFailedAttempts(
                        redisProperties.getCluster().getFailedAttempts())
                .setRetryAttempts(
                        redisProperties.getCluster().getRetryAttempts())
                .setRetryInterval(
                        redisProperties.getCluster().getRetryInterval())
                .setMasterConnectionPoolSize(redisProperties.getCluster()
                        .getMasterConnectionPoolSize())
                .setSlaveConnectionPoolSize(redisProperties.getCluster()
                        .getSlaveConnectionPoolSize())
                .setTimeout(redisProperties.getTimeout());
        if (StringUtils.isNotBlank(redisProperties.getPassword())) {
            config.useClusterServers().setPassword(redisProperties.getPassword());
        }
        return Redisson.create(config);
    }

    /**
     * 哨兵模式
     * Redisson sentinel redisson client.
     * @return the redisson client
     */
    @Bean
    @ConditionalOnProperty(name = "spring.redis.mode", havingValue = "sentinel")
    RedissonClient redissonSentinel() {
        System.out.println("sentinel redisProperties:" + redisProperties.getSentinel());
        Config config = new Config();
        String[] nodes = redisProperties.getSentinel().getNodes().split(",");
        List<String> newNodes = new ArrayList(nodes.length);
        Arrays.stream(nodes).forEach((index) -> newNodes.add(
                index.startsWith("redis://") ? index : "redis://" + index));

        SentinelServersConfig serverConfig = config.useSentinelServers()
                .addSentinelAddress(newNodes.toArray(new String[0]))
                .setMasterName(redisProperties.getSentinel().getMaster())
                .setReadMode(ReadMode.SLAVE)
                .setFailedAttempts(redisProperties.getSentinel().getFailMax())
                .setTimeout(redisProperties.getTimeout())
                .setMasterConnectionPoolSize(redisProperties.getPool().getSize())
                .setSlaveConnectionPoolSize(redisProperties.getPool().getSize());

        if (StringUtils.isNotBlank(redisProperties.getPassword())) {
            serverConfig.setPassword(redisProperties.getPassword());
        }

        return Redisson.create(config);
    }
}
