package com.example.booking.test;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

/**
 * Simple OpenAI API Key Test
 */
@Component
public class OpenAITest {
    
    @Value("${ai.openai.api-key}")
    private String apiKey;
    
    @Value("${ai.openai.model:gpt-4o-mini}")
    private String model;
    
    /**
     * Test OpenAI API key
     */
    public void testOpenAIKey() {
        System.out.println("🔑 Testing OpenAI API Key...");
        System.out.println("API Key: " + (apiKey != null && !apiKey.isEmpty() ? 
            apiKey.substring(0, Math.min(10, apiKey.length())) + "..." : "NOT SET"));
        System.out.println("Model: " + model);
        
        try {
            // Create OpenAI service
            OpenAiService service = new OpenAiService(apiKey, Duration.ofSeconds(30));
            
            // Simple test request
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(model)
                .messages(List.of(
                    new ChatMessage(ChatMessageRole.USER.value(), "Hello, are you working?")
                ))
                .maxTokens(50)
                .temperature(0.1)
                .build();
            
            System.out.println("📡 Sending test request to OpenAI...");
            
            // Make request
            String response = service.createChatCompletion(request)
                .getChoices().get(0).getMessage().getContent();
            
            System.out.println("✅ OpenAI API Key is working!");
            System.out.println("Response: " + response);
            
        } catch (Exception e) {
            System.err.println("❌ OpenAI API Key test failed!");
            System.err.println("Error: " + e.getMessage());
            
            if (e.getMessage().contains("401")) {
                System.err.println("💡 Issue: Invalid API key");
            } else if (e.getMessage().contains("429")) {
                System.err.println("💡 Issue: Rate limit exceeded");
            } else if (e.getMessage().contains("timeout")) {
                System.err.println("💡 Issue: Network timeout");
            } else {
                System.err.println("💡 Issue: " + e.getClass().getSimpleName());
            }
        }
    }
    
    /**
     * Test restaurant search intent parsing
     */
    public void testRestaurantIntentParsing() {
        System.out.println("\n🍽️ Testing Restaurant Intent Parsing...");
        
        try {
            OpenAiService service = new OpenAiService(apiKey, Duration.ofSeconds(30));
            
            String systemPrompt = """
                Bạn là AI chuyên phân tích ý định tìm kiếm nhà hàng.
                Phân tích câu hỏi và trả về JSON với các trường:
                - cuisine: danh sách loại ẩm thực
                - party_size: số người
                - price_range: khoảng giá (min, max)
                - distance: khoảng cách km
                - dietary: yêu cầu ăn uống đặc biệt
                """;
            
            String userPrompt = "Tôi muốn ăn sushi gần đây với 2 người, giá khoảng 200k-500k";
            
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(model)
                .messages(List.of(
                    new ChatMessage(ChatMessageRole.SYSTEM.value(), systemPrompt),
                    new ChatMessage(ChatMessageRole.USER.value(), userPrompt)
                ))
                .maxTokens(200)
                .temperature(0.3)
                .build();
            
            System.out.println("📡 Testing intent parsing...");
            
            String response = service.createChatCompletion(request)
                .getChoices().get(0).getMessage().getContent();
            
            System.out.println("✅ Intent parsing test successful!");
            System.out.println("Response: " + response);
            
        } catch (Exception e) {
            System.err.println("❌ Intent parsing test failed!");
            System.err.println("Error: " + e.getMessage());
        }
    }
}
