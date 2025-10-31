package com.example.booking.dto.notification;

import com.example.booking.domain.Notification;
import com.example.booking.domain.NotificationType;
import com.example.booking.domain.UserRole;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NotificationDtoTest {

    @Test
    void notificationViewConstructorShouldCopyDomainFields() {
        Notification notification = new Notification();
        notification.setNotificationId(42);
        notification.setType(NotificationType.BOOKING_CONFIRMED);
        notification.setTitle("Booking confirmed");
        notification.setContent("Your reservation is confirmed");
        notification.setLinkUrl("https://example.com/booking/42");
        LocalDateTime publishAt = LocalDateTime.now().minusHours(1);
        LocalDateTime readAt = LocalDateTime.now();
        notification.setPublishAt(publishAt);
        notification.setReadAt(readAt);
        notification.setPriority(7);

        NotificationView view = new NotificationView(notification);

        assertEquals(42, view.getId());
        assertEquals(NotificationType.BOOKING_CONFIRMED, view.getType());
        assertEquals("Booking confirmed", view.getTitle());
        assertEquals("Your reservation is confirmed", view.getContent());
        assertEquals("https://example.com/booking/42", view.getLinkUrl());
        assertEquals(publishAt, view.getPublishAt());
        assertEquals(readAt, view.getReadAt());
        assertEquals(7, view.getPriority());
        assertFalse(view.isUnread(), "readAt should mark notification as read");
    }

    @Test
    void notificationViewSettersShouldAllowMutation() {
        NotificationView view = new NotificationView();
        view.setId(5);
        view.setType(NotificationType.SYSTEM_ANNOUNCEMENT);
        view.setTitle("System message");
        view.setContent("Important update");
        view.setLinkUrl("https://example.com/system");
        LocalDateTime publishAt = LocalDateTime.now();
        LocalDateTime readAt = null;
        view.setPublishAt(publishAt);
        view.setReadAt(readAt);
        view.setPriority(3);
        view.setUnread(true);

        assertEquals(5, view.getId());
        assertEquals(NotificationType.SYSTEM_ANNOUNCEMENT, view.getType());
        assertEquals("System message", view.getTitle());
        assertEquals("Important update", view.getContent());
        assertEquals("https://example.com/system", view.getLinkUrl());
        assertEquals(publishAt, view.getPublishAt());
        assertNull(view.getReadAt());
        assertEquals(3, view.getPriority());
        assertTrue(view.isUnread());
    }

    @Test
    void adminNotificationSummaryShouldExposeAllFields() {
        AdminNotificationSummary summary = new AdminNotificationSummary();
        summary.setId(99);
        summary.setType(NotificationType.PAYMENT_SUCCESS);
        summary.setTitle("Payment completed");
        summary.setContent("Invoice #99 has been paid");
        LocalDateTime publishAt = LocalDateTime.now();
        summary.setPublishAt(publishAt);
        summary.setTotalRecipients(500L);
        summary.setCustomerRecipients(350L);
        summary.setRestaurantOwnerRecipients(150L);

        assertEquals(99, summary.getId());
        assertEquals(NotificationType.PAYMENT_SUCCESS, summary.getType());
        assertEquals("Payment completed", summary.getTitle());
        assertEquals("Invoice #99 has been paid", summary.getContent());
        assertEquals(publishAt, summary.getPublishAt());
        assertEquals(500L, summary.getTotalRecipients());
        assertEquals(350L, summary.getCustomerRecipients());
        assertEquals(150L, summary.getRestaurantOwnerRecipients());
    }

    @Test
    void notificationFilterSettersShouldPersistValues() {
        NotificationFilter filter = new NotificationFilter();
        filter.setUnreadOnly(Boolean.TRUE);
        filter.setType(NotificationType.RESTAURANT_APPROVED);
        LocalDateTime from = LocalDateTime.now().minusDays(2);
        LocalDateTime to = LocalDateTime.now().plusDays(1);
        filter.setFrom(from);
        filter.setTo(to);

        assertTrue(filter.getUnreadOnly());
        assertEquals(NotificationType.RESTAURANT_APPROVED, filter.getType());
        assertEquals(from, filter.getFrom());
        assertEquals(to, filter.getTo());
    }

    @Test
    void notificationFormShouldApplyDefaultsAndAllowUpdates() {
        NotificationForm form = new NotificationForm();

        assertEquals(0, form.getPriority());
        assertNotNull(form.getPublishAt(), "PublishAt should default to now");
        assertNull(form.getExpireAt());
        assertNull(form.getTitle());
        assertNull(form.getAudience());

        form.setType(NotificationType.REVIEW_REQUEST);
        form.setTitle("Rate your visit");
        form.setContent("Please review your dining experience");
        form.setLinkUrl("https://example.com/review");
        form.setPriority(2);
        LocalDateTime publishAt = LocalDateTime.now().plusDays(1);
        LocalDateTime expireAt = publishAt.plusDays(3);
        form.setPublishAt(publishAt);
        form.setExpireAt(expireAt);
        form.setAudience(NotificationForm.AudienceType.ROLE);
        Set<UserRole> roles = EnumSet.of(UserRole.CUSTOMER, UserRole.RESTAURANT_OWNER);
        form.setTargetRoles(roles);
        Set<UUID> userIds = Set.of(UUID.randomUUID(), UUID.randomUUID());
        form.setTargetUserIds(userIds);

        assertEquals(NotificationType.REVIEW_REQUEST, form.getType());
        assertEquals("Rate your visit", form.getTitle());
        assertEquals("Please review your dining experience", form.getContent());
        assertEquals("https://example.com/review", form.getLinkUrl());
        assertEquals(2, form.getPriority());
        assertEquals(publishAt, form.getPublishAt());
        assertEquals(expireAt, form.getExpireAt());
        assertEquals(NotificationForm.AudienceType.ROLE, form.getAudience());
        assertEquals(roles, form.getTargetRoles());
        assertEquals(userIds, form.getTargetUserIds());
    }
}

