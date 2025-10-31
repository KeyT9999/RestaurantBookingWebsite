package com.example.booking.web.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

import com.example.booking.domain.RestaurantProfile;
import com.example.booking.dto.NearbyRestaurantDto;
import com.example.booking.dto.RestaurantDto;
import com.example.booking.service.RestaurantManagementService;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.CustomerService;
import com.example.booking.service.ReviewService;
import com.example.booking.service.NotificationService;
import com.example.booking.repository.RestaurantMediaRepository;

/**
 * Unit tests for HomeController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HomeController Tests")
public class HomeControllerTest {

    @Mock
    private RestaurantManagementService restaurantService;

    @Mock
    private RestaurantOwnerService restaurantOwnerService;

    @Mock
    private CustomerService customerService;

    @Mock
    private ReviewService reviewService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private RestaurantMediaRepository restaurantMediaRepository;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private HomeController controller;

    private RestaurantProfile restaurant;

    @BeforeEach
    void setUp() {
        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");
    }

    // ========== home() Tests ==========

    @Test
    @DisplayName("shouldDisplayHomePage_successfully")
    void shouldDisplayHomePage_successfully() {
        // Given
        Pageable pageable = PageRequest.of(0, 12);
        Page<RestaurantProfile> restaurantPage = mock(Page.class);
        List<RestaurantProfile> restaurants = new ArrayList<>();
        restaurants.add(restaurant);

        when(restaurantService.findTopRatedRestaurants(any())).thenReturn(restaurants);

        // When
        String view = controller.home(null, null, null, model, authentication);

        // Then
        assertEquals("public/home", view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    // ========== search() Tests ==========

    @Test
    @DisplayName("shouldSearchRestaurants_successfully")
    void shouldSearchRestaurants_successfully() {
        // Given
        String searchTerm = "test";
        Pageable pageable = PageRequest.of(0, 12);
        Page<RestaurantProfile> restaurantPage = mock(Page.class);

        when(restaurantService.getRestaurantsWithFilters(any(), eq(searchTerm), any(), any(), any()))
                .thenReturn(restaurantPage);

        // When
        String view = controller.restaurants(0, 12, "restaurantName", "asc", searchTerm, null, null, null, model);

        // Then
        assertNotNull(view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    // ========== restaurantDetails() Tests ==========

    @Test
    @DisplayName("shouldDisplayRestaurantDetails_successfully")
    void shouldDisplayRestaurantDetails_successfully() {
        // Given
        RestaurantDto restaurantDto = new RestaurantDto();
        restaurantDto.setRestaurantId(1);

        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.of(restaurant));

        // When
        String view = controller.restaurantDetail(1, model, authentication);

        // Then
        assertNotNull(view);
        verify(model, atLeastOnce()).addAttribute(eq("restaurant"), any());
    }

    @Test
    @DisplayName("shouldReturnRedirect_whenRestaurantNotFound")
    void shouldReturnRedirect_whenRestaurantNotFound() {
        // Given
        when(restaurantOwnerService.getRestaurantById(1)).thenReturn(Optional.empty());

        // When
        String view = controller.restaurantDetail(1, model, authentication);

        // Then
        assertTrue(view.contains("redirect:/restaurants"));
    }
}
