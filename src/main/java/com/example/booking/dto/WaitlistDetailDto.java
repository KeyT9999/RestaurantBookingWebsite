package com.example.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class WaitlistDetailDto {
    private Integer waitlistId;
    private String customerName;
    private String restaurantName;
    private Integer partySize;
    private LocalDateTime joinTime;
    private String status;
    private Integer estimatedWaitTime;
    private Integer queuePosition;
    private String preferredBookingTime;
    private String specialRequests;

    // Lists for related data
    private List<WaitlistDishDto> dishes;
    private List<WaitlistServiceDto> services;
    private List<WaitlistTableDto> tables;
    
    // Constructors
    public WaitlistDetailDto() {}

    // Getters and Setters
    public Integer getWaitlistId() {
        return waitlistId;
    }
    
    public void setWaitlistId(Integer waitlistId) {
        this.waitlistId = waitlistId;
    }

    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getRestaurantName() {
        return restaurantName;
    }
    
    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }
    
    public Integer getPartySize() {
        return partySize;
    }
    
    public void setPartySize(Integer partySize) {
        this.partySize = partySize;
    }
    
    public LocalDateTime getJoinTime() {
        return joinTime;
    }
    
    public void setJoinTime(LocalDateTime joinTime) {
        this.joinTime = joinTime;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getEstimatedWaitTime() {
        return estimatedWaitTime;
    }
    
    public void setEstimatedWaitTime(Integer estimatedWaitTime) {
        this.estimatedWaitTime = estimatedWaitTime;
    }
    
    public Integer getQueuePosition() {
        return queuePosition;
    }
    
    public void setQueuePosition(Integer queuePosition) {
        this.queuePosition = queuePosition;
    }
    
    public String getPreferredBookingTime() {
        return preferredBookingTime;
    }

    public void setPreferredBookingTime(String preferredBookingTime) {
        this.preferredBookingTime = preferredBookingTime;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }
    
    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }
    
    public List<WaitlistDishDto> getDishes() {
        return dishes;
    }

    public void setDishes(List<WaitlistDishDto> dishes) {
        this.dishes = dishes;
    }

    public List<WaitlistServiceDto> getServices() {
        return services;
    }

    public void setServices(List<WaitlistServiceDto> services) {
        this.services = services;
    }

    public List<WaitlistTableDto> getTables() {
        return tables;
    }

    public void setTables(List<WaitlistTableDto> tables) {
        this.tables = tables;
    }

    // Inner DTOs
    public static class WaitlistDishDto {
        private String dishName;
        private String description;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal totalPrice;

        // Constructors
        public WaitlistDishDto() {
        }

        public WaitlistDishDto(String dishName, String description, Integer quantity, BigDecimal price,
                BigDecimal totalPrice) {
            this.dishName = dishName;
            this.description = description;
            this.quantity = quantity;
            this.price = price;
            this.totalPrice = totalPrice;
        }

        // Getters and Setters
        public String getDishName() {
            return dishName;
        }

        public void setDishName(String dishName) {
            this.dishName = dishName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public BigDecimal getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
        }
    }

    public static class WaitlistServiceDto {
        private String serviceName;
        private String description;
        private BigDecimal price;

        // Constructors
        public WaitlistServiceDto() {
        }

        public WaitlistServiceDto(String serviceName, String description, BigDecimal price) {
            this.serviceName = serviceName;
            this.description = description;
            this.price = price;
        }

        // Getters and Setters
        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getPrice() {
            return price;
        }
        
        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }
    
    public static class WaitlistTableDto {
        private String tableName;
        private Integer capacity;
        private String status;

        // Constructors
        public WaitlistTableDto() {
        }

        public WaitlistTableDto(String tableName, Integer capacity, String status) {
            this.tableName = tableName;
            this.capacity = capacity;
            this.status = status;
        }
        
        // Getters and Setters
        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public Integer getCapacity() {
            return capacity;
        }

        public void setCapacity(Integer capacity) {
            this.capacity = capacity;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}