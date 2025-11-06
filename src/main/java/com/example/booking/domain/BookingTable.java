package com.example.booking.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;

@Entity
@Table(name = "booking_table")
public class BookingTable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_table_id")
    private Integer bookingTableId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private RestaurantTable table;
    
    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;
    
    @Column(name = "table_fee", precision = 18, scale = 2, nullable = false)
    @DecimalMin(value = "0.0", message = "Phí bàn không được âm")
    private BigDecimal tableFee = BigDecimal.ZERO;
    
    // Constructors
    public BookingTable() {
        this.assignedAt = LocalDateTime.now();
        this.tableFee = BigDecimal.ZERO;
    }
    
    public BookingTable(Booking booking, RestaurantTable table) {
        this();
        this.booking = booking;
        this.table = table;
        // Snapshot phí bàn tại thời điểm tạo booking
        this.tableFee = table.getDepositAmount() != null ? 
            table.getDepositAmount() : BigDecimal.ZERO;
    }
    
    // Getters and Setters
    public Integer getBookingTableId() {
        return bookingTableId;
    }
    
    public void setBookingTableId(Integer bookingTableId) {
        this.bookingTableId = bookingTableId;
    }
    
    public Booking getBooking() {
        return booking;
    }
    
    public void setBooking(Booking booking) {
        this.booking = booking;
    }
    
    public RestaurantTable getTable() {
        return table;
    }
    
    public void setTable(RestaurantTable table) {
        this.table = table;
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
