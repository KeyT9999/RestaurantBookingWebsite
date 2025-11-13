package com.example.booking.service;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.common.enums.WithdrawalStatus;
import com.example.booking.domain.Notification;
import com.example.booking.domain.NotificationStatus;
import com.example.booking.domain.NotificationType;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.domain.WithdrawalRequest;
import com.example.booking.repository.NotificationRepository;
import com.example.booking.repository.UserRepository;

/**
 * Service để gửi thông báo cho các sự kiện withdrawal
 */
@Service
@Transactional
public class WithdrawalNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(WithdrawalNotificationService.class);
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Gửi thông báo khi tạo withdrawal request
     * Gửi cho Restaurant Owner và tất cả Admin
     */
    public void notifyWithdrawalCreated(WithdrawalRequest request) {
        try {
            String formattedAmount = formatMoney(request.getAmount());
            String restaurantName = request.getRestaurant().getRestaurantName();
            
            // Notify Restaurant Owner
            UUID ownerId = request.getRestaurant().getOwner().getUser().getId();
            createNotification(
                    ownerId,
                    NotificationType.WITHDRAWAL_STATUS,
                "Yêu cầu rút tiền đã tạo",
                    String.format("Yêu cầu rút tiền %s VNĐ đã được tạo và đang chờ admin duyệt.",
                            formattedAmount),
                    "/restaurant-owner/withdrawals/" + request.getRequestId()
            );
            logger.info("✅ Sent withdrawal created notification to restaurant owner for request: {}",
                    request.getRequestId());
            
            // Notify all admins
            List<User> admins = userRepository.findByRole(UserRole.ADMIN, Pageable.unpaged()).getContent();
            for (User admin : admins) {
                createNotification(
                        admin.getId(),
                        NotificationType.WITHDRAWAL_STATUS,
                        "Yêu cầu rút tiền mới",
                        String.format("Nhà hàng %s đã tạo yêu cầu rút tiền %s VNĐ. Vui lòng xem xét và duyệt.",
                                restaurantName, formattedAmount),
                        "/admin/withdrawal/" + request.getRequestId());
            }
            logger.info("✅ Sent withdrawal created notification to {} admins for request: {}", admins.size(),
                    request.getRequestId());

        } catch (Exception e) {
            logger.error("❌ Failed to send withdrawal created notifications", e);
        }
    }
    
    /**
     * Gửi thông báo khi withdrawal được approve cho Restaurant Owner
     */
    public void notifyWithdrawalApproved(WithdrawalRequest request) {
        try {
            UUID ownerId = request.getRestaurant().getOwner().getUser().getId();
            String formattedAmount = formatMoney(request.getAmount());
            
            createNotification(
                    ownerId,
                    NotificationType.WITHDRAWAL_STATUS,
                "Yêu cầu rút tiền đã được duyệt",
                    String.format("Yêu cầu rút tiền %s VNĐ đã được duyệt và đang xử lý chuyển tiền.",
                            formattedAmount),
                    "/restaurant-owner/withdrawals/" + request.getRequestId()
            );
            
            logger.info("✅ Sent withdrawal approved notification to restaurant owner for request: {}",
                    request.getRequestId());
        } catch (Exception e) {
            logger.error("❌ Failed to send withdrawal approved notification", e);
        }
    }
    
    /**
     * Gửi thông báo khi withdrawal bị reject cho Restaurant Owner
     */
    public void notifyWithdrawalRejected(WithdrawalRequest request) {
        try {
            UUID ownerId = request.getRestaurant().getOwner().getUser().getId();
            String formattedAmount = formatMoney(request.getAmount());
            String reason = request.getRejectionReason() != null && !request.getRejectionReason().isBlank()
                    ? request.getRejectionReason()
                    : "Không rõ";
            
            createNotification(
                    ownerId,
                    NotificationType.WITHDRAWAL_STATUS,
                "Yêu cầu rút tiền bị từ chối",
                    String.format("Yêu cầu rút tiền %s VNĐ đã bị từ chối. Lý do: %s",
                            formattedAmount, reason),
                    "/restaurant-owner/withdrawals/" + request.getRequestId()
            );
            
            logger.info("✅ Sent withdrawal rejected notification to restaurant owner for request: {}",
                    request.getRequestId());
        } catch (Exception e) {
            logger.error("❌ Failed to send withdrawal rejected notification", e);
        }
    }

    /**
     * Gửi thông báo khi withdrawal thành công cho Restaurant Owner
     */
    public void notifyWithdrawalSucceeded(WithdrawalRequest request) {
        try {
            UUID ownerId = request.getRestaurant().getOwner().getUser().getId();
            String formattedAmount = formatMoney(request.getAmount());
            String maskedAccount = maskAccountNumber(request.getBankAccount().getAccountNumber());

            createNotification(
                    ownerId,
                    NotificationType.WITHDRAWAL_STATUS,
                    "Rút tiền thành công",
                    String.format("Đã chuyển %s VNĐ vào tài khoản %s. Vui lòng kiểm tra số dư ngân hàng.",
                            formattedAmount, maskedAccount),
                    "/restaurant-owner/withdrawals/" + request.getRequestId());

            logger.info("✅ Sent withdrawal succeeded notification to restaurant owner for request: {}",
                    request.getRequestId());
        } catch (Exception e) {
            logger.error("❌ Failed to send withdrawal succeeded notification", e);
        }
    }

    /**
     * Gửi thông báo khi withdrawal failed cho Restaurant Owner và Admin
     */
    public void notifyWithdrawalFailed(WithdrawalRequest request) {
        try {
            String formattedAmount = formatMoney(request.getAmount());
            String reason = request.getRejectionReason() != null && !request.getRejectionReason().isBlank()
                    ? request.getRejectionReason()
                    : "Lỗi hệ thống";

            // Notify Restaurant Owner
            UUID ownerId = request.getRestaurant().getOwner().getUser().getId();
            createNotification(
                    ownerId,
                    NotificationType.WITHDRAWAL_STATUS,
                    "Rút tiền thất bại",
                    String.format("Giao dịch rút tiền %s VNĐ thất bại. Lý do: %s. Vui lòng liên hệ hỗ trợ.",
                            formattedAmount, reason),
                    "/restaurant-owner/withdrawals/" + request.getRequestId()
            );
            
            // Notify all admins
            List<User> admins = userRepository.findByRole(UserRole.ADMIN, Pageable.unpaged()).getContent();
            for (User admin : admins) {
                createNotification(
                        admin.getId(),
                        NotificationType.WITHDRAWAL_STATUS,
                        "Rút tiền thất bại",
                        String.format("Giao dịch rút tiền %s VNĐ từ nhà hàng %s thất bại. Lý do: %s",
                                formattedAmount, request.getRestaurant().getRestaurantName(), reason),
                        "/admin/withdrawal/" + request.getRequestId());
            }

            logger.info("✅ Sent withdrawal failed notification to restaurant owner and {} admins for request: {}",
                    admins.size(), request.getRequestId());
        } catch (Exception e) {
            logger.error("❌ Failed to send withdrawal failed notification", e);
        }
    }

    /**
     * Gửi thông báo khi withdrawal đang xử lý (PROCESSING) cho Restaurant Owner
     */
    public void notifyWithdrawalProcessing(WithdrawalRequest request) {
        try {
            UUID ownerId = request.getRestaurant().getOwner().getUser().getId();
            String formattedAmount = formatMoney(request.getAmount());

            createNotification(
                    ownerId,
                    NotificationType.WITHDRAWAL_STATUS,
                    "Đang xử lý rút tiền",
                    String.format("Yêu cầu rút tiền %s VNĐ đang được xử lý chuyển tiền.",
                            formattedAmount),
                    "/restaurant-owner/withdrawals/" + request.getRequestId());
            
            logger.info("✅ Sent withdrawal processing notification to restaurant owner for request: {}",
                    request.getRequestId());
        } catch (Exception e) {
            logger.error("❌ Failed to send withdrawal processing notification", e);
        }
    }
    
    /**
     * Gửi thông báo khi withdrawal bị hủy (CANCELLED) cho Restaurant Owner và Admin
     */
    public void notifyWithdrawalCancelled(WithdrawalRequest request) {
        try {
            String formattedAmount = formatMoney(request.getAmount());
            String restaurantName = request.getRestaurant().getRestaurantName();

            // Notify Restaurant Owner
            UUID ownerId = request.getRestaurant().getOwner().getUser().getId();
            createNotification(
                    ownerId,
                    NotificationType.WITHDRAWAL_STATUS,
                    "Yêu cầu rút tiền đã bị hủy",
                    String.format("Yêu cầu rút tiền %s VNĐ đã bị hủy.",
                            formattedAmount),
                    "/restaurant-owner/withdrawals/" + request.getRequestId()
            );
            
            // Notify all admins
            List<User> admins = userRepository.findByRole(UserRole.ADMIN, Pageable.unpaged()).getContent();
            for (User admin : admins) {
                createNotification(
                        admin.getId(),
                        NotificationType.WITHDRAWAL_STATUS,
                        "Yêu cầu rút tiền đã bị hủy",
                        String.format("Yêu cầu rút tiền %s VNĐ từ nhà hàng %s đã bị hủy.",
                                formattedAmount, restaurantName),
                        "/admin/withdrawal/" + request.getRequestId());
            }

            logger.info("✅ Sent withdrawal cancelled notification to restaurant owner and {} admins for request: {}",
                    admins.size(), request.getRequestId());
        } catch (Exception e) {
            logger.error("❌ Failed to send withdrawal cancelled notification", e);
        }
    }

    /**
     * Gửi thông báo khi withdrawal status thay đổi
     * Tự động xác định loại notification dựa trên status mới
     */
    public void notifyWithdrawalStatusChanged(WithdrawalRequest request, WithdrawalStatus oldStatus) {
        WithdrawalStatus newStatus = request.getStatus();

        // Chỉ gửi notification khi status thực sự thay đổi
        if (oldStatus == newStatus) {
            return;
        }

        try {
            switch (newStatus) {
                case PENDING:
                    // Khi tạo mới (oldStatus == null)
                    if (oldStatus == null) {
                        notifyWithdrawalCreated(request);
                    }
                    break;

                case APPROVED:
                    notifyWithdrawalApproved(request);
                    break;

                case PROCESSING:
                    notifyWithdrawalProcessing(request);
                    break;

                case SUCCEEDED:
                    notifyWithdrawalSucceeded(request);
                    break;

                case REJECTED:
                    notifyWithdrawalRejected(request);
                    break;

                case FAILED:
                    notifyWithdrawalFailed(request);
                    break;

                case CANCELLED:
                    notifyWithdrawalCancelled(request);
                    break;

                default:
                    logger.warn("⚠️ Unknown withdrawal status: {}", newStatus);
                    break;
            }
        } catch (Exception e) {
            logger.error("❌ Failed to send withdrawal status change notification", e);
        }
    }
    
    /**
     * Create notification
     */
    private void createNotification(
            UUID userId,
        NotificationType type,
        String title,
            String content,
            String linkUrl
    ) {
        Notification notification = new Notification();
        notification.setRecipientUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setLinkUrl(linkUrl);
        notification.setStatus(NotificationStatus.SENT);
        notification.setPriority(1);
        notification.setPublishAt(LocalDateTime.now());
        notification.setExpireAt(LocalDateTime.now().plusDays(30));
        
        notificationRepository.save(notification);
    }
    
    /**
     * Format money
     */
    private String formatMoney(java.math.BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
        return formatter.format(amount);
    }
    
    /**
     * Mask account number
     */
    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 6) {
            return "***";
        }
        int len = accountNumber.length();
        return accountNumber.substring(0, 3) + "****" + accountNumber.substring(len - 3);
    }

}

