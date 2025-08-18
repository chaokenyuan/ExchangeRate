package com.exchangerate.service;

import com.exchangerate.model.ExchangeRate;
import com.exchangerate.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;

    public List<ExchangeRate> getAllExchangeRates() {
        return exchangeRateRepository.findAll();
    }

    public Optional<ExchangeRate> getExchangeRateById(Long id) {
        return exchangeRateRepository.findById(id);
    }

    public Optional<ExchangeRate> getLatestRate(String fromCurrency, String toCurrency) {
        return exchangeRateRepository.findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc(
                fromCurrency.toUpperCase(), 
                toCurrency.toUpperCase()
        );
    }

    public BigDecimal convertCurrency(String fromCurrency, String toCurrency, BigDecimal amount) {
        ExchangeRate exchangeRate = getLatestRate(fromCurrency, toCurrency)
                .orElseThrow(() -> new RuntimeException(
                        String.format("Exchange rate not found for %s to %s", fromCurrency, toCurrency)
                ));
        
        return amount.multiply(exchangeRate.getRate()).setScale(2, RoundingMode.HALF_UP);
    }

    public ExchangeRate saveExchangeRate(ExchangeRate exchangeRate) {
        exchangeRate.setFromCurrency(exchangeRate.getFromCurrency().toUpperCase());
        exchangeRate.setToCurrency(exchangeRate.getToCurrency().toUpperCase());
        if (exchangeRate.getTimestamp() == null) {
            exchangeRate.setTimestamp(LocalDateTime.now());
        }
        return exchangeRateRepository.save(exchangeRate);
    }

    public ExchangeRate updateExchangeRate(Long id, ExchangeRate exchangeRateDetails) {
        ExchangeRate exchangeRate = exchangeRateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exchange rate not found with id: " + id));
        
        exchangeRate.setFromCurrency(exchangeRateDetails.getFromCurrency().toUpperCase());
        exchangeRate.setToCurrency(exchangeRateDetails.getToCurrency().toUpperCase());
        exchangeRate.setRate(exchangeRateDetails.getRate());
        exchangeRate.setSource(exchangeRateDetails.getSource());
        exchangeRate.setTimestamp(LocalDateTime.now());
        
        return exchangeRateRepository.save(exchangeRate);
    }

    public void deleteExchangeRate(Long id) {
        exchangeRateRepository.deleteById(id);
    }

    public List<ExchangeRate> getExchangeRatesByFromCurrency(String fromCurrency) {
        return exchangeRateRepository.findByFromCurrency(fromCurrency.toUpperCase());
    }

    public List<ExchangeRate> getExchangeRatesByToCurrency(String toCurrency) {
        return exchangeRateRepository.findByToCurrency(toCurrency.toUpperCase());
    }
}