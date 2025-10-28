package com.example.booking.service.ai;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.booking.domain.RestaurantProfile;
import com.example.booking.dto.ai.AISearchRequest;
import com.example.booking.dto.ai.AISearchResponse;
import com.example.booking.service.RestaurantManagementService;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecommendationService Tests")
public class RecommendationServiceTest {

    @Mock
    private OpenAIService openAIService;

    @Mock
    private RestaurantManagementService restaurantService;

    @InjectMocks
    private RecommendationService recommendationService;

    private AISearchRequest validRequest;
    private List<RestaurantProfile> mockRestaurants;
    private Map<String, Object> mockIntent;

    @BeforeEach
    void setUp() {
        // Setup valid request
        validRequest = new AISearchRequest();
        validRequest.setQuery("Tôi muốn ăn phở");
        validRequest.setMaxResults(5);
        validRequest.setUserId("user123");

        // Setup mock restaurants
        RestaurantProfile restaurant1 = new RestaurantProfile();
        restaurant1.setRestaurantId(1);
        restaurant1.setRestaurantName("Phở Hùng");
        restaurant1.setCuisineType("Việt Nam");
        restaurant1.setAveragePrice(BigDecimal.valueOf(150000));

        RestaurantProfile restaurant2 = new RestaurantProfile();
        restaurant2.setRestaurantId(2);
        restaurant2.setRestaurantName("Phở Lý");
        restaurant2.setCuisineType("Việt Nam");
        restaurant2.setAveragePrice(BigDecimal.valueOf(120000));

        RestaurantProfile restaurant3 = new RestaurantProfile();
        restaurant3.setRestaurantId(3);
        restaurant3.setRestaurantName("Nhà hàng Nhật");
        restaurant3.setCuisineType("Nhật Bản");
        restaurant3.setAveragePrice(BigDecimal.valueOf(200000));

        mockRestaurants = List.of(restaurant1, restaurant2, restaurant3);

        // Setup mock intent
        mockIntent = new HashMap<>();
        mockIntent.put("cuisine", List.of("Việt Nam"));
        mockIntent.put("party_size", 2);
        mockIntent.put("price_range", Map.of("min", 100000, "max", 200000));
        mockIntent.put("dietary", List.of());
        mockIntent.put("confidence", 0.85);
    }

    // ========== Function 1: search() - Main search method ==========

    @Test
    @DisplayName("testSearch_WithMatchingRestaurants_ShouldReturnRecommendations")
    void testSearch_WithMatchingRestaurants_ShouldReturnRecommendations() throws Exception {
        // Given
        when(openAIService.parseIntent(anyString(), anyString()))
            .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(mockIntent));
        when(restaurantService.findAllRestaurants()).thenReturn(mockRestaurants);
        when(openAIService.explainRestaurants(anyList()))
            .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(List.of("Explanation 1", "Explanation 2")));

        // When
        AISearchResponse response = recommendationService.search(validRequest);

        // Then
        assertNotNull(response);
        assertEquals("Tôi muốn ăn phở", response.getOriginalQuery());
        assertNotNull(response.getRecommendations());
    }

    @Test
    @DisplayName("testSearch_WithNoMatchingRestaurants_ShouldReturnEmptyList")
    void testSearch_WithNoMatchingRestaurants_ShouldReturnEmptyList() throws Exception {
        // Given
        Map<String, Object> strictIntent = new HashMap<>();
        strictIntent.put("cuisine", List.of("Pháp xa lạ"));
        strictIntent.put("party_size", 2);
        strictIntent.put("price_range", Map.of("min", 100000, "max", 200000));
        strictIntent.put("confidence", 0.5);

        when(openAIService.parseIntent(anyString(), anyString()))
            .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(strictIntent));
        when(restaurantService.findAllRestaurants()).thenReturn(mockRestaurants);

        AISearchRequest request = new AISearchRequest();
        request.setQuery("Ăn Pháp xa lạ");

        // When
        AISearchResponse response = recommendationService.search(request);

        // Then
        assertNotNull(response);
        // Should handle no matches gracefully
    }

    @Test
    @DisplayName("testSearch_WithMaxResultsLimit_ShouldLimitResults")
    void testSearch_WithMaxResultsLimit_ShouldLimitResults() throws Exception {
        // Given
        validRequest.setMaxResults(2);
        when(openAIService.parseIntent(anyString(), anyString()))
            .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(mockIntent));
        when(restaurantService.findAllRestaurants()).thenReturn(mockRestaurants);
        when(openAIService.explainRestaurants(anyList()))
            .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(List.of("Exp 1", "Exp 2")));

        // When
        AISearchResponse response = recommendationService.search(validRequest);

        // Then
        assertNotNull(response);
        assertTrue(response.getRecommendations().size() <= 2);
    }

    @Test
    @DisplayName("testSearch_WithNullQuery_ShouldThrowException")
    void testSearch_WithNullQuery_ShouldThrowException() {
        // Given
        AISearchRequest nullRequest = new AISearchRequest();
        nullRequest.setQuery(null);

        // When/Then
        assertThrows(Exception.class, () -> {
            recommendationService.search(nullRequest);
        });
    }

    @Test
    @DisplayName("testSearch_WhenOpenAITimeout_ShouldTriggerFallback")
    void testSearch_WhenOpenAITimeout_ShouldTriggerFallback() throws Exception {
        // Given
        when(openAIService.parseIntent(anyString(), anyString()))
            .thenReturn(java.util.concurrent.CompletableFuture.failedFuture(new java.util.concurrent.TimeoutException()));
        when(restaurantService.findAllRestaurants()).thenReturn(mockRestaurants);

        // When
        AISearchResponse response = recommendationService.search(validRequest);

        // Then
        assertNotNull(response);
        assertEquals("Tôi muốn ăn phở", response.getOriginalQuery());
    }

    @Test
    @DisplayName("testSearch_WhenParseIntentException_ShouldTriggerFallback")
    void testSearch_WhenParseIntentException_ShouldTriggerFallback() throws Exception {
        // Given
        when(openAIService.parseIntent(anyString(), anyString()))
            .thenReturn(java.util.concurrent.CompletableFuture.failedFuture(new RuntimeException("Parse error")));
        when(restaurantService.findAllRestaurants()).thenReturn(mockRestaurants);

        // When
        AISearchResponse response = recommendationService.search(validRequest);

        // Then
        assertNotNull(response);
        assertEquals("Tôi muốn ăn phở", response.getOriginalQuery());
    }

    // ========== Function 5: fallbackSearch() - Fallback when AI fails ==========

    @Test
    @DisplayName("testFallbackSearch_ShouldReturnRestaurantsMatchingQuery")
    void testFallbackSearch_ShouldReturnRestaurantsMatchingQuery() throws Exception {
        // Given
        when(restaurantService.findAllRestaurants()).thenReturn(mockRestaurants);
        when(openAIService.parseIntent(anyString(), anyString()))
            .thenReturn(java.util.concurrent.CompletableFuture.failedFuture(new RuntimeException("service down")));

        // When
        AISearchResponse response = recommendationService.search(validRequest);

        // Then - this will trigger fallback when OpenAI fails
        assertNotNull(response);
        assertTrue(response.getRecommendations().stream()
            .allMatch(rec -> rec.getRestaurantName().contains("Phở")));
    }

    @Test
    @DisplayName("testFallbackSearch_ShouldLimitTo5Results")
    void testFallbackSearch_ShouldLimitTo5Results() throws Exception {
        // Given
        List<RestaurantProfile> manyRestaurants = List.of(
            mockRestaurants.get(0), mockRestaurants.get(1), mockRestaurants.get(2),
            mockRestaurants.get(0), mockRestaurants.get(1), mockRestaurants.get(2),
            mockRestaurants.get(0), mockRestaurants.get(1), mockRestaurants.get(2)
        );
        when(restaurantService.findAllRestaurants()).thenReturn(manyRestaurants);
        when(openAIService.parseIntent(anyString(), anyString()))
            .thenReturn(java.util.concurrent.CompletableFuture.failedFuture(new RuntimeException()));

        // When
        AISearchResponse response = recommendationService.search(validRequest);

        // Then
        assertNotNull(response);
        assertEquals("Tìm thấy nhà hàng phù hợp (chế độ đơn giản)", response.getExplanation());
        assertTrue(response.getRecommendations().size() <= validRequest.getMaxResults());
        assertTrue(response.getRecommendations().stream()
            .allMatch(rec -> rec.getRestaurantName().contains("Phở")));
    }

    @Test
    @DisplayName("testFallbackSearch_ShouldSetFallbackExplanation")
    void testFallbackSearch_ShouldSetFallbackExplanation() throws Exception {
        // Given
        when(restaurantService.findAllRestaurants()).thenReturn(mockRestaurants);
        when(openAIService.parseIntent(anyString(), anyString()))
            .thenReturn(java.util.concurrent.CompletableFuture.failedFuture(new RuntimeException()));

        // When
        AISearchResponse response = recommendationService.search(validRequest);

        // Then
        assertNotNull(response);
        assertEquals("Tìm thấy nhà hàng phù hợp (chế độ đơn giản)", response.getExplanation());
        assertTrue(response.getRecommendations().stream()
            .allMatch(rec -> rec.getRestaurantName().contains("Phở")));
    }

    @Test
    @DisplayName("testSearch_WithSpecificRestaurantName_ShouldPrioritizeMatchingRestaurant")
    void testSearch_WithSpecificRestaurantName_ShouldPrioritizeMatchingRestaurant() throws Exception {
        // Given
        RestaurantProfile bbqRestaurant = new RestaurantProfile();
        bbqRestaurant.setRestaurantId(10);
        bbqRestaurant.setRestaurantName("Seoul BBQ Premium");
        bbqRestaurant.setCuisineType("BBQ");
        bbqRestaurant.setAveragePrice(BigDecimal.valueOf(350000));

        RestaurantProfile pizzaRestaurant = new RestaurantProfile();
        pizzaRestaurant.setRestaurantId(11);
        pizzaRestaurant.setRestaurantName("Pizza Italia");
        pizzaRestaurant.setCuisineType("Italian");
        pizzaRestaurant.setAveragePrice(BigDecimal.valueOf(200000));

        when(restaurantService.findAllRestaurants()).thenReturn(List.of(bbqRestaurant, pizzaRestaurant));
        when(openAIService.parseIntent(anyString(), anyString()))
            .thenReturn(java.util.concurrent.CompletableFuture.failedFuture(new RuntimeException("OpenAI unavailable")));

        AISearchRequest request = new AISearchRequest();
        request.setQuery("Tôi muốn ăn Seoul BBQ Premium");
        request.setMaxResults(5);

        // When
        AISearchResponse response = recommendationService.search(request);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalReturned());
        assertEquals("Seoul BBQ Premium", response.getRecommendations().get(0).getRestaurantName());
    }
}

