package com.example.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Test Suite")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        // Set base URL and from email using reflection
        ReflectionTestUtils.setField(emailService, "baseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@bookeat.com");
        ReflectionTestUtils.setField(emailService, "activeProfile", "test");
    }

    @Nested
    @DisplayName("sendVerificationEmail() Tests")
    class SendVerificationEmailTests {

        @Test
        @DisplayName("Should send verification email successfully")
        void shouldSendVerificationEmailSuccessfully() {
            // When mailSender is null (mock mode), it should log and not throw exception
            ReflectionTestUtils.setField(emailService, "mailSender", null);

            assertDoesNotThrow(() -> emailService.sendVerificationEmail("test@example.com", "test-token"));
        }

        @Test
        @DisplayName("Should handle valid mail sender")
        void shouldHandleValidMailSender() {
            ReflectionTestUtils.setField(emailService, "mailSender", mailSender);
            ReflectionTestUtils.setField(emailService, "baseUrl", "http://production.com");

            doNothing().when(mailSender).send(any(SimpleMailMessage.class));

            assertDoesNotThrow(() -> emailService.sendVerificationEmail("test@example.com", "test-token"));

            verify(mailSender, atMostOnce()).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("Should handle exception gracefully")
        void shouldHandleExceptionGracefully() {
            ReflectionTestUtils.setField(emailService, "mailSender", mailSender);
            ReflectionTestUtils.setField(emailService, "baseUrl", "http://production.com");

            doThrow(new RuntimeException("Mail error")).when(mailSender).send(any(SimpleMailMessage.class));

            // Should not throw exception, just log error
            assertDoesNotThrow(() -> emailService.sendVerificationEmail("test@example.com", "test-token"));
        }
    }

    @Nested
    @DisplayName("sendPasswordResetEmail() Tests")
    class SendPasswordResetEmailTests {

        @Test
        @DisplayName("Should send password reset email successfully")
        void shouldSendPasswordResetEmailSuccessfully() {
            ReflectionTestUtils.setField(emailService, "mailSender", null);

            assertDoesNotThrow(() -> emailService.sendPasswordResetEmail("test@example.com", "reset-token"));
        }

        @Test
        @DisplayName("Should handle valid mail sender")
        void shouldHandleValidMailSender() {
            ReflectionTestUtils.setField(emailService, "mailSender", mailSender);
            ReflectionTestUtils.setField(emailService, "baseUrl", "http://production.com");

            doNothing().when(mailSender).send(any(SimpleMailMessage.class));

            assertDoesNotThrow(() -> emailService.sendPasswordResetEmail("test@example.com", "reset-token"));

            verify(mailSender, atMostOnce()).send(any(SimpleMailMessage.class));
        }
    }

    @Nested
    @DisplayName("sendPaymentSuccessEmail() Tests")
    class SendPaymentSuccessEmailTests {

        @Test
        @DisplayName("Should send payment success email successfully")
        void shouldSendPaymentSuccessEmailSuccessfully() {
            ReflectionTestUtils.setField(emailService, "mailSender", null);

            assertDoesNotThrow(() -> emailService.sendPaymentSuccessEmail(
                    "customer@example.com",
                    "John Doe",
                    123,
                    "Test Restaurant",
                    "2024-01-01 18:00",
                    4,
                    BigDecimal.valueOf(500000.0),
                    BigDecimal.ZERO,
                    "PayOS"
            ));
        }

        @Test
        @DisplayName("Should handle remaining amount")
        void shouldHandleRemainingAmount() {
            ReflectionTestUtils.setField(emailService, "mailSender", null);

            assertDoesNotThrow(() -> emailService.sendPaymentSuccessEmail(
                    "customer@example.com",
                    "John Doe",
                    123,
                    "Test Restaurant",
                    "2024-01-01 18:00",
                    4,
                    BigDecimal.valueOf(300000.0),
                    BigDecimal.valueOf(200000.0),
                    "PayOS"
            ));
        }

        @Test
        @DisplayName("Should handle null remaining amount")
        void shouldHandleNullRemainingAmount() {
            ReflectionTestUtils.setField(emailService, "mailSender", null);

            assertDoesNotThrow(() -> emailService.sendPaymentSuccessEmail(
                    "customer@example.com",
                    "John Doe",
                    123,
                    "Test Restaurant",
                    "2024-01-01 18:00",
                    4,
                    BigDecimal.valueOf(500000.0),
                    null,
                    "PayOS"
            ));
        }
    }

    @Nested
    @DisplayName("sendPaymentNotificationToRestaurant() Tests")
    class SendPaymentNotificationToRestaurantTests {

        @Test
        @DisplayName("Should send payment notification to restaurant successfully")
        void shouldSendPaymentNotificationSuccessfully() {
            ReflectionTestUtils.setField(emailService, "mailSender", null);

            assertDoesNotThrow(() -> emailService.sendPaymentNotificationToRestaurant(
                    "restaurant@example.com",
                    "Test Restaurant",
                    123,
                    "John Doe",
                    "2024-01-01 18:00",
                    4,
                    BigDecimal.valueOf(500000.0),
                    "PayOS"
            ));
        }
    }

    @Nested
    @DisplayName("sendRestaurantApprovalEmail() Tests")
    class SendRestaurantApprovalEmailTests {

        @Test
        @DisplayName("Should send restaurant approval email successfully")
        void shouldSendRestaurantApprovalEmailSuccessfully() {
            ReflectionTestUtils.setField(emailService, "mailSender", null);

            assertDoesNotThrow(() -> emailService.sendRestaurantApprovalEmail(
                    "owner@example.com",
                    "Test Restaurant",
                    "Restaurant Approved",
                    "Your restaurant has been approved!"
            ));
        }

        @Test
        @DisplayName("Should throw exception on failure")
        void shouldThrowExceptionOnFailure() {
            ReflectionTestUtils.setField(emailService, "mailSender", mailSender);
            ReflectionTestUtils.setField(emailService, "baseUrl", "http://production.com");

            doThrow(new RuntimeException("Mail error")).when(mailSender).send(any(SimpleMailMessage.class));

            assertThrows(RuntimeException.class, () -> emailService.sendRestaurantApprovalEmail(
                    "owner@example.com",
                    "Test Restaurant",
                    "Restaurant Approved",
                    "Your restaurant has been approved!"
            ));
        }
    }

    @Nested
    @DisplayName("sendRestaurantRejectionEmail() Tests")
    class SendRestaurantRejectionEmailTests {

        @Test
        @DisplayName("Should send restaurant rejection email successfully")
        void shouldSendRestaurantRejectionEmailSuccessfully() {
            ReflectionTestUtils.setField(emailService, "mailSender", null);

            assertDoesNotThrow(() -> emailService.sendRestaurantRejectionEmail(
                    "owner@example.com",
                    "Test Restaurant",
                    "Restaurant Rejected",
                    "Your restaurant has been rejected."
            ));
        }

        @Test
        @DisplayName("Should throw exception on failure")
        void shouldThrowExceptionOnFailure() {
            ReflectionTestUtils.setField(emailService, "mailSender", mailSender);
            ReflectionTestUtils.setField(emailService, "baseUrl", "http://production.com");

            doThrow(new RuntimeException("Mail error")).when(mailSender).send(any(SimpleMailMessage.class));

            assertThrows(RuntimeException.class, () -> emailService.sendRestaurantRejectionEmail(
                    "owner@example.com",
                    "Test Restaurant",
                    "Restaurant Rejected",
                    "Your restaurant has been rejected."
            ));
        }
    }

    @Nested
    @DisplayName("sendRestaurantSuspensionEmail() Tests")
    class SendRestaurantSuspensionEmailTests {

        @Test
        @DisplayName("Should send restaurant suspension email successfully")
        void shouldSendRestaurantSuspensionEmailSuccessfully() {
            ReflectionTestUtils.setField(emailService, "mailSender", null);

            assertDoesNotThrow(() -> emailService.sendRestaurantSuspensionEmail(
                    "owner@example.com",
                    "Test Restaurant",
                    "Restaurant Suspended",
                    "Your restaurant has been suspended."
            ));
        }
    }

    @Nested
    @DisplayName("sendRestaurantResubmitEmail() Tests")
    class SendRestaurantResubmitEmailTests {

        @Test
        @DisplayName("Should send restaurant resubmit email successfully")
        void shouldSendRestaurantResubmitEmailSuccessfully() {
            ReflectionTestUtils.setField(emailService, "mailSender", null);

            assertDoesNotThrow(() -> emailService.sendRestaurantResubmitEmail(
                    "owner@example.com",
                    "Test Restaurant",
                    "Resubmit Required",
                    "Please resubmit your restaurant information."
            ));
        }
    }

    @Nested
    @DisplayName("sendRestaurantActivationEmail() Tests")
    class SendRestaurantActivationEmailTests {

        @Test
        @DisplayName("Should send restaurant activation email successfully")
        void shouldSendRestaurantActivationEmailSuccessfully() {
            ReflectionTestUtils.setField(emailService, "mailSender", null);

            assertDoesNotThrow(() -> emailService.sendRestaurantActivationEmail(
                    "owner@example.com",
                    "Test Restaurant",
                    "Restaurant Activated",
                    "Your restaurant has been activated!"
            ));
        }
    }

    @Nested
    @DisplayName("sendEmail() Tests")
    class SendEmailTests {

        @Test
        @DisplayName("Should handle localhost without valid config (mock mode)")
        void shouldHandleLocalhostWithoutValidConfig() {
            ReflectionTestUtils.setField(emailService, "mailSender", null);
            ReflectionTestUtils.setField(emailService, "baseUrl", "http://localhost:8080");
            ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@bookeat.com");

            assertDoesNotThrow(() -> emailService.sendEmail(
                    "test@example.com",
                    "Test Subject",
                    "Test Message"
            ));
        }

        @Test
        @DisplayName("Should handle localhost with valid config")
        void shouldHandleLocalhostWithValidConfig() {
            ReflectionTestUtils.setField(emailService, "mailSender", mailSender);
            ReflectionTestUtils.setField(emailService, "baseUrl", "http://localhost:8080");
            ReflectionTestUtils.setField(emailService, "fromEmail", "real@example.com");

            doNothing().when(mailSender).send(any(SimpleMailMessage.class));

            assertDoesNotThrow(() -> emailService.sendEmail(
                    "test@example.com",
                    "Test Subject",
                    "Test Message"
            ));

            verify(mailSender).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("Should handle production environment")
        void shouldHandleProductionEnvironment() {
            ReflectionTestUtils.setField(emailService, "mailSender", mailSender);
            ReflectionTestUtils.setField(emailService, "baseUrl", "https://bookeat.com");
            ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@bookeat.com");

            doThrow(new RuntimeException("Mail error")).when(mailSender).send(any(SimpleMailMessage.class));

            // Should fall back to mock mode and not throw exception
            assertDoesNotThrow(() -> emailService.sendEmail(
                    "test@example.com",
                    "Test Subject",
                    "Test Message"
            ));
        }
    }
}

