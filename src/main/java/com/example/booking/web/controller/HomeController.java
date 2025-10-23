package com.example.booking.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantMedia;
import com.example.booking.domain.Dish;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.domain.Customer;
import com.example.booking.domain.User;
import com.example.booking.dto.DishWithImageDto;
import com.example.booking.dto.PopularRestaurantDto;
import com.example.booking.dto.ReviewDto;
import com.example.booking.dto.ReviewForm;
import com.example.booking.dto.ReviewStatisticsDto;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.RestaurantManagementService;
import com.example.booking.service.CustomerService;
import com.example.booking.service.ReviewService;
import com.example.booking.service.NotificationService;
import com.example.booking.repository.RestaurantMediaRepository;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for handling home page and static pages
 */
@Controller
public class HomeController {
    
    private static final String[] POPULAR_CARD_GRADIENTS = {
            "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
            "linear-gradient(135deg, #43cea2 0%, #185a9d 100%)",
            "linear-gradient(135deg, #ff758c 0%, #ff7eb3 100%)",
            "linear-gradient(135deg, #f9d423 0%, #ff4e50 100%)"
    };
    
    @Autowired
    private RestaurantOwnerService restaurantOwnerService;
    
    @Autowired
    private RestaurantManagementService restaurantService;
    
    @Autowired
    private CustomerService customerService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private RestaurantMediaRepository restaurantMediaRepository;
    
    @Autowired
    private NotificationService notificationService;

    /**
     * Home page - main landing page
     * Shows home page for all users, with additional options for authenticated users
     */
    @GetMapping("/")
    public String home(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String cuisineType,
            @RequestParam(required = false) String priceRange,
            Model model, 
            Authentication authentication) {
        // Add any model attributes needed for home page
        model.addAttribute("pageTitle", "Book Eat - Đặt bàn online, giữ chỗ ngay");
        
        // Set active navigation
        model.addAttribute("activeNav", "home");
        
        // Pass search parameters to template for form persistence
        model.addAttribute("search", search);
        model.addAttribute("cuisineType", cuisineType);
        model.addAttribute("priceRange", priceRange);
        
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
            
            // Add notification count for authenticated users
            try {
                User user = (User) authentication.getPrincipal();
                long unreadCount = notificationService.countUnreadByUserId(user.getId());
                model.addAttribute("unreadCount", unreadCount);
            } catch (Exception e) {
                System.err.println("Error loading notification count: " + e.getMessage());
                model.addAttribute("unreadCount", 0L);
            }
        }

        // Popular restaurants for home page
        List<RestaurantProfile> topRestaurants = restaurantService.findTopRatedRestaurants(6);
        List<PopularRestaurantDto> popularRestaurants = buildPopularRestaurantCards(topRestaurants);
        model.addAttribute("popularRestaurants", popularRestaurants);
        
        return "public/home";
    }
    
    private List<PopularRestaurantDto> buildPopularRestaurantCards(List<RestaurantProfile> restaurants) {
        if (restaurants == null || restaurants.isEmpty()) {
            return Collections.emptyList();
        }

        List<RestaurantMedia> coverMedia = restaurantMediaRepository.findByRestaurantsAndType(restaurants, "cover");
        Map<Integer, String> coverMap = coverMedia.stream()
                .collect(Collectors.toMap(
                        media -> media.getRestaurant().getRestaurantId(),
                        RestaurantMedia::getUrl,
                        (existing, ignored) -> existing,
                        LinkedHashMap::new));

        List<PopularRestaurantDto> cards = new ArrayList<>();
        for (int i = 0; i < restaurants.size(); i++) {
            RestaurantProfile restaurant = restaurants.get(i);
            ReviewStatisticsDto statistics = null;
            try {
                statistics = reviewService.getRestaurantReviewStatistics(restaurant.getRestaurantId());
            } catch (Exception ex) {
                System.err.println("⚠️ Unable to load review statistics for restaurant "
                        + restaurant.getRestaurantId() + ": " + ex.getMessage());
            }

            double averageRating = statistics != null ? statistics.getAverageRating() : restaurant.getAverageRating();
            int reviewCount = statistics != null ? statistics.getTotalReviews() : restaurant.getReviewCount();

            cards.add(new PopularRestaurantDto(
                    restaurant.getRestaurantId(),
                    restaurant.getRestaurantName(),
                    restaurant.getCuisineType(),
                    restaurant.getAddress(),
                    averageRating,
                    reviewCount,
                    resolvePriceLabel(restaurant),
                    resolveBadge(i, reviewCount),
                    coverMap.get(restaurant.getRestaurantId()),
                    POPULAR_CARD_GRADIENTS[i % POPULAR_CARD_GRADIENTS.length]
            ));
        }

        return cards;
    }

    private String resolveBadge(int index, int reviewCount) {
        if (index == 0) {
            return "Top Rated";
        }
        if (index == 1) {
            return "Khách yêu thích";
        }
        if (index == 2) {
            return "Được đặt nhiều";
        }
        return reviewCount >= 10 ? "Yêu thích" : null;
    }

    private String resolvePriceLabel(RestaurantProfile restaurant) {
        if (restaurant.getAveragePrice() == null) {
            return "Giá đang cập nhật";
        }
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        currencyFormat.setMaximumFractionDigits(0);
        currencyFormat.setMinimumFractionDigits(0);
        return currencyFormat.format(restaurant.getAveragePrice());
    }
    
    
    /**
     * Restaurants listing page with filtering and sorting
     */
    @GetMapping("/restaurants")
    public String restaurants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "restaurantName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String cuisineType,
            @RequestParam(required = false) String priceRange,
            @RequestParam(required = false) String ratingFilter,
            Model model) {
        
        try {
            model.addAttribute("pageTitle", "Nhà hàng - Book Eat");
            model.addAttribute("activeNav", "restaurants");
            
            // Create pageable
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // Get restaurants with filters using RestaurantManagementService
            Page<RestaurantProfile> restaurants = restaurantService.getRestaurantsWithFilters(
                pageable, search, cuisineType, priceRange, ratingFilter);
            
            // ===== PERFORMANCE OPTIMIZATION: Fix N+1 with batch query =====
            // BEFORE: Loop through each restaurant and query media separately (1 + N queries)
            // AFTER: Single batch query for all restaurants (1 + 1 queries)
            if (!restaurants.getContent().isEmpty()) {
                // Batch fetch cover images for all restaurants in one query
                List<RestaurantMedia> allCoverImages = restaurantMediaRepository
                        .findByRestaurantsAndType(restaurants.getContent(), "cover");
                
                // Group by restaurant ID and take the first (newest) image for each restaurant
                Map<Integer, String> coverUrlMap = allCoverImages.stream()
                        .collect(Collectors.groupingBy(
                                m -> m.getRestaurant().getRestaurantId(),
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        list -> list.isEmpty() ? null : list.get(0).getUrl()
                                )
                        ));
                
                // Set cover image URLs on restaurants
                for (RestaurantProfile restaurant : restaurants.getContent()) {
                    String coverUrl = coverUrlMap.get(restaurant.getRestaurantId());
                    if (coverUrl != null) {
                        restaurant.setMainImageUrl(coverUrl);
                    }
                }
            }

            // Add to model
            model.addAttribute("restaurants", restaurants);
            model.addAttribute("totalElements", restaurants.getTotalElements());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", restaurants.getTotalPages());
            model.addAttribute("search", search);
            model.addAttribute("cuisineType", cuisineType);
            model.addAttribute("priceRange", priceRange);
            model.addAttribute("ratingFilter", ratingFilter);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            
            return "public/restaurants";
            
        } catch (Exception e) {
            System.out.println("ERROR in restaurants: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "public/restaurants";
        }
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
            List<RestaurantMedia> exterior = allMedia.stream()
                    .filter(m -> "exterior".equalsIgnoreCase(m.getType()))
                    .toList();
            List<RestaurantMedia> interior = allMedia.stream()
                    .filter(m -> "interior".equalsIgnoreCase(m.getType()))
                    .toList();
            List<RestaurantMedia> menus = allMedia.stream()
                .filter(m -> "menu".equalsIgnoreCase(m.getType()))
                .toList();
            List<RestaurantMedia> tableLayouts = allMedia.stream()
                    .filter(m -> "table_layout".equalsIgnoreCase(m.getType()))
                    .toList();
            
            // Get dishes with images
            List<DishWithImageDto> dishesWithImages = new ArrayList<>();
            List<Dish> dishes = restaurant.getDishes() != null ? restaurant.getDishes() : new ArrayList<>();
            for (Dish dish : dishes) {
                String dishImageUrl = restaurantOwnerService.getDishImageUrl(id, dish.getDishId());
                dishesWithImages.add(new DishWithImageDto(dish, dishImageUrl));
            }
            
            // Get tables (images will be loaded via getTableImages() method)
            List<RestaurantTable> tables = restaurant.getTables() != null ? restaurant.getTables() : new ArrayList<>();
            
            // Review-related data
            boolean hasReviewed = false;
            ReviewDto customerReview = null;
            List<ReviewDto> recentReviews = new ArrayList<>();
            ReviewStatisticsDto statistics = null;
            long totalReviews = 0;

            try {
                System.out.println("🔍 Loading review data for restaurant ID: " + id);
                System.out.println("🔍 Authentication: " + (authentication != null ? "Present" : "Null"));
                System.out.println(
                        "🔍 Is authenticated: " + (authentication != null && authentication.isAuthenticated()));

                // Check if user has reviewed this restaurant
                if (authentication != null && authentication.isAuthenticated()) {
                    User user = (User) authentication.getPrincipal();
                    System.out.println("🔍 User ID: " + user.getId());

                    Optional<Customer> customerOpt = customerService.findByUserId(user.getId());
                    System.out.println("🔍 Customer found: " + customerOpt.isPresent());

                    if (customerOpt.isPresent()) {
                        hasReviewed = reviewService.hasCustomerReviewedRestaurant(customerOpt.get().getCustomerId(),
                                id);
                        System.out.println("🔍 Has reviewed: " + hasReviewed);

                        if (hasReviewed) {
                            // Get customer's review for this restaurant
                            List<ReviewDto> customerReviews = reviewService
                                    .getReviewsByCustomer(customerOpt.get().getCustomerId());
                            Optional<ReviewDto> customerReviewOpt = customerReviews.stream()
                                    .filter(r -> r.getRestaurantId().equals(id))
                                    .findFirst();
                            if (customerReviewOpt.isPresent()) {
                                customerReview = customerReviewOpt.get();
                                System.out.println("🔍 Customer review found: " + customerReview.getReviewId());
                            }
                        }
                    }
                }

                // Get recent reviews (3-5 reviews)
                Pageable pageable = PageRequest.of(0, 5);
                Page<ReviewDto> recentReviewsPage = reviewService.getReviewsByRestaurant(id, pageable);
                recentReviews = recentReviewsPage.getContent();
                System.out.println("🔍 Recent reviews count: " + recentReviews.size());

                // Get review statistics
                statistics = reviewService.getRestaurantReviewStatistics(id);
                totalReviews = recentReviewsPage.getTotalElements();
                System.out.println("🔍 Total reviews: " + totalReviews);

            } catch (Exception e) {
                // If review service fails, continue without review data
                System.err.println("❌ Review service error: " + e.getMessage());
                e.printStackTrace();
            }

            // Add to model
            model.addAttribute("pageTitle", restaurant.getRestaurantName() + " - Chi tiết Nhà hàng");
            model.addAttribute("restaurant", restaurant);
            model.addAttribute("logo", logos.isEmpty() ? null : logos.get(0));
            model.addAttribute("cover", covers.isEmpty() ? null : covers.get(0));
            model.addAttribute("gallery", gallery);
            model.addAttribute("exterior", exterior);
            model.addAttribute("interior", interior);
            model.addAttribute("menus", menus);
            model.addAttribute("tableLayouts", tableLayouts);
            model.addAttribute("dishes", dishesWithImages);
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

            // Add debug info
            model.addAttribute("debug", true);

            return "public/restaurant-detail";
            
        } catch (Exception e) {
            return "redirect:/restaurants?error=" + e.getMessage();
        }
    }
}
