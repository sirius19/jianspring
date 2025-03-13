package com.jianspring.starter.oss;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aliyun.oss")
public class AliOssProperties {
    private String endpoint = "https://oss-cn-hangzhou.aliyuncs.com";
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private boolean enabled = true;
    private int maxConnections = 200;
    private int socketTimeout = 10000;
    private int connectionTimeout = 10000;
    private int connectionRequestTimeout = 1000;
    private int idleConnectionTime = 10000;
    private int maxErrorRetry = 5;
    private long partSize = 52428800; // 50MB
    private long defaultExpireTime = 32400000;

    // 重试配置
    private int maxAttempts = 3;
    private long initialInterval = 1000;
    private double multiplier = 1.5;
    private long maxInterval = 10000;

    // 添加文件类型限制配置
    private String[] allowedContentTypes;
    private long maxFileSize = 104857600; // 默认100MB

    // 文件名处理策略
    private String fileNameStrategy = "ORIGINAL"; // UUID, TIMESTAMP, ORIGINAL, CUSTOM
    private String fileNamePrefix = "";
    private String fileNameSuffix = "";


    public long getDefaultExpireTime() {
        return defaultExpireTime;
    }

    public void setDefaultExpireTime(long defaultExpireTime) {
        this.defaultExpireTime = defaultExpireTime;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    public void setConnectionRequestTimeout(int connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    public int getIdleConnectionTime() {
        return idleConnectionTime;
    }

    public void setIdleConnectionTime(int idleConnectionTime) {
        this.idleConnectionTime = idleConnectionTime;
    }

    public int getMaxErrorRetry() {
        return maxErrorRetry;
    }

    public void setMaxErrorRetry(int maxErrorRetry) {
        this.maxErrorRetry = maxErrorRetry;
    }

    public long getPartSize() {
        return partSize;
    }

    public void setPartSize(long partSize) {
        this.partSize = partSize;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public long getInitialInterval() {
        return initialInterval;
    }

    public void setInitialInterval(long initialInterval) {
        this.initialInterval = initialInterval;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public long getMaxInterval() {
        return maxInterval;
    }

    public void setMaxInterval(long maxInterval) {
        this.maxInterval = maxInterval;
    }

    public String[] getAllowedContentTypes() {
        return allowedContentTypes;
    }

    public void setAllowedContentTypes(String[] allowedContentTypes) {
        this.allowedContentTypes = allowedContentTypes;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public String getFileNameStrategy() {
        return fileNameStrategy;
    }

    public void setFileNameStrategy(String fileNameStrategy) {
        this.fileNameStrategy = fileNameStrategy;
    }

    public String getFileNamePrefix() {
        return fileNamePrefix;
    }

    public void setFileNamePrefix(String fileNamePrefix) {
        this.fileNamePrefix = fileNamePrefix;
    }

    public String getFileNameSuffix() {
        return fileNameSuffix;
    }

    public void setFileNameSuffix(String fileNameSuffix) {
        this.fileNameSuffix = fileNameSuffix;
    }
}
