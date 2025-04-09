package com.jianspring.starter.restclient.interceptor;

import com.jianspring.starter.restclient.config.RestClientProperties;
import io.github.resilience4j.retry.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * 请求日志拦截器
 */
public class LoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);
    private static final ThreadLocal<Integer> currentAttempt = new ThreadLocal<>();

    private final RestClientProperties properties;
    private final Retry retry;

    public LoggingInterceptor(RestClientProperties properties, Retry retry) {
        this.properties = properties;
        this.retry = retry;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (!properties.isLogEnabled()) {
            return execution.execute(request, body);
        }

        // 初始化或增加重试次数
        Integer attempt = currentAttempt.get();
        if (attempt == null) {
            attempt = 1;
            currentAttempt.set(attempt);
        } else {
            attempt = attempt + 1;
            currentAttempt.set(attempt);
        }

        long startTime = System.currentTimeMillis();
        log.info("Executing request {} {} (attempt: {})", request.getMethod(), request.getURI(), attempt);

        try {
            ClientHttpResponse response = execution.execute(request, body);
            long duration = System.currentTimeMillis() - startTime;
            log.info("Response completed {} {} - Status: {} - Time: {}ms (attempt: {})", 
                    request.getMethod(), request.getURI(), response.getStatusCode(), duration, attempt);
            
            // 请求成功，清理 ThreadLocal
            if (attempt >= retry.getRetryConfig().getMaxAttempts()) {
                currentAttempt.remove();
            }
            return response;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Request failed {} {} - Error: {} - Time: {}ms (attempt: {})", 
                    request.getMethod(), request.getURI(), e.getMessage(), duration, attempt);
            
            // 最后一次重试失败，清理 ThreadLocal
            if (attempt >= retry.getRetryConfig().getMaxAttempts()) {
                currentAttempt.remove();
            }
            throw e;
        }
    }
}