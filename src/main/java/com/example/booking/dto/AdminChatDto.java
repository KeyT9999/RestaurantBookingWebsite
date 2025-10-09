package com.example.booking.dto;

import java.util.List;
import java.util.UUID;

public class AdminChatDto {
    private UUID adminId;
    private String adminName;
    private String adminEmail;
    private boolean isActive;
    private List<RestaurantInfo> restaurants;

    public AdminChatDto() {}

    public AdminChatDto(UUID adminId, String adminName, String adminEmail, boolean isActive, List<RestaurantInfo> restaurants) {
        this.adminId = adminId;
        this.adminName = adminName;
        this.adminEmail = adminEmail;
        this.isActive = isActive;
        this.restaurants = restaurants;
    }

    // Getters and setters
    public UUID getAdminId() {
        return adminId;
    }

    public void setAdminId(UUID adminId) {
        this.adminId = adminId;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<RestaurantInfo> getRestaurants() {
        return restaurants;
    }

    public void setRestaurants(List<RestaurantInfo> restaurants) {
        this.restaurants = restaurants;
    }

    public static class RestaurantInfo {
        private Long restaurantId;
        private String restaurantName;

        public RestaurantInfo() {}

        public RestaurantInfo(Long restaurantId, String restaurantName) {
            this.restaurantId = restaurantId;
            this.restaurantName = restaurantName;
        }

        public Long getRestaurantId() {
            return restaurantId;
        }

        public void setRestaurantId(Long restaurantId) {
            this.restaurantId = restaurantId;
        }

        public String getRestaurantName() {
            return restaurantName;
        }

        public void setRestaurantName(String restaurantName) {
            this.restaurantName = restaurantName;
        }
    }
}
