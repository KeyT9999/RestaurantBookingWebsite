package com.example.booking.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.booking.common.enums.RestaurantApprovalStatus;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.repository.RestaurantProfileRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("RestaurantManagementService Tests")
public class RestaurantManagementServiceTest {

    @Mock
    private RestaurantProfileRepository restaurantProfileRepository;

    @InjectMocks
    private RestaurantManagementService restaurantManagementService;

    private RestaurantProfile approvedRestaurant1;
    private RestaurantProfile approvedRestaurant2;
    private RestaurantProfile approvedRestaurant3;
    private RestaurantProfile pendingRestaurant1;
    private RestaurantProfile pendingRestaurant2;
    private RestaurantProfile aiRestaurant;
    private RestaurantProfile rejectedRestaurant;

    private RestaurantOwner testOwner;
    private UUID testOwnerId;

    @BeforeEach
    public void setUp() {
        testOwnerId = UUID.randomUUID();
        testOwner = new RestaurantOwner();
        testOwner.setOwnerId(testOwnerId);

        // Approved restaurants
        approvedRestaurant1 = new RestaurantProfile();
        approvedRestaurant1.setRestaurantId(1);
        approvedRestaurant1.setRestaurantName("Restaurant A");
        approvedRestaurant1.setApprovalStatus(RestaurantApprovalStatus.APPROVED);
        approvedRestaurant1.setOwner(testOwner);

        approvedRestaurant2 = new RestaurantProfile();
        approvedRestaurant2.setRestaurantId(2);
        approvedRestaurant2.setRestaurantName("Restaurant B");
        approvedRestaurant2.setApprovalStatus(RestaurantApprovalStatus.APPROVED);
        approvedRestaurant2.setOwner(testOwner);

        approvedRestaurant3 = new RestaurantProfile();
        approvedRestaurant3.setRestaurantId(3);
        approvedRestaurant3.setRestaurantName("Restaurant C");
        approvedRestaurant3.setApprovalStatus(RestaurantApprovalStatus.APPROVED);
        approvedRestaurant3.setOwner(testOwner);

        // Pending restaurants
        pendingRestaurant1 = new RestaurantProfile();
        pendingRestaurant1.setRestaurantId(4);
        pendingRestaurant1.setRestaurantName("Pending Restaurant 1");
        pendingRestaurant1.setApprovalStatus(RestaurantApprovalStatus.PENDING);
        pendingRestaurant1.setOwner(testOwner);

        pendingRestaurant2 = new RestaurantProfile();
        pendingRestaurant2.setRestaurantId(5);
        pendingRestaurant2.setRestaurantName("Pending Restaurant 2");
        pendingRestaurant2.setApprovalStatus(RestaurantApprovalStatus.PENDING);
        pendingRestaurant2.setOwner(testOwner);

        // AI Restaurant
        aiRestaurant = new RestaurantProfile();
        aiRestaurant.setRestaurantId(37);
        aiRestaurant.setRestaurantName("AI Restaurant");
        aiRestaurant.setApprovalStatus(RestaurantApprovalStatus.APPROVED);
        aiRestaurant.setOwner(testOwner);

        // Rejected restaurant
        rejectedRestaurant = new RestaurantProfile();
        rejectedRestaurant.setRestaurantId(6);
        rejectedRestaurant.setRestaurantName("Rejected Restaurant");
        rejectedRestaurant.setApprovalStatus(RestaurantApprovalStatus.REJECTED);
        rejectedRestaurant.setOwner(testOwner);
    }

    // ========== findAllRestaurants() Tests ==========

    @Test
    @DisplayName("Should return approved restaurants only")
    public void testFindAllRestaurants_ShouldReturnApprovedRestaurantsOnly() {
        // Given
        List<RestaurantProfile> allRestaurants = Arrays.asList(
            approvedRestaurant1, approvedRestaurant2, approvedRestaurant3,
            pendingRestaurant1, pendingRestaurant2, rejectedRestaurant
        );
        List<RestaurantProfile> approvedRestaurants = Arrays.asList(
            approvedRestaurant1, approvedRestaurant2, approvedRestaurant3
        );

        when(restaurantProfileRepository.findApprovedExcludingAI())
            .thenReturn(approvedRestaurants);

        // When
        List<RestaurantProfile> result = restaurantManagementService.findAllRestaurants();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(approvedRestaurant1));
        assertTrue(result.contains(approvedRestaurant2));
        assertTrue(result.contains(approvedRestaurant3));
        assertFalse(result.contains(pendingRestaurant1));
        assertFalse(result.contains(pendingRestaurant2));
        assertFalse(result.contains(rejectedRestaurant));
    }

    @Test
    @DisplayName("Should exclude AI restaurant (ID=37)")
    public void testFindAllRestaurants_ShouldExcludeAIRestaurant() {
        // Given
        List<RestaurantProfile> approvedRestaurantsWithAI = Arrays.asList(
            approvedRestaurant1, approvedRestaurant2, aiRestaurant, approvedRestaurant3
        );
        List<RestaurantProfile> approvedRestaurantsWithoutAI = Arrays.asList(
            approvedRestaurant1, approvedRestaurant2, approvedRestaurant3
        );

        when(restaurantProfileRepository.findApprovedExcludingAI())
            .thenReturn(approvedRestaurantsWithoutAI);

        // When
        List<RestaurantProfile> result = restaurantManagementService.findAllRestaurants();

        // Then
        assertNotNull(result);
        assertFalse(result.stream().anyMatch(r -> r.getRestaurantId().equals(37)));
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("Should return empty list when no restaurants")
    public void testFindAllRestaurants_WithNoRestaurants_ShouldReturnEmptyList() {
        // Given
        when(restaurantProfileRepository.findApprovedExcludingAI())
            .thenReturn(Arrays.asList());

        // When
        List<RestaurantProfile> result = restaurantManagementService.findAllRestaurants();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return restaurants sorted by name")
    public void testFindAllRestaurants_ShouldReturnListSortedByName() {
        // Given
        List<RestaurantProfile> restaurants = Arrays.asList(
            approvedRestaurant3, approvedRestaurant1, approvedRestaurant2
        );
        List<RestaurantProfile> sortedRestaurants = Arrays.asList(
            approvedRestaurant1, approvedRestaurant2, approvedRestaurant3
        );

        when(restaurantProfileRepository.findApprovedExcludingAI())
            .thenReturn(sortedRestaurants);

        // When
        List<RestaurantProfile> result = restaurantManagementService.findAllRestaurants();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Restaurant A", result.get(0).getRestaurantName());
        assertEquals("Restaurant B", result.get(1).getRestaurantName());
        assertEquals("Restaurant C", result.get(2).getRestaurantName());
    }

    // ========== findRestaurantById() Tests ==========

    @Test
    @DisplayName("Should return restaurant with valid ID")
    public void testFindRestaurantById_WithValidId_ShouldReturnRestaurant() {
        // Given
        Integer restaurantId = 1;
        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.of(approvedRestaurant1));

        // When
        Optional<RestaurantProfile> result = restaurantManagementService.findRestaurantById(restaurantId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(approvedRestaurant1.getRestaurantId(), result.get().getRestaurantId());
        assertEquals(approvedRestaurant1.getRestaurantName(), result.get().getRestaurantName());
        assertEquals(RestaurantApprovalStatus.APPROVED, result.get().getApprovalStatus());
    }

    @Test
    @DisplayName("Should load all relations")
    public void testFindRestaurantById_ShouldLoadAllRelations() {
        // Given
        Integer restaurantId = 1;
        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.of(approvedRestaurant1));

        // When
        Optional<RestaurantProfile> result = restaurantManagementService.findRestaurantById(restaurantId);

        // Then
        assertTrue(result.isPresent());
        assertNotNull(result.get().getOwner());
    }

    @Test
    @DisplayName("Should return empty for non-existent ID")
    public void testFindRestaurantById_WithNonExistentId_ShouldReturnEmpty() {
        // Given
        Integer restaurantId = 9999;
        when(restaurantProfileRepository.findById(restaurantId))
            .thenReturn(Optional.empty());

        // When
        Optional<RestaurantProfile> result = restaurantManagementService.findRestaurantById(restaurantId);

        // Then
        assertFalse(result.isPresent());
    }

    // ========== findRestaurantsByOwner() Tests ==========

    @Test
    @DisplayName("Should return all restaurants for owner with multiple restaurants")
    public void testFindRestaurantsByOwner_WithOwnerHasMultipleRestaurants_ShouldReturnAll() {
        // Given
        List<RestaurantProfile> ownerRestaurants = Arrays.asList(
            approvedRestaurant1, approvedRestaurant2, approvedRestaurant3
        );

        when(restaurantProfileRepository.findByOwnerOwnerId(testOwnerId))
            .thenReturn(ownerRestaurants);

        // When
        List<RestaurantProfile> result = restaurantManagementService.findRestaurantsByOwner(testOwnerId);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(approvedRestaurant1));
        assertTrue(result.contains(approvedRestaurant2));
        assertTrue(result.contains(approvedRestaurant3));
    }

    @Test
    @DisplayName("Should return empty list for owner with no restaurants")
    public void testFindRestaurantsByOwner_WithOwnerHasNoRestaurants_ShouldReturnEmpty() {
        // Given
        when(restaurantProfileRepository.findByOwnerOwnerId(testOwnerId))
            .thenReturn(Arrays.asList());

        // When
        List<RestaurantProfile> result = restaurantManagementService.findRestaurantsByOwner(testOwnerId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

