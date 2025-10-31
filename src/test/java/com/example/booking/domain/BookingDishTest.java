package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for BookingDish domain entity
 */
@DisplayName("BookingDish Domain Entity Tests")
public class BookingDishTest {

    private BookingDish bookingDish;
    private Booking booking;
    private Dish dish;

    @BeforeEach
    void setUp() {
        bookingDish = new BookingDish();
        booking = new Booking();
        dish = new Dish();
    }

    // ========== Constructor Tests ==========

    @Test
    @DisplayName("shouldCreateBookingDish_withDefaultConstructor")
    void shouldCreateBookingDish_withDefaultConstructor() {
        // When
        BookingDish dish = new BookingDish();

        // Then
        assertNotNull(dish);
        assertEquals(1, dish.getQuantity());
    }

    @Test
    @DisplayName("shouldCreateBookingDish_withParameterizedConstructor")
    void shouldCreateBookingDish_withParameterizedConstructor() {
        // Given
        Integer quantity = 2;
        BigDecimal price = new BigDecimal("100.00");

        // When
        BookingDish dish = new BookingDish(booking, this.dish, quantity, price);

        // Then
        assertNotNull(dish);
        assertEquals(booking, dish.getBooking());
        assertEquals(this.dish, dish.getDish());
        assertEquals(quantity, dish.getQuantity());
        assertEquals(price, dish.getPrice());
    }

    @Test
    @DisplayName("shouldCreateBookingDish_withNullQuantity")
    void shouldCreateBookingDish_withNullQuantity() {
        // Given
        BigDecimal price = new BigDecimal("100.00");

        // When
        BookingDish dish = new BookingDish(booking, this.dish, null, price);

        // Then
        assertNotNull(dish);
        assertEquals(1, dish.getQuantity());
    }

    // ========== Getter/Setter Tests ==========

    @Test
    @DisplayName("shouldSetAndGetBookingDishId")
    void shouldSetAndGetBookingDishId() {
        // Given
        Integer id = 1;

        // When
        bookingDish.setBookingDishId(id);

        // Then
        assertEquals(id, bookingDish.getBookingDishId());
    }

    @Test
    @DisplayName("shouldSetAndGetBooking")
    void shouldSetAndGetBooking() {
        // When
        bookingDish.setBooking(booking);

        // Then
        assertEquals(booking, bookingDish.getBooking());
    }

    @Test
    @DisplayName("shouldSetAndGetDish")
    void shouldSetAndGetDish() {
        // When
        bookingDish.setDish(dish);

        // Then
        assertEquals(dish, bookingDish.getDish());
    }

    @Test
    @DisplayName("shouldSetAndGetQuantity")
    void shouldSetAndGetQuantity() {
        // Given
        Integer quantity = 3;

        // When
        bookingDish.setQuantity(quantity);

        // Then
        assertEquals(quantity, bookingDish.getQuantity());
    }

    @Test
    @DisplayName("shouldSetAndGetPrice")
    void shouldSetAndGetPrice() {
        // Given
        BigDecimal price = new BigDecimal("150.50");

        // When
        bookingDish.setPrice(price);

        // Then
        assertEquals(price, bookingDish.getPrice());
    }

    // ========== Helper Method Tests ==========

    @Test
    @DisplayName("shouldCalculateTotalPrice")
    void shouldCalculateTotalPrice() {
        // Given
        bookingDish.setPrice(new BigDecimal("100.00"));
        bookingDish.setQuantity(3);

        // When
        BigDecimal totalPrice = bookingDish.getTotalPrice();

        // Then
        assertEquals(new BigDecimal("300.00"), totalPrice);
    }

    @Test
    @DisplayName("shouldCalculateTotalPrice_singleItem")
    void shouldCalculateTotalPrice_singleItem() {
        // Given
        bookingDish.setPrice(new BigDecimal("50.00"));
        bookingDish.setQuantity(1);

        // When
        BigDecimal totalPrice = bookingDish.getTotalPrice();

        // Then
        assertEquals(new BigDecimal("50.00"), totalPrice);
    }
}
