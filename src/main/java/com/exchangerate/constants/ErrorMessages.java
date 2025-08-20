package com.exchangerate.constants;

/**
 * 錯誤訊息常數定義
 * 集中管理錯誤訊息，支援國際化
 */
public final class ErrorMessages {
    
    private ErrorMessages() {
        // Utility class - prevent instantiation
    }
    
    // 驗證相關錯誤訊息
    public static final String SAME_CURRENCY_ERROR = "來源與目標貨幣不可相同";
    public static final String INVALID_RATE_ERROR = "匯率必須大於0";
    public static final String UNSUPPORTED_CURRENCY_ERROR = "不支援的貨幣代碼: %s";
    public static final String INVALID_AMOUNT_ERROR = "金額必須大於0";
    public static final String NULL_CURRENCY_ERROR = "貨幣代碼不能為空";
    public static final String INVALID_CURRENCY_LENGTH_ERROR = "貨幣代碼必須為3個字元";
    
    // 業務邏輯錯誤訊息
    public static final String RATE_NOT_FOUND_ERROR = "找不到可用的匯率";
    public static final String DUPLICATE_RATE_ERROR = "此貨幣對的匯率已存在";
    
    // 權限相關錯誤訊息
    public static final String SESSION_EXPIRED_ERROR = "會話已過期，請重新登入";
    public static final String INVALID_TOKEN_ERROR = "認證令牌無效或已過期";
    public static final String LOGIN_REQUIRED_ERROR = "需要登入";
    public static final String INSUFFICIENT_PERMISSIONS_ERROR = "權限不足";
    
    // HTTP相關錯誤訊息
    public static final String ENDPOINT_NOT_FOUND_ERROR = "端點不存在";
    public static final String INTERNAL_SERVER_ERROR = "內部伺服器錯誤";
}