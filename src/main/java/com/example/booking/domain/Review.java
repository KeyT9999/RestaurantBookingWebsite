package com.example.booking.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "review", uniqueConstraints = @UniqueConstraint(columnNames = { "customer_id", "restaurant_id" }))
public class Review {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Integer reviewId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private RestaurantProfile restaurant;
    
    @Column(name = "rating")
    @Min(value = 1, message = "Đánh giá tối thiểu là 1 sao")
    @Max(value = 5, message = "Đánh giá tối đa là 5 sao")
    private Integer rating;
    
    @Column(name = "comment", columnDefinition = "TEXT")
    @Size(max = 1000, message = "Bình luận không được quá 1000 ký tự")
    private String comment;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public Review() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Review(Customer customer, RestaurantProfile restaurant, Integer rating, String comment) {
        this();
        this.customer = customer;
        this.restaurant = restaurant;
        this.rating = rating;
        this.comment = comment;
    }
    
    // Getters and Setters
    public Integer getReviewId() {
        return reviewId;
    }
    
    public void setReviewId(Integer reviewId) {
        this.reviewId = reviewId;
    }
    
    public Customer getCustomer() {
        return customer;
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    public RestaurantProfile getRestaurant() {
        return restaurant;
    }
    
    public void setRestaurant(RestaurantProfile restaurant) {
        this.restaurant = restaurant;
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Helper methods
    public boolean isEditable() {
        // Review có thể chỉnh sửa trong vòng 30 ngày
        return createdAt.isAfter(LocalDateTime.now().minusDays(30));
    }

    public String getCustomerName() {
        return customer != null && customer.getUser() != null ? customer.getUser().getFullName() : "Anonymous";
    }

    public String getRestaurantName() {
        return restaurant != null ? restaurant.getRestaurantName() : "Unknown Restaurant";
    }

    public String getFormattedCreatedAt() {
        return createdAt.toString();
    }

    @Override
    public String toString() {
        return "Review{" +
                "reviewId=" + reviewId +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
