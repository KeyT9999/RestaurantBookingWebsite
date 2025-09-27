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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "restaurant_media")
public class RestaurantMedia {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "media_id")
    private Integer mediaId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private RestaurantProfile restaurant;
    
    @Column(name = "type", nullable = false)
    @NotBlank(message = "Loại media không được để trống")
    @Size(max = 50, message = "Loại media không được quá 50 ký tự")
    private String type; // logo | cover | table_layout
    
    @Column(name = "url", nullable = false)
    @NotBlank(message = "URL không được để trống")
    @Size(max = 500, message = "URL không được quá 500 ký tự")
    private String url;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public RestaurantMedia() {
        this.createdAt = LocalDateTime.now();
    }
    
    public RestaurantMedia(RestaurantProfile restaurant, String type, String url) {
        this();
        this.restaurant = restaurant;
        this.type = type;
        this.url = url;
    }
    
    // Getters and Setters
    public Integer getMediaId() {
        return mediaId;
    }
    
    public void setMediaId(Integer mediaId) {
        this.mediaId = mediaId;
    }
    
    public RestaurantProfile getRestaurant() {
        return restaurant;
    }
    
    public void setRestaurant(RestaurantProfile restaurant) {
        this.restaurant = restaurant;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
