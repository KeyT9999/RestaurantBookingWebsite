package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.repository.NotificationRepository;
import com.example.booking.repository.UserRepository;

/**
 * Unit tests for RestaurantNotificationService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RestaurantNotificationService Tests")
public class RestaurantNotificationServiceTest {

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
        UUID userId = UUID.randomUUID();
        
        user = new User();
        user.setId(userId);
        user.setEmail("owner@test.com");
        user.setRole(UserRole.RESTAURANT_OWNER);
        user.setFullName("Restaurant Owner");

        owner = new RestaurantOwner();
        owner.setUser(user);

        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");
        restaurant.setOwner(owner);
    }

    // ========== sendApprovalNotification() Tests ==========

    @Test
    @DisplayName("shouldSendApprovalNotification_successfully")
    void shouldSendApprovalNotification_successfully() {
        // Given
        doNothing().when(emailService).sendRestaurantApprovalEmail(
            anyString(), anyString(), anyString(), anyString());

        // When
        restaurantNotificationService.sendApprovalNotification(restaurant);

        // Then
        verify(emailService, times(1)).sendRestaurantApprovalEmail(
            anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("shouldHandleMissingOwnerEmail_gracefully")
    void shouldHandleMissingOwnerEmail_gracefully() {
        // Given
        user.setEmail(null);
        restaurant.setOwner(owner);

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> {
            restaurantNotificationService.sendApprovalNotification(restaurant);
        });
    }

    // ========== sendRejectionNotification() Tests ==========

    @Test
    @DisplayName("shouldSendRejectionNotification_successfully")
    void shouldSendRejectionNotification_successfully() {
        // Given
        String reason = "Incomplete documents";
        doNothing().when(emailService).sendRestaurantRejectionEmail(
            anyString(), anyString(), anyString(), anyString());

        // When
        restaurantNotificationService.sendRejectionNotification(restaurant, reason);

        // Then
        verify(emailService, times(1)).sendRestaurantRejectionEmail(
            anyString(), anyString(), anyString(), anyString());
    }

    // ========== sendSuspensionNotification() Tests ==========

    @Test
    @DisplayName("shouldSendSuspensionNotification_successfully")
    void shouldSendSuspensionNotification_successfully() {
        // Given
        String reason = "Policy violation";
        doNothing().when(emailService).sendRestaurantSuspensionEmail(
            anyString(), anyString(), anyString(), anyString());

        // When
        restaurantNotificationService.sendSuspensionNotification(restaurant, reason);

        // Then
        verify(emailService, times(1)).sendRestaurantSuspensionEmail(
            anyString(), anyString(), anyString(), anyString());
    }

    // ========== Legacy Methods Tests ==========

    @Test
    @DisplayName("shouldNotifyRestaurantApproval_successfully")
    void shouldNotifyRestaurantApproval_successfully() {
        // Given
        doNothing().when(emailService).sendRestaurantApprovalEmail(
            anyString(), anyString(), anyString(), anyString());

        // When
        restaurantNotificationService.notifyRestaurantApproval(restaurant, "admin", "approved");

        // Then
        verify(emailService, times(1)).sendRestaurantApprovalEmail(
            anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("shouldNotifyRestaurantRejection_successfully")
    void shouldNotifyRestaurantRejection_successfully() {
        // Given
        doNothing().when(emailService).sendRestaurantRejectionEmail(
            anyString(), anyString(), anyString(), anyString());

        // When
        restaurantNotificationService.notifyRestaurantRejection(restaurant, "admin", "rejected");

        // Then
        verify(emailService, times(1)).sendRestaurantRejectionEmail(
            anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("shouldNotifyRestaurantSuspension_successfully")
    void shouldNotifyRestaurantSuspension_successfully() {
        // Given
        doNothing().when(emailService).sendRestaurantSuspensionEmail(
            anyString(), anyString(), anyString(), anyString());

        // When
        restaurantNotificationService.notifyRestaurantSuspension(restaurant, "admin", "suspended");

        // Then
        verify(emailService, times(1)).sendRestaurantSuspensionEmail(
            anyString(), anyString(), anyString(), anyString());
    }
}

