package com.exchangerate.infrastructure.adapter.out.persistence;

import com.exchangerate.domain.model.entity.ExchangeRate;
import com.exchangerate.domain.model.valueobject.CurrencyPair;
import com.exchangerate.domain.port.out.ExchangeRateRepository;
import com.exchangerate.infrastructure.adapter.out.persistence.entity.ExchangeRateJpaEntity;
import com.exchangerate.infrastructure.adapter.out.persistence.repository.ExchangeRateJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Outbound adapter: implements domain port using the existing Spring Data JPA repository
 */
@Repository
public class JpaExchangeRateRepositoryAdapter implements ExchangeRateRepository {

    private final ExchangeRateJpaRepository jpaRepository;
    private final ExchangeRatePersistenceMapper mapper;

    public JpaExchangeRateRepositoryAdapter(
            ExchangeRateJpaRepository jpaRepository,
            ExchangeRatePersistenceMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<ExchangeRate> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ExchangeRate> findAll(Pageable pageable) {
        Page<ExchangeRateJpaEntity> page = jpaRepository.findAll(pageable);
        List<ExchangeRate> content = page.getContent().stream().map(mapper::toDomain).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Override
    public Optional<ExchangeRate> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<ExchangeRate> findLatestByCurrencyPair(CurrencyPair currencyPair) {
        return jpaRepository
                .findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc(
                        currencyPair.getFromCurrency().getValue(),
                        currencyPair.getToCurrency().getValue()
                )
                .map(mapper::toDomain);
    }

    @Override
    public List<ExchangeRate> findByFromCurrency(String fromCurrency) {
        return jpaRepository.findByFromCurrency(fromCurrency.toUpperCase())
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Page<ExchangeRate> findByFromCurrency(String fromCurrency, Pageable pageable) {
        Page<ExchangeRateJpaEntity> page = jpaRepository.findByFromCurrency(fromCurrency.toUpperCase(), pageable);
        List<ExchangeRate> content = page.getContent().stream().map(mapper::toDomain).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Override
    public List<ExchangeRate> findByToCurrency(String toCurrency) {
        return jpaRepository.findByToCurrency(toCurrency.toUpperCase())
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Page<ExchangeRate> findByToCurrency(String toCurrency, Pageable pageable) {
        Page<ExchangeRateJpaEntity> page = jpaRepository.findByToCurrency(toCurrency.toUpperCase(), pageable);
        List<ExchangeRate> content = page.getContent().stream().map(mapper::toDomain).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Override
    public List<ExchangeRate> findByFromCurrencyAndToCurrency(String fromCurrency, String toCurrency) {
        return jpaRepository.findByFromCurrencyAndToCurrency(fromCurrency.toUpperCase(), toCurrency.toUpperCase())
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Page<ExchangeRate> findByFromCurrencyAndToCurrency(String fromCurrency, String toCurrency, Pageable pageable) {
        Page<ExchangeRateJpaEntity> page = jpaRepository.findByFromCurrencyAndToCurrency(fromCurrency.toUpperCase(), toCurrency.toUpperCase(), pageable);
        List<ExchangeRate> content = page.getContent().stream().map(mapper::toDomain).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Override
    public ExchangeRate save(ExchangeRate exchangeRate) {
        ExchangeRateJpaEntity saved = jpaRepository.save(mapper.toJpa(exchangeRate));
        return mapper.toDomain(saved);
    }

    @Override
    public void delete(ExchangeRate exchangeRate) {
        ExchangeRateJpaEntity entity = mapper.toJpa(exchangeRate);
        if (entity.getId() != null) {
            jpaRepository.deleteById(entity.getId());
        } else {
            jpaRepository.delete(entity);
        }
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }
}
