package com.example.booking.web.controller.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

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
import com.example.booking.dto.AvailabilityCheckResponse;
import com.example.booking.dto.WaitlistDetailDto;
import com.example.booking.service.SmartWaitlistService;
import com.example.booking.service.WaitlistService;
import com.example.booking.service.CustomerService;

import java.util.Optional;
import java.util.UUID;

/**
 * Unit tests for SmartWaitlistApiController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SmartWaitlistApiController Tests")
public class SmartWaitlistApiControllerTest {

    @Mock
    private SmartWaitlistService smartWaitlistService;

    @Mock
    private WaitlistService waitlistService;

    @Mock
    private CustomerService customerService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SmartWaitlistApiController controller;

    private User user;
    private Customer customer;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");

        customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        customer.setUser(user);
    }

    // ========== checkAvailability() Tests ==========

    @Test
    @DisplayName("shouldCheckAvailability_successfully")
    void shouldCheckAvailability_successfully() {
        // Given
        Integer restaurantId = 1;
        String bookingTime = "2024-12-25T19:00";
        Integer guestCount = 4;

        AvailabilityCheckResponse response = new AvailabilityCheckResponse(false, null);
        when(smartWaitlistService.checkGeneralAvailability(eq(restaurantId), any(LocalDateTime.class), eq(guestCount)))
            .thenReturn(response);

        // When
        ResponseEntity<AvailabilityCheckResponse> result = 
            controller.checkAvailability(restaurantId, bookingTime, guestCount, null);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    @DisplayName("shouldCheckSpecificTables_whenTableIdsProvided")
    void shouldCheckSpecificTables_whenTableIdsProvided() {
        // Given
        Integer restaurantId = 1;
        String bookingTime = "2024-12-25T19:00";
        Integer guestCount = 4;
        String selectedTableIds = "1,2,3";

        AvailabilityCheckResponse response = new AvailabilityCheckResponse(false, null);
        when(smartWaitlistService.checkSpecificTables(eq(selectedTableIds), any(LocalDateTime.class), eq(guestCount)))
            .thenReturn(response);

        // When
        ResponseEntity<AvailabilityCheckResponse> result = 
            controller.checkAvailability(restaurantId, bookingTime, guestCount, selectedTableIds);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    @DisplayName("shouldReturnError_whenInvalidTimeFormat")
    void shouldReturnError_whenInvalidTimeFormat() {
        // Given
        Integer restaurantId = 1;
        String invalidTime = "invalid-time";
        Integer guestCount = 4;

        // When
        ResponseEntity<AvailabilityCheckResponse> result = 
            controller.checkAvailability(restaurantId, invalidTime, guestCount, null);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    // ========== joinWaitlist() Tests ==========

    @Test
    @DisplayName("shouldJoinWaitlist_successfully")
    void shouldJoinWaitlist_successfully() {
        // Given
        SmartWaitlistApiController.JoinWaitlistRequest request = 
            new SmartWaitlistApiController.JoinWaitlistRequest();
        request.restaurantId = 1;
        request.guestCount = 4;
        request.preferredBookingTime = "2024-12-25T19:00";

        com.example.booking.domain.Waitlist waitlist = new com.example.booking.domain.Waitlist();
        waitlist.setWaitlistId(1);

        when(authentication.getPrincipal()).thenReturn(user);
        when(customerService.findByUsername(user.getUsername())).thenReturn(Optional.of(customer));
        when(waitlistService.addToWaitlistWithDetails(eq(1), eq(4), any(UUID.class), any(), any(), any(), any(LocalDateTime.class)))
            .thenReturn(waitlist);

        // When
        ResponseEntity<?> result = controller.joinWaitlist(request, authentication);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }
}

