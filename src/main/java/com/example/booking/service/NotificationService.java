package com.example.booking.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.booking.domain.UserRole;
import com.example.booking.dto.notification.AdminNotificationSummary;
import com.example.booking.dto.notification.NotificationForm;
import com.example.booking.dto.notification.NotificationView;

public interface NotificationService {

    // ============= ADMIN METHODS =============

    /**
     * Gửi thông báo cho tất cả user
     */
    int sendToAll(NotificationForm form, UUID adminId);

    /**
     * Gửi thông báo cho user theo vai trò
     */
    int sendToRoles(NotificationForm form, Set<UserRole> roles, UUID adminId);

    /**
     * Gửi thông báo cho user cụ thể
     */
    int sendToUsers(NotificationForm form, Set<UUID> userIds, UUID adminId);

    /**
     * Gửi thông báo dựa theo audience trong NotificationForm
     */
    void sendNotifications(NotificationForm form, UUID adminId);

    /**
     * Lấy danh sách thông báo cho admin
     */
    Page<NotificationView> findAllForAdmin(Pageable pageable);
    Page<AdminNotificationSummary> findGroupedForAdmin(Pageable pageable);

    /**
     * Lấy chi tiết thông báo theo ID
     */
    NotificationView findById(Integer id);

    /**
     * Đếm tổng số thông báo đã gửi
     */
    long countTotalSent();

    /**
     * Đếm tổng số thông báo đã đọc
     */
    long countTotalRead();

    /**
     * Hết hạn thông báo ngay lập tức
     */
    void expireNotification(Integer notificationId);

    // ============= USER METHODS =============

    /**
     * Lấy danh sách thông báo cho user
     */
    Page<NotificationView> findByUserId(UUID userId, Pageable pageable);

    /**
     * Lấy danh sách thông báo cho user (đã đọc/chưa đọc)
     */
    Page<NotificationView> findByUserIdAndUnread(UUID userId, boolean unread, Pageable pageable);

    /**
     * Đếm số thông báo chưa đọc
     */
    long countUnreadByUserId(UUID userId);

    /**
     * Đánh dấu thông báo đã đọc
     */
    void markAsRead(Integer notificationId, UUID userId);

    /**
     * Đánh dấu tất cả thông báo đã đọc
     */
    void markAllAsRead(UUID userId);

    /**
     * Lấy 5 thông báo mới nhất cho dropdown
     */
    List<NotificationView> getLatestNotifications(UUID userId);
}
