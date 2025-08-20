package com.exchangerate.application.service;

import com.exchangerate.domain.model.entity.ExchangeRate;
import com.exchangerate.domain.model.valueobject.*;
import com.exchangerate.domain.port.in.CreateExchangeRateUseCase;
import com.exchangerate.domain.port.in.QueryExchangeRateUseCase;
import com.exchangerate.domain.port.out.ExchangeRateRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ExchangeRateApplicationService implements CreateExchangeRateUseCase, QueryExchangeRateUseCase {
    
    private final ExchangeRateRepository exchangeRateRepository;
    
    public ExchangeRateApplicationService(ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
    }
    
    @Override
    public ExchangeRate createExchangeRate(CurrencyPair currencyPair, Rate rate, String source) {
        // Check if rate already exists
        Optional<ExchangeRate> existing = exchangeRateRepository.findLatestByCurrencyPair(currencyPair);
        if (existing.isPresent()) {
            // Update existing rate
            ExchangeRate existingRate = existing.get();
            existingRate.updateRate(rate);
            existingRate.updateSource(source);
            return exchangeRateRepository.save(existingRate);
        }
        
        // Create new rate
        ExchangeRate newRate = ExchangeRate.create(currencyPair, rate, source);
        return exchangeRateRepository.save(newRate);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExchangeRate> getAllExchangeRates() {
        return exchangeRateRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ExchangeRate> getAllExchangeRates(Pageable pageable) {
        return exchangeRateRepository.findAll(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ExchangeRate> getExchangeRateById(Long id) {
        return exchangeRateRepository.findById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ExchangeRate> getLatestExchangeRate(CurrencyPair currencyPair) {
        return exchangeRateRepository.findLatestByCurrencyPair(currencyPair);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExchangeRate> getExchangeRatesByFromCurrency(String fromCurrency) {
        return exchangeRateRepository.findByFromCurrency(fromCurrency);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ExchangeRate> getExchangeRatesByToCurrency(String toCurrency) {
        return exchangeRateRepository.findByToCurrency(toCurrency);
    }
}