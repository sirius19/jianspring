package com.jianspring.starter.feign.interceptor;

import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.jianspring.starter.commons.UserContextUtils;
import com.jianspring.starter.commons.enums.HeaderEnums;
import com.jianspring.starter.feign.enums.JianSpringModel;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import java.nio.charset.StandardCharsets;

/**
 * @author: InfoInsights
 * @Date: 2023/2/21 上午9:41
 * @Version: 1.0.0
 */
@Slf4j
public class BasicFeignInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        UserContextUtils.UserContext userContext = UserContextUtils.get();
        log.debug("Request Feign Interface,serviceName:{},url:{},method:{},body:{},userContext:{}", requestTemplate.feignTarget().name(), requestTemplate.feignTarget().url(), requestTemplate.request().httpMethod().name(), null == requestTemplate.request().body() ? null : new String(requestTemplate.request().body()), userContext);
        try {
            String application = SpringUtil.getBean(Environment.class).getProperty("spring.application.name");
            requestTemplate.header("JIAN-MODEL", JianSpringModel.INNER_FEIGN.name());
            if (StrUtil.isNotBlank(application)) {
                requestTemplate.header(HeaderEnums.SENTINEL_ORIGN.getKey(), application);
            }
            if (null != userContext.getLocale()) {
                requestTemplate.header("JIAN-LOCALE", userContext.getLocale());
            }
            if (null != userContext.getParentId()) {
                requestTemplate.header("JIAN-PARENT-ID", userContext.getParentId());
            }
            if (null != userContext.getSpanId()) {
                requestTemplate.header("JIAN-SPAN-ID", userContext.getSpanId());
            }
            if (null != userContext.getTraceId()) {
                requestTemplate.header("JIAN-TRACE-ID", userContext.getTraceId());
            }
            if (null != userContext.getAccountId()) {
                requestTemplate.header("JIAN-ACCOUNT-ID", userContext.getAccountId().toString());
            }
            if (null != userContext.getUserId()) {
                requestTemplate.header("JIAN-USER-ID", userContext.getUserId().toString());
            }
            if (null != userContext.getUserType()) {
                requestTemplate.header("JIAN-USER-TYPE", userContext.getUserType().toString());
            }
            if (null != userContext.getTenantId()) {
                requestTemplate.header("JIAN-TENANT-ID", userContext.getTenantId().toString());
            }

            requestTemplate.header(HeaderEnums.HINT_KEY.getKey(), userContext.getHint());

            requestTemplate.header("JIAN-MOBILE-NUMBER", userContext.getMobileNumber());
            requestTemplate.header("JIAN-NAME", URLEncodeUtil.encode(userContext.getName(), StandardCharsets.UTF_8));
            requestTemplate.header("JIAN-USER-NAME", URLEncodeUtil.encode(userContext.getUserName(), StandardCharsets.UTF_8));
            requestTemplate.header("JIAN-LOCALE", userContext.getLocale());
            requestTemplate.header("JIAN-PARENT-ID", userContext.getParentId());
            requestTemplate.header("JIAN-TRACE-ID", userContext.getTraceId());
            requestTemplate.header("JIAN-SPAN-ID", userContext.getSpanId());
            requestTemplate.header("JIAN-TOKEN-SIGN", userContext.getTokenSign());
            requestTemplate.header("JIAN-VERSION", userContext.getVersion());
            requestTemplate.header("JIAN-APP-KEY", userContext.getAppKey());
        } catch (Throwable throwable) {
            log.warn("requestTemplate user context error:{}", userContext);
        }

    }
}
