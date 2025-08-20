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
    @NotBlank(message = "Source currency is required")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    @JsonProperty("from_currency")
    private String fromCurrency;

    @Column(nullable = false, length = 3)
    @NotBlank(message = "Target currency is required")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    @JsonProperty("to_currency")
    private String toCurrency;

    @Column(nullable = false, precision = 19, scale = 6)
    @NotNull(message = "Exchange rate is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Exchange rate must be greater than 0")
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