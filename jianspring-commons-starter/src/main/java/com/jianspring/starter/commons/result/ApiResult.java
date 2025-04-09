package com.jianspring.starter.commons.result;

import com.jianspring.starter.commons.UserContextUtils;
import com.jianspring.starter.commons.error.CommonErrorCode;
import com.jianspring.starter.commons.error.IErrorCode;
import lombok.Data;
import lombok.ToString;


@Data
@ToString
public class ApiResult<T> {

    private String traceId;

    private String secret;

    private int code;

    private String msg;

    private T data;

    public ApiResult() {
        // 无参构造函数
    }

    private ApiResult(String traceId, int code, String msg, T data) {
        this.traceId = traceId;
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> ApiResult<T> success() {
        UserContextUtils.UserContext userContext = UserContextUtils.get();
        return new ApiResult<>(userContext.getTraceId(), CommonErrorCode.SUCCESS.getCode(), CommonErrorCode.SUCCESS.getLocalMessage((Object) null), null);
    }

    public static <T> ApiResult<T> success(T data) {
        UserContextUtils.UserContext userContext = UserContextUtils.get();
        return new ApiResult<>(userContext.getTraceId(), CommonErrorCode.SUCCESS.getCode(), CommonErrorCode.SUCCESS.getLocalMessage((Object) null), data);
    }

    public static <T> ApiResult<T> fail() {
        UserContextUtils.UserContext userContext = UserContextUtils.get();
        return new ApiResult<>(userContext.getTraceId(), CommonErrorCode.ERROR.getCode(), CommonErrorCode.ERROR.getLocalMessage((Object) null), null);
    }

    public static <T> ApiResult<T> fail(T data) {
        UserContextUtils.UserContext userContext = UserContextUtils.get();
        return new ApiResult<>(userContext.getTraceId(), CommonErrorCode.ERROR.getCode(), CommonErrorCode.ERROR.getLocalMessage((Object) null), data);
    }

    public static <T> ApiResult<T> fail(IErrorCode iErrorCode) {
        UserContextUtils.UserContext userContext = UserContextUtils.get();
        return new ApiResult<>(userContext.getTraceId(), iErrorCode.getCode(), iErrorCode.getLocalMessage((Object) null), null);
    }

    public static <T> ApiResult<T> fail(IErrorCode iErrorCode, Object... args) {
        UserContextUtils.UserContext userContext = UserContextUtils.get();
        return new ApiResult<>(userContext.getTraceId(), iErrorCode.getCode(), iErrorCode.getLocalMessage(args), null);
    }

    public static <T> ApiResult<T> fail(int code, String msg) {
        UserContextUtils.UserContext userContext = UserContextUtils.get();
        return new ApiResult<>(userContext.getTraceId(), code, msg, null);
    }

    public static <T> ApiResult<T> fail(int code, String msg, T data) {
        UserContextUtils.UserContext userContext = UserContextUtils.get();
        return new ApiResult<>(userContext.getTraceId(), code, msg, data);
    }

}