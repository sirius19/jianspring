package com.jianspring.starter.cloud.interceptor;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;


public class RequestCacheService {

    // 初始化 Caffeine 缓存，设置过期时间和最大容量
    private final Cache<String, Boolean> requestCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES) // 设置缓存10分钟过期
            .maximumSize(100000) // 设置最大容量10万
            .build();

    /**
     * 检查请求是否重复
     *
     * @param requestId 请求的唯一标识
     * @param url       请求的URL
     * @return true 如果请求已经存在，false 如果请求是新的
     */
    public boolean isDuplicateRequest(String requestId, String url) {
        // 构造缓存的键，组合 requestId 和 url
        String cacheKey = requestId + "::" + url;

        // 尝试从缓存中获取
        Boolean existing = requestCache.getIfPresent(cacheKey);
        if (existing != null) {
            // 如果缓存中已经存在，返回 true 表示重复请求
            return true;
        } else {
            // 如果不存在，将请求信息放入缓存，并放行请求
            requestCache.put(cacheKey, true);
            return false;
        }
    }
}

