package com.exchangerate.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 匯率查詢響應DTO
 */
@Schema(
    name = "ExchangeRateResponse",
    description = "匯率資料響應模型"
)
public class ExchangeRateResponse {
    
    @Schema(description = "匯率ID", example = "1")
    private Long id;
    
    @Schema(description = "來源貨幣代碼", example = "USD")
    @JsonProperty("from_currency")
    private String fromCurrency;
    
    @Schema(description = "目標貨幣代碼", example = "EUR")
    @JsonProperty("to_currency")
    private String toCurrency;
    
    @Schema(description = "匯率值", example = "0.85")
    private BigDecimal rate;
    
    @Schema(description = "時間戳", example = "2024-01-15T10:30:00")
    private LocalDateTime timestamp;
    
    @Schema(description = "資料來源", example = "Central Bank")
    private String source;
    
    public ExchangeRateResponse() {}
    
    public ExchangeRateResponse(Long id, String fromCurrency, String toCurrency, 
                               BigDecimal rate, LocalDateTime timestamp, String source) {
        this.id = id;
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.rate = rate;
        this.timestamp = timestamp;
        this.source = source;
    }
    
    // Builder pattern
    public static class Builder {
        private Long id;
        private String fromCurrency;
        private String toCurrency;
        private BigDecimal rate;
        private LocalDateTime timestamp;
        private String source;
        
        public Builder id(Long id) {
            this.id = id;
            return this;
        }
        
        public Builder fromCurrency(String fromCurrency) {
            this.fromCurrency = fromCurrency;
            return this;
        }
        
        public Builder toCurrency(String toCurrency) {
            this.toCurrency = toCurrency;
            return this;
        }
        
        public Builder rate(BigDecimal rate) {
            this.rate = rate;
            return this;
        }
        
        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder source(String source) {
            this.source = source;
            return this;
        }
        
        public ExchangeRateResponse build() {
            return new ExchangeRateResponse(id, fromCurrency, toCurrency, rate, timestamp, source);
        }
    }
    
    public static Builder builder() {
        return new Builder();
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
}