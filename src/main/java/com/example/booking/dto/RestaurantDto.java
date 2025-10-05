package com.example.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class RestaurantDto {
    private Integer restaurantId;
    private String restaurantName;
    private String description;
    private String address;
    private String phone;
    private String cuisineType;
    private String openingHours;
    private BigDecimal averagePrice;
    private String websiteUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID ownerId;

    // Constructors
    public RestaurantDto() {}

    public RestaurantDto(Integer restaurantId, String restaurantName, String description, 
                        String address, String phone, String cuisineType,
                        String openingHours, BigDecimal averagePrice, String websiteUrl,
                        LocalDateTime createdAt, LocalDateTime updatedAt, UUID ownerId) {
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.description = description;
        this.address = address;
        this.phone = phone;
        this.cuisineType = cuisineType;
        this.openingHours = openingHours;
        this.averagePrice = averagePrice;
        this.websiteUrl = websiteUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.ownerId = ownerId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }
}
