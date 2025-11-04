package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ContractStatus enum
 */
@DisplayName("ContractStatus Enum Tests")
public class ContractStatusTest {

    // ========== Enum Values Tests ==========

    @Test
    @DisplayName("shouldHaveAllEnumValues")
    void shouldHaveAllEnumValues() {
        assertNotNull(ContractStatus.DRAFT);
        assertNotNull(ContractStatus.PENDING_OWNER_SIGNATURE);
        assertNotNull(ContractStatus.PENDING_ADMIN_SIGNATURE);
        assertNotNull(ContractStatus.ACTIVE);
        assertNotNull(ContractStatus.EXPIRED);
        assertNotNull(ContractStatus.TERMINATED);
        assertNotNull(ContractStatus.CANCELLED);
    }

    @Test
    @DisplayName("shouldGetDisplayName_forAllValues")
    void shouldGetDisplayName_forAllValues() {
        assertEquals("Bản nháp", ContractStatus.DRAFT.getDisplayName());
        assertEquals("Chờ chủ nhà hàng ký", ContractStatus.PENDING_OWNER_SIGNATURE.getDisplayName());
        assertEquals("Chờ admin ký", ContractStatus.PENDING_ADMIN_SIGNATURE.getDisplayName());
        assertEquals("Đang hiệu lực", ContractStatus.ACTIVE.getDisplayName());
        assertEquals("Hết hạn", ContractStatus.EXPIRED.getDisplayName());
        assertEquals("Chấm dứt", ContractStatus.TERMINATED.getDisplayName());
        assertEquals("Đã hủy", ContractStatus.CANCELLED.getDisplayName());
    }

    // ========== isActive() Tests ==========

    @Test
    @DisplayName("shouldReturnTrue_whenActive")
    void shouldReturnTrue_whenActive() {
        assertTrue(ContractStatus.ACTIVE.isActive());
    }

    @Test
    @DisplayName("shouldReturnFalse_whenNotActive")
    void shouldReturnFalse_whenNotActive() {
        assertFalse(ContractStatus.DRAFT.isActive());
        assertFalse(ContractStatus.PENDING_OWNER_SIGNATURE.isActive());
        assertFalse(ContractStatus.EXPIRED.isActive());
        assertFalse(ContractStatus.TERMINATED.isActive());
        assertFalse(ContractStatus.CANCELLED.isActive());
    }

    // ========== canBeSigned() Tests ==========

    @Test
    @DisplayName("shouldReturnTrue_whenCanBeSigned")
    void shouldReturnTrue_whenCanBeSigned() {
        assertTrue(ContractStatus.DRAFT.canBeSigned());
        assertTrue(ContractStatus.PENDING_OWNER_SIGNATURE.canBeSigned());
        assertTrue(ContractStatus.PENDING_ADMIN_SIGNATURE.canBeSigned());
    }

    @Test
    @DisplayName("shouldReturnFalse_whenCannotBeSigned")
    void shouldReturnFalse_whenCannotBeSigned() {
        assertFalse(ContractStatus.ACTIVE.canBeSigned());
        assertFalse(ContractStatus.EXPIRED.canBeSigned());
        assertFalse(ContractStatus.TERMINATED.canBeSigned());
        assertFalse(ContractStatus.CANCELLED.canBeSigned());
    }

    // ========== canBeTerminated() Tests ==========

    @Test
    @DisplayName("shouldReturnTrue_whenCanBeTerminated")
    void shouldReturnTrue_whenCanBeTerminated() {
        assertTrue(ContractStatus.ACTIVE.canBeTerminated());
    }

    @Test
    @DisplayName("shouldReturnFalse_whenCannotBeTerminated")
    void shouldReturnFalse_whenCannotBeTerminated() {
        assertFalse(ContractStatus.DRAFT.canBeTerminated());
        assertFalse(ContractStatus.PENDING_OWNER_SIGNATURE.canBeTerminated());
        assertFalse(ContractStatus.EXPIRED.canBeTerminated());
        assertFalse(ContractStatus.TERMINATED.canBeTerminated());
        assertFalse(ContractStatus.CANCELLED.canBeTerminated());
    }

    // ========== canBeCancelled() Tests ==========

    @Test
    @DisplayName("shouldReturnTrue_whenCanBeCancelled")
    void shouldReturnTrue_whenCanBeCancelled() {
        assertTrue(ContractStatus.DRAFT.canBeCancelled());
        assertTrue(ContractStatus.PENDING_OWNER_SIGNATURE.canBeCancelled());
        assertTrue(ContractStatus.PENDING_ADMIN_SIGNATURE.canBeCancelled());
    }

    @Test
    @DisplayName("shouldReturnFalse_whenCannotBeCancelled")
    void shouldReturnFalse_whenCannotBeCancelled() {
        assertFalse(ContractStatus.ACTIVE.canBeCancelled());
        assertFalse(ContractStatus.EXPIRED.canBeCancelled());
        assertFalse(ContractStatus.TERMINATED.canBeCancelled());
        assertFalse(ContractStatus.CANCELLED.canBeCancelled());
    }

    // ========== getColorClass() Tests ==========

    @Test
    @DisplayName("shouldGetColorClass_forAllValues")
    void shouldGetColorClass_forAllValues() {
        assertEquals("text-muted", ContractStatus.DRAFT.getColorClass());
        assertEquals("text-warning", ContractStatus.PENDING_OWNER_SIGNATURE.getColorClass());
        assertEquals("text-warning", ContractStatus.PENDING_ADMIN_SIGNATURE.getColorClass());
        assertEquals("text-success", ContractStatus.ACTIVE.getColorClass());
        assertEquals("text-info", ContractStatus.EXPIRED.getColorClass());
        assertEquals("text-danger", ContractStatus.TERMINATED.getColorClass());
        assertEquals("text-danger", ContractStatus.CANCELLED.getColorClass());
    }

    // ========== getIcon() Tests ==========

    @Test
    @DisplayName("shouldGetIcon_forAllValues")
    void shouldGetIcon_forAllValues() {
        assertEquals("fas fa-edit", ContractStatus.DRAFT.getIcon());
        assertEquals("fas fa-user-clock", ContractStatus.PENDING_OWNER_SIGNATURE.getIcon());
        assertEquals("fas fa-user-shield", ContractStatus.PENDING_ADMIN_SIGNATURE.getIcon());
        assertEquals("fas fa-check-circle", ContractStatus.ACTIVE.getIcon());
        assertEquals("fas fa-clock", ContractStatus.EXPIRED.getIcon());
        assertEquals("fas fa-times-circle", ContractStatus.TERMINATED.getIcon());
        assertEquals("fas fa-ban", ContractStatus.CANCELLED.getIcon());
    }
}
