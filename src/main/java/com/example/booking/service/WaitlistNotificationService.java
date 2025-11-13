package com.example.booking.service;

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
import com.example.booking.domain.Waitlist;
import com.example.booking.domain.WaitlistStatus;
import com.example.booking.repository.NotificationRepository;

@Service
@Transactional
public class WaitlistNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(WaitlistNotificationService.class);
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    /**
     * Gửi thông báo khi có waitlist mới cho Restaurant Owner
     */
    public void notifyNewWaitlistToRestaurant(Waitlist waitlist) {
        try {
            UUID restaurantOwnerId = waitlist.getRestaurant().getOwner().getUser().getId();
            String restaurantName = waitlist.getRestaurant().getRestaurantName();
            String customerName = waitlist.getCustomer().getFullName();
            String joinTime = waitlist.getJoinTime().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            
            createNotification(
                restaurantOwnerId,
                NotificationType.WAITLIST_STATUS,
                "Có khách hàng mới vào danh sách chờ",
                String.format("Khách hàng %s đã tham gia danh sách chờ tại %s vào %s. Số khách: %d",
                    customerName, restaurantName, joinTime, waitlist.getPartySize()),
                "/restaurant-owner/waitlist/" + waitlist.getWaitlistId()
            );
            
            logger.info("✅ Sent new waitlist notification to restaurant owner for waitlist: {}", waitlist.getWaitlistId());
        } catch (Exception e) {
            logger.error("❌ Failed to send new waitlist notification to restaurant", e);
        }
    }
    
    /**
     * Gửi thông báo khi customer hủy waitlist cho Restaurant Owner
     */
    public void notifyWaitlistCancelledToRestaurant(Waitlist waitlist) {
        try {
            UUID restaurantOwnerId = waitlist.getRestaurant().getOwner().getUser().getId();
            String restaurantName = waitlist.getRestaurant().getRestaurantName();
            String customerName = waitlist.getCustomer().getFullName();
            
            createNotification(
                restaurantOwnerId,
                NotificationType.WAITLIST_STATUS,
                "Khách hàng đã hủy danh sách chờ",
                String.format("Khách hàng %s đã hủy danh sách chờ tại %s.",
                    customerName, restaurantName),
                "/restaurant-owner/waitlist"
            );
            
            logger.info("✅ Sent waitlist cancelled notification to restaurant owner for waitlist: {}", waitlist.getWaitlistId());
        } catch (Exception e) {
            logger.error("❌ Failed to send waitlist cancelled notification to restaurant", e);
        }
    }
    
    /**
     * Gửi thông báo khi restaurant owner cho customer ngồi (status SEATED) cho Customer
     */
    public void notifyWaitlistSeatedToCustomer(Waitlist waitlist) {
        try {
            UUID customerId = waitlist.getCustomer().getUser().getId();
            String restaurantName = waitlist.getRestaurant().getRestaurantName();
            
            createNotification(
                customerId,
                NotificationType.WAITLIST_STATUS,
                "Bạn đã được sắp chỗ ngồi",
                String.format("Nhà hàng %s đã sắp chỗ ngồi cho bạn. Chúc bạn có bữa ăn ngon miệng!",
                    restaurantName),
                "/booking/waitlist/" + waitlist.getWaitlistId()
            );
            
            logger.info("✅ Sent waitlist seated notification to customer for waitlist: {}", waitlist.getWaitlistId());
        } catch (Exception e) {
            logger.error("❌ Failed to send waitlist seated notification to customer", e);
        }
    }
    
    /**
     * Gửi thông báo khi waitlist status thay đổi
     * Tự động xác định loại notification dựa trên status mới
     */
    public void notifyWaitlistStatusChanged(Waitlist waitlist, WaitlistStatus oldStatus) {
        WaitlistStatus newStatus = waitlist.getStatus();
        
        // Chỉ gửi notification khi status thực sự thay đổi
        if (oldStatus == newStatus) {
            return;
        }
        
        try {
            switch (newStatus) {
                case WAITING:
                    // Khi tạo mới waitlist (oldStatus == null hoặc không phải WAITING)
                    if (oldStatus == null || oldStatus != WaitlistStatus.WAITING) {
                        notifyNewWaitlistToRestaurant(waitlist);
                    }
                    break;
                    
                case SEATED:
                    // Restaurant owner cho customer ngồi
                    notifyWaitlistSeatedToCustomer(waitlist);
                    break;
                    
                case CANCELLED:
                    // Customer hoặc restaurant owner hủy
                    // Chỉ gửi notification cho restaurant owner nếu customer hủy
                    if (oldStatus == WaitlistStatus.WAITING) {
                        notifyWaitlistCancelledToRestaurant(waitlist);
                    }
                    break;
                    
                case CALLED:
                    // CALLED status không còn được sử dụng, bỏ qua
                    logger.debug("⚠️ CALLED status is deprecated, skipping notification");
                    break;
                    
                default:
                    logger.warn("⚠️ Unknown waitlist status: {}", newStatus);
                    break;
            }
        } catch (Exception e) {
            logger.error("❌ Failed to send waitlist status change notification", e);
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
}

