package com.example.booking.web.controller.restaurantowner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
import com.example.booking.domain.User;
import com.example.booking.dto.admin.FavoriteStatisticsDto;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.service.FavoriteService;
import com.example.booking.service.RestaurantOwnerService;

/**
 * Unit tests for RestaurantFavoriteController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RestaurantFavoriteController Tests")
public class RestaurantFavoriteControllerTest {

    @Mock
    private FavoriteService favoriteService;

    @Mock
    private RestaurantOwnerService restaurantOwnerService;

    @Mock
    private RestaurantProfileRepository restaurantRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private Model model;

    @InjectMocks
    private RestaurantFavoriteController controller;

    private User owner;
    private RestaurantProfile restaurant;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setUsername("owner");

        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");
    }

    // ========== favoriteStatistics() Tests ==========

    @Test
    @DisplayName("shouldDisplayFavoriteStatistics_successfully")
    void shouldDisplayFavoriteStatistics_successfully() {
        // Given
        List<RestaurantProfile> restaurants = new ArrayList<>();
        restaurants.add(restaurant);

        when(authentication.getName()).thenReturn("owner");
        when(restaurantOwnerService.getRestaurantsByUserId(any())).thenReturn(restaurants);

        Pageable pageable = PageRequest.of(0, 20);
        List<FavoriteStatisticsDto> statistics = new ArrayList<>();
        
        when(restaurantOwnerService.getRestaurantOwnerByUserId(any())).thenReturn(Optional.of(new com.example.booking.domain.RestaurantOwner()));
        when(favoriteService.getFavoriteStatisticsForOwner(any(UUID.class), eq(pageable))).thenReturn(statistics);

        // When
        String view = controller.favoriteStatistics(0, 20, authentication, model);

        // Then
        assertNotNull(view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }
}

