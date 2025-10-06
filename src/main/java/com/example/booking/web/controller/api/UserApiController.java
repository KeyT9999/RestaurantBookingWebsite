package com.example.booking.web.controller.api;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.booking.domain.User;

/**
 * API controller for user information
 */
@RestController
@RequestMapping("/api/user")
public class UserApiController {
    
    /**
     * Get current user information
     */
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.badRequest().body("User not authenticated");
        }
        
        User user = (User) authentication.getPrincipal();
        
        return ResponseEntity.ok(new UserInfoResponse(
            user.getId(),
            user.getUsername(),
            user.getFullName(),
            user.getRole().getValue(),
            user.getEmail()
        ));
    }
    
    // Response DTO
    public static class UserInfoResponse {
        private UUID id;
        private String username;
        private String fullName;
        private String role;
        private String email;
        
        public UserInfoResponse(UUID id, String username, String fullName, String role, String email) {
            this.id = id;
            this.username = username;
            this.fullName = fullName;
            this.role = role;
            this.email = email;
        }
        
        // Getters
        public UUID getId() { return id; }
        public String getUsername() { return username; }
        public String getFullName() { return fullName; }
        public String getRole() { return role; }
        public String getEmail() { return email; }
    }
}
