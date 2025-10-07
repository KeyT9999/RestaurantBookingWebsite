package com.example.booking.web.controller.api;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.booking.domain.User;
import com.example.booking.service.SimpleUserService;

/**
 * API controller for user information
 */
@RestController
@RequestMapping("/api/user")
public class UserApiController {
    
    @Autowired
    private SimpleUserService userService;

    /**
     * Get current user information
     */
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.badRequest().body("User not authenticated");
        }
        
        User user = getUserFromAuthentication(authentication);
        
        return ResponseEntity.ok(new UserInfoResponse(
            user.getId(),
            user.getUsername(),
            user.getFullName(),
            user.getRole().getValue(),
            user.getEmail()
        ));
    }
    
    /**
     * Helper method to get User from Authentication (handles both User and
     * OAuth2User)
     */
    private User getUserFromAuthentication(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        // Nếu là User object trực tiếp (regular login)
        if (principal instanceof User) {
            return (User) principal;
        }

        // Nếu là OAuth2User hoặc OidcUser (OAuth2 login)
        if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            String username = authentication.getName(); // username = email cho OAuth users

            // Tìm User thực tế từ database
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
