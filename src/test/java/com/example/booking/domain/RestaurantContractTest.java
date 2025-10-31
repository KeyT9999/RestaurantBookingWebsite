package com.example.booking.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RestaurantContractTest {

    @Test
    @DisplayName("helper methods should reflect signing and status states")
    void shouldEvaluateSigningStates() {
        RestaurantContract contract = new RestaurantContract();
        contract.setOwnerId(UUID.randomUUID());
        contract.setRestaurantId(10);
        contract.setContractEndDate(LocalDateTime.now().plusDays(5));

        assertThat(contract.isFullySigned()).isFalse();
        assertThat(contract.needsOwnerSignature()).isTrue();
        assertThat(contract.needsAdminSignature()).isTrue();
        assertThat(contract.canBeSigned()).isTrue();

        contract.setSignedByOwner(true);
        contract.setSignedByAdmin(true);
        contract.setStatus(ContractStatus.ACTIVE);

        assertThat(contract.isFullySigned()).isTrue();
        assertThat(contract.isActive()).isTrue();
        assertThat(contract.needsOwnerSignature()).isFalse();
        assertThat(contract.needsAdminSignature()).isFalse();
    }

    @Test
    @DisplayName("expiry helpers should detect expired and open-ended contracts")
    void shouldDetectExpiryAndDaysRemaining() {
        RestaurantContract contract = new RestaurantContract();
        contract.setSignedByOwner(true);
        contract.setSignedByAdmin(true);
        contract.setStatus(ContractStatus.ACTIVE);

        contract.setContractEndDate(LocalDateTime.now().minusDays(1));
        assertThat(contract.isExpired()).isTrue();
        assertThat(contract.getDaysRemaining()).isLessThan(0);

        contract.setContractEndDate(LocalDateTime.now().plusDays(7));
        assertThat(contract.isExpired()).isFalse();
        assertThat(contract.getDaysRemaining()).isGreaterThanOrEqualTo(6);
    }

    @Test
    @DisplayName("status display should map to enum display name")
    void shouldExposeDisplayName() {
        RestaurantContract contract = new RestaurantContract();
        contract.setStatus(ContractStatus.PENDING_ADMIN_SIGNATURE);
        assertThat(contract.getStatusDisplay()).isEqualTo(ContractStatus.PENDING_ADMIN_SIGNATURE.getDisplayName());
    }
}
