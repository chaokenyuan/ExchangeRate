package com.exchangerate.application.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@Schema(
    name = "ConvertCurrencyCommand",
    description = "Currency conversion command",
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
        description = "Source currency code (ISO 4217 format)",
        example = "USD",
        required = true,
        minLength = 3,
        maxLength = 3,
        pattern = "^[A-Z]{3}$"
    )
    @NotBlank(message = "Source currency is required")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    @JsonProperty("from_currency")
    private String fromCurrency;
    
    @Schema(
        description = "Target currency code (ISO 4217 format)",
        example = "EUR",
        required = true,
        minLength = 3,
        maxLength = 3,
        pattern = "^[A-Z]{3}$"
    )
    @NotBlank(message = "Target currency is required")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    @JsonProperty("to_currency")
    private String toCurrency;
    
    @Schema(
        description = "Amount to convert, must be greater than 0",
        example = "100.00",
        required = true,
        minimum = "0",
        exclusiveMinimum = true
    )
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
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