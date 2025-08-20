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

@ExtendWith(MockitoExtension.class)
@DisplayName("ExchangeRateService 單元測試")
class ExchangeRateServiceTest {

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @InjectMocks
    private TestExchangeRateService exchangeRateService;

    private ExchangeRate usdToEur;
    private ExchangeRate eurToUsd;
    private ExchangeRate usdToJpy;
    private ConversionRequest validRequest;

    @BeforeEach
    void setUp() {
        usdToEur = new ExchangeRate();
        usdToEur.setId(1L);
        usdToEur.setFromCurrency("USD");
        usdToEur.setToCurrency("EUR");
        usdToEur.setRate(new BigDecimal("0.85"));
        usdToEur.setTimestamp(LocalDateTime.now());
        usdToEur.setSource("test");

        eurToUsd = new ExchangeRate();
        eurToUsd.setId(2L);
        eurToUsd.setFromCurrency("EUR");
        eurToUsd.setToCurrency("USD");
        eurToUsd.setRate(new BigDecimal("1.18"));
        eurToUsd.setTimestamp(LocalDateTime.now());
        eurToUsd.setSource("test");

        usdToJpy = new ExchangeRate();
        usdToJpy.setId(3L);
        usdToJpy.setFromCurrency("USD");
        usdToJpy.setToCurrency("JPY");
        usdToJpy.setRate(new BigDecimal("110.0"));
        usdToJpy.setTimestamp(LocalDateTime.now());
        usdToJpy.setSource("test");

        validRequest = new ConversionRequest();
        validRequest.setFromCurrency("USD");
        validRequest.setToCurrency("EUR");
        validRequest.setAmount(new BigDecimal("100"));
    }

    @Nested
    @DisplayName("取得所有匯率測試")
    class GetAllExchangeRatesTests {

        @Test
        @DisplayName("成功取得所有匯率")
        void shouldReturnAllExchangeRates() {
            // Given
            List<ExchangeRate> expectedRates = Arrays.asList(usdToEur, eurToUsd);
            when(exchangeRateRepository.findAll()).thenReturn(expectedRates);

            // When
            List<ExchangeRate> actualRates = exchangeRateService.getAllExchangeRates();

            // Then
            assertThat(actualRates).hasSize(2);
            assertThat(actualRates).containsExactly(usdToEur, eurToUsd);
            verify(exchangeRateRepository).findAll();
        }

        @Test
        @DisplayName("根據來源和目標貨幣篩選匯率")
        void shouldFilterByFromAndToCurrency() {
            // Given
            when(exchangeRateRepository.findByFromCurrencyAndToCurrency("USD", "EUR"))
                    .thenReturn(Collections.singletonList(usdToEur));

            // When
            List<ExchangeRate> actualRates = exchangeRateService.getAllExchangeRates("usd", "eur");

            // Then
            assertThat(actualRates).hasSize(1);
            assertThat(actualRates.get(0)).isEqualTo(usdToEur);
            verify(exchangeRateRepository).findByFromCurrencyAndToCurrency("USD", "EUR");
        }

        @Test
        @DisplayName("根據來源貨幣篩選匯率")
        void shouldFilterByFromCurrency() {
            // Given
            List<ExchangeRate> usdRates = Arrays.asList(usdToEur, usdToJpy);
            when(exchangeRateRepository.findByFromCurrency("USD")).thenReturn(usdRates);

            // When
            List<ExchangeRate> actualRates = exchangeRateService.getAllExchangeRates("usd", null);

            // Then
            assertThat(actualRates).hasSize(2);
            assertThat(actualRates).containsExactly(usdToEur, usdToJpy);
            verify(exchangeRateRepository).findByFromCurrency("USD");
        }

        @Test
        @DisplayName("分頁查詢所有匯率")
        void shouldReturnPagedExchangeRates() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            List<ExchangeRate> rates = Arrays.asList(usdToEur, eurToUsd);
            Page<ExchangeRate> expectedPage = new PageImpl<>(rates, pageable, 2);
            when(exchangeRateRepository.findAll(pageable)).thenReturn(expectedPage);

            // When
            Page<ExchangeRate> actualPage = exchangeRateService.getAllExchangeRates(null, null, pageable);

            // Then
            assertThat(actualPage.getContent()).hasSize(2);
            assertThat(actualPage.getTotalElements()).isEqualTo(2);
            verify(exchangeRateRepository).findAll(pageable);
        }
    }

    @Nested
    @DisplayName("取得最新匯率測試")
    class GetLatestRateTests {

        @Test
        @DisplayName("成功取得最新匯率")
        void shouldReturnLatestRate() {
            // Given
            when(exchangeRateRepository.findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc("USD", "EUR"))
                    .thenReturn(Optional.of(usdToEur));

            // When
            Optional<ExchangeRate> result = exchangeRateService.getLatestRate("USD", "EUR");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(usdToEur);
            verify(exchangeRateRepository).findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc("USD", "EUR");
        }

        @Test
        @DisplayName("找不到匯率時返回空值")
        void shouldReturnEmptyWhenRateNotFound() {
            // Given
            when(exchangeRateRepository.findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc("USD", "CNY"))
                    .thenReturn(Optional.empty());

            // When
            Optional<ExchangeRate> result = exchangeRateService.getLatestRate("USD", "CNY");

            // Then
            assertThat(result).isEmpty();
            verify(exchangeRateRepository).findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc("USD", "CNY");
        }
    }

    @Nested
    @DisplayName("貨幣轉換測試")
    class ConvertCurrencyTests {

        @Test
        @DisplayName("成功進行簡單貨幣轉換")
        void shouldConvertCurrencySuccessfully() {
            // Given
            BigDecimal amount = new BigDecimal("100");
            when(exchangeRateRepository.findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc("USD", "EUR"))
                    .thenReturn(Optional.of(usdToEur));

            // When
            BigDecimal result = exchangeRateService.convertCurrency("USD", "EUR", amount);

            // Then
            assertThat(result).isEqualByComparingTo(new BigDecimal("85.00"));
            verify(exchangeRateRepository).findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc("USD", "EUR");
        }

        @Test
        @DisplayName("找不到匯率時拋出異常")
        void shouldThrowExceptionWhenRateNotFound() {
            // Given
            BigDecimal amount = new BigDecimal("100");
            when(exchangeRateRepository.findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc("USD", "CNY"))
                    .thenReturn(Optional.empty());

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                exchangeRateService.convertCurrency("USD", "CNY", amount);
            });

            assertThat(exception.getMessage()).contains("No exchange rate found for conversion");
        }
    }

    @Nested
    @DisplayName("詳細貨幣轉換測試")
    class ConvertCurrencyDetailedTests {

        @Test
        @DisplayName("相同貨幣應拋出異常")
        void shouldThrowExceptionForSameCurrency() {
            // Given
            ConversionRequest request = new ConversionRequest();
            request.setFromCurrency("USD");
            request.setToCurrency("USD");
            request.setAmount(new BigDecimal("100"));

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                exchangeRateService.convertCurrencyDetailed(request);
            });

            assertThat(exception.getMessage()).isEqualTo("Source and target currencies cannot be the same");
        }

        @Test
        @DisplayName("負數金額應拋出異常")
        void shouldThrowExceptionForNegativeAmount() {
            // Given
            ConversionRequest request = new ConversionRequest();
            request.setFromCurrency("USD");
            request.setToCurrency("EUR");
            request.setAmount(new BigDecimal("-100"));

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                exchangeRateService.convertCurrencyDetailed(request);
            });

            assertThat(exception.getMessage()).isEqualTo("Amount must be greater than 0");
        }

        @Test
        @DisplayName("不支援的貨幣應拋出異常")
        void shouldThrowExceptionForUnsupportedCurrency() {
            // Given
            ConversionRequest request = new ConversionRequest();
            request.setFromCurrency("XXX");
            request.setToCurrency("EUR");
            request.setAmount(new BigDecimal("100"));

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                exchangeRateService.convertCurrencyDetailed(request);
            });

            assertThat(exception.getMessage()).isEqualTo("Unsupported currency code: XXX");
        }

        @Test
        @DisplayName("直接轉換成功")
        void shouldPerformDirectConversion() {
            // Given
            when(exchangeRateRepository.findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc("USD", "EUR"))
                    .thenReturn(Optional.of(usdToEur));

            // When
            ConversionResponse response = exchangeRateService.convertCurrencyDetailed(validRequest);

            // Then
            assertThat(response.getFromCurrency()).isEqualTo("USD");
            assertThat(response.getToCurrency()).isEqualTo("EUR");
            assertThat(response.getFromAmount()).isEqualByComparingTo(new BigDecimal("100"));
            assertThat(response.getToAmount()).isEqualByComparingTo(new BigDecimal("85.000000"));
            assertThat(response.getRate()).isEqualByComparingTo(new BigDecimal("0.85"));
            assertThat(response.getConversionDate()).isNotNull();
        }

        @Test
        @DisplayName("反向轉換成功")
        void shouldPerformReverseConversion() {
            // Given
            ConversionRequest request = new ConversionRequest();
            request.setFromCurrency("USD");
            request.setToCurrency("EUR");
            request.setAmount(new BigDecimal("100"));

            when(exchangeRateRepository.findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc("USD", "EUR"))
                    .thenReturn(Optional.empty());
            when(exchangeRateRepository.findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc("EUR", "USD"))
                    .thenReturn(Optional.of(eurToUsd));

            // When
            ConversionResponse response = exchangeRateService.convertCurrencyDetailed(request);

            // Then
            assertThat(response.getFromCurrency()).isEqualTo("USD");
            assertThat(response.getToCurrency()).isEqualTo("EUR");
            assertThat(response.getFromAmount()).isEqualByComparingTo(new BigDecimal("100"));
            // 100 * (1 / 1.18) = 84.745763
            assertThat(response.getToAmount().compareTo(new BigDecimal("84.0"))).isGreaterThan(0);
            assertThat(response.getRate()).isNotNull();
        }

        @Test
        @DisplayName("通過USD鏈式轉換成功")
        void shouldPerformChainConversionThroughUSD() {
            // Given
            ConversionRequest request = new ConversionRequest();
            request.setFromCurrency("EUR");
            request.setToCurrency("JPY");
            request.setAmount(new BigDecimal("100"));

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

            // When
            ConversionResponse response = exchangeRateService.convertCurrencyDetailed(request);

            // Then
            assertThat(response.getFromCurrency()).isEqualTo("EUR");
            assertThat(response.getToCurrency()).isEqualTo("JPY");
            assertThat(response.getFromAmount()).isEqualByComparingTo(new BigDecimal("100"));
            assertThat(response.getToAmount()).isEqualByComparingTo(new BigDecimal("12980.000000"));
            assertThat(response.getConversionPath()).isEqualTo("EUR→USD→JPY");
        }

        @Test
        @DisplayName("找不到匯率路徑時拋出異常")
        void shouldThrowExceptionWhenNoConversionPathFound() {
            // Given
            ConversionRequest request = new ConversionRequest();
            request.setFromCurrency("EUR");
            request.setToCurrency("JPY");
            request.setAmount(new BigDecimal("100"));

            when(exchangeRateRepository.findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc(anyString(), anyString()))
                    .thenReturn(Optional.empty());

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                exchangeRateService.convertCurrencyDetailed(request);
            });

            assertThat(exception.getMessage()).isEqualTo("No exchange rate found for conversion");
        }
    }

    @Nested
    @DisplayName("儲存匯率測試")
    class SaveExchangeRateTests {

        @Test
        @DisplayName("成功儲存新匯率")
        void shouldSaveExchangeRateSuccessfully() {
            // Given
            ExchangeRate newRate = new ExchangeRate();
            newRate.setFromCurrency("usd");
            newRate.setToCurrency("gbp");
            newRate.setRate(new BigDecimal("0.75"));
            newRate.setSource("test");

            when(exchangeRateRepository.findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc("USD", "GBP"))
                    .thenReturn(Optional.empty());
            when(exchangeRateRepository.save(any(ExchangeRate.class))).thenReturn(newRate);

            // When
            ExchangeRate savedRate = exchangeRateService.saveExchangeRate(newRate);

            // Then
            assertThat(savedRate.getFromCurrency()).isEqualTo("USD");
            assertThat(savedRate.getToCurrency()).isEqualTo("GBP");
            assertThat(savedRate.getRate()).isEqualByComparingTo(new BigDecimal("0.75"));
            verify(exchangeRateRepository).save(any(ExchangeRate.class));
        }

        @Test
        @DisplayName("相同貨幣應拋出異常")
        void shouldThrowExceptionForSameCurrencies() {
            // Given
            ExchangeRate invalidRate = new ExchangeRate();
            invalidRate.setFromCurrency("USD");
            invalidRate.setToCurrency("USD");
            invalidRate.setRate(new BigDecimal("1.0"));

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                exchangeRateService.saveExchangeRate(invalidRate);
            });

            assertThat(exception.getMessage()).isEqualTo("Source and target currencies cannot be the same");
        }

        @Test
        @DisplayName("負數或零匯率應拋出異常")
        void shouldThrowExceptionForInvalidRate() {
            // Given
            ExchangeRate invalidRate = new ExchangeRate();
            invalidRate.setFromCurrency("USD");
            invalidRate.setToCurrency("EUR");
            invalidRate.setRate(BigDecimal.ZERO);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                exchangeRateService.saveExchangeRate(invalidRate);
            });

            assertThat(exception.getMessage()).isEqualTo("Exchange rate must be greater than 0");
        }

        @Test
        @DisplayName("重複匯率組合應拋出異常")
        void shouldThrowExceptionForDuplicateRatePair() {
            // Given
            ExchangeRate duplicateRate = new ExchangeRate();
            duplicateRate.setFromCurrency("USD");
            duplicateRate.setToCurrency("EUR");
            duplicateRate.setRate(new BigDecimal("0.85"));

            when(exchangeRateRepository.findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc("USD", "EUR"))
                    .thenReturn(Optional.of(usdToEur));

            // When & Then
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
        @DisplayName("成功更新匯率")
        void shouldUpdateExchangeRateSuccessfully() {
            // Given
            ExchangeRate updatedRate = new ExchangeRate();
            updatedRate.setFromCurrency("USD");
            updatedRate.setToCurrency("EUR");
            updatedRate.setRate(new BigDecimal("0.87"));
            updatedRate.setSource("updated");

            when(exchangeRateRepository.findById(1L)).thenReturn(Optional.of(usdToEur));
            when(exchangeRateRepository.save(any(ExchangeRate.class))).thenReturn(usdToEur);

            // When
            ExchangeRate result = exchangeRateService.updateExchangeRate(1L, updatedRate);

            // Then
            verify(exchangeRateRepository).findById(1L);
            verify(exchangeRateRepository).save(any(ExchangeRate.class));
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("更新不存在的匯率應拋出異常")
        void shouldThrowExceptionWhenUpdatingNonExistentRate() {
            // Given
            ExchangeRate updatedRate = new ExchangeRate();
            updatedRate.setFromCurrency("USD");
            updatedRate.setToCurrency("EUR");
            updatedRate.setRate(new BigDecimal("0.87"));

            when(exchangeRateRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                exchangeRateService.updateExchangeRate(999L, updatedRate);
            });

            assertThat(exception.getMessage()).isEqualTo("No exchange rate found for conversion");
        }

        @Test
        @DisplayName("更新為無效匯率應拋出異常")
        void shouldThrowExceptionForInvalidRateUpdate() {
            // Given
            ExchangeRate updatedRate = new ExchangeRate();
            updatedRate.setFromCurrency("USD");
            updatedRate.setToCurrency("EUR");
            updatedRate.setRate(new BigDecimal("-0.5"));

            when(exchangeRateRepository.findById(1L)).thenReturn(Optional.of(usdToEur));

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                exchangeRateService.updateExchangeRate(1L, updatedRate);
            });

            assertThat(exception.getMessage()).isEqualTo("Exchange rate must be greater than 0");
        }

        @Test
        @DisplayName("根據貨幣對更新匯率")
        void shouldUpdateExchangeRateByPair() {
            // Given
            Map<String, Object> updates = new HashMap<>();
            updates.put("rate", "0.88");

            when(exchangeRateRepository.findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc("USD", "EUR"))
                    .thenReturn(Optional.of(usdToEur));
            when(exchangeRateRepository.save(any(ExchangeRate.class))).thenReturn(usdToEur);

            // When
            ExchangeRate result = exchangeRateService.updateExchangeRateByPair("USD", "EUR", updates);

            // Then
            verify(exchangeRateRepository).save(any(ExchangeRate.class));
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("刪除匯率測試")
    class DeleteExchangeRateTests {

        @Test
        @DisplayName("根據ID刪除匯率")
        void shouldDeleteExchangeRateById() {
            // When
            exchangeRateService.deleteExchangeRate(1L);

            // Then
            verify(exchangeRateRepository).deleteById(1L);
        }

        @Test
        @DisplayName("根據貨幣對刪除匯率")
        void shouldDeleteExchangeRateByPair() {
            // Given
            when(exchangeRateRepository.findByFromCurrencyAndToCurrency("USD", "EUR"))
                    .thenReturn(Collections.singletonList(usdToEur));

            // When
            exchangeRateService.deleteExchangeRateByPair("USD", "EUR");

            // Then
            verify(exchangeRateRepository).findByFromCurrencyAndToCurrency("USD", "EUR");
            verify(exchangeRateRepository).deleteAll(any(List.class));
        }

        @Test
        @DisplayName("刪除不存在的貨幣對應拋出異常")
        void shouldThrowExceptionWhenDeletingNonExistentPair() {
            // Given
            when(exchangeRateRepository.findByFromCurrencyAndToCurrency("USD", "CNY"))
                    .thenReturn(Collections.emptyList());

            // When & Then
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
        @DisplayName("根據來源貨幣查詢匯率")
        void shouldGetExchangeRatesByFromCurrency() {
            // Given
            List<ExchangeRate> usdRates = Arrays.asList(usdToEur, usdToJpy);
            when(exchangeRateRepository.findByFromCurrency("USD")).thenReturn(usdRates);

            // When
            List<ExchangeRate> result = exchangeRateService.getExchangeRatesByFromCurrency("usd");

            // Then
            assertThat(result).hasSize(2);
            verify(exchangeRateRepository).findByFromCurrency("USD");
        }

        @Test
        @DisplayName("根據目標貨幣查詢匯率")
        void shouldGetExchangeRatesByToCurrency() {
            // Given
            List<ExchangeRate> eurRates = Collections.singletonList(usdToEur);
            when(exchangeRateRepository.findByToCurrency("EUR")).thenReturn(eurRates);

            // When
            List<ExchangeRate> result = exchangeRateService.getExchangeRatesByToCurrency("eur");

            // Then
            assertThat(result).hasSize(1);
            verify(exchangeRateRepository).findByToCurrency("EUR");
        }
    }
}