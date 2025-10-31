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

        when(authentication.getName()).thenReturn(user.getUsername());
        when(customerService.findByUsername(user.getUsername())).thenReturn(Optional.of(customer));
        when(waitlistService.addToWaitlistWithDetails(eq(1), eq(4), any(UUID.class), any(), any(), any(), any(LocalDateTime.class)))
            .thenReturn(waitlist);

        // When
        ResponseEntity<?> result = controller.joinWaitlist(request, authentication);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        
        // Verify JoinWaitlistResponse is created correctly
        SmartWaitlistApiController.JoinWaitlistResponse response = 
            (SmartWaitlistApiController.JoinWaitlistResponse) result.getBody();
        assertNotNull(response);
        assertTrue(response.success);
        assertEquals("Successfully joined waitlist", response.message);
        assertNotNull(response.queuePosition);
        assertNotNull(response.estimatedWaitTime);
        assertNotNull(response.waitlistId);
    }

    @Test
    @DisplayName("joinWaitlist - should test JoinWaitlistResponse constructor")
    void joinWaitlist_ShouldTestJoinWaitlistResponseConstructor() {
        // Test JoinWaitlistResponse constructor directly
        SmartWaitlistApiController.JoinWaitlistResponse response = 
            new SmartWaitlistApiController.JoinWaitlistResponse(
                true,
                "Test message",
                5,
                30,
                123
            );
        
        assertTrue(response.success);
        assertEquals("Test message", response.message);
        assertEquals(5, response.queuePosition);
        assertEquals(30, response.estimatedWaitTime);
        assertEquals(123, response.waitlistId);
    }

    @Test
    @DisplayName("joinWaitlist - should handle null authentication")
    void joinWaitlist_ShouldHandleNullAuthentication() {
        // Given
        SmartWaitlistApiController.JoinWaitlistRequest request = 
            new SmartWaitlistApiController.JoinWaitlistRequest();
        request.restaurantId = 1;
        request.guestCount = 4;

        // When
        ResponseEntity<?> result = controller.joinWaitlist(request, null);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    @DisplayName("joinWaitlist - should handle customer not found")
    void joinWaitlist_ShouldHandleCustomerNotFound() {
        // Given
        SmartWaitlistApiController.JoinWaitlistRequest request = 
            new SmartWaitlistApiController.JoinWaitlistRequest();
        request.restaurantId = 1;
        request.guestCount = 4;

        when(authentication.getName()).thenReturn("nonexistent");
        when(customerService.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> result = controller.joinWaitlist(request, authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    @DisplayName("joinWaitlist - should handle exception")
    void joinWaitlist_ShouldHandleException() {
        // Given
        SmartWaitlistApiController.JoinWaitlistRequest request = 
            new SmartWaitlistApiController.JoinWaitlistRequest();
        request.restaurantId = 1;
        request.guestCount = 4;

        when(authentication.getName()).thenReturn("testuser");
        when(customerService.findByUsername("testuser"))
            .thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<?> result = controller.joinWaitlist(request, authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    @DisplayName("getWaitlistDetails - should get details successfully")
    void getWaitlistDetails_ShouldGetDetailsSuccessfully() {
        // Given
        Integer waitlistId = 1;
        WaitlistDetailDto details = new WaitlistDetailDto();
        
        when(waitlistService.getWaitlistDetails(waitlistId)).thenReturn(details);

        // When
        ResponseEntity<?> result = controller.getWaitlistDetails(waitlistId, authentication);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    @DisplayName("getWaitlistDetails - should handle null authentication")
    void getWaitlistDetails_ShouldHandleNullAuthentication() {
        // Given
        Integer waitlistId = 1;

        // When
        ResponseEntity<?> result = controller.getWaitlistDetails(waitlistId, null);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    @DisplayName("getWaitlistDetails - should handle exception")
    void getWaitlistDetails_ShouldHandleException() {
        // Given
        Integer waitlistId = 1;
        
        when(waitlistService.getWaitlistDetails(waitlistId))
            .thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<?> result = controller.getWaitlistDetails(waitlistId, authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    @DisplayName("checkAvailability - should handle URL encoded time format")
    void checkAvailability_ShouldHandleUrlEncodedTimeFormat() {
        // Given
        Integer restaurantId = 1;
        String bookingTime = "2024-12-25+19:00"; // URL encoded format
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
    @DisplayName("checkAvailability - should handle alternate time format")
    void checkAvailability_ShouldHandleAlternateTimeFormat() {
        // Given
        Integer restaurantId = 1;
        String bookingTime = "2024-12-25 19:00"; // Alternate format
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
    @DisplayName("checkAvailability - should handle empty tableIds")
    void checkAvailability_ShouldHandleEmptyTableIds() {
        // Given
        Integer restaurantId = 1;
        String bookingTime = "2024-12-25T19:00";
        Integer guestCount = 4;
        String selectedTableIds = "   "; // Empty/whitespace

        AvailabilityCheckResponse response = new AvailabilityCheckResponse(false, null);
        when(smartWaitlistService.checkGeneralAvailability(eq(restaurantId), any(LocalDateTime.class), eq(guestCount)))
            .thenReturn(response);

        // When
        ResponseEntity<AvailabilityCheckResponse> result = 
            controller.checkAvailability(restaurantId, bookingTime, guestCount, selectedTableIds);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    @DisplayName("joinWaitlist - should handle null preferredBookingTime")
    void joinWaitlist_ShouldHandleNullPreferredBookingTime() {
        // Given
        SmartWaitlistApiController.JoinWaitlistRequest request = 
            new SmartWaitlistApiController.JoinWaitlistRequest();
        request.restaurantId = 1;
        request.guestCount = 4;
        request.preferredBookingTime = null;

        com.example.booking.domain.Waitlist waitlist = new com.example.booking.domain.Waitlist();
        waitlist.setWaitlistId(1);
        waitlist.setEstimatedWaitTime(30);

        when(authentication.getName()).thenReturn("testuser");
        when(customerService.findByUsername("testuser")).thenReturn(Optional.of(customer));
        when(waitlistService.addToWaitlistWithDetails(eq(1), eq(4), any(UUID.class), any(), any(), any(), any(LocalDateTime.class)))
            .thenReturn(waitlist);

        // When
        ResponseEntity<?> result = controller.joinWaitlist(request, authentication);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    @DisplayName("joinWaitlist - should handle invalid preferredBookingTime format")
    void joinWaitlist_ShouldHandleInvalidPreferredBookingTimeFormat() {
        // Given
        SmartWaitlistApiController.JoinWaitlistRequest request = 
            new SmartWaitlistApiController.JoinWaitlistRequest();
        request.restaurantId = 1;
        request.guestCount = 4;
        request.preferredBookingTime = "invalid-format";

        com.example.booking.domain.Waitlist waitlist = new com.example.booking.domain.Waitlist();
        waitlist.setWaitlistId(1);
        waitlist.setEstimatedWaitTime(30);

        when(authentication.getName()).thenReturn("testuser");
        when(customerService.findByUsername("testuser")).thenReturn(Optional.of(customer));
        when(waitlistService.addToWaitlistWithDetails(eq(1), eq(4), any(UUID.class), any(), any(), any(), any(LocalDateTime.class)))
            .thenReturn(waitlist);

        // When
        ResponseEntity<?> result = controller.joinWaitlist(request, authentication);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }
}

