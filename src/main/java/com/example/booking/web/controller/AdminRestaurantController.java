package com.example.booking.web.controller;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booking.common.enums.RestaurantApprovalStatus;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.service.RestaurantApprovalService;

/**
 * Controller for Admin Restaurant Approval Management
 */
@Controller
@RequestMapping("/admin/restaurant")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRestaurantController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminRestaurantController.class);
    
    @Autowired
    private RestaurantApprovalService restaurantApprovalService;
    
    /**
     * GET /admin/restaurant/requests
     * Danh sách yêu cầu đăng ký nhà hàng
     */
    @GetMapping("/requests")
    public String restaurantRequests(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String search,
        Model model
    ) {
        try {
            logger.info("Loading restaurant requests page with status filter: {}, search: {}", status, search);
            
            // EXACT COPY from SimpleAdminController that works
            List<RestaurantProfile> allRestaurants = restaurantApprovalService.getAllRestaurantsWithApprovalInfo();
            
            // Count by status (EXACT COPY from SimpleAdminController)
            long pendingCount = allRestaurants.stream()
                .filter(r -> r.getApprovalStatus() != null && r.getApprovalStatus().name().equals("PENDING"))
                .count();
            
            long approvedCount = allRestaurants.stream()
                .filter(r -> r.getApprovalStatus() != null && r.getApprovalStatus().name().equals("APPROVED"))
                .count();
            
            long rejectedCount = allRestaurants.stream()
                .filter(r -> r.getApprovalStatus() != null && r.getApprovalStatus().name().equals("REJECTED"))
                .count();
            
            long suspendedCount = allRestaurants.stream()
                .filter(r -> r.getApprovalStatus() != null && r.getApprovalStatus().name().equals("SUSPENDED"))
                .count();
            
            // Filter only PENDING restaurants for display (EXACT COPY from SimpleAdminController)
            List<RestaurantProfile> pendingRestaurants = allRestaurants.stream()
                .filter(r -> r.getApprovalStatus() != null && r.getApprovalStatus().name().equals("PENDING"))
                .collect(java.util.stream.Collectors.toList());
            
            logger.info("Found {} total restaurants, {} pending", allRestaurants.size(), pendingCount);
            logger.info("Pending restaurants: {}", pendingRestaurants.stream()
                .map(RestaurantProfile::getRestaurantName)
                .collect(java.util.stream.Collectors.toList()));
            
            // Apply status filter if provided
            List<RestaurantProfile> restaurants;
            if (status != null && !status.isEmpty() && !status.equals("ALL") && !status.equals("PENDING")) {
                RestaurantApprovalStatus approvalStatus = RestaurantApprovalStatus.valueOf(status);
                restaurants = allRestaurants.stream()
                    .filter(r -> r.getApprovalStatus() != null && r.getApprovalStatus().equals(approvalStatus))
                    .collect(java.util.stream.Collectors.toList());
                model.addAttribute("filter", status);
            } else {
                // Default: Show PENDING restaurants
                restaurants = pendingRestaurants;
                model.addAttribute("filter", "PENDING");
            }
            
            // Apply search filter if provided
            if (search != null && !search.trim().isEmpty()) {
                restaurants = restaurantApprovalService.searchRestaurants(restaurants, search.trim());
                model.addAttribute("search", search);
            }
            
            model.addAttribute("restaurants", restaurants);
            model.addAttribute("pendingCount", pendingCount);
            model.addAttribute("approvedCount", approvedCount);
            model.addAttribute("rejectedCount", rejectedCount);
            model.addAttribute("suspendedCount", suspendedCount);
            
            logger.info("Loaded {} restaurants for approval management", restaurants.size());
            
            return "admin/restaurant-requests";
            
        } catch (Exception e) {
            logger.error("Error loading restaurant requests page", e);
            model.addAttribute("error", "Lỗi khi tải dữ liệu: " + e.getMessage());
            model.addAttribute("restaurants", java.util.Collections.emptyList());
            model.addAttribute("filter", "PENDING");
            model.addAttribute("search", "");
            model.addAttribute("pendingCount", 0L);
            model.addAttribute("approvedCount", 0L);
            model.addAttribute("rejectedCount", 0L);
            model.addAttribute("suspendedCount", 0L);
            return "admin/restaurant-requests";
        }
    }
    
    /**
     * GET /admin/restaurant/requests/{id}
     * Chi tiết nhà hàng chờ duyệt
     */
    @GetMapping("/requests/{id}")
    public String restaurantRequestDetail(@PathVariable Integer id, Model model) {
        try {
            logger.info("Loading restaurant detail for ID: {}", id);
            
            Optional<RestaurantProfile> restaurantOpt = restaurantApprovalService.getRestaurantById(id);
            
            if (!restaurantOpt.isPresent()) {
                logger.warn("Restaurant not found with ID: {}", id);
                model.addAttribute("error", "Không tìm thấy nhà hàng với ID: " + id);
                return "admin/restaurant-request-detail";
            }
            
            RestaurantProfile restaurant = restaurantOpt.get();
            
            // Check if restaurant can be approved/rejected
            boolean canApprove = restaurant.canBeApproved();
            boolean canReject = restaurant.canBeRejected();
            boolean canSuspend = restaurant.canBeSuspended();
            
            model.addAttribute("restaurant", restaurant);
            model.addAttribute("canApprove", canApprove);
            model.addAttribute("canReject", canReject);
            model.addAttribute("canSuspend", canSuspend);
            
            logger.info("Loaded restaurant detail for: {}", restaurant.getRestaurantName());
            
            return "admin/restaurant-request-detail";
            
        } catch (Exception e) {
            logger.error("Error loading restaurant detail for ID: {}", id, e);
            model.addAttribute("error", "Lỗi khi tải thông tin nhà hàng: " + e.getMessage());
            return "admin/restaurant-request-detail";
        }
    }
    
    /**
     * POST /admin/restaurant/approve/{id}
     * Duyệt nhà hàng
     */
    @PostMapping("/approve/{id}")
    public String approveRestaurant(
        @PathVariable Integer id,
        @RequestParam(required = false) String approvalReason,
        Principal principal,
        RedirectAttributes redirectAttributes
    ) {
        try {
            logger.info("Approving restaurant ID: {} by admin: {}", id, principal.getName());
            
            boolean success = restaurantApprovalService.approveRestaurant(
                id, 
                principal.getName(), 
                approvalReason
            );
            
            if (success) {
                redirectAttributes.addFlashAttribute("success", 
                    "Đã duyệt nhà hàng thành công!");
                logger.info("Restaurant ID: {} approved successfully by admin: {}", id, principal.getName());
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "Không thể duyệt nhà hàng. Vui lòng kiểm tra lại thông tin.");
                logger.warn("Failed to approve restaurant ID: {}", id);
            }
            
        } catch (Exception e) {
            logger.error("Error approving restaurant ID: {}", id, e);
            redirectAttributes.addFlashAttribute("error", 
                "Lỗi khi duyệt nhà hàng: " + e.getMessage());
        }
        
        return "redirect:/admin/restaurant/requests/" + id;
    }
    
    /**
     * POST /admin/restaurant/reject/{id}
     * Từ chối nhà hàng
     */
    @PostMapping("/reject/{id}")
    public String rejectRestaurant(
        @PathVariable Integer id,
        @RequestParam(required = false) String rejectionReason,
        Principal principal,
        RedirectAttributes redirectAttributes
    ) {
        try {
            logger.info("=== REJECT RESTAURANT DEBUG ===");
            logger.info("Restaurant ID: {}", id);
            logger.info("Admin: {}", principal != null ? principal.getName() : "null");
            logger.info("Rejection reason: {}", rejectionReason);
            logger.info("Reason length: {}", rejectionReason != null ? rejectionReason.length() : "null");
            
            if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
                logger.warn("Rejection reason is empty or null");
                redirectAttributes.addFlashAttribute("error", 
                    "Vui lòng nhập lý do từ chối!");
                return "redirect:/admin/restaurant/requests/" + id;
            }
            
            logger.info("Calling restaurantApprovalService.rejectRestaurant...");
            boolean success = restaurantApprovalService.rejectRestaurant(
                id, 
                principal.getName(), 
                rejectionReason.trim()
            );
            
            logger.info("Service returned success: {}", success);
            
            if (success) {
                redirectAttributes.addFlashAttribute("success", 
                    "Đã từ chối nhà hàng thành công!");
                logger.info("Restaurant ID: {} rejected successfully by admin: {}", id, principal.getName());
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "Không thể từ chối nhà hàng. Vui lòng kiểm tra lại thông tin.");
                logger.warn("Failed to reject restaurant ID: {}", id);
            }
            
        } catch (Exception e) {
            logger.error("Error rejecting restaurant ID: {}", id, e);
            redirectAttributes.addFlashAttribute("error", 
                "Lỗi khi từ chối nhà hàng: " + e.getMessage());
        }
        
        return "redirect:/admin/restaurant/requests/" + id;
    }
    
    /**
     * GET /admin/test-reject-form
     * Test form từ chối (chỉ để debug)
     */
    @GetMapping("/test-reject-form")
    public String testRejectForm() {
        return "test/test-reject-form";
    }
    
    /**
     * POST /admin/restaurant/resubmit/{id}
     * Gửi lại cho restaurant để chỉnh sửa
     */
    @PostMapping("/resubmit/{id}")
    public String resubmitRestaurant(
        @PathVariable Integer id,
        @RequestParam String resubmitReason,
        Principal principal,
        RedirectAttributes redirectAttributes
    ) {
        try {
            logger.info("Resubmitting restaurant ID: {} by admin: {}", id, principal.getName());
            
            if (resubmitReason == null || resubmitReason.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", 
                    "Vui lòng nhập lý do gửi lại!");
                return "redirect:/admin/restaurant/requests/" + id;
            }
            
            boolean success = restaurantApprovalService.resubmitRestaurant(
                id, 
                principal.getName(), 
                resubmitReason.trim()
            );
            
            if (success) {
                redirectAttributes.addFlashAttribute("success", 
                    "Đã gửi lại nhà hàng cho restaurant owner thành công!");
                logger.info("Restaurant ID: {} resubmitted successfully by admin: {}", id, principal.getName());
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "Không thể gửi lại nhà hàng. Vui lòng kiểm tra lại thông tin.");
                logger.warn("Failed to resubmit restaurant ID: {}", id);
            }
            
        } catch (Exception e) {
            logger.error("Error resubmitting restaurant ID: {}", id, e);
            redirectAttributes.addFlashAttribute("error", 
                "Lỗi khi gửi lại nhà hàng: " + e.getMessage());
        }
        
        return "redirect:/admin/restaurant/requests/" + id;
    }
    
    /**
     * POST /admin/restaurant/suspend/{id}
     * Tạm dừng nhà hàng
     */
    @PostMapping("/suspend/{id}")
    public String suspendRestaurant(
        @PathVariable Integer id,
        @RequestParam(required = false) String suspensionReason,
        Principal principal,
        RedirectAttributes redirectAttributes
    ) {
        try {
            logger.info("Suspending restaurant ID: {} by admin: {}", id, principal.getName());
            
            boolean success = restaurantApprovalService.suspendRestaurant(
                id, 
                principal.getName(), 
                suspensionReason
            );
            
            if (success) {
                redirectAttributes.addFlashAttribute("success", 
                    "Đã tạm dừng nhà hàng thành công!");
                logger.info("Restaurant ID: {} suspended successfully by admin: {}", id, principal.getName());
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "Không thể tạm dừng nhà hàng. Vui lòng kiểm tra lại thông tin.");
                logger.warn("Failed to suspend restaurant ID: {}", id);
            }
            
        } catch (Exception e) {
            logger.error("Error suspending restaurant ID: {}", id, e);
            redirectAttributes.addFlashAttribute("error", 
                "Lỗi khi tạm dừng nhà hàng: " + e.getMessage());
        }
        
        return "redirect:/admin/restaurant/requests/" + id;
    }
    
    /**
     * POST /admin/restaurant/activate/{id}
     * Kích hoạt lại nhà hàng (từ suspended về approved)
     */
    @PostMapping("/activate/{id}")
    public String activateRestaurant(
        @PathVariable Integer id,
        @RequestParam(required = false) String activationReason,
        Principal principal,
        RedirectAttributes redirectAttributes
    ) {
        try {
            logger.info("Activating restaurant ID: {} by admin: {}", id, principal.getName());
            
            boolean success = restaurantApprovalService.activateRestaurant(
                id, 
                principal.getName(), 
                activationReason
            );
            
            if (success) {
                redirectAttributes.addFlashAttribute("success", 
                    "Đã kích hoạt lại nhà hàng thành công!");
                logger.info("Restaurant ID: {} activated successfully by admin: {}", id, principal.getName());
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "Không thể kích hoạt lại nhà hàng. Vui lòng kiểm tra lại thông tin.");
                logger.warn("Failed to activate restaurant ID: {}", id);
            }
            
        } catch (Exception e) {
            logger.error("Error activating restaurant ID: {}", id, e);
            redirectAttributes.addFlashAttribute("error", 
                "Lỗi khi kích hoạt lại nhà hàng: " + e.getMessage());
        }
        
        return "redirect:/admin/restaurant/requests/" + id;
    }
}
