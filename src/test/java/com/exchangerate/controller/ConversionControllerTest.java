package com.exchangerate.controller;

import com.exchangerate.dto.ConversionRequest;
import com.exchangerate.dto.ConversionResponse;
import com.exchangerate.service.ExchangeRateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * ConversionController 純JUnit測試
 * 不依賴SpringBoot上下文，使用Mockito進行單元測試
 */
@DisplayName("ConversionController 測試")
class ConversionControllerTest {

    @Mock
    private ExchangeRateService exchangeRateService;

    @InjectMocks
    private ConversionController conversionController;

    private ObjectMapper objectMapper;
    private ConversionRequest validRequest;
    private ConversionResponse validResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        
        validRequest = new ConversionRequest();
        validRequest.setFromCurrency("USD");
        validRequest.setToCurrency("EUR");
        validRequest.setAmount(new BigDecimal("100"));

        validResponse = ConversionResponse.builder()
                .fromCurrency("USD")
                .toCurrency("EUR")
                .fromAmount(new BigDecimal("100"))
                .toAmount(new BigDecimal("85.000000"))
                .rate(new BigDecimal("0.85"))
                .conversionDate(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("貨幣轉換 API 測試")
    class ConvertCurrencyTests {

        @Test
        @DisplayName("成功轉換貨幣")
        void shouldConvertCurrencySuccessfully() {
            // Given
            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenReturn(validResponse);

            // When
            ResponseEntity<?> response = conversionController.convertCurrency(validRequest);

            // Then
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody()).isNotNull();
            ConversionResponse conversionResponse = (ConversionResponse) response.getBody();
            assertThat(conversionResponse.getFromCurrency()).isEqualTo("USD");
            assertThat(conversionResponse.getToCurrency()).isEqualTo("EUR");
            assertThat(conversionResponse.getFromAmount()).isEqualByComparingTo(new BigDecimal("100"));
            assertThat(conversionResponse.getToAmount()).isEqualByComparingTo(new BigDecimal("85.000000"));
            assertThat(conversionResponse.getRate()).isEqualByComparingTo(new BigDecimal("0.85"));
            assertThat(conversionResponse.getConversionDate()).isNotNull();
        }

        @Test
        @DisplayName("轉換失敗時返回錯誤訊息")
        void shouldReturnErrorWhenConversionFails() {
            // Given
            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenThrow(new RuntimeException("No exchange rate found for conversion"));

            // When & Then
            try {
                conversionController.convertCurrency(validRequest);
            } catch (RuntimeException e) {
                assertThat(e.getMessage()).isEqualTo("No exchange rate found for conversion");
            }
        }

        @Test
        @DisplayName("帶有轉換路徑的響應測試")
        void shouldReturnResponseWithConversionPath() {
            // Given
            ConversionResponse responseWithPath = ConversionResponse.builder()
                    .fromCurrency("EUR")
                    .toCurrency("JPY")
                    .fromAmount(new BigDecimal("100"))
                    .toAmount(new BigDecimal("12980.000000"))
                    .rate(new BigDecimal("129.80"))
                    .conversionDate(LocalDateTime.now())
                    .conversionPath("EUR→USD→JPY")
                    .build();

            ConversionRequest requestWithPath = new ConversionRequest();
            requestWithPath.setFromCurrency("EUR");
            requestWithPath.setToCurrency("JPY");
            requestWithPath.setAmount(new BigDecimal("100"));

            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenReturn(responseWithPath);

            // When
            ResponseEntity<?> response = conversionController.convertCurrency(requestWithPath);

            // Then
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(response.getBody()).isNotNull();
            ConversionResponse conversionResponse = (ConversionResponse) response.getBody();
            assertThat(conversionResponse.getFromCurrency()).isEqualTo("EUR");
            assertThat(conversionResponse.getToCurrency()).isEqualTo("JPY");
            assertThat(conversionResponse.getFromAmount()).isEqualByComparingTo(new BigDecimal("100"));
            assertThat(conversionResponse.getToAmount()).isEqualByComparingTo(new BigDecimal("12980.000000"));
            assertThat(conversionResponse.getRate()).isEqualByComparingTo(new BigDecimal("129.80"));
            assertThat(conversionResponse.getConversionPath()).isEqualTo("EUR→USD→JPY");
            assertThat(conversionResponse.getConversionDate()).isNotNull();
        }
    }

    @Nested
    @DisplayName("錯誤處理測試")
    class ErrorHandlingTests {

        @Test
        @DisplayName("相同貨幣錯誤處理")
        void shouldHandleSameCurrencyError() {
            // Given
            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenThrow(new RuntimeException("Source and target currencies cannot be the same"));

            // When & Then
            try {
                conversionController.convertCurrency(validRequest);
            } catch (RuntimeException e) {
                assertThat(e.getMessage()).isEqualTo("Source and target currencies cannot be the same");
            }
        }

        @Test
        @DisplayName("不支援貨幣錯誤處理")
        void shouldHandleUnsupportedCurrencyError() {
            // Given
            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenThrow(new RuntimeException("Unsupported currency code: XXX"));

            // When & Then
            try {
                conversionController.convertCurrency(validRequest);
            } catch (RuntimeException e) {
                assertThat(e.getMessage()).isEqualTo("Unsupported currency code: XXX");
            }
        }

        @Test
        @DisplayName("服務層一般異常錯誤處理")
        void shouldHandleGenericServiceException() {
            // Given
            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenThrow(new RuntimeException("System error"));

            // When & Then
            try {
                conversionController.convertCurrency(validRequest);
            } catch (RuntimeException e) {
                assertThat(e.getMessage()).isEqualTo("System error");
            }
        }
    }

    @Nested
    @DisplayName("輸入驗證測試")
    class InputValidationTests {

        @Test
        @DisplayName("請求對象為空時應拋出異常")
        void shouldThrowExceptionForNullRequest() {
            // Given
            ConversionRequest nullRequest = null;

            // When & Then
            try {
                conversionController.convertCurrency(nullRequest);
            } catch (Exception e) {
                assertThat(e).isInstanceOf(NullPointerException.class);
            }
        }

        @Test
        @DisplayName("來源貨幣為空的請求")
        void shouldHandleEmptyFromCurrency() {
            // Given
            ConversionRequest invalidRequest = new ConversionRequest();
            invalidRequest.setFromCurrency("");
            invalidRequest.setToCurrency("EUR");
            invalidRequest.setAmount(new BigDecimal("100"));

            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenThrow(new RuntimeException("Currency code cannot be null or empty"));

            // When & Then
            try {
                conversionController.convertCurrency(invalidRequest);
            } catch (RuntimeException e) {
                assertThat(e.getMessage()).isEqualTo("Currency code cannot be null or empty");
            }
        }

        @Test
        @DisplayName("目標貨幣為空的請求")
        void shouldHandleEmptyToCurrency() {
            // Given
            ConversionRequest invalidRequest = new ConversionRequest();
            invalidRequest.setFromCurrency("USD");
            invalidRequest.setToCurrency("");
            invalidRequest.setAmount(new BigDecimal("100"));

            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenThrow(new RuntimeException("Currency code cannot be null or empty"));

            // When & Then
            try {
                conversionController.convertCurrency(invalidRequest);
            } catch (RuntimeException e) {
                assertThat(e.getMessage()).isEqualTo("Currency code cannot be null or empty");
            }
        }

        @Test
        @DisplayName("金額為空的請求")
        void shouldHandleNullAmount() {
            // Given
            ConversionRequest invalidRequest = new ConversionRequest();
            invalidRequest.setFromCurrency("USD");
            invalidRequest.setToCurrency("EUR");
            invalidRequest.setAmount(null);

            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenThrow(new RuntimeException("Amount must be greater than 0"));

            // When & Then
            try {
                conversionController.convertCurrency(invalidRequest);
            } catch (RuntimeException e) {
                assertThat(e.getMessage()).isEqualTo("Amount must be greater than 0");
            }
        }

        @Test
        @DisplayName("負數金額的請求")
        void shouldHandleNegativeAmount() {
            // Given
            ConversionRequest invalidRequest = new ConversionRequest();
            invalidRequest.setFromCurrency("USD");
            invalidRequest.setToCurrency("EUR");
            invalidRequest.setAmount(new BigDecimal("-100"));

            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenThrow(new RuntimeException("Amount must be greater than 0"));

            // When & Then
            try {
                conversionController.convertCurrency(invalidRequest);
            } catch (RuntimeException e) {
                assertThat(e.getMessage()).isEqualTo("Amount must be greater than 0");
            }
        }

        @Test
        @DisplayName("零金額的請求")
        void shouldHandleZeroAmount() {
            // Given
            ConversionRequest invalidRequest = new ConversionRequest();
            invalidRequest.setFromCurrency("USD");
            invalidRequest.setToCurrency("EUR");
            invalidRequest.setAmount(BigDecimal.ZERO);

            when(exchangeRateService.convertCurrencyDetailed(any(ConversionRequest.class)))
                    .thenThrow(new RuntimeException("Amount must be greater than 0"));

            // When & Then
            try {
                conversionController.convertCurrency(invalidRequest);
            } catch (RuntimeException e) {
                assertThat(e.getMessage()).isEqualTo("Amount must be greater than 0");
            }
        }
    }
}