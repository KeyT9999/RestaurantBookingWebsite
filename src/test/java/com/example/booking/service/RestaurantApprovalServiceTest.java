package com.example.booking.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.booking.common.enums.RestaurantApprovalStatus;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
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
    private RestaurantProfile suspendedRestaurant;
    private RestaurantOwner testOwner;
    private User testUser;

    @BeforeEach
    public void setUp() {
        UUID ownerId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(ownerId);
        testUser.setUsername("owner123");
        
        testOwner = new RestaurantOwner();
        testOwner.setOwnerId(ownerId);
        testOwner.setUser(testUser);

        // Pending restaurant
        pendingRestaurant = new RestaurantProfile();
        pendingRestaurant.setRestaurantId(1);
        pendingRestaurant.setRestaurantName("Pending Restaurant");
        pendingRestaurant.setApprovalStatus(RestaurantApprovalStatus.PENDING);
        pendingRestaurant.setOwner(testOwner);
        pendingRestaurant.setAddress("123 Main St");
        pendingRestaurant.setCuisineType("Vietnamese");

        // Approved restaurant
        approvedRestaurant = new RestaurantProfile();
        approvedRestaurant.setRestaurantId(2);
        approvedRestaurant.setRestaurantName("Approved Restaurant");
        approvedRestaurant.setApprovalStatus(RestaurantApprovalStatus.APPROVED);
        approvedRestaurant.setOwner(testOwner);
        approvedRestaurant.setAddress("456 Oak Ave");
        approvedRestaurant.setCuisineType("Italian");

        // Rejected restaurant
        rejectedRestaurant = new RestaurantProfile();
        rejectedRestaurant.setRestaurantId(3);
        rejectedRestaurant.setRestaurantName("Rejected Restaurant");
        rejectedRestaurant.setApprovalStatus(RestaurantApprovalStatus.REJECTED);
        rejectedRestaurant.setOwner(testOwner);
        rejectedRestaurant.setAddress("789 Pine Rd");
        rejectedRestaurant.setCuisineType("Japanese");

        // Suspended restaurant
        suspendedRestaurant = new RestaurantProfile();
        suspendedRestaurant.setRestaurantId(4);
        suspendedRestaurant.setRestaurantName("Suspended Restaurant");
        suspendedRestaurant.setApprovalStatus(RestaurantApprovalStatus.SUSPENDED);
        suspendedRestaurant.setOwner(testOwner);
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
    @DisplayName("Should return false when canBeApproved() returns false")
    public void testApproveRestaurant_WhenCannotBeApproved_ShouldReturnFalse() {
        // Given
        Integer restaurantId = 2;
        String approvedBy = "admin";
        String approvalReason = "Verified";

        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.of(approvedRestaurant));
        // Mock canBeApproved to return false
        // Note: This depends on RestaurantProfile implementation

        // When
        boolean result = restaurantApprovalService.approveRestaurant(restaurantId, approvedBy, approvalReason);

        // Then
        assertFalse(result);
        verify(restaurantProfileRepository, never()).save(any(RestaurantProfile.class));
        verify(restaurantNotificationService, never()).sendApprovalNotification(any());
    }

    @Test
    @DisplayName("Should handle exception during approval")
    public void testApproveRestaurant_WithException_ShouldReturnFalse() {
        // Given
        Integer restaurantId = 1;
        String approvedBy = "admin";
        String approvalReason = "Verified";

        when(restaurantProfileRepository.findById(restaurantId))
            .thenThrow(new RuntimeException("Database error"));

        // When
        boolean result = restaurantApprovalService.approveRestaurant(restaurantId, approvedBy, approvalReason);

        // Then
        assertFalse(result);
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
    @DisplayName("Should handle exception during rejection")
    public void testRejectRestaurant_WithException_ShouldReturnFalse() {
        // Given
        Integer restaurantId = 1;
        String rejectedBy = "admin";
        String rejectionReason = "Test";

        when(restaurantProfileRepository.findById(restaurantId))
            .thenThrow(new RuntimeException("Database error"));

        // When
        boolean result = restaurantApprovalService.rejectRestaurant(restaurantId, rejectedBy, rejectionReason);

        // Then
        assertFalse(result);
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

    // ========== suspendRestaurant() Tests ==========

    @Test
    @DisplayName("Should suspend restaurant successfully")
    public void testSuspendRestaurant_ShouldSuspendSuccessfully() {
        // Given
        Integer restaurantId = 2;
        String suspendedBy = "admin";
        String suspensionReason = "Policy violation";

        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.of(approvedRestaurant));
        when(restaurantProfileRepository.save(any(RestaurantProfile.class)))
            .thenReturn(approvedRestaurant);

        // When
        boolean result = restaurantApprovalService.suspendRestaurant(restaurantId, suspendedBy, suspensionReason);

        // Then
        assertTrue(result);
        assertEquals(RestaurantApprovalStatus.SUSPENDED, approvedRestaurant.getApprovalStatus());
        assertEquals(suspendedBy, approvedRestaurant.getApprovedBy());
        assertNotNull(approvedRestaurant.getApprovedAt());
        verify(restaurantProfileRepository).save(approvedRestaurant);
        verify(restaurantNotificationService).sendSuspensionNotification(approvedRestaurant, suspensionReason);
    }

    @Test
    @DisplayName("Should return false when restaurant not found for suspension")
    public void testSuspendRestaurant_WithNonExistentId_ShouldReturnFalse() {
        // Given
        Integer restaurantId = 9999;
        String suspendedBy = "admin";
        String suspensionReason = "Test";

        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.empty());

        // When
        boolean result = restaurantApprovalService.suspendRestaurant(restaurantId, suspendedBy, suspensionReason);

        // Then
        assertFalse(result);
        verify(restaurantProfileRepository, never()).save(any(RestaurantProfile.class));
        verify(restaurantNotificationService, never()).sendSuspensionNotification(any(), any());
    }

    @Test
    @DisplayName("Should use default reason when suspension reason is null")
    public void testSuspendRestaurant_WithNullReason_ShouldUseDefault() {
        // Given
        Integer restaurantId = 2;
        String suspendedBy = "admin";
        String suspensionReason = null;

        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.of(approvedRestaurant));
        when(restaurantProfileRepository.save(any(RestaurantProfile.class)))
            .thenReturn(approvedRestaurant);

        // When
        boolean result = restaurantApprovalService.suspendRestaurant(restaurantId, suspendedBy, suspensionReason);

        // Then
        assertTrue(result);
        assertEquals("Nhà hàng bị tạm dừng", approvedRestaurant.getApprovalReason());
    }

    @Test
    @DisplayName("Should handle exception during suspension")
    public void testSuspendRestaurant_WithException_ShouldReturnFalse() {
        // Given
        Integer restaurantId = 2;
        String suspendedBy = "admin";
        String suspensionReason = "Test";

        when(restaurantProfileRepository.findById(restaurantId))
            .thenThrow(new RuntimeException("Database error"));

        // When
        boolean result = restaurantApprovalService.suspendRestaurant(restaurantId, suspendedBy, suspensionReason);

        // Then
        assertFalse(result);
    }

    // ========== resubmitRestaurant() Tests ==========

    @Test
    @DisplayName("Should resubmit restaurant from rejected to pending")
    public void testResubmitRestaurant_ShouldResubmitSuccessfully() {
        // Given
        Integer restaurantId = 3;
        String resubmittedBy = "admin";
        String resubmitReason = "Updated documents";

        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.of(rejectedRestaurant));
        when(restaurantProfileRepository.save(any(RestaurantProfile.class)))
            .thenReturn(rejectedRestaurant);

        // When
        boolean result = restaurantApprovalService.resubmitRestaurant(restaurantId, resubmittedBy, resubmitReason);

        // Then
        assertTrue(result);
        assertEquals(RestaurantApprovalStatus.PENDING, rejectedRestaurant.getApprovalStatus());
        assertNull(rejectedRestaurant.getApprovedBy());
        assertNull(rejectedRestaurant.getApprovedAt());
        assertNull(rejectedRestaurant.getApprovalReason());
        assertNull(rejectedRestaurant.getRejectionReason());
        verify(restaurantProfileRepository).save(rejectedRestaurant);
        verify(restaurantNotificationService).sendResubmitNotification(rejectedRestaurant, resubmitReason);
    }

    @Test
    @DisplayName("Should return false when resubmitting non-rejected restaurant")
    public void testResubmitRestaurant_WithNonRejectedStatus_ShouldReturnFalse() {
        // Given
        Integer restaurantId = 2;
        String resubmittedBy = "admin";
        String resubmitReason = "Test";

        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.of(approvedRestaurant));

        // When
        boolean result = restaurantApprovalService.resubmitRestaurant(restaurantId, resubmittedBy, resubmitReason);

        // Then
        assertFalse(result);
        verify(restaurantProfileRepository, never()).save(any(RestaurantProfile.class));
        verify(restaurantNotificationService, never()).sendResubmitNotification(any(), any());
    }

    @Test
    @DisplayName("Should handle exception during resubmission")
    public void testResubmitRestaurant_WithException_ShouldReturnFalse() {
        // Given
        Integer restaurantId = 3;
        String resubmittedBy = "admin";
        String resubmitReason = "Test";

        when(restaurantProfileRepository.findById(restaurantId))
            .thenThrow(new RuntimeException("Database error"));

        // When
        boolean result = restaurantApprovalService.resubmitRestaurant(restaurantId, resubmittedBy, resubmitReason);

        // Then
        assertFalse(result);
    }

    // ========== activateRestaurant() Tests ==========

    @Test
    @DisplayName("Should activate restaurant from suspended to approved")
    public void testActivateRestaurant_ShouldActivateSuccessfully() {
        // Given
        Integer restaurantId = 4;
        String activatedBy = "admin";
        String activationReason = "Compliance restored";

        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.of(suspendedRestaurant));
        when(restaurantProfileRepository.save(any(RestaurantProfile.class)))
            .thenReturn(suspendedRestaurant);

        // When
        boolean result = restaurantApprovalService.activateRestaurant(restaurantId, activatedBy, activationReason);

        // Then
        assertTrue(result);
        assertEquals(RestaurantApprovalStatus.APPROVED, suspendedRestaurant.getApprovalStatus());
        assertEquals(activatedBy, suspendedRestaurant.getApprovedBy());
        assertNotNull(suspendedRestaurant.getApprovedAt());
        verify(restaurantProfileRepository).save(suspendedRestaurant);
        verify(restaurantNotificationService).sendActivationNotification(suspendedRestaurant, activationReason);
    }

    @Test
    @DisplayName("Should return false when activating non-suspended restaurant")
    public void testActivateRestaurant_WithNonSuspendedStatus_ShouldReturnFalse() {
        // Given
        Integer restaurantId = 2;
        String activatedBy = "admin";
        String activationReason = "Test";

        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.of(approvedRestaurant));

        // When
        boolean result = restaurantApprovalService.activateRestaurant(restaurantId, activatedBy, activationReason);

        // Then
        assertFalse(result);
        verify(restaurantProfileRepository, never()).save(any(RestaurantProfile.class));
        verify(restaurantNotificationService, never()).sendActivationNotification(any(), any());
    }

    @Test
    @DisplayName("Should use default reason when activation reason is null")
    public void testActivateRestaurant_WithNullReason_ShouldUseDefault() {
        // Given
        Integer restaurantId = 4;
        String activatedBy = "admin";
        String activationReason = null;

        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.of(suspendedRestaurant));
        when(restaurantProfileRepository.save(any(RestaurantProfile.class)))
            .thenReturn(suspendedRestaurant);

        // When
        boolean result = restaurantApprovalService.activateRestaurant(restaurantId, activatedBy, activationReason);

        // Then
        assertTrue(result);
        assertEquals("Nhà hàng được kích hoạt lại", suspendedRestaurant.getApprovalReason());
    }

    @Test
    @DisplayName("Should handle exception during activation")
    public void testActivateRestaurant_WithException_ShouldReturnFalse() {
        // Given
        Integer restaurantId = 4;
        String activatedBy = "admin";
        String activationReason = "Test";

        when(restaurantProfileRepository.findById(restaurantId))
            .thenThrow(new RuntimeException("Database error"));

        // When
        boolean result = restaurantApprovalService.activateRestaurant(restaurantId, activatedBy, activationReason);

        // Then
        assertFalse(result);
    }

    // ========== getAllRestaurantsWithApprovalInfo() Tests ==========

    @Test
    @DisplayName("Should return all restaurants with approval info")
    public void testGetAllRestaurantsWithApprovalInfo_ShouldReturnAllRestaurants() {
        // Given
        List<RestaurantProfile> restaurants = List.of(pendingRestaurant, approvedRestaurant, rejectedRestaurant);
        when(restaurantProfileRepository.findAll()).thenReturn(restaurants);

        // When
        List<RestaurantProfile> result = restaurantApprovalService.getAllRestaurantsWithApprovalInfo();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(restaurantProfileRepository).findAll();
    }

    // ========== getRestaurantsByApprovalStatus() Tests ==========

    @Test
    @DisplayName("Should return restaurants by approval status")
    public void testGetRestaurantsByApprovalStatus_ShouldReturnFilteredRestaurants() {
        // Given
        List<RestaurantProfile> pendingRestaurants = List.of(pendingRestaurant);
        when(restaurantProfileRepository.findByApprovalStatus(RestaurantApprovalStatus.PENDING))
            .thenReturn(pendingRestaurants);

        // When
        List<RestaurantProfile> result = restaurantApprovalService
            .getRestaurantsByApprovalStatus(RestaurantApprovalStatus.PENDING);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(RestaurantApprovalStatus.PENDING, result.get(0).getApprovalStatus());
        verify(restaurantProfileRepository).findByApprovalStatus(RestaurantApprovalStatus.PENDING);
    }

    // ========== getRestaurantById() Tests ==========

    @Test
    @DisplayName("Should return restaurant by ID when exists")
    public void testGetRestaurantById_WhenExists_ShouldReturnRestaurant() {
        // Given
        Integer restaurantId = 1;
        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.of(pendingRestaurant));

        // When
        Optional<RestaurantProfile> result = restaurantApprovalService.getRestaurantById(restaurantId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(restaurantId, result.get().getRestaurantId());
        verify(restaurantProfileRepository).findById(restaurantId);
    }

    @Test
    @DisplayName("Should return empty when restaurant not found")
    public void testGetRestaurantById_WhenNotExists_ShouldReturnEmpty() {
        // Given
        Integer restaurantId = 9999;
        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.empty());

        // When
        Optional<RestaurantProfile> result = restaurantApprovalService.getRestaurantById(restaurantId);

        // Then
        assertFalse(result.isPresent());
        verify(restaurantProfileRepository).findById(restaurantId);
    }

    // ========== searchRestaurants() Tests ==========

    @Test
    @DisplayName("Should search restaurants by name")
    public void testSearchRestaurants_ByName_ShouldReturnMatchingRestaurants() {
        // Given
        List<RestaurantProfile> restaurants = List.of(pendingRestaurant, approvedRestaurant);
        String searchTerm = "Pending";

        // When
        List<RestaurantProfile> result = restaurantApprovalService.searchRestaurants(restaurants, searchTerm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getRestaurantName().contains("Pending"));
    }

    @Test
    @DisplayName("Should search restaurants by address")
    public void testSearchRestaurants_ByAddress_ShouldReturnMatchingRestaurants() {
        // Given
        List<RestaurantProfile> restaurants = List.of(pendingRestaurant, approvedRestaurant);
        String searchTerm = "Main";

        // When
        List<RestaurantProfile> result = restaurantApprovalService.searchRestaurants(restaurants, searchTerm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getAddress().contains("Main"));
    }

    @Test
    @DisplayName("Should search restaurants by cuisine type")
    public void testSearchRestaurants_ByCuisineType_ShouldReturnMatchingRestaurants() {
        // Given
        List<RestaurantProfile> restaurants = List.of(pendingRestaurant, approvedRestaurant);
        String searchTerm = "Vietnamese";

        // When
        List<RestaurantProfile> result = restaurantApprovalService.searchRestaurants(restaurants, searchTerm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getCuisineType().contains("Vietnamese"));
    }

    @Test
    @DisplayName("Should search restaurants by owner username")
    public void testSearchRestaurants_ByOwnerUsername_ShouldReturnMatchingRestaurants() {
        // Given
        List<RestaurantProfile> restaurants = List.of(pendingRestaurant, approvedRestaurant);
        String searchTerm = "owner123";

        // When
        List<RestaurantProfile> result = restaurantApprovalService.searchRestaurants(restaurants, searchTerm);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size()); // Both restaurants have same owner
    }

    @Test
    @DisplayName("Should return empty list when no match found")
    public void testSearchRestaurants_NoMatch_ShouldReturnEmpty() {
        // Given
        List<RestaurantProfile> restaurants = List.of(pendingRestaurant, approvedRestaurant);
        String searchTerm = "NonExistentTerm";

        // When
        List<RestaurantProfile> result = restaurantApprovalService.searchRestaurants(restaurants, searchTerm);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle null address in search")
    public void testSearchRestaurants_WithNullAddress_ShouldNotThrowException() {
        // Given
        RestaurantProfile restaurantWithNullAddress = new RestaurantProfile();
        restaurantWithNullAddress.setRestaurantName("Test Restaurant");
        restaurantWithNullAddress.setAddress(null);
        restaurantWithNullAddress.setCuisineType("Test");
        restaurantWithNullAddress.setOwner(testOwner);
        
        List<RestaurantProfile> restaurants = List.of(restaurantWithNullAddress);
        String searchTerm = "Test";

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> {
            List<RestaurantProfile> result = restaurantApprovalService.searchRestaurants(restaurants, searchTerm);
            assertNotNull(result);
        });
    }

    // ========== Count Methods Tests ==========

    @Test
    @DisplayName("Should return pending restaurant count")
    public void testGetPendingRestaurantCount_ShouldReturnCount() {
        // Given
        when(restaurantProfileRepository.countByApprovalStatus(RestaurantApprovalStatus.PENDING))
            .thenReturn(5L);

        // When
        long result = restaurantApprovalService.getPendingRestaurantCount();

        // Then
        assertEquals(5L, result);
        verify(restaurantProfileRepository).countByApprovalStatus(RestaurantApprovalStatus.PENDING);
    }

    @Test
    @DisplayName("Should return zero for pending count when none exist")
    public void testGetPendingRestaurantCount_WhenZero_ShouldReturnZero() {
        // Given
        when(restaurantProfileRepository.countByApprovalStatus(RestaurantApprovalStatus.PENDING))
            .thenReturn(0L);

        // When
        long result = restaurantApprovalService.getPendingRestaurantCount();

        // Then
        assertEquals(0L, result);
    }

    @Test
    @DisplayName("Should return approved restaurant count")
    public void testGetApprovedRestaurantCount_ShouldReturnCount() {
        // Given
        when(restaurantProfileRepository.countByApprovalStatus(RestaurantApprovalStatus.APPROVED))
            .thenReturn(10L);

        // When
        long result = restaurantApprovalService.getApprovedRestaurantCount();

        // Then
        assertEquals(10L, result);
        verify(restaurantProfileRepository).countByApprovalStatus(RestaurantApprovalStatus.APPROVED);
    }

    @Test
    @DisplayName("Should return rejected restaurant count")
    public void testGetRejectedRestaurantCount_ShouldReturnCount() {
        // Given
        when(restaurantProfileRepository.countByApprovalStatus(RestaurantApprovalStatus.REJECTED))
            .thenReturn(2L);

        // When
        long result = restaurantApprovalService.getRejectedRestaurantCount();

        // Then
        assertEquals(2L, result);
        verify(restaurantProfileRepository).countByApprovalStatus(RestaurantApprovalStatus.REJECTED);
    }

    @Test
    @DisplayName("Should return suspended restaurant count")
    public void testGetSuspendedRestaurantCount_ShouldReturnCount() {
        // Given
        when(restaurantProfileRepository.countByApprovalStatus(RestaurantApprovalStatus.SUSPENDED))
            .thenReturn(3L);

        // When
        long result = restaurantApprovalService.getSuspendedRestaurantCount();

        // Then
        assertEquals(3L, result);
        verify(restaurantProfileRepository).countByApprovalStatus(RestaurantApprovalStatus.SUSPENDED);
    }

    // ========== getApprovalStatistics() Tests ==========

    @Test
    @DisplayName("shouldGetApprovalStats_WithTotalGreaterThanZero_ShouldCalculatePercentages")
    public void shouldGetApprovalStats_WithTotalGreaterThanZero_ShouldCalculatePercentages() {
        // Given
        List<RestaurantProfile> allRestaurants = List.of(
            pendingRestaurant, approvedRestaurant, rejectedRestaurant, suspendedRestaurant
        );
        
        when(restaurantProfileRepository.findAll()).thenReturn(allRestaurants);

        // When
        Map<String, Object> stats = restaurantApprovalService.getApprovalStatistics();

        // Then
        assertNotNull(stats);
        assertEquals(4L, stats.get("total"));
        assertEquals(1L, stats.get("pending"));
        assertEquals(1L, stats.get("approved"));
        assertEquals(1L, stats.get("rejected"));
        assertEquals(1L, stats.get("suspended"));
        // Percentages should be calculated (25.0 each)
        assertNotNull(stats.get("pendingPercentage"));
        assertNotNull(stats.get("approvedPercentage"));
        assertNotNull(stats.get("rejectedPercentage"));
        assertNotNull(stats.get("suspendedPercentage"));
    }

    @Test
    @DisplayName("shouldGetApprovalStats_WithTotalEqualToZero_ShouldReturnZeroPercentages")
    public void shouldGetApprovalStats_WithTotalEqualToZero_ShouldReturnZeroPercentages() {
        // Given
        when(restaurantProfileRepository.findAll()).thenReturn(List.of());

        // When
        Map<String, Object> stats = restaurantApprovalService.getApprovalStatistics();

        // Then
        assertNotNull(stats);
        assertEquals(0L, stats.get("total"));
        assertEquals(0.0, stats.get("pendingPercentage"));
        assertEquals(0.0, stats.get("approvedPercentage"));
        assertEquals(0.0, stats.get("rejectedPercentage"));
        assertEquals(0.0, stats.get("suspendedPercentage"));
    }

    // ========== notifyNewRestaurantRegistration() Tests ==========

    @Test
    @DisplayName("shouldNotifyNewRestaurantRegistration_Successfully")
    public void shouldNotifyNewRestaurantRegistration_Successfully() {
        // Given
        doNothing().when(restaurantNotificationService).notifyAdminNewRegistration(any(RestaurantProfile.class));

        // When
        restaurantApprovalService.notifyNewRestaurantRegistration(pendingRestaurant);

        // Then
        verify(restaurantNotificationService, times(1)).notifyAdminNewRegistration(pendingRestaurant);
    }

    @Test
    @DisplayName("shouldHandleException_WhenNotifyNewRestaurantRegistrationFails")
    public void shouldHandleException_WhenNotifyNewRestaurantRegistrationFails() {
        // Given
        doThrow(new RuntimeException("Notification service error"))
            .when(restaurantNotificationService).notifyAdminNewRegistration(any(RestaurantProfile.class));

        // When & Then - Should not throw exception, should handle gracefully
        assertDoesNotThrow(() -> {
            restaurantApprovalService.notifyNewRestaurantRegistration(pendingRestaurant);
        });
    }
}
