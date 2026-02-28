package com.yuluo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {"com.yuluo.picture486backend", "com.yuluo.picture486ddd"})
@MapperScan("com.yuluo.picture486ddd.infrastructure.mapper")
@EnableAsync
@EnableAspectJAutoProxy(exposeProxy = true)
public class Picture486BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(Picture486BackendApplication.class, args);
    }

}