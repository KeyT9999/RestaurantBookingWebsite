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
@Table(name = "waitlist_dish")
public class WaitlistDish {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "waitlist_dish_id")
    private Integer waitlistDishId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waitlist_id", nullable = false)
    private Waitlist waitlist;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id", nullable = false)
    private Dish dish;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;
    
    @Column(name = "price", nullable = false)
    private java.math.BigDecimal price;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Constructors
    public WaitlistDish() {}
    
    public WaitlistDish(Waitlist waitlist, Dish dish, Integer quantity, java.math.BigDecimal price) {
        this.waitlist = waitlist;
        this.dish = dish;
        this.quantity = quantity;
        this.price = price;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Integer getWaitlistDishId() {
        return waitlistDishId;
    }
    
    public void setWaitlistDishId(Integer waitlistDishId) {
        this.waitlistDishId = waitlistDishId;
    }
    
    public Waitlist getWaitlist() {
        return waitlist;
    }
    
    public void setWaitlist(Waitlist waitlist) {
        this.waitlist = waitlist;
    }
    
    public Dish getDish() {
        return dish;
    }
    
    public void setDish(Dish dish) {
        this.dish = dish;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public java.math.BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(java.math.BigDecimal price) {
        this.price = price;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
