package com.example.booking.service;

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
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.repository.NotificationRepository;
import com.example.booking.repository.UserRepository;

/**
 * Service for Restaurant Approval Notifications
 */
@Service
@Transactional
public class RestaurantNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantNotificationService.class);
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Notify restaurant approval (legacy method name for compatibility)
     */
    public void notifyRestaurantApproval(RestaurantProfile restaurant, String approvedBy, String approvalReason) {
        sendApprovalNotification(restaurant);
    }
    
    /**
     * Notify restaurant rejection (legacy method name for compatibility)
     */
    public void notifyRestaurantRejection(RestaurantProfile restaurant, String rejectedBy, String rejectionReason) {
        sendRejectionNotification(restaurant, rejectionReason);
    }
    
    /**
     * Notify restaurant suspension (legacy method name for compatibility)
     */
    public void notifyRestaurantSuspension(RestaurantProfile restaurant, String suspendedBy, String suspensionReason) {
        sendSuspensionNotification(restaurant, suspensionReason);
    }
    
    /**
     * Gửi thông báo khi nhà hàng được duyệt
     */
    public void sendApprovalNotification(RestaurantProfile restaurant) {
        try {
            logger.info("Sending approval notification for restaurant: {}", restaurant.getRestaurantName());
            
            // Lấy thông tin owner
            User owner = restaurant.getOwner().getUser();
            if (owner == null || owner.getEmail() == null) {
                logger.warn("Cannot send notification - owner email not found for restaurant: {}", 
                    restaurant.getRestaurantId());
                return;
            }

            // Tạo email content
            String subject = "🎉 Nhà hàng của bạn đã được duyệt - BookEat";
            String emailContent = buildApprovalEmailContent(restaurant);
            
            // Gửi email
            emailService.sendRestaurantApprovalEmail(
                owner.getEmail(), 
                    restaurant.getRestaurantName(),
                subject, 
                emailContent
            );
            
            // Tạo in-app notification
            createInAppNotification(
                owner.getId(),
                NotificationType.RESTAURANT_APPROVED,
                "Nhà hàng '" + restaurant.getRestaurantName() + "' đã được duyệt thành công!",
                "Bạn có thể bắt đầu setup menu và bàn để nhận đặt bàn từ khách hàng.",
                "/restaurant-owner/profile"
            );
            
            logger.info("✅ Approval notification sent successfully for restaurant: {}", restaurant.getRestaurantName());
            
        } catch (Exception e) {
            logger.error("❌ Failed to send approval notification for restaurant: {}", 
                restaurant.getRestaurantName(), e);
        }
    }

    /**
     * Gửi thông báo khi nhà hàng bị từ chối
     */
    public void sendRejectionNotification(RestaurantProfile restaurant, String rejectionReason) {
        try {
            logger.info("Sending rejection notification for restaurant: {}", restaurant.getRestaurantName());
            
            // Lấy thông tin owner
            User owner = restaurant.getOwner().getUser();
            if (owner == null || owner.getEmail() == null) {
                logger.warn("Cannot send notification - owner email not found for restaurant: {}", 
                    restaurant.getRestaurantId());
                return;
            }
            
            // Tạo email content
            String subject = "❌ Yêu cầu đăng ký nhà hàng - BookEat";
            String emailContent = buildRejectionEmailContent(restaurant, rejectionReason);
            
            // Gửi email
            emailService.sendRestaurantRejectionEmail(
                owner.getEmail(),
                restaurant.getRestaurantName(),
                subject, 
                emailContent
            );
            
            // Tạo in-app notification
            createInAppNotification(
                owner.getId(),
                NotificationType.RESTAURANT_REJECTED,
                "Nhà hàng '" + restaurant.getRestaurantName() + "' chưa được duyệt",
                "Lý do: " + (rejectionReason != null ? rejectionReason : "Không đáp ứng yêu cầu"),
                "/restaurant-owner/register"
            );
            
            logger.info("✅ Rejection notification sent successfully for restaurant: {}", restaurant.getRestaurantName());
            
        } catch (Exception e) {
            logger.error("❌ Failed to send rejection notification for restaurant: {}", 
                restaurant.getRestaurantName(), e);
        }
    }

    /**
     * Gửi thông báo khi nhà hàng bị tạm dừng
     */
    public void sendSuspensionNotification(RestaurantProfile restaurant, String suspensionReason) {
        try {
            logger.info("Sending suspension notification for restaurant: {}", restaurant.getRestaurantName());
            
            // Lấy thông tin owner
            User owner = restaurant.getOwner().getUser();
            if (owner == null || owner.getEmail() == null) {
                logger.warn("Cannot send notification - owner email not found for restaurant: {}", 
                    restaurant.getRestaurantId());
                return;
            }
            
            // Tạo email content
            String subject = "⚠️ Nhà hàng tạm dừng hoạt động - BookEat";
            String emailContent = buildSuspensionEmailContent(restaurant, suspensionReason);
            
            // Gửi email
            emailService.sendRestaurantSuspensionEmail(
                owner.getEmail(),
                restaurant.getRestaurantName(),
                subject, 
                emailContent
            );
            
            // Tạo in-app notification
            createInAppNotification(
                owner.getId(),
                NotificationType.RESTAURANT_SUSPENDED,
                "Nhà hàng '" + restaurant.getRestaurantName() + "' đã bị tạm dừng",
                "Lý do: " + (suspensionReason != null ? suspensionReason : "Vi phạm quy định"),
                "/restaurant-owner/profile"
            );
            
            logger.info("✅ Suspension notification sent successfully for restaurant: {}", restaurant.getRestaurantName());
            
        } catch (Exception e) {
            logger.error("❌ Failed to send suspension notification for restaurant: {}", 
                restaurant.getRestaurantName(), e);
        }
    }

    /**
     * Gửi thông báo khi nhà hàng được kích hoạt lại
     */
    public void sendActivationNotification(RestaurantProfile restaurant, String activationReason) {
        try {
            logger.info("Sending activation notification for restaurant: {}", restaurant.getRestaurantName());
            
            // Lấy thông tin owner
            User owner = restaurant.getOwner().getUser();
            if (owner == null || owner.getEmail() == null) {
                logger.warn("Cannot send notification - owner email not found for restaurant: {}", 
                    restaurant.getRestaurantId());
                return;
            }
            
            // Tạo email content
            String subject = "✅ Nhà hàng được kích hoạt lại - BookEat";
            String emailContent = buildActivationEmailContent(restaurant, activationReason);
            
            // Gửi email
            emailService.sendRestaurantActivationEmail(
                owner.getEmail(),
                restaurant.getRestaurantName(),
                subject, 
                emailContent
            );
            
            // Tạo in-app notification
            createInAppNotification(
                owner.getId(),
                NotificationType.RESTAURANT_ACTIVATED,
                "Nhà hàng '" + restaurant.getRestaurantName() + "' đã được kích hoạt lại",
                "Bạn có thể tiếp tục nhận đặt bàn từ khách hàng.",
                "/restaurant-owner/profile"
            );
            
            logger.info("✅ Activation notification sent successfully for restaurant: {}", restaurant.getRestaurantName());
            
        } catch (Exception e) {
            logger.error("❌ Failed to send activation notification for restaurant: {}", 
                restaurant.getRestaurantName(), e);
        }
    }

    /**
     * Gửi thông báo cho admin khi có yêu cầu đăng ký mới
     */
    public void notifyAdminNewRegistration(RestaurantProfile restaurant) {
        try {
            logger.info("Notifying admin about new restaurant registration: {}", restaurant.getRestaurantName());
            
            // Tìm admin users
            List<User> adminUsers = userRepository.findByRoleAndActiveTrue(UserRole.admin);
            
            for (User admin : adminUsers) {
                // Tạo in-app notification cho admin
                createInAppNotification(
                    admin.getId(),
                    NotificationType.RESTAURANT_REGISTRATION_SUBMITTED,
                    "Yêu cầu đăng ký nhà hàng mới: " + restaurant.getRestaurantName(),
                    "Có nhà hàng mới cần được duyệt. Vui lòng kiểm tra thông tin và xử lý.",
                    "/admin/restaurant/requests/" + restaurant.getRestaurantId()
                );
            }
            
            logger.info("✅ Admin notification sent for new restaurant registration: {}", restaurant.getRestaurantName());
            
        } catch (Exception e) {
            logger.error("❌ Failed to notify admin about new restaurant registration: {}", 
                restaurant.getRestaurantName(), e);
        }
    }
    
    // ============= PRIVATE HELPER METHODS =============
    
    private void createInAppNotification(UUID userId, NotificationType type, String title, String content, String actionUrl) {
        try {
            Notification notification = new Notification();
            notification.setRecipientUserId(userId);
            notification.setType(type);
            notification.setTitle(title);
            notification.setContent(content);
            // notification.setActionUrl(actionUrl); // Method not available in current Notification entity
            notification.setStatus(NotificationStatus.SENT);
            notification.setPublishAt(LocalDateTime.now());
            notification.setExpireAt(LocalDateTime.now().plusDays(30)); // 30 ngày
            
            notificationRepository.save(notification);
            logger.debug("Created in-app notification for user: {} - type: {}", userId, type);
            
        } catch (Exception e) {
            logger.error("Failed to create in-app notification for user: {} - type: {}", userId, type, e);
        }
    }
    
    private String buildApprovalEmailContent(RestaurantProfile restaurant) {
        StringBuilder content = new StringBuilder();
        content.append("<html><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>");
        content.append("<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>");
        
        content.append("<div style='text-align: center; margin-bottom: 30px;'>");
        content.append("<h1 style='color: #28a745; margin-bottom: 10px;'>🎉 Chúc mừng!</h1>");
        content.append("<h2 style='color: #333; margin-bottom: 20px;'>Nhà hàng của bạn đã được duyệt</h2>");
        content.append("</div>");
        
        content.append("<div style='background: #f8f9fa; padding: 20px; border-radius: 8px; margin-bottom: 20px;'>");
        content.append("<h3 style='color: #333; margin-bottom: 15px;'>📋 Thông tin nhà hàng:</h3>");
        content.append("<p><strong>Tên nhà hàng:</strong> ").append(restaurant.getRestaurantName()).append("</p>");
        content.append("<p><strong>Địa chỉ:</strong> ").append(restaurant.getAddress()).append("</p>");
        content.append("<p><strong>Loại ẩm thực:</strong> ").append(restaurant.getCuisineType()).append("</p>");
        content.append("<p><strong>Thời gian duyệt:</strong> ").append(LocalDateTime.now().toString()).append("</p>");
        content.append("</div>");
        
        content.append("<div style='background: #e7f3ff; padding: 20px; border-radius: 8px; margin-bottom: 20px;'>");
        content.append("<h3 style='color: #0066cc; margin-bottom: 15px;'>🚀 Bước tiếp theo:</h3>");
        content.append("<ul style='color: #333;'>");
        content.append("<li>Đăng nhập vào tài khoản nhà hàng</li>");
        content.append("<li>Hoàn thiện thông tin hồ sơ nhà hàng</li>");
        content.append("<li>Thiết lập menu và giá cả</li>");
        content.append("<li>Thiết lập bàn và khu vực</li>");
        content.append("<li>Bắt đầu nhận đặt bàn từ khách hàng</li>");
        content.append("</ul>");
        content.append("</div>");
        
        content.append("<div style='text-align: center; margin: 30px 0;'>");
        content.append("<a href='http://localhost:8080/restaurant-owner/profile' ");
        content.append("style='background: #28a745; color: white; padding: 12px 30px; text-decoration: none; ");
        content.append("border-radius: 5px; font-weight: bold;'>Truy cập Dashboard</a>");
        content.append("</div>");
        
        content.append("<div style='text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee;'>");
        content.append("<p style='color: #666; font-size: 14px;'>");
        content.append("Cảm ơn bạn đã chọn BookEat làm đối tác!<br>");
        content.append("Chúc bạn kinh doanh thành công! 🍽️");
        content.append("</p>");
        content.append("</div>");
        
        content.append("</div></body></html>");
        return content.toString();
    }
    
    private String buildRejectionEmailContent(RestaurantProfile restaurant, String rejectionReason) {
        StringBuilder content = new StringBuilder();
        content.append("<html><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>");
        content.append("<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>");
        
        content.append("<div style='text-align: center; margin-bottom: 30px;'>");
        content.append("<h1 style='color: #dc3545; margin-bottom: 10px;'>❌ Thông báo</h1>");
        content.append("<h2 style='color: #333; margin-bottom: 20px;'>Yêu cầu đăng ký nhà hàng</h2>");
        content.append("</div>");
        
        content.append("<div style='background: #f8f9fa; padding: 20px; border-radius: 8px; margin-bottom: 20px;'>");
        content.append("<h3 style='color: #333; margin-bottom: 15px;'>📋 Thông tin nhà hàng:</h3>");
        content.append("<p><strong>Tên nhà hàng:</strong> ").append(restaurant.getRestaurantName()).append("</p>");
        content.append("<p><strong>Địa chỉ:</strong> ").append(restaurant.getAddress()).append("</p>");
        content.append("<p><strong>Loại ẩm thực:</strong> ").append(restaurant.getCuisineType()).append("</p>");
        content.append("</div>");
        
        content.append("<div style='background: #fff3cd; padding: 20px; border-radius: 8px; margin-bottom: 20px;'>");
        content.append("<h3 style='color: #856404; margin-bottom: 15px;'>⚠️ Lý do từ chối:</h3>");
        content.append("<p style='color: #333;'>").append(rejectionReason != null ? rejectionReason : "Không đáp ứng yêu cầu của chúng tôi").append("</p>");
        content.append("</div>");
        
        content.append("<div style='background: #e7f3ff; padding: 20px; border-radius: 8px; margin-bottom: 20px;'>");
        content.append("<h3 style='color: #0066cc; margin-bottom: 15px;'>💡 Hướng dẫn:</h3>");
        content.append("<ul style='color: #333;'>");
        content.append("<li>Vui lòng kiểm tra lại thông tin đã cung cấp</li>");
        content.append("<li>Đảm bảo các giấy tờ pháp lý đầy đủ và hợp lệ</li>");
        content.append("<li>Liên hệ với chúng tôi nếu cần hỗ trợ</li>");
        content.append("<li>Bạn có thể nộp lại đơn đăng ký sau khi khắc phục</li>");
        content.append("</ul>");
        content.append("</div>");
        
        content.append("<div style='text-align: center; margin: 30px 0;'>");
        content.append("<a href='http://localhost:8080/restaurant-owner/register' ");
        content.append("style='background: #007bff; color: white; padding: 12px 30px; text-decoration: none; ");
        content.append("border-radius: 5px; font-weight: bold;'>Đăng ký lại</a>");
        content.append("</div>");
        
        content.append("<div style='text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee;'>");
        content.append("<p style='color: #666; font-size: 14px;'>");
        content.append("Chúng tôi luôn sẵn sàng hỗ trợ bạn!<br>");
        content.append("Liên hệ: support@bookeat.com");
        content.append("</p>");
        content.append("</div>");
        
        content.append("</div></body></html>");
        return content.toString();
    }
    
    private String buildSuspensionEmailContent(RestaurantProfile restaurant, String suspensionReason) {
        StringBuilder content = new StringBuilder();
        content.append("<html><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>");
        content.append("<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>");
        
        content.append("<div style='text-align: center; margin-bottom: 30px;'>");
        content.append("<h1 style='color: #ffc107; margin-bottom: 10px;'>⚠️ Thông báo quan trọng</h1>");
        content.append("<h2 style='color: #333; margin-bottom: 20px;'>Nhà hàng tạm dừng hoạt động</h2>");
        content.append("</div>");
        
        content.append("<div style='background: #f8f9fa; padding: 20px; border-radius: 8px; margin-bottom: 20px;'>");
        content.append("<h3 style='color: #333; margin-bottom: 15px;'>📋 Thông tin nhà hàng:</h3>");
        content.append("<p><strong>Tên nhà hàng:</strong> ").append(restaurant.getRestaurantName()).append("</p>");
        content.append("<p><strong>Địa chỉ:</strong> ").append(restaurant.getAddress()).append("</p>");
        content.append("<p><strong>Thời gian tạm dừng:</strong> ").append(LocalDateTime.now().toString()).append("</p>");
        content.append("</div>");
        
        content.append("<div style='background: #fff3cd; padding: 20px; border-radius: 8px; margin-bottom: 20px;'>");
        content.append("<h3 style='color: #856404; margin-bottom: 15px;'>⚠️ Lý do tạm dừng:</h3>");
        content.append("<p style='color: #333;'>").append(suspensionReason != null ? suspensionReason : "Vi phạm quy định dịch vụ").append("</p>");
        content.append("</div>");
        
        content.append("<div style='background: #e7f3ff; padding: 20px; border-radius: 8px; margin-bottom: 20px;'>");
        content.append("<h3 style='color: #0066cc; margin-bottom: 15px;'>📞 Hành động cần thiết:</h3>");
        content.append("<ul style='color: #333;'>");
        content.append("<li>Liên hệ ngay với bộ phận hỗ trợ</li>");
        content.append("<li>Khắc phục các vấn đề được thông báo</li>");
        content.append("<li>Chờ đánh giá và kích hoạt lại từ admin</li>");
        content.append("<li>Trong thời gian tạm dừng, nhà hàng sẽ không nhận đặt bàn mới</li>");
        content.append("</ul>");
        content.append("</div>");
        
        content.append("<div style='text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee;'>");
        content.append("<p style='color: #666; font-size: 14px;'>");
        content.append("Liên hệ hỗ trợ: support@bookeat.com<br>");
        content.append("Hotline: 1900-xxxx");
        content.append("</p>");
        content.append("</div>");
        
        content.append("</div></body></html>");
        return content.toString();
    }
    
    private String buildActivationEmailContent(RestaurantProfile restaurant, String activationReason) {
        StringBuilder content = new StringBuilder();
        content.append("<html><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>");
        content.append("<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>");
        
        content.append("<div style='text-align: center; margin-bottom: 30px;'>");
        content.append("<h1 style='color: #28a745; margin-bottom: 10px;'>✅ Chào mừng trở lại!</h1>");
        content.append("<h2 style='color: #333; margin-bottom: 20px;'>Nhà hàng đã được kích hoạt lại</h2>");
        content.append("</div>");
        
        content.append("<div style='background: #f8f9fa; padding: 20px; border-radius: 8px; margin-bottom: 20px;'>");
        content.append("<h3 style='color: #333; margin-bottom: 15px;'>📋 Thông tin nhà hàng:</h3>");
        content.append("<p><strong>Tên nhà hàng:</strong> ").append(restaurant.getRestaurantName()).append("</p>");
        content.append("<p><strong>Địa chỉ:</strong> ").append(restaurant.getAddress()).append("</p>");
        content.append("<p><strong>Thời gian kích hoạt:</strong> ").append(LocalDateTime.now().toString()).append("</p>");
        content.append("</div>");
        
        content.append("<div style='background: #d4edda; padding: 20px; border-radius: 8px; margin-bottom: 20px;'>");
        content.append("<h3 style='color: #155724; margin-bottom: 15px;'>✅ Lý do kích hoạt:</h3>");
        content.append("<p style='color: #333;'>").append(activationReason != null ? activationReason : "Đã khắc phục các vấn đề").append("</p>");
        content.append("</div>");
        
        content.append("<div style='background: #e7f3ff; padding: 20px; border-radius: 8px; margin-bottom: 20px;'>");
        content.append("<h3 style='color: #0066cc; margin-bottom: 15px;'>🎉 Bạn có thể:</h3>");
        content.append("<ul style='color: #333;'>");
        content.append("<li>Tiếp tục nhận đặt bàn từ khách hàng</li>");
        content.append("<li>Cập nhật menu và thông tin nhà hàng</li>");
        content.append("<li>Quản lý các đặt bàn hiện tại</li>");
        content.append("<li>Tận hưởng dịch vụ BookEat như bình thường</li>");
        content.append("</ul>");
        content.append("</div>");
        
        content.append("<div style='text-align: center; margin: 30px 0;'>");
        content.append("<a href='http://localhost:8080/restaurant-owner/profile' ");
        content.append("style='background: #28a745; color: white; padding: 12px 30px; text-decoration: none; ");
        content.append("border-radius: 5px; font-weight: bold;'>Truy cập Dashboard</a>");
        content.append("</div>");
        
        content.append("<div style='text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee;'>");
        content.append("<p style='color: #666; font-size: 14px;'>");
        content.append("Cảm ơn bạn đã hợp tác với BookEat!<br>");
        content.append("Chúc bạn kinh doanh thành công! 🍽️");
        content.append("</p>");
        content.append("</div>");
        
        content.append("</div></body></html>");
        return content.toString();
    }
}