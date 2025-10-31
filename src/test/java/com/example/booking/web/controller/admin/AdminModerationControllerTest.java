package com.example.booking.web.controller.admin;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.config.TestRateLimitingConfig;
import com.example.booking.domain.ReviewReportStatus;
import com.example.booking.domain.User;
import com.example.booking.dto.ReviewReportView;
import com.example.booking.service.ReviewReportService;

@WebMvcTest(AdminModerationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestRateLimitingConfig.class)
class AdminModerationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewReportService reviewReportService;

    private User adminUser(UUID id) {
        User user = new User();
        user.setId(id);
        user.setUsername("admin");
        user.setEmail("admin@example.com");
        user.setPassword("securePass!");
        user.setFullName("Admin User");
        return user;
    }

    @Test
    @DisplayName("listReports should render reports with pagination context")
    void shouldListReportsForAdmin() throws Exception {
        ReviewReportView view1 = new ReviewReportView();
        view1.setReportId(1L);
        view1.setCustomerName("Customer A");
        org.springframework.data.domain.Page<ReviewReportView> page = new org.springframework.data.domain.PageImpl<>(java.util.List.of(view1),
                PageRequest.of(0, 20), 1);

        when(reviewReportService.getReportsForAdmin(Optional.empty(), PageRequest.of(0, 20)))
                .thenReturn(page);

        mockMvc.perform(get("/admin/moderation")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/moderation"))
                .andExpect(model().attribute("reportPage", hasProperty("totalElements", is(1L))))
                .andExpect(model().attribute("statuses", hasItem(ReviewReportStatus.PENDING)));
    }

    @Test
    @DisplayName("viewReport should render detail template with report payload")
    void shouldRenderReportDetail() throws Exception {
        ReviewReportView view = new ReviewReportView();
        view.setReportId(44L);
        view.setCustomerName("Jane");

        when(reviewReportService.getReportView(44L)).thenReturn(view);

        mockMvc.perform(get("/admin/moderation/44"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/moderation-detail"))
                .andExpect(model().attribute("report", hasProperty("customerName", is("Jane"))));
    }

    @Test
    @DisplayName("resolveReport should delegate to service with admin id and flash success")
    void shouldResolveReport() throws Exception {
        UUID adminId = UUID.randomUUID();
        TestingAuthenticationToken auth = new TestingAuthenticationToken(adminUser(adminId), "pwd");

        mockMvc.perform(post("/admin/moderation/10/resolve")
                        .param("resolutionMessage", "Approved")
                        .with(authentication(auth)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/moderation"))
                .andExpect(flash().attribute("success", "Đã phê duyệt và ẩn review"));

        verify(reviewReportService).resolveReport(10L, adminId, "Approved");
    }

    @Test
    @DisplayName("resolveReport should surface error flash when service throws")
    void shouldHandleResolveErrors() throws Exception {
        UUID adminId = UUID.randomUUID();
        TestingAuthenticationToken auth = new TestingAuthenticationToken(adminUser(adminId), "pwd");

        doThrow(new IllegalStateException("already resolved"))
                .when(reviewReportService)
                .resolveReport(eq(5L), eq(adminId), any());

        mockMvc.perform(post("/admin/moderation/5/resolve")
                        .param("resolutionMessage", "Approve")
                        .with(authentication(auth)))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", containsString("Không thể phê duyệt")));
    }

    @Test
    @DisplayName("rejectReport should delegate to service and flash success")
    void shouldRejectReport() throws Exception {
        UUID adminId = UUID.randomUUID();
        TestingAuthenticationToken auth = new TestingAuthenticationToken(adminUser(adminId), "pwd");

        mockMvc.perform(post("/admin/moderation/22/reject")
                        .param("resolutionMessage", "Not valid")
                        .with(authentication(auth)))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("success", "Đã từ chối báo cáo"));

        verify(reviewReportService).rejectReport(22L, adminId, "Not valid");
    }

    @Test
    @DisplayName("rejectReport should emit error flash when service fails")
    void shouldHandleRejectErrors() throws Exception {
        UUID adminId = UUID.randomUUID();
        TestingAuthenticationToken auth = new TestingAuthenticationToken(adminUser(adminId), "pwd");

        doThrow(new RuntimeException("boom"))
                .when(reviewReportService)
                .rejectReport(eq(22L), eq(adminId), any());

        mockMvc.perform(post("/admin/moderation/22/reject")
                        .with(authentication(auth)))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", containsString("Không thể từ chối báo cáo")));
    }
}
