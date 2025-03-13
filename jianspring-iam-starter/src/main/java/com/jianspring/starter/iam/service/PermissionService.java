package com.jianspring.starter.iam.service;

import com.jianspring.starter.commons.UserContextUtils;
import java.util.Set;

public interface PermissionService {
    /**
     * 获取用户权限集合
     */
    Set<String> getUserPermissions(Long userId);
}