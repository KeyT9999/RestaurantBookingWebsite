package com.example.booking.web.controller.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
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
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booking.domain.ReviewReportStatus;
import com.example.booking.domain.User;
import com.example.booking.dto.ReviewReportView;
import com.example.booking.service.ReviewReportService;

/**
 * Unit tests for AdminModerationController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminModerationController Tests")
public class AdminModerationControllerTest {

    @Mock
    private ReviewReportService reviewReportService;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private AdminModerationController controller;

    private User admin;
    private Long reportId;

    @BeforeEach
    void setUp() {
        reportId = 1L;
        admin = new User();
        admin.setId(UUID.randomUUID());
    }

    // ========== listReports() Tests ==========

    @Test
    @DisplayName("shouldListReports_successfully")
    void shouldListReports_successfully() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<ReviewReportView> reportPage = mock(Page.class);

        when(reviewReportService.getReportsForAdmin(Optional.empty(), pageable))
            .thenReturn(reportPage);

        // When
        String view = controller.listReports(0, 20, null, model);

        // Then
        assertEquals("admin/moderation", view);
        verify(model, times(1)).addAttribute("reportPage", reportPage);
        verify(model, times(1)).addAttribute("currentPage", 0);
        verify(model, times(1)).addAttribute("pageSize", 20);
    }

    @Test
    @DisplayName("shouldFilterReportsByStatus_successfully")
    void shouldFilterReportsByStatus_successfully() {
        // Given
        ReviewReportStatus status = ReviewReportStatus.PENDING;
        Pageable pageable = PageRequest.of(0, 20);
        Page<ReviewReportView> reportPage = mock(Page.class);

        when(reviewReportService.getReportsForAdmin(Optional.of(status), pageable))
            .thenReturn(reportPage);

        // When
        String view = controller.listReports(0, 20, status, model);

        // Then
        assertEquals("admin/moderation", view);
        verify(model, times(1)).addAttribute("selectedStatus", status);
    }

    // ========== viewReport() Tests ==========

    @Test
    @DisplayName("shouldViewReport_successfully")
    void shouldViewReport_successfully() {
        // Given
        ReviewReportView report = new ReviewReportView();
        when(reviewReportService.getReportView(reportId)).thenReturn(report);

        // When
        String view = controller.viewReport(reportId, model);

        // Then
        assertEquals("admin/moderation-detail", view);
        verify(model, times(1)).addAttribute("report", report);
    }

    // ========== resolveReport() Tests ==========

    @Test
    @DisplayName("shouldResolveReport_successfully")
    void shouldResolveReport_successfully() {
        // Given
        when(authentication.getPrincipal()).thenReturn(admin);
        doNothing().when(reviewReportService).resolveReport(reportId, admin.getId(), "Resolved");

        // When
        String view = controller.resolveReport(reportId, "Resolved", authentication, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/moderation", view);
        verify(reviewReportService, times(1)).resolveReport(reportId, admin.getId(), "Resolved");
        verify(redirectAttributes, times(1)).addFlashAttribute("success", anyString());
    }

    @Test
    @DisplayName("shouldHandleError_whenResolveFails")
    void shouldHandleError_whenResolveFails() {
        // Given
        when(authentication.getPrincipal()).thenReturn(admin);
        doThrow(new RuntimeException("Error")).when(reviewReportService)
            .resolveReport(reportId, admin.getId(), "Resolved");

        // When
        String view = controller.resolveReport(reportId, "Resolved", authentication, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/moderation", view);
        verify(redirectAttributes, times(1)).addFlashAttribute("error", anyString());
    }
}

