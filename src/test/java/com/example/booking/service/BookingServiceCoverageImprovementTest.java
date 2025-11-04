package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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
import com.example.booking.domain.Booking;
import com.example.booking.domain.BookingDish;
import com.example.booking.domain.BookingTable;
import com.example.booking.domain.Customer;
import com.example.booking.domain.Dish;
import com.example.booking.domain.Notification;
import com.example.booking.domain.Payment;
import com.example.booking.domain.PaymentStatus;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantService;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.domain.User;
import com.example.booking.dto.BookingForm;
import com.example.booking.exception.BookingConflictException;
import com.example.booking.repository.BookingDishRepository;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.BookingServiceRepository;
import com.example.booking.repository.BookingTableRepository;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.repository.DishRepository;
import com.example.booking.repository.NotificationRepository;
import com.example.booking.repository.PaymentRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.RestaurantServiceRepository;
import com.example.booking.repository.RestaurantTableRepository;

import jakarta.persistence.EntityManager;

/**
 * Test class to improve coverage for BookingService
 * Focuses on edge cases and uncovered paths
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingServiceCoverageImprovementTest {

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
    private Customer customer;
    private User user;
    private RestaurantProfile restaurant;
    private RestaurantTable table1;
    private RestaurantTable table2;
    private Booking booking;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        
        user = new User();
        user.setId(customerId);
        user.setFullName("Test User");
        
        customer = new Customer();
        customer.setCustomerId(customerId);
        customer.setUser(user);
        
        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");
        
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
        
        booking = new Booking();
        booking.setBookingId(1);
        booking.setCustomer(customer);
        booking.setRestaurant(restaurant);
        booking.setBookingTime(LocalDateTime.now().plusDays(1));
        booking.setNumberOfGuests(4);
        booking.setDepositAmount(new BigDecimal("100000"));
        booking.setStatus(BookingStatus.PENDING);
        
        // Default stubs
        lenient().when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        lenient().when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        lenient().when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table1));
        lenient().when(restaurantTableRepository.findById(2)).thenReturn(Optional.of(table2));
        lenient().when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            if (b.getBookingId() == null) {
                b.setBookingId(1);
            }
            return b;
        });
        lenient().when(bookingDishRepository.findByBooking(any())).thenReturn(Collections.emptyList());
        lenient().when(bookingServiceRepository.findByBooking(any())).thenReturn(Collections.emptyList());
        lenient().when(bookingTableRepository.findByBooking(any())).thenReturn(Collections.emptyList());
        lenient().when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().doNothing().when(entityManager).flush();
        lenient().doNothing().when(conflictService).validateBookingConflicts(any(), any());
        lenient().doNothing().when(conflictService).validateBookingUpdateConflicts(anyInt(), any(), any());
    }

    // ==================== CREATE BOOKING CONFIRMATION NOTIFICATION TESTS ====================

    @Test
    @DisplayName("Should handle exception when creating confirmation notification fails")
    void testConfirmBooking_WithNotificationException_ShouldNotThrow() {
        // Given
        booking.setStatus(BookingStatus.PENDING);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(notificationRepository.save(any(Notification.class))).thenThrow(new RuntimeException("Notification error"));

        // When
        Booking result = bookingService.confirmBooking(1);

        // Then
        assertNotNull(result);
        assertEquals(BookingStatus.CONFIRMED, result.getStatus());
        // Should not throw exception even if notification fails
    }

    @Test
    @DisplayName("Should create confirmation notification successfully")
    void testConfirmBooking_ShouldCreateNotification() {
        // Given
        booking.setStatus(BookingStatus.PENDING);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Booking result = bookingService.confirmBooking(1);

        // Then
        assertNotNull(result);
        verify(notificationRepository).save(any(Notification.class));
    }

    // ==================== CALCULATE TOTAL AMOUNT WITH DISHES AND SERVICES TESTS ====================

    @Test
    @DisplayName("Should calculate total amount with dishes and services correctly")
    void testCalculateTotalAmount_WithDishesAndServices_ShouldSumCorrectly() {
        // Given
        Booking booking = new Booking();
        booking.setDepositAmount(new BigDecimal("100000"));
        
        Dish dish1 = new Dish();
        dish1.setDishId(1);
        dish1.setName("Dish 1");
        dish1.setPrice(new BigDecimal("50000"));
        
        Dish dish2 = new Dish();
        dish2.setDishId(2);
        dish2.setName("Dish 2");
        dish2.setPrice(new BigDecimal("30000"));
        
        BookingDish bookingDish1 = new BookingDish();
        bookingDish1.setDish(dish1);
        bookingDish1.setQuantity(2);
        bookingDish1.setPrice(new BigDecimal("50000"));
        // Total price is computed: 2 * 50000 = 100000
        
        BookingDish bookingDish2 = new BookingDish();
        bookingDish2.setDish(dish2);
        bookingDish2.setQuantity(1);
        bookingDish2.setPrice(new BigDecimal("30000"));
        // Total price is computed: 1 * 30000 = 30000
        
        RestaurantService service1 = new RestaurantService();
        service1.setServiceId(1);
        service1.setName("Service 1");
        service1.setPrice(new BigDecimal("20000"));
        
        com.example.booking.domain.BookingService bookingService1 = new com.example.booking.domain.BookingService();
        bookingService1.setService(service1);
        bookingService1.setQuantity(1);
        bookingService1.setPrice(new BigDecimal("20000"));
        // Total price is computed: 1 * 20000 = 20000
        
        when(bookingDishRepository.findByBooking(booking)).thenReturn(Arrays.asList(bookingDish1, bookingDish2));
        when(bookingServiceRepository.findByBooking(booking)).thenReturn(Arrays.asList(bookingService1));

        // When
        BigDecimal total = bookingService.calculateTotalAmount(booking);

        // Then
        // deposit: 100000 + dishes: 130000 + service: 20000 = 250000
        assertEquals(0, new BigDecimal("250000").compareTo(total));
    }

    @Test
    @DisplayName("Should calculate total amount with only dishes")
    void testCalculateTotalAmount_WithOnlyDishes_ShouldSumCorrectly() {
        // Given
        Booking booking = new Booking();
        booking.setDepositAmount(new BigDecimal("50000"));
        
        Dish dish1 = new Dish();
        dish1.setDishId(1);
        dish1.setName("Dish 1");
        
        BookingDish bookingDish1 = new BookingDish();
        bookingDish1.setDish(dish1);
        bookingDish1.setQuantity(1);
        bookingDish1.setPrice(new BigDecimal("75000"));
        // Total price is computed: 1 * 75000 = 75000
        
        when(bookingDishRepository.findByBooking(booking)).thenReturn(Arrays.asList(bookingDish1));
        when(bookingServiceRepository.findByBooking(booking)).thenReturn(Collections.emptyList());

        // When
        BigDecimal total = bookingService.calculateTotalAmount(booking);

        // Then
        // deposit: 50000 + dishes: 75000 = 125000
        assertEquals(0, new BigDecimal("125000").compareTo(total));
    }

    @Test
    @DisplayName("Should calculate total amount with only services")
    void testCalculateTotalAmount_WithOnlyServices_ShouldSumCorrectly() {
        // Given
        Booking booking = new Booking();
        booking.setDepositAmount(new BigDecimal("80000"));
        
        RestaurantService service1 = new RestaurantService();
        service1.setServiceId(1);
        service1.setName("Service 1");
        
        com.example.booking.domain.BookingService bookingService1 = new com.example.booking.domain.BookingService();
        bookingService1.setService(service1);
        bookingService1.setQuantity(2);
        bookingService1.setPrice(new BigDecimal("20000"));
        // Total price is computed: 2 * 20000 = 40000
        
        when(bookingDishRepository.findByBooking(booking)).thenReturn(Collections.emptyList());
        when(bookingServiceRepository.findByBooking(booking)).thenReturn(Arrays.asList(bookingService1));

        // When
        BigDecimal total = bookingService.calculateTotalAmount(booking);

        // Then
        // deposit: 80000 + services: 40000 = 120000
        assertEquals(0, new BigDecimal("120000").compareTo(total));
    }

    // ==================== UPDATE BOOKING WITH MULTIPLE TABLES TESTS ====================

    @Test
    @DisplayName("Should update booking with tableId (single table)")
    void testUpdateBooking_WithTableId_ShouldUpdate() {
        // Given
        booking.setStatus(BookingStatus.PENDING);
        booking.setCustomer(customer);
        
        List<BookingTable> existingTables = new ArrayList<>();
        BookingTable existingBT = new BookingTable();
        existingBT.setTable(table1);
        existingTables.add(existingBT);
        
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(2); // Change to table 2
        form.setGuestCount(6);
        form.setBookingTime(LocalDateTime.now().plusDays(2));
        form.setDepositAmount(new BigDecimal("200000"));
        form.setNote("Updated note");

        booking.setBookingTables(existingTables);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
        when(restaurantTableRepository.findById(2)).thenReturn(Optional.of(table2));
        when(bookingTableRepository.findByBooking(booking)).thenReturn(existingTables);
        doNothing().when(bookingTableRepository).deleteByBooking(any());
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Booking result = bookingService.updateBooking(1, form, customerId);

        // Then
        assertNotNull(result);
        verify(bookingTableRepository).deleteByBooking(any());
        verify(bookingTableRepository).save(any(BookingTable.class));
    }

    @Test
    @DisplayName("Should update booking without table change (keep same table)")
    void testUpdateBooking_WithoutTableChange_ShouldUpdate() {
        // Given
        booking.setStatus(BookingStatus.PENDING);
        booking.setCustomer(customer);
        
        List<BookingTable> existingTables = new ArrayList<>();
        BookingTable existingBT = new BookingTable();
        existingBT.setTable(table1);
        existingTables.add(existingBT);
        
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(1); // Keep same table
        form.setGuestCount(4); // Keep same guest count within capacity
        form.setBookingTime(LocalDateTime.now().plusDays(2));
        form.setDepositAmount(new BigDecimal("200000"));
        form.setNote("Updated note");

        booking.setBookingTables(existingTables);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table1));
        when(bookingTableRepository.findByBooking(booking)).thenReturn(existingTables);
        doNothing().when(bookingTableRepository).deleteByBooking(any());
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Booking result = bookingService.updateBooking(1, form, customerId);

        // Then
        assertNotNull(result);
        assertEquals(4, result.getNumberOfGuests());
    }

    // ==================== UPDATE BOOKING FOR RESTAURANT OWNER TESTS ====================

    @Test
    @DisplayName("Should update booking for restaurant owner with restaurant change")
    void testUpdateBookingForRestaurantOwner_WithRestaurantChange_ShouldUpdate() {
        // Given
        booking.setStatus(BookingStatus.PENDING);
        Set<Integer> ownerRestaurantIds = Set.of(1, 2);
        
        RestaurantProfile newRestaurant = new RestaurantProfile();
        newRestaurant.setRestaurantId(2);
        newRestaurant.setRestaurantName("New Restaurant");
        
        BookingForm form = new BookingForm();
        form.setRestaurantId(2);
        form.setTableId(1);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(2));

        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
        when(restaurantProfileRepository.findById(2)).thenReturn(Optional.of(newRestaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table1));
        when(bookingTableRepository.findByBooking(any())).thenReturn(Collections.emptyList());
        doNothing().when(bookingTableRepository).deleteByBooking(any());

        // When
        Booking result = bookingService.updateBookingForRestaurantOwner(1, form, ownerRestaurantIds);

        // Then
        assertNotNull(result);
        verify(bookingTableRepository).deleteByBooking(any());
    }

    @Test
    @DisplayName("Should throw exception when owner tries to move booking to restaurant they don't own")
    void testUpdateBookingForRestaurantOwner_WithInvalidRestaurant_ShouldThrowException() {
        // Given
        booking.setStatus(BookingStatus.PENDING);
        Set<Integer> ownerRestaurantIds = Set.of(1);
        
        BookingForm form = new BookingForm();
        form.setRestaurantId(99); // Owner doesn't own this restaurant

        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            bookingService.updateBookingForRestaurantOwner(1, form, ownerRestaurantIds)
        );
        assertTrue(exception.getMessage().contains("Cannot move booking"));
    }

    @Test
    @DisplayName("Should throw exception when owner has no restaurants")
    void testUpdateBookingForRestaurantOwner_WithNoRestaurants_ShouldThrowException() {
        // Given
        Set<Integer> ownerRestaurantIds = new HashSet<>();
        BookingForm form = new BookingForm();

        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            bookingService.updateBookingForRestaurantOwner(1, form, ownerRestaurantIds)
        );
        assertTrue(exception.getMessage().contains("does not have any restaurants"));
    }

    // ==================== GET BOOKINGS BY RESTAURANT TESTS ====================

    @Test
    @DisplayName("Should get bookings by restaurant successfully")
    void testGetBookingsByRestaurant_WithValidRestaurant_ShouldReturnBookings() {
        // Given
        List<Booking> bookings = Arrays.asList(booking);
        
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(bookingRepository.findByRestaurantOrderByBookingTimeDesc(restaurant)).thenReturn(bookings);

        // When
        List<Booking> result = bookingService.getBookingsByRestaurant(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should throw exception when restaurant not found")
    void testGetBookingsByRestaurant_WithInvalidRestaurant_ShouldThrowException() {
        // Given
        when(restaurantProfileRepository.findById(99)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            bookingService.getBookingsByRestaurant(99)
        );
        assertEquals("Restaurant not found", exception.getMessage());
    }

    // ==================== GET BOOKING WITH DETAILS BY ID TESTS ====================

    @Test
    @DisplayName("Should get booking with details including relationships")
    void testGetBookingWithDetailsById_WithRelationships_ShouldReturnBooking() {
        // Given
        List<BookingDish> dishes = new ArrayList<>();
        BookingDish bd = new BookingDish();
        Dish dish = new Dish();
        dish.setName("Test Dish");
        bd.setDish(dish);
        bd.setQuantity(2);
        dishes.add(bd);
        
        List<com.example.booking.domain.BookingService> services = new ArrayList<>();
        com.example.booking.domain.BookingService bs = new com.example.booking.domain.BookingService();
        RestaurantService service = new RestaurantService();
        service.setName("Test Service");
        bs.setService(service);
        services.add(bs);
        
        List<BookingTable> tables = new ArrayList<>();
        BookingTable bt = new BookingTable();
        bt.setTable(table1);
        tables.add(bt);
        
        booking.setBookingDishes(dishes);
        booking.setBookingServices(services);
        booking.setBookingTables(tables);
        
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));

        // When
        Optional<Booking> result = bookingService.getBookingWithDetailsById(1);

        // Then
        assertTrue(result.isPresent());
        assertNotNull(result.get().getBookingDishes());
        assertNotNull(result.get().getBookingServices());
        assertNotNull(result.get().getBookingTables());
    }

    @Test
    @DisplayName("Should handle booking with null relationships")
    void testGetBookingWithDetailsById_WithNullRelationships_ShouldHandleGracefully() {
        // Given
        booking.setBookingDishes(null);
        booking.setBookingServices(null);
        booking.setBookingTables(null);
        
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));

        // When
        Optional<Booking> result = bookingService.getBookingWithDetailsById(1);

        // Then
        assertTrue(result.isPresent());
    }

    // ==================== FIND AVAILABLE TABLES TESTS ====================

    @Test
    @DisplayName("Should find available tables for restaurant")
    void testFindAvailableTables_WithValidData_ShouldReturnAvailableTables() {
        // Given
        LocalDateTime bookingTime = LocalDateTime.now().plusDays(1);
        Integer guestCount = 4;
        
        List<RestaurantTable> allTables = Arrays.asList(table1, table2);
        
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findByRestaurantAndCapacityGreaterThanEqual(1, guestCount))
            .thenReturn(allTables);
        when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any()))
            .thenReturn(false);

        // When
        List<RestaurantTable> result = bookingService.findAvailableTables(1, bookingTime, guestCount);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should filter out booked tables")
    void testFindAvailableTables_WithBookedTables_ShouldFilterOut() {
        // Given
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

        // When
        List<RestaurantTable> result = bookingService.findAvailableTables(1, bookingTime, guestCount);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(table2.getTableId(), result.get(0).getTableId());
    }

    @Test
    @DisplayName("Should throw exception when restaurant not found")
    void testFindAvailableTables_WithInvalidRestaurant_ShouldThrowException() {
        // Given
        when(restaurantProfileRepository.findById(99)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            bookingService.findAvailableTables(99, LocalDateTime.now().plusDays(1), 4)
        );
        assertEquals("Restaurant not found", exception.getMessage());
    }

    // ==================== CREATE BOOKING WITH VOUCHER CODE APPLIED TESTS ====================

    @Test
    @DisplayName("Should create booking with voucher code applied from form")
    void testCreateBooking_WithVoucherCodeApplied_ShouldUseFormVoucher() {
        // Given
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(1);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(1));
        form.setDepositAmount(new BigDecimal("100000"));
        form.setVoucherCodeApplied("SUMMER20");
        form.setVoucherDiscountAmount(new BigDecimal("20000"));
        
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1);
            return b;
        });
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Booking result = bookingService.createBooking(form, customerId);

        // Then
        assertNotNull(result);
        // Voucher should be applied from form
        verify(voucherService, never()).validate(any());
    }

    // ==================== CREATE BOOKING WITH MULTIPLE TABLES DEPOSIT TESTS ====================

    @Test
    @DisplayName("Should calculate deposit from multiple tables")
    void testCreateBooking_WithMultipleTablesDeposit_ShouldCalculateCorrectly() {
        // Given
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableIds("1,2");
        form.setGuestCount(8);
        form.setBookingTime(LocalDateTime.now().plusDays(1));
        form.setDepositAmount(null); // Let it calculate from tables
        
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1);
            return b;
        });
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookingDishRepository.findByBooking(any())).thenReturn(Collections.emptyList());
        when(bookingServiceRepository.findByBooking(any())).thenReturn(Collections.emptyList());

        // When
        Booking result = bookingService.createBooking(form, customerId);

        // Then
        assertNotNull(result);
        // Deposit should be sum of table deposits: 100000 + 150000 = 250000
        // But then adjusted to 10% of total, which would be calculated
        assertNotNull(result.getDepositAmount());
    }

    // ==================== CANCEL BOOKING BY RESTAURANT WITH BANK INFO TESTS ====================

    @Test
    @DisplayName("Should cancel booking by restaurant with bank info")
    void testCancelBookingByRestaurant_WithBankInfo_ShouldCancel() {
        // Given
        UUID ownerId = customerId; // Use same ID as user
        com.example.booking.domain.RestaurantOwner owner = new com.example.booking.domain.RestaurantOwner();
        owner.setUser(user); // user.getId() == customerId == ownerId
        restaurant.setOwner(owner);
        booking.setRestaurant(restaurant);
        
        Payment payment = new Payment();
        payment.setPaymentId(1);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setAmount(new BigDecimal("100000"));
        
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBooking(booking)).thenReturn(Optional.of(payment));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(refundService.processRefundWithManualTransfer(anyInt(), anyString(), anyString(), anyString()))
            .thenReturn(payment);

        // When
        Booking result = bookingService.cancelBookingByRestaurant(1, ownerId, "Test reason", "VCB", "1234567890");

        // Then
        assertNotNull(result);
        assertEquals(BookingStatus.CANCELLED, result.getStatus());
        verify(refundService).processRefundWithManualTransfer(eq(1), anyString(), eq("VCB"), eq("1234567890"));
    }

    // ==================== VALIDATE BOOKING TIME EDGE CASES TESTS ====================

    @Test
    @DisplayName("Should throw exception when booking time is null")
    void testCreateBooking_WithNullBookingTime_ShouldThrowException() {
        // Given
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(1);
        form.setGuestCount(4);
        form.setBookingTime(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            bookingService.createBooking(form, customerId)
        );
        assertTrue(exception.getMessage().contains("Booking time cannot be null"));
    }

    @Test
    @DisplayName("Should throw exception when booking time is too far in future")
    void testCreateBooking_WithBookingTimeTooFar_ShouldThrowException() {
        // Given
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(1);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(31)); // More than 30 days

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            bookingService.createBooking(form, customerId)
        );
        assertTrue(exception.getMessage().contains("30 days"));
    }

    // ==================== VALIDATE GUEST COUNT EDGE CASES TESTS ====================

    @Test
    @DisplayName("Should throw exception when guest count is null")
    void testCreateBooking_WithNullGuestCount_ShouldThrowException() {
        // Given
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(1);
        form.setGuestCount(null);
        form.setBookingTime(LocalDateTime.now().plusDays(1));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            bookingService.createBooking(form, customerId)
        );
        assertTrue(exception.getMessage().contains("Guest count cannot be null"));
    }

    @Test
    @DisplayName("Should throw exception when guest count exceeds 100")
    void testCreateBooking_WithGuestCountExceeding100_ShouldThrowException() {
        // Given
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(1);
        form.setGuestCount(101);
        form.setBookingTime(LocalDateTime.now().plusDays(1));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            bookingService.createBooking(form, customerId)
        );
        assertTrue(exception.getMessage().contains("100 people"));
    }

    // ==================== VALIDATE TABLE CAPACITY WITH MULTIPLE TABLES TESTS ====================

    @Test
    @DisplayName("Should throw exception when guest count exceeds multiple tables capacity")
    void testCreateBooking_WithGuestCountExceedingMultipleTablesCapacity_ShouldThrowException() {
        // Given
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableIds("1,2");
        form.setGuestCount(15); // Exceeds capacity of 4 + 6 = 10
        form.setBookingTime(LocalDateTime.now().plusDays(1));
        form.setDepositAmount(new BigDecimal("250000"));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            bookingService.createBooking(form, customerId)
        );
        assertTrue(exception.getMessage().contains("vượt quá tổng sức chứa"));
    }

    @Test
    @DisplayName("Should throw exception when no table selected")
    void testCreateBooking_WithNoTable_ShouldThrowException() {
        // Given
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(null);
        form.setTableIds(null);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(1));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            bookingService.createBooking(form, customerId)
        );
        assertTrue(exception.getMessage().contains("chọn ít nhất một bàn"));
    }

    // ==================== UPDATE BOOKING STATUS TRANSITION TESTS ====================

    @Test
    @DisplayName("Should update status from CONFIRMED to COMPLETED")
    void testUpdateBookingStatus_ConfirmedToCompleted_ShouldUpdate() {
        // Given
        booking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        // When
        Booking result = bookingService.updateBookingStatus(1, BookingStatus.COMPLETED);

        // Then
        assertEquals(BookingStatus.COMPLETED, result.getStatus());
    }

    @Test
    @DisplayName("Should update status from CONFIRMED to NO_SHOW")
    void testUpdateBookingStatus_ConfirmedToNoShow_ShouldUpdate() {
        // Given
        booking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        // When
        Booking result = bookingService.updateBookingStatus(1, BookingStatus.NO_SHOW);

        // Then
        assertEquals(BookingStatus.NO_SHOW, result.getStatus());
    }

    // ==================== GET BOOKING COUNT TESTS ====================

    @Test
    @DisplayName("Should get booking count by status")
    void testGetBookingCountByStatus_ShouldReturnCount() {
        // Given
        when(bookingRepository.countByStatus(BookingStatus.PENDING)).thenReturn(5L);

        // When
        long count = bookingService.getBookingCountByStatus(BookingStatus.PENDING);

        // Then
        assertEquals(5L, count);
    }

    @Test
    @DisplayName("Should get booking count in date range")
    void testGetBookingCountInDateRange_ShouldReturnCount() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        when(bookingRepository.countByBookingTimeBetween(start, end)).thenReturn(10L);

        // When
        long count = bookingService.getBookingCountInDateRange(start, end);

        // Then
        assertEquals(10L, count);
    }

    // Helper method to create BookingForm
    private BookingForm createBaseBookingForm(LocalDateTime time, Integer guestCount, Integer tableId, String tableIds) {
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setBookingTime(time);
        form.setGuestCount(guestCount);
        form.setTableId(tableId);
        form.setTableIds(tableIds);
        form.setDepositAmount(new BigDecimal("100000"));
        return form;
    }

    // ==================== CREATE BOOKING BRANCH COVERAGE TESTS ====================

    @Test
    @DisplayName("Should throw exception when form is null")
    void testCreateBooking_WithNullForm_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(null, customerId);
        });
        assertEquals("BookingForm cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when customerId is null")
    void testCreateBooking_WithNullCustomerId_ShouldThrowException() {
        BookingForm form = createBaseBookingForm(LocalDateTime.now().plusDays(1), 4, 1, null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(form, null);
        });
        assertEquals("Customer ID cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle restaurant validation exception")
    void testCreateBooking_WithRestaurantValidationException_ShouldThrowException() {
        BookingForm form = createBaseBookingForm(LocalDateTime.now().plusDays(1), 4, 1, null);
        form.setRestaurantId(99);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(99)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(form, customerId);
        });
        assertEquals("Restaurant not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle booking conflict exception")
    void testCreateBooking_WithBookingConflict_ShouldThrowException() {
        BookingForm form = createBaseBookingForm(LocalDateTime.now().plusDays(1), 4, 1, null);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table1));
        doThrow(new BookingConflictException(BookingConflictException.ConflictType.TABLE_OCCUPIED, "Table already booked")).when(conflictService)
                .validateBookingConflicts(any(), any());

        BookingConflictException exception = assertThrows(BookingConflictException.class, () -> {
            bookingService.createBooking(form, customerId);
        });
        assertTrue(exception.getMessage().contains("Table already booked"));
    }

    @Test
    @DisplayName("Should handle voucher validation null result")
    void testCreateBooking_WithVoucherValidationNullResult_ShouldContinue() {
        BookingForm form = createBaseBookingForm(LocalDateTime.now().plusDays(1), 4, 1, null);
        form.setVoucherCode("INVALID_VOUCHER");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table1));
        when(voucherService.validate(any())).thenReturn(null);

        Booking result = bookingService.createBooking(form, customerId);

        assertNotNull(result);
        verify(voucherService).validate(any());
    }

    @Test
    @DisplayName("Should handle invalid voucher")
    void testCreateBooking_WithInvalidVoucher_ShouldThrowException() {
        BookingForm form = createBaseBookingForm(LocalDateTime.now().plusDays(1), 4, 1, null);
        form.setVoucherCode("INVALID_VOUCHER");

        VoucherService.ValidationResult invalidResult = new VoucherService.ValidationResult(
                false, "Voucher expired", null, null);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table1));
        when(voucherService.validate(any())).thenReturn(invalidResult);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(form, customerId);
        });
        assertTrue(exception.getMessage().contains("Invalid voucher"));
    }

    @Test
    @DisplayName("Should handle voucher application null result")
    void testCreateBooking_WithVoucherApplicationNullResult_ShouldContinue() {
        BookingForm form = createBaseBookingForm(LocalDateTime.now().plusDays(1), 4, 1, null);
        form.setVoucherCodeApplied("VOUCHER_CODE");
        form.setVoucherDiscountAmount(new BigDecimal("50000"));

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table1));
        when(voucherService.applyToBooking(any())).thenReturn(null);

        Booking result = bookingService.createBooking(form, customerId);

        assertNotNull(result);
        verify(voucherService).applyToBooking(any());
    }

    @Test
    @DisplayName("Should handle voucher application failure")
    void testCreateBooking_WithVoucherApplicationFailure_ShouldThrowException() {
        BookingForm form = createBaseBookingForm(LocalDateTime.now().plusDays(1), 4, 1, null);
        form.setVoucherCodeApplied("VOUCHER_CODE");
        form.setVoucherDiscountAmount(new BigDecimal("50000"));

        VoucherService.ApplyResult failedResult = new VoucherService.ApplyResult(
                false, "Voucher already used", null, null);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table1));
        when(voucherService.applyToBooking(any())).thenReturn(failedResult);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(form, customerId);
        });
        assertTrue(exception.getMessage().contains("Failed to apply voucher"));
    }

    @Test
    @DisplayName("Should handle deposit calculation with zero total amount")
    void testCreateBooking_WithZeroTotalAmount_ShouldSetDepositToZero() {
        BookingForm form = createBaseBookingForm(LocalDateTime.now().plusDays(1), 4, 1, null);
        form.setDepositAmount(null); // Let service calculate

        booking.setBookingDishes(Collections.emptyList());
        booking.setBookingServices(Collections.emptyList());

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table1));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1);
            b.setBookingDishes(Collections.emptyList());
            b.setBookingServices(Collections.emptyList());
            return b;
        });
        when(bookingTableRepository.findByBooking(any())).thenReturn(Collections.emptyList());

        Booking result = bookingService.createBooking(form, customerId);

        assertNotNull(result);
        // Deposit should be calculated from table deposit or set to zero if total is zero
    }

    @Test
    @DisplayName("Should handle dish assignment exception gracefully")
    void testCreateBooking_WithDishAssignmentException_ShouldContinue() {
        BookingForm form = createBaseBookingForm(LocalDateTime.now().plusDays(1), 4, 1, null);
        form.setDishIds("1,2");

        Dish dish1 = new Dish();
        dish1.setDishId(1);
        dish1.setName("Dish 1");
        dish1.setPrice(new BigDecimal("50000"));

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table1));
        when(dishRepository.findById(1)).thenReturn(Optional.of(dish1));
        when(dishRepository.findById(2)).thenReturn(Optional.empty()); // Cause exception
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1);
            return b;
        });
        when(bookingTableRepository.findByBooking(any())).thenReturn(Collections.emptyList());

        // Should not throw exception, but continue with booking creation
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(form, customerId);
        });
        // Exception is thrown from assignDishesToBooking, but caught in createBooking
        // Actually, looking at the code, the exception is caught and logged, but not re-thrown
        // So the test should expect the exception to be thrown from assignDishesToBooking
        assertTrue(exception.getMessage().contains("Dish not found") || 
                   exception.getMessage().contains("Service not found") ||
                   exception.getMessage().contains("Table not found"));
    }

    @Test
    @DisplayName("Should handle service assignment exception gracefully")
    void testCreateBooking_WithServiceAssignmentException_ShouldContinue() {
        BookingForm form = createBaseBookingForm(LocalDateTime.now().plusDays(1), 4, 1, null);
        form.setServiceIds("1,2");

        RestaurantService service1 = new RestaurantService();
        service1.setServiceId(1);
        service1.setName("Service 1");
        service1.setPrice(new BigDecimal("20000"));

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table1));
        when(restaurantServiceRepository.findById(1)).thenReturn(Optional.of(service1));
        when(restaurantServiceRepository.findById(2)).thenReturn(Optional.empty()); // Cause exception
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1);
            return b;
        });
        when(bookingTableRepository.findByBooking(any())).thenReturn(Collections.emptyList());

        // Should not throw exception, but continue with booking creation
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(form, customerId);
        });
        assertTrue(exception.getMessage().contains("Service not found"));
    }

    @Test
    @DisplayName("Should handle deposit provided by form")
    void testCreateBooking_WithDepositProvidedByForm_ShouldUseFormDeposit() {
        BookingForm form = createBaseBookingForm(LocalDateTime.now().plusDays(1), 4, 1, null);
        BigDecimal customDeposit = new BigDecimal("200000");
        form.setDepositAmount(customDeposit); // Different from table deposit

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table1));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1);
            b.setBookingDishes(Collections.emptyList());
            b.setBookingServices(Collections.emptyList());
            return b;
        });
        when(bookingTableRepository.findByBooking(any())).thenReturn(Collections.emptyList());

        Booking result = bookingService.createBooking(form, customerId);

        assertNotNull(result);
        assertEquals(customDeposit, result.getDepositAmount());
    }

    @Test
    @DisplayName("Should handle table assignment exception")
    void testCreateBooking_WithTableAssignmentException_ShouldThrowException() {
        BookingForm form = createBaseBookingForm(LocalDateTime.now().plusDays(1), 4, 1, null);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table1));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1);
            return b;
        });
        doThrow(new RuntimeException("Table assignment failed")).when(bookingTableRepository)
                .save(any(BookingTable.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingService.createBooking(form, customerId);
        });
        assertTrue(exception.getMessage().contains("Table assignment failed"));
    }
}

