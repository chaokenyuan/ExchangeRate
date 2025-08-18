package com.exchangerate.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_rates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 3)
    @NotBlank(message = "來源貨幣為必填欄位")
    @Size(min = 3, max = 3, message = "貨幣代碼必須為3個字元")
    @JsonProperty("from_currency")
    private String fromCurrency;

    @Column(nullable = false, length = 3)
    @NotBlank(message = "目標貨幣為必填欄位")
    @Size(min = 3, max = 3, message = "貨幣代碼必須為3個字元")
    @JsonProperty("to_currency")
    private String toCurrency;

    @Column(nullable = false, precision = 19, scale = 6)
    @NotNull(message = "匯率為必填欄位")
    @DecimalMin(value = "0.0", inclusive = false, message = "匯率必須大於0")
    private BigDecimal rate;

    @Column(nullable = false)
    @JsonProperty("updated_at")
    private LocalDateTime timestamp;

    @JsonProperty("created_at")
    public LocalDateTime getCreatedAt() {
        return timestamp;
    }

    @Column(length = 50)
    private String source;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}