package com.example.booking.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Test controller for rate limiting
 */
@RestController
@RequestMapping("/api")
public class TestController {
    
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