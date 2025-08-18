package com.exchangerate.repository;

import com.exchangerate.model.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    Optional<ExchangeRate> findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc(
            String fromCurrency, String toCurrency);

    List<ExchangeRate> findByFromCurrency(String fromCurrency);

    List<ExchangeRate> findByToCurrency(String toCurrency);

    List<ExchangeRate> findByFromCurrencyAndToCurrency(String fromCurrency, String toCurrency);
}