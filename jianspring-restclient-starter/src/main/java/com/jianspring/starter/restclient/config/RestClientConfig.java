package com.jianspring.starter.restclient.config;

import com.jianspring.starter.restclient.interceptor.LoggingInterceptor;
import com.jianspring.starter.restclient.interceptor.TraceIdInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * RestClient 配置类
 */
@Configuration
@EnableConfigurationProperties(RestClientProperties.class)
@ConditionalOnProperty(prefix = "jianspring.rest-client", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RestClientConfig {

    /**
     * 创建 RestClient.Builder
     *
     * @return RestClient.Builder
     */
    @Bean
    @ConditionalOnMissingBean
    @LoadBalanced
    public RestClient.Builder restClientBuilder(RestClientProperties properties,
                                                TraceIdInterceptor traceIdInterceptor,
                                                LoggingInterceptor loggingInterceptor) {
        RestClient.Builder builder = RestClient.builder();

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        if (properties.getConnectTimeout() != null) {
            requestFactory.setConnectTimeout((int) properties.getConnectTimeout().toMillis());
        }
        if (properties.getResponseTimeout() != null) {
            requestFactory.setReadTimeout((int) properties.getResponseTimeout().toMillis());
        }

        builder.requestFactory(requestFactory)
                .requestInterceptors(interceptors -> {
                    interceptors.add(traceIdInterceptor);
                    interceptors.add(loggingInterceptor);
                });

        return builder;
    }

    /**
     * 创建 TraceId 拦截器
     *
     * @param properties 配置属性
     * @return TraceIdInterceptor
     */
    @Bean
    @ConditionalOnMissingBean
    public TraceIdInterceptor traceIdInterceptor(RestClientProperties properties) {
        return new TraceIdInterceptor(properties);
    }

    /**
     * 创建日志拦截器
     *
     * @param properties 配置属性
     * @return LoggingInterceptor
     */
    @Bean
    @ConditionalOnMissingBean
    public LoggingInterceptor loggingInterceptor(RestClientProperties properties) {
        return new LoggingInterceptor(properties);
    }

    /**
     * 创建 RestClient
     *
     * @param builder RestClient.Builder
     * @return RestClient
     */
    @Bean
    @ConditionalOnMissingBean
    public RestClient restClient(RestClient.Builder builder) {
        return builder.build();
    }
}