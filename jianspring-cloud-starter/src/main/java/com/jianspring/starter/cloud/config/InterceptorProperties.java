package com.jianspring.starter.cloud.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 拦截器配置属性
 */
@Component
@ConfigurationProperties(prefix = "jianspring.interceptor")
public class InterceptorProperties {

    /**
     * 重复请求拦截器配置
     */
    private DuplicateRequest duplicateRequest = new DuplicateRequest();

    public DuplicateRequest getDuplicateRequest() {
        return duplicateRequest;
    }

    public void setDuplicateRequest(DuplicateRequest duplicateRequest) {
        this.duplicateRequest = duplicateRequest;
    }

    public static class DuplicateRequest {
        /**
         * 拦截路径
         */
        private List<String> includePaths = new ArrayList<>();

        /**
         * 排除路径
         */
        private List<String> excludePaths = new ArrayList<>();

        /**
         * 是否启用
         */
        private boolean enabled = true;

        public List<String> getIncludePaths() {
            return includePaths;
        }

        public void setIncludePaths(List<String> includePaths) {
            this.includePaths = includePaths;
        }

        public List<String> getExcludePaths() {
            return excludePaths;
        }

        public void setExcludePaths(List<String> excludePaths) {
            this.excludePaths = excludePaths;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}