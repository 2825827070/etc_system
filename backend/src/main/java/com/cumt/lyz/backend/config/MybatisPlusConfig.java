package com.cumt.lyz.backend.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * MyBatisPlus配置类
 * 通过Spring的配置注解来定义MyBatisPlus的相关配置
 */
@Configuration
public class MybatisPlusConfig {
    /**
     * 配置MyBatisPlus拦截器
     * 该方法用于创建和配置MyBatisPlus的核心拦截器，添加各种插件
     *
     * @return MybatisPlusInterceptor 配置好的拦截器实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页插件 (如果不加，Page查询可能不生效)
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}