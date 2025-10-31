package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.example.booking.common.enums.BookingStatus;
import com.example.booking.domain.*;
import com.example.booking.repository.*;

import jakarta.persistence.EntityManager;

/**
 * Test class for remaining methods in BookingService
 * Covers: cancelBookingByRestaurant, getBookingDetailById, getBookingWithDetailsById,
 * findAvailableTables, getBookingCountByStatus, getBookingCountInDateRange
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingServiceRemainingMethodsTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RestaurantProfileRepository restaurantProfileRepository;

    @Mock
    private RestaurantTableRepository restaurantTableRepository;

    @Mock
    private BookingTableRepository bookingTableRepository;

    @Mock
    private VoucherService voucherService;

    @Mock
    private BookingDishRepository bookingDishRepository;

    @Mock
    private BookingServiceRepository bookingServiceRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private BookingConflictService conflictService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RefundService refundService;

    @Mock
    private EntityManager entityManager;

    @Mock
    private DishRepository dishRepository;

    @Mock
    private RestaurantServiceRepository restaurantServiceRepository;

    @InjectMocks
    private BookingService bookingService;

    private UUID customerId;
    private UUID restaurantOwnerId;
    private Customer customer;
    private User user;
    private RestaurantProfile restaurant;
    private RestaurantOwner owner;
    private RestaurantTable table1;
    private RestaurantTable table2;
    private Booking booking;
    private Payment payment;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        restaurantOwnerId = UUID.randomUUID();

        // Setup User
        user = new User();
        user.setId(customerId);
        user.setFullName("Test User");

        // Setup Customer
        customer = new Customer();
        customer.setCustomerId(customerId);
        customer.setUser(user);

        // Setup Restaurant Owner
        User ownerUser = new User();
        ownerUser.setId(restaurantOwnerId);
        owner = new RestaurantOwner();
        owner.setUser(ownerUser);

        // Setup Restaurant
        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");
        restaurant.setOwner(owner);

        // Setup Tables
        table1 = new RestaurantTable();
        table1.setTableId(1);
        table1.setTableName("Table 1");
        table1.setCapacity(4);
        table1.setDepositAmount(new BigDecimal("100000"));
        table1.setRestaurant(restaurant);

        table2 = new RestaurantTable();
        table2.setTableId(2);
        table2.setTableName("Table 2");
        table2.setCapacity(6);
        table2.setDepositAmount(new BigDecimal("150000"));
        table2.setRestaurant(restaurant);

        // Setup Booking
        booking = new Booking();
        booking.setBookingId(1);
        booking.setCustomer(customer);
        booking.setRestaurant(restaurant);
        booking.setBookingTime(LocalDateTime.now().plusDays(1));
        booking.setDepositAmount(new BigDecimal("100000"));
        booking.setStatus(BookingStatus.PENDING);
        booking.setNumberOfGuests(4);

        // Setup Payment
        payment = new Payment();
        payment.setPaymentId(1);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setAmount(new BigDecimal("100000"));
        payment.setBooking(booking);

        // Default mocks
        lenient().when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
        lenient().when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(paymentRepository.findByBooking(booking)).thenReturn(Optional.of(payment));
    }

    // ==================== CANCEL BOOKING BY RESTAURANT TESTS ====================

    @Test
    @DisplayName("Should cancel booking by restaurant successfully")
    void testCancelBookingByRestaurant_WithValidData_ShouldCancel() {
        when(refundService.processRefundWithManualTransfer(anyInt(), anyString(), anyString(), anyString()))
            .thenReturn(payment);

        Booking result = bookingService.cancelBookingByRestaurant(1, restaurantOwnerId, "Test reason", "VCB", "1234567890");

        assertNotNull(result);
        assertEquals(BookingStatus.CANCELLED, result.getStatus());
        assertEquals("Test reason", result.getCancelReason());
        assertNotNull(result.getCancelledAt());
        assertEquals(restaurantOwnerId, result.getCancelledBy());
        verify(refundService).processRefundWithManualTransfer(anyInt(), anyString(), eq("VCB"), eq("1234567890"));
    }

    @Test
    @DisplayName("Should cancel booking by restaurant without payment")
    void testCancelBookingByRestaurant_WithoutPayment_ShouldCancel() {
        when(paymentRepository.findByBooking(booking)).thenReturn(Optional.empty());

        Booking result = bookingService.cancelBookingByRestaurant(1, restaurantOwnerId, "Test reason", "VCB", "1234567890");

        assertNotNull(result);
        assertEquals(BookingStatus.CANCELLED, result.getStatus());
        verify(refundService, never()).processRefundWithManualTransfer(anyInt(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should cancel booking by restaurant with pending payment")
    void testCancelBookingByRestaurant_WithPendingPayment_ShouldCancel() {
        payment.setStatus(PaymentStatus.PENDING);
        when(paymentRepository.findByBooking(booking)).thenReturn(Optional.of(payment));

        Booking result = bookingService.cancelBookingByRestaurant(1, restaurantOwnerId, "Test reason", "VCB", "1234567890");

        assertNotNull(result);
        assertEquals(BookingStatus.CANCELLED, result.getStatus());
        verify(refundService, never()).processRefundWithManualTransfer(anyInt(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should cancel booking by restaurant using legacy method")
    void testCancelBookingByRestaurant_LegacyMethod_ShouldCancel() {
        when(paymentRepository.findByBooking(booking)).thenReturn(Optional.empty());

        Booking result = bookingService.cancelBookingByRestaurant(1, restaurantOwnerId, "Test reason");

        assertNotNull(result);
        assertEquals(BookingStatus.CANCELLED, result.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when restaurant owner doesn't own restaurant")
    void testCancelBookingByRestaurant_WithUnauthorizedOwner_ShouldThrowException() {
        UUID differentOwnerId = UUID.randomUUID();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.cancelBookingByRestaurant(1, differentOwnerId, "Test", "VCB", "1234567890");
        });
        assertTrue(exception.getMessage().contains("You can only cancel bookings"));
    }

    @Test
    @DisplayName("Should throw exception when booking not found")
    void testCancelBookingByRestaurant_WithBookingNotFound_ShouldThrowException() {
        when(bookingRepository.findById(999)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.cancelBookingByRestaurant(999, restaurantOwnerId, "Test", "VCB", "1234567890");
        });
        assertEquals("Booking not found", exception.getMessage());
    }

    // ==================== GET BOOKING DETAIL BY ID TESTS ====================

    @Test
    @DisplayName("Should get booking detail by id successfully")
    void testGetBookingDetailById_WithValidId_ShouldReturnBooking() {
        Optional<Booking> result = bookingService.getBookingDetailById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getBookingId());
        verify(bookingRepository).findById(1);
    }

    @Test
    @DisplayName("Should return empty when booking not found")
    void testGetBookingDetailById_WithInvalidId_ShouldReturnEmpty() {
        when(bookingRepository.findById(999)).thenReturn(Optional.empty());

        Optional<Booking> result = bookingService.getBookingDetailById(999);

        assertFalse(result.isPresent());
    }

    // ==================== GET BOOKING WITH DETAILS BY ID TESTS ====================

    @Test
    @DisplayName("Should get booking with details by id successfully")
    void testGetBookingWithDetailsById_WithValidId_ShouldReturnBooking() {
        BookingDish bookingDish = new BookingDish();
        Dish dish = new Dish();
        dish.setName("Test Dish");
        bookingDish.setDish(dish);
        bookingDish.setQuantity(2);

        com.example.booking.domain.BookingService bookingServiceEntity = new com.example.booking.domain.BookingService();
        RestaurantService service = new RestaurantService();
        service.setName("Test Service");
        bookingServiceEntity.setService(service);

        BookingTable bookingTable = new BookingTable();
        bookingTable.setTable(table1);

        booking.setBookingDishes(new ArrayList<>(Arrays.asList(bookingDish)));
        booking.setBookingServices(new ArrayList<>(Arrays.asList(bookingServiceEntity)));
        booking.setBookingTables(new ArrayList<>(Arrays.asList(bookingTable)));

        Optional<Booking> result = bookingService.getBookingWithDetailsById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getBookingId());
        assertNotNull(result.get().getBookingDishes());
        assertNotNull(result.get().getBookingServices());
        assertNotNull(result.get().getBookingTables());
    }

    @Test
    @DisplayName("Should get booking with details when relationships are null")
    void testGetBookingWithDetailsById_WithNullRelationships_ShouldReturn() {
        booking.setBookingDishes(null);
        booking.setBookingServices(null);
        booking.setBookingTables(null);

        Optional<Booking> result = bookingService.getBookingWithDetailsById(1);

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("Should return empty when booking not found")
    void testGetBookingWithDetailsById_WithInvalidId_ShouldReturnEmpty() {
        when(bookingRepository.findById(999)).thenReturn(Optional.empty());

        Optional<Booking> result = bookingService.getBookingWithDetailsById(999);

        assertFalse(result.isPresent());
    }

    // ==================== FIND AVAILABLE TABLES TESTS ====================

    @Test
    @DisplayName("Should find available tables successfully")
    void testFindAvailableTables_WithValidParams_ShouldReturnAvailableTables() {
        LocalDateTime bookingTime = LocalDateTime.now().plusDays(1);
        Integer guestCount = 4;
        List<RestaurantTable> allTables = Arrays.asList(table1, table2);
        
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findByRestaurantAndCapacityGreaterThanEqual(1, guestCount))
            .thenReturn(allTables);
        when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any()))
            .thenReturn(false);

        List<RestaurantTable> result = bookingService.findAvailableTables(1, bookingTime, guestCount);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(restaurantProfileRepository).findById(1);
    }

    @Test
    @DisplayName("Should filter out booked tables")
    void testFindAvailableTables_WithBookedTables_ShouldFilterOut() {
        LocalDateTime bookingTime = LocalDateTime.now().plusDays(1);
        Integer guestCount = 4;
        List<RestaurantTable> allTables = Arrays.asList(table1, table2);
        
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findByRestaurantAndCapacityGreaterThanEqual(1, guestCount))
            .thenReturn(allTables);
        when(bookingTableRepository.existsByTableAndBookingTimeRange(eq(table1), any(), any()))
            .thenReturn(true); // table1 is booked
        when(bookingTableRepository.existsByTableAndBookingTimeRange(eq(table2), any(), any()))
            .thenReturn(false); // table2 is available

        List<RestaurantTable> result = bookingService.findAvailableTables(1, bookingTime, guestCount);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(table2.getTableId(), result.get(0).getTableId());
    }

    @Test
    @DisplayName("Should return empty list when all tables are booked")
    void testFindAvailableTables_WithAllBooked_ShouldReturnEmpty() {
        LocalDateTime bookingTime = LocalDateTime.now().plusDays(1);
        Integer guestCount = 4;
        List<RestaurantTable> allTables = Arrays.asList(table1, table2);
        
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findByRestaurantAndCapacityGreaterThanEqual(1, guestCount))
            .thenReturn(allTables);
        when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any()))
            .thenReturn(true); // All tables booked

        List<RestaurantTable> result = bookingService.findAvailableTables(1, bookingTime, guestCount);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should throw exception when restaurant not found")
    void testFindAvailableTables_WithInvalidRestaurant_ShouldThrowException() {
        when(restaurantProfileRepository.findById(999)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.findAvailableTables(999, LocalDateTime.now().plusDays(1), 4);
        });
        assertEquals("Restaurant not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should filter tables by capacity")
    void testFindAvailableTables_WithCapacityFilter_ShouldFilter() {
        LocalDateTime bookingTime = LocalDateTime.now().plusDays(1);
        Integer guestCount = 5;
        
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findByRestaurantAndCapacityGreaterThanEqual(1, guestCount))
            .thenReturn(Arrays.asList(table2)); // Only table2 has capacity >= 5
        when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any()))
            .thenReturn(false);

        List<RestaurantTable> result = bookingService.findAvailableTables(1, bookingTime, guestCount);

        assertEquals(1, result.size());
        assertEquals(table2.getTableId(), result.get(0).getTableId());
    }

    // ==================== GET BOOKING COUNT BY STATUS TESTS ====================

    @Test
    @DisplayName("Should get booking count by status successfully")
    void testGetBookingCountByStatus_WithValidStatus_ShouldReturnCount() {
        when(bookingRepository.countByStatus(BookingStatus.PENDING)).thenReturn(5L);

        long result = bookingService.getBookingCountByStatus(BookingStatus.PENDING);

        assertEquals(5L, result);
        verify(bookingRepository).countByStatus(BookingStatus.PENDING);
    }

    @Test
    @DisplayName("Should return zero when no bookings with status")
    void testGetBookingCountByStatus_WithNoBookings_ShouldReturnZero() {
        when(bookingRepository.countByStatus(BookingStatus.COMPLETED)).thenReturn(0L);

        long result = bookingService.getBookingCountByStatus(BookingStatus.COMPLETED);

        assertEquals(0L, result);
    }

    @Test
    @DisplayName("Should get booking count for all statuses")
    void testGetBookingCountByStatus_WithAllStatuses_ShouldReturnCount() {
        when(bookingRepository.countByStatus(BookingStatus.PENDING)).thenReturn(10L);
        when(bookingRepository.countByStatus(BookingStatus.CONFIRMED)).thenReturn(5L);
        when(bookingRepository.countByStatus(BookingStatus.COMPLETED)).thenReturn(20L);
        when(bookingRepository.countByStatus(BookingStatus.CANCELLED)).thenReturn(3L);
        when(bookingRepository.countByStatus(BookingStatus.NO_SHOW)).thenReturn(2L);

        assertEquals(10L, bookingService.getBookingCountByStatus(BookingStatus.PENDING));
        assertEquals(5L, bookingService.getBookingCountByStatus(BookingStatus.CONFIRMED));
        assertEquals(20L, bookingService.getBookingCountByStatus(BookingStatus.COMPLETED));
        assertEquals(3L, bookingService.getBookingCountByStatus(BookingStatus.CANCELLED));
        assertEquals(2L, bookingService.getBookingCountByStatus(BookingStatus.NO_SHOW));
    }

    // ==================== GET BOOKING COUNT IN DATE RANGE TESTS ====================

    @Test
    @DisplayName("Should get booking count in date range successfully")
    void testGetBookingCountInDateRange_WithValidRange_ShouldReturnCount() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        when(bookingRepository.countByBookingTimeBetween(startDate, endDate)).thenReturn(10L);

        long result = bookingService.getBookingCountInDateRange(startDate, endDate);

        assertEquals(10L, result);
        verify(bookingRepository).countByBookingTimeBetween(startDate, endDate);
    }

    @Test
    @DisplayName("Should return zero when no bookings in range")
    void testGetBookingCountInDateRange_WithNoBookings_ShouldReturnZero() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        when(bookingRepository.countByBookingTimeBetween(startDate, endDate)).thenReturn(0L);

        long result = bookingService.getBookingCountInDateRange(startDate, endDate);

        assertEquals(0L, result);
    }

    @Test
    @DisplayName("Should handle date range with same start and end")
    void testGetBookingCountInDateRange_WithSameDates_ShouldReturnCount() {
        LocalDateTime date = LocalDateTime.now();
        when(bookingRepository.countByBookingTimeBetween(date, date)).thenReturn(0L);

        long result = bookingService.getBookingCountInDateRange(date, date);

        assertEquals(0L, result);
    }

    @Test
    @DisplayName("Should handle date range with end before start")
    void testGetBookingCountInDateRange_WithEndBeforeStart_ShouldReturnCount() {
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().minusDays(7);
        when(bookingRepository.countByBookingTimeBetween(startDate, endDate)).thenReturn(0L);

        long result = bookingService.getBookingCountInDateRange(startDate, endDate);

        assertEquals(0L, result);
    }

    // ==================== FIND BOOKING BY ID TESTS ====================

    @Test
    @DisplayName("Should find booking by id successfully")
    void testFindBookingById_WithValidId_ShouldReturnBooking() {
        Optional<Booking> result = bookingService.findBookingById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getBookingId());
        verify(bookingRepository).findById(1);
    }

    @Test
    @DisplayName("Should return empty when booking not found")
    void testFindBookingById_WithInvalidId_ShouldReturnEmpty() {
        when(bookingRepository.findById(999)).thenReturn(Optional.empty());

        Optional<Booking> result = bookingService.findBookingById(999);

        assertFalse(result.isPresent());
    }
}

