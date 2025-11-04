package com.example.booking.common.enums;

import com.example.booking.domain.ContractStatus;
import com.example.booking.domain.ContractType;
import com.example.booking.domain.UserRole;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for Enums with business logic
 */
class EnumTest {

    // ========== RestaurantApprovalStatus Tests ==========
    @Test
    void testRestaurantApprovalStatus_DisplayNames() {
        assertThat(RestaurantApprovalStatus.PENDING.getDisplayName()).isEqualTo("Chờ duyệt");
        assertThat(RestaurantApprovalStatus.APPROVED.getDisplayName()).isEqualTo("Đã duyệt");
        assertThat(RestaurantApprovalStatus.REJECTED.getDisplayName()).isEqualTo("Bị từ chối");
        assertThat(RestaurantApprovalStatus.SUSPENDED.getDisplayName()).isEqualTo("Tạm dừng");
    }

    @Test
    void testRestaurantApprovalStatus_IsTerminal() {
        assertThat(RestaurantApprovalStatus.PENDING.isTerminal()).isFalse();
        assertThat(RestaurantApprovalStatus.APPROVED.isTerminal()).isTrue();
        assertThat(RestaurantApprovalStatus.REJECTED.isTerminal()).isTrue();
        assertThat(RestaurantApprovalStatus.SUSPENDED.isTerminal()).isTrue();
    }

    @Test
    void testRestaurantApprovalStatus_IsPending() {
        assertThat(RestaurantApprovalStatus.PENDING.isPending()).isTrue();
        assertThat(RestaurantApprovalStatus.APPROVED.isPending()).isFalse();
        assertThat(RestaurantApprovalStatus.REJECTED.isPending()).isFalse();
        assertThat(RestaurantApprovalStatus.SUSPENDED.isPending()).isFalse();
    }

    @Test
    void testRestaurantApprovalStatus_CanBeApproved() {
        assertThat(RestaurantApprovalStatus.PENDING.canBeApproved()).isTrue();
        assertThat(RestaurantApprovalStatus.APPROVED.canBeApproved()).isFalse();
        assertThat(RestaurantApprovalStatus.REJECTED.canBeApproved()).isFalse();
        assertThat(RestaurantApprovalStatus.SUSPENDED.canBeApproved()).isFalse();
    }

    @Test
    void testRestaurantApprovalStatus_CanBeRejected() {
        assertThat(RestaurantApprovalStatus.PENDING.canBeRejected()).isTrue();
        assertThat(RestaurantApprovalStatus.APPROVED.canBeRejected()).isTrue();
        assertThat(RestaurantApprovalStatus.REJECTED.canBeRejected()).isFalse();
        assertThat(RestaurantApprovalStatus.SUSPENDED.canBeRejected()).isFalse();
    }

    @Test
    void testRestaurantApprovalStatus_CanBeSuspended() {
        assertThat(RestaurantApprovalStatus.PENDING.canBeSuspended()).isFalse();
        assertThat(RestaurantApprovalStatus.APPROVED.canBeSuspended()).isTrue();
        assertThat(RestaurantApprovalStatus.REJECTED.canBeSuspended()).isFalse();
        assertThat(RestaurantApprovalStatus.SUSPENDED.canBeSuspended()).isFalse();
    }

    // ========== TableStatus Tests ==========
    @Test
    void testTableStatus_DisplayNames() {
        assertThat(TableStatus.AVAILABLE.getDisplayName()).isEqualTo("Có sẵn");
        assertThat(TableStatus.OCCUPIED.getDisplayName()).isEqualTo("Đang sử dụng");
        assertThat(TableStatus.RESERVED.getDisplayName()).isEqualTo("Đã đặt");
        assertThat(TableStatus.CLEANING.getDisplayName()).isEqualTo("Đang dọn dẹp");
        assertThat(TableStatus.MAINTENANCE.getDisplayName()).isEqualTo("Bảo trì");
    }

    @Test
    void testTableStatus_Values() {
        assertThat(TableStatus.AVAILABLE.getValue()).isEqualTo("available");
        assertThat(TableStatus.OCCUPIED.getValue()).isEqualTo("occupied");
        assertThat(TableStatus.RESERVED.getValue()).isEqualTo("reserved");
        assertThat(TableStatus.CLEANING.getValue()).isEqualTo("cleaning");
        assertThat(TableStatus.MAINTENANCE.getValue()).isEqualTo("maintenance");
    }

    @Test
    void testTableStatus_FromValue_CaseInsensitive() {
        assertThat(TableStatus.fromValue("available")).isEqualTo(TableStatus.AVAILABLE);
        assertThat(TableStatus.fromValue("AVAILABLE")).isEqualTo(TableStatus.AVAILABLE);
        assertThat(TableStatus.fromValue("Available")).isEqualTo(TableStatus.AVAILABLE);
        assertThat(TableStatus.fromValue("occupied")).isEqualTo(TableStatus.OCCUPIED);
        assertThat(TableStatus.fromValue("OCCUPIED")).isEqualTo(TableStatus.OCCUPIED);
    }

    @Test
    void testTableStatus_FromValue_EnumNameFallback() {
        assertThat(TableStatus.fromValue("AVAILABLE")).isEqualTo(TableStatus.AVAILABLE);
        assertThat(TableStatus.fromValue("OCCUPIED")).isEqualTo(TableStatus.OCCUPIED);
    }

    @Test
    void testTableStatus_FromValue_Null() {
        assertThat(TableStatus.fromValue(null)).isNull();
    }

    @Test
    void testTableStatus_FromValue_InvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> {
            TableStatus.fromValue("invalid_status");
        });
    }

    // ========== BookingStatus Tests ==========
    @Test
    void testBookingStatus_DisplayNames() {
        assertThat(BookingStatus.PENDING.getDisplayName()).isEqualTo("Chờ xác nhận");
        assertThat(BookingStatus.CONFIRMED.getDisplayName()).isEqualTo("Đã xác nhận");
        assertThat(BookingStatus.COMPLETED.getDisplayName()).isEqualTo("Hoàn thành");
        assertThat(BookingStatus.PENDING_CANCEL.getDisplayName()).isEqualTo("Chờ hủy (đang xử lý hoàn tiền)");
        assertThat(BookingStatus.CANCELLED.getDisplayName()).isEqualTo("Đã hủy");
        assertThat(BookingStatus.NO_SHOW.getDisplayName()).isEqualTo("Không đến");
    }

    @Test
    void testBookingStatus_Values() {
        assertThat(BookingStatus.PENDING.getValue()).isEqualTo("pending");
        assertThat(BookingStatus.CONFIRMED.getValue()).isEqualTo("confirmed");
        assertThat(BookingStatus.COMPLETED.getValue()).isEqualTo("completed");
        assertThat(BookingStatus.PENDING_CANCEL.getValue()).isEqualTo("pending_cancel");
        assertThat(BookingStatus.CANCELLED.getValue()).isEqualTo("cancelled");
        assertThat(BookingStatus.NO_SHOW.getValue()).isEqualTo("no_show");
    }

    // ========== UserRole Tests ==========
    @Test
    void testUserRole_DisplayNames() {
        assertThat(UserRole.ADMIN.getDisplayName()).isEqualTo("Quản trị viên");
        assertThat(UserRole.CUSTOMER.getDisplayName()).isEqualTo("Khách hàng");
        assertThat(UserRole.RESTAURANT_OWNER.getDisplayName()).isEqualTo("Chủ nhà hàng");
        assertThat(UserRole.admin.getDisplayName()).isEqualTo("Quản trị viên");
        assertThat(UserRole.customer.getDisplayName()).isEqualTo("Khách hàng");
        assertThat(UserRole.restaurant_owner.getDisplayName()).isEqualTo("Chủ nhà hàng");
    }

    @Test
    void testUserRole_Values() {
        assertThat(UserRole.ADMIN.getValue()).isEqualTo("admin");
        assertThat(UserRole.CUSTOMER.getValue()).isEqualTo("customer");
        assertThat(UserRole.RESTAURANT_OWNER.getValue()).isEqualTo("restaurant_owner");
    }

    @Test
    void testUserRole_IsAdmin() {
        assertThat(UserRole.ADMIN.isAdmin()).isTrue();
        assertThat(UserRole.admin.isAdmin()).isTrue();
        assertThat(UserRole.CUSTOMER.isAdmin()).isFalse();
        assertThat(UserRole.RESTAURANT_OWNER.isAdmin()).isFalse();
    }

    @Test
    void testUserRole_IsCustomer() {
        assertThat(UserRole.CUSTOMER.isCustomer()).isTrue();
        assertThat(UserRole.customer.isCustomer()).isTrue();
        assertThat(UserRole.ADMIN.isCustomer()).isFalse();
        assertThat(UserRole.RESTAURANT_OWNER.isCustomer()).isFalse();
    }

    @Test
    void testUserRole_IsRestaurantOwner() {
        assertThat(UserRole.RESTAURANT_OWNER.isRestaurantOwner()).isTrue();
        assertThat(UserRole.restaurant_owner.isRestaurantOwner()).isTrue();
        assertThat(UserRole.ADMIN.isRestaurantOwner()).isFalse();
        assertThat(UserRole.CUSTOMER.isRestaurantOwner()).isFalse();
    }

    // ========== ContractType Tests ==========
    @Test
    void testContractType_DisplayNames() {
        assertThat(ContractType.STANDARD.getDisplayName()).isEqualTo("Hợp đồng tiêu chuẩn");
        assertThat(ContractType.PREMIUM.getDisplayName()).isEqualTo("Hợp đồng cao cấp");
        assertThat(ContractType.ENTERPRISE.getDisplayName()).isEqualTo("Hợp đồng doanh nghiệp");
        assertThat(ContractType.TRIAL.getDisplayName()).isEqualTo("Hợp đồng thử nghiệm");
    }

    @Test
    void testContractType_DefaultCommissionRate() {
        assertThat(ContractType.STANDARD.getDefaultCommissionRate()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(ContractType.PREMIUM.getDefaultCommissionRate()).isEqualByComparingTo(new BigDecimal("4.50"));
        assertThat(ContractType.ENTERPRISE.getDefaultCommissionRate()).isEqualByComparingTo(new BigDecimal("4.00"));
        assertThat(ContractType.TRIAL.getDefaultCommissionRate()).isEqualByComparingTo(new BigDecimal("0.00"));
    }

    @Test
    void testContractType_DefaultMinimumGuarantee() {
        assertThat(ContractType.STANDARD.getDefaultMinimumGuarantee()).isEqualByComparingTo(new BigDecimal("1000000.00"));
        assertThat(ContractType.PREMIUM.getDefaultMinimumGuarantee()).isEqualByComparingTo(new BigDecimal("2000000.00"));
        assertThat(ContractType.ENTERPRISE.getDefaultMinimumGuarantee()).isEqualByComparingTo(new BigDecimal("5000000.00"));
        assertThat(ContractType.TRIAL.getDefaultMinimumGuarantee()).isEqualByComparingTo(new BigDecimal("0.00"));
    }

    @Test
    void testContractType_DefaultDurationMonths() {
        assertThat(ContractType.STANDARD.getDefaultDurationMonths()).isEqualTo(12);
        assertThat(ContractType.PREMIUM.getDefaultDurationMonths()).isEqualTo(24);
        assertThat(ContractType.ENTERPRISE.getDefaultDurationMonths()).isEqualTo(36);
        assertThat(ContractType.TRIAL.getDefaultDurationMonths()).isEqualTo(3);
    }

    // ========== ContractStatus Tests ==========
    @Test
    void testContractStatus_DisplayNames() {
        assertThat(ContractStatus.DRAFT.getDisplayName()).isEqualTo("Bản nháp");
        assertThat(ContractStatus.PENDING_OWNER_SIGNATURE.getDisplayName()).isEqualTo("Chờ chủ nhà hàng ký");
        assertThat(ContractStatus.PENDING_ADMIN_SIGNATURE.getDisplayName()).isEqualTo("Chờ admin ký");
        assertThat(ContractStatus.ACTIVE.getDisplayName()).isEqualTo("Đang hiệu lực");
        assertThat(ContractStatus.EXPIRED.getDisplayName()).isEqualTo("Hết hạn");
        assertThat(ContractStatus.TERMINATED.getDisplayName()).isEqualTo("Chấm dứt");
        assertThat(ContractStatus.CANCELLED.getDisplayName()).isEqualTo("Đã hủy");
    }

    @Test
    void testContractStatus_IsActive() {
        assertThat(ContractStatus.DRAFT.isActive()).isFalse();
        assertThat(ContractStatus.PENDING_OWNER_SIGNATURE.isActive()).isFalse();
        assertThat(ContractStatus.PENDING_ADMIN_SIGNATURE.isActive()).isFalse();
        assertThat(ContractStatus.ACTIVE.isActive()).isTrue();
        assertThat(ContractStatus.EXPIRED.isActive()).isFalse();
        assertThat(ContractStatus.TERMINATED.isActive()).isFalse();
        assertThat(ContractStatus.CANCELLED.isActive()).isFalse();
    }

    @Test
    void testContractStatus_CanBeSigned() {
        assertThat(ContractStatus.DRAFT.canBeSigned()).isTrue();
        assertThat(ContractStatus.PENDING_OWNER_SIGNATURE.canBeSigned()).isTrue();
        assertThat(ContractStatus.PENDING_ADMIN_SIGNATURE.canBeSigned()).isTrue();
        assertThat(ContractStatus.ACTIVE.canBeSigned()).isFalse();
        assertThat(ContractStatus.EXPIRED.canBeSigned()).isFalse();
        assertThat(ContractStatus.TERMINATED.canBeSigned()).isFalse();
        assertThat(ContractStatus.CANCELLED.canBeSigned()).isFalse();
    }

    @Test
    void testContractStatus_CanBeTerminated() {
        assertThat(ContractStatus.DRAFT.canBeTerminated()).isFalse();
        assertThat(ContractStatus.PENDING_OWNER_SIGNATURE.canBeTerminated()).isFalse();
        assertThat(ContractStatus.PENDING_ADMIN_SIGNATURE.canBeTerminated()).isFalse();
        assertThat(ContractStatus.ACTIVE.canBeTerminated()).isTrue();
        assertThat(ContractStatus.EXPIRED.canBeTerminated()).isFalse();
        assertThat(ContractStatus.TERMINATED.canBeTerminated()).isFalse();
        assertThat(ContractStatus.CANCELLED.canBeTerminated()).isFalse();
    }

    @Test
    void testContractStatus_CanBeCancelled() {
        assertThat(ContractStatus.DRAFT.canBeCancelled()).isTrue();
        assertThat(ContractStatus.PENDING_OWNER_SIGNATURE.canBeCancelled()).isTrue();
        assertThat(ContractStatus.PENDING_ADMIN_SIGNATURE.canBeCancelled()).isTrue();
        assertThat(ContractStatus.ACTIVE.canBeCancelled()).isFalse();
        assertThat(ContractStatus.EXPIRED.canBeCancelled()).isFalse();
        assertThat(ContractStatus.TERMINATED.canBeCancelled()).isFalse();
        assertThat(ContractStatus.CANCELLED.canBeCancelled()).isFalse();
    }

    @Test
    void testContractStatus_ColorClass() {
        assertThat(ContractStatus.DRAFT.getColorClass()).isEqualTo("text-muted");
        assertThat(ContractStatus.PENDING_OWNER_SIGNATURE.getColorClass()).isEqualTo("text-warning");
        assertThat(ContractStatus.PENDING_ADMIN_SIGNATURE.getColorClass()).isEqualTo("text-warning");
        assertThat(ContractStatus.ACTIVE.getColorClass()).isEqualTo("text-success");
        assertThat(ContractStatus.EXPIRED.getColorClass()).isEqualTo("text-info");
        assertThat(ContractStatus.TERMINATED.getColorClass()).isEqualTo("text-danger");
        assertThat(ContractStatus.CANCELLED.getColorClass()).isEqualTo("text-danger");
    }

    @Test
    void testContractStatus_Icon() {
        assertThat(ContractStatus.DRAFT.getIcon()).isEqualTo("fas fa-edit");
        assertThat(ContractStatus.PENDING_OWNER_SIGNATURE.getIcon()).isEqualTo("fas fa-user-clock");
        assertThat(ContractStatus.PENDING_ADMIN_SIGNATURE.getIcon()).isEqualTo("fas fa-user-shield");
        assertThat(ContractStatus.ACTIVE.getIcon()).isEqualTo("fas fa-check-circle");
        assertThat(ContractStatus.EXPIRED.getIcon()).isEqualTo("fas fa-clock");
        assertThat(ContractStatus.TERMINATED.getIcon()).isEqualTo("fas fa-times-circle");
        assertThat(ContractStatus.CANCELLED.getIcon()).isEqualTo("fas fa-ban");
    }
}

