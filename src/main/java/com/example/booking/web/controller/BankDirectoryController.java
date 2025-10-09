package com.example.booking.web.controller;

import java.util.Collection;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.booking.common.api.ApiResponse;
import com.example.booking.dto.payout.AccountLookupDto;
import com.example.booking.dto.payout.BankInfoDto;
import com.example.booking.service.VietQRService;

import jakarta.validation.Valid;

/**
 * Controller để lấy danh sách ngân hàng và verify account
 */
@RestController
@RequestMapping("/api/banks")
public class BankDirectoryController {
    
    private static final Logger logger = LoggerFactory.getLogger(BankDirectoryController.class);
    
    private final VietQRService vietQRService;
    
    public BankDirectoryController(VietQRService vietQRService) {
        this.vietQRService = vietQRService;
    }
    
    /**
     * Lấy danh sách tất cả ngân hàng
     * GET /api/banks
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Collection<BankInfoDto>>> listBanks() {
        try {
            Collection<BankInfoDto> banks = vietQRService.listBanks();
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách ngân hàng thành công", banks));
        } catch (Exception e) {
            logger.error("Error listing banks", e);
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        }
    }
    
    /**
     * Lấy thông tin ngân hàng theo BIN
     * GET /api/banks/{bin}
     */
    @GetMapping("/{bin}")
    public ResponseEntity<ApiResponse<BankInfoDto>> getBankByBin(@PathVariable String bin) {
        try {
            Optional<BankInfoDto> bank = vietQRService.getBankByBin(bin);
            
            if (bank.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("Lấy thông tin ngân hàng thành công", bank.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error getting bank by BIN", e);
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        }
    }
    
    /**
     * Verify số tài khoản và lấy tên chủ tài khoản
     * POST /api/banks/lookup
     */
    @PostMapping("/lookup")
    public ResponseEntity<ApiResponse<String>> lookupAccount(@Valid @RequestBody AccountLookupDto dto) {
        try {
            Optional<String> accountName = vietQRService.lookupAccountName(dto.getBin(), dto.getAccountNumber());
            
            if (accountName.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("Xác thực tài khoản thành công", accountName.get()));
            } else {
                return ResponseEntity.badRequest().body(
                    ApiResponse.error("Không thể xác thực tài khoản. Vui lòng kiểm tra lại số tài khoản")
                );
            }
        } catch (Exception e) {
            logger.error("Error looking up account", e);
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Lỗi khi xác thực tài khoản: " + e.getMessage())
            );
        }
    }
    
    /**
     * Sync danh sách ngân hàng từ VietQR (Admin only)
     * POST /api/banks/sync
     */
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<Void>> syncBanks() {
        try {
            vietQRService.syncBanksFromVietQR();
            return ResponseEntity.ok(ApiResponse.success("Đồng bộ danh sách ngân hàng thành công"));
        } catch (Exception e) {
            logger.error("Error syncing banks", e);
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Lỗi khi đồng bộ: " + e.getMessage())
            );
        }
    }
}

