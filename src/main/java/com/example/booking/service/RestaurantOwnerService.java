    package com.example.booking.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.domain.Booking;
import com.example.booking.domain.Dish;
import com.example.booking.domain.RestaurantMedia;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.domain.User;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.DiningTableRepository;
import com.example.booking.repository.DishRepository;
import com.example.booking.repository.RestaurantMediaRepository;
import com.example.booking.repository.RestaurantOwnerRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.RestaurantRepository;

/**
 * Service for Restaurant Owner management operations
 * Handles restaurant profile, tables, bookings, and media management
 */
@Service
@Transactional
public class RestaurantOwnerService {
    
    private static final Logger logger = LoggerFactory.getLogger(RestaurantOwnerService.class);

    private final RestaurantRepository restaurantRepository;
    private final RestaurantOwnerRepository restaurantOwnerRepository;
    private final RestaurantProfileRepository restaurantProfileRepository;
    private final BookingRepository bookingRepository;
    private final DiningTableRepository diningTableRepository;
    private final DishRepository dishRepository;
    private final RestaurantMediaRepository restaurantMediaRepository;
    private final SimpleUserService userService;
    private final RestaurantNotificationService restaurantNotificationService;

    @Autowired
    public RestaurantOwnerService(RestaurantRepository restaurantRepository,
            RestaurantOwnerRepository restaurantOwnerRepository,
            RestaurantProfileRepository restaurantProfileRepository,
                                BookingRepository bookingRepository,
                                DiningTableRepository diningTableRepository,
                                DishRepository dishRepository,
            RestaurantMediaRepository restaurantMediaRepository,
            SimpleUserService userService,
            RestaurantNotificationService restaurantNotificationService) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantOwnerRepository = restaurantOwnerRepository;
        this.restaurantProfileRepository = restaurantProfileRepository;
        this.bookingRepository = bookingRepository;
        this.diningTableRepository = diningTableRepository;
        this.dishRepository = dishRepository;
        this.restaurantMediaRepository = restaurantMediaRepository;
        this.userService = userService;
        this.restaurantNotificationService = restaurantNotificationService;
    }

    /**
     * Get restaurant owner by user ID
     */
    public Optional<RestaurantOwner> getRestaurantOwnerByUserId(UUID userId) {
        return restaurantOwnerRepository.findByUserId(userId);
    }

    /**
     * Đảm bảo RestaurantOwner record tồn tại cho user
     * Tạo mới nếu chưa có
     */
    public RestaurantOwner ensureRestaurantOwnerExists(UUID userId) {
        Optional<RestaurantOwner> existingOwner = restaurantOwnerRepository.findByUserId(userId);
        
        if (existingOwner.isPresent()) {
            return existingOwner.get();
        }
        
        // Lấy User entity
        User user;
        try {
            user = userService.findById(userId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }
        
        // Tạo mới RestaurantOwner record
        RestaurantOwner newOwner = new RestaurantOwner(user);
        
        return restaurantOwnerRepository.save(newOwner);
    }

    /**
     * Get all restaurants owned by a specific owner
     */
    public List<RestaurantProfile> getRestaurantsByOwnerId(UUID ownerId) {
        return restaurantProfileRepository.findByOwnerOwnerId(ownerId);
    }
    
    /**
     * Get restaurant ID by owner ID
     * For now, returns the first restaurant ID or null
     * Supports development mode when ownerId is null
     */
    public Integer getRestaurantIdByOwnerId(UUID ownerId) {
        // TODO: Implement proper owner-restaurant relationship
        // For now, return the first available restaurant ID as a placeholder
        
        // Development mode - when ownerId is null, return first available restaurant
        if (ownerId == null) {
            List<RestaurantProfile> restaurants = restaurantRepository.findAll();
            if (!restaurants.isEmpty()) {
                return restaurants.get(0).getRestaurantId();
            }
            return null;
        }
        
        // Normal flow - when ownerId is provided
        List<RestaurantProfile> restaurants = restaurantRepository.findAll();
        if (!restaurants.isEmpty()) {
            return restaurants.get(0).getRestaurantId();
        }
        return null;
    }

    /**
     * Get all restaurants owned by a specific user through authentication
     * This method properly handles the User -> RestaurantOwner -> RestaurantProfile relationship
     */
    public List<RestaurantProfile> getRestaurantsByUserId(UUID userId) {
        // First, get the RestaurantOwner by userId
        Optional<RestaurantOwner> ownerOpt = restaurantOwnerRepository.findByUserId(userId);
        
        if (ownerOpt.isEmpty()) {
            return List.of(); // Return empty list if no owner found
        }
        
        RestaurantOwner owner = ownerOpt.get();
        // Get all restaurants owned by this owner
        return restaurantProfileRepository.findByOwnerOwnerId(owner.getOwnerId());
    }

    /**
     * Get restaurants owned by current authenticated user
     * This is a convenience method for controllers
     */
    public List<RestaurantProfile> getRestaurantsByCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return List.of();
        }
        
        try {
            // Try to parse as UUID first (if using UUID-based authentication)
            UUID userId = UUID.fromString(authentication.getName());
            return getRestaurantsByUserId(userId);
        } catch (IllegalArgumentException e) {
            // If not a UUID, this is username-based authentication
            // We need to get the User first, then get their restaurants
            try {
                // Get User by username
                User user = getUserFromAuthentication(authentication);
                if (user == null) {
                    return List.of();
                }
                
                // Get restaurants owned by this user
                return getRestaurantsByUserId(user.getId());
            } catch (Exception ex) {
                System.err.println("❌ Error getting restaurants for user: " + ex.getMessage());
                return List.of();
            }
        }
    }
    
    /**
     * Helper method to get User from authentication
     * This should be moved to a utility class in a real application
     */
    private User getUserFromAuthentication(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        
        // If it's a User object directly (regular login)
        if (principal instanceof User) {
            return (User) principal;
        }
        
        // If it's OAuth2User (OAuth2 login)
        if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            String username = authentication.getName(); // username = email for OAuth users
            
            // Find actual User from database
            try {
                return (User) userService.loadUserByUsername(username);
            } catch (Exception e) {
                throw new RuntimeException("User not found for OAuth username: " + username +
                        ". Error: " + e.getMessage());
            }
        }
        
        throw new RuntimeException("Unsupported authentication principal type: " + principal.getClass().getSimpleName());
    }

    /**
     * Get restaurant profile by ID (for customer access - only APPROVED restaurants)
     */
    public Optional<RestaurantProfile> getRestaurantById(Integer restaurantId) {
        Optional<RestaurantProfile> restaurantOpt = restaurantRepository.findById(restaurantId);
        
        // Only return APPROVED restaurants for customer access
        if (restaurantOpt.isPresent()) {
            RestaurantProfile restaurant = restaurantOpt.get();
            if (restaurant.getApprovalStatus() == com.example.booking.common.enums.RestaurantApprovalStatus.APPROVED) {
                return Optional.of(restaurant);
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Get restaurant profile by ID (for admin/owner access - all statuses)
     */
    public Optional<RestaurantProfile> getRestaurantByIdForAdmin(Integer restaurantId) {
        return restaurantRepository.findById(restaurantId);
    }

    /**
     * Get all restaurants (for now using existing RestaurantRepository)
     */
    public List<RestaurantProfile> getAllRestaurants() {
        // Using existing repository - this will work with current database structure
        return restaurantRepository.findAll();
    }


    /**
     * Create new restaurant profile
     */
    public RestaurantProfile createRestaurantProfile(RestaurantProfile restaurantProfile) {
        // Set creation timestamp
        restaurantProfile.setCreatedAt(LocalDateTime.now());
        
        // Save restaurant
        RestaurantProfile savedRestaurant = restaurantRepository.save(restaurantProfile);
        
        // Notify admin about new restaurant registration
        try {
            restaurantNotificationService.notifyAdminNewRegistration(savedRestaurant);
        } catch (Exception e) {
            logger.warn("Failed to notify admin about new restaurant registration", e);
        }
        
        return savedRestaurant;
    }

    /**
     * Update restaurant profile
     */
    public RestaurantProfile updateRestaurantProfile(RestaurantProfile restaurantProfile) {
        // Set update timestamp
        restaurantProfile.setUpdatedAt(LocalDateTime.now());
        return restaurantRepository.save(restaurantProfile);
    }

    /**
     * Delete restaurant profile
     */
    public void deleteRestaurantProfile(Integer restaurantId) {
        restaurantRepository.deleteById(restaurantId);
    }

    /**
     * Get restaurant statistics
     */
    public RestaurantStats getRestaurantStats(Integer restaurantId) {
        // Calculate real statistics from database
        RestaurantStats stats = new RestaurantStats();
        
        // Get total bookings for this restaurant
        List<Booking> allBookings = bookingRepository.findAll();
        stats.setTotalBookings(allBookings.size());
        
        // Get active bookings (confirmed)
        long activeBookings = allBookings.stream()
            .filter(booking -> booking.getStatus().toString().equals("CONFIRMED"))
            .count();
        stats.setActiveBookings(activeBookings);
        
        // Get total tables
        List<RestaurantTable> allTables = diningTableRepository.findAll();
        stats.setTotalTables(allTables.size());
        
        // Get available tables
        long availableTables = allTables.stream()
            .filter(table -> table.getStatus().toString().equals("AVAILABLE"))
            .count();
        stats.setAvailableTables(availableTables);
        
        // Calculate average rating (placeholder)
        stats.setAverageRating(4.5);
        
        return stats;
    }

    // ===== TABLE MANAGEMENT =====

    /**
     * Create new table
     */
    public RestaurantTable createTable(RestaurantTable table) {
        return diningTableRepository.save(table);
    }

    /**
     * Update table
     */
    public RestaurantTable updateTable(RestaurantTable table) {
        return diningTableRepository.save(table);
    }

    /**
     * Delete table
     */
    public void deleteTable(Integer tableId) {
        diningTableRepository.deleteById(tableId);
    }

    /**
     * Get table by ID
     */
    public Optional<RestaurantTable> getTableById(Integer tableId) {
        return diningTableRepository.findById(tableId);
    }

    // ===== DISH MANAGEMENT =====

    /**
     * Create new dish
     */
    public Dish createDish(Dish dish) {
        return dishRepository.save(dish);
    }

    /**
     * Update dish
     */
    public Dish updateDish(Dish dish) {
        return dishRepository.save(dish);
    }

    /**
     * Delete dish
     */
    public void deleteDish(Integer dishId) {
        dishRepository.deleteById(dishId);
    }

    /**
     * Get dish by ID
     */
    public Optional<Dish> getDishById(Integer dishId) {
        return dishRepository.findById(dishId);
    }

    // ===== MEDIA MANAGEMENT =====

    /**
     * Create new media
     */
    public RestaurantMedia createMedia(RestaurantMedia media) {
        return restaurantMediaRepository.save(media);
    }

    /**
     * Update media
     */
    public RestaurantMedia updateMedia(RestaurantMedia media) {
        return restaurantMediaRepository.save(media);
    }

    /**
     * Delete media
     */
    public void deleteMedia(Integer mediaId) {
        restaurantMediaRepository.deleteById(mediaId);
    }

    /**
     * Get media by ID
     */
    public Optional<RestaurantMedia> getMediaById(Integer mediaId) {
        return restaurantMediaRepository.findById(mediaId);
    }

    /**
     * Get all media by restaurant
     */
    public List<RestaurantMedia> getMediaByRestaurant(RestaurantProfile restaurant) {
        return restaurantMediaRepository.findByRestaurant(restaurant);
    }

    /**
     * Get media by restaurant and type
     */
    public List<RestaurantMedia> getMediaByRestaurantAndType(RestaurantProfile restaurant, String type) {
        return restaurantMediaRepository.findByRestaurantAndType(restaurant, type);
    }

    /**
     * Get restaurant by owner username
     */
    public Optional<RestaurantProfile> getRestaurantByOwnerUsername(String username) {
        List<RestaurantProfile> restaurants = restaurantProfileRepository.findByOwnerUsername(username);
        return restaurants.isEmpty() ? Optional.empty() : Optional.of(restaurants.get(0));
    }
    
    /**
     * Inner class for restaurant statistics
     */
    public static class RestaurantStats {
        private long totalBookings;
        private long activeBookings;
        private long totalTables;
        private long availableTables;
        private double averageRating;
        
        // Getters and setters
        public long getTotalBookings() { return totalBookings; }
        public void setTotalBookings(long totalBookings) { this.totalBookings = totalBookings; }
        
        public long getActiveBookings() { return activeBookings; }
        public void setActiveBookings(long activeBookings) { this.activeBookings = activeBookings; }
        
        public long getTotalTables() { return totalTables; }
        public void setTotalTables(long totalTables) { this.totalTables = totalTables; }
        
        public long getAvailableTables() { return availableTables; }
        public void setAvailableTables(long availableTables) { this.availableTables = availableTables; }
        
        public double getAverageRating() { return averageRating; }
        public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
    }
}
