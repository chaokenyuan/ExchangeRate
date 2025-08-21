package com.exchangerate.infrastructure.adapter.in.web;

import com.exchangerate.application.dto.response.ExchangeRateResponse;
import com.exchangerate.application.mapper.ExchangeRateMapper;
import com.exchangerate.domain.model.entity.ExchangeRate;
import com.exchangerate.domain.model.valueobject.CurrencyCode;
import com.exchangerate.domain.model.valueobject.CurrencyPair;
import com.exchangerate.domain.model.valueobject.Rate;
import com.exchangerate.domain.port.in.CreateExchangeRateUseCase;
import com.exchangerate.domain.port.in.QueryExchangeRateUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Profile("hex")
@RestController
@RequestMapping("/api/exchange-rates")
@Tag(name = "Exchange Rate Management", description = "Exchange rate CRUD operations")
public class ExchangeRateController {

    private final CreateExchangeRateUseCase createExchangeRateUseCase;
    private final QueryExchangeRateUseCase queryExchangeRateUseCase;
    private final ExchangeRateMapper exchangeRateMapper;

    public ExchangeRateController(CreateExchangeRateUseCase createExchangeRateUseCase,
                                 QueryExchangeRateUseCase queryExchangeRateUseCase,
                                 ExchangeRateMapper exchangeRateMapper) {
        this.createExchangeRateUseCase = createExchangeRateUseCase;
        this.queryExchangeRateUseCase = queryExchangeRateUseCase;
        this.exchangeRateMapper = exchangeRateMapper;
    }

    @Operation(
        summary = "Create new exchange rate",
        description = "Create or update an exchange rate for a currency pair"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Exchange rate created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping
    public ResponseEntity<?> createExchangeRate(@Valid @RequestBody CreateExchangeRateRequest request) {
        try {
            CurrencyCode fromCurrency = CurrencyCode.of(request.getFromCurrency());
            CurrencyCode toCurrency = CurrencyCode.of(request.getToCurrency());
            CurrencyPair currencyPair = CurrencyPair.of(fromCurrency, toCurrency);
            Rate rate = Rate.fromDouble(request.getRate().doubleValue());
            
            ExchangeRate exchangeRate = createExchangeRateUseCase.createExchangeRate(
                currencyPair, rate, request.getSource()
            );
            
            ExchangeRateResponse response = exchangeRateMapper.toResponse(exchangeRate);
            return ResponseEntity.created(URI.create("/api/exchange-rates/" + exchangeRate.getId()))
                               .body(response);
                               
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @Operation(
        summary = "Get all exchange rates",
        description = "Retrieve all exchange rates with optional pagination"
    )
    @GetMapping
    public ResponseEntity<Page<ExchangeRateResponse>> getAllExchangeRates(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "timestamp") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ExchangeRate> exchangeRates = queryExchangeRateUseCase.getAllExchangeRates(pageable);
        Page<ExchangeRateResponse> response = exchangeRates.map(exchangeRateMapper::toResponse);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get exchange rate by currency pair",
        description = "Get the latest exchange rate for a specific currency pair"
    )
    @GetMapping("/{fromCurrency}/{toCurrency}")
    public ResponseEntity<?> getExchangeRate(
            @Parameter(description = "Source currency code") @PathVariable String fromCurrency,
            @Parameter(description = "Target currency code") @PathVariable String toCurrency) {
        
        try {
            CurrencyCode from = CurrencyCode.of(fromCurrency);
            CurrencyCode to = CurrencyCode.of(toCurrency);
            CurrencyPair currencyPair = CurrencyPair.of(from, to);
            
            Optional<ExchangeRate> exchangeRate = queryExchangeRateUseCase.getLatestExchangeRate(currencyPair);
            
            if (exchangeRate.isPresent()) {
                ExchangeRateResponse response = exchangeRateMapper.toResponse(exchangeRate.get());
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Exchange rate not found for " + fromCurrency + "/" + toCurrency);
                error.put("timestamp", java.time.LocalDateTime.now());
                return ResponseEntity.notFound().build();
            }
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @Operation(
        summary = "Get exchange rates by source currency",
        description = "Get all exchange rates where the specified currency is the source"
    )
    @GetMapping("/from/{currency}")
    public ResponseEntity<List<ExchangeRateResponse>> getExchangeRatesByFromCurrency(
            @Parameter(description = "Source currency code") @PathVariable String currency) {
        
        List<ExchangeRate> exchangeRates = queryExchangeRateUseCase.getExchangeRatesByFromCurrency(currency);
        List<ExchangeRateResponse> response = exchangeRates.stream()
                .map(exchangeRateMapper::toResponse)
                .toList();
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get exchange rates by target currency", 
        description = "Get all exchange rates where the specified currency is the target"
    )
    @GetMapping("/to/{currency}")
    public ResponseEntity<List<ExchangeRateResponse>> getExchangeRatesByToCurrency(
            @Parameter(description = "Target currency code") @PathVariable String currency) {
        
        List<ExchangeRate> exchangeRates = queryExchangeRateUseCase.getExchangeRatesByToCurrency(currency);
        List<ExchangeRateResponse> response = exchangeRates.stream()
                .map(exchangeRateMapper::toResponse)
                .toList();
        
        return ResponseEntity.ok(response);
    }

    // DTO for create request
    @Schema(description = "Request to create or update an exchange rate")
    public static class CreateExchangeRateRequest {
        
        @Schema(description = "Source currency code (ISO 4217)", example = "USD", required = true)
        @NotBlank(message = "Source currency is required")
        @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
        private String fromCurrency;
        
        @Schema(description = "Target currency code (ISO 4217)", example = "EUR", required = true)
        @NotBlank(message = "Target currency is required")
        @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
        private String toCurrency;
        
        @Schema(description = "Exchange rate value", example = "1.08", required = true)
        @NotNull(message = "Rate is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Rate must be greater than 0")
        private BigDecimal rate;
        
        @Schema(description = "Source of the exchange rate data", example = "ECB", required = false)
        private String source;

        // Constructors
        public CreateExchangeRateRequest() {}

        public CreateExchangeRateRequest(String fromCurrency, String toCurrency, BigDecimal rate, String source) {
            this.fromCurrency = fromCurrency;
            this.toCurrency = toCurrency;
            this.rate = rate;
            this.source = source;
        }

        // Getters and setters
        public String getFromCurrency() { return fromCurrency; }
        public void setFromCurrency(String fromCurrency) { this.fromCurrency = fromCurrency; }
        
        public String getToCurrency() { return toCurrency; }
        public void setToCurrency(String toCurrency) { this.toCurrency = toCurrency; }
        
        public BigDecimal getRate() { return rate; }
        public void setRate(BigDecimal rate) { this.rate = rate; }
        
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
    }
}