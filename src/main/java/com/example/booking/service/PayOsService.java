package com.example.booking.service;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
public class PayOsService {
    private static final Logger logger = LoggerFactory.getLogger(PayOsService.class);

    @Value("${payment.payos.client-id}")
    private String clientId; // reserved for future use
    
    @Value("${payment.payos.api-key}")
    private String apiKey;
    
    @Value("${payment.payos.checksum-key}")
    private String checksumKey;
    
    @Value("${payment.payos.endpoint}")
    private String endpoint;
    
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

    public CreateLinkResponse createPaymentLink(long orderCode, long amount, String description) {
        try {
            String signature = signCreate(amount, cancelUrl, description, orderCode, returnUrl);

            Map<String, Object> payload = new HashMap<>();
            payload.put("orderCode", orderCode);
            payload.put("amount", amount);
            payload.put("description", description);
            payload.put("cancelUrl", cancelUrl);
            payload.put("returnUrl", returnUrl);
            payload.put("expiredAt", Instant.now().plusSeconds(15 * 60).getEpochSecond());
            payload.put("signature", signature);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("x-client-idx-api-key", apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            String url = endpoint + "/v2/payment-requests";
            ResponseEntity<CreateLinkResponse> resp = restTemplate.exchange(
                URI.create(url), HttpMethod.POST, entity, CreateLinkResponse.class);
            return resp.getBody();
        } catch (Exception e) {
            logger.error("Failed to create PayOS payment link", e);
            throw new RuntimeException("Failed to create PayOS payment link", e);
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
}


