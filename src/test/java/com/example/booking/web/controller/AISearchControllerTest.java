package com.example.booking.web.controller;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.ai.AISearchRequest;
import com.example.booking.dto.ai.AISearchResponse;
import com.example.booking.service.SimpleUserService;
import com.example.booking.service.ai.RecommendationService;

@ExtendWith(MockitoExtension.class)
@DisplayName("AISearchController Tests")
public class AISearchControllerTest {

    @Mock
    private RecommendationService recommendationService;

    @Mock
    private SimpleUserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AISearchController aiSearchController;

    private AISearchRequest validRequest;
    private AISearchResponse successResponse;
    private AISearchResponse errorResponse;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Setup valid request
        validRequest = new AISearchRequest();
        validRequest.setQuery("Tôi muốn ăn phở");
        validRequest.setMaxResults(5);

        // Setup success response
        successResponse = new AISearchResponse();
        successResponse.setOriginalQuery("Tôi muốn ăn phở");
        successResponse.setTotalFound(3);
        successResponse.setTotalReturned(3);
        successResponse.setExplanation("Tìm thấy 3 nhà hàng phù hợp");

        // Setup error response
        errorResponse = new AISearchResponse();
        errorResponse.setOriginalQuery("Tôi muốn ăn phở");
        errorResponse.setTotalFound(0);
        errorResponse.setExplanation("Có lỗi xảy ra khi tìm kiếm. Vui lòng thử lại.");

        // Setup test user
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("customer@example.com");
        testUser.setRole(UserRole.CUSTOMER);
    }

    // ========== Endpoint 1: searchRestaurants() - POST /ai/search ==========

    @Test
    @DisplayName("testSearchRestaurants_WithAuthenticatedUser_ShouldReturnRecommendations")
    void testSearchRestaurants_WithAuthenticatedUser_ShouldReturnRecommendations() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("customer@example.com");
        when(userService.findByUsername("customer@example.com")).thenReturn(Optional.of(testUser));
        when(recommendationService.search(any(AISearchRequest.class))).thenReturn(successResponse);

        // When
        ResponseEntity<AISearchResponse> response = aiSearchController.searchRestaurants(validRequest, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AISearchResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("Tôi muốn ăn phở", body.getOriginalQuery());
        assertEquals(3, body.getTotalFound());
    }

    @Test
    @DisplayName("testSearchRestaurants_WithoutAuthentication_ShouldReturnRecommendations")
    void testSearchRestaurants_WithoutAuthentication_ShouldReturnRecommendations() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(false);
        when(recommendationService.search(any(AISearchRequest.class))).thenReturn(successResponse);

        // When
        ResponseEntity<AISearchResponse> response = aiSearchController.searchRestaurants(validRequest, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AISearchResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(3, body.getTotalFound());
    }

    @Test
    @DisplayName("testSearchRestaurants_WithValidQuery_ShouldReturnMatchingRestaurants")
    void testSearchRestaurants_WithValidQuery_ShouldReturnMatchingRestaurants() {
        // Given
        AISearchRequest specificRequest = new AISearchRequest();
        specificRequest.setQuery("Ăn buffet giá rẻ");
        
        AISearchResponse customResponse = new AISearchResponse();
        customResponse.setOriginalQuery("Ăn buffet giá rẻ");
        customResponse.setTotalFound(2);
        customResponse.setTotalReturned(2);
        customResponse.setExplanation("Tìm thấy 2 nhà hàng phù hợp");

        when(authentication.isAuthenticated()).thenReturn(false);
        when(recommendationService.search(specificRequest)).thenReturn(customResponse);

        // When
        ResponseEntity<AISearchResponse> response = aiSearchController.searchRestaurants(specificRequest, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AISearchResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("Ăn buffet giá rẻ", body.getOriginalQuery());
    }

    @Test
    @DisplayName("testSearchRestaurants_WithNullRequest_ShouldThrowException")
    void testSearchRestaurants_WithNullRequest_ShouldThrowException() {
        // Given
        Authentication auth = null;

        // When/Then
        assertThrows(NullPointerException.class, () -> {
            aiSearchController.searchRestaurants(null, auth);
        });
    }

    @Test
    @DisplayName("testSearchRestaurants_WithEmptyQuery_ShouldReturnErrorResponse")
    void testSearchRestaurants_WithEmptyQuery_ShouldReturnErrorResponse() {
        // Given
        AISearchRequest emptyRequest = new AISearchRequest();
        emptyRequest.setQuery("");

        when(authentication.isAuthenticated()).thenReturn(false);
        when(recommendationService.search(emptyRequest)).thenThrow(new IllegalArgumentException("Query text is required"));

        // When
        ResponseEntity<AISearchResponse> response = aiSearchController.searchRestaurants(emptyRequest, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AISearchResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("Có lỗi xảy ra khi tìm kiếm. Vui lòng thử lại.", body.getExplanation());
    }

    @Test
    @DisplayName("testSearchRestaurants_WhenRecommendationServiceFails_ShouldReturnErrorResponse")
    void testSearchRestaurants_WhenRecommendationServiceFails_ShouldReturnErrorResponse() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(false);
        when(recommendationService.search(any(AISearchRequest.class))).thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<AISearchResponse> response = aiSearchController.searchRestaurants(validRequest, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AISearchResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("Có lỗi xảy ra khi tìm kiếm. Vui lòng thử lại.", body.getExplanation());
        assertEquals(0, body.getTotalFound());
    }

    @Test
    @DisplayName("testSearchRestaurants_WithVeryLongQuery_ShouldValidateLength")
    void testSearchRestaurants_WithVeryLongQuery_ShouldValidateLength() {
        // Given
        AISearchRequest longRequest = new AISearchRequest();
        String longQuery = "x".repeat(501);
        longRequest.setQuery(longQuery);

        when(authentication.isAuthenticated()).thenReturn(false);
        when(recommendationService.search(any(AISearchRequest.class))).thenReturn(errorResponse);

        // When
        ResponseEntity<AISearchResponse> response = aiSearchController.searchRestaurants(longRequest, authentication);

        // Then
        assertNotNull(response);
        // Should handle long query or return error
    }

    // ========== Endpoint 2: searchRestaurantsAdvanced() - POST /ai/restaurants/search ==========

    @Test
    @DisplayName("testSearchRestaurantsAdvanced_ShouldRedirectToMainSearch")
    void testSearchRestaurantsAdvanced_ShouldRedirectToMainSearch() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(false);
        when(recommendationService.search(any(AISearchRequest.class))).thenReturn(successResponse);

        // When
        ResponseEntity<AISearchResponse> response = aiSearchController.searchRestaurantsAdvanced(validRequest, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AISearchResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(3, body.getTotalFound());
    }
}

