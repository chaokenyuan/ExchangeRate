package com.exchangerate.service;

import com.exchangerate.model.ExchangeRate;
import com.exchangerate.repository.ExchangeRateRepository;
import com.exchangerate.dto.ConversionRequest;
import com.exchangerate.dto.ConversionResponse;
import com.exchangerate.exception.ResourceNotFoundException;
import com.exchangerate.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Transactional
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;

    public List<ExchangeRate> getAllExchangeRates() {
        return exchangeRateRepository.findAll();
    }

    public List<ExchangeRate> getAllExchangeRates(String from, String to) {
        if (from != null && to != null) {
            return exchangeRateRepository.findByFromCurrencyAndToCurrency(
                from.toUpperCase(), to.toUpperCase());
        } else if (from != null) {
            return exchangeRateRepository.findByFromCurrency(from.toUpperCase());
        } else if (to != null) {
            return exchangeRateRepository.findByToCurrency(to.toUpperCase());
        }
        return exchangeRateRepository.findAll();
    }

    public Page<ExchangeRate> getAllExchangeRates(String from, String to, Pageable pageable) {
        if (from != null && to != null) {
            return exchangeRateRepository.findByFromCurrencyAndToCurrency(
                from.toUpperCase(), to.toUpperCase(), pageable);
        } else if (from != null) {
            return exchangeRateRepository.findByFromCurrency(from.toUpperCase(), pageable);
        } else if (to != null) {
            return exchangeRateRepository.findByToCurrency(to.toUpperCase(), pageable);
        }
        return exchangeRateRepository.findAll(pageable);
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
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("找不到匯率資料進行換算")
                ));
        
        return amount.multiply(exchangeRate.getRate()).setScale(2, RoundingMode.HALF_UP);
    }

    public ConversionResponse convertCurrencyDetailed(ConversionRequest request) {
        String from = request.getFrom_currency().toUpperCase();
        String to = request.getTo_currency().toUpperCase();
        
        // Validation
        if (from.equals(to)) {
            throw new IllegalArgumentException("來源與目標貨幣不可相同");
        }
        
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("金額必須大於0");
        }
        
        // Check supported currencies  
        List<String> supportedCurrencies = Arrays.asList("USD", "EUR", "JPY", "CNY", "CHF", "TWD");
        if (!supportedCurrencies.contains(from)) {
            throw new IllegalArgumentException("不支援的貨幣代碼: " + from);
        }
        if (!supportedCurrencies.contains(to)) {
            throw new IllegalArgumentException("不支援的貨幣代碼: " + to);
        }
        
        // Try direct conversion
        Optional<ExchangeRate> directRate = getLatestRate(from, to);
        if (directRate.isPresent()) {
            BigDecimal result = request.getAmount().multiply(directRate.get().getRate())
                    .setScale(6, RoundingMode.HALF_UP);
            
            return ConversionResponse.builder()
                    .from_currency(from)
                    .to_currency(to)
                    .from_amount(request.getAmount())
                    .to_amount(result)
                    .rate(directRate.get().getRate())
                    .conversion_date(LocalDateTime.now())
                    .build();
        }
        
        // Try reverse conversion
        Optional<ExchangeRate> reverseRate = getLatestRate(to, from);
        if (reverseRate.isPresent()) {
            BigDecimal rate = BigDecimal.ONE.divide(reverseRate.get().getRate(), 6, RoundingMode.HALF_UP);
            BigDecimal result = request.getAmount().multiply(rate).setScale(6, RoundingMode.HALF_UP);
            
            return ConversionResponse.builder()
                    .from_currency(from)
                    .to_currency(to)
                    .from_amount(request.getAmount())
                    .to_amount(result)
                    .rate(rate)
                    .conversion_date(LocalDateTime.now())
                    .build();
        }
        
        // Try chain conversion through USD
        if (!"USD".equals(from) && !"USD".equals(to)) {
            Optional<ExchangeRate> fromToUsd = getLatestRate(from, "USD");
            Optional<ExchangeRate> usdToTarget = getLatestRate("USD", to);
            
            if (fromToUsd.isPresent() && usdToTarget.isPresent()) {
                BigDecimal rate = fromToUsd.get().getRate().multiply(usdToTarget.get().getRate());
                BigDecimal result = request.getAmount().multiply(rate).setScale(6, RoundingMode.HALF_UP);
                
                return ConversionResponse.builder()
                        .from_currency(from)
                        .to_currency(to)
                        .from_amount(request.getAmount())
                        .to_amount(result)
                        .rate(rate)
                        .conversion_date(LocalDateTime.now())
                        .conversion_path(from + "→USD→" + to)
                        .build();
            }
        }
        
        throw new ResourceNotFoundException("找不到匯率資料進行換算");
    }

    public ExchangeRate saveExchangeRate(ExchangeRate exchangeRate) {
        String from = exchangeRate.getFromCurrency().toUpperCase();
        String to = exchangeRate.getToCurrency().toUpperCase();
        
        // Validation
        if (from.equals(to)) {
            throw new IllegalArgumentException("來源與目標貨幣不可相同");
        }
        
        if (exchangeRate.getRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("匯率必須大於0");
        }
        
        // Check supported currencies  
        List<String> supportedCurrencies = Arrays.asList("USD", "EUR", "JPY", "CNY", "CHF", "TWD");
        if (!supportedCurrencies.contains(from)) {
            throw new IllegalArgumentException("不支援的貨幣代碼: " + from);
        }
        if (!supportedCurrencies.contains(to)) {
            throw new IllegalArgumentException("不支援的貨幣代碼: " + to);
        }
        
        // Check for duplicates
        Optional<ExchangeRate> existing = getLatestRate(from, to);
        if (existing.isPresent()) {
            throw new DuplicateResourceException("匯率組合已存在");
        }
        
        exchangeRate.setFromCurrency(from);
        exchangeRate.setToCurrency(to);
        if (exchangeRate.getTimestamp() == null) {
            exchangeRate.setTimestamp(LocalDateTime.now());
        }
        return exchangeRateRepository.save(exchangeRate);
    }

    public ExchangeRate updateExchangeRate(Long id, ExchangeRate exchangeRateDetails) {
        ExchangeRate exchangeRate = exchangeRateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到指定的匯率資料"));
        
        if (exchangeRateDetails.getRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("匯率必須大於0");
        }
        
        exchangeRate.setFromCurrency(exchangeRateDetails.getFromCurrency().toUpperCase());
        exchangeRate.setToCurrency(exchangeRateDetails.getToCurrency().toUpperCase());
        exchangeRate.setRate(exchangeRateDetails.getRate());
        exchangeRate.setSource(exchangeRateDetails.getSource());
        exchangeRate.setTimestamp(LocalDateTime.now());
        
        return exchangeRateRepository.save(exchangeRate);
    }

    public ExchangeRate updateExchangeRateByPair(String from, String to, Map<String, Object> updates) {
        ExchangeRate exchangeRate = getLatestRate(from, to)
                .orElseThrow(() -> new ResourceNotFoundException("找不到指定的匯率資料"));
        
        if (updates.containsKey("rate")) {
            BigDecimal newRate = new BigDecimal(updates.get("rate").toString());
            if (newRate.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("匯率必須大於0");
            }
            exchangeRate.setRate(newRate);
        }
        
        exchangeRate.setTimestamp(LocalDateTime.now());
        return exchangeRateRepository.save(exchangeRate);
    }

    public void deleteExchangeRate(Long id) {
        exchangeRateRepository.deleteById(id);
    }

    public void deleteExchangeRateByPair(String from, String to) {
        List<ExchangeRate> rates = exchangeRateRepository.findByFromCurrencyAndToCurrency(
            from.toUpperCase(), to.toUpperCase());
        if (rates.isEmpty()) {
            throw new ResourceNotFoundException("找不到指定的匯率資料");
        }
        exchangeRateRepository.deleteAll(rates);
    }

    public List<ExchangeRate> getExchangeRatesByFromCurrency(String fromCurrency) {
        return exchangeRateRepository.findByFromCurrency(fromCurrency.toUpperCase());
    }

    public List<ExchangeRate> getExchangeRatesByToCurrency(String toCurrency) {
        return exchangeRateRepository.findByToCurrency(toCurrency.toUpperCase());
    }
}