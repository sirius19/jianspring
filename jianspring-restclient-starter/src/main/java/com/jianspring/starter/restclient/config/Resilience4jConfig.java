package com.jianspring.starter.restclient.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClientException;

/**
 * Resilience4j 配置类
 */
@Configuration
@ConditionalOnProperty(prefix = "jianspring.rest-client.resilience4j", name = "enabled", havingValue = "true", matchIfMissing = true)
public class Resilience4jConfig {

    /**
     * 创建重试注册表
     *
     * @param properties 配置属性
     * @return RetryRegistry
     */
    @Bean
    @ConditionalOnMissingBean
    public RetryRegistry retryRegistry(RestClientProperties properties) {
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(properties.getResilience4j().getRetryConfig().getMaxAttempts())
                .waitDuration(properties.getResilience4j().getRetryConfig().getWaitDuration())
                .retryExceptions(RestClientException.class)
                .build();
        return RetryRegistry.of(retryConfig);
    }

    /**
     * 创建重试实例
     *
     * @param retryRegistry 重试注册表
     * @return Retry
     */
    @Bean
    @ConditionalOnMissingBean
    public Retry restClientRetry(RetryRegistry retryRegistry) {
        return retryRegistry.retry("restClientRetry");
    }

    /**
     * 创建限流注册表
     *
     * @param properties 配置属性
     * @return RateLimiterRegistry
     */
    @Bean
    @ConditionalOnMissingBean
    public RateLimiterRegistry rateLimiterRegistry(RestClientProperties properties) {
        RateLimiterConfig rateLimiterConfig = RateLimiterConfig.custom()
                .limitRefreshPeriod(properties.getResilience4j().getRateLimiterConfig().getLimitRefreshPeriod())
                .limitForPeriod(properties.getResilience4j().getRateLimiterConfig().getLimitForPeriod())
                .timeoutDuration(properties.getResilience4j().getRateLimiterConfig().getTimeoutDuration())
                .build();
        return RateLimiterRegistry.of(rateLimiterConfig);
    }

    /**
     * 创建限流实例
     *
     * @param rateLimiterRegistry 限流注册表
     * @return RateLimiter
     */
    @Bean
    @ConditionalOnMissingBean
    public RateLimiter restClientRateLimiter(RateLimiterRegistry rateLimiterRegistry) {
        return rateLimiterRegistry.rateLimiter("restClientRateLimiter");
    }

    /**
     * 创建断路器注册表
     *
     * @param properties 配置属性
     * @return CircuitBreakerRegistry
     */
    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerRegistry circuitBreakerRegistry(RestClientProperties properties) {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(properties.getResilience4j().getCircuitBreakerConfig().getFailureRateThreshold())
                .waitDurationInOpenState(properties.getResilience4j().getCircuitBreakerConfig().getWaitDurationInOpenState())
                .slidingWindowSize(properties.getResilience4j().getCircuitBreakerConfig().getSlidingWindowSize())
                .recordExceptions(RestClientException.class)
                .build();
        return CircuitBreakerRegistry.of(circuitBreakerConfig);
    }

    /**
     * 创建断路器实例
     *
     * @param circuitBreakerRegistry 断路器注册表
     * @return CircuitBreaker
     */
    @Bean
    @ConditionalOnMissingBean
    public CircuitBreaker restClientCircuitBreaker(CircuitBreakerRegistry circuitBreakerRegistry) {
        return circuitBreakerRegistry.circuitBreaker("restClientCircuitBreaker");
    }
}