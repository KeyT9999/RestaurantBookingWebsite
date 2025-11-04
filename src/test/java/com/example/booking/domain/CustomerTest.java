package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Customer domain entity
 */
@DisplayName("Customer Domain Entity Tests")
public class CustomerTest {

    private Customer customer;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("customer@test.com");

        customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        customer.setUser(user);
    }

    // ========== Basic Getters/Setters Tests ==========

    @Test
    @DisplayName("shouldSetAndGetUser_successfully")
    void shouldSetAndGetUser_successfully() {
        // When
        User result = customer.getUser();

        // Then
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
    }

    @Test
    @DisplayName("shouldSetAndGetCustomerId_successfully")
    void shouldSetAndGetCustomerId_successfully() {
        // Given
        UUID customerId = UUID.randomUUID();

        // When
        customer.setCustomerId(customerId);

        // Then
        assertEquals(customerId, customer.getCustomerId());
    }

    // ========== Constructor Tests ==========

    @Test
    @DisplayName("shouldCreateCustomer_withUserConstructor")
    void shouldCreateCustomer_withUserConstructor() {
        // Given
        User newUser = new User();
        newUser.setId(UUID.randomUUID());
        newUser.setFullName("Test Customer");

        // When
        Customer newCustomer = new Customer(newUser);

        // Then
        assertEquals(newUser, newCustomer.getUser());
        assertEquals(newUser.getFullName(), newCustomer.getFullName());
    }

    // ========== FullName Tests ==========

    @Test
    @DisplayName("shouldSetAndGetFullName_successfully")
    void shouldSetAndGetFullName_successfully() {
        // Given
        String fullName = "John Doe";

        // When
        customer.setFullName(fullName);

        // Then
        assertEquals(fullName, customer.getFullName());
    }

    // ========== Lifecycle Callback Tests ==========

    @Test
    @DisplayName("shouldSetCreatedAtAndUpdatedAt_onPrePersist")
    void shouldSetCreatedAtAndUpdatedAt_onPrePersist() throws Exception {
        // Given
        Customer newCustomer = new Customer();
        newCustomer.setUser(user);
        user.setFullName("Test User");

        // When - Simulate @PrePersist by calling onCreate directly
        java.lang.reflect.Method onCreate = Customer.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(newCustomer);

        // Then
        assertNotNull(newCustomer.getCreatedAt());
        assertNotNull(newCustomer.getUpdatedAt());
        assertEquals(newCustomer.getCreatedAt(), newCustomer.getUpdatedAt());
    }

    @Test
    @DisplayName("shouldSetFullNameFromUser_onPrePersist_whenFullNameIsNull")
    void shouldSetFullNameFromUser_onPrePersist_whenFullNameIsNull() throws Exception {
        // Given
        Customer newCustomer = new Customer();
        user.setFullName("Test User Name");
        newCustomer.setUser(user);
        newCustomer.setFullName(null); // fullName is null

        // When - Simulate @PrePersist
        java.lang.reflect.Method onCreate = Customer.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(newCustomer);

        // Then
        assertEquals(user.getFullName(), newCustomer.getFullName());
    }

    @Test
    @DisplayName("shouldNotOverrideFullName_onPrePersist_whenFullNameIsSet")
    void shouldNotOverrideFullName_onPrePersist_whenFullNameIsSet() throws Exception {
        // Given
        Customer newCustomer = new Customer();
        user.setFullName("User Name");
        newCustomer.setUser(user);
        newCustomer.setFullName("Customer Name"); // fullName is already set

        // When - Simulate @PrePersist
        java.lang.reflect.Method onCreate = Customer.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(newCustomer);

        // Then
        assertEquals("Customer Name", newCustomer.getFullName()); // Should keep existing fullName
    }

    @Test
    @DisplayName("shouldSetUpdatedAt_onPreUpdate")
    void shouldSetUpdatedAt_onPreUpdate() throws Exception {
        // Given
        LocalDateTime initialCreatedAt = LocalDateTime.now().minusHours(1);
        customer.setCreatedAt(initialCreatedAt);
        customer.setUpdatedAt(initialCreatedAt);

        // When - Simulate @PreUpdate
        java.lang.reflect.Method onUpdate = Customer.class.getDeclaredMethod("onUpdate");
        onUpdate.setAccessible(true);
        onUpdate.invoke(customer);

        // Then
        assertNotNull(customer.getUpdatedAt());
        assertTrue(customer.getUpdatedAt().isAfter(initialCreatedAt));
        assertEquals(initialCreatedAt, customer.getCreatedAt()); // createdAt should not change
    }

    @Test
    @DisplayName("shouldSyncFullNameFromUser_onPreUpdate")
    void shouldSyncFullNameFromUser_onPreUpdate() throws Exception {
        // Given
        user.setFullName("Updated User Name");
        customer.setUser(user);
        customer.setFullName("Old Name");

        // When - Simulate @PreUpdate
        java.lang.reflect.Method onUpdate = Customer.class.getDeclaredMethod("onUpdate");
        onUpdate.setAccessible(true);
        onUpdate.invoke(customer);

        // Then
        assertEquals(user.getFullName(), customer.getFullName());
    }

    @Test
    @DisplayName("shouldNotSyncFullName_onPreUpdate_whenUserFullNameIsNull")
    void shouldNotSyncFullName_onPreUpdate_whenUserFullNameIsNull() throws Exception {
        // Given
        user.setFullName(null);
        customer.setUser(user);
        customer.setFullName("Customer Name");

        // When - Simulate @PreUpdate
        java.lang.reflect.Method onUpdate = Customer.class.getDeclaredMethod("onUpdate");
        onUpdate.setAccessible(true);
        onUpdate.invoke(customer);

        // Then
        assertEquals("Customer Name", customer.getFullName()); // Should keep existing fullName
    }

    // ========== Relationship Tests ==========

    @Test
    @DisplayName("shouldSetAndGetBookings_successfully")
    void shouldSetAndGetBookings_successfully() {
        // Given
        List<Booking> bookings = new java.util.ArrayList<>();
        Booking booking = new Booking();
        bookings.add(booking);

        // When
        customer.setBookings(bookings);

        // Then
        assertNotNull(customer.getBookings());
        assertEquals(1, customer.getBookings().size());
    }

    @Test
    @DisplayName("shouldSetAndGetReviews_successfully")
    void shouldSetAndGetReviews_successfully() {
        // Given
        List<Review> reviews = new java.util.ArrayList<>();
        Review review = new Review();
        reviews.add(review);

        // When
        customer.setReviews(reviews);

        // Then
        assertNotNull(customer.getReviews());
        assertEquals(1, customer.getReviews().size());
    }

    @Test
    @DisplayName("shouldSetAndGetFavorites_successfully")
    void shouldSetAndGetFavorites_successfully() {
        // Given
        List<CustomerFavorite> favorites = new java.util.ArrayList<>();
        CustomerFavorite favorite = new CustomerFavorite();
        favorites.add(favorite);

        // When
        customer.setFavorites(favorites);

        // Then
        assertNotNull(customer.getFavorites());
        assertEquals(1, customer.getFavorites().size());
    }

    @Test
    @DisplayName("shouldSetAndGetVouchers_successfully")
    void shouldSetAndGetVouchers_successfully() {
        // Given
        List<CustomerVoucher> vouchers = new java.util.ArrayList<>();
        CustomerVoucher voucher = new CustomerVoucher();
        vouchers.add(voucher);

        // When
        customer.setVouchers(vouchers);

        // Then
        assertNotNull(customer.getVouchers());
        assertEquals(1, customer.getVouchers().size());
    }

    @Test
    @DisplayName("shouldSetAndGetPayments_successfully")
    void shouldSetAndGetPayments_successfully() {
        // Given
        List<Payment> payments = new java.util.ArrayList<>();
        Payment payment = new Payment();
        payments.add(payment);

        // When
        customer.setPayments(payments);

        // Then
        assertNotNull(customer.getPayments());
        assertEquals(1, customer.getPayments().size());
    }

    @Test
    @DisplayName("shouldSetAndGetWaitlists_successfully")
    void shouldSetAndGetWaitlists_successfully() {
        // Given
        List<Waitlist> waitlists = new java.util.ArrayList<>();
        Waitlist waitlist = new Waitlist();
        waitlists.add(waitlist);

        // When
        customer.setWaitlists(waitlists);

        // Then
        assertNotNull(customer.getWaitlists());
        assertEquals(1, customer.getWaitlists().size());
    }

    // ========== Timestamp Tests ==========

    @Test
    @DisplayName("shouldSetAndGetCreatedAt_successfully")
    void shouldSetAndGetCreatedAt_successfully() {
        // Given
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        customer.setCreatedAt(createdAt);

        // Then
        assertEquals(createdAt, customer.getCreatedAt());
    }

    @Test
    @DisplayName("shouldSetAndGetUpdatedAt_successfully")
    void shouldSetAndGetUpdatedAt_successfully() {
        // Given
        LocalDateTime updatedAt = LocalDateTime.now();

        // When
        customer.setUpdatedAt(updatedAt);

        // Then
        assertEquals(updatedAt, customer.getUpdatedAt());
    }
}

