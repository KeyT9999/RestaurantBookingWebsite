package com.example.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lightweight DTO for nearby restaurant recommendations with approximate distance.
 */
public class NearbyRestaurantDto {
    private Integer restaurantId;
    private String name;
    private String address;
    private String cuisineType;
    private BigDecimal averagePrice;
    private String mainImageUrl;
    private Double distanceKm;
    private LocalDateTime createdAt;

    public NearbyRestaurantDto() {}

    public NearbyRestaurantDto(Integer restaurantId, String name, String address, String cuisineType,
                               BigDecimal averagePrice, String mainImageUrl, Double distanceKm,
                               LocalDateTime createdAt) {
        this.restaurantId = restaurantId;
        this.name = name;
        this.address = address;
        this.cuisineType = cuisineType;
        this.averagePrice = averagePrice;
        this.mainImageUrl = mainImageUrl;
        this.distanceKm = distanceKm;
        this.createdAt = createdAt;
    }

    public Integer getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Integer restaurantId) { this.restaurantId = restaurantId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCuisineType() { return cuisineType; }
    public void setCuisineType(String cuisineType) { this.cuisineType = cuisineType; }
    public BigDecimal getAveragePrice() { return averagePrice; }
    public void setAveragePrice(BigDecimal averagePrice) { this.averagePrice = averagePrice; }
    public String getMainImageUrl() { return mainImageUrl; }
    public void setMainImageUrl(String mainImageUrl) { this.mainImageUrl = mainImageUrl; }
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

