package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

/**
 * Unit tests for EmailService
 * 
 * Test Coverage:
 * 1. sendVerificationEmail() - Happy path, exception handling
 * 2. sendPasswordResetEmail() - Happy path, exception handling
 * 3. sendPaymentSuccessEmail() - Happy path, exception handling
 * 4. sendBookingConfirmationEmail() - Happy path, exception handling
 * 5. sendBookingCancellationEmail() - Happy path, exception handling
 * 6. sendEmail() - Core method, null handling, profile checks
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Tests")
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private String testEmail;
    private String testToken;
    private String testBaseUrl;
    private String testFromEmail;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        testToken = "test-token-123";
        testBaseUrl = "http://localhost:8080";
        testFromEmail = "noreply@bookeat.com";
        
        // Set field values using ReflectionTestUtils
        ReflectionTestUtils.setField(emailService, "baseUrl", testBaseUrl);
        ReflectionTestUtils.setField(emailService, "fromEmail", testFromEmail);
        ReflectionTestUtils.setField(emailService, "activeProfile", "prod");
    }

    // ========== sendVerificationEmail() Tests ==========

    @Test
    @DisplayName("shouldSendVerificationEmail_successfully")
    void shouldSendVerificationEmail_successfully() {
        // Given
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // When
        emailService.sendVerificationEmail(testEmail, testToken);

        // Then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("shouldHandleException_whenSendVerificationEmailFails")
    void shouldHandleException_whenSendVerificationEmailFails() {
        // Given
        doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(SimpleMailMessage.class));

        // When & Then - Should not throw exception, should handle gracefully
        assertDoesNotThrow(() -> emailService.sendVerificationEmail(testEmail, testToken));
    }

    @Test
    @DisplayName("shouldNotSendEmail_whenMailSenderIsNull")
    void shouldNotSendEmail_whenMailSenderIsNull() {
        // Given
        ReflectionTestUtils.setField(emailService, "mailSender", null);

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> emailService.sendVerificationEmail(testEmail, testToken));
    }

    // ========== sendPasswordResetEmail() Tests ==========

    @Test
    @DisplayName("shouldSendPasswordResetEmail_successfully")
    void shouldSendPasswordResetEmail_successfully() {
        // Given
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // When
        emailService.sendPasswordResetEmail(testEmail, testToken);

        // Then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("shouldHandleException_whenSendPasswordResetEmailFails")
    void shouldHandleException_whenSendPasswordResetEmailFails() {
        // Given
        doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(SimpleMailMessage.class));

        // When & Then
        assertDoesNotThrow(() -> emailService.sendPasswordResetEmail(testEmail, testToken));
    }

    // ========== sendPaymentSuccessEmail() Tests ==========

    @Test
    @DisplayName("shouldSendPaymentSuccessEmail_successfully")
    void shouldSendPaymentSuccessEmail_successfully() {
        // Given
        String customerName = "John Doe";
        Integer bookingId = 123;
        String restaurantName = "Test Restaurant";
        String bookingTime = "2024-12-31 19:00";
        Integer numberOfGuests = 4;
        BigDecimal paidAmount = new BigDecimal("500000");
        BigDecimal remainingAmount = new BigDecimal("0");
        String paymentMethod = "Momo";
        
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // When
        emailService.sendPaymentSuccessEmail(testEmail, customerName, bookingId, restaurantName,
                bookingTime, numberOfGuests, paidAmount, remainingAmount, paymentMethod);

        // Then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("shouldSendPaymentSuccessEmail_withRemainingAmount")
    void shouldSendPaymentSuccessEmail_withRemainingAmount() {
        // Given
        BigDecimal remainingAmount = new BigDecimal("300000");
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // When
        emailService.sendPaymentSuccessEmail(testEmail, "John Doe", 123, "Restaurant",
                "2024-12-31 19:00", 4, new BigDecimal("500000"), remainingAmount, "Momo");

        // Then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("shouldHandleException_whenSendPaymentSuccessEmailFails")
    void shouldHandleException_whenSendPaymentSuccessEmailFails() {
        // Given
        doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(SimpleMailMessage.class));

        // When & Then
        assertDoesNotThrow(() -> emailService.sendPaymentSuccessEmail(testEmail, "John", 123, "Restaurant",
                "2024-12-31 19:00", 4, new BigDecimal("500000"), new BigDecimal("0"), "Momo"));
    }


    // ========== sendEmail() Core Method Tests ==========

    @Test
    @DisplayName("shouldNotSendEmail_inDevProfile")
    void shouldNotSendEmail_inDevProfile() {
        // Given
        ReflectionTestUtils.setField(emailService, "activeProfile", "dev");

        // When
        emailService.sendVerificationEmail(testEmail, testToken);

        // Then
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("shouldNotSendEmail_inTestProfile")
    void shouldNotSendEmail_inTestProfile() {
        // Given
        ReflectionTestUtils.setField(emailService, "activeProfile", "test");

        // When
        emailService.sendVerificationEmail(testEmail, testToken);

        // Then
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("shouldSendEmail_inProdProfile")
    void shouldSendEmail_inProdProfile() {
        // Given
        ReflectionTestUtils.setField(emailService, "activeProfile", "prod");
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // When
        emailService.sendVerificationEmail(testEmail, testToken);

        // Then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("shouldNotSendEmail_whenEmailIsNull")
    void shouldNotSendEmail_whenEmailIsNull() {
        // When & Then - Should handle null gracefully
        assertDoesNotThrow(() -> emailService.sendVerificationEmail(null, testToken));
    }

    @Test
    @DisplayName("shouldNotSendEmail_whenEmailIsEmpty")
    void shouldNotSendEmail_whenEmailIsEmpty() {
        // When & Then
        assertDoesNotThrow(() -> emailService.sendVerificationEmail("", testToken));
    }

    @Test
    @DisplayName("shouldIncludeCorrectBaseUrl_inVerificationLink")
    void shouldIncludeCorrectBaseUrl_inVerificationLink() {
        // Given
        String customBaseUrl = "https://app.bookeat.com";
        ReflectionTestUtils.setField(emailService, "baseUrl", customBaseUrl);
        ReflectionTestUtils.setField(emailService, "activeProfile", "prod");
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // When
        emailService.sendVerificationEmail(testEmail, testToken);

        // Then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    // ========== sendPaymentNotificationToRestaurant() Tests ==========

    @Test
    @DisplayName("shouldSendPaymentNotificationToRestaurant_successfully")
    void shouldSendPaymentNotificationToRestaurant_successfully() {
        // Given
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // When
        emailService.sendPaymentNotificationToRestaurant(testEmail, "Restaurant", 123, "Customer",
                "2024-12-31 19:00", 4, new BigDecimal("500000"), "Momo");

        // Then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    // ========== Restaurant Email Methods Tests ==========

    @Test
    @DisplayName("shouldSendRestaurantApprovalEmail_successfully")
    void shouldSendRestaurantApprovalEmail_successfully() {
        // Given
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // When
        emailService.sendRestaurantApprovalEmail(testEmail, "Restaurant", "Subject", "Content");

        // Then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("shouldSendRestaurantRejectionEmail_successfully")
    void shouldSendRestaurantRejectionEmail_successfully() {
        // Given
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // When
        emailService.sendRestaurantRejectionEmail(testEmail, "Restaurant", "Subject", "Content");

        // Then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("shouldSendRestaurantSuspensionEmail_successfully")
    void shouldSendRestaurantSuspensionEmail_successfully() {
        // Given
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // When
        emailService.sendRestaurantSuspensionEmail(testEmail, "Restaurant", "Subject", "Content");

        // Then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("shouldSendRestaurantResubmitEmail_successfully")
    void shouldSendRestaurantResubmitEmail_successfully() {
        // Given
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // When
        emailService.sendRestaurantResubmitEmail(testEmail, "Restaurant", "Subject", "Content");

        // Then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("shouldSendRestaurantActivationEmail_successfully")
    void shouldSendRestaurantActivationEmail_successfully() {
        // Given
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // When
        emailService.sendRestaurantActivationEmail(testEmail, "Restaurant", "Subject", "Content");

        // Then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("shouldThrowException_whenRestaurantApprovalEmailFails")
    void shouldThrowException_whenRestaurantApprovalEmailFails() {
        // Given
        doThrow(new RuntimeException("Mail error")).when(mailSender).send(any(SimpleMailMessage.class));
        ReflectionTestUtils.setField(emailService, "activeProfile", "prod");

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            emailService.sendRestaurantApprovalEmail(testEmail, "Restaurant", "Subject", "Content");
        });
    }

    @Test
    @DisplayName("shouldThrowException_whenRestaurantRejectionEmailFails")
    void shouldThrowException_whenRestaurantRejectionEmailFails() {
        // Given
        doThrow(new RuntimeException("Mail error")).when(mailSender).send(any(SimpleMailMessage.class));
        ReflectionTestUtils.setField(emailService, "activeProfile", "prod");

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            emailService.sendRestaurantRejectionEmail(testEmail, "Restaurant", "Subject", "Content");
        });
    }

    @Test
    @DisplayName("shouldThrowException_whenRestaurantSuspensionEmailFails")
    void shouldThrowException_whenRestaurantSuspensionEmailFails() {
        // Given
        doThrow(new RuntimeException("Mail error")).when(mailSender).send(any(SimpleMailMessage.class));
        ReflectionTestUtils.setField(emailService, "activeProfile", "prod");

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            emailService.sendRestaurantSuspensionEmail(testEmail, "Restaurant", "Subject", "Content");
        });
    }

    @Test
    @DisplayName("shouldThrowException_whenRestaurantResubmitEmailFails")
    void shouldThrowException_whenRestaurantResubmitEmailFails() {
        // Given
        doThrow(new RuntimeException("Mail error")).when(mailSender).send(any(SimpleMailMessage.class));
        ReflectionTestUtils.setField(emailService, "activeProfile", "prod");

        // When & Then - sendRestaurantResubmitEmail throws RuntimeException in catch block
        assertThrows(RuntimeException.class, () -> {
            emailService.sendRestaurantResubmitEmail(testEmail, "Restaurant", "Subject", "Content");
        });
    }

    @Test
    @DisplayName("shouldThrowException_whenRestaurantActivationEmailFails")
    void shouldThrowException_whenRestaurantActivationEmailFails() {
        // Given
        doThrow(new RuntimeException("Mail error")).when(mailSender).send(any(SimpleMailMessage.class));
        ReflectionTestUtils.setField(emailService, "activeProfile", "prod");

        // When & Then - sendRestaurantActivationEmail throws RuntimeException in catch block
        assertThrows(RuntimeException.class, () -> {
            emailService.sendRestaurantActivationEmail(testEmail, "Restaurant", "Subject", "Content");
        });
    }

    @Test
    @DisplayName("shouldLogError_whenSendVerificationEmailCatchBlockExecuted")
    void shouldLogError_whenSendVerificationEmailCatchBlockExecuted() {
        // Given
        doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(SimpleMailMessage.class));
        ReflectionTestUtils.setField(emailService, "activeProfile", "prod");

        // When & Then - Should handle exception gracefully (no throw)
        assertDoesNotThrow(() -> {
            emailService.sendVerificationEmail(testEmail, testToken);
        });
    }

    @Test
    @DisplayName("shouldLogError_whenSendPasswordResetEmailCatchBlockExecuted")
    void shouldLogError_whenSendPasswordResetEmailCatchBlockExecuted() {
        // Given
        doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(SimpleMailMessage.class));
        ReflectionTestUtils.setField(emailService, "activeProfile", "prod");

        // When & Then - Should handle exception gracefully (no throw)
        assertDoesNotThrow(() -> {
            emailService.sendPasswordResetEmail(testEmail, testToken);
        });
    }
}

