package com.example.booking.dto;

import java.math.BigDecimal;

/**
 * DTO for booking service details
 */
public class BookingServiceDto {
    
    private Integer serviceId;
    private String serviceName;
    private String description;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
    private String category;
    
    // Constructors
    public BookingServiceDto() {}
    
    public BookingServiceDto(Integer serviceId, String serviceName, String description,
                            Integer quantity, BigDecimal price, BigDecimal totalPrice, String category) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.description = description;
        this.quantity = quantity;
        this.price = price;
        this.totalPrice = totalPrice;
        this.category = category;
    }
    
    // Getters and Setters
    public Integer getServiceId() {
        return serviceId;
    }
    
    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }
    
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
