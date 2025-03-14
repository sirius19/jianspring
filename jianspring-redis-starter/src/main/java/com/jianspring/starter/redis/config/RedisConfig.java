package com.jianspring.starter.redis.config;

import com.jianspring.starter.redis.holder.RedisHolder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: InfoInsights
 * @Date: 2023/3/3 下午2:31
 * @Version: 1.0.0
 */
@Configuration
@ConditionalOnMissingBean(RedisProperties.class)
public class RedisConfig {

    @Bean
    RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    RedisHolder redisHolder(RedisTemplate<String, Object> redisTemplate) {
        return new RedisHolder(redisTemplate);
    }

    @Bean
    @ConditionalOnProperty(name = "spring.redis.cluster.nodes")
    public RedisConnectionFactory redisConnectionFactory(RedisProperties redisProperties) {
        RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration();

        // 设置集群节点
        String[] nodes = redisProperties.getCluster().getNodes().toArray(new String[0]);
        clusterConfig.setClusterNodes(getClusterNodes(nodes));

        // 设置最大重定向次数
        clusterConfig.setMaxRedirects(redisProperties.getCluster().getMaxRedirects());

        // 设置密码
        if (StringUtils.hasText(redisProperties.getPassword())) {
            clusterConfig.setPassword(RedisPassword.of(redisProperties.getPassword()));
        }

        return new LettuceConnectionFactory(clusterConfig);
    }

    private List<RedisNode> getClusterNodes(String[] nodes) {
        return Arrays.stream(nodes)
                .map(node -> {
                    String[] parts = node.split(":");
                    return new RedisNode(parts[0], Integer.parseInt(parts[1]));
                })
                .collect(Collectors.toList());
    }

}