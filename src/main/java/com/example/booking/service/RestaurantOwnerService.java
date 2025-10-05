    package com.example.booking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.domain.Booking;
import com.example.booking.domain.Dish;
import com.example.booking.domain.RestaurantMedia;
import com.example.booking.repository.RestaurantRepository;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.DiningTableRepository;
import com.example.booking.repository.DishRepository;
import com.example.booking.repository.RestaurantMediaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for Restaurant Owner management operations
 * Handles restaurant profile, tables, bookings, and media management
 */
@Service
@Transactional
public class RestaurantOwnerService {

    private final RestaurantRepository restaurantRepository;
    private final BookingRepository bookingRepository;
    private final DiningTableRepository diningTableRepository;
    private final DishRepository dishRepository;
    private final RestaurantMediaRepository restaurantMediaRepository;

    @Autowired
    public RestaurantOwnerService(RestaurantRepository restaurantRepository,
                                BookingRepository bookingRepository,
                                DiningTableRepository diningTableRepository,
                                DishRepository dishRepository,
                                RestaurantMediaRepository restaurantMediaRepository) {
        this.restaurantRepository = restaurantRepository;
        this.bookingRepository = bookingRepository;
        this.diningTableRepository = diningTableRepository;
        this.dishRepository = dishRepository;
        this.restaurantMediaRepository = restaurantMediaRepository;
    }

    /**
     * Get restaurant owner by user ID
     */
    public Optional<RestaurantOwner> getRestaurantOwnerByUserId(UUID userId) {
        // TODO: Implement when RestaurantOwnerRepository is available
        // For now, return empty - will be implemented when RestaurantOwner entity is properly set up
        return Optional.empty();
    }

    /**
     * Get all restaurants owned by a specific owner
     */
    public List<RestaurantProfile> getRestaurantsByOwnerId(UUID ownerId) {
        // TODO: Implement when RestaurantProfileRepository is available
        // For now, return empty list - will be implemented when RestaurantProfile entity is properly set up
        return List.of();
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
     * Get restaurant profile by ID
     */
    public Optional<RestaurantProfile> getRestaurantById(Integer restaurantId) {
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
        return restaurantRepository.save(restaurantProfile);
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
