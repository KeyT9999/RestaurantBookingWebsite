package com.example.booking.web.controller.admin;

import com.example.booking.domain.ReviewReportStatus;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.ReviewReportView;
import com.example.booking.service.ReviewReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminModerationController.class)
@DisplayName("AdminModerationController Test Suite")
class AdminModerationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewReportService reviewReportService;

    private User testAdminUser;
    private ReviewReportView testReportView;
    private Page<ReviewReportView> reportPage;

    @BeforeEach
    void setUp() {
        testAdminUser = new User();
        testAdminUser.setId(UUID.randomUUID());
        testAdminUser.setEmail("admin@test.com");
        testAdminUser.setRole(UserRole.ADMIN);

        testReportView = new ReviewReportView();
        testReportView.setReportId(1L);
        testReportView.setStatus(ReviewReportStatus.PENDING);
        testReportView.setReasonText("Inappropriate content");

        List<ReviewReportView> reports = Arrays.asList(testReportView);
        reportPage = new PageImpl<>(reports, PageRequest.of(0, 20), 1);
    }

    @Nested
    @DisplayName("listReports() Tests")
    class ListReportsTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should display reports list successfully")
        void shouldDisplayReportsList() throws Exception {
            when(reviewReportService.getReportsForAdmin(any(Optional.class), any(Pageable.class)))
                    .thenReturn(reportPage);

            mockMvc.perform(get("/admin/moderation"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/moderation"))
                    .andExpect(model().attributeExists("reportPage"))
                    .andExpect(model().attributeExists("currentPage"))
                    .andExpect(model().attributeExists("statuses"));

            verify(reviewReportService).getReportsForAdmin(any(Optional.class), any(Pageable.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should filter reports by status")
        void shouldFilterByStatus() throws Exception {
            when(reviewReportService.getReportsForAdmin(any(Optional.class), any(Pageable.class)))
                    .thenReturn(reportPage);

            mockMvc.perform(get("/admin/moderation")
                    .param("status", "PENDING"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("selectedStatus", ReviewReportStatus.PENDING));

            verify(reviewReportService).getReportsForAdmin(any(Optional.class), any(Pageable.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle pagination")
        void shouldHandlePagination() throws Exception {
            when(reviewReportService.getReportsForAdmin(any(Optional.class), any(Pageable.class)))
                    .thenReturn(reportPage);

            mockMvc.perform(get("/admin/moderation")
                    .param("page", "1")
                    .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("currentPage", 1));

            verify(reviewReportService).getReportsForAdmin(any(Optional.class), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("viewReport() Tests")
    class ViewReportTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should display report details")
        void shouldDisplayReportDetails() throws Exception {
            when(reviewReportService.getReportView(1L)).thenReturn(testReportView);

            mockMvc.perform(get("/admin/moderation/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/moderation-detail"))
                    .andExpect(model().attributeExists("report"))
                    .andExpect(model().attributeExists("pageTitle"));

            verify(reviewReportService).getReportView(1L);
        }
    }

    @Nested
    @DisplayName("resolveReport() Tests")
    class ResolveReportTests {

        @Test
        @WithMockUser(roles = "ADMIN", username = "admin@test.com")
        @DisplayName("Should resolve report successfully")
        void shouldResolveReportSuccessfully() throws Exception {
            doNothing().when(reviewReportService).resolveReport(eq(1L), any(UUID.class), anyString());

            mockMvc.perform(post("/admin/moderation/1/resolve")
                    .with(csrf())
                    .param("resolutionMessage", "Report resolved"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/moderation"))
                    .andExpect(flash().attributeExists("success"));

            verify(reviewReportService).resolveReport(eq(1L), any(UUID.class), anyString());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle resolve exception")
        void shouldHandleResolveException() throws Exception {
            doThrow(new RuntimeException("Resolve error")).when(reviewReportService)
                    .resolveReport(eq(1L), any(UUID.class), anyString());

            mockMvc.perform(post("/admin/moderation/1/resolve")
                    .with(csrf())
                    .param("resolutionMessage", "Report resolved"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/moderation"))
                    .andExpect(flash().attributeExists("error"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should resolve report without message")
        void shouldResolveReportWithoutMessage() throws Exception {
            doNothing().when(reviewReportService).resolveReport(eq(1L), any(UUID.class), isNull());

            mockMvc.perform(post("/admin/moderation/1/resolve")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/moderation"))
                    .andExpect(flash().attributeExists("success"));

            verify(reviewReportService).resolveReport(eq(1L), any(UUID.class), isNull());
        }
    }

    @Nested
    @DisplayName("rejectReport() Tests")
    class RejectReportTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should reject report successfully")
        void shouldRejectReportSuccessfully() throws Exception {
            doNothing().when(reviewReportService).rejectReport(eq(1L), any(UUID.class), anyString());

            mockMvc.perform(post("/admin/moderation/1/reject")
                    .with(csrf())
                    .param("resolutionMessage", "Report rejected"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/moderation"))
                    .andExpect(flash().attributeExists("success"));

            verify(reviewReportService).rejectReport(eq(1L), any(UUID.class), anyString());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle reject exception")
        void shouldHandleRejectException() throws Exception {
            doThrow(new RuntimeException("Reject error")).when(reviewReportService)
                    .rejectReport(eq(1L), any(UUID.class), anyString());

            mockMvc.perform(post("/admin/moderation/1/reject")
                    .with(csrf())
                    .param("resolutionMessage", "Report rejected"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/moderation"))
                    .andExpect(flash().attributeExists("error"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should reject report without message")
        void shouldRejectReportWithoutMessage() throws Exception {
            doNothing().when(reviewReportService).rejectReport(eq(1L), any(UUID.class), isNull());

            mockMvc.perform(post("/admin/moderation/1/reject")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/moderation"))
                    .andExpect(flash().attributeExists("success"));

            verify(reviewReportService).rejectReport(eq(1L), any(UUID.class), isNull());
        }
    }
}

