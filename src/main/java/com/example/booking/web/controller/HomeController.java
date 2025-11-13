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
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Boolean nearby,
            @RequestParam(required = false) Double maxDistance,
            Model model) {
        
        try {
            model.addAttribute("pageTitle", "Nhà hàng - Book Eat");
            model.addAttribute("activeNav", "restaurants");
            
            Page<RestaurantProfile> restaurants = null;
            
            // If nearby search is requested and coordinates are provided
            if (Boolean.TRUE.equals(nearby) && latitude != null && longitude != null) {
                log.info("📍 Nearby search requested - User location: lat={}, lon={}", latitude, longitude);
                
                // Validate coordinates range
                if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                    log.error("❌ Invalid coordinates received: lat={}, lon={}", latitude, longitude);
                    model.addAttribute("error", "Tọa độ không hợp lệ");
                    // Fallback to normal filtering
                    Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
                    Pageable pageable = PageRequest.of(page, size, sort);
                    restaurants = restaurantService.getRestaurantsWithFilters(
                        pageable, search, cuisineType, priceRange, ratingFilter);
                } else {
                    // Validate coordinates are within Vietnam bounds
                    // Vietnam: ~8.5°N to 23.5°N, ~102°E to 110°E
                    boolean withinVietnam = (latitude >= 8 && latitude <= 24 && longitude >= 102 && longitude <= 111);
                    
                    // Check distance to major cities to detect wrong location
                    double daNangLat = 16.047079;
                    double daNangLon = 108.206230;
                    double hcmLat = 10.776889;
                    double hcmLon = 106.700806;
                    
                    double distanceToDaNang = calculateDistance(latitude, longitude, daNangLat, daNangLon);
                    double distanceToHCM = calculateDistance(latitude, longitude, hcmLat, hcmLon);
                    
                    log.info("📍 User location validation:");
                    log.info("   Coordinates: ({}, {})", latitude, longitude);
                    log.info("   Distance to Đà Nẵng: {} km", String.format("%.2f", distanceToDaNang));
                    log.info("   Distance to Hồ Chí Minh: {} km", String.format("%.2f", distanceToHCM));
                    
                    // If user is closer to HCM than Đà Nẵng, and distance to Đà Nẵng > 50km, likely wrong location
                    if (distanceToHCM < distanceToDaNang && distanceToDaNang > 50) {
                        log.error("❌ User location seems to be in HCM area ({} km from HCM, {} km from Đà Nẵng). " +
                            "This might be a cached/wrong location if user is actually in Đà Nẵng.", 
                            String.format("%.2f", distanceToHCM), String.format("%.2f", distanceToDaNang));
                        model.addAttribute("error", 
                            String.format("Vị trí hiện tại có vẻ không đúng (cách Đà Nẵng %.0f km, cách HCM %.0f km). " +
                                "Vui lòng xóa cache và cập nhật lại vị trí.", 
                                distanceToDaNang, distanceToHCM));
                        // Fallback to normal filtering
                        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
                        Pageable pageable = PageRequest.of(page, size, sort);
                        restaurants = restaurantService.getRestaurantsWithFilters(
                            pageable, search, cuisineType, priceRange, ratingFilter);
                    } else if (!withinVietnam) {
                        log.warn("⚠️ Coordinates outside Vietnam bounds: lat={}, lon={}", latitude, longitude);
                        log.warn("   Expected range: lat 8-24, lon 102-111");
                        
                        if (distanceToDaNang > 1000) {
                            log.error("❌ Coordinates are very far from Vietnam (>1000km). " +
                                "Distance to Đà Nẵng: {} km. This might be a cached/wrong location.", 
                                String.format("%.2f", distanceToDaNang));
                            model.addAttribute("error", 
                                String.format("Vị trí không hợp lệ (cách Đà Nẵng %.0f km). Vui lòng cập nhật vị trí.", 
                                    distanceToDaNang));
                            // Fallback to normal filtering
                            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
                            Pageable pageable = PageRequest.of(page, size, sort);
                            restaurants = restaurantService.getRestaurantsWithFilters(
                                pageable, search, cuisineType, priceRange, ratingFilter);
                        } else {
                            // Within reasonable distance, proceed but log warning
                            log.info("📍 Proceeding with nearby search despite coordinates outside Vietnam bounds");
                        }
                    } else if (distanceToDaNang > 100) {
                        // User claims to be in Đà Nẵng, but location is > 100km away
                        log.warn("⚠️ User location is {} km from Đà Nẵng center. " +
                            "If user is actually in Đà Nẵng, this might be a cached/wrong location.", 
                            String.format("%.2f", distanceToDaNang));
                        // Still proceed, but log warning
                    }
                    
                    if (restaurants == null) { // Only proceed if we haven't set restaurants in error case above
                    // Get all restaurants first (we'll filter and sort by distance)
                    Pageable allPageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("restaurantName"));
                    Page<RestaurantProfile> allRestaurants = restaurantService.getRestaurantsWithFilters(
                        allPageable, search, cuisineType, priceRange, ratingFilter);
                    
                    log.info("📊 Total restaurants found: {}", allRestaurants.getTotalElements());
                    
                    // Count restaurants with and without coordinates
                    long restaurantsWithCoords = allRestaurants.getContent().stream()
                        .filter(r -> r.getLatitude() != null && r.getLongitude() != null)
                        .count();
                    long restaurantsWithoutCoords = allRestaurants.getTotalElements() - restaurantsWithCoords;
                    log.info("📍 Restaurants with coordinates: {} | Without coordinates: {}", 
                        restaurantsWithCoords, restaurantsWithoutCoords);
                    
                    // Log first few restaurants without coordinates for debugging
                    if (restaurantsWithoutCoords > 0) {
                        log.warn("⚠️ Some restaurants don't have coordinates:");
                        allRestaurants.getContent().stream()
                            .filter(r -> r.getLatitude() == null || r.getLongitude() == null)
                            .limit(5)
                            .forEach(r -> log.warn("   - {} (ID: {}) - Address: {}", 
                                r.getRestaurantName(), r.getRestaurantId(), r.getAddress()));
                    }
                    
                    // Calculate distances - ONLY use coordinates stored in database
                    // Logic: Distance = user's current location vs restaurant's stored coordinates
                    List<RestaurantProfile> restaurantsWithDistance = allRestaurants.getContent().stream()
                        .map(r -> {
                            // Only use coordinates from database - no real-time geocoding
                            if (r.getLatitude() != null && r.getLongitude() != null) {
                                Double restaurantLat = r.getLatitude().doubleValue();
                                Double restaurantLon = r.getLongitude().doubleValue();
                                
                                // Calculate distance from user's current location to restaurant's stored coordinates
                                double distance = calculateDistance(latitude, longitude, restaurantLat, restaurantLon);
                                r.setDistance(distance);
                                
                                log.debug("📏 Restaurant {} - Distance: {} km (DB coordinates: {}, {})", 
                                    r.getRestaurantName(), 
                                    String.format("%.2f", distance), 
                                    restaurantLat, restaurantLon);
                                return r;
                            } else {
                                // Restaurant doesn't have coordinates in database - skip it
                                log.warn("⚠️ Restaurant {} (ID: {}) has no coordinates in database - Address: '{}'. " +
                                    "Please update restaurant profile to geocode and save coordinates.", 
                                    r.getRestaurantName(), r.getRestaurantId(), r.getAddress());
                                return null;
                            }
                        })
                        .filter(java.util.Objects::nonNull)
                        .filter(r -> {
                            // Filter by maxDistance if provided
                            if (maxDistance != null && maxDistance > 0 && r.getDistance() != null) {
                                boolean withinDistance = r.getDistance() <= maxDistance;
                                if (!withinDistance) {
                                    log.debug("📍 Restaurant {} filtered out (distance: {} km > maxDistance: {} km)", 
                                        r.getRestaurantName(), 
                                        r.getDistance() != null ? String.format("%.2f", r.getDistance()) : "0.00", 
                                        String.format("%.2f", maxDistance));
                                }
                                return withinDistance;
                            }
                            return true; // No distance filter, include all
                        })
                        .sorted((r1, r2) -> Double.compare(
                            r1.getDistance() != null ? r1.getDistance() : Double.MAX_VALUE,
                            r2.getDistance() != null ? r2.getDistance() : Double.MAX_VALUE))
                        .collect(java.util.stream.Collectors.toList());
                    
                    // Log distance filter info
                    // Note: restaurantsWithDistance is already filtered by maxDistance above
                    if (maxDistance != null && maxDistance > 0) {
                        log.info("📍 Distance filter applied: maxDistance={} km", maxDistance);
                        log.info("✅ Restaurants within {} km: {} (filtered from {} total restaurants)", 
                            maxDistance, restaurantsWithDistance.size(), allRestaurants.getTotalElements());
                    } else {
                        log.info("✅ Restaurants with coordinates: {} (no distance filter, showing all within range)", 
                            restaurantsWithDistance.size());
                    }
                    
                    if (restaurantsWithDistance.isEmpty()) {
                        log.warn("⚠️ No restaurants found with coordinates near user location");
                        log.warn("   User location: ({}, {})", latitude, longitude);
                        if (maxDistance != null && maxDistance > 0) {
                            log.warn("   Max distance filter: {} km", maxDistance);
                        }
                        log.warn("   This might mean:");
                        log.warn("   1. No restaurants have coordinates in database");
                        if (maxDistance != null && maxDistance > 0) {
                            log.warn("   2. All restaurants are farther than {} km from user location", maxDistance);
                        } else {
                            log.warn("   2. All restaurants are too far from user location");
                        }
                    } else {
                        // Log top 10 closest restaurants for debugging
                        log.info("🎯 Top 10 closest restaurants (from user location: {}, {}):", latitude, longitude);
                        int count = Math.min(10, restaurantsWithDistance.size());
                        for (int i = 0; i < count; i++) {
                            RestaurantProfile r = restaurantsWithDistance.get(i);
                            String coordsStr = "N/A";
                            if (r.getLatitude() != null && r.getLongitude() != null) {
                                coordsStr = String.format("%.6f, %.6f", 
                                    r.getLatitude().doubleValue(), r.getLongitude().doubleValue());
                            }
                            log.info("   {}. {} - Distance: {} km - Coords: ({}) - Address: {}", 
                                i + 1, r.getRestaurantName(), 
                                r.getDistance() != null ? String.format("%.2f", r.getDistance()) : "N/A", 
                                coordsStr, r.getAddress());
                        }
                        
                        // Log closest restaurant with detailed info
                        RestaurantProfile closest = restaurantsWithDistance.get(0);
                        String closestCoordsStr = "N/A";
                        if (closest.getLatitude() != null && closest.getLongitude() != null) {
                            closestCoordsStr = String.format("%.6f, %.6f", 
                                closest.getLatitude().doubleValue(), closest.getLongitude().doubleValue());
                        }
                        log.info("🎯 Closest restaurant: {} - Distance: {} km - Coords: ({}) - Address: {}", 
                            closest.getRestaurantName(), 
                            closest.getDistance() != null ? String.format("%.2f", closest.getDistance()) : "N/A", 
                            closestCoordsStr, closest.getAddress());
                        
                        // Verify Đà Nẵng location (using variables already declared above)
                        log.info("📍 User location verification: Distance to Đà Nẵng center: {} km", 
                            String.format("%.2f", distanceToDaNang));
                        if (distanceToDaNang > 10) {
                            log.warn("⚠️ User location seems far from Đà Nẵng center ({} km). Expected < 10 km.", 
                                String.format("%.2f", distanceToDaNang));
                        }
                    }
                    
                    // Apply pagination manually
                    int start = page * size;
                    int end = Math.min(start + size, restaurantsWithDistance.size());
                    List<RestaurantProfile> pagedContent = start < restaurantsWithDistance.size() 
                        ? restaurantsWithDistance.subList(start, end)
                        : Collections.emptyList();
                    
                    restaurants = new org.springframework.data.domain.PageImpl<>(
                        pagedContent, PageRequest.of(page, size), restaurantsWithDistance.size());
                    
                    model.addAttribute("userLatitude", latitude);
                    model.addAttribute("userLongitude", longitude);
                    model.addAttribute("nearbySearch", true);
                    model.addAttribute("maxDistance", maxDistance);
                    } // Close if (restaurants == null)
                } // Close else (valid coordinates)
            } else {
                // Normal filtering without location
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
                restaurants = restaurantService.getRestaurantsWithFilters(
                pageable, search, cuisineType, priceRange, ratingFilter);
            }
            
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
            if (latitude != null) model.addAttribute("latitude", latitude);
            if (longitude != null) model.addAttribute("longitude", longitude);
            if (nearby != null) model.addAttribute("nearby", nearby);
            if (maxDistance != null) model.addAttribute("maxDistance", maxDistance);
            
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
    
    /**
     * Calculate distance between two coordinates using Haversine formula
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return Distance in kilometers
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;
        
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLatRad = Math.toRadians(lat2 - lat1);
        double deltaLonRad = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }
}
