package com.jianspring.starter.feign.decode;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jianspring.starter.commons.error.CommonErrorCode;
import com.jianspring.starter.commons.exception.BizException;
import feign.Request;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @Author:  InfoInsights
 * @Date: 2023/2/22 下午4:42
 * @Version: 1.0.0
 */
@Slf4j
public class BasicFeignResponseErrorDecode implements ErrorDecoder {

    @Override
    public Exception decode(String s, Response response) {
        String     body       = null;
        Request    request    = response.request();
        HttpStatus httpStatus = HttpStatus.valueOf(response.status());
        try {
            body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("", e);
            log.error("feign decode error url:{}, method:{},headers:{},response status:{}", request.url(), response.request().httpMethod(),
                    JSONUtil.toJsonStr(request.headers()), httpStatus.value());
        }
        if (!JSONUtil.isJson(body)) {
            log.error("feign request error url:{}, method:{},headers:{},response status:{},body:{}", request.url(), response.request().httpMethod(),
                    JSONUtil.toJsonStr(request.headers()), httpStatus.value(), body);
            throw BizException.of(CommonErrorCode.SYSTEM_ERROR.getCode(), body);
        }
        JSONObject jsonObject = JSONUtil.parseObj(body);
        if (jsonObject.isEmpty()) {
            log.error("feign request error url:{}, method:{},headers:{},response status:{},body:{}", request.url(), response.request().httpMethod(),
                    JSONUtil.toJsonStr(request.headers()), httpStatus.value(), body);
            throw BizException.of(CommonErrorCode.BIZ_ERROR);
        }
        log.error("feign request fail url:{}, method:{},headers:{},response status:{},body:{}", request.url(), response.request().httpMethod(),
                JSONUtil.toJsonStr(request.headers()), httpStatus.value(), body);
        throw BizException.of(jsonObject.getInt("code"), jsonObject.getStr("msg"));
    }
}
