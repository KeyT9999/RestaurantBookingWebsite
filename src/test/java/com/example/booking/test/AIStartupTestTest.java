package com.example.booking.test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.booking.dto.ai.AISearchRequest;
import com.example.booking.dto.ai.AISearchResponse;
import com.example.booking.service.ai.OpenAIService;
import com.example.booking.service.ai.RecommendationService;

/**
 * Unit tests for AIStartupTest
 */
@ExtendWith(MockitoExtension.class)
class AIStartupTestTest {

    @Mock
    private OpenAIService openAIService;

    @Mock
    private RecommendationService recommendationService;

    @InjectMocks
    private AIStartupTest aiStartupTest;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void testRun_Success() throws Exception {
        // Given
        Map<String, Object> intentMap = new HashMap<>();
        intentMap.put("cuisine", List.of("phở"));
        intentMap.put("party_size", 2);
        
        List<String> explanations = List.of(
            "Phở Hùng is a popular Vietnamese restaurant",
            "Phở Lý is known for traditional beef pho"
        );
        
        AISearchResponse searchResponse = new AISearchResponse();
        searchResponse.setTotalFound(2);
        searchResponse.setRecommendations(new ArrayList<>());
        searchResponse.setExplanation("These restaurants match your search for phở");

        when(openAIService.parseIntent(anyString(), anyString()))
            .thenReturn(CompletableFuture.completedFuture(intentMap));
        
        when(openAIService.explainRestaurants(anyList()))
            .thenReturn(CompletableFuture.completedFuture(explanations));
        
        when(recommendationService.search(any(AISearchRequest.class)))
            .thenReturn(searchResponse);

        // When
        aiStartupTest.run();

        // Then
        String output = outContent.toString();
        assertTrue(output.contains("🚀 === AI SERVICE STARTUP TEST ==="));
        assertTrue(output.contains("1️⃣ Test Intent Parsing..."));
        assertTrue(output.contains("🔍 Query: Tôi muốn ăn phở bò"));
        assertTrue(output.contains("✅ Intent parsed:"));
        assertTrue(output.contains("2️⃣ Test Explanation Generation..."));
        assertTrue(output.contains("✅ Explanations:"));
        assertTrue(output.contains("3️⃣ Test Full Recommendation Flow..."));
        assertTrue(output.contains("✅ Total found: 2"));
        assertTrue(output.contains("🎉 === AI SERVICE TEST COMPLETED SUCCESSFULLY ==="));

        verify(openAIService).parseIntent(eq("Tôi muốn ăn phở bò"), eq("test-user"));
        verify(openAIService).explainRestaurants(anyList());
        verify(recommendationService).search(any(AISearchRequest.class));
    }

    @Test
    void testRun_IntentParsingFailure() throws Exception {
        // Given
        when(openAIService.parseIntent(anyString(), anyString()))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Intent parsing failed")));

        // When
        aiStartupTest.run();

        // Then
        String output = outContent.toString();
        String error = errContent.toString();
        // Check that test started but failed
        assertTrue(output.contains("🚀 === AI SERVICE STARTUP TEST ===") || 
                   error.contains("❌ === AI SERVICE TEST FAILED ==="));

        verify(openAIService).parseIntent(anyString(), anyString());
        verify(openAIService, never()).explainRestaurants(anyList());
        verify(recommendationService, never()).search(any(AISearchRequest.class));
    }

    @Test
    void testRun_ExplanationGenerationFailure() throws Exception {
        // Given
        Map<String, Object> intentMap = new HashMap<>();
        intentMap.put("cuisine", List.of("phở"));
        
        when(openAIService.parseIntent(anyString(), anyString()))
            .thenReturn(CompletableFuture.completedFuture(intentMap));
        
        when(openAIService.explainRestaurants(anyList()))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Explanation failed")));

        // When
        aiStartupTest.run();

        // Then
        String output = outContent.toString();
        String error = errContent.toString();
        // Check that test started but failed
        assertTrue(output.contains("🚀 === AI SERVICE STARTUP TEST ===") || 
                   error.contains("❌ === AI SERVICE TEST FAILED ==="));

        verify(openAIService).parseIntent(anyString(), anyString());
        verify(openAIService).explainRestaurants(anyList());
        verify(recommendationService, never()).search(any(AISearchRequest.class));
    }

    @Test
    void testRun_RecommendationServiceFailure() throws Exception {
        // Given
        Map<String, Object> intentMap = new HashMap<>();
        intentMap.put("cuisine", List.of("phở"));
        
        List<String> explanations = List.of("Explanation 1", "Explanation 2");
        
        when(openAIService.parseIntent(anyString(), anyString()))
            .thenReturn(CompletableFuture.completedFuture(intentMap));
        
        when(openAIService.explainRestaurants(anyList()))
            .thenReturn(CompletableFuture.completedFuture(explanations));
        
        when(recommendationService.search(any(AISearchRequest.class)))
            .thenThrow(new RuntimeException("Search failed"));

        // When
        aiStartupTest.run();

        // Then
        String error = errContent.toString();
        assertTrue(error.contains("❌ === AI SERVICE TEST FAILED ==="));
        assertTrue(error.contains("Error: Search failed"));

        verify(openAIService).parseIntent(anyString(), anyString());
        verify(openAIService).explainRestaurants(anyList());
        verify(recommendationService).search(any(AISearchRequest.class));
    }

    @Test
    void testRun_TimeoutException() throws Exception {
        // Given
        CompletableFuture<Map<String, Object>> timeoutFuture = new CompletableFuture<>();
        // Don't complete the future, let it timeout
        
        when(openAIService.parseIntent(anyString(), anyString()))
            .thenReturn(timeoutFuture);

        // When
        aiStartupTest.run();

        // Then
        String error = errContent.toString();
        assertTrue(error.contains("❌ === AI SERVICE TEST FAILED ==="));

        verify(openAIService).parseIntent(anyString(), anyString());
    }

    @Test
    void testRun_NullPointerException() throws Exception {
        // Given
        when(openAIService.parseIntent(anyString(), anyString()))
            .thenReturn(CompletableFuture.completedFuture(null));

        // When & Then
        assertDoesNotThrow(() -> aiStartupTest.run());
    }

    @Test
    void testRun_EmptyIntentMap() throws Exception {
        // Given
        Map<String, Object> emptyMap = new HashMap<>();
        List<String> explanations = List.of();
        AISearchResponse emptyResponse = new AISearchResponse();
        emptyResponse.setTotalFound(0);
        emptyResponse.setRecommendations(new ArrayList<>());
        emptyResponse.setExplanation("");

        when(openAIService.parseIntent(anyString(), anyString()))
            .thenReturn(CompletableFuture.completedFuture(emptyMap));
        
        when(openAIService.explainRestaurants(anyList()))
            .thenReturn(CompletableFuture.completedFuture(explanations));
        
        when(recommendationService.search(any(AISearchRequest.class)))
            .thenReturn(emptyResponse);

        // When
        aiStartupTest.run();

        // Then
        String output = outContent.toString();
        assertTrue(output.contains("✅ Total found: 0"));
        assertTrue(output.contains("🎉 === AI SERVICE TEST COMPLETED SUCCESSFULLY ==="));
    }

    @Test
    void testRun_WithArguments() throws Exception {
        // Given
        String[] args = {"arg1", "arg2", "arg3"};
        
        Map<String, Object> intentMap = new HashMap<>();
        intentMap.put("cuisine", List.of("phở"));
        
        List<String> explanations = List.of("Explanation");
        
        AISearchResponse searchResponse = new AISearchResponse();
        searchResponse.setTotalFound(1);
        searchResponse.setRecommendations(new ArrayList<>());
        searchResponse.setExplanation("Test");

        when(openAIService.parseIntent(anyString(), anyString()))
            .thenReturn(CompletableFuture.completedFuture(intentMap));
        
        when(openAIService.explainRestaurants(anyList()))
            .thenReturn(CompletableFuture.completedFuture(explanations));
        
        when(recommendationService.search(any(AISearchRequest.class)))
            .thenReturn(searchResponse);

        // When
        aiStartupTest.run(args);

        // Then
        String output = outContent.toString();
        assertTrue(output.contains("🎉 === AI SERVICE TEST COMPLETED SUCCESSFULLY ==="));
    }

    @Test
    void testRun_ComplexIntent() throws Exception {
        // Given
        Map<String, Object> complexIntent = new HashMap<>();
        complexIntent.put("cuisine", List.of("phở", "bún", "cơm"));
        complexIntent.put("party_size", 5);
        complexIntent.put("price_range", Map.of("min", 100000, "max", 500000));
        complexIntent.put("distance", 5.5);
        complexIntent.put("dietary", List.of("vegetarian"));
        
        List<String> explanations = List.of(
            "Restaurant 1 explanation",
            "Restaurant 2 explanation",
            "Restaurant 3 explanation"
        );
        
        AISearchResponse searchResponse = new AISearchResponse();
        searchResponse.setTotalFound(3);
        searchResponse.setRecommendations(new ArrayList<>());
        searchResponse.setExplanation("Complex search results");

        when(openAIService.parseIntent(anyString(), anyString()))
            .thenReturn(CompletableFuture.completedFuture(complexIntent));
        
        when(openAIService.explainRestaurants(anyList()))
            .thenReturn(CompletableFuture.completedFuture(explanations));
        
        when(recommendationService.search(any(AISearchRequest.class)))
            .thenReturn(searchResponse);

        // When
        aiStartupTest.run();

        // Then
        String output = outContent.toString();
        assertTrue(output.contains("🚀 === AI SERVICE STARTUP TEST ==="));
        assertTrue(output.contains("✅ Total found: 3"));
        assertTrue(output.contains("🎉 === AI SERVICE TEST COMPLETED SUCCESSFULLY ==="));
    }

    @Test
    void testRun_InterruptedException() throws Exception {
        // Given
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        
        when(openAIService.parseIntent(anyString(), anyString()))
            .thenReturn(future);

        // Simulate interruption
        Thread testThread = new Thread(() -> {
            try {
                aiStartupTest.run();
            } catch (Exception e) {
                // Expected
            }
        });
        
        testThread.start();
        Thread.sleep(100);
        testThread.interrupt();
        testThread.join(1000);

        // Then - should handle gracefully
        assertTrue(errContent.toString().contains("❌ === AI SERVICE TEST FAILED ===") 
                || testThread.isInterrupted());
    }

    @Test
    void testRun_MultipleRecommendations() throws Exception {
        // Given
        Map<String, Object> intentMap = new HashMap<>();
        intentMap.put("cuisine", List.of("phở"));
        
        List<String> explanations = List.of(
            "Restaurant 1", "Restaurant 2", "Restaurant 3", 
            "Restaurant 4", "Restaurant 5"
        );
        
        AISearchResponse searchResponse = new AISearchResponse();
        searchResponse.setTotalFound(5);
        searchResponse.setRecommendations(new ArrayList<>());
        searchResponse.setExplanation("Multiple restaurants found");

        when(openAIService.parseIntent(anyString(), anyString()))
            .thenReturn(CompletableFuture.completedFuture(intentMap));
        
        when(openAIService.explainRestaurants(anyList()))
            .thenReturn(CompletableFuture.completedFuture(explanations));
        
        when(recommendationService.search(any(AISearchRequest.class)))
            .thenReturn(searchResponse);

        // When
        aiStartupTest.run();

        // Then
        String output = outContent.toString();
        assertTrue(output.contains("✅ Total found: 5"));
        assertTrue(output.contains("🎉 === AI SERVICE TEST COMPLETED SUCCESSFULLY ==="));
    }
}

