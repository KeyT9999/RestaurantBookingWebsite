package com.example.booking.web.controller.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.UserRepository;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.util.CityGeoResolver;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminApiController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminApiController.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RestaurantProfileRepository restaurantProfileRepository;
    
    @Autowired(required = false)
    private RestTemplate restTemplate;
    
    @Autowired
    private RestaurantOwnerService restaurantOwnerService;
    
    @PostMapping("/create-admin")
    public ResponseEntity<String> createAdmin() {
        try {
            // Kiểm tra xem đã có admin chưa
            if (userRepository.findByRole(UserRole.ADMIN, org.springframework.data.domain.Pageable.unpaged()).isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@bookeat.vn");
                admin.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyVhUx0J8KqF0vVjqKjKjKjKjKjK"); // admin123
                admin.setRole(UserRole.ADMIN);
                admin.setActive(true);
                
                userRepository.save(admin);
                return ResponseEntity.ok("Admin user created successfully! Username: admin, Password: admin123");
            } else {
                return ResponseEntity.ok("Admin user already exists!");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating admin user: " + e.getMessage());
        }
    }
    
    /**
     * Geocode all restaurants that don't have coordinates
     * POST /api/admin/geocode-restaurants
     */
    @PostMapping("/geocode-restaurants")
    public ResponseEntity<Map<String, Object>> geocodeAllRestaurants() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (restTemplate == null) {
                result.put("success", false);
                result.put("message", "RestTemplate not available");
                return ResponseEntity.badRequest().body(result);
            }
            
            CityGeoResolver cityGeoResolver = new CityGeoResolver(restTemplate);
            List<RestaurantProfile> allRestaurants = restaurantProfileRepository.findAll();
            
            int total = allRestaurants.size();
            int withoutCoords = 0;
            int geocoded = 0;
            int failed = 0;
            
            // Known city-center coordinates that should be re-geocoded
            java.math.BigDecimal daNangCenterLat = java.math.BigDecimal.valueOf(16.047079);
            java.math.BigDecimal daNangCenterLon = java.math.BigDecimal.valueOf(108.206230);
            java.math.BigDecimal hcmCenterLat = java.math.BigDecimal.valueOf(10.776889);
            java.math.BigDecimal hcmCenterLon = java.math.BigDecimal.valueOf(106.700806);
            java.math.BigDecimal hanoiCenterLat = java.math.BigDecimal.valueOf(21.027763);
            java.math.BigDecimal hanoiCenterLon = java.math.BigDecimal.valueOf(105.834160);
            
            for (RestaurantProfile restaurant : allRestaurants) {
                boolean needsGeocoding = false;
                String reason = "";
                
                // Check if restaurant has no coordinates
                if (restaurant.getLatitude() == null || restaurant.getLongitude() == null) {
                    needsGeocoding = true;
                    reason = "no coordinates";
                    withoutCoords++;
                } 
                // Check if coordinates match city-center (likely fallback, need to re-geocode)
                else if (isCityCenter(restaurant.getLatitude(), restaurant.getLongitude(), 
                        daNangCenterLat, daNangCenterLon, hcmCenterLat, hcmCenterLon, 
                        hanoiCenterLat, hanoiCenterLon)) {
                    needsGeocoding = true;
                    reason = "city-center coordinates detected";
                    withoutCoords++;
                    logger.info("🔄 Restaurant '{}' has city-center coordinates, will re-geocode", 
                        restaurant.getRestaurantName());
                }
                
                if (needsGeocoding && restaurant.getAddress() != null && !restaurant.getAddress().isBlank()) {
                    try {
                        // Clear existing coordinates to force fresh geocoding
                        if (reason.equals("city-center coordinates detected")) {
                            restaurant.setLatitude(null);
                            restaurant.setLongitude(null);
                        }
                        
                        CityGeoResolver.LatLng coords = cityGeoResolver.resolveFromAddress(restaurant.getAddress());
                        if (coords != null) {
                            // Check if geocoded result is significantly different from city center
                            String normalizedAddress = restaurant.getAddress().toLowerCase();
                            boolean isDaNang = normalizedAddress.contains("đà nẵng") || normalizedAddress.contains("da nang");
                            boolean isHCM = normalizedAddress.contains("hồ chí minh") || normalizedAddress.contains("ho chi minh") 
                                         || normalizedAddress.contains("hcm") || normalizedAddress.contains("tp.hcm");
                            boolean isHanoi = normalizedAddress.contains("hà nội") || normalizedAddress.contains("ha noi");
                            
                            boolean isCityCenter = false;
                            if (isDaNang) {
                                double dist = com.example.booking.util.GeoUtils.haversineKm(
                                    coords.lat, coords.lng, daNangCenterLat.doubleValue(), daNangCenterLon.doubleValue());
                                isCityCenter = dist < 1.0; // Within 1km of city center
                            } else if (isHCM) {
                                double dist = com.example.booking.util.GeoUtils.haversineKm(
                                    coords.lat, coords.lng, hcmCenterLat.doubleValue(), hcmCenterLon.doubleValue());
                                isCityCenter = dist < 1.0;
                            } else if (isHanoi) {
                                double dist = com.example.booking.util.GeoUtils.haversineKm(
                                    coords.lat, coords.lng, hanoiCenterLat.doubleValue(), hanoiCenterLon.doubleValue());
                                isCityCenter = dist < 1.0;
                            }
                            
                            if (isCityCenter) {
                                logger.warn("⚠️ Geocoding returned city-center for '{}', skipping save", 
                                    restaurant.getRestaurantName());
                                failed++;
                            } else {
                                restaurant.setLatitude(java.math.BigDecimal.valueOf(coords.lat));
                                restaurant.setLongitude(java.math.BigDecimal.valueOf(coords.lng));
                                restaurantOwnerService.updateRestaurantProfile(restaurant);
                                geocoded++;
                                logger.info("✅ Geocoded restaurant '{}' (ID: {}): ({}, {}) - Reason: {}", 
                                    restaurant.getRestaurantName(), restaurant.getRestaurantId(), 
                                    coords.lat, coords.lng, reason);
                            }
                            
                            // Add delay to respect rate limiting (1 request per second)
                            Thread.sleep(1100);
                        } else {
                            failed++;
                            logger.warn("⚠️ Could not geocode: {} - Address: {}", 
                                restaurant.getRestaurantName(), restaurant.getAddress());
                        }
                    } catch (Exception e) {
                        failed++;
                        logger.error("❌ Error geocoding restaurant '{}': {}", 
                            restaurant.getRestaurantName(), e.getMessage());
                    }
                } else if (needsGeocoding) {
                    failed++;
                    logger.warn("⚠️ Restaurant '{}' has no address", restaurant.getRestaurantName());
                }
            }
            
            result.put("success", true);
            result.put("total", total);
            result.put("withoutCoords", withoutCoords);
            result.put("geocoded", geocoded);
            result.put("failed", failed);
            result.put("message", String.format(
                "Geocoding completed: %d geocoded, %d failed out of %d restaurants without coordinates", 
                geocoded, failed, withoutCoords));
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("❌ Error in geocodeAllRestaurants: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * Check restaurant coordinates status
     * GET /api/admin/check-restaurant-coordinates
     */
    @org.springframework.web.bind.annotation.GetMapping("/check-restaurant-coordinates")
    public ResponseEntity<Map<String, Object>> checkRestaurantCoordinates() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<RestaurantProfile> allRestaurants = restaurantProfileRepository.findAll();
            
            java.math.BigDecimal daNangCenterLat = java.math.BigDecimal.valueOf(16.047079);
            java.math.BigDecimal daNangCenterLon = java.math.BigDecimal.valueOf(108.206230);
            
            List<Map<String, Object>> restaurants = new java.util.ArrayList<>();
            int withCityCenter = 0;
            int withUniqueCoords = 0;
            int withoutCoords = 0;
            
            for (RestaurantProfile r : allRestaurants) {
                Map<String, Object> info = new HashMap<>();
                info.put("id", r.getRestaurantId());
                info.put("name", r.getRestaurantName());
                info.put("address", r.getAddress());
                
                if (r.getLatitude() == null || r.getLongitude() == null) {
                    info.put("status", "no_coordinates");
                    info.put("lat", null);
                    info.put("lon", null);
                    withoutCoords++;
                } else {
                    double lat = r.getLatitude().doubleValue();
                    double lon = r.getLongitude().doubleValue();
                    info.put("lat", lat);
                    info.put("lon", lon);
                    
                    // Check if it's city-center
                    if (isCityCenter(r.getLatitude(), r.getLongitude(), 
                            daNangCenterLat, daNangCenterLon, 
                            java.math.BigDecimal.valueOf(10.776889), java.math.BigDecimal.valueOf(106.700806),
                            java.math.BigDecimal.valueOf(21.027763), java.math.BigDecimal.valueOf(105.834160))) {
                        info.put("status", "city_center");
                        withCityCenter++;
                    } else {
                        info.put("status", "unique_coordinates");
                        withUniqueCoords++;
                    }
                }
                restaurants.add(info);
            }
            
            result.put("success", true);
            result.put("total", allRestaurants.size());
            result.put("withCityCenter", withCityCenter);
            result.put("withUniqueCoords", withUniqueCoords);
            result.put("withoutCoords", withoutCoords);
            result.put("restaurants", restaurants);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("❌ Error checking coordinates: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * Force re-geocode all restaurants (including those with city-center coordinates)
     * POST /api/admin/force-geocode-restaurants
     */
    @PostMapping("/force-geocode-restaurants")
    public ResponseEntity<Map<String, Object>> forceGeocodeAllRestaurants() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (restTemplate == null) {
                result.put("success", false);
                result.put("message", "RestTemplate not available");
                return ResponseEntity.badRequest().body(result);
            }
            
            CityGeoResolver cityGeoResolver = new CityGeoResolver(restTemplate);
            List<RestaurantProfile> allRestaurants = restaurantProfileRepository.findAll();
            
            int total = allRestaurants.size();
            int geocoded = 0;
            int failed = 0;
            int skipped = 0;
            
            for (RestaurantProfile restaurant : allRestaurants) {
                if (restaurant.getAddress() == null || restaurant.getAddress().isBlank()) {
                    skipped++;
                    continue;
                }
                
                try {
                    // Clear existing coordinates to force fresh geocoding
                    restaurant.setLatitude(null);
                    restaurant.setLongitude(null);
                    
                    CityGeoResolver.LatLng coords = cityGeoResolver.resolveFromAddress(restaurant.getAddress());
                    if (coords != null) {
                        // Check if result is city-center
                        String normalizedAddress = restaurant.getAddress().toLowerCase();
                        boolean isDaNang = normalizedAddress.contains("đà nẵng") || normalizedAddress.contains("da nang");
                        
                        if (isDaNang) {
                            double dist = com.example.booking.util.GeoUtils.haversineKm(
                                coords.lat, coords.lng, 16.047079, 108.206230);
                            if (dist < 1.0) {
                                logger.warn("⚠️ Geocoding returned city-center for '{}', skipping", 
                                    restaurant.getRestaurantName());
                                failed++;
                                continue;
                            }
                        }
                        
                        restaurant.setLatitude(java.math.BigDecimal.valueOf(coords.lat));
                        restaurant.setLongitude(java.math.BigDecimal.valueOf(coords.lng));
                        restaurantOwnerService.updateRestaurantProfile(restaurant);
                        geocoded++;
                        logger.info("✅ Force geocoded restaurant '{}' (ID: {}): ({}, {})", 
                            restaurant.getRestaurantName(), restaurant.getRestaurantId(), 
                            coords.lat, coords.lng);
                    } else {
                        failed++;
                        logger.warn("⚠️ Could not geocode: {} - Address: {}", 
                            restaurant.getRestaurantName(), restaurant.getAddress());
                    }
                    
                    // Add delay to respect rate limiting
                    Thread.sleep(1100);
                    
                } catch (Exception e) {
                    failed++;
                    logger.error("❌ Error geocoding restaurant '{}': {}", 
                        restaurant.getRestaurantName(), e.getMessage());
                }
            }
            
            result.put("success", true);
            result.put("total", total);
            result.put("geocoded", geocoded);
            result.put("failed", failed);
            result.put("skipped", skipped);
            result.put("message", String.format(
                "Force geocoding completed: %d geocoded, %d failed, %d skipped", 
                geocoded, failed, skipped));
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("❌ Error in forceGeocodeAllRestaurants: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * Check if coordinates match known city-center coordinates
     */
    private boolean isCityCenter(java.math.BigDecimal lat, java.math.BigDecimal lon,
                                 java.math.BigDecimal... cityCenters) {
        if (lat == null || lon == null) return false;
        
        double latVal = lat.doubleValue();
        double lonVal = lon.doubleValue();
        
        // Check against all provided city centers (lat, lon pairs)
        for (int i = 0; i < cityCenters.length; i += 2) {
            if (i + 1 < cityCenters.length) {
                double cityLat = cityCenters[i].doubleValue();
                double cityLon = cityCenters[i + 1].doubleValue();
                
                // Consider it city-center if within 0.001 degrees (~100m)
                if (Math.abs(latVal - cityLat) < 0.001 && Math.abs(lonVal - cityLon) < 0.001) {
                    return true;
                }
            }
        }
        return false;
    }
}
