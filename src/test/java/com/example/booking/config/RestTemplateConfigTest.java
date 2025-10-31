package com.example.booking.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

class RestTemplateConfigTest {

    private final RestTemplateConfig config = new RestTemplateConfig();

    @Test
    @DisplayName("restTemplate bean should configure timeouts and request factory")
    void shouldConfigureRestTemplate() {
        RestTemplate restTemplate = config.restTemplate();
        assertThat(restTemplate.getRequestFactory()).isInstanceOf(SimpleClientHttpRequestFactory.class);
        SimpleClientHttpRequestFactory factory = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
        
        // Access private fields using ReflectionTestUtils
        Object connectTimeout = ReflectionTestUtils.getField(factory, "connectTimeout");
        Object readTimeout = ReflectionTestUtils.getField(factory, "readTimeout");
        
        assertThat(connectTimeout).isEqualTo(30_000);
        assertThat(readTimeout).isEqualTo(30_000);
    }
}
