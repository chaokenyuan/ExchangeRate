package com.exchangerate;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.FEATURES_PROPERTY_NAME;

/**
 * Cucumber 測試執行器
 * 使用 JUnit Platform Suite 來執行 Cucumber 測試
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = 
    "pretty," +
    "html:target/cucumber-reports/cucumber.html," +
    "json:target/cucumber-reports/cucumber.json," +
    "junit:target/cucumber-reports/cucumber.xml")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = 
    "com.exchangerate.stepdefinitions,com.exchangerate.config")
@ConfigurationParameter(key = FEATURES_PROPERTY_NAME, value = "src/test/resources/features")
public class CucumberTestRunner {
    
    // 這個類別作為測試執行器，不需要額外的程式碼
    // JUnit Platform 會自動發現並執行 Cucumber 測試
    
}