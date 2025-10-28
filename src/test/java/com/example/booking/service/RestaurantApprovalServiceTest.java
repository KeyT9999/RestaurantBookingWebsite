package com.example.booking.service;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.booking.common.enums.RestaurantApprovalStatus;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.repository.RestaurantProfileRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("RestaurantApprovalService Tests")
public class RestaurantApprovalServiceTest {

    @Mock
    private RestaurantProfileRepository restaurantProfileRepository;

    @Mock
    private RestaurantNotificationService restaurantNotificationService;

    @InjectMocks
    private RestaurantApprovalService restaurantApprovalService;

    private RestaurantProfile pendingRestaurant;
    private RestaurantProfile approvedRestaurant;
    private RestaurantProfile rejectedRestaurant;
    private RestaurantOwner testOwner;

    @BeforeEach
    public void setUp() {
        UUID ownerId = UUID.randomUUID();
        testOwner = new RestaurantOwner();
        testOwner.setOwnerId(ownerId);

        // Pending restaurant
        pendingRestaurant = new RestaurantProfile();
        pendingRestaurant.setRestaurantId(1);
        pendingRestaurant.setRestaurantName("Pending Restaurant");
        pendingRestaurant.setApprovalStatus(RestaurantApprovalStatus.PENDING);
        pendingRestaurant.setOwner(testOwner);

        // Approved restaurant
        approvedRestaurant = new RestaurantProfile();
        approvedRestaurant.setRestaurantId(2);
        approvedRestaurant.setRestaurantName("Approved Restaurant");
        approvedRestaurant.setApprovalStatus(RestaurantApprovalStatus.APPROVED);
        approvedRestaurant.setOwner(testOwner);

        // Rejected restaurant
        rejectedRestaurant = new RestaurantProfile();
        rejectedRestaurant.setRestaurantId(3);
        rejectedRestaurant.setRestaurantName("Rejected Restaurant");
        rejectedRestaurant.setApprovalStatus(RestaurantApprovalStatus.REJECTED);
        rejectedRestaurant.setOwner(testOwner);
    }

    // ========== approveRestaurant() Tests ==========

    @Test
    @DisplayName("Should approve restaurant with pending status successfully")
    public void testApproveRestaurant_WithPendingStatus_ShouldApproveSuccessfully() {
        // Given
        Integer restaurantId = 1;
        String approvedBy = "admin";
        String approvalReason = "All documents verified";

        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.of(pendingRestaurant));
        when(restaurantProfileRepository.save(any(RestaurantProfile.class)))
            .thenReturn(pendingRestaurant);

        // When
        boolean result = restaurantApprovalService.approveRestaurant(restaurantId, approvedBy, approvalReason);

        // Then
        assertTrue(result);
        assertEquals(RestaurantApprovalStatus.APPROVED, pendingRestaurant.getApprovalStatus());
        assertEquals(approvedBy, pendingRestaurant.getApprovedBy());
        assertEquals(approvalReason, pendingRestaurant.getApprovalReason());
        assertNotNull(pendingRestaurant.getApprovedAt());
        verify(restaurantProfileRepository).save(pendingRestaurant);
        verify(restaurantNotificationService).sendApprovalNotification(pendingRestaurant);
    }

    @Test
    @DisplayName("Should set approval fields")
    public void testApproveRestaurant_ShouldSetApprovalFields() {
        // Given
        Integer restaurantId = 1;
        String approvedBy = "admin";
        String approvalReason = "Verified";

        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.of(pendingRestaurant));
        when(restaurantProfileRepository.save(any(RestaurantProfile.class)))
            .thenReturn(pendingRestaurant);

        // When
        boolean result = restaurantApprovalService.approveRestaurant(restaurantId, approvedBy, approvalReason);

        // Then
        assertTrue(result);
        assertEquals(approvedBy, pendingRestaurant.getApprovedBy());
        assertEquals(approvalReason, pendingRestaurant.getApprovalReason());
        assertNotNull(pendingRestaurant.getApprovedAt());
    }

    @Test
    @DisplayName("Should send notification to owner")
    public void testApproveRestaurant_ShouldSendNotificationToOwner() {
        // Given
        Integer restaurantId = 1;
        String approvedBy = "admin";
        String approvalReason = "Verified";

        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.of(pendingRestaurant));
        when(restaurantProfileRepository.save(any(RestaurantProfile.class)))
            .thenReturn(pendingRestaurant);

        // When
        restaurantApprovalService.approveRestaurant(restaurantId, approvedBy, approvalReason);

        // Then
        verify(restaurantNotificationService).sendApprovalNotification(pendingRestaurant);
    }

    @Test
    @DisplayName("Should return false for non-existent restaurant")
    public void testApproveRestaurant_WithNonExistentId_ShouldReturnFalse() {
        // Given
        Integer restaurantId = 9999;
        String approvedBy = "admin";
        String approvalReason = "Verified";

        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.empty());

        // When
        boolean result = restaurantApprovalService.approveRestaurant(restaurantId, approvedBy, approvalReason);

        // Then
        assertFalse(result);
        verify(restaurantProfileRepository, never()).save(any(RestaurantProfile.class));
        verify(restaurantNotificationService, never()).sendApprovalNotification(any());
    }

    @Test
    @DisplayName("Should return false for already approved restaurant")
    public void testApproveRestaurant_WithAlreadyApprovedStatus_ShouldReturnFalse() {
        // Given
        Integer restaurantId = 2;
        String approvedBy = "admin";
        String approvalReason = "Verified";

        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.of(approvedRestaurant));

        // When
        boolean result = restaurantApprovalService.approveRestaurant(restaurantId, approvedBy, approvalReason);

        // Then
        assertFalse(result);
        verify(restaurantProfileRepository, never()).save(any(RestaurantProfile.class));
        verify(restaurantNotificationService, never()).sendApprovalNotification(any());
    }

    @Test
    @DisplayName("Should return false for rejected restaurant")
    public void testApproveRestaurant_WithRejectedStatus_ShouldReturnFalse() {
        // Given
        Integer restaurantId = 3;
        String approvedBy = "admin";
        String approvalReason = "Verified";

        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.of(rejectedRestaurant));

        // When
        boolean result = restaurantApprovalService.approveRestaurant(restaurantId, approvedBy, approvalReason);

        // Then
        assertFalse(result);
        verify(restaurantProfileRepository, never()).save(any(RestaurantProfile.class));
        verify(restaurantNotificationService, never()).sendApprovalNotification(any());
    }

    @Test
    @DisplayName("Should clear rejection reason on approval")
    public void testApproveRestaurant_ShouldClearRejectionReason() {
        // Given
        Integer restaurantId = 1;
        String approvedBy = "admin";
        String approvalReason = "Verified";
        pendingRestaurant.setRejectionReason("Previous rejection reason");

        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.of(pendingRestaurant));
        when(restaurantProfileRepository.save(any(RestaurantProfile.class)))
            .thenReturn(pendingRestaurant);

        // When
        boolean result = restaurantApprovalService.approveRestaurant(restaurantId, approvedBy, approvalReason);

        // Then
        assertTrue(result);
        assertNull(pendingRestaurant.getRejectionReason());
    }

    // ========== rejectRestaurant() Tests ==========

    @Test
    @DisplayName("Should reject restaurant with pending status successfully")
    public void testRejectRestaurant_WithPendingStatus_ShouldRejectSuccessfully() {
        // Given
        Integer restaurantId = 1;
        String rejectedBy = "admin";
        String rejectionReason = "Insufficient documentation";

        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.of(pendingRestaurant));
        when(restaurantProfileRepository.save(any(RestaurantProfile.class)))
            .thenReturn(pendingRestaurant);

        // When
        boolean result = restaurantApprovalService.rejectRestaurant(restaurantId, rejectedBy, rejectionReason);

        // Then
        assertTrue(result);
        assertEquals(RestaurantApprovalStatus.REJECTED, pendingRestaurant.getApprovalStatus());
        assertEquals(rejectedBy, pendingRestaurant.getApprovedBy());
        assertEquals(rejectionReason, pendingRestaurant.getRejectionReason());
        assertNotNull(pendingRestaurant.getApprovedAt());
        verify(restaurantProfileRepository).save(pendingRestaurant);
        verify(restaurantNotificationService).sendRejectionNotification(pendingRestaurant, rejectionReason);
    }

    @Test
    @DisplayName("Should set rejection fields")
    public void testRejectRestaurant_ShouldSetRejectionFields() {
        // Given
        Integer restaurantId = 1;
        String rejectedBy = "admin";
        String rejectionReason = "Documentation issues";

        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.of(pendingRestaurant));
        when(restaurantProfileRepository.save(any(RestaurantProfile.class)))
            .thenReturn(pendingRestaurant);

        // When
        boolean result = restaurantApprovalService.rejectRestaurant(restaurantId, rejectedBy, rejectionReason);

        // Then
        assertTrue(result);
        assertEquals(rejectedBy, pendingRestaurant.getApprovedBy());
        assertEquals(rejectionReason, pendingRestaurant.getRejectionReason());
        assertNotNull(pendingRestaurant.getApprovedAt());
    }

    @Test
    @DisplayName("Should send notification with reason")
    public void testRejectRestaurant_ShouldSendNotificationWithReason() {
        // Given
        Integer restaurantId = 1;
        String rejectedBy = "admin";
        String rejectionReason = "Incomplete forms";

        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.of(pendingRestaurant));
        when(restaurantProfileRepository.save(any(RestaurantProfile.class)))
            .thenReturn(pendingRestaurant);

        // When
        restaurantApprovalService.rejectRestaurant(restaurantId, rejectedBy, rejectionReason);

        // Then
        verify(restaurantNotificationService).sendRejectionNotification(pendingRestaurant, rejectionReason);
    }

    @Test
    @DisplayName("Should return false for non-existent restaurant")
    public void testRejectRestaurant_WithNonExistentId_ShouldReturnFalse() {
        // Given
        Integer restaurantId = 9999;
        String rejectedBy = "admin";
        String rejectionReason = "Test";

        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.empty());

        // When
        boolean result = restaurantApprovalService.rejectRestaurant(restaurantId, rejectedBy, rejectionReason);

        // Then
        assertFalse(result);
        verify(restaurantProfileRepository, never()).save(any(RestaurantProfile.class));
        verify(restaurantNotificationService, never()).sendRejectionNotification(any(), any());
    }

    @Test
    @DisplayName("Should return false for already rejected restaurant")
    public void testRejectRestaurant_WithAlreadyRejectedStatus_ShouldReturnFalse() {
        // Given
        Integer restaurantId = 3;
        String rejectedBy = "admin";
        String rejectionReason = "Test";

        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.of(rejectedRestaurant));

        // When
        boolean result = restaurantApprovalService.rejectRestaurant(restaurantId, rejectedBy, rejectionReason);

        // Then
        assertFalse(result);
        verify(restaurantProfileRepository, never()).save(any(RestaurantProfile.class));
        verify(restaurantNotificationService, never()).sendRejectionNotification(any(), any());
    }

    @Test
    @DisplayName("Should return true for approved restaurant (approved can be rejected)")
    public void testRejectRestaurant_WithApprovedStatus_ShouldReturnTrue() {
        // Given
        Integer restaurantId = 2;
        String rejectedBy = "admin";
        String rejectionReason = "Violation of terms";

        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.of(approvedRestaurant));
        when(restaurantProfileRepository.save(any(RestaurantProfile.class)))
            .thenReturn(approvedRestaurant);

        // When
        boolean result = restaurantApprovalService.rejectRestaurant(restaurantId, rejectedBy, rejectionReason);

        // Then
        assertTrue(result);
        assertEquals(RestaurantApprovalStatus.REJECTED, approvedRestaurant.getApprovalStatus());
        verify(restaurantProfileRepository).save(approvedRestaurant);
        verify(restaurantNotificationService).sendRejectionNotification(approvedRestaurant, rejectionReason);
    }

    @Test
    @DisplayName("Should clear approval reason on rejection")
    public void testRejectRestaurant_ShouldClearApprovalReason() {
        // Given
        Integer restaurantId = 1;
        String rejectedBy = "admin";
        String rejectionReason = "Test";
        pendingRestaurant.setApprovalReason("Previous approval reason");

        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.of(pendingRestaurant));
        when(restaurantProfileRepository.save(any(RestaurantProfile.class)))
            .thenReturn(pendingRestaurant);

        // When
        boolean result = restaurantApprovalService.rejectRestaurant(restaurantId, rejectedBy, rejectionReason);

        // Then
        assertTrue(result);
        assertNull(pendingRestaurant.getApprovalReason());
    }
}

