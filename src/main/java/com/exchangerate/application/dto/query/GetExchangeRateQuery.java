package com.exchangerate.application.dto.query;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 查詢特定匯率的Query對象
 * CQRS模式中的Query端
 */
public class GetExchangeRateQuery {
    
    @NotBlank(message = "來源貨幣代碼不能為空")
    @Size(min = 3, max = 3, message = "貨幣代碼必須為3個字元")
    private String fromCurrency;
    
    @NotBlank(message = "目標貨幣代碼不能為空")
    @Size(min = 3, max = 3, message = "貨幣代碼必須為3個字元")
    private String toCurrency;
    
    public GetExchangeRateQuery() {}
    
    public GetExchangeRateQuery(String fromCurrency, String toCurrency) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
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
}