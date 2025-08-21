package com.exchangerate.stepdefinitions;

import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 基礎步驟定義類，提供共享的測試狀態和工具方法
 * 
 * 這個類遵循BDD最佳實踐：
 * - 提供測試狀態的中央管理
 * - 支援不同業務域的步驟定義類共享狀態
 * - 確保測試隔離性和一致性
 */
public abstract class BaseStepDefinitions {
    
    // ==================== 共享測試狀態 ====================
    
    /** 最後一次API回應的狀態碼 */
    protected static String lastResponseStatus;
    
    /** 最後一次API回應的內容 */
    protected static String lastResponseBody;
    
    /** 系統是否已啟動 */
    protected static boolean systemStarted = false;
    
    /** 資料庫是否已連線 */
    protected static boolean databaseConnected = false;
    
    /** 使用者是否有管理者權限 */
    protected static boolean adminPermissions = false;
    
    /** 使用者是否已登入系統 */
    protected static boolean isLoggedIn = false;
    
    /** 測試資料容器 - 用於存放測試期間的動態資料 */
    protected static Map<String, Object> testContext = new java.util.HashMap<>();
    
    /** 請求開始時間 (nanoseconds) - 用於效能測試 */
    protected static Long requestStartTime;
    
    /** 請求結束時間 (nanoseconds) - 用於效能測試 */
    protected static Long requestEndTime;
    
    // ==================== 狀態重置方法 ====================
    
    /**
     * 重置所有測試狀態到初始值
     * 這個方法應該在每個場景開始前調用，確保測試隔離性
     */
    protected static void resetTestState() {
        lastResponseStatus = null;
        lastResponseBody = null;
        systemStarted = false;
        databaseConnected = false;
        adminPermissions = false;
        isLoggedIn = false;
        requestStartTime = null;
        requestEndTime = null;
        testContext.clear();
    }
    
    // ==================== 權限檢查工具方法 ====================
    
    /**
     * 檢查權限並設置適當的回應狀態
     * 
     * @return true 如果有權限執行操作，false 如果需要返回錯誤
     */
    protected static boolean checkPermissionsForModification() {
        if (!isLoggedIn) {
            lastResponseStatus = "401";
            lastResponseBody = "{\"error\":\"需要登入\"}";
            return false;
        }
        
        if (!adminPermissions) {
            lastResponseStatus = "403";
            lastResponseBody = "{\"error\":\"權限不足\"}";
            return false;
        }
        
        return true;
    }
    
    // ==================== 資料驗證工具方法 ====================
    
    /**
     * 驗證貨幣代碼格式
     */
    protected static boolean isValidCurrencyFormat(String currency) {
        if (currency == null || currency.length() != 3) return false;
        // 模擬無效的貨幣代碼
        return !currency.equals("ABC") && !currency.equals("XYZ") && 
               !currency.equals("XXX") && !currency.equals("YYY") &&
               !currency.equals("ZZZ");
    }
    
    /**
     * 檢查基本資料欄位的完整性
     */
    protected static boolean hasValidBasicData(String fromCurrency, String toCurrency, String rate) {
        if (fromCurrency == null || "null".equals(fromCurrency) || 
            "[empty]".equals(fromCurrency) || fromCurrency.trim().isEmpty()) {
            lastResponseStatus = "400";
            lastResponseBody = "{\"error\":\"來源貨幣為必填欄位\"}";
            return false;
        }
        
        if (toCurrency == null || "null".equals(toCurrency) || 
            "[empty]".equals(toCurrency) || toCurrency.trim().isEmpty()) {
            lastResponseStatus = "400";
            lastResponseBody = "{\"error\":\"目標貨幣為必填欄位\"}";
            return false;
        }
        
        if (rate == null || "null".equals(rate) || 
            "[empty]".equals(rate) || rate.trim().isEmpty()) {
            lastResponseStatus = "400";
            lastResponseBody = "{\"error\":\"匯率為必填欄位\"}";
            return false;
        }
        
        return true;
    }
    
    // ==================== JSON解析工具方法 ====================
    
    /**
     * 簡單的JSON解析器，用於測試目的
     * 在實際項目中應該使用Jackson或Gson
     */
    protected static Map<String, String> parseJsonToMap(String json) {
        Map<String, String> result = new java.util.HashMap<>();
        if (json == null || json.trim().isEmpty()) return result;
        
        // 移除大括號並分割
        json = json.replaceAll("[{}]", "").trim();
        if (json.isEmpty()) return result;
        
        String[] pairs = json.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().replaceAll("\"", "");
                String value = keyValue[1].trim().replaceAll("\"", "");
                result.put(key, value);
            }
        }
        return result;
    }
    
    // ==================== 效能測試工具方法 ====================
    
    /**
     * 開始計時 - 記錄請求開始時間
     */
    protected static void startRequestTiming() {
        requestStartTime = System.nanoTime();
    }
    
    /**
     * 結束計時 - 記錄請求結束時間
     */
    protected static void endRequestTiming() {
        requestEndTime = System.nanoTime();
    }
    
    /**
     * 執行請求並自動計時的通用方法
     * @param operation 要執行的操作
     */
    protected static void executeWithTiming(Runnable operation) {
        startRequestTiming();
        try {
            operation.run();
        } finally {
            endRequestTiming();
        }
    }
}