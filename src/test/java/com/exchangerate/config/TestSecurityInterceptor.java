package com.exchangerate.config;

import com.exchangerate.stepdefinitions.SessionContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 測試環境專用的安全攔截器，模擬權限控制
 */
@Component
public class TestSecurityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        // 只對需要權限的操作進行檢查
        if (isProtectedOperation(method, uri)) {
            // 檢查會話是否過期
            if (SessionContext.isSessionExpired()) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"會話已過期，請重新登入\"}");
                return false;
            }
            
            // 檢查是否已登入
            if (!SessionContext.isLoggedIn()) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"需要登入\"}");
                return false;
            }
            
            // 檢查權限
            if (!hasRequiredPermission(method, uri)) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"權限不足\"}");
                return false;
            }
        }
        
        return true;
    }
    
    private boolean isProtectedOperation(String method, String uri) {
        // POST, PUT, DELETE 操作需要權限
        return ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method)) 
               && uri.startsWith("/api/exchange-rates");
    }
    
    private boolean hasRequiredPermission(String method, String uri) {
        String userRole = SessionContext.getUserRole();
        
        // 只有admin可以執行寫入操作
        if ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method)) {
            return "admin".equals(userRole);
        }
        
        return true;
    }
}