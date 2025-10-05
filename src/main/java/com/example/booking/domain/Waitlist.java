package com.example.booking.domain;

import java.time.LocalDateTime;

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
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "waitlist")
public class Waitlist {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "waitlist_id")
    private Integer waitlistId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private RestaurantProfile restaurant;
    
    @Column(name = "party_size", nullable = false)
    @Min(value = 1, message = "Số người tối thiểu là 1")
    @Max(value = 20, message = "Số người tối đa là 20")
    private Integer partySize;
    
    @Column(name = "join_time", nullable = false)
    private LocalDateTime joinTime;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WaitlistStatus status = WaitlistStatus.WAITING;
    
    // Transient field for calculated estimated wait time
    @Transient
    private Integer estimatedWaitTime;

    // Constructors
    public Waitlist() {
        this.joinTime = LocalDateTime.now();
    }
    
    public Waitlist(Customer customer, RestaurantProfile restaurant, Integer partySize, WaitlistStatus status) {
        this();
        this.customer = customer;
        this.restaurant = restaurant;
        this.partySize = partySize;
        this.status = status != null ? status : WaitlistStatus.WAITING;
    }
    
    // Getters and Setters
    public Integer getWaitlistId() {
        return waitlistId;
    }
    
    public void setWaitlistId(Integer waitlistId) {
        this.waitlistId = waitlistId;
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
    
    public Integer getPartySize() {
        return partySize;
    }
    
    public void setPartySize(Integer partySize) {
        this.partySize = partySize;
    }
    
    public LocalDateTime getJoinTime() {
        return joinTime;
    }
    
    public void setJoinTime(LocalDateTime joinTime) {
        this.joinTime = joinTime;
    }
    
    public WaitlistStatus getStatus() {
        return status;
    }
    
    public void setStatus(WaitlistStatus status) {
        this.status = status;
    }

    public Integer getEstimatedWaitTime() {
        return estimatedWaitTime;
    }

    public void setEstimatedWaitTime(Integer estimatedWaitTime) {
        this.estimatedWaitTime = estimatedWaitTime;
    }
}
