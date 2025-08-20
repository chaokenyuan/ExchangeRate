package com.exchangerate.domain.model.entity;

import com.exchangerate.domain.model.valueobject.CurrencyPair;
import com.exchangerate.domain.model.valueobject.Rate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

class ExchangeRateTest {

    @Test
    void should_CreateValidExchangeRate_When_GivenValidInputs() {
        // Given
        CurrencyPair currencyPair = CurrencyPair.of("USD", "TWD");
        Rate rate = Rate.of(new BigDecimal("32.5"));
        String source = "Bank API";
        
        // When
        ExchangeRate exchangeRate = ExchangeRate.create(currencyPair, rate, source);
        
        // Then
        assertNotNull(exchangeRate.getId());
        assertEquals(currencyPair, exchangeRate.getCurrencyPair());
        assertEquals(rate, exchangeRate.getRate());
        assertEquals(source, exchangeRate.getSource());
        assertNotNull(exchangeRate.getTimestamp());
    }

    @Test
    void should_ThrowException_When_CurrencyPairIsNull() {
        // Given
        CurrencyPair nullPair = null;
        Rate rate = Rate.of(new BigDecimal("32.5"));
        String source = "Bank API";
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
                ExchangeRate.create(nullPair, rate, source));
    }

    @Test
    void should_ThrowException_When_RateIsNull() {
        // Given
        CurrencyPair currencyPair = CurrencyPair.of("USD", "TWD");
        Rate nullRate = null;
        String source = "Bank API";
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
                ExchangeRate.create(currencyPair, nullRate, source));
    }

    @Test
    void should_CreateWithDefaultSource_When_SourceIsNull() {
        // Given
        CurrencyPair currencyPair = CurrencyPair.of("USD", "TWD");
        Rate rate = Rate.of(new BigDecimal("32.5"));
        String nullSource = null;
        
        // When
        ExchangeRate exchangeRate = ExchangeRate.create(currencyPair, rate, nullSource);
        
        // Then
        assertEquals("Manual", exchangeRate.getSource());
    }

    @Test
    void should_UpdateRate_When_ValidNewRate() {
        // Given
        CurrencyPair currencyPair = CurrencyPair.of("USD", "TWD");
        Rate originalRate = Rate.of(new BigDecimal("32.5"));
        ExchangeRate exchangeRate = ExchangeRate.create(currencyPair, originalRate, "Bank API");
        
        Rate newRate = Rate.of(new BigDecimal("33.0"));
        LocalDateTime beforeUpdate = exchangeRate.getTimestamp();
        
        // When
        try {
            Thread.sleep(1); // 確保時間戳不同
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        exchangeRate.updateRate(newRate);
        
        // Then
        assertEquals(newRate, exchangeRate.getRate());
        assertTrue(exchangeRate.getTimestamp().isAfter(beforeUpdate));
    }

    @Test
    void should_ThrowException_When_UpdateRateWithNull() {
        // Given
        CurrencyPair currencyPair = CurrencyPair.of("USD", "TWD");
        Rate originalRate = Rate.of(new BigDecimal("32.5"));
        ExchangeRate exchangeRate = ExchangeRate.create(currencyPair, originalRate, "Bank API");
        
        Rate nullRate = null;
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
                exchangeRate.updateRate(nullRate));
    }

    @Test
    void should_UpdateSource_When_ValidNewSource() {
        // Given
        CurrencyPair currencyPair = CurrencyPair.of("USD", "TWD");
        Rate rate = Rate.of(new BigDecimal("32.5"));
        ExchangeRate exchangeRate = ExchangeRate.create(currencyPair, rate, "Bank API");
        
        String newSource = "External API";
        LocalDateTime beforeUpdate = exchangeRate.getTimestamp();
        
        // When
        try {
            Thread.sleep(1); // 確保時間戳不同
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        exchangeRate.updateSource(newSource);
        
        // Then
        assertEquals(newSource, exchangeRate.getSource());
        assertTrue(exchangeRate.getTimestamp().isAfter(beforeUpdate));
    }

    @Test
    void should_BeEqual_When_SameId() {
        // Given
        CurrencyPair currencyPair = CurrencyPair.of("USD", "TWD");
        Rate rate = Rate.of(new BigDecimal("32.5"));
        ExchangeRate rate1 = ExchangeRate.create(currencyPair, rate, "Bank API");
        ExchangeRate rate2 = ExchangeRate.create(currencyPair, rate, "Bank API");
        
        // 設置相同的ID來模擬從數據庫加載的實體
        rate2.setId(rate1.getId());
        
        // When & Then
        assertEquals(rate1, rate2);
        assertEquals(rate1.hashCode(), rate2.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentId() {
        // Given
        CurrencyPair currencyPair = CurrencyPair.of("USD", "TWD");
        Rate rate = Rate.of(new BigDecimal("32.5"));
        ExchangeRate rate1 = ExchangeRate.create(currencyPair, rate, "Bank API");
        ExchangeRate rate2 = ExchangeRate.create(currencyPair, rate, "Bank API");
        
        // When & Then
        assertNotEquals(rate1, rate2); // 不同的ID
    }

    @Test
    void should_CheckIfCurrent_When_WithinTimeWindow() {
        // Given
        CurrencyPair currencyPair = CurrencyPair.of("USD", "TWD");
        Rate rate = Rate.of(new BigDecimal("32.5"));
        ExchangeRate exchangeRate = ExchangeRate.create(currencyPair, rate, "Bank API");
        
        // When & Then
        assertTrue(exchangeRate.isCurrent(60)); // 60秒內是當前的
        assertFalse(exchangeRate.isCurrent(0)); // 0秒內不是當前的（因為創建需要時間）
    }

    @Test
    void should_ValidateBusinessRules_When_Created() {
        // Given & When & Then
        // CurrencyPair已經在其構造函數中驗證了相同貨幣的情況
        assertThrows(IllegalArgumentException.class, () -> 
                CurrencyPair.of("USD", "USD"));
    }
}