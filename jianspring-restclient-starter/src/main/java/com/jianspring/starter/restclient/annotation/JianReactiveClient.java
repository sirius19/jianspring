package com.jianspring.starter.restclient.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JianReactiveClient {
    String value() default "";
    
    String url() default "";
    
    String name() default "";
}