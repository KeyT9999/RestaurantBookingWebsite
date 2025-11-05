package com.example.booking.util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * PayOS Signature Generator Utility
 * Tạo signature HMAC-SHA256 cho PayOS API
 */
public class PayOSSignatureGenerator {
    
    private static final String HMAC_SHA256 = "HmacSHA256";
    
    /**
     * Tạo signature cho PayOS payment request
     * 
     * @param orderCode Order code
     * @param amount Amount in VND
     * @param description Description
     * @param cancelUrl Cancel URL
     * @param returnUrl Return URL
     * @param checksumKey PayOS checksum key
     * @return HMAC-SHA256 signature
     */
    public static String generateSignature(long orderCode, long amount, String description, 
                                         String cancelUrl, String returnUrl, String checksumKey) {
        
        // Tạo map và sort theo alphabet
        Map<String, String> params = new TreeMap<>();
        params.put("amount", String.valueOf(amount));
        params.put("cancelUrl", cancelUrl);
        params.put("description", description);
        params.put("orderCode", String.valueOf(orderCode));
        params.put("returnUrl", returnUrl);
        
        // Tạo string để ký
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }
        
        String dataToSign = sb.toString();
        System.out.println("Data to sign: " + dataToSign);
        
        // Tạo HMAC-SHA256 signature
        return generateHMAC(dataToSign, checksumKey);
    }
    
    /**
     * Tạo HMAC-SHA256 signature
     * 
     * @param data Data to sign
     * @param key Secret key
     * @return HMAC-SHA256 signature
     */
    private static String generateHMAC(String data, String key) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);
            
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error generating HMAC signature", e);
        }
    }
    
    /**
     * Test method để generate signature cho test
     */
    public static void main(String[] args) {
        // Test data - Replace with your actual values
        long orderCode = 20251007001L;
        long amount = 20000L;
        String description = "Test PayOS 20k - BookEat";
        String cancelUrl = "http://localhost:8080/payment/payos/cancel";
        String returnUrl = "http://localhost:8080/payment/payos/return";
        String checksumKey = "your_payos_checksum_key"; // Replace with your actual checksum key
        
        String signature = generateSignature(orderCode, amount, description, cancelUrl, returnUrl, checksumKey);
        
        System.out.println("=== PayOS Signature Generator ===");
        System.out.println("Order Code: " + orderCode);
        System.out.println("Amount: " + amount + " VND");
        System.out.println("Description: " + description);
        System.out.println("Cancel URL: " + cancelUrl);
        System.out.println("Return URL: " + returnUrl);
        System.out.println("Checksum Key: " + checksumKey);
        System.out.println("Generated Signature: " + signature);
        System.out.println("================================");
        
        // JSON payload
        System.out.println("\n=== JSON Payload for Postman ===");
        System.out.println("{");
        System.out.println("  \"orderCode\": " + orderCode + ",");
        System.out.println("  \"amount\": " + amount + ",");
        System.out.println("  \"description\": \"" + description + "\",");
        System.out.println("  \"cancelUrl\": \"" + cancelUrl + "\",");
        System.out.println("  \"returnUrl\": \"" + returnUrl + "\",");
        System.out.println("  \"signature\": \"" + signature + "\"");
        System.out.println("}");
        
        // cURL command
        System.out.println("\n=== cURL Command ===");
        System.out.println("curl -X POST 'https://api-merchant.payos.vn/v2/payment-requests' \\");
        System.out.println("  -H 'Content-Type: application/json' \\");
        System.out.println("  -H 'x-client-id: your_payos_client_id' \\");
        System.out.println("  -H 'x-api-key: your_payos_api_key' \\");
        System.out.println("  -d '{");
        System.out.println("    \"orderCode\": " + orderCode + ",");
        System.out.println("    \"amount\": " + amount + ",");
        System.out.println("    \"description\": \"" + description + "\",");
        System.out.println("    \"cancelUrl\": \"" + cancelUrl + "\",");
        System.out.println("    \"returnUrl\": \"" + returnUrl + "\",");
        System.out.println("    \"signature\": \"" + signature + "\"");
        System.out.println("  }'");
    }
}
