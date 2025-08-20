package com.exchangerate.domain.port.out;

import com.exchangerate.domain.model.entity.ExchangeRate;
import com.exchangerate.domain.model.valueobject.CurrencyPair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ExchangeRateRepository {
    List<ExchangeRate> findAll();
    Page<ExchangeRate> findAll(Pageable pageable);
    Optional<ExchangeRate> findById(Long id);
    Optional<ExchangeRate> findLatestByCurrencyPair(CurrencyPair currencyPair);
    List<ExchangeRate> findByFromCurrency(String fromCurrency);
    Page<ExchangeRate> findByFromCurrency(String fromCurrency, Pageable pageable);
    List<ExchangeRate> findByToCurrency(String toCurrency);
    Page<ExchangeRate> findByToCurrency(String toCurrency, Pageable pageable);
    List<ExchangeRate> findByFromCurrencyAndToCurrency(String fromCurrency, String toCurrency);
    Page<ExchangeRate> findByFromCurrencyAndToCurrency(String fromCurrency, String toCurrency, Pageable pageable);
    ExchangeRate save(ExchangeRate exchangeRate);
    void delete(ExchangeRate exchangeRate);
    void deleteById(Long id);
    boolean existsById(Long id);
}