package com.jianspring.starter.redis.enums;


import lombok.Getter;
import lombok.ToString;

import java.util.concurrent.TimeUnit;

/**
 * @Author:  InfoInsights
 * @Date: 2023/3/3 下午1:42
 * @Version: 1.0.0
 */
@Getter
@ToString
public enum CommonRedisKeyEnum implements IRedisKey {

    COMMON_BUSINESS_KEY_FIRST("1001:", 1, 3, TimeUnit.SECONDS, "公共的redis key"),
    ;

    private final String key;

    private final Object defaultValue;

    private final long ttl;

    private final TimeUnit timeUnit;

    private final String desc;

    CommonRedisKeyEnum(String key, Object defaultValue, long ttl, TimeUnit timeUnit, String desc) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.ttl = ttl;
        this.timeUnit = timeUnit;
        this.desc = desc;
    }

    @Override
    public String getPrefixKey() {
        return "0000:" + key;
    }

}