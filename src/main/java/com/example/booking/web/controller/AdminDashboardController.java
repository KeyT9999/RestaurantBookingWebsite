package com.example.booking.web.controller;

import java.math.BigDecimal;

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
}
