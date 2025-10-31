package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.booking.common.enums.BookingStatus;
import com.example.booking.common.enums.TableStatus;
import com.example.booking.domain.Booking;
import com.example.booking.domain.BookingTable;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.BookingTableRepository;
import com.example.booking.repository.RestaurantTableRepository;

/**
 * Unit tests for TableStatusManagementService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TableStatusManagementService Tests")
public class TableStatusManagementServiceTest {

    @Mock
    private BookingTableRepository bookingTableRepository;

    @Mock
    private RestaurantTableRepository restaurantTableRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private TableStatusManagementService tableStatusService;

    private RestaurantTable table;
    private Booking booking;
    private BookingTable bookingTable;

    @BeforeEach
    void setUp() {
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);

        table = new RestaurantTable();
        table.setTableId(1);
        table.setRestaurant(restaurant);
        table.setTableName("Table 1");
        table.setStatus(TableStatus.RESERVED);

        booking = new Booking();
        booking.setBookingId(1);
        booking.setStatus(BookingStatus.PENDING);
        booking.setBookingTime(LocalDateTime.now().minusMinutes(20));

        bookingTable = new BookingTable();
        bookingTable.setBooking(booking);
        bookingTable.setTable(table);
    }

    // ========== updateTableStatuses() Tests ==========

    @Test
    @DisplayName("shouldUpdateTableStatuses_successfully")
    void shouldUpdateTableStatuses_successfully() {
        // Given
        when(bookingRepository.findNoShowBookings(any(LocalDateTime.class))).thenReturn(new ArrayList<>());
        when(restaurantTableRepository.findByStatus(TableStatus.CLEANING)).thenReturn(new ArrayList<>());
        when(bookingRepository.findUpcomingBookings(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(new ArrayList<>());

        // When
        tableStatusService.updateTableStatuses();

        // Then
        verify(bookingRepository, times(1)).findNoShowBookings(any(LocalDateTime.class));
    }

    // ========== handleNoShowBookings() Tests (via updateTableStatuses) ==========

    @Test
    @DisplayName("shouldHandleNoShowBookings_successfully")
    void shouldHandleNoShowBookings_successfully() {
        // Given
        List<Booking> noShowBookings = Arrays.asList(booking);
        List<BookingTable> bookingTables = Arrays.asList(bookingTable);

        when(bookingRepository.findNoShowBookings(any(LocalDateTime.class))).thenReturn(noShowBookings);
        when(bookingTableRepository.findByBooking(booking)).thenReturn(bookingTables);
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(restaurantTableRepository.save(table)).thenReturn(table);
        when(restaurantTableRepository.findByStatus(TableStatus.CLEANING)).thenReturn(new ArrayList<>());
        when(bookingRepository.findUpcomingBookings(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(new ArrayList<>());

        // When
        tableStatusService.updateTableStatuses();

        // Then
        verify(bookingRepository, times(1)).save(booking);
        verify(restaurantTableRepository, times(1)).save(table);
    }

    // ========== handleCleaningTables() Tests (via updateTableStatuses) ==========

    @Test
    @DisplayName("shouldHandleCleaningTables_successfully")
    void shouldHandleCleaningTables_successfully() {
        // Given
        table.setStatus(TableStatus.CLEANING);
        List<RestaurantTable> cleaningTables = Arrays.asList(table);

        when(bookingRepository.findNoShowBookings(any(LocalDateTime.class))).thenReturn(new ArrayList<>());
        when(restaurantTableRepository.findByStatus(TableStatus.CLEANING)).thenReturn(cleaningTables);
        when(restaurantTableRepository.save(table)).thenReturn(table);
        when(bookingRepository.findUpcomingBookings(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(new ArrayList<>());

        // When
        tableStatusService.updateTableStatuses();

        // Then
        verify(restaurantTableRepository, atLeastOnce()).save(table);
    }

    // ========== handleUpcomingBookings() Tests (via updateTableStatuses) ==========

    @Test
    @DisplayName("shouldHandleUpcomingBookings_successfully")
    void shouldHandleUpcomingBookings_successfully() {
        // Given
        Booking upcomingBooking = new Booking();
        upcomingBooking.setBookingId(2);
        upcomingBooking.setBookingTime(LocalDateTime.now().plusMinutes(10));
        List<Booking> upcomingBookings = Arrays.asList(upcomingBooking);

        when(bookingRepository.findNoShowBookings(any(LocalDateTime.class))).thenReturn(new ArrayList<>());
        when(restaurantTableRepository.findByStatus(TableStatus.CLEANING)).thenReturn(new ArrayList<>());
        when(bookingRepository.findUpcomingBookings(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(upcomingBookings);
        when(bookingTableRepository.findByBooking(upcomingBooking)).thenReturn(Arrays.asList(bookingTable));
        when(restaurantTableRepository.save(table)).thenReturn(table);

        // When
        tableStatusService.updateTableStatuses();

        // Then
        verify(restaurantTableRepository, atLeastOnce()).save(any(RestaurantTable.class));
    }
}


