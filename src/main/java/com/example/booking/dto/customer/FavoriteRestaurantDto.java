package com.example.booking.dto.customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public class FavoriteRestaurantDto {
    
    private Integer restaurantId;
    private String restaurantName;
    private String address;
    private String phone;
    private String description;
    private String cuisineType;
    private String openingHours;
    private BigDecimal averagePrice;
    private String websiteUrl;
    private LocalDateTime favoritedAt;
    private Double averageRating;
    private Integer reviewCount;
    private String imageUrl;
    private boolean isFavorited;
    
    // Constructors
    public FavoriteRestaurantDto() {}
    
    public FavoriteRestaurantDto(Integer restaurantId, String restaurantName, String address, 
                               String phone, String description, String cuisineType, 
                               String openingHours, BigDecimal averagePrice, String websiteUrl,
                               LocalDateTime favoritedAt, Double averageRating, Integer reviewCount,
                               String imageUrl, boolean isFavorited) {
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.address = address;
        this.phone = phone;
        this.description = description;
        this.cuisineType = cuisineType;
        this.openingHours = openingHours;
        this.averagePrice = averagePrice;
        this.websiteUrl = websiteUrl;
        this.favoritedAt = favoritedAt;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
        this.imageUrl = imageUrl;
        this.isFavorited = isFavorited;
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
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCuisineType() {
        return cuisineType;
    }
    
    public void setCuisineType(String cuisineType) {
        this.cuisineType = cuisineType;
    }
    
    public String getOpeningHours() {
        return openingHours;
    }
    
    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }
    
    public BigDecimal getAveragePrice() {
        return averagePrice;
    }
    
    public void setAveragePrice(BigDecimal averagePrice) {
        this.averagePrice = averagePrice;
    }
    
    public String getWebsiteUrl() {
        return websiteUrl;
    }
    
    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }
    
    public LocalDateTime getFavoritedAt() {
        return favoritedAt;
    }
    
    public void setFavoritedAt(LocalDateTime favoritedAt) {
        this.favoritedAt = favoritedAt;
    }
    
    public Double getAverageRating() {
        return averageRating;
    }
    
    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
    
    public Integer getReviewCount() {
        return reviewCount;
    }
    
    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public boolean isFavorited() {
        return isFavorited;
    }
    
    public void setFavorited(boolean isFavorited) {
        this.isFavorited = isFavorited;
    }
}
