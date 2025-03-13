package com.jianspring.starter.lock;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jianspring.redisson.lock")
public class RedissonLockProperties {
}
