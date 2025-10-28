package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import com.example.booking.common.enums.RestaurantApprovalStatus;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Test cases for RestaurantSecurityService
 */
@ExtendWith(MockitoExtension.class)
class RestaurantSecurityServiceTest {

    @Mock
    private SimpleUserService userService;

    @Mock
    private RestaurantOwnerService restaurantOwnerService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private RestaurantSecurityService restaurantSecurityService;

    private User mockUser;
    private RestaurantOwner mockRestaurantOwner;
    private List<RestaurantProfile> mockRestaurants;

    @BeforeEach
    void setUp() {
        // Setup mock user
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("testuser");
        mockUser.setRole(UserRole.RESTAURANT_OWNER);
        mockUser.setActive(true);

        // Setup mock restaurant owner
        mockRestaurantOwner = new RestaurantOwner();
        mockRestaurantOwner.setOwnerId(UUID.randomUUID());
        mockRestaurantOwner.setUser(mockUser);

        // Setup mock restaurants
        RestaurantProfile approvedRestaurant = new RestaurantProfile();
        approvedRestaurant.setRestaurantId(1);
        approvedRestaurant.setApprovalStatus(RestaurantApprovalStatus.APPROVED);

        RestaurantProfile pendingRestaurant = new RestaurantProfile();
        pendingRestaurant.setRestaurantId(2);
        pendingRestaurant.setApprovalStatus(RestaurantApprovalStatus.PENDING);

        mockRestaurants = List.of(approvedRestaurant, pendingRestaurant);
    }

    @Test
    void testIsUserActiveAndApproved_WithValidUserAndApprovedRestaurant_ShouldReturnTrue() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(mockUser.getId().toString());
        when(userService.findById(any(UUID.class))).thenReturn(mockUser);
        when(restaurantOwnerService.getRestaurantOwnerByUserId(any(UUID.class)))
                .thenReturn(Optional.of(mockRestaurantOwner));
        when(restaurantOwnerService.getRestaurantsByOwnerId(any(UUID.class)))
                .thenReturn(mockRestaurants);

        // When
        boolean result = restaurantSecurityService.isUserActiveAndApproved(authentication);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsUserActiveAndApproved_WithInactiveUser_ShouldReturnFalse() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        mockUser.setActive(false);
        when(authentication.getName()).thenReturn(mockUser.getId().toString());
        when(userService.findById(any(UUID.class))).thenReturn(mockUser);

        // When
        boolean result = restaurantSecurityService.isUserActiveAndApproved(authentication);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsUserActiveAndApproved_WithCustomerRole_ShouldReturnFalse() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        mockUser.setRole(UserRole.CUSTOMER);
        when(authentication.getName()).thenReturn(mockUser.getId().toString());
        when(userService.findById(any(UUID.class))).thenReturn(mockUser);

        // When
        boolean result = restaurantSecurityService.isUserActiveAndApproved(authentication);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsUserActiveAndApproved_WithNoApprovedRestaurants_ShouldReturnFalse() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        RestaurantProfile pendingOnly = new RestaurantProfile();
        pendingOnly.setApprovalStatus(RestaurantApprovalStatus.PENDING);
        List<RestaurantProfile> pendingOnlyList = List.of(pendingOnly);

        when(authentication.getName()).thenReturn(mockUser.getId().toString());
        when(userService.findById(any(UUID.class))).thenReturn(mockUser);
        when(restaurantOwnerService.getRestaurantOwnerByUserId(any(UUID.class)))
                .thenReturn(Optional.of(mockRestaurantOwner));
        when(restaurantOwnerService.getRestaurantsByOwnerId(any(UUID.class)))
                .thenReturn(pendingOnlyList);

        // When
        boolean result = restaurantSecurityService.isUserActiveAndApproved(authentication);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsUserActiveAndApproved_WithNullAuthentication_ShouldReturnFalse() {
        // When
        boolean result = restaurantSecurityService.isUserActiveAndApproved(null);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsUserActiveAndApproved_WithUserNotFound_ShouldReturnFalse() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("nonexistent");
        when(userService.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When
        boolean result = restaurantSecurityService.isUserActiveAndApproved(authentication);

        // Then
        assertFalse(result);
    }
}
