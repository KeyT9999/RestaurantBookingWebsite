package com.example.booking.web.controller.admin;

import com.example.booking.domain.ReviewReportStatus;
import com.example.booking.domain.User;
import com.example.booking.dto.ReviewReportView;
import com.example.booking.service.ReviewReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminModerationControllerUnitTest {

    @Mock
    private ReviewReportService reviewReportService;

    @InjectMocks
    private AdminModerationController controller;

    private Model model;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
    }

    @Test
    void listReportsShouldPopulateModel() {
        PageRequest pageable = PageRequest.of(1, 10);
        Page<ReviewReportView> reportPage = new PageImpl<>(List.of(new ReviewReportView()));
        when(reviewReportService.getReportsForAdmin(Optional.of(ReviewReportStatus.PENDING), pageable))
                .thenReturn(reportPage);

        String view = controller.listReports(1, 10, ReviewReportStatus.PENDING, model);

        assertEquals("admin/moderation", view);
        assertSame(reportPage, model.getAttribute("reportPage"));
        assertEquals(1, model.getAttribute("currentPage"));
        assertEquals(10, model.getAttribute("pageSize"));
        assertEquals(ReviewReportStatus.PENDING, model.getAttribute("selectedStatus"));
        assertTrue(model.containsAttribute("statuses"));
    }

    @Test
    void viewReportShouldAddReportToModel() {
        ReviewReportView report = new ReviewReportView();
        when(reviewReportService.getReportView(15L)).thenReturn(report);

        String view = controller.viewReport(15L, model);

        assertEquals("admin/moderation-detail", view);
        assertSame(report, model.getAttribute("report"));
    }

    @Test
    void resolveReportShouldForwardAdminIdToService() {
        User admin = new User();
        UUID adminId = UUID.randomUUID();
        admin.setId(adminId);
        AuthenticationStub authentication = AuthenticationStub.withPrincipal(admin);
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        controller.resolveReport(20L, "resolved", authentication, redirectAttributes);

        ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(reviewReportService).resolveReport(eq(20L), idCaptor.capture(), eq("resolved"));
        assertEquals(adminId, idCaptor.getValue());
        verify(redirectAttributes).addFlashAttribute("success", "Đã phê duyệt và ẩn review");
    }

    @Test
    void resolveReportShouldCaptureExceptionAndSetErrorFlash() {
        AuthenticationStub authentication = AuthenticationStub.anonymous();
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        doThrow(new IllegalStateException("failure"))
                .when(reviewReportService).resolveReport(eq(25L), any(), eq("msg"));

        String view = controller.resolveReport(25L, "msg", authentication, redirectAttributes);

        assertEquals("redirect:/admin/moderation", view);
        verify(redirectAttributes).addFlashAttribute(eq("error"), contains("failure"));
    }

    @Test
    void rejectReportShouldCallServiceWithAdminId() {
        UUID adminId = UUID.randomUUID();
        AuthenticationStub authentication = AuthenticationStub.withPrincipal(TestUserFactory.userWithId(adminId));
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        controller.rejectReport(30L, "", authentication, redirectAttributes);

        verify(reviewReportService).rejectReport(eq(30L), eq(adminId), eq(""));
        verify(redirectAttributes).addFlashAttribute("success", "Đã từ chối báo cáo");
    }

    @Test
    void rejectReportShouldHandleExceptionGracefully() {
        AuthenticationStub authentication = AuthenticationStub.anonymous();
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        doThrow(new RuntimeException("boom"))
                .when(reviewReportService).rejectReport(eq(31L), any(), eq("warn"));

        String view = controller.rejectReport(31L, "warn", authentication, redirectAttributes);

        assertEquals("redirect:/admin/moderation", view);
        verify(redirectAttributes).addFlashAttribute(eq("error"), contains("boom"));
    }

    private static class AuthenticationStub implements org.springframework.security.core.Authentication {
        private final Object principal;

        private AuthenticationStub(Object principal) {
            this.principal = principal;
        }

        static AuthenticationStub withPrincipal(Object principal) {
            return new AuthenticationStub(principal);
        }

        static AuthenticationStub anonymous() {
            return new AuthenticationStub(null);
        }

        @Override
        public Object getPrincipal() {
            return principal;
        }

        @Override
        public String getName() {
            return principal instanceof User user ? user.getUsername() : "anonymous";
        }

        @Override
        public Object getCredentials() {
            return null;
        }

        @Override
        public Object getDetails() {
            return null;
        }

        @Override
        public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
            return java.util.List.of();
        }

        @Override
        public boolean isAuthenticated() {
            return principal != null;
        }

        @Override
        public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
            // no-op
        }
    }

    private static class TestUserFactory {
        static User userWithId(UUID id) {
            User user = new User();
            user.setId(id);
            return user;
        }
    }
}

