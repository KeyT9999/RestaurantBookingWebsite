package com.example.booking.dto.ai;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for AI search responses
 */
public class AISearchResponse {
    
    private String requestId;
    private String sessionId;
    
    // Query analysis
    private String originalQuery;
    private String processedQuery;
    private String intent;
    private List<String> extractedKeywords;
    private String language;
    
    // Recommendations
    private List<RestaurantRecommendation> recommendations;
    private Integer totalFound;
    private Integer totalReturned;
    
    // Quality metrics
    private BigDecimal confidenceScore;
    private BigDecimal diversityScore;
    private String explanation;
    
    // Performance metrics
    private Integer responseTimeMs;
    private String modelUsed;
    private Integer tokensUsed;
    private BigDecimal costUsd;
    
    // Context
    private String contextUsed;
    private String suggestions;
    
    // AI Interpretation and Food Suggestions
    private String aiInterpretation; // AI's explanation/recommendation (e.g., "Bạn đang tập gym nên ăn ức gà")
    private List<String> suggestedFoods; // List of suggested food items
    private String searchStrategy; // How AI searched: "cuisine", "dish", "mixed"
    
    // User feedback
    private Boolean feedbackEnabled;
    private String feedbackUrl;
    
    // Constructors
    public AISearchResponse() {}
    
    public AISearchResponse(String requestId, String sessionId) {
        this.requestId = requestId;
        this.sessionId = sessionId;
    }
    
    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getOriginalQuery() {
        return originalQuery;
    }
    
    public void setOriginalQuery(String originalQuery) {
        this.originalQuery = originalQuery;
    }
    
    public String getProcessedQuery() {
        return processedQuery;
    }
    
    public void setProcessedQuery(String processedQuery) {
        this.processedQuery = processedQuery;
    }
    
    public String getIntent() {
        return intent;
    }
    
    public void setIntent(String intent) {
        this.intent = intent;
    }
    
    public List<String> getExtractedKeywords() {
        return extractedKeywords;
    }
    
    public void setExtractedKeywords(List<String> extractedKeywords) {
        this.extractedKeywords = extractedKeywords;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public List<RestaurantRecommendation> getRecommendations() {
        return recommendations;
    }
    
    public void setRecommendations(List<RestaurantRecommendation> recommendations) {
        this.recommendations = recommendations;
    }
    
    public Integer getTotalFound() {
        return totalFound;
    }
    
    public void setTotalFound(Integer totalFound) {
        this.totalFound = totalFound;
    }
    
    public Integer getTotalReturned() {
        return totalReturned;
    }
    
    public void setTotalReturned(Integer totalReturned) {
        this.totalReturned = totalReturned;
    }
    
    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }
    
    public void setConfidenceScore(BigDecimal confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
    
    public BigDecimal getDiversityScore() {
        return diversityScore;
    }
    
    public void setDiversityScore(BigDecimal diversityScore) {
        this.diversityScore = diversityScore;
    }
    
    public String getExplanation() {
        return explanation;
    }
    
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
    
    public Integer getResponseTimeMs() {
        return responseTimeMs;
    }
    
    public void setResponseTimeMs(Integer responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }
    
    public String getModelUsed() {
        return modelUsed;
    }
    
    public void setModelUsed(String modelUsed) {
        this.modelUsed = modelUsed;
    }
    
    public Integer getTokensUsed() {
        return tokensUsed;
    }
    
    public void setTokensUsed(Integer tokensUsed) {
        this.tokensUsed = tokensUsed;
    }
    
    public BigDecimal getCostUsd() {
        return costUsd;
    }
    
    public void setCostUsd(BigDecimal costUsd) {
        this.costUsd = costUsd;
    }
    
    public String getContextUsed() {
        return contextUsed;
    }
    
    public void setContextUsed(String contextUsed) {
        this.contextUsed = contextUsed;
    }
    
    public String getSuggestions() {
        return suggestions;
    }
    
    public void setSuggestions(String suggestions) {
        this.suggestions = suggestions;
    }
    
    public String getAiInterpretation() {
        return aiInterpretation;
    }
    
    public void setAiInterpretation(String aiInterpretation) {
        this.aiInterpretation = aiInterpretation;
    }
    
    public List<String> getSuggestedFoods() {
        return suggestedFoods;
    }
    
    public void setSuggestedFoods(List<String> suggestedFoods) {
        this.suggestedFoods = suggestedFoods;
    }
    
    public String getSearchStrategy() {
        return searchStrategy;
    }
    
    public void setSearchStrategy(String searchStrategy) {
        this.searchStrategy = searchStrategy;
    }
    
    public Boolean getFeedbackEnabled() {
        return feedbackEnabled;
    }
    
    public void setFeedbackEnabled(Boolean feedbackEnabled) {
        this.feedbackEnabled = feedbackEnabled;
    }
    
    public String getFeedbackUrl() {
        return feedbackUrl;
    }
    
    public void setFeedbackUrl(String feedbackUrl) {
        this.feedbackUrl = feedbackUrl;
    }
    
    // Helper methods
    public boolean isHighConfidence() {
        return confidenceScore != null && confidenceScore.compareTo(new BigDecimal("0.8")) >= 0;
    }
    
    public boolean isDiverse() {
        return diversityScore != null && diversityScore.compareTo(new BigDecimal("0.7")) >= 0;
    }
    
    public boolean hasRecommendations() {
        return recommendations != null && !recommendations.isEmpty();
    }
    
    /**
     * Inner class for restaurant recommendations
     */
    public static class RestaurantRecommendation {
        private String restaurantId;
        private String restaurantName;
        private String cuisineType;
        private String priceRange;
        private String imageUrl;
        private String rating;
        private Double distanceKm;
        private String bookingUrl;
        private String viewDetailsUrl;
        
        // Constructors
        public RestaurantRecommendation() {}
        
        // Getters and Setters
        public String getRestaurantId() {
            return restaurantId;
        }
        
        public void setRestaurantId(String restaurantId) {
            this.restaurantId = restaurantId;
        }
        
        public String getRestaurantName() {
            return restaurantName;
        }
        
        public void setRestaurantName(String restaurantName) {
            this.restaurantName = restaurantName;
        }
        
        public String getCuisineType() {
            return cuisineType;
        }
        
        public void setCuisineType(String cuisineType) {
            this.cuisineType = cuisineType;
        }
        
        public String getPriceRange() {
            return priceRange;
        }
        
        public void setPriceRange(String priceRange) {
            this.priceRange = priceRange;
        }
        
        public String getImageUrl() {
            return imageUrl;
        }
        
        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
        
        public String getRating() {
            return rating;
        }
        
        public void setRating(String rating) {
            this.rating = rating;
        }
        
        public Double getDistanceKm() {
            return distanceKm;
        }
        
        public void setDistanceKm(Double distanceKm) {
            this.distanceKm = distanceKm;
        }
        
        public String getBookingUrl() {
            return bookingUrl;
        }
        
        public void setBookingUrl(String bookingUrl) {
            this.bookingUrl = bookingUrl;
        }
        
        public String getViewDetailsUrl() {
            return viewDetailsUrl;
        }
        
        public void setViewDetailsUrl(String viewDetailsUrl) {
            this.viewDetailsUrl = viewDetailsUrl;
        }
    }
}
