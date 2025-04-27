package com.jianspring.starter.cloud.interceptor;

import com.jianspring.starter.cloud.config.InterceptorProperties;
import com.jianspring.starter.iam.AuthenticationInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

/**
 * @Author: InfoInsights
 * @Date: 2023/2/23 下午3:04
 * @Version: 1.0.0
 */
@Configuration
public class MvcInterceptorConfig {

    @Bean
    public NoWrapInterceptor noWrapInterceptor() {
        return new NoWrapInterceptor();
    }

    @Bean
    public DuplicateRequestInterceptor duplicateRequestInterceptor() {
        return new DuplicateRequestInterceptor(requestCacheService());
    }

    @Bean
    public RequestCacheService requestCacheService() {
        return new RequestCacheService();
    }

    @Bean
    @ConditionalOnMissingBean(I18nInterceptor.class)
    public LocaleChangeInterceptor localeChangeInterceptor() {
        return new I18nInterceptor();
    }

    @Bean(name = "customLocaleResolver")
    @ConditionalOnMissingBean(name = "customLocaleResolver")
    LocaleResolver customLocaleResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        localeResolver.setDefaultLocale(LocaleContextHolder.getLocale());
        return localeResolver;
    }

    @Bean
    @DependsOn({"localeChangeInterceptor", "authenticationInterceptor"})
    WebMvcConfigurer webMvcConfigurer(AuthenticationInterceptor authenticationInterceptor, 
                                     LocaleChangeInterceptor localeChangeInterceptor,
                                     InterceptorProperties interceptorProperties) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                if (null != authenticationInterceptor) {
                    registry.addInterceptor(authenticationInterceptor)
                            .addPathPatterns("/**")
                            .excludePathPatterns("/health", "/api-doc", "/actuator/**",
                                    "/favicon.ico", "/swagger-ui.html",
                                    "/webjars/**", "/v3/api-docs/**", "/error", "/v3/api-docs.yaml");
                }
                if (null != localeChangeInterceptor) {
                    registry.addInterceptor(localeChangeInterceptor)
                            .addPathPatterns("/**")
                            .excludePathPatterns("/health", "/api-doc", "/actuator/**", "/v3/api-docs/**", "/swagger-ui.html");
                }

                if (duplicateRequestInterceptor() != null && interceptorProperties.getDuplicateRequest().isEnabled()) {
                    InterceptorProperties.DuplicateRequest config = interceptorProperties.getDuplicateRequest();
                    registry.addInterceptor(duplicateRequestInterceptor())
                            .addPathPatterns(config.getIncludePaths().toArray(new String[0]))
                            .excludePathPatterns(config.getExcludePaths().toArray(new String[0]));
                }

                if (null != noWrapInterceptor()) {
                    registry.addInterceptor(noWrapInterceptor())
                            .addPathPatterns("/**");
                }
            }
        };
    }
}