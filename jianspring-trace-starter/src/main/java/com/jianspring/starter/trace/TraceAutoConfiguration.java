package com.jianspring.starter.trace;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TracingLogProperties.class)
public class TraceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TracingFilter tracingFilter() {
        return new TracingFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    public TracingLogFilter tracingLogFilter(TracingLogProperties tracingLogProperties) {
        return new TracingLogFilter(tracingLogProperties);
    }

}
