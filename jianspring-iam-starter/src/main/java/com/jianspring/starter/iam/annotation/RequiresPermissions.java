package com.jianspring.starter.iam.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermissions {
    /**
     * 权限标识
     */
    String[] value();

    /**
     * 逻辑类型：AND | OR
     */
    Logical logical() default Logical.AND;
}