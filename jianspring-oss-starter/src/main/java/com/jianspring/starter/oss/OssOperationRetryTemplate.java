package com.jianspring.starter.oss;

import com.aliyun.oss.OSSException;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

public class OssOperationRetryTemplate {
    private final RetryTemplate retryTemplate;

    public OssOperationRetryTemplate(int maxAttempts, long initialInterval, double multiplier, long maxInterval) {
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(initialInterval);
        backOffPolicy.setMultiplier(multiplier);
        backOffPolicy.setMaxInterval(maxInterval);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(maxAttempts);

        this.retryTemplate = RetryTemplate.builder()
                .customBackoff(backOffPolicy)
                .customPolicy(retryPolicy)
                .retryOn(OSSException.class)
                .build();
    }

    public <T> T execute(RetryCallback<T> retryCallback) {
        try {
            return retryTemplate.execute(context -> retryCallback.doWithRetry());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface RetryCallback<T> {
        T doWithRetry() throws Exception;
    }
}
