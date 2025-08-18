package com.exchangerate.dto;

import lombok.Data;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ConversionResponse {
    
    private String from_currency;
    private String to_currency;
    private BigDecimal from_amount;
    private BigDecimal to_amount;
    private BigDecimal rate;
    private LocalDateTime conversion_date;
    private String conversion_path;
}