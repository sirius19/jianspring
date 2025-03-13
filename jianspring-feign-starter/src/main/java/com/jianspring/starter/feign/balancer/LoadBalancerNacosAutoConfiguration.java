package com.jianspring.starter.feign.balancer;

import com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.context.annotation.Configuration;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * Auto-configuration} that sets up LoadBalancer for Nacos.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
@ConditionalOnLoadBalancerNacos
@ConditionalOnNacosDiscoveryEnabled
@LoadBalancerClients(defaultConfiguration = NacosLoadBalancerClientConfiguration.class)
public class LoadBalancerNacosAutoConfiguration {

}
