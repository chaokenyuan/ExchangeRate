package com.exchangerate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@Data
@Schema(
    name = "ConversionRequest",
    description = "貨幣轉換請求資料模型",
    example = """
        {
            "from_currency": "USD",
            "to_currency": "EUR",
            "amount": 100.00
        }
        """
)
public class ConversionRequest {
    
    @Schema(
        description = "來源貨幣代碼（ISO 4217格式）",
        example = "USD",
        required = true,
        minLength = 3,
        maxLength = 3,
        pattern = "^[A-Z]{3}$"
    )
    @NotBlank(message = "來源貨幣為必填欄位")
    @Size(min = 3, max = 3, message = "貨幣代碼必須為3個字元")
    @JsonProperty("from_currency")
    private String fromCurrency;
    
    @Schema(
        description = "目標貨幣代碼（ISO 4217格式）",
        example = "EUR",
        required = true,
        minLength = 3,
        maxLength = 3,
        pattern = "^[A-Z]{3}$"
    )
    @NotBlank(message = "目標貨幣為必填欄位")
    @Size(min = 3, max = 3, message = "貨幣代碼必須為3個字元")
    @JsonProperty("to_currency")
    private String toCurrency;
    
    @Schema(
        description = "要轉換的金額，必須大於0",
        example = "100.00",
        required = true,
        minimum = "0",
        exclusiveMinimum = true
    )
    @NotNull(message = "金額為必填欄位")
    @DecimalMin(value = "0.0", inclusive = false, message = "金額必須大於0")
    private BigDecimal amount;
}