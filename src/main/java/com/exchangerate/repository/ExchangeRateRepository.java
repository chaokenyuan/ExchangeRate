package com.exchangerate.repository;

import com.exchangerate.model.ExchangeRate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    Optional<ExchangeRate> findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc(
            String fromCurrency, String toCurrency);

    List<ExchangeRate> findByFromCurrency(String fromCurrency);
    Page<ExchangeRate> findByFromCurrency(String fromCurrency, Pageable pageable);

    List<ExchangeRate> findByToCurrency(String toCurrency);
    Page<ExchangeRate> findByToCurrency(String toCurrency, Pageable pageable);

    List<ExchangeRate> findByFromCurrencyAndToCurrency(String fromCurrency, String toCurrency);
    Page<ExchangeRate> findByFromCurrencyAndToCurrency(String fromCurrency, String toCurrency, Pageable pageable);
}