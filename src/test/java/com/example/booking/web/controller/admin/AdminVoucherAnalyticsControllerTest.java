package com.example.booking.web.controller.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import com.example.booking.service.VoucherService;

/**
 * Unit tests for AdminVoucherAnalyticsController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminVoucherAnalyticsController Tests")
public class AdminVoucherAnalyticsControllerTest {

    @Mock
    private VoucherService voucherService;

    @Mock
    private Model model;

    @InjectMocks
    private AdminVoucherAnalyticsController controller;

    // ========== analytics() Tests ==========

    @Test
    @DisplayName("shouldDisplayAnalytics_successfully")
    void shouldDisplayAnalytics_successfully() {
        // When
        String view = controller.analytics(null, null, null, null, model);

        // Then
        assertEquals("admin/vouchers/analytics", view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }
}

