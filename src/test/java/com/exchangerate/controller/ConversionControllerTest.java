package com.exchangerate.controller;

import com.exchangerate.dto.ConversionRequest;
import com.exchangerate.dto.ConversionResponse;
import com.exchangerate.service.ExchangeRateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConversionController.class)
@DisplayName("ConversionController 測試")
class ConversionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExchangeRateService exchangeRateService;

    @Autowired
    private ObjectMapper objectMapper;

    private ConversionRequest validRequest;
    private ConversionResponse validResponse;

    @BeforeEach
    void setUp() {
        validRequest = new ConversionRequest();
        validRequest.setFrom_currency("USD");
        validRequest.setTo_currency("EUR");
        validRequest.setAmount(new BigDecimal("100"));

        validResponse = ConversionResponse.builder()
                .from_currency("USD")
                .to_currency("EUR")
                .from_amount(new BigDecimal("100"))
                .to_amount(new BigDecimal("85.000000"))
                .rate(new BigDecimal("0.85"))
                .conversion_date(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("貨幣轉換 API 測試")
    class ConvertCurrencyTests {

        @Test
        @DisplayName("成功轉換貨幣")
        void shouldConvertCurrencySuccessfully() throws Exception {
            // Given
            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenReturn(validResponse);

            // When & Then
            mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.from_currency").value("USD"))
                    .andExpect(jsonPath("$.to_currency").value("EUR"))
                    .andExpect(jsonPath("$.from_amount").value(100))
                    .andExpect(jsonPath("$.to_amount").value("85.0"))
                    .andExpect(jsonPath("$.rate").value("0.85"))
                    .andExpect(jsonPath("$.conversion_date").exists());
        }

        @Test
        @DisplayName("轉換失敗時返回錯誤訊息")
        void shouldReturnErrorWhenConversionFails() throws Exception {
            // Given
            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenThrow(new RuntimeException("找不到匯率資料進行換算"));

            // When & Then
            mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error").value("找不到匯率資料進行換算"));
        }

        @Test
        @DisplayName("請求體為空時返回驗證錯誤")
        void shouldReturnValidationErrorForEmptyRequestBody() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("來源貨幣為空時返回驗證錯誤")
        void shouldReturnValidationErrorForEmptyFromCurrency() throws Exception {
            // Given
            ConversionRequest invalidRequest = new ConversionRequest();
            invalidRequest.setFrom_currency("");
            invalidRequest.setTo_currency("EUR");
            invalidRequest.setAmount(new BigDecimal("100"));

            // When & Then
            mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("目標貨幣為空時返回驗證錯誤")
        void shouldReturnValidationErrorForEmptyToCurrency() throws Exception {
            // Given
            ConversionRequest invalidRequest = new ConversionRequest();
            invalidRequest.setFrom_currency("USD");
            invalidRequest.setTo_currency("");
            invalidRequest.setAmount(new BigDecimal("100"));

            // When & Then
            mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("金額為空時返回驗證錯誤")
        void shouldReturnValidationErrorForNullAmount() throws Exception {
            // Given
            ConversionRequest invalidRequest = new ConversionRequest();
            invalidRequest.setFrom_currency("USD");
            invalidRequest.setTo_currency("EUR");
            invalidRequest.setAmount(null);

            // When & Then
            mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("負數金額時返回驗證錯誤")
        void shouldReturnValidationErrorForNegativeAmount() throws Exception {
            // Given
            ConversionRequest invalidRequest = new ConversionRequest();
            invalidRequest.setFrom_currency("USD");
            invalidRequest.setTo_currency("EUR");
            invalidRequest.setAmount(new BigDecimal("-100"));

            // When & Then
            mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("零金額時返回驗證錯誤")
        void shouldReturnValidationErrorForZeroAmount() throws Exception {
            // Given
            ConversionRequest invalidRequest = new ConversionRequest();
            invalidRequest.setFrom_currency("USD");
            invalidRequest.setTo_currency("EUR");
            invalidRequest.setAmount(BigDecimal.ZERO);

            // When & Then
            mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("貨幣代碼長度不正確時返回驗證錯誤")
        void shouldReturnValidationErrorForInvalidCurrencyCodeLength() throws Exception {
            // Given
            ConversionRequest invalidRequest = new ConversionRequest();
            invalidRequest.setFrom_currency("US"); // 只有2個字元
            invalidRequest.setTo_currency("EUR");
            invalidRequest.setAmount(new BigDecimal("100"));

            // When & Then
            mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Content-Type不正確時返回錯誤")
        void shouldReturnErrorForIncorrectContentType() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.TEXT_PLAIN)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        @DisplayName("無效的JSON格式時返回錯誤")
        void shouldReturnErrorForInvalidJson() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{ invalid json }"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("帶有轉換路徑的響應測試")
        void shouldReturnResponseWithConversionPath() throws Exception {
            // Given
            ConversionResponse responseWithPath = ConversionResponse.builder()
                    .from_currency("EUR")
                    .to_currency("JPY")
                    .from_amount(new BigDecimal("100"))
                    .to_amount(new BigDecimal("12980.000000"))
                    .rate(new BigDecimal("129.80"))
                    .conversion_date(LocalDateTime.now())
                    .conversion_path("EUR→USD→JPY")
                    .build();

            ConversionRequest requestWithPath = new ConversionRequest();
            requestWithPath.setFrom_currency("EUR");
            requestWithPath.setTo_currency("JPY");
            requestWithPath.setAmount(new BigDecimal("100"));

            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenReturn(responseWithPath);

            // When & Then
            mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithPath)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.from_currency").value("EUR"))
                    .andExpect(jsonPath("$.to_currency").value("JPY"))
                    .andExpect(jsonPath("$.from_amount").value(100))
                    .andExpect(jsonPath("$.to_amount").value("12980.0"))
                    .andExpect(jsonPath("$.rate").value("129.8"))
                    .andExpect(jsonPath("$.conversion_path").value("EUR→USD→JPY"))
                    .andExpect(jsonPath("$.conversion_date").exists());
        }
    }

    @Nested
    @DisplayName("錯誤處理測試")
    class ErrorHandlingTests {

        @Test
        @DisplayName("相同貨幣錯誤處理")
        void shouldHandleSameCurrencyError() throws Exception {
            // Given
            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenThrow(new RuntimeException("來源與目標貨幣不可相同"));

            // When & Then
            mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("來源與目標貨幣不可相同"));
        }

        @Test
        @DisplayName("不支援貨幣錯誤處理")
        void shouldHandleUnsupportedCurrencyError() throws Exception {
            // Given
            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenThrow(new RuntimeException("不支援的貨幣代碼: XXX"));

            // When & Then
            mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("不支援的貨幣代碼: XXX"));
        }

        @Test
        @DisplayName("服務層一般異常錯誤處理")
        void shouldHandleGenericServiceException() throws Exception {
            // Given
            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenThrow(new RuntimeException("系統錯誤"));

            // When & Then
            mockMvc.perform(post("/api/convert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("系統錯誤"));
        }
    }
}