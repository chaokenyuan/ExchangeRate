package com.exchangerate.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ConversionRequest DTO 驗證測試")
class ConversionRequestTest {

    private Validator validator;
    private ConversionRequest validRequest;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        validRequest = new ConversionRequest();
        validRequest.setFrom_currency("USD");
        validRequest.setTo_currency("EUR");
        validRequest.setAmount(new BigDecimal("100"));
    }

    @Nested
    @DisplayName("有效資料驗證")
    class ValidDataTests {

        @Test
        @DisplayName("有效的轉換請求應該通過驗證")
        void shouldPassValidationForValidConversionRequest() {
            // Given
            ConversionRequest givenValidRequest = validRequest;

            // When
            Set<ConstraintViolation<ConversionRequest>> whenValidating = validator.validate(givenValidRequest);

            // Then
            thenShouldHaveNoViolations(whenValidating);
        }

        @Test
        @DisplayName("最小有效金額應該通過驗證")
        void shouldPassValidationForMinimumValidAmount() {
            // Given
            ConversionRequest givenRequestWithMinAmount = new ConversionRequest();
            givenRequestWithMinAmount.setFrom_currency("USD");
            givenRequestWithMinAmount.setTo_currency("EUR");
            givenRequestWithMinAmount.setAmount(new BigDecimal("0.01"));

            // When
            Set<ConstraintViolation<ConversionRequest>> whenValidating = validator.validate(givenRequestWithMinAmount);

            // Then
            thenShouldHaveNoViolations(whenValidating);
        }
    }

    @Nested
    @DisplayName("來源貨幣驗證")
    class FromCurrencyValidationTests {

        @Test
        @DisplayName("來源貨幣為空時應該驗證失敗")
        void shouldFailValidationForNullFromCurrency() {
            // Given
            ConversionRequest givenRequestWithNullFromCurrency = new ConversionRequest();
            givenRequestWithNullFromCurrency.setFrom_currency(null);
            givenRequestWithNullFromCurrency.setTo_currency("EUR");
            givenRequestWithNullFromCurrency.setAmount(new BigDecimal("100"));

            // When
            Set<ConstraintViolation<ConversionRequest>> whenValidating = 
                validator.validate(givenRequestWithNullFromCurrency);

            // Then
            thenShouldHaveViolationWithMessage(whenValidating, "來源貨幣為必填欄位");
        }

        @Test
        @DisplayName("來源貨幣為空字串時應該驗證失敗")
        void shouldFailValidationForEmptyFromCurrency() {
            // Given
            ConversionRequest givenRequestWithEmptyFromCurrency = createRequestWithFromCurrency("");

            // When
            Set<ConstraintViolation<ConversionRequest>> whenValidating = 
                validator.validate(givenRequestWithEmptyFromCurrency);

            // Then
            thenShouldHaveViolationWithMessage(whenValidating, "來源貨幣為必填欄位");
        }

        @Test
        @DisplayName("來源貨幣長度不足時應該驗證失敗")
        void shouldFailValidationForShortFromCurrency() {
            // Given
            ConversionRequest givenRequestWithShortFromCurrency = createRequestWithFromCurrency("US");

            // When
            Set<ConstraintViolation<ConversionRequest>> whenValidating = 
                validator.validate(givenRequestWithShortFromCurrency);

            // Then
            thenShouldHaveViolationWithMessage(whenValidating, "貨幣代碼必須為3個字元");
        }

        @Test
        @DisplayName("來源貨幣長度過長時應該驗證失敗")
        void shouldFailValidationForLongFromCurrency() {
            // Given
            ConversionRequest givenRequestWithLongFromCurrency = createRequestWithFromCurrency("USDD");

            // When
            Set<ConstraintViolation<ConversionRequest>> whenValidating = 
                validator.validate(givenRequestWithLongFromCurrency);

            // Then
            thenShouldHaveViolationWithMessage(whenValidating, "貨幣代碼必須為3個字元");
        }
    }

    @Nested
    @DisplayName("目標貨幣驗證")
    class ToCurrencyValidationTests {

        @Test
        @DisplayName("目標貨幣為空時應該驗證失敗")
        void shouldFailValidationForNullToCurrency() {
            // Given
            ConversionRequest givenRequestWithNullToCurrency = createRequestWithToCurrency(null);

            // When
            Set<ConstraintViolation<ConversionRequest>> whenValidating = 
                validator.validate(givenRequestWithNullToCurrency);

            // Then
            thenShouldHaveViolationWithMessage(whenValidating, "目標貨幣為必填欄位");
        }

        @Test
        @DisplayName("目標貨幣為空字串時應該驗證失敗")
        void shouldFailValidationForEmptyToCurrency() {
            // Given
            ConversionRequest givenRequestWithEmptyToCurrency = createRequestWithToCurrency("");

            // When
            Set<ConstraintViolation<ConversionRequest>> whenValidating = 
                validator.validate(givenRequestWithEmptyToCurrency);

            // Then
            thenShouldHaveViolationWithMessage(whenValidating, "目標貨幣為必填欄位");
        }

        @Test
        @DisplayName("目標貨幣長度不足時應該驗證失敗")
        void shouldFailValidationForShortToCurrency() {
            // Given
            ConversionRequest givenRequestWithShortToCurrency = createRequestWithToCurrency("EU");

            // When
            Set<ConstraintViolation<ConversionRequest>> whenValidating = 
                validator.validate(givenRequestWithShortToCurrency);

            // Then
            thenShouldHaveViolationWithMessage(whenValidating, "貨幣代碼必須為3個字元");
        }
    }

    @Nested
    @DisplayName("金額驗證")
    class AmountValidationTests {

        @Test
        @DisplayName("金額為空時應該驗證失敗")
        void shouldFailValidationForNullAmount() {
            // Given
            ConversionRequest givenRequestWithNullAmount = createRequestWithAmount(null);

            // When
            Set<ConstraintViolation<ConversionRequest>> whenValidating = 
                validator.validate(givenRequestWithNullAmount);

            // Then
            thenShouldHaveViolationWithMessage(whenValidating, "金額為必填欄位");
        }

        @Test
        @DisplayName("金額為零時應該驗證失敗")
        void shouldFailValidationForZeroAmount() {
            // Given
            ConversionRequest givenRequestWithZeroAmount = createRequestWithAmount(BigDecimal.ZERO);

            // When
            Set<ConstraintViolation<ConversionRequest>> whenValidating = 
                validator.validate(givenRequestWithZeroAmount);

            // Then
            thenShouldHaveViolationWithMessage(whenValidating, "金額必須大於0");
        }

        @Test
        @DisplayName("金額為負數時應該驗證失敗")
        void shouldFailValidationForNegativeAmount() {
            // Given
            ConversionRequest givenRequestWithNegativeAmount = createRequestWithAmount(new BigDecimal("-100"));

            // When
            Set<ConstraintViolation<ConversionRequest>> whenValidating = 
                validator.validate(givenRequestWithNegativeAmount);

            // Then
            thenShouldHaveViolationWithMessage(whenValidating, "金額必須大於0");
        }
    }

    @Nested
    @DisplayName("複合驗證測試")
    class MultipleValidationTests {

        @Test
        @DisplayName("多個欄位無效時應該返回多個錯誤")
        void shouldReturnMultipleErrorsForMultipleInvalidFields() {
            // Given
            ConversionRequest givenInvalidRequest = new ConversionRequest();
            givenInvalidRequest.setFrom_currency("");
            givenInvalidRequest.setTo_currency(null);
            givenInvalidRequest.setAmount(BigDecimal.ZERO);

            // When
            Set<ConstraintViolation<ConversionRequest>> whenValidating = 
                validator.validate(givenInvalidRequest);

            // Then
            thenShouldHaveMultipleViolations(whenValidating, 3);
        }
    }

    // Helper methods for Given clauses
    private ConversionRequest createRequestWithFromCurrency(String fromCurrency) {
        ConversionRequest request = new ConversionRequest();
        request.setFrom_currency(fromCurrency);
        request.setTo_currency("EUR");
        request.setAmount(new BigDecimal("100"));
        return request;
    }

    private ConversionRequest createRequestWithToCurrency(String toCurrency) {
        ConversionRequest request = new ConversionRequest();
        request.setFrom_currency("USD");
        request.setTo_currency(toCurrency);
        request.setAmount(new BigDecimal("100"));
        return request;
    }

    private ConversionRequest createRequestWithAmount(BigDecimal amount) {
        ConversionRequest request = new ConversionRequest();
        request.setFrom_currency("USD");
        request.setTo_currency("EUR");
        request.setAmount(amount);
        return request;
    }

    // Helper methods for Then clauses
    private void thenShouldHaveNoViolations(Set<ConstraintViolation<ConversionRequest>> violations) {
        assertThat(violations).isEmpty();
    }

    private void thenShouldHaveViolationWithMessage(Set<ConstraintViolation<ConversionRequest>> violations, String expectedMessage) {
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals(expectedMessage));
    }

    private void thenShouldHaveMultipleViolations(Set<ConstraintViolation<ConversionRequest>> violations, int expectedCount) {
        assertThat(violations).hasSizeGreaterThanOrEqualTo(expectedCount);
    }
}