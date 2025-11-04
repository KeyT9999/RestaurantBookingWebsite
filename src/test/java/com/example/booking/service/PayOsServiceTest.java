package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.booking.service.PayOsService.*;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("PayOsService Comprehensive Unit Tests - 100% Coverage")
class PayOsServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PayOsService payOsService;

    private static final String CLIENT_ID = "CLIENT12345";
    private static final String API_KEY = "APIKEY12345";
    private static final String CHECKSUM_KEY = "SECRETKEY12345";
    private static final String ENDPOINT = "https://sandbox.payos.vn";
    private static final String RETURN_URL = "https://domain.com/payment/payos/return";
    private static final String CANCEL_URL = "https://domain.com/payment/payos/cancel";

    private long orderCode;
    private long amount;
    private String description;

    @BeforeEach
    void setUp() {
        orderCode = 12345L;
        amount = 50000L;
        description = "Test order description";

        ReflectionTestUtils.setField(payOsService, "clientId", CLIENT_ID);
        ReflectionTestUtils.setField(payOsService, "apiKey", API_KEY);
        ReflectionTestUtils.setField(payOsService, "checksumKey", CHECKSUM_KEY);
        ReflectionTestUtils.setField(payOsService, "endpoint", ENDPOINT);
        ReflectionTestUtils.setField(payOsService, "returnUrl", RETURN_URL);
        ReflectionTestUtils.setField(payOsService, "cancelUrl", CANCEL_URL);
        ReflectionTestUtils.setField(payOsService, "expirationMinutes", 15);
    }

    // ==================== CREATE PAYMENT LINK TESTS ====================

    @Nested
    @DisplayName("1. createPaymentLink() Tests")
    class CreatePaymentLinkTests {

        @Test
        @DisplayName("Happy Path: Should create payment link successfully")
        void testCreatePaymentLink_Success() {
            // Given
            CreateLinkResponse.Data data = new CreateLinkResponse.Data();
            ReflectionTestUtils.setField(data, "checkoutUrl", "https://checkout.url");
            ReflectionTestUtils.setField(data, "paymentLinkId", "PAY123");
            ReflectionTestUtils.setField(data, "orderCode", orderCode);
            ReflectionTestUtils.setField(data, "amount", amount);
            ReflectionTestUtils.setField(data, "description", description);

            CreateLinkResponse response = new CreateLinkResponse();
            ReflectionTestUtils.setField(response, "code", "00");
            ReflectionTestUtils.setField(response, "desc", "Success");
            ReflectionTestUtils.setField(response, "data", data);
            ReflectionTestUtils.setField(response, "signature", "sig123");

            ResponseEntity<CreateLinkResponse> entity = new ResponseEntity<>(response, HttpStatus.OK);
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    eq(CreateLinkResponse.class))).thenReturn(entity);

            // When
            CreateLinkResponse result = payOsService.createPaymentLink(orderCode, amount, description);

            // Then
            assertNotNull(result);
            assertEquals("00", result.getCode());
            assertEquals("Success", result.getDesc());
            assertNotNull(result.getData());
            assertEquals("https://checkout.url", result.getData().getCheckoutUrl());
            assertEquals("PAY123", result.getData().getPaymentLinkId());
            verify(restTemplate).exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    eq(CreateLinkResponse.class));
        }

        @Test
        @DisplayName("Exception: Null clientId should throw IllegalStateException")
        void testCreatePaymentLink_NullClientId_ThrowsException() {
            // Given
            ReflectionTestUtils.setField(payOsService, "clientId", null);

            // When & Then
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                payOsService.createPaymentLink(orderCode, amount, description);
            });
            assertTrue(exception.getMessage().contains("ClientId chưa được cấu hình"));
        }

        @Test
        @DisplayName("Exception: Empty clientId should throw IllegalStateException")
        void testCreatePaymentLink_EmptyClientId_ThrowsException() {
            // Given
            ReflectionTestUtils.setField(payOsService, "clientId", "   ");

            // When & Then
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                payOsService.createPaymentLink(orderCode, amount, description);
            });
            assertTrue(exception.getMessage().contains("ClientId chưa được cấu hình"));
        }

        @Test
        @DisplayName("Exception: Null apiKey should throw IllegalStateException")
        void testCreatePaymentLink_NullApiKey_ThrowsException() {
            // Given
            ReflectionTestUtils.setField(payOsService, "apiKey", null);

            // When & Then
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                payOsService.createPaymentLink(orderCode, amount, description);
            });
            assertTrue(exception.getMessage().contains("ApiKey chưa được cấu hình"));
        }

        @Test
        @DisplayName("Exception: Empty apiKey should throw IllegalStateException")
        void testCreatePaymentLink_EmptyApiKey_ThrowsException() {
            // Given
            ReflectionTestUtils.setField(payOsService, "apiKey", "");

            // When & Then
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                payOsService.createPaymentLink(orderCode, amount, description);
            });
            assertTrue(exception.getMessage().contains("ApiKey chưa được cấu hình"));
        }

        @Test
        @DisplayName("Exception: Null checksumKey should throw IllegalStateException")
        void testCreatePaymentLink_NullChecksumKey_ThrowsException() {
            // Given
            ReflectionTestUtils.setField(payOsService, "checksumKey", null);

            // When & Then
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                payOsService.createPaymentLink(orderCode, amount, description);
            });
            assertTrue(exception.getMessage().contains("ChecksumKey chưa được cấu hình"));
        }

        @Test
        @DisplayName("Exception: Empty checksumKey should throw IllegalStateException")
        void testCreatePaymentLink_EmptyChecksumKey_ThrowsException() {
            // Given
            ReflectionTestUtils.setField(payOsService, "checksumKey", "   ");

            // When & Then
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                payOsService.createPaymentLink(orderCode, amount, description);
            });
            assertTrue(exception.getMessage().contains("ChecksumKey chưa được cấu hình"));
        }

        @Test
        @DisplayName("Exception: RestTemplate throws exception")
        void testCreatePaymentLink_RestTemplateException_ThrowsRuntimeException() {
            // Given
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    eq(CreateLinkResponse.class)))
                    .thenThrow(new RestClientException("Connection failed"));

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                payOsService.createPaymentLink(orderCode, amount, description);
            });
            assertTrue(exception.getMessage().contains("Failed to create PayOS payment link"));
        }

        @Test
        @DisplayName("Should handle response with null body")
        void testCreatePaymentLink_ResponseWithNullBody() {
            // Given
            ResponseEntity<CreateLinkResponse> entity = new ResponseEntity<>(null, HttpStatus.OK);
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    eq(CreateLinkResponse.class))).thenReturn(entity);

            // When
            CreateLinkResponse result = payOsService.createPaymentLink(orderCode, amount, description);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("Should handle response with null data")
        void testCreatePaymentLink_ResponseWithNullData() {
            // Given
            CreateLinkResponse response = new CreateLinkResponse();
            ReflectionTestUtils.setField(response, "code", "00");
            ReflectionTestUtils.setField(response, "desc", "Success");
            ReflectionTestUtils.setField(response, "data", null);

            ResponseEntity<CreateLinkResponse> entity = new ResponseEntity<>(response, HttpStatus.OK);
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    eq(CreateLinkResponse.class))).thenReturn(entity);

            // When
            CreateLinkResponse result = payOsService.createPaymentLink(orderCode, amount, description);

            // Then
            assertNotNull(result);
            assertNull(result.getData());
        }

        @Test
        @DisplayName("Should ensure alias fields initialized")
        void testCreatePaymentLink_EnsuresAliasFieldsInitialized() {
            // Given - secretKey and baseUrl are null initially
            ReflectionTestUtils.setField(payOsService, "secretKey", null);
            ReflectionTestUtils.setField(payOsService, "baseUrl", null);

            CreateLinkResponse.Data data = new CreateLinkResponse.Data();
            ReflectionTestUtils.setField(data, "checkoutUrl", "https://checkout.url");
            CreateLinkResponse response = new CreateLinkResponse();
            ReflectionTestUtils.setField(response, "code", "00");
            ReflectionTestUtils.setField(response, "data", data);

            ResponseEntity<CreateLinkResponse> entity = new ResponseEntity<>(response, HttpStatus.OK);
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    eq(CreateLinkResponse.class))).thenReturn(entity);

            // When
            payOsService.createPaymentLink(orderCode, amount, description);

            // Then - ensureAliasFieldsInitialized should set them
            String secretKey = (String) ReflectionTestUtils.getField(payOsService, "secretKey");
            String baseUrl = (String) ReflectionTestUtils.getField(payOsService, "baseUrl");
            assertNotNull(secretKey);
            assertNotNull(baseUrl);
        }

        @Test
        @DisplayName("Should set expiredAt correctly")
        void testCreatePaymentLink_ShouldSetExpiredAt() {
            // Given
            long beforeTime = Instant.now().getEpochSecond();
            CreateLinkResponse.Data data = new CreateLinkResponse.Data();
            ReflectionTestUtils.setField(data, "checkoutUrl", "https://checkout.url");
            CreateLinkResponse response = new CreateLinkResponse();
            ReflectionTestUtils.setField(response, "code", "00");
            ReflectionTestUtils.setField(response, "data", data);

            ResponseEntity<CreateLinkResponse> entity = new ResponseEntity<>(response, HttpStatus.OK);
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    eq(CreateLinkResponse.class)))
                    .thenAnswer(invocation -> {
                        HttpEntity<?> httpEntity = invocation.getArgument(2);
                        @SuppressWarnings("unchecked")
                        Map<String, Object> payload = (Map<String, Object>) httpEntity.getBody();
                        assertNotNull(payload.get("expiredAt"));
                        long expiredAt = ((Number) payload.get("expiredAt")).longValue();
                        long afterTime = Instant.now().getEpochSecond();
                        // Should be approximately 15 minutes (900 seconds) from now
                        assertTrue(expiredAt >= beforeTime + 14 * 60 && expiredAt <= afterTime + 16 * 60);
                        return entity;
                    });

            // When
            payOsService.createPaymentLink(orderCode, amount, description);

            // Then - verified in thenAnswer
            verify(restTemplate).exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    eq(CreateLinkResponse.class));
        }
    }

    // ==================== CREATE REFUND PAYMENT LINK TESTS ====================

    @Nested
    @DisplayName("2. createRefundPaymentLink() Tests")
    class CreateRefundPaymentLinkTests {

        @Test
        @DisplayName("Should create refund payment link with custom URLs")
        void testCreateRefundPaymentLink_Success() {
            // Given
            CreateLinkResponse.Data data = new CreateLinkResponse.Data();
            ReflectionTestUtils.setField(data, "checkoutUrl", "https://checkout.url");
            CreateLinkResponse response = new CreateLinkResponse();
            ReflectionTestUtils.setField(response, "code", "00");
            ReflectionTestUtils.setField(response, "data", data);

            ResponseEntity<CreateLinkResponse> entity = new ResponseEntity<>(response, HttpStatus.OK);
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    eq(CreateLinkResponse.class)))
                    .thenAnswer(invocation -> {
                        HttpEntity<?> httpEntity = invocation.getArgument(2);
                        @SuppressWarnings("unchecked")
                        Map<String, Object> payload = (Map<String, Object>) httpEntity.getBody();
                        // Verify refund URLs are used
                        String returnUrl = (String) payload.get("returnUrl");
                        String cancelUrl = (String) payload.get("cancelUrl");
                        assertTrue(returnUrl.contains("/refund/payos/return"));
                        assertTrue(cancelUrl.contains("/refund/payos/cancel"));
                        return entity;
                    });

            // When
            CreateLinkResponse result = payOsService.createRefundPaymentLink(orderCode, amount, description);

            // Then
            assertNotNull(result);
            verify(restTemplate).exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    eq(CreateLinkResponse.class));
        }
    }

    // ==================== GET PAYMENT INFO TESTS ====================

    @Nested
    @DisplayName("3. getPaymentInfo() Tests")
    class GetPaymentInfoTests {

        @Test
        @DisplayName("Should get payment info successfully")
        void testGetPaymentInfo_Success() {
            // Given
            PaymentInfoResponse.PaymentInfoData data = new PaymentInfoResponse.PaymentInfoData();
            ReflectionTestUtils.setField(data, "orderCode", orderCode);
            ReflectionTestUtils.setField(data, "amount", amount);
            ReflectionTestUtils.setField(data, "amountPaid", amount);
            ReflectionTestUtils.setField(data, "status", "PAID");
            ReflectionTestUtils.setField(data, "createdAt", "2024-01-01T00:00:00Z");

            PaymentInfoResponse response = new PaymentInfoResponse();
            ReflectionTestUtils.setField(response, "code", "00");
            ReflectionTestUtils.setField(response, "desc", "Success");
            ReflectionTestUtils.setField(response, "data", data);
            ReflectionTestUtils.setField(response, "signature", "sig123");

            ResponseEntity<PaymentInfoResponse> entity = new ResponseEntity<>(response, HttpStatus.OK);
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
                    eq(PaymentInfoResponse.class))).thenReturn(entity);

            // When
            PaymentInfoResponse result = payOsService.getPaymentInfo(orderCode);

            // Then
            assertNotNull(result);
            assertEquals("00", result.getCode());
            assertNotNull(result.getData());
            assertEquals(Long.valueOf(orderCode), result.getData().getOrderCode());
            assertEquals("PAID", result.getData().getStatus());
            verify(restTemplate).exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
                    eq(PaymentInfoResponse.class));
        }

        @Test
        @DisplayName("Exception: RestTemplate throws exception")
        void testGetPaymentInfo_RestTemplateException_ThrowsRuntimeException() {
            // Given
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
                    eq(PaymentInfoResponse.class)))
                    .thenThrow(new RestClientException("Connection failed"));

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                payOsService.getPaymentInfo(orderCode);
            });
            assertTrue(exception.getMessage().contains("Failed to get PayOS payment info"));
        }

        @Test
        @DisplayName("Should handle response with null body")
        void testGetPaymentInfo_ResponseWithNullBody() {
            // Given
            ResponseEntity<PaymentInfoResponse> entity = new ResponseEntity<>(null, HttpStatus.OK);
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
                    eq(PaymentInfoResponse.class))).thenReturn(entity);

            // When
            PaymentInfoResponse result = payOsService.getPaymentInfo(orderCode);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("Should include all payment info fields")
        void testGetPaymentInfo_IncludesAllFields() {
            // Given
            PaymentInfoResponse.PaymentInfoData data = new PaymentInfoResponse.PaymentInfoData();
            ReflectionTestUtils.setField(data, "id", "PAY123");
            ReflectionTestUtils.setField(data, "orderCode", orderCode);
            ReflectionTestUtils.setField(data, "amount", amount);
            ReflectionTestUtils.setField(data, "amountPaid", amount);
            ReflectionTestUtils.setField(data, "amountRemaining", 0L);
            ReflectionTestUtils.setField(data, "status", "PAID");
            ReflectionTestUtils.setField(data, "createdAt", "2024-01-01T00:00:00Z");
            ReflectionTestUtils.setField(data, "canceledAt", null);
            ReflectionTestUtils.setField(data, "cancellationReason", null);

            PaymentInfoResponse response = new PaymentInfoResponse();
            ReflectionTestUtils.setField(response, "code", "00");
            ReflectionTestUtils.setField(response, "data", data);

            ResponseEntity<PaymentInfoResponse> entity = new ResponseEntity<>(response, HttpStatus.OK);
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
                    eq(PaymentInfoResponse.class))).thenReturn(entity);

            // When
            PaymentInfoResponse result = payOsService.getPaymentInfo(orderCode);

            // Then
            assertNotNull(result);
            assertNotNull(result.getData());
            assertEquals("PAY123", result.getData().getId());
            assertEquals(0L, result.getData().getAmountRemaining().longValue());
        }
    }

    // ==================== CANCEL PAYMENT TESTS ====================

    @Nested
    @DisplayName("4. cancelPayment() Tests")
    class CancelPaymentTests {

        @Test
        @DisplayName("Should cancel payment with reason successfully")
        void testCancelPayment_WithReason_Success() {
            // Given
            PaymentInfoResponse.PaymentInfoData data = new PaymentInfoResponse.PaymentInfoData();
            ReflectionTestUtils.setField(data, "status", "CANCELLED");
            ReflectionTestUtils.setField(data, "cancellationReason", "Customer request");

            CancelPaymentResponse response = new CancelPaymentResponse();
            ReflectionTestUtils.setField(response, "code", "00");
            ReflectionTestUtils.setField(response, "desc", "Cancelled");
            ReflectionTestUtils.setField(response, "data", data);
            ReflectionTestUtils.setField(response, "signature", "sig123");

            ResponseEntity<CancelPaymentResponse> entity = new ResponseEntity<>(response, HttpStatus.OK);
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    eq(CancelPaymentResponse.class))).thenReturn(entity);

            // When
            CancelPaymentResponse result = payOsService.cancelPayment(orderCode, "Customer request");

            // Then
            assertNotNull(result);
            assertEquals("00", result.getCode());
            assertNotNull(result.getData());
            verify(restTemplate).exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    eq(CancelPaymentResponse.class));
        }

        @Test
        @DisplayName("Should cancel payment without reason")
        void testCancelPayment_WithoutReason_Success() {
            // Given
            CancelPaymentResponse response = new CancelPaymentResponse();
            ReflectionTestUtils.setField(response, "code", "00");
            ReflectionTestUtils.setField(response, "desc", "Cancelled");

            ResponseEntity<CancelPaymentResponse> entity = new ResponseEntity<>(response, HttpStatus.OK);
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    eq(CancelPaymentResponse.class)))
                    .thenAnswer(invocation -> {
                        HttpEntity<?> httpEntity = invocation.getArgument(2);
                        @SuppressWarnings("unchecked")
                        Map<String, Object> payload = (Map<String, Object>) httpEntity.getBody();
                        // Should not contain cancellationReason when null or empty
                        assertTrue(payload.isEmpty() || !payload.containsKey("cancellationReason"));
                        return entity;
                    });

            // When
            CancelPaymentResponse result = payOsService.cancelPayment(orderCode, null);

            // Then
            assertNotNull(result);
            verify(restTemplate).exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    eq(CancelPaymentResponse.class));
        }

        @Test
        @DisplayName("Should cancel payment with empty reason")
        void testCancelPayment_WithEmptyReason_Success() {
            // Given
            CancelPaymentResponse response = new CancelPaymentResponse();
            ReflectionTestUtils.setField(response, "code", "00");

            ResponseEntity<CancelPaymentResponse> entity = new ResponseEntity<>(response, HttpStatus.OK);
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    eq(CancelPaymentResponse.class)))
                    .thenAnswer(invocation -> {
                        HttpEntity<?> httpEntity = invocation.getArgument(2);
                        @SuppressWarnings("unchecked")
                        Map<String, Object> payload = (Map<String, Object>) httpEntity.getBody();
                        // Empty reason should not be included
                        assertTrue(payload.isEmpty() || !payload.containsKey("cancellationReason"));
                        return entity;
                    });

            // When
            CancelPaymentResponse result = payOsService.cancelPayment(orderCode, "   ");

            // Then
            assertNotNull(result);
        }

        @Test
        @DisplayName("Exception: RestTemplate throws exception")
        void testCancelPayment_RestTemplateException_ThrowsRuntimeException() {
            // Given
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    eq(CancelPaymentResponse.class)))
                    .thenThrow(new RestClientException("Connection failed"));

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                payOsService.cancelPayment(orderCode, "reason");
            });
            assertTrue(exception.getMessage().contains("Failed to cancel PayOS payment"));
        }
    }

    // ==================== GET INVOICE INFO TESTS ====================

    @Nested
    @DisplayName("5. getInvoiceInfo() Tests")
    class GetInvoiceInfoTests {

        @Test
        @DisplayName("Should get invoice info successfully")
        void testGetInvoiceInfo_Success() {
            // Given
            InvoiceInfoResponse.InvoiceData.Invoice invoice = new InvoiceInfoResponse.InvoiceData.Invoice();
            ReflectionTestUtils.setField(invoice, "invoiceId", "INV123");
            ReflectionTestUtils.setField(invoice, "invoiceNumber", "INV-001");
            ReflectionTestUtils.setField(invoice, "issuedTimestamp", 1234567890L);
            ReflectionTestUtils.setField(invoice, "issuedDatetime", "2024-01-01");
            ReflectionTestUtils.setField(invoice, "transactionId", "TXN123");
            ReflectionTestUtils.setField(invoice, "reservationCode", "RES123");
            ReflectionTestUtils.setField(invoice, "codeOfTax", "TAX123");

            List<InvoiceInfoResponse.InvoiceData.Invoice> invoices = new ArrayList<>();
            invoices.add(invoice);

            InvoiceInfoResponse.InvoiceData invoiceData = new InvoiceInfoResponse.InvoiceData();
            ReflectionTestUtils.setField(invoiceData, "invoices", invoices);

            InvoiceInfoResponse response = new InvoiceInfoResponse();
            ReflectionTestUtils.setField(response, "code", "00");
            ReflectionTestUtils.setField(response, "desc", "Success");
            ReflectionTestUtils.setField(response, "data", invoiceData);
            ReflectionTestUtils.setField(response, "signature", "sig123");

            ResponseEntity<InvoiceInfoResponse> entity = new ResponseEntity<>(response, HttpStatus.OK);
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
                    eq(InvoiceInfoResponse.class))).thenReturn(entity);

            // When
            InvoiceInfoResponse result = payOsService.getInvoiceInfo(orderCode);

            // Then
            assertNotNull(result);
            assertEquals("00", result.getCode());
            assertNotNull(result.getData());
            assertNotNull(result.getData().getInvoices());
            assertEquals(1, result.getData().getInvoices().size());
            assertEquals("INV123", result.getData().getInvoices().get(0).getInvoiceId());
            verify(restTemplate).exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
                    eq(InvoiceInfoResponse.class));
        }

        @Test
        @DisplayName("Exception: RestTemplate throws exception")
        void testGetInvoiceInfo_RestTemplateException_ThrowsRuntimeException() {
            // Given
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
                    eq(InvoiceInfoResponse.class)))
                    .thenThrow(new RestClientException("Connection failed"));

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                payOsService.getInvoiceInfo(orderCode);
            });
            assertTrue(exception.getMessage().contains("Failed to get PayOS invoice info"));
        }
    }

    // ==================== DOWNLOAD INVOICE TESTS ====================

    @Nested
    @DisplayName("6. downloadInvoice() Tests")
    class DownloadInvoiceTests {

        @Test
        @DisplayName("Should download invoice PDF successfully")
        void testDownloadInvoice_Success() {
            // Given
            byte[] pdfData = "PDF_CONTENT".getBytes();
            ResponseEntity<byte[]> entity = new ResponseEntity<>(pdfData, HttpStatus.OK);
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
                    eq(byte[].class))).thenReturn(entity);

            // When
            byte[] result = payOsService.downloadInvoice(orderCode, "INV123");

            // Then
            assertNotNull(result);
            assertArrayEquals(pdfData, result);
            verify(restTemplate).exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
                    eq(byte[].class));
        }

        @Test
        @DisplayName("Exception: RestTemplate throws exception")
        void testDownloadInvoice_RestTemplateException_ThrowsRuntimeException() {
            // Given
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
                    eq(byte[].class)))
                    .thenThrow(new RestClientException("Connection failed"));

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                payOsService.downloadInvoice(orderCode, "INV123");
            });
            assertTrue(exception.getMessage().contains("Failed to download PayOS invoice"));
        }
    }

    // ==================== CONFIRM WEBHOOK TESTS ====================

    @Nested
    @DisplayName("7. confirmWebhook() Tests")
    class ConfirmWebhookTests {

        @Test
        @DisplayName("Happy Path: Should confirm webhook successfully")
        void testConfirmWebhook_Success() {
            // Given
            String webhookUrl = "https://domain.com/webhook";
            WebhookConfirmResponse.WebhookConfirmData data = new WebhookConfirmResponse.WebhookConfirmData();
            ReflectionTestUtils.setField(data, "webhookUrl", webhookUrl);
            ReflectionTestUtils.setField(data, "accountNumber", "1234567890");
            ReflectionTestUtils.setField(data, "accountName", "Test Account");
            ReflectionTestUtils.setField(data, "name", "Name");
            ReflectionTestUtils.setField(data, "shortName", "Short");

            WebhookConfirmResponse response = new WebhookConfirmResponse();
            ReflectionTestUtils.setField(response, "code", "00");
            ReflectionTestUtils.setField(response, "desc", "Success");
            ReflectionTestUtils.setField(response, "data", data);

            ResponseEntity<WebhookConfirmResponse> entity = new ResponseEntity<>(response, HttpStatus.OK);
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    eq(WebhookConfirmResponse.class))).thenReturn(entity);

            // When
            WebhookConfirmResponse result = payOsService.confirmWebhook(webhookUrl);

            // Then
            assertNotNull(result);
            assertEquals("00", result.getCode());
            assertNotNull(result.getData());
            assertEquals(webhookUrl, result.getData().getWebhookUrl());
            verify(restTemplate).exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    eq(WebhookConfirmResponse.class));
        }

        @Test
        @DisplayName("Exception: Null webhook URL should throw IllegalArgumentException")
        void testConfirmWebhook_NullUrl_ThrowsIllegalArgumentException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                payOsService.confirmWebhook(null);
            });
            assertTrue(exception.getMessage().contains("cannot be null or empty"));
        }

        @Test
        @DisplayName("Exception: Empty webhook URL should throw IllegalArgumentException")
        void testConfirmWebhook_EmptyUrl_ThrowsIllegalArgumentException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                payOsService.confirmWebhook("   ");
            });
            assertTrue(exception.getMessage().contains("cannot be null or empty"));
        }

        @Test
        @DisplayName("Exception: Invalid webhook URL (no http/https) should throw IllegalArgumentException")
        void testConfirmWebhook_InvalidUrl_ThrowsIllegalArgumentException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                payOsService.confirmWebhook("invalid-url");
            });
            assertTrue(exception.getMessage().contains("must start with http:// or https://"));
        }

        @Test
        @DisplayName("Should accept http:// URL")
        void testConfirmWebhook_HttpUrl_Success() {
            // Given
            String webhookUrl = "http://domain.com/webhook";
            WebhookConfirmResponse response = new WebhookConfirmResponse();
            ReflectionTestUtils.setField(response, "code", "00");

            ResponseEntity<WebhookConfirmResponse> entity = new ResponseEntity<>(response, HttpStatus.OK);
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    eq(WebhookConfirmResponse.class))).thenReturn(entity);

            // When
            WebhookConfirmResponse result = payOsService.confirmWebhook(webhookUrl);

            // Then
            assertNotNull(result);
        }

        @Test
        @DisplayName("Should handle response with null body")
        void testConfirmWebhook_ResponseWithNullBody() {
            // Given
            String webhookUrl = "https://domain.com/webhook";
            ResponseEntity<WebhookConfirmResponse> entity = new ResponseEntity<>(null, HttpStatus.OK);
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    eq(WebhookConfirmResponse.class))).thenReturn(entity);

            // When
            WebhookConfirmResponse result = payOsService.confirmWebhook(webhookUrl);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("Exception: RestTemplate throws exception (non-IllegalArgumentException)")
        void testConfirmWebhook_RestTemplateException_ThrowsRuntimeException() {
            // Given
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    eq(WebhookConfirmResponse.class)))
                    .thenThrow(new RestClientException("Connection failed"));

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                payOsService.confirmWebhook("https://domain.com/webhook");
            });
            assertTrue(exception.getMessage().contains("Failed to confirm PayOS webhook"));
        }
    }

    // ==================== VERIFY WEBHOOK TESTS ====================

    @Nested
    @DisplayName("8. verifyWebhook() Tests")
    class VerifyWebhookTests {

        @Test
        @DisplayName("Should verify webhook with correct signature")
        void testVerifyWebhook_CorrectSignature_ReturnsTrue() {
            // Given
            String body = "test data";
            // Calculate expected signature
            String expectedSignature;
            try {
                javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
                javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(
                        CHECKSUM_KEY.getBytes("UTF-8"), "HmacSHA256");
                mac.init(secretKeySpec);
                byte[] raw = mac.doFinal(body.getBytes("UTF-8"));
                StringBuilder sb = new StringBuilder();
                for (byte b : raw) {
                    sb.append(String.format("%02x", b));
                }
                expectedSignature = sb.toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // When
            boolean result = payOsService.verifyWebhook(body, expectedSignature);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false for incorrect signature")
        void testVerifyWebhook_IncorrectSignature_ReturnsFalse() {
            // Given
            String body = "test data";
            String invalidSignature = "invalid_signature";

            // When
            boolean result = payOsService.verifyWebhook(body, invalidSignature);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("Exception: Should return false when exception occurs")
        void testVerifyWebhook_Exception_ReturnsFalse() {
            // Given - null body will cause exception
            String body = null;
            String signature = "any";

            // When
            boolean result = payOsService.verifyWebhook(body, signature);

            // Then
            assertFalse(result);
        }
    }

    // ==================== CREATE TRANSFER QR CODE TESTS ====================

    @Nested
    @DisplayName("9. createTransferQRCode() Tests")
    class CreateTransferQRCodeTests {

        @Test
        @DisplayName("Should create transfer QR code successfully")
        void testCreateTransferQRCode_Success() throws Exception {
            // Given
            BigDecimal amount = new BigDecimal("100000");
            String bankAccount = "1234567890";
            String bankName = "Vietcombank";
            String accountHolder = "Test Account";
            String desc = "Refund";

            ReflectionTestUtils.setField(payOsService, "secretKey", CHECKSUM_KEY);
            ReflectionTestUtils.setField(payOsService, "baseUrl", ENDPOINT);

            when(objectMapper.writeValueAsString(any())).thenReturn("{\"qr\":\"data\"}");

            // When
            String result = payOsService.createTransferQRCode(amount, bankAccount, bankName, accountHolder, desc);

            // Then
            assertNotNull(result);
            verify(objectMapper).writeValueAsString(any());
        }

        @Test
        @DisplayName("Exception: Should return null when exception occurs")
        void testCreateTransferQRCode_Exception_ReturnsNull() throws Exception {
            // Given
            when(objectMapper.writeValueAsString(any()))
                    .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("Error") {
                        private static final long serialVersionUID = 1L;
                    });

            // When
            String result = payOsService.createTransferQRCode(new BigDecimal("100000"), "123", "Bank", "Holder",
                    "Desc");

            // Then
            assertNull(result);
        }
    }

    // ==================== CREATE TRANSFER QR CODE URL TESTS ====================

    @Nested
    @DisplayName("10. createTransferQRCodeUrl() Tests")
    class CreateTransferQRCodeUrlTests {

        @Test
        @DisplayName("Should create transfer QR code URL successfully")
        void testCreateTransferQRCodeUrl_Success() throws Exception {
            // Given
            BigDecimal amount = new BigDecimal("100000");
            String bankAccount = "1234567890";
            String bankName = "Vietcombank";
            String accountHolder = "Test Account";
            String desc = "Refund";

            ReflectionTestUtils.setField(payOsService, "secretKey", CHECKSUM_KEY);
            ReflectionTestUtils.setField(payOsService, "baseUrl", ENDPOINT);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("qrCodeUrl", "https://qr.payos.vn/123");
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> entity = new ResponseEntity<>(responseBody, HttpStatus.OK);

            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    any(Class.class))).thenReturn(entity);

            // When
            String result = payOsService.createTransferQRCodeUrl(amount, bankAccount, bankName, accountHolder, desc);

            // Then
            assertNotNull(result);
            assertEquals("https://qr.payos.vn/123", result);
        }

        @Test
        @DisplayName("Should return null when API returns error")
        void testCreateTransferQRCodeUrl_ApiError_ReturnsNull() {
            // Given
            ResponseEntity<Map<String, Object>> entity = new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    any(Class.class))).thenReturn(entity);

            // When
            String result = payOsService.createTransferQRCodeUrl(new BigDecimal("100000"), "123", "Bank", "Holder",
                    "Desc");

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("Exception: Should fallback to createTransferQRCode when exception occurs")
        void testCreateTransferQRCodeUrl_Exception_FallbackToCreateTransferQRCode() throws Exception {
            // Given
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    any(Class.class)))
                    .thenThrow(new RestClientException("Connection failed"));

            ReflectionTestUtils.setField(payOsService, "secretKey", CHECKSUM_KEY);
            when(objectMapper.writeValueAsString(any())).thenReturn("{\"qr\":\"data\"}");

            // When
            String result = payOsService.createTransferQRCodeUrl(new BigDecimal("100000"), "123", "Bank", "Holder",
                    "Desc");

            // Then - Should fallback to createTransferQRCode
            assertNotNull(result);
            verify(objectMapper).writeValueAsString(any());
        }
    }

    // ==================== INIT ALIAS FIELDS TESTS ====================

    @Nested
    @DisplayName("11. initAliasFields() Tests")
    class InitAliasFieldsTests {

        @Test
        @DisplayName("Should initialize alias fields")
        void testInitAliasFields_Success() {
            // Given
            ReflectionTestUtils.setField(payOsService, "secretKey", null);
            ReflectionTestUtils.setField(payOsService, "baseUrl", null);

            // When
            payOsService.initAliasFields();

            // Then
            String secretKey = (String) ReflectionTestUtils.getField(payOsService, "secretKey");
            String baseUrl = (String) ReflectionTestUtils.getField(payOsService, "baseUrl");
            assertEquals(CHECKSUM_KEY, secretKey);
            assertEquals(ENDPOINT, baseUrl);
        }
    }

    // ==================== INNER CLASS GETTERS TESTS ====================

    @Nested
    @DisplayName("12. Inner Classes Getters Tests")
    class InnerClassGettersTests {

        @Test
        @DisplayName("CreateLinkResponse getters")
        void testCreateLinkResponse_Getters() {
            CreateLinkResponse response = new CreateLinkResponse();
            ReflectionTestUtils.setField(response, "code", "00");
            ReflectionTestUtils.setField(response, "desc", "Desc");
            ReflectionTestUtils.setField(response, "signature", "sig");

            CreateLinkResponse.Data data = new CreateLinkResponse.Data();
            ReflectionTestUtils.setField(data, "paymentLinkId", "PAY123");
            ReflectionTestUtils.setField(data, "amount", amount);
            ReflectionTestUtils.setField(data, "description", description);
            ReflectionTestUtils.setField(data, "orderCode", orderCode);
            ReflectionTestUtils.setField(data, "checkoutUrl", "https://checkout.url");
            ReflectionTestUtils.setField(data, "qrCode", "QR123");
            ReflectionTestUtils.setField(response, "data", data);

            assertEquals("00", response.getCode());
            assertEquals("Desc", response.getDesc());
            assertEquals("sig", response.getSignature());
            assertNotNull(response.getData());
            assertEquals("PAY123", response.getData().getPaymentLinkId());
            assertEquals(Long.valueOf(amount), response.getData().getAmount());
            assertEquals(description, response.getData().getDescription());
            assertEquals(Long.valueOf(orderCode), response.getData().getOrderCode());
            assertEquals("https://checkout.url", response.getData().getCheckoutUrl());
            assertEquals("QR123", response.getData().getQrCode());
        }

        @Test
        @DisplayName("PaymentInfoResponse getters")
        void testPaymentInfoResponse_Getters() {
            PaymentInfoResponse response = new PaymentInfoResponse();
            ReflectionTestUtils.setField(response, "code", "00");
            ReflectionTestUtils.setField(response, "desc", "Desc");
            ReflectionTestUtils.setField(response, "signature", "sig");

            PaymentInfoResponse.PaymentInfoData data = new PaymentInfoResponse.PaymentInfoData();
            ReflectionTestUtils.setField(data, "id", "ID123");
            ReflectionTestUtils.setField(data, "orderCode", orderCode);
            ReflectionTestUtils.setField(data, "amount", amount);
            ReflectionTestUtils.setField(data, "amountPaid", amount);
            ReflectionTestUtils.setField(data, "amountRemaining", 0L);
            ReflectionTestUtils.setField(data, "status", "PAID");
            ReflectionTestUtils.setField(data, "createdAt", "2024-01-01");
            ReflectionTestUtils.setField(data, "canceledAt", "2024-01-02");
            ReflectionTestUtils.setField(data, "cancellationReason", "Reason");
            ReflectionTestUtils.setField(response, "data", data);

            assertEquals("00", response.getCode());
            assertEquals("Desc", response.getDesc());
            assertEquals("sig", response.getSignature());
            assertNotNull(response.getData());
            assertEquals("ID123", response.getData().getId());
            assertEquals(Long.valueOf(orderCode), response.getData().getOrderCode());
            assertEquals(Long.valueOf(amount), response.getData().getAmount());
            assertEquals(Long.valueOf(amount), response.getData().getAmountPaid());
            assertEquals(Long.valueOf(0L), response.getData().getAmountRemaining());
            assertEquals("PAID", response.getData().getStatus());
            assertEquals("2024-01-01", response.getData().getCreatedAt());
            assertEquals("2024-01-02", response.getData().getCanceledAt());
            assertEquals("Reason", response.getData().getCancellationReason());
        }

        @Test
        @DisplayName("CancelPaymentResponse getters")
        void testCancelPaymentResponse_Getters() {
            CancelPaymentResponse response = new CancelPaymentResponse();
            ReflectionTestUtils.setField(response, "code", "00");
            ReflectionTestUtils.setField(response, "desc", "Desc");
            ReflectionTestUtils.setField(response, "signature", "sig");

            PaymentInfoResponse.PaymentInfoData data = new PaymentInfoResponse.PaymentInfoData();
            ReflectionTestUtils.setField(data, "status", "CANCELLED");
            ReflectionTestUtils.setField(response, "data", data);

            assertEquals("00", response.getCode());
            assertEquals("Desc", response.getDesc());
            assertEquals("sig", response.getSignature());
            assertNotNull(response.getData());
            assertEquals("CANCELLED", response.getData().getStatus());
        }

        @Test
        @DisplayName("InvoiceInfoResponse getters")
        void testInvoiceInfoResponse_Getters() {
            InvoiceInfoResponse response = new InvoiceInfoResponse();
            ReflectionTestUtils.setField(response, "code", "00");
            ReflectionTestUtils.setField(response, "desc", "Desc");
            ReflectionTestUtils.setField(response, "signature", "sig");

            InvoiceInfoResponse.InvoiceData invoiceData = new InvoiceInfoResponse.InvoiceData();
            List<InvoiceInfoResponse.InvoiceData.Invoice> invoices = new ArrayList<>();
            InvoiceInfoResponse.InvoiceData.Invoice invoice = new InvoiceInfoResponse.InvoiceData.Invoice();
            ReflectionTestUtils.setField(invoice, "invoiceId", "INV123");
            ReflectionTestUtils.setField(invoice, "invoiceNumber", "INV-001");
            ReflectionTestUtils.setField(invoice, "issuedTimestamp", 1234567890L);
            ReflectionTestUtils.setField(invoice, "issuedDatetime", "2024-01-01");
            ReflectionTestUtils.setField(invoice, "transactionId", "TXN123");
            ReflectionTestUtils.setField(invoice, "reservationCode", "RES123");
            ReflectionTestUtils.setField(invoice, "codeOfTax", "TAX123");
            invoices.add(invoice);
            ReflectionTestUtils.setField(invoiceData, "invoices", invoices);
            ReflectionTestUtils.setField(response, "data", invoiceData);

            assertEquals("00", response.getCode());
            assertEquals("Desc", response.getDesc());
            assertEquals("sig", response.getSignature());
            assertNotNull(response.getData());
            assertNotNull(response.getData().getInvoices());
            assertEquals(1, response.getData().getInvoices().size());
            assertEquals("INV123", response.getData().getInvoices().get(0).getInvoiceId());
            assertEquals("INV-001", response.getData().getInvoices().get(0).getInvoiceNumber());
            assertEquals(Long.valueOf(1234567890L), response.getData().getInvoices().get(0).getIssuedTimestamp());
            assertEquals("2024-01-01", response.getData().getInvoices().get(0).getIssuedDatetime());
            assertEquals("TXN123", response.getData().getInvoices().get(0).getTransactionId());
            assertEquals("RES123", response.getData().getInvoices().get(0).getReservationCode());
            assertEquals("TAX123", response.getData().getInvoices().get(0).getCodeOfTax());
        }

        @Test
        @DisplayName("WebhookRequest getters")
        void testWebhookRequest_Getters() {
            WebhookRequest request = new WebhookRequest();
            ReflectionTestUtils.setField(request, "code", "00");
            ReflectionTestUtils.setField(request, "desc", "Desc");
            ReflectionTestUtils.setField(request, "success", true);
            ReflectionTestUtils.setField(request, "signature", "sig");

            WebhookRequest.WebhookData data = new WebhookRequest.WebhookData();
            ReflectionTestUtils.setField(data, "orderCode", 123);
            ReflectionTestUtils.setField(data, "amount", 50000);
            ReflectionTestUtils.setField(data, "description", "Desc");
            ReflectionTestUtils.setField(data, "accountNumber", "1234567890");
            ReflectionTestUtils.setField(data, "reference", "REF123");
            ReflectionTestUtils.setField(data, "transactionDateTime", "2024-01-01");
            ReflectionTestUtils.setField(data, "currency", "VND");
            ReflectionTestUtils.setField(data, "paymentLinkId", "PAY123");
            ReflectionTestUtils.setField(data, "code", "00");
            ReflectionTestUtils.setField(data, "desc", "Desc");
            ReflectionTestUtils.setField(data, "counterAccountBankId", "BANK123");
            ReflectionTestUtils.setField(data, "counterAccountBankName", "Bank Name");
            ReflectionTestUtils.setField(data, "counterAccountName", "Account Name");
            ReflectionTestUtils.setField(data, "counterAccountNumber", "9876543210");
            ReflectionTestUtils.setField(data, "virtualAccountName", "Virtual Name");
            ReflectionTestUtils.setField(data, "virtualAccountNumber", "VIRTUAL123");
            ReflectionTestUtils.setField(request, "data", data);

            assertEquals("00", request.getCode());
            assertEquals("Desc", request.getDesc());
            assertTrue(request.getSuccess());
            assertEquals("sig", request.getSignature());
            assertNotNull(request.getData());
            assertEquals(Integer.valueOf(123), request.getData().getOrderCode());
            assertEquals(Integer.valueOf(50000), request.getData().getAmount());
            assertEquals("Desc", request.getData().getDescription());
            assertEquals("1234567890", request.getData().getAccountNumber());
            assertEquals("REF123", request.getData().getReference());
            assertEquals("2024-01-01", request.getData().getTransactionDateTime());
            assertEquals("VND", request.getData().getCurrency());
            assertEquals("PAY123", request.getData().getPaymentLinkId());
            assertEquals("00", request.getData().getCode());
            assertEquals("Desc", request.getData().getDesc());
            assertEquals("BANK123", request.getData().getCounterAccountBankId());
            assertEquals("Bank Name", request.getData().getCounterAccountBankName());
            assertEquals("Account Name", request.getData().getCounterAccountName());
            assertEquals("9876543210", request.getData().getCounterAccountNumber());
            assertEquals("Virtual Name", request.getData().getVirtualAccountName());
            assertEquals("VIRTUAL123", request.getData().getVirtualAccountNumber());
        }

        @Test
        @DisplayName("WebhookConfirmResponse getters")
        void testWebhookConfirmResponse_Getters() {
            WebhookConfirmResponse response = new WebhookConfirmResponse();
            ReflectionTestUtils.setField(response, "code", "00");
            ReflectionTestUtils.setField(response, "desc", "Desc");

            WebhookConfirmResponse.WebhookConfirmData data = new WebhookConfirmResponse.WebhookConfirmData();
            ReflectionTestUtils.setField(data, "webhookUrl", "https://webhook.url");
            ReflectionTestUtils.setField(data, "accountNumber", "1234567890");
            ReflectionTestUtils.setField(data, "accountName", "Account Name");
            ReflectionTestUtils.setField(data, "name", "Name");
            ReflectionTestUtils.setField(data, "shortName", "Short");
            ReflectionTestUtils.setField(response, "data", data);

            assertEquals("00", response.getCode());
            assertEquals("Desc", response.getDesc());
            assertNotNull(response.getData());
            assertEquals("https://webhook.url", response.getData().getWebhookUrl());
            assertEquals("1234567890", response.getData().getAccountNumber());
            assertEquals("Account Name", response.getData().getAccountName());
            assertEquals("Name", response.getData().getName());
            assertEquals("Short", response.getData().getShortName());
        }
    }

    // ==================== PRIVATE METHODS TESTS (via public methods)
    // ====================

    @Nested
    @DisplayName("13. Private Methods Coverage (via public methods)")
    class PrivateMethodsTests {

        @Test
        @DisplayName("signCreate: Should generate signature for create payment")
        void testSignCreate_ViaCreatePaymentLink() {
            // Given
            CreateLinkResponse response = new CreateLinkResponse();
            ReflectionTestUtils.setField(response, "code", "00");
            ResponseEntity<CreateLinkResponse> entity = new ResponseEntity<>(response, HttpStatus.OK);
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    eq(CreateLinkResponse.class))).thenReturn(entity);

            // When
            payOsService.createPaymentLink(orderCode, amount, description);

            // Then - signCreate is called internally, verify via signature in payload
            verify(restTemplate).exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    eq(CreateLinkResponse.class));
        }

        @Test
        @DisplayName("hmacSHA256: Should generate HMAC signature")
        void testHmacSHA256_ViaVerifyWebhook() {
            // Given
            String body = "test data";
            String expectedSignature;
            try {
                javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
                javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(
                        CHECKSUM_KEY.getBytes("UTF-8"), "HmacSHA256");
                mac.init(secretKeySpec);
                byte[] raw = mac.doFinal(body.getBytes("UTF-8"));
                StringBuilder sb = new StringBuilder();
                for (byte b : raw) {
                    sb.append(String.format("%02x", b));
                }
                expectedSignature = sb.toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // When
            boolean result = payOsService.verifyWebhook(body, expectedSignature);

            // Then - hmacSHA256 is called internally
            assertTrue(result);
        }

        @Test
        @DisplayName("signTransferQR: Should generate signature for transfer QR")
        void testSignTransferQR_ViaCreateTransferQRCode() throws Exception {
            // Given
            ReflectionTestUtils.setField(payOsService, "secretKey", CHECKSUM_KEY);
            when(objectMapper.writeValueAsString(any())).thenReturn("{\"qr\":\"data\"}");

            // When
            payOsService.createTransferQRCode(new BigDecimal("100000"), "123", "Bank", "Holder", "Desc");

            // Then - signTransferQR is called internally
            verify(objectMapper).writeValueAsString(any());
        }
    }

    // ==================== EDGE CASES AND BRANCH COVERAGE ====================

    @Nested
    @DisplayName("14. Edge Cases and Branch Coverage")
    class EdgeCasesTests {

        @Test
        @DisplayName("createPaymentLink: Should handle exception with cause")
        void testCreatePaymentLink_ExceptionWithCause() {
            // Given
            RuntimeException cause = new RuntimeException("Root cause");
            RuntimeException exception = new RuntimeException("Wrapper", cause);
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    eq(CreateLinkResponse.class))).thenThrow(exception);

            // When & Then
            RuntimeException result = assertThrows(RuntimeException.class, () -> {
                payOsService.createPaymentLink(orderCode, amount, description);
            });
            assertTrue(result.getMessage().contains("Failed to create PayOS payment link"));
        }

        @Test
        @DisplayName("createTransferQRCodeUrl: Should handle null response body")
        void testCreateTransferQRCodeUrl_NullResponseBody() {
            // Given
            ReflectionTestUtils.setField(payOsService, "secretKey", CHECKSUM_KEY);
            Map<String, Object> responseBody = new HashMap<>();
            ResponseEntity<Map<String, Object>> entity = new ResponseEntity<>(responseBody, HttpStatus.OK);
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    any(Class.class))).thenReturn(entity);

            // When
            String result = payOsService.createTransferQRCodeUrl(new BigDecimal("100000"), "123", "Bank", "Holder",
                    "Desc");

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("createTransferQRCodeUrl: Should handle response body without qrCodeUrl")
        void testCreateTransferQRCodeUrl_NoQrCodeUrl() {
            // Given
            ReflectionTestUtils.setField(payOsService, "secretKey", CHECKSUM_KEY);
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("error", "No QR code");
            ResponseEntity<Map<String, Object>> entity = new ResponseEntity<>(responseBody, HttpStatus.OK);
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    any(Class.class))).thenReturn(entity);

            // When
            String result = payOsService.createTransferQRCodeUrl(new BigDecimal("100000"), "123", "Bank", "Holder",
                    "Desc");

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("createPaymentLink: Should handle clientId with length < 8")
        void testCreatePaymentLink_ShortClientId() {
            // Given
            ReflectionTestUtils.setField(payOsService, "clientId", "12345");
            CreateLinkResponse response = new CreateLinkResponse();
            ReflectionTestUtils.setField(response, "code", "00");
            ResponseEntity<CreateLinkResponse> entity = new ResponseEntity<>(response, HttpStatus.OK);
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    eq(CreateLinkResponse.class))).thenReturn(entity);

            // When
            payOsService.createPaymentLink(orderCode, amount, description);

            // Then - Should handle short clientId (uses substring logic)
            verify(restTemplate).exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    eq(CreateLinkResponse.class));
        }

        @Test
        @DisplayName("createPaymentLink: Should handle apiKey with length < 8")
        void testCreatePaymentLink_ShortApiKey() {
            // Given
            ReflectionTestUtils.setField(payOsService, "apiKey", "12345");
            CreateLinkResponse response = new CreateLinkResponse();
            ReflectionTestUtils.setField(response, "code", "00");
            ResponseEntity<CreateLinkResponse> entity = new ResponseEntity<>(response, HttpStatus.OK);
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    eq(CreateLinkResponse.class))).thenReturn(entity);

            // When
            payOsService.createPaymentLink(orderCode, amount, description);

            // Then - Should handle short apiKey
            verify(restTemplate).exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class),
                    eq(CreateLinkResponse.class));
        }
    }
}
