package com.exchangerate.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ConversionRequest {
    
    @NotBlank(message = "來源貨幣為必填欄位")
    @Size(min = 3, max = 3, message = "貨幣代碼必須為3個字元")
    private String from_currency;
    
    @NotBlank(message = "目標貨幣為必填欄位")
    @Size(min = 3, max = 3, message = "貨幣代碼必須為3個字元")
    private String to_currency;
    
    @NotNull(message = "金額為必填欄位")
    @DecimalMin(value = "0.0", inclusive = false, message = "金額必須大於0")
    private BigDecimal amount;
}