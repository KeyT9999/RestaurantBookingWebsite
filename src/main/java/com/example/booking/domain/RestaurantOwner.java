package com.example.booking.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "restaurant_owner")
public class RestaurantOwner {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "owner_id")
    private UUID ownerId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "owner_name", nullable = false, length = 255)
    private String ownerName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RestaurantProfile> restaurants;
    
    // Constructors
    public RestaurantOwner() {
        this.createdAt = LocalDateTime.now();
    }
    
    public RestaurantOwner(User user) {
        this();
        this.user = user;
        this.ownerName = user.getFullName(); // Set owner name from user's full name
    }
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        // Ensure ownerName is set
        if (this.ownerName == null && this.user != null) {
            this.ownerName = this.user.getFullName();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public UUID getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
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
    
    public List<RestaurantProfile> getRestaurants() {
        return restaurants;
    }
    
    public void setRestaurants(List<RestaurantProfile> restaurants) {
        this.restaurants = restaurants;
    }
}
