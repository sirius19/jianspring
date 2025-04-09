package com.jianspring.starter.restclient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * RestClient 配置属性
 */
@ConfigurationProperties(prefix = "jianspring.rest-client")
public class RestClientProperties {

    /**
     * 是否启用 RestClient
     */
    private boolean enabled = true;

    /**
     * 连接超时时间
     */
    private Duration connectTimeout = Duration.ofSeconds(5);

    /**
     * 响应超时时间
     */
    private Duration responseTimeout = Duration.ofSeconds(5);

    /**
     * 是否自动解封 ApiResult
     */
    private boolean unwrapApiResult = true;

    /**
     * 是否传递 traceId
     */
    private boolean traceIdEnabled = true;

    /**
     * 是否启用请求日志
     */
    private boolean logEnabled = true;

    /**
     * Resilience4j 配置
     */
    private Resilience4j resilience4j = new Resilience4j();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getResponseTimeout() {
        return responseTimeout;
    }

    public void setResponseTimeout(Duration responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

    public boolean isUnwrapApiResult() {
        return unwrapApiResult;
    }

    public void setUnwrapApiResult(boolean unwrapApiResult) {
        this.unwrapApiResult = unwrapApiResult;
    }

    public boolean isTraceIdEnabled() {
        return traceIdEnabled;
    }

    public void setTraceIdEnabled(boolean traceIdEnabled) {
        this.traceIdEnabled = traceIdEnabled;
    }

    public boolean isLogEnabled() {
        return logEnabled;
    }

    public void setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
    }

    public Resilience4j getResilience4j() {
        return resilience4j;
    }

    public void setResilience4j(Resilience4j resilience4j) {
        this.resilience4j = resilience4j;
    }

    /**
     * Resilience4j 配置
     */
    public static class Resilience4j {
        /**
         * 是否启用 Resilience4j
         */
        private boolean enabled = true;

        /**
         * 重试配置
         */
        private RetryConfig retryConfig = new RetryConfig();

        /**
         * 限流配置
         */
        private RateLimiterConfig rateLimiterConfig = new RateLimiterConfig();

        /**
         * 断路器配置
         */
        private CircuitBreakerConfig circuitBreakerConfig = new CircuitBreakerConfig();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public RetryConfig getRetryConfig() {
            return retryConfig;
        }

        public void setRetryConfig(RetryConfig retryConfig) {
            this.retryConfig = retryConfig;
        }

        public RateLimiterConfig getRateLimiterConfig() {
            return rateLimiterConfig;
        }

        public void setRateLimiterConfig(RateLimiterConfig rateLimiterConfig) {
            this.rateLimiterConfig = rateLimiterConfig;
        }

        public CircuitBreakerConfig getCircuitBreakerConfig() {
            return circuitBreakerConfig;
        }

        public void setCircuitBreakerConfig(CircuitBreakerConfig circuitBreakerConfig) {
            this.circuitBreakerConfig = circuitBreakerConfig;
        }

        /**
         * 重试配置
         */
        public static class RetryConfig {
            /**
             * 最大重试次数
             */
            private int maxAttempts = 3;

            /**
             * 重试等待时间
             */
            private Duration waitDuration = Duration.ofMillis(500);

            public int getMaxAttempts() {
                return maxAttempts;
            }

            public void setMaxAttempts(int maxAttempts) {
                this.maxAttempts = maxAttempts;
            }

            public Duration getWaitDuration() {
                return waitDuration;
            }

            public void setWaitDuration(Duration waitDuration) {
                this.waitDuration = waitDuration;
            }
        }

        /**
         * 限流配置
         */
        public static class RateLimiterConfig {
            /**
             * 限流刷新周期
             */
            private Duration limitRefreshPeriod = Duration.ofSeconds(1);

            /**
             * 周期内允许的请求数
             */
            private int limitForPeriod = 100;

            /**
             * 超时时间
             */
            private Duration timeoutDuration = Duration.ofSeconds(1);

            public Duration getLimitRefreshPeriod() {
                return limitRefreshPeriod;
            }

            public void setLimitRefreshPeriod(Duration limitRefreshPeriod) {
                this.limitRefreshPeriod = limitRefreshPeriod;
            }

            public int getLimitForPeriod() {
                return limitForPeriod;
            }

            public void setLimitForPeriod(int limitForPeriod) {
                this.limitForPeriod = limitForPeriod;
            }

            public Duration getTimeoutDuration() {
                return timeoutDuration;
            }

            public void setTimeoutDuration(Duration timeoutDuration) {
                this.timeoutDuration = timeoutDuration;
            }
        }

        /**
         * 断路器配置
         */
        public static class CircuitBreakerConfig {
            /**
             * 失败率阈值
             */
            private float failureRateThreshold = 50;

            /**
             * 开路状态等待时间
             */
            private Duration waitDurationInOpenState = Duration.ofSeconds(10);

            /**
             * 滑动窗口大小
             */
            private int slidingWindowSize = 100;

            public float getFailureRateThreshold() {
                return failureRateThreshold;
            }

            public void setFailureRateThreshold(float failureRateThreshold) {
                this.failureRateThreshold = failureRateThreshold;
            }

            public Duration getWaitDurationInOpenState() {
                return waitDurationInOpenState;
            }

            public void setWaitDurationInOpenState(Duration waitDurationInOpenState) {
                this.waitDurationInOpenState = waitDurationInOpenState;
            }

            public int getSlidingWindowSize() {
                return slidingWindowSize;
            }

            public void setSlidingWindowSize(int slidingWindowSize) {
                this.slidingWindowSize = slidingWindowSize;
            }
        }
    }
}