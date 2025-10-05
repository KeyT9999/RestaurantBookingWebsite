package com.example.booking.web.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.booking.domain.Customer;
import com.example.booking.domain.Review;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.dto.ReviewDto;
import com.example.booking.dto.ReviewStatisticsDto;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.ReviewService;

@Controller
@RequestMapping("/restaurant-owner/reviews")
public class RestaurantReviewController {
    
    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private RestaurantOwnerService restaurantOwnerService;
    
    
    /**
     * Hiển thị trang quản lý review cho restaurant owner
     */
    @GetMapping
    public String manageReviews(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) Integer rating,
                              Model model,
                              Authentication authentication) {
        
        System.out.println("🔍 RestaurantReviewController.manageReviews() called");
        System.out.println("   Page: " + page + ", Size: " + size);
        System.out.println("   Rating filter: " + rating);
        
        try {
            User user = (User) authentication.getPrincipal();
            Optional<RestaurantOwner> ownerOpt = restaurantOwnerService.getRestaurantOwnerByUserId(user.getId());
            
            if (ownerOpt.isEmpty()) {
                model.addAttribute("error", "Restaurant owner profile not found");
                return "error/404";
            }
            
            RestaurantOwner owner = ownerOpt.get();
            
            // Lấy restaurant đầu tiên của owner (có thể mở rộng để hỗ trợ nhiều restaurant)
            List<RestaurantProfile> restaurants = restaurantOwnerService.getRestaurantsByOwnerId(owner.getOwnerId());
            if (restaurants.isEmpty()) {
                model.addAttribute("error", "No restaurants found for this owner");
                return "error/404";
            }
            
            RestaurantProfile restaurant = restaurants.get(0);
            Integer restaurantId = restaurant.getRestaurantId();
            
            // Lấy review theo filter
            List<ReviewDto> reviews;
            if (rating != null) {
                reviews = reviewService.getReviewsByRestaurantAndRating(restaurantId, rating);
            } else {
                Pageable pageable = PageRequest.of(page, size);
                Page<ReviewDto> reviewPage = reviewService.getReviewsByRestaurant(restaurantId, pageable);
                reviews = reviewPage.getContent();
                model.addAttribute("totalPages", reviewPage.getTotalPages());
                model.addAttribute("currentPage", page);
            }
            
            // Lấy thống kê review
            ReviewStatisticsDto statistics = reviewService.getRestaurantReviewStatistics(restaurantId);
            
            model.addAttribute("restaurant", restaurant);
            model.addAttribute("restaurantId", restaurantId);
            model.addAttribute("reviews", reviews);
            model.addAttribute("statistics", statistics);
            model.addAttribute("selectedRating", rating);
            model.addAttribute("pageTitle", "Quản lý đánh giá");
            
            return "restaurant-owner/reviews";
            
        } catch (Exception e) {
            System.err.println("❌ Error in manageReviews: " + e.getMessage());
            model.addAttribute("error", "Lỗi khi tải danh sách đánh giá: " + e.getMessage());
            return "restaurant-owner/reviews";
        }
    }
    
    /**
     * Hiển thị thống kê chi tiết review
     */
    @GetMapping("/statistics")
    public String reviewStatistics(Model model, Authentication authentication) {
        
        System.out.println("🔍 RestaurantReviewController.reviewStatistics() called");
        
        try {
            User user = (User) authentication.getPrincipal();
            Optional<RestaurantOwner> ownerOpt = restaurantOwnerService.getRestaurantOwnerByUserId(user.getId());
            
            if (ownerOpt.isEmpty()) {
                model.addAttribute("error", "Restaurant owner profile not found");
                return "error/404";
            }
            
            RestaurantOwner owner = ownerOpt.get();
            
            // Lấy restaurant đầu tiên của owner
            List<RestaurantProfile> restaurants = restaurantOwnerService.getRestaurantsByOwnerId(owner.getOwnerId());
            if (restaurants.isEmpty()) {
                model.addAttribute("error", "No restaurants found for this owner");
                return "error/404";
            }
            
            RestaurantProfile restaurant = restaurants.get(0);
            Integer restaurantId = restaurant.getRestaurantId();
            
            // Lấy thống kê chi tiết
            ReviewStatisticsDto statistics = reviewService.getRestaurantReviewStatistics(restaurantId);
            
            // Lấy review mới nhất
            List<ReviewDto> recentReviews = reviewService.getRecentReviewsByRestaurant(restaurantId, 10);
            
            model.addAttribute("restaurant", restaurant);
            model.addAttribute("statistics", statistics);
            model.addAttribute("recentReviews", recentReviews);
            model.addAttribute("pageTitle", "Thống kê đánh giá");
            
            return "restaurant-owner/review-statistics";
            
        } catch (Exception e) {
            System.err.println("❌ Error in reviewStatistics: " + e.getMessage());
            model.addAttribute("error", "Lỗi khi tải thống kê đánh giá: " + e.getMessage());
            return "restaurant-owner/review-statistics";
        }
    }
    
}
