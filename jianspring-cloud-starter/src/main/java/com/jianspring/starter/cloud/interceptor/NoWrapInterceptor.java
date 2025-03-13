package com.jianspring.starter.cloud.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class NoWrapInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestURI = request.getRequestURI();

        // 如果设置了 context-path 那么请求路径将是类似 "/api/swagger" 这样的形式
        String contextPath = request.getContextPath();
        if (requestURI.startsWith(contextPath + "/swagger") || requestURI.startsWith(contextPath + "/v3/api-docs")) {
            // 如果是Swagger相关的路径，不进行封装
            request.setAttribute("noWrap", true);
        }

        return true;
    }
}

