package com.example.booking.web.controller.customer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.example.booking.domain.Customer;
import com.example.booking.domain.User;
import com.example.booking.dto.customer.ToggleFavoriteRequest;
import com.example.booking.dto.customer.ToggleFavoriteResponse;
import com.example.booking.service.CustomerService;
import com.example.booking.service.FavoriteService;

/**
 * Unit tests for FavoriteController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FavoriteController Tests")
public class FavoriteControllerTest {

    @Mock
    private FavoriteService favoriteService;

    @Mock
    private CustomerService customerService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private FavoriteController controller;

    private User user;
    private Customer customer;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());

        customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        customer.setUser(user);
    }

    // ========== toggleFavorite() Tests ==========

    @Test
    @DisplayName("shouldToggleFavorite_successfully")
    void shouldToggleFavorite_successfully() {
        // Given
        ToggleFavoriteRequest request = new ToggleFavoriteRequest();
        request.setRestaurantId(1);

        ToggleFavoriteResponse response = new ToggleFavoriteResponse();
        response.setFavorited(true);
        response.setMessage("Added to favorites");

        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.of(customer));
        when(favoriteService.toggleFavorite(customer.getCustomerId(), request))
                .thenReturn(response);

        // When
        ResponseEntity<ToggleFavoriteResponse> result = controller.toggleFavorite(request, authentication);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isFavorited());
    }

    @Test
    @DisplayName("shouldReturnError_whenCustomerNotFound")
    void shouldReturnError_whenCustomerNotFound() {
        // Given
        ToggleFavoriteRequest request = new ToggleFavoriteRequest();
        request.setRestaurantId(1);

        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUserId(user.getId())).thenReturn(Optional.empty());

        // When
        ResponseEntity<ToggleFavoriteResponse> result = controller.toggleFavorite(request, authentication);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }
}
