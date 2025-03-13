package com.jianspring.starter.cloud.advice;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;
import jakarta.annotation.Resource;

import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(name = "jianspring.response.wrapper.enable", havingValue = "true", matchIfMissing = true)
public class BasicResponseWrapperInitializing implements InitializingBean {

    @Resource
    RequestMappingHandlerAdapter requestMappingHandlerAdapter;
    @Resource
    BasicResponseProperties basicResponseConfig;

    public void afterPropertiesSet() {
        List<HandlerMethodReturnValueHandler> returnValueHandlers = requestMappingHandlerAdapter
                .getReturnValueHandlers();
        assert returnValueHandlers != null;
        List<HandlerMethodReturnValueHandler> handlers = new ArrayList<>(returnValueHandlers);
        for (HandlerMethodReturnValueHandler handler : handlers) {
            if (handler instanceof RequestResponseBodyMethodProcessor) {
                BasicResponseWrapper wrapHandler = new BasicResponseWrapper(handler, basicResponseConfig);
                handlers.set(handlers.indexOf(handler), wrapHandler);
                break;
            }
        }
        requestMappingHandlerAdapter.setReturnValueHandlers(handlers);
    }

}