package com.exchangerate.domain.model.valueobject;

import java.util.Objects;

public final class CurrencyPair {
    private final CurrencyCode fromCurrency;
    private final CurrencyCode toCurrency;
    
    private CurrencyPair(CurrencyCode fromCurrency, CurrencyCode toCurrency) {
        if (fromCurrency == null) {
            throw new IllegalArgumentException("From currency cannot be null");
        }
        if (toCurrency == null) {
            throw new IllegalArgumentException("To currency cannot be null");
        }
        if (fromCurrency.equals(toCurrency)) {
            throw new IllegalArgumentException("Source and target currency cannot be the same");
        }
        
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
    }
    
    public static CurrencyPair of(CurrencyCode fromCurrency, CurrencyCode toCurrency) {
        return new CurrencyPair(fromCurrency, toCurrency);
    }
    
    public static CurrencyPair of(String fromCurrency, String toCurrency) {
        return new CurrencyPair(
                CurrencyCode.of(fromCurrency), 
                CurrencyCode.of(toCurrency)
        );
    }
    
    public CurrencyCode getFromCurrency() {
        return fromCurrency;
    }
    
    public CurrencyCode getToCurrency() {
        return toCurrency;
    }
    
    public CurrencyPair reverse() {
        return new CurrencyPair(toCurrency, fromCurrency);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CurrencyPair that = (CurrencyPair) obj;
        return Objects.equals(fromCurrency, that.fromCurrency) &&
               Objects.equals(toCurrency, that.toCurrency);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(fromCurrency, toCurrency);
    }
    
    @Override
    public String toString() {
        return fromCurrency.getValue() + "/" + toCurrency.getValue();
    }
}