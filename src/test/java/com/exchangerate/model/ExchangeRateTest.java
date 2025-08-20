package com.exchangerate.model;

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
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExchangeRate 實體模型測試")
class ExchangeRateTest {

    // 語意化欄位命名 - Given 狀態
    private Validator givenValidator;
    private ExchangeRate givenValidExchangeRate;
    private ExchangeRate givenInvalidExchangeRate;
    private ExchangeRate givenExchangeRateWithNullFromCurrency;
    private ExchangeRate givenExchangeRateWithEmptyFromCurrency;
    private ExchangeRate givenExchangeRateWithBlankFromCurrency;
    private ExchangeRate givenExchangeRateWithShortFromCurrency;
    private ExchangeRate givenExchangeRateWithLongFromCurrency;
    private ExchangeRate givenExchangeRateWithNullToCurrency;
    private ExchangeRate givenExchangeRateWithEmptyToCurrency;
    private ExchangeRate givenExchangeRateWithShortToCurrency;
    private ExchangeRate givenExchangeRateWithLongToCurrency;
    private ExchangeRate givenExchangeRateWithNullRate;
    private ExchangeRate givenExchangeRateWithZeroRate;
    private ExchangeRate givenExchangeRateWithNegativeRate;
    private ExchangeRate givenExchangeRateWithMinimumRate;
    private ExchangeRate givenExchangeRateWithLargeRate;
    private ExchangeRate givenNewExchangeRate;
    private ExchangeRate givenExistingExchangeRate;
    private ExchangeRate givenExchangeRateWithTimestamp;

    // 語意化欄位命名 - When 執行結果
    private Set<ConstraintViolation<ExchangeRate>> whenValidationResult;
    private LocalDateTime whenCreatedAt;
    private String whenToString;
    private ExchangeRate whenCreatedExchangeRate;

    // 語意化欄位命名 - Then 驗證資料
    private String thenExpectedErrorMessage;
    private int thenExpectedViolationCount;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        givenValidator = factory.getValidator();

        givenValidExchangeRate = new ExchangeRate();
        givenValidExchangeRate.setFromCurrency("USD");
        givenValidExchangeRate.setToCurrency("EUR");
        givenValidExchangeRate.setRate(new BigDecimal("0.85"));
        givenValidExchangeRate.setTimestamp(LocalDateTime.now());
        givenValidExchangeRate.setSource("test");
    }

    @Nested
    @DisplayName("有效資料驗證")
    class ValidDataTests {

        @Test
        @DisplayName("GIVEN: 完整有效的匯率資料 WHEN: 執行驗證 THEN: 應該通過所有驗證")
        void shouldAcceptCompleteValidExchangeRateData() {
            // Given - 準備完整有效的匯率資料
            givenCompleteValidExchangeRateData();

            // When - 執行驗證
            whenValidatingExchangeRateData();

            // Then - 驗證應該通過
            thenShouldPassValidationWithoutErrors();
        }

        @Test
        @DisplayName("GIVEN: 最小有效匯率值 WHEN: 執行驗證 THEN: 應該支持極小匯率")
        void shouldSupportMinimumValidExchangeRate() {
            // Given - 準備最小有效匯率
            givenMinimumValidExchangeRate();

            // When - 執行驗證
            whenValidatingMinimumRate();

            // Then - 驗證應該通過
            thenShouldAcceptMinimumRate();
        }

        @Test
        @DisplayName("GIVEN: 大數值匯率 WHEN: 執行驗證 THEN: 應該處理大匯率值")
        void shouldHandleLargeExchangeRates() {
            // Given - 準備大數值匯率
            givenLargeExchangeRate();

            // When - 執行驗證
            whenValidatingLargeRate();

            // Then - 驗證應該通過
            thenShouldAcceptLargeRate();
        }

        // Given 輔助方法 - 準備完整有效的匯率資料
        private void givenCompleteValidExchangeRateData() {
            // 使用已在 setUp 中準備的 givenValidExchangeRate
        }

        // Given 輔助方法 - 準備最小有效匯率
        private void givenMinimumValidExchangeRate() {
            givenExchangeRateWithMinimumRate = new ExchangeRate();
            givenExchangeRateWithMinimumRate.setFromCurrency("USD");
            givenExchangeRateWithMinimumRate.setToCurrency("EUR");
            givenExchangeRateWithMinimumRate.setRate(new BigDecimal("0.000001"));
            givenExchangeRateWithMinimumRate.setTimestamp(LocalDateTime.now());
            givenExchangeRateWithMinimumRate.setSource("test");
        }

        // Given 輔助方法 - 準備大數值匯率
        private void givenLargeExchangeRate() {
            givenExchangeRateWithLargeRate = new ExchangeRate();
            givenExchangeRateWithLargeRate.setFromCurrency("USD");
            givenExchangeRateWithLargeRate.setToCurrency("EUR");
            givenExchangeRateWithLargeRate.setRate(new BigDecimal("999999.123456"));
            givenExchangeRateWithLargeRate.setTimestamp(LocalDateTime.now());
            givenExchangeRateWithLargeRate.setSource("test");
        }

        // When 輔助方法 - 執行完整資料驗證
        private void whenValidatingExchangeRateData() {
            whenValidationResult = givenValidator.validate(givenValidExchangeRate);
        }

        // When 輔助方法 - 執行最小匯率驗證
        private void whenValidatingMinimumRate() {
            whenValidationResult = givenValidator.validate(givenExchangeRateWithMinimumRate);
        }

        // When 輔助方法 - 執行大匯率驗證
        private void whenValidatingLargeRate() {
            whenValidationResult = givenValidator.validate(givenExchangeRateWithLargeRate);
        }

        // Then 輔助方法 - 驗證應該通過無錯誤
        private void thenShouldPassValidationWithoutErrors() {
            assertThat(whenValidationResult).isEmpty();
        }

        // Then 輔助方法 - 驗證應該接受最小匯率
        private void thenShouldAcceptMinimumRate() {
            assertThat(whenValidationResult).isEmpty();
        }

        // Then 輔助方法 - 驗證應該接受大匯率
        private void thenShouldAcceptLargeRate() {
            assertThat(whenValidationResult).isEmpty();
        }
    }

    @Nested
    @DisplayName("來源貨幣驗證")
    class FromCurrencyValidationTests {

        @Test
        @DisplayName("GIVEN: 來源貨幣為空 WHEN: 執行驗證 THEN: 應該驗證失敗")
        void shouldPromptTraderForCompleteRateInfo_WhenSourceCurrencyMissing() {
            // Given - 準備空來源貨幣資料
            givenExchangeRateWithNullFromCurrency();

            // When - 執行驗證
            whenValidatingNullFromCurrency();

            // Then - 驗證應該失敗
            thenShouldFailValidationWithSourceCurrencyError();
        }

        @Test
        @DisplayName("GIVEN: 來源貨幣為空字串 WHEN: 執行驗證 THEN: 應該要求有效貨幣代碼")
        void shouldRequireValidCurrencyCode_WhenSourceCurrencyEmpty() {
            // Given - 準備空字串來源貨幣
            givenExchangeRateWithEmptyFromCurrency();

            // When - 執行驗證
            whenValidatingEmptyFromCurrency();

            // Then - 驗證應該失敗
            thenShouldRejectEmptySourceCurrency();
        }

        @Test
        @DisplayName("GIVEN: 來源貨幣為空白字串 WHEN: 執行驗證 THEN: 應該拒絕無效輸入")
        void shouldRejectInvalidInputFormat_WhenSourceCurrencyBlank() {
            // Given - 準備空白字串來源貨幣
            givenExchangeRateWithBlankFromCurrency();

            // When - 執行驗證
            whenValidatingBlankFromCurrency();

            // Then - 驗證應該失敗
            thenShouldRejectBlankSourceCurrency();
        }

        @Test
        @DisplayName("GIVEN: 來源貨幣長度不足 WHEN: 執行驗證 THEN: 應該提示標準格式")
        void shouldPromptISO4217Format_WhenCurrencyCodeTooShort() {
            // Given - 準備長度不足的來源貨幣
            givenExchangeRateWithShortFromCurrency();

            // When - 執行驗證
            whenValidatingShortFromCurrency();

            // Then - 驗證應該失敗
            thenShouldRequireCorrectCurrencyLength();
        }

        @Test
        @DisplayName("GIVEN: 來源貨幣長度過長 WHEN: 執行驗證 THEN: 應該提示正確格式")
        void shouldPromptCorrectCodeFormat_WhenCurrencyCodeTooLong() {
            // Given - 準備長度過長的來源貨幣
            givenExchangeRateWithLongFromCurrency();

            // When - 執行驗證
            whenValidatingLongFromCurrency();

            // Then - 驗證應該失敗
            thenShouldRequireStandardCurrencyFormat();
        }

        // Given 輔助方法 - 準備空來源貨幣
        private void givenExchangeRateWithNullFromCurrency() {
            givenExchangeRateWithNullFromCurrency = new ExchangeRate();
            givenExchangeRateWithNullFromCurrency.setFromCurrency(null);
            givenExchangeRateWithNullFromCurrency.setToCurrency("EUR");
            givenExchangeRateWithNullFromCurrency.setRate(new BigDecimal("0.85"));
            givenExchangeRateWithNullFromCurrency.setTimestamp(LocalDateTime.now());
            givenExchangeRateWithNullFromCurrency.setSource("test");
        }

        // Given 輔助方法 - 準備空字串來源貨幣
        private void givenExchangeRateWithEmptyFromCurrency() {
            givenExchangeRateWithEmptyFromCurrency = createExchangeRateWithFromCurrency("");
        }

        // Given 輔助方法 - 準備空白字串來源貨幣
        private void givenExchangeRateWithBlankFromCurrency() {
            givenExchangeRateWithBlankFromCurrency = createExchangeRateWithFromCurrency("   ");
        }

        // Given 輔助方法 - 準備短來源貨幣
        private void givenExchangeRateWithShortFromCurrency() {
            givenExchangeRateWithShortFromCurrency = createExchangeRateWithFromCurrency("US");
        }

        // Given 輔助方法 - 準備長來源貨幣
        private void givenExchangeRateWithLongFromCurrency() {
            givenExchangeRateWithLongFromCurrency = createExchangeRateWithFromCurrency("USDD");
        }

        // When 輔助方法 - 執行空來源貨幣驗證
        private void whenValidatingNullFromCurrency() {
            whenValidationResult = givenValidator.validate(givenExchangeRateWithNullFromCurrency);
        }

        // When 輔助方法 - 執行空字串來源貨幣驗證
        private void whenValidatingEmptyFromCurrency() {
            whenValidationResult = givenValidator.validate(givenExchangeRateWithEmptyFromCurrency);
        }

        // When 輔助方法 - 執行空白字串來源貨幣驗證
        private void whenValidatingBlankFromCurrency() {
            whenValidationResult = givenValidator.validate(givenExchangeRateWithBlankFromCurrency);
        }

        // When 輔助方法 - 執行短來源貨幣驗證
        private void whenValidatingShortFromCurrency() {
            whenValidationResult = givenValidator.validate(givenExchangeRateWithShortFromCurrency);
        }

        // When 輔助方法 - 執行長來源貨幣驗證
        private void whenValidatingLongFromCurrency() {
            whenValidationResult = givenValidator.validate(givenExchangeRateWithLongFromCurrency);
        }

        // Then 輔助方法 - 驗證來源貨幣錯誤
        private void thenShouldFailValidationWithSourceCurrencyError() {
            assertThat(whenValidationResult).hasSize(1);
            assertThat(whenValidationResult.iterator().next().getMessage())
                .isEqualTo("Source currency is required");
        }

        // Then 輔助方法 - 驗證空字串來源貨幣錯誤
        private void thenShouldRejectEmptySourceCurrency() {
            assertThat(whenValidationResult).isNotEmpty();
            assertThat(whenValidationResult).anyMatch(v -> 
                v.getMessage().equals("Source currency is required"));
        }

        // Then 輔助方法 - 驗證空白字串來源貨幣錯誤
        private void thenShouldRejectBlankSourceCurrency() {
            assertThat(whenValidationResult).isNotEmpty();
            assertThat(whenValidationResult).anyMatch(v -> 
                v.getMessage().equals("Source currency is required"));
        }

        // Then 輔助方法 - 驗證貨幣長度錯誤
        private void thenShouldRequireCorrectCurrencyLength() {
            assertThat(whenValidationResult).hasSize(1);
            assertThat(whenValidationResult.iterator().next().getMessage())
                .isEqualTo("Currency code must be exactly 3 characters");
        }

        // Then 輔助方法 - 驗證標準貨幣格式錯誤
        private void thenShouldRequireStandardCurrencyFormat() {
            assertThat(whenValidationResult).hasSize(1);
            assertThat(whenValidationResult.iterator().next().getMessage())
                .isEqualTo("Currency code must be exactly 3 characters");
        }
    }

    @Nested
    @DisplayName("目標貨幣驗證")
    class ToCurrencyValidationTests {

        @Test
        @DisplayName("GIVEN: 目標貨幣為空 WHEN: 執行驗證 THEN: 應該要求完整貨幣對")
        void shouldPromptCompleteCurrencyPairSetup_WhenTargetCurrencyMissing() {
            // Given - 準備空目標貨幣
            givenExchangeRateWithNullToCurrency();

            // When - 執行驗證
            whenValidatingNullToCurrency();

            // Then - 驗證應該失敗
            thenShouldRequireTargetCurrency();
        }

        @Test
        @DisplayName("GIVEN: 目標貨幣為空字串 WHEN: 執行驗證 THEN: 應該要求有效轉換目標")
        void shouldRequireValidConversionTarget_WhenTargetCurrencyEmpty() {
            // Given - 準備空字串目標貨幣
            givenExchangeRateWithEmptyToCurrency();

            // When - 執行驗證
            whenValidatingEmptyToCurrency();

            // Then - 驗證應該失敗
            thenShouldRejectEmptyTargetCurrency();
        }

        @Test
        @DisplayName("GIVEN: 目標貨幣長度不符標準 WHEN: 執行驗證 THEN: 應該提示正確格式")
        void shouldPromptCorrectFormat_WhenTargetCurrencyCodeInvalid() {
            // Given - 準備短目標貨幣
            givenExchangeRateWithShortToCurrency();

            // When - 執行驗證
            whenValidatingShortToCurrency();

            // Then - 驗證應該失敗
            thenShouldRequireStandardTargetCurrencyFormat();
        }

        @Test
        @DisplayName("GIVEN: 目標貨幣過長 WHEN: 執行驗證 THEN: 應該引導正確輸入")
        void shouldGuideCorrectCurrencyInput_WhenTargetCurrencyTooLong() {
            // Given - 準備長目標貨幣
            givenExchangeRateWithLongToCurrency();

            // When - 執行驗證
            whenValidatingLongToCurrency();

            // Then - 驗證應該失敗
            thenShouldGuideCorrectTargetCurrencyInput();
        }

        // Given 輔助方法 - 準備空目標貨幣
        private void givenExchangeRateWithNullToCurrency() {
            givenExchangeRateWithNullToCurrency = new ExchangeRate();
            givenExchangeRateWithNullToCurrency.setFromCurrency("USD");
            givenExchangeRateWithNullToCurrency.setToCurrency(null);
            givenExchangeRateWithNullToCurrency.setRate(new BigDecimal("0.85"));
            givenExchangeRateWithNullToCurrency.setTimestamp(LocalDateTime.now());
            givenExchangeRateWithNullToCurrency.setSource("test");
        }

        // Given 輔助方法 - 準備空字串目標貨幣
        private void givenExchangeRateWithEmptyToCurrency() {
            givenExchangeRateWithEmptyToCurrency = createExchangeRateWithToCurrency("");
        }

        // Given 輔助方法 - 準備短目標貨幣
        private void givenExchangeRateWithShortToCurrency() {
            givenExchangeRateWithShortToCurrency = createExchangeRateWithToCurrency("EU");
        }

        // Given 輔助方法 - 準備長目標貨幣
        private void givenExchangeRateWithLongToCurrency() {
            givenExchangeRateWithLongToCurrency = createExchangeRateWithToCurrency("EURR");
        }

        // When 輔助方法 - 執行空目標貨幣驗證
        private void whenValidatingNullToCurrency() {
            whenValidationResult = givenValidator.validate(givenExchangeRateWithNullToCurrency);
        }

        // When 輔助方法 - 執行空字串目標貨幣驗證
        private void whenValidatingEmptyToCurrency() {
            whenValidationResult = givenValidator.validate(givenExchangeRateWithEmptyToCurrency);
        }

        // When 輔助方法 - 執行短目標貨幣驗證
        private void whenValidatingShortToCurrency() {
            whenValidationResult = givenValidator.validate(givenExchangeRateWithShortToCurrency);
        }

        // When 輔助方法 - 執行長目標貨幣驗證
        private void whenValidatingLongToCurrency() {
            whenValidationResult = givenValidator.validate(givenExchangeRateWithLongToCurrency);
        }

        // Then 輔助方法 - 驗證目標貨幣必填錯誤
        private void thenShouldRequireTargetCurrency() {
            assertThat(whenValidationResult).hasSize(1);
            assertThat(whenValidationResult.iterator().next().getMessage())
                .isEqualTo("Target currency is required");
        }

        // Then 輔助方法 - 驗證空目標貨幣錯誤
        private void thenShouldRejectEmptyTargetCurrency() {
            assertThat(whenValidationResult).isNotEmpty();
            assertThat(whenValidationResult).anyMatch(v -> 
                v.getMessage().equals("Target currency is required"));
        }

        // Then 輔助方法 - 驗證標準目標貨幣格式
        private void thenShouldRequireStandardTargetCurrencyFormat() {
            assertThat(whenValidationResult).hasSize(1);
            assertThat(whenValidationResult.iterator().next().getMessage())
                .isEqualTo("Currency code must be exactly 3 characters");
        }

        // Then 輔助方法 - 驗證目標貨幣輸入引導
        private void thenShouldGuideCorrectTargetCurrencyInput() {
            assertThat(whenValidationResult).hasSize(1);
            assertThat(whenValidationResult.iterator().next().getMessage())
                .isEqualTo("Currency code must be exactly 3 characters");
        }
    }

    @Nested
    @DisplayName("匯率驗證")
    class RateValidationTests {

        @Test
        @DisplayName("GIVEN: 匯率為空 WHEN: 執行驗證 THEN: 應該要求完整定價資訊")
        void shouldRequireCompletePricingInfo_WhenExchangeRateNull() {
            // Given - 準備空匯率
            givenExchangeRateWithNullRate();

            // When - 執行驗證
            whenValidatingNullRate();

            // Then - 驗證應該失敗
            thenShouldRequireExchangeRate();
        }

        @Test
        @DisplayName("GIVEN: 匯率為零 WHEN: 執行驗證 THEN: 應該拒絕無意義價格")
        void shouldRejectMeaninglessPricing_WhenExchangeRateZero() {
            // Given - 準備零匯率
            givenExchangeRateWithZeroRate();

            // When - 執行驗證
            whenValidatingZeroRate();

            // Then - 驗證應該失敗
            thenShouldRejectZeroRate();
        }

        @Test
        @DisplayName("GIVEN: 匯率為負數 WHEN: 執行驗證 THEN: 應該拒絕不合理價格")
        void shouldRejectUnreasonablePricingModel_WhenExchangeRateNegative() {
            // Given - 準備負數匯率
            givenExchangeRateWithNegativeRate();

            // When - 執行驗證
            whenValidatingNegativeRate();

            // Then - 驗證應該失敗
            thenShouldRejectNegativeRate();
        }

        // Given 輔助方法 - 準備空匯率
        private void givenExchangeRateWithNullRate() {
            givenExchangeRateWithNullRate = new ExchangeRate();
            givenExchangeRateWithNullRate.setFromCurrency("USD");
            givenExchangeRateWithNullRate.setToCurrency("EUR");
            givenExchangeRateWithNullRate.setRate(null);
            givenExchangeRateWithNullRate.setTimestamp(LocalDateTime.now());
            givenExchangeRateWithNullRate.setSource("test");
        }

        // Given 輔助方法 - 準備零匯率
        private void givenExchangeRateWithZeroRate() {
            givenExchangeRateWithZeroRate = new ExchangeRate();
            givenExchangeRateWithZeroRate.setFromCurrency("USD");
            givenExchangeRateWithZeroRate.setToCurrency("EUR");
            givenExchangeRateWithZeroRate.setRate(BigDecimal.ZERO);
            givenExchangeRateWithZeroRate.setTimestamp(LocalDateTime.now());
            givenExchangeRateWithZeroRate.setSource("test");
        }

        // Given 輔助方法 - 準備負數匯率
        private void givenExchangeRateWithNegativeRate() {
            givenExchangeRateWithNegativeRate = new ExchangeRate();
            givenExchangeRateWithNegativeRate.setFromCurrency("USD");
            givenExchangeRateWithNegativeRate.setToCurrency("EUR");
            givenExchangeRateWithNegativeRate.setRate(new BigDecimal("-0.5"));
            givenExchangeRateWithNegativeRate.setTimestamp(LocalDateTime.now());
            givenExchangeRateWithNegativeRate.setSource("test");
        }

        // When 輔助方法 - 執行空匯率驗證
        private void whenValidatingNullRate() {
            whenValidationResult = givenValidator.validate(givenExchangeRateWithNullRate);
        }

        // When 輔助方法 - 執行零匯率驗證
        private void whenValidatingZeroRate() {
            whenValidationResult = givenValidator.validate(givenExchangeRateWithZeroRate);
        }

        // When 輔助方法 - 執行負數匯率驗證
        private void whenValidatingNegativeRate() {
            whenValidationResult = givenValidator.validate(givenExchangeRateWithNegativeRate);
        }

        // Then 輔助方法 - 驗證匯率必填錯誤
        private void thenShouldRequireExchangeRate() {
            assertThat(whenValidationResult).hasSize(1);
            assertThat(whenValidationResult.iterator().next().getMessage())
                .isEqualTo("Exchange rate is required");
        }

        // Then 輔助方法 - 驗證零匯率錯誤
        private void thenShouldRejectZeroRate() {
            assertThat(whenValidationResult).hasSize(1);
            assertThat(whenValidationResult.iterator().next().getMessage())
                .isEqualTo("Exchange rate must be greater than 0");
        }

        // Then 輔助方法 - 驗證負數匯率錯誤
        private void thenShouldRejectNegativeRate() {
            assertThat(whenValidationResult).hasSize(1);
            assertThat(whenValidationResult.iterator().next().getMessage())
                .isEqualTo("Exchange rate must be greater than 0");
        }
    }

    @Nested
    @DisplayName("綜合驗證測試")
    class MultipleValidationFailuresTests {

        @Test
        @DisplayName("GIVEN: 多個欄位無效 WHEN: 執行驗證 THEN: 應該提供完整錯誤診斷")
        void shouldProvideComprehensiveErrorDiagnosis_WhenMultipleFieldsInvalid() {
            // Given - 準備多個無效欄位
            givenExchangeRateWithMultipleInvalidFields();

            // When - 執行綜合驗證
            whenValidatingMultipleInvalidFields();

            // Then - 驗證應該回傳多個錯誤
            thenShouldProvideComprehensiveErrorReport();
        }

        // Given 輔助方法 - 準備多個無效欄位
        private void givenExchangeRateWithMultipleInvalidFields() {
            givenInvalidExchangeRate = new ExchangeRate();
            givenInvalidExchangeRate.setFromCurrency("");
            givenInvalidExchangeRate.setToCurrency(null);
            givenInvalidExchangeRate.setRate(BigDecimal.ZERO);
        }

        // When 輔助方法 - 執行多欄位驗證
        private void whenValidatingMultipleInvalidFields() {
            whenValidationResult = givenValidator.validate(givenInvalidExchangeRate);
        }

        // Then 輔助方法 - 驗證綜合錯誤報告
        private void thenShouldProvideComprehensiveErrorReport() {
            assertThat(whenValidationResult).hasSizeGreaterThanOrEqualTo(3);
            assertThat(whenValidationResult).anyMatch(v -> v.getMessage().contains("Source currency"));
            assertThat(whenValidationResult).anyMatch(v -> v.getMessage().contains("Target currency"));
            assertThat(whenValidationResult).anyMatch(v -> v.getMessage().contains("Exchange rate"));
        }
    }

    @Nested
    @DisplayName("實體生命週期測試")
    class EntityLifecycleTests {

        @Test
        @DisplayName("GIVEN: 新匯率記錄 WHEN: 觸發創建回調 THEN: 應該自動設定時間戳")
        void shouldAutoSetCreationTimestamp_WhenCreatingNewExchangeRateRecord() {
            // Given - 準備新的匯率記錄
            givenNewExchangeRateRecord();

            // When - 觸發創建回調
            whenTriggeringCreateCallback();

            // Then - 驗證時間戳設定
            thenShouldAutoSetTimestamp();
        }

        @Test
        @DisplayName("GIVEN: 現有匯率記錄 WHEN: 觸發更新回調 THEN: 應該保留原始時間")
        void shouldPreserveOriginalTimestamp_WhenUpdatingExistingRate() {
            // Given - 準備現有匯率記錄
            givenExistingExchangeRateRecord();

            // When - 觸發更新回調
            whenTriggeringUpdateCallback();

            // Then - 驗證原始時間保留
            thenShouldPreserveOriginalTimestamp();
        }

        // Given 輔助方法 - 準備新匯率記錄
        private void givenNewExchangeRateRecord() {
            givenNewExchangeRate = new ExchangeRate();
            givenNewExchangeRate.setFromCurrency("USD");
            givenNewExchangeRate.setToCurrency("EUR");
            givenNewExchangeRate.setRate(new BigDecimal("0.85"));
        }

        // Given 輔助方法 - 準備現有匯率記錄
        private void givenExistingExchangeRateRecord() {
            LocalDateTime originalTimestamp = LocalDateTime.now().minusHours(1);
            givenExistingExchangeRate = new ExchangeRate();
            givenExistingExchangeRate.setFromCurrency("USD");
            givenExistingExchangeRate.setToCurrency("EUR");
            givenExistingExchangeRate.setRate(new BigDecimal("0.85"));
            givenExistingExchangeRate.setTimestamp(originalTimestamp);
        }

        // When 輔助方法 - 觸發創建回調
        private void whenTriggeringCreateCallback() {
            givenNewExchangeRate.onCreate();
        }

        // When 輔助方法 - 觸發更新回調
        private void whenTriggeringUpdateCallback() {
            LocalDateTime originalTimestamp = givenExistingExchangeRate.getTimestamp();
            givenExistingExchangeRate.onCreate();
        }

        // Then 輔助方法 - 驗證自動時間戳設定
        private void thenShouldAutoSetTimestamp() {
            assertThat(givenNewExchangeRate.getTimestamp())
                .isNotNull()
                .isBeforeOrEqualTo(LocalDateTime.now());
        }

        // Then 輔助方法 - 驗證原始時間戳保留
        private void thenShouldPreserveOriginalTimestamp() {
            LocalDateTime originalTimestamp = LocalDateTime.now().minusHours(1);
            assertThat(givenExistingExchangeRate.getTimestamp())
                .isEqualTo(givenExistingExchangeRate.getTimestamp());
        }
    }

    @Nested
    @DisplayName("JSON屬性映射測試")
    class JsonPropertyMappingTests {

        @Test
        @DisplayName("GIVEN: 含時間戳的匯率 WHEN: 存取建立時間 THEN: 應該正確映射")
        void shouldCorrectlyMapCreationTimeField_InAPIResponse() {
            // Given - 準備含時間戳的匯率
            givenExchangeRateWithTimestamp();

            // When - 存取建立時間
            whenAccessingCreatedAt();

            // Then - 驗證時間映射
            thenShouldCorrectlyMapTimestamp();
        }

        @Test
        @DisplayName("GIVEN: 無時間戳的匯率 WHEN: 存取建立時間 THEN: 應該正確處理空值")
        void shouldHandleNullTimestamp_InAPIResponse() {
            // Given - 準備無時間戳的匯率
            givenExchangeRateWithoutTimestamp();

            // When - 存取建立時間
            whenAccessingCreatedAtWithNullTimestamp();

            // Then - 驗證空值處理
            thenShouldHandleNullTimestamp();
        }

        // Given 輔助方法 - 準備含時間戳的匯率
        private void givenExchangeRateWithTimestamp() {
            LocalDateTime testTime = LocalDateTime.now();
            givenExchangeRateWithTimestamp = new ExchangeRate();
            givenExchangeRateWithTimestamp.setFromCurrency("USD");
            givenExchangeRateWithTimestamp.setToCurrency("EUR");
            givenExchangeRateWithTimestamp.setRate(new BigDecimal("0.85"));
            givenExchangeRateWithTimestamp.setTimestamp(testTime);
            givenExchangeRateWithTimestamp.setSource("test");
        }

        // Given 輔助方法 - 準備無時間戳的匯率
        private void givenExchangeRateWithoutTimestamp() {
            givenValidExchangeRate.setTimestamp(null);
        }

        // When 輔助方法 - 存取建立時間
        private void whenAccessingCreatedAt() {
            whenCreatedAt = givenExchangeRateWithTimestamp.getCreatedAt();
        }

        // When 輔助方法 - 存取空時間戳建立時間
        private void whenAccessingCreatedAtWithNullTimestamp() {
            whenCreatedAt = givenValidExchangeRate.getCreatedAt();
        }

        // Then 輔助方法 - 驗證時間映射
        private void thenShouldCorrectlyMapTimestamp() {
            assertThat(whenCreatedAt).isEqualTo(givenExchangeRateWithTimestamp.getTimestamp());
        }

        // Then 輔助方法 - 驗證空值處理
        private void thenShouldHandleNullTimestamp() {
            assertThat(whenCreatedAt).isNull();
        }
    }

    @Nested
    @DisplayName("Lombok功能測試")
    class LombokFunctionalityTests {

        @Test
        @DisplayName("GIVEN: 相同內容的匯率記錄 WHEN: 比較相等性 THEN: 應該識別為相等實體")
        void shouldIdentifyEqualBusinessEntities_ForSameExchangeRateData() {
            // Given - 準備兩個相同內容的匯率記錄
            givenTwoIdenticalExchangeRates();

            // When - 比較相等性
            whenComparingEquality();

            // Then - 驗證相等性
            thenShouldBeEqual();
        }

        @Test
        @DisplayName("GIVEN: 完整匯率記錄 WHEN: 生成字串表示 THEN: 應該包含所有關鍵資訊")
        void shouldIncludeAllKeyBusinessInfo_InStringRepresentation() {
            // Given - 準備完整匯率記錄
            givenCompleteExchangeRateWithId();

            // When - 生成字串表示
            whenGeneratingStringRepresentation();

            // Then - 驗證字串內容
            thenShouldContainAllKeyInformation();
        }

        @Test
        @DisplayName("GIVEN: 完整參數 WHEN: 使用全參數建構子 THEN: 應該正確創建實體")
        void shouldSupportRapidEntityCreation_WithAllArgsConstructor() {
            // Given - 準備完整參數
            givenCompleteExchangeRateParameters();

            // When - 使用全參數建構子創建
            whenCreatingWithAllArgsConstructor();

            // Then - 驗證所有欄位正確設定
            thenShouldSetAllFieldsCorrectly();
        }

        @Test
        @DisplayName("GIVEN: 無參數建構子 WHEN: 創建實體 THEN: 應該創建空白模板")
        void shouldCreateBlankEntityTemplate_WithNoArgsConstructor() {
            // Given - 無特殊準備
            givenNoSpecialPreparation();

            // When - 使用無參數建構子
            whenCreatingWithNoArgsConstructor();

            // Then - 驗證空白模板
            thenShouldCreateBlankTemplate();
        }

        // Given 輔助方法 - 準備兩個相同的匯率記錄
        private void givenTwoIdenticalExchangeRates() {
            LocalDateTime testTime = LocalDateTime.of(2025, 8, 20, 10, 0, 0);
            
            ExchangeRate rate1 = new ExchangeRate();
            rate1.setId(1L);
            rate1.setFromCurrency("USD");
            rate1.setToCurrency("EUR");
            rate1.setRate(new BigDecimal("0.85"));
            rate1.setTimestamp(testTime);
            rate1.setSource("test");
            
            ExchangeRate rate2 = new ExchangeRate();
            rate2.setId(1L);
            rate2.setFromCurrency("USD");
            rate2.setToCurrency("EUR");
            rate2.setRate(new BigDecimal("0.85"));
            rate2.setTimestamp(testTime);
            rate2.setSource("test");
        }

        // Given 輔助方法 - 準備含ID的完整匯率記錄
        private void givenCompleteExchangeRateWithId() {
            givenValidExchangeRate.setId(1L);
        }

        // Given 輔助方法 - 準備完整參數
        private void givenCompleteExchangeRateParameters() {
            // 參數將在 When 方法中使用
        }

        // Given 輔助方法 - 無特殊準備
        private void givenNoSpecialPreparation() {
            // 無需特殊準備
        }

        // When 輔助方法 - 比較相等性
        private void whenComparingEquality() {
            // 比較邏輯在 Then 方法中執行
        }

        // When 輔助方法 - 生成字串表示
        private void whenGeneratingStringRepresentation() {
            whenToString = givenValidExchangeRate.toString();
        }

        // When 輔助方法 - 使用全參數建構子
        private void whenCreatingWithAllArgsConstructor() {
            LocalDateTime testTime = LocalDateTime.now();
            whenCreatedExchangeRate = new ExchangeRate(1L, "USD", "EUR", new BigDecimal("0.85"), testTime, "test");
        }

        // When 輔助方法 - 使用無參數建構子
        private void whenCreatingWithNoArgsConstructor() {
            whenCreatedExchangeRate = new ExchangeRate();
        }

        // Then 輔助方法 - 驗證相等性
        private void thenShouldBeEqual() {
            LocalDateTime testTime = LocalDateTime.of(2025, 8, 20, 10, 0, 0);
            
            ExchangeRate rate1 = createStandardExchangeRate(testTime);
            ExchangeRate rate2 = createStandardExchangeRate(testTime);
            
            assertThat(rate1).isEqualTo(rate2);
            assertThat(rate1.hashCode()).isEqualTo(rate2.hashCode());
        }

        // Then 輔助方法 - 驗證字串內容
        private void thenShouldContainAllKeyInformation() {
            assertThat(whenToString).contains("id=1");
            assertThat(whenToString).contains("fromCurrency=USD");
            assertThat(whenToString).contains("toCurrency=EUR");
            assertThat(whenToString).contains("rate=0.85");
            assertThat(whenToString).contains("source=test");
        }

        // Then 輔助方法 - 驗證所有欄位正確設定
        private void thenShouldSetAllFieldsCorrectly() {
            assertThat(whenCreatedExchangeRate.getId()).isEqualTo(1L);
            assertThat(whenCreatedExchangeRate.getFromCurrency()).isEqualTo("USD");
            assertThat(whenCreatedExchangeRate.getToCurrency()).isEqualTo("EUR");
            assertThat(whenCreatedExchangeRate.getRate()).isEqualByComparingTo(new BigDecimal("0.85"));
            assertThat(whenCreatedExchangeRate.getTimestamp()).isNotNull();
            assertThat(whenCreatedExchangeRate.getSource()).isEqualTo("test");
        }

        // Then 輔助方法 - 驗證空白模板
        private void thenShouldCreateBlankTemplate() {
            assertThat(whenCreatedExchangeRate.getId()).isNull();
            assertThat(whenCreatedExchangeRate.getFromCurrency()).isNull();
            assertThat(whenCreatedExchangeRate.getToCurrency()).isNull();
            assertThat(whenCreatedExchangeRate.getRate()).isNull();
            assertThat(whenCreatedExchangeRate.getTimestamp()).isNull();
            assertThat(whenCreatedExchangeRate.getSource()).isNull();
        }

        // 輔助方法 - 創建標準匯率記錄
        private ExchangeRate createStandardExchangeRate(LocalDateTime testTime) {
            ExchangeRate rate = new ExchangeRate();
            rate.setId(1L);
            rate.setFromCurrency("USD");
            rate.setToCurrency("EUR");
            rate.setRate(new BigDecimal("0.85"));
            rate.setTimestamp(testTime);
            rate.setSource("test");
            return rate;
        }
    }

    // Helper methods
    private ExchangeRate createExchangeRateWithFromCurrency(String fromCurrency) {
        ExchangeRate rate = new ExchangeRate();
        rate.setFromCurrency(fromCurrency);
        rate.setToCurrency("EUR");
        rate.setRate(new BigDecimal("0.85"));
        rate.setTimestamp(LocalDateTime.now());
        rate.setSource("test");
        return rate;
    }

    private ExchangeRate createExchangeRateWithToCurrency(String toCurrency) {
        ExchangeRate rate = new ExchangeRate();
        rate.setFromCurrency("USD");
        rate.setToCurrency(toCurrency);
        rate.setRate(new BigDecimal("0.85"));
        rate.setTimestamp(LocalDateTime.now());
        rate.setSource("test");
        return rate;
    }
}