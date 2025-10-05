package com.example.booking.dto;

import java.math.BigDecimal;

/**
 * DTO for booking dish details
 */
public class BookingDishDto {
    
    private Integer dishId;
    private String dishName;
    private String description;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
    private String category;
    
    // Constructors
    public BookingDishDto() {}
    
    public BookingDishDto(Integer dishId, String dishName, String description, 
                         Integer quantity, BigDecimal price, BigDecimal totalPrice, String category) {
        this.dishId = dishId;
        this.dishName = dishName;
        this.description = description;
        this.quantity = quantity;
        this.price = price;
        this.totalPrice = totalPrice;
        this.category = category;
    }
    
    // Getters and Setters
    public Integer getDishId() {
        return dishId;
    }
    
    public void setDishId(Integer dishId) {
        this.dishId = dishId;
    }
    
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
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    // Helper methods
    public String getFormattedPrice() {
        return String.format("%,.0f VND", price.doubleValue());
    }
    
    public String getFormattedTotalPrice() {
        return String.format("%,.0f VND", totalPrice.doubleValue());
    }
}
