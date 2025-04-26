package com.jianspring.starter.commons;

import lombok.Data;
import lombok.ToString;

import java.util.Locale;

public class UserContextUtils {

    private static final ThreadLocal<UserContext> CONTEXT = new InheritableThreadLocal<>();

    public static void set(UserContext userContext) {
        CONTEXT.remove();
        CONTEXT.set(userContext);
    }

    public static UserContext get() {
        UserContext context = CONTEXT.get();
        if (null == context) {
            return UserContext.defaultContext();
        }
        return context;
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static void setOnlyLocale(String locale) {
        UserContext context = CONTEXT.get();
        if (null == context) {
            context = UserContext.defaultContext();
        }
        context.setLocale(locale);
        CONTEXT.set(context);
    }

    public static void setOnlyUser(UserContext userContext) {
        UserContext context = CONTEXT.get();
        if (null != context) {
            userContext.setParentId(context.getParentId());
            userContext.setSpanId(context.getSpanId());
            userContext.setTraceId(context.getTraceId());
            if (userContext.getHint() == null) {
                userContext.setHint(context.getHint());
            }
        }
        CONTEXT.set(userContext);
    }

    public static void setOnlyTracing(String parentId, String spanId, String traceId) {
        UserContext context = CONTEXT.get();
        if (null == context) {
            context = UserContext.defaultContext();
        }
        context.setParentId(parentId);
        context.setSpanId(spanId);
        context.setTraceId(traceId);
        CONTEXT.set(context);
    }

    public static void setOnlyHint(String hint) {
        UserContext context = CONTEXT.get();
        if (null == context) {
            context = UserContext.defaultContext();
        }
        context.setHint(hint);
        CONTEXT.set(context);
    }

    @Data
    @ToString
    public static class UserContext {

        private Long accountId;

        private Long userId;

        private Integer userType;

        private String mobileNumber;

        private String name;

        private String userName;

        private Long tenantId;

        private String locale;

        private String parentId;

        private String traceId;

        private String spanId;

        private String tokenSign;

        private String version;

        private String appKey;

        private String hint;

        public static UserContext defaultContext() {
            UserContext userContext = new UserContext();
            userContext.setAccountId(0L);
            userContext.setUserId(0L);
            userContext.setMobileNumber(null);
            userContext.setName(null);
            userContext.setUserName("default user");
            userContext.setTenantId(0L);
            userContext.setLocale(Locale.CHINA.toString());
            userContext.setTraceId(null);
            userContext.setSpanId(null);
            userContext.setParentId(null);
            userContext.setUserType(null);
            userContext.setVersion("1.0");
            userContext.setTokenSign("");
            userContext.setAppKey("");
            userContext.setHint(null);
            return userContext;
        }
    }

}