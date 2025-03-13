package com.jianspring.starter.db.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
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
public class BaseTenantLogicEntity<T extends Model<?>> extends BaseLogicEntity<T> {

    private Long tenantId;


}