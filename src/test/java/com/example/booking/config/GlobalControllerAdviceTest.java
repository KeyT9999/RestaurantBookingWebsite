package com.example.booking.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Unit tests for GlobalControllerAdvice
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalControllerAdvice Tests")
public class GlobalControllerAdviceTest {

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GlobalControllerAdvice advice;

    @BeforeEach
    void setUp() {
        // Setup default request mocks
        when(request.getRequestURI()).thenReturn("/test/path");
        when(request.getContextPath()).thenReturn("/app");
        when(request.getServerName()).thenReturn("localhost");
        when(request.getServerPort()).thenReturn(8080);
    }

    // ========== currentPath() Tests ==========

    @Test
    @DisplayName("shouldReturnCurrentPath_successfully")
    void shouldReturnCurrentPath_successfully() {
        // Given
        String expectedPath = "/test/path";
        when(request.getRequestURI()).thenReturn(expectedPath);

        // When
        String result = advice.currentPath(request);

        // Then
        assertEquals(expectedPath, result);
        verify(request, times(1)).getRequestURI();
    }

    @Test
    @DisplayName("shouldReturnRootPath_whenRequestURIIsRoot")
    void shouldReturnRootPath_whenRequestURIIsRoot() {
        // Given
        when(request.getRequestURI()).thenReturn("/");

        // When
        String result = advice.currentPath(request);

        // Then
        assertEquals("/", result);
    }

    @Test
    @DisplayName("shouldReturnNestedPath_whenRequestURIHasMultipleSegments")
    void shouldReturnNestedPath_whenRequestURIHasMultipleSegments() {
        // Given
        when(request.getRequestURI()).thenReturn("/admin/users/edit/123");

        // When
        String result = advice.currentPath(request);

        // Then
        assertEquals("/admin/users/edit/123", result);
    }

    // ========== contextPath() Tests ==========

    @Test
    @DisplayName("shouldReturnContextPath_successfully")
    void shouldReturnContextPath_successfully() {
        // Given
        String expectedContextPath = "/app";
        when(request.getContextPath()).thenReturn(expectedContextPath);

        // When
        String result = advice.contextPath(request);

        // Then
        assertEquals(expectedContextPath, result);
        verify(request, times(1)).getContextPath();
    }

    @Test
    @DisplayName("shouldReturnEmptyContextPath_whenNoContextPath")
    void shouldReturnEmptyContextPath_whenNoContextPath() {
        // Given
        when(request.getContextPath()).thenReturn("");

        // When
        String result = advice.contextPath(request);

        // Then
        assertEquals("", result);
    }

    // ========== serverInfo() Tests ==========

    @Test
    @DisplayName("shouldReturnServerInfo_successfully")
    void shouldReturnServerInfo_successfully() {
        // Given
        when(request.getServerName()).thenReturn("localhost");
        when(request.getServerPort()).thenReturn(8080);

        // When
        String result = advice.serverInfo(request);

        // Then
        assertEquals("localhost:8080", result);
        verify(request, times(1)).getServerName();
        verify(request, times(1)).getServerPort();
    }

    @Test
    @DisplayName("shouldReturnServerInfo_withDifferentPort")
    void shouldReturnServerInfo_withDifferentPort() {
        // Given
        when(request.getServerName()).thenReturn("example.com");
        when(request.getServerPort()).thenReturn(443);

        // When
        String result = advice.serverInfo(request);

        // Then
        assertEquals("example.com:443", result);
    }

    @Test
    @DisplayName("shouldReturnServerInfo_withDefaultPort")
    void shouldReturnServerInfo_withDefaultPort() {
        // Given
        when(request.getServerName()).thenReturn("localhost");
        when(request.getServerPort()).thenReturn(80);

        // When
        String result = advice.serverInfo(request);

        // Then
        assertEquals("localhost:80", result);
    }

    // ========== Annotation Tests ==========

    @Test
    @DisplayName("shouldBeControllerAdvice")
    void shouldBeControllerAdvice() {
        // Then
        assertTrue(advice.getClass().isAnnotationPresent(org.springframework.web.bind.annotation.ControllerAdvice.class));
    }
}

