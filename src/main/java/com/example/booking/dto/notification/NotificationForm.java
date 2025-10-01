package com.example.booking.dto.notification;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import com.example.booking.domain.NotificationType;
import com.example.booking.domain.UserRole;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class NotificationForm {
    
    @NotNull(message = "Loại thông báo không được để trống")
    private NotificationType type;
    
    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 200, message = "Tiêu đề không được quá 200 ký tự")
    private String title;
    
    @NotBlank(message = "Nội dung không được để trống")
    private String content;
    
    @Size(max = 500, message = "Link URL không được quá 500 ký tự")
    private String linkUrl;
    
    private Integer priority = 0;
    
    private LocalDateTime publishAt;
    
    private LocalDateTime expireAt;
    
    @NotNull(message = "Đối tượng nhận không được để trống")
    private AudienceType audience;
    
    // For ROLE audience
    private Set<UserRole> targetRoles;
    
    // For USER audience
    private Set<UUID> targetUserIds;
    
    public enum AudienceType {
        ALL,    // Gửi cho tất cả
        ROLE,   // Gửi theo vai trò
        USER    // Gửi cho user cụ thể
    }
    
    // Constructors
    public NotificationForm() {
        this.publishAt = LocalDateTime.now();
    }
    
    // Getters and Setters
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
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
    
    public LocalDateTime getPublishAt() {
        return publishAt;
    }
    
    public void setPublishAt(LocalDateTime publishAt) {
        this.publishAt = publishAt;
    }
    
    public LocalDateTime getExpireAt() {
        return expireAt;
    }
    
    public void setExpireAt(LocalDateTime expireAt) {
        this.expireAt = expireAt;
    }
    
    public AudienceType getAudience() {
        return audience;
    }
    
    public void setAudience(AudienceType audience) {
        this.audience = audience;
    }
    
    public Set<UserRole> getTargetRoles() {
        return targetRoles;
    }
    
    public void setTargetRoles(Set<UserRole> targetRoles) {
        this.targetRoles = targetRoles;
    }
    
    public Set<UUID> getTargetUserIds() {
        return targetUserIds;
    }
    
    public void setTargetUserIds(Set<UUID> targetUserIds) {
        this.targetUserIds = targetUserIds;
    }
} 