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
public class PermissionException extends BizException {

    public PermissionException(Integer code, String message) {
        super(code, message);
    }

    public PermissionException(Integer code, String message, Object[] args) {
        super(code, message, args);
    }

    public PermissionException(IErrorCode iErrorCode) {
        super(iErrorCode);
    }

    public PermissionException(IErrorCode iErrorCode, Object[] args) {
        super(iErrorCode, args);
    }

    public static PermissionException of(Integer code, String message) {
        return new PermissionException(code, message);
    }

    public static PermissionException of(Integer code, String message, Object[] args) {
        return new PermissionException(code, message, args);
    }

    public static PermissionException of(IErrorCode iErrorCode) {
        return new PermissionException(iErrorCode);
    }

    public static PermissionException of(IErrorCode iErrorCode, Object[] args) {
        return new PermissionException(iErrorCode, args);
    }

}