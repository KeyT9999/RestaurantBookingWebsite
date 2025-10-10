package com.example.booking.web.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.booking.dto.admin.RestaurantBalanceInfoDto;
import com.example.booking.dto.admin.WithdrawalStatsDto;
import com.example.booking.service.RestaurantBalanceService;
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
            
            logger.info("Admin dashboard loaded successfully");
            
            return "admin/dashboard";
            
        } catch (Exception e) {
            logger.error("Error loading admin dashboard", e);
            model.addAttribute("error", "Lỗi khi tải dashboard: " + e.getMessage());
            return "admin/dashboard";
        }
    }
}
