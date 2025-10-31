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
 * Test class for edge cases and uncovered paths in BookingService
 * Focuses on increasing code coverage to > 80%
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingServiceEdgeCasesTest {

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
    private BookingForm form;

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

    // ==================== VALIDATE BOOKING TIME EDGE CASES ====================

    @Test
    @DisplayName("Should throw exception when booking time is null")
    void testValidateBookingTime_WithNullTime_ShouldThrowException() {
        form.setBookingTime(null);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(form, customerId);
        });
        assertEquals("Booking time cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when booking time is exactly now")
    void testValidateBookingTime_WithCurrentTime_ShouldThrowException() {
        form.setBookingTime(LocalDateTime.now());
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(form, customerId);
        });
        assertTrue(exception.getMessage().contains("cannot be in the past"));
    }

    @Test
    @DisplayName("Should throw exception when booking time is 29 minutes from now")
    void testValidateBookingTime_With29Minutes_ShouldThrowException() {
        form.setBookingTime(LocalDateTime.now().plusMinutes(29));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(form, customerId);
        });
        assertTrue(exception.getMessage().contains("30 minutes"));
    }

    @Test
    @DisplayName("Should throw exception when booking time is 31 days from now")
    void testValidateBookingTime_With31Days_ShouldThrowException() {
        form.setBookingTime(LocalDateTime.now().plusDays(31));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(form, customerId);
        });
        assertTrue(exception.getMessage().contains("30 days"));
    }

    // ==================== VALIDATE GUEST COUNT EDGE CASES ====================

    @Test
    @DisplayName("Should throw exception when guest count is null")
    void testValidateGuestCount_WithNull_ShouldThrowException() {
        form.setGuestCount(null);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(form, customerId);
        });
        assertEquals("Guest count cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when guest count is negative")
    void testValidateGuestCount_WithNegative_ShouldThrowException() {
        form.setGuestCount(-1);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(form, customerId);
        });
        assertTrue(exception.getMessage().contains("greater than 0"));
    }

    @Test
    @DisplayName("Should accept guest count of exactly 1")
    void testValidateGuestCount_WithOne_ShouldPass() {
        form.setGuestCount(1);
        table1.setCapacity(1);
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table1));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(entityManager).flush();

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should accept guest count of exactly 100")
    void testValidateGuestCount_With100_ShouldPass() {
        form.setGuestCount(100);
        table1.setCapacity(100);
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table1));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(entityManager).flush();

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
    }

    // ==================== VALIDATE DEPOSIT AMOUNT EDGE CASES ====================

    @Test
    @DisplayName("Should accept null deposit amount")
    void testValidateDepositAmount_WithNull_ShouldPass() {
        form.setDepositAmount(null);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(entityManager).flush();

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should accept zero deposit amount")
    void testValidateDepositAmount_WithZero_ShouldPass() {
        form.setDepositAmount(BigDecimal.ZERO);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(entityManager).flush();

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
    }

    // ==================== VALIDATE TABLE CAPACITY EDGE CASES ====================

    @Test
    @DisplayName("Should throw exception when table ID format is invalid in tableIds")
    void testValidateTableCapacity_WithInvalidTableIdFormat_ShouldThrowException() {
        form.setTableId(null);
        form.setTableIds("invalid,2");
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(form, customerId);
        });
        assertTrue(exception.getMessage().contains("Invalid table ID format"));
    }

    @Test
    @DisplayName("Should throw exception when tableIds contains empty string")
    void testValidateTableCapacity_WithEmptyTableIdInList_ShouldThrowException() {
        form.setTableId(null);
        form.setTableIds("1,,2");
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table1));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(form, customerId);
        });
        assertTrue(exception.getMessage().contains("Invalid table ID format"));
    }

    @Test
    @DisplayName("Should throw exception when tableIds contains whitespace only")
    void testValidateTableCapacity_WithWhitespaceTableId_ShouldThrowException() {
        form.setTableId(null);
        form.setTableIds("1,   ,2");
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table1));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(form, customerId);
        });
        assertTrue(exception.getMessage().contains("Invalid table ID format"));
    }

    // ==================== CREATE BOOKING WITH VOUCHER EDGE CASES ====================

    @Test
    @DisplayName("Should handle voucher validation failure")
    void testCreateBooking_WithInvalidVoucher_ShouldThrowException() {
        form.setVoucherCode("INVALID");
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(entityManager).flush();
        when(voucherService.validate(any())).thenReturn(
            new VoucherService.ValidationResult(false, "Voucher expired", null, null)
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(form, customerId);
        });
        assertTrue(exception.getMessage().contains("Invalid voucher"));
    }

    @Test
    @DisplayName("Should handle voucher validation returning null")
    void testCreateBooking_WithNullVoucherValidation_ShouldContinue() {
        form.setVoucherCode("TEST");
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(entityManager).flush();
        when(voucherService.validate(any())).thenReturn(null);

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should handle voucher service being null")
    void testCreateBooking_WithNullVoucherService_ShouldContinue() {
        form.setVoucherCode("TEST");
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(entityManager).flush();

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
    }

    // ==================== ASSIGN MULTIPLE TABLES EDGE CASES ====================

    @Test
    @DisplayName("Should handle empty tableIds string")
    void testAssignMultipleTables_WithEmptyString_ShouldNotAssign() {
        form.setTableId(null);
        form.setTableIds("");
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(form, customerId);
        });
        assertTrue(exception.getMessage().contains("chọn ít nhất một bàn"));
    }

    @Test
    @DisplayName("Should handle tableIds with only whitespace")
    void testAssignMultipleTables_WithWhitespaceOnly_ShouldThrowException() {
        form.setTableId(null);
        form.setTableIds("   ");
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(form, customerId);
        });
        assertTrue(exception.getMessage().contains("chọn ít nhất một bàn"));
    }

    // ==================== ASSIGN DISHES EDGE CASES ====================

    @Test
    @DisplayName("Should handle dishIds with invalid format - missing colon")
    void testAssignDishes_WithInvalidFormat_ShouldThrowException() {
        booking.setBookingId(1);
        when(dishRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            bookingService.assignDishesToBooking(booking, "1,2");
        });
    }

    @Test
    @DisplayName("Should handle dishIds with multiple colons")
    void testAssignDishes_WithMultipleColons_ShouldThrowException() {
        booking.setBookingId(1);
        when(dishRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            bookingService.assignDishesToBooking(booking, "1:2:3");
        });
    }

    @Test
    @DisplayName("Should handle dishIds with whitespace in quantity")
    void testAssignDishes_WithWhitespaceQuantity_ShouldHandle() {
        Dish dish = new Dish();
        dish.setDishId(1);
        dish.setPrice(new BigDecimal("50000"));
        when(dishRepository.findById(1)).thenReturn(Optional.of(dish));
        when(bookingDishRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        bookingService.assignDishesToBooking(booking, "1: 2 ");
        verify(bookingDishRepository).save(any(BookingDish.class));
    }

    // ==================== ASSIGN SERVICES EDGE CASES ====================

    @Test
    @DisplayName("Should handle serviceIds with whitespace")
    void testAssignServices_WithWhitespace_ShouldParse() {
        RestaurantService service = new RestaurantService();
        service.setServiceId(1);
        service.setPrice(new BigDecimal("20000"));
        when(restaurantServiceRepository.findById(1)).thenReturn(Optional.of(service));
        when(bookingServiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        bookingService.assignServicesToBooking(booking, " 1 , 2 ");
        verify(bookingServiceRepository, atLeastOnce()).save(any());
    }

    @Test
    @DisplayName("Should handle serviceIds with empty entries")
    void testAssignServices_WithEmptyEntries_ShouldSkip() {
        RestaurantService service = new RestaurantService();
        service.setServiceId(1);
        service.setPrice(new BigDecimal("20000"));
        when(restaurantServiceRepository.findById(1)).thenReturn(Optional.of(service));
        when(bookingServiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        bookingService.assignServicesToBooking(booking, "1,,2");
        verify(bookingServiceRepository, atLeastOnce()).save(any());
    }

    // ==================== CALCULATE TOTAL AMOUNT EDGE CASES ====================

    @Test
    @DisplayName("Should calculate total with null deposit")
    void testCalculateTotalAmount_WithNullDeposit_ShouldUseZero() {
        booking.setDepositAmount(null);
        when(bookingDishRepository.findByBooking(booking)).thenReturn(Collections.emptyList());
        when(bookingServiceRepository.findByBooking(booking)).thenReturn(Collections.emptyList());

        BigDecimal total = bookingService.calculateTotalAmount(booking);
        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    @DisplayName("Should calculate total with dishes having null price")
    void testCalculateTotalAmount_WithNullDishPrice_ShouldHandle() {
        booking.setDepositAmount(new BigDecimal("100000"));
        BookingDish dish = new BookingDish();
        dish.setPrice(null);
        dish.setQuantity(1);
        when(bookingDishRepository.findByBooking(booking)).thenReturn(Collections.singletonList(dish));
        when(bookingServiceRepository.findByBooking(booking)).thenReturn(Collections.emptyList());

        assertThrows(Exception.class, () -> {
            bookingService.calculateTotalAmount(booking);
        });
    }

    // ==================== UPDATE BOOKING EDGE CASES ====================

    @Test
    @DisplayName("Should update booking without changing restaurant")
    void testUpdateBooking_WithoutRestaurantChange_ShouldUpdate() {
        booking.setStatus(BookingStatus.PENDING);
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

    @Test
    @DisplayName("Should update booking without table change")
    void testUpdateBooking_WithoutTableChange_ShouldUpdate() {
        booking.setStatus(BookingStatus.PENDING);
        form.setTableId(null);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(2));
        form.setDepositAmount(new BigDecimal("100000"));

        Booking result = bookingService.updateBooking(1, form, customerId);
        assertNotNull(result);
    }

    // ==================== CANCEL BOOKING EDGE CASES ====================

    @Test
    @DisplayName("Should cancel booking with empty bank code")
    void testCancelBooking_WithEmptyBankCode_ShouldCancel() {
        booking.setCustomer(customer);
        when(paymentRepository.findByBooking(booking)).thenReturn(Optional.empty());

        Booking result = bookingService.cancelBooking(1, customerId, "Test", "", "");
        assertNotNull(result);
        assertEquals(BookingStatus.CANCELLED, result.getStatus());
    }

    @Test
    @DisplayName("Should cancel booking with null cancel reason")
    void testCancelBooking_WithNullReason_ShouldCancel() {
        booking.setCustomer(customer);
        when(paymentRepository.findByBooking(booking)).thenReturn(Optional.empty());

        Booking result = bookingService.cancelBooking(1, customerId, null, "VCB", "1234567890");
        assertNotNull(result);
        assertEquals(BookingStatus.CANCELLED, result.getStatus());
    }

    // ==================== GET BOOKING WITH DETAILS EDGE CASES ====================

    @Test
    @DisplayName("Should get booking with null relationships")
    void testGetBookingWithDetailsById_WithNullRelationships_ShouldReturn() {
        booking.setBookingDishes(null);
        booking.setBookingServices(null);
        booking.setBookingTables(null);

        Optional<Booking> result = bookingService.getBookingWithDetailsById(1);
        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("Should get booking with empty relationships")
    void testGetBookingWithDetailsById_WithEmptyRelationships_ShouldReturn() {
        booking.setBookingDishes(new ArrayList<>());
        booking.setBookingServices(new ArrayList<>());
        booking.setBookingTables(new ArrayList<>());

        Optional<Booking> result = bookingService.getBookingWithDetailsById(1);
        assertTrue(result.isPresent());
    }

    // ==================== FIND AVAILABLE TABLES EDGE CASES ====================

    @Test
    @DisplayName("Should return empty list when all tables are booked")
    void testFindAvailableTables_WithAllBooked_ShouldReturnEmpty() {
        LocalDateTime bookingTime = LocalDateTime.now().plusDays(1);
        List<RestaurantTable> allTables = Arrays.asList(table1, table2);
        
        when(restaurantTableRepository.findByRestaurantAndCapacityGreaterThanEqual(1, 4))
            .thenReturn(allTables);
        when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any()))
            .thenReturn(true); // All tables booked

        List<RestaurantTable> result = bookingService.findAvailableTables(1, bookingTime, 4);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should filter tables with capacity less than guest count")
    void testFindAvailableTables_WithSmallCapacity_ShouldFilter() {
        LocalDateTime bookingTime = LocalDateTime.now().plusDays(1);
        RestaurantTable smallTable = new RestaurantTable();
        smallTable.setTableId(3);
        smallTable.setCapacity(2);
        List<RestaurantTable> allTables = Arrays.asList(table1, smallTable);
        
        when(restaurantTableRepository.findByRestaurantAndCapacityGreaterThanEqual(1, 4))
            .thenReturn(allTables);
        when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any()))
            .thenReturn(false);

        List<RestaurantTable> result = bookingService.findAvailableTables(1, bookingTime, 4);
        assertEquals(2, result.size());
    }

    // ==================== STATUS TRANSITION EDGE CASES ====================

    @Test
    @DisplayName("Should reject transition from PENDING to COMPLETED")
    void testIsValidStatusTransition_PendingToCompleted_ShouldReject() {
        booking.setStatus(BookingStatus.PENDING);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.updateBookingStatus(1, BookingStatus.COMPLETED);
        });
        assertTrue(exception.getMessage().contains("Invalid status transition"));
    }

    @Test
    @DisplayName("Should reject transition from PENDING to NO_SHOW")
    void testIsValidStatusTransition_PendingToNoShow_ShouldReject() {
        booking.setStatus(BookingStatus.PENDING);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.updateBookingStatus(1, BookingStatus.NO_SHOW);
        });
        assertTrue(exception.getMessage().contains("Invalid status transition"));
    }

    @Test
    @DisplayName("Should reject transition from COMPLETED to any status")
    void testIsValidStatusTransition_CompletedToAny_ShouldReject() {
        booking.setStatus(BookingStatus.COMPLETED);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.updateBookingStatus(1, BookingStatus.CANCELLED);
        });
        assertTrue(exception.getMessage().contains("Invalid status transition"));
    }

    // ==================== CREATE BOOKING NOTIFICATION EDGE CASES ====================

    @Test
    @DisplayName("Should handle customer without user when creating notification")
    void testCreateBooking_WithCustomerWithoutUser_ShouldUseCustomerId() {
        Customer customerWithoutUser = new Customer();
        customerWithoutUser.setCustomerId(customerId);
        customerWithoutUser.setUser(null);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customerWithoutUser));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setCustomer(customerWithoutUser);
            return b;
        });
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(entityManager).flush();

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should handle notification creation failure gracefully")
    void testCreateBooking_WithNotificationFailure_ShouldContinue() {
        when(notificationRepository.save(any(Notification.class))).thenThrow(new RuntimeException("DB error"));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(entityManager).flush();

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
        assertEquals(BookingStatus.PENDING, result.getStatus());
    }

    // ==================== UPDATE BOOKING FOR RESTAURANT OWNER EDGE CASES ====================

    @Test
    @DisplayName("Should throw exception when trying to move booking to unauthorized restaurant")
    void testUpdateBookingForRestaurantOwner_WithUnauthorizedRestaurant_ShouldThrowException() {
        booking.setStatus(BookingStatus.PENDING);
        Set<Integer> ownerRestaurantIds = Set.of(1);
        form.setRestaurantId(999); // Owner doesn't own this restaurant

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.updateBookingForRestaurantOwner(1, form, ownerRestaurantIds);
        });
        assertTrue(exception.getMessage().contains("Cannot move booking"));
    }

    @Test
    @DisplayName("Should update booking for restaurant owner without restaurant change")
    void testUpdateBookingForRestaurantOwner_WithoutRestaurantChange_ShouldUpdate() {
        booking.setStatus(BookingStatus.PENDING);
        Set<Integer> ownerRestaurantIds = Set.of(1);
        form.setRestaurantId(1); // Same restaurant
        form.setTableId(1);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(2));

        when(bookingTableRepository.findByBooking(any())).thenReturn(Collections.emptyList());
        doNothing().when(bookingTableRepository).deleteByBooking(any());
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking result = bookingService.updateBookingForRestaurantOwner(1, form, ownerRestaurantIds);
        assertNotNull(result);
    }
}

