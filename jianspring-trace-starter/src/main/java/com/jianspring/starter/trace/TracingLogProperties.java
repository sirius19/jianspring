package com.jianspring.starter.trace;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "trace.log.filter")
public class TracingLogProperties {

    @Value("${excluded-endpoints:}")
    private String excludedEndpoints;

    public String[] getExcludedEndpoints() {
        // 将配置的字符串拆分成数组
        return excludedEndpoints.split(",");
    }

    public void setExcludedEndpoints(String excludedEndpoints) {
        this.excludedEndpoints = excludedEndpoints;
    }
}
