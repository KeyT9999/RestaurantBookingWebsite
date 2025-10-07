package com.example.booking.dto;

public class RestaurantChatDto {
    private Integer restaurantId;
    private String restaurantName;
    private String address;
    private String phone;
    private boolean isActive;
    private String roomId;
    private Long unreadCount;

    public RestaurantChatDto() {}

    public RestaurantChatDto(Integer restaurantId, String restaurantName, String address, String phone,
            boolean isActive) {
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.address = address;
        this.phone = phone;
        this.isActive = isActive;
    }

    public RestaurantChatDto(Integer restaurantId, String restaurantName, String address, String phone,
            boolean isActive, String roomId) {
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.address = address;
        this.phone = phone;
        this.isActive = isActive;
        this.roomId = roomId;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public Long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Long unreadCount) {
        this.unreadCount = unreadCount;
    }
}