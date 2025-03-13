package com.jianspring.starter.cloud.mapper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;


import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

public class JacksonObjectMapper extends ObjectMapper {

    public JacksonObjectMapper() {
        super();
        //收到未知属性时不报异常
        this.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);

        //反序列化时，属性不存在的兼容处理
        this.getDeserializationConfig().withoutFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        SimpleModule simpleModule = new SimpleModule()
                //处理Long型数据
                .addSerializer(Long.class, ToStringSerializer.instance)
                //修改 JacksonObjectMapper，为 byte[] 添加自定义序列化器，避免 Base64
                .addSerializer(byte[].class, new JsonSerializer<byte[]>() {
                    @Override
                    public void serialize(byte[] value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                        gen.writeRawValue(new String(value, StandardCharsets.UTF_8));
                    }
                });
        //注册功能模块 例如，可以添加自定义序列化器和反序列化器
        this.registerModule(simpleModule);
    }

}
