package com.example.booking.web.controller.admin;

import com.example.booking.domain.ReviewReportStatus;
import com.example.booking.dto.ReviewReportView;
import com.example.booking.service.ReviewReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive tests for AdminModerationController
 * 
 * Coverage Target: ≥80% Branch Coverage
 * 
 * Branches to cover:
 * - listReports(): status != null vs status == null
 * - viewReport(): happy path
 * - resolveReport(): authentication != null && principal instanceof User vs null/not User
 * - resolveReport(): try-catch (success vs exception)
 * - rejectReport(): authentication != null && principal instanceof User vs null/not User
 * - rejectReport(): try-catch (success vs exception)
 * 
 * @author Senior SDET
 */
@WebMvcTest(AdminModerationController.class)
@DisplayName("AdminModerationController Tests")
class AdminModerationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewReportService reviewReportService;

    private ReviewReportView createMockReportView(Long id) {
        ReviewReportView view = new ReviewReportView();
        view.setReportId(id);
        view.setStatus(ReviewReportStatus.PENDING);
        return view;
    }

    @Nested
    @DisplayName("listReports() - GET /admin/moderation")
    class ListReportsTests {

        @Test
        @DisplayName("Should return reports list with default parameters - Happy path (null status)")
        // Branch: status == null (default parameter)
        void listReports_WithNullStatus_ReturnsReportsList() throws Exception {
            // Given
            List<ReviewReportView> reports = new ArrayList<>();
            reports.add(createMockReportView(1L));
            Page<ReviewReportView> reportPage = new PageImpl<>(reports, PageRequest.of(0, 20), reports.size());
            
            when(reviewReportService.getReportsForAdmin(eq(Optional.empty()), any()))
                    .thenReturn(reportPage);

            // When/Then
            mockMvc.perform(get("/admin/moderation"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/moderation"))
                    .andExpect(model().attribute("currentPage", 0))
                    .andExpect(model().attribute("pageSize", 20));
        }

        @Test
        @DisplayName("Should return reports list with status filter - Happy path (status provided)")
        // Branch: status != null
        void listReports_WithStatusFilter_ReturnsFilteredReports() throws Exception {
            // Given
            ReviewReportStatus status = ReviewReportStatus.PENDING;
            List<ReviewReportView> reports = new ArrayList<>();
            reports.add(createMockReportView(1L));
            Page<ReviewReportView> reportPage = new PageImpl<>(reports, PageRequest.of(0, 20), reports.size());
            
            when(reviewReportService.getReportsForAdmin(eq(Optional.of(status)), any()))
                    .thenReturn(reportPage);

            // When/Then
            mockMvc.perform(get("/admin/moderation")
                            .param("status", status.name()))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("selectedStatus", status));
        }
    }

    @Nested
    @DisplayName("viewReport() - GET /admin/moderation/{reportId}")
    class ViewReportTests {

        @Test
        @DisplayName("Should return report detail view - Happy path")
        void viewReport_WithValidId_ReturnsReportDetail() throws Exception {
            // Given
            Long reportId = 1L;
            ReviewReportView report = createMockReportView(reportId);
            
            when(reviewReportService.getReportView(reportId)).thenReturn(report);

            // When/Then
            mockMvc.perform(get("/admin/moderation/{reportId}", reportId))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/moderation-detail"))
                    .andExpect(model().attribute("report", report));
        }
    }

    @Nested
    @DisplayName("resolveReport() - POST /admin/moderation/{reportId}/resolve")
    class ResolveReportTests {

        @Test
        @WithMockUser
        @DisplayName("Should resolve report with authentication - Happy path")
        // Branch: authentication != null && principal instanceof User
        void resolveReport_WithAuthenticatedUser_ResolvesReport() throws Exception {
            // Given
            Long reportId = 1L;

            // When/Then
            mockMvc.perform(post("/admin/moderation/{reportId}/resolve", reportId)
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/moderation"))
                    .andExpect(flash().attributeExists("success"));
        }

        @Test
        @DisplayName("Should resolve report without authentication - Edge case")
        // Branch: authentication == null
        void resolveReport_WithoutAuthentication_ResolvesWithNullAdminId() throws Exception {
            // Given
            Long reportId = 1L;

            // When/Then
            mockMvc.perform(post("/admin/moderation/{reportId}/resolve", reportId)
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/moderation"))
                    .andExpect(flash().attributeExists("success"));
        }

        @Test
        @WithMockUser
        @DisplayName("Should handle exception when resolving report - Error case")
        // Branch: catch exception
        void resolveReport_WhenServiceThrowsException_HandlesError() throws Exception {
            // Given
            Long reportId = 1L;
            doThrow(new RuntimeException("Service error"))
                    .when(reviewReportService).resolveReport(any(), any(), any());

            // When/Then
            mockMvc.perform(post("/admin/moderation/{reportId}/resolve", reportId)
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/moderation"))
                    .andExpect(flash().attributeExists("error"));
        }
    }

    @Nested
    @DisplayName("rejectReport() - POST /admin/moderation/{reportId}/reject")
    class RejectReportTests {

        @Test
        @WithMockUser
        @DisplayName("Should reject report with authentication - Happy path")
        // Branch: authentication != null && principal instanceof User
        void rejectReport_WithAuthenticatedUser_RejectsReport() throws Exception {
            // Given
            Long reportId = 1L;

            // When/Then
            mockMvc.perform(post("/admin/moderation/{reportId}/reject", reportId)
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/moderation"))
                    .andExpect(flash().attributeExists("success"));
        }

        @Test
        @DisplayName("Should reject report without authentication - Edge case")
        // Branch: authentication == null
        void rejectReport_WithoutAuthentication_RejectsWithNullAdminId() throws Exception {
            // Given
            Long reportId = 1L;

            // When/Then
            mockMvc.perform(post("/admin/moderation/{reportId}/reject", reportId)
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/moderation"))
                    .andExpect(flash().attributeExists("success"));
        }

        @Test
        @WithMockUser
        @DisplayName("Should handle exception when rejecting report - Error case")
        // Branch: catch exception
        void rejectReport_WhenServiceThrowsException_HandlesError() throws Exception {
            // Given
            Long reportId = 1L;
            doThrow(new RuntimeException("Service error"))
                    .when(reviewReportService).rejectReport(any(), any(), any());

            // When/Then
            mockMvc.perform(post("/admin/moderation/{reportId}/reject", reportId)
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/moderation"))
                    .andExpect(flash().attributeExists("error"));
        }
    }
}

