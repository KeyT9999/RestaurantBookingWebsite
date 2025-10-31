package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.booking.common.enums.BookingStatus;

/**
 * Unit tests for Booking domain entity
 */
@DisplayName("Booking Domain Entity Tests")
public class BookingTest {

    private Booking booking;

    @BeforeEach
    void setUp() {
        booking = new Booking();
        booking.setBookingId(1);
        booking.setBookingTime(LocalDateTime.now().plusHours(2));
        booking.setNumberOfGuests(4);
        booking.setDepositAmount(new BigDecimal("500000"));
        booking.setStatus(BookingStatus.PENDING);
    }

    // ========== Basic Getters/Setters Tests ==========

    @Test
    @DisplayName("shouldSetAndGetBookingId_successfully")
    void shouldSetAndGetBookingId_successfully() {
        // Given
        Integer bookingId = 123;

        // When
        booking.setBookingId(bookingId);

        // Then
        assertEquals(bookingId, booking.getBookingId());
    }

    @Test
    @DisplayName("shouldSetAndGetStatus_successfully")
    void shouldSetAndGetStatus_successfully() {
        // Given
        BookingStatus status = BookingStatus.CONFIRMED;

        // When
        booking.setStatus(status);

        // Then
        assertEquals(status, booking.getStatus());
    }

    @Test
    @DisplayName("shouldSetAndGetDepositAmount_successfully")
    void shouldSetAndGetDepositAmount_successfully() {
        // Given
        BigDecimal amount = new BigDecimal("1000000");

        // When
        booking.setDepositAmount(amount);

        // Then
        assertEquals(amount, booking.getDepositAmount());
    }

    @Test
    @DisplayName("shouldSetAndGetBookingTime_successfully")
    void shouldSetAndGetBookingTime_successfully() {
        // Given
        LocalDateTime bookingTime = LocalDateTime.now().plusDays(1);

        // When
        booking.setBookingTime(bookingTime);

        // Then
        assertEquals(bookingTime, booking.getBookingTime());
    }

    // ========== Validation Tests ==========

    @Test
    @DisplayName("shouldAcceptValidNumberOfGuests")
    void shouldAcceptValidNumberOfGuests() {
        // Given
        int guests = 4;

        // When
        booking.setNumberOfGuests(guests);

        // Then
        assertEquals(guests, booking.getNumberOfGuests());
    }

    // ========== Constructor Tests ==========

    @Test
    @DisplayName("shouldCreateBooking_withConstructor")
    void shouldCreateBooking_withConstructor() {
        // Given
        Customer customer = new Customer();
        RestaurantProfile restaurant = new RestaurantProfile();
        LocalDateTime bookingTime = LocalDateTime.now().plusHours(2);
        Integer numberOfGuests = 4;
        BigDecimal depositAmount = new BigDecimal("500000");
        String note = "Window seat preferred";

        // When
        Booking newBooking = new Booking(customer, restaurant, bookingTime, numberOfGuests, depositAmount, note);

        // Then
        assertEquals(customer, newBooking.getCustomer());
        assertEquals(restaurant, newBooking.getRestaurant());
        assertEquals(bookingTime, newBooking.getBookingTime());
        assertEquals(numberOfGuests, newBooking.getNumberOfGuests());
        assertEquals(depositAmount, newBooking.getDepositAmount());
        assertEquals(note, newBooking.getNote());
    }

    @Test
    @DisplayName("shouldCreateBooking_withNullDepositAmount")
    void shouldCreateBooking_withNullDepositAmount() {
        // Given
        Customer customer = new Customer();
        RestaurantProfile restaurant = new RestaurantProfile();
        LocalDateTime bookingTime = LocalDateTime.now().plusHours(2);
        Integer numberOfGuests = 4;

        // When
        Booking newBooking = new Booking(customer, restaurant, bookingTime, numberOfGuests, null, null);

        // Then
        assertEquals(BigDecimal.ZERO, newBooking.getDepositAmount());
    }

    // ========== Relationship Tests ==========

    @Test
    @DisplayName("shouldSetAndGetCustomer_successfully")
    void shouldSetAndGetCustomer_successfully() {
        // Given
        Customer customer = new Customer();
        customer.setCustomerId(java.util.UUID.randomUUID());

        // When
        booking.setCustomer(customer);

        // Then
        assertEquals(customer, booking.getCustomer());
    }

    @Test
    @DisplayName("shouldSetAndGetRestaurant_successfully")
    void shouldSetAndGetRestaurant_successfully() {
        // Given
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);

        // When
        booking.setRestaurant(restaurant);

        // Then
        assertEquals(restaurant, booking.getRestaurant());
    }

    @Test
    @DisplayName("shouldSetAndGetNote_successfully")
    void shouldSetAndGetNote_successfully() {
        // Given
        String note = "Special occasion";

        // When
        booking.setNote(note);

        // Then
        assertEquals(note, booking.getNote());
    }

    @Test
    @DisplayName("shouldSetAndGetCancelReason_successfully")
    void shouldSetAndGetCancelReason_successfully() {
        // Given
        String cancelReason = "Changed mind";

        // When
        booking.setCancelReason(cancelReason);

        // Then
        assertEquals(cancelReason, booking.getCancelReason());
    }

    @Test
    @DisplayName("shouldSetAndGetCancelledAt_successfully")
    void shouldSetAndGetCancelledAt_successfully() {
        // Given
        LocalDateTime cancelledAt = LocalDateTime.now();

        // When
        booking.setCancelledAt(cancelledAt);

        // Then
        assertEquals(cancelledAt, booking.getCancelledAt());
    }

    @Test
    @DisplayName("shouldSetAndGetCancelledBy_successfully")
    void shouldSetAndGetCancelledBy_successfully() {
        // Given
        java.util.UUID cancelledBy = java.util.UUID.randomUUID();

        // When
        booking.setCancelledBy(cancelledBy);

        // Then
        assertEquals(cancelledBy, booking.getCancelledBy());
    }

    // ========== Lifecycle Callback Tests ==========

    @Test
    @DisplayName("shouldSetCreatedAtAndUpdatedAt_onPrePersist")
    void shouldSetCreatedAtAndUpdatedAt_onPrePersist() throws Exception {
        // Given
        Booking newBooking = new Booking();

        // When - Simulate @PrePersist by calling onCreate directly
        // Note: We use reflection to call the protected method
        java.lang.reflect.Method onCreate = Booking.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(newBooking);

        // Then
        assertNotNull(newBooking.getCreatedAt());
        assertNotNull(newBooking.getUpdatedAt());
        assertEquals(newBooking.getCreatedAt(), newBooking.getUpdatedAt());
    }

    @Test
    @DisplayName("shouldSetUpdatedAt_onPreUpdate")
    void shouldSetUpdatedAt_onPreUpdate() throws Exception {
        // Given
        LocalDateTime initialCreatedAt = LocalDateTime.now().minusHours(1);
        booking.setCreatedAt(initialCreatedAt);
        booking.setUpdatedAt(initialCreatedAt);

        // When - Simulate @PreUpdate by calling onUpdate directly
        java.lang.reflect.Method onUpdate = Booking.class.getDeclaredMethod("onUpdate");
        onUpdate.setAccessible(true);
        onUpdate.invoke(booking);

        // Then
        assertNotNull(booking.getUpdatedAt());
        assertTrue(booking.getUpdatedAt().isAfter(initialCreatedAt));
        assertEquals(initialCreatedAt, booking.getCreatedAt()); // createdAt should not change
    }

    // ========== Collection Relationships Tests ==========

    @Test
    @DisplayName("shouldSetAndGetBookingTables_successfully")
    void shouldSetAndGetBookingTables_successfully() {
        // Given
        List<BookingTable> bookingTables = new java.util.ArrayList<>();
        BookingTable table = new BookingTable();
        bookingTables.add(table);

        // When
        booking.setBookingTables(bookingTables);

        // Then
        assertNotNull(booking.getBookingTables());
        assertEquals(1, booking.getBookingTables().size());
    }

    @Test
    @DisplayName("shouldSetAndGetBookingDishes_successfully")
    void shouldSetAndGetBookingDishes_successfully() {
        // Given
        List<BookingDish> bookingDishes = new java.util.ArrayList<>();
        BookingDish dish = new BookingDish();
        bookingDishes.add(dish);

        // When
        booking.setBookingDishes(bookingDishes);

        // Then
        assertNotNull(booking.getBookingDishes());
        assertEquals(1, booking.getBookingDishes().size());
    }

    @Test
    @DisplayName("shouldSetAndGetBookingServices_successfully")
    void shouldSetAndGetBookingServices_successfully() {
        // Given
        List<BookingService> bookingServices = new java.util.ArrayList<>();
        BookingService service = new BookingService();
        bookingServices.add(service);

        // When
        booking.setBookingServices(bookingServices);

        // Then
        assertNotNull(booking.getBookingServices());
        assertEquals(1, booking.getBookingServices().size());
    }

    @Test
    @DisplayName("shouldSetAndGetPayments_successfully")
    void shouldSetAndGetPayments_successfully() {
        // Given
        List<Payment> payments = new java.util.ArrayList<>();
        Payment payment = new Payment();
        payments.add(payment);

        // When
        booking.setPayments(payments);

        // Then
        assertNotNull(booking.getPayments());
        assertEquals(1, booking.getPayments().size());
    }

    // ========== Edge Cases Tests ==========

    @Test
    @DisplayName("shouldAcceptMinimumGuests")
    void shouldAcceptMinimumGuests() {
        // Given
        int guests = 1;

        // When
        booking.setNumberOfGuests(guests);

        // Then
        assertEquals(guests, booking.getNumberOfGuests());
    }

    @Test
    @DisplayName("shouldAcceptMaximumGuests")
    void shouldAcceptMaximumGuests() {
        // Given
        int guests = 100;

        // When
        booking.setNumberOfGuests(guests);

        // Then
        assertEquals(guests, booking.getNumberOfGuests());
    }

    @Test
    @DisplayName("shouldAcceptZeroDepositAmount")
    void shouldAcceptZeroDepositAmount() {
        // Given
        BigDecimal amount = BigDecimal.ZERO;

        // When
        booking.setDepositAmount(amount);

        // Then
        assertEquals(amount, booking.getDepositAmount());
    }

    @Test
    @DisplayName("shouldHandleNullNote")
    void shouldHandleNullNote() {
        // When
        booking.setNote(null);

        // Then
        assertNull(booking.getNote());
    }

    @Test
    @DisplayName("shouldHandleNullCancelReason")
    void shouldHandleNullCancelReason() {
        // When
        booking.setCancelReason(null);

        // Then
        assertNull(booking.getCancelReason());
    }
}

