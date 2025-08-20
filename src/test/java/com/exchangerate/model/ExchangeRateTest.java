package com.exchangerate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ExchangeRate Model 驗證測試")
class ExchangeRateTest {

    private Validator validator;
    private ExchangeRate validExchangeRate;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        validExchangeRate = new ExchangeRate();
        validExchangeRate.setFromCurrency("USD");
        validExchangeRate.setToCurrency("EUR");
        validExchangeRate.setRate(new BigDecimal("0.85"));
        validExchangeRate.setTimestamp(LocalDateTime.now());
        validExchangeRate.setSource("test");
    }

    @Nested
    @DisplayName("有效資料驗證")
    class ValidDataTests {

        @Test
        @DisplayName("有效的匯率資料應該通過驗證")
        void shouldPassValidationForValidExchangeRate() {
            // When
            Set<ConstraintViolation<ExchangeRate>> violations = validator.validate(validExchangeRate);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("最小有效匯率值應該通過驗證")
        void shouldPassValidationForMinimumValidRate() {
            // Given
            validExchangeRate.setRate(new BigDecimal("0.000001"));

            // When
            Set<ConstraintViolation<ExchangeRate>> violations = validator.validate(validExchangeRate);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("較大的匯率值應該通過驗證")
        void shouldPassValidationForLargeRate() {
            // Given
            validExchangeRate.setRate(new BigDecimal("999999.123456"));

            // When
            Set<ConstraintViolation<ExchangeRate>> violations = validator.validate(validExchangeRate);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("來源貨幣驗證")
    class FromCurrencyValidationTests {

        @Test
        @DisplayName("來源貨幣為空時應該驗證失敗")
        void shouldFailValidationForNullFromCurrency() {
            // Given
            validExchangeRate.setFromCurrency(null);

            // When
            Set<ConstraintViolation<ExchangeRate>> violations = validator.validate(validExchangeRate);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Source currency is required");
        }

        @Test
        @DisplayName("來源貨幣為空字串時應該驗證失敗")
        void shouldFailValidationForEmptyFromCurrency() {
            // Given
            validExchangeRate.setFromCurrency("");

            // When
            Set<ConstraintViolation<ExchangeRate>> violations = validator.validate(validExchangeRate);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> 
                    v.getMessage().equals("Source currency is required"));
        }

        @Test
        @DisplayName("來源貨幣為空白字串時應該驗證失敗")
        void shouldFailValidationForBlankFromCurrency() {
            // Given
            validExchangeRate.setFromCurrency("   ");

            // When
            Set<ConstraintViolation<ExchangeRate>> violations = validator.validate(validExchangeRate);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> 
                    v.getMessage().equals("Source currency is required"));
        }

        @Test
        @DisplayName("來源貨幣長度不足3個字元時應該驗證失敗")
        void shouldFailValidationForShortFromCurrency() {
            // Given
            validExchangeRate.setFromCurrency("US");

            // When
            Set<ConstraintViolation<ExchangeRate>> violations = validator.validate(validExchangeRate);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Currency code must be exactly 3 characters");
        }

        @Test
        @DisplayName("來源貨幣長度超過3個字元時應該驗證失敗")
        void shouldFailValidationForLongFromCurrency() {
            // Given
            validExchangeRate.setFromCurrency("USDD");

            // When
            Set<ConstraintViolation<ExchangeRate>> violations = validator.validate(validExchangeRate);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Currency code must be exactly 3 characters");
        }
    }

    @Nested
    @DisplayName("目標貨幣驗證")
    class ToCurrencyValidationTests {

        @Test
        @DisplayName("目標貨幣為空時應該驗證失敗")
        void shouldFailValidationForNullToCurrency() {
            // Given
            validExchangeRate.setToCurrency(null);

            // When
            Set<ConstraintViolation<ExchangeRate>> violations = validator.validate(validExchangeRate);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Target currency is required");
        }

        @Test
        @DisplayName("目標貨幣為空字串時應該驗證失敗")
        void shouldFailValidationForEmptyToCurrency() {
            // Given
            validExchangeRate.setToCurrency("");

            // When
            Set<ConstraintViolation<ExchangeRate>> violations = validator.validate(validExchangeRate);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> 
                    v.getMessage().equals("Target currency is required"));
        }

        @Test
        @DisplayName("目標貨幣長度不足3個字元時應該驗證失敗")
        void shouldFailValidationForShortToCurrency() {
            // Given
            validExchangeRate.setToCurrency("EU");

            // When
            Set<ConstraintViolation<ExchangeRate>> violations = validator.validate(validExchangeRate);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Currency code must be exactly 3 characters");
        }

        @Test
        @DisplayName("目標貨幣長度超過3個字元時應該驗證失敗")
        void shouldFailValidationForLongToCurrency() {
            // Given
            validExchangeRate.setToCurrency("EURR");

            // When
            Set<ConstraintViolation<ExchangeRate>> violations = validator.validate(validExchangeRate);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Currency code must be exactly 3 characters");
        }
    }

    @Nested
    @DisplayName("匯率驗證")
    class RateValidationTests {

        @Test
        @DisplayName("匯率為空時應該驗證失敗")
        void shouldFailValidationForNullRate() {
            // Given
            validExchangeRate.setRate(null);

            // When
            Set<ConstraintViolation<ExchangeRate>> violations = validator.validate(validExchangeRate);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Exchange rate is required");
        }

        @Test
        @DisplayName("匯率為零時應該驗證失敗")
        void shouldFailValidationForZeroRate() {
            // Given
            validExchangeRate.setRate(BigDecimal.ZERO);

            // When
            Set<ConstraintViolation<ExchangeRate>> violations = validator.validate(validExchangeRate);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Exchange rate must be greater than 0");
        }

        @Test
        @DisplayName("匯率為負數時應該驗證失敗")
        void shouldFailValidationForNegativeRate() {
            // Given
            validExchangeRate.setRate(new BigDecimal("-0.5"));

            // When
            Set<ConstraintViolation<ExchangeRate>> violations = validator.validate(validExchangeRate);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Exchange rate must be greater than 0");
        }
    }

    @Nested
    @DisplayName("多重驗證失敗測試")
    class MultipleValidationFailuresTests {

        @Test
        @DisplayName("多個欄位同時無效時應該返回多個驗證錯誤")
        void shouldReturnMultipleValidationErrors() {
            // Given
            ExchangeRate invalidRate = new ExchangeRate();
            invalidRate.setFromCurrency("");
            invalidRate.setToCurrency(null);
            invalidRate.setRate(BigDecimal.ZERO);

            // When
            Set<ConstraintViolation<ExchangeRate>> violations = validator.validate(invalidRate);

            // Then
            assertThat(violations).hasSizeGreaterThanOrEqualTo(3);
            assertThat(violations).anyMatch(v -> 
                    v.getMessage().contains("Source currency"));
            assertThat(violations).anyMatch(v -> 
                    v.getMessage().contains("Target currency"));
            assertThat(violations).anyMatch(v -> 
                    v.getMessage().contains("Exchange rate"));
        }
    }

    @Nested
    @DisplayName("實體生命週期測試")
    class EntityLifecycleTests {

        @Test
        @DisplayName("PrePersist 回調應該設定時間戳")
        void shouldSetTimestampOnPrePersist() {
            // Given
            ExchangeRate rate = new ExchangeRate();
            rate.setFromCurrency("USD");
            rate.setToCurrency("EUR");
            rate.setRate(new BigDecimal("0.85"));
            // 不設定 timestamp

            // When
            rate.onCreate(); // 手動呼叫 @PrePersist 方法

            // Then
            assertThat(rate.getTimestamp()).isNotNull();
            assertThat(rate.getTimestamp()).isBeforeOrEqualTo(LocalDateTime.now());
        }

        @Test
        @DisplayName("已有時間戳時 PrePersist 不應該覆蓋")
        void shouldNotOverrideExistingTimestampOnPrePersist() {
            // Given
            LocalDateTime existingTimestamp = LocalDateTime.now().minusHours(1);
            ExchangeRate rate = new ExchangeRate();
            rate.setFromCurrency("USD");
            rate.setToCurrency("EUR");
            rate.setRate(new BigDecimal("0.85"));
            rate.setTimestamp(existingTimestamp);

            // When
            rate.onCreate();

            // Then
            assertThat(rate.getTimestamp()).isEqualTo(existingTimestamp);
        }
    }

    @Nested
    @DisplayName("JSON 屬性映射測試")
    class JsonPropertyMappingTests {

        @Test
        @DisplayName("getCreatedAt 應該返回時間戳")
        void shouldReturnTimestampForCreatedAt() {
            // Given
            LocalDateTime testTime = LocalDateTime.now();
            validExchangeRate.setTimestamp(testTime);

            // When
            LocalDateTime createdAt = validExchangeRate.getCreatedAt();

            // Then
            assertThat(createdAt).isEqualTo(testTime);
        }

        @Test
        @DisplayName("getCreatedAt 在時間戳為空時應該返回空值")
        void shouldReturnNullForCreatedAtWhenTimestampIsNull() {
            // Given
            validExchangeRate.setTimestamp(null);

            // When
            LocalDateTime createdAt = validExchangeRate.getCreatedAt();

            // Then
            assertThat(createdAt).isNull();
        }
    }

    @Nested
    @DisplayName("Lombok 功能測試")
    class LombokFunctionalityTests {

        @Test
        @DisplayName("equals 和 hashCode 應該正常工作")
        void shouldWorkWithEqualsAndHashCode() {
            // Given
            ExchangeRate rate1 = new ExchangeRate();
            rate1.setId(1L);
            rate1.setFromCurrency("USD");
            rate1.setToCurrency("EUR");
            rate1.setRate(new BigDecimal("0.85"));
            rate1.setTimestamp(LocalDateTime.now());

            ExchangeRate rate2 = new ExchangeRate();
            rate2.setId(1L);
            rate2.setFromCurrency("USD");
            rate2.setToCurrency("EUR");
            rate2.setRate(new BigDecimal("0.85"));
            rate2.setTimestamp(rate1.getTimestamp());

            // When & Then
            assertThat(rate1).isEqualTo(rate2);
            assertThat(rate1.hashCode()).isEqualTo(rate2.hashCode());
        }

        @Test
        @DisplayName("toString 應該包含所有欄位")
        void shouldIncludeAllFieldsInToString() {
            // Given
            validExchangeRate.setId(1L);

            // When
            String toString = validExchangeRate.toString();

            // Then
            assertThat(toString).contains("id=1");
            assertThat(toString).contains("fromCurrency=USD");
            assertThat(toString).contains("toCurrency=EUR");
            assertThat(toString).contains("rate=0.85");
            assertThat(toString).contains("source=test");
        }

        @Test
        @DisplayName("AllArgsConstructor 應該正常工作")
        void shouldWorkWithAllArgsConstructor() {
            // Given
            LocalDateTime testTime = LocalDateTime.now();

            // When
            ExchangeRate rate = new ExchangeRate(1L, "USD", "EUR", new BigDecimal("0.85"), testTime, "test");

            // Then
            assertThat(rate.getId()).isEqualTo(1L);
            assertThat(rate.getFromCurrency()).isEqualTo("USD");
            assertThat(rate.getToCurrency()).isEqualTo("EUR");
            assertThat(rate.getRate()).isEqualByComparingTo(new BigDecimal("0.85"));
            assertThat(rate.getTimestamp()).isEqualTo(testTime);
            assertThat(rate.getSource()).isEqualTo("test");
        }

        @Test
        @DisplayName("NoArgsConstructor 應該正常工作")
        void shouldWorkWithNoArgsConstructor() {
            // When
            ExchangeRate rate = new ExchangeRate();

            // Then
            assertThat(rate.getId()).isNull();
            assertThat(rate.getFromCurrency()).isNull();
            assertThat(rate.getToCurrency()).isNull();
            assertThat(rate.getRate()).isNull();
            assertThat(rate.getTimestamp()).isNull();
            assertThat(rate.getSource()).isNull();
        }
    }
}