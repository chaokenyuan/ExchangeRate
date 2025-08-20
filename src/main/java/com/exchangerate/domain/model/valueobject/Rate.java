package com.exchangerate.domain.model.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Rate implements Comparable<Rate> {
    private static final int DEFAULT_SCALE = 6;
    private final BigDecimal value;
    
    private Rate(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("Rate cannot be null");
        }
        
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Rate must be greater than zero");
        }
        
        this.value = value;
    }
    
    public static Rate of(BigDecimal value) {
        return new Rate(value);
    }
    
    public static Rate fromDouble(double value) {
        return new Rate(BigDecimal.valueOf(value));
    }
    
    public BigDecimal getValue() {
        return value;
    }
    
    public double doubleValue() {
        return value.doubleValue();
    }
    
    public Rate inverse() {
        return new Rate(BigDecimal.ONE.divide(value, DEFAULT_SCALE, RoundingMode.HALF_UP));
    }
    
    @Override
    public int compareTo(Rate other) {
        return this.value.compareTo(other.value);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Rate rate = (Rate) obj;
        return Objects.equals(value, rate.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value.toString();
    }
}