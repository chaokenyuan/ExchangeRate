package com.exchangerate.mock;

import com.exchangerate.model.ExchangeRate;
import com.exchangerate.dto.ConversionRequest;
import com.exchangerate.dto.ConversionResponse;
import com.exchangerate.constants.CurrencyConstants;
import com.exchangerate.constants.ErrorMessages;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Mock Exchange Rate Service
 * 完全獨立的Mock實現，不依賴Spring或JPA
 */
public class MockExchangeRateService {
    
    private final Map<Long, ExchangeRate> storage = new ConcurrentHashMap<>();
    private final Map<String, ExchangeRate> currencyPairIndex = new ConcurrentHashMap<>();
    private Long nextId = 1L;
    
    public List<ExchangeRate> getAllExchangeRates() {
        return new ArrayList<>(storage.values());
    }
    
    public List<ExchangeRate> getAllExchangeRates(String from, String to) {
        List<ExchangeRate> allRates = getAllExchangeRates();
        
        if (from != null && to != null) {
            return allRates.stream()
                .filter(rate -> rate.getFromCurrency().equalsIgnoreCase(from) && 
                               rate.getToCurrency().equalsIgnoreCase(to))
                .collect(Collectors.toList());
        } else if (from != null) {
            return allRates.stream()
                .filter(rate -> rate.getFromCurrency().equalsIgnoreCase(from))
                .collect(Collectors.toList());
        } else if (to != null) {
            return allRates.stream()
                .filter(rate -> rate.getToCurrency().equalsIgnoreCase(to))
                .collect(Collectors.toList());
        }
        return allRates;
    }
    
    public Optional<ExchangeRate> getLatestRate(String fromCurrency, String toCurrency) {
        if (fromCurrency == null || toCurrency == null) {
            return Optional.empty();
        }
        String key = generateKey(fromCurrency.toUpperCase(), toCurrency.toUpperCase());
        return Optional.ofNullable(currencyPairIndex.get(key));
    }
    
    public ExchangeRate saveExchangeRate(ExchangeRate exchangeRate) {
        validateExchangeRate(exchangeRate);
        
        String from = exchangeRate.getFromCurrency().toUpperCase();
        String to = exchangeRate.getToCurrency().toUpperCase();
        
        // Validation
        if (from.equals(to)) {
            throw new IllegalArgumentException(ErrorMessages.SAME_CURRENCY_ERROR);
        }
        
        if (exchangeRate.getRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(ErrorMessages.INVALID_RATE_ERROR);
        }
        
        // Check supported currencies using constants
        if (!CurrencyConstants.isSupportedCurrency(from)) {
            throw new IllegalArgumentException(String.format(ErrorMessages.UNSUPPORTED_CURRENCY_ERROR, from));
        }
        if (!CurrencyConstants.isSupportedCurrency(to)) {
            throw new IllegalArgumentException(String.format(ErrorMessages.UNSUPPORTED_CURRENCY_ERROR, to));
        }
        
        // Check for duplicates - throw error for duplicates in test environment
        String key = generateKey(from, to);
        ExchangeRate existing = currencyPairIndex.get(key);
        if (existing != null && !"ALLOW_DUPLICATE".equals(exchangeRate.getSource())) {
            throw new IllegalArgumentException("匯率組合已存在");
        }
        
        // Create new rate
        if (exchangeRate.getId() == null) {
            exchangeRate.setId(nextId++);
        }
        exchangeRate.setFromCurrency(from);
        exchangeRate.setToCurrency(to);
        if (exchangeRate.getTimestamp() == null) {
            exchangeRate.setTimestamp(LocalDateTime.now());
        }
        
        storage.put(exchangeRate.getId(), exchangeRate);
        currencyPairIndex.put(key, exchangeRate);
        
        return exchangeRate;
    }
    
    public ConversionResponse convertCurrencyDetailed(ConversionRequest request) {
        validateConversionRequest(request);
        
        String from = request.getFromCurrency().toUpperCase();
        String to = request.getToCurrency().toUpperCase();
        
        // Validation
        if (from.equals(to)) {
            throw new IllegalArgumentException(ErrorMessages.SAME_CURRENCY_ERROR);
        }
        
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(ErrorMessages.INVALID_AMOUNT_ERROR);
        }
        
        // Check supported currencies using constants
        if (!CurrencyConstants.isSupportedCurrency(from)) {
            throw new IllegalArgumentException(String.format(ErrorMessages.UNSUPPORTED_CURRENCY_ERROR, from));
        }
        if (!CurrencyConstants.isSupportedCurrency(to)) {
            throw new IllegalArgumentException(String.format(ErrorMessages.UNSUPPORTED_CURRENCY_ERROR, to));
        }
        
        // Try direct conversion
        Optional<ExchangeRate> directRate = getLatestRate(from, to);
        if (directRate.isPresent()) {
            BigDecimal rate = directRate.get().getRate();
            BigDecimal result = request.getAmount().multiply(rate);
            
            // 根據目標貨幣決定精度
            int scale = getCurrencyScale(to);
            result = result.setScale(scale, RoundingMode.HALF_UP);
            
            return ConversionResponse.builder()
                    .fromCurrency(from)
                    .toCurrency(to)
                    .fromAmount(request.getAmount())
                    .toAmount(result)
                    .rate(rate)
                    .conversionDate(LocalDateTime.now())
                    .build();
        }
        
        // Try reverse conversion
        Optional<ExchangeRate> reverseRate = getLatestRate(to, from);
        if (reverseRate.isPresent()) {
            // 計算反向匯率，使用更高精度以避免累積誤差
            BigDecimal rate = BigDecimal.ONE.divide(reverseRate.get().getRate(), 10, RoundingMode.HALF_UP);
            BigDecimal result = request.getAmount().multiply(rate);
            
            // 如果結果應該是整數，確保沒有小數部分
            if (result.stripTrailingZeros().scale() <= 0) {
                result = result.setScale(0, RoundingMode.HALF_UP);
            } else {
                // 使用統一的精度設定
                int scale = getCurrencyScale(to);
                result = result.setScale(scale, RoundingMode.HALF_UP);
            }
            
            return ConversionResponse.builder()
                    .fromCurrency(from)
                    .toCurrency(to)
                    .fromAmount(request.getAmount())
                    .toAmount(result)
                    .rate(rate.setScale(5, RoundingMode.HALF_UP))
                    .conversionDate(LocalDateTime.now())
                    .build();
        }
        
        // Try chain conversion through USD
        if (!"USD".equals(from) && !"USD".equals(to)) {
            Optional<ExchangeRate> fromToUsd = getLatestRate(from, "USD");
            Optional<ExchangeRate> usdToTarget = getLatestRate("USD", to);
            
            if (fromToUsd.isPresent() && usdToTarget.isPresent()) {
                BigDecimal rate = fromToUsd.get().getRate().multiply(usdToTarget.get().getRate());
                BigDecimal result = request.getAmount().multiply(rate);
                
                // 使用統一的精度設定
                int scale = getCurrencyScale(to);
                result = result.setScale(scale, RoundingMode.HALF_UP);
                
                return ConversionResponse.builder()
                        .fromCurrency(from)
                        .toCurrency(to)
                        .fromAmount(request.getAmount())
                        .toAmount(result)
                        .rate(rate.setScale(6, RoundingMode.HALF_UP))
                        .conversionDate(LocalDateTime.now())
                        .conversionPath(from + "→USD→" + to)
                        .build();
            }
        }
        
        throw new IllegalArgumentException(ErrorMessages.RATE_NOT_FOUND_ERROR);
    }
    
    public void deleteExchangeRateByPair(String from, String to) {
        String key = generateKey(from.toUpperCase(), to.toUpperCase());
        ExchangeRate rate = currencyPairIndex.remove(key);
        if (rate != null) {
            storage.remove(rate.getId());
        } else {
            throw new IllegalArgumentException("找不到指定的匯率資料");
        }
    }
    
    private String generateKey(String fromCurrency, String toCurrency) {
        return fromCurrency + "_" + toCurrency;
    }
    
    /**
     * 根據貨幣代碼返回適當的小數位數
     */
    private int getCurrencyScale(String currency) {
        switch (currency) {
            case "JPY":
                return 0; // 日圓不使用小數
            case "TWD":
            case "CNY":
            case "USD":
            case "EUR":
            case "GBP":
            case "AUD":
            case "CAD":
            case "CHF":
                return 2; // 大多數貨幣使用2位小數
            default:
                return 6; // 預設高精度
        }
    }
    
    public void clear() {
        storage.clear();
        currencyPairIndex.clear();
        nextId = 1L;
    }
    
    /**
     * 驗證ExchangeRate對象的有效性
     */
    private void validateExchangeRate(ExchangeRate exchangeRate) {
        if (exchangeRate == null) {
            throw new IllegalArgumentException("Exchange rate object cannot be null");
        }
        if (exchangeRate.getFromCurrency() == null || exchangeRate.getFromCurrency().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.NULL_CURRENCY_ERROR);
        }
        if (exchangeRate.getToCurrency() == null || exchangeRate.getToCurrency().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.NULL_CURRENCY_ERROR);
        }
        if (exchangeRate.getRate() == null) {
            throw new IllegalArgumentException(ErrorMessages.INVALID_RATE_ERROR);
        }
    }
    
    /**
     * 驗證ConversionRequest對象的有效性
     */
    private void validateConversionRequest(ConversionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Conversion request cannot be null");
        }
        if (request.getFromCurrency() == null || request.getFromCurrency().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.NULL_CURRENCY_ERROR);
        }
        if (request.getToCurrency() == null || request.getToCurrency().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.NULL_CURRENCY_ERROR);
        }
        if (request.getAmount() == null) {
            throw new IllegalArgumentException(ErrorMessages.INVALID_AMOUNT_ERROR);
        }
    }
}