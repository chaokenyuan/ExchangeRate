package com.exchangerate.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConversionRequest DTO 驗證測試")
class ConversionRequestTest {

    // 語意化欄位命名 - Given 狀態
    private Validator givenValidator;
    private ConversionRequest givenValidRequest;
    private ConversionRequest givenInvalidRequest;
    private ConversionRequest givenRequestWithNullFromCurrency;
    private ConversionRequest givenRequestWithEmptyFromCurrency;
    private ConversionRequest givenRequestWithShortFromCurrency;
    private ConversionRequest givenRequestWithLongFromCurrency;
    private ConversionRequest givenRequestWithNullToCurrency;
    private ConversionRequest givenRequestWithEmptyToCurrency;
    private ConversionRequest givenRequestWithShortToCurrency;
    private ConversionRequest givenRequestWithNullAmount;
    private ConversionRequest givenRequestWithZeroAmount;
    private ConversionRequest givenRequestWithNegativeAmount;
    private ConversionRequest givenRequestWithMinimumAmount;

    // 語意化欄位命名 - When 執行結果
    private Set<ConstraintViolation<ConversionRequest>> whenValidationResult;

    // 語意化欄位命名 - Then 驗證資料
    private String thenExpectedErrorMessage;
    private int thenExpectedViolationCount;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        givenValidator = factory.getValidator();

        givenValidRequest = new ConversionRequest();
        givenValidRequest.setFromCurrency("USD");
        givenValidRequest.setToCurrency("EUR");
        givenValidRequest.setAmount(new BigDecimal("100"));
    }

    @Nested
    @DisplayName("有效資料驗證")
    class ValidDataTests {

        @Test
        @DisplayName("GIVEN: 有效的轉換請求 WHEN: 執行驗證 THEN: 應該通過驗證")
        void shouldPassValidationForValidConversionRequest() {
            // Given - 準備有效的請求資料
            givenValidConversionRequest();

            // When - 執行驗證
            whenValidatingConversionRequest();

            // Then - 驗證結果應該通過
            thenShouldPassValidationWithoutErrors();
        }

        @Test
        @DisplayName("GIVEN: 最小有效金額 WHEN: 執行驗證 THEN: 應該通過驗證")
        void shouldPassValidationForMinimumValidAmount() {
            // Given - 準備最小有效金額請求
            givenConversionRequestWithMinimumValidAmount();

            // When - 執行驗證
            whenValidatingMinimumAmountRequest();

            // Then - 驗證結果應該通過
            thenShouldPassValidationWithoutErrors();
        }

        // Given 輔助方法 - 準備有效的轉換請求
        private void givenValidConversionRequest() {
            // 使用已在 setUp 中準備的 givenValidRequest
        }

        // Given 輔助方法 - 準備最小有效金額請求
        private void givenConversionRequestWithMinimumValidAmount() {
            givenRequestWithMinimumAmount = new ConversionRequest();
            givenRequestWithMinimumAmount.setFromCurrency("USD");
            givenRequestWithMinimumAmount.setToCurrency("EUR");
            givenRequestWithMinimumAmount.setAmount(new BigDecimal("0.01"));
        }

        // When 輔助方法 - 執行有效請求驗證
        private void whenValidatingConversionRequest() {
            whenValidationResult = givenValidator.validate(givenValidRequest);
        }

        // When 輔助方法 - 執行最小金額請求驗證
        private void whenValidatingMinimumAmountRequest() {
            whenValidationResult = givenValidator.validate(givenRequestWithMinimumAmount);
        }

        // Then 輔助方法 - 驗證應該通過且無錯誤
        private void thenShouldPassValidationWithoutErrors() {
            assertThat(whenValidationResult).isEmpty();
        }
    }

    @Nested
    @DisplayName("來源貨幣驗證")
    class FromCurrencyValidationTests {

        @Test
        @DisplayName("GIVEN: 來源貨幣為空 WHEN: 執行驗證 THEN: 應該驗證失敗")
        void shouldFailValidationForNullFromCurrency() {
            // Given - 準備空來源貨幣請求
            givenConversionRequestWithNullFromCurrency();

            // When - 執行驗證
            whenValidatingNullFromCurrencyRequest();

            // Then - 驗證應該失敗並回傳錯誤訊息
            thenShouldFailValidationWithMessage("來源貨幣為必填欄位");
        }

        @Test
        @DisplayName("GIVEN: 來源貨幣為空字串 WHEN: 執行驗證 THEN: 應該驗證失敗")
        void shouldFailValidationForEmptyFromCurrency() {
            // Given - 準備空字串來源貨幣請求
            givenConversionRequestWithEmptyFromCurrency();

            // When - 執行驗證
            whenValidatingEmptyFromCurrencyRequest();

            // Then - 驗證應該失敗並回傳錯誤訊息
            thenShouldFailValidationWithMessage("來源貨幣為必填欄位");
        }

        @Test
        @DisplayName("GIVEN: 來源貨幣長度不足 WHEN: 執行驗證 THEN: 應該驗證失敗")
        void shouldFailValidationForShortFromCurrency() {
            // Given - 準備長度不足的來源貨幣請求
            givenConversionRequestWithShortFromCurrency();

            // When - 執行驗證
            whenValidatingShortFromCurrencyRequest();

            // Then - 驗證應該失敗並回傳錯誤訊息
            thenShouldFailValidationWithMessage("貨幣代碼必須為3個字元");
        }

        @Test
        @DisplayName("GIVEN: 來源貨幣長度過長 WHEN: 執行驗證 THEN: 應該驗證失敗")
        void shouldFailValidationForLongFromCurrency() {
            // Given - 準備長度過長的來源貨幣請求
            givenConversionRequestWithLongFromCurrency();

            // When - 執行驗證
            whenValidatingLongFromCurrencyRequest();

            // Then - 驗證應該失敗並回傳錯誤訊息
            thenShouldFailValidationWithMessage("貨幣代碼必須為3個字元");
        }

        // Given 輔助方法 - 準備空來源貨幣請求
        private void givenConversionRequestWithNullFromCurrency() {
            givenRequestWithNullFromCurrency = new ConversionRequest();
            givenRequestWithNullFromCurrency.setFromCurrency(null);
            givenRequestWithNullFromCurrency.setToCurrency("EUR");
            givenRequestWithNullFromCurrency.setAmount(new BigDecimal("100"));
        }

        // Given 輔助方法 - 準備空字串來源貨幣請求
        private void givenConversionRequestWithEmptyFromCurrency() {
            givenRequestWithEmptyFromCurrency = createRequestWithFromCurrency("");
        }

        // Given 輔助方法 - 準備短來源貨幣請求
        private void givenConversionRequestWithShortFromCurrency() {
            givenRequestWithShortFromCurrency = createRequestWithFromCurrency("US");
        }

        // Given 輔助方法 - 準備長來源貨幣請求
        private void givenConversionRequestWithLongFromCurrency() {
            givenRequestWithLongFromCurrency = createRequestWithFromCurrency("USDD");
        }

        // When 輔助方法 - 執行空來源貨幣驗證
        private void whenValidatingNullFromCurrencyRequest() {
            whenValidationResult = givenValidator.validate(givenRequestWithNullFromCurrency);
        }

        // When 輔助方法 - 執行空字串來源貨幣驗證
        private void whenValidatingEmptyFromCurrencyRequest() {
            whenValidationResult = givenValidator.validate(givenRequestWithEmptyFromCurrency);
        }

        // When 輔助方法 - 執行短來源貨幣驗證
        private void whenValidatingShortFromCurrencyRequest() {
            whenValidationResult = givenValidator.validate(givenRequestWithShortFromCurrency);
        }

        // When 輔助方法 - 執行長來源貨幣驗證
        private void whenValidatingLongFromCurrencyRequest() {
            whenValidationResult = givenValidator.validate(givenRequestWithLongFromCurrency);
        }

        // Then 輔助方法 - 驗證應該失敗並包含特定訊息
        private void thenShouldFailValidationWithMessage(String expectedMessage) {
            thenExpectedErrorMessage = expectedMessage;
            assertThat(whenValidationResult).isNotEmpty();
            assertThat(whenValidationResult).anyMatch(v -> v.getMessage().equals(thenExpectedErrorMessage));
        }
    }

    @Nested
    @DisplayName("目標貨幣驗證")
    class ToCurrencyValidationTests {

        @Test
        @DisplayName("GIVEN: 目標貨幣為空 WHEN: 執行驗證 THEN: 應該驗證失敗")
        void shouldFailValidationForNullToCurrency() {
            // Given - 準備空目標貨幣請求
            givenConversionRequestWithNullToCurrency();

            // When - 執行驗證
            whenValidatingNullToCurrencyRequest();

            // Then - 驗證應該失敗並回傳錯誤訊息
            thenShouldFailValidationWithTargetCurrencyMessage("目標貨幣為必填欄位");
        }

        @Test
        @DisplayName("GIVEN: 目標貨幣為空字串 WHEN: 執行驗證 THEN: 應該驗證失敗")
        void shouldFailValidationForEmptyToCurrency() {
            // Given - 準備空字串目標貨幣請求
            givenConversionRequestWithEmptyToCurrency();

            // When - 執行驗證
            whenValidatingEmptyToCurrencyRequest();

            // Then - 驗證應該失敗並回傳錯誤訊息
            thenShouldFailValidationWithTargetCurrencyMessage("目標貨幣為必填欄位");
        }

        @Test
        @DisplayName("GIVEN: 目標貨幣長度不足 WHEN: 執行驗證 THEN: 應該驗證失敗")
        void shouldFailValidationForShortToCurrency() {
            // Given - 準備長度不足的目標貨幣請求
            givenConversionRequestWithShortToCurrency();

            // When - 執行驗證
            whenValidatingShortToCurrencyRequest();

            // Then - 驗證應該失敗並回傳錯誤訊息
            thenShouldFailValidationWithTargetCurrencyMessage("貨幣代碼必須為3個字元");
        }

        // Given 輔助方法 - 準備空目標貨幣請求
        private void givenConversionRequestWithNullToCurrency() {
            givenRequestWithNullToCurrency = createRequestWithToCurrency(null);
        }

        // Given 輔助方法 - 準備空字串目標貨幣請求
        private void givenConversionRequestWithEmptyToCurrency() {
            givenRequestWithEmptyToCurrency = createRequestWithToCurrency("");
        }

        // Given 輔助方法 - 準備短目標貨幣請求
        private void givenConversionRequestWithShortToCurrency() {
            givenRequestWithShortToCurrency = createRequestWithToCurrency("EU");
        }

        // When 輔助方法 - 執行空目標貨幣驗證
        private void whenValidatingNullToCurrencyRequest() {
            whenValidationResult = givenValidator.validate(givenRequestWithNullToCurrency);
        }

        // When 輔助方法 - 執行空字串目標貨幣驗證
        private void whenValidatingEmptyToCurrencyRequest() {
            whenValidationResult = givenValidator.validate(givenRequestWithEmptyToCurrency);
        }

        // When 輔助方法 - 執行短目標貨幣驗證
        private void whenValidatingShortToCurrencyRequest() {
            whenValidationResult = givenValidator.validate(givenRequestWithShortToCurrency);
        }

        // Then 輔助方法 - 驗證目標貨幣錯誤
        private void thenShouldFailValidationWithTargetCurrencyMessage(String expectedMessage) {
            thenExpectedErrorMessage = expectedMessage;
            assertThat(whenValidationResult).isNotEmpty();
            assertThat(whenValidationResult).anyMatch(v -> v.getMessage().equals(thenExpectedErrorMessage));
        }
    }

    @Nested
    @DisplayName("金額驗證")
    class AmountValidationTests {

        @Test
        @DisplayName("GIVEN: 金額為空 WHEN: 執行驗證 THEN: 應該驗證失敗")
        void shouldFailValidationForNullAmount() {
            // Given - 準備空金額請求
            givenConversionRequestWithNullAmount();

            // When - 執行驗證
            whenValidatingNullAmountRequest();

            // Then - 驗證應該失敗並回傳錯誤訊息
            thenShouldFailValidationWithAmountMessage("金額為必填欄位");
        }

        @Test
        @DisplayName("GIVEN: 金額為零 WHEN: 執行驗證 THEN: 應該驗證失敗")
        void shouldFailValidationForZeroAmount() {
            // Given - 準備零金額請求
            givenConversionRequestWithZeroAmount();

            // When - 執行驗證
            whenValidatingZeroAmountRequest();

            // Then - 驗證應該失敗並回傳錯誤訊息
            thenShouldFailValidationWithAmountMessage("金額必須大於0");
        }

        @Test
        @DisplayName("GIVEN: 金額為負數 WHEN: 執行驗證 THEN: 應該驗證失敗")
        void shouldFailValidationForNegativeAmount() {
            // Given - 準備負數金額請求
            givenConversionRequestWithNegativeAmount();

            // When - 執行驗證
            whenValidatingNegativeAmountRequest();

            // Then - 驗證應該失敗並回傳錯誤訊息
            thenShouldFailValidationWithAmountMessage("金額必須大於0");
        }

        // Given 輔助方法 - 準備空金額請求
        private void givenConversionRequestWithNullAmount() {
            givenRequestWithNullAmount = createRequestWithAmount(null);
        }

        // Given 輔助方法 - 準備零金額請求
        private void givenConversionRequestWithZeroAmount() {
            givenRequestWithZeroAmount = createRequestWithAmount(BigDecimal.ZERO);
        }

        // Given 輔助方法 - 準備負數金額請求
        private void givenConversionRequestWithNegativeAmount() {
            givenRequestWithNegativeAmount = createRequestWithAmount(new BigDecimal("-100"));
        }

        // When 輔助方法 - 執行空金額驗證
        private void whenValidatingNullAmountRequest() {
            whenValidationResult = givenValidator.validate(givenRequestWithNullAmount);
        }

        // When 輔助方法 - 執行零金額驗證
        private void whenValidatingZeroAmountRequest() {
            whenValidationResult = givenValidator.validate(givenRequestWithZeroAmount);
        }

        // When 輔助方法 - 執行負數金額驗證
        private void whenValidatingNegativeAmountRequest() {
            whenValidationResult = givenValidator.validate(givenRequestWithNegativeAmount);
        }

        // Then 輔助方法 - 驗證金額錯誤
        private void thenShouldFailValidationWithAmountMessage(String expectedMessage) {
            thenExpectedErrorMessage = expectedMessage;
            assertThat(whenValidationResult).isNotEmpty();
            assertThat(whenValidationResult).anyMatch(v -> v.getMessage().equals(thenExpectedErrorMessage));
        }
    }

    @Nested
    @DisplayName("複合驗證測試")
    class MultipleValidationTests {

        @Test
        @DisplayName("GIVEN: 多個欄位無效 WHEN: 執行驗證 THEN: 應該返回多個錯誤")
        void shouldReturnMultipleErrorsForMultipleInvalidFields() {
            // Given - 準備多個無效欄位的請求
            givenConversionRequestWithMultipleInvalidFields();

            // When - 執行多欄位驗證
            whenValidatingMultipleInvalidFieldsRequest();

            // Then - 驗證應該回傳多個錯誤
            thenShouldReturnMultipleValidationErrors();
        }

        // Given 輔助方法 - 準備多個無效欄位請求
        private void givenConversionRequestWithMultipleInvalidFields() {
            givenInvalidRequest = new ConversionRequest();
            givenInvalidRequest.setFromCurrency("");
            givenInvalidRequest.setToCurrency(null);
            givenInvalidRequest.setAmount(BigDecimal.ZERO);
        }

        // When 輔助方法 - 執行多欄位驗證
        private void whenValidatingMultipleInvalidFieldsRequest() {
            whenValidationResult = givenValidator.validate(givenInvalidRequest);
        }

        // Then 輔助方法 - 驗證多個錯誤
        private void thenShouldReturnMultipleValidationErrors() {
            thenExpectedViolationCount = 3;
            assertThat(whenValidationResult).hasSizeGreaterThanOrEqualTo(thenExpectedViolationCount);
        }
    }

    // Helper methods for Given clauses
    private ConversionRequest createRequestWithFromCurrency(String fromCurrency) {
        ConversionRequest request = new ConversionRequest();
        request.setFromCurrency(fromCurrency);
        request.setToCurrency("EUR");
        request.setAmount(new BigDecimal("100"));
        return request;
    }

    private ConversionRequest createRequestWithToCurrency(String toCurrency) {
        ConversionRequest request = new ConversionRequest();
        request.setFromCurrency("USD");
        request.setToCurrency(toCurrency);
        request.setAmount(new BigDecimal("100"));
        return request;
    }

    private ConversionRequest createRequestWithAmount(BigDecimal amount) {
        ConversionRequest request = new ConversionRequest();
        request.setFromCurrency("USD");
        request.setToCurrency("EUR");
        request.setAmount(amount);
        return request;
    }
}