package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class EndpointRateLimitingServiceTest {

    @Mock
    private AdvancedRateLimitingService advancedRateLimitingService;

    @InjectMocks
    private EndpointRateLimitingService endpointRateLimitingService;

    @Test
    void isLoginAllowed_shouldDelegateWithLoginKey() {
        MockHttpServletRequest request = buildRequest("/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(advancedRateLimitingService.isRequestAllowed(request, response, "login")).thenReturn(true);

        assertTrue(endpointRateLimitingService.isLoginAllowed(request, response));
        verify(advancedRateLimitingService).isRequestAllowed(request, response, "login");
    }

    @Test
    void isBookingAllowed_shouldDelegateWithBookingKey() {
        MockHttpServletRequest request = buildRequest("/booking/create");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(advancedRateLimitingService.isRequestAllowed(request, response, "booking")).thenReturn(true);

        assertTrue(endpointRateLimitingService.isBookingAllowed(request, response));
        verify(advancedRateLimitingService).isRequestAllowed(request, response, "booking");
    }

    @Test
    void isAnalyticsAllowed_whenAdvancedServiceBlocks_shouldReturnFalse() {
        MockHttpServletRequest request = buildRequest("/admin/analytics");
        request.addHeader("X-Forwarded-For", "203.0.113.10, 10.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(advancedRateLimitingService.isRequestAllowed(request, response, "analytics")).thenReturn(false);

        assertFalse(endpointRateLimitingService.isAnalyticsAllowed(request, response));
        verify(advancedRateLimitingService).isRequestAllowed(request, response, "analytics");
    }

    private MockHttpServletRequest buildRequest(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(uri);
        request.setRemoteAddr("192.168.1.1");
        request.addHeader("User-Agent", "JUnit");
        return request;
    }
}
