package com.exchangerate.infrastructure.adapter.in.web;

import com.exchangerate.service.ExchangeRateService;
import com.exchangerate.dto.ConversionRequest;
import com.exchangerate.dto.ConversionResponse;
import com.exchangerate.exception.GlobalExceptionHandler;
import com.exchangerate.exception.ResourceNotFoundException;
import com.exchangerate.constants.EnglishErrorMessages;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ConversionController 單元測試
 * 
 * 測試範圍：
 * 1. 成功的貨幣換算
 * 2. 輸入驗證錯誤
 * 3. 業務邏輯錯誤處理
 * 4. HTTP狀態碼驗證
 * 5. JSON回應格式驗證
 */
@WebMvcTest(com.exchangerate.controller.ConversionController.class)
@ContextConfiguration(classes = {com.exchangerate.controller.ConversionController.class, GlobalExceptionHandler.class})
@DisplayName("ConversionController 單元測試")
class ConversionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExchangeRateService exchangeRateService;

    @Autowired
    private ObjectMapper objectMapper;

    private ConversionResponse successResponse;
    private Map<String, Object> validRequest;

    @BeforeEach
    void setUp() {
        // 準備成功回應
        successResponse = ConversionResponse.builder()
                .fromCurrency("USD")
                .toCurrency("TWD")
                .fromAmount(new BigDecimal("100"))
                .toAmount(new BigDecimal("3250"))
                .rate(new BigDecimal("32.5"))
                .conversionDate(LocalDateTime.of(2024, 1, 15, 10, 0, 0))
                .build();

        // 準備有效請求
        validRequest = new HashMap<>();
        validRequest.put("from_currency", "USD");
        validRequest.put("to_currency", "TWD");
        validRequest.put("amount", 100);
    }

    @Nested
    @DisplayName("成功貨幣換算測試")
    class SuccessfulConversionTests {

        @Test
        @DisplayName("應該成功進行貨幣換算")
        void shouldConvertCurrencySuccessfully() throws Exception {
            // Given
            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenReturn(successResponse);

            // When & Then
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.fromCurrency").value("USD"))
                    .andExpect(jsonPath("$.toCurrency").value("TWD"))
                    .andExpect(jsonPath("$.fromAmount").value(100))
                    .andExpect(jsonPath("$.toAmount").value(3250))
                    .andExpect(jsonPath("$.rate").value(32.5))
                    .andExpect(jsonPath("$.conversionDate").exists());
        }

        @Test
        @DisplayName("應該正確處理小數金額")
        void shouldHandleDecimalAmounts() throws Exception {
            // Given
            validRequest.put("amount", 100.50);
            ConversionResponse decimalResponse = ConversionResponse.builder()
                    .fromCurrency("USD")
                    .toCurrency("TWD")
                    .fromAmount(new BigDecimal("100.50"))
                    .toAmount(new BigDecimal("3266.25"))
                    .rate(new BigDecimal("32.5"))
                    .conversionDate(LocalDateTime.now())
                    .build();

            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenReturn(decimalResponse);

            // When & Then
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fromAmount").value(100.50))
                    .andExpect(jsonPath("$.toAmount").value(3266.25));
        }
    }

    @Nested
    @DisplayName("輸入驗證錯誤測試")
    class InputValidationErrorTests {

        @Test
        @DisplayName("應該拒絕空的來源貨幣")
        void shouldRejectEmptyFromCurrency() throws Exception {
            // Given
            validRequest.put("from_currency", "");

            // When & Then
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("應該拒絕空的目標貨幣")
        void shouldRejectEmptyToCurrency() throws Exception {
            // Given
            validRequest.put("to_currency", "");

            // When & Then
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("應該拒絕零金額")
        void shouldRejectZeroAmount() throws Exception {
            // Given
            validRequest.put("amount", 0);

            // When & Then
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("應該拒絕負數金額")
        void shouldRejectNegativeAmount() throws Exception {
            // Given
            validRequest.put("amount", -100);

            // When & Then
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("應該拒絕相同的來源和目標貨幣")
        void shouldRejectSameCurrencies() throws Exception {
            // Given
            validRequest.put("from_currency", "USD");
            validRequest.put("to_currency", "USD");

            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenThrow(new IllegalArgumentException(EnglishErrorMessages.SAME_CURRENCY_ERROR));

            // When & Then
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value(EnglishErrorMessages.SAME_CURRENCY_ERROR));
        }

        @Test
        @DisplayName("應該拒絕無效的貨幣代碼")
        void shouldRejectInvalidCurrencyCode() throws Exception {
            // Given - 使用 XXX 代替 INVALID，因為長度符合但是無效的貨幣代碼
            validRequest.put("from_currency", "XXX");

            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenThrow(new IllegalArgumentException("Unsupported currency code: XXX"));

            // When & Then
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Unsupported currency code: XXX"));
        }
    }

    @Nested
    @DisplayName("業務邏輯錯誤測試")
    class BusinessLogicErrorTests {

        @Test
        @DisplayName("應該處理找不到匯率的情況")
        void shouldHandleExchangeRateNotFound() throws Exception {
            // Given
            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenThrow(new ResourceNotFoundException(EnglishErrorMessages.RATE_NOT_FOUND_ERROR));

            // When & Then
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value(EnglishErrorMessages.RATE_NOT_FOUND_ERROR));
        }

        @Test
        @DisplayName("應該處理服務內部錯誤")
        void shouldHandleServiceInternalError() throws Exception {
            // Given
            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenThrow(new RuntimeException("Internal service error"));

            // When & Then
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }
    }

    @Nested
    @DisplayName("HTTP方法和內容類型測試")
    class HttpMethodAndContentTypeTests {

        @Test
        @DisplayName("應該只接受POST請求")
        void shouldOnlyAcceptPostRequests() throws Exception {
            // When & Then - GET請求應該返回405
            mockMvc.perform(get("/api/convert"))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("應該要求JSON內容類型")
        void shouldRequireJsonContentType() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.TEXT_PLAIN)
                    .content("plain text"))
                    .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        @DisplayName("應該拒絕格式錯誤的JSON")
        void shouldRejectMalformedJson() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{invalid json"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("回應格式測試")
    class ResponseFormatTests {

        @Test
        @DisplayName("成功回應應該包含所有必要字段")
        void successResponseShouldContainAllRequiredFields() throws Exception {
            // Given
            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenReturn(successResponse);

            // When & Then
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fromCurrency").exists())
                    .andExpect(jsonPath("$.toCurrency").exists())
                    .andExpect(jsonPath("$.fromAmount").exists())
                    .andExpect(jsonPath("$.toAmount").exists())
                    .andExpect(jsonPath("$.rate").exists())
                    .andExpect(jsonPath("$.conversionDate").exists());
        }

        @Test
        @DisplayName("錯誤回應應該包含錯誤訊息")
        void errorResponseShouldContainErrorMessage() throws Exception {
            // Given
            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenThrow(new IllegalArgumentException("Test error message"));

            // When & Then
            mockMvc.perform(post("/api/convert")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Test error message"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }
}