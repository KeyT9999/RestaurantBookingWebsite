package com.example.booking.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for AI sync webhook client.
 */
@Configuration
@EnableConfigurationProperties(AiSyncProperties.class)
public class AiSyncConfig {

    /**
     * Dedicated RestTemplate for AI sync webhook with short timeouts.
     */
    @Bean
    @Qualifier("aiSyncRestTemplate")
    public RestTemplate aiSyncRestTemplate(AiSyncProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        int timeout = properties.getTimeoutMs() != null ? properties.getTimeoutMs() : 2000;
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        return new RestTemplate(factory);
    }
}
