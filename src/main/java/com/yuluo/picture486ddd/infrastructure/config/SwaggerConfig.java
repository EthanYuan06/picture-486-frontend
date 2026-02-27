package com.yuluo.picture486ddd.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 接口文档配置类
 */
@Configuration
public class SwaggerConfig {

    /**
     * 配置OpenAPI信息
     * @return OpenAPI对象
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("昴云-接口文档")
                        .version("1.0.0")
                        .description("昴云，你的智能云图库。联系作者：Subaru_0427@outlook.com")
                        .contact(new Contact().name("安和昴")));
    }
}