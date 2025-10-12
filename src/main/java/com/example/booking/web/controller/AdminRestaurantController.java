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
            
            // Get restaurant requests based on filter
            List<RestaurantProfile> restaurants;
            if (status != null && !status.isEmpty() && !status.equals("ALL")) {
                RestaurantApprovalStatus approvalStatus = RestaurantApprovalStatus.valueOf(status);
                restaurants = restaurantApprovalService.getRestaurantsByApprovalStatus(approvalStatus);
                model.addAttribute("filter", status);
            } else {
                // Get all restaurants with approval info
                restaurants = restaurantApprovalService.getAllRestaurantsWithApprovalInfo();
                model.addAttribute("filter", "ALL");
            }
            
            // Apply search filter if provided
            if (search != null && !search.trim().isEmpty()) {
                restaurants = restaurantApprovalService.searchRestaurants(restaurants, search.trim());
                model.addAttribute("search", search);
            }
            
            // Get statistics
            long pendingCount = restaurantApprovalService.getPendingRestaurantCount();
            long approvedCount = restaurantApprovalService.getApprovedRestaurantCount();
            long rejectedCount = restaurantApprovalService.getRejectedRestaurantCount();
            long suspendedCount = restaurantApprovalService.getSuspendedRestaurantCount();
            
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
            model.addAttribute("filter", "ALL");
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
        @RequestParam String rejectionReason,
        Principal principal,
        RedirectAttributes redirectAttributes
    ) {
        try {
            logger.info("Rejecting restaurant ID: {} by admin: {}", id, principal.getName());
            
            if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", 
                    "Vui lòng nhập lý do từ chối!");
                return "redirect:/admin/restaurant/requests/" + id;
            }
            
            boolean success = restaurantApprovalService.rejectRestaurant(
                id, 
                principal.getName(), 
                rejectionReason.trim()
            );
            
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
