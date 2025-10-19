package com.example.booking.service.ai;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

/**
 * Simplified OpenAI Service for MVP
 */
@Service
public class OpenAIService {
    
    @Autowired
    private OpenAiService openAiService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Value("${ai.openai.model:gpt-4o-mini}")
    private String model;
    
    @Value("${ai.openai.timeout-ms:800}")
    private int timeoutMs;
    
    /**
     * Parse user intent - simplified version
     */
    public CompletableFuture<Map<String, Object>> parseIntent(String query, String userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String systemPrompt = """
                    Bạn là AI chuyên phân tích ý định tìm kiếm nhà hàng.
                    Phân tích câu hỏi và trả về JSON với các trường:
                    - cuisine: danh sách loại ẩm thực
                    - party_size: số người
                    - price_range: khoảng giá (min, max)
                    - distance: khoảng cách km
                    - dietary: yêu cầu ăn uống đặc biệt
                    """;
                
                String userPrompt = String.format("Phân tích: '%s'", query);
                
                ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(List.of(
                        new ChatMessage(ChatMessageRole.SYSTEM.value(), systemPrompt),
                        new ChatMessage(ChatMessageRole.USER.value(), userPrompt)
                    ))
                    .temperature(0.3)
                    .maxTokens(300)
                    .build();
                
                String response = openAiService.createChatCompletion(request)
                    .getChoices().get(0).getMessage().getContent();
                
                // Parse JSON response
                @SuppressWarnings("unchecked")
                Map<String, Object> result = objectMapper.readValue(response, Map.class);
                return result;
                
            } catch (Exception e) {
                // Simple fallback
                Map<String, Object> fallback = new HashMap<>();
                fallback.put("cuisine", List.of());
                fallback.put("party_size", 2);
                fallback.put("price_range", Map.of("min", 100000, "max", 500000));
                fallback.put("distance", 5.0);
                fallback.put("dietary", List.of());
                fallback.put("confidence", 0.5);
                return fallback;
            }
        }).orTimeout(timeoutMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Generate simple explanations for restaurants
     */
    public CompletableFuture<List<String>> explainRestaurants(List<String> restaurantNames) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String systemPrompt = """
                    Bạn là AI chuyên giải thích lý do recommend nhà hàng.
                    Trả về danh sách lý do ngắn gọn cho từng nhà hàng.
                    """;
                
                String userPrompt = String.format("Giải thích tại sao recommend: %s", 
                    String.join(", ", restaurantNames));
                
                ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(List.of(
                        new ChatMessage(ChatMessageRole.SYSTEM.value(), systemPrompt),
                        new ChatMessage(ChatMessageRole.USER.value(), userPrompt)
                    ))
                    .temperature(0.5)
                    .maxTokens(200)
                    .build();
                
                String response = openAiService.createChatCompletion(request)
                    .getChoices().get(0).getMessage().getContent();
                
                // Simple parsing - split by lines
                return List.of(response.split("\n"));
                
            } catch (Exception e) {
                // Fallback explanations
                return restaurantNames.stream()
                    .map(name -> "Nhà hàng " + name + " phù hợp với yêu cầu của bạn")
                    .toList();
            }
        }).orTimeout(timeoutMs, TimeUnit.MILLISECONDS);
    }
}