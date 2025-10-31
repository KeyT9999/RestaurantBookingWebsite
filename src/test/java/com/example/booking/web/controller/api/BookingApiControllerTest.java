package com.example.booking.web.controller.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

import com.example.booking.domain.Booking;
import com.example.booking.domain.Customer;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantMedia;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.domain.User;
import com.example.booking.dto.BookingDetailsDto;
import com.example.booking.service.BookingService;
import com.example.booking.service.CustomerService;
import com.example.booking.service.RestaurantManagementService;
import com.example.booking.service.SimpleUserService;

import java.util.UUID;

/**
 * Unit tests for BookingApiController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookingApiController Tests")
public class BookingApiControllerTest {

    @Mock
    private RestaurantManagementService restaurantService;

    @Mock
    private BookingService bookingService;

    @Mock
    private CustomerService customerService;

    @Mock
    private SimpleUserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BookingApiController controller;

    private RestaurantProfile restaurant;
    private Booking booking;
    private User user;
    private Customer customer;

    @BeforeEach
    void setUp() {
        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");

        booking = new Booking();
        booking.setBookingId(1);
        booking.setRestaurant(restaurant);

        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");

        customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        customer.setUser(user);
        booking.setCustomer(customer);
    }

    // ========== getTableLayoutsByRestaurant() Tests ==========

    @Test
    @DisplayName("shouldGetTableLayouts_successfully")
    void shouldGetTableLayouts_successfully() {
        // Given
        List<RestaurantMedia> layouts = new ArrayList<>();
        RestaurantMedia layout = new RestaurantMedia();
        layout.setMediaId(1);
        layout.setUrl("http://example.com/layout.jpg");
        layout.setType("table_layout");
        layouts.add(layout);

        when(restaurantService.findMediaByRestaurantAndType(1, "table_layout"))
            .thenReturn(layouts);

        // When
        ResponseEntity<List<Map<String, Object>>> response = 
            controller.getTableLayoutsByRestaurant(1);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("shouldReturnEmptyList_whenNoLayoutsFound")
    void shouldReturnEmptyList_whenNoLayoutsFound() {
        // Given
        when(restaurantService.findMediaByRestaurantAndType(1, "table_layout"))
            .thenReturn(new ArrayList<>());

        // When
        ResponseEntity<List<Map<String, Object>>> response = 
            controller.getTableLayoutsByRestaurant(1);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    // ========== getBookingDetails() Tests ==========

    @Test
    @DisplayName("shouldGetBookingDetails_successfully")
    void shouldGetBookingDetails_successfully() {
        // Given
        when(bookingService.findBookingById(1)).thenReturn(Optional.of(booking));

        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUsername(user.getUsername())).thenReturn(Optional.of(customer));

        // When
        ResponseEntity<?> response = controller.getBookingDetails(1, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("shouldReturn404_whenBookingNotFound")
    void shouldReturn404_whenBookingNotFound() {
        // Given
        when(bookingService.findBookingById(1)).thenReturn(Optional.empty());
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUsername(user.getUsername())).thenReturn(Optional.of(customer));

        // When
        ResponseEntity<?> response = controller.getBookingDetails(1, authentication);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
