package com.jianspring.starter.commons.exception;

import com.jianspring.starter.commons.error.IErrorCode;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @Author:  InfoInsights
 * @Date: 2022/12/28 下午7:44
 * @Version: 1.0.0
 */
@ToString
@EqualsAndHashCode(callSuper = true)
public class LoginException extends BizException {

    public LoginException(Integer code, String message) {
        super(code, message);
    }

    public LoginException(Integer code, String message, Object[] args) {
        super(code, message, args);
    }

    public LoginException(IErrorCode iErrorCode) {
        super(iErrorCode);
    }

    public LoginException(IErrorCode iErrorCode, Object[] args) {
        super(iErrorCode, args);
    }

    public static LoginException of(Integer code, String message) {
        return new LoginException(code, message);
    }

    public static LoginException of(Integer code, String message, Object[] args) {
        return new LoginException(code, message, args);
    }

    public static LoginException of(IErrorCode iErrorCode) {
        return new LoginException(iErrorCode);
    }

    public static LoginException of(IErrorCode iErrorCode, Object[] args) {
        return new LoginException(iErrorCode, args);
    }

}