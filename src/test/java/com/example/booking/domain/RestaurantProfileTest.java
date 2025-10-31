package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.booking.common.enums.RestaurantApprovalStatus;

/**
 * Unit tests for RestaurantProfile domain entity
 */
@DisplayName("RestaurantProfile Domain Entity Tests")
public class RestaurantProfileTest {

    private RestaurantProfile restaurant;
    private RestaurantOwner owner;

    @BeforeEach
    void setUp() {
        owner = new RestaurantOwner();
        owner.setUser(new User());

        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");
        restaurant.setOwner(owner);
    }

    // ========== Basic Getters/Setters Tests ==========

    @Test
    @DisplayName("shouldSetAndGetRestaurantName_successfully")
    void shouldSetAndGetRestaurantName_successfully() {
        // Given
        String name = "New Restaurant Name";

        // When
        restaurant.setRestaurantName(name);

        // Then
        assertEquals(name, restaurant.getRestaurantName());
    }

    @Test
    @DisplayName("shouldSetAndGetOwner_successfully")
    void shouldSetAndGetOwner_successfully() {
        // When
        RestaurantOwner result = restaurant.getOwner();

        // Then
        assertNotNull(result);
        assertEquals(owner.getOwnerId(), result.getOwnerId());
    }

    @Test
    @DisplayName("shouldSetAndGetApprovalStatus_successfully")
    void shouldSetAndGetApprovalStatus_successfully() {
        // Given
        RestaurantApprovalStatus status = RestaurantApprovalStatus.APPROVED;

        // When
        restaurant.setApprovalStatus(status);

        // Then
        assertEquals(status, restaurant.getApprovalStatus());
    }
}

