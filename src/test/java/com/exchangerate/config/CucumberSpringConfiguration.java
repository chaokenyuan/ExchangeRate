package com.exchangerate.config;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Cucumber Spring Integration Configuration
 * 整合Cucumber測試與Spring Boot應用程式上下文
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CucumberSpringConfiguration {
    
    // 這個類別提供Cucumber與Spring Boot的整合配置
    // Spring Boot會自動載入應用程式上下文供Cucumber測試使用
    
}