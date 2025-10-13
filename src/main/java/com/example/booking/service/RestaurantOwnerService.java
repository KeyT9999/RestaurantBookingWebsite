    package com.example.booking.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
import com.example.booking.service.ImageUploadService;
import com.example.booking.dto.DishWithImageDto;

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
    private final ImageUploadService imageUploadService;

    @Autowired
    public RestaurantOwnerService(RestaurantRepository restaurantRepository,
            RestaurantOwnerRepository restaurantOwnerRepository,
            RestaurantProfileRepository restaurantProfileRepository,
                                BookingRepository bookingRepository,
                                DiningTableRepository diningTableRepository,
                                DishRepository dishRepository,
            RestaurantMediaRepository restaurantMediaRepository,
            SimpleUserService userService,
            RestaurantNotificationService restaurantNotificationService,
            ImageUploadService imageUploadService) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantOwnerRepository = restaurantOwnerRepository;
        this.restaurantProfileRepository = restaurantProfileRepository;
        this.bookingRepository = bookingRepository;
        this.diningTableRepository = diningTableRepository;
        this.dishRepository = dishRepository;
        this.restaurantMediaRepository = restaurantMediaRepository;
        this.userService = userService;
        this.restaurantNotificationService = restaurantNotificationService;
        this.imageUploadService = imageUploadService;
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
        try {
            // Delete all images from Cloudinary first
            String folderPath = "restaurants/" + restaurantId;
            boolean folderDeleted = imageUploadService.deleteFolderResources(folderPath);
            if (folderDeleted) {
                logger.info("Successfully deleted all images from Cloudinary folder: {}", folderPath);
            } else {
                logger.warn("Failed to delete some images from Cloudinary folder: {}", folderPath);
            }

            // Delete restaurant from database (cascade delete will handle related entities)
            restaurantRepository.deleteById(restaurantId);
            logger.info("Successfully deleted restaurant profile with ID: {}", restaurantId);

        } catch (Exception e) {
            logger.error("Error deleting restaurant profile with ID: {}", restaurantId, e);
            throw e;
        }
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
        logger.info("DEBUG: RestaurantOwnerService.createTable called with table: {}", table);
        logger.info("DEBUG: Table restaurant: {}", table.getRestaurant());
        logger.info("DEBUG: Table name: {}, capacity: {}", table.getTableName(), table.getCapacity());

        RestaurantTable savedTable = diningTableRepository.save(table);
        logger.info("DEBUG: Table saved with ID: {}", savedTable.getTableId());

        return savedTable;
    }

    /**
     * Update table
     */
    public RestaurantTable updateTable(RestaurantTable table) {
        logger.info("DEBUG: RestaurantOwnerService.updateTable called with tableId: {}", table.getTableId());
        logger.info("DEBUG: Table object state - hashCode: {}, toString: {}", table.hashCode(), table.toString());

        RestaurantTable updatedTable = diningTableRepository.save(table);
        logger.info("DEBUG: Table updated successfully");
        logger.info("DEBUG: Updated table object state - hashCode: {}, toString: {}", updatedTable.hashCode(),
                updatedTable.toString());

        // Verify the save by querying the database
        Optional<RestaurantTable> verifyTable = diningTableRepository.findById(table.getTableId());
        if (verifyTable.isPresent()) {
            logger.info("DEBUG: Database verification - table saved successfully");
        } else {
            logger.error("DEBUG: Database verification failed - table not found!");
        }

        return updatedTable;
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

    // ===== DISH IMAGE MANAGEMENT =====

    /**
     * Get dish image URL by restaurant and dish ID
     */
    public String getDishImageUrl(Integer restaurantId, Integer dishId) {
        Optional<RestaurantProfile> restaurant = getRestaurantById(restaurantId);
        if (restaurant.isEmpty()) {
            logger.warn("Restaurant not found for ID: {}", restaurantId);
            return null;
        }

        String dishIdPattern = "/dish_" + dishId + "_";
        RestaurantMedia dishImage = restaurantMediaRepository.findDishImageByRestaurantAndDishId(restaurant.get(),
                dishIdPattern);

        return dishImage != null ? dishImage.getUrl() : null;
    }

    // ===== TABLE IMAGE MANAGEMENT =====

    /**
     * Get all table images by restaurant and table ID
     */
    public List<RestaurantMedia> getTableImages(Integer restaurantId, Integer tableId) {
        Optional<RestaurantProfile> restaurant = getRestaurantById(restaurantId);
        if (restaurant.isEmpty()) {
            logger.warn("Restaurant not found for ID: {}", restaurantId);
            return new ArrayList<>();
        }

        String tableIdPattern = "/table_" + tableId + "_";
        return restaurantMediaRepository.findTableImagesByRestaurantAndTableId(restaurant.get(), tableIdPattern);
    }

    /**
     * Upload multiple table images
     */
    public List<RestaurantMedia> uploadTableImages(Integer restaurantId, Integer tableId, MultipartFile[] files) {
        Optional<RestaurantProfile> restaurant = getRestaurantById(restaurantId);
        if (restaurant.isEmpty()) {
            throw new IllegalArgumentException("Restaurant not found for ID: " + restaurantId);
        }

        List<RestaurantMedia> savedImages = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            try {
                String folder = "restaurants/" + restaurantId + "/media/table";
                String publicId = "table_" + tableId + "_" + System.currentTimeMillis() + "_" + savedImages.size();
                String imageUrl = imageUploadService.uploadImage(file, folder, publicId);

                RestaurantMedia media = new RestaurantMedia();
                media.setRestaurant(restaurant.get());
                media.setType("table");
                media.setUrl(imageUrl);
                media.setCreatedAt(LocalDateTime.now());

                RestaurantMedia savedMedia = restaurantMediaRepository.save(media);
                savedImages.add(savedMedia);
                logger.info("Uploaded table image for restaurant {} table {}: {}", restaurantId, tableId, imageUrl);
            } catch (Exception e) {
                logger.error("Failed to upload table image for restaurant {} table {}: {}", restaurantId, tableId,
                        e.getMessage());
            }
        }
        return savedImages;
    }

    /**
     * Delete specific table image
     */
    public void deleteTableImage(Integer mediaId) {
        Optional<RestaurantMedia> media = restaurantMediaRepository.findById(mediaId);
        if (media.isEmpty()) {
            logger.warn("Table image not found for ID: {}", mediaId);
            return;
        }

        RestaurantMedia mediaToDelete = media.get();
        if (mediaToDelete.getUrl() != null && mediaToDelete.getUrl().startsWith("http")) {
            try {
                imageUploadService.deleteImage(mediaToDelete.getUrl());
                logger.info("Deleted table image from Cloudinary: {}", mediaToDelete.getUrl());
            } catch (Exception e) {
                logger.warn("Failed to delete table image from Cloudinary: {}", e.getMessage());
            }
        }
        restaurantMediaRepository.delete(mediaToDelete);
        logger.info("Deleted table image from database: {}", mediaId);
    }

    /**
     * Save dish image to restaurant_media
     */
    public RestaurantMedia saveDishImage(Integer restaurantId, Integer dishId, String imageUrl) {
        Optional<RestaurantProfile> restaurant = getRestaurantById(restaurantId);
        if (restaurant.isEmpty()) {
            throw new IllegalArgumentException("Restaurant not found for ID: " + restaurantId);
        }

        // Delete existing dish image if any
        deleteDishImage(restaurantId, dishId);

        // Create new dish image record
        RestaurantMedia dishImage = new RestaurantMedia();
        dishImage.setRestaurant(restaurant.get());
        dishImage.setType("dish");
        dishImage.setUrl(imageUrl);

        return restaurantMediaRepository.save(dishImage);
    }

    /**
     * Delete dish image from restaurant_media
     */
    public void deleteDishImage(Integer restaurantId, Integer dishId) {
        Optional<RestaurantProfile> restaurant = getRestaurantById(restaurantId);
        if (restaurant.isEmpty()) {
            logger.warn("Restaurant not found for ID: {}", restaurantId);
            return;
        }

        String dishIdPattern = "/dish_" + dishId + "_";
        List<RestaurantMedia> dishImages = restaurantMediaRepository
                .findDishImagesByRestaurantAndDishId(restaurant.get(), dishIdPattern);

        for (RestaurantMedia image : dishImages) {
            // Delete from Cloudinary if it's a Cloudinary URL
            if (image.getUrl() != null && image.getUrl().startsWith("http")) {
                try {
                    imageUploadService.deleteImage(image.getUrl());
                } catch (Exception e) {
                    logger.warn("Failed to delete image from Cloudinary: {}", e.getMessage());
                }
            }
            // Delete from database
            restaurantMediaRepository.delete(image);
        }
    }

    /**
     * Get all dishes by restaurant with their images
     */
    public List<DishWithImageDto> getDishesByRestaurantWithImages(Integer restaurantId) {
        List<Dish> dishes = dishRepository.findByRestaurantRestaurantIdOrderByNameAsc(restaurantId);

        // Convert to DTO with image URLs
        return dishes.stream()
                .map(dish -> {
                    String imageUrl = getDishImageUrl(restaurantId, dish.getDishId());
                    return new DishWithImageDto(dish, imageUrl);
                })
                .toList();
    }

    /**
     * Get restaurant by owner username
     */
    public Optional<RestaurantProfile> getRestaurantByOwnerUsername(String username) {
        List<RestaurantProfile> restaurants = restaurantProfileRepository.findByOwnerUsername(username);
        return restaurants.isEmpty() ? Optional.empty() : Optional.of(restaurants.get(0));
    }

    // ===== RESTAURANT MEDIA MANAGEMENT =====

    /**
     * Get restaurant media for management (excludes managed types)
     */
    public List<RestaurantMedia> getRestaurantMediaForManagement(Integer restaurantId) {
        Optional<RestaurantProfile> restaurant = getRestaurantById(restaurantId);
        if (restaurant.isEmpty()) {
            logger.warn("Restaurant not found for ID: {}", restaurantId);
            return new ArrayList<>();
        }

        // Get all media for this restaurant
        List<RestaurantMedia> allMedia = restaurantMediaRepository.findByRestaurant(restaurant.get());

        // Filter out managed types (logo, cover, business_license, dish, table)
        List<String> managedTypes = List.of("logo", "cover", "business_license", "dish", "table");

        return allMedia.stream()
                .filter(media -> !managedTypes.contains(media.getType()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Upload restaurant media (multiple files, one type)
     */
    public List<RestaurantMedia> uploadRestaurantMedia(Integer restaurantId, String mediaType, MultipartFile[] files) {
        Optional<RestaurantProfile> restaurant = getRestaurantById(restaurantId);
        if (restaurant.isEmpty()) {
            throw new IllegalArgumentException("Restaurant not found for ID: " + restaurantId);
        }

        // Validate media type
        List<String> allowedTypes = List.of("gallery", "menu", "interior", "exterior", "table_layout");
        if (!allowedTypes.contains(mediaType)) {
            throw new IllegalArgumentException("Invalid media type: " + mediaType);
        }

        List<RestaurantMedia> savedMedia = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }

            try {
                // Upload to Cloudinary
                String folder = "restaurants/" + restaurantId + "/media/" + mediaType;
                String publicId = mediaType + "_" + System.currentTimeMillis() + "_" + savedMedia.size();
                String imageUrl = imageUploadService.uploadImage(file, folder, publicId);

                // Save to database
                RestaurantMedia media = new RestaurantMedia();
                media.setRestaurant(restaurant.get());
                media.setType(mediaType);
                media.setUrl(imageUrl);
                media.setCreatedAt(LocalDateTime.now());

                RestaurantMedia savedMediaItem = restaurantMediaRepository.save(media);
                savedMedia.add(savedMediaItem);

                logger.info("Uploaded {} media for restaurant {}: {}", mediaType, restaurantId, imageUrl);

            } catch (Exception e) {
                logger.error("Failed to upload {} media for restaurant {}: {}", mediaType, restaurantId,
                        e.getMessage());
                // Continue with other files even if one fails
            }
        }

        return savedMedia;
    }

    /**
     * Delete restaurant media
     */
    public void deleteRestaurantMedia(Integer mediaId) {
        Optional<RestaurantMedia> media = restaurantMediaRepository.findById(mediaId);
        if (media.isEmpty()) {
            logger.warn("Media not found for ID: {}", mediaId);
            return;
        }

        RestaurantMedia mediaToDelete = media.get();

        // Delete from Cloudinary if it's a Cloudinary URL
        if (mediaToDelete.getUrl() != null && mediaToDelete.getUrl().startsWith("http")) {
            try {
                imageUploadService.deleteImage(mediaToDelete.getUrl());
                logger.info("Deleted media from Cloudinary: {}", mediaToDelete.getUrl());
            } catch (Exception e) {
                logger.warn("Failed to delete media from Cloudinary: {}", e.getMessage());
            }
        }

        // Delete from database
        restaurantMediaRepository.delete(mediaToDelete);
        logger.info("Deleted media from database: {}", mediaId);
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
