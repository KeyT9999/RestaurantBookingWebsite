package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for NotificationType enum
 */
@DisplayName("NotificationType Enum Tests")
public class NotificationTypeTest {

    @Test
    @DisplayName("shouldHaveAllEnumValues")
    void shouldHaveAllEnumValues() {
        assertNotNull(NotificationType.BOOKING_CONFIRMED);
        assertNotNull(NotificationType.BOOKING_CANCELLED);
        assertNotNull(NotificationType.BOOKING_REMINDER);
        assertNotNull(NotificationType.PAYMENT_SUCCESS);
        assertNotNull(NotificationType.PAYMENT_STATUS);
        assertNotNull(NotificationType.WITHDRAWAL_STATUS);
        assertNotNull(NotificationType.VOUCHER_ASSIGNED);
        assertNotNull(NotificationType.REVIEW_REQUEST);
        assertNotNull(NotificationType.REVIEW_REPORT_SUBMITTED);
        assertNotNull(NotificationType.REVIEW_REPORT_RESOLVED);
        assertNotNull(NotificationType.REVIEW_REPORT_REJECTED);
        assertNotNull(NotificationType.RESTAURANT_REGISTRATION_SUBMITTED);
        assertNotNull(NotificationType.RESTAURANT_APPROVED);
        assertNotNull(NotificationType.RESTAURANT_REJECTED);
        assertNotNull(NotificationType.RESTAURANT_RESUBMIT);
        assertNotNull(NotificationType.RESTAURANT_SUSPENDED);
        assertNotNull(NotificationType.RESTAURANT_ACTIVATED);
        assertNotNull(NotificationType.SYSTEM_ANNOUNCEMENT);
    }

    @Test
    @DisplayName("shouldGetDisplayName_forAllValues")
    void shouldGetDisplayName_forAllValues() {
        assertEquals("Đặt bàn được xác nhận", NotificationType.BOOKING_CONFIRMED.getDisplayName());
        assertEquals("Đặt bàn bị hủy", NotificationType.BOOKING_CANCELLED.getDisplayName());
        assertEquals("Nhắc nhở đặt bàn", NotificationType.BOOKING_REMINDER.getDisplayName());
        assertEquals("Thanh toán thành công", NotificationType.PAYMENT_SUCCESS.getDisplayName());
        assertEquals("Trạng thái thanh toán", NotificationType.PAYMENT_STATUS.getDisplayName());
        assertEquals("Trạng thái rút tiền", NotificationType.WITHDRAWAL_STATUS.getDisplayName());
        assertEquals("Nhận voucher", NotificationType.VOUCHER_ASSIGNED.getDisplayName());
        assertEquals("Yêu cầu đánh giá", NotificationType.REVIEW_REQUEST.getDisplayName());
        assertEquals("Báo cáo đánh giá mới", NotificationType.REVIEW_REPORT_SUBMITTED.getDisplayName());
        assertEquals("Báo cáo đánh giá được chấp thuận", NotificationType.REVIEW_REPORT_RESOLVED.getDisplayName());
        assertEquals("Báo cáo đánh giá bị từ chối", NotificationType.REVIEW_REPORT_REJECTED.getDisplayName());
        assertEquals("Đăng ký nhà hàng mới", NotificationType.RESTAURANT_REGISTRATION_SUBMITTED.getDisplayName());
        assertEquals("Nhà hàng được duyệt", NotificationType.RESTAURANT_APPROVED.getDisplayName());
        assertEquals("Nhà hàng bị từ chối", NotificationType.RESTAURANT_REJECTED.getDisplayName());
        assertEquals("Nhà hàng cần chỉnh sửa", NotificationType.RESTAURANT_RESUBMIT.getDisplayName());
        assertEquals("Nhà hàng bị tạm dừng", NotificationType.RESTAURANT_SUSPENDED.getDisplayName());
        assertEquals("Nhà hàng được kích hoạt", NotificationType.RESTAURANT_ACTIVATED.getDisplayName());
        assertEquals("Thông báo hệ thống", NotificationType.SYSTEM_ANNOUNCEMENT.getDisplayName());
    }
}
