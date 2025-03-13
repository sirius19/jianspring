package com.jianspring.starter.oss;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AliOssProperties.class)
@ConditionalOnProperty(prefix = "aliyun.oss", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OssAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OssClientUtil ossClientUtil(AliOssProperties aliOssProperties, ApplicationContext applicationContext) {
        return new OssClientUtil(aliOssProperties, applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public OssOperationRetryTemplate ossOperationRetryTemplate(AliOssProperties aliOssProperties) {
        return new OssOperationRetryTemplate(
                aliOssProperties.getMaxAttempts(),
                aliOssProperties.getInitialInterval(),
                aliOssProperties.getMultiplier(),
                aliOssProperties.getMaxInterval()
        );
    }
}
