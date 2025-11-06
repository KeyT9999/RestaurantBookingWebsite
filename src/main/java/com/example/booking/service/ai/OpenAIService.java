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
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${ai.openai.model:gpt-4o-mini}")
    private String model = "gpt-4o-mini";
    
    @Value("${ai.openai.timeout-ms:800}")
    private int timeoutMs = 800;
    
    /**
     * Parse user intent - enhanced version with food suggestions
     */
    public CompletableFuture<Map<String, Object>> parseIntent(String query, String userId) {
        if (query == null || query.trim().isEmpty()) {
            return CompletableFuture.completedFuture(defaultIntentFallback());
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                String systemPrompt = """
                    Bạn là AI chuyên phân tích ý định tìm kiếm nhà hàng và tư vấn món ăn.
                    Phân tích câu hỏi và trả về JSON với các trường:
                    - cuisine: danh sách loại ẩm thực
                    - party_size: số người
                    - price_range: khoảng giá (min, max)
                    - distance: khoảng cách km
                    - dietary: yêu cầu ăn uống đặc biệt
                    - intent_type: "restaurant_search" hoặc "food_advice"
                    - suggested_foods: danh sách món ăn được đề xuất (BẮT BUỘC khi intent_type là "food_advice")
                    - interpretation: câu giải thích ngắn gọn bằng tiếng Việt (BẮT BUỘC khi intent_type là "food_advice")
                    
                    QUY TẮC QUAN TRỌNG:
                    Nếu user hỏi về món ăn nên ăn (tập gym, giảm cân, muốn ăn cái gì, nên ăn gì, v.v.) 
                    → BẮT BUỘC phải đặt intent_type = "food_advice" và đưa ra interpretation + suggested_foods
                    
                    CÁC TRƯỜNG HỢP LÀ "food_advice":
                    - "Tôi đang tập gym, tôi muốn ăn cái gì" → food_advice
                    - "Tôi tập gym nên ăn gì" → food_advice
                    - "Tôi đang giảm cân, muốn ăn gì đó" → food_advice
                    - "Tôi đang giảm cân" → food_advice
                    - "Tôi muốn bổ sung protein" → food_advice
                    - "Dạo này tôi tập gym nên ăn gì" → food_advice
                    - Bất kỳ câu hỏi nào về "nên ăn gì", "ăn cái gì", "muốn ăn gì" → food_advice
                    
                    CÁC TRƯỜNG HỢP LÀ "restaurant_search":
                    - "Nhà hàng sushi" → restaurant_search
                    - "Tìm quán phở" → restaurant_search
                    - "Nhà hàng gần đây" → restaurant_search
                    - Chỉ tìm nhà hàng, không hỏi về món ăn → restaurant_search
                    
                    VÍ DỤ ĐẦY ĐỦ:
                    Query: "Tôi đang tập gym, tôi muốn ăn cái gì"
                    Response: {
                      "intent_type": "food_advice",
                      "suggested_foods": ["ức gà", "cá hồi", "trứng", "thịt bò", "yến mạch"],
                      "interpretation": "Bạn đang tập gym nên ăn ức gà, cá hồi, trứng, thịt bò, yến mạch để bổ sung protein và hỗ trợ phát triển cơ bắp hiệu quả",
                      "cuisine": ["Healthy"],
                      "party_size": 1,
                      "price_range": {"min": null, "max": null},
                      "distance": null,
                      "dietary": []
                    }
                    
                    Query: "Tôi đang giảm cân, muốn ăn gì đó"
                    Response: {
                      "intent_type": "food_advice",
                      "suggested_foods": ["salad", "ức gà", "rau củ luộc", "cá nướng", "trái cây"],
                      "interpretation": "Bạn đang giảm cân nên ăn salad, ức gà, rau củ luộc, cá nướng, trái cây - những món ít calo nhưng giàu dinh dưỡng, giúp bạn giảm cân hiệu quả",
                      "cuisine": ["Healthy"],
                      "party_size": 1,
                      "price_range": {"min": null, "max": null},
                      "distance": null,
                      "dietary": ["low-calorie"]
                    }
                    
                    Query: "Nhà hàng sushi"
                    Response: {
                      "intent_type": "restaurant_search",
                      "suggested_foods": [],
                      "interpretation": "",
                      "cuisine": ["Japanese"],
                      "party_size": 2,
                      "price_range": {"min": null, "max": null},
                      "distance": null,
                      "dietary": []
                    }
                    
                    LƯU Ý: Luôn trả về interpretation khi intent_type = "food_advice", không được để trống!
                    """;
                
                String userPrompt = String.format("Phân tích: '%s'", query);
                
                ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(List.of(
                        new ChatMessage(ChatMessageRole.SYSTEM.value(), systemPrompt),
                        new ChatMessage(ChatMessageRole.USER.value(), userPrompt)
                    ))
                    .temperature(0.2)  // Lower temperature for more consistent, structured output
                    .maxTokens(600)    // Increase tokens to ensure full JSON response
                    .build();
                
                String response = openAiService.createChatCompletion(request)
                    .getChoices().get(0).getMessage().getContent();
                
                // Clean JSON response (remove markdown code blocks if present)
                String cleanedResponse = response.trim();
                if (cleanedResponse.startsWith("```json")) {
                    cleanedResponse = cleanedResponse.substring(7);
                }
                if (cleanedResponse.startsWith("```")) {
                    cleanedResponse = cleanedResponse.substring(3);
                }
                if (cleanedResponse.endsWith("```")) {
                    cleanedResponse = cleanedResponse.substring(0, cleanedResponse.length() - 3);
                }
                cleanedResponse = cleanedResponse.trim();
                
                // Parse JSON response
                @SuppressWarnings("unchecked")
                Map<String, Object> result = objectMapper.readValue(cleanedResponse, Map.class);
                
                // Ensure default values
                if (!result.containsKey("intent_type")) {
                    result.put("intent_type", "restaurant_search");
                }
                if (!result.containsKey("suggested_foods")) {
                    result.put("suggested_foods", List.of());
                }
                
                // Auto-generate interpretation if missing but has suggested_foods
                String currentInterpretation = "";
                if (result.containsKey("interpretation") && result.get("interpretation") != null) {
                    currentInterpretation = result.get("interpretation").toString().trim();
                }
                
                if (currentInterpretation.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    List<String> foods = extractStringListFromObject(result.get("suggested_foods"));
                    String intentType = result.getOrDefault("intent_type", "restaurant_search").toString();
                    
                    if ("food_advice".equals(intentType) && foods != null && !foods.isEmpty()) {
                        // Generate interpretation based on context
                        String foodList = String.join(", ", foods);
                        String queryLower = query.toLowerCase();
                        
                        if (queryLower.contains("tập gym") || queryLower.contains("gym")) {
                            result.put("interpretation", "Bạn đang tập gym nên ăn " + foodList + 
                                " để bổ sung protein và hỗ trợ phát triển cơ bắp hiệu quả.");
                        } else if (queryLower.contains("giảm cân") || queryLower.contains("giảm cân")) {
                            result.put("interpretation", "Bạn đang giảm cân nên ăn " + foodList + 
                                " - những món ít calo nhưng giàu dinh dưỡng, giúp bạn giảm cân hiệu quả.");
                        } else if (queryLower.contains("protein")) {
                            result.put("interpretation", "Để bổ sung protein, bạn nên ăn " + foodList + 
                                " - những thực phẩm giàu protein tốt cho sức khỏe.");
                        } else {
                            result.put("interpretation", "Dựa trên yêu cầu của bạn, tôi đề xuất các món: " + foodList + 
                                ". Đây là những món ăn phù hợp với nhu cầu của bạn.");
                        }
                        System.out.println("✅ Auto-generated interpretation: " + result.get("interpretation"));
                    } else {
                        result.put("interpretation", "");
                    }
                }
                
                System.out.println("📊 Parsed intent result: " + result);
                System.out.println("📝 Interpretation: " + result.get("interpretation"));
                System.out.println("🍽️ Suggested foods: " + result.get("suggested_foods"));
                
                return result;
                
            } catch (Exception e) {
                System.err.println("Error parsing intent: " + e.getMessage());
                e.printStackTrace();
                return defaultIntentFallback();
            }
        }).orTimeout(timeoutMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Suggest food items based on user context (gym, weight loss, etc.)
     * This method is called when intent_type is "food_advice"
     */
    public CompletableFuture<List<String>> suggestFoodItems(String query, String userId) {
        if (query == null || query.trim().isEmpty()) {
            return CompletableFuture.completedFuture(java.util.Collections.emptyList());
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                String systemPrompt = """
                    Bạn là chuyên gia dinh dưỡng AI. Dựa vào yêu cầu của người dùng, đề xuất 3-5 món ăn cụ thể phù hợp.
                    Trả về danh sách tên món ăn bằng tiếng Việt, mỗi món trên một dòng.
                    Chỉ trả về tên món, không có số thứ tự hay dấu gạch đầu dòng.
                    
                    Ví dụ:
                    - "Tôi tập gym" → ức gà, cá hồi, trứng, thịt bò, yến mạch
                    - "Tôi đang giảm cân" → salad, ức gà, rau củ luộc, cá nướng, trái cây
                    - "Tôi muốn bổ sung protein" → ức gà, cá hồi, trứng, đậu phụ, thịt bò
                    """;
                
                String userPrompt = String.format("Đề xuất món ăn cho: '%s'", query);
                
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
                
                // Parse response - split by newlines and commas, clean up
                List<String> foods = java.util.Arrays.stream(response.split("[,\\n]"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .filter(s -> !s.matches("^\\d+[.\\-]\\s*")) // Remove numbered lists
                    .map(s -> s.replaceAll("^[\\-*]\\s*", "")) // Remove bullet points
                    .map(s -> s.replaceAll("^\\d+[.\\-]\\s*", "")) // Remove numbers
                    .distinct()
                    .limit(5)
                    .collect(java.util.stream.Collectors.toList());
                
                return foods;
                
            } catch (Exception e) {
                System.err.println("Error suggesting food items: " + e.getMessage());
                e.printStackTrace();
                return new java.util.ArrayList<String>();
            }
        }, java.util.concurrent.ForkJoinPool.commonPool()).orTimeout(timeoutMs, TimeUnit.MILLISECONDS);
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
    
    /**
     * Helper method to extract string list from object
     */
    @SuppressWarnings("unchecked")
    private List<String> extractStringListFromObject(Object obj) {
        if (obj == null) {
            return List.of();
        }
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            return list.stream()
                .map(item -> item != null ? item.toString().trim() : "")
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toList());
        }
        return List.of();
    }

    private Map<String, Object> defaultIntentFallback() {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("cuisine", List.of());
        fallback.put("party_size", 2);
        fallback.put("price_range", Map.of("min", 100000, "max", 500000));
        fallback.put("distance", 5.0);
        fallback.put("dietary", List.of());
        fallback.put("confidence", 0.5);
        fallback.put("intent_type", "restaurant_search");
        fallback.put("suggested_foods", List.of());
        fallback.put("interpretation", "");
        return fallback;
    }
}
