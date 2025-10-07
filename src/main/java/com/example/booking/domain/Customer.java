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
@Table(name = "customer")
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "customer_id")
    private UUID customerId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings;
    
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews;
    
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CustomerFavorite> favorites;
    
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CustomerVoucher> vouchers;
    
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments;
    
    // Messages are accessed through ChatRoom, not direct relationship
    // @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch =
    // FetchType.LAZY)
    // private List<Message> messages;
    
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Waitlist> waitlists;
    
    // Constructors
    public Customer() {
        // createdAt and updatedAt will be set by @PrePersist
    }
    
    public Customer(User user) {
        this();
        this.user = user;
        this.fullName = user.getFullName();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        // Set fullName from user if not already set
        if (this.fullName == null && this.user != null) {
            this.fullName = this.user.getFullName();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        // Sync fullName with user if user exists
        if (this.user != null && this.user.getFullName() != null) {
            this.fullName = this.user.getFullName();
        }
    }
    
    // Getters and Setters
    public UUID getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
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
    
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<Booking> getBookings() {
        return bookings;
    }
    
    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
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
    
    public List<CustomerVoucher> getVouchers() {
        return vouchers;
    }
    
    public void setVouchers(List<CustomerVoucher> vouchers) {
        this.vouchers = vouchers;
    }
    
    public List<Payment> getPayments() {
        return payments;
    }
    
    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }
    
    // Messages are accessed through ChatRoom, not direct relationship
    // public List<Message> getMessages() {
    // return messages;
    // }
    //
    // public void setMessages(List<Message> messages) {
    // this.messages = messages;
    // }
    
    public List<Waitlist> getWaitlists() {
        return waitlists;
    }
    
    public void setWaitlists(List<Waitlist> waitlists) {
        this.waitlists = waitlists;
    }
}
