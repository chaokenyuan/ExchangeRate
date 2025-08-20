package com.exchangerate.constants;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * 貨幣相關常數定義
 * 集中管理支援的貨幣代碼，避免硬編碼重複
 */
public final class CurrencyConstants {
    
    private CurrencyConstants() {
        // Utility class - prevent instantiation
    }
    
    /**
     * 支援的貨幣代碼清單 (ISO 4217)
     */
    public static final List<String> SUPPORTED_CURRENCIES = Arrays.asList(
        "USD", // 美元
        "EUR", // 歐元 - 中介轉換需要
        "JPY", // 日圓
        "CNY", // 人民幣
        "CHF", // 瑞士法郎
        "TWD", // 台幣
        "AUD", // 澳幣
        "CAD", // 加幣
        "GBP"  // 英鎊 - 單元測試需要
    );
    
    /**
     * 支援的貨幣代碼集合 (用於快速查找)
     */
    public static final Set<String> SUPPORTED_CURRENCY_SET = new HashSet<>(SUPPORTED_CURRENCIES);
    
    /**
     * 預設精度配置
     */
    public static final int DEFAULT_SCALE = 6;
    public static final int JPY_SCALE = 0; // 日圓無小數點
    public static final int TWD_SCALE = 2; // 台幣兩位小數
    
    /**
     * 檢查貨幣代碼是否被支援
     */
    public static boolean isSupportedCurrency(String currencyCode) {
        return currencyCode != null && SUPPORTED_CURRENCY_SET.contains(currencyCode.toUpperCase());
    }
    
    /**
     * 根據貨幣代碼獲取對應的精度
     */
    public static int getScaleForCurrency(String currencyCode) {
        if (currencyCode == null) {
            return DEFAULT_SCALE;
        }
        
        switch (currencyCode.toUpperCase()) {
            case "JPY":
                return JPY_SCALE;
            case "TWD":
            case "CNY":
            case "USD":
            case "CHF":
            case "AUD": 
            case "CAD":
                return TWD_SCALE; // 使用2位小數
            default:
                return DEFAULT_SCALE;
        }
    }
}