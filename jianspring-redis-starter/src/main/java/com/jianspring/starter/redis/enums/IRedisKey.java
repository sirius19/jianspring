package com.jianspring.starter.redis.enums;

import java.util.concurrent.TimeUnit;

/**
 * @Author: InfoInsights
 * @Date: 2023/3/3 下午1:48
 * @Version: 1.0.0
 */
public interface IRedisKey {

    default String getLockPrefix() {
        return "jian:";
    }

    String getPrefixKey();

    Object getDefaultValue();

    long getTtl();

    TimeUnit getTimeUnit();

}