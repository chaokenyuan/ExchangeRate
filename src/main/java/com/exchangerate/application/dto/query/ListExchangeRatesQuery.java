package com.exchangerate.application.dto.query;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * 查詢匯率列表的Query對象
 * 支援篩選和分頁
 */
public class ListExchangeRatesQuery {
    
    @Size(min = 3, max = 3, message = "貨幣代碼必須為3個字元")
    private String fromCurrency;
    
    @Size(min = 3, max = 3, message = "貨幣代碼必須為3個字元")
    private String toCurrency;
    
    @Min(value = 1, message = "頁碼必須大於0")
    private Integer page;
    
    @Min(value = 1, message = "每頁筆數必須大於0")
    private Integer limit;
    
    private String sortBy = "timestamp";
    private String sortDirection = "DESC";
    
    public ListExchangeRatesQuery() {}
    
    public ListExchangeRatesQuery(String fromCurrency, String toCurrency, Integer page, Integer limit) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.page = page;
        this.limit = limit;
    }
    
    // Getters and Setters
    public String getFromCurrency() {
        return fromCurrency;
    }
    
    public void setFromCurrency(String fromCurrency) {
        this.fromCurrency = fromCurrency;
    }
    
    public String getToCurrency() {
        return toCurrency;
    }
    
    public void setToCurrency(String toCurrency) {
        this.toCurrency = toCurrency;
    }
    
    public Integer getPage() {
        return page;
    }
    
    public void setPage(Integer page) {
        this.page = page;
    }
    
    public Integer getLimit() {
        return limit;
    }
    
    public void setLimit(Integer limit) {
        this.limit = limit;
    }
    
    public String getSortBy() {
        return sortBy;
    }
    
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
    
    public String getSortDirection() {
        return sortDirection;
    }
    
    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
    
    public boolean hasPagination() {
        return page != null && limit != null && page > 0 && limit > 0;
    }
    
    public boolean hasFromFilter() {
        return fromCurrency != null && !fromCurrency.isEmpty();
    }
    
    public boolean hasToFilter() {
        return toCurrency != null && !toCurrency.isEmpty();
    }
}