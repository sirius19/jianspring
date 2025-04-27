package com.jianspring.starter.commons.enums;

import lombok.Getter;

/**
 * @author: InfoInsights
 * @Date: 2023/6/7 10:27
 * @Version: 1.0.0
 */
@Getter
public enum HeaderEnums {

    TOKEN("JIAN-TOKEN", "token"),
    ACCOUNT_ID("JIAN-ACCOUNT-ID", "账号ID"),
    USER_ID("JIAN-USER-ID", "用户ID"),
    USER_TYPE("JIAN-USER-TYPE", "用户类型"),
    USER_MOBILE_NUMBER("JIAN-MOBILE-NUMBER", "手机号"),
    NAME("JIAN-NAME", "用户名称"),
    USER_NAME("JIAN-USER-NAME", "用户名"),
    TENANT_ID("JIAN-TENANT-ID", "租户ID"),
    LOCALE("JIAN-LOCALE", "国际化"),
    PARENT_ID("JIAN-PARENT-ID", "tracing 父ID"),
    TRACE_ID("JIAN-TRACE-ID", "tracing"),
    SPAN_ID("JIAN-SPAN-ID", "span"),
    TOKEN_SIGN("JIAN-TOKEN-SIGN", "token sign"),
    VERSION("JIAN-VERSION", "版本号"),
    APP_KEY("JIAN-APP-KEY", "应用标识"),
    HINT_KEY("hint", "hint"),
    REQUESTID("JIAN-REQUESTID", "请求id"),
    SENTINEL_ORIGN("S-User", "sentinel origin标识"),
    JIANSPRINGMODEL("JIAN-MODEL", "JianSpringModel"),
    ;

    private final String key;

    private final String desc;

    HeaderEnums(String key, String desc) {
        this.key = key;
        this.desc = desc;
    }
}