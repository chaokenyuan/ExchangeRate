package com.exchangerate.controller;

import com.exchangerate.dto.ConversionRequest;
import com.exchangerate.dto.ConversionResponse;
import com.exchangerate.service.ExchangeRateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * ConversionController 單元測試
 * 
 * 測試範圍：
 * - 成功的貨幣轉換場景
 * - 錯誤處理和異常情況
 * - 邊界條件和驗證
 * - 響應格式和狀態碼
 * 
 * 測試原則：
 * - 使用 Given-When-Then 語意化方法包裝
 * - 清晰的測試方法命名
 * - 適當的測試分組和嵌套
 * - 全面的邊界條件覆蓋
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConversionController 單元測試")
class ConversionControllerTest {

    // Constants for test data
    private static final String USD_CURRENCY = "USD";
    private static final String EUR_CURRENCY = "EUR";
    private static final String TWD_CURRENCY = "TWD";
    private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("100.00");
    private static final BigDecimal DEFAULT_RATE = new BigDecimal("0.85");
    private static final BigDecimal DEFAULT_CONVERTED_AMOUNT = new BigDecimal("85.000000");

    @Mock
    private ExchangeRateService exchangeRateService;

    @InjectMocks
    private ConversionController conversionController;

    // 語意化欄位命名 - Given 狀態
    private ObjectMapper givenObjectMapper;
    private ConversionRequest givenValidRequest;
    private ConversionResponse givenValidResponse;
    private ConversionRequest givenRequestWithPath;
    private ConversionResponse givenResponseWithPath;
    private ConversionRequest givenInvalidRequest;
    private ConversionRequest givenBoundaryRequest;
    private ConversionResponse givenBoundaryResponse;

    // 語意化欄位命名 - When 執行結果
    private ResponseEntity<?> whenControllerResponse;

    // 語意化欄位命名 - Then 驗證資料
    private ConversionResponse thenExpectedConversionResponse;
    private Map<String, String> thenExpectedErrorResponse;

    @BeforeEach
    void setUp() {
        givenObjectMapper = new ObjectMapper();
        
        // Setup valid request
        givenValidRequest = createConversionRequest(USD_CURRENCY, EUR_CURRENCY, DEFAULT_AMOUNT);
        
        // Setup valid response
        givenValidResponse = ConversionResponse.builder()
                .fromCurrency(USD_CURRENCY)
                .toCurrency(EUR_CURRENCY)
                .fromAmount(DEFAULT_AMOUNT)
                .toAmount(DEFAULT_CONVERTED_AMOUNT)
                .rate(DEFAULT_RATE)
                .conversionDate(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("成功轉換場景")
    class SuccessfulConversionTests {

        @Test
        @DisplayName("GIVEN: 有效的轉換請求 WHEN: 執行貨幣轉換 THEN: 應該回傳成功的轉換結果")
        void shouldSuccessfullyConvertCurrency() {
            // Given - 準備測試資料
            givenValidConversionRequestAndExpectedResponse();
            
            // When - 執行被測方法
            whenExecutingCurrencyConversion();
            
            // Then - 驗證結果
            thenShouldReturnSuccessfulConversionResponse();
        }

        @Test
        @DisplayName("GIVEN: 帶轉換路徑的請求 WHEN: 執行轉換 THEN: 應該包含轉換路徑資訊")
        void shouldHandleResponseWithConversionPath() {
            // Given - 準備帶路徑的測試資料
            givenConversionRequestWithPathAndExpectedResponse();
            
            // When - 執行轉換
            whenExecutingConversionWithPath();
            
            // Then - 驗證路徑資訊
            thenShouldReturnResponseWithConversionPath();
        }

        @ParameterizedTest(name = "轉換 {0} 到 {1}")
        @MethodSource("provideCurrencyPairs")
        @DisplayName("GIVEN: 不同貨幣對 WHEN: 執行轉換 THEN: 應該成功處理各種貨幣對")
        void shouldConvertDifferentCurrencyPairs(String from, String to, BigDecimal expectedRate) {
            // Given - 準備不同貨幣對資料
            givenDifferentCurrencyPairRequest(from, to, expectedRate);
            
            // When - 執行轉換
            whenExecutingCurrencyPairConversion();
            
            // Then - 驗證貨幣對轉換結果
            thenShouldReturnCorrectCurrencyPairResult(from, to);
        }

        // Given 輔助方法 - 準備成功轉換的測試資料
        private void givenValidConversionRequestAndExpectedResponse() {
            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenReturn(givenValidResponse);
        }

        // Given 輔助方法 - 準備帶轉換路徑的測試資料
        private void givenConversionRequestWithPathAndExpectedResponse() {
            givenResponseWithPath = ConversionResponse.builder()
                    .fromCurrency("EUR")
                    .toCurrency("JPY")
                    .fromAmount(DEFAULT_AMOUNT)
                    .toAmount(new BigDecimal("12980.000000"))
                    .rate(new BigDecimal("129.80"))
                    .conversionDate(LocalDateTime.now())
                    .conversionPath("EUR→USD→JPY")
                    .build();

            givenRequestWithPath = createConversionRequest("EUR", "JPY", DEFAULT_AMOUNT);

            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenReturn(givenResponseWithPath);
        }

        // Given 輔助方法 - 準備不同貨幣對的測試資料
        private void givenDifferentCurrencyPairRequest(String from, String to, BigDecimal expectedRate) {
            givenValidRequest = createConversionRequest(from, to, DEFAULT_AMOUNT);
            thenExpectedConversionResponse = ConversionResponse.builder()
                    .fromCurrency(from)
                    .toCurrency(to)
                    .fromAmount(DEFAULT_AMOUNT)
                    .toAmount(DEFAULT_AMOUNT.multiply(expectedRate))
                    .rate(expectedRate)
                    .conversionDate(LocalDateTime.now())
                    .build();

            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenReturn(thenExpectedConversionResponse);
        }

        // When 輔助方法 - 執行基本貨幣轉換
        private void whenExecutingCurrencyConversion() {
            whenControllerResponse = conversionController.convertCurrency(givenValidRequest);
        }

        // When 輔助方法 - 執行帶路徑的轉換
        private void whenExecutingConversionWithPath() {
            whenControllerResponse = conversionController.convertCurrency(givenRequestWithPath);
        }

        // When 輔助方法 - 執行貨幣對轉換
        private void whenExecutingCurrencyPairConversion() {
            whenControllerResponse = conversionController.convertCurrency(givenValidRequest);
        }

        // Then 輔助方法 - 驗證成功的轉換響應
        private void thenShouldReturnSuccessfulConversionResponse() {
            assertThat(whenControllerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(whenControllerResponse.getBody()).isNotNull();
            
            ConversionResponse conversionResponse = (ConversionResponse) whenControllerResponse.getBody();
            assertThat(conversionResponse.getFromCurrency()).isEqualTo(USD_CURRENCY);
            assertThat(conversionResponse.getToCurrency()).isEqualTo(EUR_CURRENCY);
            assertThat(conversionResponse.getFromAmount()).isEqualByComparingTo(DEFAULT_AMOUNT);
            assertThat(conversionResponse.getToAmount()).isEqualByComparingTo(DEFAULT_CONVERTED_AMOUNT);
            assertThat(conversionResponse.getRate()).isEqualByComparingTo(DEFAULT_RATE);
            assertThat(conversionResponse.getConversionDate()).isNotNull();
        }

        // Then 輔助方法 - 驗證帶路徑的響應
        private void thenShouldReturnResponseWithConversionPath() {
            assertThat(whenControllerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            ConversionResponse conversionResponse = (ConversionResponse) whenControllerResponse.getBody();
            assertThat(conversionResponse.getConversionPath()).isEqualTo("EUR→USD→JPY");
        }

        // Then 輔助方法 - 驗證貨幣對轉換結果
        private void thenShouldReturnCorrectCurrencyPairResult(String from, String to) {
            assertThat(whenControllerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            ConversionResponse result = (ConversionResponse) whenControllerResponse.getBody();
            assertThat(result.getFromCurrency()).isEqualTo(from);
            assertThat(result.getToCurrency()).isEqualTo(to);
        }

        private static Stream<Arguments> provideCurrencyPairs() {
            return Stream.of(
                Arguments.of("USD", "EUR", new BigDecimal("0.85")),
                Arguments.of("EUR", "USD", new BigDecimal("1.18")),
                Arguments.of("USD", "JPY", new BigDecimal("110.25")),
                Arguments.of("GBP", "USD", new BigDecimal("1.25"))
            );
        }
    }

    @Nested
    @DisplayName("錯誤處理測試")
    class ErrorHandlingTests {

        @ParameterizedTest(name = "錯誤訊息: {0}")
        @ValueSource(strings = {
            "Source and target currencies cannot be the same",
            "Unsupported currency code: XXX",
            "No exchange rate found for conversion",
            "System error"
        })
        @DisplayName("GIVEN: 服務層異常 WHEN: 執行轉換 THEN: 應該回傳適當錯誤響應")
        void shouldHandleServiceExceptions(String errorMessage) {
            // Given - 準備異常情況
            givenServiceExceptionWithMessage(errorMessage);
            
            // When - 執行轉換
            whenExecutingConversionWithException();
            
            // Then - 驗證錯誤響應
            thenShouldReturnErrorResponse(errorMessage);
        }

        @Test
        @DisplayName("GIVEN: 相同貨幣錯誤 WHEN: 執行轉換 THEN: 應該回傳相同貨幣錯誤訊息")
        void shouldHandleSameCurrencyError() {
            // Given - 準備相同貨幣錯誤
            givenSameCurrencyError();
            
            // When - 執行轉換
            whenExecutingConversionWithSameCurrencyError();
            
            // Then - 驗證相同貨幣錯誤響應
            thenShouldReturnSameCurrencyErrorResponse();
        }

        @Test
        @DisplayName("GIVEN: 不支援貨幣錯誤 WHEN: 執行轉換 THEN: 應該回傳不支援貨幣錯誤訊息")
        void shouldHandleUnsupportedCurrencyError() {
            // Given - 準備不支援貨幣錯誤
            givenUnsupportedCurrencyError();
            
            // When - 執行轉換
            whenExecutingConversionWithUnsupportedCurrency();
            
            // Then - 驗證不支援貨幣錯誤響應
            thenShouldReturnUnsupportedCurrencyErrorResponse();
        }

        @Test
        @DisplayName("GIVEN: 找不到匯率錯誤 WHEN: 執行轉換 THEN: 應該回傳找不到匯率錯誤訊息")
        void shouldHandleExchangeRateNotFoundError() {
            // Given - 準備找不到匯率錯誤
            givenExchangeRateNotFoundError();
            
            // When - 執行轉換
            whenExecutingConversionWithRateNotFound();
            
            // Then - 驗證找不到匯率錯誤響應
            thenShouldReturnExchangeRateNotFoundErrorResponse();
        }

        // Given 輔助方法 - 準備服務異常
        private void givenServiceExceptionWithMessage(String errorMessage) {
            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenThrow(new RuntimeException(errorMessage));
        }

        // Given 輔助方法 - 準備相同貨幣錯誤
        private void givenSameCurrencyError() {
            String expectedErrorMessage = "Source and target currencies cannot be the same";
            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenThrow(new RuntimeException(expectedErrorMessage));
        }

        // Given 輔助方法 - 準備不支援貨幣錯誤
        private void givenUnsupportedCurrencyError() {
            String expectedErrorMessage = "Unsupported currency code: XXX";
            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenThrow(new RuntimeException(expectedErrorMessage));
        }

        // Given 輔助方法 - 準備找不到匯率錯誤
        private void givenExchangeRateNotFoundError() {
            String expectedErrorMessage = "No exchange rate found for conversion";
            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenThrow(new RuntimeException(expectedErrorMessage));
        }

        // When 輔助方法 - 執行有異常的轉換
        private void whenExecutingConversionWithException() {
            whenControllerResponse = conversionController.convertCurrency(givenValidRequest);
        }

        // When 輔助方法 - 執行相同貨幣錯誤的轉換
        private void whenExecutingConversionWithSameCurrencyError() {
            whenControllerResponse = conversionController.convertCurrency(givenValidRequest);
        }

        // When 輔助方法 - 執行不支援貨幣的轉換
        private void whenExecutingConversionWithUnsupportedCurrency() {
            whenControllerResponse = conversionController.convertCurrency(givenValidRequest);
        }

        // When 輔助方法 - 執行找不到匯率的轉換
        private void whenExecutingConversionWithRateNotFound() {
            whenControllerResponse = conversionController.convertCurrency(givenValidRequest);
        }

        // Then 輔助方法 - 驗證錯誤響應
        private void thenShouldReturnErrorResponse(String expectedErrorMessage) {
            assertThat(whenControllerResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(whenControllerResponse.getBody()).isNotNull();
            
            @SuppressWarnings("unchecked")
            Map<String, String> errorBody = (Map<String, String>) whenControllerResponse.getBody();
            assertThat(errorBody).containsEntry("error", expectedErrorMessage);
        }

        // Then 輔助方法 - 驗證相同貨幣錯誤響應
        private void thenShouldReturnSameCurrencyErrorResponse() {
            String expectedErrorMessage = "Source and target currencies cannot be the same";
            thenShouldReturnErrorResponse(expectedErrorMessage);
        }

        // Then 輔助方法 - 驗證不支援貨幣錯誤響應
        private void thenShouldReturnUnsupportedCurrencyErrorResponse() {
            String expectedErrorMessage = "Unsupported currency code: XXX";
            thenShouldReturnErrorResponse(expectedErrorMessage);
        }

        // Then 輔助方法 - 驗證找不到匯率錯誤響應
        private void thenShouldReturnExchangeRateNotFoundErrorResponse() {
            String expectedErrorMessage = "No exchange rate found for conversion";
            thenShouldReturnErrorResponse(expectedErrorMessage);
        }
    }

    @Nested
    @DisplayName("輸入驗證測試")
    class InputValidationTests {

        @Test
        @DisplayName("GIVEN: 空的來源貨幣 WHEN: 執行轉換 THEN: 應該回傳驗證錯誤")
        void shouldHandleEmptyFromCurrency() {
            // Given - 準備空來源貨幣請求
            givenEmptyFromCurrencyRequest();
            
            // When - 執行轉換
            whenExecutingConversionWithEmptyFromCurrency();
            
            // Then - 驗證空來源貨幣錯誤
            thenShouldReturnEmptyFromCurrencyError();
        }

        @Test
        @DisplayName("GIVEN: 空的目標貨幣 WHEN: 執行轉換 THEN: 應該回傳驗證錯誤")
        void shouldHandleEmptyToCurrency() {
            // Given - 準備空目標貨幣請求
            givenEmptyToCurrencyRequest();
            
            // When - 執行轉換
            whenExecutingConversionWithEmptyToCurrency();
            
            // Then - 驗證空目標貨幣錯誤
            thenShouldReturnEmptyToCurrencyError();
        }

        @ParameterizedTest(name = "無效金額: {0}")
        @MethodSource("provideInvalidAmounts")
        @DisplayName("GIVEN: 無效金額 WHEN: 執行轉換 THEN: 應該回傳金額驗證錯誤")
        void shouldHandleInvalidAmounts(BigDecimal invalidAmount, String expectedError) {
            // Given - 準備無效金額請求
            givenInvalidAmountRequest(invalidAmount, expectedError);
            
            // When - 執行轉換
            whenExecutingConversionWithInvalidAmount();
            
            // Then - 驗證無效金額錯誤
            thenShouldReturnInvalidAmountError(expectedError);
        }

        // Given 輔助方法 - 準備空來源貨幣請求
        private void givenEmptyFromCurrencyRequest() {
            givenInvalidRequest = createConversionRequest("", EUR_CURRENCY, DEFAULT_AMOUNT);
            String expectedErrorMessage = "Currency code cannot be null or empty";
            
            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenThrow(new RuntimeException(expectedErrorMessage));
        }

        // Given 輔助方法 - 準備空目標貨幣請求
        private void givenEmptyToCurrencyRequest() {
            givenInvalidRequest = createConversionRequest(USD_CURRENCY, "", DEFAULT_AMOUNT);
            String expectedErrorMessage = "Currency code cannot be null or empty";
            
            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenThrow(new RuntimeException(expectedErrorMessage));
        }

        // Given 輔助方法 - 準備無效金額請求
        private void givenInvalidAmountRequest(BigDecimal invalidAmount, String expectedError) {
            givenInvalidRequest = createConversionRequest(USD_CURRENCY, EUR_CURRENCY, invalidAmount);
            
            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenThrow(new RuntimeException(expectedError));
        }

        // When 輔助方法 - 執行空來源貨幣轉換
        private void whenExecutingConversionWithEmptyFromCurrency() {
            whenControllerResponse = conversionController.convertCurrency(givenInvalidRequest);
        }

        // When 輔助方法 - 執行空目標貨幣轉換
        private void whenExecutingConversionWithEmptyToCurrency() {
            whenControllerResponse = conversionController.convertCurrency(givenInvalidRequest);
        }

        // When 輔助方法 - 執行無效金額轉換
        private void whenExecutingConversionWithInvalidAmount() {
            whenControllerResponse = conversionController.convertCurrency(givenInvalidRequest);
        }

        // Then 輔助方法 - 驗證空來源貨幣錯誤
        private void thenShouldReturnEmptyFromCurrencyError() {
            String expectedErrorMessage = "Currency code cannot be null or empty";
            assertErrorResponse(whenControllerResponse, expectedErrorMessage);
        }

        // Then 輔助方法 - 驗證空目標貨幣錯誤
        private void thenShouldReturnEmptyToCurrencyError() {
            String expectedErrorMessage = "Currency code cannot be null or empty";
            assertErrorResponse(whenControllerResponse, expectedErrorMessage);
        }

        // Then 輔助方法 - 驗證無效金額錯誤
        private void thenShouldReturnInvalidAmountError(String expectedError) {
            assertErrorResponse(whenControllerResponse, expectedError);
        }

        private static Stream<Arguments> provideInvalidAmounts() {
            return Stream.of(
                Arguments.of(null, "Amount must be greater than 0"),
                Arguments.of(BigDecimal.ZERO, "Amount must be greater than 0"),
                Arguments.of(new BigDecimal("-100"), "Amount must be greater than 0"),
                Arguments.of(new BigDecimal("-0.01"), "Amount must be greater than 0")
            );
        }
    }

    @Nested
    @DisplayName("邊界條件測試")
    class BoundaryConditionTests {

        @Test
        @DisplayName("GIVEN: 極小金額 WHEN: 執行轉換 THEN: 應該正確處理極小金額")
        void shouldHandleVerySmallAmount() {
            // Given - 準備極小金額請求
            givenVerySmallAmountRequest();
            
            // When - 執行極小金額轉換
            whenExecutingVerySmallAmountConversion();
            
            // Then - 驗證極小金額轉換結果
            thenShouldReturnVerySmallAmountResult();
        }

        @Test
        @DisplayName("GIVEN: 極大金額 WHEN: 執行轉換 THEN: 應該正確處理極大金額")
        void shouldHandleVeryLargeAmount() {
            // Given - 準備極大金額請求
            givenVeryLargeAmountRequest();
            
            // When - 執行極大金額轉換
            whenExecutingVeryLargeAmountConversion();
            
            // Then - 驗證極大金額轉換結果
            thenShouldReturnVeryLargeAmountResult();
        }

        @Test
        @DisplayName("GIVEN: 高精度小數 WHEN: 執行轉換 THEN: 應該正確處理高精度小數")
        void shouldHandleHighPrecisionDecimals() {
            // Given - 準備高精度小數請求
            givenHighPrecisionDecimalRequest();
            
            // When - 執行高精度小數轉換
            whenExecutingHighPrecisionDecimalConversion();
            
            // Then - 驗證高精度小數轉換結果
            thenShouldReturnHighPrecisionDecimalResult();
        }

        // Given 輔助方法 - 準備極小金額請求
        private void givenVerySmallAmountRequest() {
            BigDecimal verySmallAmount = new BigDecimal("0.01");
            givenBoundaryRequest = createConversionRequest(USD_CURRENCY, EUR_CURRENCY, verySmallAmount);
            givenBoundaryResponse = ConversionResponse.builder()
                    .fromCurrency(USD_CURRENCY)
                    .toCurrency(EUR_CURRENCY)
                    .fromAmount(verySmallAmount)
                    .toAmount(verySmallAmount.multiply(DEFAULT_RATE))
                    .rate(DEFAULT_RATE)
                    .conversionDate(LocalDateTime.now())
                    .build();

            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenReturn(givenBoundaryResponse);
        }

        // Given 輔助方法 - 準備極大金額請求
        private void givenVeryLargeAmountRequest() {
            BigDecimal veryLargeAmount = new BigDecimal("999999999.99");
            givenBoundaryRequest = createConversionRequest(USD_CURRENCY, EUR_CURRENCY, veryLargeAmount);
            givenBoundaryResponse = ConversionResponse.builder()
                    .fromCurrency(USD_CURRENCY)
                    .toCurrency(EUR_CURRENCY)
                    .fromAmount(veryLargeAmount)
                    .toAmount(veryLargeAmount.multiply(DEFAULT_RATE))
                    .rate(DEFAULT_RATE)
                    .conversionDate(LocalDateTime.now())
                    .build();

            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenReturn(givenBoundaryResponse);
        }

        // Given 輔助方法 - 準備高精度小數請求
        private void givenHighPrecisionDecimalRequest() {
            BigDecimal preciseAmount = new BigDecimal("123.456789");
            givenBoundaryRequest = createConversionRequest(USD_CURRENCY, EUR_CURRENCY, preciseAmount);
            givenBoundaryResponse = ConversionResponse.builder()
                    .fromCurrency(USD_CURRENCY)
                    .toCurrency(EUR_CURRENCY)
                    .fromAmount(preciseAmount)
                    .toAmount(new BigDecimal("104.938271")) // 123.456789 * 0.85
                    .rate(DEFAULT_RATE)
                    .conversionDate(LocalDateTime.now())
                    .build();

            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenReturn(givenBoundaryResponse);
        }

        // When 輔助方法 - 執行極小金額轉換
        private void whenExecutingVerySmallAmountConversion() {
            whenControllerResponse = conversionController.convertCurrency(givenBoundaryRequest);
        }

        // When 輔助方法 - 執行極大金額轉換
        private void whenExecutingVeryLargeAmountConversion() {
            whenControllerResponse = conversionController.convertCurrency(givenBoundaryRequest);
        }

        // When 輔助方法 - 執行高精度小數轉換
        private void whenExecutingHighPrecisionDecimalConversion() {
            whenControllerResponse = conversionController.convertCurrency(givenBoundaryRequest);
        }

        // Then 輔助方法 - 驗證極小金額結果
        private void thenShouldReturnVerySmallAmountResult() {
            BigDecimal verySmallAmount = new BigDecimal("0.01");
            assertThat(whenControllerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            ConversionResponse result = (ConversionResponse) whenControllerResponse.getBody();
            assertThat(result.getFromAmount()).isEqualByComparingTo(verySmallAmount);
        }

        // Then 輔助方法 - 驗證極大金額結果
        private void thenShouldReturnVeryLargeAmountResult() {
            BigDecimal veryLargeAmount = new BigDecimal("999999999.99");
            assertThat(whenControllerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            ConversionResponse result = (ConversionResponse) whenControllerResponse.getBody();
            assertThat(result.getFromAmount()).isEqualByComparingTo(veryLargeAmount);
        }

        // Then 輔助方法 - 驗證高精度小數結果
        private void thenShouldReturnHighPrecisionDecimalResult() {
            BigDecimal preciseAmount = new BigDecimal("123.456789");
            assertThat(whenControllerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            ConversionResponse result = (ConversionResponse) whenControllerResponse.getBody();
            assertThat(result.getFromAmount()).isEqualByComparingTo(preciseAmount);
        }
    }

    // Helper methods

    /**
     * 創建ConversionRequest對象的工廠方法
     */
    private ConversionRequest createConversionRequest(String fromCurrency, String toCurrency, BigDecimal amount) {
        ConversionRequest request = new ConversionRequest();
        request.setFromCurrency(fromCurrency);
        request.setToCurrency(toCurrency);
        request.setAmount(amount);
        return request;
    }

    /**
     * 驗證錯誤響應的工具方法
     */
    private void assertErrorResponse(ResponseEntity<?> response, String expectedErrorMessage) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        
        @SuppressWarnings("unchecked")
        Map<String, String> errorBody = (Map<String, String>) response.getBody();
        assertThat(errorBody).isNotNull();
        assertThat(errorBody).containsEntry("error", expectedErrorMessage);
    }
}