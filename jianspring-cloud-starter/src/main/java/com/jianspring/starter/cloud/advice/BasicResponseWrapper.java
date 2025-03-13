package com.jianspring.starter.cloud.advice;

import com.jianspring.starter.cloud.annotation.NoWrap;
import com.jianspring.starter.commons.result.ApiResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @Author: InfoInsights
 * @Date: 2023/2/21 上午10:08
 * @Version: 1.0.0
 */
public class BasicResponseWrapper implements HandlerMethodReturnValueHandler {

    private final HandlerMethodReturnValueHandler handlerMethodReturnValueHandler;

    private final BasicResponseProperties basicResponseConfig;

    public BasicResponseWrapper(HandlerMethodReturnValueHandler handlerMethodReturnValueHandler, BasicResponseProperties basicResponseConfig) {
        this.handlerMethodReturnValueHandler = handlerMethodReturnValueHandler;
        this.basicResponseConfig = basicResponseConfig;
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return handlerMethodReturnValueHandler.supportsReturnType(returnType);
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (Objects.requireNonNull(request).getAttribute("noWrap") != null && (boolean) request.getAttribute("noWrap")) {
            // 如果标记为不封装，直接返回原始数据
            handlerMethodReturnValueHandler.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
            return;
        }
        // 检查方法上是否有 @NoWrap 注解
        Method method = returnType.getMethod();
        if (Objects.nonNull(method) && method.isAnnotationPresent(NoWrap.class)) {
            // 如果方法上有 @NoWrap 注解，则不进行封装
            handlerMethodReturnValueHandler.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
            return;
        }

        if (returnValue instanceof ApiResult) {
            ApiResult apiResult = (ApiResult) returnValue;
            if (basicResponseConfig.getEncryptEnable() && Objects.nonNull(apiResult.getData())) {
                // 对 ApiResult 的 content 加密
                apiResult.setSecret("rand-secret");
            }
            handlerMethodReturnValueHandler.handleReturnValue(apiResult, returnType, mavContainer, webRequest);
            return;
        }
        // 如果返回值不是 ApiResult 类型，先加密，然后封装成 ApiResult
        if (returnValue != null) {
            // 对返回值加密
            Object encryptedContent = encryptContent(returnValue);
            ApiResult apiResult = ApiResult.success(encryptedContent);
            apiResult.setSecret("rand-secret");
            handlerMethodReturnValueHandler.handleReturnValue(apiResult, returnType, mavContainer, webRequest);
        } else {
            // 如果返回值为空，直接封装成 ApiResult
            handlerMethodReturnValueHandler.handleReturnValue(ApiResult.success(null), returnType, mavContainer, webRequest);
        }
    }

    private Object encryptContent(Object content) {
        return content;
    }
}
