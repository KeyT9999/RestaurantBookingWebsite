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
import com.example.booking.repository.NotificationRepository;

@Service
@Transactional
public class VoucherNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(VoucherNotificationService.class);
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    /**
     * Gửi thông báo khi customer nhận voucher
     */
    public void notifyVoucherAssigned(UUID customerId, String voucherCode, BigDecimal discountAmount, String voucherName) {
        try {
            String formattedAmount = formatMoney(discountAmount);
            String name = voucherName != null && !voucherName.isEmpty() ? voucherName : "voucher";
            
            createNotification(
                customerId,
                NotificationType.VOUCHER_ASSIGNED,
                "Bạn đã nhận voucher",
                String.format("Bạn đã nhận %s giảm %s VNĐ. Mã: %s",
                    name, formattedAmount, voucherCode),
                "/customer/vouchers"
            );
            
            logger.info("✅ Sent voucher assigned notification to customer: {}", customerId);
        } catch (Exception e) {
            logger.error("❌ Failed to send voucher assigned notification", e);
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

