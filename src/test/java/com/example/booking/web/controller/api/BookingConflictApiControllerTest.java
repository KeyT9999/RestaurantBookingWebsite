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

    @Test
    @DisplayName("checkBookingConflicts - should handle BookingConflictException")
    void checkBookingConflicts_ShouldHandleBookingConflictException() {
        // Given
        com.example.booking.dto.BookingForm form = new com.example.booking.dto.BookingForm();
        form.setRestaurantId(1);
        java.util.UUID customerId = java.util.UUID.randomUUID();

        com.example.booking.exception.BookingConflictException exception = 
            new com.example.booking.exception.BookingConflictException(
                com.example.booking.exception.BookingConflictException.ConflictType.TABLE_OCCUPIED,
                "Table is occupied"
            );
        
        doThrow(exception).when(conflictService).validateBookingConflicts(any(), eq(customerId));

        // When
        ResponseEntity<?> result = controller.checkBookingConflicts(form, customerId);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    @DisplayName("checkBookingConflicts - should handle general exception")
    void checkBookingConflicts_ShouldHandleGeneralException() {
        // Given
        com.example.booking.dto.BookingForm form = new com.example.booking.dto.BookingForm();
        form.setRestaurantId(1);
        java.util.UUID customerId = java.util.UUID.randomUUID();

        doThrow(new RuntimeException("Service error")).when(conflictService).validateBookingConflicts(any(), eq(customerId));

        // When
        ResponseEntity<?> result = controller.checkBookingConflicts(form, customerId);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    @DisplayName("checkBookingUpdateConflicts - should check successfully")
    void checkBookingUpdateConflicts_ShouldCheckSuccessfully() {
        // Given
        Integer bookingId = 1;
        com.example.booking.dto.BookingForm form = new com.example.booking.dto.BookingForm();
        form.setRestaurantId(1);
        java.util.UUID customerId = java.util.UUID.randomUUID();

        doNothing().when(conflictService).validateBookingUpdateConflicts(eq(bookingId), any(), eq(customerId));

        // When
        ResponseEntity<?> result = controller.checkBookingUpdateConflicts(bookingId, form, customerId);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    @DisplayName("checkBookingUpdateConflicts - should handle BookingConflictException")
    void checkBookingUpdateConflicts_ShouldHandleBookingConflictException() {
        // Given
        Integer bookingId = 1;
        com.example.booking.dto.BookingForm form = new com.example.booking.dto.BookingForm();
        form.setRestaurantId(1);
        java.util.UUID customerId = java.util.UUID.randomUUID();

        com.example.booking.exception.BookingConflictException exception = 
            new com.example.booking.exception.BookingConflictException(
                com.example.booking.exception.BookingConflictException.ConflictType.TIME_OVERLAP,
                "Time overlap detected"
            );
        
        doThrow(exception).when(conflictService).validateBookingUpdateConflicts(eq(bookingId), any(), eq(customerId));

        // When
        ResponseEntity<?> result = controller.checkBookingUpdateConflicts(bookingId, form, customerId);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    @DisplayName("checkBookingUpdateConflicts - should handle general exception")
    void checkBookingUpdateConflicts_ShouldHandleGeneralException() {
        // Given
        Integer bookingId = 1;
        com.example.booking.dto.BookingForm form = new com.example.booking.dto.BookingForm();
        form.setRestaurantId(1);
        java.util.UUID customerId = java.util.UUID.randomUUID();

        doThrow(new RuntimeException("Service error")).when(conflictService).validateBookingUpdateConflicts(eq(bookingId), any(), eq(customerId));

        // When
        ResponseEntity<?> result = controller.checkBookingUpdateConflicts(bookingId, form, customerId);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    @DisplayName("getAvailableTimeSlots - should handle exception")
    void getAvailableTimeSlots_ShouldHandleException() {
        // Given
        Integer tableId = 1;
        String date = "2024-12-25";
        
        when(conflictService.getAvailableTimeSlots(eq(tableId), any(LocalDateTime.class)))
            .thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<?> result = controller.getAvailableTimeSlots(tableId, date);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        
        // Verify ErrorResponse is returned
        BookingConflictApiController.ErrorResponse errorResponse = 
            (BookingConflictApiController.ErrorResponse) result.getBody();
        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getError());
        assertTrue(errorResponse.getError().contains("Error getting available slots"));
    }

    @Test
    @DisplayName("ErrorResponse - should test constructor and getter")
    void errorResponse_ShouldTestConstructorAndGetter() {
        // Test ErrorResponse constructor and getter
        BookingConflictApiController.ErrorResponse response = 
            new BookingConflictApiController.ErrorResponse("Test error message");
        
        assertNotNull(response);
        assertEquals("Test error message", response.getError());
    }

    @Test
    @DisplayName("ConflictCheckResponse - should test constructor with conflictType")
    void conflictCheckResponse_ShouldTestConstructorWithConflictType() {
        // Test ConflictCheckResponse constructor with conflictType
        com.example.booking.exception.BookingConflictException.ConflictType conflictType = 
            com.example.booking.exception.BookingConflictException.ConflictType.TABLE_OCCUPIED;
        
        BookingConflictApiController.ConflictCheckResponse response = 
            new BookingConflictApiController.ConflictCheckResponse(
                false,
                "Conflict message",
                conflictType
            );
        
        assertNotNull(response);
        assertFalse(response.isValid());
        assertEquals("Conflict message", response.getMessage());
        assertEquals(conflictType, response.getConflictType());
    }

    @Test
    @DisplayName("ConflictCheckResponse - should test constructor without conflictType")
    void conflictCheckResponse_ShouldTestConstructorWithoutConflictType() {
        // Test ConflictCheckResponse constructor without conflictType
        BookingConflictApiController.ConflictCheckResponse response = 
            new BookingConflictApiController.ConflictCheckResponse(
                true,
                "No conflicts found"
            );
        
        assertNotNull(response);
        assertTrue(response.isValid());
        assertEquals("No conflicts found", response.getMessage());
        assertNull(response.getConflictType());
    }

    @Test
    @DisplayName("AvailableSlotsResponse - should test constructor and getter")
    void availableSlotsResponse_ShouldTestConstructorAndGetter() {
        // Test AvailableSlotsResponse constructor and getter
        java.util.List<LocalDateTime> slots = java.util.List.of(
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1)
        );
        
        BookingConflictApiController.AvailableSlotsResponse response = 
            new BookingConflictApiController.AvailableSlotsResponse(slots);
        
        assertNotNull(response);
        assertNotNull(response.getSlots());
        assertEquals(2, response.getSlots().size());
    }
}
