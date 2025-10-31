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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminNotificationController.class)
@DisplayName("AdminNotificationController Test Suite")
class AdminNotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private UserRepository userRepository;

    private User testUser;
    private AdminNotificationSummary testNotificationSummary;
    private NotificationView testNotificationView;
    private Page<AdminNotificationSummary> notificationPage;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("admin@test.com");
        testUser.setRole(UserRole.ADMIN);

        testNotificationSummary = new AdminNotificationSummary();
        testNotificationSummary.setId(1);
        testNotificationSummary.setTitle("Test Notification");
        testNotificationSummary.setContent("Test Content");
        testNotificationSummary.setType(NotificationType.SYSTEM_ANNOUNCEMENT);

        testNotificationView = new NotificationView();
        testNotificationView.setId(1);
        testNotificationView.setTitle("Test Notification");
        testNotificationView.setContent("Test Content");

        List<AdminNotificationSummary> notifications = Arrays.asList(testNotificationSummary);
        notificationPage = new PageImpl<>(notifications, PageRequest.of(0, 20), 1);
    }

    @Nested
    @DisplayName("listNotifications() Tests")
    class ListNotificationsTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should display notifications list successfully")
        void shouldDisplayNotificationsList() throws Exception {
            when(notificationService.findGroupedForAdmin(any(Pageable.class))).thenReturn(notificationPage);

            mockMvc.perform(get("/admin/notifications"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/notifications/list"))
                    .andExpect(model().attributeExists("notifications"))
                    .andExpect(model().attributeExists("currentPage"))
                    .andExpect(model().attributeExists("totalPages"));

            verify(notificationService).findGroupedForAdmin(any(Pageable.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle pagination")
        void shouldHandlePagination() throws Exception {
            when(notificationService.findGroupedForAdmin(any(Pageable.class))).thenReturn(notificationPage);

            mockMvc.perform(get("/admin/notifications")
                    .param("page", "1")
                    .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("currentPage", 1));

            verify(notificationService).findGroupedForAdmin(any(Pageable.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle sorting")
        void shouldHandleSorting() throws Exception {
            when(notificationService.findGroupedForAdmin(any(Pageable.class))).thenReturn(notificationPage);

            mockMvc.perform(get("/admin/notifications")
                    .param("sortBy", "publishAt")
                    .param("sortDir", "asc"))
                    .andExpect(status().isOk());

            verify(notificationService).findGroupedForAdmin(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("createNotificationForm() Tests")
    class CreateNotificationFormTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should display create notification form")
        void shouldDisplayCreateForm() throws Exception {
            mockMvc.perform(get("/admin/notifications/new"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/notifications/form"))
                    .andExpect(model().attributeExists("notificationForm"))
                    .andExpect(model().attributeExists("notificationTypes"))
                    .andExpect(model().attributeExists("userRoles"));
        }
    }

    @Nested
    @DisplayName("createNotification() Tests")
    class CreateNotificationTests {

        @Test
        @WithMockUser(roles = "ADMIN", username = "admin@test.com")
        @DisplayName("Should create notification for all users")
        void shouldCreateNotificationForAll() throws Exception {
            when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(testUser));
            doNothing().when(notificationService).sendToAll(any(NotificationForm.class), any(UUID.class));

            mockMvc.perform(post("/admin/notifications/create")
                    .with(csrf())
                    .param("title", "Test Title")
                    .param("content", "Test Content")
                    .param("audience", "ALL")
                    .param("notificationType", "INFO"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/notifications"))
                    .andExpect(flash().attributeExists("success"));

            verify(notificationService).sendToAll(any(NotificationForm.class), any(UUID.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN", username = "admin@test.com")
        @DisplayName("Should create notification for specific roles")
        void shouldCreateNotificationForRoles() throws Exception {
            when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(testUser));
            doReturn(10).when(notificationService).sendToRoles(any(NotificationForm.class), any(Set.class), any(UUID.class));

            mockMvc.perform(post("/admin/notifications/create")
                    .with(csrf())
                    .param("title", "Test Title")
                    .param("content", "Test Content")
                    .param("audience", "ROLE")
                    .param("targetRoles", "CUSTOMER", "RESTAURANT_OWNER")
                    .param("notificationType", "INFO"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/notifications"))
                    .andExpect(flash().attributeExists("success"));

            verify(notificationService).sendToRoles(any(NotificationForm.class), any(Set.class), any(UUID.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN", username = "admin@test.com")
        @DisplayName("Should create notification for specific users")
        void shouldCreateNotificationForUsers() throws Exception {
            when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(testUser));
            doReturn(5).when(notificationService).sendToUsers(any(NotificationForm.class), any(Set.class), any(UUID.class));

            UUID targetUserId = UUID.randomUUID();
            mockMvc.perform(post("/admin/notifications/create")
                    .with(csrf())
                    .param("title", "Test Title")
                    .param("content", "Test Content")
                    .param("audience", "USER")
                    .param("targetUserIds", targetUserId.toString())
                    .param("notificationType", "INFO"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/notifications"))
                    .andExpect(flash().attributeExists("success"));

            verify(notificationService).sendToUsers(any(NotificationForm.class), any(Set.class), any(UUID.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN", username = "admin@test.com")
        @DisplayName("Should handle service exception")
        void shouldHandleServiceException() throws Exception {
            when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(testUser));
            doThrow(new RuntimeException("Service error")).when(notificationService).sendToAll(any(), any());

            mockMvc.perform(post("/admin/notifications/create")
                    .with(csrf())
                    .param("title", "Test Title")
                    .param("content", "Test Content")
                    .param("audience", "ALL")
                    .param("notificationType", "INFO"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/notifications/new"))
                    .andExpect(flash().attributeExists("error"));
        }
    }

    @Nested
    @DisplayName("viewNotification() Tests")
    class ViewNotificationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should display notification details")
        void shouldDisplayNotificationDetails() throws Exception {
            when(notificationService.findById(1)).thenReturn(testNotificationView);

            mockMvc.perform(get("/admin/notifications/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/notifications/detail"))
                    .andExpect(model().attributeExists("notification"));

            verify(notificationService).findById(1);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 404 when notification not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(notificationService.findById(999)).thenReturn(null);

            mockMvc.perform(get("/admin/notifications/999"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("error/404"));

            verify(notificationService).findById(999);
        }
    }

    @Nested
    @DisplayName("expireNotification() Tests")
    class ExpireNotificationTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should expire notification successfully")
        void shouldExpireNotification() throws Exception {
            doNothing().when(notificationService).expireNotification(1);

            mockMvc.perform(post("/admin/notifications/1/expire")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/notifications"))
                    .andExpect(flash().attributeExists("success"));

            verify(notificationService).expireNotification(1);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle expire exception")
        void shouldHandleExpireException() throws Exception {
            doThrow(new RuntimeException("Expire error")).when(notificationService).expireNotification(1);

            mockMvc.perform(post("/admin/notifications/1/expire")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/notifications"))
                    .andExpect(flash().attributeExists("error"));
        }
    }

    @Nested
    @DisplayName("notificationStats() Tests")
    class NotificationStatsTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should display notification statistics")
        void shouldDisplayStatistics() throws Exception {
            when(notificationService.countTotalSent()).thenReturn(100L);
            when(notificationService.countTotalRead()).thenReturn(75L);

            mockMvc.perform(get("/admin/notifications/stats"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/notifications/stats"))
                    .andExpect(model().attribute("totalSent", 100L))
                    .andExpect(model().attribute("totalRead", 75L))
                    .andExpect(model().attribute("totalUnread", 25L));

            verify(notificationService).countTotalSent();
            verify(notificationService).countTotalRead();
        }
    }
}

