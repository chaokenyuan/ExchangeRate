package com.exchangerate.application.mapper;

import com.exchangerate.application.dto.response.ExchangeRateResponse;
import com.exchangerate.domain.model.entity.ExchangeRate;
import org.springframework.stereotype.Component;

/**
 * Exchange rate entity mapper
 * Responsible for object conversion between Domain and Application layers
 */
@Component
public class ExchangeRateMapper {
    
    /**
     * Convert Domain entity to Response DTO
     */
    public ExchangeRateResponse toResponse(ExchangeRate exchangeRate) {
        if (exchangeRate == null) {
            return null;
        }
        
        Long id = null;
        try {
            id = Long.parseLong(exchangeRate.getId());
        } catch (NumberFormatException e) {
            // ID might be UUID format, keep as null
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