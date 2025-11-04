package com.yuluo.picture486backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.yuluo.picture486backend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class Picture486BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(Picture486BackendApplication.class, args);
    }

}
