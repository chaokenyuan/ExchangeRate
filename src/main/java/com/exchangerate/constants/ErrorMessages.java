package com.exchangerate.constants;

/**
 * 錯誤訊息常數定義
 * 集中管理錯誤訊息，支援國際化
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
    public static final String RATE_NOT_FOUND_ERROR = "No exchange rate found for conversion";
    public static final String DUPLICATE_RATE_ERROR = "Exchange rate already exists for this currency pair";
    
    // 權限相關錯誤訊息
    public static final String SESSION_EXPIRED_ERROR = "會話已過期，請重新登入";
    public static final String INVALID_TOKEN_ERROR = "認證令牌無效或已過期";
    public static final String LOGIN_REQUIRED_ERROR = "需要登入";
    public static final String INSUFFICIENT_PERMISSIONS_ERROR = "權限不足";
    
    // HTTP相關錯誤訊息
    public static final String ENDPOINT_NOT_FOUND_ERROR = "端點不存在";
    public static final String INTERNAL_SERVER_ERROR = "內部伺服器錯誤";
}