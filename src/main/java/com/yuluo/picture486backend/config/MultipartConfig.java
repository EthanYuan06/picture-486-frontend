package com.yuluo.picture486backend.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import jakarta.servlet.MultipartConfigElement;

/**
 * 文件上传配置
 */
@Configuration
public class MultipartConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        // 单个文件最大大小
        factory.setMaxFileSize(DataSize.ofMegabytes(15));
        // 请求总大小最大值
        factory.setMaxRequestSize(DataSize.ofMegabytes(15));
        return factory.createMultipartConfig();
    }
}