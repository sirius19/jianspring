# JianSpring Framework

基于 Spring Boot 3.2.4 的微服务开发框架，提供了一系列开箱即用的starter组件。

## 环境要求
- JDK 17+
- Spring Boot 3.2.4
- Spring Cloud 2023.0.1
- Spring Cloud Alibaba 2023.0.1.0

## 支持的功能

### 数据库模块 (jianspring-db-starter)
- 基于 MyBatis-Plus 的数据库操作封装
- 自动填充创建时间、更新时间、创建人、更新人
- 支持多租户和逻辑删除
- 自定义ID生成器

### 认证授权模块 (jianspring-iam-starter)
- JWT token 认证
- 权限注解支持
- 灵活的权限校验机制

### 分布式锁模块 (jianspring-lock-starter)
- 基于 Redisson 的分布式锁实现
- 支持可重入锁
- 支持读写锁

### 缓存模块 (jianspring-redis-starter)
- Redis 操作封装
- 批量操作支持
- 通用的缓存key管理

### 对象存储模块 (jianspring-oss-starter)
- 支持多种对象存储服务
- 统一的文件操作接口
- 支持文件上传下载

### 远程调用模块
- REST 客户端 (jianspring-restclient-starter)
  - 基于 Spring 6 RestClient
  - 支持服务发现
  - 内置重试、限流、熔断
- Feign 客户端 (jianspring-feign-starter)
  - 声明式 HTTP 客户端
  - 支持负载均衡

### 链路追踪模块 (jianspring-trace-starter)
- 分布式链路追踪
- 统一的日志格式
- TraceId 传递

### 流量控制模块 (jianspring-sentinel-starter)
- 基于 Sentinel 的流量控制
- 熔断降级
- 系统保护

### 日志模块 (jianspring-logs-starter)
- 统一的日志处理
- 操作日志记录
- 异常日志采集

### 工具模块 (jianspring-utils-starter)
- 通用工具类
- 常用功能封装
- 异常处理

## 快速开始

1. 添加依赖
```xml
<dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>com.jianspring</groupId>
                <artifactId>jian-spring</artifactId>
                <version>2.0.0-SNAPSHOT</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
</dependencyManagement>
```
其他模块按需添加
