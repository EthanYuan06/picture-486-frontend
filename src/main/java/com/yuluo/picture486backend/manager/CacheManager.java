package com.yuluo.picture486backend.manager;

import cn.hutool.json.JSONUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 通用缓存管理器
 */
@Deprecated
@Component
public class CacheManager {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private final Cache<String, String> localCache =
            Caffeine.newBuilder().initialCapacity(1024)
                    .maximumSize(10000L)
                    // 缓存 5 分钟移除
                    .expireAfterWrite(5L, TimeUnit.MINUTES)
                    .build();

    /**
     * 获取缓存结果的通用方法
     *
     * @param keyPrefix 缓存键前缀
     * @param queryCondition 查询条件
     * @param databaseQuery 数据库查询函数
     * @return 查询结果
     */
    public <T> T getCachedResult(String keyPrefix, Object queryCondition, Supplier<T> databaseQuery) {
        //构建缓存key格式
        String queryConditionStr = JSONUtil.toJsonStr(queryCondition);
        String hashKey = DigestUtils.md5DigestAsHex(queryConditionStr.getBytes());
        String cacheKey = keyPrefix + ":" + hashKey;

        //查询本地缓存
        String cachedValue = localCache.getIfPresent(cacheKey);
        if (cachedValue != null) {
            //缓存命中，返回结果
            return (T) JSONUtil.toBean(cachedValue, Object.class);
        }

        //查询分布式缓存
        ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
        cachedValue = valueOps.get(cacheKey);
        if (cachedValue != null) {
            //缓存命中，返回结果
            localCache.put(cacheKey, cachedValue);
            return (T) JSONUtil.toBean(cachedValue, Object.class);
        }

        //查询数据库
        T result = databaseQuery.get();
        String cacheValue = JSONUtil.toJsonStr(result);

        //更新本地缓存
        localCache.put(cacheKey, cacheValue);

        //更新Redis缓存，设置过期时间5分钟
        valueOps.set(cacheKey, cacheValue, 5, TimeUnit.MINUTES);

        return result;
    }

    /**
     * 清除指定前缀的缓存
     *
     * @param keyPrefix 缓存键前缀
     */
    public void clearCache(String keyPrefix) {
        // 清除本地缓存
        localCache.invalidateAll();
        // 清除Redis缓存中与指定前缀相关的缓存
        stringRedisTemplate.delete(stringRedisTemplate.keys(keyPrefix + "*"));
    }
}