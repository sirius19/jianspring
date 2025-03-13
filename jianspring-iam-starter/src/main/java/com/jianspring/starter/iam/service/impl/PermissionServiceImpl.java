package com.jianspring.starter.iam.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.jianspring.starter.iam.service.PermissionService;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class PermissionServiceImpl implements PermissionService {
    
    private final Cache<Long, Set<String>> permissionCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build();

    @Override
    public Set<String> getUserPermissions(Long userId) {
        return permissionCache.get(userId, this::loadPermissions);
    }
    
    private Set<String> loadPermissions(Long userId) {
        // TODO: 这里预留远程调用接口
        // return permissionFeignClient.getUserPermissions(userId);
        return new HashSet<>();
    }
}