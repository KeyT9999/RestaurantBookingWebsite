package com.example.booking.dto;

import java.math.BigDecimal;

public class DishDto {
    private Integer dishId;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private String status;
    private Integer restaurantId;

    // Constructors
    public DishDto() {}

    public DishDto(Integer dishId, String name, String description, BigDecimal price, 
                   String category, String status, Integer restaurantId) {
        this.dishId = dishId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.status = status;
        this.restaurantId = restaurantId;
    }

    // Getters and Setters
    public Integer getDishId() {
        return dishId;
    }

    public void setDishId(Integer dishId) {
        this.dishId = dishId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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
