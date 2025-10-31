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

import java.util.Optional;

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

    // ========== setTableToMaintenance() Tests ==========

    @Test
    @DisplayName("shouldSetTableToMaintenance_successfully")
    void shouldSetTableToMaintenance_successfully() {
        // Given
        Integer tableId = 1;
        table.setTableId(tableId);
        table.setStatus(TableStatus.AVAILABLE);
        
        when(restaurantTableRepository.findById(tableId)).thenReturn(Optional.of(table));
        when(restaurantTableRepository.save(table)).thenReturn(table);

        // When
        tableStatusService.setTableToMaintenance(tableId);

        // Then
        assertEquals(TableStatus.MAINTENANCE, table.getStatus());
        verify(restaurantTableRepository).save(table);
    }

    @Test
    @DisplayName("shouldThrowException_whenTableNotFoundForMaintenance")
    void shouldThrowException_whenTableNotFoundForMaintenance() {
        // Given
        Integer tableId = 999;
        when(restaurantTableRepository.findById(tableId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tableStatusService.setTableToMaintenance(tableId);
        });

        assertTrue(exception.getMessage().contains("Table not found"));
    }

    // ========== setTableToAvailable() Tests ==========

    @Test
    @DisplayName("shouldSetTableToAvailable_successfully")
    void shouldSetTableToAvailable_successfully() {
        // Given
        Integer tableId = 1;
        table.setTableId(tableId);
        table.setStatus(TableStatus.MAINTENANCE);
        
        when(restaurantTableRepository.findById(tableId)).thenReturn(Optional.of(table));
        when(restaurantTableRepository.save(table)).thenReturn(table);

        // When
        tableStatusService.setTableToAvailable(tableId);

        // Then
        assertEquals(TableStatus.AVAILABLE, table.getStatus());
        verify(restaurantTableRepository).save(table);
    }

    @Test
    @DisplayName("shouldThrowException_whenTableNotFoundForAvailable")
    void shouldThrowException_whenTableNotFoundForAvailable() {
        // Given
        Integer tableId = 999;
        when(restaurantTableRepository.findById(tableId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tableStatusService.setTableToAvailable(tableId);
        });

        assertTrue(exception.getMessage().contains("Table not found"));
    }

    // ========== checkInCustomer() Tests ==========

    @Test
    @DisplayName("shouldCheckInCustomer_successfully")
    void shouldCheckInCustomer_successfully() {
        // Given
        Integer bookingId = 1;
        booking.setBookingId(bookingId);
        booking.setStatus(BookingStatus.CONFIRMED);
        table.setStatus(TableStatus.RESERVED);
        
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingTableRepository.findByBooking(booking)).thenReturn(Arrays.asList(bookingTable));
        when(restaurantTableRepository.save(table)).thenReturn(table);

        // When
        tableStatusService.checkInCustomer(bookingId);

        // Then
        assertEquals(TableStatus.OCCUPIED, table.getStatus());
        verify(restaurantTableRepository).save(table);
    }

    @Test
    @DisplayName("shouldThrowException_whenBookingNotFoundForCheckIn")
    void shouldThrowException_whenBookingNotFoundForCheckIn() {
        // Given
        Integer bookingId = 999;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tableStatusService.checkInCustomer(bookingId);
        });

        assertTrue(exception.getMessage().contains("Booking not found"));
    }

    // ========== checkOutCustomer() Tests ==========

    @Test
    @DisplayName("shouldCheckOutCustomer_successfully")
    void shouldCheckOutCustomer_successfully() {
        // Given
        Integer bookingId = 1;
        booking.setBookingId(bookingId);
        booking.setStatus(BookingStatus.CONFIRMED);
        table.setStatus(TableStatus.OCCUPIED);
        
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingTableRepository.findByBooking(booking)).thenReturn(Arrays.asList(bookingTable));
        when(restaurantTableRepository.save(table)).thenReturn(table);

        // When
        tableStatusService.checkOutCustomer(bookingId);

        // Then
        assertEquals(TableStatus.CLEANING, table.getStatus());
        verify(restaurantTableRepository).save(table);
    }

    @Test
    @DisplayName("shouldThrowException_whenBookingNotFoundForCheckOut")
    void shouldThrowException_whenBookingNotFoundForCheckOut() {
        // Given
        Integer bookingId = 999;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tableStatusService.checkOutCustomer(bookingId);
        });

        assertTrue(exception.getMessage().contains("Booking not found"));
    }
}


