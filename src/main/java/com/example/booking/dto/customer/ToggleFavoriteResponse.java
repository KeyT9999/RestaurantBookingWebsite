package com.example.booking.dto.customer;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ToggleFavoriteResponse {
    
    private boolean success;
    private String message;
    
    @JsonProperty("isFavorited")
    private boolean isFavorited;
    
    private Integer favoriteCount;
    private Integer restaurantId;
    
    // Constructors
    public ToggleFavoriteResponse() {}
    
    public ToggleFavoriteResponse(boolean success, String message, boolean isFavorited, Integer favoriteCount) {
        this.success = success;
        this.message = message;
        this.isFavorited = isFavorited;
        this.favoriteCount = favoriteCount;
    }
    
    public ToggleFavoriteResponse(boolean success, String message, boolean isFavorited, Integer favoriteCount,
            Integer restaurantId) {
        this.success = success;
        this.message = message;
        this.isFavorited = isFavorited;
        this.favoriteCount = favoriteCount;
        this.restaurantId = restaurantId;
    }

    // Static factory methods
    public static ToggleFavoriteResponse success(boolean isFavorited, Integer favoriteCount) {
        String message = isFavorited ? "Đã thêm vào danh sách yêu thích" : "Đã xóa khỏi danh sách yêu thích";
        return new ToggleFavoriteResponse(true, message, isFavorited, favoriteCount, null);
    }

    public static ToggleFavoriteResponse success(boolean isFavorited, Integer favoriteCount, Integer restaurantId) {
        String message = isFavorited ? "Đã thêm vào danh sách yêu thích" : "Đã xóa khỏi danh sách yêu thích";
        return new ToggleFavoriteResponse(true, message, isFavorited, favoriteCount, restaurantId);
    }
    
    public static ToggleFavoriteResponse error(String message) {
        return new ToggleFavoriteResponse(false, message, false, 0, null);
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public boolean isFavorited() {
        return isFavorited;
    }
    
    public void setFavorited(boolean isFavorited) {
        this.isFavorited = isFavorited;
    }
    
    public Integer getFavoriteCount() {
        return favoriteCount;
    }
    
    public void setFavoriteCount(Integer favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public Integer getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Integer restaurantId) {
        this.restaurantId = restaurantId;
    }
}
