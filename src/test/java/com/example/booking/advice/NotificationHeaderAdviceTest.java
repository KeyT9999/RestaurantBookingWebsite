package com.example.booking.advice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.example.booking.domain.User;
import com.example.booking.dto.notification.NotificationView;
import com.example.booking.service.NotificationService;
import com.example.booking.web.advice.NotificationHeaderAdvice;

class NotificationHeaderAdviceTest {

    @Mock private NotificationService notificationService;
    private NotificationHeaderAdvice advice;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        advice = new NotificationHeaderAdvice();
        try {
            var f = NotificationHeaderAdvice.class.getDeclaredField("notificationService");
            f.setAccessible(true);
            f.set(advice, notificationService);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private Authentication authWithUser(UUID id) {
        User u = new User();
        u.setId(id);
        return new UsernamePasswordAuthenticationToken(u, "pwd");
    }

    // TC NA-001
    @Test
    @DisplayName("returns unread count for authenticated user (NA-001)")
    void unreadCount_authenticated() {
        UUID id = UUID.randomUUID();
        when(notificationService.countUnreadByUserId(id)).thenReturn(7L);
        assertThat(advice.unreadCount(authWithUser(id))).isEqualTo(7L);
    }

    // TC NA-002
    @Test
    @DisplayName("returns 0 for anonymous (NA-002)")
    void unreadCount_anonymous() {
        assertThat(advice.unreadCount(null)).isEqualTo(0L);
    }

    // TC NA-003
    @Test
    @DisplayName("returns latest notifications (NA-003)")
    void latestNotifications_ok() {
        UUID id = UUID.randomUUID();
        when(notificationService.getLatestNotifications(id)).thenReturn(List.of(mock(NotificationView.class), mock(NotificationView.class), mock(NotificationView.class)));
        assertThat(advice.latestNotifications(authWithUser(id))).hasSize(3);
    }

    // TC NA-004
    @Test
    @DisplayName("returns empty list for OAuth2User principal (NA-004)")
    void latestNotifications_oauth2User() {
        Authentication a = new UsernamePasswordAuthenticationToken(mock(org.springframework.security.oauth2.core.user.OAuth2User.class), "pwd");
        assertThat(advice.latestNotifications(a)).isEmpty();
    }
}


