package com.example.booking.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import com.example.booking.common.enums.BookingStatus;

@Entity
@Table(name = "booking")
public class Booking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Integer bookingId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @Column(name = "booking_time", nullable = false)
    @NotNull(message = "Thời gian đặt bàn không được để trống")
    private LocalDateTime bookingTime;
    
    @Column(name = "number_of_guests", nullable = false)
    @Min(value = 1, message = "Số khách tối thiểu là 1")
    @Max(value = 20, message = "Số khách tối đa là 20")
    private Integer numberOfGuests;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status = BookingStatus.PENDING;
    
    @Column(name = "deposit_amount", precision = 18, scale = 2, nullable = false)
    @DecimalMin(value = "0.0", message = "Số tiền đặt cọc không được âm")
    private BigDecimal depositAmount = BigDecimal.ZERO;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookingTable> bookingTables;
    
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookingDish> bookingDishes;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments;
    
    // Constructors
    public Booking() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Booking(Customer customer, LocalDateTime bookingTime, Integer numberOfGuests,
            BigDecimal depositAmount) {
        this();
        this.customer = customer;
        this.bookingTime = bookingTime;
        this.numberOfGuests = numberOfGuests;
        this.depositAmount = depositAmount != null ? depositAmount : BigDecimal.ZERO;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Integer getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }
    
    public Customer getCustomer() {
        return customer;
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    public Integer getNumberOfGuests() {
        return numberOfGuests;
    }
    
    public void setNumberOfGuests(Integer numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
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
    
    public List<BookingTable> getBookingTables() {
        return bookingTables;
    }
    
    public void setBookingTables(List<BookingTable> bookingTables) {
        this.bookingTables = bookingTables;
    }
    
    public List<BookingDish> getBookingDishes() {
        return bookingDishes;
    }
    
    public void setBookingDishes(List<BookingDish> bookingDishes) {
        this.bookingDishes = bookingDishes;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
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