package com.exchangerate.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(
    name = "ConversionResponse",
    description = "貨幣轉換響應資料模型",
    example = """
        {
            "from_currency": "USD",
            "to_currency": "EUR",
            "from_amount": 100.00,
            "to_amount": 85.000000,
            "rate": 0.85,
            "conversion_date": "2024-01-15T10:30:00",
            "conversion_path": "USD→EUR"
        }
        """
)
public class ConversionResponse {
    
    @Schema(
        description = "來源貨幣代碼",
        example = "USD"
    )
    @JsonProperty("from_currency")
    private String fromCurrency;
    
    @Schema(
        description = "目標貨幣代碼",
        example = "EUR"
    )
    @JsonProperty("to_currency")
    private String toCurrency;
    
    @Schema(
        description = "來源金額",
        example = "100.00"
    )
    @JsonProperty("from_amount")
    private BigDecimal fromAmount;
    
    @Schema(
        description = "轉換後金額",
        example = "85.000000"
    )
    @JsonProperty("to_amount")
    private BigDecimal toAmount;
    
    @Schema(
        description = "使用的匯率",
        example = "0.85"
    )
    private BigDecimal rate;
    
    @Schema(
        description = "轉換執行時間",
        example = "2024-01-15T10:30:00"
    )
    @JsonProperty("conversion_date")
    private LocalDateTime conversionDate;
    
    @Schema(
        description = "轉換路徑，顯示轉換經過的貨幣鏈",
        example = "USD→EUR",
        nullable = true
    )
    @JsonProperty("conversion_path")
    private String conversionPath;

    public ConversionResponse() {}

    public ConversionResponse(String fromCurrency, String toCurrency, BigDecimal fromAmount, 
                            BigDecimal toAmount, BigDecimal rate, LocalDateTime conversionDate, 
                            String conversionPath) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.fromAmount = fromAmount;
        this.toAmount = toAmount;
        this.rate = rate;
        this.conversionDate = conversionDate;
        this.conversionPath = conversionPath;
    }

    // Getters and Setters
    public String getFromCurrency() { return fromCurrency; }
    public void setFromCurrency(String fromCurrency) { this.fromCurrency = fromCurrency; }
    
    public String getToCurrency() { return toCurrency; }
    public void setToCurrency(String toCurrency) { this.toCurrency = toCurrency; }
    
    public BigDecimal getFromAmount() { return fromAmount; }
    public void setFromAmount(BigDecimal fromAmount) { this.fromAmount = fromAmount; }
    
    public BigDecimal getToAmount() { return toAmount; }
    public void setToAmount(BigDecimal toAmount) { this.toAmount = toAmount; }
    
    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
    
    public LocalDateTime getConversionDate() { return conversionDate; }
    public void setConversionDate(LocalDateTime conversionDate) { this.conversionDate = conversionDate; }
    
    public String getConversionPath() { return conversionPath; }
    public void setConversionPath(String conversionPath) { this.conversionPath = conversionPath; }
}