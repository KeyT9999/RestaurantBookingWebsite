package com.example.booking.web.controller.api;

import com.example.booking.dto.BookingForm;
import com.example.booking.exception.BookingConflictException;
import com.example.booking.service.BookingConflictService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Expanded comprehensive tests for BookingConflictApiController
 */
@WebMvcTest(BookingConflictApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("BookingConflictApiController Expanded Test Suite")
class BookingConflictApiControllerExpandedTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingConflictService conflictService;

    @Nested
    @DisplayName("checkBookingConflicts() Tests")
    class CheckBookingConflictsTests {

        @Test
        @DisplayName("Should return valid when no conflicts exist")
        void testCheckBookingConflicts_WithNoConflicts_ShouldReturnValid() throws Exception {
            UUID customerId = UUID.randomUUID();
            doNothing().when(conflictService).validateBookingConflicts(any(BookingForm.class), eq(customerId));

            mockMvc.perform(post("/api/booking/conflicts/check")
                    .param("customerId", customerId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valid").value(true))
                    .andExpect(jsonPath("$.message").exists());

            verify(conflictService).validateBookingConflicts(any(BookingForm.class), eq(customerId));
        }

        @Test
        @DisplayName("Should return invalid when table not available")
        void testCheckBookingConflicts_WithTableNotAvailable_ShouldReturnInvalid() throws Exception {
            UUID customerId = UUID.randomUUID();
            doThrow(new BookingConflictException(
                    BookingConflictException.ConflictType.TABLE_NOT_AVAILABLE, "Table is not available"))
                    .when(conflictService).validateBookingConflicts(any(BookingForm.class), eq(customerId));

            mockMvc.perform(post("/api/booking/conflicts/check")
                    .param("customerId", customerId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.valid").value(false))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.conflictType").exists());

            verify(conflictService).validateBookingConflicts(any(BookingForm.class), eq(customerId));
        }

        @Test
        @DisplayName("Should handle general exception")
        void testCheckBookingConflicts_WhenServiceThrowsException_ShouldHandleError() throws Exception {
            UUID customerId = UUID.randomUUID();
            doThrow(new RuntimeException("Service error"))
                    .when(conflictService).validateBookingConflicts(any(BookingForm.class), eq(customerId));

            mockMvc.perform(post("/api/booking/conflicts/check")
                    .param("customerId", customerId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.valid").value(false))
                    .andExpect(jsonPath("$.message").exists());

            verify(conflictService).validateBookingConflicts(any(BookingForm.class), eq(customerId));
        }
    }

    @Nested
    @DisplayName("checkBookingUpdateConflicts() Tests")
    class CheckBookingUpdateConflictsTests {

        @Test
        @DisplayName("Should return valid when no conflicts for update")
        void testCheckBookingUpdateConflicts_WithNoConflicts_ShouldReturnValid() throws Exception {
            Integer bookingId = 1;
            UUID customerId = UUID.randomUUID();
            doNothing().when(conflictService)
                    .validateBookingUpdateConflicts(eq(bookingId), any(BookingForm.class), eq(customerId));

            mockMvc.perform(post("/api/booking/conflicts/check-update/1")
                    .param("customerId", customerId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valid").value(true));

            verify(conflictService).validateBookingUpdateConflicts(eq(bookingId), any(BookingForm.class), eq(customerId));
        }

        @Test
        @DisplayName("Should return invalid when conflict exists for update")
        void testCheckBookingUpdateConflicts_WithConflict_ShouldReturnInvalid() throws Exception {
            Integer bookingId = 1;
            UUID customerId = UUID.randomUUID();
            doThrow(new BookingConflictException(
                    BookingConflictException.ConflictType.TIME_OVERLAP, "Time overlap detected"))
                    .when(conflictService).validateBookingUpdateConflicts(eq(bookingId), any(BookingForm.class), eq(customerId));

            mockMvc.perform(post("/api/booking/conflicts/check-update/1")
                    .param("customerId", customerId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.valid").value(false))
                    .andExpect(jsonPath("$.conflictType").exists());

            verify(conflictService).validateBookingUpdateConflicts(eq(bookingId), any(BookingForm.class), eq(customerId));
        }
    }

    @Nested
    @DisplayName("getAvailableTimeSlots() Tests")
    class GetAvailableTimeSlotsTests {

        @Test
        @DisplayName("Should return available time slots successfully")
        void testGetAvailableTimeSlots_WithValidDate_ShouldReturnSlots() throws Exception {
            Integer tableId = 1;
            List<LocalDateTime> slots = List.of(
                    LocalDateTime.parse("2024-10-10T10:00:00"),
                    LocalDateTime.parse("2024-10-10T12:00:00"),
                    LocalDateTime.parse("2024-10-10T14:00:00")
            );

            when(conflictService.getAvailableTimeSlots(eq(tableId), any(LocalDateTime.class)))
                    .thenReturn(slots);

            mockMvc.perform(get("/api/booking/conflicts/available-slots/1")
                    .param("date", "2024-10-10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.slots").isArray())
                    .andExpect(jsonPath("$.slots.length()").value(3));

            verify(conflictService).getAvailableTimeSlots(eq(tableId), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Should return empty list when no slots available")
        void testGetAvailableTimeSlots_WithNoSlots_ShouldReturnEmptyList() throws Exception {
            Integer tableId = 1;
            when(conflictService.getAvailableTimeSlots(eq(tableId), any(LocalDateTime.class)))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/booking/conflicts/available-slots/1")
                    .param("date", "2024-10-10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.slots").isArray())
                    .andExpect(jsonPath("$.slots").isEmpty());

            verify(conflictService).getAvailableTimeSlots(eq(tableId), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Should handle invalid date format")
        void testGetAvailableTimeSlots_WithInvalidDate_ShouldHandleError() throws Exception {
            mockMvc.perform(get("/api/booking/conflicts/available-slots/1")
                    .param("date", "invalid-date"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("Should handle exception when getting slots")
        void testGetAvailableTimeSlots_WhenServiceThrowsException_ShouldHandleError() throws Exception {
            when(conflictService.getAvailableTimeSlots(anyInt(), any(LocalDateTime.class)))
                    .thenThrow(new RuntimeException("Database error"));

            mockMvc.perform(get("/api/booking/conflicts/available-slots/1")
                    .param("date", "2024-10-10"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());

            verify(conflictService).getAvailableTimeSlots(anyInt(), any(LocalDateTime.class));
        }
    }
}

