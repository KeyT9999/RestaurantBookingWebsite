package com.example.booking.web.controller;

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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

import com.example.booking.domain.User;
import com.example.booking.dto.notification.NotificationView;
import com.example.booking.service.NotificationService;

/**
 * Unit tests for NotificationController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationController Tests")
public class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private NotificationController notificationController;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setEmail("test@test.com");

        when(authentication.getPrincipal()).thenReturn(user);
    }

    // ========== listNotifications() Tests ==========

    @Test
    @DisplayName("shouldListNotifications_successfully")
    void shouldListNotifications_successfully() {
        // Given
        Page<NotificationView> notifications = new PageImpl<>(java.util.Collections.emptyList());
        when(notificationService.findByUserId(eq(userId), any(Pageable.class))).thenReturn(notifications);
        when(notificationService.countUnreadByUserId(userId)).thenReturn(0L);

        // When
        String view = notificationController.listNotifications(0, 20, false, model, authentication);

        // Then
        assertEquals("notifications/list", view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    @Test
    @DisplayName("shouldListUnreadOnly_whenRequested")
    void shouldListUnreadOnly_whenRequested() {
        // Given
        Page<NotificationView> notifications = new PageImpl<>(java.util.Collections.emptyList());
        when(notificationService.findByUserIdAndUnread(eq(userId), eq(true), any(Pageable.class)))
            .thenReturn(notifications);
        when(notificationService.countUnreadByUserId(userId)).thenReturn(5L);

        // When
        String view = notificationController.listNotifications(0, 20, true, model, authentication);

        // Then
        assertEquals("notifications/list", view);
        verify(notificationService, times(1)).findByUserIdAndUnread(eq(userId), eq(true), any(Pageable.class));
    }

    @Test
    @DisplayName("shouldRedirectToLogin_whenUserNotAuthenticated")
    void shouldRedirectToLogin_whenUserNotAuthenticated() {
        // Given
        when(authentication.getPrincipal()).thenReturn(null);

        // When
        String view = notificationController.listNotifications(0, 20, false, model, authentication);

        // Then
        assertEquals("redirect:/login", view);
    }

    // ========== viewNotification() Tests ==========

    @Test
    @DisplayName("shouldViewNotification_successfully")
    void shouldViewNotification_successfully() {
        // Given
        NotificationView notification = new NotificationView();
        when(notificationService.findById(1)).thenReturn(notification);
        doNothing().when(notificationService).markAsRead(1, userId);

        // When
        String view = notificationController.viewNotification(1, model, authentication);

        // Then
        assertEquals("notifications/detail", view);
        verify(notificationService, times(1)).markAsRead(1, userId);
    }

    // ========== markAsRead() Tests ==========

    @Test
    @DisplayName("shouldMarkAsRead_successfully")
    void shouldMarkAsRead_successfully() {
        // Given
        doNothing().when(notificationService).markAsRead(1, userId);

        // When
        ResponseEntity<String> response = notificationController.markAsRead(1, authentication);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        verify(notificationService, times(1)).markAsRead(1, userId);
    }

    // ========== markAllAsRead() Tests ==========

    @Test
    @DisplayName("shouldMarkAllAsRead_successfully")
    void shouldMarkAllAsRead_successfully() {
        // Given
        doNothing().when(notificationService).markAllAsRead(userId);

        // When
        ResponseEntity<String> response = notificationController.markAllAsRead(authentication);

        // Then
        assertEquals(200, response.getStatusCodeValue());
        verify(notificationService, times(1)).markAllAsRead(userId);
    }
}

