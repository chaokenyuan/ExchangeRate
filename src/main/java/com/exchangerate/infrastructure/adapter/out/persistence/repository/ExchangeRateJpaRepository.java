package com.exchangerate.infrastructure.adapter.out.persistence.repository;

import com.exchangerate.infrastructure.adapter.out.persistence.entity.ExchangeRateJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeRateJpaRepository extends JpaRepository<ExchangeRateJpaEntity, Long> {
    
    Optional<ExchangeRateJpaEntity> findTopByFromCurrencyAndToCurrencyOrderByTimestampDesc(
            String fromCurrency, String toCurrency);
    
    List<ExchangeRateJpaEntity> findByFromCurrencyAndToCurrency(
            String fromCurrency, String toCurrency);
    
    Page<ExchangeRateJpaEntity> findByFromCurrencyAndToCurrency(
            String fromCurrency, String toCurrency, Pageable pageable);
    
    List<ExchangeRateJpaEntity> findByFromCurrency(String fromCurrency);
    
    Page<ExchangeRateJpaEntity> findByFromCurrency(String fromCurrency, Pageable pageable);
    
    List<ExchangeRateJpaEntity> findByToCurrency(String toCurrency);
    
    Page<ExchangeRateJpaEntity> findByToCurrency(String toCurrency, Pageable pageable);
    
    @Query("SELECT e FROM ExchangeRateJpaEntity e WHERE e.fromCurrency = :from OR e.toCurrency = :to")
    List<ExchangeRateJpaEntity> findByFromCurrencyOrToCurrency(@Param("from") String from, @Param("to") String to);
    
    boolean existsByFromCurrencyAndToCurrency(String fromCurrency, String toCurrency);
    
    void deleteByFromCurrencyAndToCurrency(String fromCurrency, String toCurrency);
}