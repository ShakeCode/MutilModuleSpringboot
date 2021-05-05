package com.redis.service.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.HashMap;
import java.util.Map;

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

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.max-redirects}")
    private String redirects;

    @Value("${spring.redis.timeout}")
    private String timeout;

    @Value("${spring.redis.password}")
    private String password;

    /**
     * Redis template redis template.
     * @param factory the factory
     * @return the redis template
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
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
     * Gets cluster configuration.
     * @return the cluster configuration
     * @Description:redis集群配置
     */
    @Bean
    public RedisClusterConfiguration getClusterConfiguration() {
        if (host.split(",").length > 1) {
            //如果是host是集群模式的才进行以下操作
            Map<String, Object> source = new HashMap<String, Object>();
            source.put("spring.redis.cluster.nodes", host);
            source.put("spring.redis.cluster.timeout", timeout.replace("ms",""));
            source.put("spring.redis.cluster.max-redirects", redirects);
            source.put("spring.redis.cluster.password", password);
            return new RedisClusterConfiguration(new MapPropertySource("RedisClusterConfiguration", source));
        } else {
            return null;
        }

    }

    /**
     * Jedis connection factory jedis connection factory.
     * @return the jedis connection factory
     * @Description:集群遍历
     */
    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        if (host.split(",").length == 1) {
            JedisConnectionFactory factory = new JedisConnectionFactory();
            factory.setHostName(host.split(":")[0]);
            factory.setPort(Integer.valueOf(host.split(":")[1]));
            factory.setPassword(password);
            factory.setTimeout(Integer.parseInt(timeout.replace("ms","")));
            return factory;
        } else {
            JedisConnectionFactory jcf = new JedisConnectionFactory(getClusterConfiguration());
            jcf.setPassword(password); // 集群的密码认证
            return jcf;
        }
    }
}
