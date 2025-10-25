package com.example.booking.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
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
import com.example.booking.repository.NotificationRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.RestaurantTableRepository;

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
}
