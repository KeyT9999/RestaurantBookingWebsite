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
@Table(name = "waitlist_service")
public class WaitlistServiceItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "waitlist_service_id")
    private Integer waitlistServiceId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waitlist_id", nullable = false)
    private Waitlist waitlist;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private RestaurantService service;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;
    
    @Column(name = "price", nullable = false)
    private java.math.BigDecimal price;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Constructors
    public WaitlistServiceItem() {}
    
    public WaitlistServiceItem(Waitlist waitlist, RestaurantService service, Integer quantity, java.math.BigDecimal price) {
        this.waitlist = waitlist;
        this.service = service;
        this.quantity = quantity;
        this.price = price;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Integer getWaitlistServiceId() {
        return waitlistServiceId;
    }
    
    public void setWaitlistServiceId(Integer waitlistServiceId) {
        this.waitlistServiceId = waitlistServiceId;
    }
    
    public Waitlist getWaitlist() {
        return waitlist;
    }
    
    public void setWaitlist(Waitlist waitlist) {
        this.waitlist = waitlist;
    }
    
    public RestaurantService getService() {
        return service;
    }
    
    public void setService(RestaurantService service) {
        this.service = service;
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
