package com.jianspring.start.utils.lambda;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class LambdaUtils implements Serializable {

    private static final Map<Class<?>, WeakReference<SerializedLambda>> LAMBDA_CACHE     = new ConcurrentHashMap<>();
    private static final long                                           serialVersionUID = -3131253000000242702L;

    public static <T> String getFieldName(Function<T, ?> function) {
        String implMethodName = getImplMethodName(function);
        String fieldName;

        if (implMethodName.startsWith("is")) {
            fieldName = implMethodName.substring(2);
        } else if (implMethodName.startsWith("get") || implMethodName.startsWith("set")) {
            fieldName = implMethodName.substring(3);
        } else {
            throw new IllegalArgumentException("匿名方法必须是 is、get、set 开头 ：" + implMethodName);
        }

        if (fieldName.length() == 1 || (fieldName.length() > 1 && !Character.isUpperCase(implMethodName.charAt(1)))) {
            fieldName = fieldName.substring(0, 1).toLowerCase(Locale.ENGLISH) + fieldName.substring(1);
        }

        return fieldName;
    }

    /**
     * 获取 匿名实现的方法名
     *
     * @param function 匿名方法
     * @param <T>      泛型类
     * @return 方法名
     */
    public static <T> String getImplMethodName(Function<T, ?> function) {
        return getLambda(function).getImplMethodName();
    }

    /**
     * 获取 匿名实现的方法名的 lambda 对象
     *
     * @param function 匿名方法
     * @param <T>      泛型类
     * @return 方法名
     */
    public static <T> SerializedLambda getLambda(Function<T, ?> function) {
        return Optional.ofNullable(LAMBDA_CACHE.get(function.getClass())).map(WeakReference::get).orElseGet(() -> {
            SerializedLambda serializedLambda = SerializedLambda.resolve(function);
            LAMBDA_CACHE.put(function.getClass(), new WeakReference<>(serializedLambda));
            return serializedLambda;
        });
    }

}