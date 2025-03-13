package com.jianspring.starter.feign.decode;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jianspring.starter.commons.result.ApiResult;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

class BasicFeignResponseDecodeTest {

    @Test
    void strDecode() {
        String body = "Hello";
        Object decode = decode(body, getMethod("getString").getGenericReturnType());
        System.out.println("result:" + decode.getClass());
        System.out.println("result:" + decode);
    }

    @Test
    void objDecode() {
        String body = null;
        Object decode = decode(body, SimpleResponse.class);
        System.out.println("result:" + decode.getClass());
        System.out.println("result:" + decode);
    }

    @Test
    void bigDecimaldecodebasic() {
        String body = "1.00001";
        Object decode = decode(body, getMethod("getBigDecimal").getGenericReturnType());
        System.out.println("result:" + decode.getClass());
        System.out.println("result:" + (BigDecimal) decode);
    }

    @Test
    void datedecodebasic() {
        String body = "2023-11-23 11:00:00";
        Object decode = decode(body, getMethod("getDate").getGenericReturnType());
        System.out.println("result:" + decode.getClass());
        System.out.println("result:" + decode);
    }

    @Test
    void arraydecodebasic() {
        String body = "{\"a\",\"b\"}";
        Object decode = decode(body, getMethod("getArray").getGenericReturnType());
        System.out.println("result:" + decode.getClass());
        System.out.println("result:" + decode);
    }

    @Test
    void bigDecimaldecode() {
        String body = "{\"code\":200,\"data\":1}";
        Object decode = decode(body, getMethod("getBigDecimal").getGenericReturnType());
        System.out.println("result:" + decode.getClass());
        System.out.println("result:" + (BigDecimal) decode);
    }

    @Test
    void setDecode() {
        Set<String> testSet = new HashSet<>(Arrays.asList("a", "b"));
        ApiResult result = ApiResult.success(testSet);
        Object decode = decode(result, getMethod("getSet").getGenericReturnType());
        System.out.println("result:" + decode.getClass());
        System.out.println("result:" + JSONUtil.toJsonPrettyStr(decode));
    }

    @Test
    void listDecode() {
        List<String> listSet = null;
        ApiResult result = ApiResult.success(listSet);
        Object decode = decode("{\"code\":200,\"data\":null}", List.class);
        System.out.println("result:" + decode.getClass());
        System.out.println("result:" + JSONUtil.toJsonPrettyStr(decode));
    }

    @Test
    void longDecode() {
        ApiResult result = ApiResult.success(1L);
        Object decode = decode(result, Long.class);
        System.out.println("result:" + decode.getClass());
        System.out.println("result:" + decode);
    }

    @Test
    void simpleObjDecode() {
        SimpleResponse simpleResponse = new SimpleResponse();
        simpleResponse.setId(1L);
        simpleResponse.setName("abc");
        ApiResult<SimpleResponse> result = ApiResult.success(simpleResponse);

        Object decode = decode(result, SimpleResponse.class);
        System.out.println("result:" + decode.getClass());
        System.out.println("result:" + JSONUtil.toJsonPrettyStr(decode));
    }

    @Test
    void mapDecode() {
        Map<String, String> map = new HashMap<>();
        map.put("a", "100");
        map.put("b", "101");

        ApiResult<Map> result = ApiResult.success(map);
        Object decode = decode(result, Map.class);
        System.out.println("result:" + decode.getClass());
        System.out.println("result:" + JSONUtil.toJsonPrettyStr(decode));
    }

    @Test
    void map2Decode() {
        SimpleResponse simpleResponse = new SimpleResponse();
        simpleResponse.setId(1L);
        simpleResponse.setName("abc");

        Map<String, String> map = new HashMap<>();
        map.put("a", "100");
        map.put("b", "101");
        simpleResponse.setMap(map);

        JSONObject jsonObject = new JSONObject();
        jsonObject.set("key", "value");
        simpleResponse.setJsonObject(jsonObject);

        ApiResult<SimpleResponse> result = ApiResult.success(simpleResponse);

        Object decode = decode(result, SimpleResponse.class);
        System.out.println("result:" + decode.getClass());
        System.out.println("result:" + JSONUtil.toJsonPrettyStr(decode));
        SimpleResponse res = (SimpleResponse) decode;
        System.out.println("Type:" + res.getMap().getClass());
        System.out.println("map value:" + JSONUtil.toJsonPrettyStr(res.getMap()));

        System.out.println("Type:" + res.getJsonObject().getClass());
        System.out.println("json value:" + JSONUtil.toJsonPrettyStr(res.getJsonObject()));
    }

    @Test
    void listObjDecode() {
        SimpleResponse simpleResponse = new SimpleResponse();
        simpleResponse.setId(1L);
        simpleResponse.setName("abc");

        Map<String, String> map = new HashMap<>();
        map.put("a", "100");
        map.put("b", "101");
        simpleResponse.setMap(map);

        JSONObject jsonObject = new JSONObject();
        jsonObject.set("key", "value");
        simpleResponse.setJsonObject(jsonObject);

        List<SimpleResponse> responseList = new ArrayList<>();
        responseList.add(simpleResponse);
        responseList.add(simpleResponse);
        ApiResult<List<SimpleResponse>> result = ApiResult.success(responseList);

        Object decode = decode(result, getMethod("getObjList").getGenericReturnType());
        System.out.println("result:" + decode.getClass());
        System.out.println("result:" + JSONUtil.toJsonPrettyStr(decode));
        SimpleResponse simpleResponse1 = ((List<SimpleResponse>) decode).get(0);

        System.out.println("result:" + simpleResponse1.getClass());
        System.out.println("result:" + JSONUtil.toJsonPrettyStr(simpleResponse1));
    }

    @Test
    public void nullDecode() {
        ApiResult<SimpleResponse> result = ApiResult.success(null);
        Object decode = decode(result, SimpleResponse.class);
        System.out.println("result:" + decode);
    }

    private Object decode(Object body, Type type) {
        BasicFeignResponseDecode basicFeignResponseDecode = new BasicFeignResponseDecode();
        Response test1 = Response.builder()
                .body(JSONUtil.toJsonStr(body), StandardCharsets.UTF_8)
                .request(Request.create(Request.HttpMethod.GET, "/test", new HashMap<>(), Request.Body.create("test"), new RequestTemplate()))
                .build();

        return basicFeignResponseDecode.decode(test1, type);
    }

    @Data
    private class SimpleResponse {
        private Long id;
        private String name;
        private Map<String, String> map;
        private JSONObject jsonObject;
    }

    class Example {
        public List<String> getList() {
            return null;
        }

        public Map<String, Integer> getMap() {
            return null;
        }

        public String getString() {
            return null;
        }

        public Set<String> getSet() {
            return null;
        }

        public List<SimpleResponse> getObjList() {
            return null;
        }

        public BigDecimal getBigDecimal() {
            return null;
        }

        public Date getDate() {
            return null;
        }

        public Array getArray() {
            return null;
        }
    }

    private Method getMethod(String methodName) {
        try {
            return Example.class.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }
}