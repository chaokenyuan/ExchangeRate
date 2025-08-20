package com.exchangerate.infrastructure.config;

import com.exchangerate.infrastructure.adapter.out.persistence.entity.ExchangeRateJpaEntity;
import com.exchangerate.infrastructure.adapter.out.persistence.repository.ExchangeRateJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
@Profile("hex")
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(ExchangeRateJpaRepository repository) {
        return args -> {
            // USD -> EUR
            repository.save(new ExchangeRateJpaEntity(
                "USD", "EUR", new BigDecimal("0.92"), "Central Bank"
            ));
            
            // USD -> GBP
            repository.save(new ExchangeRateJpaEntity(
                "USD", "GBP", new BigDecimal("0.79"), "Central Bank"
            ));
            
            // USD -> JPY
            repository.save(new ExchangeRateJpaEntity(
                "USD", "JPY", new BigDecimal("149.50"), "Central Bank"
            ));
            
            // EUR -> USD
            repository.save(new ExchangeRateJpaEntity(
                "EUR", "USD", new BigDecimal("1.09"), "Central Bank"
            ));
            
            // EUR -> GBP
            repository.save(new ExchangeRateJpaEntity(
                "EUR", "GBP", new BigDecimal("0.86"), "Central Bank"
            ));
            
            // GBP -> USD
            repository.save(new ExchangeRateJpaEntity(
                "GBP", "USD", new BigDecimal("1.27"), "Central Bank"
            ));
            
            // USD -> CNY
            repository.save(new ExchangeRateJpaEntity(
                "USD", "CNY", new BigDecimal("7.24"), "Central Bank"
            ));
            
            // USD -> CHF
            repository.save(new ExchangeRateJpaEntity(
                "USD", "CHF", new BigDecimal("0.88"), "Central Bank"
            ));
        };
    }
}