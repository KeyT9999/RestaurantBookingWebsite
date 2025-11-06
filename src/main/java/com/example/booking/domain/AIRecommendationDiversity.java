package com.example.booking.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AI Recommendation Diversity Entity to track and prevent bias
 */
@Entity
@Table(name = "ai_recommendation_diversity")
public class AIRecommendationDiversity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private RestaurantProfile restaurant;
    
    // Diversity Metrics
    @Column(name = "recommendation_count")
    private Integer recommendationCount = 0;
    
    @Column(name = "last_recommended_at")
    private LocalDateTime lastRecommendedAt;
    
    // Diversity Scores
    @Column(name = "cuisine_diversity_score", precision = 3, scale = 2)
    private BigDecimal cuisineDiversityScore = BigDecimal.ZERO;
    
    @Column(name = "price_diversity_score", precision = 3, scale = 2)
    private BigDecimal priceDiversityScore = BigDecimal.ZERO;
    
    @Column(name = "location_diversity_score", precision = 3, scale = 2)
    private BigDecimal locationDiversityScore = BigDecimal.ZERO;
    
    // User Response
    @Enumerated(EnumType.STRING)
    @Column(name = "user_response", length = 50)
    private UserResponse userResponse;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Enums
    public enum UserResponse {
        ACCEPTED, IGNORED, BOOKED, DISLIKED, REPORTED
    }
    
    // Constructors
    public AIRecommendationDiversity() {}
    
    public AIRecommendationDiversity(User user, RestaurantProfile restaurant, LocalDate date) {
        this.user = user;
        this.restaurant = restaurant;
        this.date = date;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public RestaurantProfile getRestaurant() {
        return restaurant;
    }
    
    public void setRestaurant(RestaurantProfile restaurant) {
        this.restaurant = restaurant;
    }
    
    public Integer getRecommendationCount() {
        return recommendationCount;
    }
    
    public void setRecommendationCount(Integer recommendationCount) {
        this.recommendationCount = recommendationCount;
    }
    
    public LocalDateTime getLastRecommendedAt() {
        return lastRecommendedAt;
    }
    
    public void setLastRecommendedAt(LocalDateTime lastRecommendedAt) {
        this.lastRecommendedAt = lastRecommendedAt;
    }
    
    public BigDecimal getCuisineDiversityScore() {
        return cuisineDiversityScore;
    }
    
    public void setCuisineDiversityScore(BigDecimal cuisineDiversityScore) {
        this.cuisineDiversityScore = cuisineDiversityScore;
    }
    
    public BigDecimal getPriceDiversityScore() {
        return priceDiversityScore;
    }
    
    public void setPriceDiversityScore(BigDecimal priceDiversityScore) {
        this.priceDiversityScore = priceDiversityScore;
    }
    
    public BigDecimal getLocationDiversityScore() {
        return locationDiversityScore;
    }
    
    public void setLocationDiversityScore(BigDecimal locationDiversityScore) {
        this.locationDiversityScore = locationDiversityScore;
    }
    
    public UserResponse getUserResponse() {
        return userResponse;
    }
    
    public void setUserResponse(UserResponse userResponse) {
        this.userResponse = userResponse;
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
    
    // Helper methods
    public void incrementRecommendationCount() {
        this.recommendationCount++;
        this.lastRecommendedAt = LocalDateTime.now();
    }
    
    public BigDecimal getOverallDiversityScore() {
        return cuisineDiversityScore.add(priceDiversityScore).add(locationDiversityScore)
                .divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP);
    }
    
    public boolean isOverRecommended() {
        return recommendationCount > 3; // More than 3 times per day
    }
    
    public boolean hasPositiveResponse() {
        return userResponse == UserResponse.ACCEPTED || userResponse == UserResponse.BOOKED;
    }
}
