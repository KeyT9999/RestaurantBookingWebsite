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
@Table(name = "notification")
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Integer notificationId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_user_id", nullable = false)
    private User recipientUser;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    @NotBlank(message = "Loại thông báo không được để trống")
    private NotificationType type;
    
    @Column(name = "content", columnDefinition = "TEXT")
    @Size(max = 1000, message = "Nội dung không được quá 1000 ký tự")
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationStatus status = NotificationStatus.PENDING;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public Notification() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Notification(User recipientUser, NotificationType type, String content, NotificationStatus status) {
        this();
        this.recipientUser = recipientUser;
        this.type = type;
        this.content = content;
        this.status = status != null ? status : NotificationStatus.PENDING;
    }
    
    // Getters and Setters
    public Integer getNotificationId() {
        return notificationId;
    }
    
    public void setNotificationId(Integer notificationId) {
        this.notificationId = notificationId;
    }
    
    public User getRecipientUser() {
        return recipientUser;
    }
    
    public void setRecipientUser(User recipientUser) {
        this.recipientUser = recipientUser;
    }
    
    public NotificationType getType() {
        return type;
    }
    
    public void setType(NotificationType type) {
        this.type = type;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public NotificationStatus getStatus() {
        return status;
    }
    
    public void setStatus(NotificationStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
