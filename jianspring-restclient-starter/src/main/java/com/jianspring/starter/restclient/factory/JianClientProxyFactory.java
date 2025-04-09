package com.jianspring.starter.restclient.factory;

import com.jianspring.starter.restclient.annotation.JianClient;
import com.jianspring.starter.restclient.annotation.JianReactiveClient;
import com.jianspring.starter.restclient.annotation.JianRequest;
import com.jianspring.starter.restclient.service.JianRestClient;
import com.jianspring.starter.restclient.service.JianWebClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

// 添加WebClient支持
public class JianClientProxyFactory {
    private final JianRestClient restClient;
    private final JianWebClient webClient;

    public JianClientProxyFactory(JianRestClient restClient, JianWebClient webClient) {
        this.restClient = restClient;
        this.webClient = webClient;
    }

    @SuppressWarnings("unchecked")
    public <T> T createClient(Class<T> clientClass) {
        // 根据注解类型选择不同的处理器
        if (clientClass.isAnnotationPresent(JianReactiveClient.class)) {
            return (T) Proxy.newProxyInstance(
                    clientClass.getClassLoader(),
                    new Class[]{clientClass},
                    new JianReactiveClientInvocationHandler(clientClass, webClient)
            );
        }
        return (T) Proxy.newProxyInstance(
                clientClass.getClassLoader(),
                new Class[]{clientClass},
                new JianClientInvocationHandler(clientClass, restClient)
        );
    }

    private static class JianClientInvocationHandler implements InvocationHandler {
        private final String baseUrl;
        private final String serviceName;
        private final JianRestClient jianRestClient;

        public JianClientInvocationHandler(Class<?> clientClass, JianRestClient jianRestClient) {
            JianClient annotation = clientClass.getAnnotation(JianClient.class);
            this.baseUrl = annotation != null ? annotation.url() : "";
            this.serviceName = annotation != null ? annotation.name() : "";
            this.jianRestClient = jianRestClient;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 如果是 Object 类的方法，直接调用
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }

            JianRequest annotation = method.getAnnotation(JianRequest.class);
            if (annotation == null) {
                throw new IllegalStateException("Method " + method.getName() + " must be annotated with @JianRequest");
            }

            String path = annotation.value().length > 0 ? annotation.value()[0] : "";
            String url;
            if (!baseUrl.isEmpty()) {
                url = baseUrl + path;
            } else if (!serviceName.isEmpty()) {
                url = "http://" + serviceName + path;
            } else {
                throw new IllegalStateException("Neither url nor name is specified in @JianClient");
            }

            // 处理路径参数
            Map<String, Object> pathVariables = extractPathVariables(method, args);

            // 根据 HTTP 方法类型调用对应的方法
            RequestMethod requestMethod = annotation.method();
            switch (requestMethod) {
                case GET:
                    if (!pathVariables.isEmpty()) {
                        return jianRestClient.get(url, pathVariables, method.getReturnType());
                    }
                    return jianRestClient.get(url, method.getReturnType());
                case POST:
                    Object requestBody = extractRequestBody(method, args);
                    return jianRestClient.post(url, requestBody, method.getReturnType());
                case PUT:
                    Object putBody = extractRequestBody(method, args);
                    return jianRestClient.put(url, putBody, method.getReturnType());
                case DELETE:
                    if (!pathVariables.isEmpty()) {
                        return jianRestClient.delete(url, pathVariables, method.getReturnType());
                    }
                    return jianRestClient.delete(url, method.getReturnType());
                default:
                    throw new UnsupportedOperationException("Unsupported HTTP method: " + requestMethod);
            }
        }

        /**
         * 提取请求体参数
         */
        private Object extractRequestBody(Method method, Object[] args) {
            if (args == null || args.length == 0) {
                return null;
            }

            Parameter[] parameters = method.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].isAnnotationPresent(RequestBody.class) ||
                        !parameters[i].isAnnotationPresent(PathVariable.class) &&
                                !parameters[i].isAnnotationPresent(RequestParam.class)) {
                    return args[i];
                }
            }

            // 如果没有找到 @RequestBody 注解，默认使用第一个参数
            return args[0];
        }

        /**
         * 提取路径变量
         */
        private Map<String, Object> extractPathVariables(Method method, Object[] args) {
            Map<String, Object> pathVariables = new HashMap<>();
            if (args == null || args.length == 0) {
                return pathVariables;
            }

            Parameter[] parameters = method.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                PathVariable pathVariable = parameters[i].getAnnotation(PathVariable.class);
                if (pathVariable != null) {
                    String name = pathVariable.value();
                    if (name.isEmpty()) {
                        name = parameters[i].getName();
                    }
                    pathVariables.put(name, args[i]);
                }
            }

            return pathVariables;
        }
    }

    private static class JianReactiveClientInvocationHandler implements InvocationHandler {
        private final String baseUrl;
        private final String serviceName;
        private final JianWebClient webClient;

        public JianReactiveClientInvocationHandler(Class<?> clientClass, JianWebClient webClient) {
            JianReactiveClient annotation = clientClass.getAnnotation(JianReactiveClient.class);
            this.baseUrl = annotation != null ? annotation.url() : "";
            this.serviceName = annotation != null ? annotation.name() : "";
            this.webClient = webClient;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }

            JianRequest annotation = method.getAnnotation(JianRequest.class);
            if (annotation == null) {
                throw new IllegalStateException("Method " + method.getName() + " must be annotated with @JianRequest");
            }

            String path = annotation.value().length > 0 ? annotation.value()[0] : "";
            String url;
            if (!baseUrl.isEmpty()) {
                url = baseUrl + path;
            } else if (!serviceName.isEmpty()) {
                url = "http://" + serviceName + path;
            } else {
                throw new IllegalStateException("Neither url nor name is specified in @JianReactiveClient");
            }

            // 处理路径参数
            Map<String, Object> pathVariables = extractPathVariables(method, args);
            if (!pathVariables.isEmpty()) {
                for (Map.Entry<String, Object> entry : pathVariables.entrySet()) {
                    url = url.replace("{" + entry.getKey() + "}", entry.getValue().toString());
                }
            }

            // 根据 HTTP 方法类型调用对应的异步方法
            RequestMethod requestMethod = annotation.method();
            Object requestBody = extractRequestBody(method, args);

            switch (requestMethod) {
                case GET:
                    return webClient.get(url, method.getReturnType());
                case POST:
                    return webClient.post(url, requestBody, method.getReturnType());
                case PUT:
                    return webClient.put(url, requestBody, method.getReturnType());
                case DELETE:
                    return webClient.delete(url, method.getReturnType());
                default:
                    throw new UnsupportedOperationException("Unsupported HTTP method: " + requestMethod);
            }
        }

        /**
         * 提取请求体参数
         */
        private Object extractRequestBody(Method method, Object[] args) {
            if (args == null || args.length == 0) {
                return null;
            }

            Parameter[] parameters = method.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].isAnnotationPresent(RequestBody.class) ||
                        !parameters[i].isAnnotationPresent(PathVariable.class) &&
                                !parameters[i].isAnnotationPresent(RequestParam.class)) {
                    return args[i];
                }
            }

            // 如果没有找到 @RequestBody 注解，默认使用第一个参数
            return args[0];
        }

        /**
         * 提取路径变量
         */
        private Map<String, Object> extractPathVariables(Method method, Object[] args) {
            Map<String, Object> pathVariables = new HashMap<>();
            if (args == null || args.length == 0) {
                return pathVariables;
            }

            Parameter[] parameters = method.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                PathVariable pathVariable = parameters[i].getAnnotation(PathVariable.class);
                if (pathVariable != null) {
                    String name = pathVariable.value();
                    if (name.isEmpty()) {
                        name = parameters[i].getName();
                    }
                    pathVariables.put(name, args[i]);
                }
            }

            return pathVariables;
        }
    }
}