package com.example.booking.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.domain.Notification;
import com.example.booking.domain.NotificationStatus;
import com.example.booking.domain.NotificationType;
import com.example.booking.domain.Payment;
import com.example.booking.repository.NotificationRepository;

@Service
@Transactional
public class PaymentNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentNotificationService.class);
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    /**
     * Gửi thông báo thanh toán thành công cho Customer
     */
    public void notifyPaymentSuccessToCustomer(Payment payment) {
        try {
            UUID customerId = payment.getBooking().getCustomer().getUser().getId();
            String restaurantName = payment.getBooking().getRestaurant().getRestaurantName();
            String formattedAmount = formatMoney(payment.getAmount());
            
            createNotification(
                customerId,
                NotificationType.PAYMENT_SUCCESS,
                "Thanh toán thành công",
                String.format("Bạn đã thanh toán thành công %s VNĐ cho đặt bàn tại %s.",
                    formattedAmount, restaurantName),
                "/payment/result/" + payment.getPaymentId()
            );
            
            logger.info("✅ Sent payment success notification to customer for payment: {}", payment.getPaymentId());
        } catch (Exception e) {
            logger.error("❌ Failed to send payment success notification to customer", e);
        }
    }
    
    /**
     * Gửi thông báo thanh toán thành công cho Restaurant Owner
     */
    public void notifyPaymentSuccessToRestaurant(Payment payment) {
        try {
            UUID restaurantOwnerId = payment.getBooking().getRestaurant().getOwner().getUser().getId();
            String customerName = payment.getBooking().getCustomer().getFullName();
            String formattedAmount = formatMoney(payment.getAmount());
            
            createNotification(
                restaurantOwnerId,
                NotificationType.PAYMENT_STATUS,
                "Khách hàng đã thanh toán",
                String.format("Khách hàng %s đã thanh toán %s VNĐ cho đặt bàn #%d.",
                    customerName, formattedAmount, payment.getBooking().getBookingId()),
                "/restaurant-owner/bookings/" + payment.getBooking().getBookingId()
            );
            
            logger.info("✅ Sent payment success notification to restaurant for payment: {}", payment.getPaymentId());
        } catch (Exception e) {
            logger.error("❌ Failed to send payment success notification to restaurant", e);
        }
    }
    
    /**
     * Gửi thông báo thanh toán thất bại cho Customer
     */
    public void notifyPaymentFailedToCustomer(Payment payment, String reason) {
        try {
            UUID customerId = payment.getBooking().getCustomer().getUser().getId();
            String restaurantName = payment.getBooking().getRestaurant().getRestaurantName();
            
            createNotification(
                customerId,
                NotificationType.PAYMENT_STATUS,
                "Thanh toán thất bại",
                String.format("Thanh toán cho đặt bàn tại %s thất bại. Lý do: %s. Vui lòng thử lại.",
                    restaurantName, reason != null ? reason : "Không rõ"),
                "/payment/" + payment.getBooking().getBookingId()
            );
            
            logger.info("✅ Sent payment failed notification to customer for payment: {}", payment.getPaymentId());
        } catch (Exception e) {
            logger.error("❌ Failed to send payment failed notification to customer", e);
        }
    }
    
    /**
     * Gửi thông báo thanh toán thất bại cho Restaurant Owner
     */
    public void notifyPaymentFailedToRestaurant(Payment payment, String reason) {
        try {
            UUID restaurantOwnerId = payment.getBooking().getRestaurant().getOwner().getUser().getId();
            String customerName = payment.getBooking().getCustomer().getFullName();
            
            createNotification(
                restaurantOwnerId,
                NotificationType.PAYMENT_STATUS,
                "Thanh toán thất bại",
                String.format("Thanh toán của khách hàng %s cho đặt bàn #%d thất bại. Lý do: %s",
                    customerName, payment.getBooking().getBookingId(), reason != null ? reason : "Không rõ"),
                "/restaurant-owner/bookings/" + payment.getBooking().getBookingId()
            );
            
            logger.info("✅ Sent payment failed notification to restaurant for payment: {}", payment.getPaymentId());
        } catch (Exception e) {
            logger.error("❌ Failed to send payment failed notification to restaurant", e);
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
        return java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN")).format(amount);
    }
}

