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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "message")
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Integer messageId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = true)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = true)
    private Customer customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = true)
    private RestaurantOwner owner;
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    @Size(max = 1000, message = "Nội dung tin nhắn không được quá 1000 ký tự")
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType = MessageType.TEXT;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
    
    @Column(name = "is_read")
    private Boolean isRead = false;

    // Constructors
    public Message() {
        this.sentAt = LocalDateTime.now();
    }
    
    public Message(ChatRoom room, User sender, String content) {
        this();
        this.room = room;
        this.sender = sender;
        this.content = content;

        // Set customer if sender is a customer
        if (sender.getRole() == UserRole.CUSTOMER) {
            // Find customer by user ID
            // This will be set by service layer
        }
    }

    public Message(ChatRoom room, User sender, String content, MessageType messageType) {
        this(room, sender, content);
        this.messageType = messageType;
    }

    // Helper methods
    public String getSenderName() {
        if (sender == null) {
            System.out.println("=== DEBUG MESSAGE SENDER NAME ===");
            System.out.println("Sender is NULL");
            return "Unknown";
        }

        System.out.println("=== DEBUG MESSAGE SENDER NAME ===");
        System.out.println("Sender ID: " + sender.getId());
        System.out.println("Sender Role: " + sender.getRole());
        System.out.println("Sender FullName: " + sender.getFullName());
        System.out.println("Customer object: " + (customer != null ? "NOT NULL" : "NULL"));
        if (customer != null) {
            System.out.println("Customer FullName: " + customer.getFullName());
            System.out.println("Customer User FullName: "
                    + (customer.getUser() != null ? customer.getUser().getFullName() : "NULL"));
        }

        // For customers: ALWAYS prioritize sender.fullName (from users table)
        // because customer.fullName might be outdated/unsynchronized
        if (sender.getRole().isCustomer()) {
            System.out.println("Processing as CUSTOMER");
            String userFullName = sender.getFullName();
            if (userFullName != null && !userFullName.trim().isEmpty()) {
                System.out.println("Using sender.fullName (prioritized): " + userFullName);
                return userFullName;
            }
            // If user table doesn't have fullName, fallback to customer table
            if (customer != null && customer.getFullName() != null && !customer.getFullName().trim().isEmpty()) {
                System.out.println("Using customer.fullName (fallback): " + customer.getFullName());
                return customer.getFullName();
            }
            System.out.println("Final fallback to Unknown Customer");
            return sender.getEmail() != null ? sender.getEmail() : "Unknown Customer";
        }

        // For admins and restaurant owners: use user.fullName (from users table)
        System.out.println("Processing as ADMIN/RESTAURANT_OWNER");
        String userFullName = sender.getFullName();
        if (userFullName != null && !userFullName.trim().isEmpty()) {
            System.out.println("Using sender.fullName: " + userFullName);
            return userFullName;
        }

        // Last resort: use email
        System.out.println("Using email as last resort: " + sender.getEmail());
        return sender.getEmail() != null ? sender.getEmail() : "Unknown";
    }

    public boolean isFromCustomer() {
        return sender != null && sender.getRole().isCustomer();
    }

    public boolean isFromRestaurantOwner() {
        return sender != null && sender.getRole().isRestaurantOwner();
    }

    public boolean isFromAdmin() {
        return sender != null && sender.getRole().isAdmin();
    }

    // Getters and Setters
    public Integer getMessageId() {
        return messageId;
    }
    
    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }
    
    public ChatRoom getRoom() {
        return room;
    }
    
    public void setRoom(ChatRoom room) {
        this.room = room;
    }
    
    public User getSender() {
        return sender;
    }
    
    public void setSender(User sender) {
        this.sender = sender;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }
    
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    // Customer getter/setter
    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    // Owner getter/setter
    public RestaurantOwner getOwner() {
        return owner;
    }

    public void setOwner(RestaurantOwner owner) {
        this.owner = owner;
    }
}
