package com.example.booking.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for chat rooms
 */
public class ChatRoomDto {
    private String roomId;
    private UUID participantId;
    private String participantName;
    private String participantRole;
    private Integer restaurantId;
    private String restaurantName;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private Long unreadCount;
    private Boolean isActive;
    private String participantAvatarUrl;
    
    // Constructors
    public ChatRoomDto() {}
    
    public ChatRoomDto(String roomId, UUID participantId, String participantName, 
                      String participantRole, Integer restaurantId, String restaurantName,
                      String lastMessage, LocalDateTime lastMessageAt, Long unreadCount, Boolean isActive) {
        this(roomId, participantId, participantName, participantRole, restaurantId, restaurantName,
             lastMessage, lastMessageAt, unreadCount, isActive, null);
    }
    
    public ChatRoomDto(String roomId, UUID participantId, String participantName, 
                      String participantRole, Integer restaurantId, String restaurantName,
                      String lastMessage, LocalDateTime lastMessageAt, Long unreadCount, Boolean isActive,
                      String participantAvatarUrl) {
        this.roomId = roomId;
        this.participantId = participantId;
        this.participantName = participantName;
        this.participantRole = participantRole;
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.lastMessage = lastMessage;
        this.lastMessageAt = lastMessageAt;
        this.unreadCount = unreadCount;
        this.isActive = isActive;
        this.participantAvatarUrl = participantAvatarUrl;
    }
    
    // Getters and Setters
    public String getRoomId() {
        return roomId;
    }
    
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    
    public UUID getParticipantId() {
        return participantId;
    }
    
    public void setParticipantId(UUID participantId) {
        this.participantId = participantId;
    }
    
    public String getParticipantName() {
        return participantName;
    }
    
    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }
    
    public String getParticipantRole() {
        return participantRole;
    }
    
    public void setParticipantRole(String participantRole) {
        this.participantRole = participantRole;
    }
    
    public Integer getRestaurantId() {
        return restaurantId;
    }
    
    public void setRestaurantId(Integer restaurantId) {
        this.restaurantId = restaurantId;
    }
    
    public String getRestaurantName() {
        return restaurantName;
    }
    
    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }
    
    public String getLastMessage() {
        return lastMessage;
    }
    
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
    
    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }
    
    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }
    
    public Long getUnreadCount() {
        return unreadCount;
    }
    
    public void setUnreadCount(Long unreadCount) {
        this.unreadCount = unreadCount;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public String getParticipantAvatarUrl() {
        return participantAvatarUrl;
    }
    
    public void setParticipantAvatarUrl(String participantAvatarUrl) {
        this.participantAvatarUrl = participantAvatarUrl;
    }
}
