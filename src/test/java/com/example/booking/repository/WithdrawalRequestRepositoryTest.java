package com.example.booking.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.example.booking.common.enums.WithdrawalStatus;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.WithdrawalRequest;

/**
 * Unit tests for WithdrawalRequestRepository
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("WithdrawalRequestRepository Tests")
public class WithdrawalRequestRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WithdrawalRequestRepository withdrawalRepository;

    @Test
    @DisplayName("shouldFindWithdrawalsByRestaurantAndStatus_successfully")
    void shouldFindWithdrawalsByRestaurantAndStatus_successfully() {
        // Given
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantName("Test Restaurant");
        entityManager.persist(restaurant);

        WithdrawalRequest withdrawal = new WithdrawalRequest();
        withdrawal.setRestaurant(restaurant);
        withdrawal.setAmount(new BigDecimal("500000"));
        withdrawal.setStatus(WithdrawalStatus.PENDING);
        entityManager.persistAndFlush(withdrawal);

        // When
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<WithdrawalRequest> found = withdrawalRepository.findByRestaurantRestaurantIdAndStatus(
            restaurant.getRestaurantId(), WithdrawalStatus.PENDING, pageable);

        // Then
        assertTrue(found.getTotalElements() > 0);
        assertEquals(WithdrawalStatus.PENDING, found.getContent().get(0).getStatus());
    }
}

