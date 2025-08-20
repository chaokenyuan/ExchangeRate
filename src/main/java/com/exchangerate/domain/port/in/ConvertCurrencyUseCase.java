package com.exchangerate.domain.port.in;

import com.exchangerate.domain.model.valueobject.ConversionResult;
import com.exchangerate.domain.model.valueobject.CurrencyCode;
import java.math.BigDecimal;

public interface ConvertCurrencyUseCase {
    ConversionResult convertCurrency(CurrencyCode fromCurrency, CurrencyCode toCurrency, BigDecimal amount);
}