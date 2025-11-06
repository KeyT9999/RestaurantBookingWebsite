package com.example.booking.web.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.booking.test.OpenAITest;

/**
 * Test Controller for OpenAI API
 */
@RestController
@RequestMapping("/test")
public class TestController {
    
    @Autowired
    private OpenAITest openAITest;
    
    @Autowired
    private Environment environment;
    
    @Value("${ai.openai.api-key:}")
    private String openaiApiKey;
    
    /**
     * Test OpenAI API key
     */
    @GetMapping("/openai")
    public ResponseEntity<Map<String, Object>> testOpenAI() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("🧪 Starting OpenAI API test...");
            openAITest.testOpenAIKey();
            
            result.put("status", "success");
            result.put("message", "OpenAI API key is working");
            
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "OpenAI API key test failed: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Test restaurant intent parsing
     */
    @GetMapping("/openai/intent")
    public ResponseEntity<Map<String, Object>> testIntentParsing() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("🧪 Starting intent parsing test...");
            openAITest.testRestaurantIntentParsing();
            
            result.put("status", "success");
            result.put("message", "Intent parsing test completed");
            
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "Intent parsing test failed: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Test with custom query
     */
    @PostMapping("/openai/query")
    public ResponseEntity<Map<String, Object>> testCustomQuery(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String query = request.get("query");
            if (query == null || query.trim().isEmpty()) {
                result.put("status", "error");
                result.put("message", "Query is required");
                return ResponseEntity.badRequest().body(result);
            }
            
            System.out.println("🧪 Testing custom query: " + query);
            
            // Get API key from Spring Environment (supports .env file via DotenvConfig)
            String apiKey = openaiApiKey;
            if (apiKey == null || apiKey.isEmpty()) {
                // Fallback to environment variable or system property
                apiKey = environment.getProperty("OPENAI_API_KEY", 
                    System.getProperty("OPENAI_API_KEY", 
                    System.getenv("OPENAI_API_KEY")));
            }
            
            if (apiKey == null || apiKey.isEmpty()) {
                result.put("status", "error");
                result.put("message", "OpenAI API key not found. Please set OPENAI_API_KEY in .env file or environment variables.");
                return ResponseEntity.badRequest().body(result);
            }
            
            // Test the query with OpenAI
            com.theokanning.openai.service.OpenAiService service = 
                new com.theokanning.openai.service.OpenAiService(
                    apiKey, 
                    java.time.Duration.ofSeconds(30)
                );
            
            com.theokanning.openai.completion.chat.ChatCompletionRequest chatRequest = 
                com.theokanning.openai.completion.chat.ChatCompletionRequest.builder()
                    .model("gpt-4o-mini")
                    .messages(java.util.List.of(
                        new com.theokanning.openai.completion.chat.ChatMessage(
                            com.theokanning.openai.completion.chat.ChatMessageRole.USER.value(), 
                            query
                        )
                    ))
                    .maxTokens(100)
                    .temperature(0.3)
                    .build();
            
            String response = service.createChatCompletion(chatRequest)
                .getChoices().get(0).getMessage().getContent();
            
            result.put("status", "success");
            result.put("query", query);
            result.put("response", response);
            
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "Custom query test failed: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
}