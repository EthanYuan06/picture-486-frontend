package com.yuluo.picture486ddd.interfaces.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yuluo.picture486ddd.domain.picture.entity.Picture;
import com.yuluo.picture486ddd.domain.picture.service.PictureDomainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 虚拟线程 vs OS线程 性能对比测试接口
 */
@RestController
@RequestMapping("/thread-benchmark")
@Tag(name = "线程性能测试", description = "虚拟线程与传统OS线程对比测试")
@Slf4j
public class ThreadBenchmarkController {

    @Resource
    private PictureDomainService pictureDomainService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // 本地缓存（Caffeine）
    private final Cache<String, String> LOCAL_CACHE =
            Caffeine.newBuilder().initialCapacity(1024)
                    .maximumSize(10000L)
                    .expireAfterWrite(2L, TimeUnit.MINUTES)
                    .build();

    @GetMapping("/load")
    @Operation(summary = "加载图片列表（带缓存）")
    public Page<Picture> loadPictures(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size) {
        
        Thread t = Thread.currentThread();
        long start = System.currentTimeMillis();
        
        log.info("请求 - 线程:{}, 虚拟:{}, 页:{}", t.getName(), t.isVirtual(), current);
        
        // 构建缓存key
        String cacheKey = "thread-benchmark:list:" + current + ":" + size;
        
        // 查询本地缓存
        String cachedValue = LOCAL_CACHE.getIfPresent(cacheKey);
        if (cachedValue != null) {
            log.info("本地缓存命中 - 耗时:{}ms", System.currentTimeMillis() - start);
            return convertJsonToPicturePage(cachedValue);
        }
        
        // 查询Redis缓存
        ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
        cachedValue = valueOps.get(cacheKey);
        if (cachedValue != null) {
            log.info("Redis缓存命中 - 耗时:{}ms", System.currentTimeMillis() - start);
            LOCAL_CACHE.put(cacheKey, cachedValue);
            return convertJsonToPicturePage(cachedValue);
        }
        
        // 查询数据库
        Page<Picture> result = pictureDomainService.page(
            new Page<>(current, size),
            new QueryWrapper<Picture>().like("name", "用户%_图片%").orderByDesc("createTime")
        );
        
        // 更新缓存
        String cacheValue = JSONUtil.toJsonStr(result);
        LOCAL_CACHE.put(cacheKey, cacheValue);
        valueOps.set(cacheKey, cacheValue, 5, TimeUnit.MINUTES);
        
        log.info("数据库查询 - 耗时:{}ms, 返回:{}条", System.currentTimeMillis() - start, result.getRecords().size());
        return result;
    }

    @GetMapping("/check")
    @Operation(summary = "检查当前线程类型")
    public Map<String, Object> checkThread() {
        Thread t = Thread.currentThread();
        Map<String, Object> result = new HashMap<>();
        result.put("threadName", t.getName());
        result.put("isVirtual", t.isVirtual());
        result.put("threadId", t.threadId());
        result.put("javaVersion", System.getProperty("java.version"));
        return result;
    }

    /**
     * 缓存类型转换处理
     */
    private Page<Picture> convertJsonToPicturePage(String cachedValue) {
        Page<?> tempResult = JSONUtil.toBean(cachedValue, Page.class);
        Page<Picture> result = new Page<>();
        result.setCurrent(tempResult.getCurrent());
        result.setSize(tempResult.getSize());
        result.setTotal(tempResult.getTotal());

        List<Picture> pictureRecords = new ArrayList<>();
        for (Object obj : tempResult.getRecords()) {
            if (obj instanceof JSONObject jsonObj) {
                Picture picture = jsonObj.toBean(Picture.class);
                pictureRecords.add(picture);
            } else if (obj instanceof Picture) {
                pictureRecords.add((Picture) obj);
            }
        }
        result.setRecords(pictureRecords);
        return result;
    }
}
