# JianSpring RestClient Starter

基于 Spring 6 的 RestClient 的封装，提供了更加便捷的远程调用方式。

## 特性

- 基于 Spring 6 和 Spring Boot 3 设计
- 使用 Spring 6 的 RestClient
- 支持负载均衡
- 支持统一结果值封装和解封
- 支持自定义 header，包括 traceId
- 支持请求重试、限流和断路器（基于 Resilience4j）
- 支持请求日志记录
- 支持异常处理
- 支持基于 WebClient 的异步请求

## 使用方法

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.jianspring</groupId>
    <artifactId>jianspring-restclient-starter</artifactId>
    <version>${jianspring.version}</version>
</dependency>
```

### 4. 使用WebClient进行异步调用

除了使用RestClient进行同步调用外，还可以使用WebClient进行异步调用。

#### 4.1 定义接口

```java
@JianWebClient("http://user-service")
public interface UserAsyncService {
    
    @JianRequest(path = "/users/{id}", method = RequestMethod.GET)
    Mono<User> getUserById(@PathVariable("id") Long id);
    
    @JianRequest(path = "/users", method = RequestMethod.POST)
    Mono<User> createUser(@RequestBody User user);
}
```