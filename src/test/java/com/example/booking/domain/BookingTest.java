package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.booking.common.enums.BookingStatus;

/**
 * Unit tests for Booking domain entity
 */
@DisplayName("Booking Domain Entity Tests")
public class BookingTest {

    private Booking booking;

    @BeforeEach
    void setUp() {
        booking = new Booking();
        booking.setBookingId(1);
        booking.setBookingTime(LocalDateTime.now().plusHours(2));
        booking.setNumberOfGuests(4);
        booking.setDepositAmount(new BigDecimal("500000"));
        booking.setStatus(BookingStatus.PENDING);
    }

    // ========== Basic Getters/Setters Tests ==========

    @Test
    @DisplayName("shouldSetAndGetBookingId_successfully")
    void shouldSetAndGetBookingId_successfully() {
        // Given
        Integer bookingId = 123;

        // When
        booking.setBookingId(bookingId);

        // Then
        assertEquals(bookingId, booking.getBookingId());
    }

    @Test
    @DisplayName("shouldSetAndGetStatus_successfully")
    void shouldSetAndGetStatus_successfully() {
        // Given
        BookingStatus status = BookingStatus.CONFIRMED;

        // When
        booking.setStatus(status);

        // Then
        assertEquals(status, booking.getStatus());
    }

    @Test
    @DisplayName("shouldSetAndGetDepositAmount_successfully")
    void shouldSetAndGetDepositAmount_successfully() {
        // Given
        BigDecimal amount = new BigDecimal("1000000");

        // When
        booking.setDepositAmount(amount);

        // Then
        assertEquals(amount, booking.getDepositAmount());
    }

    @Test
    @DisplayName("shouldSetAndGetBookingTime_successfully")
    void shouldSetAndGetBookingTime_successfully() {
        // Given
        LocalDateTime bookingTime = LocalDateTime.now().plusDays(1);

        // When
        booking.setBookingTime(bookingTime);

        // Then
        assertEquals(bookingTime, booking.getBookingTime());
    }

    // ========== Validation Tests ==========

    @Test
    @DisplayName("shouldAcceptValidNumberOfGuests")
    void shouldAcceptValidNumberOfGuests() {
        // Given
        int guests = 4;

        // When
        booking.setNumberOfGuests(guests);

        // Then
        assertEquals(guests, booking.getNumberOfGuests());
    }
}

