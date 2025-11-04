package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for BookingService domain entity
 */
@DisplayName("BookingService Domain Entity Tests")
public class BookingServiceTest {

    private BookingService bookingService;
    private Booking booking;
    private RestaurantService service;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService();
        booking = new Booking();
        service = new RestaurantService();
    }

    // ========== Constructor Tests ==========

    @Test
    @DisplayName("shouldCreateBookingService_withDefaultConstructor")
    void shouldCreateBookingService_withDefaultConstructor() {
        // When
        BookingService bs = new BookingService();

        // Then
        assertNotNull(bs);
        assertNotNull(bs.getCreatedAt());
        assertEquals(1, bs.getQuantity());
    }

    @Test
    @DisplayName("shouldCreateBookingService_withParameterizedConstructor")
    void shouldCreateBookingService_withParameterizedConstructor() {
        // Given
        Integer quantity = 2;
        BigDecimal price = new BigDecimal("200.00");

        // When
        BookingService bs = new BookingService(booking, service, quantity, price);

        // Then
        assertNotNull(bs);
        assertEquals(booking, bs.getBooking());
        assertEquals(service, bs.getService());
        assertEquals(quantity, bs.getQuantity());
        assertEquals(price, bs.getPrice());
    }

    @Test
    @DisplayName("shouldCreateBookingService_withNullQuantity")
    void shouldCreateBookingService_withNullQuantity() {
        // Given
        BigDecimal price = new BigDecimal("200.00");

        // When
        BookingService bs = new BookingService(booking, service, null, price);

        // Then
        assertNotNull(bs);
        assertEquals(1, bs.getQuantity());
    }

    // ========== Getter/Setter Tests ==========

    @Test
    @DisplayName("shouldSetAndGetBookingServiceId")
    void shouldSetAndGetBookingServiceId() {
        // Given
        Integer id = 1;

        // When
        bookingService.setBookingServiceId(id);

        // Then
        assertEquals(id, bookingService.getBookingServiceId());
    }

    @Test
    @DisplayName("shouldSetAndGetBooking")
    void shouldSetAndGetBooking() {
        // When
        bookingService.setBooking(booking);

        // Then
        assertEquals(booking, bookingService.getBooking());
    }

    @Test
    @DisplayName("shouldSetAndGetService")
    void shouldSetAndGetService() {
        // When
        bookingService.setService(service);

        // Then
        assertEquals(service, bookingService.getService());
    }

    @Test
    @DisplayName("shouldSetAndGetQuantity")
    void shouldSetAndGetQuantity() {
        // Given
        Integer quantity = 4;

        // When
        bookingService.setQuantity(quantity);

        // Then
        assertEquals(quantity, bookingService.getQuantity());
    }

    @Test
    @DisplayName("shouldSetAndGetPrice")
    void shouldSetAndGetPrice() {
        // Given
        BigDecimal price = new BigDecimal("250.75");

        // When
        bookingService.setPrice(price);

        // Then
        assertEquals(price, bookingService.getPrice());
    }

    @Test
    @DisplayName("shouldSetAndGetCreatedAt")
    void shouldSetAndGetCreatedAt() {
        // Given
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        bookingService.setCreatedAt(createdAt);

        // Then
        assertEquals(createdAt, bookingService.getCreatedAt());
    }

    @Test
    @DisplayName("shouldSetAndGetUpdatedAt")
    void shouldSetAndGetUpdatedAt() {
        // Given
        LocalDateTime updatedAt = LocalDateTime.now();

        // When
        bookingService.setUpdatedAt(updatedAt);

        // Then
        assertEquals(updatedAt, bookingService.getUpdatedAt());
    }

    // ========== Helper Method Tests ==========

    @Test
    @DisplayName("shouldCalculateTotalPrice")
    void shouldCalculateTotalPrice() {
        // Given
        bookingService.setPrice(new BigDecimal("150.00"));
        bookingService.setQuantity(2);

        // When
        BigDecimal totalPrice = bookingService.getTotalPrice();

        // Then
        assertEquals(new BigDecimal("300.00"), totalPrice);
    }

    @Test
    @DisplayName("shouldCalculateTotalPrice_singleItem")
    void shouldCalculateTotalPrice_singleItem() {
        // Given
        bookingService.setPrice(new BigDecimal("100.00"));
        bookingService.setQuantity(1);

        // When
        BigDecimal totalPrice = bookingService.getTotalPrice();

        // Then
        assertEquals(new BigDecimal("100.00"), totalPrice);
    }
}
