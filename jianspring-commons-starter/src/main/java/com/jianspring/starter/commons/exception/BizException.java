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
    private final Object[] args;
    private final IErrorCode errorCode; // 保存原始的错误码对象
    private final String defaultMessage; // 保存默认消息

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
        this.defaultMessage = message;
        this.args = null;
        this.errorCode = null;
    }

    public BizException(Integer code, String message, Object[] args) {
        super(message); // 仅用于堆栈跟踪
        this.code = code;
        this.defaultMessage = message;
        this.args = args;
        this.errorCode = null;
    }

    public BizException(IErrorCode errorCode) {
        super(errorCode.getDefaultMessage()); // 使用默认消息用于堆栈跟踪
        this.code = errorCode.getCode();
        this.defaultMessage = errorCode.getDefaultMessage();
        this.args = null;
        this.errorCode = errorCode;
    }

    public BizException(IErrorCode errorCode, Object[] args) {
        super(errorCode.getDefaultMessage()); // 使用默认消息用于堆栈跟踪
        this.code = errorCode.getCode();
        this.defaultMessage = errorCode.getDefaultMessage();
        this.args = args;
        this.errorCode = errorCode;
    }

    // 重写getMessage方法，根据当前语言环境动态获取消息
    @Override
    public String getMessage() {
        if (errorCode != null) {
            return args != null ? errorCode.getLocalMessage(args) : errorCode.getLocalMessage();
        }

        if (args != null && defaultMessage != null) {
            try {
                return String.format(defaultMessage, args);
            } catch (Exception e) {
                return defaultMessage;
            }
        }

        return defaultMessage;
    }

    // 获取原始默认消息
    public String getDefaultMessage() {
        return defaultMessage;
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