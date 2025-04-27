package com.jianspring.starter.iam;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * @Author: InfoInsights
 * @Date: 2023/4/24 下午2:39
 * @Version: 1.0.0
 */
@Data
@ToString
@RefreshScope
@ConfigurationProperties(prefix = "iam-jwt")
public class IamJwtProperties {

    private String sign;
    private Boolean allowDirectInvoke = false;
    private Long expiration = 86400000L * 30;// 默认30天

}