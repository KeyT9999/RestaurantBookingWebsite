package com.example.booking.service.ai;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;
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
        when(openAIService.parseIntent(anyString(), any()))
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

        when(openAIService.parseIntent(anyString(), any()))
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
        when(openAIService.parseIntent(anyString(), any()))
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
        when(openAIService.parseIntent(anyString(), any()))
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
        when(openAIService.parseIntent(anyString(), any()))
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
        when(openAIService.parseIntent(anyString(), any()))
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
        when(openAIService.parseIntent(anyString(), any()))
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
        when(openAIService.parseIntent(anyString(), any()))
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
        when(openAIService.parseIntent(anyString(), any()))
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

    @Test
    @DisplayName("testSearch_WithUserLocation_ShouldSortByDistanceAndPopulateDistanceKm")
    void testSearch_WithUserLocation_ShouldSortByDistanceAndPopulateDistanceKm() throws Exception {
        // Given
        AISearchRequest request = new AISearchRequest();
        request.setQuery("Dining near me");
        request.setUserLocation("10.776889, 106.700806");
        request.setMaxResults(5);
        request.setUserId("user123");

        Map<String, Object> openIntent = new HashMap<>();
        openIntent.put("cuisine", List.of());
        openIntent.put("price_range", Map.of("min", 0, "max", 1_000_000));
        openIntent.put("dietary", List.of());
        openIntent.put("confidence", 0.9);

        RestaurantProfile saigonRestaurant = new RestaurantProfile();
        saigonRestaurant.setRestaurantId(20);
        saigonRestaurant.setRestaurantName("Saigon Dining");
        saigonRestaurant.setCuisineType("Fusion");
        saigonRestaurant.setAveragePrice(BigDecimal.valueOf(250000));
        saigonRestaurant.setAddress("45 Nguyen Hue, Quận 1, Hồ Chí Minh");

        RestaurantProfile hanoiRestaurant = new RestaurantProfile();
        hanoiRestaurant.setRestaurantId(30);
        hanoiRestaurant.setRestaurantName("Hanoi Dining");
        hanoiRestaurant.setCuisineType("Vietnamese");
        hanoiRestaurant.setAveragePrice(BigDecimal.valueOf(200000));
        hanoiRestaurant.setAddress("12 Phố Cổ, Hoàn Kiếm, Hà Nội");

        when(openAIService.parseIntent(anyString(), any()))
            .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(openIntent));
        when(restaurantService.findAllRestaurants()).thenReturn(List.of(hanoiRestaurant, saigonRestaurant));
        when(openAIService.explainRestaurants(anyList()))
            .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(List.of("Reason 1", "Reason 2")));

        // When
        AISearchResponse response = recommendationService.search(request);

        // Then
        assertNotNull(response);
        assertEquals(2, response.getTotalReturned());
        AISearchResponse.RestaurantRecommendation first = response.getRecommendations().get(0);
        AISearchResponse.RestaurantRecommendation second = response.getRecommendations().get(1);

        assertEquals("Saigon Dining", first.getRestaurantName());
        assertNotNull(first.getDistanceKm());
        assertTrue(first.getDistanceKm() < 1.0);

        assertEquals("Hanoi Dining", second.getRestaurantName());
        assertNotNull(second.getDistanceKm());
        assertTrue(second.getDistanceKm() > 100.0);
    }

    @Test
    @DisplayName("testSearch_WithMaxDistance_ShouldFilterOutFarRestaurants")
    void testSearch_WithMaxDistance_ShouldFilterOutFarRestaurants() throws Exception {
        // Given
        AISearchRequest request = new AISearchRequest();
        request.setQuery("Dining near me");
        request.setUserLocation("10.776889, 106.700806");
        request.setMaxDistance(10); // km
        request.setMaxResults(5);
        request.setUserId("user123");

        Map<String, Object> openIntent = new HashMap<>();
        openIntent.put("cuisine", List.of());
        openIntent.put("price_range", Map.of("min", 0, "max", 1_000_000));
        openIntent.put("dietary", List.of());
        openIntent.put("confidence", 0.9);

        RestaurantProfile saigonRestaurant = new RestaurantProfile();
        saigonRestaurant.setRestaurantId(101);
        saigonRestaurant.setRestaurantName("Saigon Dining");
        saigonRestaurant.setCuisineType("Vietnamese");
        saigonRestaurant.setAveragePrice(BigDecimal.valueOf(220000));
        saigonRestaurant.setAddress("22 Lê Lợi, Quận 1, Hồ Chí Minh");

        RestaurantProfile hanoiRestaurant = new RestaurantProfile();
        hanoiRestaurant.setRestaurantId(202);
        hanoiRestaurant.setRestaurantName("Hanoi Dining");
        hanoiRestaurant.setCuisineType("Vietnamese");
        hanoiRestaurant.setAveragePrice(BigDecimal.valueOf(180000));
        hanoiRestaurant.setAddress("1 Hàng Đào, Hoàn Kiếm, Hà Nội");

        when(openAIService.parseIntent(anyString(), any()))
            .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(openIntent));
        when(restaurantService.findAllRestaurants()).thenReturn(List.of(saigonRestaurant, hanoiRestaurant));
        when(openAIService.explainRestaurants(anyList()))
            .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(List.of("Reason 1", "Reason 2")));

        // When
        AISearchResponse response = recommendationService.search(request);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalReturned());
        assertEquals(1, response.getTotalFound());
        AISearchResponse.RestaurantRecommendation recommendation = response.getRecommendations().get(0);
        assertEquals("Saigon Dining", recommendation.getRestaurantName());
        assertNotNull(recommendation.getDistanceKm());
        assertTrue(recommendation.getDistanceKm() < 1.0);
    }

    @Test
    @DisplayName("testSearch_WithDistrictKeyword_ShouldFilterByAddress")
    void testSearch_WithDistrictKeyword_ShouldFilterByAddress() throws Exception {
        // Given
        AISearchRequest request = new AISearchRequest();
        request.setQuery("Nhà hàng quận 1");
        request.setMaxResults(5);
        request.setUserId("user123");

        Map<String, Object> openIntent = new HashMap<>();
        openIntent.put("cuisine", List.of());
        openIntent.put("price_range", Map.of("min", 0, "max", 1_000_000));
        openIntent.put("dietary", List.of());
        openIntent.put("confidence", 0.9);

        RestaurantProfile districtOneRestaurant = new RestaurantProfile();
        districtOneRestaurant.setRestaurantId(301);
        districtOneRestaurant.setRestaurantName("Quan 1 Bistro");
        districtOneRestaurant.setCuisineType("Vietnamese");
        districtOneRestaurant.setAveragePrice(BigDecimal.valueOf(200000));
        districtOneRestaurant.setAddress("123 Lê Lợi, Quận 1, Hồ Chí Minh");

        RestaurantProfile districtThreeRestaurant = new RestaurantProfile();
        districtThreeRestaurant.setRestaurantId(302);
        districtThreeRestaurant.setRestaurantName("Quan 3 Bistro");
        districtThreeRestaurant.setCuisineType("Vietnamese");
        districtThreeRestaurant.setAveragePrice(BigDecimal.valueOf(200000));
        districtThreeRestaurant.setAddress("456 Điện Biên Phủ, Quận 3, Hồ Chí Minh");

        when(openAIService.parseIntent(anyString(), any()))
            .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(openIntent));
        when(restaurantService.findAllRestaurants()).thenReturn(List.of(districtOneRestaurant, districtThreeRestaurant));
        when(openAIService.explainRestaurants(anyList()))
            .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(List.of("Reason 1", "Reason 2")));

        // When
        AISearchResponse response = recommendationService.search(request);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalReturned());
        assertEquals("Quan 1 Bistro", response.getRecommendations().get(0).getRestaurantName());
    }

    @Test
    @DisplayName("testSearch_WithLowPriceHint_ShouldExcludeExpensiveRestaurants")
    void testSearch_WithLowPriceHint_ShouldExcludeExpensiveRestaurants() throws Exception {
        // Given
        AISearchRequest request = new AISearchRequest();
        request.setQuery("Tìm nhà hàng BBQ giá tầm 10k");
        request.setMaxResults(5);
        request.setUserId("user123");

        Map<String, Object> openIntent = new HashMap<>();
        openIntent.put("cuisine", List.of("BBQ"));
        openIntent.put("price_range", Map.of("min", 0, "max", 1_000_000));
        openIntent.put("dietary", List.of());
        openIntent.put("confidence", 0.8);

        RestaurantProfile expensiveRestaurant = new RestaurantProfile();
        expensiveRestaurant.setRestaurantId(401);
        expensiveRestaurant.setRestaurantName("Seoul BBQ Premium");
        expensiveRestaurant.setCuisineType("BBQ");
        expensiveRestaurant.setAveragePrice(BigDecimal.valueOf(350000));

        when(openAIService.parseIntent(anyString(), any()))
            .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(openIntent));
        when(restaurantService.findAllRestaurants()).thenReturn(List.of(expensiveRestaurant));

        // When
        AISearchResponse response = recommendationService.search(request);

        // Then
        assertNotNull(response);
        assertEquals(0, response.getTotalReturned());
        assertTrue(response.getRecommendations().isEmpty());
    }

    @Test
    @DisplayName("testSearch_WithPriceHint_ShouldAllowToleranceRange")
    void testSearch_WithPriceHint_ShouldAllowToleranceRange() throws Exception {
        // Given
        AISearchRequest request = new AISearchRequest();
        request.setQuery("Tìm nhà hàng BBQ giá khoảng 350k");
        request.setMaxResults(5);
        request.setUserId("user123");

        Map<String, Object> openIntent = new HashMap<>();
        openIntent.put("cuisine", List.of("BBQ"));
        openIntent.put("price_range", Map.of("min", 0, "max", 1_000_000));
        openIntent.put("dietary", List.of());
        openIntent.put("confidence", 0.8);

        RestaurantProfile withinRangeRestaurant = new RestaurantProfile();
        withinRangeRestaurant.setRestaurantId(501);
        withinRangeRestaurant.setRestaurantName("BBQ House 300k");
        withinRangeRestaurant.setCuisineType("BBQ");
        withinRangeRestaurant.setAveragePrice(BigDecimal.valueOf(300000));

        RestaurantProfile upperRangeRestaurant = new RestaurantProfile();
        upperRangeRestaurant.setRestaurantId(502);
        upperRangeRestaurant.setRestaurantName("BBQ House 400k");
        upperRangeRestaurant.setCuisineType("BBQ");
        upperRangeRestaurant.setAveragePrice(BigDecimal.valueOf(400000));

        RestaurantProfile outOfRangeRestaurant = new RestaurantProfile();
        outOfRangeRestaurant.setRestaurantId(503);
        outOfRangeRestaurant.setRestaurantName("BBQ House 450k");
        outOfRangeRestaurant.setCuisineType("BBQ");
        outOfRangeRestaurant.setAveragePrice(BigDecimal.valueOf(450000));

        when(openAIService.parseIntent(anyString(), any()))
            .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(openIntent));
        when(restaurantService.findAllRestaurants())
            .thenReturn(List.of(withinRangeRestaurant, upperRangeRestaurant, outOfRangeRestaurant));
        when(openAIService.explainRestaurants(anyList()))
            .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(List.of("Reason 1", "Reason 2", "Reason 3")));

        // When
        AISearchResponse response = recommendationService.search(request);

        // Then
        assertNotNull(response);
        assertEquals(2, response.getTotalReturned());
        List<String> names = response.getRecommendations().stream()
            .map(AISearchResponse.RestaurantRecommendation::getRestaurantName)
            .toList();
        assertTrue(names.contains("BBQ House 300k"));
        assertTrue(names.contains("BBQ House 400k"));
        assertTrue(names.stream().noneMatch(name -> name.contains("450k")));
    }

    @Test
    @DisplayName("testSearch_WithMalformedUserLocation_ShouldNotCrashAndReturnResults")
    void testSearch_WithMalformedUserLocation_ShouldNotCrashAndReturnResults() throws Exception {
        // Given
        AISearchRequest request = new AISearchRequest();
        request.setQuery("Nhà hàng gần tui");
        request.setUserLocation("abc,xyz"); // malformed
        request.setMaxResults(5);

        Map<String, Object> openIntent = new HashMap<>();
        openIntent.put("cuisine", List.of());
        openIntent.put("price_range", Map.of("min", 0, "max", 1_000_000));
        openIntent.put("dietary", List.of());
        openIntent.put("confidence", 0.9);

        RestaurantProfile r1 = new RestaurantProfile();
        r1.setRestaurantId(401);
        r1.setRestaurantName("A Bistro");
        r1.setCuisineType("Vietnamese");
        r1.setAveragePrice(BigDecimal.valueOf(180000));
        r1.setAddress("1 ABC, Quận 1, Hồ Chí Minh");

        RestaurantProfile r2 = new RestaurantProfile();
        r2.setRestaurantId(402);
        r2.setRestaurantName("B Bistro");
        r2.setCuisineType("Vietnamese");
        r2.setAveragePrice(BigDecimal.valueOf(200000));
        r2.setAddress("2 DEF, Hoàn Kiếm, Hà Nội");

        when(openAIService.parseIntent(anyString(), any()))
            .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(openIntent));
        when(restaurantService.findAllRestaurants()).thenReturn(List.of(r1, r2));
        lenient().when(openAIService.explainRestaurants(anyList()))
            .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(List.of("Reason 1", "Reason 2")));

        // When
        AISearchResponse response = recommendationService.search(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getRecommendations());
        assertTrue(response.getTotalReturned() >= 0);
        assertTrue(response.getRecommendations().size() <= 5);
    }
}

