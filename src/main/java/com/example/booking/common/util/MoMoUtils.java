package com.example.booking.common.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for MoMo payment integration
 * Handles HMAC-SHA256 signature generation and validation
 */
public class MoMoUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(MoMoUtils.class);
    private static final String HMAC_SHA256 = "HmacSHA256";
    
    /**
     * Generate HMAC-SHA256 signature for MoMo API
     * @param data The data to sign
     * @param secretKey The secret key
     * @return Base64 encoded signature
     */
    public static String generateSignature(String data, String secretKey) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), HMAC_SHA256);
            mac.init(secretKeySpec);
            
            byte[] signatureBytes = mac.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(signatureBytes);
            
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Error generating MoMo signature", e);
            throw new RuntimeException("Failed to generate MoMo signature", e);
        }
    }
    
    /**
     * Verify HMAC-SHA256 signature from MoMo
     * @param data The original data
     * @param signature The signature to verify
     * @param secretKey The secret key
     * @return true if signature is valid
     */
    public static boolean verifySignature(String data, String signature, String secretKey) {
        try {
            String expectedSignature = generateSignature(data, secretKey);
            return expectedSignature.equals(signature);
        } catch (Exception e) {
            logger.error("Error verifying MoMo signature", e);
            return false;
        }
    }
    
    /**
     * Generate unique order ID for MoMo
     * Format: RESTAURANT_BOOKING_YYYYMMDDHHMMSS_UUID
     * @param bookingId The booking ID
     * @return Unique order ID
     */
    public static String generateOrderId(Integer bookingId) {
        String timestamp = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return String.format("RESTAURANT_BOOKING_%s_%s_%s", timestamp, bookingId, uuid);
    }
    
    /**
     * Generate unique request ID for MoMo
     * @return Unique request ID
     */
    public static String generateRequestId() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Build query string for MoMo signature
     * @param partnerCode Partner code
     * @param accessKey Access key
     * @param requestId Request ID
     * @param amount Amount
     * @param orderId Order ID
     * @param orderInfo Order info
     * @param returnUrl Return URL
     * @param notifyUrl Notify URL
     * @param extraData Extra data
     * @return Query string for signature
     */
    public static String buildQueryString(String partnerCode, String accessKey, String requestId,
                                        String amount, String orderId, String orderInfo,
                                        String returnUrl, String notifyUrl, String extraData) {
        
        StringBuilder query = new StringBuilder();
        query.append("accessKey=").append(accessKey);
        query.append("&amount=").append(amount);
        query.append("&extraData=").append(extraData != null ? extraData : "");
        query.append("&ipnUrl=").append(notifyUrl);
        query.append("&orderId=").append(orderId);
        query.append("&orderInfo=").append(orderInfo);
        query.append("&partnerCode=").append(partnerCode);
        query.append("&redirectUrl=").append(returnUrl);
        query.append("&requestId=").append(requestId);
        query.append("&requestType=captureWallet");
        
        return query.toString();
    }
    
    /**
     * Build query string for MoMo query API
     * @param partnerCode Partner code
     * @param accessKey Access key
     * @param requestId Request ID
     * @param orderId Order ID
     * @param lang Language
     * @return Query string for signature
     */
    public static String buildQueryString(String partnerCode, String accessKey, String requestId,
                                        String orderId, String lang) {
        
        StringBuilder query = new StringBuilder();
        query.append("accessKey=").append(accessKey);
        query.append("&orderId=").append(orderId);
        query.append("&partnerCode=").append(partnerCode);
        query.append("&requestId=").append(requestId);
        query.append("&lang=").append(lang);
        
        return query.toString();
    }
    
    /**
     * Validate MoMo result code
     * @param resultCode The result code from MoMo
     * @return true if result code indicates success
     */
    public static boolean isSuccessResultCode(String resultCode) {
        return "0".equals(resultCode);
    }
    
    /**
     * Get result code description
     * @param resultCode The result code
     * @return Description of the result code
     */
    public static String getResultCodeDescription(String resultCode) {
        return switch (resultCode) {
            case "0" -> "Thành công";
            case "1001" -> "Thiếu thông tin bắt buộc";
            case "1002" -> "Thông tin không hợp lệ";
            case "1003" -> "Giao dịch không tồn tại";
            case "1004" -> "Giao dịch đã được xử lý";
            case "1005" -> "Số tiền không hợp lệ";
            case "1006" -> "Đơn hàng đã tồn tại";
            case "1007" -> "Hết hạn giao dịch";
            case "1008" -> "Lỗi hệ thống";
            case "1009" -> "Thông tin đối tác không hợp lệ";
            case "1010" -> "Chữ ký không hợp lệ";
            default -> "Lỗi không xác định: " + resultCode;
        };
    }
    
    /**
     * Mask sensitive data for logging
     * @param data The data to mask
     * @return Masked data
     */
    public static String maskSensitiveData(String data) {
        if (data == null || data.length() <= 8) {
            return "***";
        }
        return data.substring(0, 4) + "***" + data.substring(data.length() - 4);
    }
    
    /**
     * Validate MoMo configuration
     * @param partnerCode Partner code
     * @param accessKey Access key
     * @param secretKey Secret key
     * @param endpoint Endpoint URL
     * @return true if configuration is valid
     */
    public static boolean validateConfig(String partnerCode, String accessKey, String secretKey, String endpoint) {
        return partnerCode != null && !partnerCode.trim().isEmpty() &&
               accessKey != null && !accessKey.trim().isEmpty() &&
               secretKey != null && !secretKey.trim().isEmpty() &&
               endpoint != null && !endpoint.trim().isEmpty();
    }
    
    /**
     * Verify MoMo response signature
     * @param response The MoMo response
     * @param accessKey The access key
     * @param secretKey The secret key
     * @return true if signature is valid
     */
    public static boolean verifyResponseSignature(com.example.booking.dto.MoMoCreateResponse response, String accessKey, String secretKey) {
        try {
            // Build query string for response signature verification
            // Format: accessKey=$accessKey&amount=$amount&orderId=$orderId&partnerCode=$partnerCode&payUrl=$payUrl&requestId=$requestId&responseTime=$responseTime&resultCode=$resultCode
            StringBuilder query = new StringBuilder();
            query.append("accessKey=").append(accessKey);
            query.append("&amount=").append(response.getAmount());
            query.append("&orderId=").append(response.getOrderId());
            query.append("&partnerCode=").append(response.getPartnerCode());
            query.append("&payUrl=").append(response.getPayUrl() != null ? response.getPayUrl() : "");
            query.append("&requestId=").append(response.getRequestId());
            query.append("&responseTime=").append(response.getResponseTime());
            query.append("&resultCode=").append(response.getResultCode());
            
            String expectedSignature = generateSignature(query.toString(), secretKey);
            return expectedSignature.equals(response.getSignature());
            
        } catch (Exception e) {
            logger.error("Error verifying MoMo response signature", e);
            return false;
        }
    }
}
