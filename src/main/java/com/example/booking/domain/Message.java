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
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
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
        return sender != null ? sender.getFullName() : "Unknown";
    }

    public boolean isFromCustomer() {
        return sender != null && sender.getRole() == UserRole.CUSTOMER;
    }

    public boolean isFromRestaurantOwner() {
        return sender != null && sender.getRole() == UserRole.RESTAURANT_OWNER;
    }

    public boolean isFromAdmin() {
        return sender != null && sender.getRole() == UserRole.ADMIN;
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
