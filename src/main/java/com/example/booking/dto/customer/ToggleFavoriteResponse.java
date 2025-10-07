package com.example.booking.dto.customer;

public class ToggleFavoriteResponse {
    
    private boolean success;
    private String message;
    private boolean isFavorited;
    private Integer favoriteCount;
    
    // Constructors
    public ToggleFavoriteResponse() {}
    
    public ToggleFavoriteResponse(boolean success, String message, boolean isFavorited, Integer favoriteCount) {
        this.success = success;
        this.message = message;
        this.isFavorited = isFavorited;
        this.favoriteCount = favoriteCount;
    }
    
    // Static factory methods
    public static ToggleFavoriteResponse success(boolean isFavorited, Integer favoriteCount) {
        String message = isFavorited ? "Đã thêm vào danh sách yêu thích" : "Đã xóa khỏi danh sách yêu thích";
        return new ToggleFavoriteResponse(true, message, isFavorited, favoriteCount);
    }
    
    public static ToggleFavoriteResponse error(String message) {
        return new ToggleFavoriteResponse(false, message, false, 0);
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
}
