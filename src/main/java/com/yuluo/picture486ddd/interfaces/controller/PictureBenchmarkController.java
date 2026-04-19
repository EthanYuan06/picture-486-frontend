package com.yuluo.picture486ddd.interfaces.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuluo.picture486ddd.domain.picture.entity.Picture;
import com.yuluo.picture486ddd.domain.picture.service.PictureDomainService;
import com.yuluo.picture486ddd.domain.user.entity.User;
import com.yuluo.picture486ddd.domain.user.service.UserDomainService;
import com.yuluo.picture486ddd.infrastructure.common.BaseResponse;
import com.yuluo.picture486ddd.infrastructure.common.ResultUtils;
import com.yuluo.picture486ddd.infrastructure.exception.BusinessException;
import com.yuluo.picture486ddd.infrastructure.exception.ErrorCode;
import com.yuluo.picture486ddd.interfaces.dto.picture.PictureQueryRequest;
import com.yuluo.picture486ddd.interfaces.vo.picture.PictureVo;
import com.yuluo.picture486ddd.interfaces.vo.user.UserVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 压测专用Controller - 用于性能对比测试
 * 
 * @author 东山羽洛
 */
@RestController
@RequestMapping("/benchmark")
@Tag(name = "压测接口", description = "用于N+1查询优化前后性能对比测试")
@Slf4j
public class PictureBenchmarkController {

    @Resource
    private PictureDomainService pictureDomainService;

    @Resource
    private UserDomainService userDomainService;

    /**
     * 【优化前】存在N+1查询问题的图片分页接口
     * 
     * 问题说明：
     * - 查询1次获取图片列表（假设20条）
     * - 循环20次，每次查询1个用户信息
     * - 总计：1 + 20 = 21次数据库查询
     * 
     * @param current 当前页码
     * @param size 每页大小
     * @return 图片分页列表
     */
    @GetMapping("/page-with-n-plus-1")
    @Operation(summary = "【优化前】存在N+1问题的图片分页查询", description = "用于性能对比测试，展示N+1查询问题")
    public BaseResponse<Page<PictureVo>> getPicturePageWithNPlus1(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size,
            HttpServletRequest request) {
        
        long startTime = System.currentTimeMillis();
        log.info("[N+1问题] 开始查询图片列表: current={}, size={}", current, size);
        
        // 1. 查询图片列表（1次查询）
        Page<Picture> picturePage = pictureDomainService.page(
                new Page<>(current, size),
                new QueryWrapper<Picture>().orderByDesc("createTime")
        );
        
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVo> pictureVoPage = new Page<>(current, size, picturePage.getTotal());
        
        if (pictureList.isEmpty()) {
            return ResultUtils.success(pictureVoPage);
        }
        
        // 2. ❌ N+1查询问题：循环中逐条查询用户信息
        List<PictureVo> pictureVoList = pictureList.stream().map(picture -> {
            PictureVo vo = PictureVo.objToVo(picture);
            
            // 每次都单独查询数据库（N次查询）
            long userQueryStart = System.currentTimeMillis();
            User user = userDomainService.getUser(picture.getUserId());
            long userQueryTime = System.currentTimeMillis() - userQueryStart;
            
            if (user != null) {
                UserVo userVo = new UserVo();
                userVo.setId(user.getId());
                userVo.setUserName(user.getUserName());
                userVo.setUserAvatar(user.getUserAvatar());
                userVo.setUserProfile(user.getUserProfile());
                vo.setUser(userVo);
            }
            
            if (userQueryTime > 10) {
                log.warn("[N+1问题] 单次用户查询耗时过长: {} ms, userId={}", userQueryTime, picture.getUserId());
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        pictureVoPage.setRecords(pictureVoList);
        
        long totalTime = System.currentTimeMillis() - startTime;
        log.info("[N+1问题] 查询完成，总耗时: {} ms, 图片数量: {}", totalTime, pictureList.size());
        
        if (totalTime > 1000) {
            log.warn("[性能告警] N+1查询耗时超过1秒: {} ms", totalTime);
        }
        
        return ResultUtils.success(pictureVoPage);
    }

    /**
     * 【优化后】已解决N+1查询问题的图片分页接口
     * 
     * 优化方案：
     * - 查询1次获取图片列表
     * - 提取所有用户ID并去重
     * - 批量查询所有用户信息（1次查询）
     * - 使用HashMap在内存中匹配
     * - 总计：1 + 1 = 2次数据库查询
     * 
     * @param current 当前页码
     * @param size 每页大小
     * @return 图片分页列表
     */
    @GetMapping("/page-optimized")
    @Operation(summary = "【优化后】已解决N+1问题的图片分页查询", description = "使用批量预加载优化，仅2次数据库查询")
    public BaseResponse<Page<PictureVo>> getPicturePageOptimized(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size,
            HttpServletRequest request) {
        
        long startTime = System.currentTimeMillis();
        log.info("[优化后] 开始查询图片列表: current={}, size={}", current, size);
        
        // 1. 查询图片列表（1次查询）
        Page<Picture> picturePage = pictureDomainService.page(
                new Page<>(current, size),
                new QueryWrapper<Picture>().orderByDesc("createTime")
        );
        
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVo> pictureVoPage = new Page<>(current, size, picturePage.getTotal());
        
        if (pictureList.isEmpty()) {
            return ResultUtils.success(pictureVoPage);
        }
        
        long step1Time = System.currentTimeMillis();
        log.debug("[优化后] 步骤1-查询图片列表耗时: {} ms", step1Time - startTime);
        
        // 2. ✅ 优化方案：批量预加载用户信息
        
        // 2.1 提取所有不重复的用户ID
        java.util.Set<Long> userIdSet = pictureList.stream()
                .map(Picture::getUserId)
                .collect(Collectors.toSet());
        
        long step2Time = System.currentTimeMillis();
        log.debug("[优化后] 步骤2-提取用户ID耗时: {} ms, 用户数量: {}", step2Time - step1Time, userIdSet.size());
        
        // 2.2 批量查询所有用户信息（1次查询）
        List<User> users = userDomainService.listByIds(userIdSet);
        
        long step3Time = System.currentTimeMillis();
        log.debug("[优化后] 步骤3-批量查询用户耗时: {} ms", step3Time - step2Time);
        
        // 2.3 构建用户ID到User对象的Map索引（O(1)查找）
        java.util.Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        
        long step4Time = System.currentTimeMillis();
        log.debug("[优化后] 步骤4-构建Map索引耗时: {} ms", step4Time - step3Time);
        
        // 2.4 转换为VO并填充用户信息（内存操作，无数据库查询）
        List<PictureVo> pictureVoList = pictureList.stream().map(picture -> {
            PictureVo vo = PictureVo.objToVo(picture);
            
            // 从Map中获取用户信息（O(1)时间复杂度）
            User user = userMap.get(picture.getUserId());
            if (user != null) {
                UserVo userVo = new UserVo();
                userVo.setId(user.getId());
                userVo.setUserName(user.getUserName());
                userVo.setUserAvatar(user.getUserAvatar());
                userVo.setUserProfile(user.getUserProfile());
                vo.setUser(userVo);
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        long step5Time = System.currentTimeMillis();
        log.debug("[优化后] 步骤5-填充用户信息耗时: {} ms", step5Time - step4Time);
        
        pictureVoPage.setRecords(pictureVoList);
        
        long totalTime = System.currentTimeMillis() - startTime;
        log.info("[优化后] 查询完成，总耗时: {} ms, 图片数量: {}, 数据库查询次数: 2次", 
                totalTime, pictureList.size());
        
        if (totalTime > 500) {
            log.warn("[性能告警] 优化后查询耗时超过500ms: {} ms", totalTime);
        }
        
        return ResultUtils.success(pictureVoPage);
    }

    /**
     * 性能对比测试接口 - 分别独立调用两个方法并返回对比结果
     * 注意：每个测试独立执行5次取平均值，避免JVM预热和缓存影响
     * 
     * @param current 当前页码
     * @param size 每页大小
     * @return 性能对比结果
     */
    @GetMapping("/compare")
    @Operation(summary = "性能对比测试", description = "独立执行优化前后的查询，取5次平均值进行对比")
    public BaseResponse<PerformanceCompareResult> comparePerformance(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size,
            HttpServletRequest request) {
        
        log.info("========== 开始性能对比测试 ==========");
        log.info("测试参数: current={}, size={}", current, size);
        
        int warmupRounds = 2; // 预热轮数
        int testRounds = 5;   // 正式测试轮数
        
        // 预热阶段（不计入统计）
        log.info("--- 预热阶段 ({} 轮) ---", warmupRounds);
        for (int i = 0; i < warmupRounds; i++) {
            getPicturePageWithNPlus1(current, size, request);
            getPicturePageOptimized(current, size, request);
        }
        
        // 正式测试阶段 - N+1查询
        log.info("--- 测试N+1查询 ({} 轮) ---", testRounds);
        long totalNPlus1Time = 0;
        for (int i = 0; i < testRounds; i++) {
            long start = System.currentTimeMillis();
            getPicturePageWithNPlus1(current, size, request);
            long elapsed = System.currentTimeMillis() - start;
            totalNPlus1Time += elapsed;
            log.info("  第{}轮 N+1耗时: {} ms", i + 1, elapsed);
        }
        long avgNPlus1Time = totalNPlus1Time / testRounds;
        
        // 正式测试阶段 - 优化查询
        log.info("--- 测试优化查询 ({} 轮) ---", testRounds);
        long totalOptimizedTime = 0;
        for (int i = 0; i < testRounds; i++) {
            long start = System.currentTimeMillis();
            getPicturePageOptimized(current, size, request);
            long elapsed = System.currentTimeMillis() - start;
            totalOptimizedTime += elapsed;
            log.info("  第{}轮 优化耗时: {} ms", i + 1, elapsed);
        }
        long avgOptimizedTime = totalOptimizedTime / testRounds;
        
        // 计算性能提升
        double improvement = ((avgNPlus1Time - avgOptimizedTime) * 100.0) / avgNPlus1Time;
        double speedup = (double) avgNPlus1Time / avgOptimizedTime;
        
        PerformanceCompareResult result = new PerformanceCompareResult();
        result.setCurrentPage(current);
        result.setPageSize(size);
        result.setTimeNPlus1Ms(avgNPlus1Time);
        result.setTimeOptimizedMs(avgOptimizedTime);
        result.setImprovementPercent(Math.round(improvement * 100.0) / 100.0);
        result.setSpeedupTimes(Math.round(speedup * 100.0) / 100.0);
        result.setDatabaseQueriesNPlus1((int)(size + 1)); // 1次查图片 + N次查用户
        result.setDatabaseQueriesOptimized(2); // 1次查图片 + 1次批量查用户
        result.setTestRounds(testRounds);
        
        log.info("========== 性能对比测试结果 ==========");
        log.info("平均耗时 - 优化前（N+1）: {} ms", avgNPlus1Time);
        log.info("平均耗时 - 优化后（批量）: {} ms", avgOptimizedTime);
        log.info("性能提升: {}%", result.getImprovementPercent());
        log.info("加速倍数: {}x", result.getSpeedupTimes());
        log.info("数据库查询减少: {}次 -> {}次", result.getDatabaseQueriesNPlus1(), result.getDatabaseQueriesOptimized());
        
        return ResultUtils.success(result);
    }

    /**
     * 性能对比结果DTO
     */
    @lombok.Data
    public static class PerformanceCompareResult {
        private long currentPage;
        private long pageSize;
        private long timeNPlus1Ms;           // N+1查询平均耗时
        private long timeOptimizedMs;        // 优化后平均耗时
        private double improvementPercent;   // 性能提升百分比
        private double speedupTimes;         // 加速倍数
        private int databaseQueriesNPlus1;   // N+1查询次数
        private int databaseQueriesOptimized;// 优化后查询次数
        private int testRounds;              // 测试轮数
    }
}
