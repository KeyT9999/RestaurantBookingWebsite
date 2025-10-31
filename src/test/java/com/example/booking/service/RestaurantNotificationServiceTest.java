package com.example.booking.service;

import com.example.booking.domain.Notification;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.repository.NotificationRepository;
import com.example.booking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantNotificationServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RestaurantNotificationService restaurantNotificationService;

    private RestaurantProfile restaurant;
    private RestaurantOwner owner;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("owner@restaurant.com");
        user.setFullName("Restaurant Owner");
        user.setRole(UserRole.RESTAURANT_OWNER);

        owner = new RestaurantOwner();
        owner.setUser(user);

        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");
        restaurant.setOwner(owner);
    }

    @Test
    void shouldSendApprovalNotification() {
        doNothing().when(emailService).sendRestaurantApprovalEmail(
                anyString(), anyString(), anyString(), anyString());
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        restaurantNotificationService.sendApprovalNotification(restaurant);

        verify(emailService).sendRestaurantApprovalEmail(
                eq("owner@restaurant.com"),
                eq("Test Restaurant"),
                anyString(),
                anyString());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void shouldSendRejectionNotification() {
        String rejectionReason = "Missing documents";
        doNothing().when(emailService).sendRestaurantRejectionEmail(
                anyString(), anyString(), anyString(), anyString());
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        restaurantNotificationService.sendRejectionNotification(restaurant, rejectionReason);

        verify(emailService).sendRestaurantRejectionEmail(
                eq("owner@restaurant.com"),
                eq("Test Restaurant"),
                anyString(),
                anyString());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void shouldSendSuspensionNotification() {
        String suspensionReason = "Violation of terms";
        doNothing().when(emailService).sendRestaurantSuspensionEmail(
                anyString(), anyString(), anyString(), anyString());
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        restaurantNotificationService.sendSuspensionNotification(restaurant, suspensionReason);

        verify(emailService).sendRestaurantSuspensionEmail(
                eq("owner@restaurant.com"),
                eq("Test Restaurant"),
                anyString(),
                anyString());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void shouldSendApprovalNotification_LegacyMethod() {
        doNothing().when(emailService).sendRestaurantApprovalEmail(
                anyString(), anyString(), anyString(), anyString());
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        restaurantNotificationService.notifyRestaurantApproval(restaurant, "admin", "All documents verified");

        verify(emailService).sendRestaurantApprovalEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void shouldSendRejectionNotification_LegacyMethod() {
        doNothing().when(emailService).sendRestaurantRejectionEmail(
                anyString(), anyString(), anyString(), anyString());
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        restaurantNotificationService.notifyRestaurantRejection(restaurant, "admin", "Missing documents");

        verify(emailService).sendRestaurantRejectionEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void shouldSendSuspensionNotification_LegacyMethod() {
        doNothing().when(emailService).sendRestaurantSuspensionEmail(
                anyString(), anyString(), anyString(), anyString());
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        restaurantNotificationService.notifyRestaurantSuspension(restaurant, "admin", "Violation");

        verify(emailService).sendRestaurantSuspensionEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void shouldNotSendNotification_WhenOwnerEmailIsNull() {
        user.setEmail(null);
        restaurant.setOwner(owner);

        restaurantNotificationService.sendApprovalNotification(restaurant);

        verify(emailService, never()).sendRestaurantApprovalEmail(anyString(), anyString(), anyString(), anyString());
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void shouldNotSendNotification_WhenOwnerIsNull() {
        restaurant.setOwner(null);

        restaurantNotificationService.sendApprovalNotification(restaurant);

        verify(emailService, never()).sendRestaurantApprovalEmail(anyString(), anyString(), anyString(), anyString());
        verify(notificationRepository, never()).save(any(Notification.class));
    }
}

