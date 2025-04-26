package com.jianspring.starter.cloud.interceptor;

import com.jianspring.starter.commons.UserContextUtils;
import com.jianspring.starter.commons.enums.HeaderEnums;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.Locale;


public class I18nInterceptor extends LocaleChangeInterceptor {

    @Autowired
    @Qualifier("customLocaleResolver")  // 注入自定义名称的LocaleResolver
    private LocaleResolver customLocaleResolver;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String locale = request.getHeader(HeaderEnums.LOCALE.getKey());
        if (null == locale) {
            return true;
        }
        if (!checkMethod(request.getMethod())) {
            return true;
        }
        
        // 优先使用自定义的LocaleResolver
        LocaleResolver localeResolver = customLocaleResolver;
        if (localeResolver == null) {
            // 如果自定义的为null，则尝试从请求中获取
            localeResolver = RequestContextUtils.getLocaleResolver(request);
            if (localeResolver == null) {
                throw new IllegalStateException(
                        "No LocaleResolver found: not in a DispatcherServlet request?");
            }
        }
        
        try {
            Locale localeBean = parseLocaleValue(locale);
            localeResolver.setLocale(request, response, localeBean);
            if (null != localeBean) {
                UserContextUtils.setOnlyLocale(localeBean.toString());
            }
        } catch (IllegalArgumentException ex) {
            if (!isIgnoreInvalidLocale()) {
                throw ex;
            }
            logger.warn("Ignoring invalid locale value [" + locale + "]: " + ex.getMessage());
        } catch (UnsupportedOperationException ex) {
            // 捕获不支持设置语言的异常
            logger.warn("当前 LocaleResolver 不支持设置语言: " + ex.getMessage());
            // 仍然设置 UserContext 中的语言
            try {
                Locale localeBean = parseLocaleValue(locale);
                if (null != localeBean) {
                    UserContextUtils.setOnlyLocale(localeBean.toString());
                }
            } catch (Exception e) {
                logger.warn("设置 UserContext 语言失败: " + e.getMessage());
            }
        }
        return true;
    }

    private boolean checkMethod(String currentMethod) {
        String[] configuredMethods = getHttpMethods();
        if (ObjectUtils.isEmpty(configuredMethods)) {
            return true;
        }
        for (String configuredMethod : configuredMethods) {
            if (configuredMethod.equalsIgnoreCase(currentMethod)) {
                return true;
            }
        }
        return false;
    }
}
