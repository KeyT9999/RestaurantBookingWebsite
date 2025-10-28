package com.example.booking.service.ai;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private static final Set<String> DEFAULT_STOP_WORDS = Set.of(
        "toi", "minh", "ban", "chungtoi",
        "muon", "an", "uong", "can",
        "o", "tai", "quan", "phuong", "thanh", "thanhpho",
        "mon", "gi", "hom", "nay", "ngay", "cho", "nguoi",
        "gia", "tam", "khoang", "dat", "nhahang", "nha", "hang"
    );
    
    /**
     * Main search method - simplified version
     */
    public AISearchResponse search(AISearchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Search request is required");
        }

        String query = request.getQuery();
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query text is required");
        }
        query = query.trim();

        request.setQuery(query);

        try {
            System.out.println("🔍 AI Search started for query: " + query);
            
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
    private Map<String, Object> parseIntentWithTimeout(AISearchRequest request) throws Exception {
        java.util.concurrent.CompletableFuture<Map<String, Object>> intentFuture =
            openAIService.parseIntent(request.getQuery(), request.getUserId());

        if (intentFuture == null) {
            throw new IllegalStateException("Intent parsing service unavailable");
        }

        return intentFuture.get(800, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
    
    /**
     * Find restaurant candidates based on intent
     */
    private List<RestaurantProfile> findCandidates(Map<String, Object> intent, AISearchRequest request) {
        System.out.println("🔍 Finding restaurant candidates...");
        List<RestaurantProfile> allRestaurants = restaurantService.findAllRestaurants();
        if (allRestaurants == null) {
            allRestaurants = List.of();
        }
        System.out.println("📊 Total restaurants in DB: " + allRestaurants.size());
        
        List<RestaurantProfile> filtered = allRestaurants.stream()
            .filter(restaurant -> matchesCuisine(restaurant, intent))
            .filter(restaurant -> matchesPriceRange(restaurant, intent))
            .filter(restaurant -> matchesDietary(restaurant, intent))
            .collect(Collectors.toList());

        List<RestaurantProfile> queryFiltered = applyQueryFiltering(filtered, request);
            
        System.out.println("✅ Filtered restaurants: " + queryFiltered.size());
        return queryFiltered;
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
        if (restaurants == null) {
            restaurants = List.of();
        }

        restaurants = applyQueryFiltering(restaurants, request);
        int limit = request.getMaxResults() != null ? request.getMaxResults() : 5;
        
        List<AISearchResponse.RestaurantRecommendation> recommendations = restaurants.stream()
            .limit(limit)
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
        
        String restaurantCuisine = normalize(restaurant.getCuisineType());
        if (restaurantCuisine.isEmpty()) {
            return false;
        }
        
        return cuisines.stream()
            .map(this::normalize)
            .anyMatch(restaurantCuisine::contains);
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

    private List<RestaurantProfile> applyQueryFiltering(List<RestaurantProfile> restaurants, AISearchRequest request) {
        if (restaurants == null || restaurants.isEmpty()) {
            return List.of();
        }

        String query = request.getQuery();
        if (query == null || query.isBlank()) {
            return restaurants;
        }

        String normalizedQuery = normalize(query);
        List<String> keywords = extractKeywords(normalizedQuery);

        return restaurants.stream()
            .filter(restaurant -> matchesQueryText(restaurant, normalizedQuery, keywords))
            .collect(Collectors.toList());
    }

    private boolean matchesQueryText(RestaurantProfile restaurant, String normalizedQuery, List<String> keywords) {
        if (normalizedQuery == null || normalizedQuery.isBlank()) {
            return true;
        }

        String normalizedName = normalize(restaurant.getRestaurantName());
        String normalizedCuisine = normalize(restaurant.getCuisineType());

        if (!normalizedName.isEmpty() && normalizedName.contains(normalizedQuery)) {
            return true;
        }

        if (!normalizedCuisine.isEmpty() && normalizedCuisine.contains(normalizedQuery)) {
            return true;
        }

        if (keywords.isEmpty()) {
            return true;
        }

        for (String keyword : keywords) {
            if (!normalizedName.isEmpty() && normalizedName.contains(keyword)) {
                return true;
            }
            if (!normalizedCuisine.isEmpty() && normalizedCuisine.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    private List<String> extractKeywords(String normalizedQuery) {
        if (normalizedQuery == null || normalizedQuery.isBlank()) {
            return List.of();
        }

        return Arrays.stream(normalizedQuery.split("\\s+"))
            .map(String::trim)
            .filter(token -> token.length() > 1)
            .filter(token -> !DEFAULT_STOP_WORDS.contains(token))
            .distinct()
            .collect(Collectors.toList());
    }

    private String normalize(String input) {
        if (input == null) {
            return "";
        }
        String lower = input.toLowerCase();
        String normalized = Normalizer.normalize(lower, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized.trim();
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
        rec.setBookingUrl("/booking/new?restaurantId=" + restaurant.getId());
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
        rec.setBookingUrl("/booking/new?restaurantId=" + restaurant.getId());
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
