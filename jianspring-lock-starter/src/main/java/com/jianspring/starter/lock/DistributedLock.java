package com.jianspring.starter.lock;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface DistributedLock {
    // 锁的key 表达式，支持spel
    String key() default "";

    //锁的前缀
    String prefixKey() default "RedissonLock:";

    // 租约时长
    int leaseTime() default 10;

    //获取不到锁后是否等待
    boolean needWait() default true;

    boolean autoRelease() default true;

    //异常提示
    String errorDesc() default "系统繁忙，请稍后提交";

    //等待锁的最长时间
    int waitTime() default 3;

    //锁的前缀是否加上方法名
    boolean needMethodPrefix() default true;

}
