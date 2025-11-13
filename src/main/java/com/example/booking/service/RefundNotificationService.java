package com.example.booking.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.domain.Notification;
import com.example.booking.domain.NotificationStatus;
import com.example.booking.domain.NotificationType;
import com.example.booking.domain.RefundRequest;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.repository.NotificationRepository;
import com.example.booking.repository.UserRepository;

@Service
@Transactional
public class RefundNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(RefundNotificationService.class);
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Gửi thông báo khi tạo yêu cầu hoàn tiền cho Customer
     */
    public void notifyRefundRequestCreated(RefundRequest refundRequest) {
        try {
            UUID customerId = refundRequest.getCustomer().getUser().getId();
            String formattedAmount = formatMoney(refundRequest.getAmount());
            
            createNotification(
                customerId,
                NotificationType.PAYMENT_STATUS,
                "Yêu cầu hoàn tiền đã tạo",
                String.format("Yêu cầu hoàn tiền %s VNĐ đã được tạo và đang chờ xử lý.",
                    formattedAmount),
                "/booking/my"
            );
            
            logger.info("✅ Sent refund request created notification to customer for refund: {}", refundRequest.getRefundRequestId());
        } catch (Exception e) {
            logger.error("❌ Failed to send refund request created notification", e);
        }
    }
    
    /**
     * Gửi thông báo khi có yêu cầu hoàn tiền cho Restaurant Owner
     */
    public void notifyRefundRequestToRestaurant(RefundRequest refundRequest) {
        try {
            UUID restaurantOwnerId = refundRequest.getRestaurant().getOwner().getUser().getId();
            String customerName = refundRequest.getCustomer().getFullName();
            String formattedAmount = formatMoney(refundRequest.getAmount());
            Integer bookingId = refundRequest.getPayment().getBooking().getBookingId();
            
            createNotification(
                restaurantOwnerId,
                NotificationType.PAYMENT_STATUS,
                "Có yêu cầu hoàn tiền",
                String.format("Khách hàng %s đã yêu cầu hoàn tiền %s VNĐ cho đặt bàn #%d.",
                    customerName, formattedAmount, bookingId),
                "/restaurant-owner/bookings/" + bookingId
            );
            
            logger.info("✅ Sent refund request notification to restaurant for refund: {}", refundRequest.getRefundRequestId());
        } catch (Exception e) {
            logger.error("❌ Failed to send refund request notification to restaurant", e);
        }
    }
    
    /**
     * Gửi thông báo khi có yêu cầu hoàn tiền mới cho Admin
     */
    public void notifyRefundRequestToAdmin(RefundRequest refundRequest) {
        try {
            List<User> adminUsers = userRepository.findByRole(UserRole.ADMIN, org.springframework.data.domain.Pageable.unpaged()).getContent();
            
            String customerName = refundRequest.getCustomer().getFullName();
            String restaurantName = refundRequest.getRestaurant().getRestaurantName();
            String formattedAmount = formatMoney(refundRequest.getAmount());
            Integer bookingId = refundRequest.getPayment().getBooking().getBookingId();
            
            for (User admin : adminUsers) {
                createNotification(
                    admin.getId(),
                    NotificationType.PAYMENT_STATUS,
                    "Yêu cầu hoàn tiền mới cần duyệt",
                    String.format("Khách hàng %s yêu cầu hoàn tiền %s VNĐ từ nhà hàng %s. Đặt bàn #%d.",
                        customerName, formattedAmount, restaurantName, bookingId),
                    "/admin/refunds/" + refundRequest.getRefundRequestId()
                );
            }
            
            logger.info("✅ Sent refund request notification to admins for refund: {}", refundRequest.getRefundRequestId());
        } catch (Exception e) {
            logger.error("❌ Failed to send refund request notification to admins", e);
        }
    }
    
    /**
     * Gửi thông báo khi hoàn tiền được duyệt cho Customer và Restaurant Owner
     */
    public void notifyRefundApproved(RefundRequest refundRequest) {
        try {
            String formattedAmount = formatMoney(refundRequest.getAmount());
            Integer bookingId = refundRequest.getPayment().getBooking().getBookingId();
            
            // Notify Customer
            UUID customerId = refundRequest.getCustomer().getUser().getId();
            createNotification(
                customerId,
                NotificationType.PAYMENT_STATUS,
                "Yêu cầu hoàn tiền đã được duyệt",
                String.format("Yêu cầu hoàn tiền %s VNĐ đã được duyệt. Tiền sẽ được chuyển vào tài khoản trong 1-3 ngày làm việc.",
                    formattedAmount),
                "/booking/my"
            );
            
            // Notify Restaurant Owner
            UUID restaurantOwnerId = refundRequest.getRestaurant().getOwner().getUser().getId();
            createNotification(
                restaurantOwnerId,
                NotificationType.PAYMENT_STATUS,
                "Yêu cầu hoàn tiền đã được duyệt",
                String.format("Yêu cầu hoàn tiền %s VNĐ cho đặt bàn #%d đã được admin duyệt.",
                    formattedAmount, bookingId),
                "/restaurant-owner/bookings/" + bookingId
            );
            
            logger.info("✅ Sent refund approved notification to customer and restaurant owner for refund: {}", refundRequest.getRefundRequestId());
        } catch (Exception e) {
            logger.error("❌ Failed to send refund approved notification", e);
        }
    }
    
    /**
     * Gửi thông báo khi hoàn tiền bị từ chối cho Customer và Restaurant Owner
     */
    public void notifyRefundRejected(RefundRequest refundRequest, String reason) {
        try {
            String formattedAmount = formatMoney(refundRequest.getAmount());
            Integer bookingId = refundRequest.getPayment().getBooking().getBookingId();
            String rejectReason = reason != null && !reason.isBlank() ? reason : "Không rõ";
            
            // Notify Customer
            UUID customerId = refundRequest.getCustomer().getUser().getId();
            createNotification(
                customerId,
                NotificationType.PAYMENT_STATUS,
                "Yêu cầu hoàn tiền bị từ chối",
                String.format("Yêu cầu hoàn tiền %s VNĐ đã bị từ chối. Lý do: %s",
                    formattedAmount, rejectReason),
                "/booking/my"
            );
            
            // Notify Restaurant Owner
            UUID restaurantOwnerId = refundRequest.getRestaurant().getOwner().getUser().getId();
            createNotification(
                restaurantOwnerId,
                NotificationType.PAYMENT_STATUS,
                "Yêu cầu hoàn tiền bị từ chối",
                String.format("Yêu cầu hoàn tiền %s VNĐ cho đặt bàn #%d đã bị admin từ chối. Lý do: %s",
                    formattedAmount, bookingId, rejectReason),
                "/restaurant-owner/bookings/" + bookingId
            );
            
            logger.info("✅ Sent refund rejected notification to customer and restaurant owner for refund: {}", refundRequest.getRefundRequestId());
        } catch (Exception e) {
            logger.error("❌ Failed to send refund rejected notification", e);
        }
    }
    
    /**
     * Gửi thông báo khi hoàn tiền thành công cho Customer và Restaurant Owner
     */
    public void notifyRefundSucceeded(RefundRequest refundRequest) {
        try {
            String formattedAmount = formatMoney(refundRequest.getAmount());
            Integer bookingId = refundRequest.getPayment().getBooking().getBookingId();
            
            // Notify Customer
            UUID customerId = refundRequest.getCustomer().getUser().getId();
            createNotification(
                customerId,
                NotificationType.PAYMENT_STATUS,
                "Hoàn tiền thành công",
                String.format("Đã hoàn tiền %s VNĐ vào tài khoản của bạn. Vui lòng kiểm tra số dư.",
                    formattedAmount),
                "/booking/my"
            );
            
            // Notify Restaurant Owner
            UUID restaurantOwnerId = refundRequest.getRestaurant().getOwner().getUser().getId();
            createNotification(
                restaurantOwnerId,
                NotificationType.PAYMENT_STATUS,
                "Hoàn tiền đã hoàn tất",
                String.format("Yêu cầu hoàn tiền %s VNĐ cho đặt bàn #%d đã được xử lý thành công.",
                    formattedAmount, bookingId),
                "/restaurant-owner/bookings/" + bookingId
            );
            
            logger.info("✅ Sent refund succeeded notification to customer and restaurant owner for refund: {}", refundRequest.getRefundRequestId());
        } catch (Exception e) {
            logger.error("❌ Failed to send refund succeeded notification", e);
        }
    }
    
    /**
     * Gửi thông báo khi refund status thay đổi
     * Tự động xác định loại notification dựa trên status mới
     */
    public void notifyRefundStatusChanged(RefundRequest refundRequest, com.example.booking.common.enums.RefundStatus oldStatus) {
        com.example.booking.common.enums.RefundStatus newStatus = refundRequest.getStatus();
        
        // Chỉ gửi notification khi status thực sự thay đổi
        if (oldStatus == newStatus) {
            return;
        }
        
        try {
            switch (newStatus) {
                case PENDING:
                    // Khi tạo mới (oldStatus == null)
                    if (oldStatus == null) {
                        notifyRefundRequestCreated(refundRequest);
                        notifyRefundRequestToRestaurant(refundRequest);
                        notifyRefundRequestToAdmin(refundRequest);
                    }
                    break;
                    
                case COMPLETED:
                    notifyRefundSucceeded(refundRequest);
                    break;
                    
                case REJECTED:
                    notifyRefundRejected(refundRequest, refundRequest.getAdminNote());
                    break;
                    
                default:
                    logger.warn("⚠️ Unknown refund status: {}", newStatus);
                    break;
            }
        } catch (Exception e) {
            logger.error("❌ Failed to send refund status change notification", e);
        }
    }
    
    private void createNotification(UUID userId, NotificationType type, String title, String content, String linkUrl) {
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
    
    private String formatMoney(BigDecimal amount) {
        return java.text.NumberFormat.getInstance(java.util.Locale.forLanguageTag("vi-VN")).format(amount);
    }
}

