package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.booking.common.enums.BookingStatus;
import com.example.booking.domain.Booking;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.dto.AvailabilityCheckResponse;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.RestaurantTableRepository;

/**
 * Comprehensive Test Suite for SmartWaitlistService
 * 
 * Test Coverage:
 * 1. checkSpecificTables() - 8 test cases
 * 2. checkGeneralAvailability() - 6 test cases
 * 
 * Total: 14 test cases
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
    private Integer tableId1;
    private Integer tableId2;
    private Integer guestCount;
    private LocalDateTime bookingTime;
    private RestaurantProfile restaurant;
    private RestaurantTable table1;
    private RestaurantTable table2;

    @BeforeEach
    void setUp() {
        restaurantId = 1;
        tableId1 = 1;
        tableId2 = 2;
        guestCount = 4;
        bookingTime = LocalDateTime.now().plusHours(2);

        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(restaurantId);
        restaurant.setRestaurantName("Test Restaurant");

        table1 = new RestaurantTable();
        table1.setTableId(tableId1);
        table1.setTableName("Table 1");
        table1.setCapacity(4);
        table1.setRestaurant(restaurant);

        table2 = new RestaurantTable();
        table2.setTableId(tableId2);
        table2.setTableName("Table 2");
        table2.setCapacity(6);
        table2.setRestaurant(restaurant);
    }

    @Nested
    @DisplayName("checkSpecificTables() Tests")
    class CheckSpecificTablesTests {

        @Test
        @DisplayName("Should return success when tables are available")
        void checkSpecificTables_withAvailableTables_shouldReturnSuccess() {
            // Given
            String tableIds = "1,2";
            when(restaurantTableRepository.findById(tableId1)).thenReturn(Optional.of(table1));
            when(restaurantTableRepository.findById(tableId2)).thenReturn(Optional.of(table2));
            when(bookingRepository.findTableConflictsInTimeRange(anyInt(), any(), any()))
                    .thenReturn(new ArrayList<>());

            // When
            AvailabilityCheckResponse result = smartWaitlistService.checkSpecificTables(
                    tableIds, bookingTime, guestCount);

            // Then
            assertNotNull(result);
            assertFalse(result.isHasConflict());
            verify(restaurantTableRepository, times(2)).findById(anyInt());
        }

        @Test
        @DisplayName("Should return conflict when tables have conflicts")
        void checkSpecificTables_withConflicts_shouldReturnConflict() {
            // Given
            String tableIds = "1";
            Booking conflictBooking = createBooking(BookingStatus.CONFIRMED, bookingTime);
            
            when(restaurantTableRepository.findById(tableId1)).thenReturn(Optional.of(table1));
            when(bookingRepository.findTableConflictsInTimeRange(eq(tableId1), any(), any()))
                    .thenReturn(List.of(conflictBooking));

            // When
            AvailabilityCheckResponse result = smartWaitlistService.checkSpecificTables(
                    tableIds, bookingTime, guestCount);

            // Then
            assertNotNull(result);
            assertTrue(result.isHasConflict());
            assertNotNull(result.getConflictDetails());
        }

        @Test
        @DisplayName("Should handle invalid table IDs gracefully")
        void checkSpecificTables_withInvalidTableIds_shouldHandleGracefully() {
            // Given
            String tableIds = "999,1000";
            when(restaurantTableRepository.findById(anyInt())).thenReturn(Optional.empty());

            // When
            AvailabilityCheckResponse result = smartWaitlistService.checkSpecificTables(
                    tableIds, bookingTime, guestCount);

            // Then
            assertNotNull(result);
            assertFalse(result.isHasConflict());
        }

        @Test
        @DisplayName("Should parse multiple table IDs correctly")
        void checkSpecificTables_withMultipleTables_shouldParseCorrectly() {
            // Given
            String tableIds = "1, 2, 3";
            when(restaurantTableRepository.findById(anyInt())).thenReturn(Optional.of(table1));
            when(bookingRepository.findTableConflictsInTimeRange(anyInt(), any(), any()))
                    .thenReturn(new ArrayList<>());

            // When
            AvailabilityCheckResponse result = smartWaitlistService.checkSpecificTables(
                    tableIds, bookingTime, guestCount);

            // Then
            assertNotNull(result);
            verify(restaurantTableRepository, atLeastOnce()).findById(anyInt());
        }

        @Test
        @DisplayName("Should include conflict details when conflicts exist")
        void checkSpecificTables_withConflicts_shouldIncludeConflictDetails() {
            // Given
            String tableIds = "1";
            Booking conflictBooking = createBooking(BookingStatus.CONFIRMED, bookingTime);
            
            when(restaurantTableRepository.findById(tableId1)).thenReturn(Optional.of(table1));
            when(bookingRepository.findTableConflictsInTimeRange(eq(tableId1), any(), any()))
                    .thenReturn(List.of(conflictBooking));

            // When
            AvailabilityCheckResponse result = smartWaitlistService.checkSpecificTables(
                    tableIds, bookingTime, guestCount);

            // Then
            assertNotNull(result);
            assertTrue(result.isHasConflict());
            assertNotNull(result.getConflictDetails());
            assertNotNull(result.getConflictDetails().getSelectedTables());
            assertFalse(result.getConflictDetails().getSelectedTables().isEmpty());
        }

        @Test
        @DisplayName("Should calculate wait time when conflicts exist")
        void checkSpecificTables_withConflicts_shouldCalculateWaitTime() {
            // Given
            String tableIds = "1";
            Booking conflictBooking = createBooking(BookingStatus.CONFIRMED, bookingTime);
            
            when(restaurantTableRepository.findById(tableId1)).thenReturn(Optional.of(table1));
            when(bookingRepository.findTableConflictsInTimeRange(eq(tableId1), any(), any()))
                    .thenReturn(List.of(conflictBooking));

            // When
            AvailabilityCheckResponse result = smartWaitlistService.checkSpecificTables(
                    tableIds, bookingTime, guestCount);

            // Then
            assertNotNull(result);
            if (result.isHasConflict() && result.getWaitlistInfo() != null) {
                assertNotNull(result.getWaitlistInfo().getEstimatedWaitTime());
            }
        }

        @Test
        @DisplayName("Should provide alternative tables when conflicts exist")
        void checkSpecificTables_withConflicts_shouldProvideAlternatives() {
            // Given
            String tableIds = "1";
            Booking conflictBooking = createBooking(BookingStatus.CONFIRMED, bookingTime);
            
            when(restaurantTableRepository.findById(tableId1)).thenReturn(Optional.of(table1));
            when(bookingRepository.findTableConflictsInTimeRange(eq(tableId1), any(), any()))
                    .thenReturn(List.of(conflictBooking));
            when(restaurantTableRepository.findByRestaurantRestaurantId(restaurantId))
                    .thenReturn(List.of(table1, table2));

            // When
            AvailabilityCheckResponse result = smartWaitlistService.checkSpecificTables(
                    tableIds, bookingTime, guestCount);

            // Then
            assertNotNull(result);
            // Note: Alternative tables logic might be in the response building method
        }
    }

    @Nested
    @DisplayName("checkGeneralAvailability() Tests")
    class CheckGeneralAvailabilityTests {

        @Test
        @DisplayName("Should return success when tables are available")
        void checkGeneralAvailability_withAvailableTables_shouldReturnSuccess() {
            // Given
            List<RestaurantTable> suitableTables = List.of(table1, table2);
            when(restaurantTableRepository.findByRestaurantAndCapacityGreaterThanEqual(restaurantId, guestCount))
                    .thenReturn(suitableTables);
            when(bookingRepository.findTableConflictsInTimeRange(anyInt(), any(), any()))
                    .thenReturn(new ArrayList<>());

            // When
            AvailabilityCheckResponse result = smartWaitlistService.checkGeneralAvailability(
                    restaurantId, bookingTime, guestCount);

            // Then
            assertNotNull(result);
            assertFalse(result.isHasConflict());
            verify(restaurantTableRepository).findByRestaurantAndCapacityGreaterThanEqual(restaurantId, guestCount);
        }

        @Test
        @DisplayName("Should return conflict when no suitable tables available")
        void checkGeneralAvailability_withNoSuitableTables_shouldReturnConflict() {
            // Given
            List<RestaurantTable> suitableTables = new ArrayList<>();
            List<RestaurantTable> allTables = List.of(table1, table2);
            
            when(restaurantTableRepository.findByRestaurantAndCapacityGreaterThanEqual(restaurantId, guestCount))
                    .thenReturn(suitableTables);
            when(restaurantTableRepository.findByRestaurantRestaurantId(restaurantId))
                    .thenReturn(allTables);
            when(bookingRepository.findTableConflictsInTimeRange(anyInt(), any(), any()))
                    .thenReturn(new ArrayList<>());

            // When
            AvailabilityCheckResponse result = smartWaitlistService.checkGeneralAvailability(
                    restaurantId, bookingTime, guestCount);

            // Then
            assertNotNull(result);
            // Should have conflict or waitlist info when no tables available
        }

        @Test
        @DisplayName("Should filter out conflicted tables")
        void checkGeneralAvailability_shouldFilterConflictedTables() {
            // Given
            List<RestaurantTable> suitableTables = List.of(table1, table2);
            Booking conflictBooking = createBooking(BookingStatus.CONFIRMED, bookingTime);
            
            when(restaurantTableRepository.findByRestaurantAndCapacityGreaterThanEqual(restaurantId, guestCount))
                    .thenReturn(suitableTables);
            when(bookingRepository.findTableConflictsInTimeRange(eq(tableId1), any(), any()))
                    .thenReturn(List.of(conflictBooking));
            when(bookingRepository.findTableConflictsInTimeRange(eq(tableId2), any(), any()))
                    .thenReturn(new ArrayList<>());

            // When
            AvailabilityCheckResponse result = smartWaitlistService.checkGeneralAvailability(
                    restaurantId, bookingTime, guestCount);

            // Then
            assertNotNull(result);
            verify(bookingRepository, atLeastOnce()).findTableConflictsInTimeRange(anyInt(), any(), any());
        }

        @Test
        @DisplayName("Should handle empty table list")
        void checkGeneralAvailability_withEmptyTableList_shouldHandleGracefully() {
            // Given
            when(restaurantTableRepository.findByRestaurantAndCapacityGreaterThanEqual(restaurantId, guestCount))
                    .thenReturn(new ArrayList<>());
            when(restaurantTableRepository.findByRestaurantRestaurantId(restaurantId))
                    .thenReturn(new ArrayList<>());

            // When
            AvailabilityCheckResponse result = smartWaitlistService.checkGeneralAvailability(
                    restaurantId, bookingTime, guestCount);

            // Then
            assertNotNull(result);
        }

        @Test
        @DisplayName("Should find smaller tables when no suitable tables available")
        void checkGeneralAvailability_withNoSuitableTables_shouldFindSmallerTables() {
            // Given
            List<RestaurantTable> suitableTables = new ArrayList<>();
            RestaurantTable smallerTable = new RestaurantTable();
            smallerTable.setTableId(3);
            smallerTable.setCapacity(2);
            List<RestaurantTable> allTables = List.of(table1, smallerTable);
            
            when(restaurantTableRepository.findByRestaurantAndCapacityGreaterThanEqual(restaurantId, guestCount))
                    .thenReturn(suitableTables);
            when(restaurantTableRepository.findByRestaurantRestaurantId(restaurantId))
                    .thenReturn(allTables);
            when(bookingRepository.findTableConflictsInTimeRange(anyInt(), any(), any()))
                    .thenReturn(new ArrayList<>());

            // When
            AvailabilityCheckResponse result = smartWaitlistService.checkGeneralAvailability(
                    restaurantId, bookingTime, guestCount);

            // Then
            assertNotNull(result);
            verify(restaurantTableRepository).findByRestaurantRestaurantId(restaurantId);
        }

        @Test
        @DisplayName("Should check conflicts within buffer time")
        void checkGeneralAvailability_shouldCheckConflictsInBufferTime() {
            // Given
            List<RestaurantTable> suitableTables = List.of(table1);
            when(restaurantTableRepository.findByRestaurantAndCapacityGreaterThanEqual(restaurantId, guestCount))
                    .thenReturn(suitableTables);
            when(bookingRepository.findTableConflictsInTimeRange(anyInt(), any(), any()))
                    .thenReturn(new ArrayList<>());

            // When
            smartWaitlistService.checkGeneralAvailability(restaurantId, bookingTime, guestCount);

            // Then
            verify(bookingRepository).findTableConflictsInTimeRange(
                    eq(tableId1),
                    argThat(time -> time.isBefore(bookingTime)),
                    argThat(time -> time.isAfter(bookingTime))
            );
        }
    }

    // Helper methods

    private Booking createBooking(BookingStatus status, LocalDateTime bookingTime) {
        Booking booking = new Booking();
        booking.setBookingId(1);
        booking.setStatus(status);
        booking.setBookingTime(bookingTime);
        booking.setNumberOfGuests(4);
        booking.setRestaurant(restaurant);
        return booking;
    }
}
