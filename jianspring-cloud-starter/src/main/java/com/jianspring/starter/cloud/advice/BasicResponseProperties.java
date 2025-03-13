package com.jianspring.starter.cloud.advice;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jianspring.response.wrapper")
public class BasicResponseProperties {
    private Boolean encryptEnable = true;

    private String encryptKey;

    public Boolean getEncryptEnable() {
        return encryptEnable;
    }

    public void setEncryptEnable(Boolean encryptEnable) {
        this.encryptEnable = encryptEnable;
    }

    public String getEncryptKey() {
        return encryptKey;
    }

    public void setEncryptKey(String encryptKey) {
        this.encryptKey = encryptKey;
    }
}
