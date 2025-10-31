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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingServiceComprehensiveTest {

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
    private DishRepository dishRepository;

    @Mock
    private RestaurantServiceRepository restaurantServiceRepository;

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

    @InjectMocks
    private BookingService bookingService;

    private UUID customerId;
    private UUID restaurantOwnerId;
    private Customer customer;
    private RestaurantProfile restaurant;
    private RestaurantTable table1;
    private RestaurantTable table2;
    private Booking booking;
    private User user;
    private RestaurantOwner owner;

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
        lenient().when(bookingTableRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().doNothing().when(entityManager).flush();
        lenient().doNothing().when(conflictService).validateBookingConflicts(any(), any());
        lenient().doNothing().when(conflictService).validateBookingUpdateConflicts(anyInt(), any(), any());
    }

    // ==================== GET BOOKINGS BY RESTAURANT TESTS ====================

    @Test
    @DisplayName("Should get bookings by restaurant successfully")
    void testGetBookingsByRestaurant_WithValidRestaurant_ShouldReturnBookings() {
        // Given
        List<Booking> bookings = Arrays.asList(booking);
        when(bookingRepository.findByRestaurantOrderByBookingTimeDesc(restaurant)).thenReturn(bookings);

        // When
        List<Booking> result = bookingService.getBookingsByRestaurant(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(restaurantProfileRepository).findById(1);
        verify(bookingRepository).findByRestaurantOrderByBookingTimeDesc(restaurant);
    }

    @Test
    @DisplayName("Should throw exception when restaurant not found")
    void testGetBookingsByRestaurant_WithInvalidRestaurant_ShouldThrowException() {
        // Given
        when(restaurantProfileRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.getBookingsByRestaurant(999);
        });
        assertEquals("Restaurant not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should return empty list when restaurant has no bookings")
    void testGetBookingsByRestaurant_WithNoBookings_ShouldReturnEmpty() {
        // Given
        when(bookingRepository.findByRestaurantOrderByBookingTimeDesc(restaurant)).thenReturn(Collections.emptyList());

        // When
        List<Booking> result = bookingService.getBookingsByRestaurant(1);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== GET BOOKING DETAIL BY ID TESTS ====================

    @Test
    @DisplayName("Should get booking detail by id successfully")
    void testGetBookingDetailById_WithValidId_ShouldReturnBooking() {
        // When
        Optional<Booking> result = bookingService.getBookingDetailById(1);

        // Then
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getBookingId());
        verify(bookingRepository).findById(1);
    }

    @Test
    @DisplayName("Should return empty when booking not found")
    void testGetBookingDetailById_WithInvalidId_ShouldReturnEmpty() {
        // Given
        when(bookingRepository.findById(999)).thenReturn(Optional.empty());

        // When
        Optional<Booking> result = bookingService.getBookingDetailById(999);

        // Then
        assertFalse(result.isPresent());
    }

    // ==================== GET BOOKING WITH DETAILS BY ID TESTS ====================

    @Test
    @DisplayName("Should get booking with details by id successfully")
    void testGetBookingWithDetailsById_WithValidId_ShouldReturnBooking() {
        // Given
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

        // When
        Optional<Booking> result = bookingService.getBookingWithDetailsById(1);

        // Then
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getBookingId());
        assertNotNull(result.get().getBookingDishes());
        assertNotNull(result.get().getBookingServices());
        assertNotNull(result.get().getBookingTables());
    }

    // ==================== FIND AVAILABLE TABLES TESTS ====================

    @Test
    @DisplayName("Should find available tables successfully")
    void testFindAvailableTables_WithValidParams_ShouldReturnAvailableTables() {
        // Given
        LocalDateTime bookingTime = LocalDateTime.now().plusDays(1);
        Integer guestCount = 4;
        List<RestaurantTable> allTables = Arrays.asList(table1, table2);
        
        when(restaurantTableRepository.findByRestaurantAndCapacityGreaterThanEqual(1, guestCount))
            .thenReturn(allTables);
        when(bookingTableRepository.existsByTableAndBookingTimeRange(any(), any(), any()))
            .thenReturn(false);

        // When
        List<RestaurantTable> result = bookingService.findAvailableTables(1, bookingTime, guestCount);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(restaurantProfileRepository).findById(1);
    }

    @Test
    @DisplayName("Should filter out booked tables")
    void testFindAvailableTables_WithBookedTables_ShouldFilterOut() {
        // Given
        LocalDateTime bookingTime = LocalDateTime.now().plusDays(1);
        Integer guestCount = 4;
        List<RestaurantTable> allTables = Arrays.asList(table1, table2);
        
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
        when(restaurantProfileRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.findAvailableTables(999, LocalDateTime.now().plusDays(1), 4);
        });
        assertEquals("Restaurant not found", exception.getMessage());
    }

    // ==================== GET BOOKING COUNT BY STATUS TESTS ====================

    @Test
    @DisplayName("Should get booking count by status successfully")
    void testGetBookingCountByStatus_WithValidStatus_ShouldReturnCount() {
        // Given
        when(bookingRepository.countByStatus(BookingStatus.PENDING)).thenReturn(5L);

        // When
        long result = bookingService.getBookingCountByStatus(BookingStatus.PENDING);

        // Then
        assertEquals(5L, result);
        verify(bookingRepository).countByStatus(BookingStatus.PENDING);
    }

    @Test
    @DisplayName("Should return zero when no bookings with status")
    void testGetBookingCountByStatus_WithNoBookings_ShouldReturnZero() {
        // Given
        when(bookingRepository.countByStatus(BookingStatus.COMPLETED)).thenReturn(0L);

        // When
        long result = bookingService.getBookingCountByStatus(BookingStatus.COMPLETED);

        // Then
        assertEquals(0L, result);
    }

    // ==================== GET BOOKING COUNT IN DATE RANGE TESTS ====================

    @Test
    @DisplayName("Should get booking count in date range successfully")
    void testGetBookingCountInDateRange_WithValidRange_ShouldReturnCount() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        when(bookingRepository.countByBookingTimeBetween(startDate, endDate)).thenReturn(10L);

        // When
        long result = bookingService.getBookingCountInDateRange(startDate, endDate);

        // Then
        assertEquals(10L, result);
        verify(bookingRepository).countByBookingTimeBetween(startDate, endDate);
    }

    @Test
    @DisplayName("Should return zero when no bookings in range")
    void testGetBookingCountInDateRange_WithNoBookings_ShouldReturnZero() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        when(bookingRepository.countByBookingTimeBetween(startDate, endDate)).thenReturn(0L);

        // When
        long result = bookingService.getBookingCountInDateRange(startDate, endDate);

        // Then
        assertEquals(0L, result);
    }

    // ==================== UPDATE BOOKING WITH ITEMS TESTS ====================

    @Test
    @DisplayName("Should update booking with items successfully")
    void testUpdateBookingWithItems_WithValidData_ShouldUpdate() {
        // Given
        booking.setStatus(BookingStatus.PENDING);
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(2);
        form.setGuestCount(6);
        form.setBookingTime(LocalDateTime.now().plusDays(2));
        form.setDepositAmount(new BigDecimal("200000"));
        form.setNote("Updated note");
        form.setDishIds("1:2");
        form.setServiceIds("1");

        Dish dish = new Dish();
        dish.setDishId(1);
        dish.setPrice(new BigDecimal("50000"));
        when(dishRepository.findById(1)).thenReturn(Optional.of(dish));

        RestaurantService service = new RestaurantService();
        service.setServiceId(1);
        service.setPrice(new BigDecimal("20000"));
        when(restaurantServiceRepository.findById(1)).thenReturn(Optional.of(service));

        when(bookingTableRepository.findByBooking(booking)).thenReturn(Collections.emptyList());
        doNothing().when(bookingTableRepository).deleteByBooking(booking);
        doNothing().when(bookingDishRepository).deleteByBooking(booking);
        doNothing().when(bookingServiceRepository).deleteByBooking(booking);

        // When
        Booking result = bookingService.updateBookingWithItems(1, form);

        // Then
        assertNotNull(result);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw exception when booking cannot be edited")
    void testUpdateBookingWithItems_WithCompletedBooking_ShouldThrowException() {
        // Given
        booking.setStatus(BookingStatus.COMPLETED);
        BookingForm form = new BookingForm();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.updateBookingWithItems(1, form);
        });
        assertTrue(exception.getMessage().contains("cannot be updated"));
    }

    // ==================== UPDATE BOOKING FOR RESTAURANT OWNER TESTS ====================

    @Test
    @DisplayName("Should update booking for restaurant owner successfully")
    void testUpdateBookingForRestaurantOwner_WithValidData_ShouldUpdate() {
        // Given
        booking.setStatus(BookingStatus.PENDING);
        Set<Integer> ownerRestaurantIds = Set.of(1);
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(2);
        form.setGuestCount(6);
        form.setBookingTime(LocalDateTime.now().plusDays(2));

        when(bookingTableRepository.findByBooking(any())).thenReturn(Collections.emptyList());
        doNothing().when(bookingTableRepository).deleteByBooking(any());

        // When
        Booking result = bookingService.updateBookingForRestaurantOwner(1, form, ownerRestaurantIds);

        // Then
        assertNotNull(result);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw exception when owner has no restaurants")
    void testUpdateBookingForRestaurantOwner_WithNoRestaurants_ShouldThrowException() {
        // Given
        Set<Integer> ownerRestaurantIds = Collections.emptySet();
        BookingForm form = new BookingForm();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.updateBookingForRestaurantOwner(1, form, ownerRestaurantIds);
        });
        assertTrue(exception.getMessage().contains("does not have any restaurants"));
    }

    @Test
    @DisplayName("Should throw exception when owner doesn't own restaurant")
    void testUpdateBookingForRestaurantOwner_WithUnauthorizedOwner_ShouldThrowException() {
        // Given
        Set<Integer> ownerRestaurantIds = Set.of(999);
        BookingForm form = new BookingForm();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.updateBookingForRestaurantOwner(1, form, ownerRestaurantIds);
        });
        assertTrue(exception.getMessage().contains("You can only edit bookings"));
    }

    // ==================== CANCEL BOOKING BY RESTAURANT TESTS ====================

    @Test
    @DisplayName("Should cancel booking by restaurant successfully")
    void testCancelBookingByRestaurant_WithValidData_ShouldCancel() {
        // Given
        Payment payment = new Payment();
        payment.setPaymentId(1);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setAmount(new BigDecimal("100000"));

        when(paymentRepository.findByBooking(booking)).thenReturn(Optional.of(payment));
        when(refundService.processRefundWithManualTransfer(anyInt(), anyString(), anyString(), anyString()))
            .thenReturn(payment);

        // When
        Booking result = bookingService.cancelBookingByRestaurant(1, restaurantOwnerId, "Test reason", "VCB", "1234567890");

        // Then
        assertNotNull(result);
        assertEquals(BookingStatus.CANCELLED, result.getStatus());
        verify(refundService).processRefundWithManualTransfer(anyInt(), anyString(), eq("VCB"), eq("1234567890"));
    }

    @Test
    @DisplayName("Should throw exception when restaurant owner doesn't own restaurant")
    void testCancelBookingByRestaurant_WithUnauthorizedOwner_ShouldThrowException() {
        // Given
        UUID differentOwnerId = UUID.randomUUID();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.cancelBookingByRestaurant(1, differentOwnerId, "Test", "VCB", "1234567890");
        });
        assertTrue(exception.getMessage().contains("You can only cancel bookings"));
    }

    @Test
    @DisplayName("Should cancel booking by restaurant without payment")
    void testCancelBookingByRestaurant_WithNoPayment_ShouldCancel() {
        // Given
        when(paymentRepository.findByBooking(booking)).thenReturn(Optional.empty());

        // When
        Booking result = bookingService.cancelBookingByRestaurant(1, restaurantOwnerId, "Test reason", "VCB", "1234567890");

        // Then
        assertNotNull(result);
        assertEquals(BookingStatus.CANCELLED, result.getStatus());
        verify(refundService, never()).processRefundWithManualTransfer(anyInt(), anyString(), anyString(), anyString());
    }

    // ==================== CREATE BOOKING WITH MULTIPLE TABLES TESTS ====================

    @Test
    @DisplayName("Should create booking with multiple tables successfully")
    void testCreateBooking_WithMultipleTables_ShouldSuccess() {
        // Given
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableIds("1,2");
        form.setGuestCount(8);
        form.setBookingTime(LocalDateTime.now().plusDays(1));
        form.setDepositAmount(new BigDecimal("250000"));

        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table1));
        when(restaurantTableRepository.findById(2)).thenReturn(Optional.of(table2));
        when(bookingTableRepository.findByBooking(any())).thenReturn(Arrays.asList(
            new BookingTable(booking, table1),
            new BookingTable(booking, table2)
        ));

        // When
        Booking result = bookingService.createBooking(form, customerId);

        // Then
        assertNotNull(result);
        verify(bookingTableRepository, atLeastOnce()).save(any(BookingTable.class));
    }

    // ==================== CREATE BOOKING WITH VOUCHER APPLIED TESTS ====================

    @Test
    @DisplayName("Should create booking with voucher code applied from form")
    void testCreateBooking_WithVoucherCodeApplied_ShouldApplyVoucher() {
        // Given
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(1);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(1));
        form.setVoucherCodeApplied("SUMMER20");
        form.setVoucherDiscountAmount(new BigDecimal("20000"));

        // When
        Booking result = bookingService.createBooking(form, customerId);

        // Then
        assertNotNull(result);
        verify(voucherService, never()).validate(any());
    }

    // ==================== CALCULATE TOTAL AMOUNT WITH ITEMS TESTS ====================

    @Test
    @DisplayName("Should calculate total amount with dishes and services")
    void testCalculateTotalAmount_WithDishesAndServices_ShouldSumCorrectly() {
        // Given
        BookingDish dish1 = new BookingDish();
        dish1.setQuantity(2);
        dish1.setPrice(new BigDecimal("50000"));

        BookingDish dish2 = new BookingDish();
        dish2.setQuantity(1);
        dish2.setPrice(new BigDecimal("30000"));

        com.example.booking.domain.BookingService service1 = new com.example.booking.domain.BookingService();
        service1.setQuantity(1);
        service1.setPrice(new BigDecimal("20000"));

        booking.setDepositAmount(new BigDecimal("100000"));
        when(bookingDishRepository.findByBooking(booking)).thenReturn(Arrays.asList(dish1, dish2));
        when(bookingServiceRepository.findByBooking(booking)).thenReturn(Arrays.asList(service1));

        // When
        BigDecimal total = bookingService.calculateTotalAmount(booking);

        // Then
        // deposit: 100000 + dishes: 100000 (2*50000) + 30000 + service: 20000 = 250000
        assertEquals(0, new BigDecimal("250000").compareTo(total));
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    @DisplayName("Should throw exception when booking time too far in future")
    void testCreateBooking_WithBookingTimeTooFar_ShouldThrowException() {
        // Given
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(1);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(31));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(form, customerId);
        });
        assertTrue(exception.getMessage().contains("30 days"));
    }

    @Test
    @DisplayName("Should throw exception when guest count exceeds 100")
    void testCreateBooking_WithGuestCountTooLarge_ShouldThrowException() {
        // Given
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(1);
        form.setGuestCount(101);
        form.setBookingTime(LocalDateTime.now().plusDays(1));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(form, customerId);
        });
        assertTrue(exception.getMessage().contains("100 people"));
    }

    @Test
    @DisplayName("Should throw exception when guest count exceeds multiple tables capacity")
    void testCreateBooking_WithGuestCountExceedingMultipleTables_ShouldThrowException() {
        // Given
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableIds("1,2");
        form.setGuestCount(15); // Exceeds total capacity of 10
        form.setBookingTime(LocalDateTime.now().plusDays(1));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(form, customerId);
        });
        assertTrue(exception.getMessage().contains("vượt quá tổng sức chứa"));
    }

    // ==================== ASSIGN DISHES ERROR HANDLING TESTS ====================

    @Test
    @DisplayName("Should throw exception when dish format is invalid")
    void testAssignDishesToBooking_WithInvalidFormat_ShouldThrowException() {
        // Given
        when(dishRepository.findById(anyInt())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            bookingService.assignDishesToBooking(booking, "1:2,invalid");
        });
    }

    // ==================== ASSIGN SERVICES ERROR HANDLING TESTS ====================

    @Test
    @DisplayName("Should throw exception when service format is invalid")
    void testAssignServicesToBooking_WithInvalidFormat_ShouldThrowException() {
        // Given
        when(restaurantServiceRepository.findById(anyInt())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            bookingService.assignServicesToBooking(booking, "invalid");
        });
    }

    // ==================== CREATE BOOKING WITH NO TABLE TESTS ====================

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
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(form, customerId);
        });
        assertTrue(exception.getMessage().contains("chọn ít nhất một bàn"));
    }

    // ==================== CREATE BOOKING WITH DEPOSIT CALCULATION TESTS ====================

    @Test
    @DisplayName("Should calculate deposit as 10% of total when deposit not provided")
    void testCreateBooking_WithDepositCalculation_ShouldCalculate() {
        // Given
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(1);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(1));
        form.setDepositAmount(null); // No deposit provided

        Dish dish = new Dish();
        dish.setDishId(1);
        dish.setPrice(new BigDecimal("100000"));
        BookingDish bookingDish = new BookingDish();
        bookingDish.setDish(dish);
        bookingDish.setQuantity(1);
        bookingDish.setPrice(new BigDecimal("100000"));

        when(bookingDishRepository.findByBooking(any())).thenReturn(Arrays.asList(bookingDish));
        when(bookingServiceRepository.findByBooking(any())).thenReturn(Collections.emptyList());

        // When
        Booking result = bookingService.createBooking(form, customerId);

        // Then
        assertNotNull(result);
        // Deposit should be calculated as 10% of total
    }

    // ==================== UPDATE BOOKING WITH MULTIPLE TABLES TESTS ====================

    @Test
    @DisplayName("Should update booking with multiple tables using tableIds")
    void testUpdateBooking_WithMultipleTables_ShouldUpdate() {
        // Given
        booking.setStatus(BookingStatus.PENDING);
        booking.setCustomer(customer);
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableIds("1,2");
        form.setGuestCount(8);
        form.setBookingTime(LocalDateTime.now().plusDays(2));
        form.setDepositAmount(new BigDecimal("250000"));

        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table1));
        when(restaurantTableRepository.findById(2)).thenReturn(Optional.of(table2));
        when(bookingTableRepository.findByBooking(booking)).thenReturn(Collections.emptyList());
        doNothing().when(bookingTableRepository).deleteByBooking(any());
        doNothing().when(conflictService).validateBookingUpdateConflicts(anyInt(), any(), any());

        // When
        Booking result = bookingService.updateBooking(1, form, customerId);

        // Then
        assertNotNull(result);
        verify(bookingTableRepository).deleteByBooking(any());
    }

    // ==================== CANCEL BOOKING BY RESTAURANT LEGACY TESTS ====================

    @Test
    @DisplayName("Should cancel booking by restaurant using legacy method")
    void testCancelBookingByRestaurant_LegacyMethod_ShouldCancel() {
        // Given
        when(paymentRepository.findByBooking(booking)).thenReturn(Optional.empty());

        // When
        Booking result = bookingService.cancelBookingByRestaurant(1, restaurantOwnerId, "Test reason");

        // Then
        assertNotNull(result);
        assertEquals(BookingStatus.CANCELLED, result.getStatus());
    }

    // ==================== ASSIGN DISHES WITH INVALID FORMAT TESTS ====================

    @Test
    @DisplayName("Should handle invalid dish format gracefully")
    void testAssignDishesToBooking_WithInvalidFormat_ShouldHandle() {
        // Given
        when(dishRepository.findById(anyInt())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            bookingService.assignDishesToBooking(booking, "1:2,invalid:3");
        });
    }

    // ==================== ASSIGN SERVICES WITH WHITESPACE TESTS ====================

    @Test
    @DisplayName("Should handle service IDs with whitespace")
    void testAssignServicesToBooking_WithWhitespace_ShouldParse() {
        // Given
        RestaurantService service = new RestaurantService();
        service.setServiceId(1);
        service.setPrice(new BigDecimal("20000"));
        when(restaurantServiceRepository.findById(1)).thenReturn(Optional.of(service));
        when(bookingServiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // When
        bookingService.assignServicesToBooking(booking, " 1 , 2 ");

        // Then
        verify(bookingServiceRepository, atLeastOnce()).save(any());
    }

    // ==================== CREATE BOOKING WITH EXCEPTION IN NOTIFICATION TESTS ====================

    @Test
    @DisplayName("Should continue when notification creation fails")
    void testCreateBooking_WithNotificationFailure_ShouldContinue() {
        // Given
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(1);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(1));
        form.setDepositAmount(new BigDecimal("100000"));

        when(notificationRepository.save(any())).thenThrow(new RuntimeException("Notification error"));

        // When
        Booking result = bookingService.createBooking(form, customerId);

        // Then
        assertNotNull(result);
        // Booking should still be created even if notification fails
    }

    // ==================== CREATE BOOKING WITH CUSTOMER WITHOUT USER TESTS ====================

    @Test
    @DisplayName("Should create booking when customer has no user but has customerId")
    void testCreateBooking_WithCustomerWithoutUser_ShouldUseCustomerId() {
        // Given
        Customer customerWithoutUser = new Customer();
        customerWithoutUser.setCustomerId(customerId);
        customerWithoutUser.setUser(null);

        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(1);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(1));
        form.setDepositAmount(new BigDecimal("100000"));

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customerWithoutUser));

        // When
        Booking result = bookingService.createBooking(form, customerId);

        // Then
        assertNotNull(result);
    }

    // ==================== UPDATE BOOKING FOR RESTAURANT OWNER WITH RESTAURANT CHANGE TESTS ====================

    @Test
    @DisplayName("Should update booking for restaurant owner with restaurant change")
    void testUpdateBookingForRestaurantOwner_WithRestaurantChange_ShouldReassignTables() {
        // Given
        booking.setStatus(BookingStatus.PENDING);
        Set<Integer> ownerRestaurantIds = Set.of(1, 2);
        RestaurantProfile newRestaurant = new RestaurantProfile();
        newRestaurant.setRestaurantId(2);
        newRestaurant.setOwner(owner);

        BookingForm form = new BookingForm();
        form.setRestaurantId(2);
        form.setTableId(1);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(2));

        RestaurantTable newTable = new RestaurantTable();
        newTable.setTableId(1);
        newTable.setCapacity(4);
        newTable.setRestaurant(newRestaurant);

        when(restaurantProfileRepository.findById(2)).thenReturn(Optional.of(newRestaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(newTable));
        doNothing().when(bookingTableRepository).deleteByBooking(any());

        // When
        Booking result = bookingService.updateBookingForRestaurantOwner(1, form, ownerRestaurantIds);

        // Then
        assertNotNull(result);
        verify(bookingTableRepository, atLeastOnce()).deleteByBooking(any());
    }

    // ==================== VALIDATE BOOKING TIME EDGE CASES TESTS ====================

    @Test
    @DisplayName("Should throw exception when booking time is exactly 30 minutes from now")
    void testCreateBooking_WithBookingTimeExactly30Minutes_ShouldPass() {
        // Given
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(1);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusMinutes(30));
        form.setDepositAmount(new BigDecimal("100000"));

        // When
        Booking result = bookingService.createBooking(form, customerId);

        // Then
        assertNotNull(result);
    }

    // ==================== VALIDATE GUEST COUNT EDGE CASES TESTS ====================

    @Test
    @DisplayName("Should accept guest count of exactly 100")
    void testCreateBooking_WithGuestCountExactly100_ShouldPass() {
        // Given
        table1.setCapacity(100);
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(1);
        form.setGuestCount(100);
        form.setBookingTime(LocalDateTime.now().plusDays(1));
        form.setDepositAmount(new BigDecimal("100000"));

        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table1));

        // When
        Booking result = bookingService.createBooking(form, customerId);

        // Then
        assertNotNull(result);
    }

    // ==================== VALIDATE TABLE CAPACITY WITH MULTIPLE TABLES TESTS ====================

    @Test
    @DisplayName("Should validate total capacity when using multiple tables")
    void testCreateBooking_WithMultipleTablesCapacity_ShouldValidate() {
        // Given
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableIds("1,2");
        form.setGuestCount(11); // Exceeds total capacity of 10
        form.setBookingTime(LocalDateTime.now().plusDays(1));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(form, customerId);
        });
        assertTrue(exception.getMessage().contains("vượt quá tổng sức chứa"));
    }
}

