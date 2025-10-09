package com.example.booking.service;

import java.text.NumberFormat;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.domain.Notification;
import com.example.booking.domain.NotificationStatus;
import com.example.booking.domain.NotificationType;
import com.example.booking.domain.WithdrawalRequest;
import com.example.booking.repository.NotificationRepository;

/**
 * Service để gửi thông báo cho các sự kiện withdrawal
 */
@Service
public class WithdrawalNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(WithdrawalNotificationService.class);
    
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    
    public WithdrawalNotificationService(
        NotificationRepository notificationRepository,
        EmailService emailService
    ) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }
    
    /**
     * Gửi thông báo khi tạo withdrawal request
     */
    @Transactional
    public void notifyWithdrawalCreated(WithdrawalRequest request) {
        try {
            String message = String.format(
                "Yêu cầu rút tiền %s VNĐ đã được tạo và đang chờ admin duyệt",
                formatMoney(request.getAmount())
            );
            
            createNotification(
                request.getRestaurant().getOwner().getUser().getId(),
                NotificationType.PAYMENT_STATUS,
                "Yêu cầu rút tiền đã tạo",
                message
            );
            
            logger.info("📧 Sent notification for withdrawal created: {}", request.getRequestId());
        } catch (Exception e) {
            logger.error("Failed to send withdrawal created notification", e);
        }
    }
    
    /**
     * Gửi thông báo khi withdrawal được approve
     */
    @Transactional
    public void notifyWithdrawalApproved(WithdrawalRequest request) {
        try {
            String message = String.format(
                "Yêu cầu rút tiền %s VNĐ đã được duyệt và đang xử lý chuyển tiền",
                formatMoney(request.getAmount())
            );
            
            createNotification(
                request.getRestaurant().getOwner().getUser().getId(),
                NotificationType.PAYMENT_STATUS,
                "Yêu cầu rút tiền đã được duyệt",
                message
            );
            
            logger.info("📧 Sent notification for withdrawal approved: {}", request.getRequestId());
        } catch (Exception e) {
            logger.error("Failed to send withdrawal approved notification", e);
        }
    }
    
    /**
     * Gửi thông báo khi withdrawal bị reject
     */
    @Transactional
    public void notifyWithdrawalRejected(WithdrawalRequest request) {
        try {
            String message = String.format(
                "Yêu cầu rút tiền %s VNĐ đã bị từ chối. Lý do: %s",
                formatMoney(request.getAmount()),
                request.getRejectionReason() != null ? request.getRejectionReason() : "Không rõ"
            );
            
            createNotification(
                request.getRestaurant().getOwner().getUser().getId(),
                NotificationType.PAYMENT_STATUS,
                "Yêu cầu rút tiền bị từ chối",
                message
            );
            
            // Send email
            String ownerEmail = request.getRestaurant().getOwner().getUser().getEmail();
            if (ownerEmail != null) {
                emailService.sendEmail(
                    ownerEmail,
                    "Yêu cầu rút tiền bị từ chối",
                    buildRejectionEmailContent(request)
                );
            }
            
            logger.info("📧 Sent notification for withdrawal rejected: {}", request.getRequestId());
        } catch (Exception e) {
            logger.error("Failed to send withdrawal rejected notification", e);
        }
    }
    
    /**
     * Gửi thông báo khi withdrawal thành công
     */
    @Transactional
    public void notifyWithdrawalSucceeded(WithdrawalRequest request) {
        try {
            String message = String.format(
                "Đã chuyển %s VNĐ vào tài khoản %s. Vui lòng kiểm tra số dư ngân hàng",
                formatMoney(request.getAmount()),
                maskAccountNumber(request.getBankAccount().getAccountNumber())
            );
            
            createNotification(
                request.getRestaurant().getOwner().getUser().getId(),
                NotificationType.PAYMENT_STATUS,
                "Rút tiền thành công",
                message
            );
            
            // Send email
            String ownerEmail = request.getRestaurant().getOwner().getUser().getEmail();
            if (ownerEmail != null) {
                emailService.sendEmail(
                    ownerEmail,
                    "Rút tiền thành công",
                    buildSuccessEmailContent(request)
                );
            }
            
            logger.info("📧 Sent notification for withdrawal succeeded: {}", request.getRequestId());
        } catch (Exception e) {
            logger.error("Failed to send withdrawal succeeded notification", e);
        }
    }
    
    /**
     * Gửi thông báo khi withdrawal failed
     */
    @Transactional
    public void notifyWithdrawalFailed(WithdrawalRequest request) {
        try {
            String message = String.format(
                "Giao dịch rút tiền %s VNĐ thất bại. Lý do: %s. Vui lòng liên hệ hỗ trợ",
                formatMoney(request.getAmount()),
                request.getRejectionReason() != null ? request.getRejectionReason() : "Lỗi hệ thống"
            );
            
            createNotification(
                request.getRestaurant().getOwner().getUser().getId(),
                NotificationType.PAYMENT_STATUS,
                "Rút tiền thất bại",
                message
            );
            
            logger.info("📧 Sent notification for withdrawal failed: {}", request.getRequestId());
        } catch (Exception e) {
            logger.error("Failed to send withdrawal failed notification", e);
        }
    }
    
    /**
     * Create notification
     */
    private void createNotification(
        java.util.UUID userId,
        NotificationType type,
        String title,
        String content
    ) {
        Notification notification = new Notification();
        notification.setRecipientUserId(userId);
        notification.setType(type);
        notification.setContent(content);
        notification.setStatus(NotificationStatus.PENDING);
        
        notificationRepository.save(notification);
    }
    
    /**
     * Format money
     */
    private String formatMoney(java.math.BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
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
    
    /**
     * Build rejection email content
     */
    private String buildRejectionEmailContent(WithdrawalRequest request) {
        return String.format(
            "Kính gửi %s,\n\n" +
            "Yêu cầu rút tiền #%d của quý khách đã bị từ chối.\n\n" +
            "Số tiền: %s VNĐ\n" +
            "Lý do: %s\n\n" +
            "Vui lòng liên hệ bộ phận hỗ trợ nếu cần thêm thông tin.\n\n" +
            "Trân trọng,\n" +
            "Restaurant Booking System",
            request.getRestaurant().getRestaurantName(),
            request.getRequestId(),
            formatMoney(request.getAmount()),
            request.getRejectionReason()
        );
    }
    
    /**
     * Build success email content
     */
    private String buildSuccessEmailContent(WithdrawalRequest request) {
        return String.format(
            "Kính gửi %s,\n\n" +
            "Yêu cầu rút tiền #%d của quý khách đã được xử lý thành công.\n\n" +
            "Số tiền: %s VNĐ\n" +
            "Tài khoản nhận: %s - %s\n" +
            "Ngân hàng: %s\n\n" +
            "Vui lòng kiểm tra số dư tài khoản ngân hàng của quý khách.\n\n" +
            "Trân trọng,\n" +
            "Restaurant Booking System",
            request.getRestaurant().getRestaurantName(),
            request.getRequestId(),
            formatMoney(request.getAmount()),
            maskAccountNumber(request.getBankAccount().getAccountNumber()),
            request.getBankAccount().getAccountHolderName(),
            request.getBankAccount().getBankName()
        );
    }
}

