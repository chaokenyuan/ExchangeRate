package com.exchangerate.mock;

/**
 * Mock Service Factory for Cucumber Tests
 * 提供完全獨立的Mock服務實現，不依賴Spring Boot
 */
public class MockServiceFactory {
    
    private static MockServiceFactory instance;
    private final MockExchangeRateService mockService;
    
    private MockServiceFactory() {
        this.mockService = new MockExchangeRateService();
    }
    
    public static MockServiceFactory getInstance() {
        if (instance == null) {
            instance = new MockServiceFactory();
        }
        return instance;
    }
    
    public MockExchangeRateService getExchangeRateService() {
        return mockService;
    }
    
    public void reset() {
        mockService.clear();
    }
}