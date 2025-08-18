package com.exchangerate.controller;

import com.exchangerate.dto.ConversionRequest;
import com.exchangerate.dto.ConversionResponse;
import com.exchangerate.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ConversionController {

    private final ExchangeRateService exchangeRateService;

    @PostMapping("/convert")
    public ResponseEntity<?> convertCurrency(@Valid @RequestBody ConversionRequest request) {
        try {
            ConversionResponse response = exchangeRateService.convertCurrencyDetailed(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}