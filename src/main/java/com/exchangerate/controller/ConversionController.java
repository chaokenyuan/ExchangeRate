package com.exchangerate.controller;

import com.exchangerate.dto.ConversionRequest;
import com.exchangerate.dto.ConversionResponse;
import com.exchangerate.service.ExchangeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "貨幣轉換", description = "貨幣轉換相關API")
public class ConversionController {

    private final ExchangeRateService exchangeRateService;

    @Operation(
        summary = "貨幣轉換",
        description = "根據提供的來源貨幣、目標貨幣和金額，執行即時貨幣轉換計算。" +
                     "支援直接轉換、反向轉換和通過USD的鏈式轉換。",
        tags = {"貨幣轉換"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "轉換成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ConversionResponse.class),
                examples = @ExampleObject(
                    name = "成功轉換範例",
                    value = """
                        {
                            "from_currency": "USD",
                            "to_currency": "EUR",
                            "from_amount": 100.00,
                            "to_amount": 85.000000,
                            "rate": 0.85,
                            "conversion_date": "2024-01-15T10:30:00",
                            "conversion_path": "USD→EUR"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "請求參數錯誤或轉換失敗",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    type = "object",
                    example = "{\"error\": \"來源與目標貨幣不可相同\"}"
                ),
                examples = {
                    @ExampleObject(
                        name = "相同貨幣錯誤",
                        value = "{\"error\": \"來源與目標貨幣不可相同\"}"
                    ),
                    @ExampleObject(
                        name = "不支援的貨幣",
                        value = "{\"error\": \"不支援的貨幣代碼: XXX\"}"
                    ),
                    @ExampleObject(
                        name = "找不到匯率",
                        value = "{\"error\": \"找不到匯率資料進行換算\"}"
                    ),
                    @ExampleObject(
                        name = "驗證錯誤",
                        value = "{\"error\": \"金額必須大於0\"}"
                    )
                }
            )
        )
    })
    @PostMapping("/convert")
    public ResponseEntity<ConversionResponse> convertCurrency(
        @Parameter(
            description = "貨幣轉換請求資料，包含來源貨幣、目標貨幣和轉換金額",
            required = true,
            schema = @Schema(implementation = ConversionRequest.class)
        )
        @Valid @RequestBody ConversionRequest request
    ) {
        ConversionResponse response = exchangeRateService.convertCurrencyDetailed(request);
        return ResponseEntity.ok(response);
    }
}