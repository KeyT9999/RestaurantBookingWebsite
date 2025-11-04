package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for UserRole enum
 */
@DisplayName("UserRole Enum Tests")
public class UserRoleTest {

    // ========== Enum Values Tests ==========

    @Test
    @DisplayName("shouldHaveAllEnumValues")
    void shouldHaveAllEnumValues() {
        assertNotNull(UserRole.admin);
        assertNotNull(UserRole.customer);
        assertNotNull(UserRole.restaurant_owner);
        assertNotNull(UserRole.ADMIN);
        assertNotNull(UserRole.CUSTOMER);
        assertNotNull(UserRole.RESTAURANT_OWNER);
    }

    @Test
    @DisplayName("shouldGetValue_forAllValues")
    void shouldGetValue_forAllValues() {
        assertEquals("admin", UserRole.admin.getValue());
        assertEquals("customer", UserRole.customer.getValue());
        assertEquals("restaurant_owner", UserRole.restaurant_owner.getValue());
        assertEquals("admin", UserRole.ADMIN.getValue());
        assertEquals("customer", UserRole.CUSTOMER.getValue());
        assertEquals("restaurant_owner", UserRole.RESTAURANT_OWNER.getValue());
    }

    @Test
    @DisplayName("shouldGetDisplayName_forAllValues")
    void shouldGetDisplayName_forAllValues() {
        assertEquals("Quản trị viên", UserRole.admin.getDisplayName());
        assertEquals("Khách hàng", UserRole.customer.getDisplayName());
        assertEquals("Chủ nhà hàng", UserRole.restaurant_owner.getDisplayName());
        assertEquals("Quản trị viên", UserRole.ADMIN.getDisplayName());
        assertEquals("Khách hàng", UserRole.CUSTOMER.getDisplayName());
        assertEquals("Chủ nhà hàng", UserRole.RESTAURANT_OWNER.getDisplayName());
    }

    // ========== isRestaurantOwner() Tests ==========

    @Test
    @DisplayName("shouldReturnTrue_whenRestaurantOwner_lowercase")
    void shouldReturnTrue_whenRestaurantOwner_lowercase() {
        assertTrue(UserRole.restaurant_owner.isRestaurantOwner());
    }

    @Test
    @DisplayName("shouldReturnTrue_whenRestaurantOwner_uppercase")
    void shouldReturnTrue_whenRestaurantOwner_uppercase() {
        assertTrue(UserRole.RESTAURANT_OWNER.isRestaurantOwner());
    }

    @Test
    @DisplayName("shouldReturnFalse_whenNotRestaurantOwner")
    void shouldReturnFalse_whenNotRestaurantOwner() {
        assertFalse(UserRole.admin.isRestaurantOwner());
        assertFalse(UserRole.customer.isRestaurantOwner());
        assertFalse(UserRole.ADMIN.isRestaurantOwner());
        assertFalse(UserRole.CUSTOMER.isRestaurantOwner());
    }

    // ========== isAdmin() Tests ==========

    @Test
    @DisplayName("shouldReturnTrue_whenAdmin_lowercase")
    void shouldReturnTrue_whenAdmin_lowercase() {
        assertTrue(UserRole.admin.isAdmin());
    }

    @Test
    @DisplayName("shouldReturnTrue_whenAdmin_uppercase")
    void shouldReturnTrue_whenAdmin_uppercase() {
        assertTrue(UserRole.ADMIN.isAdmin());
    }

    @Test
    @DisplayName("shouldReturnFalse_whenNotAdmin")
    void shouldReturnFalse_whenNotAdmin() {
        assertFalse(UserRole.customer.isAdmin());
        assertFalse(UserRole.restaurant_owner.isAdmin());
        assertFalse(UserRole.CUSTOMER.isAdmin());
        assertFalse(UserRole.RESTAURANT_OWNER.isAdmin());
    }

    // ========== isCustomer() Tests ==========

    @Test
    @DisplayName("shouldReturnTrue_whenCustomer_lowercase")
    void shouldReturnTrue_whenCustomer_lowercase() {
        assertTrue(UserRole.customer.isCustomer());
    }

    @Test
    @DisplayName("shouldReturnTrue_whenCustomer_uppercase")
    void shouldReturnTrue_whenCustomer_uppercase() {
        assertTrue(UserRole.CUSTOMER.isCustomer());
    }

    @Test
    @DisplayName("shouldReturnFalse_whenNotCustomer")
    void shouldReturnFalse_whenNotCustomer() {
        assertFalse(UserRole.admin.isCustomer());
        assertFalse(UserRole.restaurant_owner.isCustomer());
        assertFalse(UserRole.ADMIN.isCustomer());
        assertFalse(UserRole.RESTAURANT_OWNER.isCustomer());
    }
}
