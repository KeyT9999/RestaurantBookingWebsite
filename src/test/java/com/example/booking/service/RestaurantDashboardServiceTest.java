package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
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
import com.example.booking.domain.WaitlistStatus;
import com.example.booking.domain.Dish;
import com.example.booking.domain.BookingDish;
import com.example.booking.domain.Customer;
import com.example.booking.domain.User;
import com.example.booking.domain.BookingTable;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.DishRepository;
import com.example.booking.repository.BookingDishRepository;
import com.example.booking.repository.RestaurantBalanceRepository;
import com.example.booking.repository.RestaurantTableRepository;
import com.example.booking.repository.WaitlistRepository;
import java.util.Optional;
import java.util.Collections;

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

    @Test
    @DisplayName("shouldHandleEmptyData_whenNoBookings")
    void shouldHandleEmptyData_whenNoBookings() {
        // Given
        when(bookingRepository.findByRestaurantRestaurantIdAndBookingTimeBetween(
            eq(restaurantId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Collections.emptyList());
        when(tableRepository.findByRestaurantRestaurantId(restaurantId))
            .thenReturn(Collections.emptyList());
        when(waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(
            eq(restaurantId), any())).thenReturn(Collections.emptyList());
        when(balanceRepository.findByRestaurantRestaurantId(restaurantId))
            .thenReturn(Optional.empty());

        // When
        RestaurantDashboardService.DashboardStats result = dashboardService.getDashboardStats(restaurantId);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTodayBookings());
        assertEquals(0, result.getTotalTables());
        assertEquals(0, result.getWaitingCustomers());
        assertEquals(BigDecimal.ZERO, result.getTodayRevenue());
    }

    @Test
    @DisplayName("shouldCalculatePendingBookings_correctly")
    void shouldCalculatePendingBookings_correctly() {
        // Given
        Booking pendingBooking = new Booking();
        pendingBooking.setBookingId(2);
        pendingBooking.setRestaurant(restaurant);
        pendingBooking.setStatus(BookingStatus.PENDING);
        pendingBooking.setBookingTime(LocalDateTime.now());
        
        List<Booking> bookings = Arrays.asList(booking, pendingBooking);
        when(bookingRepository.findByRestaurantRestaurantIdAndBookingTimeBetween(
            eq(restaurantId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(bookings);
        when(tableRepository.findByRestaurantRestaurantId(restaurantId))
            .thenReturn(Arrays.asList(table));
        when(waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(
            eq(restaurantId), any())).thenReturn(Collections.emptyList());
        when(balanceRepository.findByRestaurantRestaurantId(restaurantId))
            .thenReturn(Optional.empty());

        // When
        RestaurantDashboardService.DashboardStats result = dashboardService.getDashboardStats(restaurantId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTodayBookings());
        assertEquals(1, result.getTodayCompletedBookings());
        assertEquals(1, result.getTodayPendingBookings());
    }

    @Test
    @DisplayName("shouldCalculateTableStatus_correctly")
    void shouldCalculateTableStatus_correctly() {
        // Given
        RestaurantTable occupiedTable = new RestaurantTable();
        occupiedTable.setTableId(2);
        occupiedTable.setRestaurant(restaurant);
        occupiedTable.setStatus(TableStatus.OCCUPIED);
        
        List<RestaurantTable> tables = Arrays.asList(table, occupiedTable);
        when(bookingRepository.findByRestaurantRestaurantIdAndBookingTimeBetween(
            eq(restaurantId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Collections.emptyList());
        when(tableRepository.findByRestaurantRestaurantId(restaurantId))
            .thenReturn(tables);
        when(waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(
            eq(restaurantId), any())).thenReturn(Collections.emptyList());
        when(balanceRepository.findByRestaurantRestaurantId(restaurantId))
            .thenReturn(Optional.empty());

        // When
        RestaurantDashboardService.DashboardStats result = dashboardService.getDashboardStats(restaurantId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalTables());
        assertEquals(1, result.getAvailableTables());
        assertEquals(1, result.getOccupiedTables());
    }

    // ========== getRevenueDataByPeriod() Tests ==========

    @Test
    @DisplayName("shouldGetRevenueDataByPeriod_week")
    void shouldGetRevenueDataByPeriod_week() {
        // Given
        String period = "week";
        when(bookingRepository.findByRestaurantRestaurantIdAndBookingTimeBetween(
            eq(restaurantId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(booking));

        // When
        List<RestaurantDashboardService.DailyRevenueData> result = 
            dashboardService.getRevenueDataByPeriod(restaurantId, period);

        // Then
        assertNotNull(result);
        assertEquals(7, result.size());
    }

    @Test
    @DisplayName("shouldGetRevenueDataByPeriod_month")
    void shouldGetRevenueDataByPeriod_month() {
        // Given
        String period = "month";
        when(bookingRepository.findByRestaurantRestaurantIdAndBookingTimeBetween(
            eq(restaurantId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(booking));

        // When
        List<RestaurantDashboardService.DailyRevenueData> result = 
            dashboardService.getRevenueDataByPeriod(restaurantId, period);

        // Then
        assertNotNull(result);
        assertEquals(30, result.size());
    }

    @Test
    @DisplayName("shouldGetRevenueDataByPeriod_year")
    void shouldGetRevenueDataByPeriod_year() {
        // Given
        String period = "year";
        when(bookingRepository.findByRestaurantRestaurantIdAndBookingTimeBetween(
            eq(restaurantId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(booking));

        // When
        List<RestaurantDashboardService.DailyRevenueData> result = 
            dashboardService.getRevenueDataByPeriod(restaurantId, period);

        // Then
        assertNotNull(result);
        assertEquals(12, result.size());
    }

    @Test
    @DisplayName("shouldGetRevenueDataByPeriod_default")
    void shouldGetRevenueDataByPeriod_default() {
        // Given
        String period = "unknown";
        when(bookingRepository.findByRestaurantRestaurantIdAndBookingTimeBetween(
            eq(restaurantId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(booking));

        // When
        List<RestaurantDashboardService.DailyRevenueData> result = 
            dashboardService.getRevenueDataByPeriod(restaurantId, period);

        // Then
        assertNotNull(result);
        assertEquals(7, result.size()); // Defaults to week
    }

    // ========== getPopularDishesData() Tests ==========

    @Test
    @DisplayName("shouldGetPopularDishesData_successfully")
    void shouldGetPopularDishesData_successfully() {
        // Given
        Dish dish1 = new Dish();
        dish1.setDishId(1);
        dish1.setName("Dish 1");
        dish1.setPrice(new BigDecimal("100000"));
        dish1.setRestaurant(restaurant);
        
        BookingDish bookingDish1 = new BookingDish();
        bookingDish1.setQuantity(5);
        bookingDish1.setDish(dish1);
        
        List<Dish> dishes = Arrays.asList(dish1);
        List<BookingDish> bookingDishes = Arrays.asList(bookingDish1);
        
        when(dishRepository.findByRestaurantRestaurantId(restaurantId))
            .thenReturn(dishes);
        when(bookingDishRepository.findByDishDishIdAndBookingBookingTimeAfter(
            eq(1), any(LocalDateTime.class)))
            .thenReturn(bookingDishes);

        // When
        List<RestaurantDashboardService.PopularDishData> result = 
            dashboardService.getPopularDishesData(restaurantId);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("Dish 1", result.get(0).getDishName());
        assertEquals(5, result.get(0).getQuantitySold());
    }

    @Test
    @DisplayName("shouldReturnEmptyList_whenNoPopularDishes")
    void shouldReturnEmptyList_whenNoPopularDishes() {
        // Given
        Dish dish1 = new Dish();
        dish1.setDishId(1);
        dish1.setName("Dish 1");
        dish1.setRestaurant(restaurant);
        
        List<Dish> dishes = Arrays.asList(dish1);
        when(dishRepository.findByRestaurantRestaurantId(restaurantId))
            .thenReturn(dishes);
        when(bookingDishRepository.findByDishDishIdAndBookingBookingTimeAfter(
            eq(1), any(LocalDateTime.class)))
            .thenReturn(Collections.emptyList());

        // When
        List<RestaurantDashboardService.PopularDishData> result = 
            dashboardService.getPopularDishesData(restaurantId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("shouldLimitPopularDishesToTop5")
    void shouldLimitPopularDishesToTop5() {
        // Given
        List<Dish> dishes = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Dish dish = new Dish();
            dish.setDishId(i);
            dish.setName("Dish " + i);
            dish.setRestaurant(restaurant);
            dishes.add(dish);
        }
        
        when(dishRepository.findByRestaurantRestaurantId(restaurantId))
            .thenReturn(dishes);
        when(bookingDishRepository.findByDishDishIdAndBookingBookingTimeAfter(
            anyInt(), any(LocalDateTime.class)))
            .thenAnswer(invocation -> {
                BookingDish bd = new BookingDish();
                bd.setQuantity((Integer) invocation.getArgument(0));
                return Arrays.asList(bd);
            });

        // When
        List<RestaurantDashboardService.PopularDishData> result = 
            dashboardService.getPopularDishesData(restaurantId);

        // Then
        assertNotNull(result);
        assertTrue(result.size() <= 5);
    }

    // ========== getRecentBookingsWithDetails() Tests ==========

    @Test
    @DisplayName("shouldGetRecentBookingsWithDetails_successfully")
    void shouldGetRecentBookingsWithDetails_successfully() {
        // Given
        Customer customer = new Customer();
        customer.setFullName("Test Customer");
        
        User user = new User();
        user.setFullName("Test User");
        user.setPhoneNumber("0123456789");
        customer.setUser(user);
        
        booking.setCustomer(customer);
        
        BookingTable bookingTable = new BookingTable();
        bookingTable.setTable(table);
        booking.setBookingTables(Arrays.asList(bookingTable));
        
        when(bookingRepository.findByRestaurantRestaurantIdOrderByBookingTimeDesc(restaurantId))
            .thenReturn(Arrays.asList(booking));

        // When
        List<RestaurantDashboardService.BookingInfo> result = 
            dashboardService.getRecentBookingsWithDetails(restaurantId, 5);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.get(0).getBookingId());
        assertNotNull(result.get(0).getCustomerName());
    }

    @Test
    @DisplayName("shouldHandleNullCustomer_gracefully")
    void shouldHandleNullCustomer_gracefully() {
        // Given
        booking.setCustomer(null);
        when(bookingRepository.findByRestaurantRestaurantIdOrderByBookingTimeDesc(restaurantId))
            .thenReturn(Arrays.asList(booking));

        // When
        List<RestaurantDashboardService.BookingInfo> result = 
            dashboardService.getRecentBookingsWithDetails(restaurantId, 5);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("Khách hàng không xác định", result.get(0).getCustomerName());
    }

    @Test
    @DisplayName("shouldLimitRecentBookingsToLimit")
    void shouldLimitRecentBookingsToLimit() {
        // Given
        List<Booking> bookings = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Booking b = new Booking();
            b.setBookingId(i);
            b.setRestaurant(restaurant);
            bookings.add(b);
        }
        
        when(bookingRepository.findByRestaurantRestaurantIdOrderByBookingTimeDesc(restaurantId))
            .thenReturn(bookings);

        // When
        List<RestaurantDashboardService.BookingInfo> result = 
            dashboardService.getRecentBookingsWithDetails(restaurantId, 5);

        // Then
        assertNotNull(result);
        assertTrue(result.size() <= 5);
    }

    // ========== getWaitingCustomers() Tests ==========

    @Test
    @DisplayName("shouldGetWaitingCustomers_successfully")
    void shouldGetWaitingCustomers_successfully() {
        // Given
        waitlist.setStatus(WaitlistStatus.WAITING);
        List<Waitlist> waitingCustomers = Arrays.asList(waitlist);
        
        when(waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(
            restaurantId, WaitlistStatus.WAITING))
            .thenReturn(waitingCustomers);

        // When
        List<Waitlist> result = dashboardService.getWaitingCustomers(restaurantId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(waitlist, result.get(0));
    }

    @Test
    @DisplayName("shouldReturnEmptyList_whenNoWaitingCustomers")
    void shouldReturnEmptyList_whenNoWaitingCustomers() {
        // Given
        when(waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(
            restaurantId, WaitlistStatus.WAITING))
            .thenReturn(Collections.emptyList());

        // When
        List<Waitlist> result = dashboardService.getWaitingCustomers(restaurantId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}


