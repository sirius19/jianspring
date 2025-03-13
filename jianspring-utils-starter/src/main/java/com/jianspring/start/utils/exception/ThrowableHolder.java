package com.jianspring.start.utils.exception;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
public class ThrowableHolder {

    public static <T> T getOrDefault(Supplier<T> supplier, T defaultValue) {
        try {
            return supplier.get();
        } catch (Exception exception) {
            log.warn("", exception);
            return defaultValue;
        }
    }

    public static <T> T getOrNull(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception exception) {
            log.warn("", exception);
            return null;
        }
    }

    public static <T> T getOrException(Supplier<T> supplier, RuntimeException runtimeException) {
        try {
            return supplier.get();
        } catch (Exception exception) {
            log.warn("", exception);
            throw runtimeException;
        }
    }

}