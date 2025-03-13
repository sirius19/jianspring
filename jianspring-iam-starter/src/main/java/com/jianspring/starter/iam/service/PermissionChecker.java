package com.jianspring.starter.iam.service;

import com.jianspring.starter.commons.UserContextUtils;
import com.jianspring.starter.iam.annotation.Logical;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class PermissionChecker {

    private final PermissionService permissionService;

    public PermissionChecker(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public boolean hasPermission(String[] permissions, Logical logical) {
        if (permissions.length == 0) {
            return true;
        }

        UserContextUtils.UserContext user = UserContextUtils.get();
        if (user == null || user.getUserId() == null || user.getUserId() == 0L) {
            return false;
        }

        Set<String> userPermissions = permissionService.getUserPermissions(user.getUserId());
        if (userPermissions == null || userPermissions.isEmpty()) {
            return false;
        }

        if (logical == Logical.OR) {
            for (String permission : permissions) {
                if (userPermissions.contains(permission)) {
                    return true;
                }
            }
            return false;
        } else {
            for (String permission : permissions) {
                if (!userPermissions.contains(permission)) {
                    return false;
                }
            }
            return true;
        }
    }
}