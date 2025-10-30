package com.example.booking.dto.notification;

import com.example.booking.domain.NotificationType;
import com.example.booking.domain.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for NotificationForm.
 * Coverage Target: 100%
 * Test Cases: 6
 *
 * @author Professional Test Engineer
 */
@DisplayName("NotificationForm Tests")
class NotificationFormTest {

    @Test
    @DisplayName("Should create form with default values")
    void constructor_SetsDefaults() {
        // When
        NotificationForm form = new NotificationForm();

        // Then
        assertThat(form).isNotNull();
        assertThat(form.getPriority()).isEqualTo(0);
        assertThat(form.getPublishAt()).isNotNull();
        assertThat(form.getPublishAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should set and get all basic fields")
    void settersGetters_BasicFields_WorkCorrectly() {
        // Given
        NotificationForm form = new NotificationForm();

        // When
        form.setType(NotificationType.BOOKING_CONFIRMED);
        form.setTitle("Test Notification");
        form.setContent("This is a test notification content");
        form.setLinkUrl("https://example.com/booking/123");
        form.setPriority(5);

        // Then
        assertThat(form.getType()).isEqualTo(NotificationType.BOOKING_CONFIRMED);
        assertThat(form.getTitle()).isEqualTo("Test Notification");
        assertThat(form.getContent()).isEqualTo("This is a test notification content");
        assertThat(form.getLinkUrl()).isEqualTo("https://example.com/booking/123");
        assertThat(form.getPriority()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should set and get date/time fields")
    void settersGetters_DateTimeFields_WorkCorrectly() {
        // Given
        NotificationForm form = new NotificationForm();
        LocalDateTime publishAt = LocalDateTime.now().plusHours(1);
        LocalDateTime expireAt = LocalDateTime.now().plusDays(7);

        // When
        form.setPublishAt(publishAt);
        form.setExpireAt(expireAt);

        // Then
        assertThat(form.getPublishAt()).isEqualTo(publishAt);
        assertThat(form.getExpireAt()).isEqualTo(expireAt);
    }

    @Test
    @DisplayName("Should handle ALL audience type")
    void audience_All_WorksCorrectly() {
        // Given
        NotificationForm form = new NotificationForm();

        // When
        form.setAudience(NotificationForm.AudienceType.ALL);

        // Then
        assertThat(form.getAudience()).isEqualTo(NotificationForm.AudienceType.ALL);
    }

    @Test
    @DisplayName("Should handle ROLE audience type with target roles")
    void audience_Role_WithTargetRoles_WorksCorrectly() {
        // Given
        NotificationForm form = new NotificationForm();
        Set<UserRole> roles = Set.of(UserRole.RESTAURANT_OWNER, UserRole.CUSTOMER);

        // When
        form.setAudience(NotificationForm.AudienceType.ROLE);
        form.setTargetRoles(roles);

        // Then
        assertThat(form.getAudience()).isEqualTo(NotificationForm.AudienceType.ROLE);
        assertThat(form.getTargetRoles()).containsExactlyInAnyOrder(
            UserRole.RESTAURANT_OWNER, UserRole.CUSTOMER
        );
    }

    @Test
    @DisplayName("Should handle USER audience type with target user IDs")
    void audience_User_WithTargetUserIds_WorksCorrectly() {
        // Given
        NotificationForm form = new NotificationForm();
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        Set<UUID> userIds = Set.of(userId1, userId2);

        // When
        form.setAudience(NotificationForm.AudienceType.USER);
        form.setTargetUserIds(userIds);

        // Then
        assertThat(form.getAudience()).isEqualTo(NotificationForm.AudienceType.USER);
        assertThat(form.getTargetUserIds()).containsExactlyInAnyOrder(userId1, userId2);
    }
}

