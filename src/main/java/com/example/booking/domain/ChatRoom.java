package com.example.booking.domain;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Chat room entity for managing conversations between users
 * Supports customer-restaurant and admin-restaurant chats
 */
@Entity
@Table(name = "chat_room")
public class ChatRoom {
    
    @Id
    @Column(name = "room_id", length = 100)
    private String roomId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer; // NULL for admin-restaurant chats
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private RestaurantProfile restaurant;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private User admin; // NULL for customer-restaurant chats
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @OneToMany(mappedBy = "room", cascade = jakarta.persistence.CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Message> messages;
    
    // Constructors
    public ChatRoom() {
        this.createdAt = LocalDateTime.now();
    }
    
    public ChatRoom(String roomId, Customer customer, RestaurantProfile restaurant) {
        this();
        this.roomId = roomId;
        this.customer = customer;
        this.restaurant = restaurant;
    }
    
    public ChatRoom(String roomId, User admin, RestaurantProfile restaurant) {
        this();
        this.roomId = roomId;
        this.admin = admin;
        this.restaurant = restaurant;
    }
    
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
    
    // Helper methods
    public boolean isCustomerRestaurantChat() {
        return customer != null && restaurant != null && admin == null;
    }
    
    public boolean isAdminRestaurantChat() {
        return admin != null && restaurant != null && customer == null;
    }
    
    public String getParticipantName() {
        if (isCustomerRestaurantChat()) {
            return customer.getFullName();
        } else if (isAdminRestaurantChat()) {
            return admin.getFullName();
        }
        return "Unknown";
    }
    
    public String getRestaurantName() {
        return restaurant != null ? restaurant.getRestaurantName() : "Unknown Restaurant";
    }
    
    // Getters and Setters
    public String getRoomId() {
        return roomId;
    }
    
    public void setRoomId(String roomId) {
        this.roomId = roomId;
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
    
    public User getAdmin() {
        return admin;
    }
    
    public void setAdmin(User admin) {
        this.admin = admin;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }
    
    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public List<Message> getMessages() {
        return messages;
    }
    
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
