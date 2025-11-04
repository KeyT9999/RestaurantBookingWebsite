package com.example.booking.web.controller.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.booking.service.TableStatusManagementService;

/**
 * Unit tests for TableStatusApiController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TableStatusApiController Tests")
public class TableStatusApiControllerTest {

    @Mock
    private TableStatusManagementService tableStatusService;

    @InjectMocks
    private TableStatusApiController controller;

    // ========== checkInCustomer() Tests ==========

    @Test
    @DisplayName("shouldCheckInCustomer_successfully")
    void shouldCheckInCustomer_successfully() {
        // Given
        Integer bookingId = 1;
        doNothing().when(tableStatusService).checkInCustomer(bookingId);

        // When
        ResponseEntity<Map<String, Object>> response = controller.checkInCustomer(bookingId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("success"));
        verify(tableStatusService, times(1)).checkInCustomer(bookingId);
    }

    @Test
    @DisplayName("shouldReturnError_whenCheckInFails")
    void shouldReturnError_whenCheckInFails() {
        // Given
        Integer bookingId = 1;
        doThrow(new RuntimeException("Booking not found")).when(tableStatusService).checkInCustomer(bookingId);

        // When
        ResponseEntity<Map<String, Object>> response = controller.checkInCustomer(bookingId);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse((Boolean) response.getBody().get("success"));
    }

    // ========== checkOutCustomer() Tests ==========

    @Test
    @DisplayName("shouldCheckOutCustomer_successfully")
    void shouldCheckOutCustomer_successfully() {
        // Given
        Integer bookingId = 1;
        doNothing().when(tableStatusService).checkOutCustomer(bookingId);

        // When
        ResponseEntity<Map<String, Object>> response = controller.checkOutCustomer(bookingId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        verify(tableStatusService, times(1)).checkOutCustomer(bookingId);
    }

    // ========== completeCleaning() Tests ==========

    @Test
    @DisplayName("shouldCompleteCleaning_successfully")
    void shouldCompleteCleaning_successfully() {
        // Given
        Integer tableId = 1;
        doNothing().when(tableStatusService).completeCleaning(tableId);

        // When
        ResponseEntity<Map<String, Object>> response = controller.completeCleaning(tableId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        verify(tableStatusService, times(1)).completeCleaning(tableId);
    }

    // ========== setTableToMaintenance() Tests ==========

    @Test
    @DisplayName("shouldSetTableToMaintenance_successfully")
    void shouldSetTableToMaintenance_successfully() {
        // Given
        Integer tableId = 1;
        doNothing().when(tableStatusService).setTableToMaintenance(tableId);

        // When
        ResponseEntity<Map<String, Object>> response = controller.setTableToMaintenance(tableId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        verify(tableStatusService, times(1)).setTableToMaintenance(tableId);
    }

    // ========== setTableToAvailable() Tests ==========

    @Test
    @DisplayName("shouldSetTableToAvailable_successfully")
    void shouldSetTableToAvailable_successfully() {
        // Given
        Integer tableId = 1;
        doNothing().when(tableStatusService).setTableToAvailable(tableId);

        // When
        ResponseEntity<Map<String, Object>> response = controller.setTableToAvailable(tableId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        verify(tableStatusService, times(1)).setTableToAvailable(tableId);
    }
}

