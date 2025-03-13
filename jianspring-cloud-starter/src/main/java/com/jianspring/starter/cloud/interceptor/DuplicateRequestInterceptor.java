package com.jianspring.starter.cloud.interceptor;

import com.jianspring.starter.commons.error.CommonErrorCode;
import com.jianspring.starter.commons.exception.BizException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class DuplicateRequestInterceptor implements HandlerInterceptor {

    private final RequestCacheService requestCacheService;

    // 构造器注入 RequestCacheService
    public DuplicateRequestInterceptor(RequestCacheService requestCacheService) {
        this.requestCacheService = requestCacheService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求的 URL 和 requestId（这里假设从 header 中获取）
        String requestId = request.getHeader("requestId"); // 可以根据实际情况更改
        String url = request.getRequestURI();
        String contextPath = request.getContextPath();
        // 仅拦截以 /api 开头的请求
        if ((url.startsWith(contextPath + "/auth") || url.startsWith(contextPath + "/admin")
                || url.startsWith(contextPath + "/customer"))
                && (requestId == null || requestCacheService.isDuplicateRequest(requestId, url))) {
            throw BizException.of(CommonErrorCode.PARAM_ERROR);
        }

        return true; // 放行请求
    }
}

