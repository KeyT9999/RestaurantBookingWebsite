package com.example.booking.web.controller.api;

import java.security.Principal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.booking.common.api.ApiResponse;
import com.example.booking.common.enums.WithdrawalStatus;
import com.example.booking.dto.payout.CreateWithdrawalRequestDto;
import com.example.booking.dto.payout.RestaurantBalanceDto;
import com.example.booking.dto.payout.RestaurantBankAccountDto;
import com.example.booking.dto.payout.WithdrawalRequestDto;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.service.RestaurantBalanceService;
import com.example.booking.service.RestaurantBankAccountService;
import com.example.booking.service.WithdrawalService;

import jakarta.validation.Valid;

/**
 * REST API Controller cho Restaurant Withdrawal
 */
@RestController
@RequestMapping("/api/restaurant/withdrawal")
@PreAuthorize("hasRole('RESTAURANT_OWNER')")
public class RestaurantWithdrawalApiController {
    
    private static final Logger logger = LoggerFactory.getLogger(RestaurantWithdrawalApiController.class);
    
    private final RestaurantBalanceService balanceService;
    private final RestaurantBankAccountService bankAccountService;
    private final WithdrawalService withdrawalService;
    private final RestaurantProfileRepository restaurantRepository;
    
    public RestaurantWithdrawalApiController(
        RestaurantBalanceService balanceService,
        RestaurantBankAccountService bankAccountService,
        WithdrawalService withdrawalService,
        RestaurantProfileRepository restaurantRepository
    ) {
        this.balanceService = balanceService;
        this.bankAccountService = bankAccountService;
        this.withdrawalService = withdrawalService;
        this.restaurantRepository = restaurantRepository;
    }
    
    /**
     * Tạo yêu cầu rút tiền
     * POST /api/restaurant/withdrawal/request
     */
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<WithdrawalRequestDto>> createWithdrawalRequest(
        @Valid @RequestBody CreateWithdrawalRequestDto dto,
        Principal principal
    ) {
        try {
            Integer restaurantId = getRestaurantId(principal);
            logger.info("💸 Restaurant {} creating withdrawal request for {} VNĐ", restaurantId, dto.getAmount());
            
            WithdrawalRequestDto result = withdrawalService.createWithdrawal(restaurantId, dto);
            
            logger.info("✅ Created withdrawal request #{} for restaurant {}", result.getRequestId(), restaurantId);
            return ResponseEntity.ok(ApiResponse.success("Tạo yêu cầu rút tiền thành công", result));
            
        } catch (Exception e) {
            logger.error("❌ Error creating withdrawal request for restaurant", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Lấy số dư của restaurant
     * GET /api/restaurant/withdrawal/balance
     */
    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<RestaurantBalanceDto>> getBalance(Principal principal) {
        try {
            Integer restaurantId = getRestaurantId(principal);
            RestaurantBalanceDto balance = balanceService.getBalance(restaurantId);
            
            return ResponseEntity.ok(ApiResponse.success("Lấy số dư thành công", balance));
            
        } catch (Exception e) {
            logger.error("❌ Error getting balance for restaurant", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Lấy danh sách tài khoản ngân hàng
     * GET /api/restaurant/withdrawal/bank-accounts
     */
    @GetMapping("/bank-accounts")
    public ResponseEntity<ApiResponse<List<RestaurantBankAccountDto>>> getBankAccounts(Principal principal) {
        try {
            Integer restaurantId = getRestaurantId(principal);
            List<RestaurantBankAccountDto> bankAccounts = bankAccountService.getBankAccounts(restaurantId);
            
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tài khoản thành công", bankAccounts));
            
        } catch (Exception e) {
            logger.error("❌ Error getting bank accounts for restaurant", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Thêm tài khoản ngân hàng
     * POST /api/restaurant/withdrawal/bank-accounts
     */
    @PostMapping("/bank-accounts")
    public ResponseEntity<ApiResponse<RestaurantBankAccountDto>> addBankAccount(
        @Valid @RequestBody RestaurantBankAccountDto dto,
        Principal principal
    ) {
        try {
            Integer restaurantId = getRestaurantId(principal);
            logger.info("🏦 Restaurant {} adding bank account: {}", restaurantId, dto.getAccountNumber());
            
            RestaurantBankAccountDto result = bankAccountService.addBankAccount(restaurantId, dto);
            
            logger.info("✅ Added bank account #{} for restaurant {}", result.getAccountId(), restaurantId);
            return ResponseEntity.ok(ApiResponse.success("Thêm tài khoản ngân hàng thành công", result));
            
        } catch (Exception e) {
            logger.error("❌ Error adding bank account for restaurant", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Lấy danh sách yêu cầu rút tiền của restaurant
     * GET /api/restaurant/withdrawal/requests
     */
    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<Page<WithdrawalRequestDto>>> getWithdrawalRequests(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String status,
        Principal principal
    ) {
        try {
            Integer restaurantId = getRestaurantId(principal);
            
            Page<WithdrawalRequestDto> requests;
            if (status != null && !status.isEmpty() && !status.equals("ALL")) {
                WithdrawalStatus withdrawalStatus = WithdrawalStatus.valueOf(status);
                // Filter by restaurant and status
                requests = withdrawalService.getAllWithdrawals(Pageable.ofSize(size).withPage(page))
                    .map(w -> w.getRestaurantId().equals(restaurantId) ? w : null)
                    .map(w -> w != null && w.getStatus() == withdrawalStatus ? w : null);
            } else {
                // Filter by restaurant only
                requests = withdrawalService.getAllWithdrawals(Pageable.ofSize(size).withPage(page))
                    .map(w -> w.getRestaurantId().equals(restaurantId) ? w : null);
            }
            
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách yêu cầu thành công", requests));
            
        } catch (Exception e) {
            logger.error("❌ Error getting withdrawal requests for restaurant", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Lấy chi tiết yêu cầu rút tiền
     * GET /api/restaurant/withdrawal/requests/{requestId}
     */
    @GetMapping("/requests/{requestId}")
    public ResponseEntity<ApiResponse<WithdrawalRequestDto>> getWithdrawalRequest(
        @PathVariable Integer requestId,
        Principal principal
    ) {
        try {
            Integer restaurantId = getRestaurantId(principal);
            WithdrawalRequestDto request = withdrawalService.getWithdrawal(requestId);
            
            // Verify ownership
            if (!request.getRestaurantId().equals(restaurantId)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Không có quyền truy cập yêu cầu này"));
            }
            
            return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết yêu cầu thành công", request));
            
        } catch (Exception e) {
            logger.error("❌ Error getting withdrawal request for restaurant", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Helper method to get restaurant ID from principal
     */
    private Integer getRestaurantId(Principal principal) {
        String username = principal.getName();
        return restaurantRepository.findByOwnerUsername(username)
            .stream()
            .findFirst()
            .map(r -> r.getRestaurantId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà hàng với tài khoản: " + username));
    }
}
