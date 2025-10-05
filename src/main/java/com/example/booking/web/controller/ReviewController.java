package com.example.booking.web.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booking.domain.Customer;
import com.example.booking.domain.Review;
import com.example.booking.domain.User;
import com.example.booking.dto.ReviewDto;
import com.example.booking.dto.ReviewForm;
import com.example.booking.dto.ReviewStatisticsDto;
import com.example.booking.service.CustomerService;
import com.example.booking.service.ReviewService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/reviews")
public class ReviewController {
    
    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private CustomerService customerService;
    
    /**
     * Hiển thị danh sách review của restaurant
     */
    @GetMapping("/restaurant/{restaurantId}")
    public String getRestaurantReviews(@PathVariable Integer restaurantId,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     @RequestParam(required = false) Integer rating,
                                     Model model,
                                     Authentication authentication) {
        
        try {
            // Load reviews data
            loadReviewsData(restaurantId, page, size, rating, model);
            
            // Load customer review status
            loadCustomerReviewStatus(restaurantId, authentication, model);
            
            // Set common attributes
            setCommonAttributes(restaurantId, rating, model);
            
            return "review/list";
            
        } catch (Exception e) {
            handleError("Lỗi khi tải danh sách đánh giá", e, model);
            return "review/list";
        }
    }
    
    /**
     * Load reviews data based on filters
     */
    private void loadReviewsData(Integer restaurantId, int page, int size, Integer rating, Model model) {
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
        
        ReviewStatisticsDto statistics = reviewService.getRestaurantReviewStatistics(restaurantId);
        
        model.addAttribute("reviews", reviews);
        model.addAttribute("statistics", statistics);
    }
    
    /**
     * Load customer review status and existing review
     */
    private void loadCustomerReviewStatus(Integer restaurantId, Authentication authentication, Model model) {
        if (authentication == null) {
            model.addAttribute("hasReviewed", false);
            model.addAttribute("customerReview", null);
            return;
        }
        
        User user = (User) authentication.getPrincipal();
        Optional<Customer> customerOpt = customerService.findByUserId(user.getId());
        
        if (customerOpt.isEmpty()) {
            model.addAttribute("hasReviewed", false);
            model.addAttribute("customerReview", null);
            return;
        }
        
        Customer customer = customerOpt.get();
        boolean hasReviewed = reviewService.hasCustomerReviewedRestaurant(customer.getCustomerId(), restaurantId);
        ReviewDto customerReview = null;
        
        if (hasReviewed) {
            customerReview = reviewService.getCustomerReviewForRestaurant(customer.getCustomerId(), restaurantId)
                    .orElse(null);
        }
        
        model.addAttribute("hasReviewed", hasReviewed);
        model.addAttribute("customerReview", customerReview);
    }
    
    /**
     * Set common attributes for review pages
     */
    private void setCommonAttributes(Integer restaurantId, Integer rating, Model model) {
        model.addAttribute("restaurantId", restaurantId);
        model.addAttribute("selectedRating", rating);
        model.addAttribute("pageTitle", "Đánh giá nhà hàng");
    }
    
    /**
     * Handle errors consistently
     */
    private void handleError(String message, Exception e, Model model) {
        System.err.println("❌ Error: " + message + " - " + e.getMessage());
        model.addAttribute("error", message + ": " + e.getMessage());
    }
    
    /**
     * Redirect to restaurant detail page for review creation
     */
    @GetMapping("/create/{restaurantId}")
    public String showCreateReviewForm(@PathVariable Integer restaurantId,
                                     Authentication authentication) {
        
        if (authentication == null) {
            return "redirect:/login?redirect=/restaurants/" + restaurantId;
        }
        
        return "redirect:/restaurants/" + restaurantId + "#reviews";
    }
    
    /**
     * Handle review submission from restaurant detail page
     */
    @PostMapping
    public String handleReviewSubmission(@Valid @ModelAttribute("reviewForm") ReviewForm form,
                                        BindingResult bindingResult,
                                        Authentication authentication,
                                        RedirectAttributes redirectAttributes) {
        
        
        if (authentication == null) {
            return "redirect:/login";
        }
        
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng kiểm tra lại thông tin đánh giá.");
            return "redirect:/restaurants/" + form.getRestaurantId();
        }
        
        try {
            User user = (User) authentication.getPrincipal();
            Optional<Customer> customerOpt = customerService.findByUserId(user.getId());
            
            if (customerOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Customer profile not found");
                return "redirect:/restaurants/" + form.getRestaurantId();
            }
            
            Customer customer = customerOpt.get();
            
            // Create or update review
            reviewService.createOrUpdateReview(form, customer.getCustomerId());
            
            redirectAttributes.addFlashAttribute("success", "Đánh giá đã được lưu thành công!");
            return "redirect:/restaurants/" + form.getRestaurantId();
            
        } catch (Exception e) {
            System.err.println("❌ Error in handleReviewSubmission: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Lỗi khi lưu đánh giá: " + e.getMessage());
            return "redirect:/restaurants/" + form.getRestaurantId();
        }
    }
    
    /**
     * Redirect to restaurant detail page for review editing
     */
    @GetMapping("/edit/{reviewId}")
    public String showEditReviewForm(@PathVariable Integer reviewId,
                                   Authentication authentication) {
        
        if (authentication == null) {
            return "redirect:/login";
        }
        
        try {
            User user = (User) authentication.getPrincipal();
            Optional<Customer> customerOpt = customerService.findByUserId(user.getId());
            
            if (customerOpt.isEmpty()) {
                return "redirect:/error/404";
            }
            
            Customer customer = customerOpt.get();
            
            // Lấy review
            Optional<Review> reviewOpt = reviewService.getReviewById(reviewId);
            if (reviewOpt.isEmpty()) {
                return "redirect:/error/404";
            }
            
            Review review = reviewOpt.get();
            
            // Kiểm tra quyền sở hữu
            if (!review.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
                return "redirect:/error/403";
            }
            
            // Kiểm tra có thể chỉnh sửa không
            if (!review.isEditable()) {
                return "redirect:/error/400";
            }
            
            // Redirect to restaurant detail page where the review form is integrated
            return "redirect:/restaurants/" + review.getRestaurant().getRestaurantId() + "#reviews";
            
        } catch (Exception e) {
            return "redirect:/error/500";
        }
    }
    
    /**
     * Xử lý chỉnh sửa review
     */
    @PostMapping("/edit/{reviewId}")
    public String editReview(@PathVariable Integer reviewId,
                           @Valid @ModelAttribute("reviewForm") ReviewForm form,
                           BindingResult bindingResult,
                           Model model,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        
        if (authentication == null) {
            return "redirect:/login";
        }
        
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng kiểm tra lại thông tin đánh giá.");
            return "redirect:/restaurants/" + form.getRestaurantId() + "#reviews";
        }
        
        try {
            User user = (User) authentication.getPrincipal();
            Optional<Customer> customerOpt = customerService.findByUserId(user.getId());
            
            if (customerOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Customer profile not found");
                return "redirect:/restaurants/" + form.getRestaurantId() + "#reviews";
            }
            
            Customer customer = customerOpt.get();
            
            // Kiểm tra quyền sở hữu
            Optional<Review> reviewOpt = reviewService.getReviewById(reviewId);
            if (reviewOpt.isEmpty()) {
                model.addAttribute("error", "Review not found");
                return "error/404";
            }
            
            Review review = reviewOpt.get();
            if (!review.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
                model.addAttribute("error", "You can only edit your own reviews");
                return "error/403";
            }
            
            // Cập nhật review
            reviewService.createOrUpdateReview(form, customer.getCustomerId());
            
            redirectAttributes.addFlashAttribute("success", "Đánh giá đã được cập nhật thành công!");
            return "redirect:/restaurants/" + form.getRestaurantId() + "#reviews";
            
        } catch (Exception e) {
            System.err.println("❌ Error in editReview: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật đánh giá: " + e.getMessage());
            return "redirect:/restaurants/" + form.getRestaurantId() + "#reviews";
        }
    }
    
    /**
     * Xóa review
     */
    @PostMapping("/delete/{reviewId}")
    public String deleteReview(@PathVariable Integer reviewId,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        
        System.out.println("🔍 ReviewController.deleteReview() called");
        System.out.println("   Review ID: " + reviewId);
        
        if (authentication == null) {
            return "redirect:/login";
        }
        
        try {
            User user = (User) authentication.getPrincipal();
            Optional<Customer> customerOpt = customerService.findByUserId(user.getId());
            
            if (customerOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Customer profile not found");
                return "redirect:/";
            }
            
            Customer customer = customerOpt.get();
            
            // Lấy restaurant ID trước khi xóa
            Optional<Review> reviewOpt = reviewService.getReviewById(reviewId);
            if (reviewOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Review not found");
                return "redirect:/";
            }
            
            Integer restaurantId = reviewOpt.get().getRestaurant().getRestaurantId();
            
            // Xóa review
            reviewService.deleteReview(reviewId, customer.getCustomerId());
            
            redirectAttributes.addFlashAttribute("success", "Đánh giá đã được xóa thành công!");
            return "redirect:/reviews/restaurant/" + restaurantId;
            
        } catch (Exception e) {
            System.err.println("❌ Error in deleteReview: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa đánh giá: " + e.getMessage());
            return "redirect:/";
        }
    }
    
    /**
     * Hiển thị review của customer
     */
    @GetMapping("/my-reviews")
    public String getMyReviews(Model model, Authentication authentication) {
        
        System.out.println("🔍 ReviewController.getMyReviews() called");
        
        if (authentication == null) {
            return "redirect:/login";
        }
        
        try {
            User user = (User) authentication.getPrincipal();
            Optional<Customer> customerOpt = customerService.findByUserId(user.getId());
            
            if (customerOpt.isEmpty()) {
                model.addAttribute("error", "Customer profile not found");
                return "error/404";
            }
            
            Customer customer = customerOpt.get();
            List<ReviewDto> reviews = reviewService.getReviewsByCustomer(customer.getCustomerId());
            
            model.addAttribute("reviews", reviews);
            model.addAttribute("pageTitle", "Đánh giá của tôi");
            
            return "review/my-reviews";
            
        } catch (Exception e) {
            System.err.println("❌ Error in getMyReviews: " + e.getMessage());
            model.addAttribute("error", "Lỗi khi tải đánh giá: " + e.getMessage());
            return "review/my-reviews";
        }
    }
}
