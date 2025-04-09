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
import org.springframework.web.client.RestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;


/**
 * Jian RestClient 服务类
 */

public class JianRestClient {

    private Logger log = LoggerFactory.getLogger(JianRestClient.class);

    private final RestClient restClient;
    private final RestClientProperties properties;
    private final Retry retry;
    private final RateLimiter rateLimiter;
    private final CircuitBreaker circuitBreaker;
    private final LoadBalancerClientFactory loadBalancerClientFactory;

    // 构造函数更新
    public JianRestClient(RestClient restClient,
                          RestClientProperties properties,
                          Retry retry,
                          RateLimiter rateLimiter,
                          CircuitBreaker circuitBreaker,
                          LoadBalancerClientFactory loadBalancerClientFactory) {
        this.restClient = restClient;
        this.properties = properties;
        this.retry = retry;
        this.rateLimiter = rateLimiter;
        this.circuitBreaker = circuitBreaker;
        this.loadBalancerClientFactory = loadBalancerClientFactory;
    }

    private String resolveUrl(String url) {
        if (!url.startsWith("http://") || url.startsWith("http://localhost")) {
            return url;
        }

        String serviceName = url.substring(7).split("/")[0];
        if (serviceName.contains(":")) {
            return url;
        }

        try {
            ReactiveLoadBalancer<ServiceInstance> loadBalancer = loadBalancerClientFactory
                    .getInstance(serviceName);
            if (loadBalancer == null) {
                log.warn("No load balancer found for service: {}", serviceName);
                return url;  // 返回原始URL，让后续重试机制处理
            }

            ServiceInstance instance = Mono.from(loadBalancer.choose())
                    .filter(response -> response != null)
                    .map(response -> response.getServer())
                    .filter(server -> server != null)
                    .blockOptional()
                    .orElse(null);  // 使用 orElse 替代 orElseThrow

            if (instance == null) {
                log.warn("No available instance for service: {}", serviceName);
                return url;  // 返回原始URL，让后续重试机制处理
            }

            return url.replace(
                    "http://" + serviceName,
                    "http://" + instance.getHost() + ":" + instance.getPort()
            );
        } catch (Exception e) {
            log.warn("Failed to resolve service: {}, falling back to original URL", serviceName, e);
            return url;  // 返回原始URL，让后续重试机制处理
        }
    }

    /**
     * 执行带弹性功能的操作
     *
     * @param supplier 操作
     * @param <T>      返回类型
     * @return 操作结果
     */
    private <T> T executeWithResilience(Supplier<T> supplier) {

        // 如果 Resilience4j 未启用，直接执行
        if (!properties.getResilience4j().isEnabled()) {
            return RestClientExceptionHandler.executeWithExceptionHandling(v -> supplier.get());
        }

        // Create final reference for the decorated supplier
        Supplier<T> decoratedSupplier = supplier;

        // 添加重试
        if (properties.getResilience4j().isEnabled()) {
            decoratedSupplier = Retry.decorateSupplier(retry, decoratedSupplier);
        }

        // 添加限流
        if (properties.getResilience4j().isEnabled()) {
            decoratedSupplier = RateLimiter.decorateSupplier(rateLimiter, decoratedSupplier);
        }

        // 添加断路器
        if (properties.getResilience4j().isEnabled()) {
            decoratedSupplier = CircuitBreaker.decorateSupplier(circuitBreaker, decoratedSupplier);
        }

        // Create final reference for the lambda
        final Supplier<T> finalSupplier = decoratedSupplier;

        // 添加异常处理
        return RestClientExceptionHandler.executeWithExceptionHandling(v -> finalSupplier.get());
    }

    /**
     * GET 请求
     *
     * @param url          请求 URL
     * @param responseType 响应类型
     * @param <T>          响应类型泛型
     * @return 响应结果
     */
    public <T> T get(String url, Class<T> responseType) {
        url = resolveUrl(url);
        return get(url, (Consumer<HttpHeaders>) null, responseType);
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
    public <T> T get(String url, Consumer<HttpHeaders> headers, Class<T> responseType) {
        return executeWithResilience(() -> {
            if (properties.isUnwrapApiResult()) {
                // 处理基本类型和String类型
                if (responseType.isPrimitive() || String.class.equals(responseType)) {
                    ApiResult<?> result = restClient.get()
                            .uri(url)
                            .headers(headers != null ? headers : h -> {
                                h.setContentType(MediaType.APPLICATION_JSON);
                                h.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                            })
                            .retrieve()
                            .body(new ParameterizedTypeReference<ApiResult<?>>() {
                            });
                    return responseType.cast(result != null ? result.getData() : null);
                }

                // 处理复杂对象类型
                ParameterizedTypeReference<ApiResult<T>> typeReference = new ParameterizedTypeReference<>() {
                };
                ApiResult<T> result = restClient.get()
                        .uri(url)
                        .headers(headers != null ? headers : h -> {
                            h.setContentType(MediaType.APPLICATION_JSON);
                            h.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                        })
                        .retrieve()
                        .body(typeReference);
                return result != null ? result.getData() : null;
            } else {
                return restClient.get()
                        .uri(url)
                        .headers(headers != null ? headers : h -> {
                            h.setContentType(MediaType.APPLICATION_JSON);
                            h.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                        })
                        .retrieve()
                        .body(responseType);
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
    public <T> T get(String url, Map<String, ?> uriVariables, Class<T> responseType) {
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
    public <T> T get(String url, Map<String, ?> uriVariables, Consumer<HttpHeaders> headers, Class<T> responseType) {
        return executeWithResilience(() -> {
            if (properties.isUnwrapApiResult()) {
                ParameterizedTypeReference<ApiResult<T>> typeReference = new ParameterizedTypeReference<>() {
                };
                ApiResult<T> result = restClient.get()
                        .uri(url, uriVariables)
                        .headers(headers != null ? headers : h -> {
                        })
                        .retrieve()
                        .body(typeReference);
                return result != null ? result.getData() : null;
            } else {
                return restClient.get()
                        .uri(url, uriVariables)
                        .headers(headers != null ? headers : h -> {
                        })
                        .retrieve()
                        .body(responseType);
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
    public <T> T post(String url, Object request, Class<T> responseType) {
        return post(url, request, null, responseType);
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
    public <T> T post(String url, Object request, Consumer<HttpHeaders> headers, Class<T> responseType) {
        return executeWithResilience(() -> {
            if (properties.isUnwrapApiResult()) {
                ParameterizedTypeReference<ApiResult<T>> typeReference = new ParameterizedTypeReference<>() {
                };
                ApiResult<T> result = restClient.post()
                        .uri(url)
                        .headers(headers != null ? headers : h -> h.setContentType(MediaType.APPLICATION_JSON))
                        .body(request)
                        .retrieve()
                        .body(typeReference);
                return result != null ? result.getData() : null;
            } else {
                return restClient.post()
                        .uri(url)
                        .headers(headers != null ? headers : h -> h.setContentType(MediaType.APPLICATION_JSON))
                        .body(request)
                        .retrieve()
                        .body(responseType);
            }
        });
    }

    /**
     * PUT 请求
     *
     * @param url     请求 URL
     * @param request 请求体
     */
    public void put(String url, Object request) {
        put(url, request, null, Void.class);
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
    public <T> T put(String url, Object request, Class<T> responseType) {
        return put(url, request, null, responseType);
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
    public <T> T put(String url, Object request, Consumer<HttpHeaders> headers, Class<T> responseType) {
        return executeWithResilience(() -> {
            if (properties.isUnwrapApiResult()) {
                ParameterizedTypeReference<ApiResult<T>> typeReference = new ParameterizedTypeReference<>() {
                };
                ApiResult<T> result = restClient.put()
                        .uri(url)
                        .headers(headers != null ? headers : h -> h.setContentType(MediaType.APPLICATION_JSON))
                        .body(request)
                        .retrieve()
                        .body(typeReference);
                return result != null ? result.getData() : null;
            } else {
                return restClient.put()
                        .uri(url)
                        .headers(headers != null ? headers : h -> h.setContentType(MediaType.APPLICATION_JSON))
                        .body(request)
                        .retrieve()
                        .body(responseType);
            }
        });
    }

    /**
     * DELETE 请求
     *
     * @param url 请求 URL
     */
    public void delete(String url) {
        // 明确指定使用 Consumer<HttpHeaders> 版本的方法
        delete(url, (Consumer<HttpHeaders>) null, Void.class);
    }

    /**
     * DELETE 请求（带响应类型）
     *
     * @param url          请求 URL
     * @param responseType 响应类型
     * @param <T>          响应类型泛型
     * @return 响应结果
     */
    public <T> T delete(String url, Class<T> responseType) {
        return delete(url, (Consumer<HttpHeaders>) null, responseType);
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
    public <T> T delete(String url, Map<String, ?> uriVariables, Class<T> responseType) {
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
    public <T> T delete(String url, Map<String, ?> uriVariables, Consumer<HttpHeaders> headers, Class<T> responseType) {
        return executeWithResilience(() -> {
            if (properties.isUnwrapApiResult()) {
                ParameterizedTypeReference<ApiResult<T>> typeReference = new ParameterizedTypeReference<>() {
                };
                ApiResult<T> result = restClient.delete()
                        .uri(url, uriVariables)
                        .headers(headers != null ? headers : h -> {
                            h.setContentType(MediaType.APPLICATION_JSON);
                            h.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                        })
                        .retrieve()
                        .body(typeReference);
                return result != null ? result.getData() : null;
            } else {
                return restClient.delete()
                        .uri(url, uriVariables)
                        .headers(headers != null ? headers : h -> {
                            h.setContentType(MediaType.APPLICATION_JSON);
                            h.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                        })
                        .retrieve()
                        .body(responseType);
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
    public <T> T delete(String url, Consumer<HttpHeaders> headers, Class<T> responseType) {
        return executeWithResilience(() -> {
            if (properties.isUnwrapApiResult()) {
                ParameterizedTypeReference<ApiResult<T>> typeReference = new ParameterizedTypeReference<>() {
                };
                ApiResult<T> result = restClient.delete()
                        .uri(url)
                        .headers(headers != null ? headers : h -> {
                        })
                        .retrieve()
                        .body(typeReference);
                return result != null ? result.getData() : null;
            } else {
                return restClient.delete()
                        .uri(url)
                        .headers(headers != null ? headers : h -> {
                        })
                        .retrieve()
                        .body(responseType);
            }
        });
    }

    /**
     * 获取原始 RestClient
     *
     * @return RestClient
     */
    public RestClient getRestClient() {
        return restClient;
    }

    /**
     * 获取重试实例
     *
     * @return Retry
     */
    public Retry getRetry() {
        return retry;
    }

    /**
     * 获取限流实例
     *
     * @return RateLimiter
     */
    public RateLimiter getRateLimiter() {
        return rateLimiter;
    }

    /**
     * 获取断路器实例
     *
     * @return CircuitBreaker
     */
    public CircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }
}