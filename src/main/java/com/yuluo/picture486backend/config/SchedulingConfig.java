package com.yuluo.picture486backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
public class SchedulingConfig implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        
        // 设置线程池大小
        threadPoolTaskScheduler.setPoolSize(2);
        threadPoolTaskScheduler.setThreadNamePrefix("scheduled-task-pool-");
        threadPoolTaskScheduler.initialize();
        
        taskRegistrar.setTaskScheduler(threadPoolTaskScheduler);
    }
}