package com.example.booking.web.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.booking.domain.User;
import com.example.booking.dto.ai.AISearchRequest;
import com.example.booking.dto.ai.AISearchResponse;
import com.example.booking.service.SimpleUserService;
import com.example.booking.service.ai.RecommendationService;

/**
 * Simplified AI Search Controller for MVP
 */
@Controller
@RequestMapping("/ai")
public class AISearchController {
    
    @Autowired
    private RecommendationService recommendationService;
    
    @Autowired
    private SimpleUserService userService;
    
    /**
     * Handle AI search requests
     */
    @PostMapping("/search")
    @ResponseBody
    public ResponseEntity<AISearchResponse> searchRestaurants(
            @RequestBody AISearchRequest request,
            Authentication authentication) {
        
        try {
            System.out.println("🔍 AI Search Request: " + request.getQuery());
            
            // Get user if authenticated
            if (authentication != null && authentication.isAuthenticated()) {
                Optional<User> userOpt = userService.findByUsername(authentication.getName());
                if (userOpt.isPresent()) {
                    request.setUserId(userOpt.get().getId().toString());
                }
            }
            
            // Use AI recommendation service
            AISearchResponse response = recommendationService.search(request);
            
            System.out.println("✅ AI Search Response: " + response.getTotalFound() + " restaurants found");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ AI Search Error: " + e.getMessage());
            e.printStackTrace();
            
            AISearchResponse errorResponse = new AISearchResponse();
            errorResponse.setOriginalQuery(request.getQuery());
            errorResponse.setTotalFound(0);
            errorResponse.setExplanation("Có lỗi xảy ra khi tìm kiếm. Vui lòng thử lại.");
            
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    /**
     * Handle restaurant-specific AI search
     */
    @PostMapping("/restaurants/search")
    @ResponseBody
    public ResponseEntity<AISearchResponse> searchRestaurantsAdvanced(
            @RequestBody AISearchRequest request,
            Authentication authentication) {
        
        // Redirect to main search
        return searchRestaurants(request, authentication);
    }
}