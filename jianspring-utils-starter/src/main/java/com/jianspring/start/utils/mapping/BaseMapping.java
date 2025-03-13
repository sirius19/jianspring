package com.jianspring.start.utils.mapping;

import org.mapstruct.InheritConfiguration;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.MapperConfig;

import java.util.List;
import java.util.stream.Stream;

/**
 *  @Author: InfoInsights
 *  @Date 2023/3/7
 *  @Description 不同对象如果只是简单转换可以直接继承该基类，而无需覆写基类任何方法，即只需要一个空类即可。
 *              如果子类覆写了基类的方法，则基类上的 @Mapping 会失效
 */
@MapperConfig
public interface BaseMapping<S, T> {
    /**
     * 映射同名属性
     */
    T sourceToTarget(S var1);

    /**
     * 反向，映射同名属性
     */
    @InheritInverseConfiguration(name = "sourceToTarget")
    S targetToSource(T var1);

    /**
     * 映射同名属性，集合形式
     */
    @InheritConfiguration(name = "sourceToTarget")
    List<T> sourceToTargetBatch(List<S> var1);

    /**
     * 反向，映射同名属性，集合形式
     */
    @InheritConfiguration(name = "targetToSource")
    List<S> targetToSourceBatch(List<T> var1);

    /**
     * 映射同名属性，集合流形式
     */
    List<T> sourceToTargetBatch(Stream<S> stream);

    /**
     * 反向，映射同名属性，集合流形式
     */
    List<S> targetToSourceBatch(Stream<T> stream);
}
