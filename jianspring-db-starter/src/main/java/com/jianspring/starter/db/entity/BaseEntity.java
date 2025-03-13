package com.jianspring.starter.db.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * @Author: InfoInsights
 * @Date: 2023/2/23 下午4:44
 * @Version: 1.0.0
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class BaseEntity<T extends Model<?>> extends Model<T> {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private Long createdBy;

    @TableField(value = "updated_time", fill = FieldFill.UPDATE)
    private LocalDateTime updatedTime;

    @TableField(value = "updated_by", fill = FieldFill.UPDATE)
    private Long updatedBy;
}