package com.jianspring.starter.restclient.interceptor;

import com.jianspring.starter.restclient.config.RestClientProperties;
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

    private final RestClientProperties properties;

    public LoggingInterceptor(RestClientProperties properties) {
        this.properties = properties;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        if (properties.isLogEnabled()) {
            log.info("RestClient 请求: {} {}", request.getMethod(), request.getURI());
        }
        return execution.execute(request, body);
    }
}