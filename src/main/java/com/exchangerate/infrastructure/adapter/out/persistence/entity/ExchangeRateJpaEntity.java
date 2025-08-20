package com.exchangerate.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_rates", 
       indexes = {
           @Index(name = "idx_from_to_timestamp", columnList = "from_currency,to_currency,timestamp"),
           @Index(name = "idx_from_currency", columnList = "from_currency"),
           @Index(name = "idx_to_currency", columnList = "to_currency")
       })
public class ExchangeRateJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "來源貨幣代碼不能為空")
    @Size(min = 3, max = 3, message = "貨幣代碼必須為3個字元")
    @Column(name = "from_currency", nullable = false, length = 3)
    private String fromCurrency;
    
    @NotBlank(message = "目標貨幣代碼不能為空") 
    @Size(min = 3, max = 3, message = "貨幣代碼必須為3個字元")
    @Column(name = "to_currency", nullable = false, length = 3)
    private String toCurrency;
    
    @NotNull(message = "匯率不能為空")
    @Positive(message = "匯率必須大於0")
    @Column(name = "rate", nullable = false, precision = 19, scale = 6)
    private BigDecimal rate;
    
    @NotNull(message = "時間戳不能為空")
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "source", length = 100)
    private String source;
    
    // Default constructor
    public ExchangeRateJpaEntity() {
        this.timestamp = LocalDateTime.now();
    }
    
    // Constructor
    public ExchangeRateJpaEntity(String fromCurrency, String toCurrency, 
                                BigDecimal rate, String source) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.rate = rate;
        this.source = source;
        this.timestamp = LocalDateTime.now();
    }
    
    // Full constructor
    public ExchangeRateJpaEntity(Long id, String fromCurrency, String toCurrency, 
                                BigDecimal rate, LocalDateTime timestamp, String source) {
        this.id = id;
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.rate = rate;
        this.timestamp = timestamp;
        this.source = source;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFromCurrency() { return fromCurrency; }
    public void setFromCurrency(String fromCurrency) { this.fromCurrency = fromCurrency; }
    
    public String getToCurrency() { return toCurrency; }
    public void setToCurrency(String toCurrency) { this.toCurrency = toCurrency; }
    
    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    @Override
    public String toString() {
        return String.format("ExchangeRate{id=%d, %s->%s, rate=%s, timestamp=%s, source='%s'}", 
                id, fromCurrency, toCurrency, rate, timestamp, source);
    }
}