package com.jianspring.starter.restclient.interceptor;

import com.jianspring.starter.restclient.config.RestClientProperties;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.UUID;

/**
 * TraceId 拦截器
 */
public class TraceIdInterceptor implements ClientHttpRequestInterceptor {

    private static final String TRACE_ID = "traceId";

    private final RestClientProperties properties;

    public TraceIdInterceptor(RestClientProperties properties) {
        this.properties = properties;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        if (properties.isTraceIdEnabled()) {
            String traceId = MDC.get(TRACE_ID);
            if (traceId == null || traceId.isEmpty()) {
                traceId = UUID.randomUUID().toString().replace("-", "");
                MDC.put(TRACE_ID, traceId);
            }
            request.getHeaders().set(TRACE_ID, traceId);
        }
        return execution.execute(request, body);
    }
}