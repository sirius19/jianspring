package com.jianspring.starter.restclient.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

import java.util.function.Function;

/**
 * RestClient 异常处理类
 */
public class RestClientExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestClientExceptionHandler.class);

    /**
     * 处理 RestClient 异常
     *
     * @param e 异常
     * @return 业务异常
     */
    public static BusinessException handleException(RestClientException e) {
        log.error("RestClient 请求异常", e);

        if (e instanceof HttpClientErrorException clientErrorException) {
            HttpStatusCode statusCode = clientErrorException.getStatusCode();
            String responseBody = clientErrorException.getResponseBodyAsString();

            if (statusCode.equals(HttpStatus.NOT_FOUND)) {
                return new BusinessException(404, "请求的资源不存在", responseBody);
            } else if (statusCode.equals(HttpStatus.UNAUTHORIZED)) {
                return new BusinessException(401, "未授权的请求", responseBody);
            } else if (statusCode.equals(HttpStatus.FORBIDDEN)) {
                return new BusinessException(403, "禁止访问", responseBody);
            } else {
                return new BusinessException(statusCode.value(), "客户端错误: " + statusCode.toString(), responseBody);
            }
        } else if (e instanceof HttpServerErrorException serverErrorException) {
            HttpStatusCode statusCode = serverErrorException.getStatusCode();
            String responseBody = serverErrorException.getResponseBodyAsString();

            return new BusinessException(statusCode.value(), "服务器错误: " + statusCode.toString(), responseBody);
        } else {
            return new BusinessException(500, "远程调用异常", e.getMessage());
        }
    }

    /**
     * 执行带异常处理的操作
     *
     * @param operation 操作
     * @param <T>       返回类型
     * @return 操作结果
     */
    public static <T> T executeWithExceptionHandling(Function<Void, T> operation) {
        try {
            return operation.apply(null);
        } catch (RestClientException e) {
            throw handleException(e);
        }
    }
}