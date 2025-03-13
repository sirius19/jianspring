package com.jianspring.starter.db.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @Author: InfoInsights
 * @Date: 2023/2/23 下午4:44
 * @Version: 1.0.0
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class BaseLogicEntity<T extends Model<?>> extends BaseEntity<T> {

    @TableLogic(value = "0", delval = "id")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long deleted;

}