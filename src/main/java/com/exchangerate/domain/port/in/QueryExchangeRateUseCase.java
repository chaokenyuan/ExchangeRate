package com.exchangerate.domain.port.in;

import com.exchangerate.domain.model.entity.ExchangeRate;
import com.exchangerate.domain.model.valueobject.CurrencyPair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface QueryExchangeRateUseCase {
    List<ExchangeRate> getAllExchangeRates();
    Page<ExchangeRate> getAllExchangeRates(Pageable pageable);
    Optional<ExchangeRate> getExchangeRateById(Long id);
    Optional<ExchangeRate> getLatestExchangeRate(CurrencyPair currencyPair);
    List<ExchangeRate> getExchangeRatesByFromCurrency(String fromCurrency);
    List<ExchangeRate> getExchangeRatesByToCurrency(String toCurrency);
}