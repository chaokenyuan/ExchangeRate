package com.exchangerate.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        // 收集所有驗證錯誤訊息
        String message = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    if (error instanceof FieldError) {
                        return ((FieldError) error).getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .findFirst()
                .orElse("驗證失敗");
        
        // 返回JSON格式的錯誤訊息
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        errorResponse.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        errorResponse.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        errorResponse.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateResourceException(
            DuplicateResourceException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        errorResponse.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        errorResponse.put("timestamp", LocalDateTime.now());
        // 大部分 RuntimeException 在這個應用中都是業務邏輯錯誤，返回 400
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}