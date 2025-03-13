package com.jianspring.starter.db.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @Author:  InfoInsights
 * @Date: 2023/2/23 下午4:44
 * @Version: 1.0.0
 */

@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class BaseTenantEntity extends BaseEntity {

    private Long tenantId;

}