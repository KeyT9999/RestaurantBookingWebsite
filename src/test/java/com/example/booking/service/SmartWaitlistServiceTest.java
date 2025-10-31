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

import com.example.booking.domain.Booking;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.dto.AvailabilityCheckResponse;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.RestaurantTableRepository;

/**
 * Unit tests for SmartWaitlistService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SmartWaitlistService Tests")
public class SmartWaitlistServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RestaurantTableRepository restaurantTableRepository;

    @InjectMocks
    private SmartWaitlistService smartWaitlistService;

    private Integer restaurantId;
    private LocalDateTime bookingTime;
    private Integer guestCount;
    private RestaurantTable table;
    private Booking booking;

    @BeforeEach
    void setUp() {
        restaurantId = 1;
        bookingTime = LocalDateTime.now().plusHours(2);
        guestCount = 4;

        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(restaurantId);

        table = new RestaurantTable();
        table.setTableId(1);
        table.setRestaurant(restaurant);
        table.setTableName("Table 1");
        table.setCapacity(6);

        booking = new Booking();
        booking.setBookingId(1);
        booking.setBookingTime(bookingTime);
        booking.setNumberOfGuests(guestCount);
    }

    // ========== checkSpecificTables() Tests ==========

    @Test
    @DisplayName("shouldCheckSpecificTables_noConflicts")
    void shouldCheckSpecificTables_noConflicts() {
        // Given
        when(restaurantTableRepository.findById(1)).thenReturn(java.util.Optional.of(table));
        when(bookingRepository.findTableConflictsInTimeRange(eq(1), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(new ArrayList<>());

        // When
        AvailabilityCheckResponse result = smartWaitlistService.checkSpecificTables("1", bookingTime, guestCount);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("shouldCheckSpecificTables_withConflicts")
    void shouldCheckSpecificTables_withConflicts() {
        // Given
        List<Booking> conflicts = Arrays.asList(booking);
        when(restaurantTableRepository.findById(1)).thenReturn(java.util.Optional.of(table));
        when(bookingRepository.findTableConflictsInTimeRange(eq(1), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(conflicts);

        // When
        AvailabilityCheckResponse result = smartWaitlistService.checkSpecificTables("1", bookingTime, guestCount);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("shouldCheckSpecificTables_multipleTables")
    void shouldCheckSpecificTables_multipleTables() {
        // Given
        RestaurantTable table2 = new RestaurantTable();
        table2.setTableId(2);
        table2.setTableName("Table 2");
        table2.setCapacity(4);

        when(restaurantTableRepository.findById(1)).thenReturn(java.util.Optional.of(table));
        when(restaurantTableRepository.findById(2)).thenReturn(java.util.Optional.of(table2));
        when(bookingRepository.findTableConflictsInTimeRange(anyInt(), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(new ArrayList<>());

        // When
        AvailabilityCheckResponse result = smartWaitlistService.checkSpecificTables("1,2", bookingTime, guestCount);

        // Then
        assertNotNull(result);
    }

    // ========== checkGeneralAvailability() Tests ==========

    @Test
    @DisplayName("shouldCheckGeneralAvailability_hasAvailableTables")
    void shouldCheckGeneralAvailability_hasAvailableTables() {
        // Given
        List<RestaurantTable> suitableTables = Arrays.asList(table);
        when(restaurantTableRepository.findByRestaurantAndCapacityGreaterThanEqual(restaurantId, guestCount))
            .thenReturn(suitableTables);
        when(bookingRepository.findTableConflictsInTimeRange(eq(1), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(new ArrayList<>());

        // When
        AvailabilityCheckResponse result = smartWaitlistService.checkGeneralAvailability(restaurantId, bookingTime, guestCount);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("shouldCheckGeneralAvailability_noAvailableTables")
    void shouldCheckGeneralAvailability_noAvailableTables() {
        // Given
        List<RestaurantTable> suitableTables = Arrays.asList(table);
        when(restaurantTableRepository.findByRestaurantAndCapacityGreaterThanEqual(restaurantId, guestCount))
            .thenReturn(suitableTables);
        when(bookingRepository.findTableConflictsInTimeRange(eq(1), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(booking));
        when(restaurantTableRepository.findByRestaurantRestaurantId(restaurantId))
            .thenReturn(suitableTables);

        // When
        AvailabilityCheckResponse result = smartWaitlistService.checkGeneralAvailability(restaurantId, bookingTime, guestCount);

        // Then
        assertNotNull(result);
    }
}


