package com.example.booking.dto;

import java.math.BigDecimal;

import com.example.booking.domain.Dish;
import com.example.booking.domain.DishStatus;

/**
 * DTO for Dish with image URL
 * Used to display dish information along with its image
 */
public class DishWithImageDto {
    private Integer dishId;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private DishStatus status;
    private Integer restaurantId;
    private String imageUrl;

    // Constructors
    public DishWithImageDto() {}

    public DishWithImageDto(Dish dish, String imageUrl) {
        this.dishId = dish.getDishId();
        this.name = dish.getName();
        this.description = dish.getDescription();
        this.price = dish.getPrice();
        this.category = dish.getCategory();
        this.status = dish.getStatus();
        this.restaurantId = dish.getRestaurant().getRestaurantId();
        this.imageUrl = imageUrl;
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

    public DishStatus getStatus() {
        return status;
    }

    public void setStatus(DishStatus status) {
        this.status = status;
    }

    public Integer getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Integer restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
