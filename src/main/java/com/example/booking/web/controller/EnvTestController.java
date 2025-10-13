package com.example.booking.web.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Test controller to check if .env file is being read correctly
 * Remove this controller in production
 */
@RestController
@RequestMapping("/test/env")
public class EnvTestController {

    @Value("${CLOUDINARY_CLOUD_NAME:NOT_FOUND}")
    private String cloudinaryCloudName;

    @Value("${CLOUDINARY_API_KEY:NOT_FOUND}")
    private String cloudinaryApiKey;

    @Value("${CLOUDINARY_API_SECRET:NOT_FOUND}")
    private String cloudinaryApiSecret;

    @Value("${JDBC_DATABASE_URL:NOT_FOUND}")
    private String jdbcUrl;

    @Value("${DB_USERNAME:NOT_FOUND}")
    private String dbUsername;

    @Value("${DB_PASSWORD:NOT_FOUND}")
    private String dbPassword;

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkEnvVariables() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("cloudinary_cloud_name", cloudinaryCloudName);
        response.put("cloudinary_api_key", cloudinaryApiKey != null ? "***" + cloudinaryApiKey.substring(Math.max(0, cloudinaryApiKey.length() - 4)) : "NOT_FOUND");
        response.put("cloudinary_api_secret", cloudinaryApiSecret != null ? "***" + cloudinaryApiSecret.substring(Math.max(0, cloudinaryApiSecret.length() - 4)) : "NOT_FOUND");
        response.put("jdbc_url", jdbcUrl);
        response.put("db_username", dbUsername);
        response.put("db_password", dbPassword != null ? "***" + dbPassword.substring(Math.max(0, dbPassword.length() - 4)) : "NOT_FOUND");
        
        // Check if .env is being read
        boolean envRead = !cloudinaryCloudName.equals("NOT_FOUND") || 
                         !jdbcUrl.equals("NOT_FOUND") || 
                         !dbUsername.equals("NOT_FOUND");
        
        response.put("env_file_read", envRead);
        response.put("message", envRead ? "Environment variables are being read from .env file" : "Environment variables are NOT being read from .env file");
        
        return ResponseEntity.ok(response);
    }
}
