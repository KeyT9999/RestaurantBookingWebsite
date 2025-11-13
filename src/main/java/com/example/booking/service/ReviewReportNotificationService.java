package com.example.booking.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.domain.Customer;
import com.example.booking.domain.Notification;
import com.example.booking.domain.NotificationStatus;
import com.example.booking.domain.NotificationType;
import com.example.booking.domain.ReviewReport;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.repository.NotificationRepository;
import com.example.booking.repository.UserRepository;

@Service
@Transactional
public class ReviewReportNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReviewReportNotificationService.class);
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    /**
     * Gửi thông báo khi restaurant owner tạo review report cho Customer và Admin
     */
    public void notifyReviewReportSubmitted(ReviewReport report) {
        try {
            // Get user ID from customer
            UUID userId = getCustomerUserId(report);
            if (userId != null) {
                createNotification(
                    userId,
                    NotificationType.REVIEW_REPORT_SUBMITTED,
                    "Review của bạn đã bị báo cáo",
                    String.format("Nhà hàng %s đã báo cáo review của bạn. Admin sẽ xem xét và xử lý.",
                        report.getRestaurant().getRestaurantName()),
                    "/reviews/my-reviews"
                );
                logger.info("✅ Sent review report submitted notification to customer (userId: {}) for report: {}", userId, report.getReportId());
            } else {
                logger.warn("⚠️ Could not find user ID for customer in report: {}", report.getReportId());
            }
            
            // Notify all admins
            List<User> admins = userRepository.findByRole(UserRole.ADMIN, org.springframework.data.domain.Pageable.unpaged()).getContent();
            for (User admin : admins) {
                createNotification(
                    admin.getId(),
                    NotificationType.REVIEW_REPORT_SUBMITTED,
                    "Báo cáo review mới",
                    String.format("Nhà hàng %s đã báo cáo review từ khách hàng %s. Lý do: %s",
                        report.getRestaurant().getRestaurantName(),
                        report.getCustomerNameSnapshot() != null ? report.getCustomerNameSnapshot() : "Không rõ",
                        report.getReasonText() != null && report.getReasonText().length() > 100 
                            ? report.getReasonText().substring(0, 100) + "..." 
                            : report.getReasonText()),
                    "/admin/moderation/" + report.getReportId()
                );
            }
            logger.info("✅ Sent review report submitted notification to {} admins for report: {}", admins.size(), report.getReportId());
            
        } catch (Exception e) {
            logger.error("❌ Failed to send review report submitted notifications", e);
        }
    }
    
    /**
     * Gửi thông báo khi admin resolve review report (đồng ý, gỡ review) cho Customer và Restaurant Owner
     */
    public void notifyReviewReportResolved(ReviewReport report) {
        try {
            // Notify customer
            UUID userId = getCustomerUserId(report);
            if (userId != null) {
                createNotification(
                    userId,
                    NotificationType.REVIEW_REPORT_RESOLVED,
                    "Review của bạn đã bị gỡ",
                    String.format("Review của bạn cho nhà hàng %s đã bị gỡ sau khi được xem xét bởi admin. Lý do: %s",
                        report.getRestaurant().getRestaurantName(),
                        report.getResolutionMessage() != null && !report.getResolutionMessage().isBlank()
                            ? report.getResolutionMessage()
                            : "Review không phù hợp với quy định của nền tảng."),
                    "/reviews/my-reviews"
                );
                logger.info("✅ Sent review report resolved notification to customer (userId: {}) for report: {}", userId, report.getReportId());
            } else {
                logger.warn("⚠️ Could not find user ID for customer in report: {}", report.getReportId());
            }
            
            // Notify restaurant owner
            if (report.getOwner() != null && report.getOwner().getUser() != null) {
                UUID ownerId = report.getOwner().getUser().getId();
                createNotification(
                    ownerId,
                    NotificationType.REVIEW_REPORT_RESOLVED,
                    "Báo cáo review đã được chấp thuận",
                    String.format("Báo cáo của bạn cho review từ khách hàng %s đã được chấp thuận. Review đã bị gỡ.",
                        report.getCustomerNameSnapshot() != null ? report.getCustomerNameSnapshot() : "Không rõ"),
                    "/restaurant-owner/reviews"
                );
                logger.info("✅ Sent review report resolved notification to restaurant owner for report: {}", report.getReportId());
            }
            
        } catch (Exception e) {
            logger.error("❌ Failed to send review report resolved notifications", e);
        }
    }
    
    /**
     * Gửi thông báo khi admin reject review report (từ chối) cho Customer và Restaurant Owner
     */
    public void notifyReviewReportRejected(ReviewReport report) {
        try {
            // Notify customer
            UUID userId = getCustomerUserId(report);
            if (userId != null) {
                createNotification(
                    userId,
                    NotificationType.REVIEW_REPORT_REJECTED,
                    "Review của bạn vẫn được giữ lại",
                    String.format("Review của bạn cho nhà hàng %s vẫn được giữ lại sau khi admin xem xét.",
                        report.getRestaurant().getRestaurantName()),
                    "/reviews/my-reviews"
                );
                logger.info("✅ Sent review report rejected notification to customer (userId: {}) for report: {}", userId, report.getReportId());
            } else {
                logger.warn("⚠️ Could not find user ID for customer in report: {}", report.getReportId());
            }
            
            // Notify restaurant owner
            if (report.getOwner() != null && report.getOwner().getUser() != null) {
                UUID ownerId = report.getOwner().getUser().getId();
                String reason = report.getResolutionMessage() != null && !report.getResolutionMessage().isBlank()
                    ? report.getResolutionMessage()
                    : "Review vẫn phù hợp nên không thể gỡ.";
                
                createNotification(
                    ownerId,
                    NotificationType.REVIEW_REPORT_REJECTED,
                    "Báo cáo review bị từ chối",
                    String.format("Báo cáo của bạn cho review từ khách hàng %s đã bị từ chối. %s",
                        report.getCustomerNameSnapshot() != null ? report.getCustomerNameSnapshot() : "Không rõ",
                        reason),
                    "/restaurant-owner/reviews"
                );
                logger.info("✅ Sent review report rejected notification to restaurant owner for report: {}", report.getReportId());
            }
            
        } catch (Exception e) {
            logger.error("❌ Failed to send review report rejected notifications", e);
        }
    }
    
    /**
     * Lấy User ID từ Customer ID trong ReviewReport
     * Thử lấy từ review entity trước, nếu không có thì query từ customerIdSnapshot
     */
    private UUID getCustomerUserId(ReviewReport report) {
        try {
            // Try to get from review entity first (if review still exists)
            if (report.getReview() != null && report.getReview().getCustomer() != null) {
                Customer customer = report.getReview().getCustomer();
                if (customer.getUser() != null) {
                    logger.debug("✅ Got user ID from review entity for report: {}", report.getReportId());
                    return customer.getUser().getId();
                }
            }
            
            // Fallback: get from customerIdSnapshot
            UUID customerId = report.getCustomerIdSnapshot();
            if (customerId != null) {
                java.util.Optional<Customer> customerOpt = customerRepository.findById(customerId);
                if (customerOpt.isPresent()) {
                    Customer customer = customerOpt.get();
                    if (customer.getUser() != null) {
                        logger.debug("✅ Got user ID from customerIdSnapshot for report: {}", report.getReportId());
                        return customer.getUser().getId();
                    }
                } else {
                    logger.warn("⚠️ Customer not found for customerId: {} in report: {}", customerId, report.getReportId());
                }
            } else {
                logger.warn("⚠️ customerIdSnapshot is null for report: {}", report.getReportId());
            }
            
            return null;
        } catch (Exception e) {
            logger.error("❌ Error getting customer user ID for report: {}", report.getReportId(), e);
            return null;
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

