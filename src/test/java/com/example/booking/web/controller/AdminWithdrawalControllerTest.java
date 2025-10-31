package com.example.booking.web.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booking.common.enums.WithdrawalStatus;
import com.example.booking.domain.WithdrawalRequest;
import com.example.booking.service.WithdrawalService;

/**
 * Unit tests for AdminWithdrawalController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminWithdrawalController Tests")
public class AdminWithdrawalControllerTest {

    @Mock
    private WithdrawalService withdrawalService;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private AdminWithdrawalController controller;

    private WithdrawalRequest withdrawalRequest;

    @BeforeEach
    void setUp() {
        withdrawalRequest = new WithdrawalRequest();
        withdrawalRequest.setRequestId(1);
        withdrawalRequest.setAmount(new BigDecimal("1000000"));
        withdrawalRequest.setStatus(WithdrawalStatus.PENDING);
    }

    // ========== listWithdrawals() Tests ==========

    @Test
    @DisplayName("shouldListWithdrawals_successfully")
    void shouldListWithdrawals_successfully() {
        // Given
        List<com.example.booking.dto.payout.WithdrawalRequestDto> withdrawals = new ArrayList<>();
        com.example.booking.dto.payout.WithdrawalRequestDto dto = new com.example.booking.dto.payout.WithdrawalRequestDto();
        withdrawals.add(dto);

        Page<com.example.booking.dto.payout.WithdrawalRequestDto> withdrawalPage = new org.springframework.data.domain.PageImpl<>(
                withdrawals);
        when(withdrawalService.getAllWithdrawals(any())).thenReturn(withdrawalPage);
        when(withdrawalService.getWithdrawalStats()).thenReturn(new com.example.booking.dto.admin.WithdrawalStatsDto());

        // When
        String view = controller.withdrawalManagement(null, model);

        // Then
        assertEquals("admin/withdrawal-management", view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    // ========== approveWithdrawal() Tests ==========

    @Test
    @DisplayName("shouldApproveWithdrawal_successfully")
    void shouldApproveWithdrawal_successfully() {
        // Given
        when(withdrawalService.approveWithdrawal(eq(1), any(UUID.class), anyString()))
                .thenReturn(new com.example.booking.dto.payout.WithdrawalRequestDto());

        // When
        String view = controller.approveWithdrawal(1, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/withdrawal?status=PENDING", view);
        verify(withdrawalService, times(1)).approveWithdrawal(eq(1), any(UUID.class), anyString());
    }

    // ========== rejectWithdrawal() Tests ==========

    @Test
    @DisplayName("shouldRejectWithdrawal_successfully")
    void shouldRejectWithdrawal_successfully() {
        // Given
        String reason = "Invalid bank account";
        when(withdrawalService.rejectWithdrawal(eq(1), any(UUID.class), eq(reason)))
                .thenReturn(new com.example.booking.dto.payout.WithdrawalRequestDto());

        // When
        String view = controller.rejectWithdrawal(1, reason, null, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/withdrawal?status=REJECTED", view);
        verify(withdrawalService, times(1)).rejectWithdrawal(eq(1), any(UUID.class), eq(reason));
    }

    @Test
    @DisplayName("shouldRejectWithdrawal_withNote_successfully")
    void shouldRejectWithdrawal_withNote_successfully() {
        // Given
        String reason = "Invalid bank account";
        String note = "Additional note";
        String fullReason = reason + " - " + note;
        when(withdrawalService.rejectWithdrawal(eq(1), any(UUID.class), eq(fullReason)))
                .thenReturn(new com.example.booking.dto.payout.WithdrawalRequestDto());

        // When
        String view = controller.rejectWithdrawal(1, reason, note, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/withdrawal?status=REJECTED", view);
        verify(withdrawalService, times(1)).rejectWithdrawal(eq(1), any(UUID.class), eq(fullReason));
    }

    @Test
    @DisplayName("shouldRejectWithdrawal_handleException")
    void shouldRejectWithdrawal_handleException() {
        // Given
        String reason = "Invalid bank account";
        when(withdrawalService.rejectWithdrawal(eq(1), any(UUID.class), eq(reason)))
                .thenThrow(new RuntimeException("Database error"));

        // When
        String view = controller.rejectWithdrawal(1, reason, null, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/withdrawal?status=REJECTED", view);
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
    }

    @Test
    @DisplayName("shouldApproveWithdrawal_handleException")
    void shouldApproveWithdrawal_handleException() {
        // Given
        when(withdrawalService.approveWithdrawal(eq(1), any(UUID.class), anyString()))
                .thenThrow(new RuntimeException("Database error"));

        // When
        String view = controller.approveWithdrawal(1, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/withdrawal?status=PENDING", view);
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
    }

    @Test
    @DisplayName("shouldListWithdrawals_withStatusFilter")
    void shouldListWithdrawals_withStatusFilter() {
        // Given
        List<com.example.booking.dto.payout.WithdrawalRequestDto> withdrawals = new ArrayList<>();
        when(withdrawalService.getWithdrawalsByStatus(com.example.booking.common.enums.WithdrawalStatus.PENDING))
                .thenReturn(withdrawals);
        when(withdrawalService.getWithdrawalStats()).thenReturn(new com.example.booking.dto.admin.WithdrawalStatsDto());

        // When
        String view = controller.withdrawalManagement("PENDING", model);

        // Then
        assertEquals("admin/withdrawal-management", view);
        verify(model).addAttribute("filter", "PENDING");
    }

    @Test
    @DisplayName("shouldListWithdrawals_withAllStatus")
    void shouldListWithdrawals_withAllStatus() {
        // Given
        List<com.example.booking.dto.payout.WithdrawalRequestDto> withdrawals = new ArrayList<>();
        when(withdrawalService.getAllWithdrawals(any()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(withdrawals));
        when(withdrawalService.getWithdrawalStats()).thenReturn(new com.example.booking.dto.admin.WithdrawalStatsDto());

        // When
        String view = controller.withdrawalManagement("ALL", model);

        // Then
        assertEquals("admin/withdrawal-management", view);
        verify(model).addAttribute("filter", "ALL");
    }

    @Test
    @DisplayName("shouldListWithdrawals_handleException")
    void shouldListWithdrawals_handleException() {
        // Given
        when(withdrawalService.getWithdrawalStats())
                .thenThrow(new RuntimeException("Database error"));

        // When
        String view = controller.withdrawalManagement(null, model);

        // Then
        assertEquals("admin/withdrawal-management", view);
        verify(model).addAttribute(eq("error"), anyString());
    }

    @Test
    @DisplayName("shouldMarkWithdrawalPaid_successfully")
    void shouldMarkWithdrawalPaid_successfully() {
        // Given
        doNothing().when(withdrawalService).markWithdrawalPaid(eq(1), any(UUID.class), any());

        // When
        String view = controller.markWithdrawalPaid(1, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/withdrawal?status=SUCCEEDED", view);
        verify(withdrawalService, times(1)).markWithdrawalPaid(eq(1), any(UUID.class), any());
    }

    @Test
    @DisplayName("shouldMarkWithdrawalPaid_handleException")
    void shouldMarkWithdrawalPaid_handleException() {
        // Given
        doThrow(new RuntimeException("Database error"))
                .when(withdrawalService).markWithdrawalPaid(eq(1), any(UUID.class), any());

        // When
        String view = controller.markWithdrawalPaid(1, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/withdrawal?status=SUCCEEDED", view);
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
    }
}
