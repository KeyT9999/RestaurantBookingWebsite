package com.example.booking.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.booking.common.enums.RestaurantApprovalStatus;
import com.example.booking.domain.RestaurantProfile;

@Repository
public interface RestaurantProfileRepository extends JpaRepository<RestaurantProfile, Integer> {
    
    List<RestaurantProfile> findByRestaurantNameContainingIgnoreCase(String name);
    
    List<RestaurantProfile> findByRestaurantNameContainingIgnoreCaseAndApprovalStatus(String name, RestaurantApprovalStatus approvalStatus);
    
    List<RestaurantProfile> findByOwnerOwnerId(UUID ownerId);
    
    /**
     * Find restaurant profile by owner's user ID
     */
    @Query("SELECT r FROM RestaurantProfile r WHERE r.owner.user.id = :userId")
    List<RestaurantProfile> findByOwnerUserId(@Param("userId") UUID userId);
    
    /**
     * Find restaurant profile by owner's username
     */
    @Query("SELECT r FROM RestaurantProfile r WHERE r.owner.user.username = :username")
    List<RestaurantProfile> findByOwnerUsername(@Param("username") String username);

    /**
     * Check if restaurant exists and belongs to specific owner
     */
    @Query("SELECT COUNT(r) > 0 FROM RestaurantProfile r WHERE r.restaurantId = :restaurantId AND r.owner.user.id = :ownerId")
    boolean existsByIdAndOwnerUser_Id(@Param("restaurantId") Integer restaurantId, @Param("ownerId") UUID ownerId);

    // === APPROVAL WORKFLOW METHODS ===
    
    /**
     * Find restaurants by approval status
     */
    List<RestaurantProfile> findByApprovalStatus(RestaurantApprovalStatus approvalStatus);
    
    /**
     * Find approved restaurants excluding AI restaurant (ID = 37)
     */
    @Query("SELECT r FROM RestaurantProfile r WHERE r.approvalStatus = 'APPROVED' AND r.restaurantId != 37")
    List<RestaurantProfile> findApprovedExcludingAI();

    /**
     * Find restaurants by approval status with pagination
     */
    Page<RestaurantProfile> findByApprovalStatus(RestaurantApprovalStatus approvalStatus, Pageable pageable);
    
    /**
     * Count restaurants by approval status
     */
    long countByApprovalStatus(RestaurantApprovalStatus approvalStatus);
    
    /**
     * Find restaurants by approved by admin
     */
    List<RestaurantProfile> findByApprovedBy(String approvedBy);
    
    /**
     * Find pending restaurants (for approval workflow)
     */
    @Query("SELECT r FROM RestaurantProfile r WHERE r.approvalStatus = 'PENDING' ORDER BY r.createdAt ASC")
    List<RestaurantProfile> findPendingRestaurants();
    
    /**
     * Find approved restaurants (active restaurants)
     */
    @Query("SELECT r FROM RestaurantProfile r WHERE r.approvalStatus = 'APPROVED' ORDER BY r.approvedAt DESC")
    List<RestaurantProfile> findApprovedRestaurants();
    
    /**
     * Find rejected restaurants
     */
    @Query("SELECT r FROM RestaurantProfile r WHERE r.approvalStatus = 'REJECTED' ORDER BY r.approvedAt DESC")
    List<RestaurantProfile> findRejectedRestaurants();
    
    /**
     * Find suspended restaurants
     */
    @Query("SELECT r FROM RestaurantProfile r WHERE r.approvalStatus = 'SUSPENDED' ORDER BY r.approvedAt DESC")
    List<RestaurantProfile> findSuspendedRestaurants();

    /**
     * ===== PERFORMANCE OPTIMIZATION: Push filters to database =====
     * Find APPROVED restaurants with filters applied at database level
     * BEFORE: Load all restaurants (Integer.MAX_VALUE) then filter in Java
     * AFTER: Filter in database, return only matching restaurants with pagination
     * 
     * @param search - Search term for name/address/cuisine (nullable)
     * @param cuisineType - Exact cuisine type match (nullable)
     * @param minPrice - Minimum average price (nullable)
     * @param maxPrice - Maximum average price (nullable)
     * @param minRating - Minimum average rating (nullable, but filtered in Java not DB)
     * @param pageable - Pagination and sorting
     * @return Page of matching restaurants
     */
    @Query("SELECT r FROM RestaurantProfile r " +
           "WHERE r.approvalStatus = 'APPROVED' " +
                  "AND r.restaurantId != 37 " +
           "AND (:search IS NULL OR :search = '' OR " +
           "     LOWER(r.restaurantName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(r.address) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(r.cuisineType) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:cuisineType IS NULL OR :cuisineType = '' OR LOWER(TRIM(r.cuisineType)) = LOWER(TRIM(:cuisineType))) " +
           "AND (:minPrice IS NULL OR r.averagePrice >= :minPrice) " +
           "AND (:maxPrice IS NULL OR r.averagePrice <= :maxPrice) " +
           "AND (1=1 OR :minRating IS NULL)")
    Page<RestaurantProfile> findApprovedWithFilters(
            @Param("search") String search,
            @Param("cuisineType") String cuisineType,
            @Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            @Param("minRating") Double minRating,
            Pageable pageable);
    
    /**
     * Find distinct cuisine types from approved restaurants
     * Returns list of unique, non-null, non-empty cuisine types
     */
    @Query("SELECT DISTINCT r.cuisineType FROM RestaurantProfile r " +
           "WHERE r.approvalStatus = 'APPROVED' " +
           "AND r.cuisineType IS NOT NULL " +
           "AND TRIM(r.cuisineType) != '' " +
           "ORDER BY r.cuisineType ASC")
    List<String> findDistinctCuisineTypes();
    
    /**
     * Find top-rated approved restaurants sorted by rating, review count, and approval time
     */
    @Query("SELECT r FROM RestaurantProfile r " +
           "LEFT JOIN r.reviews rv " +
           "WHERE r.approvalStatus = 'APPROVED' AND r.restaurantId <> 37 " +
           "GROUP BY r " +
           "ORDER BY COALESCE(AVG(rv.rating), 0) DESC, COUNT(rv) DESC, r.approvedAt DESC")
    List<RestaurantProfile> findTopRatedRestaurants(Pageable pageable);

}
