package com.example.booking.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
            tables.forEach(table -> System.out.println("   - " + table.getTableName() + " (Capacity: "
                    + table.getCapacity() + ", Deposit: " + table.getDepositAmount() + ")"));

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
     * Lấy danh sách nhà hàng với bộ lọc và phân trang
     */
    @Transactional(readOnly = true)
    public Page<RestaurantProfile> getRestaurantsWithFilters(Pageable pageable, 
            String search, String cuisineType, String priceRange, String ratingFilter) {
        
        System.out.println("=== DEBUG RESTAURANT FILTERS ===");
        System.out.println("Search: " + search);
        System.out.println("Cuisine Type: " + cuisineType);
        System.out.println("Price Range: " + priceRange);
        System.out.println("Rating Filter: " + ratingFilter);
        System.out.println("Sort By: " + pageable.getSort());
        
        // Get only APPROVED restaurants first (without pagination to apply filters first)
        Pageable allPageable = PageRequest.of(0, Integer.MAX_VALUE, pageable.getSort());
        Page<RestaurantProfile> allRestaurants = restaurantProfileRepository.findByApprovalStatus(
            com.example.booking.common.enums.RestaurantApprovalStatus.APPROVED, allPageable);
        
        List<RestaurantProfile> filteredRestaurants = new ArrayList<>();
        for (RestaurantProfile restaurant : allRestaurants.getContent()) {
            // Apply filters
            if (matchesFilters(restaurant, search, cuisineType, priceRange, ratingFilter)) {
                filteredRestaurants.add(restaurant);
                System.out.println("✅ Matched: " + restaurant.getRestaurantName());
            } else {
                System.out.println("❌ Filtered out: " + restaurant.getRestaurantName());
            }
        }
        
        // Apply sorting to filtered results
        filteredRestaurants = applySorting(filteredRestaurants, pageable.getSort());
        
        // Apply pagination to sorted results
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredRestaurants.size());
        List<RestaurantProfile> paginatedRestaurants = filteredRestaurants.subList(start, end);
        
        System.out.println("Filtered results: " + filteredRestaurants.size() + " out of " + allRestaurants.getContent().size());
        System.out.println("Paginated results: " + paginatedRestaurants.size());
        System.out.println("===============================");
        
        return new PageImpl<>(paginatedRestaurants, pageable, filteredRestaurants.size());
    }
    
    /**
     * Apply sorting to the filtered results
     */
    private List<RestaurantProfile> applySorting(List<RestaurantProfile> restaurants, Sort sort) {
        if (sort.isUnsorted()) {
            return restaurants;
        }
        
        return restaurants.stream()
            .sorted((r1, r2) -> {
                for (Sort.Order order : sort) {
                    int comparison = compareByField(r1, r2, order.getProperty());
                    if (comparison != 0) {
                        return order.getDirection() == Sort.Direction.ASC ? comparison : -comparison;
                    }
                }
                return 0;
            })
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Compare two restaurants by field
     */
    private int compareByField(RestaurantProfile r1, RestaurantProfile r2, String field) {
        switch (field) {
            case "restaurantName":
                return String.CASE_INSENSITIVE_ORDER.compare(
                    r1.getRestaurantName() != null ? r1.getRestaurantName() : "",
                    r2.getRestaurantName() != null ? r2.getRestaurantName() : ""
                );
            case "averagePrice":
                return Double.compare(
                    r1.getAveragePrice() != null ? r1.getAveragePrice().doubleValue() : 0.0,
                    r2.getAveragePrice() != null ? r2.getAveragePrice().doubleValue() : 0.0
                );
            case "averageRating":
                return Double.compare(
                    r1.getAverageRating(),
                    r2.getAverageRating()
                );
            case "createdAt":
                return r1.getCreatedAt().compareTo(r2.getCreatedAt());
            default:
                return 0;
        }
    }
    
    /**
     * Check if restaurant matches the applied filters
     */
    private boolean matchesFilters(RestaurantProfile restaurant, String search, String cuisineType, 
                                 String priceRange, String ratingFilter) {
        
        // APPROVAL STATUS FILTER - Only show APPROVED restaurants to customers
        if (restaurant.getApprovalStatus() != com.example.booking.common.enums.RestaurantApprovalStatus.APPROVED) {
            return false;
        }
        
        // Search filter
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            boolean matchesSearch = (restaurant.getRestaurantName() != null && restaurant.getRestaurantName().toLowerCase().contains(searchLower)) ||
                                  (restaurant.getAddress() != null && restaurant.getAddress().toLowerCase().contains(searchLower)) ||
                                  (restaurant.getCuisineType() != null && restaurant.getCuisineType().toLowerCase().contains(searchLower));
            if (!matchesSearch) {
                return false;
            }
        }
        
        // Cuisine type filter
        if (cuisineType != null && !cuisineType.trim().isEmpty()) {
            if (restaurant.getCuisineType() == null || !restaurant.getCuisineType().equals(cuisineType)) {
                return false;
            }
        }
        
        // Price range filter
        if (priceRange != null && !priceRange.trim().isEmpty() && restaurant.getAveragePrice() != null) {
            double price = restaurant.getAveragePrice().doubleValue();
            boolean matchesPrice = false;
            
            switch (priceRange) {
                case "under-50k":
                    matchesPrice = price < 50000;
                    break;
                case "50k-100k":
                    matchesPrice = price >= 50000 && price <= 100000;
                    break;
                case "100k-200k":
                    matchesPrice = price >= 100000 && price <= 200000;
                    break;
                case "over-200k":
                    matchesPrice = price > 200000;
                    break;
            }
            
            if (!matchesPrice) {
                return false;
            }
        }
        
        // Rating filter
        if (ratingFilter != null && !ratingFilter.trim().isEmpty()) {
            double rating = restaurant.getAverageRating();
            boolean matchesRating = false;
            
            switch (ratingFilter) {
                case "5-star":
                    matchesRating = rating >= 5.0;
                    break;
                case "4-star":
                    matchesRating = rating >= 4.0;
                    break;
                case "3-star":
                    matchesRating = rating >= 3.0;
                    break;
                case "2-star":
                    matchesRating = rating >= 2.0;
                    break;
            }
            
            if (!matchesRating) {
                return false;
            }
        }
        
        return true;
    }
}