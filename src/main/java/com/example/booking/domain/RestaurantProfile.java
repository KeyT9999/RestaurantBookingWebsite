package com.example.booking.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "restaurant_profile")
public class RestaurantProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restaurant_id")
    private Integer restaurantId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = true)
    private RestaurantOwner owner;
    
    @Column(name = "restaurant_name", nullable = false)
    @NotBlank(message = "Tên nhà hàng không được để trống")
    @Size(max = 255, message = "Tên nhà hàng không được quá 255 ký tự")
    private String restaurantName;
    
    @Column(name = "address")
    @Size(max = 500, message = "Địa chỉ không được quá 500 ký tự")
    private String address;
    
    @Column(name = "phone")
    @Size(max = 20, message = "Số điện thoại không được quá 20 ký tự")
    private String phone;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "cuisine_type")
    @Size(max = 100, message = "Loại ẩm thực không được quá 100 ký tự")
    private String cuisineType;
    
    @Column(name = "opening_hours")
    @Size(max = 100, message = "Giờ mở cửa không được quá 100 ký tự")
    private String openingHours;
    
    @Column(name = "average_price", precision = 18, scale = 2)
    @DecimalMin(value = "0.0", message = "Giá trung bình không được âm")
    private BigDecimal averagePrice;
    
    @Column(name = "website_url")
    @Size(max = 255, message = "URL website không được quá 255 ký tự")
    private String websiteUrl;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RestaurantTable> tables;
    
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Dish> dishes;
    
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews;
    
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CustomerFavorite> favorites;
    
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Voucher> vouchers;
    
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Waitlist> waitlists;
    
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RestaurantMedia> media;
    
    // Constructors
    public RestaurantProfile() {
        this.createdAt = LocalDateTime.now();
    }
    
    public RestaurantProfile(RestaurantOwner owner, String restaurantName, String address, 
                           String phone, String description, String cuisineType, 
                           String openingHours, BigDecimal averagePrice, String websiteUrl) {
        this();
        this.owner = owner;
        this.restaurantName = restaurantName;
        this.address = address;
        this.phone = phone;
        this.description = description;
        this.cuisineType = cuisineType;
        this.openingHours = openingHours;
        this.averagePrice = averagePrice;
        this.websiteUrl = websiteUrl;
    }
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Integer getRestaurantId() {
        return restaurantId;
    }
    
    public void setRestaurantId(Integer restaurantId) {
        this.restaurantId = restaurantId;
    }
    
    public RestaurantOwner getOwner() {
        return owner;
    }
    
    public void setOwner(RestaurantOwner owner) {
        this.owner = owner;
    }
    
    public String getRestaurantName() {
        return restaurantName;
    }
    
    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCuisineType() {
        return cuisineType;
    }
    
    public void setCuisineType(String cuisineType) {
        this.cuisineType = cuisineType;
    }
    
    public String getOpeningHours() {
        return openingHours;
    }
    
    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }
    
    public BigDecimal getAveragePrice() {
        return averagePrice;
    }
    
    public void setAveragePrice(BigDecimal averagePrice) {
        this.averagePrice = averagePrice;
    }
    
    public String getWebsiteUrl() {
        return websiteUrl;
    }
    
    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
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
    
    public List<RestaurantTable> getTables() {
        return tables;
    }
    
    public void setTables(List<RestaurantTable> tables) {
        this.tables = tables;
    }
    
    public List<Dish> getDishes() {
        return dishes;
    }
    
    public void setDishes(List<Dish> dishes) {
        this.dishes = dishes;
    }
    
    public List<Review> getReviews() {
        return reviews;
    }
    
    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }
    
    public List<CustomerFavorite> getFavorites() {
        return favorites;
    }
    
    public void setFavorites(List<CustomerFavorite> favorites) {
        this.favorites = favorites;
    }
    
    public List<Voucher> getVouchers() {
        return vouchers;
    }
    
    public void setVouchers(List<Voucher> vouchers) {
        this.vouchers = vouchers;
    }
    
    public List<Waitlist> getWaitlists() {
        return waitlists;
    }
    
    public void setWaitlists(List<Waitlist> waitlists) {
        this.waitlists = waitlists;
    }
    
    public List<RestaurantMedia> getMedia() {
        return media;
    }
    
    public void setMedia(List<RestaurantMedia> media) {
        this.media = media;
    }

    // Helper methods
    public String getName() {
        return restaurantName;
    }

    public String getId() {
        return restaurantId.toString();
    }

    public boolean hasTables() {
        return tables != null && !tables.isEmpty();
    }

    public int getTableCount() {
        return tables != null ? tables.size() : 0;
    }
}
