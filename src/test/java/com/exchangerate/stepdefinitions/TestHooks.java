package com.exchangerate.stepdefinitions;

import io.cucumber.java.Before;

/**
 * Cucumber測試鉤子
 * 
 * 負責在測試場景執行前後進行必要的設置和清理
 */
public class TestHooks extends BaseStepDefinitions {
    
    /**
     * 在每個場景開始前重置測試狀態
     * 確保測試隔離性，避免不同場景之間的狀態污染
     */
    @Before
    public void resetStateBeforeScenario() {
        resetTestState();
    }
}