package com.example.booking.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestRateLimitingConfig {
    @Bean
    public AdvancedRateLimitingInterceptor advancedRateLimitingInterceptor() {
        return Mockito.mock(AdvancedRateLimitingInterceptor.class);
    }

    @Bean
    public AuthRateLimitFilter authRateLimitFilter() {
        return Mockito.mock(AuthRateLimitFilter.class);
    }

    @Bean
    public GeneralRateLimitFilter generalRateLimitFilter() {
        return Mockito.mock(GeneralRateLimitFilter.class);
    }

    @Bean
    public LoginRateLimitFilter loginRateLimitFilter() {
        return Mockito.mock(LoginRateLimitFilter.class);
    }

    @Bean
    public com.example.booking.service.EndpointRateLimitingService endpointRateLimitingService() {
        return Mockito.mock(com.example.booking.service.EndpointRateLimitingService.class);
    }

    @Bean
    public com.example.booking.service.AuthRateLimitingService authRateLimitingService() {
        return Mockito.mock(com.example.booking.service.AuthRateLimitingService.class);
    }

    @Bean
    public com.example.booking.service.GeneralRateLimitingService generalRateLimitingService() {
        return Mockito.mock(com.example.booking.service.GeneralRateLimitingService.class);
    }

    @Bean
    public com.example.booking.service.LoginRateLimitingService loginRateLimitingService() {
        return Mockito.mock(com.example.booking.service.LoginRateLimitingService.class);
    }

    @Bean
    public com.example.booking.service.DatabaseRateLimitingService databaseRateLimitingService() {
        return Mockito.mock(com.example.booking.service.DatabaseRateLimitingService.class);
    }
}
