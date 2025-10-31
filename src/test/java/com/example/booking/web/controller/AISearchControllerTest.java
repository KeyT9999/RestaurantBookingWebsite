package com.example.booking.web.controller;

import com.example.booking.domain.User;
import com.example.booking.dto.ai.AISearchRequest;
import com.example.booking.dto.ai.AISearchResponse;
import com.example.booking.service.SimpleUserService;
import com.example.booking.service.ai.RecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AISearchControllerTest {

    @InjectMocks
    private AISearchController controller;

    @Mock
    private RecommendationService recommendationService;

    @Mock
    private SimpleUserService userService;

    private AISearchRequest request;

    @BeforeEach
    void setUp() {
        request = new AISearchRequest("find sushi");
        request.setSessionId("session-123");
    }

    @Test
    void searchRestaurants_shouldAttachAuthenticatedUserAndReturnResponse() {
        Authentication authentication = new TestingAuthenticationToken("jane", "pwd");
        authentication.setAuthenticated(true);

        User user = new User();
        user.setId(UUID.randomUUID());
        when(userService.findByUsername("jane")).thenReturn(Optional.of(user));

        AISearchResponse expectedResponse = new AISearchResponse();
        expectedResponse.setTotalFound(3);
        when(recommendationService.search(request)).thenReturn(expectedResponse);

        ResponseEntity<AISearchResponse> response = controller.searchRestaurants(request, authentication);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedResponse, response.getBody());
        assertEquals(user.getId().toString(), request.getUserId());
        verify(recommendationService).search(request);
    }

    @Test
    void searchRestaurants_shouldHandleExceptionsGracefully() {
        Authentication authentication = new TestingAuthenticationToken("anonymous", "pwd");
        authentication.setAuthenticated(true);
        when(userService.findByUsername("anonymous")).thenReturn(Optional.empty());

        doThrow(new RuntimeException("AI error"))
                .when(recommendationService).search(any(AISearchRequest.class));

        ResponseEntity<AISearchResponse> response = controller.searchRestaurants(request, authentication);

        assertEquals(200, response.getStatusCodeValue());
        AISearchResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("find sushi", body.getOriginalQuery());
        assertEquals(0, body.getTotalFound());
        assertEquals("Có lỗi xảy ra khi tìm kiếm. Vui lòng thử lại.", body.getExplanation());
    }

    @Test
    void searchRestaurantsAdvanced_shouldDelegateToMainSearch() {
        Authentication authentication = new TestingAuthenticationToken("demo", "pwd");
        authentication.setAuthenticated(true);

        AISearchResponse expected = new AISearchResponse();
        when(recommendationService.search(request)).thenReturn(expected);

        ResponseEntity<AISearchResponse> response = controller.searchRestaurantsAdvanced(request, authentication);

        assertEquals(expected, response.getBody());
        verify(recommendationService).search(request);
    }
}
 