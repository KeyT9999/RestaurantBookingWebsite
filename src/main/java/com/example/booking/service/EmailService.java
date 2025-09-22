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
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    @Value("${spring.mail.username:noreply@bookeat.com}")
    private String fromEmail;
    
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;
    
    public void sendVerificationEmail(String toEmail, String token) {
        try {
            String subject = "Xác thực tài khoản - Aurelius Fine Dining";
            String verificationUrl = baseUrl + "/auth/verify-email?token=" + token;
            
            String message = String.format("""
                Chào bạn,
                
                Cảm ơn bạn đã đăng ký tài khoản tại Aurelius Fine Dining!
                
                Vui lòng click vào link dưới đây để xác thực email của bạn:
                %s
                
                Link này sẽ hết hạn sau 24 giờ.
                
                Nếu bạn không đăng ký tài khoản này, vui lòng bỏ qua email này.
                
                Trân trọng,
                Aurelius Fine Dining Team
                """, verificationUrl);
            
            sendEmail(toEmail, subject, message);
            logger.info("✅ Verification email sent to: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("❌ Failed to send verification email to: {}", toEmail, e);
        }
    }
    
    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            String subject = "Đặt lại mật khẩu - Aurelius Fine Dining";
            String resetUrl = baseUrl + "/auth/reset-password?token=" + token;
            
            String message = String.format("""
                Chào bạn,
                
                Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.
                
                Vui lòng click vào link dưới đây để đặt lại mật khẩu:
                %s
                
                Link này sẽ hết hạn sau 1 giờ.
                
                Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.
                
                Trân trọng,
                Aurelius Fine Dining Team
                """, resetUrl);
            
            sendEmail(toEmail, subject, message);
            logger.info("✅ Password reset email sent to: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("❌ Failed to send password reset email to: {}", toEmail, e);
        }
    }
    
    private void sendEmail(String toEmail, String subject, String message) {
        // Force mock mode in dev for testing (no credentials needed)
        if ("dev".equals(activeProfile)) {
            logger.info("📧 [MOCK EMAIL] To: {}", toEmail);
            logger.info("📧 [MOCK EMAIL] Subject: {}", subject);
            logger.info("📧 [MOCK EMAIL] Message:\n{}", message);
            logger.info("🔗 [VERIFICATION LINK] Check the message above for the verification URL");
            return;
        }
        
        // Production mode - use real email service
        if (mailSender == null) {
            logger.error("❌ Mail sender not configured in production mode!");
            throw new RuntimeException("Email service not configured");
        }
        
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromEmail);
            mailMessage.setTo(toEmail);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);
            
            mailSender.send(mailMessage);
            logger.info("✅ Email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("❌ Failed to send email to: {}", toEmail, e);
            throw new RuntimeException("Không thể gửi email", e);
        }
    }
} 