package com.yuluo.picture486backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.yuluo.picture486backend.mapper")
@EnableAsync
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableScheduling
public class Picture486BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(Picture486BackendApplication.class, args);
    }

}