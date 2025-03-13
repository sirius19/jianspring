package com.jianspring.starter.iam.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
*
* @Author:  InfoInsights
* @Date: 2023/2/20 下午2:20
* @Version: 1.0.0
*/
@Documented
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreToken {
}