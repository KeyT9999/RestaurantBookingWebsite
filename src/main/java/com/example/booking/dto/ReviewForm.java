package com.example.booking.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ReviewForm {
    
    @NotNull(message = "Restaurant ID không được để trống")
    private Integer restaurantId;
    
    @NotNull(message = "Rating không được để trống")
    @Min(value = 1, message = "Rating tối thiểu là 1 sao")
    @Max(value = 5, message = "Rating tối đa là 5 sao")
    private Integer rating;
    
    @Size(max = 1000, message = "Bình luận không được quá 1000 ký tự")
    private String comment;
    
    // Constructors
    public ReviewForm() {}
    
    public ReviewForm(Integer restaurantId, Integer rating, String comment) {
        this.restaurantId = restaurantId;
        this.rating = rating;
        this.comment = comment;
    }
    
    // Getters and Setters
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
    
    @Override
    public String toString() {
        return "ReviewForm{" +
                "restaurantId=" + restaurantId +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                '}';
    }
}
