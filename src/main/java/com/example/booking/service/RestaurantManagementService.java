package com.example.booking.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
     * Get all distinct cuisine types from approved restaurants
     * Used for populating filter dropdowns
     */
    @Transactional(readOnly = true)
    public List<String> getAllCuisineTypes() {
        List<String> cuisineTypes = restaurantProfileRepository.findDistinctCuisineTypes();
        // Normalize and filter out null/empty values
        return cuisineTypes.stream()
                .filter(ct -> ct != null && !ct.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Lấy tất cả nhà hàng (chỉ APPROVED cho customer)
     */
    @Transactional(readOnly = true)
    public List<RestaurantProfile> findAllRestaurants() {
        return restaurantProfileRepository.findApprovedExcludingAI();
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
        List<RestaurantProfile> restaurants = restaurantProfileRepository
                .findByRestaurantNameContainingIgnoreCaseAndApprovalStatus(
            name, com.example.booking.common.enums.RestaurantApprovalStatus.APPROVED);
        // Filter out AI restaurant (ID = 37)
        return restaurants.stream()
                .filter(r -> !r.getRestaurantId().equals(37))
                .collect(java.util.stream.Collectors.toList());
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

            // Use eager fetch query to avoid lazy loading issues
            List<RestaurantTable> tables = restaurantTableRepository
                    .findByRestaurantRestaurantIdWithEagerFetch(restaurantId);

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
     * Lấy danh sách nhà hàng được đánh giá cao nhất
     */
    @Transactional(readOnly = true)
    public List<RestaurantProfile> findTopRatedRestaurants(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        Pageable pageable = PageRequest.of(0, limit);
        return restaurantProfileRepository.findTopRatedRestaurants(pageable);
    }

    /**
     * Lấy danh sách nhà hàng approved đơn giản (fallback method)
     * Không cần tính toán phức tạp, chỉ lấy bất kỳ nhà hàng approved nào
     * Sử dụng khi findTopRatedRestaurants() fail hoặc không có dữ liệu
     */
    @Transactional(readOnly = true)
    public List<RestaurantProfile> findApprovedRestaurantsSimple(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        try {
            // Sử dụng query đơn giản, không có JOIN phức tạp
            List<RestaurantProfile> allApproved = restaurantProfileRepository.findApprovedExcludingAI();
            
            // Limit kết quả và return
            if (allApproved.size() <= limit) {
                return allApproved;
            }
            return allApproved.subList(0, limit);
        } catch (Exception e) {
            // Log error nhưng return empty list thay vì throw
            // Method này là fallback nên không nên throw exception
            return Collections.emptyList();
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
        
        // Normalize cuisineType (trim and handle null/empty)
        String normalizedCuisineType = null;
        if (cuisineType != null && !cuisineType.trim().isEmpty()) {
            normalizedCuisineType = cuisineType.trim();
            System.out.println("Normalized Cuisine Type: '" + normalizedCuisineType + "'");
        }
        
        // Convert UI filter strings to database query parameters
        java.math.BigDecimal minPrice = null;
        java.math.BigDecimal maxPrice = null;
        if (priceRange != null && !priceRange.trim().isEmpty()) {
            switch (priceRange) {
                // Old format (for backward compatibility)
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
                // New format (current UI)
                case "low":
                    // Dưới 200k
                    maxPrice = new java.math.BigDecimal("200000");
                    break;
                case "medium":
                    // 200k - 500k
                    minPrice = new java.math.BigDecimal("200000");
                    maxPrice = new java.math.BigDecimal("500000");
                    break;
                case "high":
                    // Trên 500k
                    minPrice = new java.math.BigDecimal("500000");
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
            search, normalizedCuisineType, minPrice, maxPrice, minRating, pageable);
        
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

    /**
     * Get all dishes by restaurant with their images
     */
    @Transactional(readOnly = true)
    public List<com.example.booking.dto.DishWithImageDto> getDishesByRestaurantWithImages(Integer restaurantId) {
        List<Dish> dishes = dishRepository.findByRestaurantRestaurantIdOrderByNameAsc(restaurantId);

        // Convert to DTO with image URLs
        return dishes.stream()
                .map(dish -> {
                    String imageUrl = getDishImageUrl(restaurantId, dish.getDishId());
                    return new com.example.booking.dto.DishWithImageDto(dish, imageUrl);
                })
                .toList();
    }

    /**
     * Get dish image URL by restaurant and dish ID
     * First tries pattern-based lookup, then falls back to type-based lookup
     */
    private String getDishImageUrl(Integer restaurantId, Integer dishId) {
        try {
            Optional<RestaurantProfile> restaurant = restaurantProfileRepository.findById(restaurantId);
            if (restaurant.isEmpty()) {
                return null;
            }

            // Try pattern-based lookup first (for backward compatibility)
            String dishIdPattern = "/dish_" + dishId + "_";
            RestaurantMedia dishImage = restaurantMediaRepository
                    .findDishImageByRestaurantAndDishId(restaurant.get(), dishIdPattern);

            if (dishImage != null) {
                return dishImage.getUrl();
            }

            // Fallback: Get all dish images and map by order
            // This handles cases where images are added without pattern in URL
            List<RestaurantMedia> allDishImages = restaurantMediaRepository
                    .findByRestaurantAndType(restaurant.get(), "dish");
            
            if (allDishImages != null && !allDishImages.isEmpty()) {
                // Get all dishes for this restaurant to find index
                List<Dish> allDishes = dishRepository.findByRestaurantRestaurantIdOrderByNameAsc(restaurantId);
                int dishIndex = -1;
                for (int i = 0; i < allDishes.size(); i++) {
                    if (allDishes.get(i).getDishId().equals(dishId)) {
                        dishIndex = i;
                        break;
                    }
                }
                
                // Return image at same index if available
                if (dishIndex >= 0 && dishIndex < allDishImages.size()) {
                    return allDishImages.get(dishIndex).getUrl();
                }
                
                // If no exact match, return first dish image
                if (!allDishImages.isEmpty()) {
                    return allDishImages.get(0).getUrl();
                }
            }

            return null;
        } catch (Exception e) {
            System.err.println("Error getting dish image URL: " + e.getMessage());
            return null;
        }
    }

    /**
     * Lấy danh sách nhà hàng liên quan dựa trên:
     * 1. Cùng loại ẩm thực
     * 2. Cùng khu vực (địa chỉ tương tự)
     * 3. Loại trừ nhà hàng hiện tại
     * 
     * @param restaurant Nhà hàng hiện tại
     * @param limit Số lượng nhà hàng tối đa (mặc định 6)
     * @return Danh sách nhà hàng liên quan
     */
    @Transactional(readOnly = true)
    public List<RestaurantProfile> findRelatedRestaurants(RestaurantProfile restaurant, int limit) {
        if (restaurant == null || restaurant.getRestaurantId() == null) {
            return Collections.emptyList();
        }

        List<RestaurantProfile> related = new ArrayList<>();
        
        // Lấy tất cả nhà hàng đã được duyệt (trừ nhà hàng hiện tại)
        List<RestaurantProfile> allRestaurants = restaurantProfileRepository.findApprovedExcludingAI();
        allRestaurants = allRestaurants.stream()
                .filter(r -> !r.getRestaurantId().equals(restaurant.getRestaurantId()))
                .collect(java.util.stream.Collectors.toList());

        if (allRestaurants.isEmpty()) {
            return Collections.emptyList();
        }

        // Ưu tiên 1: Cùng loại ẩm thực
        if (restaurant.getCuisineType() != null && !restaurant.getCuisineType().trim().isEmpty()) {
            List<RestaurantProfile> sameCuisine = allRestaurants.stream()
                    .filter(r -> restaurant.getCuisineType().equalsIgnoreCase(r.getCuisineType()))
                    .collect(java.util.stream.Collectors.toList());
            related.addAll(sameCuisine);
        }

        // Ưu tiên 2: Cùng khu vực (kiểm tra địa chỉ có chứa từ khóa chung)
        if (restaurant.getAddress() != null && !restaurant.getAddress().trim().isEmpty()) {
            String address = restaurant.getAddress().toLowerCase();
            // Tìm các từ khóa địa điểm phổ biến
            String[] locationKeywords = {"quận", "huyện", "phường", "đường", "street", "district"};
            
            for (String keyword : locationKeywords) {
                if (address.contains(keyword)) {
                    List<RestaurantProfile> sameLocation = allRestaurants.stream()
                            .filter(r -> r.getAddress() != null && 
                                    r.getAddress().toLowerCase().contains(keyword) &&
                                    !related.contains(r))
                            .limit(limit - related.size())
                            .collect(java.util.stream.Collectors.toList());
                    related.addAll(sameLocation);
                    break;
                }
            }
        }

        // Nếu chưa đủ, thêm các nhà hàng khác (sắp xếp theo rating)
        if (related.size() < limit) {
            List<RestaurantProfile> others = allRestaurants.stream()
                    .filter(r -> !related.contains(r))
                    .sorted((r1, r2) -> {
                        double rating1 = r1.getAverageRating();
                        double rating2 = r2.getAverageRating();
                        return Double.compare(rating2, rating1);
                    })
                    .limit(limit - related.size())
                    .collect(java.util.stream.Collectors.toList());
            related.addAll(others);
        }

        // Giới hạn số lượng và loại bỏ trùng lặp
        return related.stream()
                .distinct()
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Kiểm tra nhà hàng có đang mở cửa không dựa trên openingHours và giờ hiện tại
     * 
     * @param restaurant Nhà hàng cần kiểm tra
     * @return true nếu đang mở cửa, false nếu đóng cửa hoặc không có thông tin giờ mở cửa
     */
    @Transactional(readOnly = true)
    public boolean isRestaurantCurrentlyOpen(RestaurantProfile restaurant) {
        if (restaurant == null || restaurant.getOpeningHours() == null || 
            restaurant.getOpeningHours().trim().isEmpty()) {
            return false;
        }

        try {
            LocalTime now = LocalTime.now();
            String openingHours = restaurant.getOpeningHours().trim();
            
            // Parse format like "10:00-22:00" or "10:00 - 22:00"
            String[] hours = openingHours.replaceAll("\\s+", "").split("-");
            if (hours.length == 2) {
                LocalTime openTime = LocalTime.parse(hours[0]);
                LocalTime closeTime = LocalTime.parse(hours[1]);
                
                // Kiểm tra nếu đang trong khoảng thời gian mở cửa
                // Trường hợp bình thường: mở cửa nếu hiện tại >= openTime và <= closeTime
                boolean isOpen = (now.isAfter(openTime) || now.equals(openTime)) && 
                                (now.isBefore(closeTime) || now.equals(closeTime));
                
                // Debug logging
                System.out.println("Restaurant: " + restaurant.getRestaurantName() + 
                                 " | Hours: " + openingHours + 
                                 " | Now: " + now + 
                                 " | Open: " + openTime + 
                                 " | Close: " + closeTime + 
                                 " | IsOpen: " + isOpen);
                
                return isOpen;
            }
        } catch (DateTimeParseException e) {
            System.err.println("Error parsing opening hours: " + restaurant.getOpeningHours() + 
                             " for restaurant: " + restaurant.getRestaurantName());
        } catch (Exception e) {
            System.err.println("Error checking restaurant hours: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
}
