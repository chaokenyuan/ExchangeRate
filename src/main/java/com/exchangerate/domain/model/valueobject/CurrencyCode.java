package com.exchangerate.domain.model.valueobject;

import java.util.Objects;

public final class CurrencyCode {
    private final String value;
    
    private CurrencyCode(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency code cannot be null or empty");
        }
        
        String normalizedValue = value.toUpperCase().trim();
        if (normalizedValue.length() != 3) {
            throw new IllegalArgumentException("Currency code must be exactly 3 characters");
        }
        
        this.value = normalizedValue;
    }
    
    public static CurrencyCode of(String value) {
        return new CurrencyCode(value);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CurrencyCode that = (CurrencyCode) obj;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}