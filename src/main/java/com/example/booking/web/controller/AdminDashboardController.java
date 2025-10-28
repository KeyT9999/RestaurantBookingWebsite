package com.example.booking.web.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.booking.service.RestaurantApprovalService;
import com.example.booking.service.RestaurantBalanceService;
import com.example.booking.service.RefundService;
import com.example.booking.domain.RefundRequest;
import com.example.booking.common.enums.RefundStatus;

/**
 * Controller for Admin Dashboard
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardController.class);
    
    @Autowired
    private RestaurantBalanceService balanceService;
    
    @Autowired
    private RestaurantApprovalService restaurantApprovalService;
    
    @Autowired
    private RefundService refundService;

    /**
     * Add common model attributes for admin pages
     */
    @ModelAttribute
    public void addCommonAttributes(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            
            if (isAdmin) {
                try {
                    long pendingRestaurants = restaurantApprovalService.getPendingRestaurantCount();
                    model.addAttribute("pendingRestaurants", pendingRestaurants);
                } catch (Exception e) {
                    logger.warn("Failed to get pending restaurants count", e);
                    model.addAttribute("pendingRestaurants", 0L);
                }
            }
        }
    }
    
    /**
     * GET /admin/dashboard
     */
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        try {
            logger.info("Loading admin dashboard");

            BigDecimal commissionToday = balanceService.getCommissionToday();
            BigDecimal weeklyCommission = balanceService.getWeeklyCommission();
            BigDecimal monthlyCommission = balanceService.getMonthlyCommission();
            BigDecimal totalCommission = balanceService.getTotalCommission();
            BigDecimal avgCommissionPerBooking = balanceService.getAverageCommissionPerBooking();
            long todayBookings = balanceService.getCompletedBookingsToday();

            model.addAttribute("commissionToday", commissionToday);
            model.addAttribute("commissionRate", balanceService.getCommissionRate());
            model.addAttribute("todayBookings", todayBookings);
            model.addAttribute("avgCommissionPerBooking", avgCommissionPerBooking);
            model.addAttribute("weeklyCommission", weeklyCommission);
            model.addAttribute("monthlyCommission", monthlyCommission);
            model.addAttribute("totalCommission", totalCommission);
            
            logger.info("Admin dashboard loaded successfully");
            
            return "admin/dashboard";
            
        } catch (Exception e) {
            logger.error("Error loading admin dashboard", e);
            model.addAttribute("error", "Lỗi khi tải dashboard: " + e.getMessage());
            return "admin/dashboard";
        }
    }

    /**
     * GET /admin/refund-requests
     */
    @GetMapping("/refund-requests")
    public String refundRequests(Model model) {
        try {
            logger.info("Loading refund requests page");

            // Get all refund requests by status
            List<RefundRequest> pendingRefunds = refundService.getPendingRefunds();
            List<RefundRequest> completedRefunds = refundService.getRefundsByStatus(RefundStatus.COMPLETED);
            List<RefundRequest> rejectedRefunds = refundService.getRefundsByStatus(RefundStatus.REJECTED);

            // Calculate statistics
            long pendingCount = pendingRefunds.size();
            long completedCount = completedRefunds.size();
            long rejectedCount = rejectedRefunds.size();

            BigDecimal totalAmount = pendingRefunds.stream()
                    .map(RefundRequest::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            model.addAttribute("pendingRefunds", pendingRefunds);
            model.addAttribute("completedRefunds", completedRefunds);
            model.addAttribute("rejectedRefunds", rejectedRefunds);
            model.addAttribute("pendingCount", pendingCount);
            model.addAttribute("completedCount", completedCount);
            model.addAttribute("rejectedCount", rejectedCount);
            model.addAttribute("totalAmount", totalAmount);

            // Add bank name mapping for template
            Map<String, String> bankNameMap = new HashMap<>();
            bankNameMap.put("970422", "MB Bank");
            bankNameMap.put("970436", "Vietcombank");
            bankNameMap.put("970415", "Techcombank");
            bankNameMap.put("970416", "VietinBank");
            bankNameMap.put("970423", "Agribank");
            bankNameMap.put("970427", "ACB");
            bankNameMap.put("970418", "Sacombank");
            bankNameMap.put("970419", "BIDV");
            model.addAttribute("bankNameMap", bankNameMap);

            logger.info("Refund requests page loaded successfully");

            return "admin/refund-requests";

        } catch (Exception e) {
            logger.error("Error loading refund requests page", e);
            model.addAttribute("error", "Lỗi khi tải trang refund requests: " + e.getMessage());
            return "admin/refund-requests";
        }
    }
}
