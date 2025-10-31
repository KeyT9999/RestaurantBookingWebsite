package com.example.booking.web.controller.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
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
import com.example.booking.domain.RestaurantService;
import com.example.booking.domain.User;
import com.example.booking.dto.BookingDetailsDto;
import com.example.booking.service.BookingService;
import com.example.booking.service.CustomerService;
import com.example.booking.service.RestaurantManagementService;
import com.example.booking.service.SimpleUserService;

import java.util.UUID;
import java.util.Arrays;
import java.math.BigDecimal;

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

    @Test
    @DisplayName("shouldReturn403_whenBookingDoesNotBelongToCustomer")
    void shouldReturn403_whenBookingDoesNotBelongToCustomer() {
        // Given
        Customer otherCustomer = new Customer();
        otherCustomer.setCustomerId(UUID.randomUUID());
        booking.setCustomer(otherCustomer);

        when(bookingService.findBookingById(1)).thenReturn(Optional.of(booking));
        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUsername(user.getUsername())).thenReturn(Optional.of(customer));

        // When
        ResponseEntity<?> response = controller.getBookingDetails(1, authentication);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("shouldHandleError_whenGetTableLayoutsFails")
    void shouldHandleError_whenGetTableLayoutsFails() {
        // Given
        when(restaurantService.findMediaByRestaurantAndType(1, "table_layout"))
            .thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<List<Map<String, Object>>> response = 
            controller.getTableLayoutsByRestaurant(1);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ========== getTablesByRestaurant() Tests ==========

    @Test
    @DisplayName("shouldGetTablesByRestaurant_successfully")
    void shouldGetTablesByRestaurant_successfully() {
        // Given
        RestaurantTable table = new RestaurantTable();
        table.setTableId(1);
        table.setTableName("Table 1");
        table.setCapacity(4);
        table.setRestaurant(restaurant);

        List<RestaurantTable> tables = Arrays.asList(table);
        when(restaurantService.findTablesByRestaurant(1)).thenReturn(tables);

        // When
        ResponseEntity<List<Map<String, Object>>> response = 
            controller.getTablesByRestaurant(1);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("shouldReturnEmptyList_whenNoTablesFound")
    void shouldReturnEmptyList_whenNoTablesFound() {
        // Given
        when(restaurantService.findTablesByRestaurant(1)).thenReturn(new ArrayList<>());

        // When
        ResponseEntity<List<Map<String, Object>>> response = 
            controller.getTablesByRestaurant(1);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    @DisplayName("shouldHandleError_whenGetTablesFails")
    void shouldHandleError_whenGetTablesFails() {
        // Given
        when(restaurantService.findTablesByRestaurant(1))
            .thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<List<Map<String, Object>>> response = 
            controller.getTablesByRestaurant(1);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ========== getRestaurant() Tests ==========

    @Test
    @DisplayName("shouldGetRestaurant_successfully")
    void shouldGetRestaurant_successfully() {
        // Given
        when(restaurantService.findRestaurantById(1)).thenReturn(Optional.of(restaurant));

        // When
        ResponseEntity<?> response = controller.getRestaurant(1);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("shouldReturn404_whenRestaurantNotFound")
    void shouldReturn404_whenRestaurantNotFound() {
        // Given
        when(restaurantService.findRestaurantById(999)).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = controller.getRestaurant(999);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("shouldHandleError_whenGetRestaurantFails")
    void shouldHandleError_whenGetRestaurantFails() {
        // Given
        when(restaurantService.findRestaurantById(1))
            .thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<?> response = controller.getRestaurant(1);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ========== getAllRestaurants() Tests ==========

    @Test
    @DisplayName("shouldGetAllRestaurants_successfully")
    void shouldGetAllRestaurants_successfully() {
        // Given
        List<RestaurantProfile> restaurants = Arrays.asList(restaurant);
        when(restaurantService.findAllRestaurants()).thenReturn(restaurants);

        // When
        ResponseEntity<?> response = controller.getAllRestaurants();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("shouldReturnEmptyList_whenNoRestaurants")
    void shouldReturnEmptyList_whenNoRestaurants() {
        // Given
        when(restaurantService.findAllRestaurants()).thenReturn(new ArrayList<>());

        // When
        ResponseEntity<?> response = controller.getAllRestaurants();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("shouldHandleError_whenGetAllRestaurantsFails")
    void shouldHandleError_whenGetAllRestaurantsFails() {
        // Given
        when(restaurantService.findAllRestaurants())
            .thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<?> response = controller.getAllRestaurants();

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ========== getNearbyRestaurants() Tests ==========

    @Test
    @DisplayName("shouldGetNearbyRestaurants_successfully")
    void shouldGetNearbyRestaurants_successfully() {
        // Given
        restaurant.setAddress("Ho Chi Minh City");
        List<RestaurantProfile> restaurants = Arrays.asList(restaurant);
        when(restaurantService.findAllRestaurants()).thenReturn(restaurants);

        // When
        ResponseEntity<?> response = controller.getNearbyRestaurants(10.77, 106.70, 3000, 10);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("shouldHandleError_whenGetNearbyRestaurantsFails")
    void shouldHandleError_whenGetNearbyRestaurantsFails() {
        // Given
        when(restaurantService.findAllRestaurants())
            .thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<?> response = controller.getNearbyRestaurants(10.77, 106.70, 3000, 10);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ========== getDishesByRestaurant() Tests ==========

    @Test
    @DisplayName("shouldGetDishesByRestaurant_successfully")
    void shouldGetDishesByRestaurant_successfully() {
        // Given
        List<com.example.booking.dto.DishWithImageDto> dishes = new ArrayList<>();
        when(restaurantService.getDishesByRestaurantWithImages(1)).thenReturn(dishes);

        // When
        ResponseEntity<?> response = controller.getDishesByRestaurant(1);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("shouldHandleError_whenGetDishesFails")
    void shouldHandleError_whenGetDishesFails() {
        // Given
        when(restaurantService.getDishesByRestaurantWithImages(1))
            .thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<?> response = controller.getDishesByRestaurant(1);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ========== getServicesByRestaurant() Tests ==========

    @Test
    @DisplayName("shouldGetServicesByRestaurant_successfully")
    void shouldGetServicesByRestaurant_successfully() {
        // Given
        RestaurantService service = new RestaurantService();
        service.setServiceId(1);
        service.setName("Service 1");
        service.setRestaurant(restaurant);
        List<RestaurantService> services = Arrays.asList(service);
        when(restaurantService.findServicesByRestaurant(1)).thenReturn(services);

        // When
        ResponseEntity<?> response = controller.getServicesByRestaurant(1);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("shouldHandleError_whenGetServicesFails")
    void shouldHandleError_whenGetServicesFails() {
        // Given
        when(restaurantService.findServicesByRestaurant(1))
            .thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<?> response = controller.getServicesByRestaurant(1);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
