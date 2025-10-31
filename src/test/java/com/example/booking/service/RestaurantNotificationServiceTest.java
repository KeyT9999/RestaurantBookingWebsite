package com.example.booking.service;

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

    // ========== Build Email Content Tests - Testing through public methods ==========

    @Test
    @DisplayName("shouldBuildApprovalEmailContent_ShouldContainRestaurantInfo")
    void shouldBuildApprovalEmailContent_ShouldContainRestaurantInfo() {
        // Given
        restaurant.setRestaurantName("Test Restaurant");
        restaurant.setAddress("123 Test St");
        restaurant.setCuisineType("Vietnamese");
        
        // The buildApprovalEmailContent is called internally by sendApprovalNotification
        // We can verify by checking if emailService.sendRestaurantApprovalEmail is called
        doNothing().when(emailService).sendRestaurantApprovalEmail(
            anyString(), anyString(), anyString(), anyString());

        // When
        restaurantNotificationService.sendApprovalNotification(restaurant);

        // Then - Verify email content is built and sent (contains restaurant info)
        verify(emailService, times(1)).sendRestaurantApprovalEmail(
            anyString(), anyString(), 
            argThat(content -> content != null && content.contains("Test Restaurant")),
            anyString());
    }

    @Test
    @DisplayName("shouldBuildRejectionEmailContent_ShouldContainRejectionReason")
    void shouldBuildRejectionEmailContent_ShouldContainRejectionReason() {
        // Given
        String rejectionReason = "Insufficient documentation";
        doNothing().when(emailService).sendRestaurantRejectionEmail(
            anyString(), anyString(), anyString(), anyString());

        // When
        restaurantNotificationService.sendRejectionNotification(restaurant, rejectionReason);

        // Then - Verify email content contains rejection reason
        verify(emailService, times(1)).sendRestaurantRejectionEmail(
            anyString(), anyString(),
            argThat(content -> content != null && content.contains(rejectionReason)),
            anyString());
    }

    @Test
    @DisplayName("shouldBuildSuspensionEmailContent_ShouldContainSuspensionReason")
    void shouldBuildSuspensionEmailContent_ShouldContainSuspensionReason() {
        // Given
        String suspensionReason = "Policy violation";
        doNothing().when(emailService).sendRestaurantSuspensionEmail(
            anyString(), anyString(), anyString(), anyString());

        // When
        restaurantNotificationService.sendSuspensionNotification(restaurant, suspensionReason);

        // Then - Verify email content contains suspension reason
        verify(emailService, times(1)).sendRestaurantSuspensionEmail(
            anyString(), anyString(),
            argThat(content -> content != null && content.contains(suspensionReason)),
            anyString());
    }


    @Test
    @DisplayName("shouldBuildResubmitEmailContent_ShouldContainResubmitReason")
    void shouldBuildResubmitEmailContent_ShouldContainResubmitReason() {
        // Given
        String resubmitReason = "Please update restaurant information";
        restaurant.setRestaurantName("Test Restaurant");
        
        // sendResubmitNotification uses buildResubmitEmailContent internally
        doNothing().when(emailService).sendRestaurantResubmitEmail(
            anyString(), anyString(), anyString(), anyString());

        // When
        restaurantNotificationService.sendResubmitNotification(restaurant, resubmitReason);

        // Then - Verify email content is built and sent
        verify(emailService, times(1)).sendRestaurantResubmitEmail(
            anyString(), anyString(),
            anyString(), // subject
            argThat(content -> content != null && content.contains(resubmitReason)));
    }

    @Test
    @DisplayName("shouldBuildActivationEmailContent_ShouldContainActivationInfo")
    void shouldBuildActivationEmailContent_ShouldContainActivationInfo() {
        // Given
        String activationReason = "Restaurant reactivated";
        restaurant.setRestaurantName("Test Restaurant");
        
        doNothing().when(emailService).sendRestaurantActivationEmail(
            anyString(), anyString(), anyString(), anyString());

        // When
        restaurantNotificationService.sendActivationNotification(restaurant, activationReason);

        // Then - Verify email content is built
        verify(emailService, times(1)).sendRestaurantActivationEmail(
            anyString(), anyString(),
            anyString(), // subject
            argThat(content -> content != null && content.contains("Chào mừng trở lại")));
    }

    @Test
    @DisplayName("shouldBuildEmailContent_WithNullFields_ShouldHandleGracefully")
    void shouldBuildEmailContent_WithNullFields_ShouldHandleGracefully() {
        // Given
        restaurant.setRestaurantName(null);
        restaurant.setAddress(null);
        restaurant.setCuisineType(null);
        
        doNothing().when(emailService).sendRestaurantApprovalEmail(
            anyString(), anyString(), anyString(), anyString());

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> {
            restaurantNotificationService.sendApprovalNotification(restaurant);
        });
        
        verify(emailService, times(1)).sendRestaurantApprovalEmail(
            anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("shouldBuildRejectionEmailContent_WithNullReason_ShouldUseDefault")
    void shouldBuildRejectionEmailContent_WithNullReason_ShouldUseDefault() {
        // Given
        String nullReason = null;
        doNothing().when(emailService).sendRestaurantRejectionEmail(
            anyString(), anyString(), anyString(), anyString());

        // When
        restaurantNotificationService.sendRejectionNotification(restaurant, nullReason);

        // Then - Should use default message when reason is null
        verify(emailService, times(1)).sendRestaurantRejectionEmail(
            anyString(), anyString(),
            argThat(content -> content != null && 
                (content.contains("Không đáp ứng yêu cầu") || content.contains("Không đáp ứng"))),
            anyString());
    }
}

