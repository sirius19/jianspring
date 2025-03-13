package com.jianspring.starter.redis.holder;

import com.jianspring.starter.redis.enums.IRedisKey;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @Author: InfoInsights
 * @Date: 2023/3/3 下午1:56
 * @Version: 1.0.0
 */
public class RedisHolder {


    private final RedisTemplate<String, Object> redisTemplate;


    public RedisHolder(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取RedisTemplate实例
     *
     * @return RedisTemplate实例
     */
    public RedisTemplate<String, Object> getRedisTemplate() {
        return this.redisTemplate;
    }

    /**
     * 设置key
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     */
    public void set(IRedisKey iRedisKey, String bizKey) {
        String key = buildKey(iRedisKey, bizKey);
        redisTemplate.opsForValue().set(key, iRedisKey.getDefaultValue(), iRedisKey.getTtl(), iRedisKey.getTimeUnit());
    }

    /**
     * 设置key
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     * @param value     业务value
     */
    public void set(IRedisKey iRedisKey, String bizKey, Object value) {
        String key = buildKey(iRedisKey, bizKey);
        redisTemplate.opsForValue().set(key, null == value ? iRedisKey.getDefaultValue() : value, iRedisKey.getTtl(), iRedisKey.getTimeUnit());
    }

    public Object get(IRedisKey iRedisKey, String bizKey) {
        String key = buildKey(iRedisKey, bizKey);
        applyTtl(iRedisKey, key);
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 设置key
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     */
    public void delete(IRedisKey iRedisKey, String bizKey) {
        String key = buildKey(iRedisKey, bizKey);
        redisTemplate.delete(key);
    }

    /**
     * key 不存在时,则设置值
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     * @param value     业务value
     */
    public boolean setIfPresent(IRedisKey iRedisKey, String bizKey, Object value) {
        String key = buildKey(iRedisKey, bizKey);
        Boolean flag = redisTemplate.opsForValue().setIfPresent(key, null == value ? iRedisKey.getDefaultValue() : value, iRedisKey.getTtl(), iRedisKey.getTimeUnit());
        return null == flag ? Boolean.FALSE : flag;
    }

    /**
     * key 存在时，则设置值
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     * @param value     业务value
     */
    public boolean setIfAbsent(IRedisKey iRedisKey, String bizKey, Object value) {
        Boolean flag = redisTemplate.opsForValue().setIfAbsent(iRedisKey.getPrefixKey() + bizKey, null == value ? iRedisKey.getDefaultValue() : value, iRedisKey.getTtl(), iRedisKey.getTimeUnit());
        return null == flag ? Boolean.FALSE : flag;
    }


    /**
     * 设置Set 类型
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     * @param values    list 值
     */
    public void setSet(IRedisKey iRedisKey, String bizKey, List<Object> values) {
        redisTemplate.opsForSet().add(iRedisKey.getPrefixKey() + bizKey, values);
        redisTemplate.expire(iRedisKey.getPrefixKey() + bizKey, iRedisKey.getTtl(), iRedisKey.getTimeUnit());
    }

    /**
     * 设置Set 类型
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     * @param value     list 值
     */
    public void setSet(IRedisKey iRedisKey, String bizKey, Object value) {
        redisTemplate.opsForSet().add(iRedisKey.getPrefixKey() + bizKey, value);
        redisTemplate.expire(iRedisKey.getPrefixKey() + bizKey, iRedisKey.getTtl(), iRedisKey.getTimeUnit());
    }

    /**
     * 删除set元素
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     */
    public Object popSet(IRedisKey iRedisKey, String bizKey) {
        Object value = redisTemplate.opsForSet().pop(iRedisKey.getPrefixKey() + bizKey);
        redisTemplate.expire(iRedisKey.getPrefixKey() + bizKey, iRedisKey.getTtl(), iRedisKey.getTimeUnit());
        return value;
    }

    /**
     * 删除set元素
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     */
    public void removeSet(IRedisKey iRedisKey, String bizKey, String obj) {
        redisTemplate.opsForSet().remove(iRedisKey + bizKey, obj);
        redisTemplate.expire(iRedisKey.getPrefixKey() + bizKey, iRedisKey.getTtl(), iRedisKey.getTimeUnit());
    }

    /**
     * 获取set key对应的size
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     * @return set 的size
     */
    public long getSetSize(IRedisKey iRedisKey, String bizKey) {
        Long size = redisTemplate.opsForSet().size(iRedisKey.getPrefixKey() + bizKey);
        if (null == size) {
            return 0L;
        }
        return size;
    }

    /**
     * 获取所有set的值
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     * @return set的值
     */
    public Set<Object> getSetValues(IRedisKey iRedisKey, String bizKey) {
        Set<Object> members = redisTemplate.opsForSet().members(iRedisKey.getPrefixKey() + bizKey);
        if (null == members || members.isEmpty()) {
            return Collections.emptySet();
        }
        return members;
    }

    /**
     * 是否含有key
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     * @return
     */
    public boolean hasKey(IRedisKey iRedisKey, String bizKey) {
        return redisTemplate.hasKey(iRedisKey.getPrefixKey() + bizKey);
    }

    /**
     * geo添加
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     * @param point     经纬度信息
     * @param member    成员
     * @return {@link Long}
     */
    @Nullable
    public Long geoAdd(IRedisKey iRedisKey, String bizKey, Point point, String member) {
        long result = redisTemplate.opsForGeo().add(iRedisKey.getPrefixKey() + bizKey, point, member);
        if (iRedisKey.getTtl() > 0) {
            redisTemplate.expire(iRedisKey.getPrefixKey() + bizKey, iRedisKey.getTtl(), iRedisKey.getTimeUnit());
        }
        return result;
    }

    /**
     * 查询半径内成员
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     * @param within    范围
     * @param args      参数
     * @return {@link GeoResults}<{@link RedisGeoCommands.GeoLocation}<{@link Object}>>
     */
    public GeoResults<RedisGeoCommands.GeoLocation<Object>> geoRadius(IRedisKey iRedisKey, String bizKey, Circle within, RedisGeoCommands.GeoRadiusCommandArgs args) {
        return redisTemplate.opsForGeo().radius(iRedisKey.getPrefixKey() + bizKey, within, args);
    }

    /**
     * 删除
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     * @param member    成员
     * @return {@link Long}
     */
    @Nullable
    public Long geoRemove(IRedisKey iRedisKey, String bizKey, String member) {
        return redisTemplate.opsForGeo().remove(iRedisKey.getPrefixKey() + bizKey, member);
    }

    /**
     * 返回经纬度，可以一次获取多个
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务Key
     * @param member    成员
     * @return 返回值 getX() getY()
     */
    public List<Point> geoPos(IRedisKey iRedisKey, String bizKey, String... member) {
        return redisTemplate.opsForGeo().position(iRedisKey.getPrefixKey() + bizKey, member);
    }

    // 新增键构建方法（减少重复代码）
    private String buildKey(IRedisKey redisKey, String bizKey) {
        return redisKey.getPrefixKey() + bizKey;
    }

    // 新增TTL设置方法（集中过期策略）
    private void applyTtl(IRedisKey redisKey, String fullKey) {
        if (redisKey.getTtl() > 0) {
            redisTemplate.expire(fullKey, redisKey.getTtl(), redisKey.getTimeUnit());
        }
    }

    /**
     * 通用Redis操作方法 - 带返回值
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     * @param operation 具体操作函数
     * @param <T>       返回值类型
     * @return 操作结果
     */
    public <T> T executeCommand(IRedisKey iRedisKey, String bizKey, Function<String, T> operation) {
        String key = buildKey(iRedisKey, bizKey);
        try {
            T result = operation.apply(key);
            applyTtl(iRedisKey, key);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Redis操作失败: " + e.getMessage(), e);
        }
    }

    /**
     * 通用Redis操作方法 - 无返回值
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     * @param operation 具体操作函数
     */
    public void executeVoidCommand(IRedisKey iRedisKey, String bizKey, Consumer<String> operation) {
        String key = buildKey(iRedisKey, bizKey);
        try {
            operation.accept(key);
            applyTtl(iRedisKey, key);
        } catch (Exception e) {
            throw new RuntimeException("Redis操作失败: " + e.getMessage(), e);
        }
    }

    // ================ Hash类型操作 ================

    /**
     * 设置Hash字段值
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     * @param hashKey   hash字段
     * @param value     值
     */
    public void hashPut(IRedisKey iRedisKey, String bizKey, Object hashKey, Object value) {
        executeVoidCommand(iRedisKey, bizKey, key ->
                redisTemplate.opsForHash().put(key, hashKey, value));
    }

    /**
     * 获取Hash字段值
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     * @param hashKey   hash字段
     * @return 字段值
     */
    public Object hashGet(IRedisKey iRedisKey, String bizKey, Object hashKey) {
        return executeCommand(iRedisKey, bizKey, key ->
                redisTemplate.opsForHash().get(key, hashKey));
    }

    /**
     * 获取Hash所有字段和值
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     * @return 所有字段和值的映射
     */
    public Map<Object, Object> hashGetAll(IRedisKey iRedisKey, String bizKey) {
        return executeCommand(iRedisKey, bizKey, key ->
                redisTemplate.opsForHash().entries(key));
    }

    /**
     * 删除Hash字段
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     * @param hashKeys  hash字段列表
     * @return 删除的字段数量
     */
    public Long hashDelete(IRedisKey iRedisKey, String bizKey, Object... hashKeys) {
        return executeCommand(iRedisKey, bizKey, key ->
                redisTemplate.opsForHash().delete(key, hashKeys));
    }

    // ================ List类型操作 ================

    /**
     * 向List左侧添加元素
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     * @param value     值
     * @return 添加后的List长度
     */
    public Long listLeftPush(IRedisKey iRedisKey, String bizKey, Object value) {
        return executeCommand(iRedisKey, bizKey, key ->
                redisTemplate.opsForList().leftPush(key, value));
    }

    /**
     * 向List右侧添加元素
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     * @param value     值
     * @return 添加后的List长度
     */
    public Long listRightPush(IRedisKey iRedisKey, String bizKey, Object value) {
        return executeCommand(iRedisKey, bizKey, key ->
                redisTemplate.opsForList().rightPush(key, value));
    }

    /**
     * 获取List指定范围的元素
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     * @param start     开始索引
     * @param end       结束索引
     * @return 元素列表
     */
    public List<Object> listRange(IRedisKey iRedisKey, String bizKey, long start, long end) {
        return executeCommand(iRedisKey, bizKey, key ->
                redisTemplate.opsForList().range(key, start, end));
    }

    /**
     * 从List左侧弹出元素
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     * @return 弹出的元素
     */
    public Object listLeftPop(IRedisKey iRedisKey, String bizKey) {
        return executeCommand(iRedisKey, bizKey, key ->
                redisTemplate.opsForList().leftPop(key));
    }

    /**
     * 从List右侧弹出元素
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     * @return 弹出的元素
     */
    public Object listRightPop(IRedisKey iRedisKey, String bizKey) {
        return executeCommand(iRedisKey, bizKey, key ->
                redisTemplate.opsForList().rightPop(key));
    }

    // ================ ZSet类型操作 ================

    /**
     * 向ZSet添加元素
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     * @param value     值
     * @param score     分数
     * @return 添加成功的数量
     */
    public Boolean zSetAdd(IRedisKey iRedisKey, String bizKey, Object value, double score) {
        return executeCommand(iRedisKey, bizKey, key ->
                redisTemplate.opsForZSet().add(key, value, score));
    }

    /**
     * 获取ZSet指定分数范围的元素
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     * @param min       最小分数
     * @param max       最大分数
     * @return 元素集合
     */
    public Set<Object> zSetRangeByScore(IRedisKey iRedisKey, String bizKey, double min, double max) {
        return executeCommand(iRedisKey, bizKey, key ->
                redisTemplate.opsForZSet().rangeByScore(key, min, max));
    }

    /**
     * 获取ZSet指定索引范围的元素
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     * @param start     开始索引
     * @param end       结束索引
     * @return 元素集合
     */
    public Set<Object> zSetRange(IRedisKey iRedisKey, String bizKey, long start, long end) {
        return executeCommand(iRedisKey, bizKey, key ->
                redisTemplate.opsForZSet().range(key, start, end));
    }

    /**
     * 获取ZSet元素的分数
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     * @param value     元素
     * @return 分数
     */
    public Double zSetScore(IRedisKey iRedisKey, String bizKey, Object value) {
        return executeCommand(iRedisKey, bizKey, key ->
                redisTemplate.opsForZSet().score(key, value));
    }

    /**
     * 删除ZSet元素
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     * @param values    元素列表
     * @return 删除的元素数量
     */
    public Long zSetRemove(IRedisKey iRedisKey, String bizKey, Object... values) {
        return executeCommand(iRedisKey, bizKey, key ->
                redisTemplate.opsForZSet().remove(key, values));
    }

    // ================ HyperLogLog类型操作 ================

    /**
     * 添加HyperLogLog元素
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     * @param values    元素列表
     * @return 添加后影响的键数量
     */
    public Long hyperLogLogAdd(IRedisKey iRedisKey, String bizKey, Object... values) {
        return executeCommand(iRedisKey, bizKey, key ->
                redisTemplate.opsForHyperLogLog().add(key, values));
    }

    /**
     * 获取HyperLogLog基数估计值
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     * @return 基数估计值
     */
    public Long hyperLogLogSize(IRedisKey iRedisKey, String bizKey) {
        return executeCommand(iRedisKey, bizKey, key ->
                redisTemplate.opsForHyperLogLog().size(key));
    }

    // ================ Bitmap类型操作 ================

    /**
     * 设置Bitmap指定位置的值
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     * @param offset    偏移量
     * @param value     值
     * @return 原来的位值
     */
    public Boolean bitMapSetBit(IRedisKey iRedisKey, String bizKey, long offset, boolean value) {
        return executeCommand(iRedisKey, bizKey, key ->
                redisTemplate.opsForValue().setBit(key, offset, value));
    }

    /**
     * 获取Bitmap指定位置的值
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     * @param offset    偏移量
     * @return 位值
     */
    public Boolean bitMapGetBit(IRedisKey iRedisKey, String bizKey, long offset) {
        return executeCommand(iRedisKey, bizKey, key ->
                redisTemplate.opsForValue().getBit(key, offset));
    }

    /**
     * 获取Bitmap中值为1的位数
     *
     * @param iRedisKey key前缀
     * @param bizKey    业务key
     * @return 值为1的位数
     */
    public Long bitMapCount(IRedisKey iRedisKey, String bizKey) {
        return executeCommand(iRedisKey, bizKey, key ->
                redisTemplate.execute((RedisCallback<Long>) conn -> conn.bitCount(key.getBytes())));
    }


    /**
     * 执行自定义Redis操作
     *
     * @param callback 自定义操作回调
     * @param <T>      返回值类型
     * @return 操作结果
     */
    public <T> T execute(Function<RedisTemplate<String, Object>, T> callback) {
        try {
            return callback.apply(redisTemplate);
        } catch (Exception e) {
            throw new RuntimeException("Redis操作失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取指定类型的操作接口
     *
     * @param opsFunction 获取操作接口的函数
     * @param <T>         操作接口类型
     * @return 操作接口
     */
    public <T> T getOps(Function<RedisTemplate<String, Object>, T> opsFunction) {
        return opsFunction.apply(redisTemplate);
    }

    /**
     * 批量执行Redis操作
     *
     * @param operations 操作列表
     */
    public void executeBatch(List<Consumer<RedisTemplate<String, Object>>> operations) {
        try {
            for (Consumer<RedisTemplate<String, Object>> operation : operations) {
                operation.accept(redisTemplate);
            }
        } catch (Exception e) {
            throw new RuntimeException("Redis批量操作失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行Redis事务
     *
     * @param operations 事务中的操作
     * @return 事务执行结果
     */
    public List<Object> executeTransaction(Consumer<RedisTemplate<String, Object>> operations) {
        try {
            redisTemplate.setEnableTransactionSupport(true);
            redisTemplate.multi();
            operations.accept(redisTemplate);
            return redisTemplate.exec();
        } catch (Exception e) {
            redisTemplate.discard();
            throw new RuntimeException("Redis事务操作失败: " + e.getMessage(), e);
        } finally {
            redisTemplate.setEnableTransactionSupport(false);
        }
    }

    /**
     * 执行Redis管道操作
     *
     * @param pipelineCallback 管道操作回调
     * @return 管道执行结果
     */
    public List<Object> executePipeline(Consumer<RedisTemplate<String, Object>> pipelineCallback) {
        return redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            pipelineCallback.accept(redisTemplate);
            return null;
        });
    }
}