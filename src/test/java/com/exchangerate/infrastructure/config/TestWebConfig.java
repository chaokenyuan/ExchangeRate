package com.exchangerate.infrastructure.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 測試環境專用的Web配置，暫時禁用攔截器
 */
@Configuration
@Profile("test")
public class TestWebConfig implements WebMvcConfigurer {

    // 暫時禁用攔截器，直接在步驟定義中處理權限邏輯
    // @Autowired
    // private TestSecurityInterceptor testSecurityInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 暫時不添加攔截器
        // registry.addInterceptor(testSecurityInterceptor)
        //         .addPathPatterns("/api/exchange-rates/**");
    }
}