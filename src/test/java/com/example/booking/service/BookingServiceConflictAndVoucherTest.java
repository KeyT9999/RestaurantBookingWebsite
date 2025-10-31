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
import com.example.booking.dto.BookingForm;
import com.example.booking.exception.BookingConflictException;
import com.example.booking.repository.*;

import jakarta.persistence.EntityManager;

/**
 * Test class for conflict validation and voucher handling in BookingService
 * Focuses on testing conflict scenarios and voucher application edge cases
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingServiceConflictAndVoucherTest {

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
    private RestaurantTable table;
    private Booking booking;
    private BookingForm form;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();

        // Setup User
        user = new User();
        user.setId(customerId);
        user.setFullName("Test User");

        // Setup Customer
        customer = new Customer();
        customer.setCustomerId(customerId);
        customer.setUser(user);

        // Setup Restaurant
        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");

        // Setup Table
        table = new RestaurantTable();
        table.setTableId(1);
        table.setTableName("Table 1");
        table.setCapacity(4);
        table.setDepositAmount(new BigDecimal("100000"));
        table.setRestaurant(restaurant);

        // Setup Booking
        booking = new Booking();
        booking.setBookingId(1);
        booking.setCustomer(customer);
        booking.setRestaurant(restaurant);
        booking.setBookingTime(LocalDateTime.now().plusDays(1));
        booking.setDepositAmount(new BigDecimal("100000"));
        booking.setStatus(BookingStatus.PENDING);
        booking.setNumberOfGuests(4);

        // Setup BookingForm
        form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(1);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(1));
        form.setDepositAmount(new BigDecimal("100000"));

        // Default mocks
        lenient().when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        lenient().when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        lenient().when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table));
        lenient().when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
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
        lenient().when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().doNothing().when(entityManager).flush();
    }

    // ==================== CONFLICT VALIDATION TESTS ====================

    @Test
    @DisplayName("Should throw exception when booking conflict detected")
    void testCreateBooking_WithConflict_ShouldThrowException() {
        doThrow(new BookingConflictException(BookingConflictException.ConflictType.TABLE_OCCUPIED, "Table already booked")).when(conflictService)
            .validateBookingConflicts(any(), any());

        BookingConflictException exception = assertThrows(BookingConflictException.class, () -> {
            bookingService.createBooking(form, customerId);
        });
        assertTrue(exception.getMessage().contains("already booked"));
    }

    @Test
    @DisplayName("Should throw exception when update conflict detected")
    void testUpdateBooking_WithConflict_ShouldThrowException() {
        booking.setStatus(BookingStatus.PENDING);
        doThrow(new BookingConflictException(BookingConflictException.ConflictType.TIME_OVERLAP, "Time slot conflict")).when(conflictService)
            .validateBookingUpdateConflicts(anyInt(), any(), any());

        BookingConflictException exception = assertThrows(BookingConflictException.class, () -> {
            bookingService.updateBooking(1, form, customerId);
        });
        assertTrue(exception.getMessage().contains("conflict"));
    }

    @Test
    @DisplayName("Should proceed when no conflict detected")
    void testCreateBooking_WithoutConflict_ShouldProceed() {
        doNothing().when(conflictService).validateBookingConflicts(any(), any());
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
    }

    // ==================== VOUCHER APPLICATION TESTS ====================

    @Test
    @DisplayName("Should apply voucher code from form")
    void testCreateBooking_WithVoucherCodeApplied_ShouldApply() {
        form.setVoucherCodeApplied("SUMMER20");
        form.setVoucherDiscountAmount(new BigDecimal("20000"));

        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        when(voucherService.applyToBooking(any())).thenReturn(
            new VoucherService.ApplyResult(true, null, new BigDecimal("20000"), 1)
        );

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
        verify(voucherService).applyToBooking(any());
    }

    @Test
    @DisplayName("Should validate and apply voucher code when not applied")
    void testCreateBooking_WithVoucherCode_ShouldValidateAndApply() {
        form.setVoucherCode("SUMMER20");

        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        when(voucherService.validate(any())).thenReturn(
            new VoucherService.ValidationResult(true, null, new BigDecimal("20000"), null)
        );
        when(voucherService.applyToBooking(any())).thenReturn(
            new VoucherService.ApplyResult(true, null, new BigDecimal("20000"), 1)
        );

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
        verify(voucherService).validate(any());
        verify(voucherService).applyToBooking(any());
    }

    @Test
    @DisplayName("Should skip voucher when validation returns null")
    void testCreateBooking_WithNullVoucherValidation_ShouldSkip() {
        form.setVoucherCode("TEST");

        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        when(voucherService.validate(any())).thenReturn(null);

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
        verify(voucherService, never()).applyToBooking(any());
    }

    @Test
    @DisplayName("Should skip voucher when validation returns invalid")
    void testCreateBooking_WithInvalidVoucherValidation_ShouldSkip() {
        form.setVoucherCode("INVALID");

        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        when(voucherService.validate(any())).thenReturn(
            new VoucherService.ValidationResult(false, "Voucher expired", null, null)
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(form, customerId);
        });
        assertTrue(exception.getMessage().contains("Invalid voucher"));
    }

    @Test
    @DisplayName("Should handle voucher application failure")
    void testCreateBooking_WithVoucherApplicationFailure_ShouldThrowException() {
        form.setVoucherCodeApplied("SUMMER20");
        form.setVoucherDiscountAmount(new BigDecimal("20000"));

        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        when(voucherService.applyToBooking(any())).thenReturn(
            new VoucherService.ApplyResult(false, "Application failed", null, null)
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(form, customerId);
        });
        assertTrue(exception.getMessage().contains("Failed to apply voucher"));
    }

    @Test
    @DisplayName("Should handle voucher service being null")
    void testCreateBooking_WithNullVoucherService_ShouldContinue() {
        form.setVoucherCode("TEST");
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should skip voucher when voucherCodeApplied is empty")
    void testCreateBooking_WithEmptyVoucherCodeApplied_ShouldSkip() {
        form.setVoucherCodeApplied("   ");
        form.setVoucherCode("SUMMER20");

        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        when(voucherService.validate(any())).thenReturn(
            new VoucherService.ValidationResult(true, null, new BigDecimal("20000"), null)
        );
        when(voucherService.applyToBooking(any())).thenReturn(
            new VoucherService.ApplyResult(true, null, new BigDecimal("20000"), 1)
        );

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
        verify(voucherService).validate(any());
    }

    @Test
    @DisplayName("Should skip voucher when discount amount is zero")
    void testCreateBooking_WithZeroVoucherDiscount_ShouldSkip() {
        form.setVoucherCodeApplied("SUMMER20");
        form.setVoucherDiscountAmount(BigDecimal.ZERO);

        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
        verify(voucherService, never()).applyToBooking(any());
    }

    // ==================== UPDATE BOOKING WITH CONFLICT TESTS ====================

    @Test
    @DisplayName("Should update booking without conflict")
    void testUpdateBooking_WithoutConflict_ShouldUpdate() {
        booking.setStatus(BookingStatus.PENDING);
        form.setTableId(1);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(2));
        form.setDepositAmount(new BigDecimal("100000"));

        when(bookingTableRepository.findByBooking(booking)).thenReturn(Collections.emptyList());
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(bookingTableRepository).deleteByBooking(any());
        doNothing().when(conflictService).validateBookingUpdateConflicts(anyInt(), any(), any());

        Booking result = bookingService.updateBooking(1, form, customerId);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should handle conflict exception during update")
    void testUpdateBooking_WithConflictException_ShouldThrow() {
        booking.setStatus(BookingStatus.PENDING);
        doThrow(new BookingConflictException(BookingConflictException.ConflictType.TIME_OVERLAP, "Conflict")).when(conflictService)
            .validateBookingUpdateConflicts(anyInt(), any(), any());

        BookingConflictException exception = assertThrows(BookingConflictException.class, () -> {
            bookingService.updateBooking(1, form, customerId);
        });
        assertNotNull(exception);
    }
}

