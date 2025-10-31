package com.example.booking.web.controller.admin;

import com.example.booking.domain.NotificationType;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.notification.AdminNotificationSummary;
import com.example.booking.dto.notification.NotificationForm;
import com.example.booking.dto.notification.NotificationView;
import com.example.booking.repository.UserRepository;
import com.example.booking.service.NotificationService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminNotificationControllerUnitTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminNotificationController controller;

    private Model model;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
    }

    @Test
    void listNotificationsShouldPopulateModel() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by("publishAt").descending());
        Page<AdminNotificationSummary> summaries = new PageImpl<>(List.of(new AdminNotificationSummary()));
        when(notificationService.findGroupedForAdmin(pageable)).thenReturn(summaries);

        String view = controller.listNotifications(0, 20, "publishAt", "desc", model);

        assertEquals("admin/notifications/list", view);
        assertSame(summaries, model.getAttribute("notifications"));
        assertEquals(0, model.getAttribute("currentPage"));
        assertEquals(summaries.getTotalPages(), model.getAttribute("totalPages"));
        assertEquals(summaries.getTotalElements(), model.getAttribute("totalElements"));
    }

    @Test
    void createNotificationFormShouldExposeEnums() {
        String view = controller.createNotificationForm(model);

        assertEquals("admin/notifications/form", view);
        assertNotNull(model.getAttribute("notificationForm"));
        assertArrayEquals(NotificationType.values(), (NotificationType[]) model.getAttribute("notificationTypes"));
        assertArrayEquals(UserRole.values(), (UserRole[]) model.getAttribute("userRoles"));
    }

    @Test
    void createNotificationShouldDispatchToAllAudience() {
        NotificationForm form = new NotificationForm();
        form.setAudience(NotificationForm.AudienceType.ALL);
        UUID adminId = UUID.randomUUID();
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        controller.createNotification(form, AuthenticationStub.withId(adminId), redirectAttributes);

        verify(notificationService).sendToAll(eq(form), eq(adminId));
        verify(redirectAttributes).addFlashAttribute("success", "Thông báo đã được gửi thành công!");
    }

    @Test
    void createNotificationShouldDispatchToRoles() {
        NotificationForm form = new NotificationForm();
        form.setAudience(NotificationForm.AudienceType.ROLE);
        form.setTargetRoles(Set.of(UserRole.ADMIN));
        UUID adminId = UUID.randomUUID();
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        controller.createNotification(form, AuthenticationStub.withId(adminId), redirectAttributes);

        verify(notificationService).sendToRoles(eq(form), eq(form.getTargetRoles()), eq(adminId));
    }

    @Test
    void createNotificationShouldDispatchToUsersUsingOauthPrincipal() {
        NotificationForm form = new NotificationForm();
        form.setAudience(NotificationForm.AudienceType.USER);
        form.setTargetUserIds(Set.of(UUID.randomUUID()));
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("email")).thenReturn("admin@example.com");
        User user = new User();
        UUID adminId = UUID.randomUUID();
        user.setId(adminId);
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));

        controller.createNotification(form, new TestingAuthenticationToken(oAuth2User, "pwd"), redirectAttributes);

        verify(notificationService).sendToUsers(eq(form), eq(form.getTargetUserIds()), eq(adminId));
    }

    @Test
    void createNotificationShouldHandleExceptionsAndRedirect() {
        NotificationForm form = new NotificationForm();
        form.setAudience(NotificationForm.AudienceType.ALL);
        UUID adminId = UUID.randomUUID();
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        doThrow(new RuntimeException("boom"))
                .when(notificationService).sendToAll(form, adminId);

        String view = controller.createNotification(form, AuthenticationStub.withId(adminId), redirectAttributes);

        assertEquals("redirect:/admin/notifications/new", view);
        verify(redirectAttributes).addFlashAttribute(eq("error"), contains("boom"));
    }

    @Test
    void viewNotificationShouldReturnDetailView() {
        NotificationView viewDto = new NotificationView();
        when(notificationService.findById(88)).thenReturn(viewDto);

        String view = controller.viewNotification(88, model);

        assertEquals("admin/notifications/detail", view);
        assertSame(viewDto, model.getAttribute("notification"));
    }

    @Test
    void viewNotificationShouldReturn404WhenMissing() {
        when(notificationService.findById(99)).thenReturn(null);

        assertEquals("error/404", controller.viewNotification(99, model));
    }

    @Test
    void expireNotificationShouldDelegateToService() {
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        String view = controller.expireNotification(55, redirectAttributes);

        verify(notificationService).expireNotification(55);
        verify(redirectAttributes).addFlashAttribute("success", "Thông báo đã được kết thúc!");
        assertEquals("redirect:/admin/notifications", view);
    }

    @Test
    void expireNotificationShouldHandleErrors() {
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        doThrow(new RuntimeException("err")).when(notificationService).expireNotification(56);

        String view = controller.expireNotification(56, redirectAttributes);

        assertEquals("redirect:/admin/notifications", view);
        verify(redirectAttributes).addFlashAttribute(eq("error"), contains("err"));
    }

    @Test
    void notificationStatsShouldPopulateCounts() {
        when(notificationService.countTotalSent()).thenReturn(100L);
        when(notificationService.countTotalRead()).thenReturn(60L);

        String view = controller.notificationStats(model);

        assertEquals("admin/notifications/stats", view);
        assertEquals(100L, model.getAttribute("totalSent"));
        assertEquals(60L, model.getAttribute("totalRead"));
        assertEquals(40L, model.getAttribute("totalUnread"));
    }

    private static class AuthenticationStub implements org.springframework.security.core.Authentication {
        private final User principal = new User();

        static AuthenticationStub withId(UUID id) {
            AuthenticationStub stub = new AuthenticationStub();
            stub.principal.setId(id);
            return stub;
        }

        static AuthenticationStub anonymous() {
            return new AuthenticationStub();
        }

        @Override
        public Object getPrincipal() {
            return principal;
        }

        @Override
        public String getName() {
            return principal.getUsername();
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
            return principal.getId() != null;
        }

        @Override
        public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
            // no-op
        }

        private AuthenticationStub() {
        }
    }
}

