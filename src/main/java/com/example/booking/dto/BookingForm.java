package com.example.booking.dto;

import com.example.booking.validation.FuturePlus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingForm {
    
    @NotNull(message = "Vui lòng chọn nhà hàng")
    private Integer restaurantId;
    
    private Integer tableId; // Optional
    
    @NotNull(message = "Số khách không được để trống")
    @Min(value = 1, message = "Số khách tối thiểu là 1")
    @Max(value = 20, message = "Số khách tối đa là 20")
    private Integer guestCount;
    
    @NotNull(message = "Thời gian đặt bàn không được để trống")
    @FuturePlus(minutes = 30, message = "Thời gian đặt bàn phải từ 30 phút trở lên so với hiện tại")
    private LocalDateTime bookingTime;
    
    @DecimalMin(value = "0.0", message = "Số tiền đặt cọc không được âm")
    private BigDecimal depositAmount = BigDecimal.ZERO;
    
    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    private String note;
    
    // Constructors
    public BookingForm() {}
    
    public BookingForm(Integer restaurantId, Integer tableId, Integer guestCount, 
                       LocalDateTime bookingTime, BigDecimal depositAmount, String note) {
        this.restaurantId = restaurantId;
        this.tableId = tableId;
        this.guestCount = guestCount;
        this.bookingTime = bookingTime;
        this.depositAmount = depositAmount != null ? depositAmount : BigDecimal.ZERO;
        this.note = note;
    }
    
    // Getters and Setters
    public Integer getRestaurantId() {
        return restaurantId;
    }
    
    public void setRestaurantId(Integer restaurantId) {
        this.restaurantId = restaurantId;
    }
    
    public Integer getTableId() {
        return tableId;
    }
    
    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }
    
    public Integer getGuestCount() {
        return guestCount;
    }
    
    public void setGuestCount(Integer guestCount) {
        this.guestCount = guestCount;
    }
    
    public LocalDateTime getBookingTime() {
        return bookingTime;
    }
    
    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }
    
    public BigDecimal getDepositAmount() {
        return depositAmount;
    }
    
    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
} 