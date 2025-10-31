package com.example.booking.web.controller.admin;

import com.example.booking.domain.ReviewReportStatus;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.ReviewReportView;
import com.example.booking.service.ReviewReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminModerationController.class)
class AdminModerationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewReportService reviewReportService;

    private ReviewReportView reportView;
    private User adminUser;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(UUID.randomUUID());
        adminUser.setEmail("admin@test.com");
        adminUser.setRole(UserRole.ADMIN);

        reportView = new ReviewReportView();
        reportView.setReportId(1L);
        reportView.setReviewId(100);
        reportView.setStatus(ReviewReportStatus.PENDING);
        reportView.setReportedAt(LocalDateTime.now());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldListReports() throws Exception {
        Page<ReviewReportView> reportPage = new PageImpl<>(Arrays.asList(reportView));
        when(reviewReportService.getReportsForAdmin(any(Optional.class), any())).thenReturn(reportPage);

        mockMvc.perform(get("/admin/moderation"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/moderation"))
                .andExpect(model().attributeExists("reportPage"))
                .andExpect(model().attributeExists("statuses"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldListReports_WithPagination() throws Exception {
        Page<ReviewReportView> reportPage = new PageImpl<>(Arrays.asList(reportView));
        when(reviewReportService.getReportsForAdmin(any(Optional.class), any())).thenReturn(reportPage);

        mockMvc.perform(get("/admin/moderation")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attribute("pageSize", 10));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldListReports_WithStatusFilter() throws Exception {
        Page<ReviewReportView> reportPage = new PageImpl<>(Arrays.asList(reportView));
        when(reviewReportService.getReportsForAdmin(eq(Optional.of(ReviewReportStatus.PENDING)), any()))
                .thenReturn(reportPage);

        mockMvc.perform(get("/admin/moderation")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedStatus", ReviewReportStatus.PENDING));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldViewReport() throws Exception {
        when(reviewReportService.getReportView(1L)).thenReturn(reportView);

        mockMvc.perform(get("/admin/moderation/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/moderation-detail"))
                .andExpect(model().attributeExists("report"));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void shouldResolveReport() throws Exception {
        doNothing().when(reviewReportService).resolveReport(anyLong(), any(), anyString());

        mockMvc.perform(post("/admin/moderation/1/resolve")
                        .param("resolutionMessage", "Resolved")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/moderation"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void shouldResolveReport_WithoutMessage() throws Exception {
        doNothing().when(reviewReportService).resolveReport(anyLong(), any(), any());

        mockMvc.perform(post("/admin/moderation/1/resolve")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/moderation"));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void shouldHandleResolveReportException() throws Exception {
        doThrow(new RuntimeException("Service error"))
                .when(reviewReportService).resolveReport(anyLong(), any(), any());

        mockMvc.perform(post("/admin/moderation/1/resolve")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/moderation"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void shouldRejectReport() throws Exception {
        doNothing().when(reviewReportService).rejectReport(anyLong(), any(), anyString());

        mockMvc.perform(post("/admin/moderation/1/reject")
                        .param("resolutionMessage", "Rejected")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/moderation"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void shouldHandleRejectReportException() throws Exception {
        doThrow(new RuntimeException("Service error"))
                .when(reviewReportService).rejectReport(anyLong(), any(), any());

        mockMvc.perform(post("/admin/moderation/1/reject")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/moderation"))
                .andExpect(flash().attributeExists("error"));
    }
}
