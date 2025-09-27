package com.example.booking.domain;

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
    
    // Constructors
    public BookingTable() {
        this.assignedAt = LocalDateTime.now();
    }
    
    public BookingTable(Booking booking, RestaurantTable table) {
        this();
        this.booking = booking;
        this.table = table;
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
}
