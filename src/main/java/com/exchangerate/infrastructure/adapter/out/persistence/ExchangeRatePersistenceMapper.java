package com.exchangerate.infrastructure.adapter.out.persistence;

import com.exchangerate.domain.model.entity.ExchangeRate;
import com.exchangerate.domain.model.valueobject.CurrencyCode;
import com.exchangerate.domain.model.valueobject.CurrencyPair;
import com.exchangerate.domain.model.valueobject.Rate;
import com.exchangerate.infrastructure.adapter.out.persistence.entity.ExchangeRateJpaEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Maps between Domain ExchangeRate aggregate and JPA ExchangeRate entity
 */
@Component
public class ExchangeRatePersistenceMapper {

    public ExchangeRate toDomain(ExchangeRateJpaEntity jpa) {
        if (jpa == null) return null;
        String id = jpa.getId() == null ? null : String.valueOf(jpa.getId());
        CurrencyPair pair = CurrencyPair.of(
                CurrencyCode.of(jpa.getFromCurrency()),
                CurrencyCode.of(jpa.getToCurrency())
        );
        Rate rate = Rate.of(jpa.getRate());
        LocalDateTime ts = jpa.getTimestamp();
        String source = jpa.getSource();
        if (id == null) {
            // reconstruct requires non-null id; if missing, generate from timestamp
            id = java.util.UUID.randomUUID().toString();
        }
        return ExchangeRate.reconstruct(id, pair, rate, source, ts);
    }

    public ExchangeRateJpaEntity toJpa(ExchangeRate domain) {
        if (domain == null) return null;
        ExchangeRateJpaEntity jpa = new ExchangeRateJpaEntity();
        // Map ID only if it's a numeric value; otherwise keep null to let JPA generate
        Long id = tryParseLong(domain.getId());
        if (id != null) {
            jpa.setId(id);
        }
        jpa.setFromCurrency(domain.getCurrencyPair().getFromCurrency().getValue());
        jpa.setToCurrency(domain.getCurrencyPair().getToCurrency().getValue());
        BigDecimal rate = domain.getRate().getValue();
        jpa.setRate(rate);
        jpa.setTimestamp(domain.getTimestamp());
        jpa.setSource(domain.getSource());
        return jpa;
    }

    private Long tryParseLong(String value) {
        if (value == null) return null;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
