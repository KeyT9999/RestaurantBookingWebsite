package com.example.booking.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.example.booking.domain.BankDirectory;
import com.example.booking.dto.payout.BankInfoDto;
import com.example.booking.dto.vietqr.VietQRBanksResponse;
import com.example.booking.dto.vietqr.VietQRLookupRequest;
import com.example.booking.dto.vietqr.VietQRLookupResponse;
import com.example.booking.repository.BankDirectoryRepository;

/**
 * Service để tích hợp VietQR API
 * - Lấy danh sách ngân hàng
 * - Verify số tài khoản
 */
@Service
public class VietQRService {
    
    private static final Logger logger = LoggerFactory.getLogger(VietQRService.class);
    
    // Cache in-memory (fallback nếu DB chậm)
    private final Map<String, BankInfoDto> bankCache = new ConcurrentHashMap<>();
    private volatile long lastFetchTime = 0L;
    private static final long CACHE_DURATION_MS = 6 * 60 * 60 * 1000; // 6 hours
    
    @Value("${vietqr.banks-url:https://api.vietqr.io/v2/banks}")
    private String banksUrl;
    
    @Value("${vietqr.lookup-url:https://api.vietqr.io/v2/lookup}")
    private String lookupUrl;
    
    @Value("${vietqr.client-id:#{null}}")
    private String clientId;
    
    @Value("${vietqr.api-key:#{null}}")
    private String apiKey;
    
    private final RestTemplate restTemplate;
    private final BankDirectoryRepository bankDirectoryRepository;
    
    public VietQRService(RestTemplate restTemplate, BankDirectoryRepository bankDirectoryRepository) {
        this.restTemplate = restTemplate;
        this.bankDirectoryRepository = bankDirectoryRepository;
    }
    
    /**
     * Lấy danh sách ngân hàng (từ cache hoặc DB)
     */
    public Collection<BankInfoDto> listBanks() {
        // Try memory cache first
        if (isCacheValid()) {
            logger.debug("🔍 Returning banks from memory cache");
            return bankCache.values();
        }
        
        // Load from DB
        List<BankDirectory> banksFromDb = bankDirectoryRepository.findByIsActiveTrueOrderByShortNameAsc();
        
        if (!banksFromDb.isEmpty()) {
            logger.debug("🔍 Returning {} banks from database", banksFromDb.size());
            // Refresh memory cache
            bankCache.clear();
            banksFromDb.forEach(bank -> {
                bankCache.put(bank.getBin(), convertToDto(bank));
            });
            lastFetchTime = System.currentTimeMillis();
            return bankCache.values();
        }
        
        // If DB is empty, force sync
        logger.warn("⚠️ Bank directory empty, forcing sync from VietQR");
        syncBanksFromVietQR();
        
        return bankCache.values();
    }
    
    /**
     * Lấy bank theo BIN
     */
    public Optional<BankInfoDto> getBankByBin(String bin) {
        // Try memory cache
        if (isCacheValid() && bankCache.containsKey(bin)) {
            return Optional.of(bankCache.get(bin));
        }
        
        // Try DB
        return bankDirectoryRepository.findByBin(bin)
            .map(this::convertToDto);
    }
    
    /**
     * Sync danh sách ngân hàng từ VietQR API
     */
    @Transactional
    public void syncBanksFromVietQR() {
        try {
            logger.info("🔄 Syncing banks from VietQR API: {}", banksUrl);
            
            ResponseEntity<VietQRBanksResponse> response = restTemplate.getForEntity(
                banksUrl,
                VietQRBanksResponse.class
            );
            
            VietQRBanksResponse banksResponse = response.getBody();
            
            if (banksResponse != null && banksResponse.isSuccess()) {
                List<VietQRBanksResponse.VietQRBank> banks = banksResponse.getData();
                
                logger.info("✅ Received {} banks from VietQR", banks != null ? banks.size() : 0);
                
                if (banks != null) {
                    LocalDateTime now = LocalDateTime.now();
                    int updated = 0;
                    int created = 0;
                    
                    for (VietQRBanksResponse.VietQRBank vietQRBank : banks) {
                        Optional<BankDirectory> existing = bankDirectoryRepository.findByBin(vietQRBank.getBin());
                        
                        BankDirectory bank;
                        if (existing.isPresent()) {
                            bank = existing.get();
                            updated++;
                        } else {
                            bank = new BankDirectory();
                            created++;
                        }
                        
                        bank.setVietqrId(vietQRBank.getId());
                        bank.setBin(vietQRBank.getBin());
                        bank.setCode(vietQRBank.getCode());
                        bank.setName(vietQRBank.getName());
                        bank.setShortName(vietQRBank.getShortName());
                        bank.setLogoUrl(vietQRBank.getLogo());
                        bank.setTransferSupported(vietQRBank.getTransferSupported() == 1);
                        bank.setLookupSupported(vietQRBank.getLookupSupported() == 1);
                        bank.setIsActive(true);
                        bank.setLastSyncedAt(now);
                        
                        bankDirectoryRepository.save(bank);
                        
                        // Update memory cache
                        bankCache.put(bank.getBin(), convertToDto(bank));
                    }
                    
                    lastFetchTime = System.currentTimeMillis();
                    
                    logger.info("✅ Sync completed: {} created, {} updated", created, updated);
                }
            } else {
                logger.error("❌ VietQR API returned error: {}", 
                    banksResponse != null ? banksResponse.getDesc() : "null response");
            }
            
        } catch (Exception e) {
            logger.error("❌ Failed to sync banks from VietQR", e);
            throw new RuntimeException("Failed to sync banks: " + e.getMessage(), e);
        }
    }
    
    /**
     * Verify số tài khoản và lấy tên chủ tài khoản
     */
    public Optional<String> lookupAccountName(String bin, String accountNumber) {
        try {
            logger.info("🔍 Looking up account: bin={}, number={}", bin, maskAccountNumber(accountNumber));
            
            VietQRLookupRequest request = new VietQRLookupRequest(bin, accountNumber);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Add auth headers if configured
            if (clientId != null && !clientId.isEmpty()) {
                headers.set("x-client-id", clientId);
            }
            if (apiKey != null && !apiKey.isEmpty()) {
                headers.set("x-api-key", apiKey);
            }
            
            HttpEntity<VietQRLookupRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<VietQRLookupResponse> response = restTemplate.exchange(
                lookupUrl,
                HttpMethod.POST,
                entity,
                VietQRLookupResponse.class
            );
            
            VietQRLookupResponse lookupResponse = response.getBody();
            
            if (lookupResponse != null && lookupResponse.isSuccess()) {
                String accountName = lookupResponse.getData().getAccountName();
                logger.info("✅ Account lookup successful: {}", accountName);
                return Optional.of(accountName);
            } else {
                logger.warn("⚠️ Account lookup failed: {}", 
                    lookupResponse != null ? lookupResponse.getDesc() : "null response");
                return Optional.empty();
            }
            
        } catch (Exception e) {
            logger.error("❌ Failed to lookup account", e);
            return Optional.empty();
        }
    }
    
    /**
     * Check if memory cache is still valid
     */
    private boolean isCacheValid() {
        long now = System.currentTimeMillis();
        return (now - lastFetchTime) < CACHE_DURATION_MS && !bankCache.isEmpty();
    }
    
    /**
     * Convert entity to DTO
     */
    private BankInfoDto convertToDto(BankDirectory bank) {
        BankInfoDto dto = new BankInfoDto();
        dto.setBin(bank.getBin());
        dto.setCode(bank.getCode());
        dto.setName(bank.getName());
        dto.setShortName(bank.getShortName());
        dto.setLogoUrl(bank.getLogoUrl());
        dto.setTransferSupported(bank.getTransferSupported());
        dto.setLookupSupported(bank.getLookupSupported());
        return dto;
    }
    
    /**
     * Mask account number for logging
     */
    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 6) {
            return "***";
        }
        int len = accountNumber.length();
        return accountNumber.substring(0, 3) + "****" + accountNumber.substring(len - 3);
    }
}

