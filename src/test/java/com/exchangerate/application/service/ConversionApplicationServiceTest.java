package com.exchangerate.application.service;

import com.exchangerate.domain.model.entity.ExchangeRate;
import com.exchangerate.domain.model.valueobject.*;
import com.exchangerate.domain.port.out.ExchangeRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConversionApplicationServiceTest {

    private ConversionApplicationService conversionService;

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        conversionService = new ConversionApplicationService(exchangeRateRepository);
    }

    @Test
    void 直接轉換_成功() {
        // Given
        CurrencyCode fromCurrency = CurrencyCode.of("USD");
        CurrencyCode toCurrency = CurrencyCode.of("EUR");
        BigDecimal amount = new BigDecimal("100");
        
        CurrencyPair currencyPair = CurrencyPair.of(fromCurrency, toCurrency);
        ExchangeRate exchangeRate = ExchangeRate.create(
            currencyPair,
            Rate.of(new BigDecimal("0.85")),
            "Central Bank"
        );

        // When
        when(exchangeRateRepository.findLatestByCurrencyPair(currencyPair))
            .thenReturn(Optional.of(exchangeRate));

        ConversionResult result = conversionService.convertCurrency(fromCurrency, toCurrency, amount);

        // Then
        assertNotNull(result);
        assertEquals("USD", result.getCurrencyPair().getFromCurrency().getValue());
        assertEquals("EUR", result.getCurrencyPair().getToCurrency().getValue());
        assertEquals(new BigDecimal("100"), result.getFromAmount());
        assertEquals(0, new BigDecimal("85.00").compareTo(result.getToAmount()));
        assertEquals(new BigDecimal("0.85"), result.getRate().getValue());

        verify(exchangeRateRepository).findLatestByCurrencyPair(currencyPair);
    }

    @Test
    void 相同貨幣轉換_應該拋出異常() {
        // Given
        CurrencyCode sameCurrency = CurrencyCode.of("USD");
        BigDecimal amount = new BigDecimal("100");

        // When & Then
        // 當嘗試創建CurrencyPair時，相同貨幣會拋出IllegalArgumentException
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> {
                // 這裡會在CurrencyPair.of()時拋出異常
                conversionService.convertCurrency(sameCurrency, sameCurrency, amount);
            }
        );

        assertEquals("Source and target currency cannot be the same", exception.getMessage());
        verifyNoInteractions(exchangeRateRepository);
    }

    @Test
    void 負數金額_應該拋出異常() {
        // Given
        CurrencyCode fromCurrency = CurrencyCode.of("USD");
        CurrencyCode toCurrency = CurrencyCode.of("EUR");
        BigDecimal negativeAmount = new BigDecimal("-100");

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> conversionService.convertCurrency(fromCurrency, toCurrency, negativeAmount)
        );

        assertEquals("Amount must be greater than zero", exception.getMessage());
        verifyNoInteractions(exchangeRateRepository);
    }
}