package com.example.booking.service;

import com.example.booking.domain.Notification;
import com.example.booking.domain.RestaurantBankAccount;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.common.enums.WithdrawalStatus;
import com.example.booking.domain.WithdrawalRequest;
import com.example.booking.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WithdrawalNotificationServiceTest {

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
    private RestaurantBankAccount bankAccount;

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

        bankAccount = new RestaurantBankAccount();
        bankAccount.setAccountId(1);
        bankAccount.setAccountNumber("1234567890");
        bankAccount.setBankName("Test Bank");
        bankAccount.setRestaurant(restaurant);

        withdrawalRequest = new WithdrawalRequest();
        withdrawalRequest.setRequestId(1);
        withdrawalRequest.setRestaurant(restaurant);
        withdrawalRequest.setBankAccount(bankAccount);
        withdrawalRequest.setAmount(new BigDecimal("1000000"));
        withdrawalRequest.setStatus(WithdrawalStatus.PENDING);
        withdrawalRequest.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void shouldNotifyWithdrawalCreated() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        withdrawalNotificationService.notifyWithdrawalCreated(withdrawalRequest);

        verify(notificationRepository).save(any(Notification.class));
        verify(emailService).sendEmail(eq("owner@restaurant.com"), anyString(), anyString());
    }

    @Test
    void shouldNotifyWithdrawalApproved() {
        withdrawalRequest.setStatus(WithdrawalStatus.APPROVED);
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        withdrawalNotificationService.notifyWithdrawalApproved(withdrawalRequest);

        verify(notificationRepository).save(any(Notification.class));
        verify(emailService).sendEmail(eq("owner@restaurant.com"), anyString(), anyString());
    }

    @Test
    void shouldNotifyWithdrawalRejected() {
        withdrawalRequest.setStatus(WithdrawalStatus.REJECTED);
        withdrawalRequest.setRejectionReason("Insufficient funds");
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        withdrawalNotificationService.notifyWithdrawalRejected(withdrawalRequest);

        verify(notificationRepository).save(any(Notification.class));
        verify(emailService).sendEmail(eq("owner@restaurant.com"), anyString(), anyString());
    }

    @Test
    void shouldNotifyWithdrawalRejected_WithoutReason() {
        withdrawalRequest.setStatus(WithdrawalStatus.REJECTED);
        withdrawalRequest.setRejectionReason(null);
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        withdrawalNotificationService.notifyWithdrawalRejected(withdrawalRequest);

        verify(notificationRepository).save(any(Notification.class));
        verify(emailService).sendEmail(eq("owner@restaurant.com"), anyString(), anyString());
    }

    @Test
    void shouldNotifyWithdrawalSucceeded() {
        withdrawalRequest.setStatus(WithdrawalStatus.SUCCEEDED);
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        withdrawalNotificationService.notifyWithdrawalSucceeded(withdrawalRequest);

        verify(notificationRepository).save(any(Notification.class));
        verify(emailService).sendEmail(eq("owner@restaurant.com"), anyString(), anyString());
    }

    @Test
    void shouldNotifyWithdrawalFailed() {
        withdrawalRequest.setStatus(WithdrawalStatus.FAILED);
        withdrawalRequest.setRejectionReason("Bank error");
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        withdrawalNotificationService.notifyWithdrawalFailed(withdrawalRequest);

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void shouldHandleExceptionGracefully() {
        withdrawalRequest.getRestaurant().getOwner().getUser().setEmail(null); // Cause exception

        withdrawalNotificationService.notifyWithdrawalCreated(withdrawalRequest);

        // Should not throw exception, just log error
        verify(notificationRepository, never()).save(any(Notification.class));
    }
}

