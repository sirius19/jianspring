package com.jianspring.starter.feign.decode;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jianspring.starter.commons.error.CommonErrorCode;
import com.jianspring.starter.commons.exception.BizException;
import feign.Response;
import feign.Util;
import feign.codec.Decoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * @Author:  InfoInsights
 * @Date: 2023/2/22 下午4:42
 * @Version: 1.0.0
 */
@Slf4j
public class BasicFeignResponseDecode implements Decoder {

    private static final JSONConfig config;

    static {
        config = new JSONConfig();
        config.setIgnoreNullValue(false);
    }

    @Override
    public Object decode(Response response, Type type) {
        try {
            String body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
            JSON json;
            try {
                json = JSONUtil.parse(body, config);
            } catch (Exception e) {
                return dealBody(body, type);
            }

            if ((json instanceof JSONObject && (!((JSONObject) json).containsKey("data")
                    || !((JSONObject) json).containsKey("code")))
                    || json instanceof JSONArray) {
                return json.toBean(type);
            }

            Object obj;
            if (type instanceof Class && !isGenericType(type)) {
                obj = ((JSONObject) json).get("data", (Class) type);
            } else {
                obj = ((JSONObject) json).get("data");
            }

            if (obj instanceof JSONArray) {
                JSONArray data = JSONUtil.parseArray(obj.toString());
                return data.isEmpty() ? obj : data.toBean(type);
            } else if (obj instanceof JSONObject) {
                JSONObject data = JSONUtil.parseObj(obj);
                return data.isEmpty() ? obj : data.toBean(type);
            }

            log.debug("Response Feign,url:{},method:{},request body:{}, response body:{}", response.request().url(), response.request().httpMethod(), Objects.toString(response.request().body(), ""), body);
            return obj;
        } catch (Exception e) {
            log.error("Error decoding response: " + e.getMessage(), e);
            throw BizException.of(CommonErrorCode.SYSTEM_ERROR.getCode(), "Error decoding response");
        }
    }

    private static boolean isGenericType(Type type) {
        ResolvableType resolvableType = ResolvableType.forType(type);
        return resolvableType.hasGenerics();
    }

    private Object dealBody(String body, Type type) {
        if (type == String.class) {
            return body;
        } else if (type == Integer.class) {
            return Integer.parseInt(body);
        } else if (type == Long.class) {
            return Long.valueOf(body);
        } else if (type == BigDecimal.class) {
            return new BigDecimal(body);
        } else if (type == Float.class) {
            return Float.valueOf(body);
        } else if (type == Double.class) {
            return Double.valueOf(body);
        } else if (type == Boolean.class) {
            return Boolean.valueOf(body);
        } else if (type == Date.class) {
            Date date = parseDate(body);
            return date != null ? date : body;
        } else {
            return body;
        }
    }

    private static final String[] DATE_FORMATS = {
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",  // 示例：2023-11-23T10:30:00.000+0000
            "yyyy-MM-dd'T'HH:mm:ssZ",      // 示例：2023-11-23T10:30:00+0000
            "yyyy-MM-dd HH:mm:ss",         // 示例：2023-11-23 10:30:00
            "yyyy-MM-dd"                    // 示例：2023-11-23
    };

    public static Date parseDate(String dateString) {
        for (String format : DATE_FORMATS) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(format);
                return dateFormat.parse(dateString);
            } catch (ParseException e) {
                // 尝试下一个日期格式
            }
        }
        // 如果所有格式都尝试失败，返回 null 或者抛出异常，具体取决于需求
        return null;
    }

}