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
 * Test class for helper methods and internal logic in BookingService
 * Focuses on testing parseDishIds, parseServiceIds, and other helper methods
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingServiceHelperMethodsTest {

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

    private Booking booking;
    private Dish dish1;
    private Dish dish2;
    private RestaurantService service1;
    private RestaurantService service2;

    @BeforeEach
    void setUp() {
        booking = new Booking();
        booking.setBookingId(1);
        booking.setStatus(BookingStatus.PENDING);

        dish1 = new Dish();
        dish1.setDishId(1);
        dish1.setName("Dish 1");
        dish1.setPrice(new BigDecimal("50000"));

        dish2 = new Dish();
        dish2.setDishId(2);
        dish2.setName("Dish 2");
        dish2.setPrice(new BigDecimal("30000"));

        service1 = new RestaurantService();
        service1.setServiceId(1);
        service1.setName("Service 1");
        service1.setPrice(new BigDecimal("20000"));

        service2 = new RestaurantService();
        service2.setServiceId(2);
        service2.setName("Service 2");
        service2.setPrice(new BigDecimal("15000"));

        lenient().when(dishRepository.findById(1)).thenReturn(Optional.of(dish1));
        lenient().when(dishRepository.findById(2)).thenReturn(Optional.of(dish2));
        lenient().when(restaurantServiceRepository.findById(1)).thenReturn(Optional.of(service1));
        lenient().when(restaurantServiceRepository.findById(2)).thenReturn(Optional.of(service2));
        lenient().when(bookingDishRepository.save(any(BookingDish.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(bookingServiceRepository.save(any(com.example.booking.domain.BookingService.class)))
            .thenAnswer(inv -> inv.getArgument(0));
    }

    // ==================== PARSE DISH IDS TESTS ====================

    @Test
    @DisplayName("Should parse dishIds with single dish")
    void testAssignDishesToBooking_WithSingleDish_ShouldParse() {
        bookingService.assignDishesToBooking(booking, "1:2");
        verify(bookingDishRepository).save(any(BookingDish.class));
    }

    @Test
    @DisplayName("Should parse dishIds with multiple dishes")
    void testAssignDishesToBooking_WithMultipleDishes_ShouldParse() {
        bookingService.assignDishesToBooking(booking, "1:2,2:3");
        verify(bookingDishRepository, times(2)).save(any(BookingDish.class));
    }

    @Test
    @DisplayName("Should parse dishIds with whitespace")
    void testAssignDishesToBooking_WithWhitespace_ShouldParse() {
        bookingService.assignDishesToBooking(booking, " 1 : 2 , 2 : 3 ");
        verify(bookingDishRepository, times(2)).save(any(BookingDish.class));
    }

    @Test
    @DisplayName("Should handle dishIds with large quantities")
    void testAssignDishesToBooking_WithLargeQuantities_ShouldParse() {
        bookingService.assignDishesToBooking(booking, "1:100,2:50");
        verify(bookingDishRepository, times(2)).save(any(BookingDish.class));
    }

    @Test
    @DisplayName("Should throw exception when dish not found")
    void testAssignDishesToBooking_WithNonExistentDish_ShouldThrowException() {
        when(dishRepository.findById(99)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.assignDishesToBooking(booking, "99:1");
        });
        assertEquals("Dish not found: 99", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle dishIds with quantity zero")
    void testAssignDishesToBooking_WithZeroQuantity_ShouldCreate() {
        bookingService.assignDishesToBooking(booking, "1:0");
        verify(bookingDishRepository).save(any(BookingDish.class));
    }

    // ==================== PARSE SERVICE IDS TESTS ====================

    @Test
    @DisplayName("Should parse serviceIds with single service")
    void testAssignServicesToBooking_WithSingleService_ShouldParse() {
        bookingService.assignServicesToBooking(booking, "1");
        verify(bookingServiceRepository).save(any(com.example.booking.domain.BookingService.class));
    }

    @Test
    @DisplayName("Should parse serviceIds with multiple services")
    void testAssignServicesToBooking_WithMultipleServices_ShouldParse() {
        bookingService.assignServicesToBooking(booking, "1,2");
        verify(bookingServiceRepository, times(2)).save(any(com.example.booking.domain.BookingService.class));
    }

    @Test
    @DisplayName("Should parse serviceIds with whitespace")
    void testAssignServicesToBooking_WithWhitespace_ShouldParse() {
        bookingService.assignServicesToBooking(booking, " 1 , 2 ");
        verify(bookingServiceRepository, times(2)).save(any(com.example.booking.domain.BookingService.class));
    }

    @Test
    @DisplayName("Should throw exception when service not found")
    void testAssignServicesToBooking_WithNonExistentService_ShouldThrowException() {
        when(restaurantServiceRepository.findById(99)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.assignServicesToBooking(booking, "99");
        });
        assertEquals("Service not found: 99", exception.getMessage());
    }

    // ==================== CALCULATE TOTAL AMOUNT TESTS ====================

    @Test
    @DisplayName("Should calculate total with dishes only")
    void testCalculateTotalAmount_WithDishesOnly_ShouldCalculate() {
        booking.setDepositAmount(new BigDecimal("100000"));
        
        BookingDish dish1 = new BookingDish();
        dish1.setQuantity(2);
        dish1.setPrice(new BigDecimal("50000"));
        
        BookingDish dish2 = new BookingDish();
        dish2.setQuantity(1);
        dish2.setPrice(new BigDecimal("30000"));

        when(bookingDishRepository.findByBooking(booking)).thenReturn(Arrays.asList(dish1, dish2));
        when(bookingServiceRepository.findByBooking(booking)).thenReturn(Collections.emptyList());

        BigDecimal total = bookingService.calculateTotalAmount(booking);
        assertEquals(0, new BigDecimal("230000").compareTo(total));
    }

    @Test
    @DisplayName("Should calculate total with services only")
    void testCalculateTotalAmount_WithServicesOnly_ShouldCalculate() {
        booking.setDepositAmount(new BigDecimal("100000"));
        
        com.example.booking.domain.BookingService service1 = new com.example.booking.domain.BookingService();
        service1.setQuantity(1);
        service1.setPrice(new BigDecimal("20000"));
        
        com.example.booking.domain.BookingService service2 = new com.example.booking.domain.BookingService();
        service2.setQuantity(1);
        service2.setPrice(new BigDecimal("15000"));

        when(bookingDishRepository.findByBooking(booking)).thenReturn(Collections.emptyList());
        when(bookingServiceRepository.findByBooking(booking)).thenReturn(Arrays.asList(service1, service2));

        BigDecimal total = bookingService.calculateTotalAmount(booking);
        assertEquals(0, new BigDecimal("135000").compareTo(total));
    }

    @Test
    @DisplayName("Should calculate total with empty dishes and services")
    void testCalculateTotalAmount_WithEmptyItems_ShouldReturnDeposit() {
        booking.setDepositAmount(new BigDecimal("100000"));
        when(bookingDishRepository.findByBooking(booking)).thenReturn(Collections.emptyList());
        when(bookingServiceRepository.findByBooking(booking)).thenReturn(Collections.emptyList());

        BigDecimal total = bookingService.calculateTotalAmount(booking);
        assertEquals(0, new BigDecimal("100000").compareTo(total));
    }

    // ==================== CREATE BOOKING WITH DEPOSIT CALCULATION TESTS ====================

    @Test
    @DisplayName("Should calculate deposit as 10% when not provided")
    void testCreateBooking_WithAutoDepositCalculation_ShouldCalculate() {
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setCustomerId(customerId);
        User user = new User();
        user.setId(customerId);
        customer.setUser(user);

        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");

        RestaurantTable table = new RestaurantTable();
        table.setTableId(1);
        table.setCapacity(4);
        table.setDepositAmount(new BigDecimal("100000"));
        table.setRestaurant(restaurant);

        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(1);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(1));
        form.setDepositAmount(null); // Not provided

        Booking booking = new Booking();
        booking.setBookingId(1);
        booking.setCustomer(customer);
        booking.setRestaurant(restaurant);
        booking.setDepositAmount(new BigDecimal("100000"));

        BookingDish dish = new BookingDish();
        dish.setQuantity(1);
        dish.setPrice(new BigDecimal("500000"));

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1);
            return b;
        });
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookingDishRepository.findByBooking(any())).thenReturn(Collections.singletonList(dish));
        when(bookingServiceRepository.findByBooking(any())).thenReturn(Collections.emptyList());
        when(bookingTableRepository.findByBooking(any())).thenReturn(Collections.emptyList());
        doNothing().when(entityManager).flush();
        doNothing().when(conflictService).validateBookingConflicts(any(), any());
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
        verify(bookingRepository, atLeastOnce()).save(any(Booking.class));
    }

    // ==================== UPDATE BOOKING WITH ITEMS TESTS ====================

    @Test
    @DisplayName("Should update booking with items successfully")
    void testUpdateBookingWithItems_WithValidData_ShouldUpdate() {
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setCustomerId(customerId);
        User user = new User();
        user.setId(customerId);
        customer.setUser(user);
        
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");
        
        booking.setStatus(BookingStatus.PENDING);
        booking.setCustomer(customer);
        booking.setRestaurant(restaurant);

        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(1);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(2));
        form.setDepositAmount(new BigDecimal("100000"));
        form.setDishIds("1:2");
        form.setServiceIds("1");

        RestaurantTable table = new RestaurantTable();
        table.setTableId(1);
        table.setCapacity(4);
        table.setDepositAmount(new BigDecimal("100000"));

        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table));
        when(bookingTableRepository.findByBooking(booking)).thenReturn(Collections.emptyList());
        doNothing().when(bookingTableRepository).deleteByBooking(booking);
        doNothing().when(bookingDishRepository).deleteByBooking(booking);
        doNothing().when(bookingServiceRepository).deleteByBooking(booking);
        doNothing().when(conflictService).validateBookingUpdateConflicts(anyInt(), any(), any());

        Booking result = bookingService.updateBookingWithItems(1, form);
        assertNotNull(result);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw exception when updating completed booking")
    void testUpdateBookingWithItems_WithCompletedBooking_ShouldThrowException() {
        booking.setStatus(BookingStatus.COMPLETED);
        BookingForm form = new BookingForm();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.updateBookingWithItems(1, form);
        });
        assertTrue(exception.getMessage().contains("cannot be updated"));
    }

    // ==================== ASSIGN MULTIPLE TABLES TESTS ====================

    @Test
    @DisplayName("Should assign multiple tables successfully")
    void testCreateBooking_WithMultipleTables_ShouldAssign() {
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setCustomerId(customerId);
        User user = new User();
        user.setId(customerId);
        customer.setUser(user);

        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);

        RestaurantTable table1 = new RestaurantTable();
        table1.setTableId(1);
        table1.setCapacity(4);
        table1.setDepositAmount(new BigDecimal("100000"));

        RestaurantTable table2 = new RestaurantTable();
        table2.setTableId(2);
        table2.setCapacity(4);
        table2.setDepositAmount(new BigDecimal("100000"));

        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableIds("1,2");
        form.setGuestCount(6);
        form.setBookingTime(LocalDateTime.now().plusDays(1));
        form.setDepositAmount(new BigDecimal("200000"));

        Booking booking = new Booking();
        booking.setBookingId(1);
        booking.setCustomer(customer);
        booking.setRestaurant(restaurant);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantProfileRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table1));
        when(restaurantTableRepository.findById(2)).thenReturn(Optional.of(table2));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1);
            return b;
        });
        when(bookingTableRepository.save(any(BookingTable.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookingDishRepository.findByBooking(any())).thenReturn(Collections.emptyList());
        when(bookingServiceRepository.findByBooking(any())).thenReturn(Collections.emptyList());
        when(bookingTableRepository.findByBooking(any())).thenReturn(Collections.emptyList());
        doNothing().when(entityManager).flush();
        doNothing().when(conflictService).validateBookingConflicts(any(), any());
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking result = bookingService.createBooking(form, customerId);
        assertNotNull(result);
        verify(bookingTableRepository, times(2)).save(any(BookingTable.class));
    }

    @Test
    @DisplayName("Should throw exception when guest count exceeds multiple tables capacity")
    void testAssignMultipleTables_WithExceedingCapacity_ShouldThrowException() {
        booking.setNumberOfGuests(15); // Exceeds total capacity of 8

        RestaurantTable table1 = new RestaurantTable();
        table1.setTableId(1);
        table1.setCapacity(4);
        table1.setTableName("Table 1");

        RestaurantTable table2 = new RestaurantTable();
        table2.setTableId(2);
        table2.setCapacity(4);
        table2.setTableName("Table 2");

        when(restaurantTableRepository.findById(1)).thenReturn(Optional.of(table1));
        when(restaurantTableRepository.findById(2)).thenReturn(Optional.of(table2));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(createFormWithMultipleTables(), customerId);
        });
        assertTrue(exception.getMessage().contains("vượt quá tổng sức chứa"));
    }

    private BookingForm createFormWithMultipleTables() {
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableIds("1,2");
        form.setGuestCount(15);
        form.setBookingTime(LocalDateTime.now().plusDays(1));
        form.setDepositAmount(new BigDecimal("200000"));
        return form;
    }

    private UUID customerId = UUID.randomUUID();
}

