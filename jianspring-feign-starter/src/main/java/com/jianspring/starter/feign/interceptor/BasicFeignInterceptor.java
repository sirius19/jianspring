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
            requestTemplate.header(HeaderEnums.JIANSPRINGMODEL.getKey(), JianSpringModel.INNER_FEIGN.name());
            if (StrUtil.isNotBlank(application)) {
                requestTemplate.header(HeaderEnums.SENTINEL_ORIGN.getKey(), application);
            }
            if (null != userContext.getLocale()) {
                requestTemplate.header(HeaderEnums.LOCALE.getKey(), userContext.getLocale());
            }
            if (null != userContext.getParentId()) {
                requestTemplate.header(HeaderEnums.PARENT_ID.getKey(), userContext.getParentId());
            }
            if (null != userContext.getSpanId()) {
                requestTemplate.header(HeaderEnums.SPAN_ID.getKey(), userContext.getSpanId());
            }
            if (null != userContext.getTraceId()) {
                requestTemplate.header(HeaderEnums.TRACE_ID.getKey(), userContext.getTraceId());
            }
            if (null != userContext.getAccountId()) {
                requestTemplate.header(HeaderEnums.ACCOUNT_ID.getKey(), userContext.getAccountId().toString());
            }
            if (null != userContext.getUserId()) {
                requestTemplate.header(HeaderEnums.USER_ID.getKey(), userContext.getUserId().toString());
            }
            if (null != userContext.getUserType()) {
                requestTemplate.header(HeaderEnums.USER_TYPE.getKey(), userContext.getUserType().toString());
            }
            if (null != userContext.getTenantId()) {
                requestTemplate.header(HeaderEnums.TENANT_ID.getKey(), userContext.getTenantId().toString());
            }

            requestTemplate.header(HeaderEnums.HINT_KEY.getKey(), userContext.getHint());

            requestTemplate.header(HeaderEnums.USER_MOBILE_NUMBER.getKey(), userContext.getMobileNumber());
            requestTemplate.header(HeaderEnums.NAME.getKey(), URLEncodeUtil.encode(userContext.getName(), StandardCharsets.UTF_8));
            requestTemplate.header(HeaderEnums.USER_NAME.getKey(), URLEncodeUtil.encode(userContext.getUserName(), StandardCharsets.UTF_8));
            requestTemplate.header(HeaderEnums.TOKEN_SIGN.getKey(), userContext.getTokenSign());
            requestTemplate.header(HeaderEnums.VERSION.getKey(), userContext.getVersion());
            requestTemplate.header(HeaderEnums.APP_KEY.getKey(), userContext.getAppKey());
        } catch (Throwable throwable) {
            log.warn("requestTemplate user context error:{}", userContext);
        }

    }
}
