package com.example.booking.service.ai;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.booking.domain.RestaurantProfile;
import com.example.booking.dto.ai.AISearchRequest;
import com.example.booking.dto.ai.AISearchResponse;
import com.example.booking.service.RestaurantManagementService;

/**
 * Simplified Recommendation Service for MVP
 */
@Service
public class RecommendationService {
    
    @Autowired
    private OpenAIService openAIService;
    
    @Autowired
    private RestaurantManagementService restaurantService;
    
    /**
     * Main search method - simplified version
     */
    public AISearchResponse search(AISearchRequest request) {
        try {
            System.out.println("🔍 AI Search started for query: " + request.getQuery());
            
            // 1. Parse intent with timeout
            Map<String, Object> intent = parseIntentWithTimeout(request);
            System.out.println("📊 Parsed intent: " + intent);
            
            // 2. Find candidates based on intent
            List<RestaurantProfile> candidates = findCandidates(intent, request);
            System.out.println("🏪 Found " + candidates.size() + " candidate restaurants");
            
            // 3. Take top results
            List<RestaurantProfile> topResults = candidates.stream()
                .limit(request.getMaxResults() != null ? request.getMaxResults() : 5)
                .collect(Collectors.toList());
            
            System.out.println("🎯 Selected " + topResults.size() + " top results");
            
            // 4. Generate recommendations
            List<AISearchResponse.RestaurantRecommendation> recommendations = 
                generateRecommendations(topResults, intent);
            System.out.println("✨ Generated " + recommendations.size() + " recommendations");
            
            // 5. Build response
            AISearchResponse response = buildResponse(request, recommendations, intent);
            System.out.println("📤 Built response with totalFound: " + response.getTotalFound());
            
            return response;
            
        } catch (Exception e) {
            System.err.println("❌ AI Search Error: " + e.getMessage());
            e.printStackTrace();
            // Fallback to simple search
            return fallbackSearch(request);
        }
    }
    
    /**
     * Parse intent with timeout
     */
    private Map<String, Object> parseIntentWithTimeout(AISearchRequest request) {
        try {
            return openAIService.parseIntent(request.getQuery(), request.getUserId())
                .get(800, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            // Simple fallback
            Map<String, Object> fallback = new java.util.HashMap<>();
            fallback.put("cuisine", List.of());
            fallback.put("party_size", 2);
            fallback.put("price_range", Map.of("min", 100000, "max", 500000));
            fallback.put("distance", 5.0);
            fallback.put("dietary", List.of());
            fallback.put("confidence", 0.5);
            return fallback;
        }
    }
    
    /**
     * Find restaurant candidates based on intent
     */
    private List<RestaurantProfile> findCandidates(Map<String, Object> intent, AISearchRequest request) {
        System.out.println("🔍 Finding restaurant candidates...");
        List<RestaurantProfile> allRestaurants = restaurantService.findAllRestaurants();
        System.out.println("📊 Total restaurants in DB: " + allRestaurants.size());
        
        List<RestaurantProfile> filtered = allRestaurants.stream()
            .filter(restaurant -> matchesCuisine(restaurant, intent))
            .filter(restaurant -> matchesPriceRange(restaurant, intent))
            .filter(restaurant -> matchesDietary(restaurant, intent))
            .collect(Collectors.toList());
            
        System.out.println("✅ Filtered restaurants: " + filtered.size());
        return filtered;
    }
    
    /**
     * Generate recommendations with explanations
     */
    private List<AISearchResponse.RestaurantRecommendation> generateRecommendations(
            List<RestaurantProfile> restaurants, Map<String, Object> intent) {
        
        try {
            List<String> restaurantNames = restaurants.stream()
                .map(RestaurantProfile::getRestaurantName)
                .collect(Collectors.toList());
            
            List<String> explanations = openAIService.explainRestaurants(restaurantNames)
                .get(800, java.util.concurrent.TimeUnit.MILLISECONDS);
            
            return restaurants.stream()
                .map(restaurant -> createRecommendation(restaurant, explanations))
                .collect(Collectors.toList());
            
        } catch (Exception e) {
            // Fallback to simple recommendations
            return restaurants.stream()
                .map(this::createSimpleRecommendation)
                .collect(Collectors.toList());
        }
    }
    
    /**
     * Fallback search when AI fails
     */
    private AISearchResponse fallbackSearch(AISearchRequest request) {
        List<RestaurantProfile> restaurants = restaurantService.findAllRestaurants();
        
        List<AISearchResponse.RestaurantRecommendation> recommendations = restaurants.stream()
            .limit(5)
            .map(this::createSimpleRecommendation)
            .collect(Collectors.toList());
        
        AISearchResponse response = new AISearchResponse();
        response.setOriginalQuery(request.getQuery());
        response.setTotalFound(restaurants.size());
        response.setTotalReturned(recommendations.size());
        response.setExplanation("Tìm thấy nhà hàng phù hợp (chế độ đơn giản)");
        response.setRecommendations(recommendations);
        
        return response;
    }
    
    // Helper methods
    private boolean matchesCuisine(RestaurantProfile restaurant, Map<String, Object> intent) {
        @SuppressWarnings("unchecked")
        List<String> cuisines = (List<String>) intent.get("cuisine");
        if (cuisines == null || cuisines.isEmpty()) return true;
        
        return cuisines.stream().anyMatch(cuisine -> 
            restaurant.getCuisineType() != null && 
            restaurant.getCuisineType().toLowerCase().contains(cuisine.toLowerCase()));
    }
    
    private boolean matchesPriceRange(RestaurantProfile restaurant, Map<String, Object> intent) {
        @SuppressWarnings("unchecked")
        Map<String, Object> priceRange = (Map<String, Object>) intent.get("price_range");
        if (priceRange == null || restaurant.getAveragePrice() == null) return true;
        
        int minPrice = (Integer) priceRange.get("min");
        int maxPrice = (Integer) priceRange.get("max");
        int price = restaurant.getAveragePrice().intValue();
        
        return price >= minPrice && price <= maxPrice;
    }
    
    private boolean matchesDietary(RestaurantProfile restaurant, Map<String, Object> intent) {
        // Simple dietary matching - can be enhanced later
        return true;
    }
    
    private AISearchResponse.RestaurantRecommendation createRecommendation(
            RestaurantProfile restaurant, List<String> explanations) {
        AISearchResponse.RestaurantRecommendation rec = new AISearchResponse.RestaurantRecommendation();
        rec.setRestaurantId(restaurant.getId().toString());
        rec.setRestaurantName(restaurant.getRestaurantName());
        rec.setCuisineType(restaurant.getCuisineType());
        rec.setPriceRange(restaurant.getAveragePrice() != null ? 
            restaurant.getAveragePrice().toString() : "Liên hệ");
        rec.setImageUrl(restaurant.getMainImageUrl());
        rec.setRating(restaurant.getFormattedAverageRating());
        rec.setDistanceKm(0.0);
        rec.setBookingUrl("/restaurants/" + restaurant.getId() + "/booking");
        rec.setViewDetailsUrl("/restaurants/" + restaurant.getId());
        return rec;
    }
    
    private AISearchResponse.RestaurantRecommendation createSimpleRecommendation(RestaurantProfile restaurant) {
        AISearchResponse.RestaurantRecommendation rec = new AISearchResponse.RestaurantRecommendation();
        rec.setRestaurantId(restaurant.getId().toString());
        rec.setRestaurantName(restaurant.getRestaurantName());
        rec.setCuisineType(restaurant.getCuisineType());
        rec.setPriceRange(restaurant.getAveragePrice() != null ? 
            restaurant.getAveragePrice().toString() : "Liên hệ");
        rec.setImageUrl(restaurant.getMainImageUrl());
        rec.setRating(restaurant.getFormattedAverageRating());
        rec.setDistanceKm(0.0);
        rec.setBookingUrl("/restaurants/" + restaurant.getId() + "/booking");
        rec.setViewDetailsUrl("/restaurants/" + restaurant.getId());
        return rec;
    }
    
    private AISearchResponse buildResponse(AISearchRequest request, 
                                         List<AISearchResponse.RestaurantRecommendation> recommendations,
                                         Map<String, Object> intent) {
        AISearchResponse response = new AISearchResponse();
        response.setOriginalQuery(request.getQuery());
        response.setTotalFound(recommendations.size());
        response.setTotalReturned(recommendations.size());
        response.setExplanation("Tìm thấy " + recommendations.size() + " nhà hàng phù hợp");
        response.setRecommendations(recommendations);
        
        // Set confidence from intent
        Object confidence = intent.get("confidence");
        if (confidence instanceof Number) {
            response.setConfidenceScore(java.math.BigDecimal.valueOf(((Number) confidence).doubleValue()));
        }
        
        return response;
    }
}