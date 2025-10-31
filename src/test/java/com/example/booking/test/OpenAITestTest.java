package com.example.booking.test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

/**
 * Unit tests for OpenAITest
 */
@ExtendWith(MockitoExtension.class)
class OpenAITestTest {

    @InjectMocks
    private OpenAITest openAITest;

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
    void testOpenAIKey_Success() {
        // Given
        ReflectionTestUtils.setField(openAITest, "apiKey", "sk-test1234567890");
        ReflectionTestUtils.setField(openAITest, "model", "gpt-4o-mini");

        try (MockedConstruction<OpenAiService> mockedService = mockConstruction(OpenAiService.class,
                (mock, context) -> {
                    // Mock the response
                    ChatMessage responseMessage = new ChatMessage();
                    responseMessage.setContent("Yes, I'm working!");
                    
                    ChatCompletionChoice choice = new ChatCompletionChoice();
                    choice.setMessage(responseMessage);
                    
                    ChatCompletionResult result = new ChatCompletionResult();
                    result.setChoices(List.of(choice));
                    
                    when(mock.createChatCompletion(any(ChatCompletionRequest.class)))
                        .thenReturn(result);
                })) {

            // When
            openAITest.testOpenAIKey();

            // Then
            String output = outContent.toString();
            assertTrue(output.contains("🔑 Testing OpenAI API Key..."));
            assertTrue(output.contains("Model: gpt-4o-mini"));
            assertTrue(output.contains("✅ OpenAI API Key is working!"));
            assertTrue(output.contains("Response: Yes, I'm working!"));
        }
    }

    @Test
    void testOpenAIKey_NullApiKey() {
        // Given
        ReflectionTestUtils.setField(openAITest, "apiKey", null);
        ReflectionTestUtils.setField(openAITest, "model", "gpt-4o-mini");

        // When
        openAITest.testOpenAIKey();

        // Then
        String output = outContent.toString();
        assertTrue(output.contains("🔑 Testing OpenAI API Key..."));
        assertTrue(output.contains("NOT SET"));
    }

    @Test
    void testOpenAIKey_EmptyApiKey() {
        // Given
        ReflectionTestUtils.setField(openAITest, "apiKey", "");
        ReflectionTestUtils.setField(openAITest, "model", "gpt-4o-mini");

        // When
        openAITest.testOpenAIKey();

        // Then
        String output = outContent.toString();
        assertTrue(output.contains("🔑 Testing OpenAI API Key..."));
        assertTrue(output.contains("NOT SET"));
    }

    @Test
    void testOpenAIKey_ShortApiKey() {
        // Given
        ReflectionTestUtils.setField(openAITest, "apiKey", "sk-12345");
        ReflectionTestUtils.setField(openAITest, "model", "gpt-4o-mini");

        // When
        openAITest.testOpenAIKey();

        // Then
        String output = outContent.toString();
        assertTrue(output.contains("API Key: sk-12345..."));
    }

    @Test
    void testOpenAIKey_401Error() {
        // Given
        ReflectionTestUtils.setField(openAITest, "apiKey", "sk-invalid");
        ReflectionTestUtils.setField(openAITest, "model", "gpt-4o-mini");

        try (MockedConstruction<OpenAiService> mockedService = mockConstruction(OpenAiService.class,
                (mock, context) -> {
                    when(mock.createChatCompletion(any(ChatCompletionRequest.class)))
                        .thenThrow(new RuntimeException("401 Unauthorized"));
                })) {

            // When
            openAITest.testOpenAIKey();

            // Then
            String error = errContent.toString();
            assertTrue(error.contains("❌ OpenAI API Key test failed!"));
            assertTrue(error.contains("💡 Issue: Invalid API key"));
        }
    }

    @Test
    void testOpenAIKey_429Error() {
        // Given
        ReflectionTestUtils.setField(openAITest, "apiKey", "sk-test");
        ReflectionTestUtils.setField(openAITest, "model", "gpt-4o-mini");

        try (MockedConstruction<OpenAiService> mockedService = mockConstruction(OpenAiService.class,
                (mock, context) -> {
                    when(mock.createChatCompletion(any(ChatCompletionRequest.class)))
                        .thenThrow(new RuntimeException("429 Rate limit exceeded"));
                })) {

            // When
            openAITest.testOpenAIKey();

            // Then
            String error = errContent.toString();
            assertTrue(error.contains("❌ OpenAI API Key test failed!"));
            assertTrue(error.contains("💡 Issue: Rate limit exceeded"));
        }
    }

    @Test
    void testOpenAIKey_TimeoutError() {
        // Given
        ReflectionTestUtils.setField(openAITest, "apiKey", "sk-test");
        ReflectionTestUtils.setField(openAITest, "model", "gpt-4o-mini");

        try (MockedConstruction<OpenAiService> mockedService = mockConstruction(OpenAiService.class,
                (mock, context) -> {
                    when(mock.createChatCompletion(any(ChatCompletionRequest.class)))
                        .thenThrow(new RuntimeException("Connection timeout"));
                })) {

            // When
            openAITest.testOpenAIKey();

            // Then
            String error = errContent.toString();
            assertTrue(error.contains("❌ OpenAI API Key test failed!"));
            assertTrue(error.contains("💡 Issue: Network timeout"));
        }
    }

    @Test
    void testOpenAIKey_GenericError() {
        // Given
        ReflectionTestUtils.setField(openAITest, "apiKey", "sk-test");
        ReflectionTestUtils.setField(openAITest, "model", "gpt-4o-mini");

        try (MockedConstruction<OpenAiService> mockedService = mockConstruction(OpenAiService.class,
                (mock, context) -> {
                    when(mock.createChatCompletion(any(ChatCompletionRequest.class)))
                        .thenThrow(new IllegalArgumentException("Something went wrong"));
                })) {

            // When
            openAITest.testOpenAIKey();

            // Then
            String error = errContent.toString();
            assertTrue(error.contains("❌ OpenAI API Key test failed!"));
            assertTrue(error.contains("💡 Issue: IllegalArgumentException"));
        }
    }

    @Test
    void testRestaurantIntentParsing_Success() {
        // Given
        ReflectionTestUtils.setField(openAITest, "apiKey", "sk-test1234567890");
        ReflectionTestUtils.setField(openAITest, "model", "gpt-4o-mini");

        String expectedJson = """
            {
                "cuisine": ["sushi"],
                "party_size": 2,
                "price_range": {"min": 200000, "max": 500000},
                "distance": 5,
                "dietary": []
            }
            """;

        try (MockedConstruction<OpenAiService> mockedService = mockConstruction(OpenAiService.class,
                (mock, context) -> {
                    // Mock the response
                    ChatMessage responseMessage = new ChatMessage();
                    responseMessage.setContent(expectedJson);
                    
                    ChatCompletionChoice choice = new ChatCompletionChoice();
                    choice.setMessage(responseMessage);
                    
                    ChatCompletionResult result = new ChatCompletionResult();
                    result.setChoices(List.of(choice));
                    
                    when(mock.createChatCompletion(any(ChatCompletionRequest.class)))
                        .thenReturn(result);
                })) {

            // When
            openAITest.testRestaurantIntentParsing();

            // Then
            String output = outContent.toString();
            assertTrue(output.contains("🍽️ Testing Restaurant Intent Parsing..."));
            assertTrue(output.contains("📡 Testing intent parsing..."));
            assertTrue(output.contains("✅ Intent parsing test successful!"));
            assertTrue(output.contains("cuisine"));
        }
    }

    @Test
    void testRestaurantIntentParsing_Failure() {
        // Given
        ReflectionTestUtils.setField(openAITest, "apiKey", "sk-test");
        ReflectionTestUtils.setField(openAITest, "model", "gpt-4o-mini");

        try (MockedConstruction<OpenAiService> mockedService = mockConstruction(OpenAiService.class,
                (mock, context) -> {
                    when(mock.createChatCompletion(any(ChatCompletionRequest.class)))
                        .thenThrow(new RuntimeException("API Error"));
                })) {

            // When
            openAITest.testRestaurantIntentParsing();

            // Then
            String error = errContent.toString();
            assertTrue(error.contains("❌ Intent parsing test failed!"));
            assertTrue(error.contains("Error: API Error"));
        }
    }

    @Test
    void testRestaurantIntentParsing_NullPointerException() {
        // Given
        ReflectionTestUtils.setField(openAITest, "apiKey", "sk-test");
        ReflectionTestUtils.setField(openAITest, "model", "gpt-4o-mini");

        try (MockedConstruction<OpenAiService> mockedService = mockConstruction(OpenAiService.class,
                (mock, context) -> {
                    when(mock.createChatCompletion(any(ChatCompletionRequest.class)))
                        .thenThrow(new NullPointerException("Null value encountered"));
                })) {

            // When
            openAITest.testRestaurantIntentParsing();

            // Then
            String error = errContent.toString();
            assertTrue(error.contains("❌ Intent parsing test failed!"));
        }
    }

    @Test
    void testApiKeyMasking_LongApiKey() {
        // Given
        String longApiKey = "sk-1234567890abcdefghijklmnopqrstuvwxyz";
        ReflectionTestUtils.setField(openAITest, "apiKey", longApiKey);
        ReflectionTestUtils.setField(openAITest, "model", "gpt-4o-mini");

        // When
        openAITest.testOpenAIKey();

        // Then
        String output = outContent.toString();
        assertTrue(output.contains("API Key: sk-1234567..."));
        assertFalse(output.contains(longApiKey)); // Should not expose full key
    }

    @Test
    void testBothMethods_Sequential() {
        // Given
        ReflectionTestUtils.setField(openAITest, "apiKey", "sk-test1234567890");
        ReflectionTestUtils.setField(openAITest, "model", "gpt-4o-mini");

        try (MockedConstruction<OpenAiService> mockedService = mockConstruction(OpenAiService.class,
                (mock, context) -> {
                    ChatMessage responseMessage = new ChatMessage();
                    responseMessage.setContent("Test response");
                    
                    ChatCompletionChoice choice = new ChatCompletionChoice();
                    choice.setMessage(responseMessage);
                    
                    ChatCompletionResult result = new ChatCompletionResult();
                    result.setChoices(List.of(choice));
                    
                    when(mock.createChatCompletion(any(ChatCompletionRequest.class)))
                        .thenReturn(result);
                })) {

            // When
            openAITest.testOpenAIKey();
            openAITest.testRestaurantIntentParsing();

            // Then
            String output = outContent.toString();
            assertTrue(output.contains("🔑 Testing OpenAI API Key..."));
            assertTrue(output.contains("🍽️ Testing Restaurant Intent Parsing..."));
            assertTrue(output.contains("✅ OpenAI API Key is working!"));
            assertTrue(output.contains("✅ Intent parsing test successful!"));
        }
    }
}

