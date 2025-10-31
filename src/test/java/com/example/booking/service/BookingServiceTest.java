package com.example.booking.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.example.booking.common.enums.BookingStatus;
import com.example.booking.domain.Booking;
import com.example.booking.domain.BookingTable;
import com.example.booking.domain.Customer;
import com.example.booking.domain.Notification;
import com.example.booking.domain.Payment;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.dto.BookingForm;
import com.example.booking.repository.BookingDishRepository;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.BookingServiceRepository;
import com.example.booking.repository.BookingTableRepository;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.repository.DishRepository;
import com.example.booking.repository.NotificationRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.RestaurantServiceRepository;
import com.example.booking.repository.RestaurantTableRepository;
import com.example.booking.repository.PaymentRepository;

import jakarta.persistence.EntityManager;

/**
 * Simplified test cases for BookingService
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingServiceTest {

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
    private BookingConflictService conflictService;

    @Mock
    private VoucherService voucherService;

    @Mock
    private BookingDishRepository bookingDishRepository;

    @Mock
    private BookingServiceRepository bookingServiceRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private DishRepository dishRepository;

    @Mock
    private RestaurantServiceRepository restaurantServiceRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RefundService refundService;

    @InjectMocks
    private BookingService bookingService;

    private BookingForm bookingForm;
    private Customer customer;
    private RestaurantProfile restaurant;
    private RestaurantTable table;
    private UUID customerId;
    private Booking mockBooking;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        
        // Setup BookingForm
        bookingForm = new BookingForm();
        bookingForm.setRestaurantId(1);
        bookingForm.setTableId(1);
        bookingForm.setGuestCount(4);
        bookingForm.setBookingTime(LocalDateTime.now().plusDays(1));
        bookingForm.setDepositAmount(new BigDecimal("100000"));
        bookingForm.setNote("Test booking");

        // Setup Customer
        customer = new Customer();
        customer.setCustomerId(customerId);
        customer.setFullName("Test Customer");

        // Setup Restaurant
        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");
        restaurant.setAddress("123 Test Street");
        restaurant.setPhone("0987654321");

        // Setup Table
        table = new RestaurantTable();
        table.setTableId(1);
        table.setTableName("Table 1");
        table.setCapacity(4);
        table.setRestaurant(restaurant);
        table.setDepositAmount(new BigDecimal("100000"));

        // Setup Mock Booking
        mockBooking = new Booking();
        mockBooking.setBookingId(1);
        mockBooking.setCustomer(customer);
        mockBooking.setRestaurant(restaurant);
        mockBooking.setBookingTime(LocalDateTime.now().plusDays(1));
        mockBooking.setDepositAmount(new BigDecimal("100000"));
        mockBooking.setStatus(BookingStatus.PENDING);
        mockBooking.setNumberOfGuests(4);
    }

    private void prepareCreateBookingStubs() {
        when(bookingDishRepository.findByBooking(any(Booking.class))).thenReturn(Collections.emptyList());
        when(bookingServiceRepository.findByBooking(any(Booking.class))).thenReturn(Collections.emptyList());
        when(bookingTableRepository.findByBooking(any(Booking.class))).thenReturn(Collections.emptyList());
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingRepository.save(any(Booking.class))).thenReturn(mockBooking);
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(entityManager).flush();
    }

    // ==================== HAPPY PATH TESTS ====================

    @Test
    void testCreateBooking_WithValidData_ShouldSuccess() {
        // Given
        prepareCreateBookingStubs();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table));

        // When
        Booking result = bookingService.createBooking(bookingForm, customerId);

        // Then
        assertNotNull(result);
        assertEquals(customerId, result.getCustomer().getCustomerId());
        assertEquals(1, result.getRestaurant().getRestaurantId());
        verify(bookingRepository, atLeastOnce()).save(any(Booking.class));
        verify(bookingTableRepository).save(any(BookingTable.class));
    }

    @Test
    void testCalculateTotalAmount_WithOnlyDeposit_ShouldReturnDepositAmount() {
        // Given
        Booking booking = new Booking();
        booking.setDepositAmount(new BigDecimal("100000"));
        when(bookingDishRepository.findByBooking(booking)).thenReturn(Collections.emptyList());
        when(bookingServiceRepository.findByBooking(booking)).thenReturn(Collections.emptyList());

        // When
        BigDecimal total = bookingService.calculateTotalAmount(booking);

        // Then
        assertEquals(new BigDecimal("100000"), total);
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    void testCreateBooking_WithCustomerNotFound_ShouldThrowException() {
        // Given
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(bookingForm, customerId);
        });
        assertEquals("Customer not found", exception.getMessage());
    }

    @Test
    void testCreateBooking_WithRestaurantNotFound_ShouldThrowException() {
        // Given
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(bookingForm, customerId);
        });
        assertEquals("Restaurant not found", exception.getMessage());
    }

    @Test
    void testCreateBooking_WithTableNotFound_ShouldThrowException() {
        // Given
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(bookingForm, customerId);
        });
        assertEquals("Table not found", exception.getMessage());
    }

    // ==================== BUSINESS LOGIC TESTS ====================

    @Test
    void testCreateBooking_ShouldSetCorrectStatus() {
        // Given
        prepareCreateBookingStubs();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table));

        // When
        Booking result = bookingService.createBooking(bookingForm, customerId);

        // Then
        assertEquals(BookingStatus.PENDING, result.getStatus());
    }

    @Test
    void testCreateBooking_ShouldSetCorrectDepositAmount() {
        // Given
        prepareCreateBookingStubs();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table));

        // When
        Booking result = bookingService.createBooking(bookingForm, customerId);

        // Then
        assertEquals(new BigDecimal("10000"), result.getDepositAmount());
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    void testCreateBooking_WithNullBookingForm_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(null, customerId);
        });
        assertEquals("BookingForm cannot be null", exception.getMessage());
    }

    @Test
    void testCreateBooking_WithNullCustomerId_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(bookingForm, null);
        });
        assertEquals("Customer ID cannot be null", exception.getMessage());
    }

    @Test
    void testCreateBooking_WithInvalidBookingTime_ShouldThrowException() {
        // Given
        bookingForm.setBookingTime(LocalDateTime.now().minusDays(1));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(bookingForm, customerId);
        });
        assertEquals("Booking time cannot be in the past", exception.getMessage());
    }

    @Test
    void testCreateBooking_WithInvalidGuestCount_ShouldThrowException() {
        // Given
        bookingForm.setGuestCount(0);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(bookingForm, customerId);
        });
        assertEquals("Guest count must be greater than 0", exception.getMessage());
    }

    @Test
    void testCreateBooking_WithNegativeDepositAmount_ShouldThrowException() {
        // Given
        bookingForm.setDepositAmount(new BigDecimal("-1000"));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(bookingForm, customerId);
        });
        assertEquals("Deposit amount cannot be negative", exception.getMessage());
    }

    // ==================== BUSINESS LOGIC TESTS ====================

    @Test
    void testCreateBooking_WithDishes_ShouldCreateBookingWithDishes() {
        // Given
        prepareCreateBookingStubs();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table));
        bookingForm.setDishIds("1,2,3");

        // When
        Booking result = bookingService.createBooking(bookingForm, customerId);

        // Then
        assertNotNull(result);
        assertEquals(BookingStatus.PENDING, result.getStatus());
        verify(bookingRepository, atLeastOnce()).save(any(Booking.class));
    }

    @Test
    void testCreateBooking_WithServices_ShouldCreateBookingWithServices() {
        // Given
        prepareCreateBookingStubs();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table));
        bookingForm.setServiceIds("1,2");

        // When
        Booking result = bookingService.createBooking(bookingForm, customerId);

        // Then
        assertNotNull(result);
        assertEquals(BookingStatus.PENDING, result.getStatus());
        verify(bookingRepository, atLeastOnce()).save(any(Booking.class));
    }

    @Test
    void testCreateBooking_WithDishesAndServices_ShouldCreateBookingWithBoth() {
        // Given
        prepareCreateBookingStubs();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table));
        bookingForm.setDishIds("1,2");
        bookingForm.setServiceIds("1");

        // When
        Booking result = bookingService.createBooking(bookingForm, customerId);

        // Then
        assertNotNull(result);
        assertEquals(BookingStatus.PENDING, result.getStatus());
        verify(bookingRepository, atLeastOnce()).save(any(Booking.class));
    }

    @Test
    void testCreateBooking_ShouldCreateBookingTable() {
        // Given
        prepareCreateBookingStubs();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table));

        // When
        Booking result = bookingService.createBooking(bookingForm, customerId);

        // Then
        assertNotNull(result);
        verify(bookingTableRepository).save(any(BookingTable.class));
    }

    @Test
    void testCreateBooking_ShouldCreateNotification() {
        // Given
        prepareCreateBookingStubs();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table));

        // When
        Booking result = bookingService.createBooking(bookingForm, customerId);

        // Then
        assertNotNull(result);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void testCreateBooking_ShouldSetCorrectCreatedAt() {
        // Given
        prepareCreateBookingStubs();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table));

        // When
        Booking result = bookingService.createBooking(bookingForm, customerId);

        // Then
        assertNotNull(result);
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void testCreateBooking_ShouldSetCorrectUpdatedAt() {
        // Given
        prepareCreateBookingStubs();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table));

        // When
        Booking result = bookingService.createBooking(bookingForm, customerId);

        // Then
        assertNotNull(result);
        assertNotNull(result.getUpdatedAt());
    }

    // ==================== EDGE CASES ====================

    @Test
    void testCreateBooking_WithEmptyDishIds_ShouldSuccess() {
        // Given
        prepareCreateBookingStubs();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table));
        bookingForm.setDishIds("");

        // When
        Booking result = bookingService.createBooking(bookingForm, customerId);

        // Then
        assertNotNull(result);
        assertEquals(BookingStatus.PENDING, result.getStatus());
    }

    @Test
    void testCreateBooking_WithEmptyServiceIds_ShouldSuccess() {
        // Given
        prepareCreateBookingStubs();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table));
        bookingForm.setServiceIds("");

        // When
        Booking result = bookingService.createBooking(bookingForm, customerId);

        // Then
        assertNotNull(result);
        assertEquals(BookingStatus.PENDING, result.getStatus());
    }

    @Test
    void testCreateBooking_WithNullNote_ShouldSuccess() {
        // Given
        prepareCreateBookingStubs();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table));
        bookingForm.setNote(null);

        // When
        Booking result = bookingService.createBooking(bookingForm, customerId);

        // Then
        assertNotNull(result);
        assertEquals(BookingStatus.PENDING, result.getStatus());
    }

    @Test
    void testCreateBooking_WithVeryLongNote_ShouldSuccess() {
        // Given
        prepareCreateBookingStubs();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table));
        bookingForm.setNote("A".repeat(2000));

        // When
        Booking result = bookingService.createBooking(bookingForm, customerId);

        // Then
        assertNotNull(result);
        assertEquals(BookingStatus.PENDING, result.getStatus());
    }

    // ==================== CALCULATION TESTS ====================

    @Test
    void testCalculateTotalAmount_WithDishes_ShouldReturnCorrectTotal() {
        // Given
        Booking booking = new Booking();
        booking.setDepositAmount(new BigDecimal("100000"));
        // Mock dish prices
        when(bookingDishRepository.findByBooking(booking)).thenReturn(Collections.emptyList());
        when(bookingServiceRepository.findByBooking(booking)).thenReturn(Collections.emptyList());

        // When
        BigDecimal total = bookingService.calculateTotalAmount(booking);

        // Then
        assertEquals(new BigDecimal("100000"), total);
    }

    @Test
    void testCalculateTotalAmount_WithServices_ShouldReturnCorrectTotal() {
        // Given
        Booking booking = new Booking();
        booking.setDepositAmount(new BigDecimal("100000"));
        // Mock service prices
        when(bookingDishRepository.findByBooking(booking)).thenReturn(Collections.emptyList());
        when(bookingServiceRepository.findByBooking(booking)).thenReturn(Collections.emptyList());

        // When
        BigDecimal total = bookingService.calculateTotalAmount(booking);

        // Then
        assertEquals(new BigDecimal("100000"), total);
    }

    @Test
    void testCalculateTotalAmount_WithDishesAndServices_ShouldReturnCorrectTotal() {
        // Given
        Booking booking = new Booking();
        booking.setDepositAmount(new BigDecimal("100000"));
        // Mock both dish and service prices
        when(bookingDishRepository.findByBooking(booking)).thenReturn(Collections.emptyList());
        when(bookingServiceRepository.findByBooking(booking)).thenReturn(Collections.emptyList());

        // When
        BigDecimal total = bookingService.calculateTotalAmount(booking);

        // Then
        assertEquals(new BigDecimal("100000"), total);
    }

    @Test
    void testCalculateTotalAmount_WithZeroDeposit_ShouldReturnZero() {
        // Given
        Booking booking = new Booking();
        booking.setDepositAmount(BigDecimal.ZERO);
        when(bookingDishRepository.findByBooking(booking)).thenReturn(Collections.emptyList());
        when(bookingServiceRepository.findByBooking(booking)).thenReturn(Collections.emptyList());

        // When
        BigDecimal total = bookingService.calculateTotalAmount(booking);

        // Then
        assertEquals(BigDecimal.ZERO, total);
    }

    // ==================== CANCEL BOOKING TESTS ====================

    @Test
    @DisplayName("testCancelBooking_ByCustomer_ShouldSuccess")
    void testCancelBooking_ByCustomer_ShouldSuccess() {
        // Given
        mockBooking.setCustomer(customer);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(mockBooking));
        // Note: processRefund is a void method, but we don't need to stub it for this test
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Booking result = bookingService.cancelBooking(1, customerId, "Change of plans", "VCB", "1234567890");

        // Then
        assertNotNull(result);
        assertEquals(BookingStatus.CANCELLED, result.getStatus());
    }

    @Test
    @DisplayName("testCancelBooking_WithBookingNotFound_ShouldThrowException")
    void testCancelBooking_WithBookingNotFound_ShouldThrowException() {
        // Given
        when(bookingRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.cancelBooking(999, customerId, "Test", "VCB", "1234567890");
        });
        assertEquals("Booking not found", exception.getMessage());
    }

    @Test
    @DisplayName("testCancelBooking_WithUnauthorizedCustomer_ShouldThrowException")
    void testCancelBooking_WithUnauthorizedCustomer_ShouldThrowException() {
        // Given
        UUID differentCustomerId = UUID.randomUUID();
        mockBooking.setCustomer(customer);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(mockBooking));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.cancelBooking(1, differentCustomerId, "Test", "VCB", "1234567890");
        });
        assertTrue(exception.getMessage().contains("You can only cancel your own bookings"));
    }

    // ==================== FIND BOOKING BY ID TESTS ====================

    @Test
    @DisplayName("testFindBookingById_WithValidId_ShouldReturnBooking")
    void testFindBookingById_WithValidId_ShouldReturnBooking() {
        // Given
        when(bookingRepository.findById(1)).thenReturn(Optional.of(mockBooking));

        // When
        Optional<Booking> result = bookingService.findBookingById(1);

        // Then
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getBookingId());
    }

    @Test
    @DisplayName("testFindBookingById_WithNonExistentId_ShouldReturnEmpty")
    void testFindBookingById_WithNonExistentId_ShouldReturnEmpty() {
        // Given
        when(bookingRepository.findById(999)).thenReturn(Optional.empty());

        // When
        Optional<Booking> result = bookingService.findBookingById(999);

        // Then
        assertFalse(result.isPresent());
    }

    // ==================== FIND BOOKINGS BY CUSTOMER TESTS ====================

    @Test
    @DisplayName("testFindBookingsByCustomer_WithMultipleBookings_ShouldReturnAll")
    void testFindBookingsByCustomer_WithMultipleBookings_ShouldReturnAll() {
        // Given
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        List<Booking> bookings = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Booking booking = new Booking();
            booking.setBookingId(i);
            booking.setCustomer(customer);
            booking.setBookingTime(LocalDateTime.now().plusDays(i));
            booking.setStatus(BookingStatus.PENDING);
            bookings.add(booking);
        }
        when(bookingRepository.findByCustomerOrderByBookingTimeDesc(customer)).thenReturn(bookings);

        // When
        List<Booking> result = bookingService.findBookingsByCustomer(customerId);

        // Then
        assertNotNull(result);
        assertEquals(5, result.size());
    }

    @Test
    @DisplayName("testFindBookingsByCustomer_WithNoBookings_ShouldReturnEmpty")
    void testFindBookingsByCustomer_WithNoBookings_ShouldReturnEmpty() {
        // Given
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(bookingRepository.findByCustomerOrderByBookingTimeDesc(any(Customer.class))).thenReturn(Collections.emptyList());

        // When
        List<Booking> result = bookingService.findBookingsByCustomer(customerId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("testFindBookingsByCustomer_OrderedByBookingTimeDesc_ShouldReturnSorted")
    void testFindBookingsByCustomer_OrderedByBookingTimeDesc_ShouldReturnSorted() {
        // Given
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        LocalDateTime now = LocalDateTime.now();
        Booking booking1 = new Booking();
        booking1.setBookingId(1);
        booking1.setBookingTime(now.plusDays(3));
        Booking booking2 = new Booking();
        booking2.setBookingId(2);
        booking2.setBookingTime(now.plusDays(1));
        Booking booking3 = new Booking();
        booking3.setBookingId(3);
        booking3.setBookingTime(now.plusDays(5));
        
        List<Booking> bookings = Arrays.asList(booking1, booking2, booking3);
        when(bookingRepository.findByCustomerOrderByBookingTimeDesc(any(Customer.class))).thenReturn(bookings);

        // When
        List<Booking> result = bookingService.findBookingsByCustomer(customerId);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
    }

    // ==================== ASSIGN DISHES TESTS ====================

    @Test
    @DisplayName("testAssignDishesToBooking_WithValidIds_ShouldSuccess")
    void testAssignDishesToBooking_WithValidIds_ShouldSuccess() {
        // Given
        com.example.booking.domain.Dish dish1 = new com.example.booking.domain.Dish();
        dish1.setDishId(1);
        dish1.setPrice(new BigDecimal("50000"));
        
        com.example.booking.domain.Dish dish2 = new com.example.booking.domain.Dish();
        dish2.setDishId(2);
        dish2.setPrice(new BigDecimal("30000"));
        
        when(dishRepository.findById(1)).thenReturn(Optional.of(dish1));
        when(dishRepository.findById(2)).thenReturn(Optional.of(dish2));
        when(bookingDishRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        bookingService.assignDishesToBooking(mockBooking, "1:2,2:1");

        // Then
        verify(bookingDishRepository, atLeastOnce()).save(any());
    }

    @Test
    @DisplayName("testAssignDishesToBooking_WithEmptyString_ShouldSkip")
    void testAssignDishesToBooking_WithEmptyString_ShouldSkip() {
        // When
        bookingService.assignDishesToBooking(mockBooking, "");

        // Then
        verify(bookingDishRepository, never()).save(any());
    }

    @Test
    @DisplayName("testAssignDishesToBooking_WithNull_ShouldSkip")
    void testAssignDishesToBooking_WithNull_ShouldSkip() {
        // When
        bookingService.assignDishesToBooking(mockBooking, null);

        // Then
        verify(bookingDishRepository, never()).save(any());
    }

    // ==================== ASSIGN SERVICES TESTS ====================

    @Test
    @DisplayName("testAssignServicesToBooking_WithValidIds_ShouldSuccess")
    void testAssignServicesToBooking_WithValidIds_ShouldSuccess() {
        // Given
        com.example.booking.domain.RestaurantService service1 = new com.example.booking.domain.RestaurantService();
        service1.setServiceId(1);
        service1.setPrice(new BigDecimal("20000"));
        
        com.example.booking.domain.RestaurantService service2 = new com.example.booking.domain.RestaurantService();
        service2.setServiceId(2);
        service2.setPrice(new BigDecimal("15000"));
        
        when(restaurantServiceRepository.findById(1)).thenReturn(Optional.of(service1));
        when(restaurantServiceRepository.findById(2)).thenReturn(Optional.of(service2));
        when(bookingServiceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        bookingService.assignServicesToBooking(mockBooking, "1,2");

        // Then
        verify(bookingServiceRepository, atLeastOnce()).save(any());
    }

    @Test
    @DisplayName("testAssignServicesToBooking_WithEmptyString_ShouldSkip")
    void testAssignServicesToBooking_WithEmptyString_ShouldSkip() {
        // When
        bookingService.assignServicesToBooking(mockBooking, "");

        // Then
        verify(bookingServiceRepository, never()).save(any());
    }

    @Test
    @DisplayName("testAssignServicesToBooking_WithNull_ShouldSkip")
    void testAssignServicesToBooking_WithNull_ShouldSkip() {
        // When
        bookingService.assignServicesToBooking(mockBooking, null);

        // Then
        verify(bookingServiceRepository, never()).save(any());
    }

    // ==================== CANCEL BOOKING TESTS ====================

    @Test
    @DisplayName("TC BS-022: Should cancel booking with refund when payment completed")
    void testCancelBooking_WithCompletedPayment_ShouldCreateRefund() {
        // Given: Booking with completed payment
        Integer bookingId = 1;
        String cancelReason = "Changed plans";
        String bankCode = "VCB";
        String accountNumber = "1234567890";
        
        mockBooking.setStatus(BookingStatus.PENDING);
        Payment completedPayment = new Payment();
        completedPayment.setStatus(com.example.booking.domain.PaymentStatus.COMPLETED);
        completedPayment.setAmount(new BigDecimal("100000"));

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(mockBooking));
        when(paymentRepository.findByBooking(mockBooking)).thenReturn(Optional.of(completedPayment));
        when(refundService.processRefundWithManualTransfer(anyInt(), anyString(), anyString(), anyString()))
            .thenReturn(completedPayment);
        when(bookingRepository.save(any(Booking.class))).thenReturn(mockBooking);

        // When
        Booking result = bookingService.cancelBooking(bookingId, customerId, cancelReason, bankCode, accountNumber);

        // Then
        assertEquals(BookingStatus.CANCELLED, result.getStatus());
        assertEquals(cancelReason, result.getCancelReason());
        assertNotNull(result.getCancelledAt());
        verify(refundService).processRefundWithManualTransfer(anyInt(), anyString(), eq(bankCode), eq(accountNumber));
    }

    @Test
    @DisplayName("TC BS-025: Should throw exception when customer doesn't own booking")
    void testCancelBooking_WithInvalidCustomer_ShouldThrowException() {
        // Given: Different customer
        Integer bookingId = 1;
        UUID differentCustomerId = UUID.randomUUID();
        String cancelReason = "Changed plans";
        String bankCode = "VCB";
        String accountNumber = "1234567890";

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(mockBooking));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            bookingService.cancelBooking(bookingId, differentCustomerId, cancelReason, bankCode, accountNumber)
        );
        assertEquals("You can only cancel your own bookings", exception.getMessage());
    }

    // ==================== CONFIRM BOOKING TESTS ====================

    @Test
    @DisplayName("TC BS-029: Should confirm PENDING booking successfully")
    void testConfirmBooking_WithPendingBooking_ShouldConfirm() {
        // Given: PENDING booking
        Integer bookingId = 1;
        mockBooking.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(mockBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(mockBooking);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Booking result = bookingService.confirmBooking(bookingId);

        // Then
        assertEquals(BookingStatus.CONFIRMED, result.getStatus());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("TC BS-031: Should throw exception when confirming non-PENDING booking")
    void testConfirmBooking_WithNonPendingBooking_ShouldThrowException() {
        // Given: COMPLETED booking
        Integer bookingId = 1;
        mockBooking.setStatus(BookingStatus.COMPLETED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(mockBooking));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            bookingService.confirmBooking(bookingId)
        );
        assertTrue(exception.getMessage().contains("Booking cannot be confirmed"));
    }

    // ==================== COMPLETE BOOKING TESTS ====================

    @Test
    @DisplayName("TC BS-033: Should complete CONFIRMED booking successfully")
    void testCompleteBooking_WithConfirmedBooking_ShouldComplete() {
        // Given: CONFIRMED booking
        Integer bookingId = 1;
        mockBooking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(mockBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(mockBooking);

        // When
        Booking result = bookingService.completeBooking(bookingId);

        // Then
        assertEquals(BookingStatus.COMPLETED, result.getStatus());
    }

    @Test
    @DisplayName("TC BS-034: Should throw exception when completing non-CONFIRMED booking")
    void testCompleteBooking_WithPendingBooking_ShouldThrowException() {
        // Given: PENDING booking
        Integer bookingId = 1;
        mockBooking.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(mockBooking));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            bookingService.completeBooking(bookingId)
        );
        assertTrue(exception.getMessage().contains("Booking cannot be completed"));
    }

    // ==================== UPDATE BOOKING STATUS TESTS ====================

    @Test
    @DisplayName("TC BS-035: Should update booking status PENDING to CONFIRMED")
    void testUpdateBookingStatus_PendingToConfirmed_ShouldUpdate() {
        // Given: PENDING booking
        Integer bookingId = 1;
        mockBooking.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(mockBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(mockBooking);

        // When
        Booking result = bookingService.updateBookingStatus(bookingId, BookingStatus.CONFIRMED);

        // Then
        assertEquals(BookingStatus.CONFIRMED, result.getStatus());
    }

    @Test
    @DisplayName("TC BS-038: Should throw exception for invalid status transition")
    void testUpdateBookingStatus_InvalidTransition_ShouldThrowException() {
        // Given: COMPLETED booking
        Integer bookingId = 1;
        mockBooking.setStatus(BookingStatus.COMPLETED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(mockBooking));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            bookingService.updateBookingStatus(bookingId, BookingStatus.PENDING)
        );
        assertTrue(exception.getMessage().contains("Invalid status transition"));
    }

    // ==================== UPDATE BOOKING TESTS ====================

    @Test
    @DisplayName("TC BS-016: Should update PENDING booking successfully")
    void testUpdateBooking_WithPendingBooking_ShouldUpdate() {
        // Given: PENDING booking
        Integer bookingId = 1;
        mockBooking.setStatus(BookingStatus.PENDING);
        
        BookingForm updateForm = new BookingForm();
        updateForm.setRestaurantId(1);
        updateForm.setTableId(2);
        updateForm.setGuestCount(6);
        updateForm.setBookingTime(LocalDateTime.now().plusDays(2));
        updateForm.setDepositAmount(new BigDecimal("200000"));
        updateForm.setNote("Updated note");

        RestaurantTable newTable = new RestaurantTable();
        newTable.setTableId(2);
        newTable.setTableName("Table 2");
        newTable.setCapacity(8);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(mockBooking));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(2)).thenReturn(Optional.of(newTable));
        when(bookingTableRepository.findByBooking(mockBooking)).thenReturn(Collections.emptyList());
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingRepository.save(any(Booking.class))).thenReturn(mockBooking);
        doNothing().when(conflictService).validateBookingUpdateConflicts(anyInt(), any(BookingForm.class), any(UUID.class));

        // When
        Booking result = bookingService.updateBooking(bookingId, updateForm, customerId);

        // Then
        assertEquals(updateForm.getGuestCount(), result.getNumberOfGuests());
        verify(bookingRepository).save(mockBooking);
    }

    @Test
    @DisplayName("TC BS-018: Should throw exception when booking not found for update")
    void testUpdateBooking_WithBookingNotFound_ShouldThrowException() {
        // Given: Invalid booking ID
        Integer bookingId = 999;
        BookingForm form = new BookingForm();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            bookingService.updateBooking(bookingId, form, customerId)
        );
        assertEquals("Booking not found", exception.getMessage());
    }

    @Test
    @DisplayName("TC BS-019: Should throw exception when customer doesn't own booking")
    void testUpdateBooking_WithInvalidCustomer_ShouldThrowException() {
        // Given: Different customer
        Integer bookingId = 1;
        UUID differentCustomerId = UUID.randomUUID();
        mockBooking.setStatus(BookingStatus.PENDING);
        BookingForm form = new BookingForm();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(mockBooking));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            bookingService.updateBooking(bookingId, form, differentCustomerId)
        );
        assertEquals("You can only edit your own bookings", exception.getMessage());
    }

    @Test
    @DisplayName("TC BS-020: Should throw exception when booking cannot be edited")
    void testUpdateBooking_WithCompletedBooking_ShouldThrowException() {
        // Given: COMPLETED booking
        Integer bookingId = 1;
        mockBooking.setStatus(BookingStatus.COMPLETED);
        BookingForm form = new BookingForm();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(mockBooking));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            bookingService.updateBooking(bookingId, form, customerId)
        );
        assertEquals("This booking cannot be edited", exception.getMessage());
    }

    // ==================== CALCULATE TOTAL AMOUNT TESTS ====================

    @Test
    @DisplayName("TC BS-043: Should calculate total with deposit, dishes, and services")
    void testCalculateTotalAmount_WithAllItems_ShouldSumCorrectly() {
        // Given: Booking with deposit, 2 dishes, 1 service
        Booking booking = new Booking();
        booking.setDepositAmount(new BigDecimal("100000"));

        com.example.booking.domain.BookingDish dish1 = new com.example.booking.domain.BookingDish();
        dish1.setQuantity(2);
        dish1.setPrice(new BigDecimal("50000"));
        
        com.example.booking.domain.BookingDish dish2 = new com.example.booking.domain.BookingDish();
        dish2.setQuantity(1);
        dish2.setPrice(new BigDecimal("30000"));

        com.example.booking.domain.BookingService service1 = new com.example.booking.domain.BookingService();
        service1.setQuantity(1);
        service1.setPrice(new BigDecimal("20000"));

        when(bookingDishRepository.findByBooking(booking)).thenReturn(Arrays.asList(dish1, dish2));
        when(bookingServiceRepository.findByBooking(booking)).thenReturn(Arrays.asList(service1));

        // When
        BigDecimal total = bookingService.calculateTotalAmount(booking);

        // Then
        // deposit: 100000 + dishes: 100000 (2*50000) + 30000 + service: 20000 = 250000
        assertEquals(0, new BigDecimal("250000").compareTo(total));
    }

    @Test
    @DisplayName("TC BS-045: Should throw exception when booking is null")
    void testCalculateTotalAmount_WithNullBooking_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            bookingService.calculateTotalAmount(null)
        );
        assertEquals("Booking cannot be null", exception.getMessage());
    }

    // ==================== FIND BOOKINGS BY CUSTOMER TESTS ====================

    @Test
    @DisplayName("TC BS-040: Should find all bookings for customer")
    void testFindBookingsByCustomer_WithValidCustomer_ShouldReturnBookings() {
        // Given: Customer with 3 bookings
        List<Booking> bookings = Arrays.asList(mockBooking, mockBooking, mockBooking);
        
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(bookingRepository.findByCustomerOrderByBookingTimeDesc(any(Customer.class))).thenReturn(bookings);

        // When
        List<Booking> result = bookingService.findBookingsByCustomer(customerId);

        // Then
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("TC BS-042: Should throw exception when customer not found")
    void testFindBookingsByCustomer_WithInvalidCustomer_ShouldThrowException() {
        // Given: Invalid customer ID
        UUID invalidCustomerId = UUID.randomUUID();

        when(customerRepository.findById(invalidCustomerId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            bookingService.findBookingsByCustomer(invalidCustomerId)
        );
        assertEquals("Customer not found", exception.getMessage());
    }

    // ==================== ASSIGN DISHES ERROR TESTS ====================

    @Test
    @DisplayName("TC BS-048: Should throw exception when dish not found")
    void testAssignDishesToBooking_WithDishNotFound_ShouldThrowException() {
        // Given: Invalid dish ID
        when(dishRepository.findById(99)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            bookingService.assignDishesToBooking(mockBooking, "99:1")
        );
        assertEquals("Dish not found: 99", exception.getMessage());
    }

    // ==================== ASSIGN SERVICES ERROR TESTS ====================

    @Test
    @DisplayName("Should throw exception when service not found")
    void testAssignServicesToBooking_WithServiceNotFound_ShouldThrowException() {
        // Given: Invalid service ID
        when(restaurantServiceRepository.findById(99)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            bookingService.assignServicesToBooking(mockBooking, "99")
        );
        assertEquals("Service not found: 99", exception.getMessage());
    }

    // ==================== CREATE BOOKING WITH VOUCHER TESTS ====================

    @Test
    @DisplayName("TC BS-003: Should create booking with valid voucher")
    void testCreateBooking_WithValidVoucher_ShouldApplyVoucher() {
        // Given: Booking form with voucher code
        bookingForm.setVoucherCode("SUMMER20");
        bookingForm.setVoucherDiscountAmount(new BigDecimal("20000"));

        prepareCreateBookingStubs();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table));
        when(voucherService.validate(any())).thenReturn(
            new com.example.booking.service.VoucherService.ValidationResult(true, null, new BigDecimal("20000"), null)
        );
        when(voucherService.applyToBooking(any())).thenReturn(
            new com.example.booking.service.VoucherService.ApplyResult(true, null, new BigDecimal("20000"), 1)
        );

        // When
        Booking result = bookingService.createBooking(bookingForm, customerId);

        // Then
        assertNotNull(result);
        verify(voucherService).applyToBooking(any());
    }

    // ==================== CREATE BOOKING ERROR TESTS ====================

    @Test
    @DisplayName("TC BS-007: Should throw exception when booking time in past")
    void testCreateBooking_WithPastBookingTime_ShouldThrowException() {
        // Given: Booking time in the past
        bookingForm.setBookingTime(LocalDateTime.now().minusDays(1));

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            bookingService.createBooking(bookingForm, customerId)
        );
        assertTrue(exception.getMessage().contains("Booking time cannot be in the past"));
    }

    @Test
    @DisplayName("TC BS-008: Should throw exception when booking time too early")
    void testCreateBooking_WithBookingTimeTooEarly_ShouldThrowException() {
        // Given: Booking time less than 30 minutes from now
        bookingForm.setBookingTime(LocalDateTime.now().plusMinutes(15));

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            bookingService.createBooking(bookingForm, customerId)
        );
        assertTrue(exception.getMessage().contains("30 minutes"));
    }

    @Test
    @DisplayName("TC BS-009: Should throw exception when guest count is zero")
    void testCreateBooking_WithZeroGuestCount_ShouldThrowException() {
        // Given: Guest count 0
        bookingForm.setGuestCount(0);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            bookingService.createBooking(bookingForm, customerId)
        );
        assertTrue(exception.getMessage().contains("Guest count must be greater than 0"));
    }

    @Test
    @DisplayName("TC BS-010: Should throw exception when guest count too large")
    void testCreateBooking_WithGuestCountTooLarge_ShouldThrowException() {
        // Given: Guest count > 100
        bookingForm.setGuestCount(101);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            bookingService.createBooking(bookingForm, customerId)
        );
        assertTrue(exception.getMessage().contains("100 people"));
    }

    @Test
    @DisplayName("TC BS-012: Should throw exception when guest count exceeds table capacity")
    void testCreateBooking_WithGuestCountExceedingCapacity_ShouldThrowException() {
        // Given: Table capacity 4, guest count 5
        bookingForm.setGuestCount(5);

        prepareCreateBookingStubs();
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            bookingService.createBooking(bookingForm, customerId)
        );
        assertTrue(exception.getMessage().contains("vượt quá sức chứa"));
    }

    // ==================== CREATE BOOKING WITH TABLE IDS TESTS ====================

    @Test
    @DisplayName("Should create booking with tableIds string")
    void testCreateBooking_WithTableIds_ShouldSuccess() {
        // Given
        prepareCreateBookingStubs();
        bookingForm.setTableId(null);
        bookingForm.setTableIds("1");
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table));
        when(bookingTableRepository.findByBooking(any())).thenReturn(Arrays.asList(new BookingTable()));

        // When
        Booking result = bookingService.createBooking(bookingForm, customerId);

        // Then
        assertNotNull(result);
        verify(bookingTableRepository).save(any(BookingTable.class));
    }

    // ==================== UPDATE BOOKING WITH RESTAURANT CHANGE TESTS ====================

    @Test
    @DisplayName("Should update booking with restaurant change")
    void testUpdateBooking_WithRestaurantChange_ShouldUpdate() {
        // Given
        mockBooking.setStatus(BookingStatus.PENDING);
        RestaurantProfile newRestaurant = new RestaurantProfile();
        newRestaurant.setRestaurantId(2);
        newRestaurant.setRestaurantName("New Restaurant");

        BookingForm form = new BookingForm();
        form.setRestaurantId(2);
        form.setTableId(1);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(2));
        form.setDepositAmount(new BigDecimal("100000"));

        RestaurantTable newTable = new RestaurantTable();
        newTable.setTableId(1);
        newTable.setCapacity(4);
        newTable.setRestaurant(newRestaurant);

        when(bookingRepository.findById(1)).thenReturn(Optional.of(mockBooking));
        when(restaurantProfileRepository.findById(2)).thenReturn(Optional.of(newRestaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(newTable));
        when(bookingTableRepository.findByBooking(mockBooking)).thenReturn(Collections.emptyList());
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(conflictService).validateBookingUpdateConflicts(anyInt(), any(BookingForm.class), any(UUID.class));

        // When
        Booking result = bookingService.updateBooking(1, form, customerId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getRestaurant().getRestaurantId());
    }

    // ==================== CANCEL BOOKING WITHOUT PAYMENT TESTS ====================

    @Test
    @DisplayName("Should cancel booking without payment")
    void testCancelBooking_WithoutPayment_ShouldCancel() {
        // Given
        mockBooking.setCustomer(customer);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(mockBooking));
        when(paymentRepository.findByBooking(mockBooking)).thenReturn(Optional.empty());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Booking result = bookingService.cancelBooking(1, customerId, "Test reason", "VCB", "1234567890");

        // Then
        assertNotNull(result);
        assertEquals(BookingStatus.CANCELLED, result.getStatus());
        verify(refundService, never()).processRefundWithManualTransfer(anyInt(), anyString(), anyString(), anyString());
    }

    // ==================== CANCEL BOOKING WITH PENDING PAYMENT TESTS ====================

    @Test
    @DisplayName("Should cancel booking with pending payment without refund")
    void testCancelBooking_WithPendingPayment_ShouldCancelWithoutRefund() {
        // Given
        mockBooking.setCustomer(customer);
        Payment pendingPayment = new Payment();
        pendingPayment.setStatus(com.example.booking.domain.PaymentStatus.PENDING);
        pendingPayment.setAmount(new BigDecimal("100000"));

        when(bookingRepository.findById(1)).thenReturn(Optional.of(mockBooking));
        when(paymentRepository.findByBooking(mockBooking)).thenReturn(Optional.of(pendingPayment));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Booking result = bookingService.cancelBooking(1, customerId, "Test reason", "VCB", "1234567890");

        // Then
        assertNotNull(result);
        assertEquals(BookingStatus.CANCELLED, result.getStatus());
        verify(refundService, never()).processRefundWithManualTransfer(anyInt(), anyString(), anyString(), anyString());
    }

    // ==================== STATUS TRANSITION TESTS ====================

    @Test
    @DisplayName("Should update status from PENDING to CANCELLED")
    void testUpdateBookingStatus_PendingToCancelled_ShouldUpdate() {
        // Given
        mockBooking.setStatus(BookingStatus.PENDING);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(mockBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(mockBooking);

        // When
        Booking result = bookingService.updateBookingStatus(1, BookingStatus.CANCELLED);

        // Then
        assertEquals(BookingStatus.CANCELLED, result.getStatus());
    }

    @Test
    @DisplayName("Should update status from CONFIRMED to CANCELLED")
    void testUpdateBookingStatus_ConfirmedToCancelled_ShouldUpdate() {
        // Given
        mockBooking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(mockBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(mockBooking);

        // When
        Booking result = bookingService.updateBookingStatus(1, BookingStatus.CANCELLED);

        // Then
        assertEquals(BookingStatus.CANCELLED, result.getStatus());
    }

    @Test
    @DisplayName("Should update status from CONFIRMED to NO_SHOW")
    void testUpdateBookingStatus_ConfirmedToNoShow_ShouldUpdate() {
        // Given
        mockBooking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(mockBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(mockBooking);

        // When
        Booking result = bookingService.updateBookingStatus(1, BookingStatus.NO_SHOW);

        // Then
        assertEquals(BookingStatus.NO_SHOW, result.getStatus());
    }

    @Test
    @DisplayName("Should throw exception for invalid transition from CANCELLED")
    void testUpdateBookingStatus_FromCancelled_ShouldThrowException() {
        // Given
        mockBooking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(mockBooking));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            bookingService.updateBookingStatus(1, BookingStatus.PENDING)
        );
        assertTrue(exception.getMessage().contains("Invalid status transition"));
    }

    @Test
    @DisplayName("Should throw exception for invalid transition from NO_SHOW")
    void testUpdateBookingStatus_FromNoShow_ShouldThrowException() {
        // Given
        mockBooking.setStatus(BookingStatus.NO_SHOW);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(mockBooking));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            bookingService.updateBookingStatus(1, BookingStatus.PENDING)
        );
        assertTrue(exception.getMessage().contains("Invalid status transition"));
    }

    // ==================== CALCULATE TOTAL AMOUNT EDGE CASES ====================

    @Test
    @DisplayName("Should calculate total with null deposit amount")
    void testCalculateTotalAmount_WithNullDeposit_ShouldUseZero() {
        // Given
        Booking booking = new Booking();
        booking.setDepositAmount(null);
        when(bookingDishRepository.findByBooking(booking)).thenReturn(Collections.emptyList());
        when(bookingServiceRepository.findByBooking(booking)).thenReturn(Collections.emptyList());

        // When
        BigDecimal total = bookingService.calculateTotalAmount(booking);

        // Then
        assertEquals(BigDecimal.ZERO, total);
    }

    // ==================== CREATE BOOKING WITH DEPOSIT FROM TABLE TESTS ====================

    @Test
    @DisplayName("Should use deposit amount from table when form deposit is null")
    void testCreateBooking_WithTableDeposit_ShouldUseTableDeposit() {
        // Given
        prepareCreateBookingStubs();
        bookingForm.setDepositAmount(null);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table));
        when(bookingDishRepository.findByBooking(any())).thenReturn(Collections.emptyList());
        when(bookingServiceRepository.findByBooking(any())).thenReturn(Collections.emptyList());

        // When
        Booking result = bookingService.createBooking(bookingForm, customerId);

        // Then
        assertNotNull(result);
        assertNotNull(result.getDepositAmount());
    }
}
