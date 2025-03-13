package com.jianspring.starter.lock;

import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@AutoConfigureAfter(name = "org.redisson.spring.starter.RedissonAutoConfiguration")
@EnableAspectJAutoProxy
@EnableConfigurationProperties(RedissonLockProperties.class)
public class RedissonLockAutoConfiguration {
    private final RedissonLockProperties properties;

    public RedissonLockAutoConfiguration(RedissonLockProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public RedissonLock redissonLock(RedissonClient redisson) {
        return new RedissonLock(redisson);
    }

    @Bean
    @ConditionalOnMissingBean
    public DistributedLockHandler distributedLockHandler(RedissonLock redissonLock) {
        return new DistributedLockHandler(redissonLock);
    }
}

