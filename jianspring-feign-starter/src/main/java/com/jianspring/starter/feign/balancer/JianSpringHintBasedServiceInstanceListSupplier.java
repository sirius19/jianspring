package com.jianspring.starter.feign.balancer;

import com.jianspring.starter.commons.UserContextUtils;
import com.jianspring.starter.commons.enums.HeaderEnums;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.HintRequestContext;
import org.springframework.cloud.client.loadbalancer.LoadBalancerProperties;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.RequestDataContext;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.loadbalancer.core.DelegatingServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.HintBasedServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JianSpringHintBasedServiceInstanceListSupplier extends DelegatingServiceInstanceListSupplier {

    private final LoadBalancerProperties properties;

    /**
     * @deprecated in favour of
     * {@link HintBasedServiceInstanceListSupplier#HintBasedServiceInstanceListSupplier(ServiceInstanceListSupplier, ReactiveLoadBalancer.Factory)}
     */
    @Deprecated
    public JianSpringHintBasedServiceInstanceListSupplier(ServiceInstanceListSupplier delegate,
                                                          LoadBalancerProperties properties) {
        super(delegate);
        this.properties = properties;
    }

    public JianSpringHintBasedServiceInstanceListSupplier(ServiceInstanceListSupplier delegate,
                                                          ReactiveLoadBalancer.Factory<ServiceInstance> factory) {
        super(delegate);
        this.properties = factory.getProperties(getServiceId());
    }

    @Override
    public Flux<List<ServiceInstance>> get() {
        return delegate.get();
    }

    @Override
    public Flux<List<ServiceInstance>> get(Request request) {
        return delegate.get(request).map(instances -> filteredByHint(instances, getHint(request.getContext())));
    }

    private String getHint(Object requestContext) {
        if (requestContext == null) {
            return null;
        }
        String hint = null;
        if (requestContext instanceof RequestDataContext) {
            hint = getHintFromHeader((RequestDataContext) requestContext);
        }
        if (!StringUtils.hasText(hint) && requestContext instanceof HintRequestContext) {
            hint = ((HintRequestContext) requestContext).getHint();
        }
        if (!StringUtils.hasText(hint)) {
            hint = UserContextUtils.get().getHint();
        }
        return hint;
    }

    private String getHintFromHeader(RequestDataContext context) {
        if (context.getClientRequest() != null) {
            HttpHeaders headers = context.getClientRequest().getHeaders();
            if (headers != null) {
                return headers.getFirst(properties.getHintHeaderName());
            }
        }
        return null;
    }

    private List<ServiceInstance> filteredByHint(List<ServiceInstance> instances, String hint) {
        if (!StringUtils.hasText(hint)) {
            return instances;
        }

        List<ServiceInstance> filteredInstances = instances.stream()
                .filter(serviceInstance -> {
                    String hintMetaData = serviceInstance.getMetadata().getOrDefault(HeaderEnums.HINT_KEY.getKey(), "");
                    return Arrays.asList(hintMetaData.split(",")).contains(hint);
                })
                .collect(Collectors.toList());

        return filteredInstances.isEmpty() ? instances : filteredInstances;
    }

}
