package com.yuluo.picture486backend.service;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
public class RedissonTest {

    @Resource
    private RedisConnectionFactory redisConnectionFactory;
    
    @Resource
    private RedissonClient redissonClient;

    /**
     * 测试Redis连接是否正常工作
     */
    @Test
    public void testRedisConnection() {
        try {
            StringRedisTemplate redisTemplate = new StringRedisTemplate(redisConnectionFactory);
            redisTemplate.opsForValue().set("test_key", "test_value", 60, TimeUnit.SECONDS);
            String value = redisTemplate.opsForValue().get("test_key");
            System.out.println("Redis连接测试: " + value);
            assert "test_value".equals(value);
        } catch (Exception e) {
            System.err.println("Redis连接测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 测试Redis分布式锁的基本功能
     */
    @Test
    public void testRedisLockBasic() throws InterruptedException {
        StringRedisTemplate redisTemplate = new StringRedisTemplate(redisConnectionFactory);
        String lockKey = "test_lock_key";
        
        // 模拟5个线程竞争同一个锁
        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    
                    // 尝试获取锁，使用SET NX命令模拟
                    Boolean lockAcquired = redisTemplate.opsForValue()
                        .setIfAbsent(lockKey, "locked", 10, TimeUnit.SECONDS);
                    
                    if (lockAcquired != null && lockAcquired) {
                        successCount.incrementAndGet();
                        System.out.println("线程 " + index + " 获取锁成功");
                        
                        // 模拟业务处理时间
                        Thread.sleep(2000);
                        
                        // 释放锁
                        redisTemplate.delete(lockKey);
                        System.out.println("线程 " + index + " 释放锁");
                    } else {
                        failCount.incrementAndGet();
                        System.out.println("线程 " + index + " 获取锁失败");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    finishLatch.countDown();
                }
            });
        }
        
        startLatch.countDown();
        finishLatch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();
        
        System.out.println("获取锁成功次数: " + successCount.get());
        System.out.println("获取锁失败次数: " + failCount.get());
        
        // 验证只有一个线程能成功获取锁
        assert successCount.get() == 1;
        assert failCount.get() == 4;
    }
    
    /**
     * 测试Redisson分布式锁功能
     */
    @Test
    public void testRedissonLock() throws InterruptedException {
        String lockKey = "redisson_test_lock";
        
        // 模拟5个线程竞争同一个Redisson锁
        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        AtomicInteger timeoutCount = new AtomicInteger(0);
        
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    
                    // 获取Redisson锁
                    RLock lock = redissonClient.getLock(lockKey);
                    
                    // 尝试获取锁，等待3秒，持有5秒
                    boolean locked = lock.tryLock(3, 5, TimeUnit.SECONDS);
                    
                    if (locked) {
                        successCount.incrementAndGet();
                        System.out.println("线程 " + index + " 通过Redisson获取锁成功");
                        
                        // 模拟业务处理时间
                        Thread.sleep(2000);
                        
                        // 释放锁
                        lock.unlock();
                        System.out.println("线程 " + index + " 释放Redisson锁");
                    } else {
                        timeoutCount.incrementAndGet();
                        System.out.println("线程 " + index + " 通过Redisson获取锁超时");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.out.println("线程 " + index + " 通过Redisson获取锁发生异常: " + e.getMessage());
                } finally {
                    finishLatch.countDown();
                }
            });
        }
        
        startLatch.countDown();
        finishLatch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();
        
        System.out.println("Redisson锁测试结果:");
        System.out.println("获取锁成功次数: " + successCount.get());
        System.out.println("获取锁超时次数: " + timeoutCount.get());
        System.out.println("获取锁失败次数: " + failCount.get());
        
        // 验证结果
        assert successCount.get() == 1;  // 只有一个线程能成功获取锁
        assert timeoutCount.get() >= 3;  // 至少3个线程会超时（取决于执行时间）
    }
}