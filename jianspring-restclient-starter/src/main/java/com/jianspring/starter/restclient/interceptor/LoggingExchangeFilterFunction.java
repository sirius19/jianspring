package com.jianspring.starter.restclient.interceptor;

import com.jianspring.starter.restclient.config.RestClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

/**
 * 日志过滤器
 */
public class LoggingExchangeFilterFunction implements ExchangeFilterFunction {

    private static final Logger log = LoggerFactory.getLogger(LoggingExchangeFilterFunction.class);

    private final RestClientProperties properties;

    public LoggingExchangeFilterFunction(RestClientProperties properties) {
        this.properties = properties;
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        if (!properties.isLogEnabled()) {
            return next.exchange(request);
        }

        Instant start = Instant.now();
        log.info("WebClient Request: {} {}", request.method(), request.url());

        return next.exchange(request)
                .doOnSuccess(response -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.info("WebClient Response: {} {} - Status: {} - Time: {}ms",
                            request.method(), request.url(), response.statusCode(), duration.toMillis());
                })
                .doOnError(error -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.error("WebClient Error: {} {} - Error: {} - Time: {}ms",
                            request.method(), request.url(), error.getMessage(), duration.toMillis());
                });
    }
}