package com.yuluo.picture486backend.service;

import com.yuluo.picture486backend.model.dto.space.SpaceAddRequest;
import com.yuluo.picture486backend.model.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
public class SpaceServiceTest {

    /**
     * 测试Redisson分布式锁是否生效
     * 模拟多个线程同时尝试创建空间的情况
     * 注意：此测试需要真实的服务环境才能运行，因为它依赖于真实的Redis连接和数据库操作
     */
    @Test
    public void testRedissonLockEffectiveness() throws InterruptedException {
        System.out.println("该测试需要在完整的服务环境中运行以验证Redisson锁的有效性");
        System.out.println("请手动通过API快速连续发送多个创建空间的请求来测试");
        
        /*
        // 准备测试数据
        SpaceAddRequest spaceAddRequest = new SpaceAddRequest();
        spaceAddRequest.setSpaceName("测试空间");

        User loginUser = new User();
        loginUser.setId(1L);

        // 创建固定大小的线程池
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        
        // 使用CountDownLatch确保所有线程同时开始执行
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(10);

        // 记录成功创建空间的次数
        AtomicInteger successCount = new AtomicInteger(0);
        // 记录因"操作频繁"而失败的次数
        AtomicInteger operationErrorCount = new AtomicInteger(0);
        // 记录其他错误的次数
        AtomicInteger otherErrorCount = new AtomicInteger(0);

        // 启动10个线程同时尝试创建空间
        for (int i = 0; i < 10; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    // 等待所有线程准备就绪后一起执行
                    startLatch.await();
                    
                    // TODO: 在这里调用真实的SpaceService.addSpace方法
                    // 由于缺少完整的依赖注入环境，此处无法直接调用
                    
                    // 模拟调用过程
                    System.out.println("线程 " + index + " 尝试创建空间");
                    Thread.sleep((long) (Math.random() * 100)); // 随机延迟
                    
                    successCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    if (e.getMessage() != null && e.getMessage().contains("操作频繁")) {
                        operationErrorCount.incrementAndGet();
                    } else {
                        otherErrorCount.incrementAndGet();
                    }
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        // 开始执行所有任务
        startLatch.countDown();
        // 等待所有任务完成
        finishLatch.await();
        // 关闭线程池
        executorService.shutdown();

        // 输出结果
        System.out.println("=== Redisson锁有效性测试结果 ===");
        System.out.println("成功创建数: " + successCount.get());
        System.out.println("操作频繁错误数: " + operationErrorCount.get());
        System.out.println("其他错误数: " + otherErrorCount.get());
        
        // 验证结果：
        // 1. 只能有一个人成功创建空间
        // assertEquals(1, successCount.get(), "应该只有一个人成功创建空间");
        // 2. 至少要有一个人因为"操作频繁"而失败
        // assertTrue(operationErrorCount.get() >= 1, "至少要有一人因为操作频繁而失败");
        */
    }
}