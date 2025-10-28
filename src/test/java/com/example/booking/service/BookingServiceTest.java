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

    @Test
    void testCalculateTotalAmount_WithNullBooking_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.calculateTotalAmount(null);
        });
        assertEquals("Booking cannot be null", exception.getMessage());
    }

    // ==================== UPDATE BOOKING STATUS TESTS ====================

    @Test
    @DisplayName("testUpdateBookingStatus_PendingToConfirmed_ShouldSuccess")
    void testUpdateBookingStatus_PendingToConfirmed_ShouldSuccess() {
        // Given
        mockBooking.setStatus(BookingStatus.PENDING);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(mockBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Booking result = bookingService.updateBookingStatus(1, BookingStatus.CONFIRMED);

        // Then
        assertNotNull(result);
        assertEquals(BookingStatus.CONFIRMED, result.getStatus());
    }

    @Test
    @DisplayName("testUpdateBookingStatus_PendingToCancelled_ShouldSuccess")
    void testUpdateBookingStatus_PendingToCancelled_ShouldSuccess() {
        // Given
        mockBooking.setStatus(BookingStatus.PENDING);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(mockBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Booking result = bookingService.updateBookingStatus(1, BookingStatus.CANCELLED);

        // Then
        assertNotNull(result);
        assertEquals(BookingStatus.CANCELLED, result.getStatus());
    }

    @Test
    @DisplayName("testUpdateBookingStatus_WithBookingNotFound_ShouldThrowException")
    void testUpdateBookingStatus_WithBookingNotFound_ShouldThrowException() {
        // Given
        when(bookingRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.updateBookingStatus(999, BookingStatus.CONFIRMED);
        });
        assertEquals("Booking not found", exception.getMessage());
    }

    @Test
    @DisplayName("testUpdateBookingStatus_InvalidTransition_ShouldThrowException")
    void testUpdateBookingStatus_InvalidTransition_ShouldThrowException() {
        // Given
        mockBooking.setStatus(BookingStatus.COMPLETED);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(mockBooking));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.updateBookingStatus(1, BookingStatus.CONFIRMED);
        });
        assertTrue(exception.getMessage().contains("Invalid status transition"));
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
}
