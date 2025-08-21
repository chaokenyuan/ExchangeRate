package com.exchangerate.infrastructure.adapter.in.web;

import com.exchangerate.application.dto.command.ConvertCurrencyCommand;
import com.exchangerate.application.dto.response.ConversionResponse;
import com.exchangerate.application.mapper.ConversionMapper;
import com.exchangerate.domain.model.valueobject.ConversionResult;
import com.exchangerate.domain.model.valueobject.CurrencyCode;
import com.exchangerate.domain.port.in.ConvertCurrencyUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api")
@Tag(name = "Currency Conversion", description = "Currency conversion related APIs")
public class ConversionController {

    private final ConvertCurrencyUseCase convertCurrencyUseCase;
    private final ConversionMapper conversionMapper;

    public ConversionController(ConvertCurrencyUseCase convertCurrencyUseCase, 
                               ConversionMapper conversionMapper) {
        this.convertCurrencyUseCase = convertCurrencyUseCase;
        this.conversionMapper = conversionMapper;
    }

    @Operation(
        summary = "Currency Conversion",
        description = """
            Convert specified amount from one currency to another.
            
            The system supports the following conversion strategies:
            1. Direct conversion: Use direct exchange rate for conversion
            2. Reverse conversion: Use reverse exchange rate for conversion
            3. Chain conversion: Convert through intermediate currency (like USD)
            
            The conversion result will show the actual conversion path used.
            """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Currency conversion request",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ConvertCurrencyCommand.class),
                examples = {
                    @ExampleObject(
                        name = "USD to EUR",
                        value = """
                            {
                                "from_currency": "USD",
                                "to_currency": "EUR", 
                                "amount": 100.00
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Large amount conversion",
                        value = """
                            {
                                "from_currency": "USD",
                                "to_currency": "JPY",
                                "amount": 50000.00
                            }
                            """
                    )
                }
            )
        )
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Conversion successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ConversionResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Request parameter error",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Amount error",
                        value = """
                            {
                                "error": "Amount must be greater than 0",
                                "timestamp": "2024-01-15T10:30:00"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Currency code error",
                        value = """
                            {
                                "error": "Unsupported currency code: XXX",
                                "timestamp": "2024-01-15T10:30:00"
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Exchange rate not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                            "error": "No available exchange rate found",
                            "timestamp": "2024-01-15T10:30:00"
                        }
                        """
                )
            )
        )
    })
    @PostMapping("/convert")
    public ResponseEntity<?> convertCurrency(@Valid @RequestBody ConvertCurrencyCommand command) {
        try {
            // Validate same currency
            if (command.getFromCurrency().equalsIgnoreCase(command.getToCurrency())) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Source and target currencies cannot be the same");
                error.put("timestamp", java.time.LocalDateTime.now());
                return ResponseEntity.badRequest().body(error);
            }

            CurrencyCode fromCurrency = conversionMapper.mapFromCurrency(command);
            CurrencyCode toCurrency = conversionMapper.mapToCurrency(command);
            
            ConversionResult result = convertCurrencyUseCase.convertCurrency(
                fromCurrency, 
                toCurrency, 
                command.getAmount()
            );
            
            ConversionResponse response = conversionMapper.mapToResponse(result);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
