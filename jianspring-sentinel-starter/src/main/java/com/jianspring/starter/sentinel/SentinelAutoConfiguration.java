package com.jianspring.starter.sentinel;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.SentinelWebInterceptor;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.RequestOriginParser;
import com.jianspring.starter.commons.enums.HeaderEnums;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration(
        proxyBeanMethods = false
)
@ConditionalOnWebApplication(
        type = ConditionalOnWebApplication.Type.SERVLET
)
@ConditionalOnProperty(
        name = {"spring.cloud.sentinel.enabled"},
        matchIfMissing = true
)
@ConditionalOnClass({SentinelWebInterceptor.class})
public class SentinelAutoConfiguration {

    @Bean
    @ConditionalOnProperty(
            name = {"spring.cloud.sentinel.filter.enabled"},
            matchIfMissing = true
    )
    public SentinelBlockExceptionHandler etrBlockExceptionHandler() {
        return new SentinelBlockExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public RequestOriginParser requestOriginParser() {
        return request -> Optional.ofNullable(request.getHeader(HeaderEnums.SENTINEL_ORIGN.getKey())).orElse("default");
    }
}
