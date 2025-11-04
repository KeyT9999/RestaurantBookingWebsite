package com.example.booking.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.example.booking.common.enums.RestaurantApprovalStatus;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;

/**
 * Unit tests for RestaurantProfileRepository using @DataJpaTest
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("RestaurantProfileRepository Tests")
public class RestaurantProfileRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RestaurantProfileRepository restaurantProfileRepository;

    // ========== findById() Tests ==========

    @Test
    @DisplayName("shouldFindRestaurantById_successfully")
    void shouldFindRestaurantById_successfully() {
        // Given
        RestaurantProfile restaurant = createTestRestaurant();
        entityManager.persistAndFlush(restaurant);

        // When
        Optional<RestaurantProfile> found = restaurantProfileRepository.findById(restaurant.getRestaurantId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(restaurant.getRestaurantId(), found.get().getRestaurantId());
    }

    // ========== findByStatus() Tests ==========

    @Test
    @DisplayName("shouldFindRestaurantsByStatus_successfully")
    void shouldFindRestaurantsByStatus_successfully() {
        // Given
        RestaurantProfile approved = createTestRestaurant();
        approved.setApprovalStatus(RestaurantApprovalStatus.APPROVED);
        entityManager.persistAndFlush(approved);

        RestaurantProfile pending = createTestRestaurant();
        pending.setApprovalStatus(RestaurantApprovalStatus.PENDING);
        entityManager.persistAndFlush(pending);

        // When
        List<RestaurantProfile> approvedList = restaurantProfileRepository.findByApprovalStatus(RestaurantApprovalStatus.APPROVED);

        // Then
        assertTrue(approvedList.size() > 0);
        approvedList.forEach(r -> assertEquals(RestaurantApprovalStatus.APPROVED, r.getApprovalStatus()));
    }

    // ========== Helper Methods ==========

    private RestaurantProfile createTestRestaurant() {
        User user = new User();
        user.setEmail("owner@test.com");
        user.setUsername("owner");
        entityManager.persist(user);

        RestaurantOwner owner = new RestaurantOwner();
        owner.setUser(user);
        entityManager.persist(owner);

        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantName("Test Restaurant");
        restaurant.setOwner(owner);
        restaurant.setApprovalStatus(RestaurantApprovalStatus.PENDING);
        
        return restaurant;
    }
}

