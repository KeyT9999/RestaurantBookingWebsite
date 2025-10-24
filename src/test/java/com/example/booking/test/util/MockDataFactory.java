package com.example.booking.test.util;

import com.example.booking.common.enums.BookingStatus;
import com.example.booking.common.enums.TableStatus;
import com.example.booking.common.enums.PaymentStatus;
import com.example.booking.domain.*;
import com.example.booking.dto.BookingForm;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * MockDataFactory - Comprehensive factory for creating realistic test data
 * Used in Mockito-based unit tests for Restaurant Booking System
 */
public class MockDataFactory {
    
    // ==================== REALISTIC UUIDs ====================
    
    public static final UUID CUSTOMER_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    public static final UUID RESTAURANT_OWNER_UUID = UUID.fromString("987fcdeb-51a2-43d1-b789-123456789abc");
    public static final UUID ADMIN_UUID = UUID.fromString("456789ab-cdef-1234-5678-90abcdef1234");
    
    // ==================== CUSTOMER FACTORY ====================
    
    public static Customer createMockCustomer() {
        Customer customer = new Customer();
        customer.setCustomerId(CUSTOMER_UUID);
        customer.setFullName("John Doe");
        customer.setEmail("john.doe@example.com");
        customer.setPhoneNumber("0123456789");
        return customer;
    }
    
    public static Customer createMockCustomer(String name, String email) {
        Customer customer = createMockCustomer();
        customer.setFullName(name);
        customer.setEmail(email);
        return customer;
    }
    
    public static Customer createMockCustomerWithUser() {
        Customer customer = createMockCustomer();
        User user = createMockUser();
        customer.setUser(user);
        return customer;
    }
    
    // ==================== RESTAURANT FACTORY ====================
    
    public static RestaurantProfile createMockRestaurant() {
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");
        restaurant.setAddress("123 Test Street, District 1, Ho Chi Minh City");
        restaurant.setPhoneNumber("0987654321");
        restaurant.setDescription("A beautiful restaurant for testing");
        restaurant.setStatus(com.example.booking.common.enums.RestaurantStatus.ACTIVE);
        return restaurant;
    }
    
    public static RestaurantProfile createMockRestaurant(String name, String address) {
        RestaurantProfile restaurant = createMockRestaurant();
        restaurant.setRestaurantName(name);
        restaurant.setAddress(address);
        return restaurant;
    }
    
    public static RestaurantProfile createMockRestaurantWithOwner() {
        RestaurantProfile restaurant = createMockRestaurant();
        RestaurantOwner owner = createMockRestaurantOwner();
        restaurant.setOwner(owner);
        return restaurant;
    }
    
    // ==================== TABLE FACTORY ====================
    
    public static RestaurantTable createMockTable() {
        RestaurantTable table = new RestaurantTable();
        table.setTableId(1);
        table.setTableName("Table 1");
        table.setCapacity(4);
        table.setStatus(TableStatus.AVAILABLE);
        table.setDescription("A comfortable table for 4 people");
        return table;
    }
    
    public static RestaurantTable createMockTable(String name, int capacity) {
        RestaurantTable table = createMockTable();
        table.setTableName(name);
        table.setCapacity(capacity);
        return table;
    }
    
    public static RestaurantTable createMockTableWithRestaurant() {
        RestaurantTable table = createMockTable();
        RestaurantProfile restaurant = createMockRestaurant();
        table.setRestaurant(restaurant);
        return table;
    }
    
    // ==================== BOOKING FACTORY ====================
    
    public static Booking createMockBooking() {
        Booking booking = new Booking();
        booking.setBookingId(1);
        booking.setCustomer(createMockCustomer());
        booking.setRestaurant(createMockRestaurant());
        booking.setBookingTime(LocalDateTime.now().plusDays(1).withHour(19).withMinute(0));
        booking.setNumberOfGuests(4);
        booking.setStatus(BookingStatus.PENDING);
        booking.setDepositAmount(new BigDecimal("100000.00"));
        booking.setNote("Test booking for dinner");
        booking.setCreatedAt(LocalDateTime.now());
        return booking;
    }
    
    public static Booking createMockBooking(BookingStatus status) {
        Booking booking = createMockBooking();
        booking.setStatus(status);
        return booking;
    }
    
    public static Booking createMockBooking(LocalDateTime bookingTime) {
        Booking booking = createMockBooking();
        booking.setBookingTime(bookingTime);
        return booking;
    }
    
    public static Booking createMockBookingWithDetails() {
        Booking booking = createMockBooking();
        booking.setCustomer(createMockCustomerWithUser());
        booking.setRestaurant(createMockRestaurantWithOwner());
        return booking;
    }
    
    // ==================== BOOKING FORM FACTORY ====================
    
    public static BookingForm createMockBookingForm() {
        BookingForm form = new BookingForm();
        form.setRestaurantId(1);
        form.setTableId(1);
        form.setGuestCount(4);
        form.setBookingTime(LocalDateTime.now().plusDays(1).withHour(19).withMinute(0));
        form.setDepositAmount(new BigDecimal("100000.00"));
        form.setNote("Test booking form");
        return form;
    }
    
    public static BookingForm createMockBookingForm(Integer restaurantId, Integer tableId) {
        BookingForm form = createMockBookingForm();
        form.setRestaurantId(restaurantId);
        form.setTableId(tableId);
        return form;
    }
    
    public static BookingForm createMockBookingFormWithInvalidData() {
        BookingForm form = createMockBookingForm();
        form.setGuestCount(0); // Invalid guest count
        form.setBookingTime(LocalDateTime.now().minusDays(1)); // Past date
        form.setDepositAmount(new BigDecimal("-1000")); // Negative amount
        return form;
    }
    
    // ==================== USER FACTORY ====================
    
    public static User createMockUser() {
        User user = new User();
        user.setId(CUSTOMER_UUID);
        user.setUsername("johndoe");
        user.setEmail("john.doe@example.com");
        user.setFullName("John Doe");
        user.setPhoneNumber("0123456789");
        user.setRole(com.example.booking.common.enums.UserRole.CUSTOMER);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
    
    public static User createMockUser(com.example.booking.common.enums.UserRole role) {
        User user = createMockUser();
        user.setRole(role);
        return user;
    }
    
    public static User createMockRestaurantOwnerUser() {
        User user = createMockUser(com.example.booking.common.enums.UserRole.RESTAURANT_OWNER);
        user.setId(RESTAURANT_OWNER_UUID);
        user.setUsername("restaurantowner");
        user.setEmail("owner@restaurant.com");
        return user;
    }
    
    public static User createMockAdminUser() {
        User user = createMockUser(com.example.booking.common.enums.UserRole.ADMIN);
        user.setId(ADMIN_UUID);
        user.setUsername("admin");
        user.setEmail("admin@system.com");
        return user;
    }
    
    // ==================== RESTAURANT OWNER FACTORY ====================
    
    public static RestaurantOwner createMockRestaurantOwner() {
        RestaurantOwner owner = new RestaurantOwner();
        owner.setOwnerId(1);
        owner.setUser(createMockRestaurantOwnerUser());
        owner.setBusinessLicense("BL123456789");
        owner.setTaxCode("TC987654321");
        owner.setCreatedAt(LocalDateTime.now());
        return owner;
    }
    
    // ==================== PAYMENT FACTORY ====================
    
    public static Payment createMockPayment() {
        Payment payment = new Payment();
        payment.setPaymentId(1);
        payment.setBooking(createMockBooking());
        payment.setAmount(new BigDecimal("100000.00"));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod("CREDIT_CARD");
        payment.setCreatedAt(LocalDateTime.now());
        return payment;
    }
    
    public static Payment createMockPayment(PaymentStatus status) {
        Payment payment = createMockPayment();
        payment.setStatus(status);
        return payment;
    }
    
    // ==================== NOTIFICATION FACTORY ====================
    
    public static Notification createMockNotification() {
        Notification notification = new Notification();
        notification.setNotificationId(1);
        notification.setUser(createMockUser());
        notification.setTitle("Booking Confirmation");
        notification.setMessage("Your booking has been confirmed");
        notification.setType(com.example.booking.domain.NotificationType.BOOKING_CONFIRMATION);
        notification.setStatus(com.example.booking.domain.NotificationStatus.UNREAD);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }
    
    // ==================== DISH FACTORY ====================
    
    public static Dish createMockDish() {
        Dish dish = new Dish();
        dish.setDishId(1);
        dish.setName("Test Dish");
        dish.setDescription("A delicious test dish");
        dish.setPrice(new BigDecimal("50000.00"));
        dish.setCategory("Main Course");
        dish.setRestaurant(createMockRestaurant());
        dish.setAvailable(true);
        return dish;
    }
    
    public static Dish createMockDish(String name, BigDecimal price) {
        Dish dish = createMockDish();
        dish.setName(name);
        dish.setPrice(price);
        return dish;
    }
    
    // ==================== UTILITY METHODS ====================
    
    public static LocalDateTime getFutureDateTime(int daysFromNow) {
        return LocalDateTime.now().plusDays(daysFromNow).withHour(19).withMinute(0);
    }
    
    public static LocalDateTime getPastDateTime(int daysAgo) {
        return LocalDateTime.now().minusDays(daysAgo).withHour(19).withMinute(0);
    }
    
    public static BigDecimal createPrice(String amount) {
        return new BigDecimal(amount);
    }
    
    public static UUID createRandomUUID() {
        return UUID.randomUUID();
    }
    
    public static String createValidPhoneNumber() {
        return "0123456789";
    }
    
    public static String createValidEmail() {
        return "test@example.com";
    }
    
    // ==================== EDGE CASE DATA ====================
    
    public static BookingForm createEmptyBookingForm() {
        return new BookingForm();
    }
    
    public static BookingForm createBookingFormWithMaxGuests() {
        BookingForm form = createMockBookingForm();
        form.setGuestCount(20); // Maximum capacity
        return form;
    }
    
    public static BookingForm createBookingFormWithMinGuests() {
        BookingForm form = createMockBookingForm();
        form.setGuestCount(1); // Minimum guests
        return form;
    }
    
    public static LocalDateTime getBookingTimeAtMidnight() {
        return LocalDateTime.now().plusDays(1).withHour(0).withMinute(0);
    }
    
    public static LocalDateTime getBookingTimeAtClosing() {
        return LocalDateTime.now().plusDays(1).withHour(23).withMinute(30);
    }
}
