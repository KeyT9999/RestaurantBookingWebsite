package com.example.booking.web.controller;

import java.util.List;

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

import com.example.booking.domain.RestaurantProfile;
import com.example.booking.repository.RestaurantProfileRepository;

/**
 * Simple Admin Controller for Restaurant Approval - Debug Version
 */
@Controller
@RequestMapping("/admin/simple")
@PreAuthorize("hasRole('ADMIN')")
public class SimpleAdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(SimpleAdminController.class);
    
    @Autowired
    private RestaurantProfileRepository restaurantProfileRepository;
    
    /**
     * GET /admin/simple/restaurants
     * Simple list of all restaurants
     */
    @GetMapping("/restaurants")
    public String simpleRestaurantList(Model model) {
        try {
            logger.info("Loading simple restaurant list");
            
            // Get all restaurants directly from repository
            List<RestaurantProfile> allRestaurants = restaurantProfileRepository.findAll();
            
            // Count by status
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
            
            // Filter only PENDING restaurants for display
            List<RestaurantProfile> pendingRestaurants = allRestaurants.stream()
                .filter(r -> r.getApprovalStatus() != null && r.getApprovalStatus().name().equals("PENDING"))
                .toList();
            
            logger.info("Found {} total restaurants, {} pending", allRestaurants.size(), pendingCount);
            logger.info("Pending restaurants: {}", pendingRestaurants.stream()
                .map(RestaurantProfile::getRestaurantName)
                .toList());
            
            model.addAttribute("restaurants", pendingRestaurants);
            model.addAttribute("pendingCount", pendingCount);
            model.addAttribute("approvedCount", approvedCount);
            model.addAttribute("rejectedCount", rejectedCount);
            model.addAttribute("suspendedCount", suspendedCount);
            
            return "admin/simple-restaurant-list";
            
        } catch (Exception e) {
            logger.error("Error loading simple restaurant list", e);
            model.addAttribute("error", "Lỗi khi tải dữ liệu: " + e.getMessage());
            model.addAttribute("restaurants", java.util.Collections.emptyList());
            model.addAttribute("pendingCount", 0L);
            model.addAttribute("approvedCount", 0L);
            model.addAttribute("rejectedCount", 0L);
            model.addAttribute("suspendedCount", 0L);
            return "admin/simple-restaurant-list";
        }
    }
    
    /**
     * POST /admin/simple/approve/{id}
     * Simple approve restaurant
     */
    @PostMapping("/approve/{id}")
    public String simpleApproveRestaurant(
        @PathVariable Integer id,
        RedirectAttributes redirectAttributes
    ) {
        try {
            logger.info("Approving restaurant ID: {}", id);
            
            RestaurantProfile restaurant = restaurantProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
            
            restaurant.setApprovalStatus(com.example.booking.common.enums.RestaurantApprovalStatus.APPROVED);
            restaurant.setApprovalReason("Được duyệt bởi admin");
            restaurant.setApprovedBy("Admin");
            restaurant.setApprovedAt(java.time.LocalDateTime.now());
            
            restaurantProfileRepository.save(restaurant);
            
            logger.info("Successfully approved restaurant: {}", restaurant.getRestaurantName());
            redirectAttributes.addFlashAttribute("success", "Đã duyệt nhà hàng: " + restaurant.getRestaurantName());
            
        } catch (Exception e) {
            logger.error("Error approving restaurant ID: {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi duyệt nhà hàng: " + e.getMessage());
        }
        
        return "redirect:/admin/simple/restaurants";
    }
    
    /**
     * POST /admin/simple/reject/{id}
     * Simple reject restaurant
     */
    @PostMapping("/reject/{id}")
    public String simpleRejectRestaurant(
        @PathVariable Integer id,
        @RequestParam(required = false) String reason,
        RedirectAttributes redirectAttributes
    ) {
        try {
            logger.info("Rejecting restaurant ID: {} with reason: {}", id, reason);
            
            RestaurantProfile restaurant = restaurantProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
            
            restaurant.setApprovalStatus(com.example.booking.common.enums.RestaurantApprovalStatus.REJECTED);
            restaurant.setRejectionReason(reason != null ? reason : "Không đáp ứng yêu cầu");
            restaurant.setApprovedBy("Admin");
            restaurant.setApprovedAt(java.time.LocalDateTime.now());
            
            restaurantProfileRepository.save(restaurant);
            
            logger.info("Successfully rejected restaurant: {}", restaurant.getRestaurantName());
            redirectAttributes.addFlashAttribute("success", "Đã từ chối nhà hàng: " + restaurant.getRestaurantName());
            
        } catch (Exception e) {
            logger.error("Error rejecting restaurant ID: {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi từ chối nhà hàng: " + e.getMessage());
        }
        
        return "redirect:/admin/simple/restaurants";
    }
}
