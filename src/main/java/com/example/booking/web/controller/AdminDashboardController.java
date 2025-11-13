package com.example.booking.web.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.booking.dto.analytics.CommissionSeriesPoint;
import com.example.booking.service.RestaurantApprovalService;
import com.example.booking.service.RestaurantBalanceService;
import com.example.booking.service.RefundService;
import com.example.booking.domain.RefundRequest;
import com.example.booking.common.enums.RefundStatus;
import com.example.booking.common.enums.WithdrawalStatus;
import com.example.booking.repository.WithdrawalRequestRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WithdrawalRequestRepository withdrawalRepository;
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
            long pendingRefunds = refundService.getPendingRefunds().size();
            List<CommissionSeriesPoint> dailySeries = balanceService.getCommissionSeries(
                RestaurantBalanceService.CommissionSeriesGranularity.DAILY, 7);
            List<CommissionSeriesPoint> monthlySeries = balanceService.getCommissionSeries(
                RestaurantBalanceService.CommissionSeriesGranularity.MONTHLY, 6);
            List<CommissionSeriesPoint> yearlySeries = balanceService.getCommissionSeries(
                RestaurantBalanceService.CommissionSeriesGranularity.YEARLY, 5);

            // Withdrawal stats for dashboard
            long withdrawalPendingCount = withdrawalRepository.countByStatus(WithdrawalStatus.PENDING);
            long withdrawalSucceededCount = withdrawalRepository.countByStatus(WithdrawalStatus.SUCCEEDED);
            long withdrawalRejectedCount = withdrawalRepository.countByStatus(WithdrawalStatus.REJECTED);
            BigDecimal withdrawalPendingAmount = withdrawalRepository.sumAmountByStatus(WithdrawalStatus.PENDING);
            BigDecimal withdrawalSucceededAmount = withdrawalRepository.sumAmountByStatus(WithdrawalStatus.SUCCEEDED);
            BigDecimal withdrawalCommissionTotal = withdrawalRepository.sumCommissionByStatus(WithdrawalStatus.SUCCEEDED);
            Double withdrawalAvgHours = withdrawalRepository.calculateAverageProcessingTimeHours();

            model.addAttribute("commissionToday", commissionToday);
            model.addAttribute("commissionRate", balanceService.getCommissionRate());
            model.addAttribute("todayBookings", todayBookings);
            model.addAttribute("avgCommissionPerBooking", avgCommissionPerBooking);
            model.addAttribute("weeklyCommission", weeklyCommission);
            model.addAttribute("monthlyCommission", monthlyCommission);
            model.addAttribute("totalCommission", totalCommission);
            model.addAttribute("pendingRefunds", pendingRefunds);
            model.addAttribute("withdrawalPendingCount", withdrawalPendingCount);
            model.addAttribute("withdrawalSucceededCount", withdrawalSucceededCount);
            model.addAttribute("withdrawalRejectedCount", withdrawalRejectedCount);
            model.addAttribute("withdrawalPendingAmount", withdrawalPendingAmount);
            model.addAttribute("withdrawalSucceededAmount", withdrawalSucceededAmount);
            model.addAttribute("withdrawalCommissionTotal", withdrawalCommissionTotal);
            model.addAttribute("withdrawalAvgHours", withdrawalAvgHours != null ? withdrawalAvgHours : 0.0);
            
            Map<String, List<CommissionSeriesPoint>> commissionSeries = new HashMap<>();
            commissionSeries.put("daily", dailySeries);
            commissionSeries.put("monthly", monthlySeries);
            commissionSeries.put("yearly", yearlySeries);

            try {
                String commissionSeriesJson = objectMapper.writeValueAsString(commissionSeries);
                model.addAttribute("commissionSeriesJson", commissionSeriesJson);
            } catch (JsonProcessingException e) {
                logger.error("Failed to serialize commission series data", e);
                model.addAttribute("commissionSeriesJson", "{}");
            }
            
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

            // Totals
            BigDecimal pendingTotal = pendingRefunds.stream()
                    .map(RefundRequest::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal completedTotal = completedRefunds.stream()
                    .map(RefundRequest::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            model.addAttribute("pendingRefunds", pendingRefunds);
            model.addAttribute("completedRefunds", completedRefunds);
            model.addAttribute("rejectedRefunds", rejectedRefunds);
            model.addAttribute("pendingCount", pendingCount);
            model.addAttribute("completedCount", completedCount);
            model.addAttribute("rejectedCount", rejectedCount);
            model.addAttribute("pendingTotal", pendingTotal);
            model.addAttribute("completedTotal", completedTotal);
            model.addAttribute("totalAmount", pendingTotal);

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

    /**
     * GET /admin/api/statistics
     * API endpoint to get admin dashboard statistics
     */
    @GetMapping("/api/statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            logger.info("Fetching admin dashboard statistics");

            Map<String, Object> stats = new HashMap<>();

            // Get commission statistics
            BigDecimal commissionToday = balanceService.getCommissionToday();
            BigDecimal weeklyCommission = balanceService.getWeeklyCommission();
            BigDecimal monthlyCommission = balanceService.getMonthlyCommission();
            BigDecimal totalCommission = balanceService.getTotalCommission();
            
            // Get booking statistics
            long completedBookingsToday = balanceService.getCompletedBookingsToday();
            
            // Get pending restaurants count
            long pendingRestaurants = restaurantApprovalService.getPendingRestaurantCount();
            
            // Get refund statistics
            List<RefundRequest> pendingRefunds = refundService.getPendingRefunds();
            long pendingRefundCount = pendingRefunds.size();
            BigDecimal totalPendingRefundAmount = pendingRefunds.stream()
                    .map(RefundRequest::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Build response
            stats.put("commissionToday", commissionToday);
            stats.put("weeklyCommission", weeklyCommission);
            stats.put("monthlyCommission", monthlyCommission);
            stats.put("totalCommission", totalCommission);
            stats.put("todayBookings", completedBookingsToday);
            stats.put("pendingRestaurants", pendingRestaurants);
            stats.put("pendingRefunds", pendingRefundCount);
            stats.put("totalPendingRefundAmount", totalPendingRefundAmount);

            logger.info("Admin dashboard statistics fetched successfully");
            
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            logger.error("Error fetching admin dashboard statistics", e);
            
            // Return error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch statistics");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
