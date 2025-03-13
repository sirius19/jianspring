package com.jianspring.starter.commons.exception;

import com.jianspring.starter.commons.error.IErrorCode;

public class SentinelBlockException extends BizException {
    public SentinelBlockException(Integer code, String message) {
        super(code, message);
    }

    public SentinelBlockException(Integer code, String message, Object[] args) {
        super(code, message, args);
    }

    public SentinelBlockException(IErrorCode iErrorCode) {
        super(iErrorCode);
    }

    public SentinelBlockException(IErrorCode iErrorCode, Object[] args) {
        super(iErrorCode, args);
    }

    public static SentinelBlockException of(Integer code, String message) {
        return new SentinelBlockException(code, message);
    }

    public static SentinelBlockException of(Integer code, String message, Object[] args) {
        return new SentinelBlockException(code, message, args);
    }

    public static SentinelBlockException of(IErrorCode iErrorCode) {
        return new SentinelBlockException(iErrorCode);
    }

    public static SentinelBlockException of(IErrorCode iErrorCode, Object[] args) {
        return new SentinelBlockException(iErrorCode, args);
    }
}
