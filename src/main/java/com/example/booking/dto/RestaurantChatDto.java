package com.example.booking.dto;

import java.util.UUID;

public class RestaurantChatDto {
    private Integer restaurantId;
    private String restaurantName;
    private String ownerName;
    private String ownerEmail;
    private boolean isActive;

    public RestaurantChatDto() {}

    public RestaurantChatDto(Integer restaurantId, String restaurantName, String ownerName, String ownerEmail,
            boolean isActive) {
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.ownerName = ownerName;
        this.ownerEmail = ownerEmail;
        this.isActive = isActive;
    }

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

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}