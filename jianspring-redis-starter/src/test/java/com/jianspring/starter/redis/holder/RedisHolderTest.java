package com.jianspring.starter.redis.holder;

import com.jianspring.starter.redis.config.TestRedisConfig;
import com.jianspring.starter.redis.enums.IRedisKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestRedisConfig.class)
@Testcontainers
public class RedisHolderTest {
    
    static {
        // 设置Docker主机
        System.setProperty("testcontainers.docker.host", "tcp://localhost:2375");
        // 或者使用Docker Desktop默认的命名管道
        // System.setProperty("testcontainers.docker.socket", "npipe:////./pipe/docker_engine");
    }

    @Container
    public static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:6.2.6"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort);
    }

    @Autowired
    private RedisHolder redisHolder;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private TestRedisKey testKey;

    @BeforeEach
    void setUp() {
        testKey = new TestRedisKey();
        // 清空Redis数据库
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    // 测试用的RedisKey实现
    static class TestRedisKey implements IRedisKey {
        @Override
        public String getPrefixKey() {
            return "test:";
        }

        @Override
        public Object getDefaultValue() {
            return "default";
        }

        @Override
        public long getTtl() {
            return 60;
        }

        @Override
        public TimeUnit getTimeUnit() {
            return TimeUnit.SECONDS;
        }
    }

    // ================ 基本操作测试 ================

    @Test
    void testSetAndGet() {
        String bizKey = "basic";
        String value = "testValue";

        // 测试设置值
        redisHolder.set(testKey, bizKey, value);

        // 测试获取值
        Object result = redisHolder.get(testKey, bizKey);
        assertEquals(value, result);
    }

    @Test
    void testSetWithDefaultValue() {
        String bizKey = "default";

        // 测试设置默认值
        redisHolder.set(testKey, bizKey);

        // 验证默认值
        Object result = redisHolder.get(testKey, bizKey);
        assertEquals(testKey.getDefaultValue(), result);
    }

    @Test
    void testDelete() {
        String bizKey = "delete";
        redisHolder.set(testKey, bizKey, "value");

        // 验证键存在
        assertTrue(redisHolder.hasKey(testKey, bizKey));

        // 测试删除
        redisHolder.delete(testKey, bizKey);

        // 验证键已删除
        assertFalse(redisHolder.hasKey(testKey, bizKey));
    }

    @Test
    void testSetIfAbsent() {
        String bizKey = "setIfAbsent";
        String value1 = "value1";
        String value2 = "value2";

        // 键不存在时设置成功
        boolean result1 = redisHolder.setIfAbsent(testKey, bizKey, value1);
        assertTrue(result1);
        assertEquals(value1, redisHolder.get(testKey, bizKey));

        // 键存在时设置失败
        boolean result2 = redisHolder.setIfAbsent(testKey, bizKey, value2);
        assertFalse(result2);
        assertEquals(value1, redisHolder.get(testKey, bizKey));
    }

    // ================ Set类型操作测试 ================

    @Test
    void testSetOperations() {
        String bizKey = "set";
        String value1 = "value1";
        String value2 = "value2";

        // 测试添加单个元素
        redisHolder.setSet(testKey, bizKey, value1);

        // 测试添加多个元素
        redisHolder.setSet(testKey, bizKey, Arrays.asList(value1, value2));

        // 测试获取Set大小
        assertEquals(2, redisHolder.getSetSize(testKey, bizKey));

        // 测试获取所有元素
        Set<Object> values = redisHolder.getSetValues(testKey, bizKey);
        assertEquals(2, values.size());
        assertTrue(values.contains(value1));
        assertTrue(values.contains(value2));

        // 测试弹出元素
        Object poppedValue = redisHolder.popSet(testKey, bizKey);
        assertTrue(poppedValue.equals(value1) || poppedValue.equals(value2));
        assertEquals(1, redisHolder.getSetSize(testKey, bizKey));
    }

    // ================ Hash类型操作测试 ================

    @Test
    void testHashOperations() {
        String bizKey = "hash";
        String hashKey1 = "field1";
        String hashKey2 = "field2";
        String value1 = "value1";
        String value2 = "value2";

        // 测试设置Hash字段
        redisHolder.hashPut(testKey, bizKey, hashKey1, value1);
        redisHolder.hashPut(testKey, bizKey, hashKey2, value2);

        // 测试获取Hash字段
        assertEquals(value1, redisHolder.hashGet(testKey, bizKey, hashKey1));
        assertEquals(value2, redisHolder.hashGet(testKey, bizKey, hashKey2));

        // 测试获取所有Hash字段和值
        Map<Object, Object> entries = redisHolder.hashGetAll(testKey, bizKey);
        assertEquals(2, entries.size());
        assertEquals(value1, entries.get(hashKey1));
        assertEquals(value2, entries.get(hashKey2));

        // 测试删除Hash字段
        Long deletedCount = redisHolder.hashDelete(testKey, bizKey, hashKey1);
        assertEquals(1L, deletedCount);
        assertNull(redisHolder.hashGet(testKey, bizKey, hashKey1));
        assertEquals(value2, redisHolder.hashGet(testKey, bizKey, hashKey2));
    }

    // ================ List类型操作测试 ================

    @Test
    void testListOperations() {
        String bizKey = "list";
        String value1 = "value1";
        String value2 = "value2";
        String value3 = "value3";

        // 测试左侧添加元素
        redisHolder.listLeftPush(testKey, bizKey, value1);
        redisHolder.listLeftPush(testKey, bizKey, value2);

        // 测试右侧添加元素
        redisHolder.listRightPush(testKey, bizKey, value3);

        // 测试获取范围元素
        List<Object> range = redisHolder.listRange(testKey, bizKey, 0, -1);
        assertEquals(3, range.size());
        assertEquals(value2, range.get(0));
        assertEquals(value1, range.get(1));
        assertEquals(value3, range.get(2));

        // 测试左侧弹出元素
        Object leftPop = redisHolder.listLeftPop(testKey, bizKey);
        assertEquals(value2, leftPop);

        // 测试右侧弹出元素
        Object rightPop = redisHolder.listRightPop(testKey, bizKey);
        assertEquals(value3, rightPop);

        // 验证剩余元素
        List<Object> remaining = redisHolder.listRange(testKey, bizKey, 0, -1);
        assertEquals(1, remaining.size());
        assertEquals(value1, remaining.get(0));
    }

    // ================ ZSet类型操作测试 ================

    @Test
    void testZSetOperations() {
        String bizKey = "zset";
        String value1 = "value1";
        String value2 = "value2";
        String value3 = "value3";

        // 测试添加元素
        redisHolder.zSetAdd(testKey, bizKey, value1, 1.0);
        redisHolder.zSetAdd(testKey, bizKey, value2, 2.0);
        redisHolder.zSetAdd(testKey, bizKey, value3, 3.0);

        // 测试获取分数
        assertEquals(1.0, redisHolder.zSetScore(testKey, bizKey, value1));
        assertEquals(2.0, redisHolder.zSetScore(testKey, bizKey, value2));
        assertEquals(3.0, redisHolder.zSetScore(testKey, bizKey, value3));

        // 测试按分数范围获取元素
        Set<Object> rangeByScore = redisHolder.zSetRangeByScore(testKey, bizKey, 1.5, 3.0);
        assertEquals(2, rangeByScore.size());
        assertTrue(rangeByScore.contains(value2));
        assertTrue(rangeByScore.contains(value3));

        // 测试按索引范围获取元素
        Set<Object> range = redisHolder.zSetRange(testKey, bizKey, 0, 1);
        assertEquals(2, range.size());
        assertTrue(range.contains(value1));
        assertTrue(range.contains(value2));

        // 测试删除元素
        Long removedCount = redisHolder.zSetRemove(testKey, bizKey, value1, value2);
        assertEquals(2L, removedCount);

        // 验证剩余元素
        Set<Object> remaining = redisHolder.zSetRange(testKey, bizKey, 0, -1);
        assertEquals(1, remaining.size());
        assertTrue(remaining.contains(value3));
    }

    // ================ Geo类型操作测试 ================

    @Test
    void testGeoOperations() {
        String bizKey = "geo";
        String member1 = "Beijing";
        String member2 = "Shanghai";
        Point point1 = new Point(116.397128, 39.916527);
        Point point2 = new Point(121.473701, 31.230416);

        // 测试添加地理位置
        redisHolder.geoAdd(testKey, bizKey, point1, member1);
        redisHolder.geoAdd(testKey, bizKey, point2, member2);

        // 测试获取地理位置
        List<Point> positions = redisHolder.geoPos(testKey, bizKey, member1, member2);
        assertEquals(2, positions.size());

        // 测试查询半径内的成员
        Circle circle = new Circle(point1, new Distance(1500, RedisGeoCommands.DistanceUnit.KILOMETERS));
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeDistance().sortAscending();
        GeoResults<RedisGeoCommands.GeoLocation<Object>> results = redisHolder.geoRadius(testKey, bizKey, circle, args);

        // 应该包含两个城市
        assertEquals(2, results.getContent().size());

        // 测试删除地理位置
        Long removedCount = redisHolder.geoRemove(testKey, bizKey, member1);
        assertEquals(1L, removedCount);

        // 验证剩余地理位置
        positions = redisHolder.geoPos(testKey, bizKey, member1, member2);
        assertNull(positions.get(0)); // 已删除
        assertNotNull(positions.get(1)); // 仍存在
    }

    // ================ HyperLogLog类型操作测试 ================

    @Test
    void testHyperLogLogOperations() {
        String bizKey = "hll";
        String value1 = "value1";
        String value2 = "value2";
        String value3 = "value3";

        // 测试添加元素
        redisHolder.hyperLogLogAdd(testKey, bizKey, value1, value2);
        assertEquals(2L, redisHolder.hyperLogLogSize(testKey, bizKey));

        // 测试添加重复元素
        redisHolder.hyperLogLogAdd(testKey, bizKey, value2, value3);
        assertEquals(3L, redisHolder.hyperLogLogSize(testKey, bizKey));
    }

    // ================ Bitmap类型操作测试 ================

    @Test
    void testBitmapOperations() {
        String bizKey = "bitmap";

        // 测试设置位
        redisHolder.bitMapSetBit(testKey, bizKey, 0, true);
        redisHolder.bitMapSetBit(testKey, bizKey, 1, false);
        redisHolder.bitMapSetBit(testKey, bizKey, 2, true);

        // 测试获取位
        assertTrue(redisHolder.bitMapGetBit(testKey, bizKey, 0));
        assertFalse(redisHolder.bitMapGetBit(testKey, bizKey, 1));
        assertTrue(redisHolder.bitMapGetBit(testKey, bizKey, 2));

        // 测试计数
        assertEquals(2L, redisHolder.bitMapCount(testKey, bizKey));
    }

    // ================ 通用操作测试 ================

    @Test
    void testExecuteWithoutReturn() {
        String bizKey = "executeNoReturn";
        String value = "testValue";

        // 测试执行无返回值的命令
        redisHolder.executeCommand(testKey, bizKey, key -> {
            redisTemplate.opsForValue().set(key, value);
            return null;
        });

        // 验证命令执行结果
        Object result = redisTemplate.opsForValue().get(testKey.getPrefixKey() + bizKey);
        assertEquals(value, result);
    }

    @Test
    void testExecuteCallback() {
        String value = "testValue";
        String key = "callback";

        // 测试执行回调
        String result = redisHolder.execute(template -> {
            template.opsForValue().set(key, value);
            return (String) template.opsForValue().get(key);
        });

        assertEquals(value, result);
    }

    @Test
    void testExecuteBatch() {
        String key1 = "batch1";
        String key2 = "batch2";
        String value1 = "value1";
        String value2 = "value2";

        // 测试批量执行
        List<Consumer<RedisTemplate<String, Object>>> operations = new ArrayList<>();
        operations.add(template -> template.opsForValue().set(key1, value1));
        operations.add(template -> template.opsForValue().set(key2, value2));

        redisHolder.executeBatch(operations);

        // 验证批量执行结果
        assertEquals(value1, redisTemplate.opsForValue().get(key1));
        assertEquals(value2, redisTemplate.opsForValue().get(key2));
    }

    @Test
    void testBuildKey() {
        String bizKey = "buildKey";
        String expectedKey = testKey.getPrefixKey() + bizKey;

        // 通过反射调用私有方法
        String key = null;
        try {
            java.lang.reflect.Method buildKeyMethod = RedisHolder.class.getDeclaredMethod("buildKey", IRedisKey.class, String.class);
            buildKeyMethod.setAccessible(true);
            key = (String) buildKeyMethod.invoke(redisHolder, testKey, bizKey);
        } catch (Exception e) {
            fail("Failed to invoke buildKey method: " + e.getMessage());
        }

        assertEquals(expectedKey, key);
    }

    @Test
    void testApplyTtl() {
        String bizKey = "applyTtl";
        String key = testKey.getPrefixKey() + bizKey;

        // 设置值
        redisTemplate.opsForValue().set(key, "value");

        // 通过反射调用私有方法
        try {
            java.lang.reflect.Method applyTtlMethod = RedisHolder.class.getDeclaredMethod("applyTtl", IRedisKey.class, String.class);
            applyTtlMethod.setAccessible(true);
            applyTtlMethod.invoke(redisHolder, testKey, key);
        } catch (Exception e) {
            fail("Failed to invoke applyTtl method: " + e.getMessage());
        }

        // 验证TTL已应用
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        assertNotNull(ttl);
        assertTrue(ttl <= testKey.getTtl() && ttl > 0);
    }

    @Test
    void testGetRedisTemplate() {
        // 测试获取RedisTemplate
        RedisTemplate<String, Object> template = redisHolder.getRedisTemplate();
        assertNotNull(template);
        assertSame(redisTemplate, template);
    }

}