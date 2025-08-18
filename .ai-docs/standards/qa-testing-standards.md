# QA 測試標準規範

## 測試代碼結構規範

### 語意化方法包裝 (Given-When-Then Pattern)

所有測試方法必須使用語意化的 Given-When-Then 模式來組織代碼，提高可讀性和維護性。

#### 結構要求

```java
@Test
@DisplayName("GIVEN: 條件描述 WHEN: 動作描述 THEN: 期望結果描述")
void testMethodName() {
    // Given - 準備測試數據和前置條件
    givenMethodForSetup();
    
    // When - 執行被測試的操作
    Result result = whenMethodForExecution();
    
    // Then - 驗證結果和後置條件
    thenMethodForVerification(result);
}
```

### 語意化方法包裝規範

#### 方法命名規則
- **Given 區段**: 使用 `givenMethodXXX()` 包裝準備邏輯
- **When 區段**: 使用 `whenMethodXXX()` 包裝執行邏輯
- **Then 區段**: 使用 `thenMethodXXX()` 包裝驗證邏輯

#### 輔助方法命名
- **Given 方法**: 以 `given` 開頭
  - `givenValidExchangeRate()`
  - `givenUserWithPermission()`
  - `givenMockServiceReturns()`

- **When 方法**: 以 `when` 開頭
  - `whenExecutingConversion()`
  - `whenValidatingInput()`
  - `whenCallingService()`

- **Then 方法**: 以 `then` 開頭
  - `thenShouldReturnSuccess()`
  - `thenShouldThrowException()`
  - `thenShouldHaveViolations()`

### 實作範例

#### ✅ 正確的語意化測試結構

```java
@Test
@DisplayName("GIVEN: 有效的匯率 WHEN: 執行儲存 THEN: 資料應該成功儲存")
void shouldConvertCurrencySuccessfully() {
    // Given - 準備測試數據
    givenValidConversionRequestAndMockService();

    // When - 執行轉換操作
    ConversionResponse result = whenExecutingCurrencyConversion();

    // Then - 驗證結果
    thenShouldReturnSuccessfulConversion(result);
}

// Given 輔助方法
private void givenValidConversionRequestAndMockService() {
    validRequest = new ConversionRequest();
    validRequest.setFrom_currency("USD");
    validRequest.setTo_currency("EUR");
    validRequest.setAmount(new BigDecimal("100"));
    
    expectedResponse = createExpectedResponse();
    when(exchangeRateService.convertCurrencyDetailed(any())).thenReturn(expectedResponse);
}

// When 輔助方法
private ConversionResponse whenExecutingCurrencyConversion() {
    return conversionController.convertCurrency(validRequest);
}

// Then 輔助方法  
private void thenShouldReturnSuccessfulConversion(ConversionResponse actual) {
    assertThat(actual.getFrom_currency()).isEqualTo(expectedResponse.getFrom_currency());
    assertThat(actual.getTo_currency()).isEqualTo(expectedResponse.getTo_currency());
    assertThat(actual.getFrom_amount()).isEqualByComparingTo(expectedResponse.getFrom_amount());
}
```

#### ❌ 避免的測試結構

```java
@Test
void testConversion() {
    // ❌ 混雜的代碼結構，沒有語意化方法包裝
    ConversionRequest request = new ConversionRequest();
    request.setFrom_currency("USD");
    when(service.convert(any())).thenReturn(response);
    ConversionResponse result = controller.convert(request);
    assertEquals("EUR", result.getTo_currency());
}

@Test  
void shouldConvertSuccessfully() {
    // ❌ 雖有註解但沒有方法包裝
    // Given
    ConversionRequest request = new ConversionRequest();
    request.setFrom_currency("USD");
    
    // When  
    ConversionResponse result = service.convert(request);
    
    // Then
    assertNotNull(result); // 直接驗證，未封裝
}
```

### 測試組織規範

#### 使用 @Nested 類別分組
```java
@Nested
@DisplayName("成功案例測試")
class SuccessfulOperationTests {
    // 成功案例的測試方法
}

@Nested  
@DisplayName("驗證失敗測試")
class ValidationFailureTests {
    // 驗證失敗的測試方法
}
```

#### 測試方法命名
- 使用 `should` 開頭描述期望行為
- 使用中文 `@DisplayName` 提供詳細說明

```java
@Test
@DisplayName("無效的貨幣代碼應該拋出驗證異常")
void shouldThrowValidationExceptionForInvalidCurrencyCode() {
    // 測試實作
}
```

### 斷言規範

#### 使用 AssertJ 的流暢斷言
```java
// Then 方法中使用 AssertJ
private void thenShouldHaveExpectedViolations(Set<ConstraintViolation<Object>> violations) {
    assertThat(violations)
        .hasSize(1)
        .extracting(ConstraintViolation::getMessage)
        .contains("期望的錯誤訊息");
}
```

#### 異常測試的語意化結構
```java
@Test
@DisplayName("重複的匯率組合應該拋出業務異常")
void shouldThrowBusinessExceptionForDuplicateRatePair() {
    // Given
    ExchangeRate givenExistingRate = createExistingExchangeRate();
    ExchangeRate givenDuplicateRate = createDuplicateExchangeRate();
    
    // When & Then
    RuntimeException whenSavingDuplicate = assertThrows(RuntimeException.class, () -> {
        exchangeRateService.saveExchangeRate(givenDuplicateRate);
    });
    
    thenShouldHaveExpectedErrorMessage(whenSavingDuplicate, "匯率組合已存在");
}
```

### Mock 使用規範

#### Given 階段設定 Mock
```java
@Test
@DisplayName("服務層異常應該被控制器正確處理")
void shouldHandleServiceExceptionInController() {
    // Given - 設定 Mock 行為
    ConversionRequest givenRequest = createValidRequest();
    RuntimeException givenServiceException = new RuntimeException("服務異常");
    when(exchangeRateService.convertCurrencyDetailed(givenRequest))
        .thenThrow(givenServiceException);
    
    // When - 執行控制器方法
    ResponseEntity<?> whenCallingController = conversionController.convertCurrency(givenRequest);
    
    // Then - 驗證錯誤處理
    thenShouldReturnErrorResponse(whenCallingController, "服務異常");
}
```

## 代碼審查檢查清單

### 測試結構檢查
- [ ] 每個測試方法都使用了 Given-When-Then 結構
- [ ] 變數命名遵循語意化規範
- [ ] 使用了適當的輔助方法來封裝複雜邏輯
- [ ] 測試類別使用 @Nested 進行邏輯分組

### 可讀性檢查  
- [ ] @DisplayName 使用中文提供清晰的測試描述
- [ ] 測試方法名稱以 should 開頭描述期望行為
- [ ] 測試邏輯清晰，容易理解測試意圖

### 斷言檢查
- [ ] 使用 AssertJ 的流暢斷言語法
- [ ] 斷言邏輯封裝在 Then 輔助方法中
- [ ] 異常測試使用 assertThrows 進行驗證

### 測試覆蓋檢查
- [ ] 正常路徑和異常路徑都有覆蓋
- [ ] 邊界條件和驗證規則都有測試
- [ ] Mock 使用適當，不過度模擬

## 範例檔案
- `ExchangeRateServiceTest.java` - 服務層測試範例
- `ConversionControllerTest.java` - 控制器測試範例  
- `ConversionRequestTest.java` - DTO 驗證測試範例
- `ExchangeRateTest.java` - 模型驗證測試範例

這些範例展示了完整的語意化測試結構實作，可作為其他測試的參考模板。