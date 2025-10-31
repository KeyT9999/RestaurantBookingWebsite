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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminNotificationController.class)
class AdminNotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private UserRepository userRepository;

    private AdminNotificationSummary summary;
    private NotificationView notificationView;
    private User adminUser;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(UUID.randomUUID());
        adminUser.setEmail("admin@test.com");
        adminUser.setRole(UserRole.ADMIN);

        summary = new AdminNotificationSummary();
        summary.setId(1);
        summary.setType(NotificationType.SYSTEM_ANNOUNCEMENT);
        summary.setTitle("Test Notification");
        summary.setContent("Test content");
        summary.setPublishAt(LocalDateTime.now());
        summary.setTotalRecipients(100L);
        summary.setCustomerRecipients(80L);
        summary.setRestaurantOwnerRecipients(20L);

        notificationView = new NotificationView();
        notificationView.setId(1);
        notificationView.setType(NotificationType.SYSTEM_ANNOUNCEMENT);
        notificationView.setTitle("Test Notification");
        notificationView.setContent("Test content");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldListNotifications() throws Exception {
        Page<AdminNotificationSummary> notificationPage = new PageImpl<>(Arrays.asList(summary));
        when(notificationService.findGroupedForAdmin(any())).thenReturn(notificationPage);

        mockMvc.perform(get("/admin/notifications"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/notifications/list"))
                .andExpect(model().attributeExists("notifications"))
                .andExpect(model().attributeExists("currentPage"))
                .andExpect(model().attributeExists("totalPages"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldListNotifications_WithPagination() throws Exception {
        Page<AdminNotificationSummary> notificationPage = new PageImpl<>(Arrays.asList(summary));
        when(notificationService.findGroupedForAdmin(any())).thenReturn(notificationPage);

        mockMvc.perform(get("/admin/notifications")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("currentPage", 1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldListNotifications_WithSorting() throws Exception {
        Page<AdminNotificationSummary> notificationPage = new PageImpl<>(Arrays.asList(summary));
        when(notificationService.findGroupedForAdmin(any())).thenReturn(notificationPage);

        mockMvc.perform(get("/admin/notifications")
                        .param("sortBy", "publishAt")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowCreateNotificationForm() throws Exception {
        mockMvc.perform(get("/admin/notifications/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/notifications/form"))
                .andExpect(model().attributeExists("notificationForm"))
                .andExpect(model().attributeExists("notificationTypes"))
                .andExpect(model().attributeExists("userRoles"));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void shouldCreateNotification_AllAudience() throws Exception {
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        doNothing().when(notificationService).sendToAll(any(NotificationForm.class), any(UUID.class));

        mockMvc.perform(post("/admin/notifications/create")
                        .param("type", "SYSTEM_ANNOUNCEMENT")
                        .param("title", "Test Title")
                        .param("content", "Test Content")
                        .param("audience", "ALL")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/notifications"))
                .andExpect(flash().attributeExists("success"));

        verify(notificationService).sendToAll(any(NotificationForm.class), eq(adminUser.getId()));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void shouldCreateNotification_RoleAudience() throws Exception {
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        Set<UserRole> targetRoles = new HashSet<>(Arrays.asList(UserRole.CUSTOMER));
        doNothing().when(notificationService).sendToRoles(
                any(NotificationForm.class), eq(targetRoles), any(UUID.class));

        mockMvc.perform(post("/admin/notifications/create")
                        .param("type", "SYSTEM_ANNOUNCEMENT")
                        .param("title", "Test Title")
                        .param("content", "Test Content")
                        .param("audience", "ROLE")
                        .param("targetRoles", "CUSTOMER")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/notifications"))
                .andExpect(flash().attributeExists("success"));

        verify(notificationService).sendToRoles(any(NotificationForm.class), any(), eq(adminUser.getId()));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void shouldCreateNotification_UserAudience() throws Exception {
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        Set<UUID> targetUserIds = new HashSet<>(Arrays.asList(UUID.randomUUID()));
        doNothing().when(notificationService).sendToUsers(
                any(NotificationForm.class), eq(targetUserIds), any(UUID.class));

        mockMvc.perform(post("/admin/notifications/create")
                        .param("type", "SYSTEM_ANNOUNCEMENT")
                        .param("title", "Test Title")
                        .param("content", "Test Content")
                        .param("audience", "USER")
                        .param("targetUserIds", UUID.randomUUID().toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/notifications"))
                .andExpect(flash().attributeExists("success"));

        verify(notificationService).sendToUsers(any(NotificationForm.class), any(), eq(adminUser.getId()));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void shouldHandleCreateNotificationException() throws Exception {
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        doThrow(new RuntimeException("Service error"))
                .when(notificationService).sendToAll(any(NotificationForm.class), any(UUID.class));

        mockMvc.perform(post("/admin/notifications/create")
                        .param("type", "SYSTEM_ANNOUNCEMENT")
                        .param("title", "Test Title")
                        .param("content", "Test Content")
                        .param("audience", "ALL")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/notifications/new"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldViewNotification() throws Exception {
        when(notificationService.findById(1)).thenReturn(notificationView);

        mockMvc.perform(get("/admin/notifications/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/notifications/detail"))
                .andExpect(model().attributeExists("notification"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldHandleNotificationNotFound() throws Exception {
        when(notificationService.findById(999)).thenReturn(null);

        mockMvc.perform(get("/admin/notifications/999"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldExpireNotification() throws Exception {
        doNothing().when(notificationService).expireNotification(1);

        mockMvc.perform(post("/admin/notifications/1/expire")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/notifications"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldHandleExpireNotificationException() throws Exception {
        doThrow(new RuntimeException("Service error"))
                .when(notificationService).expireNotification(1);

        mockMvc.perform(post("/admin/notifications/1/expire")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/notifications"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDisplayNotificationStats() throws Exception {
        when(notificationService.countTotalSent()).thenReturn(1000L);
        when(notificationService.countTotalRead()).thenReturn(800L);

        mockMvc.perform(get("/admin/notifications/stats"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/notifications/stats"))
                .andExpect(model().attribute("totalSent", 1000L))
                .andExpect(model().attribute("totalRead", 800L))
                .andExpect(model().attribute("totalUnread", 200L));
    }
}
