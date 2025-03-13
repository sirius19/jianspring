package com.jianspring.starter.commons.exception;

import com.jianspring.starter.commons.error.CommonErrorCode;
import com.jianspring.starter.commons.error.IErrorCode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @Author: InfoInsights
 * @Date: 2022/12/28 下午7:44
 * @Version: 1.0.0
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class BizException extends RuntimeException {

    private final Integer code;

    private final String message;

    private final Object[] args;

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
        this.args = null;
    }

    public BizException(Integer code, String message, Object[] args) {
        super(String.format(message, args));
        this.code = code;
        this.message = super.getMessage();
        this.args = args;
    }

    public BizException(IErrorCode iErrorCode) {
        super(iErrorCode.getLocalMessage());
        this.code = iErrorCode.getCode();
        this.message = super.getMessage();
        this.args = null;
    }

    public BizException(IErrorCode iErrorCode, Object[] args) {
        super(String.format(iErrorCode.getLocalMessage(), args));
        this.code = iErrorCode.getCode();
        this.message = super.getMessage();
        this.args = args;
    }

    public static BizException of(String message) {
        return new BizException(CommonErrorCode.ERROR.getCode(), message);
    }

    public static BizException of(Integer code, String message) {
        return new BizException(code, message);
    }

    public static BizException of(Integer code, String message, Object[] args) {
        return new BizException(code, message, args);
    }

    public static BizException of(IErrorCode iErrorCode) {
        return new BizException(iErrorCode);
    }

    public static BizException of(IErrorCode iErrorCode, Object[] args) {
        return new BizException(iErrorCode, args);
    }

}