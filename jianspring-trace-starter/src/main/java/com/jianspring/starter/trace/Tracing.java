/*
 * Licensed Materials - Property of ROOTCLOUD
 * THIS MODULE IS "RESTRICTED MATERIALS OF ROOTCLOUD"
 * (c) Copyright ROOTCLOUD Inc. 2021 All Rights Reserved
 *
 * The source code for this program is not published or
 * otherwise divested of its trade secrets
 */package com.jianspring.starter.trace;

import lombok.Data;
import lombok.ToString;

/**
*
* @Author:  InfoInsights
* @Date: 2022/11/6 下午4:57
* @Version: 1.0.0
*/
@Data
@ToString
public class Tracing {

    private String ip;

    private String path;

    private String method;

    private String header;

    private String queryParams;

    private String requestBody;

    private String responseBody;

    private Integer status;

    private long costTime;

}