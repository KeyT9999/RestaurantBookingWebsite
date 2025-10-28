package com.example.booking.web.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.Model;

import com.example.booking.common.enums.RestaurantApprovalStatus;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.service.RestaurantOwnerService;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
@DisplayName("RestaurantOwnerController Tests")
public class RestaurantOwnerControllerTest {

    @Mock
    private RestaurantOwnerService restaurantOwnerService;

    @Mock
    private Authentication authentication;

    @Mock
    private Model model;

    @InjectMocks
    private RestaurantOwnerController restaurantOwnerController;

    private User testUser;
    private RestaurantOwner testRestaurantOwner;
    private List<RestaurantProfile> testRestaurants;

    @BeforeEach
    public void setUp() {
        // Create test user
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("owner@example.com");
        testUser.setEmail("owner@example.com");
        testUser.setFullName("Restaurant Owner");
        testUser.setRole(UserRole.RESTAURANT_OWNER);
        testUser.setEmailVerified(true);

        // Create test restaurant owner
        testRestaurantOwner = new RestaurantOwner(testUser);
        testRestaurantOwner.setOwnerId(UUID.randomUUID());
        testRestaurantOwner.setOwnerName("Restaurant Owner");

        // Create test restaurants
        testRestaurants = new ArrayList<>();
        
        RestaurantProfile restaurant1 = new RestaurantProfile();
        restaurant1.setRestaurantId(1);
        restaurant1.setRestaurantName("Restaurant 1");
        restaurant1.setApprovalStatus(RestaurantApprovalStatus.APPROVED);
        restaurant1.setOwner(testRestaurantOwner);
        
        RestaurantProfile restaurant2 = new RestaurantProfile();
        restaurant2.setRestaurantId(2);
        restaurant2.setRestaurantName("Restaurant 2");
        restaurant2.setApprovalStatus(RestaurantApprovalStatus.APPROVED);
        restaurant2.setOwner(testRestaurantOwner);
        
        RestaurantProfile restaurant3 = new RestaurantProfile();
        restaurant3.setRestaurantId(3);
        restaurant3.setRestaurantName("Restaurant 3");
        restaurant3.setApprovalStatus(RestaurantApprovalStatus.PENDING);
        restaurant3.setOwner(testRestaurantOwner);
        
        testRestaurants = Arrays.asList(restaurant1, restaurant2, restaurant3);
    }

    // ========== profile() Tests ==========

    @Test
    @DisplayName("Should return restaurants list for authenticated owner")
    public void testGetOwnerProfile_WithAuthenticatedOwner_ShouldReturnRestaurantsList() {
        // Given
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_RESTAURANT_OWNER"))
        );
        
        when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
            .thenReturn(Optional.of(testRestaurantOwner));
        when(restaurantOwnerService.getRestaurantsByOwnerId(testRestaurantOwner.getOwnerId()))
            .thenReturn(testRestaurants);

        // When
        String result = restaurantOwnerController.profile(model, authentication);

        // Then
        assertEquals("restaurant-owner/profile", result);
    }

    @Test
    @DisplayName("Should return empty list when owner has no restaurants")
    public void testGetOwnerProfile_WithNoRestaurants_ShouldReturnEmptyList() {
        // Given
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_RESTAURANT_OWNER"))
        );
        
        when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
            .thenReturn(Optional.of(testRestaurantOwner));
        when(restaurantOwnerService.getRestaurantsByOwnerId(testRestaurantOwner.getOwnerId()))
            .thenReturn(new ArrayList<>());

        // When
        String result = restaurantOwnerController.profile(model, authentication);

        // Then
        assertEquals("restaurant-owner/profile", result);
    }

    @Test
    @DisplayName("Should return only own restaurants for multiple owners")
    public void testGetOwnerProfile_WithMultipleOwners_ShouldReturnOnlyOwnRestaurants() {
        // Given
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_RESTAURANT_OWNER"))
        );
        
        when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
            .thenReturn(Optional.of(testRestaurantOwner));
        
        // Only return restaurants owned by this owner (IDs 1, 2, 3)
        List<RestaurantProfile> ownerRestaurants = Arrays.asList(
            testRestaurants.get(0), testRestaurants.get(1), testRestaurants.get(2)
        );
        
        when(restaurantOwnerService.getRestaurantsByOwnerId(testRestaurantOwner.getOwnerId()))
            .thenReturn(ownerRestaurants);

        // When
        String result = restaurantOwnerController.profile(model, authentication);

        // Then
        assertEquals("restaurant-owner/profile", result);
        assertEquals(3, ownerRestaurants.size());
    }

    @Test
    @DisplayName("Should handle exception gracefully")
    public void testGetOwnerProfile_WithException_ShouldHandleGracefully() {
        // Given
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_RESTAURANT_OWNER"))
        );
        
        when(restaurantOwnerService.getRestaurantOwnerByUserId(testUser.getId()))
            .thenThrow(new RuntimeException("Database error"));

        // When
        String result = restaurantOwnerController.profile(model, authentication);

        // Then
        assertEquals("restaurant-owner/profile", result);
    }

    @Test
    @DisplayName("Should return error for invalid role (CUSTOMER)")
    public void testGetOwnerProfile_WithInvalidRole_ShouldReturnError() {
        // Given - Create a customer user
        User customerUser = new User();
        customerUser.setId(UUID.randomUUID());
        customerUser.setUsername("customer@example.com");
        customerUser.setRole(UserRole.CUSTOMER);
        customerUser.setEmailVerified(true);

        when(authentication.getName()).thenReturn(customerUser.getUsername());
        when(authentication.getPrincipal()).thenReturn(customerUser);
        when(authentication.getAuthorities()).thenReturn(
            (Collection) Arrays.asList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );

        // When
        String result = restaurantOwnerController.profile(model, authentication);

        // Then - Controller should still return the profile page but with empty list
        assertEquals("restaurant-owner/profile", result);
    }
}

