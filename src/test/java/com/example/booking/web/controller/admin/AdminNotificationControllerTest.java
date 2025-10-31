package com.example.booking.web.controller.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booking.domain.User;
import com.example.booking.dto.notification.NotificationForm;
import com.example.booking.service.NotificationService;

/**
 * Unit tests for AdminNotificationController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminNotificationController Tests")
public class AdminNotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private AdminNotificationController controller;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        when(authentication.getPrincipal()).thenReturn(user);
    }

    // ========== listNotifications() Tests ==========

    @Test
    @DisplayName("shouldListNotifications_successfully")
    void shouldListNotifications_successfully() {
        // Given
        Page<com.example.booking.dto.notification.AdminNotificationSummary> page = mock(Page.class);
        when(notificationService.findGroupedForAdmin(any())).thenReturn(page);

        // When
        String view = controller.listNotifications(0, 20, "publishAt", "desc", model);

        // Then
        assertEquals("admin/notifications/list", view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    // ========== createNotificationForm() Tests ==========

    @Test
    @DisplayName("shouldShowCreateForm_successfully")
    void shouldShowCreateForm_successfully() {
        // When
        String view = controller.createNotificationForm(model);

        // Then
        assertEquals("admin/notifications/form", view);
        verify(model, times(1)).addAttribute(eq("notificationForm"), any(NotificationForm.class));
    }

    // ========== createNotification() Tests ==========

    @Test
    @DisplayName("shouldCreateNotification_successfully")
    void shouldCreateNotification_successfully() {
        // Given
        NotificationForm form = new NotificationForm();
        form.setTitle("Test Notification");
        form.setContent("Test message");
        form.setAudience(NotificationForm.AudienceType.ALL);

        when(notificationService.sendToAll(any(NotificationForm.class), any(UUID.class))).thenReturn(1);

        // When
        String view = controller.createNotification(form, authentication, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/notifications", view);
        verify(notificationService, times(1)).sendToAll(any(NotificationForm.class), any(UUID.class));
    }

    @Test
    @DisplayName("shouldCreateNotificationForRoles_successfully")
    void shouldCreateNotificationForRoles_successfully() {
        // Given
        NotificationForm form = new NotificationForm();
        form.setTitle("Test Notification");
        form.setContent("Test message");
        form.setAudience(NotificationForm.AudienceType.ROLE);

        when(notificationService.sendToRoles(any(NotificationForm.class), any(), any(UUID.class))).thenReturn(1);

        // When
        String view = controller.createNotification(form, authentication, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/notifications", view);
        verify(notificationService, times(1)).sendToRoles(any(NotificationForm.class), any(), any(UUID.class));
    }
}

