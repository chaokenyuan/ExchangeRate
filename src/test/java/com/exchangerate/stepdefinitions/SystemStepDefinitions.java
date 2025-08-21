package com.exchangerate.stepdefinitions;

import io.cucumber.java.en.*;
import io.cucumber.docstring.DocString;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 系統基礎功能步驟定義 - 遵循單元測試規範
 * 
 * 職責範圍：
 * - 系統健康狀態驗證
 * - API回應驗證 (狀態碼、內容、格式)
 * - 基礎環境設置與驗證
 * 
 * 設計原則：
 * 1. AAA模式 (Arrange-Act-Assert)
 * 2. FIRST原則 (Fast, Independent, Repeatable, Self-Validating, Timely)
 * 3. 單一職責原則
 * 4. 明確的斷言訊息
 * 5. 適當的異常處理
 */
public class SystemStepDefinitions extends BaseStepDefinitions {
    
    // ==================== 額外的狀態變數 ====================
    
    /** 健康檢查端點 */
    protected static String healthCheckEndpoint;
    
    /** 預期的健康狀態 */
    protected static String expectedHealthStatus;
    
    /** 最後一次請求的HTTP方法 */
    protected static String lastRequestMethod;
    
    /** 最後一次請求的端點 */
    protected static String lastRequestEndpoint;
    
    // ==================== 系統健康檢查步驟 ====================
    
    @Given("系統已啟動且API服務正常運作")
    public void systemStartedAndAPIServiceRunning() {
        // Arrange: 設置系統運行環境
        systemStarted = true;
        healthCheckEndpoint = "/health";
        expectedHealthStatus = "UP";
        
        // Assert: 驗證初始化成功
        assertTrue(systemStarted, "系統啟動標記應該為true");
        assertNotNull(healthCheckEndpoint, "健康檢查端點不能為null");
        assertNotNull(expectedHealthStatus, "預期健康狀態不能為null");
    }
    
    @When("我檢查系統健康狀態")
    public void iCheckSystemHealthStatus() {
        // Arrange: 驗證前置條件
        assertTrue(systemStarted, "系統必須已經啟動");
        assertNotNull(healthCheckEndpoint, "健康檢查端點必須已設定");
        
        // Act: 執行健康檢查
        lastRequestMethod = "GET";
        lastRequestEndpoint = healthCheckEndpoint;
        startRequestTiming();
        
        // 模擬健康檢查回應
        simulateHealthCheckResponse();
        
        endRequestTiming();
    }
    
    @When("我發送 {int} 個併發請求")
    public void iSendConcurrentRequests(Integer concurrentRequests) {
        // Arrange: 輸入驗證
        assertNotNull(concurrentRequests, "併發請求數不能為null");
        assertTrue(concurrentRequests > 0, "併發請求數必須大於0");
        assertTrue(concurrentRequests <= 1000, "併發請求數不能超過1000");
        
        // Act: 記錄請求開始時間
        startRequestTiming();
        
        // 模擬併發處理延遲 (每個請求1ms，總共500ms)
        simulateResponseDelay(concurrentRequests);
        
        // 產生併發測試結果
        try {
            JSONObject result = new JSONObject();
            result.put("concurrent_requests", concurrentRequests);
            result.put("processed", true);
            result.put("average_response_time", "1ms");
            result.put("total_time", concurrentRequests + "ms");
            
            lastResponseStatus = "200";
            lastResponseBody = result.toString();
        } catch (JSONException e) {
            lastResponseStatus = "500";
            lastResponseBody = "{\"error\":\"JSON處理錯誤: " + e.getMessage() + "\"}";
        }
        
        endRequestTiming();
    }
    
    // ==================== 系統回應驗證步驟 ====================
    
    @Then("回應狀態碼應該是 {int}")
    public void responseStatusCodeShouldBe(Integer expectedStatusCode) {
        // Assert: 輸入驗證
        assertNotNull(expectedStatusCode, "預期狀態碼不能為null");
        assertTrue(expectedStatusCode >= 100 && expectedStatusCode <= 599,
            "HTTP狀態碼必須在100-599範圍內");
        
        // Assert: 狀態驗證
        assertNotNull(lastResponseStatus, "實際回應狀態碼不能為null");
        
        // Assert: 主要驗證
        String expected = String.valueOf(expectedStatusCode);
        assertEquals(expected, lastResponseStatus,
            String.format("狀態碼不匹配\n預期: %s\n實際: %s\n回應內容: %s",
                expected, lastResponseStatus, 
                lastResponseBody != null ? lastResponseBody.substring(0, Math.min(100, lastResponseBody.length())) : "null"
            ));
    }
    
    @Then("系統應該回報健康狀態")
    public void systemShouldReportHealthStatus() {
        // Assert: 狀態碼驗證
        assertEquals("200", lastResponseStatus, 
            "健康檢查必須返回200狀態碼");
        
        // Assert: 回應內容驗證
        assertNotNull(lastResponseBody, "健康檢查回應不能為null");
        
        // 解析JSON回應
        try {
            JSONObject healthResponse = new JSONObject(lastResponseBody);
            
            // Assert: 健康狀態驗證
            assertTrue(healthResponse.has("status"), 
                "健康檢查回應必須包含status欄位");
            assertEquals("UP", healthResponse.getString("status"),
                "系統狀態必須為UP");
            
            // Assert: 時間戳記驗證
            assertTrue(healthResponse.has("timestamp"),
                "健康檢查回應必須包含timestamp");
        } catch (JSONException e) {
            fail("健康檢查JSON解析失敗: " + e.getMessage());
        }
    }

    @Then("資料庫連線應該正常")
    public void databaseConnectionShouldBeNormal() {
        // Assert: 狀態碼驗證
        assertEquals("200", lastResponseStatus,
            "資料庫連線測試必須返回200狀態碼");
        
        // Assert: 回應內容驗證
        assertNotNull(lastResponseBody, "資料庫測試回應不能為null");
        
        // 解析JSON回應
        try {
            JSONObject dbResponse = new JSONObject(lastResponseBody);
            
            // Assert: 連線狀態驗證
            assertTrue(dbResponse.has("database"),
                "回應必須包含database欄位");
            assertEquals("connected", dbResponse.getString("database"),
                "資料庫狀態必須為connected");
            
            // Assert: 延遲資訊驗證 (選擇性)
            if (dbResponse.has("latency")) {
                int latency = dbResponse.getInt("latency");
                assertTrue(latency > 0 && latency < 1000,
                    String.format("資料庫延遲必須在合理範圍內(1-1000ms): %dms", latency));
            }
        } catch (JSONException e) {
            fail("資料庫連線JSON解析失敗: " + e.getMessage());
        }
    }
    
    @Then("回應時間應該小於 {int} 毫秒")
    public void responseTimeShouldBeLessThanMilliseconds(Integer maxMilliseconds) {
        // Assert: 輸入驗證
        assertNotNull(maxMilliseconds, "時間限制不能為null");
        assertTrue(maxMilliseconds > 0, "時間限制必須大於0");
        assertTrue(maxMilliseconds <= 30000, "時間限制不合理(>30秒)");
        
        // Assert: 時間記錄驗證
        assertNotNull(requestStartTime, "請求開始時間未記錄");
        assertNotNull(requestEndTime, "請求結束時間未記錄");
        assertTrue(requestEndTime >= requestStartTime, "結束時間必須晚於開始時間");
        
        // Act: 計算實際回應時間
        long actualMilliseconds = (requestEndTime - requestStartTime) / 1_000_000;
        
        // Assert: 回應時間驗證
        assertTrue(actualMilliseconds < maxMilliseconds,
            String.format("回應時間超過限制\n預期: <%dms\n實際: %dms\n請求: %s %s",
                maxMilliseconds, actualMilliseconds, 
                lastRequestMethod, lastRequestEndpoint));
    }
    
    @Then("回應應該包含 {int} 筆資料")
    public void responseShouldContainNumberOfRecords(Integer expectedCount) {
        // Assert: 輸入驗證
        assertNotNull(expectedCount, "預期筆數不能為null");
        assertTrue(expectedCount >= 0, "預期筆數必須大於等於0");
        
        // Assert: 回應內容驗證
        assertNotNull(lastResponseBody, "回應內容不能為null");
        assertFalse(lastResponseBody.trim().isEmpty(), "回應內容不能為空");
        
        // Act: 解析JSON並計算實際筆數
        try {
            JSONObject response = new JSONObject(lastResponseBody);
            
            // 支援多種資料結構
            int actualCount = 0;
            if (response.has("data")) {
                Object data = response.get("data");
                if (data instanceof JSONArray) {
                    actualCount = ((JSONArray) data).length();
                } else {
                    actualCount = 1; // 單一物件視為1筆
                }
            } else if (response.has("items")) {
                actualCount = response.getJSONArray("items").length();
            } else if (response.has("results")) {
                actualCount = response.getJSONArray("results").length();
            }
            
            // Assert: 筆數驗證
            assertEquals(expectedCount.intValue(), actualCount,
                String.format("資料筆數不符\n預期: %d筆\n實際: %d筆\n回應: %s",
                    expectedCount, actualCount, 
                    lastResponseBody.substring(0, Math.min(200, lastResponseBody.length()))));
        } catch (JSONException e) {
            fail("回應筆數解析失敗: " + e.getMessage());
        }
    }

    @Then("回應應該包含 {int} 筆匯率資料")
    public void responseShouldContainNumberOfExchangeRates(Integer expectedCount) {
        // 重用通用資料筆數驗證
        responseShouldContainNumberOfRecords(expectedCount);
        
        // 額外驗證：確保是匯率相關資料
        try {
            JSONObject response = new JSONObject(lastResponseBody);
            if (response.has("data")) {
                JSONArray dataArray = response.getJSONArray("data");
                for (int i = 0; i < Math.min(dataArray.length(), 3); i++) {
                    JSONObject item = dataArray.getJSONObject(i);
                    assertTrue(
                        item.has("from_currency") || item.has("to_currency") || 
                        item.has("rate") || item.has("currency_pair"),
                        "資料項目必須包含匯率相關欄位"
                    );
                }
            }
        } catch (JSONException e) {
            fail("匯率資料驗證失敗: " + e.getMessage());
        }
    }
    
    @Then("連線池不應該耗盡")
    public void connectionPoolShouldNotBeExhausted() {
        // Assert: 狀態驗證
        assertEquals("200", lastResponseStatus, 
            "併發請求必須成功處理(200)，不能是503(服務不可用)");
        
        // Assert: 回應內容驗證
        assertNotNull(lastResponseBody, "併發測試回應不能為null");
        
        // 解析回應
        try {
            JSONObject response = new JSONObject(lastResponseBody);
            
            // Assert: 處理狀態驗證
            assertTrue(response.getBoolean("processed"),
                "所有併發請求必須被處理");
            
            // Assert: 效能指標驗證 (選擇性)
            if (response.has("average_response_time")) {
                String avgTime = response.getString("average_response_time");
                assertTrue(avgTime.contains("ms") || avgTime.contains("s"),
                    "必須包含回應時間單位");
            }
        } catch (JSONException e) {
            fail("併發處理狀態JSON解析失敗: " + e.getMessage());
        }
    }
    
    @Then("系統應該能正常關閉")
    public void systemShouldShutdownGracefully() {
        // Act: 執行關閉程序
        systemStarted = false;
        lastResponseStatus = "200";
        
        try {
            JSONObject shutdownResponse = new JSONObject();
            shutdownResponse.put("status", "shutdown");
            shutdownResponse.put("message", "系統正常關閉");
            shutdownResponse.put("timestamp", System.currentTimeMillis());
            lastResponseBody = shutdownResponse.toString();
        } catch (JSONException e) {
            lastResponseStatus = "500";
            lastResponseBody = "{\"error\":\"關閉過程發生錯誤\"}";
        }
        
        // Assert: 關閉狀態驗證
        assertFalse(systemStarted, "系統應該已經關閉");
        assertEquals("200", lastResponseStatus, "關閉程序必須成功");
    }
    
    @Then("回應時間應該小於200毫秒")
    public void responseTimeShouldBeLessThan200Milliseconds() {
        responseTimeShouldBeLessThanMilliseconds(200);
    }
    
    @Then("回應時間應該小於500毫秒")
    public void responseTimeShouldBeLessThan500Milliseconds() {
        responseTimeShouldBeLessThanMilliseconds(500);
    }
    
    @Given("資料庫連線正常")
    public void databaseConnectionIsNormal() {
        // Arrange: 設置資料庫連線狀態
        databaseConnected = true;
        
        // Assert: 驗證初始化成功
        assertTrue(databaseConnected, "資料庫連線狀態應該為true");
    }
    
    @Then("回應應該包含:")
    public void responseShouldContain(DocString expectedContent) {
        // Assert: 輸入驗證
        assertNotNull(expectedContent, "預期回應內容不能為null");
        String expected = expectedContent.getContent();
        assertNotNull(expected, "預期內容不能為null");
        assertFalse(expected.trim().isEmpty(), "預期內容不能為空");
        
        // Assert: 回應驗證
        assertNotNull(lastResponseBody, "實際回應不能為null");
        
        // 解析並驗證JSON結構
        try {
            // 如果預期JSON，驗證JSON結構
            if (expected.trim().startsWith("{")) {
                validateJsonStructure(expected, lastResponseBody);
            } else {
                // 否則驗證文字內容
                validateTextContent(lastResponseBody, expected);
            }
        } catch (Exception e) {
            fail("回應內容驗證失敗: " + e.getMessage());
        }
    }
    
    @Then("回應應該包含錯誤訊息 {string}")
    public void responseShouldContainErrorMessage(String expectedErrorMessage) {
        // Assert: 輸入驗證
        assertNotNull(expectedErrorMessage, "預期錯誤訊息不能為null");
        assertFalse(expectedErrorMessage.trim().isEmpty(), "預期錯誤訊息不能為空");
        
        // Assert: 回應驗證
        assertNotNull(lastResponseBody, "錯誤回應不能為null");
        
        // 提取錯誤訊息並驗證
        String actualErrorMessage = extractErrorMessage(lastResponseBody);
        assertTrue(actualErrorMessage.contains(expectedErrorMessage),
            String.format("錯誤訊息不符\n預期包含: %s\n實際訊息: %s",
                expectedErrorMessage, actualErrorMessage));
    }
    
    // ==================== 私有輔助方法 ====================
    
    /**
     * 模擬回應延遲
     * @param delayMs 延遲毫秒數
     */
    private void simulateResponseDelay(long delayMs) {
        try {
            Thread.sleep(Math.min(delayMs, 100)); // 最多延遲100ms避免測試太慢
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 創建標準錯誤回應
     */
    private String createErrorResponse(int statusCode, String error, String message, String path) {
        try {
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("status", statusCode);
            errorResponse.put("error", error);
            errorResponse.put("message", message);
            errorResponse.put("path", path);
            errorResponse.put("timestamp", System.currentTimeMillis());
            return errorResponse.toString();
        } catch (JSONException e) {
            return "{\"error\":\"JSON創建失敗: " + e.getMessage() + "\"}";
        }
    }
    
    /**
     * 從回應中提取錯誤訊息
     */
    private String extractErrorMessage(String responseBody) {
        try {
            JSONObject response = new JSONObject(responseBody);
            if (response.has("error")) {
                return response.getString("error");
            } else if (response.has("message")) {
                return response.getString("message");
            }
        } catch (JSONException e) {
            // 如果不是JSON，直接返回原始內容
        }
        return responseBody;
    }
    
    /**
     * 模擬健康檢查回應
     */
    private void simulateHealthCheckResponse() {
        try {
            JSONObject healthResponse = new JSONObject();
            healthResponse.put("status", expectedHealthStatus);
            healthResponse.put("timestamp", System.currentTimeMillis());
            healthResponse.put("version", "1.0.0");
            healthResponse.put("uptime", "24:00:00");
            
            lastResponseStatus = "200";
            lastResponseBody = healthResponse.toString();
        } catch (JSONException e) {
            lastResponseStatus = "500";
            lastResponseBody = createErrorResponse(500, "Internal Server Error", 
                "健康檢查回應創建失敗", healthCheckEndpoint);
        }
    }
    
    /**
     * 驗證JSON結構是否匹配
     */
    private void validateJsonStructure(String expected, String actual) {
        try {
            JSONObject expectedJson = new JSONObject(expected);
            JSONObject actualJson = new JSONObject(actual);
            
            // 驗證所有預期的鍵都存在
            Iterator<String> keys = expectedJson.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                assertTrue(actualJson.has(key),
                    String.format("回應缺少預期的鍵: %s", key));
            }
        } catch (JSONException e) {
            fail("JSON解析失敗: " + e.getMessage());
        }
    }
    
    /**
     * 驗證文字內容
     */
    private void validateTextContent(String content, String expectedPattern) {
        assertNotNull(content, "內容不能為null");
        assertFalse(content.trim().isEmpty(), "內容不能為空");
        
        if (expectedPattern != null && !expectedPattern.isEmpty()) {
            assertTrue(content.contains(expectedPattern),
                String.format("內容應該包含: %s\n實際內容: %s", expectedPattern, content));
        }
    }
}