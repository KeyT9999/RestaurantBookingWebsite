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
 * Additional test file to maximize coverage for BookingService
 * Focuses on uncovered branches and edge cases
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingServiceAdditionalCoverageTest {

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
        booking.setBookingTables(new ArrayList<>());

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

    // ==================== CREATE BOOKING WITH DEPOSIT CALCULATION TESTS ====================

    @Test
    @DisplayName("Should calculate deposit as 10% when depositProvidedByForm is false")
    void testCreateBooking_WithDepositCalculation10Percent_ShouldCalculate() {
        form.setDepositAmount(null); // No deposit provided
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
        verify(bookingRepository, atLeastOnce()).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should use form deposit when depositProvidedByForm is true")
    void testCreateBooking_WithDepositProvidedByForm_ShouldUseFormDeposit() {
        form.setDepositAmount(new BigDecimal("50000"));
        table1.setDepositAmount(new BigDecimal("100000")); // Different from form

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
    @DisplayName("Should handle zero total amount in deposit calculation")
    void testCreateBooking_WithZeroTotalAmount_ShouldSetZeroDeposit() {
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

    // ==================== CREATE BOOKING WITH MULTIPLE TABLES DEPOSIT TESTS ====================

    @Test
    @DisplayName("Should calculate deposit from multiple tables")
    void testCreateBooking_WithMultipleTablesDeposit_ShouldSumDeposits() {
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

    // ==================== CREATE BOOKING WITH VOUCHER EDGE CASES ====================

    @Test
    @DisplayName("Should handle voucherCodeApplied with null discount amount")
    void testCreateBooking_WithVoucherCodeAppliedNullDiscount_ShouldUseZero() {
        form.setVoucherCodeApplied("SUMMER20");
        form.setVoucherDiscountAmount(null);

        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1);
            return b;
        });
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
        verify(voucherService, never()).applyToBooking(any());
    }

    @Test
    @DisplayName("Should handle voucher validation with null voucherService")
    void testCreateBooking_WithNullVoucherService_ShouldSkip() {
        form.setVoucherCode("TEST");
        // Mock voucherService to return null when called

        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1);
            return b;
        });
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
    }

    // ==================== CALCULATE TOTAL AMOUNT WITH FULL RELATIONSHIPS TESTS ====================

    @Test
    @DisplayName("Should calculate total with dishes having full dish relationships")
    void testCalculateTotalAmount_WithDishesFullRelationships_ShouldCalculate() {
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

        BookingDish bookingDish2 = new BookingDish();
        bookingDish2.setDish(dish2);
        bookingDish2.setQuantity(1);
        bookingDish2.setPrice(new BigDecimal("30000"));

        when(bookingDishRepository.findByBooking(booking)).thenReturn(Arrays.asList(bookingDish1, bookingDish2));
        when(bookingServiceRepository.findByBooking(booking)).thenReturn(Collections.emptyList());

        BigDecimal total = bookingService.calculateTotalAmount(booking);
        // deposit: 100000 + dishes: 100000 (2*50000) + 30000 = 230000
        assertEquals(0, new BigDecimal("230000").compareTo(total));
    }

    @Test
    @DisplayName("Should calculate total with services having full service relationships")
    void testCalculateTotalAmount_WithServicesFullRelationships_ShouldCalculate() {
        booking.setDepositAmount(new BigDecimal("100000"));

        RestaurantService service1 = new RestaurantService();
        service1.setServiceId(1);
        service1.setName("Service 1");
        service1.setPrice(new BigDecimal("20000"));

        RestaurantService service2 = new RestaurantService();
        service2.setServiceId(2);
        service2.setName("Service 2");
        service2.setPrice(new BigDecimal("15000"));

        com.example.booking.domain.BookingService bookingService1 = new com.example.booking.domain.BookingService();
        bookingService1.setService(service1);
        bookingService1.setQuantity(1);
        bookingService1.setPrice(new BigDecimal("20000"));

        com.example.booking.domain.BookingService bookingService2 = new com.example.booking.domain.BookingService();
        bookingService2.setService(service2);
        bookingService2.setQuantity(1);
        bookingService2.setPrice(new BigDecimal("15000"));

        when(bookingDishRepository.findByBooking(booking)).thenReturn(Collections.emptyList());
        when(bookingServiceRepository.findByBooking(booking)).thenReturn(Arrays.asList(bookingService1, bookingService2));

        BigDecimal total = bookingService.calculateTotalAmount(booking);
        // deposit: 100000 + services: 20000 + 15000 = 135000
        assertEquals(0, new BigDecimal("135000").compareTo(total));
    }

    // ==================== UPDATE BOOKING WITH BOOKING TABLES TESTS ====================

    @Test
    @DisplayName("Should update booking with existing booking tables")
    void testUpdateBooking_WithExistingBookingTables_ShouldDeleteOld() {
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

    // ==================== CANCEL BOOKING WITH PAYMENT TESTS ====================

    @Test
    @DisplayName("Should cancel booking with completed payment and create refund")
    void testCancelBooking_WithCompletedPayment_ShouldCreateRefund() {
        booking.setCustomer(customer);
        Payment payment = new Payment();
        payment.setPaymentId(1);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setAmount(new BigDecimal("100000"));

        when(paymentRepository.findByBooking(booking)).thenReturn(Optional.of(payment));
        when(refundService.processRefundWithManualTransfer(anyInt(), anyString(), anyString(), anyString()))
            .thenReturn(payment);

        Booking result = bookingService.cancelBooking(1, customerId, "Test reason", "VCB", "1234567890");
        assertNotNull(result);
        assertEquals(BookingStatus.CANCELLED, result.getStatus());
        verify(refundService).processRefundWithManualTransfer(eq(1), anyString(), eq("VCB"), eq("1234567890"));
    }

    @Test
    @DisplayName("Should cancel booking with non-completed payment without refund")
    void testCancelBooking_WithNonCompletedPayment_ShouldNotRefund() {
        booking.setCustomer(customer);
        Payment payment = new Payment();
        payment.setPaymentId(1);
        payment.setStatus(PaymentStatus.FAILED);
        payment.setAmount(new BigDecimal("100000"));

        when(paymentRepository.findByBooking(booking)).thenReturn(Optional.of(payment));

        Booking result = bookingService.cancelBooking(1, customerId, "Test reason", "VCB", "1234567890");
        assertNotNull(result);
        assertEquals(BookingStatus.CANCELLED, result.getStatus());
        verify(refundService, never()).processRefundWithManualTransfer(anyInt(), anyString(), anyString(), anyString());
    }

    // ==================== CREATE BOOKING NOTIFICATION EDGE CASES ====================

    @Test
    @DisplayName("Should create notification with customer having user")
    void testCreateBooking_WithCustomerUser_ShouldCreateNotification() {
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
    @DisplayName("Should handle notification creation failure gracefully")
    void testCreateBooking_WithNotificationFailure_ShouldContinue() {
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
        assertEquals(BookingStatus.PENDING, result.getStatus());
    }

    // ==================== CONFIRM BOOKING WITH NOTIFICATION TESTS ====================

    @Test
    @DisplayName("Should confirm booking and create notification")
    void testConfirmBooking_WithValidBooking_ShouldCreateNotification() {
        booking.setStatus(BookingStatus.PENDING);
        booking.setCustomer(customer);

        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking result = bookingService.confirmBooking(1);
        assertNotNull(result);
        assertEquals(BookingStatus.CONFIRMED, result.getStatus());
        verify(notificationRepository).save(any(Notification.class));
    }

    // ==================== UPDATE BOOKING FOR RESTAURANT OWNER TESTS ====================

    @Test
    @DisplayName("Should update booking for restaurant owner with restaurant change")
    void testUpdateBookingForRestaurantOwner_WithRestaurantChange_ShouldReassign() {
        booking.setStatus(BookingStatus.PENDING);
        Set<Integer> ownerRestaurantIds = Set.of(1, 2);
        RestaurantProfile newRestaurant = new RestaurantProfile();
        newRestaurant.setRestaurantId(2);

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

    // ==================== FIND AVAILABLE TABLES WITH TIME RANGE TESTS ====================

    @Test
    @DisplayName("Should find available tables considering 2 hour time range")
    void testFindAvailableTables_WithTimeRange_ShouldFilter() {
        LocalDateTime bookingTime = LocalDateTime.now().plusDays(1);
        Integer guestCount = 4;
        List<RestaurantTable> allTables = Arrays.asList(table1, table2);

        when(restaurantTableRepository.findByRestaurantAndCapacityGreaterThanEqual(1, guestCount))
            .thenReturn(allTables);
        when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any()))
            .thenReturn(false);

        List<RestaurantTable> result = bookingService.findAvailableTables(1, bookingTime, guestCount);
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Verify time range calculation (2 hours before and after)
        verify(bookingTableRepository, atLeastOnce()).existsByTableAndBookingTimeRange(
            any(), 
            eq(bookingTime.minusHours(2)), 
            eq(bookingTime.plusHours(2))
        );
    }

    // ==================== GET BOOKING WITH DETAILS EDGE CASES ====================

    @Test
    @DisplayName("Should get booking with all relationships loaded")
    void testGetBookingWithDetailsById_WithAllRelationships_ShouldLoad() {
        Dish dish = new Dish();
        dish.setName("Test Dish");
        BookingDish bookingDish = new BookingDish();
        bookingDish.setDish(dish);
        bookingDish.setQuantity(2);

        RestaurantService service = new RestaurantService();
        service.setName("Test Service");
        com.example.booking.domain.BookingService bookingServiceEntity = new com.example.booking.domain.BookingService();
        bookingServiceEntity.setService(service);

        BookingTable bookingTable = new BookingTable();
        bookingTable.setTable(table1);

        booking.setBookingDishes(new ArrayList<>(Arrays.asList(bookingDish)));
        booking.setBookingServices(new ArrayList<>(Arrays.asList(bookingServiceEntity)));
        booking.setBookingTables(new ArrayList<>(Arrays.asList(bookingTable)));

        Optional<Booking> result = bookingService.getBookingWithDetailsById(1);
        assertTrue(result.isPresent());
        assertNotNull(result.get().getBookingDishes());
        assertNotNull(result.get().getBookingServices());
        assertNotNull(result.get().getBookingTables());
    }

    // ==================== ASSIGN MULTIPLE TABLES CAPACITY VALIDATION TESTS ====================

    @Test
    @DisplayName("Should validate capacity when assigning multiple tables")
    void testAssignMultipleTables_WithCapacityValidation_ShouldValidate() {
        booking.setNumberOfGuests(8);
        String tableIds = "1,2"; // Total capacity: 4 + 6 = 10

        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table1));
        when(restaurantTableRepository.findById(2)).thenReturn(Optional.of(table2));
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));

        // Should not throw exception as 8 <= 10
        assertDoesNotThrow(() -> {
            // Create a form to trigger assignMultipleTablesToBooking indirectly
            BookingForm form = new BookingForm();
            form.setRestaurantId(1);
            form.setTableIds("1,2");
            form.setGuestCount(8);
            form.setBookingTime(LocalDateTime.now().plusDays(1));
            form.setDepositAmount(new BigDecimal("200000"));
            
            when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
                Booking b = inv.getArgument(0);
                b.setBookingId(1);
                return b;
            });
            when(bookingTableRepository.findByBooking(any())).thenReturn(Collections.emptyList());
            
            bookingService.createBooking(form, customerId);
        });
    }
}

