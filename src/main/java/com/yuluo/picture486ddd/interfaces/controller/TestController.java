package com.yuluo.picture486ddd.interfaces.controller;

import com.yuluo.picture486ddd.application.service.SpaceApplicationService;
import com.yuluo.picture486ddd.infrastructure.common.BaseResponse;
import com.yuluo.picture486ddd.infrastructure.common.ResultUtils;
import com.yuluo.picture486ddd.infrastructure.exception.BusinessException;
import com.yuluo.picture486ddd.infrastructure.exception.ErrorCode;
import com.yuluo.picture486ddd.interfaces.dto.space.SpaceAddRequest;
import com.yuluo.picture486ddd.domain.user.entity.User;
import com.yuluo.picture486ddd.domain.space.service.SpaceDomainService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 专门用于测试Redisson分布式锁的控制器
 */
@RestController
@RequestMapping("/test")
@Tag(name = "测试模块")
@Slf4j
public class TestController {

    @Resource
    private SpaceApplicationService spaceApplicationService;

    /**
     * 并发测试接口 - 用于验证Redisson分布式锁是否正常工作
     *
     * @param spaceAddRequest 空间创建请求
     * @param request HTTP请求
     * @return 测试结果
     */
    @PostMapping("/concurrent-create-space")
    public BaseResponse<String> concurrentCreateSpace(@RequestBody SpaceAddRequest spaceAddRequest, 
                                                      HttpServletRequest request) {
        try {
            // 创建10个线程并发执行
            int threadCount = 10;
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch finishLatch = new CountDownLatch(threadCount);

            // 创建模拟用户
            User mockUser = new User();
            mockUser.setId(999999L); // 使用特殊ID避免冲突
            mockUser.setUserRole("user");

            // 结果统计
            int[] successCount = {0};
            int[] operationErrorCount = {0}; // "操作频繁"错误计数
            int[] spaceExistErrorCount = {0}; // "空间已存在"错误计数
            int[] otherErrorCount = {0};
            
            // 存储错误信息
            List<String> errorMessages = new ArrayList<>();

            // 提交任务
            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                executorService.submit(() -> {
                    try {
                        startLatch.await(); // 等待统一启动
                        
                        // 调用创建空间的方法
                        long spaceId = spaceApplicationService.addSpace(spaceAddRequest, mockUser);
                        
                        synchronized (successCount) {
                            successCount[0]++;
                            log.info("线程 {} 成功创建空间，ID: {}", index, spaceId);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (BusinessException e) {
                        if (e.getCode() == ErrorCode.SYSTEM_ERROR.getCode() 
                                && e.getMessage().contains("操作频繁")) {
                            synchronized (operationErrorCount) {
                                operationErrorCount[0]++;
                                log.info("线程 {} 因操作频繁被拒绝", index);
                            }
                        } else if (e.getCode() == ErrorCode.OPERATION_ERROR.getCode()
                                && e.getMessage().contains("当前每个用户仅允许拥有一个空间")) {
                            synchronized (spaceExistErrorCount) {
                                spaceExistErrorCount[0]++;
                                log.info("线程 {} 空间已存在", index);
                            }
                        } else {
                            synchronized (otherErrorCount) {
                                otherErrorCount[0]++;
                                String errorMsg = String.format("线程 %d 发生其他业务异常: %s (错误码: %d)", 
                                        index, e.getMessage(), e.getCode());
                                errorMessages.add(errorMsg);
                                log.error("线程 {} 发生其他业务异常: ", index, e);
                            }
                        }
                    } catch (Exception e) {
                        synchronized (otherErrorCount) {
                            otherErrorCount[0]++;
                            String errorMsg = String.format("线程 %d 发生未知异常: %s", index, e.getMessage());
                            errorMessages.add(errorMsg);
                            log.error("线程 {} 发生未知异常: ", index, e);
                        }
                    } finally {
                        finishLatch.countDown();
                    }
                });
            }

            // 统一启动所有线程
            startLatch.countDown();
            
            // 等待所有线程执行完毕，最多等待30秒
            finishLatch.await(30, TimeUnit.SECONDS);
            executorService.shutdown();

            // 返回测试结果
            String result = String.format(
                "并发测试完成: 成功=%d, 操作频繁错误=%d, 空间已存在错误=%d, 其他错误=%d", 
                successCount[0], operationErrorCount[0], spaceExistErrorCount[0], otherErrorCount[0]
            );
            
            // 添加详细的错误信息
            if (!errorMessages.isEmpty()) {
                result += "\n详细错误信息:\n";
                for (String errorMsg : errorMessages) {
                    result += errorMsg + "\n";
                }
            }
            
            log.info(result);
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("并发测试发生异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "测试过程中发生异常");
        }
    }
    
    /**
     * 压力测试接口 - 更高强度的并发测试
     */
    @PostMapping("/stress-test-create-space")
    public BaseResponse<String> stressTestCreateSpace(@RequestBody SpaceAddRequest spaceAddRequest,
                                                     HttpServletRequest request) {
        try {
            // 创建20个线程并发执行
            int threadCount = 20;
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch finishLatch = new CountDownLatch(threadCount);

            // 创建模拟用户
            User mockUser = new User();
            mockUser.setId(999998L); // 使用另一个特殊ID避免冲突
            mockUser.setUserRole("user");

            // 结果统计
            int[] successCount = {0};
            int[] operationErrorCount = {0}; // "操作频繁"错误计数
            int[] spaceExistErrorCount = {0}; // "空间已存在"错误计数
            int[] otherErrorCount = {0};

            // 提交任务
            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                executorService.submit(() -> {
                    try {
                        startLatch.await(); // 等待统一启动
                        
                        // 添加一点随机延迟，增加并发性
                        Thread.sleep((long) (Math.random() * 10));
                        
                        // 调用创建空间的方法
                        long spaceId = spaceApplicationService.addSpace(spaceAddRequest, mockUser);
                        
                        synchronized (successCount) {
                            successCount[0]++;
                            log.info("压力测试线程 {} 成功创建空间，ID: {}", index, spaceId);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (BusinessException e) {
                        if (e.getCode() == ErrorCode.SYSTEM_ERROR.getCode() 
                                && e.getMessage().contains("操作频繁")) {
                            synchronized (operationErrorCount) {
                                operationErrorCount[0]++;
                                log.info("压力测试线程 {} 因操作频繁被拒绝", index);
                            }
                        } else if (e.getCode() == ErrorCode.OPERATION_ERROR.getCode()
                                && e.getMessage().contains("当前每个用户仅允许拥有一个空间")) {
                            synchronized (spaceExistErrorCount) {
                                spaceExistErrorCount[0]++;
                                log.info("压力测试线程 {} 空间已存在", index);
                            }
                        } else {
                            synchronized (otherErrorCount) {
                                otherErrorCount[0]++;
                                log.error("压力测试线程 {} 发生其他业务异常: ", index, e);
                            }
                        }
                    } catch (Exception e) {
                        synchronized (otherErrorCount) {
                            otherErrorCount[0]++;
                            log.error("压力测试线程 {} 发生未知异常: ", index, e);
                        }
                    } finally {
                        finishLatch.countDown();
                    }
                });
            }

            // 统一启动所有线程
            startLatch.countDown();
            
            // 等待所有线程执行完毕，最多等待30秒
            finishLatch.await(30, TimeUnit.SECONDS);
            executorService.shutdown();

            // 返回测试结果
            String result = String.format(
                "压力测试完成: 成功=%d, 操作频繁错误=%d, 空间已存在错误=%d, 其他错误=%d", 
                successCount[0], operationErrorCount[0], spaceExistErrorCount[0], otherErrorCount[0]
            );
            
            log.info(result);
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("压力测试发生异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压力测试过程中发生异常");
        }
    }
    
    /**
     * 专门测试Redisson锁的接口
     */
    @PostMapping("/test-redisson-lock")
    public BaseResponse<String> testRedissonLock(@RequestBody SpaceAddRequest spaceAddRequest) {
        // 先清理可能存在的测试数据
        // 注意：在实际应用中，你可能需要根据具体业务逻辑来清理测试数据
        
        return concurrentCreateSpace(spaceAddRequest, null);
    }
}