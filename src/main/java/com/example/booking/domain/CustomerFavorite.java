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

@Entity
@Table(name = "customer_favorite")
public class CustomerFavorite {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_id")
    private Integer favoriteId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private RestaurantProfile restaurant;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public CustomerFavorite() {
        this.createdAt = LocalDateTime.now();
    }
    
    public CustomerFavorite(Customer customer, RestaurantProfile restaurant) {
        this();
        this.customer = customer;
        this.restaurant = restaurant;
    }
    
    // Getters and Setters
    public Integer getFavoriteId() {
        return favoriteId;
    }
    
    public void setFavoriteId(Integer favoriteId) {
        this.favoriteId = favoriteId;
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
