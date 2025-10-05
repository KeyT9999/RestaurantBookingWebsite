package com.example.booking.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantMedia;
import com.example.booking.domain.Dish;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.domain.Customer;
import com.example.booking.domain.User;
import com.example.booking.dto.ReviewDto;
import com.example.booking.dto.ReviewStatisticsDto;
import com.example.booking.dto.ReviewForm;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.CustomerService;
import com.example.booking.service.ReviewService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller for handling home page and static pages
 */
@Controller
public class HomeController {
    
    @Autowired
    private RestaurantOwnerService restaurantOwnerService;
    
    @Autowired
    private CustomerService customerService;

    @Autowired
    private ReviewService reviewService;

    /**
     * Home page - main landing page
     * Shows home page for all users, with additional options for authenticated users
     */
    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        // Add any model attributes needed for home page
        model.addAttribute("pageTitle", "Aurelius Fine Dining - Experience Culinary Excellence");
        
        // Add user role information for conditional display
        if (authentication != null && authentication.isAuthenticated()) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                String role = authority.getAuthority();
                if ("ROLE_ADMIN".equals(role)) {
                    model.addAttribute("userRole", "ADMIN");
                    break;
                } else if ("ROLE_RESTAURANT_OWNER".equals(role)) {
                    model.addAttribute("userRole", "RESTAURANT_OWNER");
                    break;
                }
            }
        }
        
        return "home";
    }
    
    /**
     * About page
     */
    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("pageTitle", "About Us - Aurelius Fine Dining");
        return "about";
    }
    
    /**
     * Contact page  
     */
    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("pageTitle", "Contact Us - Aurelius Fine Dining");
        return "contact";
    }
    
    /**
     * Restaurants listing page
     */
    @GetMapping("/restaurants")
    public String restaurants(Model model) {
        model.addAttribute("pageTitle", "Our Restaurants - Aurelius Fine Dining");
        
        // Get all restaurants from database
        List<RestaurantProfile> restaurants = restaurantOwnerService.getAllRestaurants();
        model.addAttribute("restaurants", restaurants);
        
        return "restaurants";
    }
    
    @GetMapping("/restaurants/{id}")
    public String restaurantDetail(@PathVariable Integer id, Model model, Authentication authentication) {
        try {
            // Get restaurant details
            var restaurantOpt = restaurantOwnerService.getRestaurantById(id);
            
            if (restaurantOpt.isEmpty()) {
                return "redirect:/restaurants?error=notfound";
            }
            
            RestaurantProfile restaurant = restaurantOpt.get();
            
            // Get restaurant media
            List<RestaurantMedia> allMedia = restaurantOwnerService.getMediaByRestaurant(restaurant);
            
            // Organize media by type
            List<RestaurantMedia> logos = allMedia.stream()
                .filter(m -> "logo".equalsIgnoreCase(m.getType()))
                .toList();
            List<RestaurantMedia> covers = allMedia.stream()
                .filter(m -> "cover".equalsIgnoreCase(m.getType()))
                .toList();
            List<RestaurantMedia> gallery = allMedia.stream()
                .filter(m -> "gallery".equalsIgnoreCase(m.getType()))
                .toList();
            List<RestaurantMedia> menus = allMedia.stream()
                .filter(m -> "menu".equalsIgnoreCase(m.getType()))
                .toList();
            
            // Get dishes
            List<Dish> dishes = restaurant.getDishes() != null ? restaurant.getDishes() : new ArrayList<>();
            
            // Get tables
            List<RestaurantTable> tables = restaurant.getTables() != null ? restaurant.getTables() : new ArrayList<>();
            
            // Review-related data
            boolean hasReviewed = false;
            ReviewDto customerReview = null;
            List<ReviewDto> recentReviews = new ArrayList<>();
            ReviewStatisticsDto statistics = null;
            long totalReviews = 0;

            try {
                // Check if user has reviewed this restaurant
                if (authentication != null && authentication.isAuthenticated()) {
                    User user = (User) authentication.getPrincipal();
                    Optional<Customer> customerOpt = customerService.findByUserId(user.getId());
                    if (customerOpt.isPresent()) {
                        hasReviewed = reviewService.hasCustomerReviewedRestaurant(customerOpt.get().getCustomerId(),
                                id);
                        if (hasReviewed) {
                            // Get customer's review for this restaurant
                            List<ReviewDto> customerReviews = reviewService
                                    .getReviewsByCustomer(customerOpt.get().getCustomerId());
                            Optional<ReviewDto> customerReviewOpt = customerReviews.stream()
                                    .filter(r -> r.getRestaurantId().equals(id))
                                    .findFirst();
                            if (customerReviewOpt.isPresent()) {
                                customerReview = customerReviewOpt.get();
                            }
                        }
                    }
                }

                // Get recent reviews (3-5 reviews)
                Pageable pageable = PageRequest.of(0, 5);
                Page<ReviewDto> recentReviewsPage = reviewService.getReviewsByRestaurant(id, pageable);
                recentReviews = recentReviewsPage.getContent();

                // Get review statistics
                statistics = reviewService.getRestaurantReviewStatistics(id);
                totalReviews = recentReviewsPage.getTotalElements();

            } catch (Exception e) {
                // If review service fails, continue without review data
                System.err.println("Review service error: " + e.getMessage());
            }

            // Add to model
            model.addAttribute("pageTitle", restaurant.getRestaurantName() + " - Chi tiết Nhà hàng");
            model.addAttribute("restaurant", restaurant);
            model.addAttribute("logo", logos.isEmpty() ? null : logos.get(0));
            model.addAttribute("cover", covers.isEmpty() ? null : covers.get(0));
            model.addAttribute("gallery", gallery);
            model.addAttribute("menus", menus);
            model.addAttribute("dishes", dishes);
            model.addAttribute("tables", tables);
            
            // Review data
            model.addAttribute("hasReviewed", hasReviewed);
            model.addAttribute("customerReview", customerReview);
            model.addAttribute("recentReviews", recentReviews);
            model.addAttribute("statistics", statistics);
            model.addAttribute("totalReviews", totalReviews);

            // Add ReviewForm for new reviews
            ReviewForm reviewForm = new ReviewForm();
            reviewForm.setRestaurantId(id);
            model.addAttribute("reviewForm", reviewForm);

            return "restaurant-detail";
            
        } catch (Exception e) {
            return "redirect:/restaurants?error=" + e.getMessage();
        }
    }
}
