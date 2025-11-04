package com.example.booking.service.impl;

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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.booking.domain.Customer;
import com.example.booking.domain.CustomerFavorite;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.dto.admin.FavoriteStatisticsDto;
import com.example.booking.dto.customer.ToggleFavoriteRequest;
import com.example.booking.dto.customer.ToggleFavoriteResponse;
import com.example.booking.repository.CustomerFavoriteRepository;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.repository.RestaurantProfileRepository;

/**
 * Unit tests for FavoriteServiceImpl
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FavoriteServiceImpl Tests")
public class FavoriteServiceImplTest {

    @Mock
    private CustomerFavoriteRepository favoriteRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RestaurantProfileRepository restaurantRepository;

    @InjectMocks
    private FavoriteServiceImpl favoriteService;

    private Customer customer;
    private RestaurantProfile restaurant;
    private UUID customerId;
    private Integer restaurantId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        restaurantId = 1;

        customer = new Customer();
        customer.setCustomerId(customerId);

        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(restaurantId);
        restaurant.setRestaurantName("Test Restaurant");
    }

    // ========== toggleFavorite() Tests ==========

    @Test
    @DisplayName("shouldAddFavorite_whenNotExists")
    void shouldAddFavorite_whenNotExists() {
        // Given
        ToggleFavoriteRequest request = new ToggleFavoriteRequest();
        request.setRestaurantId(restaurantId);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(favoriteRepository.existsByCustomerCustomerIdAndRestaurantRestaurantId(customerId, restaurantId))
            .thenReturn(false);
        when(favoriteRepository.save(any(CustomerFavorite.class))).thenReturn(new CustomerFavorite());

        // When
        ToggleFavoriteResponse response = favoriteService.toggleFavorite(customerId, request);

        // Then
        assertNotNull(response);
        assertTrue(response.isFavorited());
        verify(favoriteRepository, times(1)).save(any(CustomerFavorite.class));
    }

    @Test
    @DisplayName("shouldRemoveFavorite_whenExists")
    void shouldRemoveFavorite_whenExists() {
        // Given
        ToggleFavoriteRequest request = new ToggleFavoriteRequest();
        request.setRestaurantId(restaurantId);

        CustomerFavorite favorite = new CustomerFavorite();
        favorite.setCustomer(customer);
        favorite.setRestaurant(restaurant);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(favoriteRepository.existsByCustomerCustomerIdAndRestaurantRestaurantId(customerId, restaurantId))
            .thenReturn(true);
        when(favoriteRepository.findByCustomerCustomerIdAndRestaurantRestaurantId(customerId, restaurantId))
            .thenReturn(Optional.of(favorite));

        // When
        ToggleFavoriteResponse response = favoriteService.toggleFavorite(customerId, request);

        // Then
        assertNotNull(response);
        assertFalse(response.isFavorited());
        verify(favoriteRepository, times(1)).delete(favorite);
    }

    @Test
    @DisplayName("shouldReturnError_whenCustomerNotFound")
    void shouldReturnError_whenCustomerNotFound() {
        // Given
        ToggleFavoriteRequest request = new ToggleFavoriteRequest();
        request.setRestaurantId(restaurantId);

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // When
        ToggleFavoriteResponse response = favoriteService.toggleFavorite(customerId, request);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
    }

    // ========== getFavoriteStatistics() Tests ==========

    @Test
    @DisplayName("shouldGetFavoriteStatistics_successfully")
    void shouldGetFavoriteStatistics_successfully() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);

        // When
        List<FavoriteStatisticsDto> statistics = favoriteService.getFavoriteStatistics(pageable);

        // Then
        assertNotNull(statistics);
    }
}

