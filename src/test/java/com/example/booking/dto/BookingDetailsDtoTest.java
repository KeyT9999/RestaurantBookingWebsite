package com.example.booking.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.booking.common.enums.BookingStatus;

/**
 * Unit tests for BookingDetailsDto
 */
@DisplayName("BookingDetailsDto Tests")
public class BookingDetailsDtoTest {

    private BookingDetailsDto bookingDetailsDto;

    @BeforeEach
    void setUp() {
        bookingDetailsDto = new BookingDetailsDto();
    }

    @Test
    @DisplayName("shouldSetAndGetBookingId_successfully")
    void shouldSetAndGetBookingId_successfully() {
        // Given
        Integer bookingId = 1;

        // When
        bookingDetailsDto.setBookingId(bookingId);

        // Then
        assertEquals(bookingId, bookingDetailsDto.getBookingId());
    }

    @Test
    @DisplayName("shouldSetAndGetBookingTime_successfully")
    void shouldSetAndGetBookingTime_successfully() {
        // Given
        LocalDateTime bookingTime = LocalDateTime.now().plusHours(2);

        // When
        bookingDetailsDto.setBookingTime(bookingTime);

        // Then
        assertEquals(bookingTime, bookingDetailsDto.getBookingTime());
    }

    @Test
    @DisplayName("shouldSetAndGetStatus_successfully")
    void shouldSetAndGetStatus_successfully() {
        // Given
        BookingStatus status = BookingStatus.CONFIRMED;

        // When
        bookingDetailsDto.setStatus(status);

        // Then
        assertEquals(status, bookingDetailsDto.getStatus());
    }

    @Test
    @DisplayName("shouldSetAndGetTotalAmount_successfully")
    void shouldSetAndGetTotalAmount_successfully() {
        // Given
        BigDecimal amount = new BigDecimal("1000000");

        // When
        bookingDetailsDto.setTotalAmount(amount);

        // Then
        assertEquals(amount, bookingDetailsDto.getTotalAmount());
    }
}

