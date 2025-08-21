package com.exchangerate.controller;

import com.exchangerate.dto.ConversionRequest;
import com.exchangerate.dto.ConversionResponse;
import com.exchangerate.service.ExchangeRateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ConversionController 單元測試
 * 
 * 測試範圍：
 * - 成功的貨幣轉換場景
 * - 錯誤處理和異常情況
 * - 邊界條件和驗證
 * - 響應格式和狀態碼
 * 
 * 使用 @WebMvcTest 進行 Web 層測試
 * 使用 MockMvc 測試 HTTP 請求和響應
 */
@WebMvcTest(ConversionController.class)
@DisplayName("ConversionController 單元測試")
class ConversionControllerTest {

    // Constants for test data
    private static final String USD_CURRENCY = "USD";
    private static final String EUR_CURRENCY = "EUR";
    private static final String TWD_CURRENCY = "TWD";
    private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("100.00");
    private static final BigDecimal DEFAULT_RATE = new BigDecimal("0.85");
    private static final BigDecimal DEFAULT_CONVERTED_AMOUNT = new BigDecimal("85.000000");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExchangeRateService exchangeRateService;

    private ConversionRequest validRequest;
    private ConversionResponse validResponse;

    @BeforeEach
    void setUp() {
        // Setup valid request
        validRequest = createConversionRequest(USD_CURRENCY, EUR_CURRENCY, DEFAULT_AMOUNT);
        
        // Setup valid response
        validResponse = ConversionResponse.builder()
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
        @DisplayName("應該成功執行貨幣轉換")
        void shouldSuccessfullyConvertCurrency() throws Exception {
            // Given
            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenReturn(validResponse);
            
            // When & Then
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.fromCurrency").value(USD_CURRENCY))
                    .andExpect(jsonPath("$.toCurrency").value(EUR_CURRENCY))
                    .andExpect(jsonPath("$.fromAmount").value(DEFAULT_AMOUNT.doubleValue()))
                    .andExpect(jsonPath("$.toAmount").value(DEFAULT_CONVERTED_AMOUNT.doubleValue()))
                    .andExpect(jsonPath("$.rate").value(DEFAULT_RATE.doubleValue()))
                    .andExpect(jsonPath("$.conversionDate").exists());
        }

        @Test
        @DisplayName("應該正確處理帶轉換路徑的響應")
        void shouldHandleResponseWithConversionPath() throws Exception {
            // Given
            ConversionResponse responseWithPath = ConversionResponse.builder()
                    .fromCurrency("EUR")
                    .toCurrency("JPY")
                    .fromAmount(DEFAULT_AMOUNT)
                    .toAmount(new BigDecimal("12980.000000"))
                    .rate(new BigDecimal("129.80"))
                    .conversionDate(LocalDateTime.now())
                    .conversionPath("EUR→USD→JPY")
                    .build();

            ConversionRequest requestWithPath = createConversionRequest("EUR", "JPY", DEFAULT_AMOUNT);

            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenReturn(responseWithPath);

            // When & Then
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestWithPath)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.conversionPath").value("EUR→USD→JPY"));
        }

        @ParameterizedTest(name = "轉換 {0} 到 {1}")
        @MethodSource("provideCurrencyPairs")
        @DisplayName("應該成功處理各種貨幣對")
        void shouldConvertDifferentCurrencyPairs(String from, String to, BigDecimal expectedRate) throws Exception {
            // Given
            ConversionRequest request = createConversionRequest(from, to, DEFAULT_AMOUNT);
            ConversionResponse response = ConversionResponse.builder()
                    .fromCurrency(from)
                    .toCurrency(to)
                    .fromAmount(DEFAULT_AMOUNT)
                    .toAmount(DEFAULT_AMOUNT.multiply(expectedRate))
                    .rate(expectedRate)
                    .conversionDate(LocalDateTime.now())
                    .build();

            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenReturn(response);

            // When & Then
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fromCurrency").value(from))
                    .andExpect(jsonPath("$.toCurrency").value(to));
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
        @DisplayName("應該正確處理服務層異常")
        void shouldHandleServiceExceptions(String errorMessage) throws Exception {
            // Given
            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenThrow(new RuntimeException(errorMessage));

            // When & Then
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value(errorMessage));
        }

        @Test
        @DisplayName("應該處理相同貨幣錯誤")
        void shouldHandleSameCurrencyError() throws Exception {
            // Given
            ConversionRequest sameCurrencyRequest = createConversionRequest(USD_CURRENCY, USD_CURRENCY, DEFAULT_AMOUNT);
            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenThrow(new RuntimeException("Source and target currencies cannot be the same"));

            // When & Then
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sameCurrencyRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Source and target currencies cannot be the same"));
        }

        @Test
        @DisplayName("應該處理不支援的貨幣代碼")
        void shouldHandleUnsupportedCurrencyError() throws Exception {
            // Given
            ConversionRequest invalidCurrencyRequest = createConversionRequest("XXX", EUR_CURRENCY, DEFAULT_AMOUNT);
            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenThrow(new RuntimeException("Unsupported currency code: XXX"));

            // When & Then
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidCurrencyRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Unsupported currency code: XXX"));
        }

        @Test
        @DisplayName("應該處理找不到匯率的情況")
        void shouldHandleExchangeRateNotFoundError() throws Exception {
            // Given
            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenThrow(new RuntimeException("No exchange rate found for conversion"));

            // When & Then
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("No exchange rate found for conversion"));
        }
    }

    @Nested
    @DisplayName("輸入驗證測試")
    class InputValidationTests {

        @Test
        @DisplayName("應該驗證必要欄位")
        void shouldValidateRequiredFields() throws Exception {
            // When & Then - Missing fromCurrency
            String missingFromCurrency = "{\"toCurrency\":\"EUR\",\"amount\":100}";
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(missingFromCurrency))
                    .andExpect(status().isBadRequest());

            // When & Then - Missing toCurrency
            String missingToCurrency = "{\"fromCurrency\":\"USD\",\"amount\":100}";
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(missingToCurrency))
                    .andExpect(status().isBadRequest());

            // When & Then - Missing amount
            String missingAmount = "{\"fromCurrency\":\"USD\",\"toCurrency\":\"EUR\"}";
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(missingAmount))
                    .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @MethodSource("provideInvalidAmounts")
        @DisplayName("應該驗證無效金額")
        void shouldValidateInvalidAmounts(String amount, String expectedError) throws Exception {
            // Given
            String requestJson = String.format(
                "{\"fromCurrency\":\"USD\",\"toCurrency\":\"EUR\",\"amount\":%s}", 
                amount
            );

            // When & Then
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isBadRequest());
        }

        private static Stream<Arguments> provideInvalidAmounts() {
            return Stream.of(
                Arguments.of("0", "Amount must be greater than 0"),
                Arguments.of("-100", "Amount must be greater than 0"),
                Arguments.of("-0.01", "Amount must be greater than 0")
            );
        }

        @Test
        @DisplayName("應該驗證貨幣代碼格式")
        void shouldValidateCurrencyCodeFormat() throws Exception {
            // When & Then - Invalid currency code length
            String invalidCurrencyLength = "{\"fromCurrency\":\"US\",\"toCurrency\":\"EUR\",\"amount\":100}";
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidCurrencyLength))
                    .andExpect(status().isBadRequest());

            // When & Then - Currency code with numbers
            String currencyWithNumbers = "{\"fromCurrency\":\"US1\",\"toCurrency\":\"EUR\",\"amount\":100}";
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(currencyWithNumbers))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("邊界條件測試")
    class BoundaryConditionTests {

        @Test
        @DisplayName("應該處理極小金額")
        void shouldHandleVerySmallAmount() throws Exception {
            // Given
            BigDecimal verySmallAmount = new BigDecimal("0.01");
            ConversionRequest request = createConversionRequest(USD_CURRENCY, EUR_CURRENCY, verySmallAmount);
            ConversionResponse response = ConversionResponse.builder()
                    .fromCurrency(USD_CURRENCY)
                    .toCurrency(EUR_CURRENCY)
                    .fromAmount(verySmallAmount)
                    .toAmount(verySmallAmount.multiply(DEFAULT_RATE))
                    .rate(DEFAULT_RATE)
                    .conversionDate(LocalDateTime.now())
                    .build();

            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenReturn(response);

            // When & Then
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fromAmount").value(verySmallAmount.doubleValue()));
        }

        @Test
        @DisplayName("應該處理極大金額")
        void shouldHandleVeryLargeAmount() throws Exception {
            // Given
            BigDecimal veryLargeAmount = new BigDecimal("999999999.99");
            ConversionRequest request = createConversionRequest(USD_CURRENCY, EUR_CURRENCY, veryLargeAmount);
            ConversionResponse response = ConversionResponse.builder()
                    .fromCurrency(USD_CURRENCY)
                    .toCurrency(EUR_CURRENCY)
                    .fromAmount(veryLargeAmount)
                    .toAmount(veryLargeAmount.multiply(DEFAULT_RATE))
                    .rate(DEFAULT_RATE)
                    .conversionDate(LocalDateTime.now())
                    .build();

            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenReturn(response);

            // When & Then
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fromAmount").value(veryLargeAmount.doubleValue()));
        }

        @Test
        @DisplayName("應該處理高精度小數")
        void shouldHandleHighPrecisionDecimals() throws Exception {
            // Given
            BigDecimal preciseAmount = new BigDecimal("123.456789");
            ConversionRequest request = createConversionRequest(USD_CURRENCY, EUR_CURRENCY, preciseAmount);
            ConversionResponse response = ConversionResponse.builder()
                    .fromCurrency(USD_CURRENCY)
                    .toCurrency(EUR_CURRENCY)
                    .fromAmount(preciseAmount)
                    .toAmount(new BigDecimal("104.938271"))
                    .rate(DEFAULT_RATE)
                    .conversionDate(LocalDateTime.now())
                    .build();

            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenReturn(response);

            // When & Then
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fromAmount").value(preciseAmount.doubleValue()))
                    .andExpect(jsonPath("$.toAmount").value(104.938271));
        }
    }

    @Nested
    @DisplayName("HTTP 相關測試")
    class HttpRelatedTests {

        @Test
        @DisplayName("應該拒絕非 JSON 內容類型")
        void shouldRejectNonJsonContentType() throws Exception {
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.TEXT_PLAIN)
                    .content("plain text"))
                    .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        @DisplayName("應該處理空請求體")
        void shouldHandleEmptyRequestBody() throws Exception {
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("應該處理格式錯誤的 JSON")
        void shouldHandleMalformedJson() throws Exception {
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{invalid json}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("應該設置正確的響應頭")
        void shouldSetCorrectResponseHeaders() throws Exception {
            // Given
            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenReturn(validResponse);

            // When & Then
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE));
        }
    }

    // Helper methods

    /**
     * 創建 ConversionRequest 對象的工廠方法
     */
    private ConversionRequest createConversionRequest(String fromCurrency, String toCurrency, BigDecimal amount) {
        ConversionRequest request = new ConversionRequest();
        request.setFromCurrency(fromCurrency);
        request.setToCurrency(toCurrency);
        request.setAmount(amount);
        return request;
    }
}