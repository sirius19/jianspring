package com.jianspring.starter.cloud.interceptor;

import com.jianspring.starter.cloud.mapper.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 增加对Long进行String序列化
 *
 * @Author: InfoInsights
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    /**
     * 拓展mvc框架的消息转换器
     *
     * @param converters
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        //创建消息转换器对象
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        //设置具体的对象映射器
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //通过设置索引，让自己的转换器放在最前面，否则默认的jackson转换器会在前面，用不上自己配置的转换器
        converters.add(0, messageConverter);

        // ByteArray 转换器,Jackson 的 ObjectMapper 在序列化 byte[] 时，默认会将其编码为 Base64 字符串
        ByteArrayHttpMessageConverter byteArrayConverter = new ByteArrayHttpMessageConverter();
        byteArrayConverter.setSupportedMediaTypes(List.of(MediaType.APPLICATION_JSON));
        converters.add(0, byteArrayConverter);
    }
}
