package com.jianspring.starter.iam;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jianspring.starter.iam.service.PermissionChecker;
import com.jianspring.starter.iam.service.PermissionService;
import com.jianspring.starter.iam.service.impl.PermissionServiceImpl;

@Configuration
@ConditionalOnClass(AuthenticationInterceptor.class)
@EnableConfigurationProperties(IamJwtProperties.class)
@ConditionalOnProperty(prefix = "iam", name = "enabled", havingValue = "true", matchIfMissing = true)
public class IamAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public IamJwtService iamJwtService(IamJwtProperties iamJwtProperties) {
        return new IamJwtService(iamJwtProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public PermissionService permissionService() {
        return new PermissionServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public PermissionChecker permissionChecker(PermissionService permissionService) {
        return new PermissionChecker(permissionService);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthenticationInterceptor authenticationInterceptor(
            IamJwtService iamJwtService,
            IamJwtProperties iamJwtProperties,
            PermissionChecker permissionChecker) {
        return new AuthenticationInterceptor(iamJwtService, iamJwtProperties, permissionChecker);
    }
}
