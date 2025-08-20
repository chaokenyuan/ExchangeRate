package com.exchangerate.application.dto.query;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 根據ID查詢匯率的Query對象
 */
public class GetExchangeRateByIdQuery {
    
    @NotNull(message = "ID不能為空")
    @Positive(message = "ID必須為正數")
    private Long id;
    
    public GetExchangeRateByIdQuery() {}
    
    public GetExchangeRateByIdQuery(Long id) {
        this.id = id;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
}