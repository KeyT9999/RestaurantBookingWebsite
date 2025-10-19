package com.example.booking.config;

import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * OpenAI Configuration with timeout and retry settings
 */
@Configuration
public class OpenAIConfiguration {
    
    @Value("${ai.openai.api-key}")
    private String apiKey;
    
    @Value("${ai.openai.api-url:https://api.openai.com/v1}")
    private String apiUrl;
    
    @Value("${ai.openai.timeout-ms:800}")
    private int timeoutMs;
    
    @Bean
    public OpenAiService openAiService() {
        return new OpenAiService(apiKey, Duration.ofMillis(timeoutMs));
    }
    
    @Bean
    public RestTemplate openAiRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        // Add timeout configuration if needed
        return restTemplate;
    }
}
