package com.exchangerate;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Cucumber 測試執行器 (JUnit 5版本)
 * 使用JUnit 5 Platform執行Cucumber測試，不依賴Spring Boot
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "com.exchangerate.stepdefinitions")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value = "pretty,html:target/cucumber-reports/cucumber.html,json:target/cucumber-reports/cucumber.json,junit:target/cucumber-reports/cucumber.xml")
public class CucumberTestRunner {
    
    // 這個類別作為測試執行器，不需要額外的程式碼
    // JUnit 5 Platform + Cucumber，不使用Spring Boot
    
}