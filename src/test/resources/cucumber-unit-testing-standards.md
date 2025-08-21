# Cucumber BDD 單元測試規範 v3.0

## 概述

本文件定義了Cucumber BDD測試中的單元測試規範，確保所有步驟定義遵循3A模式(Arrange-Act-Assert)和單元測試最佳實踐。

## 1. 單元測試核心原則

### 1.1 FIRST原則

**F**ast (快速)
- 測試必須快速執行
- 避免真實的外部依賴
- 使用模擬和存根

**I**ndependent (獨立)
- 測試之間不能有依賴
- 每個測試必須能單獨執行
- 使用@Before重置狀態

**R**epeatable (可重複)
- 測試結果必須一致
- 不依賴外部環境
- 相同輸入產生相同輸出

**S**elf-Validating (自我驗證)
- 測試必須有明確的通過/失敗結果
- 禁止使用`assertTrue(true)`
- 必須驗證實際行為

**T**imely (及時)
- 測試與程式碼同步開發
- 測試先行(TDD)思維

### 1.2 AAA模式 (Arrange-Act-Assert)

```java
@Test
public void testMethodName() {
    // Arrange - 準備測試資料和環境
    String input = "test data";
    ExpectedObject expected = new ExpectedObject();
    
    // Act - 執行被測試的行為
    ActualObject actual = systemUnderTest.performAction(input);
    
    // Assert - 驗證結果
    assertEquals(expected, actual);
}
```

## 2. Cucumber步驟定義的單元測試規範

### 2.1 Given步驟 - Arrange階段

```java
// ✅ 正確實作
@Given("系統已啟動且API服務正常運作")
public void systemStartedAndAPIServiceRunning() {
    // Arrange: 設置測試前置條件
    systemStarted = true;
    healthCheckEndpoint = "/health";
    expectedHealthStatus = "UP";
    
    // 驗證設置成功
    assertTrue(systemStarted, "系統啟動標記應該為true");
    assertNotNull(healthCheckEndpoint, "健康檢查端點應該已設定");
}

// ❌ 錯誤實作
@Given("系統已啟動")
public void systemStarted() {
    systemStarted = true;  // 沒有驗證
}
```

### 2.2 When步驟 - Act階段

```java
// ✅ 正確實作
@When("我發送GET請求到 {string}")
public void sendRequestTo(String endpoint) {
    // Arrange: 驗證輸入
    assertNotNull(endpoint, "端點不能為空");
    assertFalse(endpoint.trim().isEmpty(), "端點不能為空字串");
    
    // Act: 執行操作
    lastRequestEndpoint = endpoint;
    lastRequestMethod = "GET";
    lastRequestTime = System.currentTimeMillis();
    
    // 模擬請求處理
    processRequest(endpoint);
}

// ❌ 錯誤實作
@When("我發送請求")
public void sendRequest() {
    lastResponseStatus = "200";  // 直接設定結果，沒有實際行為
}
```

### 2.3 Then步驟 - Assert階段

```java
// ✅ 正確實作
@Then("回應狀態碼應該是 {int}")
public void requestStatusCodeShouldBe(Integer expectedStatusCode) {
    // Assert: 前置條件檢查
    assertNotNull(expectedStatusCode, "預期狀態碼不能為null");
    assertNotNull(lastResponseStatus, "實際回應狀態碼不應該為null");
    
    // Assert: 主要驗證
    String expected = String.valueOf(expectedStatusCode);
    assertEquals(expected, lastResponseStatus, 
        String.format("狀態碼不匹配 - 預期: %s, 實際: %s", expected, lastResponseStatus));
}

// ❌ 錯誤實作
@Then("驗證成功")
public void videValidationSucceeded() {
    assertTrue(true);  // 假驗證，永遠通過
}
```

## 3. 測試資料管理

### 3.1 測試資料建構器模式

```java
public class TestDataBuilder {
    // 使用建構器模式創建測試資料
    public static class ExchangeRateBuilder {
        private String fromCurrency = "USD";
        private String toCurrency = "TWD";
        private double rate = 32.5;
        
        public ExchangeRateBuilder withFromCurrency(String currency) {
            this.fromCurrency = currency;
            return this;
        }
        
        public ExchangeRateBuilder withToCurrency(String currency) {
            this.toCurrency = currency;
            return this;
        }
        
        public ExchangeRateBuilder withRate(double rate) {
            this.rate = rate;
            return this;
        }
        
        public Map<String, Object> build() {
            Map<String, Object> data = new HashMap<>();
            data.put("from_currency", fromCurrency);
            data.put("to_currency", toCurrency);
            data.put("rate", rate);
            return data;
        }
    }
}
```

### 3.2 測試資料隔離

```java
@Before
public void setUp() {
    // 每個測試前重置所有狀態
    testContext.clear();
    lastResponseStatus = null;
    lastResponseBody = null;
    mockResponses.clear();
}

@After
public void tearDown() {
    // 清理測試資源
    testContext.clear();
    verifyNoUnusedMocks();
}
```

## 4. 驗證策略

### 4.1 完整性驗證

```java
// ✅ 正確：完整驗證回應內容
@Then("回應應該包含 {int} 筆資料")
public void responseShouldContainNumberOfRecords(Integer expectedCount) {
    // 前置檢查
    assertNotNull(expectedCount, "預期筆數不能為null");
    assertNotNull(lastResponseBody, "回應內容不能為null");
    assertTrue(expectedCount >= 0, "預期筆數必須大於等於0");
    
    // 解析實際資料
    JSONObject response = new JSONObject(lastResponseBody);
    JSONArray dataArray = response.optJSONArray("data");
    assertNotNull(dataArray, "回應應該包含data陣列");
    
    // 驗證筆數
    int actualCount = dataArray.length();
    assertEquals(expectedCount.intValue(), actualCount,
        String.format("資料筆數不符 - 預期: %d, 實際: %d", expectedCount, actualCount));
}

// ❌ 錯誤：假驗證
@Then("回應應該包含資料")
public void responseShouldContainData() {
    assertTrue(true);  // 沒有實際驗證
}
```

### 4.2 邊界條件驗證

```java
@Then("回應時間應該小於 {int} 毫秒")
public void responseTimeShouldBeLessThanMilliseconds(Integer maxMilliseconds) {
    // 邊界檢查
    assertNotNull(maxMilliseconds, "時間限制不能為null");
    assertTrue(maxMilliseconds > 0, "時間限制必須大於0");
    assertTrue(maxMilliseconds <= 10000, "時間限制不合理(>10秒)");
    
    // 實際時間計算
    assertNotNull(lastRequestTime, "請求時間未記錄");
    assertNotNull(lastResponseTime, "回應時間未記錄");
    
    long actualResponseTime = lastResponseTime - lastRequestTime;
    
    // 驗證
    assertTrue(actualResponseTime < maxMilliseconds,
        String.format("回應時間超過限制 - 限制: %dms, 實際: %dms", 
            maxMilliseconds, actualResponseTime));
}
```

## 5. 模擬與存根

### 5.1 模擬外部依賴

```java
public class MockResponseProvider {
    private Map<String, MockResponse> mockResponses = new HashMap<>();
    
    public void setupMockResponse(String endpoint, int statusCode, String body) {
        mockResponses.put(endpoint, new MockResponse(statusCode, body));
    }
    
    public MockResponse getMockResponse(String endpoint) {
        MockResponse response = mockResponses.get(endpoint);
        assertNotNull(response, "未設定端點 " + endpoint + " 的模擬回應");
        return response;
    }
    
    public void verifyAllMocksUsed() {
        // 驗證所有設定的模擬都被使用
        assertTrue(unusedMocks.isEmpty(), 
            "未使用的模擬: " + unusedMocks);
    }
}
```

### 5.2 測試替身類型

```java
// Dummy - 佔位物件
public class DummyUser {
    public String getId() { return "dummy-id"; }
}

// Stub - 返回預設值
public class StubRepository {
    public List<String> findAll() {
        return Arrays.asList("item1", "item2");
    }
}

// Spy - 記錄互動
public class SpyLogger {
    private List<String> logs = new ArrayList<>();
    
    public void log(String message) {
        logs.add(message);
    }
    
    public void verifyLogged(String message) {
        assertTrue(logs.contains(message), 
            "未找到日誌: " + message);
    }
}

// Mock - 預期互動驗證
public class MockService {
    private Map<String, Integer> callCounts = new HashMap<>();
    
    public void recordCall(String method) {
        callCounts.merge(method, 1, Integer::sum);
    }
    
    public void verifyCalledTimes(String method, int times) {
        assertEquals(times, callCounts.getOrDefault(method, 0),
            String.format("方法 %s 呼叫次數不符", method));
    }
}
```

## 6. 異常處理測試

### 6.1 預期異常驗證

```java
@When("我發送無效的請求")
public void iSendInvalidRequest() {
    // Arrange
    String invalidEndpoint = null;
    
    // Act & Assert
    Exception exception = assertThrows(
        IllegalArgumentException.class,
        () -> processRequest(invalidEndpoint),
        "應該拋出IllegalArgumentException"
    );
    
    // 驗證異常訊息
    assertTrue(exception.getMessage().contains("端點不能為空"),
        "異常訊息應該說明問題");
}
```

### 6.2 錯誤場景測試

```java
@Then("應該返回適當的錯誤訊息")
public void shouldReturnAppropriateErrorMessage() {
    // 驗證錯誤狀態
    assertNotNull(lastResponseStatus, "應該有回應狀態");
    assertTrue(Integer.parseInt(lastResponseStatus) >= 400,
        "錯誤狀態碼應該 >= 400");
    
    // 驗證錯誤訊息結構
    assertNotNull(lastResponseBody, "應該有錯誤訊息");
    JSONObject errorResponse = new JSONObject(lastResponseBody);
    
    assertTrue(errorResponse.has("error"), "應該包含error欄位");
    assertTrue(errorResponse.has("timestamp"), "應該包含timestamp");
    assertTrue(errorResponse.has("path"), "應該包含請求路徑");
    
    // 驗證錯誤訊息內容
    String errorMessage = errorResponse.getString("error");
    assertFalse(errorMessage.isEmpty(), "錯誤訊息不應該為空");
    assertFalse(errorMessage.contains("null"), "錯誤訊息不應包含null");
}
```

## 7. 效能測試規範

### 7.1 時間測量

```java
public class PerformanceValidator {
    private long startTime;
    private long endTime;
    
    public void startTimer() {
        startTime = System.nanoTime();
    }
    
    public void stopTimer() {
        endTime = System.nanoTime();
    }
    
    public void assertExecutionTime(long maxMillis) {
        long actualMillis = (endTime - startTime) / 1_000_000;
        assertTrue(actualMillis <= maxMillis,
            String.format("執行時間超過限制: %dms > %dms", 
                actualMillis, maxMillis));
    }
}
```

### 7.2 資源使用驗證

```java
@Then("記憶體使用不應該超過 {int} MB")
public void memoryUsageShouldNotExceedMB(Integer maxMemoryMB) {
    // 取得記憶體使用資訊
    Runtime runtime = Runtime.getRuntime();
    long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
    
    // 驗證
    assertTrue(usedMemory <= maxMemoryMB,
        String.format("記憶體使用超過限制: %dMB > %dMB", 
            usedMemory, maxMemoryMB));
}
```

## 8. 程式碼覆蓋率要求

### 8.1 覆蓋率標準

- **行覆蓋率**: >= 80%
- **分支覆蓋率**: >= 75%
- **方法覆蓋率**: >= 90%
- **類別覆蓋率**: 100%

### 8.2 覆蓋率排除

```java
// 可排除的程式碼
@ExcludeFromCoverage
public class TestConfiguration {
    // 測試配置類別
}

@Generated
public class AutoGeneratedCode {
    // 自動產生的程式碼
}
```

## 9. 測試命名規範

### 9.1 Gherkin步驟與Java方法命名原則

**重要原則**：
- **Gherkin步驟描述**：使用中文（業務語言），便於業務人員理解
- **Java測試方法名稱**：必須使用英文，遵循Java命名規範

### 9.2 Cucumber步驟定義命名模式

```java
// ✅ 正確：中文Gherkin + 英文方法名
@Given("系統已啟動且API服務正常運作")
public void systemStartedAndAPIServiceRunning() {
    // 實作內容
}

@When("我發送GET請求到 {string}")
public void iSendGetRequestTo(String endpoint) {
    // 實作內容
}

@Then("回應狀態碼應該是 {int}")
public void responseStatusCodeShouldBe(Integer statusCode) {
    // 實作內容
}

// ❌ 錯誤：中文方法名
@Given("系統已啟動")
public void 系統已啟動() {
    // 不符合Java命名規範
}
```

### 9.3 單元測試方法命名模式

```java
// Given-When-Then 模式
@Test
public void givenValidInput_whenProcessing_thenReturnsExpectedResult() { }

// Should-When 模式
@Test
public void shouldReturnError_whenInputIsInvalid() { }

// 業務場景描述模式
@Test
public void shouldThrowExceptionWhenInputInvalidCurrencyCode() { }
```

### 9.4 測試類別組織

```java
public class SystemStepDefinitionsTest {
    
    @Nested
    @DisplayName("系統健康檢查")
    class HealthCheckTests {
        @Test
        void shouldReturnHealthyStatusWhenSystemIsNormal() { }
        
        @Test
        void shouldReturnUnhealthyStatusWhenDatabaseIsDown() { }
    }
    
    @Nested
    @DisplayName("回應驗證")
    class ResponseValidationTests {
        @Test
        void shouldCorrectlyValidateStatusCode() { }
        
        @Test
        void shouldCorrectlyParseJsonResponse() { }
    }
}
```

## 10. 違規檢查清單

### 必須避免的反模式

- [ ] ❌ `assertTrue(true)` - 假驗證
- [ ] ❌ `catch (Exception e) {}` - 忽略異常
- [ ] ❌ 硬編碼測試資料在步驟定義中
- [ ] ❌ 測試之間的相依性
- [ ] ❌ 沒有清理的資源使用
- [ ] ❌ 過長的測試方法 (>30行)
- [ ] ❌ 多重斷言without說明
- [ ] ❌ 測試中的生產程式碼邏輯

### 必須實作的模式

- [ ] ✅ AAA模式 (Arrange-Act-Assert)
- [ ] ✅ 單一斷言原則 (或邏輯相關的斷言群組)
- [ ] ✅ 測試隔離 (@Before/@After)
- [ ] ✅ 有意義的斷言訊息
- [ ] ✅ 邊界值測試
- [ ] ✅ 異常場景測試
- [ ] ✅ 效能驗證 (where applicable)
- [ ] ✅ 測試資料建構器

---

**版本**: 3.0
**生效日期**: 2024-08-21
**強制執行**: 所有新測試必須遵循此規範