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
import org.springframework.context.annotation.Profile;

import java.util.Map;
import java.util.HashMap;

@Profile("hex")
@RestController
@RequestMapping("/api")
@Tag(name = "貨幣轉換", description = "貨幣轉換相關API")
public class ConversionController {

    private final ConvertCurrencyUseCase convertCurrencyUseCase;
    private final ConversionMapper conversionMapper;

    public ConversionController(ConvertCurrencyUseCase convertCurrencyUseCase, 
                               ConversionMapper conversionMapper) {
        this.convertCurrencyUseCase = convertCurrencyUseCase;
        this.conversionMapper = conversionMapper;
    }

    @Operation(
        summary = "貨幣轉換",
        description = """
            將指定金額從一種貨幣轉換為另一種貨幣。
            
            系統支援以下轉換策略：
            1. 直接轉換：使用直接的匯率進行轉換
            2. 反向轉換：使用反向匯率進行轉換
            3. 鏈式轉換：通過中介貨幣（如USD）進行轉換
            
            轉換結果會顯示實際使用的轉換路徑。
            """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "貨幣轉換請求",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ConvertCurrencyCommand.class),
                examples = {
                    @ExampleObject(
                        name = "USD轉EUR",
                        value = """
                            {
                                "from_currency": "USD",
                                "to_currency": "EUR", 
                                "amount": 100.00
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "大額轉換",
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
            description = "轉換成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ConversionResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "請求參數錯誤",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "金額錯誤",
                        value = """
                            {
                                "error": "金額必須大於0",
                                "timestamp": "2024-01-15T10:30:00"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "貨幣代碼錯誤",
                        value = """
                            {
                                "error": "不支援的貨幣代碼: XXX",
                                "timestamp": "2024-01-15T10:30:00"
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "找不到匯率",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                            "error": "找不到可用的匯率",
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
                error.put("error", "來源與目標貨幣不可相同");
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
