package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.booking.common.enums.TableStatus;
import com.example.booking.domain.Booking;
import com.example.booking.domain.Customer;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.domain.Waitlist;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.DiningTableRepository;
import com.example.booking.repository.RestaurantRepository;

/**
 * Unit tests for FOHManagementService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FOHManagementService Tests")
public class FOHManagementServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private DiningTableRepository diningTableRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private WaitlistService waitlistService;

    @InjectMocks
    private FOHManagementService fohService;

    private Integer restaurantId;
    private UUID customerId;
    private RestaurantProfile restaurant;
    private RestaurantTable table;
    private Booking booking;
    private Waitlist waitlist;

    @BeforeEach
    void setUp() {
        restaurantId = 1;
        customerId = UUID.randomUUID();

        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(restaurantId);
        restaurant.setRestaurantName("Test Restaurant");

        table = new RestaurantTable();
        table.setTableId(1);
        table.setRestaurant(restaurant);
        table.setTableName("Table 1");
        table.setStatus(TableStatus.AVAILABLE);
        table.setCapacity(4);

        booking = new Booking();
        booking.setBookingId(1);
        booking.setRestaurant(restaurant);
        booking.setBookingTime(LocalDateTime.now().plusHours(1));

        waitlist = new Waitlist();
        waitlist.setWaitlistId(1);
        waitlist.setRestaurant(restaurant);
    }

    // ========== getTodayBookings() Tests ==========

    @Test
    @DisplayName("shouldGetTodayBookings_successfully")
    void shouldGetTodayBookings_successfully() {
        // Given
        List<Booking> bookings = Arrays.asList(booking);
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(bookingRepository.findByRestaurantAndBookingTimeBetween(
            any(RestaurantProfile.class), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(bookings);

        // When
        List<Booking> result = fohService.getTodayBookings(restaurantId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("shouldReturnEmptyList_whenRestaurantNotFound")
    void shouldReturnEmptyList_whenRestaurantNotFound() {
        // Given
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

        // When
        List<Booking> result = fohService.getTodayBookings(restaurantId);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // ========== getAllTables() Tests ==========

    @Test
    @DisplayName("shouldGetAllTables_successfully")
    void shouldGetAllTables_successfully() {
        // Given
        List<RestaurantTable> tables = Arrays.asList(table);
        when(diningTableRepository.findAll()).thenReturn(tables);

        // When
        List<RestaurantTable> result = fohService.getAllTables();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ========== getAvailableTables() Tests ==========

    @Test
    @DisplayName("shouldGetAvailableTables_successfully")
    void shouldGetAvailableTables_successfully() {
        // Given
        List<RestaurantTable> tables = Arrays.asList(table);
        when(diningTableRepository.findAll()).thenReturn(tables);

        // When
        List<RestaurantTable> result = fohService.getAvailableTables(restaurantId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ========== getOccupiedTables() Tests ==========

    @Test
    @DisplayName("shouldGetOccupiedTables_successfully")
    void shouldGetOccupiedTables_successfully() {
        // Given
        table.setStatus(TableStatus.OCCUPIED);
        List<RestaurantTable> tables = Arrays.asList(table);
        when(diningTableRepository.findAll()).thenReturn(tables);

        // When
        List<RestaurantTable> result = fohService.getOccupiedTables(restaurantId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ========== getWaitlistEntries() Tests ==========

    @Test
    @DisplayName("shouldGetWaitlistEntries_successfully")
    void shouldGetWaitlistEntries_successfully() {
        // Given
        List<Waitlist> waitlists = Arrays.asList(waitlist);
        when(waitlistService.getAllWaitlistByRestaurant(restaurantId)).thenReturn(waitlists);

        // When
        List<Waitlist> result = fohService.getWaitlistEntries(restaurantId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ========== addToWaitlist() Tests ==========

    @Test
    @DisplayName("shouldAddToWaitlist_successfully")
    void shouldAddToWaitlist_successfully() {
        // Given
        when(waitlistService.addToWaitlist(restaurantId, 4, customerId)).thenReturn(waitlist);

        // When
        Waitlist result = fohService.addToWaitlist(restaurantId, 4, customerId);

        // Then
        assertNotNull(result);
        verify(waitlistService, times(1)).addToWaitlist(restaurantId, 4, customerId);
    }
}


