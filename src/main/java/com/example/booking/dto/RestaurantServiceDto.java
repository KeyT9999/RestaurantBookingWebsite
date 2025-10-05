package com.example.booking.dto;

import java.math.BigDecimal;

/**
 * DTO for RestaurantService to avoid Hibernate proxy serialization issues
 */
public class RestaurantServiceDto {
    private Integer serviceId;
    private String name;
    private String category;
    private String description;
    private BigDecimal price;
    private String status;
    private Integer restaurantId;

    // Constructors
    public RestaurantServiceDto() {}

    public RestaurantServiceDto(Integer serviceId, String name, String category, 
                               String description, BigDecimal price, String status, 
                               Integer restaurantId) {
        this.serviceId = serviceId;
        this.name = name;
        this.category = category;
        this.description = description;
        this.price = price;
        this.status = status;
        this.restaurantId = restaurantId;
    }

    // Getters and Setters
    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Integer restaurantId) {
        this.restaurantId = restaurantId;
    }
}
