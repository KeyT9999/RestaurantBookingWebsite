package com.example.booking.domain;

import java.math.BigDecimal;

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
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "booking_dish")
public class BookingDish {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_dish_id")
    private Integer bookingDishId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id", nullable = false)
    private Dish dish;
    
    @Column(name = "quantity", nullable = false)
    @Min(value = 1, message = "Số lượng tối thiểu là 1")
    private Integer quantity = 1;
    
    @Column(name = "price", nullable = false, precision = 18, scale = 2)
    @DecimalMin(value = "0.0", message = "Giá không được âm")
    private BigDecimal price;
    
    // Constructors
    public BookingDish() {}
    
    public BookingDish(Booking booking, Dish dish, Integer quantity, BigDecimal price) {
        this.booking = booking;
        this.dish = dish;
        this.quantity = quantity != null ? quantity : 1;
        this.price = price;
    }

    // Getters and Setters
    public Integer getBookingDishId() {
        return bookingDishId;
    }
    
    public void setBookingDishId(Integer bookingDishId) {
        this.bookingDishId = bookingDishId;
    }
    
    public Booking getBooking() {
        return booking;
    }
    
    public void setBooking(Booking booking) {
        this.booking = booking;
    }
    
    public Dish getDish() {
        return dish;
    }
    
    public void setDish(Dish dish) {
        this.dish = dish;
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
    
    // Helper method
    public BigDecimal getTotalPrice() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
