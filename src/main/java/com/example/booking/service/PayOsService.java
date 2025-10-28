package com.example.booking.service;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PayOsService {
    private static final Logger logger = LoggerFactory.getLogger(PayOsService.class);

    @Value("${payment.payos.expiration-minutes:15}")
    private int expirationMinutes; // reserved for future use

    @Value("${payment.payos.client-id}")
    private String clientId;
    
    @Value("${payment.payos.api-key}")
    private String apiKey;
    
    @Value("${payment.payos.checksum-key}")
    private String checksumKey;
    
    @Value("${payment.payos.endpoint}")
    private String endpoint;
    
    // Alias fields for compatibility
    private String secretKey;
    private String baseUrl;

    @Value("${payment.payos.return-url}")
    private String returnUrl;
    
    @Value("${payment.payos.cancel-url}")
    private String cancelUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper; // reserved for logging/debugging

    public PayOsService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    // Initialize alias fields
    public void initAliasFields() {
        this.secretKey = this.checksumKey;
        this.baseUrl = this.endpoint;
    }

    public CreateLinkResponse createPaymentLink(long orderCode, long amount, String description) {
        return createPaymentLinkInternal(orderCode, amount, description, returnUrl, cancelUrl);
    }

    /**
     * Create PayOS payment link specifically for refunds with custom URLs
     */
    public CreateLinkResponse createRefundPaymentLink(long orderCode, long amount, String description) {
        // Use refund-specific URLs
        String refundReturnUrl = returnUrl.replace("/payment/payos/return", "/refund/payos/return");
        String refundCancelUrl = cancelUrl.replace("/payment/payos/cancel", "/refund/payos/cancel");

        logger.info("🔄 Creating PayOS refund payment link with custom URLs:");
        logger.info("   - Refund Return URL: {}", refundReturnUrl);
        logger.info("   - Refund Cancel URL: {}", refundCancelUrl);

        return createPaymentLinkInternal(orderCode, amount, description, refundReturnUrl, refundCancelUrl);
    }

    private CreateLinkResponse createPaymentLinkInternal(long orderCode, long amount, String description,
            String customReturnUrl, String customCancelUrl) {
        try {
            // Validate PayOS configuration first
            if (clientId == null || clientId.trim().isEmpty()) {
                logger.error("❌ PayOS ClientId is NULL or EMPTY!");
                logger.error("   - Check biến môi trường PAYOS_CLIENT_ID");
                throw new IllegalStateException("PayOS ClientId chưa được cấu hình. Vui lòng kiểm tra biến môi trường PAYOS_CLIENT_ID!");
            }
            
            if (apiKey == null || apiKey.trim().isEmpty()) {
                logger.error("❌ PayOS ApiKey is NULL or EMPTY!");
                logger.error("   - Check biến môi trường PAYOS_API_KEY");
                throw new IllegalStateException("PayOS ApiKey chưa được cấu hình. Vui lòng kiểm tra biến môi trường PAYOS_API_KEY!");
            }
            
            if (checksumKey == null || checksumKey.trim().isEmpty()) {
                logger.error("❌ PayOS ChecksumKey is NULL or EMPTY!");
                logger.error("   - Check biến môi trường PAYOS_CHECKSUM_KEY");
                throw new IllegalStateException("PayOS ChecksumKey chưa được cấu hình. Vui lòng kiểm tra biến môi trường PAYOS_CHECKSUM_KEY!");
            }
            
            logger.info("🔑 PayOS API Call - Preparing...");
            logger.info("   - Endpoint: {}", endpoint);
            logger.info("   - ClientId: {}... (length: {})", clientId.length() >= 8 ? clientId.substring(0, 8) : clientId, clientId.length());
            logger.info("   - ApiKey: {}... (length: {})", apiKey.length() >= 8 ? apiKey.substring(0, 8) : apiKey, apiKey.length());
            logger.info("   - OrderCode: {}", orderCode);
            logger.info("   - Amount: {}", amount);
            logger.info("   - Description: {}", description);
            logger.info("   - CancelUrl: {}", customCancelUrl);
            logger.info("   - ReturnUrl: {}", customReturnUrl);
            logger.info("   - ExpirationMinutes: {}", expirationMinutes);
            
            String signature = signCreate(amount, customCancelUrl, description, orderCode, customReturnUrl);
            logger.info("   - Signature: {}...", signature.substring(0, 16));

            Map<String, Object> payload = new HashMap<>();
            payload.put("orderCode", orderCode);
            payload.put("amount", amount);
            payload.put("description", description);
            payload.put("cancelUrl", customCancelUrl);
            payload.put("returnUrl", customReturnUrl);
            payload.put("expiredAt", Instant.now().plusSeconds(expirationMinutes * 60).getEpochSecond());
            payload.put("signature", signature);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("x-client-id", clientId);
            headers.add("x-api-key", apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            String url = endpoint + "/v2/payment-requests";
            
            logger.info("📡 Calling PayOS API: POST {}", url);
            ResponseEntity<CreateLinkResponse> resp = restTemplate.exchange(
                URI.create(url), HttpMethod.POST, entity, CreateLinkResponse.class);
            
            logger.info("✅ PayOS API Response: Status={}", resp.getStatusCode());
            CreateLinkResponse body = resp.getBody();
            if (body != null) {
                logger.info("   - Code: {}", body.getCode());
                logger.info("   - Desc: {}", body.getDesc());
                if (body.getData() != null) {
                    logger.info("   - CheckoutUrl: {}", body.getData().getCheckoutUrl());
                    logger.info("   - PaymentLinkId: {}", body.getData().getPaymentLinkId());
                }
            }
            
            return body;
        } catch (Exception e) {
            logger.error("❌ PayOS API Call FAILED!", e);
            logger.error("   - Exception: {}", e.getClass().getName());
            logger.error("   - Message: {}", e.getMessage());
            if (e.getCause() != null) {
                logger.error("   - Cause: {}", e.getCause().getMessage());
            }
            throw new RuntimeException("Failed to create PayOS payment link: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy thông tin link thanh toán từ PayOS
     */
    public PaymentInfoResponse getPaymentInfo(long orderCode) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("x-client-id", clientId);
            headers.add("x-api-key", apiKey);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            String url = endpoint + "/v2/payment-requests/" + orderCode;
            ResponseEntity<PaymentInfoResponse> resp = restTemplate.exchange(
                URI.create(url), HttpMethod.GET, entity, PaymentInfoResponse.class);
            return resp.getBody();
        } catch (Exception e) {
            logger.error("Failed to get PayOS payment info for orderCode: {}", orderCode, e);
            throw new RuntimeException("Failed to get PayOS payment info", e);
        }
    }

    /**
     * Hủy link thanh toán PayOS
     */
    public CancelPaymentResponse cancelPayment(long orderCode, String cancellationReason) {
        try {
            Map<String, Object> payload = new HashMap<>();
            if (cancellationReason != null && !cancellationReason.trim().isEmpty()) {
                payload.put("cancellationReason", cancellationReason);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("x-client-id", clientId);
            headers.add("x-api-key", apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            String url = endpoint + "/v2/payment-requests/" + orderCode + "/cancel";
            ResponseEntity<CancelPaymentResponse> resp = restTemplate.exchange(
                URI.create(url), HttpMethod.POST, entity, CancelPaymentResponse.class);
            return resp.getBody();
        } catch (Exception e) {
            logger.error("Failed to cancel PayOS payment for orderCode: {}", orderCode, e);
            throw new RuntimeException("Failed to cancel PayOS payment", e);
        }
    }

    /**
     * Lấy thông tin hóa đơn từ PayOS
     */
    public InvoiceInfoResponse getInvoiceInfo(long orderCode) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("x-client-id", clientId);
            headers.add("x-api-key", apiKey);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            String url = endpoint + "/v2/payment-requests/" + orderCode + "/invoices";
            ResponseEntity<InvoiceInfoResponse> resp = restTemplate.exchange(
                URI.create(url), HttpMethod.GET, entity, InvoiceInfoResponse.class);
            return resp.getBody();
        } catch (Exception e) {
            logger.error("Failed to get PayOS invoice info for orderCode: {}", orderCode, e);
            throw new RuntimeException("Failed to get PayOS invoice info", e);
        }
    }

    /**
     * Tải hóa đơn PDF từ PayOS
     */
    public byte[] downloadInvoice(long orderCode, String invoiceId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("x-api-key", apiKey);
            headers.add("x-client-id", clientId);
            headers.setAccept(List.of(MediaType.APPLICATION_PDF));

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            String url = endpoint + "/v2/payment-requests/" + orderCode + "/invoices/" + invoiceId + "/download";
            ResponseEntity<byte[]> resp = restTemplate.exchange(
                URI.create(url), HttpMethod.GET, entity, byte[].class);
            return resp.getBody();
        } catch (Exception e) {
            logger.error("Failed to download PayOS invoice for orderCode: {}, invoiceId: {}", orderCode, invoiceId, e);
            throw new RuntimeException("Failed to download PayOS invoice", e);
        }
    }

    /**
     * Confirm webhook URL với PayOS
     */
    public WebhookConfirmResponse confirmWebhook(String webhookUrl) {
        try {
            // Validate webhook URL
            if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
                throw new IllegalArgumentException("Webhook URL cannot be null or empty");
            }
            
            if (!webhookUrl.startsWith("http://") && !webhookUrl.startsWith("https://")) {
                throw new IllegalArgumentException("Webhook URL must start with http:// or https://");
            }
            
            logger.info("Confirming webhook URL with PayOS: {}", webhookUrl);
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("webhookUrl", webhookUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("x-client-id", clientId);
            headers.add("x-api-key", apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            String url = endpoint + "/confirm-webhook";
            
            logger.debug("Sending confirm webhook request to: {}", url);
            ResponseEntity<WebhookConfirmResponse> resp = restTemplate.exchange(
                URI.create(url), HttpMethod.POST, entity, WebhookConfirmResponse.class);
            
            WebhookConfirmResponse response = resp.getBody();
            if (response != null) {
                logger.info("Webhook confirmation response: code={}, desc={}", response.getCode(), response.getDesc());
            }
            
            return response;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid webhook URL: {}", webhookUrl, e);
            throw e;
        } catch (Exception e) {
            logger.error("Failed to confirm PayOS webhook URL: {}", webhookUrl, e);
            throw new RuntimeException("Failed to confirm PayOS webhook", e);
        }
    }


    public boolean verifyWebhook(String body, String signature) {
        try {
            String expected = hmacSHA256(body, checksumKey);
            return expected.equals(signature);
        } catch (Exception e) {
            logger.error("Verify webhook signature failed", e);
            return false;
        }
    }

    private String signCreate(long amount, String cancelUrl, String description, long orderCode, String returnUrl) throws Exception {
        String data = "amount=" + amount +
                "&cancelUrl=" + cancelUrl +
                "&description=" + description +
                "&orderCode=" + orderCode +
                "&returnUrl=" + returnUrl;
        return hmacSHA256(data, checksumKey);
    }
    
    private String signRefund(Long orderCode, Long amount, String reason) throws Exception {
        String data = "orderCode=" + orderCode +
                "&amount=" + amount +
                "&reason=" + (reason != null ? reason : "");
        return hmacSHA256(data, checksumKey);
    }

    private static String hmacSHA256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256"));
        byte[] raw = mac.doFinal(data.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : raw) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CreateLinkResponse {
        private String code;
        private String desc;
        private Data data;
        private String signature;

        public String getCode() { return code; }
        public String getDesc() { return desc; }
        public Data getData() { return data; }
        public String getSignature() { return signature; }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Data {
            private String paymentLinkId;
            private Long amount;
            private String description;
            private Long orderCode;
            private String checkoutUrl;
            private String qrCode;

            public String getPaymentLinkId() { return paymentLinkId; }
            public Long getAmount() { return amount; }
            public String getDescription() { return description; }
            public Long getOrderCode() { return orderCode; }
            public String getCheckoutUrl() { return checkoutUrl; }
            public String getQrCode() { return qrCode; }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaymentInfoResponse {
        private String code;
        private String desc;
        private PaymentInfoData data;
        private String signature;

        public String getCode() { return code; }
        public String getDesc() { return desc; }
        public PaymentInfoData getData() { return data; }
        public String getSignature() { return signature; }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class PaymentInfoData {
            private String id;
            private Long orderCode;
            private Long amount;
            private Long amountPaid;
            private Long amountRemaining;
            private String status;
            private String createdAt;
            private String canceledAt;
            private String cancellationReason;

            public String getId() { return id; }
            public Long getOrderCode() { return orderCode; }
            public Long getAmount() { return amount; }
            public Long getAmountPaid() { return amountPaid; }
            public Long getAmountRemaining() { return amountRemaining; }
            public String getStatus() { return status; }
            public String getCreatedAt() { return createdAt; }
            public String getCanceledAt() { return canceledAt; }
            public String getCancellationReason() { return cancellationReason; }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CancelPaymentResponse {
        private String code;
        private String desc;
        private PaymentInfoResponse.PaymentInfoData data;
        private String signature;

        public String getCode() { return code; }
        public String getDesc() { return desc; }
        public PaymentInfoResponse.PaymentInfoData getData() { return data; }
        public String getSignature() { return signature; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InvoiceInfoResponse {
        private String code;
        private String desc;
        private InvoiceData data;
        private String signature;

        public String getCode() { return code; }
        public String getDesc() { return desc; }
        public InvoiceData getData() { return data; }
        public String getSignature() { return signature; }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class InvoiceData {
            private List<Invoice> invoices;

            public List<Invoice> getInvoices() { return invoices; }

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Invoice {
                private String invoiceId;
                private String invoiceNumber;
                private Long issuedTimestamp;
                private String issuedDatetime;
                private String transactionId;
                private String reservationCode;
                private String codeOfTax;

                public String getInvoiceId() { return invoiceId; }
                public String getInvoiceNumber() { return invoiceNumber; }
                public Long getIssuedTimestamp() { return issuedTimestamp; }
                public String getIssuedDatetime() { return issuedDatetime; }
                public String getTransactionId() { return transactionId; }
                public String getReservationCode() { return reservationCode; }
                public String getCodeOfTax() { return codeOfTax; }
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WebhookRequest {
        private String code;
        private String desc;
        private Boolean success;
        private WebhookData data;
        private String signature;

        public String getCode() { return code; }
        public String getDesc() { return desc; }
        public Boolean getSuccess() { return success; }
        public WebhookData getData() { return data; }
        public String getSignature() { return signature; }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class WebhookData {
            private Integer orderCode;
            private Integer amount;
            private String description;
            private String accountNumber;
            private String reference;
            private String transactionDateTime;
            private String currency;
            private String paymentLinkId;
            private String code;
            private String desc;
            private String counterAccountBankId;
            private String counterAccountBankName;
            private String counterAccountName;
            private String counterAccountNumber;
            private String virtualAccountName;
            private String virtualAccountNumber;

            public Integer getOrderCode() { return orderCode; }
            public Integer getAmount() { return amount; }
            public String getDescription() { return description; }
            public String getAccountNumber() { return accountNumber; }
            public String getReference() { return reference; }
            public String getTransactionDateTime() { return transactionDateTime; }
            public String getCurrency() { return currency; }
            public String getPaymentLinkId() { return paymentLinkId; }
            public String getCode() { return code; }
            public String getDesc() { return desc; }
            public String getCounterAccountBankId() { return counterAccountBankId; }
            public String getCounterAccountBankName() { return counterAccountBankName; }
            public String getCounterAccountName() { return counterAccountName; }
            public String getCounterAccountNumber() { return counterAccountNumber; }
            public String getVirtualAccountName() { return virtualAccountName; }
            public String getVirtualAccountNumber() { return virtualAccountNumber; }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WebhookConfirmResponse {
        private String code;
        private String desc;
        private WebhookConfirmData data;

        public String getCode() { return code; }
        public String getDesc() { return desc; }
        public WebhookConfirmData getData() { return data; }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class WebhookConfirmData {
            private String webhookUrl;
            private String accountNumber;
            private String accountName;
            private String name;
            private String shortName;

            public String getWebhookUrl() { return webhookUrl; }
            public String getAccountNumber() { return accountNumber; }
            public String getAccountName() { return accountName; }
            public String getName() { return name; }
            public String getShortName() { return shortName; }
        }
    }

    /**
     * Tạo QR code chuyển tiền cho refund
     */
    public String createTransferQRCode(BigDecimal amount, String bankAccount, String bankName, String accountHolder,
            String description) {
        try {
            logger.info("🔄 Creating transfer QR code for refund");
            logger.info("   Amount: {}", amount);
            logger.info("   Bank Account: {}", bankAccount);
            logger.info("   Bank Name: {}", bankName);
            logger.info("   Account Holder: {}", accountHolder);

            // Tạo dữ liệu QR code theo format PayOS
            Map<String, Object> qrData = new HashMap<>();
            qrData.put("type", "transfer");
            qrData.put("amount", amount.toString());
            qrData.put("bankAccount", bankAccount);
            qrData.put("bankName", bankName);
            qrData.put("accountHolder", accountHolder);
            qrData.put("description", description);
            qrData.put("timestamp", System.currentTimeMillis());

            // Tạo signature cho QR code
            String dataString = String.format("%s|%s|%s|%s|%s",
                    amount.toString(), bankAccount, bankName, accountHolder, description);
            String signature = signTransferQR(dataString);
            qrData.put("signature", signature);

            // Chuyển đổi thành JSON string
            String qrCodeData = objectMapper.writeValueAsString(qrData);

            logger.info("✅ Transfer QR code created successfully");
            return qrCodeData;

        } catch (Exception e) {
            logger.error("❌ Error creating transfer QR code", e);
            return null;
        }
    }

    /**
     * Tạo signature cho transfer QR code
     */
    private String signTransferQR(String data) {
        try {
            // Sử dụng HMAC SHA256 với secret key
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);

            byte[] signature = mac.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(signature);

        } catch (Exception e) {
            logger.error("Error signing transfer QR data", e);
            return null;
        }
    }

    /**
     * Tạo URL QR code từ PayOS (nếu có API)
     */
    public String createTransferQRCodeUrl(BigDecimal amount, String bankAccount, String bankName, String accountHolder,
            String description) {
        try {
            logger.info("🔄 Creating transfer QR code URL from PayOS");

            // Tạo dữ liệu cho PayOS API
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("amount", amount.multiply(new BigDecimal("100")).intValue()); // PayOS sử dụng đơn vị nhỏ
                                                                                          // nhất
            requestData.put("description", description);
            requestData.put("bankAccount", bankAccount);
            requestData.put("bankName", bankName);
            requestData.put("accountHolder", accountHolder);

            // Gọi PayOS API để tạo QR code
            String url = baseUrl + "/v2/payment/transfer-qr";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + secretKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestData, headers);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    URI.create(url), HttpMethod.POST, entity,
                    (Class<Map<String, Object>>) (Class<?>) Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null) {
                    String qrCodeUrl = (String) responseBody.get("qrCodeUrl");
                    logger.info("✅ Transfer QR code URL created: {}", qrCodeUrl);
                    return qrCodeUrl;
                }
            }
            logger.error("❌ PayOS API returned error: {}", response.getBody());
            return null;

        } catch (Exception e) {
            logger.error("❌ Error creating transfer QR code URL", e);
            // Fallback: tạo QR code data thay vì URL
            return createTransferQRCode(amount, bankAccount, bankName, accountHolder, description);
        }
    }
}


