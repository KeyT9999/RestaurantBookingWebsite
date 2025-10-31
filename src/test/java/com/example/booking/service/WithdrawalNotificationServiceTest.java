package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.booking.common.enums.WithdrawalStatus;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.domain.WithdrawalRequest;
import com.example.booking.repository.NotificationRepository;

/**
 * Unit tests for WithdrawalNotificationService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WithdrawalNotificationService Tests")
public class WithdrawalNotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private WithdrawalNotificationService withdrawalNotificationService;

    private WithdrawalRequest withdrawalRequest;
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

        owner = new RestaurantOwner();
        owner.setUser(user);

        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");
        restaurant.setOwner(owner);

        withdrawalRequest = new WithdrawalRequest();
        withdrawalRequest.setRequestId(1);
        withdrawalRequest.setRestaurant(restaurant);
        withdrawalRequest.setAmount(new BigDecimal("1000000"));
        withdrawalRequest.setStatus(WithdrawalStatus.PENDING);
    }

    // ========== notifyWithdrawalCreated() Tests ==========

    @Test
    @DisplayName("shouldNotifyWithdrawalCreated_successfully")
    void shouldNotifyWithdrawalCreated_successfully() {
        // Given
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        withdrawalNotificationService.notifyWithdrawalCreated(withdrawalRequest);

        // Then
        verify(notificationRepository, times(1)).save(any());
    }

    // ========== notifyWithdrawalApproved() Tests ==========

    @Test
    @DisplayName("shouldNotifyWithdrawalApproved_successfully")
    void shouldNotifyWithdrawalApproved_successfully() {
        // Given
        withdrawalRequest.setStatus(WithdrawalStatus.APPROVED);
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        withdrawalNotificationService.notifyWithdrawalApproved(withdrawalRequest);

        // Then
        verify(notificationRepository, times(1)).save(any());
    }

    // ========== notifyWithdrawalRejected() Tests ==========

    @Test
    @DisplayName("shouldNotifyWithdrawalRejected_successfully")
    void shouldNotifyWithdrawalRejected_successfully() {
        // Given
        withdrawalRequest.setStatus(WithdrawalStatus.REJECTED);
        withdrawalRequest.setRejectionReason("Insufficient balance");
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        withdrawalNotificationService.notifyWithdrawalRejected(withdrawalRequest);

        // Then
        verify(notificationRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("shouldNotifyWithdrawalRejected_withNullReason")
    void shouldNotifyWithdrawalRejected_withNullReason() {
        // Given
        withdrawalRequest.setStatus(WithdrawalStatus.REJECTED);
        withdrawalRequest.setRejectionReason(null);
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        withdrawalNotificationService.notifyWithdrawalRejected(withdrawalRequest);

        // Then
        verify(notificationRepository, times(1)).save(any());
    }

    // ========== notifyWithdrawalSucceeded() Tests ==========

    @Test
    @DisplayName("shouldNotifyWithdrawalSucceeded_successfully")
    void shouldNotifyWithdrawalSucceeded_successfully() {
        // Given
        withdrawalRequest.setStatus(WithdrawalStatus.SUCCEEDED);
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        withdrawalNotificationService.notifyWithdrawalSucceeded(withdrawalRequest);

        // Then
        verify(notificationRepository, times(1)).save(any());
    }

    // ========== notifyWithdrawalFailed() Tests ==========

    @Test
    @DisplayName("shouldNotifyWithdrawalFailed_successfully")
    void shouldNotifyWithdrawalFailed_successfully() {
        // Given
        withdrawalRequest.setStatus(WithdrawalStatus.FAILED);
        // WithdrawalRequest doesn't have setFailureReason, using rejectionReason
        withdrawalRequest.setRejectionReason("Bank transfer error");
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        withdrawalNotificationService.notifyWithdrawalFailed(withdrawalRequest);

        // Then
        verify(notificationRepository, times(1)).save(any());
    }

    // ========== Error Handling Tests ==========

    @Test
    @DisplayName("shouldHandleException_gracefully")
    void shouldHandleException_gracefully() {
        // Given
        when(notificationRepository.save(any())).thenThrow(new RuntimeException("Database error"));

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> {
            withdrawalNotificationService.notifyWithdrawalCreated(withdrawalRequest);
        });
    }
}

