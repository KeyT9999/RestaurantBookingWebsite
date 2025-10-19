package com.example.booking.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO for AI search requests
 */
public class AISearchRequest {
    
    @NotBlank(message = "Query text is required")
    @Size(max = 500, message = "Query text must not exceed 500 characters")
    private String query;
    
    private String sessionId;
    
    private String userId;
    
    private String source = "web"; // web, mobile, api, widget
    
    private String language = "vi";
    
    private Integer maxResults = 5;
    
    private Boolean includeContext = true;
    
    private Boolean enableLearning = true;
    
    // Context data
    private String userLocation;
    private String userTimezone;
    private String deviceType;
    
    // User preferences override
    private List<String> preferredCuisines;
    private Integer minPrice;
    private Integer maxPrice;
    private Integer maxDistance;
    private List<String> preferredDistricts;
    
    // Constructors
    public AISearchRequest() {}
    
    public AISearchRequest(String query) {
        this.query = query;
    }
    
    // Getters and Setters
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public Integer getMaxResults() {
        return maxResults;
    }
    
    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }
    
    public Boolean getIncludeContext() {
        return includeContext;
    }
    
    public void setIncludeContext(Boolean includeContext) {
        this.includeContext = includeContext;
    }
    
    public Boolean getEnableLearning() {
        return enableLearning;
    }
    
    public void setEnableLearning(Boolean enableLearning) {
        this.enableLearning = enableLearning;
    }
    
    public String getUserLocation() {
        return userLocation;
    }
    
    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }
    
    public String getUserTimezone() {
        return userTimezone;
    }
    
    public void setUserTimezone(String userTimezone) {
        this.userTimezone = userTimezone;
    }
    
    public String getDeviceType() {
        return deviceType;
    }
    
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
    
    public List<String> getPreferredCuisines() {
        return preferredCuisines;
    }
    
    public void setPreferredCuisines(List<String> preferredCuisines) {
        this.preferredCuisines = preferredCuisines;
    }
    
    public Integer getMinPrice() {
        return minPrice;
    }
    
    public void setMinPrice(Integer minPrice) {
        this.minPrice = minPrice;
    }
    
    public Integer getMaxPrice() {
        return maxPrice;
    }
    
    public void setMaxPrice(Integer maxPrice) {
        this.maxPrice = maxPrice;
    }
    
    public Integer getMaxDistance() {
        return maxDistance;
    }
    
    public void setMaxDistance(Integer maxDistance) {
        this.maxDistance = maxDistance;
    }
    
    public List<String> getPreferredDistricts() {
        return preferredDistricts;
    }
    
    public void setPreferredDistricts(List<String> preferredDistricts) {
        this.preferredDistricts = preferredDistricts;
    }
}
