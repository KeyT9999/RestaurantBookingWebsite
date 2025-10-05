package com.example.booking.dto;

import java.time.LocalDateTime;

public class ReviewDto {
    
    private Integer reviewId;
    private Integer restaurantId;
    private Integer rating;
    private String comment;
    private String customerName;
    private String customerAvatar;
    private LocalDateTime createdAt;
    private String restaurantName;
    private boolean editable;
    
    // Constructors
    public ReviewDto() {}
    
    public ReviewDto(Integer reviewId, Integer restaurantId, Integer rating, String comment, String customerName, 
                     String customerAvatar, LocalDateTime createdAt, String restaurantName, boolean editable) {
        this.reviewId = reviewId;
        this.restaurantId = restaurantId;
        this.rating = rating;
        this.comment = comment;
        this.customerName = customerName;
        this.customerAvatar = customerAvatar;
        this.createdAt = createdAt;
        this.restaurantName = restaurantName;
        this.editable = editable;
    }
    
    // Getters and Setters
    public Integer getReviewId() {
        return reviewId;
    }
    
    public void setReviewId(Integer reviewId) {
        this.reviewId = reviewId;
    }
    
    public Integer getRestaurantId() {
        return restaurantId;
    }
    
    public void setRestaurantId(Integer restaurantId) {
        this.restaurantId = restaurantId;
    }
    
    public Integer getRating() {
        return rating;
    }
    
    public void setRating(Integer rating) {
        this.rating = rating;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public String getCustomerAvatar() {
        return customerAvatar;
    }
    
    public void setCustomerAvatar(String customerAvatar) {
        this.customerAvatar = customerAvatar;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getRestaurantName() {
        return restaurantName;
    }
    
    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }
    
    public boolean isEditable() {
        return editable;
    }
    
    public void setEditable(boolean editable) {
        this.editable = editable;
    }
    
    // Helper methods
    public String getStarRating() {
        if (rating == null) return "";
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < rating; i++) {
            stars.append("★");
        }
        for (int i = rating; i < 5; i++) {
            stars.append("☆");
        }
        return stars.toString();
    }
    
    @Override
    public String toString() {
        return "ReviewDto{" +
                "reviewId=" + reviewId +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", customerName='" + customerName + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
