package com.example.booking.web.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.booking.dto.analytics.CommissionSeriesPoint;
import com.example.booking.service.RestaurantApprovalService;
import com.example.booking.service.RestaurantBalanceService;
import com.example.booking.service.RefundService;
import com.example.booking.domain.RefundRequest;
import com.example.booking.common.enums.RefundStatus;
import com.example.booking.common.enums.WithdrawalStatus;
import com.example.booking.common.enums.BookingStatus;
import com.example.booking.common.enums.RestaurantApprovalStatus;
import com.example.booking.domain.UserRole;
import com.example.booking.domain.PaymentStatus;
import com.example.booking.repository.WithdrawalRequestRepository;
import com.example.booking.repository.UserRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.PaymentRepository;
import com.example.booking.repository.ReviewRepository;
import com.example.booking.repository.ChatRoomRepository;
import com.example.booking.domain.Booking;
import com.example.booking.domain.Payment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;

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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantProfileRepository restaurantProfileRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;
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
    public String adminDashboard(
            @RequestParam(value = "period", defaultValue = "today") String period,
            Model model) {
        try {
            logger.info("Loading admin dashboard with database statistics for period: {}", period);

            // Calculate date range based on period
            LocalDateTime startDate;
            LocalDateTime endDate;
            LocalDate today = LocalDate.now();
            
            switch (period) {
                case "yesterday":
                    startDate = today.minusDays(1).atStartOfDay();
                    endDate = today.atStartOfDay();
                    break;
                case "thisWeek":
                    startDate = today.with(java.time.DayOfWeek.MONDAY).atStartOfDay();
                    endDate = today.plusDays(1).atStartOfDay();
                    break;
                case "lastWeek":
                    startDate = today.minusWeeks(1).with(java.time.DayOfWeek.MONDAY).atStartOfDay();
                    endDate = today.with(java.time.DayOfWeek.MONDAY).atStartOfDay();
                    break;
                case "thisMonth":
                    startDate = today.withDayOfMonth(1).atStartOfDay();
                    endDate = today.plusDays(1).atStartOfDay();
                    break;
                case "lastMonth":
                    startDate = today.minusMonths(1).withDayOfMonth(1).atStartOfDay();
                    endDate = today.withDayOfMonth(1).atStartOfDay();
                    break;
                case "last3Months":
                    startDate = today.minusMonths(3).withDayOfMonth(1).atStartOfDay();
                    endDate = today.plusDays(1).atStartOfDay();
                    break;
                case "last6Months":
                    startDate = today.minusMonths(6).withDayOfMonth(1).atStartOfDay();
                    endDate = today.plusDays(1).atStartOfDay();
                    break;
                case "thisYear":
                    startDate = today.withDayOfYear(1).atStartOfDay();
                    endDate = today.plusDays(1).atStartOfDay();
                    break;
                case "all":
                    startDate = LocalDateTime.of(1970, 1, 1, 0, 0);
                    endDate = LocalDateTime.now().plusYears(100);
                    break;
                case "today":
                default:
                    startDate = today.atStartOfDay();
                    endDate = today.plusDays(1).atStartOfDay();
                    break;
            }

            // Commission statistics based on period
            BigDecimal commissionForPeriod = balanceService.getCommissionByDateRange(startDate, endDate);
            
            // Calculate revenue for period
            BigDecimal revenueForPeriod = paymentRepository.sumSubtotalFromPaymentsByPaidAtRange(startDate, endDate);
            
            // Calculate bookings count for period (based on payment paidAt, not booking time)
            long bookingsCountForPeriod = paymentRepository.countDistinctBookingsWithCompletedPaymentsByDateRange(startDate, endDate);
            
            // Calculate payments count for period
            long completedPaymentsForPeriod = paymentRepository.countByStatusAndPaidAtBetween(PaymentStatus.COMPLETED, startDate, endDate);
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

            // ===== DATABASE STATISTICS =====
            
            // User statistics
            long totalUsers = userRepository.count();
            long totalCustomers = userRepository.countByRole(UserRole.CUSTOMER);
            long totalRestaurantOwners = userRepository.countByRole(UserRole.RESTAURANT_OWNER);
            long totalAdmins = userRepository.countByRole(UserRole.ADMIN);
            
            // Restaurant statistics
            long totalRestaurants = restaurantProfileRepository.count();
            long approvedRestaurants = restaurantProfileRepository.countByApprovalStatus(RestaurantApprovalStatus.APPROVED);
            long pendingRestaurants = restaurantProfileRepository.countByApprovalStatus(RestaurantApprovalStatus.PENDING);
            long rejectedRestaurants = restaurantProfileRepository.countByApprovalStatus(RestaurantApprovalStatus.REJECTED);
            long suspendedRestaurants = restaurantProfileRepository.countByApprovalStatus(RestaurantApprovalStatus.SUSPENDED);
            
            // Booking statistics - use period-specific if not "all"
            long totalBookings = period.equals("all") ? bookingRepository.count() : bookingsCountForPeriod;
            long completedBookings = period.equals("all") ? bookingRepository.countByStatus(BookingStatus.COMPLETED) : bookingsCountForPeriod;
            long pendingBookings = bookingRepository.countByStatus(BookingStatus.PENDING);
            long confirmedBookings = bookingRepository.countByStatus(BookingStatus.CONFIRMED);
            long cancelledBookings = bookingRepository.countByStatus(BookingStatus.CANCELLED);
            
            // Payment statistics - use period-specific if not "all"
            long totalPayments = period.equals("all") ? paymentRepository.count() : completedPaymentsForPeriod;
            long completedPayments = period.equals("all") ? paymentRepository.countByStatus(PaymentStatus.COMPLETED) : completedPaymentsForPeriod;
            long pendingPayments = paymentRepository.countByStatus(PaymentStatus.PENDING);
            long failedPayments = paymentRepository.countByStatus(PaymentStatus.FAILED);
            long refundedPayments = paymentRepository.countByStatus(PaymentStatus.REFUNDED);
            
            // Calculate total revenue (subtotal) - use period-specific if not "all"
            // Revenue = subtotal = table fees + dishes + services (trước voucher discount)
            BigDecimal totalRevenue = period.equals("all") ? 
                paymentRepository.sumSubtotalFromAllCompletedPayments() : revenueForPeriod;
            
            // Review statistics
            long totalReviews = reviewRepository.count();
            
            // Chat room statistics
            long totalChatRooms = chatRoomRepository.count();
            long activeChatRooms = chatRoomRepository.countActiveRooms();
            
            // Today's statistics
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
            
            long todayTotalBookings = bookingRepository.countByBookingTimeBetween(startOfDay, endOfDay);
            long todayCompletedBookings = bookingRepository.countByStatusAndBookingTimeBetween(
                BookingStatus.COMPLETED, startOfDay, endOfDay);
            
            // Add all statistics to model (ensure no null values)
            // Use period-specific values
            model.addAttribute("commissionToday", commissionForPeriod != null ? commissionForPeriod : BigDecimal.ZERO);
            model.addAttribute("commissionRate", balanceService.getCommissionRate());
            model.addAttribute("todayBookings", bookingsCountForPeriod);
            model.addAttribute("period", period);
            model.addAttribute("totalCommission", commissionForPeriod != null ? commissionForPeriod : BigDecimal.ZERO);
            model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
            model.addAttribute("pendingRefunds", pendingRefunds);
            model.addAttribute("withdrawalPendingCount", withdrawalPendingCount);
            model.addAttribute("withdrawalSucceededCount", withdrawalSucceededCount);
            model.addAttribute("withdrawalRejectedCount", withdrawalRejectedCount);
            model.addAttribute("withdrawalPendingAmount", withdrawalPendingAmount != null ? withdrawalPendingAmount : BigDecimal.ZERO);
            model.addAttribute("withdrawalSucceededAmount", withdrawalSucceededAmount != null ? withdrawalSucceededAmount : BigDecimal.ZERO);
            model.addAttribute("withdrawalCommissionTotal", withdrawalCommissionTotal != null ? withdrawalCommissionTotal : BigDecimal.ZERO);
            model.addAttribute("withdrawalAvgHours", withdrawalAvgHours != null ? withdrawalAvgHours : 0.0);
            
            // Database statistics
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalCustomers", totalCustomers);
            model.addAttribute("totalRestaurantOwners", totalRestaurantOwners);
            model.addAttribute("totalAdmins", totalAdmins);
            model.addAttribute("totalRestaurants", totalRestaurants);
            model.addAttribute("approvedRestaurants", approvedRestaurants);
            model.addAttribute("pendingRestaurants", pendingRestaurants);
            model.addAttribute("rejectedRestaurants", rejectedRestaurants);
            model.addAttribute("suspendedRestaurants", suspendedRestaurants);
            model.addAttribute("totalBookings", totalBookings);
            model.addAttribute("completedBookings", completedBookings);
            model.addAttribute("pendingBookings", pendingBookings);
            model.addAttribute("confirmedBookings", confirmedBookings);
            model.addAttribute("cancelledBookings", cancelledBookings);
            model.addAttribute("totalPayments", totalPayments);
            model.addAttribute("completedPayments", completedPayments);
            model.addAttribute("pendingPayments", pendingPayments);
            model.addAttribute("failedPayments", failedPayments);
            model.addAttribute("refundedPayments", refundedPayments);
            model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
            model.addAttribute("totalReviews", totalReviews);
            model.addAttribute("totalChatRooms", totalChatRooms);
            model.addAttribute("activeChatRooms", activeChatRooms);
            model.addAttribute("todayTotalBookings", todayTotalBookings);
            model.addAttribute("todayCompletedBookings", todayCompletedBookings);
            
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
            
            logger.info("Admin dashboard loaded successfully with database statistics");
            
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

    /**
     * GET /admin/bookings
     * Hiển thị tất cả bookings với số tiền nhận được và trạng thái
     */
    @GetMapping("/bookings")
    public String viewAllBookings(Model model,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String status) {
        try {
            logger.info("Loading all bookings for admin");

            // Get all bookings
            List<Booking> allBookings = bookingRepository.findAll();
            
            // Sort by booking time desc
            allBookings.sort(Comparator.comparing(Booking::getBookingTime).reversed());

            // Filter by status if provided
            if (status != null && !status.isEmpty()) {
                try {
                    BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
                    allBookings = allBookings.stream()
                            .filter(booking -> booking.getStatus() == bookingStatus)
                            .toList();
                } catch (IllegalArgumentException e) {
                    // Invalid status, ignore filter
                    logger.warn("Invalid booking status filter: {}", status);
                }
            }

            // Create a list of booking info with payment amounts
            List<Map<String, Object>> bookingInfoList = new ArrayList<>();
            for (Booking booking : allBookings) {
                Map<String, Object> bookingInfo = new HashMap<>();
                bookingInfo.put("booking", booking);
                
                // Get payment information
                Optional<Payment> paymentOpt = paymentRepository.findByBooking(booking);
                if (paymentOpt.isPresent()) {
                    Payment payment = paymentOpt.get();
                    bookingInfo.put("payment", payment);
                    bookingInfo.put("amountReceived", payment.getStatus() == PaymentStatus.COMPLETED 
                        ? payment.getAmount() : BigDecimal.ZERO);
                    bookingInfo.put("paymentStatus", payment.getStatus());
                } else {
                    bookingInfo.put("payment", null);
                    bookingInfo.put("amountReceived", BigDecimal.ZERO);
                    bookingInfo.put("paymentStatus", null);
                }
                
                bookingInfoList.add(bookingInfo);
            }

            // Get statistics
            long totalBookings = allBookings.size();
            long pendingBookings = allBookings.stream()
                    .filter(b -> b.getStatus() == BookingStatus.PENDING).count();
            long confirmedBookings = allBookings.stream()
                    .filter(b -> b.getStatus() == BookingStatus.CONFIRMED).count();
            long cancelledBookings = allBookings.stream()
                    .filter(b -> b.getStatus() == BookingStatus.CANCELLED).count();
            long completedBookings = allBookings.stream()
                    .filter(b -> b.getStatus() == BookingStatus.COMPLETED).count();
            
            // Calculate total amount received
            BigDecimal totalAmountReceived = bookingInfoList.stream()
                    .map(info -> (BigDecimal) info.get("amountReceived"))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            model.addAttribute("bookings", bookingInfoList);
            model.addAttribute("totalBookings", totalBookings);
            model.addAttribute("pendingBookings", pendingBookings);
            model.addAttribute("confirmedBookings", confirmedBookings);
            model.addAttribute("cancelledBookings", cancelledBookings);
            model.addAttribute("completedBookings", completedBookings);
            model.addAttribute("totalAmountReceived", totalAmountReceived);
            model.addAttribute("selectedStatus", status);

            logger.info("Admin bookings page loaded successfully with {} bookings", totalBookings);

            return "admin/bookings";

        } catch (Exception e) {
            logger.error("Error loading admin bookings page", e);
            model.addAttribute("error", "Lỗi khi tải trang bookings: " + e.getMessage());
            return "admin/bookings";
        }
    }

    /**
     * GET /admin/reports
     * Báo cáo tổng hợp doanh thu theo khoảng thời gian
     */
    @GetMapping("/reports")
    public String reports(Model model,
            @RequestParam(required = false) String period) {
        try {
            logger.info("Loading admin reports page with period: {}", period);
            
            if (period == null || period.isEmpty()) {
                period = "month"; // Default: 30 ngày gần nhất
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startDate;
            LocalDateTime endDate = now;

            // Tính toán startDate dựa trên period
            switch (period.toLowerCase()) {
                case "week":
                    startDate = now.minusDays(7);
                    break;
                case "month":
                    startDate = now.minusDays(30);
                    break;
                case "quarter":
                    startDate = now.minusMonths(3);
                    break;
                case "year":
                    startDate = now.minusYears(1);
                    break;
                default:
                    startDate = now.minusDays(30);
                    period = "month";
            }

            // Get statistics from database
            long totalUsers = userRepository.count();
            long totalCustomers = userRepository.countByRole(UserRole.CUSTOMER);
            long totalRestaurantOwners = userRepository.countByRole(UserRole.RESTAURANT_OWNER);
            long totalRestaurants = restaurantProfileRepository.count();
            long approvedRestaurants = restaurantProfileRepository.countByApprovalStatus(RestaurantApprovalStatus.APPROVED);
            
            // Booking statistics
            long totalBookings = bookingRepository.count();
            long pendingBookings = bookingRepository.countByStatus(BookingStatus.PENDING);
            long confirmedBookings = bookingRepository.countByStatus(BookingStatus.CONFIRMED);
            long cancelledBookings = bookingRepository.countByStatus(BookingStatus.CANCELLED);
            long completedBookings = bookingRepository.countByStatus(BookingStatus.COMPLETED);
            long noShowBookings = bookingRepository.countByStatus(BookingStatus.NO_SHOW);
            
            // Calculate no-show rate
            double noShowRate = totalBookings > 0 
                ? (double) noShowBookings / totalBookings * 100 
                : 0.0;
            
            // Revenue statistics for the period
            BigDecimal periodRevenue = paymentRepository.getTotalRevenueFromCompletedPaymentsByDateRange(startDate, endDate);
            long periodBookings = bookingRepository.countByBookingTimeBetween(startDate, endDate);
            long periodCompletedBookings = bookingRepository.countByStatusAndBookingTimeBetween(
                BookingStatus.COMPLETED, startDate, endDate);
            
            // Commission statistics for the period
            BigDecimal periodCommission = balanceService.getCommissionByDateRange(startDate, endDate);
            
            // Daily bookings trend (last 7 days)
            List<Map<String, Object>> dailyBookingsData = new ArrayList<>();
            for (int i = 6; i >= 0; i--) {
                LocalDateTime dayStart = now.minusDays(i).withHour(0).withMinute(0).withSecond(0).withNano(0);
                LocalDateTime dayEnd = now.minusDays(i).withHour(23).withMinute(59).withSecond(59).withNano(999999999);
                long dayBookings = bookingRepository.countByBookingTimeBetween(dayStart, dayEnd);
                
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", now.minusDays(i).toLocalDate());
                dayData.put("count", dayBookings);
                dailyBookingsData.add(dayData);
            }

            // Create reports object
            Map<String, Object> reports = new HashMap<>();
            reports.put("totalUsers", totalUsers);
            reports.put("totalCustomers", totalCustomers);
            reports.put("totalRestaurantOwners", totalRestaurantOwners);
            reports.put("totalRestaurants", totalRestaurants);
            reports.put("approvedRestaurants", approvedRestaurants);
            reports.put("totalBookings", totalBookings);
            reports.put("pendingBookings", pendingBookings);
            reports.put("confirmedBookings", confirmedBookings);
            reports.put("cancelledBookings", cancelledBookings);
            reports.put("completedBookings", completedBookings);
            reports.put("noShowBookings", noShowBookings);
            reports.put("noShowRate", noShowRate);
            reports.put("periodRevenue", periodRevenue != null ? periodRevenue : BigDecimal.ZERO);
            reports.put("periodBookings", periodBookings);
            reports.put("periodCompletedBookings", periodCompletedBookings);
            reports.put("periodCommission", periodCommission != null ? periodCommission : BigDecimal.ZERO);
            reports.put("dailyBookingsData", dailyBookingsData);
            reports.put("reportGeneratedAt", now);
            reports.put("startDate", startDate);
            reports.put("endDate", endDate);
            reports.put("period", period);

            model.addAttribute("reports", reports);
            model.addAttribute("pageTitle", "Báo cáo tổng hợp");
            model.addAttribute("selectedPeriod", period);

            logger.info("Admin reports page loaded successfully");

            return "admin/reports";

        } catch (Exception e) {
            logger.error("Error loading admin reports page", e);
            model.addAttribute("error", "Lỗi khi tải báo cáo: " + e.getMessage());
            return "admin/reports";
        }
    }
}
