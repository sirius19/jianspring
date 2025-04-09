package com.jianspring.starter.restclient.annotation;

import com.jianspring.starter.restclient.config.JianClientScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(JianClientScannerRegistrar.class)
public @interface EnableJianClients {

    /**
     * 扫描的包，可以是多个
     */
    String[] value() default {};

    /**
     * 扫描的包，可以是多个
     */
    String[] basePackages() default {};

    /**
     * 扫描的包所在的类，可以是多个
     */
    Class<?>[] basePackageClasses() default {};
}