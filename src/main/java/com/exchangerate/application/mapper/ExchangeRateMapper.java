package com.exchangerate.application.mapper;

import com.exchangerate.application.dto.response.ExchangeRateResponse;
import com.exchangerate.domain.model.entity.ExchangeRate;
import org.springframework.stereotype.Component;

/**
 * 匯率實體映射器
 * 負責Domain層和Application層之間的對象轉換
 */
@Component
public class ExchangeRateMapper {
    
    /**
     * 將Domain實體轉換為Response DTO
     */
    public ExchangeRateResponse toResponse(ExchangeRate exchangeRate) {
        if (exchangeRate == null) {
            return null;
        }
        
        Long id = null;
        try {
            id = Long.parseLong(exchangeRate.getId());
        } catch (NumberFormatException e) {
            // ID可能是UUID格式，保持為null
        }
        
        return ExchangeRateResponse.builder()
            .id(id)
            .fromCurrency(exchangeRate.getCurrencyPair().getFromCurrency().getValue())
            .toCurrency(exchangeRate.getCurrencyPair().getToCurrency().getValue())
            .rate(exchangeRate.getRate().getValue())
            .timestamp(exchangeRate.getTimestamp())
            .source(exchangeRate.getSource())
            .build();
    }
}