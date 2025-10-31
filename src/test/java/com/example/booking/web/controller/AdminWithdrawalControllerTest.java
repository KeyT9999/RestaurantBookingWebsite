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
}
