package com.example.booking.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Enhanced User Preferences Entity with advanced learning capabilities
 */
@Entity
@Table(name = "user_preferences")
public class UserPreferences {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    // Basic Preferences
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "cuisine_preferences", columnDefinition = "jsonb")
    private String cuisinePreferences = "[]";
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "price_range", columnDefinition = "jsonb")
    private String priceRange = "{\"min\": 0, \"max\": 1000000}";
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "location_preferences", columnDefinition = "jsonb")
    private String locationPreferences = "{\"max_distance\": 10, \"districts\": []}";
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dining_occasion", columnDefinition = "jsonb")
    private String diningOccasion = "[]";
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dietary_restrictions", columnDefinition = "jsonb")
    private String dietaryRestrictions = "[]";
    
    // AI Learning Data
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "favorite_restaurants", columnDefinition = "jsonb")
    private String favoriteRestaurants = "[]";
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "disliked_restaurants", columnDefinition = "jsonb")
    private String dislikedRestaurants = "[]";
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "booking_patterns", columnDefinition = "jsonb")
    private String bookingPatterns = "{}";
    
    // Advanced Preferences
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferred_ambiance", columnDefinition = "jsonb")
    private String preferredAmbiance = "[]";
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferred_cuisine_styles", columnDefinition = "jsonb")
    private String preferredCuisineStyles = "[]";
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "special_requirements", columnDefinition = "jsonb")
    private String specialRequirements = "[]";
    
    // Learning Metrics
    @Column(name = "total_interactions")
    private Integer totalInteractions = 0;
    
    @Column(name = "successful_bookings")
    private Integer successfulBookings = 0;
    
    @Column(name = "last_updated_preferences")
    private LocalDateTime lastUpdatedPreferences;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public UserPreferences() {}
    
    public UserPreferences(User user) {
        this.user = user;
        this.lastUpdatedPreferences = LocalDateTime.now();
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
    
    public String getCuisinePreferences() {
        return cuisinePreferences;
    }
    
    public void setCuisinePreferences(String cuisinePreferences) {
        this.cuisinePreferences = cuisinePreferences;
    }
    
    public String getPriceRange() {
        return priceRange;
    }
    
    public void setPriceRange(String priceRange) {
        this.priceRange = priceRange;
    }
    
    public String getLocationPreferences() {
        return locationPreferences;
    }
    
    public void setLocationPreferences(String locationPreferences) {
        this.locationPreferences = locationPreferences;
    }
    
    public String getDiningOccasion() {
        return diningOccasion;
    }
    
    public void setDiningOccasion(String diningOccasion) {
        this.diningOccasion = diningOccasion;
    }
    
    public String getDietaryRestrictions() {
        return dietaryRestrictions;
    }
    
    public void setDietaryRestrictions(String dietaryRestrictions) {
        this.dietaryRestrictions = dietaryRestrictions;
    }
    
    public String getFavoriteRestaurants() {
        return favoriteRestaurants;
    }
    
    public void setFavoriteRestaurants(String favoriteRestaurants) {
        this.favoriteRestaurants = favoriteRestaurants;
    }
    
    public String getDislikedRestaurants() {
        return dislikedRestaurants;
    }
    
    public void setDislikedRestaurants(String dislikedRestaurants) {
        this.dislikedRestaurants = dislikedRestaurants;
    }
    
    public String getBookingPatterns() {
        return bookingPatterns;
    }
    
    public void setBookingPatterns(String bookingPatterns) {
        this.bookingPatterns = bookingPatterns;
    }
    
    public String getPreferredAmbiance() {
        return preferredAmbiance;
    }
    
    public void setPreferredAmbiance(String preferredAmbiance) {
        this.preferredAmbiance = preferredAmbiance;
    }
    
    public String getPreferredCuisineStyles() {
        return preferredCuisineStyles;
    }
    
    public void setPreferredCuisineStyles(String preferredCuisineStyles) {
        this.preferredCuisineStyles = preferredCuisineStyles;
    }
    
    public String getSpecialRequirements() {
        return specialRequirements;
    }
    
    public void setSpecialRequirements(String specialRequirements) {
        this.specialRequirements = specialRequirements;
    }
    
    public Integer getTotalInteractions() {
        return totalInteractions;
    }
    
    public void setTotalInteractions(Integer totalInteractions) {
        this.totalInteractions = totalInteractions;
    }
    
    public Integer getSuccessfulBookings() {
        return successfulBookings;
    }
    
    public void setSuccessfulBookings(Integer successfulBookings) {
        this.successfulBookings = successfulBookings;
    }
    
    public LocalDateTime getLastUpdatedPreferences() {
        return lastUpdatedPreferences;
    }
    
    public void setLastUpdatedPreferences(LocalDateTime lastUpdatedPreferences) {
        this.lastUpdatedPreferences = lastUpdatedPreferences;
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
    public void incrementInteractions() {
        this.totalInteractions++;
        this.lastUpdatedPreferences = LocalDateTime.now();
    }
    
    public void incrementSuccessfulBookings() {
        this.successfulBookings++;
        this.lastUpdatedPreferences = LocalDateTime.now();
    }
    
    public Double getSuccessRate() {
        if (totalInteractions == 0) return 0.0;
        return (double) successfulBookings / totalInteractions;
    }
}