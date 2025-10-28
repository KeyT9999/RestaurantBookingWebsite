package com.example.booking.service;

import java.net.URI;
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

/**
 * Service để validate bank account và lấy tên chủ tài khoản
 */
@Service
public class BankAccountService {
    
    private static final Logger logger = LoggerFactory.getLogger(BankAccountService.class);
    
    @Value("${payos.client-id:}")
    private String payosClientId;
    
    @Value("${payos.api-key:}")
    private String payosApiKey;
    
    @Value("${payos.checksum-key:}")
    private String payosChecksumKey;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * Tự động lấy tên chủ tài khoản từ số tài khoản và mã ngân hàng
     */
    public String getAccountHolderName(String accountNumber, String bankCode) {
        try {
            logger.info("🔄 Getting account holder name for account: {}, bank: {}", accountNumber, bankCode);
            
            // Thử lấy qua PayOS API trước
            String accountHolderName = getAccountHolderNameViaPayOS(accountNumber, bankCode);
            if (accountHolderName != null && !accountHolderName.trim().isEmpty()) {
                logger.info("✅ Got account holder name via PayOS: {}", accountHolderName);
                return accountHolderName;
            }
            
            // Fallback: thử lấy qua API của ngân hàng
            accountHolderName = getAccountHolderNameViaBankAPI(accountNumber, bankCode);
            if (accountHolderName != null && !accountHolderName.trim().isEmpty()) {
                logger.info("✅ Got account holder name via Bank API: {}", accountHolderName);
                return accountHolderName;
            }
            
            logger.warn("❌ Could not get account holder name for account: {}, bank: {}", accountNumber, bankCode);
            return null;
            
        } catch (Exception e) {
            logger.error("❌ Error getting account holder name", e);
            return null;
        }
    }
    
    /**
     * Lấy tên chủ tài khoản qua PayOS API
     */
    private String getAccountHolderNameViaPayOS(String accountNumber, String bankCode) {
        try {
            String url = "https://api-merchant.payos.vn/v2/payment/validate-account";
            
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("accountNumber", accountNumber);
            requestData.put("bankCode", bankCode);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-client-id", payosClientId);
            headers.set("x-api-key", payosApiKey);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestData, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                URI.create(url), HttpMethod.POST, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                if ((Boolean) responseBody.get("success")) {
                    return (String) responseBody.get("accountHolderName");
                }
            }
            
        } catch (Exception e) {
            logger.warn("PayOS API not available or failed: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Lấy tên chủ tài khoản qua API của ngân hàng
     */
    private String getAccountHolderNameViaBankAPI(String accountNumber, String bankCode) {
        try {
            String url = getBankAPIUrl(bankCode);
            if (url == null) {
                return null;
            }
            
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("accountNumber", accountNumber);
            requestData.put("bankCode", bankCode);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestData, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                URI.create(url), HttpMethod.POST, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                if ((Boolean) responseBody.get("success")) {
                    return (String) responseBody.get("accountHolderName");
                }
            }
            
        } catch (Exception e) {
            logger.warn("Bank API not available or failed: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Lấy URL API của ngân hàng
     */
    private String getBankAPIUrl(String bankCode) {
        Map<String, String> bankApis = new HashMap<>();
        bankApis.put("970422", "https://api.mbbank.com.vn/v1/account/validate"); // MB Bank
        bankApis.put("970436", "https://api.vietcombank.com.vn/v1/account/validate"); // Vietcombank
        bankApis.put("970415", "https://api.techcombank.com.vn/v1/account/validate"); // Techcombank
        bankApis.put("970422", "https://api.vietinbank.com.vn/v1/account/validate"); // VietinBank
        bankApis.put("970416", "https://api.agribank.com.vn/v1/account/validate"); // Agribank
        bankApis.put("970423", "https://api.acb.com.vn/v1/account/validate"); // ACB
        bankApis.put("970427", "https://api.sacombank.com.vn/v1/account/validate"); // Sacombank
        bankApis.put("970418", "https://api.bidv.com.vn/v1/account/validate"); // BIDV
        
        return bankApis.get(bankCode);
    }
    
    /**
     * Validate bank account format
     */
    public boolean isValidBankAccount(String accountNumber, String bankCode) {
        if (accountNumber == null || bankCode == null) {
            return false;
        }
        
        // Kiểm tra độ dài số tài khoản (thường từ 8-15 số)
        if (accountNumber.length() < 8 || accountNumber.length() > 15) {
            return false;
        }
        
        // Kiểm tra chỉ chứa số
        if (!accountNumber.matches("\\d+")) {
            return false;
        }
        
        // Kiểm tra mã ngân hàng hợp lệ
        Map<String, String> validBankCodes = new HashMap<>();
        validBankCodes.put("970422", "MB Bank");
        validBankCodes.put("970436", "Vietcombank");
        validBankCodes.put("970415", "Techcombank");
        validBankCodes.put("970422", "VietinBank");
        validBankCodes.put("970416", "Agribank");
        validBankCodes.put("970423", "ACB");
        validBankCodes.put("970427", "Sacombank");
        validBankCodes.put("970418", "BIDV");
        
        return validBankCodes.containsKey(bankCode);
    }
    
    /**
     * Lấy tên ngân hàng từ mã ngân hàng
     */
    public String getBankName(String bankCode) {
        Map<String, String> bankNames = new HashMap<>();
        bankNames.put("970422", "MB Bank");
        bankNames.put("970436", "Vietcombank");
        bankNames.put("970415", "Techcombank");
        bankNames.put("970422", "VietinBank");
        bankNames.put("970416", "Agribank");
        bankNames.put("970423", "ACB");
        bankNames.put("970427", "Sacombank");
        bankNames.put("970418", "BIDV");
        
        return bankNames.get(bankCode);
    }
}
