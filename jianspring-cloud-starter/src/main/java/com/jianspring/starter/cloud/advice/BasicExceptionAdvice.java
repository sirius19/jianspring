package com.jianspring.starter.cloud.advice;

import com.jianspring.starter.commons.error.CommonErrorCode;
import com.jianspring.starter.commons.exception.BizException;
import com.jianspring.starter.commons.exception.LoginException;
import com.jianspring.starter.commons.exception.PermissionException;
import com.jianspring.starter.commons.exception.SentinelBlockException;
import com.jianspring.starter.commons.result.ApiResult;
import jakarta.websocket.DecodeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Optional;


/**
 * @Author: InfoInsights
 * @Date: 2023/2/21 上午10:14
 * @Version: 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class BasicExceptionAdvice {
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<Object> bindException(BindException bindException) {
        log.warn("Global Exception: {}", bindException.getClass().getSimpleName(), bindException);
        return Optional.ofNullable(bindException.getFieldError())
                .map(fieldError -> ApiResult.fail(CommonErrorCode.PARAM_ERROR.getCode(), fieldError.getDefaultMessage()))
                .orElse(ApiResult.fail(CommonErrorCode.PARAM_ERROR));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<Object> methodArgumentNotValidException(MethodArgumentNotValidException methodArgumentNotValidException) {
        log.warn("Global Exception methodArgumentNotValidException ", methodArgumentNotValidException);

        FieldError fieldError = methodArgumentNotValidException.getBindingResult().getFieldError();
        if (null == fieldError) {
            return ApiResult.fail(CommonErrorCode.PARAM_ERROR);
        }
        String defaultMessage = fieldError.getDefaultMessage();

        if (null == defaultMessage) {
            return ApiResult.fail(CommonErrorCode.PARAM_ERROR);
        }
        try {
            return ApiResult.fail((Integer.parseInt(defaultMessage)));
        } catch (NumberFormatException exception) {
            return ApiResult.fail(CommonErrorCode.PARAM_ERROR.getCode(), fieldError.getDefaultMessage());
        }
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<Object> argumentTypeMismatchException(MethodArgumentTypeMismatchException argumentTypeMismatchException) {
        log.warn("Global Exception argumentTypeMismatchException ", argumentTypeMismatchException);
        return ApiResult.fail(CommonErrorCode.PARAM_ERROR);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiResult<Object> methodNotSupportedException(HttpRequestMethodNotSupportedException methodNotSupportedException) {
        log.warn("Global Exception methodNotSupportedException ", methodNotSupportedException);
        return ApiResult.fail(CommonErrorCode.HTTP_METHOD_NOT_SUPPORT, methodNotSupportedException.getMethod());
    }

    //参数类型不匹配
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<Object> httpMessageNotReadableException(HttpMessageNotReadableException messageNotReadableException) {
        log.warn("Global Exception messageNotReadableException ", messageNotReadableException);
        return ApiResult.fail(CommonErrorCode.PARAM_ERROR);
    }

    //参数类型异常
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<Object> illegalArgumentException(IllegalArgumentException illegalArgumentException) {
        log.warn("Global Exception illegalArgumentException ", illegalArgumentException);
        return ApiResult.fail(CommonErrorCode.PARAM_ERROR);
    }

    @ExceptionHandler(ParseException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult<Object> parseException(ParseException parseException) {
        log.warn("Global Exception ParseException ", parseException);
        return ApiResult.fail(CommonErrorCode.DATA_PARSE_ERROR);
    }

    //登陆异常
    @ExceptionHandler(LoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResult<Object> loginException(LoginException loginException) {
        log.warn("Global Exception loginException ", loginException);
        return ApiResult.fail(loginException.getCode(), loginException.getMessage());
    }

    //权限异常
    @ExceptionHandler(PermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResult<Object> permissionException(PermissionException permissionException) {
        log.warn("Global Exception permissionException ", permissionException);
        return ApiResult.fail(permissionException.getCode(), permissionException.getMessage());
    }

    //业务异常
    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult<Object> bizException(BizException bizException) {
        log.warn("Global Exception BizException ", bizException);
        return ApiResult.fail(bizException.getCode(), bizException.getMessage());
    }

    //sentinel异常
    @ExceptionHandler(SentinelBlockException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult<Object> blockException(SentinelBlockException blockException) {
        log.warn("Block Exception", blockException);
        return ApiResult.fail(blockException.getCode(), blockException.getMessage());
    }

    @ExceptionHandler(DecodeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult<Object> decodeException(DecodeException decodeException) {
        log.warn("Global Exception decodeException ", decodeException);
        return ApiResult.fail(CommonErrorCode.ERROR);
    }

    //系统异常
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult<Object> exception(Exception exception) {
        log.warn("Global Exception exception ", exception);
        return ApiResult.fail(CommonErrorCode.ERROR);
    }

    //超级系统异常
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult<Object> throwable(Throwable throwable) {
        log.warn("Global Exception Throwable ", throwable);
        return ApiResult.fail(CommonErrorCode.SYSTEM_ERROR);
    }

}