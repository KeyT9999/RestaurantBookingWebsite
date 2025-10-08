package com.example.booking.dto.customer;

import jakarta.validation.constraints.NotNull;

public class ToggleFavoriteRequest {
    
    @NotNull(message = "Restaurant ID is required")
    private Integer restaurantId;
    
    // Constructors
    public ToggleFavoriteRequest() {}
    
    public ToggleFavoriteRequest(Integer restaurantId) {
        this.restaurantId = restaurantId;
    }
    
    // Getters and Setters
    public Integer getRestaurantId() {
        return restaurantId;
    }
    
    public void setRestaurantId(Integer restaurantId) {
        this.restaurantId = restaurantId;
    }
}
