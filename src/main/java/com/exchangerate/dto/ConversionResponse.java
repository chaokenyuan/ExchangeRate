package com.exchangerate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
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
}