package com.jianspring.starter.cloud;

import com.jianspring.start.utils.spring.SpringBeanContext;
import com.jianspring.starter.cloud.advice.BasicResponseProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(value = {BasicResponseProperties.class})
@ComponentScan("com.jianspring.starter.cloud")
public class CloudAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SpringBeanContext springBeanContext() {
        return new SpringBeanContext();
    }
}
