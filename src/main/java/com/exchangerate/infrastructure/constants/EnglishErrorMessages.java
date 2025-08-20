package com.exchangerate.infrastructure.constants;

/**
 * English error message constants for unit tests
 * Provides English versions of error messages for testing compatibility
 */
public final class EnglishErrorMessages {
    
    private EnglishErrorMessages() {
        // Utility class - prevent instantiation
    }
    
    // Validation error messages
    public static final String SAME_CURRENCY_ERROR = "Source and target currencies cannot be the same";
    public static final String INVALID_RATE_ERROR = "Exchange rate must be greater than 0";
    public static final String UNSUPPORTED_CURRENCY_ERROR = "Unsupported currency code: %s";
    public static final String INVALID_AMOUNT_ERROR = "Amount must be greater than 0";
    public static final String NULL_CURRENCY_ERROR = "Currency code cannot be null";
    public static final String INVALID_CURRENCY_LENGTH_ERROR = "Currency code must be 3 characters";
    
    // Business logic error messages
    public static final String RATE_NOT_FOUND_ERROR = "No exchange rate found for conversion";
    public static final String DUPLICATE_RATE_ERROR = "Exchange rate already exists for this currency pair";
    
    // Permission error messages
    public static final String SESSION_EXPIRED_ERROR = "Session expired, please login again";
    public static final String INVALID_TOKEN_ERROR = "Invalid or expired authentication token";
    public static final String LOGIN_REQUIRED_ERROR = "Login required";
    public static final String INSUFFICIENT_PERMISSIONS_ERROR = "Insufficient permissions";
    
    // HTTP error messages
    public static final String ENDPOINT_NOT_FOUND_ERROR = "Endpoint not found";
    public static final String INTERNAL_SERVER_ERROR = "Internal server error";
}