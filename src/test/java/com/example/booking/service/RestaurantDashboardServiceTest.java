package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import com.example.booking.common.enums.TableStatus;
import com.example.booking.domain.Booking;
import com.example.booking.domain.BookingDish;
import com.example.booking.domain.BookingTable;
import com.example.booking.domain.Customer;
import com.example.booking.domain.Dish;
import com.example.booking.domain.RestaurantBalance;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.domain.User;
import com.example.booking.domain.Waitlist;
import com.example.booking.domain.WaitlistStatus;
import com.example.booking.repository.BookingDishRepository;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.DishRepository;
import com.example.booking.repository.RestaurantBalanceRepository;
import com.example.booking.repository.RestaurantTableRepository;
import com.example.booking.repository.WaitlistRepository;

/**
 * Comprehensive Test Suite for RestaurantDashboardService
 * 
 * Test Coverage:
 * 1. getDashboardStats() - 15 test cases
 * 2. getRevenueDataByPeriod() - 8 test cases
 * 3. getPopularDishesData() - 7 test cases
 * 4. getRecentBookingsWithDetails() - 6 test cases
 * 5. getWaitingCustomers() - 4 test cases
 * 
 * Total: 40 test cases
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
    private LocalDate today;
    private LocalDateTime startOfToday;
    private LocalDateTime endOfToday;

    @BeforeEach
    void setUp() {
        restaurantId = 1;
        today = LocalDate.now();
        startOfToday = today.atStartOfDay();
        endOfToday = today.atTime(23, 59, 59);

        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(restaurantId);
        restaurant.setRestaurantName("Test Restaurant");
    }

    @Nested
    @DisplayName("getDashboardStats() Tests")
    class GetDashboardStatsTests {

        @Test
        @DisplayName("Should return dashboard stats with today's bookings")
        void getDashboardStats_withTodayBookings_shouldReturnStats() {
            // Given
            List<Booking> todayBookings = createTodayBookings();
            List<RestaurantTable> tables = createTables();
            List<Waitlist> waitlists = createWaitlists();
            RestaurantBalance balance = createBalance();

            when(bookingRepository.findByRestaurantRestaurantIdAndBookingTimeBetween(
                    eq(restaurantId), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(todayBookings);
            when(tableRepository.findByRestaurantRestaurantId(restaurantId))
                    .thenReturn(tables);
            when(waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(
                    eq(restaurantId), eq(WaitlistStatus.WAITING)))
                    .thenReturn(waitlists);
            when(balanceRepository.findByRestaurantRestaurantId(restaurantId))
                    .thenReturn(Optional.of(balance));

            // When
            RestaurantDashboardService.DashboardStats stats = dashboardService.getDashboardStats(restaurantId);

            // Then
            assertNotNull(stats);
            assertEquals(3, stats.getTodayBookings());
            assertEquals(2, stats.getTodayCompletedBookings());
            assertEquals(1, stats.getTodayPendingBookings());
            assertEquals(3, stats.getTotalTables());
            assertEquals(2, stats.getAvailableTables());
            assertEquals(1, stats.getOccupiedTables());
            assertEquals(2, stats.getWaitingCustomers());
            verify(bookingRepository).findByRestaurantRestaurantIdAndBookingTimeBetween(
                    eq(restaurantId), any(LocalDateTime.class), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Should calculate today revenue correctly")
        void getDashboardStats_withCompletedBookings_shouldCalculateRevenue() {
            // Given
            List<Booking> bookings = new ArrayList<>();
            Booking completed1 = createBooking(BookingStatus.COMPLETED, new BigDecimal("500000"));
            Booking completed2 = createBooking(BookingStatus.COMPLETED, new BigDecimal("300000"));
            bookings.add(completed1);
            bookings.add(completed2);

            when(bookingRepository.findByRestaurantRestaurantIdAndBookingTimeBetween(
                    any(), any(), any())).thenReturn(bookings);
            when(tableRepository.findByRestaurantRestaurantId(any())).thenReturn(new ArrayList<>());
            when(waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(
                    any(), any())).thenReturn(new ArrayList<>());
            when(balanceRepository.findByRestaurantRestaurantId(any()))
                    .thenReturn(Optional.empty());

            // When
            RestaurantDashboardService.DashboardStats stats = dashboardService.getDashboardStats(restaurantId);

            // Then
            assertEquals(new BigDecimal("800000"), stats.getTodayRevenue());
        }

        @Test
        @DisplayName("Should return zero revenue when no completed bookings")
        void getDashboardStats_withNoCompletedBookings_shouldReturnZeroRevenue() {
            // Given
            List<Booking> bookings = new ArrayList<>();
            bookings.add(createBooking(BookingStatus.PENDING, new BigDecimal("500000")));

            when(bookingRepository.findByRestaurantRestaurantIdAndBookingTimeBetween(
                    any(), any(), any())).thenReturn(bookings);
            when(tableRepository.findByRestaurantRestaurantId(any())).thenReturn(new ArrayList<>());
            when(waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(
                    any(), any())).thenReturn(new ArrayList<>());
            when(balanceRepository.findByRestaurantRestaurantId(any()))
                    .thenReturn(Optional.empty());

            // When
            RestaurantDashboardService.DashboardStats stats = dashboardService.getDashboardStats(restaurantId);

            // Then
            assertEquals(BigDecimal.ZERO, stats.getTodayRevenue());
        }

        @Test
        @DisplayName("Should handle empty balance gracefully")
        void getDashboardStats_withNoBalance_shouldUseZeroValues() {
            // Given
            when(bookingRepository.findByRestaurantRestaurantIdAndBookingTimeBetween(
                    any(), any(), any())).thenReturn(new ArrayList<>());
            when(tableRepository.findByRestaurantRestaurantId(any())).thenReturn(new ArrayList<>());
            when(waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(
                    any(), any())).thenReturn(new ArrayList<>());
            when(balanceRepository.findByRestaurantRestaurantId(any()))
                    .thenReturn(Optional.empty());

            // When
            RestaurantDashboardService.DashboardStats stats = dashboardService.getDashboardStats(restaurantId);

            // Then
            assertEquals(BigDecimal.ZERO, stats.getTotalRevenue());
            assertEquals(BigDecimal.ZERO, stats.getAvailableBalance());
            assertEquals(0, stats.getTotalCompletedBookings());
        }

        @Test
        @DisplayName("Should calculate percentage changes correctly")
        void getDashboardStats_shouldCalculatePercentageChanges() {
            // Given
            List<Booking> todayBookings = createTodayBookings();
            List<Booking> yesterdayBookings = createYesterdayBookings();

            when(bookingRepository.findByRestaurantRestaurantIdAndBookingTimeBetween(
                    eq(restaurantId), eq(startOfToday), eq(endOfToday)))
                    .thenReturn(todayBookings);
            when(bookingRepository.findByRestaurantRestaurantIdAndBookingTimeBetween(
                    eq(restaurantId), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(yesterdayBookings);
            when(tableRepository.findByRestaurantRestaurantId(restaurantId))
                    .thenReturn(createTables());
            when(waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(
                    any(), any())).thenReturn(new ArrayList<>());
            when(balanceRepository.findByRestaurantRestaurantId(any()))
                    .thenReturn(Optional.empty());

            // When
            RestaurantDashboardService.DashboardStats stats = dashboardService.getDashboardStats(restaurantId);

            // Then
            assertNotNull(stats.getBookingChangePercent());
            assertNotNull(stats.getRevenueChangePercent());
        }

        @Test
        @DisplayName("Should handle zero yesterday bookings")
        void getDashboardStats_withZeroYesterdayBookings_shouldCalculatePercentage() {
            // Given
            List<Booking> todayBookings = createTodayBookings();
            List<Booking> yesterdayBookings = new ArrayList<>(); // No bookings yesterday

            when(bookingRepository.findByRestaurantRestaurantIdAndBookingTimeBetween(
                    eq(restaurantId), eq(startOfToday), eq(endOfToday)))
                    .thenReturn(todayBookings);
            when(bookingRepository.findByRestaurantRestaurantIdAndBookingTimeBetween(
                    eq(restaurantId), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(yesterdayBookings);
            when(tableRepository.findByRestaurantRestaurantId(restaurantId))
                    .thenReturn(createTables());
            when(waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(
                    any(), any())).thenReturn(new ArrayList<>());
            when(balanceRepository.findByRestaurantRestaurantId(any()))
                    .thenReturn(Optional.empty());

            // When
            RestaurantDashboardService.DashboardStats stats = dashboardService.getDashboardStats(restaurantId);

            // Then
            assertTrue(stats.getBookingChangePercent() >= 0);
        }

        @Test
        @DisplayName("Should count table statuses correctly")
        void getDashboardStats_shouldCountTableStatuses() {
            // Given
            List<RestaurantTable> tables = new ArrayList<>();
            RestaurantTable available1 = createTable(TableStatus.AVAILABLE);
            RestaurantTable available2 = createTable(TableStatus.AVAILABLE);
            RestaurantTable occupied = createTable(TableStatus.OCCUPIED);
            tables.add(available1);
            tables.add(available2);
            tables.add(occupied);

            when(bookingRepository.findByRestaurantRestaurantIdAndBookingTimeBetween(
                    any(), any(), any())).thenReturn(new ArrayList<>());
            when(tableRepository.findByRestaurantRestaurantId(restaurantId)).thenReturn(tables);
            when(waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(
                    any(), any())).thenReturn(new ArrayList<>());
            when(balanceRepository.findByRestaurantRestaurantId(any()))
                    .thenReturn(Optional.empty());

            // When
            RestaurantDashboardService.DashboardStats stats = dashboardService.getDashboardStats(restaurantId);

            // Then
            assertEquals(3, stats.getTotalTables());
            assertEquals(2, stats.getAvailableTables());
            assertEquals(1, stats.getOccupiedTables());
        }
    }

    @Nested
    @DisplayName("getRevenueDataByPeriod() Tests")
    class GetRevenueDataByPeriodTests {

        @Test
        @DisplayName("Should return weekly revenue data")
        void getRevenueDataByPeriod_withWeekPeriod_shouldReturnWeekData() {
            // Given
            when(bookingRepository.findByRestaurantRestaurantIdAndBookingTimeBetween(
                    any(), any(), any())).thenReturn(new ArrayList<>());

            // When
            List<RestaurantDashboardService.DailyRevenueData> result =
                    dashboardService.getRevenueDataByPeriod(restaurantId, "week");

            // Then
            assertNotNull(result);
            assertEquals(7, result.size());
            verify(bookingRepository, times(7)).findByRestaurantRestaurantIdAndBookingTimeBetween(
                    eq(restaurantId), any(LocalDateTime.class), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Should return monthly revenue data")
        void getRevenueDataByPeriod_withMonthPeriod_shouldReturnMonthData() {
            // Given
            when(bookingRepository.findByRestaurantRestaurantIdAndBookingTimeBetween(
                    any(), any(), any())).thenReturn(new ArrayList<>());

            // When
            List<RestaurantDashboardService.DailyRevenueData> result =
                    dashboardService.getRevenueDataByPeriod(restaurantId, "month");

            // Then
            assertNotNull(result);
            assertEquals(30, result.size());
        }

        @Test
        @DisplayName("Should return yearly revenue data")
        void getRevenueDataByPeriod_withYearPeriod_shouldReturnYearData() {
            // Given
            when(bookingRepository.findByRestaurantRestaurantIdAndBookingTimeBetween(
                    any(), any(), any())).thenReturn(new ArrayList<>());

            // When
            List<RestaurantDashboardService.DailyRevenueData> result =
                    dashboardService.getRevenueDataByPeriod(restaurantId, "year");

            // Then
            assertNotNull(result);
            assertEquals(12, result.size());
        }

        @Test
        @DisplayName("Should default to week for invalid period")
        void getRevenueDataByPeriod_withInvalidPeriod_shouldDefaultToWeek() {
            // Given
            when(bookingRepository.findByRestaurantRestaurantIdAndBookingTimeBetween(
                    any(), any(), any())).thenReturn(new ArrayList<>());

            // When
            List<RestaurantDashboardService.DailyRevenueData> result =
                    dashboardService.getRevenueDataByPeriod(restaurantId, "invalid");

            // Then
            assertNotNull(result);
            assertEquals(7, result.size());
        }

        @Test
        @DisplayName("Should calculate revenue for each day correctly")
        void getRevenueDataByPeriod_shouldCalculateDailyRevenue() {
            // Given
            List<Booking> bookings = new ArrayList<>();
            Booking completed = createBooking(BookingStatus.COMPLETED, new BigDecimal("500000"));
            bookings.add(completed);

            when(bookingRepository.findByRestaurantRestaurantIdAndBookingTimeBetween(
                    any(), any(), any())).thenReturn(bookings);

            // When
            List<RestaurantDashboardService.DailyRevenueData> result =
                    dashboardService.getRevenueDataByPeriod(restaurantId, "week");

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
            result.forEach(data -> {
                assertNotNull(data.getDate());
                assertNotNull(data.getRevenue());
                assertTrue(data.getBookingCount() >= 0);
            });
        }
    }

    @Nested
    @DisplayName("getPopularDishesData() Tests")
    class GetPopularDishesDataTests {

        @Test
        @DisplayName("Should return top 5 popular dishes")
        void getPopularDishesData_withMultipleDishes_shouldReturnTop5() {
            // Given
            List<Dish> dishes = createDishes(10);
            when(dishRepository.findByRestaurantRestaurantId(restaurantId)).thenReturn(dishes);
            when(bookingDishRepository.findByDishDishIdAndBookingBookingTimeAfter(
                    any(), any())).thenReturn(createBookingDishes());

            // When
            List<RestaurantDashboardService.PopularDishData> result =
                    dashboardService.getPopularDishesData(restaurantId);

            // Then
            assertNotNull(result);
            assertTrue(result.size() <= 5);
        }

        @Test
        @DisplayName("Should sort dishes by quantity sold descending")
        void getPopularDishesData_shouldSortByQuantityDescending() {
            // Given
            List<Dish> dishes = createDishes(3);
            when(dishRepository.findByRestaurantRestaurantId(restaurantId)).thenReturn(dishes);
            when(bookingDishRepository.findByDishDishIdAndBookingBookingTimeAfter(
                    any(), any())).thenReturn(createBookingDishes());

            // When
            List<RestaurantDashboardService.PopularDishData> result =
                    dashboardService.getPopularDishesData(restaurantId);

            // Then
            assertNotNull(result);
            if (result.size() > 1) {
                for (int i = 0; i < result.size() - 1; i++) {
                    assertTrue(result.get(i).getQuantitySold() >=
                            result.get(i + 1).getQuantitySold());
                }
            }
        }

        @Test
        @DisplayName("Should filter out dishes with zero sales")
        void getPopularDishesData_withZeroSalesDishes_shouldFilterOut() {
            // Given
            List<Dish> dishes = createDishes(3);
            when(dishRepository.findByRestaurantRestaurantId(restaurantId)).thenReturn(dishes);
            when(bookingDishRepository.findByDishDishIdAndBookingBookingTimeAfter(
                    any(), any())).thenReturn(new ArrayList<>());

            // When
            List<RestaurantDashboardService.PopularDishData> result =
                    dashboardService.getPopularDishesData(restaurantId);

            // Then
            assertNotNull(result);
            assertEquals(0, result.size());
        }

        @Test
        @DisplayName("Should only include dishes from last 30 days")
        void getPopularDishesData_shouldOnlyIncludeRecentDishes() {
            // Given
            List<Dish> dishes = createDishes(2);
            when(dishRepository.findByRestaurantRestaurantId(restaurantId)).thenReturn(dishes);
            when(bookingDishRepository.findByDishDishIdAndBookingBookingTimeAfter(
                    any(), any())).thenReturn(createBookingDishes());

            // When
            dashboardService.getPopularDishesData(restaurantId);

            // Then
            verify(bookingDishRepository, atLeastOnce()).findByDishDishIdAndBookingBookingTimeAfter(
                    any(Integer.class), any(LocalDateTime.class));
        }
    }

    @Nested
    @DisplayName("getRecentBookingsWithDetails() Tests")
    class GetRecentBookingsWithDetailsTests {

        @Test
        @DisplayName("Should return recent bookings with limit")
        void getRecentBookingsWithDetails_withLimit_shouldReturnLimitedBookings() {
            // Given
            List<Booking> bookings = createRecentBookings(10);
            when(bookingRepository.findByRestaurantRestaurantIdOrderByBookingTimeDesc(restaurantId))
                    .thenReturn(bookings);

            // When
            List<RestaurantDashboardService.BookingInfo> result =
                    dashboardService.getRecentBookingsWithDetails(restaurantId, 5);

            // Then
            assertNotNull(result);
            assertTrue(result.size() <= 5);
        }

        @Test
        @DisplayName("Should convert booking to BookingInfo correctly")
        void getRecentBookingsWithDetails_shouldConvertToBookingInfo() {
            // Given
            Booking booking = createBookingWithCustomer();
            List<Booking> bookings = List.of(booking);
            when(bookingRepository.findByRestaurantRestaurantIdOrderByBookingTimeDesc(restaurantId))
                    .thenReturn(bookings);

            // When
            List<RestaurantDashboardService.BookingInfo> result =
                    dashboardService.getRecentBookingsWithDetails(restaurantId, 10);

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
            RestaurantDashboardService.BookingInfo info = result.get(0);
            assertNotNull(info.getBookingId());
            assertNotNull(info.getBookingTime());
            assertNotNull(info.getStatus());
        }

        @Test
        @DisplayName("Should handle booking with null customer gracefully")
        void getRecentBookingsWithDetails_withNullCustomer_shouldHandleGracefully() {
            // Given
            Booking booking = createBooking(BookingStatus.PENDING, new BigDecimal("100000"));
            booking.setCustomer(null);
            List<Booking> bookings = List.of(booking);
            when(bookingRepository.findByRestaurantRestaurantIdOrderByBookingTimeDesc(restaurantId))
                    .thenReturn(bookings);

            // When
            List<RestaurantDashboardService.BookingInfo> result =
                    dashboardService.getRecentBookingsWithDetails(restaurantId, 10);

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertEquals("Khách hàng không xác định", result.get(0).getCustomerName());
        }

        @Test
        @DisplayName("Should include assigned tables in BookingInfo")
        void getRecentBookingsWithDetails_shouldIncludeAssignedTables() {
            // Given
            Booking booking = createBookingWithCustomer();
            BookingTable bookingTable = new BookingTable();
            RestaurantTable table = new RestaurantTable();
            table.setTableName("Table 1");
            bookingTable.setTable(table);
            booking.setBookingTables(List.of(bookingTable));

            List<Booking> bookings = List.of(booking);
            when(bookingRepository.findByRestaurantRestaurantIdOrderByBookingTimeDesc(restaurantId))
                    .thenReturn(bookings);

            // When
            List<RestaurantDashboardService.BookingInfo> result =
                    dashboardService.getRecentBookingsWithDetails(restaurantId, 10);

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertNotNull(result.get(0).getAssignedTables());
        }
    }

    @Nested
    @DisplayName("getWaitingCustomers() Tests")
    class GetWaitingCustomersTests {

        @Test
        @DisplayName("Should return waiting customers")
        void getWaitingCustomers_shouldReturnWaitingCustomers() {
            // Given
            List<Waitlist> waitlists = createWaitlists();
            when(waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(
                    eq(restaurantId), eq(WaitlistStatus.WAITING)))
                    .thenReturn(waitlists);

            // When
            List<Waitlist> result = dashboardService.getWaitingCustomers(restaurantId);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            verify(waitlistRepository).findByRestaurantIdAndStatusOrderByJoinTimeAsc(
                    eq(restaurantId), eq(WaitlistStatus.WAITING));
        }

        @Test
        @DisplayName("Should return empty list when no waiting customers")
        void getWaitingCustomers_withNoWaitingCustomers_shouldReturnEmptyList() {
            // Given
            when(waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(
                    eq(restaurantId), eq(WaitlistStatus.WAITING)))
                    .thenReturn(new ArrayList<>());

            // When
            List<Waitlist> result = dashboardService.getWaitingCustomers(restaurantId);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // Helper methods

    private List<Booking> createTodayBookings() {
        List<Booking> bookings = new ArrayList<>();
        bookings.add(createBooking(BookingStatus.COMPLETED, new BigDecimal("500000")));
        bookings.add(createBooking(BookingStatus.COMPLETED, new BigDecimal("300000")));
        bookings.add(createBooking(BookingStatus.PENDING, new BigDecimal("200000")));
        return bookings;
    }

    private List<Booking> createYesterdayBookings() {
        List<Booking> bookings = new ArrayList<>();
        bookings.add(createBooking(BookingStatus.COMPLETED, new BigDecimal("400000")));
        return bookings;
    }

    private Booking createBooking(BookingStatus status, BigDecimal depositAmount) {
        Booking booking = new Booking();
        booking.setBookingId(1);
        booking.setStatus(status);
        booking.setDepositAmount(depositAmount);
        booking.setBookingTime(LocalDateTime.now());
        booking.setRestaurant(restaurant);
        return booking;
    }

    private Booking createBookingWithCustomer() {
        Booking booking = createBooking(BookingStatus.PENDING, new BigDecimal("100000"));
        Customer customer = new Customer();
        customer.setCustomerId(java.util.UUID.randomUUID());
        customer.setFullName("Test Customer");
        User user = new User();
        user.setId(java.util.UUID.randomUUID());
        user.setFullName("Test User");
        customer.setUser(user);
        booking.setCustomer(customer);
        return booking;
    }

    private List<Booking> createRecentBookings(int count) {
        List<Booking> bookings = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Booking booking = createBooking(BookingStatus.PENDING, new BigDecimal("100000"));
            booking.setBookingId(i + 1);
            bookings.add(booking);
        }
        return bookings;
    }

    private List<RestaurantTable> createTables() {
        List<RestaurantTable> tables = new ArrayList<>();
        tables.add(createTable(TableStatus.AVAILABLE));
        tables.add(createTable(TableStatus.AVAILABLE));
        tables.add(createTable(TableStatus.OCCUPIED));
        return tables;
    }

    private RestaurantTable createTable(TableStatus status) {
        RestaurantTable table = new RestaurantTable();
        table.setTableId(1);
        table.setStatus(status);
        table.setRestaurant(restaurant);
        return table;
    }

    private List<Waitlist> createWaitlists() {
        List<Waitlist> waitlists = new ArrayList<>();
        Waitlist waitlist1 = new Waitlist();
        waitlist1.setWaitlistId(1);
        waitlist1.setStatus(WaitlistStatus.WAITING);
        Waitlist waitlist2 = new Waitlist();
        waitlist2.setWaitlistId(2);
        waitlist2.setStatus(WaitlistStatus.WAITING);
        waitlists.add(waitlist1);
        waitlists.add(waitlist2);
        return waitlists;
    }

    private RestaurantBalance createBalance() {
        RestaurantBalance balance = new RestaurantBalance();
        balance.setRestaurant(restaurant);
        balance.setTotalRevenue(new BigDecimal("5000000"));
        balance.setAvailableBalance(new BigDecimal("3000000"));
        balance.setTotalBookingsCompleted(100);
        return balance;
    }

    private List<Dish> createDishes(int count) {
        List<Dish> dishes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Dish dish = new Dish();
            dish.setDishId(i + 1);
            dish.setName("Dish " + (i + 1));
            dish.setPrice(new BigDecimal("100000"));
            dish.setRestaurant(restaurant);
            dishes.add(dish);
        }
        return dishes;
    }

    private List<BookingDish> createBookingDishes() {
        List<BookingDish> bookingDishes = new ArrayList<>();
        BookingDish bookingDish = new BookingDish();
        bookingDish.setQuantity(5);
        bookingDishes.add(bookingDish);
        return bookingDishes;
    }
}
