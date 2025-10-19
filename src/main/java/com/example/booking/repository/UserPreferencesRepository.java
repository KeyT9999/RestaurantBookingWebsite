package com.example.booking.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.booking.domain.User;
import com.example.booking.domain.UserPreferences;

/**
 * Repository for UserPreferences entity
 * All JSONB queries use native SQL to avoid Spring Data auto-generation issues
 */
@Repository
public interface UserPreferencesRepository extends JpaRepository<UserPreferences, UUID> {
    
    /**
     * Find user preferences by user ID
     */
    Optional<UserPreferences> findByUserId(UUID userId);
    
    /**
     * Find user preferences by user
     */
    Optional<UserPreferences> findByUser(User user);
    
    /**
     * Check if user has preferences
     */
    boolean existsByUserId(UUID userId);
    
    /**
     * Find users with specific cuisine preferences (exact match in JSONB array)
     */
    @Query(value = """
        SELECT up.*
        FROM user_preferences up
        WHERE EXISTS (
            SELECT 1
            FROM jsonb_array_elements_text(up.cuisine_preferences) AS elem
            WHERE elem = :cuisine
        )
        """, nativeQuery = true)
    java.util.List<UserPreferences> findByCuisinePreferencesContaining(@Param("cuisine") String cuisine);
    
    /**
     * Find users with price range within specified range
     */
    @Query(value = """
        SELECT up.*
        FROM user_preferences up
        WHERE (up.price_range->>'min')::integer <= :maxPrice 
        AND (up.price_range->>'max')::integer >= :minPrice
        """, nativeQuery = true)
    java.util.List<UserPreferences> findByPriceRangeOverlapping(
        @Param("minPrice") Integer minPrice, 
        @Param("maxPrice") Integer maxPrice);
    
    /**
     * Find users with high interaction count (active users)
     */
    @Query("SELECT up FROM UserPreferences up WHERE up.totalInteractions >= :minInteractions " +
           "ORDER BY up.totalInteractions DESC")
    java.util.List<UserPreferences> findActiveUsers(@Param("minInteractions") Integer minInteractions);
    
    /**
     * Find users with high success rate
     */
    @Query("SELECT up FROM UserPreferences up WHERE up.successfulBookings > 0 " +
           "AND (CAST(up.successfulBookings AS FLOAT) / up.totalInteractions) >= :minSuccessRate " +
           "ORDER BY (CAST(up.successfulBookings AS FLOAT) / up.totalInteractions) DESC")
    java.util.List<UserPreferences> findHighSuccessRateUsers(@Param("minSuccessRate") Double minSuccessRate);
    
    /**
     * Find users who haven't updated preferences recently
     */
    @Query("SELECT up FROM UserPreferences up WHERE up.lastUpdatedPreferences < :cutoffDate")
    java.util.List<UserPreferences> findStalePreferences(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);
    
    /**
     * Count users by cuisine preference (exact match in JSONB array)
     */
    @Query(value = """
        SELECT COUNT(*)
        FROM user_preferences up
        WHERE EXISTS (
            SELECT 1
            FROM jsonb_array_elements_text(up.cuisine_preferences) AS elem
            WHERE elem = :cuisine
        )
        """, nativeQuery = true)
    Long countByCuisinePreference(@Param("cuisine") String cuisine);
    
    /**
     * Find users with specific dietary restrictions (exact match in JSONB array)
     */
    @Query(value = """
        SELECT up.*
        FROM user_preferences up
        WHERE EXISTS (
            SELECT 1
            FROM jsonb_array_elements_text(up.dietary_restrictions) AS elem
            WHERE elem = :restriction
        )
        """, nativeQuery = true)
    java.util.List<UserPreferences> findByDietaryRestriction(@Param("restriction") String restriction);
    
    /**
     * Find users with location preferences in specific districts (exact match in JSONB array)
     */
    @Query(value = """
        SELECT up.*
        FROM user_preferences up
        WHERE EXISTS (
            SELECT 1
            FROM jsonb_array_elements_text(up.location_preferences->'districts') AS elem
            WHERE elem = :district
        )
        """, nativeQuery = true)
    java.util.List<UserPreferences> findByDistrictPreference(@Param("district") String district);
    
    /**
     * Find users with preferred ambiance (exact match in JSONB array)
     */
    @Query(value = """
        SELECT up.*
        FROM user_preferences up
        WHERE EXISTS (
            SELECT 1
            FROM jsonb_array_elements_text(up.preferred_ambiance) AS elem
            WHERE elem = :ambiance
        )
        """, nativeQuery = true)
    java.util.List<UserPreferences> findByPreferredAmbiance(@Param("ambiance") String ambiance);
    
    /**
     * Get average success rate across all users
     */
    @Query("SELECT AVG(CAST(up.successfulBookings AS FLOAT) / NULLIF(up.totalInteractions, 0)) " +
           "FROM UserPreferences up WHERE up.totalInteractions > 0")
    Double getAverageSuccessRate();
    
    /**
     * Get most popular cuisine preferences
     */
    @Query(value = """
        SELECT elem as cuisine, COUNT(*) as count
        FROM user_preferences up,
        LATERAL jsonb_array_elements_text(up.cuisine_preferences) AS elem
        GROUP BY elem
        ORDER BY count DESC
        """, nativeQuery = true)
    java.util.List<Object[]> getPopularCuisinePreferences();
    
    /**
     * Get average price range preferences
     */
    @Query(value = """
        SELECT AVG((price_range->>'min')::integer) as avgMin,
               AVG((price_range->>'max')::integer) as avgMax
        FROM user_preferences
        """, nativeQuery = true)
    Object[] getAveragePriceRange();
    
    // Additional helper methods for fuzzy search (if needed)
    
    /**
     * Search users by dietary restrictions with partial match
     */
    @Query(value = """
        SELECT up.*
        FROM user_preferences up
        WHERE EXISTS (
            SELECT 1
            FROM jsonb_array_elements_text(up.dietary_restrictions) AS elem
            WHERE elem ILIKE CONCAT('%', :keyword, '%')
        )
        """, nativeQuery = true)
    java.util.List<UserPreferences> searchByDietaryContains(@Param("keyword") String keyword);
    
    /**
     * Search users by cuisine preferences with partial match
     */
    @Query(value = """
        SELECT up.*
        FROM user_preferences up
        WHERE EXISTS (
            SELECT 1
            FROM jsonb_array_elements_text(up.cuisine_preferences) AS elem
            WHERE elem ILIKE CONCAT('%', :keyword, '%')
        )
        """, nativeQuery = true)
    java.util.List<UserPreferences> searchByCuisineContains(@Param("keyword") String keyword);
}