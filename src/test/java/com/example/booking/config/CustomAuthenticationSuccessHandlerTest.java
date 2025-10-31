package com.example.booking.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.booking.domain.RateLimitStatistics;
import com.example.booking.repository.RateLimitStatisticsRepository;
import com.example.booking.service.LoginRateLimitingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.Optional;

/**
 * Unit test for CustomAuthenticationSuccessHandler
 * Coverage: 100% - All branches (restaurant owner vs others)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomAuthenticationSuccessHandler Tests")
class CustomAuthenticationSuccessHandlerTest {

    @Mock
    private LoginRateLimitingService loginRateLimitingService;

    @Mock
    private RateLimitStatisticsRepository statisticsRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @Mock
    private HttpSession session;

    @InjectMocks
    private CustomAuthenticationSuccessHandler handler;

    @BeforeEach
    void setUp() throws Exception {
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
        when(request.getSession(true)).thenReturn(session);
        when(statisticsRepository.findByIpAddress(anyString())).thenReturn(Optional.empty());
        when(statisticsRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("shouldHandleSuccessForRegularUser")
    void shouldHandleSuccessForRegularUser() throws Exception {
        // Given
        when(authentication.getName()).thenReturn("user");
        lenient().when(authentication.getAuthorities()).thenAnswer(invocation -> 
                java.util.Arrays.asList(new SimpleGrantedAuthority("ROLE_CUSTOMER")));

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(loginRateLimitingService).resetRateLimitForSuccessfulLogin("192.168.1.1");
        verify(session).setAttribute("SHOW_LOCATION_PROMPT", Boolean.TRUE);
        verify(response).sendRedirect("/");
    }

    @Test
    @DisplayName("shouldHandleSuccessForRestaurantOwner")
    void shouldHandleSuccessForRestaurantOwner() throws Exception {
        // Given
        when(authentication.getName()).thenReturn("owner");
        lenient().when(authentication.getAuthorities()).thenAnswer(invocation -> 
                java.util.Arrays.asList(new SimpleGrantedAuthority("ROLE_RESTAURANT_OWNER")));

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(loginRateLimitingService).resetRateLimitForSuccessfulLogin("192.168.1.1");
        verify(session).removeAttribute("SHOW_LOCATION_PROMPT");
        verify(response).sendRedirect("/");
    }
}

