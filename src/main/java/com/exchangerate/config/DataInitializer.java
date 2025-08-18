package com.exchangerate.config;

import com.exchangerate.model.ExchangeRate;
import com.exchangerate.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(ExchangeRateRepository repository) {
        return args -> {
            repository.save(new ExchangeRate(null, "USD", "EUR", 
                new BigDecimal("0.92"), LocalDateTime.now(), "Central Bank"));
            
            repository.save(new ExchangeRate(null, "USD", "GBP", 
                new BigDecimal("0.79"), LocalDateTime.now(), "Central Bank"));
            
            repository.save(new ExchangeRate(null, "USD", "JPY", 
                new BigDecimal("149.50"), LocalDateTime.now(), "Central Bank"));
            
            repository.save(new ExchangeRate(null, "EUR", "USD", 
                new BigDecimal("1.09"), LocalDateTime.now(), "Central Bank"));
            
            repository.save(new ExchangeRate(null, "EUR", "GBP", 
                new BigDecimal("0.86"), LocalDateTime.now(), "Central Bank"));
            
            repository.save(new ExchangeRate(null, "GBP", "USD", 
                new BigDecimal("1.27"), LocalDateTime.now(), "Central Bank"));
            
            repository.save(new ExchangeRate(null, "USD", "CNY", 
                new BigDecimal("7.24"), LocalDateTime.now(), "Central Bank"));
            
            repository.save(new ExchangeRate(null, "USD", "CHF", 
                new BigDecimal("0.88"), LocalDateTime.now(), "Central Bank"));
        };
    }
}