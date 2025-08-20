package com.exchangerate.application.service;

import com.exchangerate.application.dto.query.GetExchangeRateByIdQuery;
import com.exchangerate.application.dto.query.GetExchangeRateQuery;
import com.exchangerate.application.dto.query.ListExchangeRatesQuery;
import com.exchangerate.application.dto.response.ExchangeRateResponse;
import com.exchangerate.application.mapper.ExchangeRateMapper;
import com.exchangerate.domain.model.entity.ExchangeRate;
import com.exchangerate.domain.model.valueobject.CurrencyCode;
import com.exchangerate.domain.model.valueobject.CurrencyPair;
import com.exchangerate.domain.port.out.ExchangeRateRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 查詢端服務 - CQRS模式的Query端實現
 * 負責處理所有查詢相關的業務邏輯
 */
@Service
@Transactional(readOnly = true)
public class ExchangeRateQueryService {
    
    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeRateMapper exchangeRateMapper;
    
    public ExchangeRateQueryService(ExchangeRateRepository exchangeRateRepository, 
                                   ExchangeRateMapper exchangeRateMapper) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.exchangeRateMapper = exchangeRateMapper;
    }
    
    /**
     * 根據貨幣對查詢最新匯率
     */
    public Optional<ExchangeRateResponse> getLatestExchangeRate(GetExchangeRateQuery query) {
        CurrencyPair currencyPair = CurrencyPair.of(
            CurrencyCode.of(query.getFromCurrency()),
            CurrencyCode.of(query.getToCurrency())
        );
        
        return exchangeRateRepository
            .findLatestByCurrencyPair(currencyPair)
            .map(exchangeRateMapper::toResponse);
    }
    
    /**
     * 根據ID查詢匯率
     */
    public Optional<ExchangeRateResponse> getExchangeRateById(GetExchangeRateByIdQuery query) {
        return exchangeRateRepository
            .findById(query.getId())
            .map(exchangeRateMapper::toResponse);
    }
    
    /**
     * 查詢匯率列表（支援篩選和分頁）
     */
    public Object listExchangeRates(ListExchangeRatesQuery query) {
        if (query.hasPagination()) {
            return listExchangeRatesWithPagination(query);
        } else {
            return listExchangeRatesWithoutPagination(query);
        }
    }
    
    private Page<ExchangeRateResponse> listExchangeRatesWithPagination(ListExchangeRatesQuery query) {
        Sort sort = Sort.by(
            query.getSortDirection().equalsIgnoreCase("ASC") 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC,
            query.getSortBy()
        );
        
        Pageable pageable = PageRequest.of(
            query.getPage() - 1, // 轉換為0-based索引
            query.getLimit(),
            sort
        );
        
        Page<ExchangeRate> page;
        
        if (query.hasFromFilter() && query.hasToFilter()) {
            page = exchangeRateRepository.findByFromCurrencyAndToCurrency(
                query.getFromCurrency(), query.getToCurrency(), pageable);
        } else if (query.hasFromFilter()) {
            page = exchangeRateRepository.findByFromCurrency(
                query.getFromCurrency(), pageable);
        } else if (query.hasToFilter()) {
            page = exchangeRateRepository.findByToCurrency(
                query.getToCurrency(), pageable);
        } else {
            page = exchangeRateRepository.findAll(pageable);
        }
        
        return page.map(exchangeRateMapper::toResponse);
    }
    
    private List<ExchangeRateResponse> listExchangeRatesWithoutPagination(ListExchangeRatesQuery query) {
        List<ExchangeRate> rates;
        
        if (query.hasFromFilter() && query.hasToFilter()) {
            rates = exchangeRateRepository.findByFromCurrencyAndToCurrency(
                query.getFromCurrency(), query.getToCurrency());
        } else if (query.hasFromFilter()) {
            rates = exchangeRateRepository.findByFromCurrency(query.getFromCurrency());
        } else if (query.hasToFilter()) {
            rates = exchangeRateRepository.findByToCurrency(query.getToCurrency());
        } else {
            rates = exchangeRateRepository.findAll();
        }
        
        return rates.stream()
            .map(exchangeRateMapper::toResponse)
            .collect(Collectors.toList());
    }
}