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
import com.example.booking.domain.Review;
import com.example.booking.repository.NotificationRepository;

@Service
@Transactional
public class ReviewNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReviewNotificationService.class);
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    /**
     * Gửi thông báo khi có đánh giá mới cho Restaurant Owner
     */
    public void notifyNewReviewToRestaurant(Review review) {
        try {
            UUID restaurantOwnerId = review.getRestaurant().getOwner().getUser().getId();
            String customerName = review.getCustomer().getFullName();
            String restaurantName = review.getRestaurant().getRestaurantName();
            String ratingStars = "⭐".repeat(review.getRating());
            
            String commentPreview = "";
            if (review.getComment() != null && !review.getComment().isEmpty()) {
                commentPreview = review.getComment().length() > 50 
                    ? review.getComment().substring(0, 50) + "..." 
                    : review.getComment();
                commentPreview = " Bình luận: " + commentPreview;
            }
            
            createNotification(
                restaurantOwnerId,
                NotificationType.REVIEW_REQUEST,
                "Có đánh giá mới",
                String.format("Khách hàng %s đã đánh giá %s cho nhà hàng %s.%s",
                    customerName, ratingStars, restaurantName, commentPreview),
                "/restaurant-owner/reviews"
            );
            
            logger.info("✅ Sent new review notification to restaurant for review: {}", review.getReviewId());
        } catch (Exception e) {
            logger.error("❌ Failed to send new review notification to restaurant", e);
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

