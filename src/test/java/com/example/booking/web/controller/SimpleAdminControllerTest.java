package com.example.booking.web.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booking.domain.RestaurantProfile;
import com.example.booking.repository.RestaurantProfileRepository;

/**
 * Unit tests for SimpleAdminController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SimpleAdminController Tests")
public class SimpleAdminControllerTest {

    @Mock
    private RestaurantProfileRepository restaurantRepository;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private SimpleAdminController controller;

    private RestaurantProfile restaurant;

    @BeforeEach
    void setUp() {
        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");
    }

    // ========== approveRestaurant() Tests ==========

    @Test
    @DisplayName("shouldApproveRestaurant_successfully")
    void shouldApproveRestaurant_successfully() {
        // Given
        when(restaurantRepository.findById(1)).thenReturn(java.util.Optional.of(restaurant));

        // When
        String view = controller.simpleApproveRestaurant(1, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/simple/restaurants", view);
        verify(restaurantRepository, times(1)).save(any(RestaurantProfile.class));
        verify(redirectAttributes, times(1)).addFlashAttribute("success", anyString());
    }

    @Test
    @DisplayName("shouldReturnError_whenRestaurantNotFound")
    void shouldReturnError_whenRestaurantNotFound() {
        // Given
        when(restaurantRepository.findById(1)).thenReturn(java.util.Optional.empty());

        // When
        String view = controller.simpleApproveRestaurant(1, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/simple/restaurants", view);
        verify(redirectAttributes, times(1)).addFlashAttribute("error", anyString());
    }

    // ========== rejectRestaurant() Tests ==========

    @Test
    @DisplayName("shouldRejectRestaurant_successfully")
    void shouldRejectRestaurant_successfully() {
        // Given
        when(restaurantRepository.findById(1)).thenReturn(java.util.Optional.of(restaurant));

        // When
        String view = controller.simpleRejectRestaurant(1, "Not suitable", redirectAttributes);

        // Then
        assertEquals("redirect:/admin/simple/restaurants", view);
        verify(restaurantRepository, times(1)).save(any(RestaurantProfile.class));
    }

    @Test
    @DisplayName("shouldRejectRestaurant_withNullReason")
    void shouldRejectRestaurant_withNullReason() {
        // Given
        when(restaurantRepository.findById(1)).thenReturn(java.util.Optional.of(restaurant));

        // When
        String view = controller.simpleRejectRestaurant(1, null, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/simple/restaurants", view);
        verify(restaurantRepository, times(1)).save(any(RestaurantProfile.class));
    }

    @Test
    @DisplayName("shouldRejectRestaurant_handleErrorWhenNotFound")
    void shouldRejectRestaurant_handleErrorWhenNotFound() {
        // Given
        when(restaurantRepository.findById(999)).thenReturn(java.util.Optional.empty());

        // When
        String view = controller.simpleRejectRestaurant(999, "Test reason", redirectAttributes);

        // Then
        assertEquals("redirect:/admin/simple/restaurants", view);
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
    }

    // ========== simpleRestaurantList() Tests ==========

    @Test
    @DisplayName("shouldDisplayRestaurantList_successfully")
    void shouldDisplayRestaurantList_successfully() {
        // Given
        java.util.List<RestaurantProfile> restaurants = java.util.List.of(restaurant);
        when(restaurantRepository.findAll()).thenReturn(restaurants);
        restaurant.setApprovalStatus(com.example.booking.common.enums.RestaurantApprovalStatus.PENDING);

        // When
        String view = controller.simpleRestaurantList(model);

        // Then
        assertEquals("admin/simple-restaurant-list", view);
        verify(model).addAttribute(eq("restaurants"), anyList());
        verify(model).addAttribute(eq("pendingCount"), anyLong());
    }

    @Test
    @DisplayName("shouldHandleException_inRestaurantList")
    void shouldHandleException_inRestaurantList() {
        // Given
        when(restaurantRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When
        String view = controller.simpleRestaurantList(model);

        // Then
        assertEquals("admin/simple-restaurant-list", view);
        verify(model).addAttribute(eq("error"), anyString());
        verify(model).addAttribute(eq("restaurants"), eq(java.util.Collections.emptyList()));
    }
}
