package com.jianspring.starter.restclient.config;

import com.jianspring.starter.restclient.factory.JianClientProxyFactory;
import com.jianspring.starter.restclient.service.JianRestClient;
import com.jianspring.starter.restclient.service.JianWebClient;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Import({RestClientConfig.class, WebClientConfig.class, Resilience4jConfig.class})
public class RestClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JianRestClient jianRestClient(RestClient.Builder builder,
                                         RestClientProperties properties,
                                         Retry retry,
                                         RateLimiter rateLimiter,
                                         CircuitBreaker circuitBreaker,
                                         LoadBalancerClientFactory loadBalancerClientFactory) {
        RestClient restClient = builder.build();
        return new JianRestClient(restClient, properties, retry, rateLimiter, circuitBreaker, loadBalancerClientFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public JianWebClient jianWebClient(WebClient.Builder builder,
                                      RestClientProperties properties,
                                      Retry retry,
                                      RateLimiter rateLimiter,
                                      CircuitBreaker circuitBreaker,
                                      LoadBalancerClientFactory loadBalancerClientFactory) {
        WebClient webClient = builder.build();
        return new JianWebClient(webClient, properties, retry, rateLimiter, circuitBreaker, loadBalancerClientFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public JianClientProxyFactory jianClientProxyFactory(JianRestClient jianRestClient, JianWebClient jianWebClient) {
        return new JianClientProxyFactory(jianRestClient, jianWebClient);
    }
}