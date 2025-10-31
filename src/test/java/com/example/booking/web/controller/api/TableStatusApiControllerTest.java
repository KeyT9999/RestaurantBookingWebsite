package com.example.booking.web.controller.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.service.TableStatusManagementService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TableStatusApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("TableStatusApiController Test Suite")
class TableStatusApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TableStatusManagementService tableStatusService;

    @Nested
    @DisplayName("checkInCustomer() Tests")
    class CheckInCustomerTests {

        @Test
        @WithMockUser(roles = {"RESTAURANT_OWNER", "STAFF"})
        @DisplayName("Should check in customer successfully")
        void shouldCheckInCustomerSuccessfully() throws Exception {
            doNothing().when(tableStatusService).checkInCustomer(1);

            mockMvc.perform(post("/api/staff/table-status/check-in/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Customer checked in successfully"))
                    .andExpect(jsonPath("$.bookingId").value(1));

            verify(tableStatusService).checkInCustomer(1);
        }

        @Test
        @WithMockUser(roles = {"RESTAURANT_OWNER", "STAFF"})
        @DisplayName("Should handle service exception during check-in")
        void shouldHandleServiceExceptionDuringCheckIn() throws Exception {
            doThrow(new RuntimeException("Check-in failed")).when(tableStatusService).checkInCustomer(1);

            mockMvc.perform(post("/api/staff/table-status/check-in/1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @WithMockUser(roles = "CUSTOMER")
        @DisplayName("Should reject customer access")
        void shouldRejectCustomerAccess() throws Exception {
            mockMvc.perform(post("/api/staff/table-status/check-in/1"))
                    .andExpect(status().isForbidden());

            verify(tableStatusService, never()).checkInCustomer(anyInt());
        }
    }

    @Nested
    @DisplayName("checkOutCustomer() Tests")
    class CheckOutCustomerTests {

        @Test
        @WithMockUser(roles = {"RESTAURANT_OWNER", "STAFF"})
        @DisplayName("Should check out customer successfully")
        void shouldCheckOutCustomerSuccessfully() throws Exception {
            doNothing().when(tableStatusService).checkOutCustomer(1);

            mockMvc.perform(post("/api/staff/table-status/check-out/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Customer checked out successfully"))
                    .andExpect(jsonPath("$.bookingId").value(1));

            verify(tableStatusService).checkOutCustomer(1);
        }

        @Test
        @WithMockUser(roles = {"RESTAURANT_OWNER", "STAFF"})
        @DisplayName("Should handle service exception during check-out")
        void shouldHandleServiceExceptionDuringCheckOut() throws Exception {
            doThrow(new RuntimeException("Check-out failed")).when(tableStatusService).checkOutCustomer(1);

            mockMvc.perform(post("/api/staff/table-status/check-out/1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("completeCleaning() Tests")
    class CompleteCleaningTests {

        @Test
        @WithMockUser(roles = {"RESTAURANT_OWNER", "STAFF"})
        @DisplayName("Should complete cleaning successfully")
        void shouldCompleteCleaningSuccessfully() throws Exception {
            doNothing().when(tableStatusService).completeCleaning(1);

            mockMvc.perform(post("/api/staff/table-status/complete-cleaning/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Cleaning completed successfully"))
                    .andExpect(jsonPath("$.tableId").value(1));

            verify(tableStatusService).completeCleaning(1);
        }

        @Test
        @WithMockUser(roles = {"RESTAURANT_OWNER", "STAFF"})
        @DisplayName("Should handle service exception during cleaning")
        void shouldHandleServiceExceptionDuringCleaning() throws Exception {
            doThrow(new RuntimeException("Cleaning failed")).when(tableStatusService).completeCleaning(1);

            mockMvc.perform(post("/api/staff/table-status/complete-cleaning/1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("setTableToMaintenance() Tests")
    class SetTableToMaintenanceTests {

        @Test
        @WithMockUser(roles = {"RESTAURANT_OWNER", "STAFF"})
        @DisplayName("Should set table to maintenance successfully")
        void shouldSetTableToMaintenanceSuccessfully() throws Exception {
            doNothing().when(tableStatusService).setTableToMaintenance(1);

            mockMvc.perform(post("/api/staff/table-status/maintenance/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Table set to maintenance successfully"))
                    .andExpect(jsonPath("$.tableId").value(1));

            verify(tableStatusService).setTableToMaintenance(1);
        }

        @Test
        @WithMockUser(roles = {"RESTAURANT_OWNER", "STAFF"})
        @DisplayName("Should handle service exception during maintenance")
        void shouldHandleServiceExceptionDuringMaintenance() throws Exception {
            doThrow(new RuntimeException("Maintenance failed")).when(tableStatusService).setTableToMaintenance(1);

            mockMvc.perform(post("/api/staff/table-status/maintenance/1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("setTableToAvailable() Tests")
    class SetTableToAvailableTests {

        @Test
        @WithMockUser(roles = {"RESTAURANT_OWNER", "STAFF"})
        @DisplayName("Should set table to available successfully")
        void shouldSetTableToAvailableSuccessfully() throws Exception {
            doNothing().when(tableStatusService).setTableToAvailable(1);

            mockMvc.perform(post("/api/staff/table-status/available/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Table set to available successfully"))
                    .andExpect(jsonPath("$.tableId").value(1));

            verify(tableStatusService).setTableToAvailable(1);
        }

        @Test
        @WithMockUser(roles = {"RESTAURANT_OWNER", "STAFF"})
        @DisplayName("Should handle service exception during set available")
        void shouldHandleServiceExceptionDuringSetAvailable() throws Exception {
            doThrow(new RuntimeException("Set available failed")).when(tableStatusService).setTableToAvailable(1);

            mockMvc.perform(post("/api/staff/table-status/available/1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}

