package com.example.booking.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "bookings")
public class Booking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;
    
    @Column(name = "table_id")
    private UUID tableId;
    
    @Column(name = "guest_count", nullable = false)
    @Min(value = 1, message = "Số khách tối thiểu là 1")
    @Max(value = 20, message = "Số khách tối đa là 20")
    private Integer guestCount;
    
    @Column(name = "booking_time", nullable = false)
    @NotNull(message = "Thời gian đặt bàn không được để trống")
    private LocalDateTime bookingTime;
    
    @Column(name = "deposit_amount", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Số tiền đặt cọc không được âm")
    private BigDecimal depositAmount = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;
    
    @Column(length = 500)
    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    private String note;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // JPA relationships (optional for display)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", insertable = false, updatable = false)
    private Restaurant restaurant;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", insertable = false, updatable = false)
    private DiningTable table;
    
    // Constructors
    public Booking() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Booking(UUID customerId, UUID restaurantId, UUID tableId, Integer guestCount, 
                   LocalDateTime bookingTime, BigDecimal depositAmount, String note) {
        this();
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.tableId = tableId;
        this.guestCount = guestCount;
        this.bookingTime = bookingTime;
        this.depositAmount = depositAmount != null ? depositAmount : BigDecimal.ZERO;
        this.note = note;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }
    
    public UUID getRestaurantId() {
        return restaurantId;
    }
    
    public void setRestaurantId(UUID restaurantId) {
        this.restaurantId = restaurantId;
    }
    
    public UUID getTableId() {
        return tableId;
    }
    
    public void setTableId(UUID tableId) {
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
    
    public BookingStatus getStatus() {
        return status;
    }
    
    public void setStatus(BookingStatus status) {
        this.status = status;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
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
    
    public Restaurant getRestaurant() {
        return restaurant;
    }
    
    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }
    
    public DiningTable getTable() {
        return table;
    }
    
    public void setTable(DiningTable table) {
        this.table = table;
    }
    
    // Helper methods
    public boolean hasDeposit() {
        return depositAmount != null && depositAmount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public boolean canBeEdited() {
        return status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED;
    }
    
    public boolean canBeCancelled() {
        return status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED;
    }
} 