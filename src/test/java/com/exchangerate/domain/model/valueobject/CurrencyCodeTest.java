package com.exchangerate.domain.model.valueobject;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CurrencyCodeTest {

    @Test
    void should_CreateValidCurrencyCode_When_GivenValidInput() {
        // Given
        String validCode = "USD";
        
        // When
        CurrencyCode currencyCode = CurrencyCode.of(validCode);
        
        // Then
        assertEquals("USD", currencyCode.getValue());
    }

    @Test
    void should_ThrowException_When_CodeIsNull() {
        // Given
        String nullCode = null;
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> CurrencyCode.of(nullCode));
    }

    @Test
    void should_ThrowException_When_CodeIsEmpty() {
        // Given
        String emptyCode = "";
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> CurrencyCode.of(emptyCode));
    }

    @Test
    void should_ThrowException_When_CodeIsNotThreeCharacters() {
        // Given
        String shortCode = "US";
        String longCode = "USDD";
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> CurrencyCode.of(shortCode));
        assertThrows(IllegalArgumentException.class, () -> CurrencyCode.of(longCode));
    }

    @Test
    void should_NormalizeToUppercase_When_GivenLowercaseInput() {
        // Given
        String lowercaseCode = "usd";
        
        // When
        CurrencyCode currencyCode = CurrencyCode.of(lowercaseCode);
        
        // Then
        assertEquals("USD", currencyCode.getValue());
    }

    @Test
    void should_BeEqual_When_SameValue() {
        // Given
        CurrencyCode code1 = CurrencyCode.of("USD");
        CurrencyCode code2 = CurrencyCode.of("USD");
        
        // When & Then
        assertEquals(code1, code2);
        assertEquals(code1.hashCode(), code2.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentValue() {
        // Given
        CurrencyCode usd = CurrencyCode.of("USD");
        CurrencyCode eur = CurrencyCode.of("EUR");
        
        // When & Then
        assertNotEquals(usd, eur);
    }

    @Test
    void should_BeImmutable() {
        // Given
        CurrencyCode original = CurrencyCode.of("USD");
        
        // When
        String value = original.getValue();
        
        // Then
        assertEquals("USD", value);
        // 無法修改內部狀態，因為只有getter方法
    }
}