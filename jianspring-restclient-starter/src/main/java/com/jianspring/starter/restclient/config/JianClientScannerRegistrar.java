package com.jianspring.starter.restclient.config;

import com.jianspring.starter.restclient.annotation.EnableJianClients;
import com.jianspring.starter.restclient.annotation.JianClient;
import com.jianspring.starter.restclient.annotation.JianReactiveClient;
import com.jianspring.starter.restclient.factory.JianClientFactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class JianClientScannerRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        // 获取 @EnableJianClients 注解的属性
        Map<String, Object> attributes = metadata.getAnnotationAttributes(EnableJianClients.class.getName());
        if (attributes == null) {
            return;
        }

        // 获取要扫描的包
        Set<String> basePackages = getBasePackages(attributes, metadata);
        registerJianClients(basePackages, registry);
    }

    private void registerJianClients(Set<String> basePackages, BeanDefinitionRegistry registry) {
        ClassPathScanningCandidateComponentProvider scanner = createScanner();

        for (String basePackage : basePackages) {
            scanner.findCandidateComponents(basePackage).forEach(beanDefinition ->
                    registerJianClient(registry, beanDefinition));
        }
    }

    // 在createScanner方法中修改
    private ClassPathScanningCandidateComponentProvider createScanner() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false) {
                    @Override
                    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                        return beanDefinition.getMetadata().isInterface() && 
                               (beanDefinition.getMetadata().hasAnnotation(JianClient.class.getName()) ||
                                beanDefinition.getMetadata().hasAnnotation(JianReactiveClient.class.getName()));
                    }
                };
        scanner.addIncludeFilter(new AnnotationTypeFilter(JianClient.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(JianReactiveClient.class)); // 添加WebClient注解过滤器
        return scanner;
    }

    private void registerJianClient(BeanDefinitionRegistry registry, BeanDefinition beanDefinition) {
        String beanClassName = beanDefinition.getBeanClassName();
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition(JianClientFactoryBean.class)
                .addPropertyValue("clientClass", beanClassName)
                .setAutowireMode(2);

        String beanName = StringUtils.uncapitalize(ClassUtils.getShortName(beanClassName));
        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
    }

    private Set<String> getBasePackages(Map<String, Object> attributes, AnnotationMetadata metadata) {
        Set<String> basePackages = new HashSet<>();

        // 添加 value() 属性指定的包
        addStringsToSet(basePackages, (String[]) attributes.get("value"));

        // 添加 basePackages() 属性指定的包
        addStringsToSet(basePackages, (String[]) attributes.get("basePackages"));

        // 添加 basePackageClasses() 属性指定的类所在的包
        for (Class<?> clazz : (Class<?>[]) attributes.get("basePackageClasses")) {
            basePackages.add(ClassUtils.getPackageName(clazz));
        }

        // 如果没有指定包，则使用启动类所在的包
        if (basePackages.isEmpty()) {
            basePackages.add(ClassUtils.getPackageName(metadata.getClassName()));
        }

        return basePackages;
    }

    private void addStringsToSet(Set<String> set, String[] strings) {
        if (strings != null) {
            for (String string : strings) {
                if (StringUtils.hasText(string)) {
                    set.add(string.trim());
                }
            }
        }
    }
}