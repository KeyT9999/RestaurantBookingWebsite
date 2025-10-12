package com.example.booking.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Value("${app.base-url:http://localhost:8081}")
    private String baseUrl;
    
    @Value("${spring.mail.username:noreply@bookeat.com}")
    private String fromEmail;
    
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;
    
    public void sendVerificationEmail(String toEmail, String token) {
        try {
            String subject = "Xác thực tài khoản - Book Eat";
            String verificationUrl = baseUrl + "/auth/verify-email?token=" + token;
            
            String message = String.format("""
                Chào bạn,
                
                Cảm ơn bạn đã đăng ký tài khoản tại Book Eat!
                
                Vui lòng click vào link dưới đây để xác thực email của bạn:
                %s
                
                Link này sẽ hết hạn sau 24 giờ.
                
                Nếu bạn không đăng ký tài khoản này, vui lòng bỏ qua email này.
                
                Trân trọng,
                Book Eat Team
                """, verificationUrl);
            
            sendEmail(toEmail, subject, message);
            logger.info("✅ Verification email sent to: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("❌ Failed to send verification email to: {}", toEmail, e);
        }
    }
    
    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            String subject = "Đặt lại mật khẩu - Book Eat";
            String resetUrl = baseUrl + "/auth/reset-password?token=" + token;
            
            String message = String.format("""
                Chào bạn,
                
                Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.
                
                Vui lòng click vào link dưới đây để đặt lại mật khẩu:
                %s
                
                Link này sẽ hết hạn sau 1 giờ.
                
                Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.
                
                Trân trọng,
                Book Eat Team
                """, resetUrl);
            
            sendEmail(toEmail, subject, message);
            logger.info("✅ Password reset email sent to: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("❌ Failed to send password reset email to: {}", toEmail, e);
        }
    }
    
    /**
     * Send payment success email to customer
     */
    public void sendPaymentSuccessEmail(String toEmail, String customerName, 
                                       Integer bookingId, String restaurantName,
                                       String bookingTime, Integer numberOfGuests,
                                       java.math.BigDecimal paidAmount, 
                                       java.math.BigDecimal remainingAmount,
                                       String paymentMethod) {
        try {
            String subject = "✅ Thanh toán thành công - Booking #" + bookingId;
            
            String message = String.format("""
                Xin chào %s,
                
                Thanh toán của bạn đã được xử lý thành công! ✅
                
                ══════════════════════════════════════
                📅 THÔNG TIN ĐẶT BÀN
                ══════════════════════════════════════
                • Mã booking: #%d
                • Nhà hàng: %s
                • Thời gian: %s
                • Số khách: %d người
                • Trạng thái: ĐÃ XÁC NHẬN ✅
                
                ══════════════════════════════════════
                💰 THÔNG TIN THANH TOÁN
                ══════════════════════════════════════
                • Phương thức: %s
                • Đã thanh toán: %s VNĐ
                %s
                
                💡 LƯU Ý QUAN TRỌNG:
                • Vui lòng đến đúng giờ đã đặt
                • Nhà hàng sẽ liên hệ trước 24h nếu cần
                • Mang theo mã booking khi đến nhà hàng
                
                🔗 Xem chi tiết booking:
                %s/booking/my
                
                Cảm ơn bạn đã sử dụng Book Eat!
                
                Trân trọng,
                Book Eat Team
                """, 
                customerName,
                bookingId,
                restaurantName,
                bookingTime,
                numberOfGuests,
                paymentMethod,
                String.format("%,d", paidAmount.intValue()),
                remainingAmount != null && remainingAmount.compareTo(java.math.BigDecimal.ZERO) > 0 
                    ? "• Số tiền còn lại: " + String.format("%,d", remainingAmount.intValue()) + " VNĐ\n  (Thanh toán tại nhà hàng)" 
                    : "• Trạng thái: ĐÃ THANH TOÁN TOÀN BỘ ✅",
                baseUrl);
            
            sendEmail(toEmail, subject, message);
            logger.info("✅ Payment success email sent to: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("❌ Failed to send payment success email to: {}", toEmail, e);
        }
    }
    
    /**
     * Send payment notification email to restaurant owner
     */
    public void sendPaymentNotificationToRestaurant(String toEmail, String restaurantName,
                                                   Integer bookingId, String customerName,
                                                   String bookingTime, Integer numberOfGuests,
                                                   java.math.BigDecimal paidAmount,
                                                   String paymentMethod) {
        try {
            String subject = "🔔 Booking mới đã thanh toán #" + bookingId;
            
            String message = String.format("""
                Xin chào %s,
                
                Có booking mới đã được thanh toán! 🎉
                
                ══════════════════════════════════════
                📅 THÔNG TIN BOOKING
                ══════════════════════════════════════
                • Mã booking: #%d
                • Khách hàng: %s
                • Thời gian: %s
                • Số khách: %d người
                
                ══════════════════════════════════════
                💰 THÔNG TIN THANH TOÁN
                ══════════════════════════════════════
                • Phương thức: %s
                • Số tiền đã nhận: %s VNĐ
                • Trạng thái: ĐÃ XÁC NHẬN ✅
                
                💡 HÀNH ĐỘNG CẦN THIẾT:
                • Chuẩn bị bàn cho thời gian đã đặt
                • Liên hệ khách hàng nếu cần xác nhận
                
                🔗 Xem chi tiết:
                %s/restaurant/bookings/%d
                
                Trân trọng,
                Book Eat System
                """,
                restaurantName,
                bookingId,
                customerName,
                bookingTime,
                numberOfGuests,
                paymentMethod,
                String.format("%,d", paidAmount.intValue()),
                baseUrl,
                bookingId);
            
            sendEmail(toEmail, subject, message);
            logger.info("✅ Payment notification sent to restaurant: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("❌ Failed to send payment notification to restaurant: {}", toEmail, e);
        }
    }
    
    public void sendEmail(String toEmail, String subject, String message) {
        logger.info("🔍 Mail debug -> profile: {}, baseUrl: {}, mailSender? {}", activeProfile, baseUrl, (mailSender != null));

        // Check if we have proper email configuration
        boolean hasValidEmailConfig = mailSender != null && 
                                     fromEmail != null && 
                                     !fromEmail.contains("your-email@gmail.com") &&
                                     !fromEmail.contains("noreply@bookeat.com") &&
                                     !fromEmail.isEmpty();

        // Check if we're in localhost development
        boolean isLocalhost = baseUrl.contains("localhost") || baseUrl.contains("127.0.0.1");

        if (isLocalhost && hasValidEmailConfig) {
            // Localhost with valid email config - try to send real email
            try {
                SimpleMailMessage mailMessage = new SimpleMailMessage();
                mailMessage.setFrom(fromEmail);
                mailMessage.setTo(toEmail);
                mailMessage.setSubject(subject);
                mailMessage.setText(message);

                logger.info("📤 [LOCALHOST REAL EMAIL] Sending... from={}, to={}", fromEmail, toEmail);
                mailSender.send(mailMessage);
                logger.info("✅ [LOCALHOST REAL EMAIL] Sent successfully to: {}", toEmail);
                return;
            } catch (Exception e) {
                logger.warn("⚠️ [LOCALHOST EMAIL FAILED] Falling back to mock mode: {}", e.getMessage());
                // Fall through to mock mode
            }
        }

        // Mock mode (localhost without config, or production fallback)
        logger.info("📧 [MOCK EMAIL] To: {}", toEmail);
        logger.info("📧 [MOCK EMAIL] Subject: {}", subject);
        logger.info("📧 [MOCK EMAIL] Message:\n{}", message);
        logger.info("�� [VERIFICATION LINK] Check the message above for the verification URL");
        
        if (isLocalhost) {
            logger.info("💡 [TIP] To enable real email on localhost, set MAIL_USERNAME and MAIL_PASSWORD environment variables");
        }
    }
    
    /**
     * Send restaurant approval notification to restaurant owner
     */
    public void sendRestaurantApprovalEmail(String toEmail, String restaurantName, String subject, String content) {
        try {
            sendEmail(toEmail, subject, content);
            logger.info("✅ Restaurant approval email sent to: {} for restaurant: {}", toEmail, restaurantName);
        } catch (Exception e) {
            logger.error("❌ Failed to send restaurant approval email to: {} for restaurant: {}", toEmail, restaurantName, e);
            throw new RuntimeException("Failed to send approval email", e);
        }
    }
    
    /**
     * Send restaurant rejection notification to restaurant owner
     */
    public void sendRestaurantRejectionEmail(String toEmail, String restaurantName, String subject, String content) {
        try {
            sendEmail(toEmail, subject, content);
            logger.info("✅ Restaurant rejection email sent to: {} for restaurant: {}", toEmail, restaurantName);
        } catch (Exception e) {
            logger.error("❌ Failed to send restaurant rejection email to: {} for restaurant: {}", toEmail, restaurantName, e);
            throw new RuntimeException("Failed to send rejection email", e);
        }
    }
    
    /**
     * Send restaurant suspension notification to restaurant owner
     */
    public void sendRestaurantSuspensionEmail(String toEmail, String restaurantName, String subject, String content) {
        try {
            sendEmail(toEmail, subject, content);
            logger.info("✅ Restaurant suspension email sent to: {} for restaurant: {}", toEmail, restaurantName);
        } catch (Exception e) {
            logger.error("❌ Failed to send restaurant suspension email to: {} for restaurant: {}", toEmail, restaurantName, e);
            throw new RuntimeException("Failed to send suspension email", e);
        }
    }
    
    /**
     * Send restaurant activation notification to restaurant owner
     */
    public void sendRestaurantActivationEmail(String toEmail, String restaurantName, String subject, String content) {
        try {
            sendEmail(toEmail, subject, content);
            logger.info("✅ Restaurant activation email sent to: {} for restaurant: {}", toEmail, restaurantName);
        } catch (Exception e) {
            logger.error("❌ Failed to send restaurant activation email to: {} for restaurant: {}", toEmail, restaurantName, e);
            throw new RuntimeException("Failed to send activation email", e);
        }
    }
} 