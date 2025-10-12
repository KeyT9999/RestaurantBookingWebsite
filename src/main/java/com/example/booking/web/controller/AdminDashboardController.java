package com.example.booking.web.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

import com.example.booking.dto.admin.RestaurantBalanceInfoDto;
import com.example.booking.dto.admin.WithdrawalStatsDto;
import com.example.booking.service.RestaurantApprovalService;
import com.example.booking.service.RestaurantBalanceService;
import com.example.booking.service.ReviewReportService;
import com.example.booking.service.WithdrawalService;

/**
 * Controller for Admin Dashboard
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardController.class);
    
    @Autowired
    private WithdrawalService withdrawalService;
    
    @Autowired
    private RestaurantBalanceService balanceService;

    @Autowired
    private ReviewReportService reviewReportService;
    
    @Autowired
    private RestaurantApprovalService restaurantApprovalService;
    
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
            
            // Get withdrawal statistics
            WithdrawalStatsDto withdrawalStats = withdrawalService.getWithdrawalStats();
            model.addAttribute("withdrawalStats", withdrawalStats);
            
            // Get total pending amount
            BigDecimal totalPendingAmount = withdrawalService.getTotalPendingAmount();
            model.addAttribute("totalPendingAmount", totalPendingAmount);
            
            // Get total withdrawn amount
            BigDecimal totalWithdrawnAmount = withdrawalService.getTotalWithdrawnAmount();
            model.addAttribute("totalWithdrawnAmount", totalWithdrawnAmount);
            
            // Get top restaurants by withdrawal amount
            List<RestaurantBalanceInfoDto> topRestaurants = withdrawalService.getTopRestaurantsByWithdrawal(10);
            model.addAttribute("topRestaurants", topRestaurants);
            
            // Get monthly withdrawal statistics for chart
            Map<String, Object> monthlyStats = withdrawalService.getMonthlyWithdrawalStats();
            model.addAttribute("monthlyStats", monthlyStats);
            
            // Get total commission earned
            BigDecimal totalCommission = withdrawalService.getTotalCommissionEarned();
            model.addAttribute("totalCommission", totalCommission);
            
            // Get restaurant approval statistics
            long pendingRestaurants = restaurantApprovalService.getPendingRestaurantCount();
            long approvedRestaurants = restaurantApprovalService.getApprovedRestaurantCount();
            long rejectedRestaurants = restaurantApprovalService.getRejectedRestaurantCount();
            long suspendedRestaurants = restaurantApprovalService.getSuspendedRestaurantCount();
            
            model.addAttribute("pendingRestaurants", pendingRestaurants);
            model.addAttribute("approvedRestaurants", approvedRestaurants);
            model.addAttribute("rejectedRestaurants", rejectedRestaurants);
            model.addAttribute("suspendedRestaurants", suspendedRestaurants);

            long pendingReviewReports = reviewReportService.countPendingReports();
            model.addAttribute("pendingReviewReports", pendingReviewReports);
            
            logger.info("Admin dashboard loaded successfully");
            
            return "admin/dashboard";
            
        } catch (Exception e) {
            logger.error("Error loading admin dashboard", e);
            model.addAttribute("error", "Lỗi khi tải dashboard: " + e.getMessage());
            return "admin/dashboard";
        }
    }
}
