package com.jianspring.starter.commons.result;

import com.jianspring.starter.commons.error.CommonErrorCode;
import com.jianspring.starter.commons.exception.BizException;

/**
 * 校验接口请求状态
 *
 * @Author:  InfoInsights
 */
public class ApiResultCheckUtil {
    /**
     * 请求成功
     */
    private static final Integer CODE = 200;


    private ApiResultCheckUtil() {
    }

    public static void checkStatus(Integer code, String msg) {
        if (!CODE.equals(code)) {
            throw new BizException(CommonErrorCode.ERROR.getCode(), msg);
        }
    }
}
