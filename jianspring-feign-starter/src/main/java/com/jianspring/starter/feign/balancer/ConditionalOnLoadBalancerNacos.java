package com.jianspring.starter.feign.balancer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ConditionalOnProperty(value = "spring.cloud.loadbalancer.jianspring.nacos.enabled", havingValue = "true")
public @interface ConditionalOnLoadBalancerNacos {

}
