package com.navan.billing.web;

import com.navan.billing.exception.ApiException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException exception) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", exception.getCode());
        body.put("message", exception.getMessage());
        body.putAll(exception.getExtra());
        return ResponseEntity.status(exception.getStatus()).body(body);
    }
}
