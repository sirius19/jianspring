package com.jianspring.start.utils.exception;

public class RetryExecutionException extends RuntimeException {
    public RetryExecutionException(String message) {
        super(message);
    }

    public RetryExecutionException(String message, Throwable throwable) {
        super(message, throwable);
    }
}