package com.exchangerate.domain.model.entity;

import com.exchangerate.domain.model.valueobject.CurrencyPair;
import com.exchangerate.domain.model.valueobject.Rate;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class ExchangeRate {
    private String id;
    private CurrencyPair currencyPair;
    private Rate rate;
    private LocalDateTime timestamp;
    private String source;
    
    // Private constructor to enforce creation through factory methods
    private ExchangeRate(String id, CurrencyPair currencyPair, Rate rate, String source, LocalDateTime timestamp) {
        this.id = id;
        this.currencyPair = currencyPair;
        this.rate = rate;
        this.source = source;
        this.timestamp = timestamp;
    }
    
    public static ExchangeRate create(CurrencyPair currencyPair, Rate rate, String source) {
        if (currencyPair == null) {
            throw new IllegalArgumentException("Currency pair cannot be null");
        }
        if (rate == null) {
            throw new IllegalArgumentException("Rate cannot be null");
        }
        
        String finalSource = source != null ? source : "Manual";
        return new ExchangeRate(
                UUID.randomUUID().toString(), 
                currencyPair, 
                rate, 
                finalSource, 
                LocalDateTime.now()
        );
    }
    
    // For reconstruction from persistence layer
    public static ExchangeRate reconstruct(String id, CurrencyPair currencyPair, Rate rate, 
                                         String source, LocalDateTime timestamp) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        if (currencyPair == null) {
            throw new IllegalArgumentException("Currency pair cannot be null");
        }
        if (rate == null) {
            throw new IllegalArgumentException("Rate cannot be null");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }
        
        return new ExchangeRate(id, currencyPair, rate, source, timestamp);
    }
    
    public void updateRate(Rate newRate) {
        if (newRate == null) {
            throw new IllegalArgumentException("New rate cannot be null");
        }
        
        this.rate = newRate;
        this.timestamp = LocalDateTime.now();
    }
    
    public void updateSource(String newSource) {
        if (newSource != null) {
            this.source = newSource;
            this.timestamp = LocalDateTime.now();
        }
    }
    
    public boolean isCurrent(long withinSeconds) {
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(withinSeconds);
        return timestamp.isAfter(cutoff);
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public CurrencyPair getCurrencyPair() {
        return currencyPair;
    }
    
    public Rate getRate() {
        return rate;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public String getSource() {
        return source;
    }
    
    // For testing purposes only
    void setId(String id) {
        this.id = id;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ExchangeRate that = (ExchangeRate) obj;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("ExchangeRate{id='%s', pair=%s, rate=%s, source='%s', timestamp=%s}", 
                id, currencyPair, rate, source, timestamp);
    }
}