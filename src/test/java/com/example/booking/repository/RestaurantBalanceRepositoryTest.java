package com.example.booking.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.example.booking.domain.RestaurantBalance;
import com.example.booking.domain.RestaurantProfile;

/**
 * Unit tests for RestaurantBalanceRepository
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("RestaurantBalanceRepository Tests")
public class RestaurantBalanceRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RestaurantBalanceRepository balanceRepository;

    @Test
    @DisplayName("shouldFindBalanceByRestaurant_successfully")
    void shouldFindBalanceByRestaurant_successfully() {
        // Given
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantName("Test Restaurant");
        entityManager.persist(restaurant);

        RestaurantBalance balance = new RestaurantBalance();
        balance.setRestaurant(restaurant);
        balance.setTotalRevenue(new BigDecimal("1000000"));
        balance.setAvailableBalance(new BigDecimal("800000"));
        entityManager.persistAndFlush(balance);

        // When
        Optional<RestaurantBalance> found = balanceRepository.findByRestaurantRestaurantId(restaurant.getRestaurantId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(restaurant.getRestaurantId(), found.get().getRestaurant().getRestaurantId());
    }
}

