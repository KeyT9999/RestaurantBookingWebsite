package com.example.booking.exception;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Exception thrown when booking conflicts are detected
 */
public class BookingConflictException extends RuntimeException {
    
    private final ConflictType conflictType;
    private final List<String> conflictDetails;
    private final LocalDateTime bookingTime;
    private final Integer tableId;
    
    public enum ConflictType {
        TABLE_OCCUPIED("Bàn đã được đặt"),
        CAPACITY_EXCEEDED("Số khách vượt quá sức chứa"),
        TIME_OVERLAP("Thời gian đặt bàn trùng lặp"),
        RESTAURANT_CLOSED("Nhà hàng đóng cửa"),
        INVALID_TIME_RANGE("Khung giờ không hợp lệ"),
        TABLE_NOT_AVAILABLE("Bàn không khả dụng"),
        TABLE_STATUS_UNAVAILABLE("Bàn không ở trạng thái khả dụng");
        
        private final String message;
        
        ConflictType(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    public BookingConflictException(ConflictType conflictType, String detail) {
        super(conflictType.getMessage() + ": " + detail);
        this.conflictType = conflictType;
        this.conflictDetails = List.of(detail);
        this.bookingTime = null;
        this.tableId = null;
    }
    
    public BookingConflictException(ConflictType conflictType, List<String> details) {
        super(conflictType.getMessage() + ": " + String.join(", ", details));
        this.conflictType = conflictType;
        this.conflictDetails = details;
        this.bookingTime = null;
        this.tableId = null;
    }
    
    public BookingConflictException(ConflictType conflictType, String detail, 
                                  LocalDateTime bookingTime, Integer tableId) {
        super(conflictType.getMessage() + ": " + detail);
        this.conflictType = conflictType;
        this.conflictDetails = List.of(detail);
        this.bookingTime = bookingTime;
        this.tableId = tableId;
    }
    
    public BookingConflictException(ConflictType conflictType, List<String> details, 
                                  LocalDateTime bookingTime, Integer tableId) {
        super(conflictType.getMessage() + ": " + String.join(", ", details));
        this.conflictType = conflictType;
        this.conflictDetails = details;
        this.bookingTime = bookingTime;
        this.tableId = tableId;
    }
    
    public ConflictType getConflictType() {
        return conflictType;
    }
    
    public List<String> getConflictDetails() {
        return conflictDetails;
    }
    
    public LocalDateTime getBookingTime() {
        return bookingTime;
    }
    
    public Integer getTableId() {
        return tableId;
    }
}
