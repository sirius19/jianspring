package com.jianspring.starter.feign.balancer;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ConditionalOnBlockingDiscoveryEnabled;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.client.ConditionalOnReactiveDiscoveryEnabled;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

/**
 * {@link ServiceInstanceListSupplier} don't use cache.<br>
 * <br>
 * 1. LoadBalancerCache causes information such as the weight of the service instance to
 * be changed without immediate effect.<br>
 * 2. Nacos itself supports caching.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnDiscoveryEnabled
public class NacosLoadBalancerClientConfiguration {
    private static final Log LOG = LogFactory.getLog(NacosLoadBalancerClientConfiguration.class);
    private static final int REACTIVE_SERVICE_INSTANCE_SUPPLIER_ORDER = 183827465;

    @Bean
    @ConditionalOnMissingBean
    public ReactorLoadBalancer<ServiceInstance> nacosLoadBalancer(Environment environment,
                                                                  LoadBalancerClientFactory loadBalancerClientFactory,
                                                                  NacosDiscoveryProperties nacosDiscoveryProperties) {
        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        return new NacosLoadBalancer(
                loadBalancerClientFactory.getLazyProvider(name,
                        ServiceInstanceListSupplier.class),
                name, nacosDiscoveryProperties);
    }


    @Configuration(proxyBeanMethods = false)
    @ConditionalOnReactiveDiscoveryEnabled
    @Order(REACTIVE_SERVICE_INSTANCE_SUPPLIER_ORDER)
    public static class ReactiveSupportConfiguration {

        @Bean
        @ConditionalOnBean(ReactiveDiscoveryClient.class)
        @ConditionalOnMissingBean
        @ConditionalOnProperty(value = "spring.cloud.loadbalancer.configurations", havingValue = "default", matchIfMissing = true)
        public ServiceInstanceListSupplier discoveryClientServiceInstanceListSupplier(
                ConfigurableApplicationContext context) {
            return ServiceInstanceListSupplier.builder().withDiscoveryClient()
                    .with((localContext, delegate) -> {
                        LoadBalancerClientFactory factory = localContext.getBean(LoadBalancerClientFactory.class);
                        return new JianSpringHintBasedServiceInstanceListSupplier(delegate, factory);
                    })
                    .build(context);
        }

        @Bean
        @ConditionalOnBean(ReactiveDiscoveryClient.class)
        @ConditionalOnMissingBean
        @ConditionalOnProperty(value = "spring.cloud.loadbalancer.configurations", havingValue = "zone-preference")
        public ServiceInstanceListSupplier zonePreferenceDiscoveryClientServiceInstanceListSupplier(
                ConfigurableApplicationContext context) {
            return ServiceInstanceListSupplier.builder().withDiscoveryClient()
                    .withZonePreference().build(context);
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBlockingDiscoveryEnabled
    @Order(REACTIVE_SERVICE_INSTANCE_SUPPLIER_ORDER + 1)
    public static class BlockingSupportConfiguration {

        @Bean
        @ConditionalOnBean(DiscoveryClient.class)
        @ConditionalOnMissingBean
        @ConditionalOnProperty(value = "spring.cloud.loadbalancer.configurations", havingValue = "default", matchIfMissing = true)
        public ServiceInstanceListSupplier discoveryClientServiceInstanceListSupplier(
                ConfigurableApplicationContext context) {
            return ServiceInstanceListSupplier.builder().withBlockingDiscoveryClient()
                    .with((localContext, delegate) -> {
                        LoadBalancerClientFactory factory = localContext.getBean(LoadBalancerClientFactory.class);
                        return new JianSpringHintBasedServiceInstanceListSupplier(delegate, factory);
                    })
                    .build(context);
        }

        @Bean
        @ConditionalOnBean(DiscoveryClient.class)
        @ConditionalOnMissingBean
        @ConditionalOnProperty(value = "spring.cloud.loadbalancer.configurations", havingValue = "zone-preference")
        public ServiceInstanceListSupplier zonePreferenceDiscoveryClientServiceInstanceListSupplier(
                ConfigurableApplicationContext context) {
            return ServiceInstanceListSupplier.builder().withBlockingDiscoveryClient()
                    .withZonePreference().build(context);
        }

    }

}
