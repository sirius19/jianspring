package com.jianspring.starter.restclient.service;

import com.jianspring.starter.commons.result.ApiResult;
import com.jianspring.starter.restclient.config.RestClientProperties;
import com.jianspring.starter.restclient.exception.RestClientExceptionHandler;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Jian WebClient 服务类 - 提供异步 HTTP 请求支持
 */
public class JianWebClient {

    private final Logger log = LoggerFactory.getLogger(JianWebClient.class);

    private final WebClient webClient;
    private final RestClientProperties properties;
    private final Retry retry;
    private final RateLimiter rateLimiter;
    private final CircuitBreaker circuitBreaker;
    private final LoadBalancerClientFactory loadBalancerClientFactory;

    public JianWebClient(WebClient webClient,
                         RestClientProperties properties,
                         Retry retry,
                         RateLimiter rateLimiter,
                         CircuitBreaker circuitBreaker,
                         LoadBalancerClientFactory loadBalancerClientFactory) {
        this.webClient = webClient;
        this.properties = properties;
        this.retry = retry;
        this.rateLimiter = rateLimiter;
        this.circuitBreaker = circuitBreaker;
        this.loadBalancerClientFactory = loadBalancerClientFactory;
    }

    /**
     * 解析服务名称为实际 URL
     *
     * @param url 原始 URL
     * @return 解析后的 URL
     */
    private Mono<String> resolveUrl(String url) {
        if (url.startsWith("http://") && !url.startsWith("http://localhost")) {
            String serviceName = url.substring(7).split("/")[0];
            if (!serviceName.contains(":")) {
                try {
                    ReactiveLoadBalancer<ServiceInstance> loadBalancer = loadBalancerClientFactory
                            .getInstance(serviceName);
                    if (loadBalancer == null) {
                        return Mono.just(url);
                    }

                    return Mono.from(loadBalancer.choose())
                            .map(response -> {
                                if (response != null && response.getServer() != null) {
                                    ServiceInstance instance = response.getServer();
                                    return url.replace(
                                            "http://" + serviceName,
                                            "http://" + instance.getHost() + ":" + instance.getPort()
                                    );
                                }
                                return url;
                            })
                            .onErrorResume(e -> {
                                log.warn("Failed to resolve service: " + serviceName, e);
                                return Mono.just(url);
                            });
                } catch (Exception e) {
                    log.warn("Failed to resolve service: " + serviceName, e);
                    return Mono.just(url);
                }
            }
        }
        return Mono.just(url);
    }

    /**
     * 执行带弹性功能的异步操作
     *
     * @param monoSupplier 异步操作
     * @param <T>          返回类型
     * @return 操作结果
     */
    private <T> Mono<T> executeWithResilience(Function<Void, Mono<T>> monoSupplier) {
        // 如果 Resilience4j 未启用，直接执行
        if (!properties.getResilience4j().isEnabled()) {
            return monoSupplier.apply(null);
        }

        // 添加弹性功能
        Function<Void, Mono<T>> decoratedSupplier = monoSupplier;

        // 添加重试
        decoratedSupplier = Retry.decorateFunction(retry, decoratedSupplier);

        // 添加限流
        decoratedSupplier = RateLimiter.decorateFunction(rateLimiter, decoratedSupplier);

        // 添加断路器
        decoratedSupplier = CircuitBreaker.decorateFunction(circuitBreaker, decoratedSupplier);

        // 执行
        return decoratedSupplier.apply(null);
    }

    /**
     * GET 请求
     *
     * @param url          请求 URL
     * @param responseType 响应类型
     * @param <T>          响应类型泛型
     * @return 响应结果
     */
    public <T> Mono<T> get(String url, Class<T> responseType) {
        return resolveUrl(url)
                .flatMap(resolvedUrl -> get(resolvedUrl, (Consumer<HttpHeaders>) null, responseType));
    }

    /**
     * GET 请求（带请求头）
     *
     * @param url          请求 URL
     * @param headers      请求头
     * @param responseType 响应类型
     * @param <T>          响应类型泛型
     * @return 响应结果
     */
    public <T> Mono<T> get(String url, Consumer<HttpHeaders> headers, Class<T> responseType) {
        return executeWithResilience(v -> {
            WebClient.RequestHeadersSpec<?> requestSpec = webClient.get()
                    .uri(url)
                    .headers(headers != null ? headers : h -> {
                        h.setContentType(MediaType.APPLICATION_JSON);
                        h.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                    });

            if (properties.isUnwrapApiResult()) {
                // 处理基本类型和String类型
                if (responseType.isPrimitive() || String.class.equals(responseType)) {
                    return requestSpec.retrieve()
                            .bodyToMono(new ParameterizedTypeReference<ApiResult<?>>() {})
                            .map(result -> responseType.cast(result != null ? result.getData() : null));
                }

                // 处理复杂对象类型
                return requestSpec.retrieve()
                        .bodyToMono(new ParameterizedTypeReference<ApiResult<T>>() {})
                        .map(result -> result != null ? result.getData() : null);
            } else {
                return requestSpec.retrieve().bodyToMono(responseType);
            }
        });
    }

    /**
     * GET 请求（带路径变量）
     *
     * @param url          请求 URL
     * @param uriVariables 路径变量
     * @param responseType 响应类型
     * @param <T>          响应类型泛型
     * @return 响应结果
     */
    public <T> Mono<T> get(String url, Map<String, ?> uriVariables, Class<T> responseType) {
        return get(url, uriVariables, null, responseType);
    }

    /**
     * GET 请求（带路径变量和请求头）
     *
     * @param url          请求 URL
     * @param uriVariables 路径变量
     * @param headers      请求头
     * @param responseType 响应类型
     * @param <T>          响应类型泛型
     * @return 响应结果
     */
    public <T> Mono<T> get(String url, Map<String, ?> uriVariables, Consumer<HttpHeaders> headers, Class<T> responseType) {
        return executeWithResilience(v -> {
            WebClient.RequestHeadersSpec<?> requestSpec = webClient.get()
                    .uri(url, uriVariables)
                    .headers(headers != null ? headers : h -> {
                        h.setContentType(MediaType.APPLICATION_JSON);
                        h.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                    });

            if (properties.isUnwrapApiResult()) {
                return requestSpec.retrieve()
                        .bodyToMono(new ParameterizedTypeReference<ApiResult<T>>() {})
                        .map(result -> result != null ? result.getData() : null);
            } else {
                return requestSpec.retrieve().bodyToMono(responseType);
            }
        });
    }

    /**
     * POST 请求
     *
     * @param url          请求 URL
     * @param request      请求体
     * @param responseType 响应类型
     * @param <T>          响应类型泛型
     * @return 响应结果
     */
    public <T> Mono<T> post(String url, Object request, Class<T> responseType) {
        return resolveUrl(url)
                .flatMap(resolvedUrl -> post(resolvedUrl, request, null, responseType));
    }

    /**
     * POST 请求（带请求头）
     *
     * @param url          请求 URL
     * @param request      请求体
     * @param headers      请求头
     * @param responseType 响应类型
     * @param <T>          响应类型泛型
     * @return 响应结果
     */
    public <T> Mono<T> post(String url, Object request, Consumer<HttpHeaders> headers, Class<T> responseType) {
        return executeWithResilience(v -> {
            WebClient.RequestHeadersSpec<?> requestSpec = webClient.post()
                    .uri(url)
                    .headers(headers != null ? headers : h -> h.setContentType(MediaType.APPLICATION_JSON))
                    .bodyValue(request != null ? request : "");

            if (properties.isUnwrapApiResult()) {
                return requestSpec.retrieve()
                        .bodyToMono(new ParameterizedTypeReference<ApiResult<T>>() {})
                        .map(result -> result != null ? result.getData() : null);
            } else {
                return requestSpec.retrieve().bodyToMono(responseType);
            }
        });
    }

    /**
     * PUT 请求
     *
     * @param url     请求 URL
     * @param request 请求体
     * @return 操作结果
     */
    public Mono<Void> put(String url, Object request) {
        return resolveUrl(url)
                .flatMap(resolvedUrl -> put(resolvedUrl, request, null, Void.class));
    }

    /**
     * PUT 请求（带响应类型）
     *
     * @param url          请求 URL
     * @param request      请求体
     * @param responseType 响应类型
     * @param <T>          响应类型泛型
     * @return 响应结果
     */
    public <T> Mono<T> put(String url, Object request, Class<T> responseType) {
        return resolveUrl(url)
                .flatMap(resolvedUrl -> put(resolvedUrl, request, null, responseType));
    }

    /**
     * PUT 请求（带请求头）
     *
     * @param url          请求 URL
     * @param request      请求体
     * @param headers      请求头
     * @param responseType 响应类型
     * @param <T>          响应类型泛型
     * @return 响应结果
     */
    public <T> Mono<T> put(String url, Object request, Consumer<HttpHeaders> headers, Class<T> responseType) {
        return executeWithResilience(v -> {
            WebClient.RequestHeadersSpec<?> requestSpec = webClient.put()
                    .uri(url)
                    .headers(headers != null ? headers : h -> h.setContentType(MediaType.APPLICATION_JSON))
                    .bodyValue(request != null ? request : "");

            if (properties.isUnwrapApiResult()) {
                return requestSpec.retrieve()
                        .bodyToMono(new ParameterizedTypeReference<ApiResult<T>>() {})
                        .map(result -> result != null ? result.getData() : null);
            } else {
                return requestSpec.retrieve().bodyToMono(responseType);
            }
        });
    }

    /**
     * DELETE 请求
     *
     * @param url 请求 URL
     * @return 操作结果
     */
    public Mono<Void> delete(String url) {
        return resolveUrl(url)
                .flatMap(resolvedUrl -> delete(resolvedUrl, (Consumer<HttpHeaders>) null, Void.class));
    }

    /**
     * DELETE 请求（带响应类型）
     *
     * @param url          请求 URL
     * @param responseType 响应类型
     * @param <T>          响应类型泛型
     * @return 响应结果
     */
    public <T> Mono<T> delete(String url, Class<T> responseType) {
        return resolveUrl(url)
                .flatMap(resolvedUrl -> delete(resolvedUrl, (Consumer<HttpHeaders>) null, responseType));
    }

    /**
     * DELETE 请求（带路径变量）
     *
     * @param url          请求 URL
     * @param uriVariables 路径变量
     * @param responseType 响应类型
     * @param <T>          响应类型泛型
     * @return 响应结果
     */
    public <T> Mono<T> delete(String url, Map<String, ?> uriVariables, Class<T> responseType) {
        return delete(url, uriVariables, null, responseType);
    }

    /**
     * DELETE 请求（带路径变量和请求头）
     *
     * @param url          请求 URL
     * @param uriVariables 路径变量
     * @param headers      请求头
     * @param responseType 响应类型
     * @param <T>          响应类型泛型
     * @return 响应结果
     */
    public <T> Mono<T> delete(String url, Map<String, ?> uriVariables, Consumer<HttpHeaders> headers, Class<T> responseType) {
        return executeWithResilience(v -> {
            WebClient.RequestHeadersSpec<?> requestSpec = webClient.delete()
                    .uri(url, uriVariables)
                    .headers(headers != null ? headers : h -> {
                        h.setContentType(MediaType.APPLICATION_JSON);
                        h.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                    });

            if (properties.isUnwrapApiResult()) {
                return requestSpec.retrieve()
                        .bodyToMono(new ParameterizedTypeReference<ApiResult<T>>() {})
                        .map(result -> result != null ? result.getData() : null);
            } else {
                return requestSpec.retrieve().bodyToMono(responseType);
            }
        });
    }

    /**
     * DELETE 请求（带请求头）
     *
     * @param url          请求 URL
     * @param headers      请求头
     * @param responseType 响应类型
     * @param <T>          响应类型泛型
     * @return 响应结果
     */
    public <T> Mono<T> delete(String url, Consumer<HttpHeaders> headers, Class<T> responseType) {
        return executeWithResilience(v -> {
            WebClient.RequestHeadersSpec<?> requestSpec = webClient.delete()
                    .uri(url)
                    .headers(headers != null ? headers : h -> {
                        h.setContentType(MediaType.APPLICATION_JSON);
                        h.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                    });

            if (properties.isUnwrapApiResult()) {
                return requestSpec.retrieve()
                        .bodyToMono(new ParameterizedTypeReference<ApiResult<T>>() {})
                        .map(result -> result != null ? result.getData() : null);
            } else {
                return requestSpec.retrieve().bodyToMono(responseType);
            }
        });
    }

    /**
     * 获取原始 WebClient
     *
     * @return WebClient
     */
    public WebClient getWebClient() {
        return webClient;
    }
}