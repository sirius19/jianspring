package com.jianspring.starter.restclient.interceptor;

import com.jianspring.starter.restclient.config.RestClientProperties;
import org.slf4j.MDC;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

/**
 * TraceId 过滤器
 */
public class TraceIdExchangeFilterFunction implements ExchangeFilterFunction {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_KEY = "traceId";

    private final RestClientProperties properties;

    public TraceIdExchangeFilterFunction(RestClientProperties properties) {
        this.properties = properties;
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        if (!properties.isTraceIdEnabled()) {
            return next.exchange(request);
        }

        String traceId = MDC.get(TRACE_ID_KEY);
        if (traceId == null || traceId.isEmpty()) {
            return next.exchange(request);
        }

        ClientRequest newRequest = ClientRequest.from(request)
                .header(TRACE_ID_HEADER, traceId)
                .build();

        return next.exchange(newRequest);
    }
}