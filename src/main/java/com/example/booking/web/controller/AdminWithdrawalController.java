package com.example.booking.web.controller;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booking.common.enums.WithdrawalStatus;
import com.example.booking.dto.admin.WithdrawalStatsDto;
import com.example.booking.dto.payout.ManualPayDto;
import com.example.booking.dto.payout.WithdrawalRequestDto;
import com.example.booking.service.WithdrawalService;

/**
 * Controller for Admin Withdrawal Management
 */
@Controller
@RequestMapping("/admin/withdrawal")
public class AdminWithdrawalController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminWithdrawalController.class);
    
    @Autowired
    private WithdrawalService withdrawalService;
    
    /**
     * GET /admin/withdrawal
     */
    @GetMapping
    public String withdrawalManagement(
        @RequestParam(required = false) String status,
        Model model
    ) {
        try {
            logger.info("Loading withdrawal management page with status filter: {}", status);
            
            // Get stats
            WithdrawalStatsDto stats = withdrawalService.getWithdrawalStats();
            model.addAttribute("stats", stats);
            logger.info("Stats loaded: pending={}, processing={}, succeeded={}, failed={}", 
                stats.getPendingCount(), stats.getProcessingCount(), stats.getSucceededCount(), stats.getFailedCount());
            
            // Get withdrawals list
            List<WithdrawalRequestDto> withdrawals;
            if (status != null && !status.isEmpty() && !status.equals("ALL")) {
                WithdrawalStatus withdrawalStatus = WithdrawalStatus.valueOf(status);
                withdrawals = withdrawalService.getWithdrawalsByStatus(withdrawalStatus);
                model.addAttribute("filter", status);
                logger.info("Loaded {} withdrawals with status: {}", withdrawals.size(), status);
            } else {
                // Get all (no pagination for simplicity)
                withdrawals = withdrawalService.getAllWithdrawals(Pageable.unpaged()).getContent();
                model.addAttribute("filter", "ALL");
                logger.info("Loaded {} total withdrawals", withdrawals.size());
            }
            
            model.addAttribute("withdrawals", withdrawals);
            
            return "admin/withdrawal-management";
            
        } catch (Exception e) {
            logger.error("Error loading withdrawal management page", e);
            model.addAttribute("error", "Lỗi khi tải dữ liệu: " + e.getMessage());
            model.addAttribute("withdrawals", java.util.Collections.emptyList());
            model.addAttribute("stats", new WithdrawalStatsDto());
            model.addAttribute("filter", "ALL");
            return "admin/withdrawal-management";
        }
    }
    
    /**
     * Approve withdrawal request
     * POST /admin/withdrawal/{id}/approve
     */
    @PostMapping("/{id}/approve")
    public String approveWithdrawal(
        @PathVariable Integer id,
        RedirectAttributes redirectAttributes
    ) {
        try {
            UUID adminUserId = getAdminUserId();
            withdrawalService.approveWithdrawal(id, adminUserId, "Admin approved");
            redirectAttributes.addFlashAttribute("success", "Đã duyệt yêu cầu rút tiền #" + id);
        } catch (Exception e) {
            logger.error("Error approving withdrawal {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi duyệt: " + e.getMessage());
        }
        return "redirect:/admin/withdrawal?status=PENDING";
    }
    
    
    /**
     * Mark withdrawal as paid (manual transfer)
     * POST /admin/withdrawal/{id}/mark-paid
     */
    @PostMapping("/{id}/mark-paid")
    public String markWithdrawalPaid(
        @PathVariable Integer id,
        RedirectAttributes redirectAttributes
    ) {
        try {
            UUID adminUserId = getAdminUserId();
            
            ManualPayDto dto = new ManualPayDto();
            dto.setTransferRef("MANUAL-" + System.currentTimeMillis()); // Auto generate
            dto.setNote("Admin đã xác nhận chuyển tiền");
            dto.setProofUrl(null);
            
            withdrawalService.markWithdrawalPaid(id, adminUserId, dto);
            redirectAttributes.addFlashAttribute("success", "Đã đánh dấu yêu cầu rút tiền #" + id + " là đã chi thành công");
        } catch (Exception e) {
            logger.error("Error marking withdrawal {} as paid", id, e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi đánh dấu đã chi: " + e.getMessage());
        }
        return "redirect:/admin/withdrawal?status=SUCCEEDED";
    }

    /**
     * Reject a withdrawal request
     */
    @PostMapping("/{id}/reject")
    public String rejectWithdrawal(
            @PathVariable Integer id,
            @RequestParam String rejectReason,
            @RequestParam(required = false) String rejectNote,
            RedirectAttributes redirectAttributes
    ) {
        try {
            UUID adminUserId = getAdminUserId(); // Mocked for now
            // Combine reason and note into one string
            String fullReason = rejectReason;
            if (rejectNote != null && !rejectNote.trim().isEmpty()) {
                fullReason += " - " + rejectNote;
            }
            withdrawalService.rejectWithdrawal(id, adminUserId, fullReason);
            redirectAttributes.addFlashAttribute("success", "Đã từ chối yêu cầu rút tiền #" + id);
        } catch (Exception e) {
            logger.error("Error rejecting withdrawal {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi từ chối yêu cầu: " + e.getMessage());
        }
        return "redirect:/admin/withdrawal?status=REJECTED";
    }
    
    /**
     * Get admin user ID (mock implementation)
     */
    private UUID getAdminUserId() {
        // TODO: Get from authentication context
        // For now, try to find an existing admin user
        try {
            // Try to find any admin user first
            return UUID.fromString("00000000-0000-0000-0000-000000000001");
        } catch (Exception e) {
            logger.error("Error getting admin user ID", e);
            // Fallback to a random UUID if needed
            return UUID.randomUUID();
        }
    }
}