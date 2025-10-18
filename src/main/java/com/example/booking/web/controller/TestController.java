package com.example.booking.web.controller;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Test controller for rate limiting
 */
@RestController
@RequestMapping("/api")
public class TestController {
    
    @Autowired
    private DataSource dataSource;
    
    @GetMapping("/db-check")
    public ResponseEntity<Map<String, Object>> databaseCheck() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Connection connection = dataSource.getConnection();
            boolean isValid = connection.isValid(5);
            connection.close();
            
            response.put("status", isValid ? "UP" : "DOWN");
            response.put("database", "connected");
            response.put("timestamp", LocalDateTime.now());
            response.put("message", isValid ? "Database connection successful" : "Database connection failed");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("database", "error");
            response.put("timestamp", LocalDateTime.now());
            response.put("message", "Database connection error: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "Application is running");
        response.put("version", "1.0.0");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testEndpoint(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Rate limiting test endpoint");
        response.put("timestamp", LocalDateTime.now());
        response.put("clientIp", getClientIpAddress(request));
        response.put("userAgent", request.getHeader("User-Agent"));
        response.put("status", "success");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/test/login")
    public ResponseEntity<Map<String, Object>> testLoginEndpoint(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login rate limiting test endpoint");
        response.put("timestamp", LocalDateTime.now());
        response.put("clientIp", getClientIpAddress(request));
        response.put("status", "success");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/test/booking")
    public ResponseEntity<Map<String, Object>> testBookingEndpoint(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Booking rate limiting test endpoint");
        response.put("timestamp", LocalDateTime.now());
        response.put("clientIp", getClientIpAddress(request));
        response.put("status", "success");
        
        return ResponseEntity.ok(response);
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}