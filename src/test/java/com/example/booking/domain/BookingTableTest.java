package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for BookingTable domain entity
 */
@DisplayName("BookingTable Domain Entity Tests")
public class BookingTableTest {

    private BookingTable bookingTable;
    private Booking booking;
    private RestaurantTable table;

    @BeforeEach
    void setUp() {
        bookingTable = new BookingTable();
        booking = new Booking();
        table = new RestaurantTable();
    }

    // ========== Constructor Tests ==========

    @Test
    @DisplayName("shouldCreateBookingTable_withDefaultConstructor")
    void shouldCreateBookingTable_withDefaultConstructor() {
        // When
        BookingTable bt = new BookingTable();

        // Then
        assertNotNull(bt);
        assertNotNull(bt.getAssignedAt());
    }

    @Test
    @DisplayName("shouldCreateBookingTable_withParameterizedConstructor")
    void shouldCreateBookingTable_withParameterizedConstructor() {
        // When
        BookingTable bt = new BookingTable(booking, table);

        // Then
        assertNotNull(bt);
        assertEquals(booking, bt.getBooking());
        assertEquals(table, bt.getTable());
        assertNotNull(bt.getAssignedAt());
    }

    // ========== Getter/Setter Tests ==========

    @Test
    @DisplayName("shouldSetAndGetBookingTableId")
    void shouldSetAndGetBookingTableId() {
        // Given
        Integer id = 1;

        // When
        bookingTable.setBookingTableId(id);

        // Then
        assertEquals(id, bookingTable.getBookingTableId());
    }

    @Test
    @DisplayName("shouldSetAndGetBooking")
    void shouldSetAndGetBooking() {
        // When
        bookingTable.setBooking(booking);

        // Then
        assertEquals(booking, bookingTable.getBooking());
    }

    @Test
    @DisplayName("shouldSetAndGetTable")
    void shouldSetAndGetTable() {
        // When
        bookingTable.setTable(table);

        // Then
        assertEquals(table, bookingTable.getTable());
    }

    @Test
    @DisplayName("shouldSetAndGetAssignedAt")
    void shouldSetAndGetAssignedAt() {
        // Given
        LocalDateTime assignedAt = LocalDateTime.now();

        // When
        bookingTable.setAssignedAt(assignedAt);

        // Then
        assertEquals(assignedAt, bookingTable.getAssignedAt());
    }
}
