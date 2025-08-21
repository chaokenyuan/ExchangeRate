package com.exchangerate.repository;

import com.exchangerate.model.ExchangeRate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ExchangeRateRepository 數據層測試
 * 
 * 使用 @DataJpaTest 進行 JPA 層測試
 * 自動配置內存數據庫（H2）
 * 每個測試方法都在事務中執行並自動回滾
 */
@DataJpaTest
@DisplayName("ExchangeRateRepository 數據層測試")
class ExchangeRateRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ExchangeRateRepository repository;

    private ExchangeRate testExchangeRate;
    private ExchangeRate usdToEur;
    private ExchangeRate eurToUsd;
    private ExchangeRate usdToGbp;

    @BeforeEach
    void setUp() {
        // 清理數據庫
        entityManager.clear();
        
        // 準備測試數據
        testExchangeRate = ExchangeRate.builder()
                .fromCurrency("USD")
                .toCurrency("EUR")
                .rate(new BigDecimal("0.85"))
                .source("Test Bank")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("基本 CRUD 操作測試")
    class BasicCrudTests {

        @Test
        @DisplayName("應該成功保存匯率數據")
        void shouldSaveExchangeRate() {
            // When
            ExchangeRate saved = repository.save(testExchangeRate);
            entityManager.flush();

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getFromCurrency()).isEqualTo("USD");
            assertThat(saved.getToCurrency()).isEqualTo("EUR");
            assertThat(saved.getRate()).isEqualByComparingTo(new BigDecimal("0.85"));
            assertThat(saved.getSource()).isEqualTo("Test Bank");
            assertThat(saved.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("應該根據 ID 查詢匯率")
        void shouldFindById() {
            // Given
            ExchangeRate saved = entityManager.persistAndFlush(testExchangeRate);

            // When
            Optional<ExchangeRate> found = repository.findById(saved.getId());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getFromCurrency()).isEqualTo("USD");
            assertThat(found.get().getToCurrency()).isEqualTo("EUR");
        }

        @Test
        @DisplayName("應該更新匯率數據")
        void shouldUpdateExchangeRate() {
            // Given
            ExchangeRate saved = entityManager.persistAndFlush(testExchangeRate);

            // When
            saved.setRate(new BigDecimal("0.90"));
            saved.setSource("Updated Bank");
            ExchangeRate updated = repository.save(saved);
            entityManager.flush();

            // Then
            ExchangeRate found = entityManager.find(ExchangeRate.class, updated.getId());
            assertThat(found.getRate()).isEqualByComparingTo(new BigDecimal("0.90"));
            assertThat(found.getSource()).isEqualTo("Updated Bank");
        }

        @Test
        @DisplayName("應該刪除匯率數據")
        void shouldDeleteExchangeRate() {
            // Given
            ExchangeRate saved = entityManager.persistAndFlush(testExchangeRate);
            Long id = saved.getId();

            // When
            repository.deleteById(id);
            entityManager.flush();

            // Then
            Optional<ExchangeRate> found = repository.findById(id);
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("應該查詢所有匯率")
        void shouldFindAllExchangeRates() {
            // Given
            entityManager.persist(testExchangeRate);
            entityManager.persist(ExchangeRate.builder()
                    .fromCurrency("EUR")
                    .toCurrency("USD")
                    .rate(new BigDecimal("1.18"))
                    .source("Test Bank")
                    .timestamp(LocalDateTime.now())
                    .build());
            entityManager.persist(ExchangeRate.builder()
                    .fromCurrency("USD")
                    .toCurrency("GBP")
                    .rate(new BigDecimal("0.79"))
                    .source("Test Bank")
                    .timestamp(LocalDateTime.now())
                    .build());
            entityManager.flush();

            // When
            List<ExchangeRate> all = repository.findAll();

            // Then
            assertThat(all).hasSize(3);
        }
    }

    @Nested
    @DisplayName("自定義查詢方法測試")
    class CustomQueryTests {

        @BeforeEach
        void setUpTestData() {
            usdToEur = entityManager.persist(ExchangeRate.builder()
                    .fromCurrency("USD")
                    .toCurrency("EUR")
                    .rate(new BigDecimal("0.85"))
                    .source("Test Bank")
                    .timestamp(LocalDateTime.now())
                    .build());
            
            eurToUsd = entityManager.persist(ExchangeRate.builder()
                    .fromCurrency("EUR")
                    .toCurrency("USD")
                    .rate(new BigDecimal("1.18"))
                    .source("Test Bank")
                    .timestamp(LocalDateTime.now())
                    .build());
            
            usdToGbp = entityManager.persist(ExchangeRate.builder()
                    .fromCurrency("USD")
                    .toCurrency("GBP")
                    .rate(new BigDecimal("0.79"))
                    .source("Test Bank")
                    .timestamp(LocalDateTime.now())
                    .build());
            
            entityManager.flush();
        }

        @Test
        @DisplayName("應該根據貨幣對查詢匯率")
        void shouldFindByFromCurrencyAndToCurrency() {
            // When
            Optional<ExchangeRate> found = repository.findByFromCurrencyAndToCurrency("USD", "EUR");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getRate()).isEqualByComparingTo(new BigDecimal("0.85"));
        }

        @Test
        @DisplayName("應該查詢不存在的貨幣對時返回空")
        void shouldReturnEmptyForNonExistentCurrencyPair() {
            // When
            Optional<ExchangeRate> found = repository.findByFromCurrencyAndToCurrency("JPY", "CNY");

            // Then
            assertThat(found).isEmpty();
        }

        @ParameterizedTest
        @CsvSource({
            "USD, EUR, 0.85",
            "EUR, USD, 1.18",
            "USD, GBP, 0.79"
        })
        @DisplayName("應該正確查詢各種貨幣對")
        void shouldFindVariousCurrencyPairs(String from, String to, String expectedRate) {
            // When
            Optional<ExchangeRate> found = repository.findByFromCurrencyAndToCurrency(from, to);

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getRate()).isEqualByComparingTo(new BigDecimal(expectedRate));
        }

        @Test
        @DisplayName("應該根據來源貨幣查詢所有匯率")
        void shouldFindByFromCurrency() {
            // When
            List<ExchangeRate> usdRates = repository.findByFromCurrency("USD");

            // Then
            assertThat(usdRates).hasSize(2);
            assertThat(usdRates).extracting(ExchangeRate::getFromCurrency)
                    .containsOnly("USD");
            assertThat(usdRates).extracting(ExchangeRate::getToCurrency)
                    .containsExactlyInAnyOrder("EUR", "GBP");
        }

        @Test
        @DisplayName("應該根據目標貨幣查詢所有匯率")
        void shouldFindByToCurrency() {
            // When
            List<ExchangeRate> toUsdRates = repository.findByToCurrency("USD");

            // Then
            assertThat(toUsdRates).hasSize(1);
            assertThat(toUsdRates.get(0).getFromCurrency()).isEqualTo("EUR");
            assertThat(toUsdRates.get(0).getToCurrency()).isEqualTo("USD");
        }
    }

    @Nested
    @DisplayName("分頁與排序測試")
    class PaginationAndSortingTests {

        @BeforeEach
        void setUpLargeDataSet() {
            // 創建多筆測試數據 - 使用標準3字符貨幣代碼
            String[] currencyCodes = {"EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY", "KRW", "SGD", "HKD", "INR", "BRL", "MXN", "RUB", "ZAR"};
            for (int i = 0; i < currencyCodes.length; i++) {
                entityManager.persist(ExchangeRate.builder()
                        .fromCurrency("USD")
                        .toCurrency(currencyCodes[i])
                        .rate(new BigDecimal(String.format("%.2f", 0.5 + (i + 1) * 0.1)))
                        .source("Test Bank " + (i + 1))
                        .timestamp(LocalDateTime.now().minusDays(i + 1))
                        .build());
            }
            entityManager.flush();
        }

        @Test
        @DisplayName("應該支援分頁查詢")
        void shouldSupportPagination() {
            // Given
            Pageable pageable = PageRequest.of(0, 5);

            // When
            Page<ExchangeRate> page = repository.findAll(pageable);

            // Then
            assertThat(page.getContent()).hasSize(5);
            assertThat(page.getTotalElements()).isEqualTo(15);
            assertThat(page.getTotalPages()).isEqualTo(3);
            assertThat(page.isFirst()).isTrue();
            assertThat(page.hasNext()).isTrue();
        }

        @Test
        @DisplayName("應該查詢指定頁面")
        void shouldFetchSpecificPage() {
            // Given
            Pageable secondPage = PageRequest.of(1, 5);

            // When
            Page<ExchangeRate> page = repository.findAll(secondPage);

            // Then
            assertThat(page.getContent()).hasSize(5);
            assertThat(page.getNumber()).isEqualTo(1);
            assertThat(page.isFirst()).isFalse();
            assertThat(page.isLast()).isFalse();
        }

        @Test
        @DisplayName("應該處理最後一頁")
        void shouldHandleLastPage() {
            // Given
            Pageable lastPage = PageRequest.of(2, 5);

            // When
            Page<ExchangeRate> page = repository.findAll(lastPage);

            // Then
            assertThat(page.getContent()).hasSize(5);
            assertThat(page.isLast()).isTrue();
            assertThat(page.hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("數據驗證測試")
    class DataValidationTests {

        @Test
        @DisplayName("應該拒絕空的來源貨幣")
        void shouldRejectNullFromCurrency() {
            // Given
            ExchangeRate invalid = ExchangeRate.builder()
                    .fromCurrency(null)
                    .toCurrency("EUR")
                    .rate(new BigDecimal("0.85"))
                    .source("Test Bank")
                    .timestamp(LocalDateTime.now())
                    .build();

            // When & Then
            assertThatThrownBy(() -> {
                repository.saveAndFlush(invalid);
            }).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("應該拒絕空的目標貨幣")
        void shouldRejectNullToCurrency() {
            // Given
            ExchangeRate invalid = ExchangeRate.builder()
                    .fromCurrency("USD")
                    .toCurrency(null)
                    .rate(new BigDecimal("0.85"))
                    .source("Test Bank")
                    .timestamp(LocalDateTime.now())
                    .build();

            // When & Then
            assertThatThrownBy(() -> {
                repository.saveAndFlush(invalid);
            }).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("應該拒絕空的匯率")
        void shouldRejectNullRate() {
            // Given
            ExchangeRate invalid = ExchangeRate.builder()
                    .fromCurrency("USD")
                    .toCurrency("EUR")
                    .rate(null)
                    .source("Test Bank")
                    .timestamp(LocalDateTime.now())
                    .build();

            // When & Then
            assertThatThrownBy(() -> {
                repository.saveAndFlush(invalid);
            }).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("應該接受沒有來源的數據")
        void shouldAcceptNullSource() {
            // Given
            ExchangeRate withoutSource = ExchangeRate.builder()
                    .fromCurrency("USD")
                    .toCurrency("EUR")
                    .rate(new BigDecimal("0.85"))
                    .source(null)
                    .timestamp(LocalDateTime.now())
                    .build();

            // When
            ExchangeRate saved = repository.saveAndFlush(withoutSource);

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getSource()).isNull();
        }
    }

    @Nested
    @DisplayName("複雜查詢測試")
    class ComplexQueryTests {

        @Test
        @DisplayName("應該支援 exists 查詢")
        void shouldSupportExistsQuery() {
            // Given
            entityManager.persistAndFlush(testExchangeRate);

            // When
            boolean exists = repository.existsByFromCurrencyAndToCurrency("USD", "EUR");
            boolean notExists = repository.existsByFromCurrencyAndToCurrency("JPY", "CNY");

            // Then
            assertThat(exists).isTrue();
            assertThat(notExists).isFalse();
        }

        @Test
        @DisplayName("應該支援 count 查詢")
        void shouldSupportCountQuery() {
            // Given
            entityManager.persist(testExchangeRate);
            entityManager.persist(ExchangeRate.builder()
                    .fromCurrency("USD")
                    .toCurrency("GBP")
                    .rate(new BigDecimal("0.79"))
                    .source("Test Bank")
                    .timestamp(LocalDateTime.now())
                    .build());
            entityManager.flush();

            // When
            long totalCount = repository.count();
            long usdCount = repository.countByFromCurrency("USD");

            // Then
            assertThat(totalCount).isEqualTo(2);
            assertThat(usdCount).isEqualTo(2);
        }

        @Test
        @DisplayName("應該支援批量刪除")
        void shouldSupportBatchDelete() {
            // Given
            List<ExchangeRate> rates = List.of(
                    ExchangeRate.builder()
                            .fromCurrency("USD")
                            .toCurrency("EUR")
                            .rate(new BigDecimal("0.85"))
                            .source("Test Bank")
                            .timestamp(LocalDateTime.now())
                            .build(),
                    ExchangeRate.builder()
                            .fromCurrency("EUR")
                            .toCurrency("USD")
                            .rate(new BigDecimal("1.18"))
                            .source("Test Bank")
                            .timestamp(LocalDateTime.now())
                            .build()
            );
            List<ExchangeRate> saved = repository.saveAllAndFlush(rates);

            // When
            repository.deleteAllInBatch(saved);
            entityManager.flush();

            // Then
            long count = repository.count();
            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("事務測試")
    class TransactionTests {

        @Test
        @DisplayName("應該支援事務回滾")
        void shouldSupportTransactionRollback() {
            // Given
            long initialCount = repository.count();

            // When - 這個測試會自動回滾
            repository.save(testExchangeRate);
            entityManager.flush();

            // Then - 在測試方法內可以看到變更
            long afterSaveCount = repository.count();
            assertThat(afterSaveCount).isEqualTo(initialCount + 1);
            
            // 測試結束後會自動回滾，不影響其他測試
        }
    }
}