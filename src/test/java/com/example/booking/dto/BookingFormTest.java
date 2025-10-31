package com.example.booking.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for BookingForm DTO
 */
@DisplayName("BookingForm DTO Tests")
public class BookingFormTest {

    private BookingForm bookingForm;

    @BeforeEach
    void setUp() {
        bookingForm = new BookingForm();
    }

    // ========== Basic Getters/Setters Tests ==========

    @Test
    @DisplayName("shouldSetAndGetRestaurantId_successfully")
    void shouldSetAndGetRestaurantId_successfully() {
        // Given
        Integer restaurantId = 1;

        // When
        bookingForm.setRestaurantId(restaurantId);

        // Then
        assertEquals(restaurantId, bookingForm.getRestaurantId());
    }

    @Test
    @DisplayName("shouldSetAndGetBookingTime_successfully")
    void shouldSetAndGetBookingTime_successfully() {
        // Given
        LocalDateTime bookingTime = LocalDateTime.now().plusHours(2);

        // When
        bookingForm.setBookingTime(bookingTime);

        // Then
        assertEquals(bookingTime, bookingForm.getBookingTime());
    }

    @Test
    @DisplayName("shouldSetAndGetGuestCount_successfully")
    void shouldSetAndGetGuestCount_successfully() {
        // Given
        Integer guests = 4;

        // When
        bookingForm.setGuestCount(guests);

        // Then
        assertEquals(guests, bookingForm.getGuestCount());
    }
}

