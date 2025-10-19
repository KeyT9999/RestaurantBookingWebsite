package com.example.booking.dto.ai;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for individual restaurant recommendations
 */
public class RestaurantRecommendation {
    
    private Integer restaurantId;
    private String restaurantName;
    private String restaurantAddress;
    private String restaurantPhone;
    
    // Basic info
    private String cuisineType;
    private String description;
    private String imageUrl;
    private String websiteUrl;
    
    // Ratings and reviews
    private BigDecimal rating;
    private Integer reviewCount;
    private String recentReview;
    
    // Pricing
    private String priceRange;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String priceLevel; // $, $$, $$$, $$$$
    
    // Location
    private Double distanceKm;
    private String district;
    private String city;
    
    // Availability
    private Boolean isAvailable;
    private String nextAvailableTime;
    private Integer availableTables;
    private String availabilityStatus;
    
    // AI-specific data
    private BigDecimal confidenceScore;
    private String aiExplanation;
    private List<String> matchingFactors;
    private List<String> whyRecommended;
    
    // Actions
    private String bookingUrl;
    private String viewDetailsUrl;
    private String saveToFavoritesUrl;
    private String shareUrl;
    
    // Special features
    private List<String> specialFeatures; // ["parking", "wifi", "wheelchair_accessible"]
    private List<String> ambiance; // ["romantic", "casual", "family-friendly"]
    private String openingHours;
    private String lastOrderTime;
    
    // Constructors
    public RestaurantRecommendation() {}
    
    public RestaurantRecommendation(Integer restaurantId, String restaurantName) {
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
    }
    
    // Getters and Setters
    public Integer getRestaurantId() {
        return restaurantId;
    }
    
    public void setRestaurantId(Integer restaurantId) {
        this.restaurantId = restaurantId;
    }
    
    public String getRestaurantName() {
        return restaurantName;
    }
    
    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }
    
    public String getRestaurantAddress() {
        return restaurantAddress;
    }
    
    public void setRestaurantAddress(String restaurantAddress) {
        this.restaurantAddress = restaurantAddress;
    }
    
    public String getRestaurantPhone() {
        return restaurantPhone;
    }
    
    public void setRestaurantPhone(String restaurantPhone) {
        this.restaurantPhone = restaurantPhone;
    }
    
    public String getCuisineType() {
        return cuisineType;
    }
    
    public void setCuisineType(String cuisineType) {
        this.cuisineType = cuisineType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getWebsiteUrl() {
        return websiteUrl;
    }
    
    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }
    
    public BigDecimal getRating() {
        return rating;
    }
    
    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }
    
    public Integer getReviewCount() {
        return reviewCount;
    }
    
    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }
    
    public String getRecentReview() {
        return recentReview;
    }
    
    public void setRecentReview(String recentReview) {
        this.recentReview = recentReview;
    }
    
    public String getPriceRange() {
        return priceRange;
    }
    
    public void setPriceRange(String priceRange) {
        this.priceRange = priceRange;
    }
    
    public BigDecimal getMinPrice() {
        return minPrice;
    }
    
    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }
    
    public BigDecimal getMaxPrice() {
        return maxPrice;
    }
    
    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }
    
    public String getPriceLevel() {
        return priceLevel;
    }
    
    public void setPriceLevel(String priceLevel) {
        this.priceLevel = priceLevel;
    }
    
    public Double getDistanceKm() {
        return distanceKm;
    }
    
    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }
    
    public String getDistrict() {
        return district;
    }
    
    public void setDistrict(String district) {
        this.district = district;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public Boolean getIsAvailable() {
        return isAvailable;
    }
    
    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }
    
    public String getNextAvailableTime() {
        return nextAvailableTime;
    }
    
    public void setNextAvailableTime(String nextAvailableTime) {
        this.nextAvailableTime = nextAvailableTime;
    }
    
    public Integer getAvailableTables() {
        return availableTables;
    }
    
    public void setAvailableTables(Integer availableTables) {
        this.availableTables = availableTables;
    }
    
    public String getAvailabilityStatus() {
        return availabilityStatus;
    }
    
    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }
    
    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }
    
    public void setConfidenceScore(BigDecimal confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
    
    public String getAiExplanation() {
        return aiExplanation;
    }
    
    public void setAiExplanation(String aiExplanation) {
        this.aiExplanation = aiExplanation;
    }
    
    public List<String> getMatchingFactors() {
        return matchingFactors;
    }
    
    public void setMatchingFactors(List<String> matchingFactors) {
        this.matchingFactors = matchingFactors;
    }
    
    public List<String> getWhyRecommended() {
        return whyRecommended;
    }
    
    public void setWhyRecommended(List<String> whyRecommended) {
        this.whyRecommended = whyRecommended;
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
    
    public String getSaveToFavoritesUrl() {
        return saveToFavoritesUrl;
    }
    
    public void setSaveToFavoritesUrl(String saveToFavoritesUrl) {
        this.saveToFavoritesUrl = saveToFavoritesUrl;
    }
    
    public String getShareUrl() {
        return shareUrl;
    }
    
    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }
    
    public List<String> getSpecialFeatures() {
        return specialFeatures;
    }
    
    public void setSpecialFeatures(List<String> specialFeatures) {
        this.specialFeatures = specialFeatures;
    }
    
    public List<String> getAmbiance() {
        return ambiance;
    }
    
    public void setAmbiance(List<String> ambiance) {
        this.ambiance = ambiance;
    }
    
    public String getOpeningHours() {
        return openingHours;
    }
    
    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }
    
    public String getLastOrderTime() {
        return lastOrderTime;
    }
    
    public void setLastOrderTime(String lastOrderTime) {
        this.lastOrderTime = lastOrderTime;
    }
    
    // Helper methods
    public boolean isHighConfidence() {
        return confidenceScore != null && confidenceScore.compareTo(new BigDecimal("0.8")) >= 0;
    }
    
    public boolean isNearby() {
        return distanceKm != null && distanceKm <= 2.0;
    }
    
    public boolean isAffordable() {
        return maxPrice != null && maxPrice.compareTo(new BigDecimal("300000")) <= 0;
    }
    
    public boolean isHighlyRated() {
        return rating != null && rating.compareTo(new BigDecimal("4.5")) >= 0;
    }
    
    public String getFormattedDistance() {
        if (distanceKm == null) return "Unknown";
        if (distanceKm < 1.0) {
            return String.format("%.0fm", distanceKm * 1000);
        }
        return String.format("%.1fkm", distanceKm);
    }
    
    public String getFormattedPrice() {
        if (minPrice == null || maxPrice == null) return priceRange;
        return String.format("%.0fk - %.0fk VNĐ", 
                           minPrice.divide(new BigDecimal("1000")), 
                           maxPrice.divide(new BigDecimal("1000")));
    }
}
