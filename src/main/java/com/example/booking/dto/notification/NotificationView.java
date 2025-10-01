package com.example.booking.dto.notification;

import java.time.LocalDateTime;

import com.example.booking.domain.Notification;
import com.example.booking.domain.NotificationType;

public class NotificationView {
    
    private Integer id;
    private NotificationType type;
    private String title;
    private String content;
    private String linkUrl;
    private LocalDateTime publishAt;
    private LocalDateTime readAt;
    private Integer priority;
    private boolean unread;
    
    // Constructors
    public NotificationView() {
    }
    
    public NotificationView(Notification notification) {
        this.id = notification.getNotificationId();
        this.type = notification.getType();
        this.title = notification.getTitle();
        this.content = notification.getContent();
        this.linkUrl = notification.getLinkUrl();
        this.publishAt = notification.getPublishAt();
        this.readAt = notification.getReadAt();
        this.priority = notification.getPriority();
        this.unread = notification.isUnread();
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public NotificationType getType() {
        return type;
    }
    
    public void setType(NotificationType type) {
        this.type = type;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getLinkUrl() {
        return linkUrl;
    }
    
    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }
    
    public LocalDateTime getPublishAt() {
        return publishAt;
    }
    
    public void setPublishAt(LocalDateTime publishAt) {
        this.publishAt = publishAt;
    }
    
    public LocalDateTime getReadAt() {
        return readAt;
    }
    
    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
    
    public boolean isUnread() {
        return unread;
    }
    
    public void setUnread(boolean unread) {
        this.unread = unread;
    }
} 