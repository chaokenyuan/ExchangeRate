package com.exchangerate.application.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@Schema(
    name = "ConvertCurrencyCommand",
    description = "貨幣轉換命令",
    example = """
        {
            "from_currency": "USD",
            "to_currency": "EUR",
            "amount": 100.00
        }
        """
)
public class ConvertCurrencyCommand {
    
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

    public ConvertCurrencyCommand() {}

    public ConvertCurrencyCommand(String fromCurrency, String toCurrency, BigDecimal amount) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.amount = amount;
    }

    public String getFromCurrency() {
        return fromCurrency;
    }

    public void setFromCurrency(String fromCurrency) {
        this.fromCurrency = fromCurrency;
    }

    public String getToCurrency() {
        return toCurrency;
    }

    public void setToCurrency(String toCurrency) {
        this.toCurrency = toCurrency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}