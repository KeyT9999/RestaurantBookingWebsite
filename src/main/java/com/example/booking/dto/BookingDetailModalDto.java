package com.example.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.booking.common.enums.BookingStatus;

/**
 * DTO for booking detail modal API response
 */
public class BookingDetailModalDto {
    
    private Integer bookingId;
    private BookingStatus status;
    private LocalDateTime bookingTime;
    private Integer numberOfGuests;
    private String note;
    private BigDecimal depositAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Customer information
    private CustomerInfo customer;
    
    // Restaurant information
    private RestaurantInfo restaurant;
    
    // Booking tables
    private List<BookingTableInfo> bookingTables;
    
    // Dishes and services
    private List<BookingDishDto> bookingDishes;
    private List<BookingServiceDto> bookingServices;
    
    // Internal notes and communication history
    private List<InternalNoteDto> internalNotes;
    private List<CommunicationHistoryDto> communicationHistory;
    
    // Constructors
    public BookingDetailModalDto() {}
    
    // Getters and Setters
    public Integer getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }
    
    public BookingStatus getStatus() {
        return status;
    }
    
    public void setStatus(BookingStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getBookingTime() {
        return bookingTime;
    }
    
    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }
    
    public Integer getNumberOfGuests() {
        return numberOfGuests;
    }
    
    public void setNumberOfGuests(Integer numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
    
    public BigDecimal getDepositAmount() {
        return depositAmount;
    }
    
    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
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
    
    public CustomerInfo getCustomer() {
        return customer;
    }
    
    public void setCustomer(CustomerInfo customer) {
        this.customer = customer;
    }
    
    public RestaurantInfo getRestaurant() {
        return restaurant;
    }
    
    public void setRestaurant(RestaurantInfo restaurant) {
        this.restaurant = restaurant;
    }
    
    public List<BookingTableInfo> getBookingTables() {
        return bookingTables;
    }
    
    public void setBookingTables(List<BookingTableInfo> bookingTables) {
        this.bookingTables = bookingTables;
    }
    
    public List<BookingDishDto> getBookingDishes() {
        return bookingDishes;
    }
    
    public void setBookingDishes(List<BookingDishDto> bookingDishes) {
        this.bookingDishes = bookingDishes;
    }
    
    public List<BookingServiceDto> getBookingServices() {
        return bookingServices;
    }
    
    public void setBookingServices(List<BookingServiceDto> bookingServices) {
        this.bookingServices = bookingServices;
    }
    
    public List<InternalNoteDto> getInternalNotes() {
        return internalNotes;
    }
    
    public void setInternalNotes(List<InternalNoteDto> internalNotes) {
        this.internalNotes = internalNotes;
    }
    
    public List<CommunicationHistoryDto> getCommunicationHistory() {
        return communicationHistory;
    }
    
    public void setCommunicationHistory(List<CommunicationHistoryDto> communicationHistory) {
        this.communicationHistory = communicationHistory;
    }
    
    // Inner classes for nested data
    public static class CustomerInfo {
        private String customerId; // Changed to String for UUID
        private String fullName;
        private String phoneNumber;
        private String email;
        private String address;
        
        public CustomerInfo() {}
        
        public String getCustomerId() {
            return customerId;
        }
        
        public void setCustomerId(String customerId) {
            this.customerId = customerId;
        }
        
        public String getFullName() {
            return fullName;
        }
        
        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
        
        public String getPhoneNumber() {
            return phoneNumber;
        }
        
        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getAddress() {
            return address;
        }
        
        public void setAddress(String address) {
            this.address = address;
        }
    }
    
    public static class RestaurantInfo {
        private Integer restaurantId;
        private String restaurantName;
        
        public RestaurantInfo() {}
        
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
    }
    
    public static class BookingTableInfo {
        private Integer bookingTableId;
        private String tableName;
        private LocalDateTime assignedAt;
        private BigDecimal tableFee;
        
        public BookingTableInfo() {}
        
        public Integer getBookingTableId() {
            return bookingTableId;
        }
        
        public void setBookingTableId(Integer bookingTableId) {
            this.bookingTableId = bookingTableId;
        }
        
        public String getTableName() {
            return tableName;
        }
        
        public void setTableName(String tableName) {
            this.tableName = tableName;
        }
        
        public LocalDateTime getAssignedAt() {
            return assignedAt;
        }
        
        public void setAssignedAt(LocalDateTime assignedAt) {
            this.assignedAt = assignedAt;
        }
        
        public BigDecimal getTableFee() {
            return tableFee;
        }
        
        public void setTableFee(BigDecimal tableFee) {
            this.tableFee = tableFee != null ? tableFee : BigDecimal.ZERO;
        }
    }
}
