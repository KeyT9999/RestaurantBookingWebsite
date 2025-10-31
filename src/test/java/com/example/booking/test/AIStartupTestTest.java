package com.example.booking.test;

import com.example.booking.dto.ai.AISearchRequest;
import com.example.booking.dto.ai.AISearchResponse;
import com.example.booking.service.ai.OpenAIService;
import com.example.booking.service.ai.RecommendationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for AIStartupTest class to achieve 100% coverage
 */
@DisplayName("AIStartupTest - 100% Coverage Tests")
class AIStartupTestTest {

    @Test
    @DisplayName("run - success path")
    void run_success() throws Exception {
        OpenAIService openAI = mock(OpenAIService.class);
        RecommendationService rec = mock(RecommendationService.class);

        when(openAI.parseIntent(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(Map.of("intent", "eat pho")));
        when(openAI.explainRestaurants(anyList()))
                .thenReturn(CompletableFuture.completedFuture(List.of("good broth", "close by")));

        AISearchResponse resp = new AISearchResponse();
        resp.setTotalFound(2);
        resp.setRecommendations(List.of());
        resp.setExplanation("Test explanation");
        when(rec.search(any(AISearchRequest.class))).thenReturn(resp);

        AIStartupTest runner = new AIStartupTest();
        ReflectionTestUtils.setField(runner, "openAIService", openAI);
        ReflectionTestUtils.setField(runner, "recommendationService", rec);

        runner.run(); // Go through happy path, including get(10, SECONDS)
    }

    @Test
    @DisplayName("run - error goes to catch")
    void run_error_goesToCatch() throws Exception {
        OpenAIService openAI = mock(OpenAIService.class);
        RecommendationService rec = mock(RecommendationService.class);

        when(openAI.parseIntent(anyString(), anyString()))
                .thenThrow(new RuntimeException("fail fast"));

        AIStartupTest runner = new AIStartupTest();
        ReflectionTestUtils.setField(runner, "openAIService", openAI);
        ReflectionTestUtils.setField(runner, "recommendationService", rec);

        runner.run(); // Throw error -> go to catch block
    }

    @Test
    @DisplayName("run - error in explainRestaurants")
    void run_error_inExplainRestaurants() throws Exception {
        OpenAIService openAI = mock(OpenAIService.class);
        RecommendationService rec = mock(RecommendationService.class);

        when(openAI.parseIntent(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(Map.of("intent", "eat pho")));
        when(openAI.explainRestaurants(anyList()))
                .thenThrow(new RuntimeException("explain error"));

        AIStartupTest runner = new AIStartupTest();
        ReflectionTestUtils.setField(runner, "openAIService", openAI);
        ReflectionTestUtils.setField(runner, "recommendationService", rec);

        runner.run(); // Error in explainRestaurants -> go to catch block
    }

    @Test
    @DisplayName("run - error in search")
    void run_error_inSearch() throws Exception {
        OpenAIService openAI = mock(OpenAIService.class);
        RecommendationService rec = mock(RecommendationService.class);

        when(openAI.parseIntent(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(Map.of("intent", "eat pho")));
        when(openAI.explainRestaurants(anyList()))
                .thenReturn(CompletableFuture.completedFuture(List.of("good broth", "close by")));
        when(rec.search(any(AISearchRequest.class)))
                .thenThrow(new RuntimeException("search error"));

        AIStartupTest runner = new AIStartupTest();
        ReflectionTestUtils.setField(runner, "openAIService", openAI);
        ReflectionTestUtils.setField(runner, "recommendationService", rec);

        runner.run(); // Error in search -> go to catch block
    }

    @Test
    @DisplayName("run - timeout in parseIntent")
    void run_timeout_inParseIntent() throws Exception {
        OpenAIService openAI = mock(OpenAIService.class);
        RecommendationService rec = mock(RecommendationService.class);

        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        // Never complete, will timeout at get(10, TimeUnit.SECONDS)
        when(openAI.parseIntent(anyString(), anyString())).thenReturn(future);

        AIStartupTest runner = new AIStartupTest();
        ReflectionTestUtils.setField(runner, "openAIService", openAI);
        ReflectionTestUtils.setField(runner, "recommendationService", rec);

        runner.run(); // Timeout -> go to catch block
    }
}


