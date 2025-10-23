package com.example.booking.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.domain.Customer;
import com.example.booking.domain.CustomerFavorite;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.dto.admin.FavoriteStatisticsDto;
import com.example.booking.dto.customer.FavoriteRestaurantDto;
import com.example.booking.dto.customer.ToggleFavoriteRequest;
import com.example.booking.dto.customer.ToggleFavoriteResponse;
import com.example.booking.repository.CustomerFavoriteRepository;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.RestaurantMediaRepository;
import com.example.booking.domain.RestaurantMedia;
import com.example.booking.service.FavoriteService;
import com.example.booking.service.ReviewService;

@Service
@Transactional
public class FavoriteServiceImpl implements FavoriteService {
    
    @Autowired
    private CustomerFavoriteRepository favoriteRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private RestaurantProfileRepository restaurantRepository;
    
    @Autowired
    private RestaurantMediaRepository restaurantMediaRepository;

    @Autowired
    private ReviewService reviewService;

    @Override
    public ToggleFavoriteResponse toggleFavorite(UUID customerId, ToggleFavoriteRequest request) {
        try {
            Integer restaurantId = request.getRestaurantId();
            
            System.out.println("=== DEBUG TOGGLE FAVORITE ===");
            System.out.println("Customer ID: " + customerId);
            System.out.println("Restaurant ID: " + restaurantId);
            
            // Validate customer exists
            Optional<Customer> customerOpt = customerRepository.findById(customerId);
            if (customerOpt.isEmpty()) {
                System.out.println("ERROR: Customer not found");
                return ToggleFavoriteResponse.error("Khách hàng không tồn tại");
            }
            
            // Validate restaurant exists
            Optional<RestaurantProfile> restaurantOpt = restaurantRepository.findById(restaurantId);
            if (restaurantOpt.isEmpty()) {
                System.out.println("ERROR: Restaurant not found");
                return ToggleFavoriteResponse.error("Nhà hàng không tồn tại");
            }
            
            Customer customer = customerOpt.get();
            RestaurantProfile restaurant = restaurantOpt.get();
            
            // Check current status
            boolean currentlyFavorited = favoriteRepository.existsByCustomerCustomerIdAndRestaurantRestaurantId(customerId, restaurantId);
            boolean finalStatus;
            
            System.out.println("Currently favorited: " + currentlyFavorited);
            
            if (currentlyFavorited) {
                // Currently favorited -> Remove from favorites
                System.out.println("ACTION: Removing from favorites");
                favoriteRepository.deleteByCustomerCustomerIdAndRestaurantRestaurantId(customerId, restaurantId);
                finalStatus = false;
            } else {
                // Not currently favorited -> Add to favorites
                System.out.println("ACTION: Adding to favorites");
                CustomerFavorite favorite = new CustomerFavorite(customer, restaurant);
                favoriteRepository.save(favorite);
                finalStatus = true;
            }
            
            // Get updated favorite count
            long favoriteCount = favoriteRepository.countByCustomerCustomerId(customerId);
            
            System.out.println("Final status: " + finalStatus);
            System.out.println("Favorite count: " + favoriteCount);
            System.out.println("=============================");
            
            return ToggleFavoriteResponse.success(finalStatus, (int) favoriteCount, restaurantId);
            
        } catch (Exception e) {
            return ToggleFavoriteResponse.error("Có lỗi xảy ra: " + e.getMessage());
        }
    }
    
    @Override
    public CustomerFavorite addToFavorites(UUID customerId, Integer restaurantId) {
        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        Optional<RestaurantProfile> restaurantOpt = restaurantRepository.findById(restaurantId);
        
        if (customerOpt.isEmpty() || restaurantOpt.isEmpty()) {
            throw new IllegalArgumentException("Customer or Restaurant not found");
        }
        
        // Check if already favorited
        Optional<CustomerFavorite> existingFavorite = favoriteRepository
            .findByCustomerCustomerIdAndRestaurantRestaurantId(customerId, restaurantId);
        
        if (existingFavorite.isPresent()) {
            return existingFavorite.get();
        }
        
        CustomerFavorite favorite = new CustomerFavorite(customerOpt.get(), restaurantOpt.get());
        return favoriteRepository.save(favorite);
    }
    
    @Override
    public void removeFromFavorites(UUID customerId, Integer restaurantId) {
        favoriteRepository.deleteByCustomerCustomerIdAndRestaurantRestaurantId(customerId, restaurantId);
    }
    
    @Override
    public boolean isFavorited(UUID customerId, Integer restaurantId) {
        return favoriteRepository.existsByCustomerCustomerIdAndRestaurantRestaurantId(customerId, restaurantId);
    }
    
    @Override
    public Page<FavoriteRestaurantDto> getFavoriteRestaurants(UUID customerId, Pageable pageable) {
        Page<CustomerFavorite> favorites = favoriteRepository.findByCustomerCustomerIdOrderByCreatedAtDesc(customerId, pageable);
        
        List<FavoriteRestaurantDto> favoriteDtos = new ArrayList<>();
        for (CustomerFavorite favorite : favorites.getContent()) {
            RestaurantProfile restaurant = favorite.getRestaurant();
            FavoriteRestaurantDto dto = convertToFavoriteRestaurantDto(restaurant, favorite.getCreatedAt(), true);
            favoriteDtos.add(dto);
        }
        
        return new PageImpl<>(favoriteDtos, pageable, favorites.getTotalElements());
    }
    
    @Override
    public Page<FavoriteRestaurantDto> getFavoriteRestaurantsWithFilters(UUID customerId, Pageable pageable, 
            String search, String cuisineType, String priceRange, String ratingFilter) {
        
        System.out.println("=== DEBUG FILTERED FAVORITES ===");
        System.out.println("Customer ID: " + customerId);
        System.out.println("Search: " + search);
        System.out.println("Cuisine Type: " + cuisineType);
        System.out.println("Price Range: " + priceRange);
        System.out.println("Rating Filter: " + ratingFilter);
        System.out.println("Sort By: " + pageable.getSort());
        
        // Get all favorites first (without pagination to apply filters first)
        Pageable allPageable = PageRequest.of(0, Integer.MAX_VALUE, pageable.getSort());
        Page<CustomerFavorite> allFavorites = favoriteRepository.findByCustomerCustomerId(customerId, allPageable);
        
        List<FavoriteRestaurantDto> favoriteDtos = new ArrayList<>();
        for (CustomerFavorite favorite : allFavorites.getContent()) {
            RestaurantProfile restaurant = favorite.getRestaurant();
            FavoriteRestaurantDto dto = convertToFavoriteRestaurantDto(restaurant, favorite.getCreatedAt(), true);
            
            // Apply filters
            if (matchesFilters(dto, search, cuisineType, priceRange, ratingFilter)) {
                favoriteDtos.add(dto);
                System.out.println("✅ Matched: " + dto.getRestaurantName());
            } else {
                System.out.println("❌ Filtered out: " + dto.getRestaurantName());
            }
        }
        
        // Apply sorting to filtered results
        favoriteDtos = applySorting(favoriteDtos, pageable.getSort());
        
        // Apply pagination to sorted results
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), favoriteDtos.size());
        List<FavoriteRestaurantDto> paginatedDtos = favoriteDtos.subList(start, end);
        
        System.out.println("Filtered results: " + favoriteDtos.size() + " out of " + allFavorites.getContent().size());
        System.out.println("Paginated results: " + paginatedDtos.size());
        System.out.println("===============================");
        
        return new PageImpl<>(paginatedDtos, pageable, favoriteDtos.size());
    }
    
    /**
     * Apply sorting to the filtered results
     */
    private List<FavoriteRestaurantDto> applySorting(List<FavoriteRestaurantDto> dtos, Sort sort) {
        if (sort.isUnsorted()) {
            return dtos;
        }
        
        return dtos.stream()
            .sorted((dto1, dto2) -> {
                for (Sort.Order order : sort) {
                    int comparison = compareByField(dto1, dto2, order.getProperty());
                    if (comparison != 0) {
                        return order.getDirection() == Sort.Direction.ASC ? comparison : -comparison;
                    }
                }
                return 0;
            })
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Compare two DTOs by field
     */
    private int compareByField(FavoriteRestaurantDto dto1, FavoriteRestaurantDto dto2, String field) {
        switch (field) {
            case "restaurantName":
                return String.CASE_INSENSITIVE_ORDER.compare(
                    dto1.getRestaurantName() != null ? dto1.getRestaurantName() : "",
                    dto2.getRestaurantName() != null ? dto2.getRestaurantName() : ""
                );
            case "averagePrice":
                return Double.compare(
                    dto1.getAveragePrice() != null ? dto1.getAveragePrice().doubleValue() : 0.0,
                    dto2.getAveragePrice() != null ? dto2.getAveragePrice().doubleValue() : 0.0
                );
            case "averageRating":
                return Double.compare(
                    dto1.getAverageRating() != null ? dto1.getAverageRating() : 0.0,
                    dto2.getAverageRating() != null ? dto2.getAverageRating() : 0.0
                );
            case "createdAt":
                return dto1.getFavoritedAt().compareTo(dto2.getFavoritedAt());
            default:
                return 0;
        }
    }
    
    /**
     * Check if restaurant matches the applied filters
     */
    private boolean matchesFilters(FavoriteRestaurantDto dto, String search, String cuisineType, 
                                 String priceRange, String ratingFilter) {
        
        // Search filter
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            boolean matchesSearch = (dto.getRestaurantName() != null && dto.getRestaurantName().toLowerCase().contains(searchLower)) ||
                                  (dto.getAddress() != null && dto.getAddress().toLowerCase().contains(searchLower)) ||
                                  (dto.getCuisineType() != null && dto.getCuisineType().toLowerCase().contains(searchLower));
            if (!matchesSearch) {
                return false;
            }
        }
        
        // Cuisine type filter
        if (cuisineType != null && !cuisineType.trim().isEmpty()) {
            if (dto.getCuisineType() == null || !dto.getCuisineType().equals(cuisineType)) {
                return false;
            }
        }
        
        // Price range filter
        if (priceRange != null && !priceRange.trim().isEmpty() && dto.getAveragePrice() != null) {
            double price = dto.getAveragePrice().doubleValue();
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
        if (ratingFilter != null && !ratingFilter.trim().isEmpty() && dto.getAverageRating() != null) {
            double rating = dto.getAverageRating();
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
    
    @Override
    public List<FavoriteRestaurantDto> getAllFavoriteRestaurants(UUID customerId) {
        List<CustomerFavorite> favorites = favoriteRepository.findByCustomerWithRestaurantDetails(customerId);
        
        List<FavoriteRestaurantDto> favoriteDtos = new ArrayList<>();
        for (CustomerFavorite favorite : favorites) {
            RestaurantProfile restaurant = favorite.getRestaurant();
            FavoriteRestaurantDto dto = convertToFavoriteRestaurantDto(restaurant, favorite.getCreatedAt(), true);
            favoriteDtos.add(dto);
        }
        
        return favoriteDtos;
    }
    
    @Override
    public long getFavoriteCount(UUID customerId) {
        return favoriteRepository.countByCustomerCustomerId(customerId);
    }
    
    @Override
    public long getRestaurantFavoriteCount(Integer restaurantId) {
        return favoriteRepository.countByRestaurantRestaurantId(restaurantId);
    }
    
    @Override
    public List<Object[]> getTopFavoritedRestaurants(Pageable pageable) {
        return favoriteRepository.findTopFavoritedRestaurants(pageable);
    }
    
    @Override
    public List<FavoriteStatisticsDto> getFavoriteStatistics(Pageable pageable) {
        try {
            System.out.println("=== DEBUG FAVORITE STATISTICS ===");
            List<Object[]> statistics = favoriteRepository.getFavoriteStatistics(pageable);
            System.out.println("Raw statistics count: " + statistics.size());
            
            List<FavoriteStatisticsDto> statsDtos = new ArrayList<>();
            for (Object[] stat : statistics) {
                try {
                    System.out.println("Processing stat: " + java.util.Arrays.toString(stat));
                    
                    FavoriteStatisticsDto dto = new FavoriteStatisticsDto(
                        (Integer) stat[0],           // restaurantId
                        (String) stat[1],            // restaurantName
                        ((Number) stat[2]).longValue(), // favoriteCount - convert to Long
                        (Double) stat[3],            // averageRating
                        ((Number) stat[4]).longValue(), // reviewCount - convert to Long
                        null,                        // averagePrice (not in query)
                        null                         // cuisineType (not in query)
                    );
                    statsDtos.add(dto);
                    System.out.println("Successfully created DTO for: " + dto.getRestaurantName());
                } catch (Exception e) {
                    System.out.println("ERROR processing individual stat: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("Processed DTOs count: " + statsDtos.size());
            System.out.println("================================");
            
            return statsDtos;
            
        } catch (Exception e) {
            System.out.println("ERROR in getFavoriteStatistics: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<FavoriteStatisticsDto> getFavoriteStatisticsForOwner(UUID ownerId, Pageable pageable) {
        try {
            System.out.println("=== DEBUG FAVORITE STATISTICS FOR OWNER ===");
            System.out.println("Owner ID: " + ownerId);
            
            List<Object[]> statistics = favoriteRepository.getFavoriteStatisticsForOwner(ownerId, pageable);
            System.out.println("Raw statistics count for owner: " + statistics.size());
            
            List<FavoriteStatisticsDto> statsDtos = new ArrayList<>();
            for (Object[] stat : statistics) {
                try {
                    System.out.println("Processing owner stat: " + java.util.Arrays.toString(stat));
                    
                    FavoriteStatisticsDto dto = new FavoriteStatisticsDto(
                        (Integer) stat[0],           // restaurantId
                        (String) stat[1],            // restaurantName
                        ((Number) stat[2]).longValue(), // favoriteCount - convert to Long
                        (Double) stat[3],            // averageRating
                        ((Number) stat[4]).longValue(), // reviewCount - convert to Long
                        null,                        // averagePrice (not in query)
                        null                         // cuisineType (not in query)
                    );
                    statsDtos.add(dto);
                    System.out.println("Successfully created DTO for owner restaurant: " + dto.getRestaurantName());
                } catch (Exception e) {
                    System.out.println("ERROR processing individual owner stat: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("Processed DTOs count for owner: " + statsDtos.size());
            System.out.println("===========================================");
            
            return statsDtos;
            
        } catch (Exception e) {
            System.out.println("ERROR in getFavoriteStatisticsForOwner: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Integer> getFavoritedRestaurantIds(UUID customerId) {
        return favoriteRepository.findRestaurantIdsByCustomerId(customerId);
    }
    
    @Override
    public Optional<CustomerFavorite> getFavorite(UUID customerId, Integer restaurantId) {
        return favoriteRepository.findByCustomerCustomerIdAndRestaurantRestaurantId(customerId, restaurantId);
    }
    
    /**
     * Convert RestaurantProfile to FavoriteRestaurantDto
     */
    private FavoriteRestaurantDto convertToFavoriteRestaurantDto(RestaurantProfile restaurant, 
                                                               java.time.LocalDateTime favoritedAt, 
                                                               boolean isFavorited) {
        FavoriteRestaurantDto dto = new FavoriteRestaurantDto();
        dto.setRestaurantId(restaurant.getRestaurantId());
        dto.setRestaurantName(restaurant.getRestaurantName());
        dto.setAddress(restaurant.getAddress());
        dto.setPhone(restaurant.getPhone());
        dto.setDescription(restaurant.getDescription());
        dto.setCuisineType(restaurant.getCuisineType());
        dto.setOpeningHours(restaurant.getOpeningHours());
        dto.setAveragePrice(restaurant.getAveragePrice());
        dto.setWebsiteUrl(restaurant.getWebsiteUrl());
        dto.setFavoritedAt(favoritedAt);
        dto.setFavorited(isFavorited);
        
        // Set real values for rating and review count from Review entity
        try {
            double averageRating = reviewService.getAverageRatingByRestaurant(restaurant.getRestaurantId());
            long reviewCount = reviewService.getReviewCountByRestaurant(restaurant.getRestaurantId());
            dto.setAverageRating(averageRating);
            dto.setReviewCount((int) reviewCount);
        } catch (Exception e) {
            // If no reviews exist, set default values
            dto.setAverageRating(0.0);
            dto.setReviewCount(0);
        }
        
        // Get main image URL from RestaurantMedia
        dto.setImageUrl(getRestaurantMainImageUrl(restaurant));
        
        return dto;
    }

    /**
     * Get restaurant main image URL from RestaurantMedia
     */
    private String getRestaurantMainImageUrl(RestaurantProfile restaurant) {
        try {
            // First try to get cover image
            List<RestaurantMedia> coverImages = restaurantMediaRepository
                    .findByRestaurantAndType(restaurant, "cover");

            if (!coverImages.isEmpty()) {
                return coverImages.get(0).getUrl();
            }

            // If no cover, try to get logo
            List<RestaurantMedia> logoImages = restaurantMediaRepository
                    .findByRestaurantAndType(restaurant, "logo");

            if (!logoImages.isEmpty()) {
                return logoImages.get(0).getUrl();
            }

            // If no logo, try to get any gallery image
            List<RestaurantMedia> galleryImages = restaurantMediaRepository
                    .findByRestaurantAndType(restaurant, "gallery");

            if (!galleryImages.isEmpty()) {
                return galleryImages.get(0).getUrl();
            }

            // If no images found, return null
            return null;

        } catch (Exception e) {
            System.err.println("Error getting restaurant image URL: " + e.getMessage());
            return null;
        }
    }
}
