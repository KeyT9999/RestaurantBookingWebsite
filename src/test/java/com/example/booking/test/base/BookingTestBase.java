package com.example.booking.test.base;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;

import com.example.booking.common.enums.BookingStatus;
import com.example.booking.domain.Booking;
import com.example.booking.domain.Customer;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.BookingForm;
import com.example.booking.test.util.TestDataFactory;

/**
 * Simplified base test class with common setup for booking tests
 */
public abstract class BookingTestBase {

    protected Customer testCustomer;
    protected RestaurantProfile testRestaurant;
    protected RestaurantTable testTable;
    protected BookingForm testBookingForm;
    protected UUID customerId;

    @BeforeEach
    void setUpBase() {
        // Create test data using factory
        testCustomer = TestDataFactory.createTestCustomer();
        testRestaurant = TestDataFactory.createTestRestaurant();
        testTable = TestDataFactory.createTestTable(testRestaurant);
        testBookingForm = TestDataFactory.createValidBookingForm();
        
        customerId = testCustomer.getCustomerId();
    }

    // ==================== HELPER METHODS ====================

    protected BookingForm createBookingFormWithRestaurant(Integer restaurantId) {
        return TestDataFactory.createValidBookingForm(restaurantId, testTable.getTableId());
    }

    protected Booking createTestBooking() {
        return TestDataFactory.createTestBooking(testCustomer, testRestaurant);
    }

    protected User createTestUser() {
        return TestDataFactory.createTestUser();
    }

    protected User createTestCustomerUser() {
        return TestDataFactory.createTestUser(UserRole.CUSTOMER);
    }

    protected User createTestRestaurantOwnerUser() {
        return TestDataFactory.createTestUser(UserRole.RESTAURANT_OWNER);
    }

    protected User createTestAdminUser() {
        return TestDataFactory.createTestUser(UserRole.ADMIN);
    }

    // ==================== ASSERTION HELPERS ====================

    protected void assertBookingCreated(Booking booking) {
        assertNotNull(booking);
        assertNotNull(booking.getBookingId());
        assertEquals(customerId, booking.getCustomer().getCustomerId());
        assertEquals(testRestaurant.getRestaurantId(), booking.getRestaurant().getRestaurantId());
        assertEquals(BookingStatus.PENDING, booking.getStatus());
        assertNotNull(booking.getCreatedAt());
    }

    protected void assertBookingFormValid(BookingForm form) {
        assertNotNull(form);
        assertNotNull(form.getRestaurantId());
        assertNotNull(form.getTableId());
        assertTrue(form.getGuestCount() > 0);
        assertNotNull(form.getBookingTime());
        assertTrue(form.getBookingTime().isAfter(LocalDateTime.now()));
    }

    protected void assertBookingAmountCorrect(BigDecimal expected, BigDecimal actual) {
        assertEquals(expected, actual);
    }

    // ==================== TEST DATA GETTERS ====================

    protected Customer getTestCustomer() {
        return testCustomer;
    }

    protected RestaurantProfile getTestRestaurant() {
        return testRestaurant;
    }

    protected RestaurantTable getTestTable() {
        return testTable;
    }

    protected BookingForm getTestBookingForm() {
        return testBookingForm;
    }

    protected UUID getCustomerId() {
        return customerId;
    }
}