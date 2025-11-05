package com.example.booking.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booking.domain.Review;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.ReviewReportStatus;
import com.example.booking.dto.ReviewDto;
import com.example.booking.dto.ReviewReportForm;
import com.example.booking.dto.ReviewReportView;
import com.example.booking.dto.ReviewStatisticsDto;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.ReviewReportService;
import com.example.booking.service.ReviewService;
import com.example.booking.util.InputSanitizer;

@Controller
@RequestMapping("/restaurant-owner/reviews")
public class RestaurantReviewController {
    
    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private RestaurantOwnerService restaurantOwnerService;

    @Autowired
    private ReviewReportService reviewReportService;
    
    @Autowired
    private InputSanitizer inputSanitizer;
    
    /**
     * Hiển thị trang quản lý review cho restaurant owner
     */
    @GetMapping
    public String manageReviews(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Integer restaurantId,
                              Model model,
                              Authentication authentication) {
        
        System.out.println("🔍 RestaurantReviewController.manageReviews() called");
        System.out.println("   Page: " + page + ", Size: " + size);
        System.out.println("   Rating filter: " + rating);
        System.out.println("   RestaurantId: " + restaurantId);
        
        try {
            User user = (User) authentication.getPrincipal();
            Optional<RestaurantOwner> ownerOpt = restaurantOwnerService.getRestaurantOwnerByUserId(user.getId());
            
            if (ownerOpt.isEmpty()) {
                model.addAttribute("error", "Restaurant owner profile not found");
                return "error/404";
            }
            
            RestaurantOwner owner = ownerOpt.get();
            
            // Get all restaurants owned by current user
            List<RestaurantProfile> restaurants = restaurantOwnerService.getRestaurantsByOwnerId(owner.getOwnerId());
            if (restaurants.isEmpty()) {
                model.addAttribute("error", "No restaurants found for this owner");
                return "error/404";
            }
            
            // Add restaurants list to model for header
            model.addAttribute("restaurants", restaurants != null ? restaurants : new ArrayList<>());

            // Get restaurant - use restaurantId from param if provided, otherwise use first
            // restaurant
            RestaurantProfile restaurant;
            Integer finalRestaurantId;

            if (restaurantId != null) {
                // Find restaurant by ID from the owner's restaurants
                Optional<RestaurantProfile> restaurantOpt = restaurants.stream()
                        .filter(r -> r.getRestaurantId().equals(restaurantId))
                        .findFirst();

                if (restaurantOpt.isEmpty()) {
                    model.addAttribute("error", "Bạn không có quyền truy cập nhà hàng này.");
                    // Fallback to first restaurant
                    restaurant = restaurants.get(0);
                    finalRestaurantId = restaurant.getRestaurantId();
                } else {
                    restaurant = restaurantOpt.get();
                    finalRestaurantId = restaurantId;
                }
            } else {
                // Use first restaurant if no restaurantId provided
                restaurant = restaurants.get(0);
                finalRestaurantId = restaurant.getRestaurantId();
            }
            
            // Lấy review theo filter
            List<ReviewDto> reviews;
            if (rating != null) {
                reviews = reviewService.getReviewsByRestaurantAndRating(finalRestaurantId, rating);
            } else {
                Pageable pageable = PageRequest.of(page, size);
                Page<ReviewDto> reviewPage = reviewService.getReviewsByRestaurant(finalRestaurantId, pageable);
                reviews = reviewPage.getContent();
                model.addAttribute("totalPages", reviewPage.getTotalPages());
                model.addAttribute("currentPage", page);
            }
            
            // Lấy thống kê review
            ReviewStatisticsDto statistics = reviewService.getRestaurantReviewStatistics(finalRestaurantId);

            // Lấy trạng thái report cho từng review
            Map<Integer, ReviewReportView> reportStatusMap = new HashMap<>();
            for (ReviewDto reviewDto : reviews) {
                reviewReportService.findLatestReportForReview(reviewDto.getReviewId())
                        .filter(report -> report.getStatus() == ReviewReportStatus.PENDING)
                        .ifPresent(report -> reportStatusMap.put(reviewDto.getReviewId(), report));
            }

            model.addAttribute("restaurant", restaurant);
            model.addAttribute("currentRestaurant", restaurant);
            model.addAttribute("restaurantId", finalRestaurantId);
            model.addAttribute("reviews", reviews);
            model.addAttribute("statistics", statistics);
            model.addAttribute("selectedRating", rating);
            model.addAttribute("reportStatusMap", reportStatusMap);
            model.addAttribute("pageTitle", "Quản lý đánh giá");
            
            return "restaurant-owner/reviews";
            
        } catch (Exception e) {
            System.err.println("❌ Error in manageReviews: " + e.getMessage());
            model.addAttribute("error", "Lỗi khi tải danh sách đánh giá: " + e.getMessage());
            return "restaurant-owner/reviews";
        }
    }

    @PostMapping("/report")
    public String reportReview(@RequestParam Integer reviewId,
            @RequestParam Integer restaurantId,
            @RequestParam String reasonText,
            @RequestParam(name = "evidenceFiles", required = false) List<MultipartFile> evidenceFiles,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return "redirect:/login";
        }

        try {
            User user = (User) authentication.getPrincipal();
            Optional<RestaurantOwner> ownerOpt = restaurantOwnerService.getRestaurantOwnerByUserId(user.getId());

            if (ownerOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin nhà hàng của bạn");
                return "redirect:/restaurant-owner/reviews";
            }

            RestaurantOwner owner = ownerOpt.get();

            Optional<RestaurantProfile> restaurantOpt = restaurantOwnerService
                    .getRestaurantsByOwnerId(owner.getOwnerId())
                    .stream()
                    .filter(r -> r.getRestaurantId().equals(restaurantId))
                    .findFirst();

            if (restaurantOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền quản lý nhà hàng này");
                return "redirect:/restaurant-owner/reviews";
            }

            Review review = reviewService.getReviewById(reviewId)
                    .orElseThrow(() -> new IllegalArgumentException("Review không tồn tại"));

            if (!review.getRestaurant().getRestaurantId().equals(restaurantId)) {
                redirectAttributes.addFlashAttribute("error", "Review không thuộc nhà hàng của bạn");
                return "redirect:/restaurant-owner/reviews";
            }

            // Sanitize report reason to prevent XSS
            String sanitizedReason = inputSanitizer.sanitizeReportReason(reasonText);
            if (sanitizedReason == null || sanitizedReason.isEmpty()) {
                throw new IllegalArgumentException("Vui lòng nhập lý do báo cáo");
            }

            List<MultipartFile> sanitizedFiles = new ArrayList<>();
            if (evidenceFiles != null) {
                for (MultipartFile file : evidenceFiles) {
                    if (file != null && !file.isEmpty()) {
                        if (file.getSize() > 5 * 1024 * 1024L) {
                            throw new IllegalArgumentException("Mỗi minh chứng không được vượt quá 5MB");
                        }
                        sanitizedFiles.add(file);
                        if (sanitizedFiles.size() == 3) {
                            break;
                        }
                    }
                }
            }

            ReviewReportForm form = new ReviewReportForm();
            form.setReasonText(sanitizedReason);
            form.setEvidenceFiles(sanitizedFiles);

            reviewReportService.submitReport(owner, restaurantOpt.get(), review, form);

            redirectAttributes.addFlashAttribute("success", "Báo cáo của bạn đã được gửi tới quản trị viên");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể gửi báo cáo: " + e.getMessage());
        }

        return "redirect:/restaurant-owner/reviews?restaurantId=" + restaurantId;
    }
    
    /**
     * Hiển thị thống kê chi tiết review
     */
    @GetMapping("/statistics")
    public String reviewStatistics(@RequestParam(required = false) Integer restaurantId,
            Model model,
            Authentication authentication) {
        
        System.out.println("🔍 RestaurantReviewController.reviewStatistics() called");
        
        try {
            User user = (User) authentication.getPrincipal();
            Optional<RestaurantOwner> ownerOpt = restaurantOwnerService.getRestaurantOwnerByUserId(user.getId());
            
            if (ownerOpt.isEmpty()) {
                model.addAttribute("error", "Restaurant owner profile not found");
                return "error/404";
            }
            
            RestaurantOwner owner = ownerOpt.get();
            
            // Get all restaurants owned by current user
            List<RestaurantProfile> restaurants = restaurantOwnerService.getRestaurantsByOwnerId(owner.getOwnerId());
            if (restaurants.isEmpty()) {
                model.addAttribute("error", "No restaurants found for this owner");
                return "error/404";
            }
            
            // Add restaurants list to model for header
            model.addAttribute("restaurants", restaurants != null ? restaurants : new ArrayList<>());

            // Get restaurant - use restaurantId from param if provided, otherwise use first
            // restaurant
            RestaurantProfile restaurant;
            Integer finalRestaurantId;

            if (restaurantId != null) {
                // Find restaurant by ID from the owner's restaurants
                Optional<RestaurantProfile> restaurantOpt = restaurants.stream()
                        .filter(r -> r.getRestaurantId().equals(restaurantId))
                        .findFirst();

                if (restaurantOpt.isEmpty()) {
                    model.addAttribute("error", "Bạn không có quyền truy cập nhà hàng này.");
                    // Fallback to first restaurant
                    restaurant = restaurants.get(0);
                    finalRestaurantId = restaurant.getRestaurantId();
                } else {
                    restaurant = restaurantOpt.get();
                    finalRestaurantId = restaurantId;
                }
            } else {
                // Use first restaurant if no restaurantId provided
                restaurant = restaurants.get(0);
                finalRestaurantId = restaurant.getRestaurantId();
            }
            
            // Lấy thống kê chi tiết
            ReviewStatisticsDto statistics = reviewService.getRestaurantReviewStatistics(finalRestaurantId);
            
            // Lấy review mới nhất
            List<ReviewDto> recentReviews = reviewService.getRecentReviewsByRestaurant(finalRestaurantId, 10);
            
            model.addAttribute("restaurant", restaurant);
            model.addAttribute("currentRestaurant", restaurant);
            model.addAttribute("restaurantId", finalRestaurantId);
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
