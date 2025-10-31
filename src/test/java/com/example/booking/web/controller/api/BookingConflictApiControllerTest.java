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

import com.example.booking.dto.AvailabilityCheckResponse;
import com.example.booking.service.BookingConflictService;

/**
 * Unit tests for BookingConflictApiController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookingConflictApiController Tests")
public class BookingConflictApiControllerTest {

    @Mock
    private BookingConflictService conflictService;

    @InjectMocks
    private BookingConflictApiController controller;

    // ========== checkBookingConflicts() Tests ==========

    @Test
    @DisplayName("shouldCheckBookingConflicts_successfully")
    void shouldCheckBookingConflicts_successfully() {
        // Given
        com.example.booking.dto.BookingForm form = new com.example.booking.dto.BookingForm();
        form.setRestaurantId(1);
        java.util.UUID customerId = java.util.UUID.randomUUID();

        doNothing().when(conflictService).validateBookingConflicts(any(), eq(customerId));

        // When
        ResponseEntity<?> result = controller.checkBookingConflicts(form, customerId);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    // ========== getAvailableTimeSlots() Tests ==========

    @Test
    @DisplayName("shouldGetAvailableTimeSlots_successfully")
    void shouldGetAvailableTimeSlots_successfully() {
        // Given
        Integer tableId = 1;
        String date = "2024-12-25";
        when(conflictService.getAvailableTimeSlots(eq(tableId), any(LocalDateTime.class)))
            .thenReturn(java.util.List.of());

        // When
        ResponseEntity<?> result = controller.getAvailableTimeSlots(tableId, date);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }
}
