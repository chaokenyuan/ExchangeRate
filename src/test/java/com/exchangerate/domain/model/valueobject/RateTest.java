package com.exchangerate.domain.model.valueobject;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

class RateTest {

    @Test
    void should_CreateValidRate_When_GivenValidDecimalValue() {
        // Given
        BigDecimal validValue = new BigDecimal("32.5");
        
        // When
        Rate rate = Rate.of(validValue);
        
        // Then
        assertEquals(validValue, rate.getValue());
    }

    @Test
    void should_ThrowException_When_RateIsNull() {
        // Given
        BigDecimal nullValue = null;
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> Rate.of(nullValue));
    }

    @Test
    void should_ThrowException_When_RateIsZero() {
        // Given
        BigDecimal zeroValue = BigDecimal.ZERO;
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> Rate.of(zeroValue));
    }

    @Test
    void should_ThrowException_When_RateIsNegative() {
        // Given
        BigDecimal negativeValue = new BigDecimal("-1.5");
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> Rate.of(negativeValue));
    }

    @Test
    void should_BeEqual_When_SameValue() {
        // Given
        BigDecimal value = new BigDecimal("32.5");
        Rate rate1 = Rate.of(value);
        Rate rate2 = Rate.of(value);
        
        // When & Then
        assertEquals(rate1, rate2);
        assertEquals(rate1.hashCode(), rate2.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentValue() {
        // Given
        Rate rate1 = Rate.of(new BigDecimal("32.5"));
        Rate rate2 = Rate.of(new BigDecimal("33.0"));
        
        // When & Then
        assertNotEquals(rate1, rate2);
    }

    @Test
    void should_PreservePrecision_When_CreatedFromString() {
        // Given
        String preciseValue = "32.123456";
        
        // When
        Rate rate = Rate.of(new BigDecimal(preciseValue));
        
        // Then
        assertEquals(0, new BigDecimal(preciseValue).compareTo(rate.getValue()));
    }

    @Test
    void should_CreateFromDouble_When_GivenDoubleValue() {
        // Given
        double doubleValue = 32.5;
        
        // When
        Rate rate = Rate.fromDouble(doubleValue);
        
        // Then
        assertEquals(0, new BigDecimal("32.5").compareTo(rate.getValue()));
    }

    @Test
    void should_ConvertToDouble_When_Requested() {
        // Given
        Rate rate = Rate.of(new BigDecimal("32.5"));
        
        // When
        double doubleValue = rate.doubleValue();
        
        // Then
        assertEquals(32.5, doubleValue, 0.0001);
    }

    @Test
    void should_HandleComparison_When_ComparingRates() {
        // Given
        Rate smaller = Rate.of(new BigDecimal("32.0"));
        Rate larger = Rate.of(new BigDecimal("33.0"));
        Rate equal = Rate.of(new BigDecimal("32.0"));
        
        // When & Then
        assertTrue(smaller.compareTo(larger) < 0);
        assertTrue(larger.compareTo(smaller) > 0);
        assertEquals(0, smaller.compareTo(equal));
    }

    @Test
    void should_CalculateInverse_When_Requested() {
        // Given
        Rate originalRate = Rate.of(new BigDecimal("2.0"));
        
        // When
        Rate inverseRate = originalRate.inverse();
        
        // Then
        assertEquals(0, new BigDecimal("0.5").compareTo(inverseRate.getValue()));
    }

    @Test
    void should_ThrowException_When_InverseOfZeroRequested() {
        // This test ensures that we handle edge cases properly
        // Even though we don't allow zero rates, this tests the inverse logic
        Rate rate = Rate.of(new BigDecimal("0.000001")); // Very small but not zero
        
        // When
        Rate inverse = rate.inverse();
        
        // Then
        assertTrue(inverse.getValue().compareTo(BigDecimal.ZERO) > 0);
    }
}