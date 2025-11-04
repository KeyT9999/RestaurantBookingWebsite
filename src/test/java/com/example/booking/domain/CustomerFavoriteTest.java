package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for CustomerFavorite domain entity
 */
@DisplayName("CustomerFavorite Domain Entity Tests")
public class CustomerFavoriteTest {

    private CustomerFavorite customerFavorite;
    private Customer customer;
    private RestaurantProfile restaurant;

    @BeforeEach
    void setUp() {
        customerFavorite = new CustomerFavorite();
        customer = new Customer();
        restaurant = new RestaurantProfile();
    }

    // ========== Constructor Tests ==========

    @Test
    @DisplayName("shouldCreateCustomerFavorite_withDefaultConstructor")
    void shouldCreateCustomerFavorite_withDefaultConstructor() {
        // When
        CustomerFavorite favorite = new CustomerFavorite();

        // Then
        assertNotNull(favorite);
        assertNotNull(favorite.getCreatedAt());
    }

    @Test
    @DisplayName("shouldCreateCustomerFavorite_withParameterizedConstructor")
    void shouldCreateCustomerFavorite_withParameterizedConstructor() {
        // When
        CustomerFavorite favorite = new CustomerFavorite(customer, restaurant);

        // Then
        assertNotNull(favorite);
        assertEquals(customer, favorite.getCustomer());
        assertEquals(restaurant, favorite.getRestaurant());
        assertNotNull(favorite.getCreatedAt());
    }

    // ========== Getter/Setter Tests ==========

    @Test
    @DisplayName("shouldSetAndGetFavoriteId")
    void shouldSetAndGetFavoriteId() {
        // Given
        Integer id = 1;

        // When
        customerFavorite.setFavoriteId(id);

        // Then
        assertEquals(id, customerFavorite.getFavoriteId());
    }

    @Test
    @DisplayName("shouldSetAndGetCustomer")
    void shouldSetAndGetCustomer() {
        // When
        customerFavorite.setCustomer(customer);

        // Then
        assertEquals(customer, customerFavorite.getCustomer());
    }

    @Test
    @DisplayName("shouldSetAndGetRestaurant")
    void shouldSetAndGetRestaurant() {
        // When
        customerFavorite.setRestaurant(restaurant);

        // Then
        assertEquals(restaurant, customerFavorite.getRestaurant());
    }

    @Test
    @DisplayName("shouldSetAndGetCreatedAt")
    void shouldSetAndGetCreatedAt() {
        // Given
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        customerFavorite.setCreatedAt(createdAt);

        // Then
        assertEquals(createdAt, customerFavorite.getCreatedAt());
    }
}
