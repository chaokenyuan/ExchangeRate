package com.exchangerate.infrastructure.constants;

/**
 * Error message constants definition
 * Centralized error message management with internationalization support
 */
public final class ErrorMessages {
    
    private ErrorMessages() {
        // Utility class - prevent instantiation
    }
    
    // Validation related error messages
    public static final String SAME_CURRENCY_ERROR = "Source and target currencies cannot be the same";
    public static final String INVALID_RATE_ERROR = "Exchange rate must be greater than 0";
    public static final String UNSUPPORTED_CURRENCY_ERROR = "Unsupported currency code: %s";
    public static final String INVALID_AMOUNT_ERROR = "Amount must be greater than 0";
    public static final String NULL_CURRENCY_ERROR = "Currency code cannot be null";
    public static final String INVALID_CURRENCY_LENGTH_ERROR = "Currency code must be exactly 3 characters";
    
    // Business logic error messages
    public static final String RATE_NOT_FOUND_ERROR = "No available exchange rate found";
    public static final String DUPLICATE_RATE_ERROR = "Exchange rate for this currency pair already exists";
    
    // Permission related error messages
    public static final String SESSION_EXPIRED_ERROR = "Session has expired, please log in again";
    public static final String INVALID_TOKEN_ERROR = "Authentication token is invalid or expired";
    public static final String LOGIN_REQUIRED_ERROR = "Login required";
    public static final String INSUFFICIENT_PERMISSIONS_ERROR = "Insufficient permissions";
    
    // HTTP related error messages
    public static final String ENDPOINT_NOT_FOUND_ERROR = "Endpoint not found";
    public static final String INTERNAL_SERVER_ERROR = "Internal server error";
}