package com.cumt.lyz.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Knife4j 接口文档配置类
 * 作用：定制化文档信息，并指定只扫描 controller 包，屏蔽 BasicErrorController
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.OAS_30) // 使用 Swagger 3.0 标准
                .enable(true) // 开启 Swagger
                .apiInfo(apiInfo())
                .select()
                // 【核心代码】指定扫描的包路径，只有这个包下的接口才会显示
                // 这样就完美屏蔽了 Spring Boot 自带的 BasicErrorController
                .apis(RequestHandlerSelectors.basePackage("com.cumt.lyz.backend.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("高速公路 ETC 大数据平台 - 接口文档")
                .description("基于 SpringBoot + MyCat 的 ETC 数据管理后端")
                .contact(new Contact("开发小组", "http://localhost:8080", "email@example.com"))
                .version("1.0")
                .build();
    }
}