package com.exchangerate.controller;

import com.exchangerate.model.ExchangeRate;
import com.exchangerate.service.ExchangeRateService;
import com.exchangerate.dto.ConversionRequest;
import com.exchangerate.dto.ConversionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/exchange-rates")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @GetMapping
    public ResponseEntity<?> getAllExchangeRates(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit) {
        
        // Handle pagination only when both page and limit are provided
        if (page != null && limit != null && page > 0 && limit > 0) {
            Pageable pageable = PageRequest.of(page - 1, limit);
            var pagedResult = exchangeRateService.getAllExchangeRates(from, to, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("data", pagedResult.getContent());
            
            // Add pagination info
            Map<String, Object> pagination = new HashMap<>();
            pagination.put("current_page", page);
            pagination.put("total_pages", pagedResult.getTotalPages());
            pagination.put("total_records", pagedResult.getTotalElements());
            pagination.put("has_next", pagedResult.hasNext());
            response.put("pagination", pagination);
            
            return ResponseEntity.ok(response);
        }
        
        // Handle filtering without pagination - return array directly
        List<ExchangeRate> rates = exchangeRateService.getAllExchangeRates(from, to);
        return ResponseEntity.ok(rates);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExchangeRate> getExchangeRateById(@PathVariable Long id) {
        return exchangeRateService.getExchangeRateById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{from}/{to}")
    public ResponseEntity<?> getSpecificExchangeRate(
            @PathVariable String from, 
            @PathVariable String to) {
        try {
            return exchangeRateService.getLatestRate(from, to)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "找不到指定的匯率資料");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/convert")
    public ResponseEntity<BigDecimal> convertCurrency(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigDecimal amount) {
        try {
            BigDecimal result = exchangeRateService.convertCurrency(from, to, amount);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/convert")
    public ResponseEntity<?> convertCurrencyPost(@Valid @RequestBody ConversionRequest request) {
        try {
            ConversionResponse response = exchangeRateService.convertCurrencyDetailed(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/rate")
    public ResponseEntity<ExchangeRate> getLatestRate(
            @RequestParam String from,
            @RequestParam String to) {
        return exchangeRateService.getLatestRate(from, to)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createExchangeRate(@Valid @RequestBody ExchangeRate exchangeRate) {
        try {
            ExchangeRate created = exchangeRateService.saveExchangeRate(exchangeRate);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateExchangeRate(
            @PathVariable Long id,
            @Valid @RequestBody ExchangeRate exchangeRate) {
        try {
            ExchangeRate updated = exchangeRateService.updateExchangeRate(id, exchangeRate);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PutMapping("/{from}/{to}")
    public ResponseEntity<?> updateExchangeRateByPair(
            @PathVariable String from,
            @PathVariable String to,
            @Valid @RequestBody Map<String, Object> updates) {
        try {
            ExchangeRate updated = exchangeRateService.updateExchangeRateByPair(from, to, updates);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExchangeRate(@PathVariable Long id) {
        exchangeRateService.deleteExchangeRate(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{from}/{to}")
    public ResponseEntity<?> deleteExchangeRateByPair(
            @PathVariable String from,
            @PathVariable String to) {
        try {
            exchangeRateService.deleteExchangeRateByPair(from, to);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "找不到指定的匯率資料");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}