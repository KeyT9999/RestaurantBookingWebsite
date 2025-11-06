package com.example.booking.web.controller.api;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.booking.domain.User;
import com.example.booking.dto.ai.AIAutoFillRequest;
import com.example.booking.dto.ai.AIAutoFillResponse;
import com.example.booking.dto.ai.AITextImprovementRequest;
import com.example.booking.dto.ai.AITextImprovementResponse;
import com.example.booking.service.SimpleUserService;
import com.example.booking.service.ai.OpenAIService;

import jakarta.validation.Valid;

/**
 * REST API controller for AI features in restaurant form
 * Provides endpoints for text improvement and auto-filling restaurant information
 */
@RestController
@RequestMapping("/api/restaurant/ai")
public class RestaurantAIController {
    
    private static final Logger logger = LoggerFactory.getLogger(RestaurantAIController.class);
    
    @Autowired
    private OpenAIService openAIService;
    
    @Autowired
    private SimpleUserService userService;
    
    /**
     * Improve text quality for a specific restaurant form field
     * Endpoint: POST /api/restaurant/ai/improve-text
     * Requires: ADMIN, RESTAURANT_OWNER or CUSTOMER role
     */
    @PostMapping("/improve-text")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_OWNER', 'CUSTOMER')")
    public ResponseEntity<AITextImprovementResponse> improveText(
            @Valid @RequestBody AITextImprovementRequest request,
            Authentication authentication) {
        
        try {
            logger.info("Received text improvement request for field: {}", request.getFieldName());
            
            // Validate authentication
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                    .body(AITextImprovementResponse.error("Authentication required"));
            }
            
            // Get user info
            User user = getUserFromAuthentication(authentication);
            if (user == null) {
                return ResponseEntity.status(401)
                    .body(AITextImprovementResponse.error("User not found"));
            }
            
            logger.info("Processing text improvement for user: {} (ID: {})", user.getUsername(), user.getId());
            
            // Call AI service
            CompletableFuture<String> improvedTextFuture = openAIService.improveText(
                request.getOriginalText(),
                request.getFieldName(),
                request.getContext()
            );
            
            // Wait for result (with timeout handled by service)
            String improvedText;
            try {
                improvedText = improvedTextFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error waiting for text improvement: {}", e.getMessage());
                Thread.currentThread().interrupt();
                return ResponseEntity.internalServerError()
                    .body(AITextImprovementResponse.error("Lỗi khi xử lý yêu cầu AI: " + e.getMessage()));
            }
            
            logger.info("Text improvement completed successfully");
            return ResponseEntity.ok(AITextImprovementResponse.success(improvedText));
            
        } catch (Exception e) {
            logger.error("Error improving text: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(AITextImprovementResponse.error("Lỗi khi cải thiện văn bản: " + e.getMessage()));
        }
    }
    
    /**
     * Auto-fill restaurant form fields from a long text description
     * Endpoint: POST /api/restaurant/ai/auto-fill
     * Requires: ADMIN, RESTAURANT_OWNER or CUSTOMER role
     */
    @PostMapping("/auto-fill")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_OWNER', 'CUSTOMER')")
    public ResponseEntity<AIAutoFillResponse> autoFill(
            @Valid @RequestBody AIAutoFillRequest request,
            Authentication authentication) {
        
        try {
            logger.info("Received auto-fill request");
            
            // Validate authentication
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                    .body(AIAutoFillResponse.error("Authentication required"));
            }
            
            // Get user info
            User user = getUserFromAuthentication(authentication);
            if (user == null) {
                return ResponseEntity.status(401)
                    .body(AIAutoFillResponse.error("User not found"));
            }
            
            logger.info("Processing auto-fill for user: {} (ID: {})", user.getUsername(), user.getId());
            
            // Call AI service
            CompletableFuture<Map<String, Object>> parsedInfoFuture = openAIService.parseRestaurantInfo(
                request.getLongText()
            );
            
            // Wait for result (with timeout handled by service)
            Map<String, Object> filledFields;
            try {
                filledFields = parsedInfoFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error waiting for auto-fill: {}", e.getMessage());
                Thread.currentThread().interrupt();
                return ResponseEntity.internalServerError()
                    .body(AIAutoFillResponse.error("Lỗi khi xử lý yêu cầu AI: " + e.getMessage()));
            }
            
            logger.info("Auto-fill completed successfully. Extracted {} fields", filledFields.size());
            return ResponseEntity.ok(AIAutoFillResponse.success(filledFields));
            
        } catch (Exception e) {
            logger.error("Error auto-filling restaurant info: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(AIAutoFillResponse.error("Lỗi khi tự động điền thông tin: " + e.getMessage()));
        }
    }
    
    /**
     * Helper method to get User from Authentication
     */
    private User getUserFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        
        try {
            // Try to parse as UUID first (if using UUID-based authentication)
            return userService.findById(java.util.UUID.fromString(authentication.getName()));
        } catch (IllegalArgumentException e) {
            // If not a UUID, this is username-based authentication
            return userService.findByUsername(authentication.getName()).orElse(null);
        }
    }
}

