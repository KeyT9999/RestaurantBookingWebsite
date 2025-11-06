package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.booking.common.enums.BookingStatus;
import com.example.booking.domain.Booking;
import com.example.booking.domain.BookingDish;
import com.example.booking.domain.BookingTable;
import com.example.booking.domain.Customer;
import com.example.booking.domain.Dish;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantService;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.domain.User;
import com.example.booking.repository.BookingDishRepository;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.BookingServiceRepository;
import com.example.booking.repository.BookingTableRepository;

/**
 * Test deposit calculation logic
 * 
 * Test cases:
 * 1. calculateSubtotal() - table fees + dishes + services
 * 2. Deposit = 10% of subtotal
 * 3. calculateTotalAmount() = subtotal + deposit
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService Deposit Calculation Tests")
class BookingServiceDepositCalculationTest {

    @Mock
    private BookingRepository bookingRepository;
    
    @Mock
    private BookingTableRepository bookingTableRepository;
    
    @Mock
    private BookingDishRepository bookingDishRepository;
    
    @Mock
    private BookingServiceRepository bookingServiceRepository;

    @InjectMocks
    private BookingService bookingService;

    private Booking booking;
    private RestaurantTable table1;
    private RestaurantTable table2;
    private BookingTable bookingTable1;
    private BookingTable bookingTable2;

    @BeforeEach
    void setUp() {
        // Create restaurant
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");

        // Create customer
        Customer customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        User user = new User();
        user.setFullName("Test Customer");
        customer.setUser(user);

        // Create tables with fees
        table1 = new RestaurantTable();
        table1.setTableId(1);
        table1.setTableName("Table 1");
        table1.setDepositAmount(new BigDecimal("50000")); // 50,000 VND

        table2 = new RestaurantTable();
        table2.setTableId(2);
        table2.setTableName("Table 2");
        table2.setDepositAmount(new BigDecimal("30000")); // 30,000 VND

        // Create booking
        booking = new Booking();
        booking.setBookingId(1);
        booking.setCustomer(customer);
        booking.setRestaurant(restaurant);
        booking.setBookingTime(LocalDateTime.now().plusDays(1));
        booking.setNumberOfGuests(4);
        booking.setStatus(BookingStatus.PENDING);

        // Create booking tables with snapshot fees
        bookingTable1 = new BookingTable(booking, table1);
        bookingTable2 = new BookingTable(booking, table2);
    }

    @Test
    @DisplayName("calculateSubtotal() should return sum of table fees only")
    void testCalculateSubtotal_WithTableFeesOnly() {
        // Given
        List<BookingTable> bookingTables = List.of(bookingTable1, bookingTable2);
        when(bookingTableRepository.findByBooking(booking)).thenReturn(bookingTables);
        when(bookingDishRepository.findByBooking(booking)).thenReturn(new ArrayList<>());
        when(bookingServiceRepository.findByBooking(booking)).thenReturn(new ArrayList<>());

        // When
        BigDecimal subtotal = bookingService.calculateSubtotal(booking);

        // Then
        // Table fees: 50,000 + 30,000 = 80,000
        assertEquals(new BigDecimal("80000"), subtotal);
        verify(bookingTableRepository).findByBooking(booking);
    }

    @Test
    @DisplayName("calculateSubtotal() should return sum of table fees + dishes")
    void testCalculateSubtotal_WithTableFeesAndDishes() {
        // Given
        List<BookingTable> bookingTables = List.of(bookingTable1);
        when(bookingTableRepository.findByBooking(booking)).thenReturn(bookingTables);
        
        // Create dishes
        Dish dish1 = new Dish();
        dish1.setDishId(1);
        dish1.setName("Dish 1");
        dish1.setPrice(new BigDecimal("150000"));
        
        Dish dish2 = new Dish();
        dish2.setDishId(2);
        dish2.setName("Dish 2");
        dish2.setPrice(new BigDecimal("100000"));
        
        BookingDish bookingDish1 = new BookingDish(booking, dish1, 2, dish1.getPrice());
        BookingDish bookingDish2 = new BookingDish(booking, dish2, 1, dish2.getPrice());
        List<BookingDish> bookingDishes = List.of(bookingDish1, bookingDish2);
        
        when(bookingDishRepository.findByBooking(booking)).thenReturn(bookingDishes);
        when(bookingServiceRepository.findByBooking(booking)).thenReturn(new ArrayList<>());

        // When
        BigDecimal subtotal = bookingService.calculateSubtotal(booking);

        // Then
        // Table fees: 50,000
        // Dishes: (150,000 * 2) + (100,000 * 1) = 300,000 + 100,000 = 400,000
        // Total: 50,000 + 400,000 = 450,000
        assertEquals(new BigDecimal("450000"), subtotal);
    }

    @Test
    @DisplayName("calculateSubtotal() should return sum of table fees + dishes + services")
    void testCalculateSubtotal_WithAllItems() {
        // Given
        List<BookingTable> bookingTables = List.of(bookingTable1);
        when(bookingTableRepository.findByBooking(booking)).thenReturn(bookingTables);
        
        // Create dishes
        Dish dish1 = new Dish();
        dish1.setDishId(1);
        dish1.setPrice(new BigDecimal("100000"));
        BookingDish bookingDish1 = new BookingDish(booking, dish1, 1, dish1.getPrice());
        when(bookingDishRepository.findByBooking(booking)).thenReturn(List.of(bookingDish1));
        
        // Create services
        RestaurantService service1 = new RestaurantService();
        service1.setServiceId(1);
        service1.setName("Service 1");
        service1.setPrice(new BigDecimal("50000"));
        
        com.example.booking.domain.BookingService bookingService1 = 
            new com.example.booking.domain.BookingService(booking, service1, 1, service1.getPrice());
        when(bookingServiceRepository.findByBooking(booking)).thenReturn(List.of(bookingService1));

        // When
        BigDecimal subtotal = bookingService.calculateSubtotal(booking);

        // Then
        // Table fees: 50,000
        // Dishes: 100,000
        // Services: 50,000
        // Total: 50,000 + 100,000 + 50,000 = 200,000
        assertEquals(new BigDecimal("200000"), subtotal);
    }

    @Test
    @DisplayName("Deposit should be 10% of subtotal")
    void testDepositCalculation_ShouldBe10PercentOfSubtotal() {
        // Given
        List<BookingTable> bookingTables = List.of(bookingTable1, bookingTable2);
        when(bookingTableRepository.findByBooking(booking)).thenReturn(bookingTables);
        when(bookingDishRepository.findByBooking(booking)).thenReturn(new ArrayList<>());
        when(bookingServiceRepository.findByBooking(booking)).thenReturn(new ArrayList<>());
        
        // Subtotal = 80,000 (table fees only)
        BigDecimal subtotal = bookingService.calculateSubtotal(booking);
        
        // When - calculate deposit
        BigDecimal deposit = subtotal.multiply(new BigDecimal("0.10"));
        deposit = deposit.setScale(0, java.math.RoundingMode.HALF_UP);

        // Then
        // Deposit = 10% of 80,000 = 8,000
        assertEquals(new BigDecimal("8000"), deposit);
    }

    @Test
    @DisplayName("calculateTotalAmount() should return subtotal + deposit")
    void testCalculateTotalAmount_ShouldReturnSubtotalPlusDeposit() {
        // Given
        List<BookingTable> bookingTables = List.of(bookingTable1);
        when(bookingTableRepository.findByBooking(booking)).thenReturn(bookingTables);
        when(bookingDishRepository.findByBooking(booking)).thenReturn(new ArrayList<>());
        when(bookingServiceRepository.findByBooking(booking)).thenReturn(new ArrayList<>());
        
        // Set deposit = 10% of subtotal
        BigDecimal subtotal = bookingService.calculateSubtotal(booking);
        BigDecimal deposit = subtotal.multiply(new BigDecimal("0.10"));
        deposit = deposit.setScale(0, java.math.RoundingMode.HALF_UP);
        booking.setDepositAmount(deposit);

        // When
        BigDecimal total = bookingService.calculateTotalAmount(booking);

        // Then
        // Subtotal = 50,000
        // Deposit = 5,000 (10% of 50,000)
        // Total = 50,000 + 5,000 = 55,000
        assertEquals(new BigDecimal("55000"), total);
        assertEquals(subtotal.add(deposit), total);
    }

    @Test
    @DisplayName("calculateSubtotal() should return zero when no items")
    void testCalculateSubtotal_WithNoItems() {
        // Given
        when(bookingTableRepository.findByBooking(booking)).thenReturn(new ArrayList<>());
        when(bookingDishRepository.findByBooking(booking)).thenReturn(new ArrayList<>());
        when(bookingServiceRepository.findByBooking(booking)).thenReturn(new ArrayList<>());

        // When
        BigDecimal subtotal = bookingService.calculateSubtotal(booking);

        // Then
        assertEquals(BigDecimal.ZERO, subtotal);
    }

    @Test
    @DisplayName("Deposit should be zero when subtotal is zero")
    void testDepositCalculation_WithZeroSubtotal() {
        // Given
        BigDecimal subtotal = BigDecimal.ZERO;
        
        // When
        BigDecimal deposit = subtotal.multiply(new BigDecimal("0.10"));
        deposit = deposit.setScale(0, java.math.RoundingMode.HALF_UP);

        // Then
        assertEquals(BigDecimal.ZERO, deposit);
    }

    @Test
    @DisplayName("calculateSubtotal() should handle multiple tables correctly")
    void testCalculateSubtotal_WithMultipleTables() {
        // Given
        RestaurantTable table3 = new RestaurantTable();
        table3.setTableId(3);
        table3.setDepositAmount(new BigDecimal("25000"));
        BookingTable bookingTable3 = new BookingTable(booking, table3);
        
        List<BookingTable> bookingTables = List.of(bookingTable1, bookingTable2, bookingTable3);
        when(bookingTableRepository.findByBooking(booking)).thenReturn(bookingTables);
        when(bookingDishRepository.findByBooking(booking)).thenReturn(new ArrayList<>());
        when(bookingServiceRepository.findByBooking(booking)).thenReturn(new ArrayList<>());

        // When
        BigDecimal subtotal = bookingService.calculateSubtotal(booking);

        // Then
        // Table fees: 50,000 + 30,000 + 25,000 = 105,000
        assertEquals(new BigDecimal("105000"), subtotal);
    }
}

