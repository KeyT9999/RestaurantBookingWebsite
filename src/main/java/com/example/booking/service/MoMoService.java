package com.example.booking.service;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.booking.common.util.MoMoUtils;
import com.example.booking.domain.Payment;
import com.example.booking.domain.PaymentStatus;
import com.example.booking.dto.MoMoCreateRequest;
import com.example.booking.dto.MoMoCreateResponse;
import com.example.booking.dto.MoMoIpnRequest;
import com.example.booking.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service for MoMo payment integration
 * Handles MoMo API communication and payment processing
 */
@Service
public class MoMoService {
    
    private static final Logger logger = LoggerFactory.getLogger(MoMoService.class);
    
    // MoMo configuration
    @Value("${payment.momo.partner-code}")
    private String partnerCode;
    
    @Value("${payment.momo.access-key}")
    private String accessKey;
    
    @Value("${payment.momo.secret-key}")
    private String secretKey;
    
    @Value("${payment.momo.endpoint}")
    private String endpoint;
    
    @Value("${payment.momo.return-url}")
    private String returnUrl;
    
    @Value("${payment.momo.notify-url}")
    private String notifyUrl;
    
    // Dependencies
    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public MoMoService(PaymentRepository paymentRepository, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Create MoMo payment
     * @param request The MoMo create request
     * @param payment The payment entity
     * @return MoMo create response
     */
    public MoMoCreateResponse createPayment(MoMoCreateRequest request, Payment payment) {
        try {
            // Validate configuration
            if (!MoMoUtils.validateConfig(partnerCode, accessKey, secretKey, endpoint)) {
                throw new IllegalArgumentException("MoMo configuration is invalid");
            }
            
            // Generate order ID and request ID
            String orderId = MoMoUtils.generateOrderId(request.getBookingId());
            String requestId = MoMoUtils.generateRequestId();
            
            // Build query string for signature according to MoMo documentation
            String queryString = MoMoUtils.buildQueryString(
                partnerCode, accessKey, requestId,
                request.getAmount().toString(),
                orderId, request.getOrderInfo(),
                returnUrl, notifyUrl, request.getExtraData()
            );
            
            // Generate signature
            String signature = MoMoUtils.generateSignature(queryString, secretKey);
            
            // Build request payload according to MoMo API documentation
            MoMoCreateRequest payload = new MoMoCreateRequest();
            payload.setPartnerCode(partnerCode);
            payload.setRequestId(requestId);
            payload.setAmount(request.getAmount());
            payload.setOrderId(orderId);
            payload.setOrderInfo(request.getOrderInfo());
            payload.setRedirectUrl(returnUrl);
            payload.setIpnUrl(notifyUrl);
            payload.setRequestType("captureWallet");
            payload.setSignature(signature);
            payload.setExtraData(request.getExtraData() != null ? request.getExtraData() : "");
            payload.setLang("vi");
            payload.setAutoCapture(true);
            payload.setBookingId(request.getBookingId());
            payload.setVoucherCode(request.getVoucherCode());
            
            // Create HTTP headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Create HTTP entity
            HttpEntity<MoMoCreateRequest> entity = new HttpEntity<>(payload, headers);
            
            // Call MoMo API
            String createUrl = endpoint + "/v2/gateway/api/create";
            logger.info("Calling MoMo create API: {}", createUrl);
            logger.debug("MoMo payload: {}", MoMoUtils.maskSensitiveData(objectMapper.writeValueAsString(payload)));
            
            ResponseEntity<MoMoCreateResponse> response = restTemplate.exchange(
                URI.create(createUrl),
                HttpMethod.POST,
                entity,
                MoMoCreateResponse.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                MoMoCreateResponse momoResponse = response.getBody();
                
                // Verify response signature
                if (!MoMoUtils.verifyResponseSignature(momoResponse, accessKey, secretKey)) {
                    logger.error("Invalid MoMo response signature");
                    throw new RuntimeException("Invalid MoMo response signature");
                }
                
                // Check result code
                if (momoResponse.getResultCode() != null && momoResponse.getResultCode() == 0) {
                    // Success - update payment with MoMo data
                    payment.setMomoOrderId(orderId);
                    payment.setMomoRequestId(requestId);
                    payment.setPayUrl(momoResponse.getPayUrl());
                    payment.setStatus(PaymentStatus.PROCESSING);
                    paymentRepository.save(payment);
                    
                    logger.info("MoMo payment created successfully. OrderId: {}, PayUrl: {}", 
                        orderId, momoResponse.getPayUrl());
                    
                    return momoResponse;
                } else {
                    // Error - log and throw exception
                    String errorMsg = momoResponse.getMessage() != null ? momoResponse.getMessage() : "Unknown error";
                    logger.error("MoMo payment creation failed. ResultCode: {}, Message: {}", 
                        momoResponse.getResultCode(), errorMsg);
                    throw new RuntimeException("MoMo payment creation failed: " + errorMsg);
                }
            } else {
                throw new RuntimeException("Failed to create MoMo payment. Status: " + response.getStatusCode());
            }
            
        } catch (RestClientException e) {
            logger.error("Error calling MoMo API", e);
            throw new RuntimeException("Failed to create MoMo payment", e);
        } catch (Exception e) {
            logger.error("Error creating MoMo payment", e);
            throw new RuntimeException("Failed to create MoMo payment", e);
        }
    }
    
    /**
     * Handle MoMo IPN (Instant Payment Notification)
     * @param ipnRequest The IPN request from MoMo
     * @return true if IPN was processed successfully
     */
    public boolean handleIpn(MoMoIpnRequest ipnRequest) {
        try {
            logger.info("Processing MoMo IPN for orderId: {}", ipnRequest.getOrderId());
            
            // Find payment by order ID
            Optional<Payment> paymentOpt = paymentRepository.findByMomoOrderId(ipnRequest.getOrderId());
            if (paymentOpt.isEmpty()) {
                logger.error("Payment not found for orderId: {}", ipnRequest.getOrderId());
                return false;
            }
            
            Payment payment = paymentOpt.get();
            
            // Verify signature
            String queryString = buildIpnQueryString(ipnRequest);
            if (!MoMoUtils.verifySignature(queryString, ipnRequest.getSignature(), secretKey)) {
                logger.error("Invalid signature for orderId: {}", ipnRequest.getOrderId());
                return false;
            }
            
            // Check if already processed (idempotency)
            if (payment.getStatus() == PaymentStatus.COMPLETED) {
                logger.info("Payment already processed for orderId: {}", ipnRequest.getOrderId());
                return true;
            }
            
            // Process payment based on result code
            if (MoMoUtils.isSuccessResultCode(ipnRequest.getResultCode())) {
                // Payment successful
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setMomoTransId(ipnRequest.getTransId());
                payment.setMomoResultCode(ipnRequest.getResultCode());
                payment.setMomoMessage(ipnRequest.getMessage());
                payment.setPaidAt(LocalDateTime.now());
                
                // Save raw IPN data
                try {
                    payment.setIpnRaw(objectMapper.writeValueAsString(ipnRequest));
                } catch (Exception e) {
                    logger.warn("Failed to save IPN raw data", e);
                }
                
                paymentRepository.save(payment);
                
                logger.info("Payment completed successfully. OrderId: {}, TransId: {}", 
                    ipnRequest.getOrderId(), ipnRequest.getTransId());
                
                // TODO: Trigger booking confirmation
                // bookingService.confirmBooking(payment.getBooking().getBookingId());
                
            } else {
                // Payment failed
                payment.setStatus(PaymentStatus.FAILED);
                payment.setMomoResultCode(ipnRequest.getResultCode());
                payment.setMomoMessage(ipnRequest.getMessage());
                
                // Save raw IPN data
                try {
                    payment.setIpnRaw(objectMapper.writeValueAsString(ipnRequest));
                } catch (Exception e) {
                    logger.warn("Failed to save IPN raw data", e);
                }
                
                paymentRepository.save(payment);
                
                logger.info("Payment failed. OrderId: {}, ResultCode: {}, Message: {}", 
                    ipnRequest.getOrderId(), ipnRequest.getResultCode(), ipnRequest.getMessage());
            }
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error processing MoMo IPN", e);
            return false;
        }
    }
    
    /**
     * Query payment status from MoMo (fallback mechanism)
     * @param orderId The order ID
     * @return true if query was successful
     */
    public boolean queryPaymentStatus(String orderId) {
        try {
            logger.info("Querying MoMo payment status for orderId: {}", orderId);
            
            // Find payment
            Optional<Payment> paymentOpt = paymentRepository.findByMomoOrderId(orderId);
            if (paymentOpt.isEmpty()) {
                logger.error("Payment not found for orderId: {}", orderId);
                return false;
            }
            
            Payment payment = paymentOpt.get();
            
            // Generate request ID for query
            String requestId = MoMoUtils.generateRequestId();
            
            // Build query string for signature
            String queryString = MoMoUtils.buildQueryString(partnerCode, accessKey, requestId, orderId, "vi");
            String signature = MoMoUtils.generateSignature(queryString, secretKey);
            
            // Build query payload
            MoMoQueryRequest queryRequest = new MoMoQueryRequest();
            queryRequest.setPartnerCode(partnerCode);
            queryRequest.setAccessKey(accessKey);
            queryRequest.setRequestId(requestId);
            queryRequest.setOrderId(orderId);
            queryRequest.setLang("vi");
            queryRequest.setSignature(signature);
            
            // Create HTTP headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Create HTTP entity
            HttpEntity<MoMoQueryRequest> entity = new HttpEntity<>(queryRequest, headers);
            
            // Call MoMo query API
            String queryUrl = endpoint + "/v2/gateway/api/query";
            logger.info("Calling MoMo query API: {}", queryUrl);
            
            ResponseEntity<MoMoQueryResponse> response = restTemplate.exchange(
                URI.create(queryUrl),
                HttpMethod.POST,
                entity,
                MoMoQueryResponse.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                MoMoQueryResponse queryResponse = response.getBody();
                
                // Update payment status based on query result
                if (MoMoUtils.isSuccessResultCode(queryResponse.getResultCode())) {
                    payment.setStatus(PaymentStatus.COMPLETED);
                    payment.setMomoTransId(queryResponse.getTransId());
                    payment.setMomoResultCode(queryResponse.getResultCode());
                    payment.setMomoMessage(queryResponse.getMessage());
                    payment.setPaidAt(LocalDateTime.now());
                } else {
                    payment.setStatus(PaymentStatus.FAILED);
                    payment.setMomoResultCode(queryResponse.getResultCode());
                    payment.setMomoMessage(queryResponse.getMessage());
                }
                
                paymentRepository.save(payment);
                
                logger.info("Payment status updated from query. OrderId: {}, Status: {}", 
                    orderId, payment.getStatus());
                
                return true;
            } else {
                logger.error("Failed to query MoMo payment. Status: {}", response.getStatusCode());
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error querying MoMo payment status", e);
            return false;
        }
    }
    
    /**
     * Build query string for IPN signature verification
     */
    private String buildIpnQueryString(MoMoIpnRequest ipnRequest) {
        StringBuilder query = new StringBuilder();
        query.append("accessKey=").append(accessKey);
        query.append("&amount=").append(ipnRequest.getAmount());
        query.append("&extraData=").append(ipnRequest.getExtraData() != null ? ipnRequest.getExtraData() : "");
        query.append("&message=").append(ipnRequest.getMessage());
        query.append("&orderId=").append(ipnRequest.getOrderId());
        query.append("&orderInfo=").append(ipnRequest.getOrderInfo());
        query.append("&orderType=").append(ipnRequest.getOrderType());
        query.append("&partnerCode=").append(ipnRequest.getPartnerCode());
        query.append("&payType=").append(ipnRequest.getPayType());
        query.append("&requestId=").append(ipnRequest.getRequestId());
        query.append("&responseTime=").append(ipnRequest.getResponseTime());
        query.append("&resultCode=").append(ipnRequest.getResultCode());
        query.append("&transId=").append(ipnRequest.getTransId());
        
        return query.toString();
    }
    
    /**
     * Inner class for MoMo query request
     */
    public static class MoMoQueryRequest {
        private String partnerCode;
        private String accessKey;
        private String requestId;
        private String orderId;
        private String lang;
        private String signature;
        
        // Getters and setters
        public String getPartnerCode() { return partnerCode; }
        public void setPartnerCode(String partnerCode) { this.partnerCode = partnerCode; }
        
        public String getAccessKey() { return accessKey; }
        public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
        
        public String getRequestId() { return requestId; }
        public void setRequestId(String requestId) { this.requestId = requestId; }
        
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        
        public String getLang() { return lang; }
        public void setLang(String lang) { this.lang = lang; }
        
        public String getSignature() { return signature; }
        public void setSignature(String signature) { this.signature = signature; }
    }
    
    /**
     * Inner class for MoMo query response
     */
    public static class MoMoQueryResponse {
        private String partnerCode;
        private String orderId;
        private String requestId;
        private String amount;
        private String orderInfo;
        private String orderType;
        private String transId;
        private String resultCode;
        private String message;
        private String payType;
        private String responseTime;
        private String extraData;
        private String signature;
        
        // Getters and setters
        public String getPartnerCode() { return partnerCode; }
        public void setPartnerCode(String partnerCode) { this.partnerCode = partnerCode; }
        
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        
        public String getRequestId() { return requestId; }
        public void setRequestId(String requestId) { this.requestId = requestId; }
        
        public String getAmount() { return amount; }
        public void setAmount(String amount) { this.amount = amount; }
        
        public String getOrderInfo() { return orderInfo; }
        public void setOrderInfo(String orderInfo) { this.orderInfo = orderInfo; }
        
        public String getOrderType() { return orderType; }
        public void setOrderType(String orderType) { this.orderType = orderType; }
        
        public String getTransId() { return transId; }
        public void setTransId(String transId) { this.transId = transId; }
        
        public String getResultCode() { return resultCode; }
        public void setResultCode(String resultCode) { this.resultCode = resultCode; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getPayType() { return payType; }
        public void setPayType(String payType) { this.payType = payType; }
        
        public String getResponseTime() { return responseTime; }
        public void setResponseTime(String responseTime) { this.responseTime = responseTime; }
        
        public String getExtraData() { return extraData; }
        public void setExtraData(String extraData) { this.extraData = extraData; }
        
        public String getSignature() { return signature; }
        public void setSignature(String signature) { this.signature = signature; }
    }
}
