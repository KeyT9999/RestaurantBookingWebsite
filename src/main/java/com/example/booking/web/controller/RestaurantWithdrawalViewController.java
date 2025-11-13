package com.example.booking.web.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booking.common.enums.WithdrawalStatus;
import com.example.booking.dto.payout.CreateWithdrawalRequestDto;
import com.example.booking.dto.payout.RestaurantBalanceDto;
import com.example.booking.dto.payout.RestaurantBankAccountDto;
import com.example.booking.dto.payout.WithdrawalRequestDto;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.service.RestaurantBalanceService;
import com.example.booking.service.RestaurantBankAccountService;
import com.example.booking.service.WithdrawalService;

/**
 * Controller để serve HTML pages cho restaurant withdrawal management
 */
@Controller
@RequestMapping("/restaurant-owner/withdrawal")
@PreAuthorize("hasRole('RESTAURANT_OWNER')")
public class RestaurantWithdrawalViewController {
    
    private static final Logger logger = LoggerFactory.getLogger(RestaurantWithdrawalViewController.class);
    
    private final RestaurantBalanceService balanceService;
    private final RestaurantBankAccountService bankAccountService;
    private final WithdrawalService withdrawalService;
    private final RestaurantProfileRepository restaurantRepository;
    
    public RestaurantWithdrawalViewController(
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
     * Hiển thị trang quản lý withdrawal của restaurant
     * GET /restaurant-owner/withdrawal
     */
    @GetMapping
    public String withdrawalManagement(
        @RequestParam(required = false) String status,
        @RequestParam(value = "restaurantId", required = false) Integer restaurantId,
        Principal principal,
        Model model
    ) {
        try {
            // Lấy danh sách restaurants của owner
            List<com.example.booking.domain.RestaurantProfile> restaurants = getAllRestaurantsByOwner(principal);
            
            if (restaurants.isEmpty()) {
                model.addAttribute("error", "Không tìm thấy nhà hàng nào của bạn. Vui lòng tạo nhà hàng trước.");
                model.addAttribute("restaurants", new ArrayList<>());
                model.addAttribute("balance", createEmptyBalance());
                model.addAttribute("bankAccounts", new ArrayList<>());
                model.addAttribute("withdrawals", new ArrayList<>());
                return "restaurant-owner/withdrawal-management";
            }
            
            // Xác định restaurant ID để hiển thị
            Integer finalRestaurantId;
            if (restaurantId == null) {
                // Dùng restaurant đầu tiên làm mặc định
                finalRestaurantId = restaurants.get(0).getRestaurantId();
            } else {
                // Kiểm tra restaurant có thuộc owner không
                boolean restaurantBelongsToUser = restaurants.stream()
                    .anyMatch(r -> r.getRestaurantId().equals(restaurantId));
                
                if (!restaurantBelongsToUser) {
                    logger.warn("⚠️ User tried to access restaurant {} they don't own", restaurantId);
                    finalRestaurantId = restaurants.get(0).getRestaurantId();
                } else {
                    finalRestaurantId = restaurantId;
                }
            }
            
            // Tìm restaurant được chọn
            com.example.booking.domain.RestaurantProfile selectedRestaurant = restaurants.stream()
                .filter(r -> r.getRestaurantId().equals(finalRestaurantId))
                .findFirst()
                .orElse(restaurants.get(0));
            
            logger.info("🏪 Restaurant owner accessing withdrawal page for restaurant {}", finalRestaurantId);
            
            // Lấy số dư
            RestaurantBalanceDto balance = balanceService.getBalance(finalRestaurantId);
            model.addAttribute("balance", balance);
            logger.debug("💰 Restaurant {} balance: {} VNĐ", finalRestaurantId, balance.getAvailableBalance());
            
            // Lấy danh sách tài khoản ngân hàng
            List<RestaurantBankAccountDto> bankAccounts = bankAccountService.getBankAccounts(finalRestaurantId);
            model.addAttribute("bankAccounts", bankAccounts);
            logger.debug("🏦 Restaurant {} has {} bank accounts", finalRestaurantId, bankAccounts.size());
            
            // Lấy lịch sử withdrawal
            List<WithdrawalRequestDto> withdrawals;
            if (status != null && !status.isEmpty() && !status.equals("ALL")) {
                WithdrawalStatus withdrawalStatus = WithdrawalStatus.valueOf(status);
                withdrawals = withdrawalService.getWithdrawalsByStatus(withdrawalStatus)
                    .stream()
                    .filter(w -> w.getRestaurantId().equals(finalRestaurantId))
                    .collect(java.util.stream.Collectors.toList());
                model.addAttribute("filter", status);
            } else {
                withdrawals = withdrawalService.getAllWithdrawals(Pageable.unpaged()).getContent()
                    .stream()
                    .filter(w -> w.getRestaurantId().equals(finalRestaurantId))
                    .collect(java.util.stream.Collectors.toList());
                model.addAttribute("filter", "ALL");
            }
            
            // Thêm dữ liệu vào model
            model.addAttribute("restaurants", restaurants);
            model.addAttribute("selectedRestaurant", selectedRestaurant);
            model.addAttribute("restaurantId", finalRestaurantId);
            model.addAttribute("restaurantName", selectedRestaurant.getRestaurantName());
            model.addAttribute("withdrawals", withdrawals);
            logger.debug("📜 Restaurant {} has {} withdrawal requests", finalRestaurantId, withdrawals.size());
            
            return "restaurant-owner/withdrawal-management";
            
        } catch (Exception e) {
            logger.error("❌ Error loading withdrawal page for restaurant", e);
            model.addAttribute("error", "Lỗi khi tải dữ liệu: " + e.getMessage());
            // Return with empty data to avoid template errors
            model.addAttribute("balance", createEmptyBalance());
            model.addAttribute("bankAccounts", new ArrayList<>());
            model.addAttribute("withdrawals", new ArrayList<>());
            model.addAttribute("filter", "ALL");
            return "restaurant-owner/withdrawal-management";
        }
    }
    
    /**
     * Tạo yêu cầu rút tiền
     * POST /restaurant-owner/withdrawal/request
     */
    @PostMapping("/request")
    public String createWithdrawalRequest(
        @RequestParam String amount,
        @RequestParam Integer bankAccountId,
        @RequestParam(required = false) String description,
        @RequestParam(value = "restaurantId", required = false) Integer restaurantId,
        Principal principal,
        RedirectAttributes redirectAttributes
    ) {
        try {
            if (restaurantId == null) {
                restaurantId = getRestaurantId(principal);
            }
            logger.info("💸 Restaurant {} creating withdrawal request for {} VNĐ", restaurantId, amount);
            
            // Tạo DTO
            CreateWithdrawalRequestDto dto = new CreateWithdrawalRequestDto();
            dto.setAmount(new java.math.BigDecimal(amount));
            dto.setBankAccountId(bankAccountId);
            dto.setDescription(description != null && !description.trim().isEmpty() 
                ? description 
                : "Rút tiền về tài khoản ngân hàng");
            
            // Tạo withdrawal request
            WithdrawalRequestDto result = withdrawalService.createWithdrawal(restaurantId, dto);
            
            redirectAttributes.addFlashAttribute("success", 
                "Đã tạo yêu cầu rút tiền #" + result.getRequestId() + " thành công! Vui lòng chờ admin duyệt.");
            logger.info("✅ Created withdrawal request #{} for restaurant {}", result.getRequestId(), restaurantId);
            
        } catch (Exception e) {
            logger.error("❌ Error creating withdrawal request for restaurant", e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tạo yêu cầu rút tiền: " + e.getMessage());
        }
        
        String redirectUrl = restaurantId != null 
            ? "redirect:/restaurant-owner/withdrawal?restaurantId=" + restaurantId
            : "redirect:/restaurant-owner/withdrawal";
        return redirectUrl;
    }
    
    /**
     * Force fix balance từ withdrawal requests thực tế
     * GET /restaurant-owner/withdrawal/fix-balance
     */
    @GetMapping("/fix-balance")
    public String fixBalance(
        @RequestParam(value = "restaurantId", required = false) Integer restaurantId,
        Principal principal,
        RedirectAttributes redirectAttributes
    ) {
        try {
            if (restaurantId == null) {
                restaurantId = getRestaurantId(principal);
            }
            logger.info("🔧 Force fixing balance for restaurant {}", restaurantId);
            
            // Gọi method fix balance từ withdrawal requests
            balanceService.fixBalanceFromWithdrawals(restaurantId);
            
            redirectAttributes.addFlashAttribute("success", "Đã sửa lại số dư từ dữ liệu thực tế!");
            logger.info("✅ Fixed balance for restaurant {}", restaurantId);
            
        } catch (Exception e) {
            logger.error("❌ Error fixing balance", e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi sửa số dư: " + e.getMessage());
        }
        
        String redirectUrl = restaurantId != null 
            ? "redirect:/restaurant-owner/withdrawal?restaurantId=" + restaurantId
            : "redirect:/restaurant-owner/withdrawal";
        return redirectUrl;
    }
    
    /**
     * Force recalculate balance từ database
     * GET /restaurant-owner/withdrawal/recalculate-balance
     */
    @GetMapping("/recalculate-balance")
    public String recalculateBalance(
        @RequestParam(value = "restaurantId", required = false) Integer restaurantId,
        Principal principal,
        RedirectAttributes redirectAttributes
    ) {
        try {
            if (restaurantId == null) {
                restaurantId = getRestaurantId(principal);
            }
            logger.info("🔄 Force recalculating balance for restaurant {}", restaurantId);
            
            // Gọi database function để recalculate
            balanceService.recalculateAll();
            
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật lại số dư từ database!");
            logger.info("✅ Recalculated balance for restaurant {}", restaurantId);
            
        } catch (Exception e) {
            logger.error("❌ Error recalculating balance", e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật số dư: " + e.getMessage());
        }
        
        String redirectUrl = restaurantId != null 
            ? "redirect:/restaurant-owner/withdrawal?restaurantId=" + restaurantId
            : "redirect:/restaurant-owner/withdrawal";
        return redirectUrl;
    }
    
    /**
     * Thêm tài khoản ngân hàng
     * POST /restaurant-owner/bank-accounts
     */
    @PostMapping("/bank-accounts")
    public String addBankAccount(
        @RequestParam String bankCode,
        @RequestParam String accountNumber,
        @RequestParam String accountHolderName,
        @RequestParam(required = false) Boolean isDefault,
        @RequestParam(value = "restaurantId", required = false) Integer restaurantId,
        Principal principal,
        RedirectAttributes redirectAttributes
    ) {
        try {
            if (restaurantId == null) {
                restaurantId = getRestaurantId(principal);
            }
            logger.info("🏦 Restaurant {} adding bank account: {}", restaurantId, accountNumber);
            
            // Tạo DTO
            RestaurantBankAccountDto dto = new RestaurantBankAccountDto();
            dto.setBankCode(bankCode);
            dto.setAccountNumber(accountNumber);
            dto.setAccountHolderName(accountHolderName.toUpperCase().trim());
            dto.setIsDefault(isDefault != null ? isDefault : false);
            
            // Thêm tài khoản
            RestaurantBankAccountDto result = bankAccountService.addBankAccount(restaurantId, dto);
            
            redirectAttributes.addFlashAttribute("success", 
                "Đã thêm tài khoản ngân hàng " + result.getBankName() + " - " + result.getAccountNumber() + " thành công!");
            logger.info("✅ Added bank account #{} for restaurant {}", result.getAccountId(), restaurantId);
            
        } catch (Exception e) {
            logger.error("❌ Error adding bank account for restaurant", e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi thêm tài khoản ngân hàng: " + e.getMessage());
        }
        
        String redirectUrl = restaurantId != null 
            ? "redirect:/restaurant-owner/withdrawal?restaurantId=" + restaurantId + "#bank-accounts"
            : "redirect:/restaurant-owner/withdrawal#bank-accounts";
        return redirectUrl;
    }
    
    /**
     * Get all restaurants owned by current user
     */
    private List<com.example.booking.domain.RestaurantProfile> getAllRestaurantsByOwner(Principal principal) {
        try {
            String username = principal.getName();
            logger.debug("🔍 Getting restaurants for username: {}", username);
            
            List<com.example.booking.domain.RestaurantProfile> restaurants = 
                restaurantRepository.findByOwnerUsername(username);
            
            logger.debug("✅ Found {} restaurants for username: {}", restaurants.size(), username);
            return restaurants;
            
        } catch (Exception e) {
            logger.error("❌ Error getting restaurants for principal: {}", 
                principal != null ? principal.getName() : "null", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Helper method to get restaurant ID from principal (first restaurant)
     */
    private Integer getRestaurantId(Principal principal) {
        try {
            String username = principal.getName(); // This returns username, not UUID
            logger.debug("🔍 Getting restaurant ID for username: {}", username);
            
            List<com.example.booking.domain.RestaurantProfile> restaurants = 
                restaurantRepository.findByOwnerUsername(username);
            
            if (restaurants.isEmpty()) {
                logger.error("❌ No restaurant found for username: {}", username);
                throw new RuntimeException("Không tìm thấy nhà hàng với tài khoản: " + username);
            }
            
            Integer restaurantId = restaurants.get(0).getRestaurantId();
            logger.debug("✅ Found restaurant ID: {} for username: {}", restaurantId, username);
            return restaurantId;
            
        } catch (jakarta.persistence.EntityNotFoundException e) {
            // Lỗi: User không tồn tại trong database (orphaned restaurant_owner)
            String errorMsg = "Lỗi dữ liệu: Không tìm thấy thông tin người dùng trong hệ thống. " +
                             "Có thể do dữ liệu không nhất quán trong database. " +
                             "Vui lòng liên hệ admin để kiểm tra và sửa lỗi.";
            logger.error("❌ EntityNotFoundException - User not found in database. Principal: {}", 
                principal != null ? principal.getName() : "null", e);
            throw new RuntimeException(errorMsg, e);
        } catch (org.hibernate.LazyInitializationException e) {
            // Lỗi: Không thể load User entity
            String errorMsg = "Lỗi dữ liệu: Không thể tải thông tin người dùng. " +
                             "Có thể do dữ liệu không nhất quán trong database. " +
                             "Vui lòng liên hệ admin để kiểm tra và sửa lỗi.";
            logger.error("❌ LazyInitializationException - Cannot load User entity. Principal: {}", 
                principal != null ? principal.getName() : "null", e);
            throw new RuntimeException(errorMsg, e);
        } catch (Exception e) {
            logger.error("❌ Error getting restaurant ID for principal: {}", 
                principal != null ? principal.getName() : "null", e);
            throw new RuntimeException("Lỗi khi lấy thông tin nhà hàng: " + e.getMessage(), e);
        }
    }
    
    /**
     * Create empty balance DTO for error handling
     */
    private RestaurantBalanceDto createEmptyBalance() {
        RestaurantBalanceDto dto = new RestaurantBalanceDto();
        dto.setAvailableBalance(java.math.BigDecimal.ZERO);
        dto.setTotalRevenue(java.math.BigDecimal.ZERO);
        dto.setTotalWithdrawn(java.math.BigDecimal.ZERO);
        dto.setPendingWithdrawal(java.math.BigDecimal.ZERO);
        dto.setTotalCommission(java.math.BigDecimal.ZERO);
        dto.setTotalBookingsCompleted(0);
        return dto;
    }
}
