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
        assertEquals(200, response.getStatusCode().value());
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
        assertEquals(200, response.getStatusCode().value());
        verify(notificationService, times(1)).markAllAsRead(userId);
    }

    @Test
    @DisplayName("shouldReturnUnauthorized_whenMarkAsReadWithoutAuth")
    void shouldReturnUnauthorized_whenMarkAsReadWithoutAuth() {
        // Given
        when(authentication.getPrincipal()).thenReturn(null);

        // When
        ResponseEntity<String> response = notificationController.markAsRead(1, authentication);

        // Then
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    @DisplayName("shouldReturnUnauthorized_whenMarkAllAsReadWithoutAuth")
    void shouldReturnUnauthorized_whenMarkAllAsReadWithoutAuth() {
        // Given
        when(authentication.getPrincipal()).thenReturn(null);

        // When
        ResponseEntity<String> response = notificationController.markAllAsRead(authentication);

        // Then
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    @DisplayName("shouldReturn404_whenViewNotificationNotFound")
    void shouldReturn404_whenViewNotificationNotFound() {
        // Given
        when(notificationService.findById(999)).thenReturn(null);

        // When
        String view = notificationController.viewNotification(999, model, authentication);

        // Then
        assertEquals("error/404", view);
    }

    @Test
    @DisplayName("shouldReturnUnreadCount_successfully")
    void shouldReturnUnreadCount_successfully() {
        // Given
        when(notificationService.countUnreadByUserId(userId)).thenReturn(5L);

        // When
        ResponseEntity<Long> response = notificationController.getUnreadCount(authentication);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(5L, response.getBody());
    }

    @Test
    @DisplayName("shouldReturnZero_whenUnreadCountWithoutAuth")
    void shouldReturnZero_whenUnreadCountWithoutAuth() {
        // Given
        when(authentication.getPrincipal()).thenReturn(null);

        // When
        ResponseEntity<Long> response = notificationController.getUnreadCount(authentication);

        // Then
        assertEquals(401, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(0L, response.getBody());
    }

    @Test
    @DisplayName("shouldGetLatestNotifications_successfully")
    void shouldGetLatestNotifications_successfully() {
        // Given
        java.util.List<NotificationView> notifications = java.util.Arrays.asList(
            new NotificationView(), new NotificationView()
        );
        when(notificationService.getLatestNotifications(userId)).thenReturn(notifications);

        // When
        ResponseEntity<java.util.List<NotificationView>> response = 
            notificationController.getLatestNotifications(authentication);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    @DisplayName("shouldReturnEmptyList_whenLatestNotificationsWithoutAuth")
    void shouldReturnEmptyList_whenLatestNotificationsWithoutAuth() {
        // Given
        when(authentication.getPrincipal()).thenReturn(null);

        // When
        ResponseEntity<java.util.List<NotificationView>> response = 
            notificationController.getLatestNotifications(authentication);

        // Then
        assertEquals(401, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    @DisplayName("shouldHandlePagination_correctly")
    void shouldHandlePagination_correctly() {
        // Given
        Page<NotificationView> notifications = new PageImpl<>(
            java.util.Collections.emptyList(),
            PageRequest.of(1, 10),
            20L
        );
        when(notificationService.findByUserId(eq(userId), any(Pageable.class))).thenReturn(notifications);
        when(notificationService.countUnreadByUserId(userId)).thenReturn(0L);

        // When
        String view = notificationController.listNotifications(1, 10, false, model, authentication);

        // Then
        assertEquals("notifications/list", view);
        verify(model, atLeastOnce()).addAttribute(eq("currentPage"), eq(1));
    }

    @Test
    @DisplayName("shouldHandleOAuth2User_correctly")
    void shouldHandleOAuth2User_correctly() {
        // Given
        org.springframework.security.oauth2.core.user.OAuth2User oauth2User = 
            mock(org.springframework.security.oauth2.core.user.OAuth2User.class);
        when(authentication.getPrincipal()).thenReturn(oauth2User);

        // When
        String view = notificationController.listNotifications(0, 20, false, model, authentication);

        // Then
        assertEquals("redirect:/login", view);
    }
}

