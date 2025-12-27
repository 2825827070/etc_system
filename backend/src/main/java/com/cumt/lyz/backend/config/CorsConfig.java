package com.cumt.lyz.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局 Web 配置类
 * 1. 解决 Swagger/Knife4j 报错 (PathMatch)
 * 2. 解决 前端跨域问题 (CORS)
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 【核心代码】解决 Knife4j 文档请求异常
     * 强制使用 AntPathMatcher，这是 Swagger 唯一能识别的路径匹配模式
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setPathMatcher(new AntPathMatcher());
    }

    /**
     * 跨域配置：允许前端访问
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600)
                .allowedHeaders("*");
    }
}