package com.example.booking.dto.admin;

import java.math.BigDecimal;

public class FavoriteStatisticsDto {
    
    private Integer restaurantId;
    private String restaurantName;
    private Long favoriteCount;
    private Double averageRating;
    private Long reviewCount;
    private BigDecimal averagePrice;
    private String cuisineType;
    
    // Constructors
    public FavoriteStatisticsDto() {}
    
    public FavoriteStatisticsDto(Integer restaurantId, String restaurantName, Long favoriteCount,
                               Double averageRating, Long reviewCount, BigDecimal averagePrice,
                               String cuisineType) {
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.favoriteCount = favoriteCount;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
        this.averagePrice = averagePrice;
        this.cuisineType = cuisineType;
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
    
    public Long getFavoriteCount() {
        return favoriteCount;
    }
    
    public void setFavoriteCount(Long favoriteCount) {
        this.favoriteCount = favoriteCount;
    }
    
    public Double getAverageRating() {
        return averageRating;
    }
    
    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
    
    public Long getReviewCount() {
        return reviewCount;
    }
    
    public void setReviewCount(Long reviewCount) {
        this.reviewCount = reviewCount;
    }
    
    public BigDecimal getAveragePrice() {
        return averagePrice;
    }
    
    public void setAveragePrice(BigDecimal averagePrice) {
        this.averagePrice = averagePrice;
    }
    
    public String getCuisineType() {
        return cuisineType;
    }
    
    public void setCuisineType(String cuisineType) {
        this.cuisineType = cuisineType;
    }
}
