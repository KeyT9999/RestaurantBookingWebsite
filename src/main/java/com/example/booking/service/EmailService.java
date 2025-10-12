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
    public void sendRestaurantApprovalEmail(String toEmail, String ownerName, String restaurantName, 
                                           String approvalReason) {
        try {
            String subject = "🎉 Nhà hàng của bạn đã được duyệt - " + restaurantName;
            
            String message = String.format("""
                Xin chào %s,
                
                Chúc mừng! Nhà hàng "%s" của bạn đã được duyệt thành công! 🎉
                
                ══════════════════════════════════════
                📋 THÔNG TIN DUYỆT
                ══════════════════════════════════════
                • Nhà hàng: %s
                • Trạng thái: ĐÃ DUYỆT ✅
                • Thời gian duyệt: %s
                %s
                
                ══════════════════════════════════════
                🚀 BƯỚC TIẾP THEO
                ══════════════════════════════════════
                1. Đăng nhập vào tài khoản nhà hàng
                2. Hoàn thiện hồ sơ nhà hàng
                3. Thiết lập thông tin bàn và menu
                4. Bắt đầu nhận booking từ khách hàng
                
                🔗 Truy cập Dashboard nhà hàng:
                %s/restaurant/dashboard
                
                📞 Hỗ trợ: Nếu cần hỗ trợ, vui lòng liên hệ:
                • Email: support@bookeat.vn
                • Hotline: +84 868899104
                
                Cảm ơn bạn đã tin tưởng và hợp tác với Book Eat!
                
                Trân trọng,
                Book Eat Team
                """,
                ownerName,
                restaurantName,
                restaurantName,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                approvalReason != null && !approvalReason.trim().isEmpty() 
                    ? "• Lý do: " + approvalReason 
                    : "",
                baseUrl);
            
            sendEmail(toEmail, subject, message);
            logger.info("✅ Restaurant approval email sent to: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("❌ Failed to send restaurant approval email to: {}", toEmail, e);
        }
    }
    
    /**
     * Send restaurant rejection notification to restaurant owner
     */
    public void sendRestaurantRejectionEmail(String toEmail, String ownerName, String restaurantName, 
                                            String rejectionReason) {
        try {
            String subject = "❌ Yêu cầu đăng ký nhà hàng bị từ chối - " + restaurantName;
            
            String message = String.format("""
                Xin chào %s,
                
                Chúng tôi rất tiếc phải thông báo rằng yêu cầu đăng ký nhà hàng "%s" của bạn đã bị từ chối.
                
                ══════════════════════════════════════
                📋 THÔNG TIN TỪ CHỐI
                ══════════════════════════════════════
                • Nhà hàng: %s
                • Trạng thái: BỊ TỪ CHỐI ❌
                • Thời gian xử lý: %s
                • Lý do từ chối: %s
                
                ══════════════════════════════════════
                🔄 CÁCH THỨC KHẮC PHỤC
                ══════════════════════════════════════
                1. Xem xét lại lý do từ chối ở trên
                2. Chuẩn bị đầy đủ giấy tờ cần thiết
                3. Gửi lại yêu cầu đăng ký mới
                4. Đảm bảo thông tin chính xác và đầy đủ
                
                📝 YÊU CẦU CHUNG:
                • Giấy phép kinh doanh hợp lệ
                • Thông tin nhà hàng chi tiết và chính xác
                • Ảnh chất lượng cao về không gian nhà hàng
                • Tuân thủ các quy định của hệ thống
                
                🔗 Gửi lại yêu cầu:
                %s/restaurant/register
                
                📞 Hỗ trợ: Nếu cần hỗ trợ, vui lòng liên hệ:
                • Email: support@bookeat.vn
                • Hotline: +84 868899104
                
                Chúng tôi luôn sẵn sàng hỗ trợ bạn!
                
                Trân trọng,
                Book Eat Team
                """,
                ownerName,
                restaurantName,
                restaurantName,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                rejectionReason != null ? rejectionReason : "Không đáp ứng yêu cầu của hệ thống",
                baseUrl);
            
            sendEmail(toEmail, subject, message);
            logger.info("✅ Restaurant rejection email sent to: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("❌ Failed to send restaurant rejection email to: {}", toEmail, e);
        }
    }
    
    /**
     * Send restaurant suspension notification to restaurant owner
     */
    public void sendRestaurantSuspensionEmail(String toEmail, String ownerName, String restaurantName, 
                                             String suspensionReason) {
        try {
            String subject = "⚠️ Nhà hàng tạm dừng hoạt động - " + restaurantName;
            
            String message = String.format("""
                Xin chào %s,
                
                Chúng tôi thông báo rằng nhà hàng "%s" của bạn đã bị tạm dừng hoạt động.
                
                ══════════════════════════════════════
                📋 THÔNG TIN TẠM DỪNG
                ══════════════════════════════════════
                • Nhà hàng: %s
                • Trạng thái: TẠM DỪNG ⚠️
                • Thời gian tạm dừng: %s
                • Lý do: %s
                
                ══════════════════════════════════════
                ⚠️ TÁC ĐỘNG
                ══════════════════════════════════════
                • Nhà hàng không thể nhận booking mới
                • Các booking hiện tại vẫn được giữ nguyên
                • Không thể cập nhật thông tin nhà hàng
                
                ══════════════════════════════════════
                🔄 CÁCH THỨC KHẮC PHỤC
                ══════════════════════════════════════
                1. Xem xét và khắc phục lý do tạm dừng
                2. Liên hệ với Book Eat để thảo luận
                3. Gửi yêu cầu kích hoạt lại khi sẵn sàng
                
                📞 Hỗ trợ khẩn cấp:
                • Email: support@bookeat.vn
                • Hotline: +84 868899104
                
                Chúng tôi sẵn sàng hỗ trợ bạn khắc phục vấn đề!
                
                Trân trọng,
                Book Eat Team
                """,
                ownerName,
                restaurantName,
                restaurantName,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                suspensionReason != null ? suspensionReason : "Vi phạm quy định hệ thống");
            
            sendEmail(toEmail, subject, message);
            logger.info("✅ Restaurant suspension email sent to: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("❌ Failed to send restaurant suspension email to: {}", toEmail, e);
        }
    }
    
    /**
     * Send new restaurant registration notification to admin
     */
    public void sendNewRestaurantRegistrationToAdmin(String adminEmail, String restaurantName, 
                                                    String ownerName, String ownerEmail) {
        try {
            String subject = "🔔 Có nhà hàng mới đăng ký - " + restaurantName;
            
            String message = String.format("""
                Xin chào Admin,
                
                Có nhà hàng mới đã đăng ký và cần được duyệt! 🔔
                
                ══════════════════════════════════════
                📋 THÔNG TIN NHÀ HÀNG
                ══════════════════════════════════════
                • Tên nhà hàng: %s
                • Chủ sở hữu: %s
                • Email chủ sở hữu: %s
                • Thời gian đăng ký: %s
                • Trạng thái: CHỜ DUYỆT ⏳
                
                ══════════════════════════════════════
                🎯 HÀNH ĐỘNG CẦN THIẾT
                ══════════════════════════════════════
                1. Truy cập trang quản lý nhà hàng
                2. Xem xét thông tin chi tiết
                3. Kiểm tra giấy tờ đính kèm
                4. Duyệt hoặc từ chối yêu cầu
                
                🔗 Truy cập ngay:
                %s/admin/restaurant/requests
                
                📊 Thống kê hiện tại:
                • Tổng yêu cầu chờ duyệt: [Sẽ hiển thị trên trang]
                
                Trân trọng,
                Book Eat System
                """,
                restaurantName,
                ownerName,
                ownerEmail,
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                baseUrl);
            
            sendEmail(adminEmail, subject, message);
            logger.info("✅ New restaurant registration notification sent to admin: {}", adminEmail);
            
        } catch (Exception e) {
            logger.error("❌ Failed to send new restaurant registration notification to admin: {}", adminEmail, e);
        }
    }
} 