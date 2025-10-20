package com.example.booking.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.common.enums.ServiceStatus;
import com.example.booking.domain.Dish;
import com.example.booking.domain.DishStatus;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantService;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.repository.DishRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.RestaurantServiceRepository;
import com.example.booking.repository.RestaurantTableRepository;
import com.example.booking.repository.RestaurantMediaRepository;
import com.example.booking.domain.RestaurantMedia;

@Service
@Transactional
public class RestaurantManagementService {

    @Autowired
    private RestaurantProfileRepository restaurantProfileRepository;

    @Autowired
    private RestaurantTableRepository restaurantTableRepository;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private RestaurantServiceRepository restaurantServiceRepository;

    @Autowired
    private RestaurantMediaRepository restaurantMediaRepository;

    /**
     * Lấy tất cả nhà hàng (chỉ APPROVED cho customer)
     */
    @Transactional(readOnly = true)
    public List<RestaurantProfile> findAllRestaurants() {
        return restaurantProfileRepository.findByApprovalStatus(com.example.booking.common.enums.RestaurantApprovalStatus.APPROVED);
    }

    /**
     * Tìm nhà hàng theo ID
     */
    @Transactional(readOnly = true)
    public Optional<RestaurantProfile> findRestaurantById(Integer restaurantId) {
        return restaurantProfileRepository.findById(restaurantId);
    }

    /**
     * Tìm nhà hàng theo tên (chỉ APPROVED)
     */
    @Transactional(readOnly = true)
    public List<RestaurantProfile> findRestaurantsByName(String name) {
        return restaurantProfileRepository.findByRestaurantNameContainingIgnoreCaseAndApprovalStatus(
            name, com.example.booking.common.enums.RestaurantApprovalStatus.APPROVED);
    }

    /**
     * Lấy nhà hàng theo owner
     */
    @Transactional(readOnly = true)
    public List<RestaurantProfile> findRestaurantsByOwner(java.util.UUID ownerId) {
        return restaurantProfileRepository.findByOwnerOwnerId(ownerId);
    }

    /**
     * Lấy danh sách bàn của nhà hàng
     */
    @Transactional(readOnly = true)
    public List<RestaurantTable> findTablesByRestaurant(Integer restaurantId) {
        try {
            // Thêm logging để debug
            System.out.println("🔍 Finding tables for restaurant ID: " + restaurantId);

            List<RestaurantTable> tables = restaurantTableRepository
                    .findByRestaurantRestaurantIdOrderByTableName(restaurantId);

            System.out.println("✅ Found " + tables.size() + " tables");
            tables.forEach(table -> {
                System.out.println("   - " + table.getTableName() + " (Capacity: "
                        + table.getCapacity() + ", Deposit: " + table.getDepositAmount() + ")");

                // Eagerly initialize related data to avoid LazyInitializationException later (API, templates…)
                try {
                    if (table.getRestaurant() != null && table.getRestaurant().getMedia() != null) {
                        table.getRestaurant().getMedia().size(); // trigger load
                    }
                    // Invoke helper once to cache results
                    table.getTableImages();
                } catch (Exception initEx) {
                    System.err.println("⚠️ Unable to preload table media for table "
                            + table.getTableId() + ": " + initEx.getMessage());
                }
            });

            return tables;
        } catch (Exception e) {
            System.err.println("❌ Error finding tables: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Tìm bàn theo ID
     */
    @Transactional(readOnly = true)
    public Optional<RestaurantTable> findTableById(Integer tableId) {
        return restaurantTableRepository.findById(tableId);
    }

    /**
     * Lưu nhà hàng
     */
    public RestaurantProfile saveRestaurant(RestaurantProfile restaurant) {
        return restaurantProfileRepository.save(restaurant);
    }

    /**
     * Lưu bàn
     */
    public RestaurantTable saveTable(RestaurantTable table) {
        return restaurantTableRepository.save(table);
    }

    /**
     * Lấy danh sách món ăn của nhà hàng
     */
    @Transactional(readOnly = true)
    public List<Dish> findDishesByRestaurant(Integer restaurantId) {
        return dishRepository.findByRestaurantRestaurantIdAndStatusOrderByNameAsc(restaurantId, DishStatus.AVAILABLE);
    }

    /**
     * Lấy media theo nhà hàng và loại
     */
    @Transactional(readOnly = true)
    public List<RestaurantMedia> findMediaByRestaurantAndType(Integer restaurantId, String type) {
        try {
            Optional<RestaurantProfile> restaurant = findRestaurantById(restaurantId);
            if (restaurant.isEmpty()) {
                System.out.println("❌ Restaurant not found for ID: " + restaurantId);
                return new ArrayList<>();
            }

            List<RestaurantMedia> media = restaurantMediaRepository.findByRestaurantAndType(restaurant.get(), type);
            System.out.println("✅ Found " + media.size() + " " + type + " media for restaurant " + restaurantId);
            return media;
        } catch (Exception e) {
            System.err.println("❌ Error finding media: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Lấy danh sách dịch vụ của nhà hàng
     */
    @Transactional(readOnly = true)
    public List<RestaurantService> findServicesByRestaurant(Integer restaurantId) {
        System.out.println("🔍 Looking for services for restaurant " + restaurantId);
        try {
            // Use the proper repository method with AVAILABLE status
            List<RestaurantService> services = restaurantServiceRepository
                    .findByRestaurantRestaurantIdAndStatusOrderByNameAsc(restaurantId, ServiceStatus.AVAILABLE);
            System.out.println("🔍 Found " + services.size() + " available services for restaurant " + restaurantId);

            // Log each service for debugging
            for (RestaurantService service : services) {
                System.out.println("   - Service: " + service.getName() + " (ID: " + service.getServiceId()
                        + ", Status: " + service.getStatus() + ")");
            }

            return services;
        } catch (Exception e) {
            System.err.println("❌ Error finding services: " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }

    /**
     * ===== PERFORMANCE OPTIMIZATION: Push filters to database =====
     * Lấy danh sách nhà hàng với bộ lọc và phân trang
     * BEFORE: Load Integer.MAX_VALUE restaurants, filter in Java, paginate in Java
     * AFTER: Push all filters to database query, let database handle filtering + pagination
     */
    @Transactional(readOnly = true)
    public Page<RestaurantProfile> getRestaurantsWithFilters(Pageable pageable, 
            String search, String cuisineType, String priceRange, String ratingFilter) {
        
        System.out.println("=== OPTIMIZED RESTAURANT FILTERS (DB-Level) ===");
        System.out.println("Search: " + search);
        System.out.println("Cuisine Type: " + cuisineType);
        System.out.println("Price Range: " + priceRange);
        System.out.println("Rating Filter: " + ratingFilter);
        System.out.println("Sort By: " + pageable.getSort());
        
        // Convert UI filter strings to database query parameters
        java.math.BigDecimal minPrice = null;
        java.math.BigDecimal maxPrice = null;
        if (priceRange != null && !priceRange.trim().isEmpty()) {
            switch (priceRange) {
                case "under-50k":
                    maxPrice = new java.math.BigDecimal("50000");
                    break;
                case "50k-100k":
                    minPrice = new java.math.BigDecimal("50000");
                    maxPrice = new java.math.BigDecimal("100000");
                    break;
                case "100k-200k":
                    minPrice = new java.math.BigDecimal("100000");
                    maxPrice = new java.math.BigDecimal("200000");
                    break;
                case "over-200k":
                    minPrice = new java.math.BigDecimal("200000");
                    break;
            }
        }
        
        Double minRating = null;
        if (ratingFilter != null && !ratingFilter.trim().isEmpty()) {
            switch (ratingFilter) {
                case "5-star":
                    minRating = 5.0;
                    break;
                case "4-star":
                    minRating = 4.0;
                    break;
                case "3-star":
                    minRating = 3.0;
                    break;
                case "2-star":
                    minRating = 2.0;
                    break;
            }
        }
        
        // Single database query with all filters and pagination
        Page<RestaurantProfile> result = restaurantProfileRepository.findApprovedWithFilters(
            search, cuisineType, minPrice, maxPrice, minRating, pageable);
        
        // Apply rating filter in Java (since averageRating is computed, not a DB column)
        if (minRating != null) {
            final Double finalMinRating = minRating;
            List<RestaurantProfile> filteredContent = result.getContent().stream()
                .filter(r -> r.getAverageRating() >= finalMinRating)
                .collect(java.util.stream.Collectors.toList());
            result = new PageImpl<>(filteredContent, pageable, filteredContent.size());
            System.out.println("⚠️  Rating filter applied in Java (computed field)");
        }
        
        System.out.println("✅ DB returned " + result.getContent().size() + " restaurants (page " + 
                          result.getNumber() + " of " + result.getTotalPages() + ", total: " + 
                          result.getTotalElements() + ")");
        System.out.println("===============================");
        
        return result;
    }
}
