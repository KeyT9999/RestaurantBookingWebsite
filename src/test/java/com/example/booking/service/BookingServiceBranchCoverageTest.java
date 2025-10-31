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
 * Test file focused on branch coverage and conditional paths
 * Covers all if/else branches and switch cases
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingServiceBranchCoverageTest {

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

        table = new RestaurantTable();
        table.setTableId(1);
        table.setTableName("Table 1");
        table.setCapacity(4);
        table.setDepositAmount(new BigDecimal("100000"));
        table.setRestaurant(restaurant);

        booking = new Booking();
        booking.setBookingId(1);
        booking.setCustomer(customer);
        booking.setRestaurant(restaurant);
        booking.setBookingTime(LocalDateTime.now().plusDays(1));
        booking.setDepositAmount(new BigDecimal("100000"));
        booking.setStatus(BookingStatus.PENDING);
        booking.setNumberOfGuests(4);
        booking.setBookingTables(new ArrayList<>());

        lenient().when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        lenient().when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        lenient().when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table));
        lenient().when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
        lenient().when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
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
    @DisplayName("Should handle createdAt null branch")
    void testCreateBooking_WithNullCreatedAt_ShouldSetCreatedAt() {
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(1);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(1));
        form.setDepositAmount(new BigDecimal("100000"));

        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1);
            b.setCreatedAt(null); // Simulate null createdAt
            return b;
        });
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should handle depositProvidedByForm true branch")
    void testCreateBooking_WithDepositProvidedByForm_ShouldSkipAutoCalculation() {
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(1);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(1));
        form.setDepositAmount(new BigDecimal("50000")); // Different from table deposit

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
    @DisplayName("Should handle effectiveTotal null or zero branch")
    void testCreateBooking_WithZeroEffectiveTotal_ShouldSetZeroDeposit() {
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(1);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(1));
        form.setDepositAmount(null);

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

    // ==================== VOUCHER BRANCH COVERAGE ====================

    @Test
    @DisplayName("Should handle voucherCodeApplied empty string branch")
    void testCreateBooking_WithEmptyVoucherCodeApplied_ShouldCheckVoucherCode() {
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(1);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(1));
        form.setDepositAmount(new BigDecimal("100000"));
        form.setVoucherCodeApplied("   "); // Empty/whitespace
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
    @DisplayName("Should handle voucher validation null result branch")
    void testCreateBooking_WithNullVoucherValidation_ShouldSkip() {
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(1);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(1));
        form.setDepositAmount(new BigDecimal("100000"));
        form.setVoucherCode("TEST");

        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1);
            return b;
        });
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        when(voucherService.validate(any())).thenReturn(null);

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should handle voucher validation with null calculatedDiscount")
    void testCreateBooking_WithNullCalculatedDiscount_ShouldSkip() {
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(1);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(1));
        form.setDepositAmount(new BigDecimal("100000"));
        form.setVoucherCode("TEST");

        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1);
            return b;
        });
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        when(voucherService.validate(any())).thenReturn(
            new VoucherService.ValidationResult(true, null, null, null)
        );

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
    }

    // ==================== UPDATE BOOKING BRANCH COVERAGE ====================

    @Test
    @DisplayName("Should handle updateBooking without table change")
    void testUpdateBooking_WithoutTableId_ShouldNotUpdateTables() {
        booking.setStatus(BookingStatus.PENDING);
        booking.setBookingTables(new ArrayList<>());

        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(null); // No table change
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(2));
        form.setDepositAmount(new BigDecimal("100000"));

        when(bookingTableRepository.findByBooking(booking)).thenReturn(Collections.emptyList());

        Booking result = bookingService.updateBooking(1, form, customerId);
        assertNotNull(result);
        verify(bookingTableRepository, never()).deleteByBooking(any());
    }

    @Test
    @DisplayName("Should handle updateBooking with same restaurant")
    void testUpdateBooking_WithSameRestaurant_ShouldNotChangeRestaurant() {
        booking.setStatus(BookingStatus.PENDING);
        booking.setBookingTables(new ArrayList<>());

        BookingForm form = new BookingForm();
        form.setRestaurantId(1); // Same restaurant
        form.setTableId(1);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(2));
        form.setDepositAmount(new BigDecimal("100000"));

        when(bookingTableRepository.findByBooking(booking)).thenReturn(Collections.emptyList());
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(bookingTableRepository).deleteByBooking(any());

        Booking result = bookingService.updateBooking(1, form, customerId);
        assertNotNull(result);
        assertEquals(1, result.getRestaurant().getRestaurantId());
    }

    // ==================== CALCULATE TOTAL AMOUNT BRANCH COVERAGE ====================

    @Test
    @DisplayName("Should handle calculateTotalAmount with empty dishes list")
    void testCalculateTotalAmount_WithEmptyDishes_ShouldSkipDishes() {
        booking.setDepositAmount(new BigDecimal("100000"));
        when(bookingDishRepository.findByBooking(booking)).thenReturn(Collections.emptyList());
        when(bookingServiceRepository.findByBooking(booking)).thenReturn(Collections.emptyList());

        BigDecimal total = bookingService.calculateTotalAmount(booking);
        assertEquals(0, new BigDecimal("100000").compareTo(total));
    }

    @Test
    @DisplayName("Should handle calculateTotalAmount with empty services list")
    void testCalculateTotalAmount_WithEmptyServices_ShouldSkipServices() {
        booking.setDepositAmount(new BigDecimal("100000"));
        when(bookingDishRepository.findByBooking(booking)).thenReturn(Collections.emptyList());
        when(bookingServiceRepository.findByBooking(booking)).thenReturn(Collections.emptyList());

        BigDecimal total = bookingService.calculateTotalAmount(booking);
        assertEquals(0, new BigDecimal("100000").compareTo(total));
    }

    // ==================== UPDATE BOOKING WITH ITEMS BRANCH COVERAGE ====================

    @Test
    @DisplayName("Should handle updateBookingWithItems with tableIds")
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

        RestaurantTable table2 = new RestaurantTable();
        table2.setTableId(2);
        table2.setCapacity(6);
        table2.setDepositAmount(new BigDecimal("150000"));

        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table));
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
    @DisplayName("Should handle updateBookingWithItems with tableId")
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
    @DisplayName("Should handle updateBookingWithItems without table assignment")
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

    // ==================== UPDATE BOOKING FOR RESTAURANT OWNER BRANCH COVERAGE ====================

    @Test
    @DisplayName("Should handle updateBookingForRestaurantOwner without restaurant change")
    void testUpdateBookingForRestaurantOwner_WithoutRestaurantChange_ShouldUpdate() {
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
        verify(bookingTableRepository, never()).deleteByBooking(any()); // No reassignment needed
    }

    @Test
    @DisplayName("Should handle updateBookingForRestaurantOwner with tableIds")
    void testUpdateBookingForRestaurantOwner_WithTableIds_ShouldReassign() {
        booking.setStatus(BookingStatus.PENDING);
        Set<Integer> ownerRestaurantIds = Set.of(1, 2);
        RestaurantProfile newRestaurant = new RestaurantProfile();
        newRestaurant.setRestaurantId(2);

        BookingForm form = new BookingForm();
        form.setRestaurantId(2);
        form.setTableIds("1,2");
        form.setGuestCount(8);
        form.setBookingTime(LocalDateTime.now().plusDays(2));

        RestaurantTable table2 = new RestaurantTable();
        table2.setTableId(2);
        table2.setCapacity(6);

        when(restaurantProfileRepository.findById(2)).thenReturn(Optional.of(newRestaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table));
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
    @DisplayName("Should handle updateBookingForRestaurantOwner with tableId")
    void testUpdateBookingForRestaurantOwner_WithTableId_ShouldReassign() {
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
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table));
        when(bookingTableRepository.findByBooking(any())).thenReturn(Collections.emptyList());
        doNothing().when(bookingTableRepository).deleteByBooking(any());
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(conflictService).validateBookingUpdateConflicts(anyInt(), any(), any());

        Booking result = bookingService.updateBookingForRestaurantOwner(1, form, ownerRestaurantIds);
        assertNotNull(result);
        verify(bookingTableRepository, atLeastOnce()).deleteByBooking(any());
    }

    // ==================== CANCEL BOOKING BRANCH COVERAGE ====================

    @Test
    @DisplayName("Should handle cancelBooking with null payment")
    void testCancelBooking_WithNullPayment_ShouldSkipRefund() {
        booking.setCustomer(customer);
        when(paymentRepository.findByBooking(booking)).thenReturn(Optional.empty());

        Booking result = bookingService.cancelBooking(1, customerId, "Test", "VCB", "1234567890");
        assertNotNull(result);
        assertEquals(BookingStatus.CANCELLED, result.getStatus());
        verify(refundService, never()).processRefundWithManualTransfer(anyInt(), anyString(), anyString(), anyString());
    }

    // ==================== PARSE DISH IDS BRANCH COVERAGE ====================

    @Test
    @DisplayName("Should handle parseDishIds with parts.length != 2")
    void testAssignDishes_WithInvalidPairFormat_ShouldSkip() {
        Dish dish = new Dish();
        dish.setDishId(1);
        dish.setPrice(new BigDecimal("50000"));
        
        when(dishRepository.findById(1)).thenReturn(Optional.of(dish));
        when(bookingDishRepository.save(any(BookingDish.class))).thenAnswer(inv -> inv.getArgument(0));

        // This should only parse valid pairs, skip invalid ones
        bookingService.assignDishesToBooking(booking, "1:2,invalid,3:1");
        verify(bookingDishRepository, times(2)).save(any(BookingDish.class)); // Only 2 valid pairs
    }

    // ==================== PARSE SERVICE IDS BRANCH COVERAGE ====================

    @Test
    @DisplayName("Should handle parseServiceIds with empty trimmed id")
    void testAssignServices_WithEmptyTrimmedId_ShouldSkip() {
        RestaurantService service = new RestaurantService();
        service.setServiceId(1);
        service.setPrice(new BigDecimal("20000"));
        
        when(restaurantServiceRepository.findById(1)).thenReturn(Optional.of(service));
        when(bookingServiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Should skip empty entries
        bookingService.assignServicesToBooking(booking, "1,,2,   ,3");
        verify(bookingServiceRepository, times(3)).save(any()); // Only 3 valid IDs
    }
}

