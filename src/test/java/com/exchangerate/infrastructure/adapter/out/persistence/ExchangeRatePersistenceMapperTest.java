package com.exchangerate.infrastructure.adapter.out.persistence;

import com.exchangerate.domain.model.entity.ExchangeRate;
import com.exchangerate.domain.model.valueobject.CurrencyCode;
import com.exchangerate.domain.model.valueobject.CurrencyPair;
import com.exchangerate.domain.model.valueobject.Rate;
import com.exchangerate.infrastructure.adapter.out.persistence.entity.ExchangeRateJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ExchangeRatePersistenceMapperTest {

    private ExchangeRatePersistenceMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ExchangeRatePersistenceMapper();
    }

    @Test
    void toDomain_成功轉換JPA實體到領域對象() {
        // Given
        ExchangeRateJpaEntity jpaEntity = new ExchangeRateJpaEntity(
            1L, "USD", "EUR", new BigDecimal("0.85"), 
            LocalDateTime.of(2024, 1, 15, 10, 30), "Central Bank"
        );

        // When
        ExchangeRate domainObject = mapper.toDomain(jpaEntity);

        // Then
        assertNotNull(domainObject);
        assertEquals("USD", domainObject.getCurrencyPair().getFromCurrency().getValue());
        assertEquals("EUR", domainObject.getCurrencyPair().getToCurrency().getValue());
        assertEquals(new BigDecimal("0.85"), domainObject.getRate().getValue());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 30), domainObject.getTimestamp());
        assertEquals("Central Bank", domainObject.getSource());
    }

    @Test
    void toJpa_成功轉換領域對象到JPA實體() {
        // Given
        CurrencyPair currencyPair = CurrencyPair.of(
            CurrencyCode.of("USD"), 
            CurrencyCode.of("EUR")
        );
        ExchangeRate domainObject = ExchangeRate.create(
            currencyPair,
            Rate.of(new BigDecimal("0.85")),
            "Central Bank"
        );

        // When
        ExchangeRateJpaEntity jpaEntity = mapper.toJpa(domainObject);

        // Then
        assertNotNull(jpaEntity);
        assertEquals("USD", jpaEntity.getFromCurrency());
        assertEquals("EUR", jpaEntity.getToCurrency());
        assertEquals(new BigDecimal("0.85"), jpaEntity.getRate());
        // JPA實體的時間戳會在創建時被設為當前時間，所以只驗證不為null
        assertNotNull(jpaEntity.getTimestamp());
        assertEquals("Central Bank", jpaEntity.getSource());
    }

    @Test
    void toDomain_當JPA實體為null_返回null() {
        // When
        ExchangeRate result = mapper.toDomain(null);

        // Then
        assertNull(result);
    }

    @Test
    void toJpa_當領域對象為null_返回null() {
        // When
        ExchangeRateJpaEntity result = mapper.toJpa(null);

        // Then
        assertNull(result);
    }

    @Test
    void toDomain_當JPA實體沒有ID_應該生成UUID() {
        // Given
        ExchangeRateJpaEntity jpaEntity = new ExchangeRateJpaEntity(
            null, "USD", "EUR", new BigDecimal("0.85"), 
            LocalDateTime.of(2024, 1, 15, 10, 30), "Central Bank"
        );

        // When
        ExchangeRate domainObject = mapper.toDomain(jpaEntity);

        // Then
        assertNotNull(domainObject);
        assertNotNull(domainObject.getId());
        // ID應該是UUID格式或系統生成的唯一標識
    }
}