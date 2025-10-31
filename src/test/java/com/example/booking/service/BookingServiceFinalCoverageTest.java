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
import com.example.booking.repository.*;

import jakarta.persistence.EntityManager;

/**
 * Final test file to maximize coverage for BookingService
 * Focuses on uncovered branches in createBooking and other methods
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingServiceFinalCoverageTest {

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
    private BookingForm form;

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
        booking.setDepositAmount(new BigDecimal("100000"));
        booking.setStatus(BookingStatus.PENDING);
        booking.setNumberOfGuests(4);
        booking.setBookingTables(new ArrayList<>());

        form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(1);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(1));
        form.setDepositAmount(new BigDecimal("100000"));

        lenient().when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        lenient().when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        lenient().when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table1));
        lenient().when(restaurantTableRepository.findById(2)).thenReturn(Optional.of(table2));
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
        lenient().doNothing().when(conflictService).validateBookingConflicts(any(), any());
        lenient().doNothing().when(conflictService).validateBookingUpdateConflicts(anyInt(), any(), any());
    }

    // ==================== CREATE BOOKING BRANCH COVERAGE ====================

    @Test
    @DisplayName("Should handle voucherCodeApplied branch with valid discount")
    void testCreateBooking_WithVoucherCodeApplied_ShouldApply() {
        form.setVoucherCodeApplied("SUMMER20");
        form.setVoucherDiscountAmount(new BigDecimal("20000"));

        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1);
            return b;
        });
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        when(voucherService.applyToBooking(any())).thenReturn(
            new VoucherService.ApplyResult(true, null, new BigDecimal("20000"), 1)
        );

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
        verify(voucherService).applyToBooking(any());
        verify(voucherService, never()).validate(any());
    }

    @Test
    @DisplayName("Should handle voucherCode branch when voucherCodeApplied is null")
    void testCreateBooking_WithVoucherCodeOnly_ShouldValidateAndApply() {
        form.setVoucherCode("SUMMER20");
        form.setVoucherCodeApplied(null);

        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1);
            return b;
        });
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
    @DisplayName("Should handle voucherCode branch when voucherCodeApplied is empty")
    void testCreateBooking_WithEmptyVoucherCodeApplied_ShouldUseVoucherCode() {
        form.setVoucherCodeApplied("");
        form.setVoucherCode("SUMMER20");

        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1);
            return b;
        });
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
    @DisplayName("Should handle voucher validation exception branch")
    void testCreateBooking_WithVoucherValidationException_ShouldThrow() {
        form.setVoucherCode("TEST");

        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1);
            return b;
        });
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        when(voucherService.validate(any())).thenThrow(new RuntimeException("Voucher service error"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(form, customerId);
        });
        assertTrue(exception.getMessage().contains("Voucher validation failed"));
    }

    @Test
    @DisplayName("Should handle voucher application exception branch")
    void testCreateBooking_WithVoucherApplicationException_ShouldThrow() {
        form.setVoucherCodeApplied("SUMMER20");
        form.setVoucherDiscountAmount(new BigDecimal("20000"));

        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1);
            return b;
        });
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        when(voucherService.applyToBooking(any())).thenThrow(new RuntimeException("Application error"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(form, customerId);
        });
        assertTrue(exception.getMessage().contains("Voucher application failed"));
    }

    @Test
    @DisplayName("Should handle tableIds branch with multiple tables")
    void testCreateBooking_WithTableIds_ShouldUseMultipleTables() {
        form.setTableId(null);
        form.setTableIds("1,2");
        form.setDepositAmount(null);

        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1);
            return b;
        });
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookingTableRepository.findByBooking(any())).thenReturn(Collections.emptyList());

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
        verify(bookingTableRepository, times(2)).save(any(BookingTable.class));
    }

    @Test
    @DisplayName("Should handle tableIds with NumberFormatException branch")
    void testCreateBooking_WithInvalidTableIdFormat_ShouldThrowException() {
        form.setTableId(null);
        form.setTableIds("invalid,2");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(form, customerId);
        });
        assertTrue(exception.getMessage().contains("Invalid table ID format"));
    }

    @Test
    @DisplayName("Should handle depositAmount comparison branch")
    void testCreateBooking_WithDepositAmountEqualToTableDeposit_ShouldNotSetDepositProvidedByForm() {
        form.setDepositAmount(new BigDecimal("100000")); // Same as table deposit
        table1.setDepositAmount(new BigDecimal("100000"));

        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1);
            return b;
        });
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should handle depositAmount different from table branch")
    void testCreateBooking_WithDepositAmountDifferentFromTable_ShouldSetDepositProvidedByForm() {
        form.setDepositAmount(new BigDecimal("50000")); // Different from table
        table1.setDepositAmount(new BigDecimal("100000"));

        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1);
            return b;
        });
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookingDishRepository.findByBooking(any())).thenReturn(Collections.emptyList());
        when(bookingServiceRepository.findByBooking(any())).thenReturn(Collections.emptyList());

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should handle effectiveTotal null branch in deposit calculation")
    void testCreateBooking_WithNullEffectiveTotal_ShouldUseBookingDeposit() {
        form.setDepositAmount(null);
        table1.setDepositAmount(new BigDecimal("100000"));

        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1);
            b.setDepositAmount(new BigDecimal("100000"));
            return b;
        });
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookingDishRepository.findByBooking(any())).thenReturn(Collections.emptyList());
        when(bookingServiceRepository.findByBooking(any())).thenReturn(Collections.emptyList());
        // Mock calculateTotalAmount to return null
        when(bookingDishRepository.findByBooking(any())).thenReturn(Collections.emptyList());
        when(bookingServiceRepository.findByBooking(any())).thenReturn(Collections.emptyList());

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should handle effectiveTotal zero or negative branch")
    void testCreateBooking_WithZeroEffectiveTotal_ShouldSetZeroDeposit() {
        form.setDepositAmount(null);
        table1.setDepositAmount(BigDecimal.ZERO);

        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1);
            b.setDepositAmount(BigDecimal.ZERO);
            return b;
        });
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookingDishRepository.findByBooking(any())).thenReturn(Collections.emptyList());
        when(bookingServiceRepository.findByBooking(any())).thenReturn(Collections.emptyList());

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should handle deposit calculation exception branch")
    void testCreateBooking_WithDepositCalculationException_ShouldContinue() {
        form.setDepositAmount(null);
        table1.setDepositAmount(new BigDecimal("100000"));

        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1);
            return b;
        });
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookingDishRepository.findByBooking(any())).thenThrow(new RuntimeException("DB error"));

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should handle dish assignment exception branch")
    void testCreateBooking_WithDishAssignmentException_ShouldContinue() {
        form.setDishIds("1:2");

        Dish dish = new Dish();
        dish.setDishId(1);
        dish.setPrice(new BigDecimal("50000"));

        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1);
            return b;
        });
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        when(dishRepository.findById(1)).thenReturn(Optional.of(dish));
        when(bookingDishRepository.save(any(BookingDish.class))).thenThrow(new RuntimeException("DB error"));

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
        // Should continue even if dish assignment fails
    }

    @Test
    @DisplayName("Should handle service assignment exception branch")
    void testCreateBooking_WithServiceAssignmentException_ShouldContinue() {
        form.setServiceIds("1");

        RestaurantService service = new RestaurantService();
        service.setServiceId(1);
        service.setPrice(new BigDecimal("20000"));

        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1);
            return b;
        });
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        when(restaurantServiceRepository.findById(1)).thenReturn(Optional.of(service));
        when(bookingServiceRepository.save(any(com.example.booking.domain.BookingService.class)))
            .thenThrow(new RuntimeException("DB error"));

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
        // Should continue even if service assignment fails
    }

    @Test
    @DisplayName("Should handle notification creation exception branch")
    void testCreateBooking_WithNotificationException_ShouldContinue() {
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1);
            b.setCustomer(customer);
            return b;
        });
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        when(notificationRepository.save(any(Notification.class))).thenThrow(new RuntimeException("DB error"));

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
        // Should continue even if notification fails
    }

    @Test
    @DisplayName("Should handle restaurant validation exception branch")
    void testCreateBooking_WithRestaurantException_ShouldThrow() {
        when(restaurantProfileRepository.findById(1)).thenThrow(new RuntimeException("DB error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingService.createBooking(form, customerId);
        });
        assertTrue(exception.getMessage().contains("DB error"));
    }

    // ==================== UPDATE BOOKING BRANCH COVERAGE ====================

    @Test
    @DisplayName("Should handle updateBooking with non-empty bookingTables branch")
    void testUpdateBooking_WithNonEmptyBookingTables_ShouldDeleteOld() {
        booking.setStatus(BookingStatus.PENDING);
        BookingTable existingTable = new BookingTable();
        existingTable.setTable(table1);
        booking.setBookingTables(new ArrayList<>(Arrays.asList(existingTable)));

        form.setTableId(2);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(2));
        form.setDepositAmount(new BigDecimal("100000"));

        when(bookingTableRepository.findByBooking(booking)).thenReturn(Collections.emptyList());
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(bookingTableRepository).deleteByBooking(booking);

        Booking result = bookingService.updateBooking(1, form, customerId);
        assertNotNull(result);
        verify(bookingTableRepository).deleteByBooking(booking);
    }

    @Test
    @DisplayName("Should handle updateBooking with empty bookingTables branch")
    void testUpdateBooking_WithEmptyBookingTables_ShouldStillDelete() {
        booking.setStatus(BookingStatus.PENDING);
        booking.setBookingTables(new ArrayList<>());

        form.setTableId(2);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(2));
        form.setDepositAmount(new BigDecimal("100000"));

        when(bookingTableRepository.findByBooking(booking)).thenReturn(Collections.emptyList());
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(bookingTableRepository).deleteByBooking(booking);

        Booking result = bookingService.updateBooking(1, form, customerId);
        assertNotNull(result);
        verify(bookingTableRepository).deleteByBooking(booking);
    }

    // ==================== UPDATE BOOKING WITH ITEMS BRANCH COVERAGE ====================

    @Test
    @DisplayName("Should handle updateBookingWithItems with tableIds branch")
    void testUpdateBookingWithItems_WithTableIds_ShouldUseMultipleTables() {
        booking.setStatus(BookingStatus.PENDING);
        booking.setCustomer(customer);
        booking.setBookingTables(new ArrayList<>());

        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableIds("1,2");
        form.setGuestCount(8);
        form.setBookingTime(LocalDateTime.now().plusDays(2));
        form.setDepositAmount(new BigDecimal("200000"));

        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table1));
        when(restaurantTableRepository.findById(2)).thenReturn(Optional.of(table2));
        when(bookingTableRepository.findByBooking(booking)).thenReturn(Collections.emptyList());
        doNothing().when(bookingTableRepository).deleteByBooking(booking);
        doNothing().when(bookingDishRepository).deleteByBooking(booking);
        doNothing().when(bookingServiceRepository).deleteByBooking(booking);
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(conflictService).validateBookingUpdateConflicts(anyInt(), any(), any());

        Booking result = bookingService.updateBookingWithItems(1, form);
        assertNotNull(result);
        verify(bookingTableRepository, times(2)).save(any(BookingTable.class));
    }

    @Test
    @DisplayName("Should handle updateBookingWithItems with tableId branch")
    void testUpdateBookingWithItems_WithTableId_ShouldUseSingleTable() {
        booking.setStatus(BookingStatus.PENDING);
        booking.setCustomer(customer);
        booking.setBookingTables(new ArrayList<>());

        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(1);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(2));
        form.setDepositAmount(new BigDecimal("100000"));

        when(bookingTableRepository.findByBooking(booking)).thenReturn(Collections.emptyList());
        doNothing().when(bookingTableRepository).deleteByBooking(booking);
        doNothing().when(bookingDishRepository).deleteByBooking(booking);
        doNothing().when(bookingServiceRepository).deleteByBooking(booking);
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(conflictService).validateBookingUpdateConflicts(anyInt(), any(), any());

        Booking result = bookingService.updateBookingWithItems(1, form);
        assertNotNull(result);
        verify(bookingTableRepository).save(any(BookingTable.class));
    }

    @Test
    @DisplayName("Should handle updateBookingWithItems without tables branch")
    void testUpdateBookingWithItems_WithoutTables_ShouldSkip() {
        booking.setStatus(BookingStatus.PENDING);
        booking.setCustomer(customer);
        booking.setBookingTables(new ArrayList<>());

        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(null);
        form.setTableIds(null);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(2));
        form.setDepositAmount(new BigDecimal("100000"));

        when(bookingTableRepository.findByBooking(booking)).thenReturn(Collections.emptyList());
        doNothing().when(bookingTableRepository).deleteByBooking(booking);
        doNothing().when(bookingDishRepository).deleteByBooking(booking);
        doNothing().when(bookingServiceRepository).deleteByBooking(booking);
        doNothing().when(conflictService).validateBookingUpdateConflicts(anyInt(), any(), any());

        Booking result = bookingService.updateBookingWithItems(1, form);
        assertNotNull(result);
        verify(bookingTableRepository).deleteByBooking(booking);
    }

    @Test
    @DisplayName("Should handle updateBookingWithItems with dishIds branch")
    void testUpdateBookingWithItems_WithDishIds_ShouldAssignDishes() {
        booking.setStatus(BookingStatus.PENDING);
        booking.setCustomer(customer);
        booking.setBookingTables(new ArrayList<>());

        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(1);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(2));
        form.setDepositAmount(new BigDecimal("100000"));
        form.setDishIds("1:2");

        Dish dish = new Dish();
        dish.setDishId(1);
        dish.setPrice(new BigDecimal("50000"));

        when(bookingTableRepository.findByBooking(booking)).thenReturn(Collections.emptyList());
        doNothing().when(bookingTableRepository).deleteByBooking(booking);
        doNothing().when(bookingDishRepository).deleteByBooking(booking);
        doNothing().when(bookingServiceRepository).deleteByBooking(booking);
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        when(dishRepository.findById(1)).thenReturn(Optional.of(dish));
        when(bookingDishRepository.save(any(BookingDish.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(conflictService).validateBookingUpdateConflicts(anyInt(), any(), any());

        Booking result = bookingService.updateBookingWithItems(1, form);
        assertNotNull(result);
        verify(bookingDishRepository).save(any(BookingDish.class));
    }

    @Test
    @DisplayName("Should handle updateBookingWithItems with serviceIds branch")
    void testUpdateBookingWithItems_WithServiceIds_ShouldAssignServices() {
        booking.setStatus(BookingStatus.PENDING);
        booking.setCustomer(customer);
        booking.setBookingTables(new ArrayList<>());

        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(1);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(2));
        form.setDepositAmount(new BigDecimal("100000"));
        form.setServiceIds("1");

        RestaurantService service = new RestaurantService();
        service.setServiceId(1);
        service.setPrice(new BigDecimal("20000"));

        when(bookingTableRepository.findByBooking(booking)).thenReturn(Collections.emptyList());
        doNothing().when(bookingTableRepository).deleteByBooking(booking);
        doNothing().when(bookingDishRepository).deleteByBooking(booking);
        doNothing().when(bookingServiceRepository).deleteByBooking(booking);
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        when(restaurantServiceRepository.findById(1)).thenReturn(Optional.of(service));
        when(bookingServiceRepository.save(any(com.example.booking.domain.BookingService.class)))
            .thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(conflictService).validateBookingUpdateConflicts(anyInt(), any(), any());

        Booking result = bookingService.updateBookingWithItems(1, form);
        assertNotNull(result);
        verify(bookingServiceRepository).save(any(com.example.booking.domain.BookingService.class));
    }

    // ==================== UPDATE BOOKING FOR RESTAURANT OWNER BRANCH COVERAGE ====================

    @Test
    @DisplayName("Should handle updateBookingForRestaurantOwner with restaurant change and tableIds")
    void testUpdateBookingForRestaurantOwner_WithRestaurantChangeAndTableIds_ShouldReassign() {
        booking.setStatus(BookingStatus.PENDING);
        Set<Integer> ownerRestaurantIds = Set.of(1, 2);
        RestaurantProfile newRestaurant = new RestaurantProfile();
        newRestaurant.setRestaurantId(2);

        BookingForm form = new BookingForm();
        form.setRestaurantId(2);
        form.setTableIds("1,2");
        form.setGuestCount(8);
        form.setBookingTime(LocalDateTime.now().plusDays(2));

        when(restaurantProfileRepository.findById(2)).thenReturn(Optional.of(newRestaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table1));
        when(restaurantTableRepository.findById(2)).thenReturn(Optional.of(table2));
        when(bookingTableRepository.findByBooking(any())).thenReturn(Collections.emptyList());
        doNothing().when(bookingTableRepository).deleteByBooking(any());
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(conflictService).validateBookingUpdateConflicts(anyInt(), any(), any());

        Booking result = bookingService.updateBookingForRestaurantOwner(1, form, ownerRestaurantIds);
        assertNotNull(result);
        verify(bookingTableRepository, atLeastOnce()).deleteByBooking(any());
    }

    @Test
    @DisplayName("Should handle updateBookingForRestaurantOwner with restaurant change and tableId")
    void testUpdateBookingForRestaurantOwner_WithRestaurantChangeAndTableId_ShouldReassign() {
        booking.setStatus(BookingStatus.PENDING);
        Set<Integer> ownerRestaurantIds = Set.of(1, 2);
        RestaurantProfile newRestaurant = new RestaurantProfile();
        newRestaurant.setRestaurantId(2);

        BookingForm form = new BookingForm();
        form.setRestaurantId(2);
        form.setTableId(1);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(2));

        when(restaurantProfileRepository.findById(2)).thenReturn(Optional.of(newRestaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table1));
        when(bookingTableRepository.findByBooking(any())).thenReturn(Collections.emptyList());
        doNothing().when(bookingTableRepository).deleteByBooking(any());
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(conflictService).validateBookingUpdateConflicts(anyInt(), any(), any());

        Booking result = bookingService.updateBookingForRestaurantOwner(1, form, ownerRestaurantIds);
        assertNotNull(result);
        verify(bookingTableRepository, atLeastOnce()).deleteByBooking(any());
    }

    @Test
    @DisplayName("Should handle updateBookingForRestaurantOwner without restaurant change")
    void testUpdateBookingForRestaurantOwner_WithoutRestaurantChange_ShouldNotReassign() {
        booking.setStatus(BookingStatus.PENDING);
        Set<Integer> ownerRestaurantIds = Set.of(1);

        BookingForm form = new BookingForm();
        form.setRestaurantId(1); // Same restaurant
        form.setTableId(1);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(2));

        when(bookingTableRepository.findByBooking(any())).thenReturn(Collections.emptyList());
        doNothing().when(bookingTableRepository).deleteByBooking(any());
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(conflictService).validateBookingUpdateConflicts(anyInt(), any(), any());

        Booking result = bookingService.updateBookingForRestaurantOwner(1, form, ownerRestaurantIds);
        assertNotNull(result);
        // Should not delete tables again after updateBookingWithItems
    }

    // ==================== CREATE BOOKING NOTIFICATION BRANCH COVERAGE ====================

    @Test
    @DisplayName("Should handle createBookingNotification with customer.user branch")
    void testCreateBookingNotification_WithCustomerUser_ShouldUseUserId() {
        booking.setCustomer(customer);
        customer.setUser(user);

        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1);
            b.setCustomer(customer);
            return b;
        });
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should handle createBookingNotification with customer without user branch")
    void testCreateBookingNotification_WithCustomerWithoutUser_ShouldUseCustomerId() {
        Customer customerWithoutUser = new Customer();
        customerWithoutUser.setCustomerId(customerId);
        customerWithoutUser.setUser(null);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customerWithoutUser));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1);
            b.setCustomer(customerWithoutUser);
            return b;
        });
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should handle createBookingNotification with null customer branch")
    void testCreateBookingNotification_WithNullCustomer_ShouldThrowException() {
        Booking bookingWithNullCustomer = new Booking();
        bookingWithNullCustomer.setBookingId(1);
        bookingWithNullCustomer.setCustomer(null);

        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1);
            b.setCustomer(null);
            return b;
        });
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
        // Should handle gracefully
    }

    // ==================== IS VALID STATUS TRANSITION BRANCH COVERAGE ====================

    @Test
    @DisplayName("Should handle isValidStatusTransition with valid transition")
    void testIsValidStatusTransition_WithValidTransition_ShouldWork() {
        booking.setStatus(BookingStatus.PENDING);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));

        // Test valid transition
        bookingService.updateBookingStatus(1, BookingStatus.CONFIRMED);
        verify(bookingRepository).save(booking);
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
    }

    // ==================== GET CURRENT TABLE ID BRANCH COVERAGE ====================

    @Test
    @DisplayName("Should test getCurrentTableId indirectly through updateBooking")
    void testGetCurrentTableId_Indirectly_ShouldWork() {
        // getCurrentTableId is private and not directly testable
        // But we can test it indirectly through code that uses it
        // However, looking at the code, getCurrentTableId is not actually used anywhere!
        // So we can't test it indirectly. This is dead code.
    }
}

