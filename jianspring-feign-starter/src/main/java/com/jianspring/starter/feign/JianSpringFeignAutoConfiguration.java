package com.jianspring.starter.feign;

import com.jianspring.starter.feign.decode.BasicFeignResponseDecode;
import com.jianspring.starter.feign.decode.BasicFeignResponseErrorDecode;
import com.jianspring.starter.feign.interceptor.BasicFeignInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JianSpringFeignAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public BasicFeignInterceptor basicFeignInterceptor() {
        return new BasicFeignInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public BasicFeignResponseDecode basicFeignResponseDecode() {
        return new BasicFeignResponseDecode();
    }

    @Bean
    @ConditionalOnMissingBean
    public BasicFeignResponseErrorDecode basicFeignResponseErrorDecode() {
        return new BasicFeignResponseErrorDecode();
    }
}
