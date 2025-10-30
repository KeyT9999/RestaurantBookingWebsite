package com.example.booking.web.controller.api;

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
import com.example.booking.dto.AIActionRequest;
import com.example.booking.dto.AIActionResponse;
import com.example.booking.service.AIIntentDispatcherService;
import com.example.booking.service.SimpleUserService;

/**
 * REST API controller for AI actions
 * This endpoint allows AI service to execute business operations under authenticated user session
 */
@RestController
@RequestMapping("/api/ai")
public class AIActionsController {
    
    private static final Logger logger = LoggerFactory.getLogger(AIActionsController.class);
    
    @Autowired
    private AIIntentDispatcherService intentDispatcherService;
    
    @Autowired
    private SimpleUserService userService;
    
    /**
     * Execute AI action based on intent and data
     * This endpoint is secured and requires CUSTOMER role
     */
    @PostMapping("/actions")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<AIActionResponse> executeAIAction(@RequestBody AIActionRequest request, 
                                                          Authentication authentication) {
        try {
            logger.info("Received AI action request: {}", request);
            
            // Validate request
            if (request.getIntent() == null || request.getIntent().trim().isEmpty()) {
                logger.warn("Invalid request: missing intent");
                return ResponseEntity.badRequest()
                    .body(AIActionResponse.error("Intent is required", "MISSING_INTENT"));
            }
            
            if (request.getData() == null) {
                logger.warn("Invalid request: missing data");
                return ResponseEntity.badRequest()
                    .body(AIActionResponse.error("Data is required", "MISSING_DATA"));
            }
            
            // Get current authenticated user
            User user = getUserFromAuthentication(authentication);
            logger.info("Processing AI action for user: {} (ID: {})", user.getUsername(), user.getId());
            
            // Dispatch intent to appropriate service
            AIActionResponse response = intentDispatcherService.dispatchIntent(
                request.getIntent(), 
                request.getData(), 
                user
            );
            
            logger.info("AI action completed. Success: {}, Message: {}", 
                response.isSuccess(), response.getMessage());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error executing AI action: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(AIActionResponse.error("Internal server error: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }
    
    /**
     * Helper method to get User from Authentication (handles both User and OAuth2User)
     */
    private User getUserFromAuthentication(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        
        // If it's User object directly (regular login)
        if (principal instanceof User) {
            return (User) principal;
        }
        
        // If it's OAuth2User or OidcUser (OAuth2 login)
        if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            String username = authentication.getName(); // username = email for OAuth users
            
            // Find actual User from database
            try {
                User user = (User) userService.loadUserByUsername(username);
                return user;
            } catch (Exception e) {
                throw new RuntimeException("User not found for OAuth username: " + username +
                        ". Error: " + e.getMessage());
            }
        }
        
        throw new RuntimeException("Unsupported principal type: " + principal.getClass().getName());
    }
}
