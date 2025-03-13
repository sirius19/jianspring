package com.jianspring.start.utils.spring;

import com.jianspring.start.utils.exception.ThrowableHolder;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SpringBeanContext implements ApplicationContextAware {

    protected static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        SpringBeanContext.applicationContext = applicationContext;
    }

    public static Object getBean(String beanName) {
        return ThrowableHolder.getOrNull(() -> applicationContext.getBean(beanName));
    }

    public static <T> T getBean(Class<T> tClass) {
        return ThrowableHolder.getOrNull(() -> applicationContext.getBean(tClass));
    }

    public static <T> T getBean(String beanName, Class<T> tClass) {
        return ThrowableHolder.getOrNull(() -> applicationContext.getBean(beanName, tClass));
    }

    public static <T> Map<String, T> getBeanMap(Class<T> tClass) {
        return ThrowableHolder.getOrDefault(() -> applicationContext.getBeansOfType(tClass), Collections.emptyMap());
    }

    public static <T> List<T> getBeanList(Class<T> tClass) {
        return new ArrayList<>(getBeanMap(tClass).values());
    }

}