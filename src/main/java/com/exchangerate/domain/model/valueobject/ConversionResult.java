package com.exchangerate.domain.model.valueobject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public final class ConversionResult {
    private final CurrencyPair currencyPair;
    private final BigDecimal fromAmount;
    private final BigDecimal toAmount;
    private final Rate rate;
    private final LocalDateTime conversionTime;
    private final String conversionPath;
    
    private ConversionResult(CurrencyPair currencyPair, BigDecimal fromAmount, BigDecimal toAmount, 
                           Rate rate, LocalDateTime conversionTime, String conversionPath) {
        if (currencyPair == null) {
            throw new IllegalArgumentException("Currency pair cannot be null");
        }
        if (fromAmount == null) {
            throw new IllegalArgumentException("From amount cannot be null");
        }
        if (toAmount == null) {
            throw new IllegalArgumentException("To amount cannot be null");
        }
        if (rate == null) {
            throw new IllegalArgumentException("Rate cannot be null");
        }
        if (conversionTime == null) {
            throw new IllegalArgumentException("Conversion time cannot be null");
        }
        
        this.currencyPair = currencyPair;
        this.fromAmount = fromAmount;
        this.toAmount = toAmount;
        this.rate = rate;
        this.conversionTime = conversionTime;
        this.conversionPath = conversionPath;
    }
    
    public static ConversionResult of(CurrencyPair currencyPair, BigDecimal fromAmount, 
                                    BigDecimal toAmount, Rate rate, LocalDateTime conversionTime) {
        return new ConversionResult(currencyPair, fromAmount, toAmount, rate, conversionTime, null);
    }
    
    public static ConversionResult withPath(CurrencyPair currencyPair, BigDecimal fromAmount, 
                                          BigDecimal toAmount, Rate rate, LocalDateTime conversionTime, 
                                          String conversionPath) {
        return new ConversionResult(currencyPair, fromAmount, toAmount, rate, conversionTime, conversionPath);
    }
    
    public CurrencyPair getCurrencyPair() {
        return currencyPair;
    }
    
    public BigDecimal getFromAmount() {
        return fromAmount;
    }
    
    public BigDecimal getToAmount() {
        return toAmount;
    }
    
    public Rate getRate() {
        return rate;
    }
    
    public LocalDateTime getConversionTime() {
        return conversionTime;
    }
    
    public String getConversionPath() {
        return conversionPath;
    }
    
    public boolean isDirectConversion() {
        return conversionPath == null;
    }
    
    public boolean isChainConversion() {
        return conversionPath != null;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ConversionResult that = (ConversionResult) obj;
        return Objects.equals(currencyPair, that.currencyPair) &&
               Objects.equals(fromAmount, that.fromAmount) &&
               Objects.equals(toAmount, that.toAmount) &&
               Objects.equals(rate, that.rate) &&
               Objects.equals(conversionTime, that.conversionTime) &&
               Objects.equals(conversionPath, that.conversionPath);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(currencyPair, fromAmount, toAmount, rate, conversionTime, conversionPath);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("ConversionResult{%s %s -> %s %s, rate=%s", 
                fromAmount, currencyPair.getFromCurrency().getValue(),
                toAmount, currencyPair.getToCurrency().getValue(),
                rate.getValue()));
        
        if (conversionPath != null) {
            sb.append(", path=").append(conversionPath);
        }
        
        sb.append(", time=").append(conversionTime).append("}");
        return sb.toString();
    }
}