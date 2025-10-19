package com.example.booking.repository;

import com.example.booking.domain.AIRecommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for AIRecommendation entity
 */
@Repository
public interface AIRecommendationRepository extends JpaRepository<AIRecommendation, UUID> {
    
    /**
     * Find recommendations by user ID
     */
    List<AIRecommendation> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    /**
     * Find recommendations by session ID
     */
    List<AIRecommendation> findBySessionIdOrderByCreatedAtDesc(String sessionId);
    
    /**
     * Find recommendations by user ID with pagination
     */
    Page<AIRecommendation> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    
    /**
     * Find high confidence recommendations
     */
    @Query("SELECT ar FROM AIRecommendation ar WHERE ar.confidenceScore >= :minConfidence " +
           "ORDER BY ar.confidenceScore DESC")
    List<AIRecommendation> findHighConfidenceRecommendations(@Param("minConfidence") BigDecimal minConfidence);
    
    /**
     * Find diverse recommendations
     */
    @Query("SELECT ar FROM AIRecommendation ar WHERE ar.diversityScore >= :minDiversity " +
           "ORDER BY ar.diversityScore DESC")
    List<AIRecommendation> findDiverseRecommendations(@Param("minDiversity") BigDecimal minDiversity);
    
    /**
     * Find recommendations by query type
     */
    List<AIRecommendation> findByQueryTypeOrderByCreatedAtDesc(AIRecommendation.QueryType queryType);
    
    /**
     * Find recommendations by source
     */
    List<AIRecommendation> findBySourceOrderByCreatedAtDesc(AIRecommendation.Source source);
    
    /**
     * Find recommendations with user feedback
     */
    List<AIRecommendation> findByUserFeedbackIsNotNullOrderByCreatedAtDesc();
    
    /**
     * Find recommendations with positive feedback
     */
    @Query("SELECT ar FROM AIRecommendation ar WHERE ar.userFeedback IN ('HELPFUL', 'ACCURATE') " +
           "ORDER BY ar.createdAt DESC")
    List<AIRecommendation> findPositiveFeedbackRecommendations();
    
    /**
     * Find recommendations by date range
     */
    @Query("SELECT ar FROM AIRecommendation ar WHERE ar.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY ar.createdAt DESC")
    List<AIRecommendation> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find recommendations by model used
     */
    List<AIRecommendation> findByModelUsedOrderByCreatedAtDesc(String modelUsed);
    
    /**
     * Find recommendations with high cost
     */
    @Query("SELECT ar FROM AIRecommendation ar WHERE ar.apiCostUsd >= :minCost " +
           "ORDER BY ar.apiCostUsd DESC")
    List<AIRecommendation> findHighCostRecommendations(@Param("minCost") BigDecimal minCost);
    
    /**
     * Find recommendations with slow response time
     */
    @Query("SELECT ar FROM AIRecommendation ar WHERE ar.responseTimeMs >= :minResponseTime " +
           "ORDER BY ar.responseTimeMs DESC")
    List<AIRecommendation> findSlowRecommendations(@Param("minResponseTime") Integer minResponseTime);
    
    /**
     * Count recommendations by user
     */
    Long countByUserId(UUID userId);
    
    /**
     * Count recommendations by session
     */
    Long countBySessionId(String sessionId);
    
    /**
     * Count recommendations by query type
     */
    Long countByQueryType(AIRecommendation.QueryType queryType);
    
    /**
     * Count recommendations by source
     */
    Long countBySource(AIRecommendation.Source source);
    
    /**
     * Get average confidence score
     */
    @Query("SELECT AVG(ar.confidenceScore) FROM AIRecommendation ar")
    BigDecimal getAverageConfidenceScore();
    
    /**
     * Get average diversity score
     */
    @Query("SELECT AVG(ar.diversityScore) FROM AIRecommendation ar")
    BigDecimal getAverageDiversityScore();
    
    /**
     * Get average response time
     */
    @Query("SELECT AVG(ar.responseTimeMs) FROM AIRecommendation ar")
    Double getAverageResponseTime();
    
    /**
     * Get average cost per recommendation
     */
    @Query("SELECT AVG(ar.apiCostUsd) FROM AIRecommendation ar")
    BigDecimal getAverageCost();
    
    /**
     * Get total cost by date range
     */
    @Query("SELECT SUM(ar.apiCostUsd) FROM AIRecommendation ar WHERE ar.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalCostByDateRange(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);
    
    /**
     * Get total tokens used by date range
     */
    @Query("SELECT SUM(ar.tokensUsed) FROM AIRecommendation ar WHERE ar.createdAt BETWEEN :startDate AND :endDate")
    Long getTotalTokensByDateRange(@Param("startDate") LocalDateTime startDate, 
                                  @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find similar queries (for caching)
     */
    @Query("SELECT ar FROM AIRecommendation ar WHERE " +
           "SIMILARITY(ar.queryText, :query) > :threshold " +
           "ORDER BY SIMILARITY(ar.queryText, :query) DESC")
    List<AIRecommendation> findSimilarQueries(@Param("query") String query, 
                                              @Param("threshold") Double threshold);
    
    /**
     * Find recommendations by language
     */
    List<AIRecommendation> findByLanguageOrderByCreatedAtDesc(String language);
    
    /**
     * Find recommendations with specific intent
     */
    @Query("SELECT ar FROM AIRecommendation ar WHERE JSON_EXTRACT(ar.intentAnalysis, '$.intent') = :intent " +
           "ORDER BY ar.createdAt DESC")
    List<AIRecommendation> findByIntent(@Param("intent") String intent);
    
    /**
     * Find recommendations with specific cuisine
     */
    @Query("SELECT ar FROM AIRecommendation ar WHERE JSON_EXTRACT(ar.intentAnalysis, '$.cuisine') = :cuisine " +
           "ORDER BY ar.createdAt DESC")
    List<AIRecommendation> findByCuisine(@Param("cuisine") String cuisine);
    
    /**
     * Get most popular queries
     */
    @Query("SELECT ar.queryText, COUNT(*) as count FROM AIRecommendation ar " +
           "GROUP BY ar.queryText ORDER BY count DESC")
    List<Object[]> getPopularQueries();
    
    /**
     * Get most popular intents
     */
    @Query("SELECT JSON_EXTRACT(ar.intentAnalysis, '$.intent') as intent, COUNT(*) as count " +
           "FROM AIRecommendation ar " +
           "GROUP BY JSON_EXTRACT(ar.intentAnalysis, '$.intent') " +
           "ORDER BY count DESC")
    List<Object[]> getPopularIntents();
    
    /**
     * Find recommendations without feedback
     */
    @Query("SELECT ar FROM AIRecommendation ar WHERE ar.userFeedback IS NULL " +
           "AND ar.createdAt < :cutoffDate")
    List<AIRecommendation> findRecommendationsWithoutFeedback(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Delete old recommendations (for cleanup)
     */
    void deleteByCreatedAtBefore(LocalDateTime cutoffDate);
}
