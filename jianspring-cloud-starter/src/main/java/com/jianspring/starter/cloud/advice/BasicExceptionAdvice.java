package com.jianspring.starter.cloud.advice;

import com.jianspring.starter.commons.error.CommonErrorCode;
import com.jianspring.starter.commons.exception.BizException;
import com.jianspring.starter.commons.exception.LoginException;
import com.jianspring.starter.commons.exception.PermissionException;
import com.jianspring.starter.commons.exception.SentinelBlockException;
import com.jianspring.starter.commons.result.ApiResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.DecodeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.annotation.Order;
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

import java.util.Locale;
import java.util.Optional;

/**
 * 全局异常处理器
 *
 * @Author: InfoInsights
 * @Date: 2023/2/21 上午10:14
 * @Version: 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class BasicExceptionAdvice {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private HttpServletRequest request;

    /**
     * 参数绑定异常处理
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<Object> bindException(BindException bindException) {
        logRequestInfo(bindException, "参数绑定异常");
        return Optional.ofNullable(bindException.getFieldError())
                .map(fieldError -> ApiResult.fail(CommonErrorCode.PARAM_ERROR.getCode(), fieldError.getDefaultMessage()))
                .orElse(ApiResult.fail(CommonErrorCode.PARAM_ERROR));
    }

    /**
     * 方法参数校验异常处理
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<Object> methodArgumentNotValidException(MethodArgumentNotValidException methodArgumentNotValidException) {
        logRequestInfo(methodArgumentNotValidException, "方法参数校验异常");

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

    /**
     * 参数类型不匹配异常处理
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<Object> argumentTypeMismatchException(MethodArgumentTypeMismatchException exception) {
        return handleException(exception, CommonErrorCode.PARAM_ERROR, "参数类型不匹配异常");
    }

    /**
     * HTTP请求方法不支持异常处理
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiResult<Object> methodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        logRequestInfo(exception, "HTTP请求方法不支持异常");
        return ApiResult.fail(CommonErrorCode.HTTP_METHOD_NOT_SUPPORT, exception.getMethod());
    }

    /**
     * HTTP消息不可读异常处理
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<Object> httpMessageNotReadableException(HttpMessageNotReadableException exception) {
        return handleException(exception, CommonErrorCode.PARAM_ERROR, "HTTP消息不可读异常");
    }

    /**
     * 非法参数异常处理
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<Object> illegalArgumentException(IllegalArgumentException exception) {
        return handleException(exception, CommonErrorCode.PARAM_ERROR, "非法参数异常");
    }

    /**
     * 解析异常处理
     */
    @ExceptionHandler(ParseException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult<Object> parseException(ParseException exception) {
        return handleException(exception, CommonErrorCode.DATA_PARSE_ERROR, "解析异常");
    }

    /**
     * 登录异常处理
     */
    @ExceptionHandler(LoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @Order(1)
    public ApiResult<Object> loginException(LoginException loginException) {
        log.warn("登录异常: {}", loginException.getMessage(), loginException);
        logRequestInfo(loginException, "登录异常");
        return ApiResult.fail(loginException.getErrorCode());
    }

    /**
     * 权限异常处理
     */
    @ExceptionHandler(PermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResult<Object> permissionException(PermissionException exception) {
        logRequestInfo(exception, "权限异常");
        return ApiResult.fail(exception.getCode(), getLocalizedMessage(exception.getMessage(), null, Locale.getDefault()));
    }

    /**
     * 业务异常处理
     */
    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @Order(2) // 较高的优先级
    public ApiResult<Object> bizException(BizException bizException) {
        log.warn("Global Exception BizException ", bizException);
        return ApiResult.fail(bizException.getErrorCode());
    }

    /**
     * Sentinel限流异常处理
     */
    @ExceptionHandler(SentinelBlockException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ApiResult<Object> blockException(SentinelBlockException exception) {
        logRequestInfo(exception, "Sentinel限流异常");
        return ApiResult.fail(exception.getCode(), getLocalizedMessage(exception.getMessage(), null, Locale.getDefault()));
    }

    /**
     * 解码异常处理
     */
    @ExceptionHandler(DecodeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult<Object> decodeException(DecodeException exception) {
        return handleException(exception, CommonErrorCode.ERROR, "解码异常");
    }

    /**
     * 通用系统异常处理
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @Order(100)
    public ApiResult<Object> exception(Exception exception) {
        log.error("系统异常: {}", exception.getMessage(), exception);
        logRequestInfo(exception, "系统异常");
        return ApiResult.fail(CommonErrorCode.ERROR);
    }

    /**
     * 顶级异常处理
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @Order(100)
    public ApiResult<Object> throwable(Throwable throwable) {
        log.error("严重系统异常: {}", throwable.getMessage(), throwable);
        return ApiResult.fail(CommonErrorCode.SYSTEM_ERROR);
    }

    /**
     * 通用异常处理方法
     */
    private <T extends Exception> ApiResult<Object> handleException(T exception, CommonErrorCode errorCode, String exceptionType) {
        logRequestInfo(exception, exceptionType);
        return ApiResult.fail(errorCode);
    }

    /**
     * 自定义错误码异常处理方法
     */
    private <T extends Exception> ApiResult<Object> handleExceptionWithCustomCode(T exception, Integer code, String message) {
        logRequestInfo(exception, "自定义错误码异常");
        return ApiResult.fail(code, message);
    }

    /**
     * 获取本地化消息
     */
    private String getLocalizedMessage(String code, Object[] args, Locale locale) {
        try {
            return messageSource.getMessage(code, args, locale);
        } catch (NoSuchMessageException e) {
            return code;
        }
    }

    /**
     * 记录请求信息
     */
    private void logRequestInfo(Exception ex, String exceptionType) {
        if (request != null) {
            log.info("{}请求信息 - URL: {}, Method: {}, IP: {}",
                    exceptionType,
                    request.getRequestURI(),
                    request.getMethod(),
                    getClientIp(request));
        } else {
            log.info("{}: {}", exceptionType, ex.getMessage());
        }
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}