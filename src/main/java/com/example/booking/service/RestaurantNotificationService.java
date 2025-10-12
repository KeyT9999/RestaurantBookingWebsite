package com.example.booking.service;

import com.example.booking.domain.Notification;
import com.example.booking.domain.NotificationType;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.repository.NotificationRepository;
import com.example.booking.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service xử lý thông báo cho Restaurant Approval Workflow
 */
@Service
@Transactional
public class RestaurantNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantNotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Gửi thông báo đăng ký nhà hàng mới cho Admin
     */
    public void notifyNewRestaurantRegistration(RestaurantProfile restaurant) {
        try {
            // Lấy danh sách admin
            List<User> admins = userRepository.findByRole(UserRole.ADMIN, null).getContent();
            
            if (admins.isEmpty()) {
                logger.warn("⚠️ No admin users found for restaurant registration notification");
                return;
            }

            // Gửi thông báo cho từng admin
            for (User admin : admins) {
                // Tạo notification trong database
                Notification notification = new Notification();
                notification.setRecipientUserId(admin.getId());
                notification.setType(NotificationType.RESTAURANT_REGISTRATION_SUBMITTED);
                notification.setTitle("Nhà hàng mới đăng ký: " + restaurant.getRestaurantName());
                notification.setContent(String.format(
                    "Nhà hàng \"%s\" đã gửi yêu cầu đăng ký và cần được duyệt.\n" +
                    "Chủ sở hữu: %s\n" +
                    "Địa chỉ: %s\n" +
                    "Loại ẩm thực: %s",
                    restaurant.getRestaurantName(),
                    restaurant.getOwner().getUser().getUsername(),
                    restaurant.getAddress(),
                    restaurant.getCuisineType()
                ));
                notification.setLinkUrl("/admin/restaurant/requests/" + restaurant.getId());
                notification.setPriority(1); // High priority
                notification.setPublishAt(LocalDateTime.now());
                notification.setExpireAt(LocalDateTime.now().plusDays(7)); // Expire after 7 days
                
                notificationRepository.save(notification);
                
                // Gửi email cho admin
                emailService.sendNewRestaurantRegistrationToAdmin(
                    admin.getEmail(),
                    restaurant.getRestaurantName(),
                    restaurant.getOwner().getUser().getUsername(),
                    restaurant.getOwner().getUser().getEmail()
                );
                
                logger.info("✅ New restaurant registration notification sent to admin: {}", admin.getUsername());
            }
            
        } catch (Exception e) {
            logger.error("❌ Failed to send new restaurant registration notification", e);
        }
    }

    /**
     * Gửi thông báo duyệt nhà hàng cho chủ sở hữu
     */
    public void notifyRestaurantApproval(RestaurantProfile restaurant, String approvedBy, String approvalReason) {
        try {
            User owner = restaurant.getOwner().getUser();
            
            // Tạo notification trong database
            Notification notification = new Notification();
            notification.setRecipientUserId(owner.getId());
            notification.setType(NotificationType.RESTAURANT_APPROVED);
            notification.setTitle("🎉 Nhà hàng đã được duyệt: " + restaurant.getRestaurantName());
            notification.setContent(String.format(
                "Chúc mừng! Nhà hàng \"%s\" của bạn đã được duyệt thành công!\n\n" +
                "Người duyệt: %s\n" +
                "Thời gian: %s\n" +
                "%s\n\n" +
                "Bạn có thể bắt đầu thiết lập thông tin nhà hàng và nhận booking từ khách hàng.",
                restaurant.getRestaurantName(),
                approvedBy,
                LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                approvalReason != null && !approvalReason.trim().isEmpty() 
                    ? "Lý do: " + approvalReason 
                    : "Nhà hàng đáp ứng đầy đủ yêu cầu của hệ thống"
            ));
            notification.setLinkUrl("/restaurant/dashboard");
            notification.setPriority(1); // High priority
            notification.setPublishAt(LocalDateTime.now());
            notification.setExpireAt(LocalDateTime.now().plusDays(30)); // Expire after 30 days
            
            notificationRepository.save(notification);
            
            // Gửi email cho chủ nhà hàng
            emailService.sendRestaurantApprovalEmail(
                owner.getEmail(),
                owner.getUsername(),
                restaurant.getRestaurantName(),
                approvalReason
            );
            
            logger.info("✅ Restaurant approval notification sent to owner: {}", owner.getUsername());
            
        } catch (Exception e) {
            logger.error("❌ Failed to send restaurant approval notification", e);
        }
    }

    /**
     * Gửi thông báo từ chối nhà hàng cho chủ sở hữu
     */
    public void notifyRestaurantRejection(RestaurantProfile restaurant, String rejectedBy, String rejectionReason) {
        try {
            User owner = restaurant.getOwner().getUser();
            
            // Tạo notification trong database
            Notification notification = new Notification();
            notification.setRecipientUserId(owner.getId());
            notification.setType(NotificationType.RESTAURANT_REJECTED);
            notification.setTitle("❌ Yêu cầu đăng ký bị từ chối: " + restaurant.getRestaurantName());
            notification.setContent(String.format(
                "Rất tiếc, yêu cầu đăng ký nhà hàng \"%s\" của bạn đã bị từ chối.\n\n" +
                "Người xử lý: %s\n" +
                "Thời gian: %s\n" +
                "Lý do từ chối: %s\n\n" +
                "Vui lòng xem xét lại và gửi yêu cầu mới với thông tin đầy đủ hơn.",
                restaurant.getRestaurantName(),
                rejectedBy,
                LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                rejectionReason != null ? rejectionReason : "Không đáp ứng yêu cầu của hệ thống"
            ));
            notification.setLinkUrl("/restaurant/register");
            notification.setPriority(2); // Medium priority
            notification.setPublishAt(LocalDateTime.now());
            notification.setExpireAt(LocalDateTime.now().plusDays(30)); // Expire after 30 days
            
            notificationRepository.save(notification);
            
            // Gửi email cho chủ nhà hàng
            emailService.sendRestaurantRejectionEmail(
                owner.getEmail(),
                owner.getUsername(),
                restaurant.getRestaurantName(),
                rejectionReason
            );
            
            logger.info("✅ Restaurant rejection notification sent to owner: {}", owner.getUsername());
            
        } catch (Exception e) {
            logger.error("❌ Failed to send restaurant rejection notification", e);
        }
    }

    /**
     * Gửi thông báo tạm dừng nhà hàng cho chủ sở hữu
     */
    public void notifyRestaurantSuspension(RestaurantProfile restaurant, String suspendedBy, String suspensionReason) {
        try {
            User owner = restaurant.getOwner().getUser();
            
            // Tạo notification trong database
            Notification notification = new Notification();
            notification.setRecipientUserId(owner.getId());
            notification.setType(NotificationType.RESTAURANT_SUSPENDED);
            notification.setTitle("⚠️ Nhà hàng tạm dừng hoạt động: " + restaurant.getRestaurantName());
            notification.setContent(String.format(
                "Nhà hàng \"%s\" của bạn đã bị tạm dừng hoạt động.\n\n" +
                "Người xử lý: %s\n" +
                "Thời gian: %s\n" +
                "Lý do: %s\n\n" +
                "Trong thời gian tạm dừng:\n" +
                "• Không thể nhận booking mới\n" +
                "• Các booking hiện tại vẫn được giữ nguyên\n" +
                "• Không thể cập nhật thông tin nhà hàng\n\n" +
                "Vui lòng liên hệ để được hỗ trợ khắc phục.",
                restaurant.getRestaurantName(),
                suspendedBy,
                LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                suspensionReason != null ? suspensionReason : "Vi phạm quy định hệ thống"
            ));
            notification.setLinkUrl("/contact");
            notification.setPriority(1); // High priority
            notification.setPublishAt(LocalDateTime.now());
            notification.setExpireAt(LocalDateTime.now().plusDays(30)); // Expire after 30 days
            
            notificationRepository.save(notification);
            
            // Gửi email cho chủ nhà hàng
            emailService.sendRestaurantSuspensionEmail(
                owner.getEmail(),
                owner.getUsername(),
                restaurant.getRestaurantName(),
                suspensionReason
            );
            
            logger.info("✅ Restaurant suspension notification sent to owner: {}", owner.getUsername());
            
        } catch (Exception e) {
            logger.error("❌ Failed to send restaurant suspension notification", e);
        }
    }

    /**
     * Gửi thông báo kích hoạt lại nhà hàng cho chủ sở hữu
     */
    public void notifyRestaurantActivation(RestaurantProfile restaurant, String activatedBy, String activationReason) {
        try {
            User owner = restaurant.getOwner().getUser();
            
            // Tạo notification trong database
            Notification notification = new Notification();
            notification.setRecipientUserId(owner.getId());
            notification.setType(NotificationType.RESTAURANT_ACTIVATED);
            notification.setTitle("✅ Nhà hàng được kích hoạt: " + restaurant.getRestaurantName());
            notification.setContent(String.format(
                "Chúc mừng! Nhà hàng \"%s\" của bạn đã được kích hoạt lại.\n\n" +
                "Người xử lý: %s\n" +
                "Thời gian: %s\n" +
                "%s\n\n" +
                "Bạn có thể tiếp tục hoạt động và nhận booking từ khách hàng.",
                restaurant.getRestaurantName(),
                activatedBy,
                LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                activationReason != null && !activationReason.trim().isEmpty() 
                    ? "Lý do: " + activationReason 
                    : "Đã khắc phục các vấn đề được yêu cầu"
            ));
            notification.setLinkUrl("/restaurant/dashboard");
            notification.setPriority(1); // High priority
            notification.setPublishAt(LocalDateTime.now());
            notification.setExpireAt(LocalDateTime.now().plusDays(30)); // Expire after 30 days
            
            notificationRepository.save(notification);
            
            // Gửi email cho chủ nhà hàng
            emailService.sendRestaurantApprovalEmail(
                owner.getEmail(),
                owner.getUsername(),
                restaurant.getRestaurantName(),
                "Nhà hàng đã được kích hoạt lại: " + (activationReason != null ? activationReason : "Đã khắc phục các vấn đề")
            );
            
            logger.info("✅ Restaurant activation notification sent to owner: {}", owner.getUsername());
            
        } catch (Exception e) {
            logger.error("❌ Failed to send restaurant activation notification", e);
        }
    }
}
