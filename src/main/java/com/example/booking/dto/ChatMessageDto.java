package com.example.booking.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for chat messages
 */
public class ChatMessageDto {
    private Integer messageId;
    private String roomId;
    private UUID senderId;
    private String senderName;
    private String content;
    private String messageType;
    private String fileUrl;
    private LocalDateTime sentAt;
    private Boolean isRead;
    
    // Constructors
    public ChatMessageDto() {}
    
    public ChatMessageDto(Integer messageId, String roomId, UUID senderId, String senderName, 
                         String content, String messageType, String fileUrl, 
                         LocalDateTime sentAt, Boolean isRead) {
        this.messageId = messageId;
        this.roomId = roomId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.messageType = messageType;
        this.fileUrl = fileUrl;
        this.sentAt = sentAt;
        this.isRead = isRead;
    }
    
    // Getters and Setters
    public Integer getMessageId() {
        return messageId;
    }
    
    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }
    
    public String getRoomId() {
        return roomId;
    }
    
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    
    public UUID getSenderId() {
        return senderId;
    }
    
    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }
    
    public String getSenderName() {
        return senderName;
    }
    
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getMessageType() {
        return messageType;
    }
    
    public void setMessageType(String messageType) {
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
}
