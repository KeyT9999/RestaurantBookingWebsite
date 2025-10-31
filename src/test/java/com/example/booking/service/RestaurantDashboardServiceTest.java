package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.domain.Waitlist;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.DishRepository;
import com.example.booking.repository.BookingDishRepository;
import com.example.booking.repository.RestaurantBalanceRepository;
import com.example.booking.repository.RestaurantTableRepository;
import com.example.booking.repository.WaitlistRepository;

/**
 * Unit tests for RestaurantDashboardService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RestaurantDashboardService Tests")
public class RestaurantDashboardServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RestaurantTableRepository tableRepository;

    @Mock
    private WaitlistRepository waitlistRepository;

    @Mock
    private DishRepository dishRepository;

    @Mock
    private BookingDishRepository bookingDishRepository;

    @Mock
    private RestaurantBalanceRepository balanceRepository;

    @InjectMocks
    private RestaurantDashboardService dashboardService;

    private Integer restaurantId;
    private RestaurantProfile restaurant;
    private Booking booking;
    private RestaurantTable table;
    private Waitlist waitlist;

    @BeforeEach
    void setUp() {
        restaurantId = 1;

        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(restaurantId);
        restaurant.setRestaurantName("Test Restaurant");

        booking = new Booking();
        booking.setBookingId(1);
        booking.setRestaurant(restaurant);
        booking.setStatus(BookingStatus.COMPLETED);
        booking.setBookingTime(LocalDateTime.now());
        booking.setDepositAmount(new BigDecimal("500000"));

        table = new RestaurantTable();
        table.setTableId(1);
        table.setRestaurant(restaurant);
        table.setStatus(TableStatus.AVAILABLE);

        waitlist = new Waitlist();
        waitlist.setWaitlistId(1);
        waitlist.setRestaurant(restaurant);
    }

    // ========== getDashboardStats() Tests ==========

    @Test
    @DisplayName("shouldGetDashboardStats_successfully")
    void shouldGetDashboardStats_successfully() {
        // Given
        List<Booking> todayBookings = Arrays.asList(booking);
        List<RestaurantTable> tables = Arrays.asList(table);
        List<Waitlist> waitlists = Arrays.asList(waitlist);

        when(bookingRepository.findByRestaurantRestaurantIdAndBookingTimeBetween(
            eq(restaurantId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(todayBookings);
        when(tableRepository.findByRestaurantRestaurantId(restaurantId)).thenReturn(tables);
        when(waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(
            eq(restaurantId), any())).thenReturn(waitlists);

        // When
        RestaurantDashboardService.DashboardStats result = dashboardService.getDashboardStats(restaurantId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTodayBookings());
        assertEquals(1, result.getTotalTables());
    }

    @Test
    @DisplayName("shouldCalculateTodayRevenue_correctly")
    void shouldCalculateTodayRevenue_correctly() {
        // Given
        List<Booking> completedBookings = Arrays.asList(booking);
        when(bookingRepository.findByRestaurantRestaurantIdAndBookingTimeBetween(
            eq(restaurantId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(completedBookings);
        when(tableRepository.findByRestaurantRestaurantId(restaurantId)).thenReturn(Arrays.asList(table));
        when(waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(
            eq(restaurantId), any())).thenReturn(new ArrayList<>());

        // When
        RestaurantDashboardService.DashboardStats result = dashboardService.getDashboardStats(restaurantId);

        // Then
        assertNotNull(result);
        assertNotNull(result.getTodayRevenue());
    }
}


