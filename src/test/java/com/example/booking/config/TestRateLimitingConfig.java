package com.example.booking.config;

import java.util.Locale;
import java.util.Map;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.AbstractView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@TestConfiguration
public class TestRateLimitingConfig {
    @Bean
    public AdvancedRateLimitingInterceptor advancedRateLimitingInterceptor() {
        AdvancedRateLimitingInterceptor interceptor = Mockito.mock(AdvancedRateLimitingInterceptor.class);
        return interceptor;
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
    public PermanentlyBlockedIpFilter permanentlyBlockedIpFilter() {
        return Mockito.mock(PermanentlyBlockedIpFilter.class);
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
        com.example.booking.service.GeneralRateLimitingService mock = Mockito.mock(com.example.booking.service.GeneralRateLimitingService.class);
        Mockito.when(mock.isBookingAllowed(Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(mock.isChatAllowed(Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(mock.isReviewAllowed(Mockito.any(), Mockito.any())).thenReturn(true);
        return mock;
    }

    @Bean
    public com.example.booking.service.LoginRateLimitingService loginRateLimitingService() {
        return Mockito.mock(com.example.booking.service.LoginRateLimitingService.class);
    }

    @Bean
    public com.example.booking.service.DatabaseRateLimitingService databaseRateLimitingService() {
        return Mockito.mock(com.example.booking.service.DatabaseRateLimitingService.class);
    }

    @Bean
    public com.example.booking.service.NotificationService notificationService() {
        com.example.booking.service.NotificationService mock = Mockito.mock(com.example.booking.service.NotificationService.class);
        Mockito.when(mock.countUnreadByUserId(Mockito.any())).thenReturn(0L);
        Mockito.when(mock.getLatestNotifications(Mockito.any())).thenReturn(java.util.Collections.emptyList());
        return mock;
    }

    @Bean
    public com.example.booking.service.AdvancedRateLimitingService advancedRateLimitingService() {
        com.example.booking.service.AdvancedRateLimitingService mock = Mockito.mock(com.example.booking.service.AdvancedRateLimitingService.class);
        Mockito.when(mock.isRequestAllowed(Mockito.any(), Mockito.any(), Mockito.anyString())).thenReturn(true);
        Mockito.when(mock.getRateLimitStats(Mockito.anyString())).thenReturn(java.util.Collections.emptyMap());
        return mock;
    }

    @Bean
    public com.example.booking.service.RateLimitingMonitoringService rateLimitingMonitoringService() {
        return Mockito.mock(com.example.booking.service.RateLimitingMonitoringService.class);
    }

    @Bean
    public com.example.booking.repository.RateLimitStatisticsRepository rateLimitStatisticsRepository() {
        return Mockito.mock(com.example.booking.repository.RateLimitStatisticsRepository.class);
    }

    @Bean
    public com.example.booking.repository.RateLimitBlockRepository rateLimitBlockRepository() {
        return Mockito.mock(com.example.booking.repository.RateLimitBlockRepository.class);
    }

    @Bean
    public com.example.booking.repository.RateLimitAlertRepository rateLimitAlertRepository() {
        return Mockito.mock(com.example.booking.repository.RateLimitAlertRepository.class);
    }

    @Bean
    public com.example.booking.repository.BlockedIpRepository blockedIpRepository() {
        return Mockito.mock(com.example.booking.repository.BlockedIpRepository.class);
    }

    @Bean
    @Primary
    public ViewResolver mockViewResolver() {
        return (viewName, locale) -> new AbstractView() {
            @Override
            protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
                    HttpServletResponse response) throws Exception {
                // no-op to bypass template rendering
            }
        };
    }
}
