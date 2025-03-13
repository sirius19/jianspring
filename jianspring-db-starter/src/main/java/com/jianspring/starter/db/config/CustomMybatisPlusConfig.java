package com.jianspring.starter.db.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.jianspring.starter.commons.UserContextUtils;
import com.jianspring.starter.db.fill.CustomMetaObjectHandler;
import com.jianspring.starter.db.id.CustomIdGenerator;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: InfoInsights
 * @Date: 2023/2/23 下午5:02
 * @Version: 1.0.0
 */
@Configuration
public class CustomMybatisPlusConfig {

    @Bean
    IdentifierGenerator identifierGenerator() {
        return new CustomIdGenerator();
    }

    @Bean
    CustomMetaObjectHandler metaObjectHandler() {
        return new CustomMetaObjectHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                // 进行空指针检查，确保 UserContextUtils.get() 和 tenantId 不为 null
                UserContextUtils.UserContext userContext = UserContextUtils.get();
                if (userContext != null && userContext.getTenantId() != null) {
                    return new StringValue(userContext.getTenantId().toString());
                } else {
                    // 当 userContext 或 tenantId 为 null 时，返回一个默认值或处理逻辑
                    return new StringValue("0"); // 你可以根据业务逻辑返回适当的默认值
                }
            }

            @Override
            public String getTenantIdColumn() {
                return "tenant_id";
            }

            @Override
            public boolean ignoreTable(String tableName) {
                return false;
            }
        }));

        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

}