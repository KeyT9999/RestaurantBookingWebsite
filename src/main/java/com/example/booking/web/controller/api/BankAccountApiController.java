package com.example.booking.web.controller.api;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.booking.service.BankAccountService;

/**
 * API Controller cho bank account validation
 */
@RestController
@RequestMapping("/api")
public class BankAccountApiController {
    
    private static final Logger logger = LoggerFactory.getLogger(BankAccountApiController.class);
    
    @Autowired
    private BankAccountService bankAccountService;
    
    // Removed validate-bank-account API as it's not needed for QR refund
    
    /**
     * API để lấy danh sách ngân hàng từ VietQR
     */
    @GetMapping("/v2/banks")
    public ResponseEntity<?> getBanksFromVietQR() {
        try {
            // Call VietQR API to get banks list
            String url = "https://api.vietqr.io/v2/banks";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return ResponseEntity.ok(response.getBody());
            } else {
                // Fallback to hardcoded banks
                return getFallbackBanks();
            }
            
        } catch (Exception e) {
            logger.error("Error getting banks from VietQR", e);
            // Fallback to hardcoded banks
            return getFallbackBanks();
        }
    }
    
    /**
     * Fallback banks list
     */
    private ResponseEntity<?> getFallbackBanks() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("desc", "success");
        
        java.util.List<Map<String, Object>> banks = new java.util.ArrayList<>();
        
        // Add Vietnamese banks
        banks.add(createBank("970422", "MB Bank", "MB", true));
        banks.add(createBank("970436", "Vietcombank", "VCB", true));
        banks.add(createBank("970407", "Techcombank", "TCB", true));
        banks.add(createBank("970415", "VietinBank", "CTG", true));
        banks.add(createBank("970405", "Agribank", "VAB", true));
        banks.add(createBank("970416", "ACB", "ACB", true));
        banks.add(createBank("970403", "Sacombank", "STB", true));
        banks.add(createBank("970418", "BIDV", "BID", true));
        
        response.put("data", banks);
        return ResponseEntity.ok(response);
    }
    
    private Map<String, Object> createBank(String bin, String name, String shortName, boolean lookupSupported) {
        Map<String, Object> bank = new HashMap<>();
        bank.put("bin", bin);
        bank.put("name", name);
        bank.put("shortName", shortName);
        bank.put("logo", "");
        bank.put("transferSupported", true);
        bank.put("lookupSupported", lookupSupported);
        return bank;
    }
}
