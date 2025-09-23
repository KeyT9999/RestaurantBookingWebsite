package com.example.booking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.example.booking.domain.User;

@Service
public class OAuth2UserService extends DefaultOAuth2UserService {
    
    private final SimpleUserService userService;
    
    @Autowired
    public OAuth2UserService(SimpleUserService userService) {
        this.userService = userService;
    }
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        // Debug attributes once to aid troubleshooting
        try { System.out.println("🔎 OAuth2 attrs: " + oAuth2User.getAttributes()); } catch (Exception ignore) {}
        
        try {
            return processOAuth2User(oAuth2User);
        } catch (Exception e) {
            System.err.println("❌ Error processing OAuth2 user: " + e.getMessage());
            e.printStackTrace();
            throw new OAuth2AuthenticationException("Error processing OAuth2 user: " + e.getMessage());
        }
    }
    
    private OAuth2User processOAuth2User(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String googleId = oAuth2User.getAttribute("sub");
        
        if (email == null || email.isEmpty()) {
            throw new OAuth2AuthenticationException("Email not found from Google");
        }
        
        // Upsert Google user to database
        User user = userService.upsertGoogleUser(googleId, email, name);
        
        // Log Google user info với authorities từ User entity
        System.out.println("✅ Google OAuth user processed:");
        System.out.println("   Email: " + email);
        System.out.println("   Name: " + name);
        System.out.println("   Google ID: " + googleId);
        System.out.println("   Database User ID: " + user.getId());
        System.out.println("   User Role: " + user.getRole());
        System.out.println("   Email Verified: " + user.getEmailVerified());
        System.out.println("   User Authorities: " + user.getAuthorities());
        
        // Sử dụng authorities từ User entity (đã có ROLE_ prefix)
        // Return OAuth2User với authorities từ User entity trong DB
        return new DefaultOAuth2User(
            user.getAuthorities(), // Sử dụng authorities từ User entity
            oAuth2User.getAttributes(),
            "email" // Sử dụng email làm keyName
        );
    }
    

}