package com.jianspring.starter.db.fill;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.jianspring.starter.commons.UserContextUtils;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * @Author: InfoInsights
 * @Date: 2023/2/23 下午5:05
 * @Version: 1.0.0
 */
public class CustomMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        UserContextUtils.UserContext userContext = UserContextUtils.get();
        this.strictInsertFill(metaObject, "createdTime", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "createdBy", userContext::getUserId, Long.class);
        this.strictInsertFill(metaObject, "updatedTime", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "updatedBy", userContext::getUserId, Long.class);
        if (metaObject.hasSetter("deleted")) {
            this.strictInsertFill(metaObject, "deleted", () -> 0L, Long.class);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        UserContextUtils.UserContext userContext = UserContextUtils.get();
        this.strictInsertFill(metaObject, "updatedTime", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "updatedBy", userContext::getUserId, Long.class);
    }
}
