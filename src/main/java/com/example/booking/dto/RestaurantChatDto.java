package com.example.booking.dto;

/**
 * DTO for restaurant information in chat context
 */
public class RestaurantChatDto {
    private Integer restaurantId;
    private String restaurantName;
    private String address;
    private String phoneNumber;
    private Boolean isActive;
    
    // Constructors
    public RestaurantChatDto() {}
    
    public RestaurantChatDto(Integer restaurantId, String restaurantName, String address, String phoneNumber, Boolean isActive) {
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.isActive = isActive;
    }
    
    // Getters and Setters
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
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
