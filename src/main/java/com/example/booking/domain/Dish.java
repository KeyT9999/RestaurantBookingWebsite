package com.example.booking.domain;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "dish")
public class Dish {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dish_id")
    private Integer dishId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private RestaurantProfile restaurant;
    
    @Column(name = "name", nullable = false)
    @NotBlank(message = "Tên món ăn không được để trống")
    @Size(max = 255, message = "Tên món ăn không được quá 255 ký tự")
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "price", nullable = false, precision = 18, scale = 2)
    @DecimalMin(value = "0.0", message = "Giá không được âm")
    private BigDecimal price;
    
    @Column(name = "category")
    @Size(max = 100, message = "Danh mục không được quá 100 ký tự")
    private String category;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DishStatus status = DishStatus.AVAILABLE;
    
    @OneToMany(mappedBy = "dish", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookingDish> bookingDishes;
    
    // Constructors
    public Dish() {}
    
    public Dish(RestaurantProfile restaurant, String name, String description, 
                BigDecimal price, String category, DishStatus status) {
        this.restaurant = restaurant;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.status = status != null ? status : DishStatus.AVAILABLE;
    }
    
    // Getters and Setters
    public Integer getDishId() {
        return dishId;
    }
    
    public void setDishId(Integer dishId) {
        this.dishId = dishId;
    }
    
    public RestaurantProfile getRestaurant() {
        return restaurant;
    }
    
    public void setRestaurant(RestaurantProfile restaurant) {
        this.restaurant = restaurant;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public DishStatus getStatus() {
        return status;
    }
    
    public void setStatus(DishStatus status) {
        this.status = status;
    }
    
    public List<BookingDish> getBookingDishes() {
        return bookingDishes;
    }
    
    public void setBookingDishes(List<BookingDish> bookingDishes) {
        this.bookingDishes = bookingDishes;
    }
}
