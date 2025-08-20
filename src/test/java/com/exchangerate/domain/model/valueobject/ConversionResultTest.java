package com.exchangerate.domain.model.valueobject;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

class ConversionResultTest {

    @Test
    void should_CreateValidConversionResult_When_GivenValidInputs() {
        // Given
        CurrencyPair currencyPair = CurrencyPair.of("USD", "TWD");
        BigDecimal fromAmount = new BigDecimal("100");
        BigDecimal toAmount = new BigDecimal("3250");
        Rate rate = Rate.of(new BigDecimal("32.5"));
        LocalDateTime conversionTime = LocalDateTime.now();
        
        // When
        ConversionResult result = ConversionResult.of(currencyPair, fromAmount, toAmount, rate, conversionTime);
        
        // Then
        assertEquals(currencyPair, result.getCurrencyPair());
        assertEquals(fromAmount, result.getFromAmount());
        assertEquals(toAmount, result.getToAmount());
        assertEquals(rate, result.getRate());
        assertEquals(conversionTime, result.getConversionTime());
        assertNull(result.getConversionPath());
    }

    @Test
    void should_CreateConversionResultWithPath_When_ChainConversion() {
        // Given
        CurrencyPair currencyPair = CurrencyPair.of("EUR", "TWD");
        BigDecimal fromAmount = new BigDecimal("100");
        BigDecimal toAmount = new BigDecimal("3510");
        Rate rate = Rate.of(new BigDecimal("35.1"));
        LocalDateTime conversionTime = LocalDateTime.now();
        String conversionPath = "EUR→USD→TWD";
        
        // When
        ConversionResult result = ConversionResult.withPath(
                currencyPair, fromAmount, toAmount, rate, conversionTime, conversionPath
        );
        
        // Then
        assertEquals(currencyPair, result.getCurrencyPair());
        assertEquals(fromAmount, result.getFromAmount());
        assertEquals(toAmount, result.getToAmount());
        assertEquals(rate, result.getRate());
        assertEquals(conversionTime, result.getConversionTime());
        assertEquals(conversionPath, result.getConversionPath());
    }

    @Test
    void should_ThrowException_When_CurrencyPairIsNull() {
        // Given
        CurrencyPair nullPair = null;
        BigDecimal fromAmount = new BigDecimal("100");
        BigDecimal toAmount = new BigDecimal("3250");
        Rate rate = Rate.of(new BigDecimal("32.5"));
        LocalDateTime conversionTime = LocalDateTime.now();
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
                ConversionResult.of(nullPair, fromAmount, toAmount, rate, conversionTime));
    }

    @Test
    void should_ThrowException_When_FromAmountIsNull() {
        // Given
        CurrencyPair currencyPair = CurrencyPair.of("USD", "TWD");
        BigDecimal nullFromAmount = null;
        BigDecimal toAmount = new BigDecimal("3250");
        Rate rate = Rate.of(new BigDecimal("32.5"));
        LocalDateTime conversionTime = LocalDateTime.now();
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
                ConversionResult.of(currencyPair, nullFromAmount, toAmount, rate, conversionTime));
    }

    @Test
    void should_ThrowException_When_ToAmountIsNull() {
        // Given
        CurrencyPair currencyPair = CurrencyPair.of("USD", "TWD");
        BigDecimal fromAmount = new BigDecimal("100");
        BigDecimal nullToAmount = null;
        Rate rate = Rate.of(new BigDecimal("32.5"));
        LocalDateTime conversionTime = LocalDateTime.now();
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
                ConversionResult.of(currencyPair, fromAmount, nullToAmount, rate, conversionTime));
    }

    @Test
    void should_ThrowException_When_RateIsNull() {
        // Given
        CurrencyPair currencyPair = CurrencyPair.of("USD", "TWD");
        BigDecimal fromAmount = new BigDecimal("100");
        BigDecimal toAmount = new BigDecimal("3250");
        Rate nullRate = null;
        LocalDateTime conversionTime = LocalDateTime.now();
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
                ConversionResult.of(currencyPair, fromAmount, toAmount, nullRate, conversionTime));
    }

    @Test
    void should_ThrowException_When_ConversionTimeIsNull() {
        // Given
        CurrencyPair currencyPair = CurrencyPair.of("USD", "TWD");
        BigDecimal fromAmount = new BigDecimal("100");
        BigDecimal toAmount = new BigDecimal("3250");
        Rate rate = Rate.of(new BigDecimal("32.5"));
        LocalDateTime nullTime = null;
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
                ConversionResult.of(currencyPair, fromAmount, toAmount, rate, nullTime));
    }

    @Test
    void should_BeEqual_When_SameValues() {
        // Given
        CurrencyPair currencyPair = CurrencyPair.of("USD", "TWD");
        BigDecimal fromAmount = new BigDecimal("100");
        BigDecimal toAmount = new BigDecimal("3250");
        Rate rate = Rate.of(new BigDecimal("32.5"));
        LocalDateTime conversionTime = LocalDateTime.now();
        
        ConversionResult result1 = ConversionResult.of(currencyPair, fromAmount, toAmount, rate, conversionTime);
        ConversionResult result2 = ConversionResult.of(currencyPair, fromAmount, toAmount, rate, conversionTime);
        
        // When & Then
        assertEquals(result1, result2);
        assertEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    void should_BeImmutable() {
        // Given
        CurrencyPair currencyPair = CurrencyPair.of("USD", "TWD");
        BigDecimal fromAmount = new BigDecimal("100");
        BigDecimal toAmount = new BigDecimal("3250");
        Rate rate = Rate.of(new BigDecimal("32.5"));
        LocalDateTime conversionTime = LocalDateTime.now();
        
        // When
        ConversionResult result = ConversionResult.of(currencyPair, fromAmount, toAmount, rate, conversionTime);
        
        // Then
        // 所有getter返回的都應該是不可變的值或值物件
        assertEquals(currencyPair, result.getCurrencyPair());
        assertEquals(fromAmount, result.getFromAmount());
        assertEquals(toAmount, result.getToAmount());
        assertEquals(rate, result.getRate());
        assertEquals(conversionTime, result.getConversionTime());
    }

    @Test
    void should_IndicateDirectConversion_When_NoPath() {
        // Given
        CurrencyPair currencyPair = CurrencyPair.of("USD", "TWD");
        BigDecimal fromAmount = new BigDecimal("100");
        BigDecimal toAmount = new BigDecimal("3250");
        Rate rate = Rate.of(new BigDecimal("32.5"));
        LocalDateTime conversionTime = LocalDateTime.now();
        
        // When
        ConversionResult result = ConversionResult.of(currencyPair, fromAmount, toAmount, rate, conversionTime);
        
        // Then
        assertTrue(result.isDirectConversion());
        assertFalse(result.isChainConversion());
    }

    @Test
    void should_IndicateChainConversion_When_HasPath() {
        // Given
        CurrencyPair currencyPair = CurrencyPair.of("EUR", "TWD");
        BigDecimal fromAmount = new BigDecimal("100");
        BigDecimal toAmount = new BigDecimal("3510");
        Rate rate = Rate.of(new BigDecimal("35.1"));
        LocalDateTime conversionTime = LocalDateTime.now();
        String conversionPath = "EUR→USD→TWD";
        
        // When
        ConversionResult result = ConversionResult.withPath(
                currencyPair, fromAmount, toAmount, rate, conversionTime, conversionPath
        );
        
        // Then
        assertFalse(result.isDirectConversion());
        assertTrue(result.isChainConversion());
    }
}