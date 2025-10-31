package com.example.booking.test;

import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for OpenAITest class to achieve 100% coverage
 */
@DisplayName("OpenAITest - 100% Coverage Tests")
class OpenAITestTest {

    // Helper method to create a mocked ChatCompletionResult with content
    private ChatCompletionResult createMockResult(String content) {
        ChatCompletionResult result = mock(ChatCompletionResult.class);
        ChatMessage msg = mock(ChatMessage.class);
        when(msg.getContent()).thenReturn(content);
        
        // Create a mock Choice object with getMessage() method
        ChatCompletionChoice choice = mock(ChatCompletionChoice.class);
        when(choice.getMessage()).thenReturn(msg);
        
        when(result.getChoices()).thenReturn(List.of(choice));
        
        return result;
    }

    @Test
    @DisplayName("testOpenAIKey - success path")
    void testOpenAIKey_success() {
        try (MockedConstruction<OpenAiService> mocked = 
                mockConstruction(OpenAiService.class, (mock, ctx) -> {
                    ChatCompletionResult result = createMockResult("hi");
                    when(mock.createChatCompletion(any(ChatCompletionRequest.class))).thenReturn(result);
                })) {
            
            OpenAITest bean = new OpenAITest();
            ReflectionTestUtils.setField(bean, "apiKey", "sk-1234567890");
            ReflectionTestUtils.setField(bean, "model", "gpt-4o-mini");
            
            bean.testOpenAIKey();
        }
    }

    @Test
    @DisplayName("testOpenAIKey - error 401 path")
    void testOpenAIKey_error401() {
        try (MockedConstruction<OpenAiService> mocked = 
                mockConstruction(OpenAiService.class, (mock, ctx) -> {
                    when(mock.createChatCompletion(any(ChatCompletionRequest.class)))
                            .thenThrow(new RuntimeException("401 unauthorized"));
                })) {
            
            OpenAITest bean = new OpenAITest();
            ReflectionTestUtils.setField(bean, "apiKey", "sk-xyz");
            ReflectionTestUtils.setField(bean, "model", "gpt-4o-mini");
            
            bean.testOpenAIKey();
        }
    }

    @Test
    @DisplayName("testOpenAIKey - error 429 path")
    void testOpenAIKey_error429() {
        try (MockedConstruction<OpenAiService> mocked = 
                mockConstruction(OpenAiService.class, (mock, ctx) -> {
                    when(mock.createChatCompletion(any(ChatCompletionRequest.class)))
                            .thenThrow(new RuntimeException("429 too many requests"));
                })) {
            
            OpenAITest bean = new OpenAITest();
            ReflectionTestUtils.setField(bean, "apiKey", "sk-xyz");
            ReflectionTestUtils.setField(bean, "model", "gpt-4o-mini");
            
            bean.testOpenAIKey();
        }
    }

    @Test
    @DisplayName("testOpenAIKey - timeout path")
    void testOpenAIKey_timeout() {
        try (MockedConstruction<OpenAiService> mocked = 
                mockConstruction(OpenAiService.class, (mock, ctx) -> {
                    when(mock.createChatCompletion(any(ChatCompletionRequest.class)))
                            .thenThrow(new RuntimeException("timeout while calling api"));
                })) {
            
            OpenAITest bean = new OpenAITest();
            ReflectionTestUtils.setField(bean, "apiKey", "sk-xyz");
            ReflectionTestUtils.setField(bean, "model", "gpt-4o-mini");
            
            bean.testOpenAIKey();
        }
    }

    @Test
    @DisplayName("testOpenAIKey - other error path")
    void testOpenAIKey_otherError() {
        try (MockedConstruction<OpenAiService> mocked = 
                mockConstruction(OpenAiService.class, (mock, ctx) -> {
                    when(mock.createChatCompletion(any(ChatCompletionRequest.class)))
                            .thenThrow(new RuntimeException("boom - other error"));
                })) {
            
            OpenAITest bean = new OpenAITest();
            ReflectionTestUtils.setField(bean, "apiKey", "sk-xyz");
            ReflectionTestUtils.setField(bean, "model", "gpt-4o-mini");
            
            bean.testOpenAIKey();
        }
    }

    @Test
    @DisplayName("testOpenAIKey - null apiKey path")
    void testOpenAIKey_nullApiKey() {
        try (MockedConstruction<OpenAiService> mocked = 
                mockConstruction(OpenAiService.class, (mock, ctx) -> {
                    ChatCompletionResult result = createMockResult("hi");
                    when(mock.createChatCompletion(any(ChatCompletionRequest.class))).thenReturn(result);
                })) {
            
            OpenAITest bean = new OpenAITest();
            ReflectionTestUtils.setField(bean, "apiKey", null);
            ReflectionTestUtils.setField(bean, "model", "gpt-4o-mini");
            
            bean.testOpenAIKey();
        }
    }

    @Test
    @DisplayName("testOpenAIKey - empty apiKey path")
    void testOpenAIKey_emptyApiKey() {
        try (MockedConstruction<OpenAiService> mocked = 
                mockConstruction(OpenAiService.class, (mock, ctx) -> {
                    ChatCompletionResult result = createMockResult("hi");
                    when(mock.createChatCompletion(any(ChatCompletionRequest.class))).thenReturn(result);
                })) {
            
            OpenAITest bean = new OpenAITest();
            ReflectionTestUtils.setField(bean, "apiKey", "");
            ReflectionTestUtils.setField(bean, "model", "gpt-4o-mini");
            
            bean.testOpenAIKey();
        }
    }

    @Test
    @DisplayName("testRestaurantIntentParsing - success path")
    void testRestaurantIntentParsing_success() {
        try (MockedConstruction<OpenAiService> mocked = 
                mockConstruction(OpenAiService.class, (mock, ctx) -> {
                    ChatCompletionResult result = createMockResult("{\"cuisine\":[\"sushi\"]}");
                    when(mock.createChatCompletion(any(ChatCompletionRequest.class))).thenReturn(result);
                })) {
            
            OpenAITest bean = new OpenAITest();
            ReflectionTestUtils.setField(bean, "apiKey", "sk-xyz");
            ReflectionTestUtils.setField(bean, "model", "gpt-4o-mini");
            
            bean.testRestaurantIntentParsing();
        }
    }

    @Test
    @DisplayName("testRestaurantIntentParsing - error path")
    void testRestaurantIntentParsing_error() {
        try (MockedConstruction<OpenAiService> mocked = 
                mockConstruction(OpenAiService.class, (mock, ctx) -> {
                    when(mock.createChatCompletion(any(ChatCompletionRequest.class)))
                            .thenThrow(new RuntimeException("boom"));
                })) {
            
            OpenAITest bean = new OpenAITest();
            ReflectionTestUtils.setField(bean, "apiKey", "sk-xyz");
            ReflectionTestUtils.setField(bean, "model", "gpt-4o-mini");
            
            bean.testRestaurantIntentParsing();
        }
    }
}
