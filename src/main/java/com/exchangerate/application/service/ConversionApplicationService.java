package com.exchangerate.application.service;

import com.exchangerate.domain.model.entity.ExchangeRate;
import com.exchangerate.domain.model.valueobject.*;
import com.exchangerate.domain.port.in.ConvertCurrencyUseCase;
import com.exchangerate.domain.port.out.ExchangeRateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class ConversionApplicationService implements ConvertCurrencyUseCase {
    
    private final ExchangeRateRepository exchangeRateRepository;
    
    public ConversionApplicationService(ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
    }
    
    @Override
    public ConversionResult convertCurrency(CurrencyCode fromCurrency, CurrencyCode toCurrency, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        
        CurrencyPair currencyPair = CurrencyPair.of(fromCurrency, toCurrency);
        
        // Try to find direct exchange rate
        ExchangeRate directRate = exchangeRateRepository
                .findLatestByCurrencyPair(currencyPair)
                .orElse(null);
                
        if (directRate != null) {
            return performDirectConversion(currencyPair, amount, directRate.getRate());
        }
        
        // Try reverse rate
        CurrencyPair reversePair = currencyPair.reverse();
        ExchangeRate reverseRate = exchangeRateRepository
                .findLatestByCurrencyPair(reversePair)
                .orElse(null);
                
        if (reverseRate != null) {
            Rate invertedRate = reverseRate.getRate().inverse();
            return performDirectConversion(currencyPair, amount, invertedRate);
        }
        
        // If no direct or reverse rate found, try chain conversion through USD
        if (!fromCurrency.getValue().equals("USD") && !toCurrency.getValue().equals("USD")) {
            ConversionResult chainResult = performChainConversion(fromCurrency, toCurrency, amount);
            if (chainResult != null) {
                return chainResult;
            }
        }
        
        throw new IllegalArgumentException("No exchange rate available for " + currencyPair);
    }
    
    private ConversionResult performDirectConversion(CurrencyPair currencyPair, BigDecimal amount, Rate rate) {
        BigDecimal convertedAmount = amount.multiply(rate.getValue());
        return ConversionResult.of(
                currencyPair,
                amount,
                convertedAmount,
                rate,
                LocalDateTime.now()
        );
    }
    
    private ConversionResult performChainConversion(CurrencyCode fromCurrency, CurrencyCode toCurrency, BigDecimal amount) {
        CurrencyCode usd = CurrencyCode.of("USD");
        
        // Convert from source to USD
        CurrencyPair fromToUsd = CurrencyPair.of(fromCurrency, usd);
        ExchangeRate fromToUsdRate = exchangeRateRepository
                .findLatestByCurrencyPair(fromToUsd)
                .orElse(null);
                
        if (fromToUsdRate == null) {
            // Try reverse: USD to source
            CurrencyPair usdToFrom = CurrencyPair.of(usd, fromCurrency);
            ExchangeRate usdToFromRate = exchangeRateRepository
                    .findLatestByCurrencyPair(usdToFrom)
                    .orElse(null);
            if (usdToFromRate != null) {
                fromToUsdRate = ExchangeRate.reconstruct(
                        usdToFromRate.getId(),
                        fromToUsd,
                        usdToFromRate.getRate().inverse(),
                        usdToFromRate.getSource(),
                        usdToFromRate.getTimestamp()
                );
            } else {
                return null;
            }
        }
        
        // Convert from USD to target
        CurrencyPair usdToTarget = CurrencyPair.of(usd, toCurrency);
        ExchangeRate usdToTargetRate = exchangeRateRepository
                .findLatestByCurrencyPair(usdToTarget)
                .orElse(null);
                
        if (usdToTargetRate == null) {
            // Try reverse: target to USD
            CurrencyPair targetToUsd = CurrencyPair.of(toCurrency, usd);
            ExchangeRate targetToUsdRate = exchangeRateRepository
                    .findLatestByCurrencyPair(targetToUsd)
                    .orElse(null);
            if (targetToUsdRate != null) {
                usdToTargetRate = ExchangeRate.reconstruct(
                        targetToUsdRate.getId(),
                        usdToTarget,
                        targetToUsdRate.getRate().inverse(),
                        targetToUsdRate.getSource(),
                        targetToUsdRate.getTimestamp()
                );
            } else {
                return null;
            }
        }
        
        // Calculate chain conversion
        BigDecimal usdAmount = amount.multiply(fromToUsdRate.getRate().getValue());
        BigDecimal finalAmount = usdAmount.multiply(usdToTargetRate.getRate().getValue());
        
        // Calculate effective rate
        Rate effectiveRate = Rate.fromDouble(finalAmount.divide(amount, 6, BigDecimal.ROUND_HALF_UP).doubleValue());
        
        String conversionPath = fromCurrency.getValue() + "→USD→" + toCurrency.getValue();
        
        return ConversionResult.withPath(
                CurrencyPair.of(fromCurrency, toCurrency),
                amount,
                finalAmount,
                effectiveRate,
                LocalDateTime.now(),
                conversionPath
        );
    }
}