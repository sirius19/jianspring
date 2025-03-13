package com.jianspring.starter.cloud.interceptor;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")  // 仅在开发环境下加载
public class SwaggerConfig {

    @Bean
    @ConditionalOnMissingBean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/**")
                .build();
    }


    @Bean
    @ConditionalOnMissingBean
    public OpenAPI jianSpringOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("API文档")
                        .version("1.0")
                        .description("系统接口文档"));
    }
}

