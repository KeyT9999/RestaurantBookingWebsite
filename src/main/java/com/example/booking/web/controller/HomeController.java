package com.example.booking.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger log = LoggerFactory.getLogger(HomeController.class);
    
    private static final int DEFAULT_TOP_RESTAURANTS_COUNT = 3;
    
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

        // Popular restaurants for home page - với exception handling và fallback
        List<PopularRestaurantDto> popularRestaurants = loadPopularRestaurants();
        model.addAttribute("popularRestaurants", popularRestaurants);
        
        return "public/home";
    }
    
    /**
     * Load popular restaurants với fallback mechanism và prioritize specific restaurants
     * Ưu tiên: Thai Market Restaurant và Danh Restaurant -> findTopRatedRestaurants -> findApprovedRestaurantsSimple -> empty list
     */
    private List<PopularRestaurantDto> loadPopularRestaurants() {
        try {
            // Step 1: Tìm và prioritize 2 nhà hàng cụ thể
            List<RestaurantProfile> prioritizedRestaurants = new ArrayList<>();
            
            // Tìm "Thai Market Restaurant"
            List<RestaurantProfile> thaiMarket = restaurantService.findRestaurantsByName("Thai Market Restaurant");
            if (!thaiMarket.isEmpty()) {
                prioritizedRestaurants.add(thaiMarket.get(0));
                log.debug("Found Thai Market Restaurant: {}", thaiMarket.get(0).getRestaurantId());
            }
            
            // Tìm "Danh Restaurant"
            List<RestaurantProfile> danhRestaurant = restaurantService.findRestaurantsByName("Danh Restaurant");
            if (!danhRestaurant.isEmpty()) {
                prioritizedRestaurants.add(danhRestaurant.get(0));
                log.debug("Found Danh Restaurant: {}", danhRestaurant.get(0).getRestaurantId());
            }
            
            // Step 2: Nếu đã có 2 nhà hàng, chỉ cần thêm 1 nhà hàng nữa
            if (prioritizedRestaurants.size() >= 2) {
                // Lấy thêm nhà hàng khác để đủ 3
                List<RestaurantProfile> additionalRestaurants = restaurantService.findTopRatedRestaurants(DEFAULT_TOP_RESTAURANTS_COUNT + 5);
                if (additionalRestaurants != null) {
                    for (RestaurantProfile restaurant : additionalRestaurants) {
                        // Chỉ thêm nhà hàng chưa có trong list
                        if (!prioritizedRestaurants.contains(restaurant) && 
                            !prioritizedRestaurants.stream().anyMatch(r -> r.getRestaurantId().equals(restaurant.getRestaurantId()))) {
                            prioritizedRestaurants.add(restaurant);
                            if (prioritizedRestaurants.size() >= DEFAULT_TOP_RESTAURANTS_COUNT) {
                                break;
                            }
                        }
                    }
                }
                
                // Nếu vẫn chưa đủ, dùng fallback
                if (prioritizedRestaurants.size() < DEFAULT_TOP_RESTAURANTS_COUNT) {
                    List<RestaurantProfile> approvedRestaurants = restaurantService.findApprovedRestaurantsSimple(DEFAULT_TOP_RESTAURANTS_COUNT + 5);
                    if (approvedRestaurants != null) {
                        for (RestaurantProfile restaurant : approvedRestaurants) {
                            if (!prioritizedRestaurants.contains(restaurant) && 
                                !prioritizedRestaurants.stream().anyMatch(r -> r.getRestaurantId().equals(restaurant.getRestaurantId()))) {
                                prioritizedRestaurants.add(restaurant);
                                if (prioritizedRestaurants.size() >= DEFAULT_TOP_RESTAURANTS_COUNT) {
                                    break;
                                }
                            }
                        }
                    }
                }
                
                // Giới hạn chỉ 3 nhà hàng
                if (prioritizedRestaurants.size() > DEFAULT_TOP_RESTAURANTS_COUNT) {
                    prioritizedRestaurants = prioritizedRestaurants.subList(0, DEFAULT_TOP_RESTAURANTS_COUNT);
                }
                
                log.info("Loaded {} prioritized restaurants (including Thai Market and Danh Restaurant)", prioritizedRestaurants.size());
                return buildPopularRestaurantCards(prioritizedRestaurants);
            }
            
            // Step 3: Nếu không tìm thấy đủ 2 nhà hàng cụ thể, fallback về logic cũ
            log.debug("Could not find both prioritized restaurants, falling back to top-rated query");
            List<RestaurantProfile> topRestaurants = restaurantService.findTopRatedRestaurants(DEFAULT_TOP_RESTAURANTS_COUNT);
            
            if (topRestaurants != null && !topRestaurants.isEmpty()) {
                // Nếu có 1 trong 2 nhà hàng cụ thể, thêm vào đầu danh sách
                if (!prioritizedRestaurants.isEmpty()) {
                    for (RestaurantProfile prioritized : prioritizedRestaurants) {
                        topRestaurants.removeIf(r -> r.getRestaurantId().equals(prioritized.getRestaurantId()));
                        topRestaurants.add(0, prioritized);
                    }
                    if (topRestaurants.size() > DEFAULT_TOP_RESTAURANTS_COUNT) {
                        topRestaurants = topRestaurants.subList(0, DEFAULT_TOP_RESTAURANTS_COUNT);
                    }
                }
                
                log.debug("Successfully loaded {} top-rated restaurants", topRestaurants.size());
                return buildPopularRestaurantCards(topRestaurants);
            }
            
            // Fallback: get any approved restaurants (simple query, no complex calculations)
            log.warn("No top-rated restaurants found, falling back to simple approved restaurants query");
            List<RestaurantProfile> approvedRestaurants = restaurantService.findApprovedRestaurantsSimple(DEFAULT_TOP_RESTAURANTS_COUNT);
            
            if (approvedRestaurants != null && !approvedRestaurants.isEmpty()) {
                // Nếu có 1 trong 2 nhà hàng cụ thể, thêm vào đầu danh sách
                if (!prioritizedRestaurants.isEmpty()) {
                    for (RestaurantProfile prioritized : prioritizedRestaurants) {
                        approvedRestaurants.removeIf(r -> r.getRestaurantId().equals(prioritized.getRestaurantId()));
                        approvedRestaurants.add(0, prioritized);
                    }
                    if (approvedRestaurants.size() > DEFAULT_TOP_RESTAURANTS_COUNT) {
                        approvedRestaurants = approvedRestaurants.subList(0, DEFAULT_TOP_RESTAURANTS_COUNT);
                    }
                }
                
                log.info("Loaded {} approved restaurants as fallback", approvedRestaurants.size());
                return buildPopularRestaurantCards(approvedRestaurants);
            }
            
            log.warn("No approved restaurants found in database");
            return Collections.emptyList();
            
        } catch (Exception e) {
            log.error("Error loading popular restaurants, attempting fallback: {}", e.getMessage(), e);
            
            // Final fallback: try simple query even if top-rated failed
            try {
                List<RestaurantProfile> approvedRestaurants = restaurantService.findApprovedRestaurantsSimple(DEFAULT_TOP_RESTAURANTS_COUNT);
                if (approvedRestaurants != null && !approvedRestaurants.isEmpty()) {
                    log.info("Fallback successful: loaded {} approved restaurants", approvedRestaurants.size());
                    return buildPopularRestaurantCards(approvedRestaurants);
                }
            } catch (Exception fallbackException) {
                log.error("Fallback also failed: {}", fallbackException.getMessage(), fallbackException);
            }
            
            // Return empty list to prevent page crash
            return Collections.emptyList();
        }
    }
    
    private List<PopularRestaurantDto> buildPopularRestaurantCards(List<RestaurantProfile> restaurants) {
        if (restaurants == null || restaurants.isEmpty()) {
            return Collections.emptyList();
        }

        // Filter out null restaurants để tránh NPE
        List<RestaurantProfile> validRestaurants = restaurants.stream()
                .filter(r -> r != null && r.getRestaurantId() != null)
                .collect(Collectors.toList());
        
        if (validRestaurants.isEmpty()) {
            return Collections.emptyList();
        }

        // Safe query với null checks
        Map<Integer, String> coverMap = Collections.emptyMap();
        try {
            List<RestaurantMedia> coverMedia = restaurantMediaRepository.findByRestaurantsAndType(validRestaurants, "cover");
            if (coverMedia != null && !coverMedia.isEmpty()) {
                coverMap = coverMedia.stream()
                        .filter(media -> media != null && media.getRestaurant() != null && media.getUrl() != null)
                        .collect(Collectors.toMap(
                                media -> media.getRestaurant().getRestaurantId(),
                                RestaurantMedia::getUrl,
                                (existing, ignored) -> existing,
                                LinkedHashMap::new));
            }
        } catch (Exception e) {
            log.warn("Error loading cover media, continuing without images: {}", e.getMessage());
            // Continue without cover images
        }

        List<PopularRestaurantDto> cards = new ArrayList<>();
        for (int i = 0; i < validRestaurants.size(); i++) {
            RestaurantProfile restaurant = validRestaurants.get(i);
            
            // Safe access với null checks
            Integer restaurantId = restaurant.getRestaurantId();
            String restaurantName = restaurant.getRestaurantName() != null ? restaurant.getRestaurantName() : "Nhà hàng";
            String cuisineType = restaurant.getCuisineType() != null ? restaurant.getCuisineType() : "";
            String address = restaurant.getAddress() != null ? restaurant.getAddress() : "";
            
            // Try to get review statistics, but don't fail if it errors
            ReviewStatisticsDto statistics = null;
            try {
                if (restaurantId != null) {
                    statistics = reviewService.getRestaurantReviewStatistics(restaurantId);
                }
            } catch (Exception ex) {
                log.debug("Unable to load review statistics for restaurant {}: {}", restaurantId, ex.getMessage());
            }

            double averageRating = statistics != null ? statistics.getAverageRating() : 
                                   (restaurant.getAverageRating() > 0 ? restaurant.getAverageRating() : 0.0);
            int reviewCount = statistics != null ? statistics.getTotalReviews() : restaurant.getReviewCount();
            
            // Get cover image URL safely
            String coverImageUrl = coverMap.get(restaurantId);
            String gradient = POPULAR_CARD_GRADIENTS[i % POPULAR_CARD_GRADIENTS.length];

            cards.add(new PopularRestaurantDto(
                    restaurantId,
                    restaurantName,
                    cuisineType,
                    address,
                    averageRating,
                    reviewCount,
                    resolvePriceLabel(restaurant),
                    resolveBadge(i, reviewCount),
                    coverImageUrl,
                    gradient
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
                
                // Check restaurant open/closed status
                Map<Integer, Boolean> restaurantsIsOpen = new java.util.HashMap<>();
                for (RestaurantProfile restaurant : restaurants.getContent()) {
                    boolean isOpen = restaurantService.isRestaurantCurrentlyOpen(restaurant);
                    restaurantsIsOpen.put(restaurant.getRestaurantId(), isOpen);
                }
                model.addAttribute("restaurantsIsOpen", restaurantsIsOpen);
            } else {
                model.addAttribute("restaurantsIsOpen", Collections.emptyMap());
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

            // Lấy danh sách nhà hàng liên quan
            try {
                List<RestaurantProfile> relatedRestaurants = restaurantService.findRelatedRestaurants(restaurant, 6);
                
                // Batch fetch cover images for related restaurants
                if (!relatedRestaurants.isEmpty()) {
                    List<RestaurantMedia> relatedCoverImages = restaurantMediaRepository
                            .findByRestaurantsAndType(relatedRestaurants, "cover");
                    
                    Map<Integer, String> relatedCoverMap = relatedCoverImages.stream()
                            .filter(media -> media != null && media.getRestaurant() != null && media.getUrl() != null)
                            .collect(Collectors.toMap(
                                    media -> media.getRestaurant().getRestaurantId(),
                                    RestaurantMedia::getUrl,
                                    (existing, ignored) -> existing));
                    
                    // Set cover URLs và kiểm tra trạng thái mở cửa
                    Map<Integer, Boolean> isOpenMap = new java.util.HashMap<>();
                    for (RestaurantProfile related : relatedRestaurants) {
                        String coverUrl = relatedCoverMap.get(related.getRestaurantId());
                        if (coverUrl != null) {
                            related.setMainImageUrl(coverUrl);
                        }
                        // Kiểm tra trạng thái mở cửa
                        boolean isOpen = restaurantService.isRestaurantCurrentlyOpen(related);
                        isOpenMap.put(related.getRestaurantId(), isOpen);
                    }
                    
                    model.addAttribute("relatedRestaurants", relatedRestaurants);
                    model.addAttribute("relatedRestaurantsIsOpen", isOpenMap);
                } else {
                    model.addAttribute("relatedRestaurants", Collections.emptyList());
                    model.addAttribute("relatedRestaurantsIsOpen", Collections.emptyMap());
                }
            } catch (Exception e) {
                log.warn("Error loading related restaurants: {}", e.getMessage());
                model.addAttribute("relatedRestaurants", Collections.emptyList());
            }

            // Add debug info
            model.addAttribute("debug", true);

            return "public/restaurant-detail-simple";
            
        } catch (Exception e) {
            log.error("Error loading restaurant detail for ID {}: {}", id, e.getMessage(), e);
            // Redirect with encoded error message to prevent URL issues
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
            // URL encode error message to prevent issues with special characters
            try {
                errorMessage = java.net.URLEncoder.encode(errorMessage, java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception encodeEx) {
                log.warn("Failed to encode error message", encodeEx);
            }
            return "redirect:/restaurants?error=" + errorMessage;
        }
    }
}
