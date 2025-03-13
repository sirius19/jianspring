package com.jianspring.starter.cloud.interceptor;

import com.jianspring.starter.commons.UserContextUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.Locale;


public class I18nInterceptor extends LocaleChangeInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String locale = request.getHeader(getParamName());
        if (null == locale) {
            return true;
        }
        if (!checkMethod(request.getMethod())) {
            return true;
        }
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        if (localeResolver == null) {
            throw new IllegalStateException(
                    "No LocaleResolver found: not in a DispatcherServlet request?");
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
