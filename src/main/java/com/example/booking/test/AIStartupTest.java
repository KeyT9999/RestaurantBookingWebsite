package com.example.booking.test;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.booking.dto.ai.AISearchRequest;
import com.example.booking.dto.ai.AISearchResponse;
import com.example.booking.service.ai.OpenAIService;
import com.example.booking.service.ai.RecommendationService;

/**
 * Test component để chạy test AI service khi ứng dụng khởi động
 */
@Component
public class AIStartupTest implements CommandLineRunner {

    @Autowired
    private OpenAIService openAIService;

    @Autowired
    private RecommendationService recommendationService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n🚀 === AI SERVICE STARTUP TEST ===");
        
        try {
            // Test 1: Intent Parsing
            System.out.println("\n1️⃣ Test Intent Parsing...");
            String testQuery = "Tôi muốn ăn phở bò";
            System.out.println("🔍 Query: " + testQuery);
            
            CompletableFuture<Map<String, Object>> intentFuture = openAIService.parseIntent(testQuery, "test-user");
            Map<String, Object> intent = intentFuture.get(10, TimeUnit.SECONDS);
            
            System.out.println("✅ Intent parsed: " + intent);
            
            // Test 2: Explanation Generation
            System.out.println("\n2️⃣ Test Explanation Generation...");
            CompletableFuture<java.util.List<String>> explanationFuture = openAIService.explainRestaurants(java.util.List.of("Phở Hùng", "Phở Lý"));
            java.util.List<String> explanations = explanationFuture.get(10, TimeUnit.SECONDS);
            
            System.out.println("✅ Explanations: " + explanations);
            
            // Test 3: Full Recommendation Flow
            System.out.println("\n3️⃣ Test Full Recommendation Flow...");
            AISearchRequest request = new AISearchRequest();
            request.setQuery(testQuery);
            request.setMaxResults(3);
            
            AISearchResponse response = recommendationService.search(request);
            
            System.out.println("✅ Total found: " + response.getTotalFound());
            System.out.println("✅ Recommendations: " + response.getRecommendations().size());
            System.out.println("✅ Explanation: " + response.getExplanation());
            
            System.out.println("\n🎉 === AI SERVICE TEST COMPLETED SUCCESSFULLY ===");
            
        } catch (Exception e) {
            System.err.println("\n❌ === AI SERVICE TEST FAILED ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
