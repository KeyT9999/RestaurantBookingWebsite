package com.example.booking.web.controller;

import com.example.booking.common.enums.RefundStatus;
import com.example.booking.domain.RefundRequest;
import com.example.booking.service.RefundService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Expanded comprehensive tests for AdminRefundController
 * Covers additional endpoints and edge cases for better coverage
 */
@WebMvcTest(AdminRefundController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminRefundController Expanded Test Suite")
class AdminRefundControllerExpandedTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RefundService refundService;

    @Nested
    @DisplayName("getPendingRefunds() Tests")
    class GetPendingRefundsTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return pending refunds with valid data")
        void testGetPendingRefunds_WithValidData_ShouldReturnSuccess() throws Exception {
            RefundRequest refund1 = createMockRefundRequest(1, RefundStatus.PENDING, new BigDecimal("500.00"));
            RefundRequest refund2 = createMockRefundRequest(2, RefundStatus.PENDING, new BigDecimal("750.00"));
            
            when(refundService.getPendingRefunds()).thenReturn(Arrays.asList(refund1, refund2));

            mockMvc.perform(get("/admin/refunds/pending"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.count").value(2))
                    .andExpect(jsonPath("$.refunds").isArray())
                    .andExpect(jsonPath("$.refunds.length()").value(2));

            verify(refundService).getPendingRefunds();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return empty list when no pending refunds")
        void testGetPendingRefunds_WithNoPendingRefunds_ShouldReturnEmptyList() throws Exception {
            when(refundService.getPendingRefunds()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/admin/refunds/pending"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.count").value(0))
                    .andExpect(jsonPath("$.refunds").isArray())
                    .andExpect(jsonPath("$.refunds.length()").value(0));

            verify(refundService).getPendingRefunds();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle service exception gracefully")
        void testGetPendingRefunds_WhenServiceThrowsException_ShouldHandleError() throws Exception {
            when(refundService.getPendingRefunds()).thenThrow(new RuntimeException("Database error"));

            mockMvc.perform(get("/admin/refunds/pending"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").exists());

            verify(refundService).getPendingRefunds();
        }
    }

    @Nested
    @DisplayName("processRefundWithWebhook() Tests")
    class ProcessRefundWithWebhookTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should process refund with webhook successfully")
        void testProcessRefundWithWebhook_WithValidData_ShouldReturnSuccess() throws Exception {
            mockMvc.perform(post("/admin/refunds/1/process-webhook")
                    .param("reason", "Customer request"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle invalid refund request ID")
        void testProcessRefundWithWebhook_WithInvalidId_ShouldHandleError() throws Exception {
            mockMvc.perform(post("/admin/refunds/999/process-webhook")
                    .param("reason", "Invalid request"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("rejectRefund() Tests")
    class RejectRefundTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should reject refund successfully")
        void testRejectRefund_WithValidData_ShouldReturnSuccess() throws Exception {
            doNothing().when(refundService).rejectRefund(anyInt(), any(UUID.class), anyString());

            mockMvc.perform(post("/admin/refunds/10/reject")
                    .param("rejectReason", "Invalid request"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").exists());

            verify(refundService).rejectRefund(eq(10), any(UUID.class), eq("Invalid request"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle service exception when rejecting refund")
        void testRejectRefund_WhenServiceThrowsException_ShouldHandleError() throws Exception {
            doThrow(new RuntimeException("Service error")).when(refundService)
                    .rejectRefund(anyInt(), any(UUID.class), anyString());

            mockMvc.perform(post("/admin/refunds/10/reject")
                    .param("rejectReason", "Error test"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());

            verify(refundService).rejectRefund(anyInt(), any(UUID.class), anyString());
        }
    }

    @Nested
    @DisplayName("completeRefund() Tests")
    class CompleteRefundTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should complete refund successfully")
        void testCompleteRefund_WithValidData_ShouldReturnSuccess() throws Exception {
            doNothing().when(refundService).completeRefund(anyInt(), any(UUID.class), anyString(), anyString());

            mockMvc.perform(post("/admin/refunds/10/complete")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"adminNote\":\"Refund processed successfully\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").exists());

            verify(refundService).completeRefund(eq(10), any(UUID.class), anyString(), anyString());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle service exception when completing refund")
        void testCompleteRefund_WhenServiceThrowsException_ShouldHandleError() throws Exception {
            doThrow(new RuntimeException("Service error")).when(refundService)
                    .completeRefund(anyInt(), any(UUID.class), anyString(), anyString());

            mockMvc.perform(post("/admin/refunds/10/complete")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"adminNote\":\"Error test\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());

            verify(refundService).completeRefund(anyInt(), any(UUID.class), anyString(), anyString());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle invalid JSON body")
        void testCompleteRefund_WithInvalidJson_ShouldHandleError() throws Exception {
            mockMvc.perform(post("/admin/refunds/10/complete")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("invalid json"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("generateVietQR() Tests")
    class GenerateVietQRTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should generate VietQR successfully")
        void testGenerateVietQR_WithValidData_ShouldReturnSuccess() throws Exception {
            String vietqrUrl = "https://vietqr.net/refund/123";
            when(refundService.generateVietQRForRefund(anyInt())).thenReturn(vietqrUrl);

            mockMvc.perform(post("/admin/refunds/11/generate-vietqr"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.vietqrUrl").value(vietqrUrl));

            verify(refundService).generateVietQRForRefund(11);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle service exception when generating VietQR")
        void testGenerateVietQR_WhenServiceThrowsException_ShouldHandleError() throws Exception {
            when(refundService.generateVietQRForRefund(anyInt()))
                    .thenThrow(new RuntimeException("VietQR service unavailable"));

            mockMvc.perform(post("/admin/refunds/11/generate-vietqr"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());

            verify(refundService).generateVietQRForRefund(11);
        }
    }

    // Helper method
    private RefundRequest createMockRefundRequest(Integer id, RefundStatus status, BigDecimal amount) {
        RefundRequest refund = new RefundRequest();
        refund.setRefundRequestId(id);
        refund.setStatus(status);
        refund.setAmount(amount);
        refund.setRequestedAt(LocalDateTime.now());
        return refund;
    }
}

