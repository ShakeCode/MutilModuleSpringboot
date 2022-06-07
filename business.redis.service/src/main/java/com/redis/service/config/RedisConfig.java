package com.redis.service.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * linux-redis服务端下载：
 * https://redis.io/download
 * https://download.redis.io/releases/?_ga=2.161743292.1681425255.1620142059-1825229395.1585492813
 * <p>
 * windows 版本下载：
 * https://github.com/MicrosoftArchive/redis/tags
 * <p>
 * org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
 * The type Redis config.自定义redis配置, 默认配置类-RedisAutoConfiguration (已初始化好RedisTemplate和一个StringRedisTemplate,但未确定类型)
 */
@Configuration
public class RedisConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.max-redirects}")
    private Integer redirects;

    @Value("${spring.redis.timeout}")
    private String timeout;

    @Value("${spring.redis.password}")
    private String password;

    private final RedisProperties redisProperties;


    /**
     * Instantiates a new Redis config.
     * @param redisProperties the redis properties
     */
    public RedisConfig(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    /**
     * Redis template redis template.
     * @param factory the factory
     * @return the redis template
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(@Qualifier("jedisConnectionFactory") RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = getJackson2JsonRedisSerializer();
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // key采用String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // value序列化方式采用jackson
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // hash的key也采用String的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        // hash的value序列化方式采用jackson
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }

    private Jackson2JsonRedisSerializer getJackson2JsonRedisSerializer() {
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        return jackson2JsonRedisSerializer;
    }


    /**
     * Jedis connection factory jedis connection factory.
     * @return the jedis connection factory
     */
    @Bean("jedisConnectionFactory")
    public JedisConnectionFactory jedisConnectionFactory() {
        if (org.apache.commons.lang3.StringUtils.equals(redisProperties.getMode(), Constant.RedisMode.SINGLE)) {
            LOGGER.info("init jedisConnectionFactory Single mode...host:{}", host);
            JedisConnectionFactory factory = new JedisConnectionFactory();
            factory.setHostName(host.split(Constant.COLON)[0]);
            factory.setPort(Integer.parseInt(host.split(Constant.COLON)[1]));
            factory.setPassword(password);
            factory.setTimeout(Integer.parseInt(timeout.replace("ms", "")));
            return factory;
        } else if (org.apache.commons.lang3.StringUtils.equals(redisProperties.getMode(), Constant.RedisMode.CLUSTER)) {
            LOGGER.info("init jedisConnectionFactory Cluster mode...");
            JedisConnectionFactory factory = new JedisConnectionFactory(getClusterConfiguration());
            // 集群的密码认证
            factory.setPassword(password);
            factory.setTimeout(Integer.parseInt(timeout.replace("ms", "")));
            return factory;
        } else if (org.apache.commons.lang3.StringUtils.equals(redisProperties.getMode(), Constant.RedisMode.SENTINEL)) {
            LOGGER.info("init jedisConnectionFactory Sentinel mode...");
            JedisConnectionFactory factory = new JedisConnectionFactory(getSentinelConfiguration());
            factory.setPassword(password);
            factory.setTimeout(Integer.parseInt(timeout.replace("ms", "")));
            return factory;
        } else {
            throw new ServiceException("unsupport redis mode");
        }
    }

    @Bean
    public BloomFilterHelper<String> initBloomFilterHelper() {
        return new BloomFilterHelper<>((Funnel<String>) (from, into) -> into.putString(from, Charsets.UTF_8)
                .putString(from, Charsets.UTF_8), 1000000, 0.01);
    }

    /**
     * Gets cluster configuration.
     * @return the cluster configuration
     */
    public RedisClusterConfiguration getClusterConfiguration() {
        LOGGER.info("init Redis Cluster Configuration...nodes:{}", redisProperties.getCluster().getNodes());
        /*  Map<String, Object> source = new HashMap<String, Object>(2);
            source.put("spring.redis.cluster.nodes", redisProperties.getCluster().getNodes());
            source.put("spring.redis.cluster.max-redirects", redirects);
            return new RedisClusterConfiguration(new MapPropertySource("RedisClusterConfiguration", source));*/
        List<RedisNode> nodes = new ArrayList<>();
        for (String node : redisProperties.getCluster().getNodes().split(Constant.COMMA)) {
            String[] parts = StringUtils.split(node, Constant.COLON);
            nodes.add(new RedisNode(parts[0], Integer.parseInt(parts[1])));
        }
        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration();
        redisClusterConfiguration.setClusterNodes(nodes);
        redisClusterConfiguration.setMaxRedirects(redirects);
        return redisClusterConfiguration;
    }

    /**
     * Gets sentinel configuration.
     * @return the sentinel configuration
     */
    public RedisSentinelConfiguration getSentinelConfiguration() {
        LOGGER.info("init Redis Sentinel Configuration...nodes:{}", redisProperties.getSentinel().getNodes());
        LOGGER.info("init Redis Sentinel Configuration...master:{}", redisProperties.getSentinel().getMaster());
     /*     Map<String, Object> source = new HashMap<String, Object>(2);
            source.put("spring.redis.sentinel.master", redisProperties.getSentinel().getMaster());
            source.put("spring.redis.sentinel.nodes", redisProperties.getSentinel().getNodes());
            return new RedisSentinelConfiguration(new MapPropertySource("RedisSentinelConfiguration", source));*/
        List<RedisNode> nodes = new ArrayList<>();
        for (String node : redisProperties.getSentinel().getNodes().split(Constant.COMMA)) {
            String[] parts = StringUtils.split(node, Constant.COLON);
            nodes.add(new RedisNode(parts[0], Integer.parseInt(parts[1])));
        }
        RedisSentinelConfiguration redisSentinelConfiguration = new RedisSentinelConfiguration();
        redisSentinelConfiguration.setMaster(redisProperties.getSentinel().getMaster());
        redisSentinelConfiguration.setSentinels(nodes);
        return redisSentinelConfiguration;
    }

    /**
     * Init bloom filter helper bloom filter helper.
     * @return the bloom filter helper
     */
    @Bean
    public BloomFilterHelper<String> initBloomFilterHelper() {
        return new BloomFilterHelper<>((Funnel<String>) (from, into) -> into.putString(from, Charsets.UTF_8)
                .putString(from, Charsets.UTF_8), 1000000, 0.01);
    }
}
