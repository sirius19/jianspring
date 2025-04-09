package com.jianspring.starter.restclient.config;

import com.jianspring.starter.restclient.interceptor.LoggingExchangeFilterFunction;
import com.jianspring.starter.restclient.interceptor.TraceIdExchangeFilterFunction;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * WebClient 配置类
 */
@Configuration
@ConditionalOnProperty(prefix = "jianspring.rest-client", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WebClientConfig {

    /**
     * 创建 WebClient.Builder
     *
     * @return WebClient.Builder
     */
    @Bean
    @ConditionalOnMissingBean
    public WebClient.Builder webClientBuilder(RestClientProperties properties,
                                             TraceIdExchangeFilterFunction traceIdFilter,
                                             LoggingExchangeFilterFunction loggingFilter) {
        HttpClient httpClient = HttpClient.create();
        
        if (properties.getConnectTimeout() != null) {
            httpClient = httpClient.responseTimeout(properties.getConnectTimeout());
        }
        
        if (properties.getResponseTimeout() != null) {
            httpClient = httpClient.responseTimeout(properties.getResponseTimeout());
        }
        
        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
        
        WebClient.Builder builder = WebClient.builder()
                .clientConnector(connector);
        
        // 添加过滤器
        if (properties.isTraceIdEnabled()) {
            builder.filter(traceIdFilter);
        }
        
        if (properties.isLogEnabled()) {
            builder.filter(loggingFilter);
        }
        
        return builder;
    }
    
    /**
     * 创建 TraceId 过滤器
     *
     * @param properties 配置属性
     * @return TraceIdExchangeFilterFunction
     */
    @Bean
    @ConditionalOnMissingBean
    public TraceIdExchangeFilterFunction traceIdExchangeFilterFunction(RestClientProperties properties) {
        return new TraceIdExchangeFilterFunction(properties);
    }
    
    /**
     * 创建日志过滤器
     *
     * @param properties 配置属性
     * @return LoggingExchangeFilterFunction
     */
    @Bean
    @ConditionalOnMissingBean
    public LoggingExchangeFilterFunction loggingExchangeFilterFunction(RestClientProperties properties) {
        return new LoggingExchangeFilterFunction(properties);
    }
    
    /**
     * 创建 WebClient
     *
     * @param builder WebClient.Builder
     * @return WebClient
     */
    @Bean
    @ConditionalOnMissingBean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
}