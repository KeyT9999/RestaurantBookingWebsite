package com.example.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.booking.common.enums.BookingStatus;

/**
 * DTO for booking details view
 */
public class BookingDetailsDto {
    
    private Integer bookingId;
    private String restaurantName;
    private String tableName; // For backward compatibility
    private List<String> tableNames; // For multiple tables
    private LocalDateTime bookingTime;
    private Integer guestCount;
    private BigDecimal depositAmount;
    private BigDecimal totalAmount;
    private BookingStatus status;
    private List<BookingDishDto> dishes;
    private List<BookingServiceDto> services;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public BookingDetailsDto() {}
    
    public BookingDetailsDto(Integer bookingId, String restaurantName, String tableName,
                            LocalDateTime bookingTime, Integer guestCount, BigDecimal depositAmount,
                            BigDecimal totalAmount, BookingStatus status) {
        this.bookingId = bookingId;
        this.restaurantName = restaurantName;
        this.tableName = tableName;
        this.bookingTime = bookingTime;
        this.guestCount = guestCount;
        this.depositAmount = depositAmount;
        this.totalAmount = totalAmount;
        this.status = status;
    }
    
    // Getters and Setters
    public Integer getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }
    
    public String getRestaurantName() {
        return restaurantName;
    }
    
    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    public List<String> getTableNames() {
        return tableNames;
    }

    public void setTableNames(List<String> tableNames) {
        this.tableNames = tableNames;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }
    
    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }
    
    public Integer getGuestCount() {
        return guestCount;
    }
    
    public void setGuestCount(Integer guestCount) {
        this.guestCount = guestCount;
    }
    
    public BigDecimal getDepositAmount() {
        return depositAmount;
    }
    
    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public BookingStatus getStatus() {
        return status;
    }
    
    public void setStatus(BookingStatus status) {
        this.status = status;
    }
    
    public List<BookingDishDto> getDishes() {
        return dishes;
    }
    
    public void setDishes(List<BookingDishDto> dishes) {
        this.dishes = dishes;
    }
    
    public List<BookingServiceDto> getServices() {
        return services;
    }
    
    public void setServices(List<BookingServiceDto> services) {
        this.services = services;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Helper methods
    public boolean hasDishes() {
        return dishes != null && !dishes.isEmpty();
    }
    
    public boolean hasServices() {
        return services != null && !services.isEmpty();
    }
    
    public boolean canBeEdited() {
        return status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED;
    }
    
    public boolean canBeCancelled() {
        return status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED;
    }
}
