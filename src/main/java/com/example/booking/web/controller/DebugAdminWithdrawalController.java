package com.example.booking.web.controller;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booking.common.enums.WithdrawalStatus;
import com.example.booking.domain.User;
import com.example.booking.dto.admin.WithdrawalStatsDto;
import com.example.booking.dto.payout.WithdrawalRequestDto;
import com.example.booking.service.WithdrawalService;

/**
 * Controller để debug admin withdrawal
 */
@Controller
@RequestMapping("/debug-admin-withdrawal")
@PreAuthorize("hasRole('ADMIN')")
public class DebugAdminWithdrawalController {
    
    private static final Logger logger = LoggerFactory.getLogger(DebugAdminWithdrawalController.class);
    
    private final WithdrawalService withdrawalService;
    
    public DebugAdminWithdrawalController(WithdrawalService withdrawalService) {
        this.withdrawalService = withdrawalService;
    }
    
    /**
     * Helper method to get admin user ID from security context
     */
    private UUID getAdminUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            return user.getId();
        }
        // Fallback: try to parse from principal name
        if (authentication != null && authentication.getName() != null) {
            try {
                return UUID.fromString(authentication.getName());
            } catch (IllegalArgumentException e) {
                logger.warn("Cannot parse UUID from authentication name: {}", authentication.getName());
            }
        }
        throw new IllegalStateException("Cannot get admin user ID from security context");
    }
    
    /**
     * Hiển thị trang debug admin withdrawal
     * GET /debug-admin-withdrawal
     */
    @GetMapping
    public String debugAdminWithdrawal(
        @RequestParam(required = false) String status,
        Model model
    ) {
        try {
            // Lấy stats
            WithdrawalStatsDto stats = withdrawalService.getWithdrawalStats();
            model.addAttribute("stats", stats);
            
            // Lấy danh sách withdrawals
            List<WithdrawalRequestDto> withdrawals;
            if (status != null && !status.isEmpty() && !status.equals("ALL")) {
                WithdrawalStatus withdrawalStatus = WithdrawalStatus.valueOf(status);
                withdrawals = withdrawalService.getWithdrawalsByStatus(withdrawalStatus);
                model.addAttribute("filter", status);
            } else {
                // Lấy tất cả (không phân trang để đơn giản)
                withdrawals = withdrawalService.getAllWithdrawals(Pageable.unpaged()).getContent();
                model.addAttribute("filter", "ALL");
            }
            
            model.addAttribute("withdrawals", withdrawals);
            
            return "debug-admin-withdrawal";
            
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tải dữ liệu: " + e.getMessage());
            return "debug-admin-withdrawal";
        }
    }
    
    /**
     * Approve withdrawal request
     * POST /debug-admin-withdrawal/{id}/approve
     */
    @PostMapping("/{id}/approve")
    public String approveWithdrawal(
        @PathVariable Integer id,
        RedirectAttributes redirectAttributes
    ) {
        try {
            UUID adminUserId = getAdminUserId();
            withdrawalService.approveWithdrawal(id, adminUserId, "Admin approved via debug interface");
            redirectAttributes.addFlashAttribute("success", "Đã duyệt yêu cầu rút tiền #" + id + " thành công!");
            logger.info("Admin {} approved withdrawal request {}", adminUserId, id);
            
        } catch (Exception e) {
            logger.error("Error approving withdrawal {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi duyệt yêu cầu: " + e.getMessage());
        }
        
        return "redirect:/debug-admin-withdrawal";
    }
    
    /**
     * Reject withdrawal request
     * POST /debug-admin-withdrawal/{id}/reject
     */
    @PostMapping("/{id}/reject")
    public String rejectWithdrawal(
        @PathVariable Integer id,
        @RequestParam String reason,
        RedirectAttributes redirectAttributes
    ) {
        try {
            UUID adminUserId = getAdminUserId();
            withdrawalService.rejectWithdrawal(id, adminUserId, reason);
            redirectAttributes.addFlashAttribute("success", "Đã từ chối yêu cầu rút tiền #" + id + "!");
            logger.info("Admin {} rejected withdrawal request {}: {}", adminUserId, id, reason);
            
        } catch (Exception e) {
            logger.error("Error rejecting withdrawal {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi từ chối yêu cầu: " + e.getMessage());
        }
        
        return "redirect:/debug-admin-withdrawal";
    }
}
