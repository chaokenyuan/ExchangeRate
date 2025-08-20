package com.exchangerate.service;

import com.exchangerate.dto.ConversionRequest;
import com.exchangerate.dto.ConversionResponse;
import com.exchangerate.model.ExchangeRate;
import com.exchangerate.repository.ExchangeRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ExchangeRateService 單元測試
 * 
 * 🔍 代碼審查規範遵循:
 * ✅ 使用 @ExtendWith(MockitoExtension.class)
 * ✅ 中文 @DisplayName 描述
 * ✅ Given-When-Then 語意化方法包裝模式
 * ✅ 所有測試邏輯封裝在輔助方法中
 * ✅ 語意化變數命名
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExchangeRateService 單元測試")
class ExchangeRateServiceTest {

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @InjectMocks
    private TestExchangeRateService exchangeRateService;

    // === 測試數據欄位 (語意化命名) ===
    private ExchangeRate givenUsdToEur;
    private ExchangeRate givenEurToUsd;
    private ExchangeRate givenUsdToJpy;
    private ConversionRequest givenValidRequest;
    private List<ExchangeRate> givenExpectedRates;
    private Page<ExchangeRate> givenExpectedPage;
    private ExchangeRate whenSavedRate;
    private List<ExchangeRate> whenResultRates;
    private ConversionResponse whenConversionResult;
    private Optional<ExchangeRate> whenOptionalResult;

    @BeforeEach
    void setUp() {
        givenUsdToEur = new ExchangeRate();
        givenUsdToEur.setId(1L);
        givenUsdToEur.setFromCurrency("USD");
        givenUsdToEur.setToCurrency("EUR");
        givenUsdToEur.setRate(new BigDecimal("0.85"));
        givenUsdToEur.setTimestamp(LocalDateTime.now());
        givenUsdToEur.setSource("test");

        givenEurToUsd = new ExchangeRate();
        givenEurToUsd.setId(2L);
        givenEurToUsd.setFromCurrency("EUR");
        givenEurToUsd.setToCurrency("USD");
        givenEurToUsd.setRate(new BigDecimal("1.18"));
        givenEurToUsd.setTimestamp(LocalDateTime.now());
        givenEurToUsd.setSource("test");

        givenUsdToJpy = new ExchangeRate();
        givenUsdToJpy.setId(3L);
        givenUsdToJpy.setFromCurrency("USD");
        givenUsdToJpy.setToCurrency("JPY");
        givenUsdToJpy.setRate(new BigDecimal("110.0"));
        givenUsdToJpy.setTimestamp(LocalDateTime.now());
        givenUsdToJpy.setSource("test");

        givenValidRequest = new ConversionRequest();
        givenValidRequest.setFromCurrency("USD");
        givenValidRequest.setToCurrency("EUR");
        givenValidRequest.setAmount(new BigDecimal("100"));
    }

    @Nested
    @DisplayName("取得所有匯率測試")
    class GetAllExchangeRatesTests {

        @Test
        @DisplayName("GIVEN: 存在匯率資料 WHEN: 取得所有匯率 THEN: 應該返回所有匯率")
        void shouldReturnAllExchangeRates() {
            // Given - 準備測試資料
            givenExistingExchangeRatesInRepository();
            
            // When - 執行被測方法
            whenGettingAllExchangeRates();
            
            // Then - 驗證結果
            thenShouldReturnAllExpectedRates();
        }

        @Test
        @DisplayName("GIVEN: 指定貨幣對篩選條件 WHEN: 根據來源和目標貨幣篩選 THEN: 應該返回符合條件的匯率")
        void shouldFilterByFromAndToCurrency() {
            // Given - 準備篩選測試資料
            givenFilterByFromAndToCurrencyData();
            
            // When - 執行篩選查詢
            whenFilteringByFromAndToCurrency();
            
            // Then - 驗證篩選結果
            thenShouldReturnFilteredRatesByPair();
        }

        @Test
        @DisplayName("GIVEN: 來源貨幣篩選條件 WHEN: 根據來源貨幣篩選 THEN: 應該返回該貨幣的所有匯率")
        void shouldFilterByFromCurrency() {
            // Given - 準備來源貨幣篩選資料
            givenFilterByFromCurrencyData();
            
            // When - 執行來源貨幣篩選
            whenFilteringByFromCurrency();
            
            // Then - 驗證來源貨幣篩選結果
            thenShouldReturnFilteredRatesByFromCurrency();
        }

        @Test
        @DisplayName("GIVEN: 分頁查詢條件 WHEN: 分頁查詢所有匯率 THEN: 應該返回分頁結果")
        void shouldReturnPagedExchangeRates() {
            // Given - 準備分頁測試資料
            givenPagedExchangeRatesData();
            
            // When - 執行分頁查詢
            whenGettingPagedExchangeRates();
            
            // Then - 驗證分頁結果
            thenShouldReturnPagedResults();
        }

        // === Given 輔助方法 ===
        private void givenExistingExchangeRatesInRepository() {
            givenExpectedRates = Arrays.asList(givenUsdToEur, givenEurToUsd);
            when(exchangeRateRepository.findAll()).thenReturn(givenExpectedRates);
        }

        private void givenFilterByFromAndToCurrencyData() {
            when(exchangeRateRepository.findByFromCurrencyAndToCurrency("USD", "EUR"))
                .thenReturn(Collections.singletonList(givenUsdToEur));
        }

        private void givenFilterByFromCurrencyData() {
            List<ExchangeRate> usdRates = Arrays.asList(givenUsdToEur, givenUsdToJpy);
            when(exchangeRateRepository.findByFromCurrency("USD")).thenReturn(usdRates);
        }

        private void givenPagedExchangeRatesData() {
            Pageable pageable = PageRequest.of(0, 10);
            List<ExchangeRate> rates = Arrays.asList(givenUsdToEur, givenEurToUsd);
            givenExpectedPage = new PageImpl<>(rates, pageable, 2);
            when(exchangeRateRepository.findAll(pageable)).thenReturn(givenExpectedPage);
        }

        // === When 輔助方法 ===
        private void whenGettingAllExchangeRates() {
            whenResultRates = exchangeRateService.getAllExchangeRates();
        }

        private void whenFilteringByFromAndToCurrency() {
            whenResultRates = exchangeRateService.getAllExchangeRates("usd", "eur");
        }

        private void whenFilteringByFromCurrency() {
            whenResultRates = exchangeRateService.getAllExchangeRates("usd", null);
        }

        private void whenGettingPagedExchangeRates() {
            Pageable pageable = PageRequest.of(0, 10);
            givenExpectedPage = exchangeRateService.getAllExchangeRates(null, null, pageable);
        }

        // === Then 輔助方法 ===
        private void thenShouldReturnAllExpectedRates() {
            assertThat(whenResultRates).hasSize(2);
            assertThat(whenResultRates).containsExactly(givenUsdToEur, givenEurToUsd);
            verify(exchangeRateRepository).findAll();
        }

        private void thenShouldReturnFilteredRatesByPair() {
            assertThat(whenResultRates).hasSize(1);
            assertThat(whenResultRates.get(0)).isEqualTo(givenUsdToEur);
            verify(exchangeRateRepository).findByFromCurrencyAndToCurrency("USD", "EUR");
        }

        private void thenShouldReturnFilteredRatesByFromCurrency() {
            assertThat(whenResultRates).hasSize(2);
            assertThat(whenResultRates).containsExactly(givenUsdToEur, givenUsdToJpy);
            verify(exchangeRateRepository).findByFromCurrency("USD");
        }

        private void thenShouldReturnPagedResults() {
            assertThat(givenExpectedPage.getContent()).hasSize(2);
            assertThat(givenExpectedPage.getTotalElements()).isEqualTo(2);
            verify(exchangeRateRepository).findAll(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("取得最新匯率測試")
    class GetLatestRateTests {

        @Test
        @DisplayName("GIVEN: 存在最新匯率 WHEN: 查詢最新匯率 THEN: 應該返回最新匯率")
        void shouldReturnLatestRate() {
            // Given - 準備最新匯率資料
            givenLatestRateExists();
            
            // When - 執行查詢最新匯率
            whenGettingLatestRate();
            
            // Then - 驗證返回最新匯率
            thenShouldReturnExpectedLatestRate();
        }

        @Test
        @DisplayName("GIVEN: 不存在匯率 WHEN: 查詢最新匯率 THEN: 應該返回空值")
        void shouldReturnEmptyWhenRateNotFound() {
            // Given - 準備不存在匯率的情況
            givenNoRateExists();
            
            // When - 執行查詢不存在的匯率
            whenGettingNonExistentRate();
            
            // Then - 驗證返回空值
            thenShouldReturnEmpty();
        }

        // === Given 輔助方法 ===
        private void givenLatestRateExists() {
            when(exchangeRateRepository.findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc("USD", "EUR"))
                .thenReturn(Optional.of(givenUsdToEur));
        }

        private void givenNoRateExists() {
            when(exchangeRateRepository.findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc("USD", "CNY"))
                .thenReturn(Optional.empty());
        }

        // === When 輔助方法 ===
        private void whenGettingLatestRate() {
            whenOptionalResult = exchangeRateService.getLatestRate("USD", "EUR");
        }

        private void whenGettingNonExistentRate() {
            whenOptionalResult = exchangeRateService.getLatestRate("USD", "CNY");
        }

        // === Then 輔助方法 ===
        private void thenShouldReturnExpectedLatestRate() {
            assertThat(whenOptionalResult).isPresent();
            assertThat(whenOptionalResult.get()).isEqualTo(givenUsdToEur);
            verify(exchangeRateRepository).findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc("USD", "EUR");
        }

        private void thenShouldReturnEmpty() {
            assertThat(whenOptionalResult).isEmpty();
            verify(exchangeRateRepository).findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc("USD", "CNY");
        }
    }

    @Nested
    @DisplayName("貨幣轉換測試")
    class ConvertCurrencyTests {

        @Test
        @DisplayName("GIVEN: 有效匯率資料 WHEN: 執行簡單貨幣轉換 THEN: 應該成功轉換")
        void shouldConvertCurrencySuccessfully() {
            // Given - 準備轉換測試資料
            givenValidExchangeRateForConversion();
            
            // When - 執行貨幣轉換
            whenConvertingCurrency();
            
            // Then - 驗證轉換結果
            thenShouldConvertSuccessfully();
        }

        @Test
        @DisplayName("GIVEN: 不存在匯率 WHEN: 執行貨幣轉換 THEN: 應該拋出異常")
        void shouldThrowExceptionWhenRateNotFound() {
            // Given - 準備不存在匯率的情況
            givenNoExchangeRateForConversion();
            
            // When & Then - 驗證異常拋出
            thenShouldThrowExceptionWhenConverting();
        }

        // === Given 輔助方法 ===
        private void givenValidExchangeRateForConversion() {
            when(exchangeRateRepository.findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc("USD", "EUR"))
                .thenReturn(Optional.of(givenUsdToEur));
        }

        private void givenNoExchangeRateForConversion() {
            when(exchangeRateRepository.findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc("USD", "CNY"))
                .thenReturn(Optional.empty());
        }

        // === When 輔助方法 ===
        private void whenConvertingCurrency() {
            BigDecimal result = exchangeRateService.convertCurrency("USD", "EUR", new BigDecimal("100"));
            // 存儲結果以供驗證
            whenConversionResult = ConversionResponse.builder()
                .toAmount(result)
                .build();
        }

        // === Then 輔助方法 ===
        private void thenShouldConvertSuccessfully() {
            assertThat(whenConversionResult.getToAmount()).isEqualByComparingTo(new BigDecimal("85.00"));
            verify(exchangeRateRepository).findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc("USD", "EUR");
        }

        private void thenShouldThrowExceptionWhenConverting() {
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                exchangeRateService.convertCurrency("USD", "CNY", new BigDecimal("100"));
            });
            
            assertThat(exception.getMessage()).contains("No exchange rate found for conversion");
        }
    }

    @Nested
    @DisplayName("詳細貨幣轉換測試")
    class ConvertCurrencyDetailedTests {

        @Test
        @DisplayName("GIVEN: 相同貨幣轉換請求 WHEN: 執行詳細轉換 THEN: 應該拋出異常")
        void shouldThrowExceptionForSameCurrency() {
            // Given - 準備相同貨幣轉換請求
            givenSameCurrencyConversionRequest();
            
            // When & Then - 驗證異常處理
            thenShouldThrowSameCurrencyException();
        }

        @Test
        @DisplayName("GIVEN: 負數金額請求 WHEN: 執行詳細轉換 THEN: 應該拋出異常")
        void shouldThrowExceptionForNegativeAmount() {
            // Given - 準備負數金額請求
            givenNegativeAmountConversionRequest();
            
            // When & Then - 驗證負數異常處理
            thenShouldThrowNegativeAmountException();
        }

        @Test
        @DisplayName("GIVEN: 不支援的貨幣 WHEN: 執行詳細轉換 THEN: 應該拋出異常")
        void shouldThrowExceptionForUnsupportedCurrency() {
            // Given - 準備不支援的貨幣請求
            givenUnsupportedCurrencyRequest();
            
            // When & Then - 驗證不支援貨幣異常
            thenShouldThrowUnsupportedCurrencyException();
        }

        @Test
        @DisplayName("GIVEN: 有效直接轉換請求 WHEN: 執行詳細轉換 THEN: 應該成功直接轉換")
        void shouldPerformDirectConversion() {
            // Given - 準備直接轉換資料
            givenValidDirectConversionData();
            
            // When - 執行直接轉換
            whenPerformingDirectConversion();
            
            // Then - 驗證直接轉換結果
            thenShouldPerformDirectConversionSuccessfully();
        }

        @Test
        @DisplayName("GIVEN: 反向轉換資料 WHEN: 執行詳細轉換 THEN: 應該成功反向轉換")
        void shouldPerformReverseConversion() {
            // Given - 準備反向轉換資料
            givenReverseConversionData();
            
            // When - 執行反向轉換
            whenPerformingReverseConversion();
            
            // Then - 驗證反向轉換結果
            thenShouldPerformReverseConversionSuccessfully();
        }

        @Test
        @DisplayName("GIVEN: 鏈式轉換資料 WHEN: 執行詳細轉換 THEN: 應該通過USD成功鏈式轉換")
        void shouldPerformChainConversionThroughUSD() {
            // Given - 準備鏈式轉換資料
            givenChainConversionData();
            
            // When - 執行鏈式轉換
            whenPerformingChainConversion();
            
            // Then - 驗證鏈式轉換結果
            thenShouldPerformChainConversionSuccessfully();
        }

        @Test
        @DisplayName("GIVEN: 無可用轉換路徑 WHEN: 執行詳細轉換 THEN: 應該拋出無路徑異常")
        void shouldThrowExceptionWhenNoConversionPathFound() {
            // Given - 準備無轉換路徑情況
            givenNoConversionPathAvailable();
            
            // When & Then - 驗證無路徑異常
            thenShouldThrowNoConversionPathException();
        }

        // === Given 輔助方法 ===
        private void givenSameCurrencyConversionRequest() {
            givenValidRequest.setFromCurrency("USD");
            givenValidRequest.setToCurrency("USD");
            givenValidRequest.setAmount(new BigDecimal("100"));
        }

        private void givenNegativeAmountConversionRequest() {
            givenValidRequest.setFromCurrency("USD");
            givenValidRequest.setToCurrency("EUR");
            givenValidRequest.setAmount(new BigDecimal("-100"));
        }

        private void givenUnsupportedCurrencyRequest() {
            givenValidRequest.setFromCurrency("XXX");
            givenValidRequest.setToCurrency("EUR");
            givenValidRequest.setAmount(new BigDecimal("100"));
        }

        private void givenValidDirectConversionData() {
            when(exchangeRateRepository.findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc("USD", "EUR"))
                .thenReturn(Optional.of(givenUsdToEur));
        }

        private void givenReverseConversionData() {
            ConversionRequest reverseRequest = new ConversionRequest();
            reverseRequest.setFromCurrency("USD");
            reverseRequest.setToCurrency("EUR");
            reverseRequest.setAmount(new BigDecimal("100"));
            givenValidRequest = reverseRequest;

            when(exchangeRateRepository.findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc("USD", "EUR"))
                .thenReturn(Optional.empty());
            when(exchangeRateRepository.findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc("EUR", "USD"))
                .thenReturn(Optional.of(givenEurToUsd));
        }

        private void givenChainConversionData() {
            ConversionRequest chainRequest = new ConversionRequest();
            chainRequest.setFromCurrency("EUR");
            chainRequest.setToCurrency("JPY");
            chainRequest.setAmount(new BigDecimal("100"));
            givenValidRequest = chainRequest;

            ExchangeRate eurToUsd = new ExchangeRate();
            eurToUsd.setFromCurrency("EUR");
            eurToUsd.setToCurrency("USD");
            eurToUsd.setRate(new BigDecimal("1.18"));

            ExchangeRate usdToJpy = new ExchangeRate();
            usdToJpy.setFromCurrency("USD");
            usdToJpy.setToCurrency("JPY");
            usdToJpy.setRate(new BigDecimal("110.0"));

            when(exchangeRateRepository.findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc("EUR", "JPY"))
                .thenReturn(Optional.empty());
            when(exchangeRateRepository.findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc("JPY", "EUR"))
                .thenReturn(Optional.empty());
            when(exchangeRateRepository.findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc("EUR", "USD"))
                .thenReturn(Optional.of(eurToUsd));
            when(exchangeRateRepository.findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc("USD", "JPY"))
                .thenReturn(Optional.of(usdToJpy));
        }

        private void givenNoConversionPathAvailable() {
            ConversionRequest noPathRequest = new ConversionRequest();
            noPathRequest.setFromCurrency("EUR");
            noPathRequest.setToCurrency("JPY");
            noPathRequest.setAmount(new BigDecimal("100"));
            givenValidRequest = noPathRequest;

            when(exchangeRateRepository.findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc(anyString(), anyString()))
                .thenReturn(Optional.empty());
        }

        // === When 輔助方法 ===
        private void whenPerformingDirectConversion() {
            whenConversionResult = exchangeRateService.convertCurrencyDetailed(givenValidRequest);
        }

        private void whenPerformingReverseConversion() {
            whenConversionResult = exchangeRateService.convertCurrencyDetailed(givenValidRequest);
        }

        private void whenPerformingChainConversion() {
            whenConversionResult = exchangeRateService.convertCurrencyDetailed(givenValidRequest);
        }

        // === Then 輔助方法 ===
        private void thenShouldThrowSameCurrencyException() {
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                exchangeRateService.convertCurrencyDetailed(givenValidRequest);
            });
            
            assertThat(exception.getMessage()).isEqualTo("Source and target currencies cannot be the same");
        }

        private void thenShouldThrowNegativeAmountException() {
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                exchangeRateService.convertCurrencyDetailed(givenValidRequest);
            });
            
            assertThat(exception.getMessage()).isEqualTo("Amount must be greater than 0");
        }

        private void thenShouldThrowUnsupportedCurrencyException() {
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                exchangeRateService.convertCurrencyDetailed(givenValidRequest);
            });
            
            assertThat(exception.getMessage()).isEqualTo("Unsupported currency code: XXX");
        }

        private void thenShouldPerformDirectConversionSuccessfully() {
            assertThat(whenConversionResult.getFromCurrency()).isEqualTo("USD");
            assertThat(whenConversionResult.getToCurrency()).isEqualTo("EUR");
            assertThat(whenConversionResult.getFromAmount()).isEqualByComparingTo(new BigDecimal("100"));
            assertThat(whenConversionResult.getToAmount()).isEqualByComparingTo(new BigDecimal("85.000000"));
            assertThat(whenConversionResult.getRate()).isEqualByComparingTo(new BigDecimal("0.85"));
            assertThat(whenConversionResult.getConversionDate()).isNotNull();
        }

        private void thenShouldPerformReverseConversionSuccessfully() {
            assertThat(whenConversionResult.getFromCurrency()).isEqualTo("USD");
            assertThat(whenConversionResult.getToCurrency()).isEqualTo("EUR");
            assertThat(whenConversionResult.getFromAmount()).isEqualByComparingTo(new BigDecimal("100"));
            assertThat(whenConversionResult.getToAmount().compareTo(new BigDecimal("84.0"))).isGreaterThan(0);
            assertThat(whenConversionResult.getRate()).isNotNull();
        }

        private void thenShouldPerformChainConversionSuccessfully() {
            assertThat(whenConversionResult.getFromCurrency()).isEqualTo("EUR");
            assertThat(whenConversionResult.getToCurrency()).isEqualTo("JPY");
            assertThat(whenConversionResult.getFromAmount()).isEqualByComparingTo(new BigDecimal("100"));
            assertThat(whenConversionResult.getToAmount()).isEqualByComparingTo(new BigDecimal("12980.000000"));
            assertThat(whenConversionResult.getConversionPath()).isEqualTo("EUR→USD→JPY");
        }

        private void thenShouldThrowNoConversionPathException() {
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                exchangeRateService.convertCurrencyDetailed(givenValidRequest);
            });
            
            assertThat(exception.getMessage()).isEqualTo("No exchange rate found for conversion");
        }
    }

    @Nested
    @DisplayName("儲存匯率測試")
    class SaveExchangeRateTests {

        @Test
        @DisplayName("GIVEN: 有效的新匯率 WHEN: 執行儲存 THEN: 應該成功儲存")
        void shouldSaveExchangeRateSuccessfully() {
            // Given - 準備有效的新匯率資料
            givenValidNewExchangeRate();
            
            // When - 執行儲存操作
            whenSavingExchangeRate();
            
            // Then - 驗證儲存結果
            thenShouldSaveSuccessfully();
        }

        @Test
        @DisplayName("GIVEN: 相同貨幣的匯率 WHEN: 執行儲存 THEN: 應該拋出異常")
        void shouldThrowExceptionForSameCurrencies() {
            // Given - 準備相同貨幣的無效匯率
            givenSameCurrencyRate();
            
            // When & Then - 驗證異常處理
            thenShouldThrowSameCurrencyException();
        }

        @Test
        @DisplayName("GIVEN: 負數或零匯率 WHEN: 執行儲存 THEN: 應該拋出異常")
        void shouldThrowExceptionForInvalidRate() {
            // Given - 準備無效的匯率值
            givenInvalidRateValue();
            
            // When & Then - 驗證無效匯率異常
            thenShouldThrowInvalidRateException();
        }

        @Test
        @DisplayName("GIVEN: 重複匯率對 WHEN: 執行儲存 THEN: 應該拋出異常")
        void shouldThrowExceptionForDuplicateRatePair() {
            // Given - 準備重複的匯率對
            givenDuplicateRatePair();
            
            // When & Then - 驗證重複匯率對異常
            thenShouldThrowDuplicateRatePairException();
        }

        // === Given 輔助方法 ===
        private void givenValidNewExchangeRate() {
            ExchangeRate newRate = new ExchangeRate();
            newRate.setFromCurrency("USD");  // 應該與服務返回的大寫一致
            newRate.setToCurrency("GBP");  // 應該與服務返回的大寫一致
            newRate.setRate(new BigDecimal("0.75"));
            newRate.setSource("test");
            givenValidRequest = new ConversionRequest(); // 重用此變數儲存測試匯率
            
            when(exchangeRateRepository.findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc("USD", "GBP"))
                .thenReturn(Optional.empty());
            when(exchangeRateRepository.save(any(ExchangeRate.class))).thenReturn(newRate);
        }

        private void givenSameCurrencyRate() {
            // 將在 Then 方法中創建，因為是異常情況
        }

        private void givenInvalidRateValue() {
            // 將在 Then 方法中創建，因為是異常情況
        }

        private void givenDuplicateRatePair() {
            when(exchangeRateRepository.findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc("USD", "EUR"))
                .thenReturn(Optional.of(givenUsdToEur));
        }

        // === When 輔助方法 ===
        private void whenSavingExchangeRate() {
            ExchangeRate newRate = new ExchangeRate();
            newRate.setFromCurrency("usd");  // 輸入小寫，服務會轉為大寫
            newRate.setToCurrency("gbp");  // 輸入小寫，服務會轉為大寫
            newRate.setRate(new BigDecimal("0.75"));
            newRate.setSource("test");
            
            whenSavedRate = exchangeRateService.saveExchangeRate(newRate);
        }

        // === Then 輔助方法 ===
        private void thenShouldSaveSuccessfully() {
            assertThat(whenSavedRate.getFromCurrency()).isEqualTo("USD");
            assertThat(whenSavedRate.getToCurrency()).isEqualTo("GBP");
            assertThat(whenSavedRate.getRate()).isEqualByComparingTo(new BigDecimal("0.75"));
            verify(exchangeRateRepository).save(any(ExchangeRate.class));
        }

        private void thenShouldThrowSameCurrencyException() {
            ExchangeRate invalidRate = new ExchangeRate();
            invalidRate.setFromCurrency("USD");
            invalidRate.setToCurrency("USD");
            invalidRate.setRate(new BigDecimal("1.0"));
            
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                exchangeRateService.saveExchangeRate(invalidRate);
            });
            
            assertThat(exception.getMessage()).isEqualTo("Source and target currencies cannot be the same");
        }

        private void thenShouldThrowInvalidRateException() {
            ExchangeRate invalidRate = new ExchangeRate();
            invalidRate.setFromCurrency("USD");
            invalidRate.setToCurrency("EUR");
            invalidRate.setRate(BigDecimal.ZERO);
            
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                exchangeRateService.saveExchangeRate(invalidRate);
            });
            
            assertThat(exception.getMessage()).isEqualTo("Exchange rate must be greater than 0");
        }

        private void thenShouldThrowDuplicateRatePairException() {
            ExchangeRate duplicateRate = new ExchangeRate();
            duplicateRate.setFromCurrency("USD");
            duplicateRate.setToCurrency("EUR");
            duplicateRate.setRate(new BigDecimal("0.85"));
            
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                exchangeRateService.saveExchangeRate(duplicateRate);
            });
            
            assertThat(exception.getMessage()).isEqualTo("Exchange rate already exists for this currency pair");
        }
    }

    @Nested
    @DisplayName("更新匯率測試")
    class UpdateExchangeRateTests {

        @Test
        @DisplayName("GIVEN: 存在的匯率ID WHEN: 執行更新 THEN: 應該成功更新")
        void shouldUpdateExchangeRateSuccessfully() {
            // Given - 準備更新測試資料
            givenExistingRateForUpdate();
            
            // When - 執行更新操作
            whenUpdatingExchangeRate();
            
            // Then - 驗證更新結果
            thenShouldUpdateSuccessfully();
        }

        @Test
        @DisplayName("GIVEN: 不存在的匯率ID WHEN: 執行更新 THEN: 應該拋出異常")
        void shouldThrowExceptionWhenUpdatingNonExistentRate() {
            // Given - 準備不存在的匯率ID
            givenNonExistentRateForUpdate();
            
            // When & Then - 驗證不存在匯率異常
            thenShouldThrowNonExistentRateException();
        }

        @Test
        @DisplayName("GIVEN: 無效的更新匯率值 WHEN: 執行更新 THEN: 應該拋出異常")
        void shouldThrowExceptionForInvalidRateUpdate() {
            // Given - 準備無效的更新匯率值
            givenInvalidRateForUpdate();
            
            // When & Then - 驗證無效更新匯率異常
            thenShouldThrowInvalidUpdateRateException();
        }

        @Test
        @DisplayName("GIVEN: 有效的貨幣對 WHEN: 根據貨幣對更新匯率 THEN: 應該成功更新")
        void shouldUpdateExchangeRateByPair() {
            // Given - 準備貨幣對更新資料
            givenValidCurrencyPairForUpdate();
            
            // When - 執行貨幣對更新
            whenUpdatingByPair();
            
            // Then - 驗證貨幣對更新結果
            thenShouldUpdateByPairSuccessfully();
        }

        // === Given 輔助方法 ===
        private void givenExistingRateForUpdate() {
            when(exchangeRateRepository.findById(1L)).thenReturn(Optional.of(givenUsdToEur));
            when(exchangeRateRepository.save(any(ExchangeRate.class))).thenReturn(givenUsdToEur);
        }

        private void givenNonExistentRateForUpdate() {
            when(exchangeRateRepository.findById(999L)).thenReturn(Optional.empty());
        }

        private void givenInvalidRateForUpdate() {
            when(exchangeRateRepository.findById(1L)).thenReturn(Optional.of(givenUsdToEur));
        }

        private void givenValidCurrencyPairForUpdate() {
            when(exchangeRateRepository.findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc("USD", "EUR"))
                .thenReturn(Optional.of(givenUsdToEur));
            when(exchangeRateRepository.save(any(ExchangeRate.class))).thenReturn(givenUsdToEur);
        }

        // === When 輔助方法 ===
        private void whenUpdatingExchangeRate() {
            ExchangeRate updatedRate = new ExchangeRate();
            updatedRate.setFromCurrency("USD");
            updatedRate.setToCurrency("EUR");
            updatedRate.setRate(new BigDecimal("0.87"));
            updatedRate.setSource("updated");
            
            whenSavedRate = exchangeRateService.updateExchangeRate(1L, updatedRate);
        }

        private void whenUpdatingByPair() {
            Map<String, Object> updates = new HashMap<>();
            updates.put("rate", "0.88");
            
            whenSavedRate = exchangeRateService.updateExchangeRateByPair("USD", "EUR", updates);
        }

        // === Then 輔助方法 ===
        private void thenShouldUpdateSuccessfully() {
            verify(exchangeRateRepository).findById(1L);
            verify(exchangeRateRepository).save(any(ExchangeRate.class));
            assertThat(whenSavedRate).isNotNull();
        }

        private void thenShouldThrowNonExistentRateException() {
            ExchangeRate updatedRate = new ExchangeRate();
            updatedRate.setFromCurrency("USD");
            updatedRate.setToCurrency("EUR");
            updatedRate.setRate(new BigDecimal("0.87"));
            
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                exchangeRateService.updateExchangeRate(999L, updatedRate);
            });
            
            assertThat(exception.getMessage()).isEqualTo("No exchange rate found for conversion");
        }

        private void thenShouldThrowInvalidUpdateRateException() {
            ExchangeRate updatedRate = new ExchangeRate();
            updatedRate.setFromCurrency("USD");
            updatedRate.setToCurrency("EUR");
            updatedRate.setRate(new BigDecimal("-0.5"));
            
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                exchangeRateService.updateExchangeRate(1L, updatedRate);
            });
            
            assertThat(exception.getMessage()).isEqualTo("Exchange rate must be greater than 0");
        }

        private void thenShouldUpdateByPairSuccessfully() {
            verify(exchangeRateRepository).save(any(ExchangeRate.class));
            assertThat(whenSavedRate).isNotNull();
        }
    }

    @Nested
    @DisplayName("刪除匯率測試")
    class DeleteExchangeRateTests {

        @Test
        @DisplayName("GIVEN: 有效的匯率ID WHEN: 根據ID刪除匯率 THEN: 應該成功刪除")
        void shouldDeleteExchangeRateById() {
            // Given - 準備刪除ID
            givenValidRateIdForDeletion();
            
            // When - 執行ID刪除
            whenDeletingById();
            
            // Then - 驗證ID刪除結果
            thenShouldDeleteByIdSuccessfully();
        }

        @Test
        @DisplayName("GIVEN: 存在的貨幣對 WHEN: 根據貨幣對刪除匯率 THEN: 應該成功刪除")
        void shouldDeleteExchangeRateByPair() {
            // Given - 準備貨幣對刪除資料
            givenValidCurrencyPairForDeletion();
            
            // When - 執行貨幣對刪除
            whenDeletingByPair();
            
            // Then - 驗證貨幣對刪除結果
            thenShouldDeleteByPairSuccessfully();
        }

        @Test
        @DisplayName("GIVEN: 不存在的貨幣對 WHEN: 根據貨幣對刪除 THEN: 應該拋出異常")
        void shouldThrowExceptionWhenDeletingNonExistentPair() {
            // Given - 準備不存在的貨幣對
            givenNonExistentCurrencyPairForDeletion();
            
            // When & Then - 驗證不存在貨幣對刪除異常
            thenShouldThrowNonExistentPairDeletionException();
        }

        // === Given 輔助方法 ===
        private void givenValidRateIdForDeletion() {
            // 無需特別準備，直接刪除
        }

        private void givenValidCurrencyPairForDeletion() {
            when(exchangeRateRepository.findByFromCurrencyAndToCurrency("USD", "EUR"))
                .thenReturn(Collections.singletonList(givenUsdToEur));
        }

        private void givenNonExistentCurrencyPairForDeletion() {
            when(exchangeRateRepository.findByFromCurrencyAndToCurrency("USD", "CNY"))
                .thenReturn(Collections.emptyList());
        }

        // === When 輔助方法 ===
        private void whenDeletingById() {
            exchangeRateService.deleteExchangeRate(1L);
        }

        private void whenDeletingByPair() {
            exchangeRateService.deleteExchangeRateByPair("USD", "EUR");
        }

        // === Then 輔助方法 ===
        private void thenShouldDeleteByIdSuccessfully() {
            verify(exchangeRateRepository).deleteById(1L);
        }

        private void thenShouldDeleteByPairSuccessfully() {
            verify(exchangeRateRepository).findByFromCurrencyAndToCurrency("USD", "EUR");
            verify(exchangeRateRepository).deleteAll(any(List.class));
        }

        private void thenShouldThrowNonExistentPairDeletionException() {
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                exchangeRateService.deleteExchangeRateByPair("USD", "CNY");
            });
            
            assertThat(exception.getMessage()).isEqualTo("No exchange rate found for conversion");
        }
    }

    @Nested
    @DisplayName("查詢特定貨幣匯率測試")
    class GetExchangeRatesByCurrencyTests {

        @Test
        @DisplayName("GIVEN: 有效的來源貨幣 WHEN: 根據來源貨幣查詢匯率 THEN: 應該返回匹配的匯率")
        void shouldGetExchangeRatesByFromCurrency() {
            // Given - 準備來源貨幣查詢資料
            givenValidFromCurrencyQueryData();
            
            // When - 執行來源貨幣查詢
            whenQueryingByFromCurrency();
            
            // Then - 驗證來源貨幣查詢結果
            thenShouldReturnMatchingFromCurrencyRates();
        }

        @Test
        @DisplayName("GIVEN: 有效的目標貨幣 WHEN: 根據目標貨幣查詢匯率 THEN: 應該返回匹配的匯率")
        void shouldGetExchangeRatesByToCurrency() {
            // Given - 準備目標貨幣查詢資料
            givenValidToCurrencyQueryData();
            
            // When - 執行目標貨幣查詢
            whenQueryingByToCurrency();
            
            // Then - 驗證目標貨幣查詢結果
            thenShouldReturnMatchingToCurrencyRates();
        }

        // === Given 輔助方法 ===
        private void givenValidFromCurrencyQueryData() {
            List<ExchangeRate> usdRates = Arrays.asList(givenUsdToEur, givenUsdToJpy);
            when(exchangeRateRepository.findByFromCurrency("USD")).thenReturn(usdRates);
        }

        private void givenValidToCurrencyQueryData() {
            List<ExchangeRate> eurRates = Collections.singletonList(givenUsdToEur);
            when(exchangeRateRepository.findByToCurrency("EUR")).thenReturn(eurRates);
        }

        // === When 輔助方法 ===
        private void whenQueryingByFromCurrency() {
            whenResultRates = exchangeRateService.getExchangeRatesByFromCurrency("usd");
        }

        private void whenQueryingByToCurrency() {
            whenResultRates = exchangeRateService.getExchangeRatesByToCurrency("eur");
        }

        // === Then 輔助方法 ===
        private void thenShouldReturnMatchingFromCurrencyRates() {
            assertThat(whenResultRates).hasSize(2);
            verify(exchangeRateRepository).findByFromCurrency("USD");
        }

        private void thenShouldReturnMatchingToCurrencyRates() {
            assertThat(whenResultRates).hasSize(1);
            verify(exchangeRateRepository).findByToCurrency("EUR");
        }
    }
}