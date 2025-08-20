package com.exchangerate.stepdefinitions;

/**
 * 在步驟定義類別間共享會話狀態
 */
public class SessionContext {
    private static boolean sessionExpired = false;
    private static boolean isLoggedIn = false;
    private static String userRole = "anonymous";
    private static boolean hasValidToken = false;
    
    public static boolean isSessionExpired() {
        return sessionExpired;
    }
    
    public static void setSessionExpired(boolean expired) {
        sessionExpired = expired;
    }
    
    public static boolean isLoggedIn() {
        return isLoggedIn;
    }
    
    public static void setLoggedIn(boolean loggedIn) {
        SessionContext.isLoggedIn = loggedIn;
    }
    
    public static String getUserRole() {
        return userRole;
    }
    
    public static void setUserRole(String role) {
        userRole = role;
    }
    
    public static boolean hasValidToken() {
        return hasValidToken;
    }
    
    public static void setHasValidToken(boolean hasToken) {
        hasValidToken = hasToken;
    }
    
    public static void reset() {
        sessionExpired = false;
        isLoggedIn = false;
        userRole = "anonymous";
        hasValidToken = false;
    }
}