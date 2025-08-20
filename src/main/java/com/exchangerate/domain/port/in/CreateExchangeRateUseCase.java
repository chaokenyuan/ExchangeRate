package com.exchangerate.domain.port.in;

import com.exchangerate.domain.model.entity.ExchangeRate;
import com.exchangerate.domain.model.valueobject.CurrencyPair;
import com.exchangerate.domain.model.valueobject.Rate;

public interface CreateExchangeRateUseCase {
    ExchangeRate createExchangeRate(CurrencyPair currencyPair, Rate rate, String source);
}