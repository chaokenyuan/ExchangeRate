package com.exchangerate.application.mapper;

import com.exchangerate.application.dto.command.ConvertCurrencyCommand;
import com.exchangerate.application.dto.response.ConversionResponse;
import com.exchangerate.domain.model.valueobject.ConversionResult;
import com.exchangerate.domain.model.valueobject.CurrencyCode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ConversionMapper {
    
    public CurrencyCode mapFromCurrency(ConvertCurrencyCommand command) {
        return CurrencyCode.of(command.getFromCurrency());
    }
    
    public CurrencyCode mapToCurrency(ConvertCurrencyCommand command) {
        return CurrencyCode.of(command.getToCurrency());
    }
    
    public ConversionResponse mapToResponse(ConversionResult result) {
        return new ConversionResponse(
                result.getCurrencyPair().getFromCurrency().getValue(),
                result.getCurrencyPair().getToCurrency().getValue(),
                result.getFromAmount(),
                result.getToAmount(),
                result.getRate().getValue(),
                result.getConversionTime(),
                result.getConversionPath()
        );
    }
}