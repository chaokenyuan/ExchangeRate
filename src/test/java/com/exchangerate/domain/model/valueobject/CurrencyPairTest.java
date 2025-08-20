package com.exchangerate.domain.model.valueobject;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CurrencyPairTest {

    @Test
    void should_CreateValidCurrencyPair_When_GivenValidCurrencies() {
        // Given
        CurrencyCode from = CurrencyCode.of("USD");
        CurrencyCode to = CurrencyCode.of("TWD");
        
        // When
        CurrencyPair pair = CurrencyPair.of(from, to);
        
        // Then
        assertEquals(from, pair.getFromCurrency());
        assertEquals(to, pair.getToCurrency());
    }

    @Test
    void should_ThrowException_When_FromCurrencyIsNull() {
        // Given
        CurrencyCode nullFrom = null;
        CurrencyCode validTo = CurrencyCode.of("TWD");
        
        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> CurrencyPair.of(nullFrom, validTo));
    }

    @Test
    void should_ThrowException_When_ToCurrencyIsNull() {
        // Given
        CurrencyCode validFrom = CurrencyCode.of("USD");
        CurrencyCode nullTo = null;
        
        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> CurrencyPair.of(validFrom, nullTo));
    }

    @Test
    void should_ThrowException_When_SameCurrency() {
        // Given
        CurrencyCode sameCurrency = CurrencyCode.of("USD");
        
        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> CurrencyPair.of(sameCurrency, sameCurrency));
    }

    @Test
    void should_CreateFromStrings_When_GivenValidStrings() {
        // Given
        String fromStr = "USD";
        String toStr = "TWD";
        
        // When
        CurrencyPair pair = CurrencyPair.of(fromStr, toStr);
        
        // Then
        assertEquals("USD", pair.getFromCurrency().getValue());
        assertEquals("TWD", pair.getToCurrency().getValue());
    }

    @Test
    void should_BeEqual_When_SameCurrencies() {
        // Given
        CurrencyPair pair1 = CurrencyPair.of("USD", "TWD");
        CurrencyPair pair2 = CurrencyPair.of("USD", "TWD");
        
        // When & Then
        assertEquals(pair1, pair2);
        assertEquals(pair1.hashCode(), pair2.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentCurrencies() {
        // Given
        CurrencyPair pair1 = CurrencyPair.of("USD", "TWD");
        CurrencyPair pair2 = CurrencyPair.of("EUR", "TWD");
        
        // When & Then
        assertNotEquals(pair1, pair2);
    }

    @Test
    void should_CreateReverse_When_Requested() {
        // Given
        CurrencyPair original = CurrencyPair.of("USD", "TWD");
        
        // When
        CurrencyPair reversed = original.reverse();
        
        // Then
        assertEquals("TWD", reversed.getFromCurrency().getValue());
        assertEquals("USD", reversed.getToCurrency().getValue());
    }

    @Test
    void should_FormatProperly_When_ToString() {
        // Given
        CurrencyPair pair = CurrencyPair.of("USD", "TWD");
        
        // When
        String formatted = pair.toString();
        
        // Then
        assertEquals("USD/TWD", formatted);
    }

    @Test
    void should_BeImmutable() {
        // Given
        CurrencyPair original = CurrencyPair.of("USD", "TWD");
        
        // When
        CurrencyCode fromCurrency = original.getFromCurrency();
        CurrencyCode toCurrency = original.getToCurrency();
        
        // Then
        assertEquals("USD", fromCurrency.getValue());
        assertEquals("TWD", toCurrency.getValue());
        // 無法修改原始對象，因為CurrencyCode也是不可變的
    }

    @Test
    void should_HandleCaseNormalization_When_CreatedFromStrings() {
        // Given
        String lowerFrom = "usd";
        String lowerTo = "twd";
        
        // When
        CurrencyPair pair = CurrencyPair.of(lowerFrom, lowerTo);
        
        // Then
        assertEquals("USD", pair.getFromCurrency().getValue());
        assertEquals("TWD", pair.getToCurrency().getValue());
    }
}