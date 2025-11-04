package com.example.booking.web.controller;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.example.booking.exception.GlobalExceptionHandler;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        redirectAttributes = new RedirectAttributesModelMap();
        
        // Setup request context for redirect
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    // TC GE-001
    void shouldRedirectWithErrorMessage_whenHandlingIllegalArgumentException() {
        // Given
        IllegalArgumentException ex = new IllegalArgumentException("Invalid input provided");
        
        // When
        String result = handler.handleIllegalArgumentException(ex, null, redirectAttributes);
        
        // Then
        assert result.equals("redirect:/");
        assert redirectAttributes.getFlashAttributes().containsKey("errorMessage");
        assert redirectAttributes.getFlashAttributes().get("errorMessage").equals("Invalid input provided");
    }

    @Test
    // TC GE-002
    void shouldSetErrorMessageInFlash_whenHandlingIllegalArgumentException() {
        // Given
        IllegalArgumentException ex = new IllegalArgumentException("Invalid input");
        
        // When
        handler.handleIllegalArgumentException(ex, null, redirectAttributes);
        
        // Then
        assert redirectAttributes.getFlashAttributes().get("errorMessage").equals("Invalid input");
    }

    @Test
    // TC GE-003
    void shouldRedirectWithPrefixedMessage_whenHandlingGenericException() {
        // Given
        RuntimeException ex = new RuntimeException("Database connection failed");
        
        // When
        String result = handler.handleGenericException(ex, null, redirectAttributes);
        
        // Then
        assert result.equals("redirect:/");
        assert redirectAttributes.getFlashAttributes().containsKey("errorMessage");
        String message = (String) redirectAttributes.getFlashAttributes().get("errorMessage");
        assert message.startsWith("Đã xảy ra lỗi: ");
    }

    @Test
    // TC GE-004
    void shouldHandleNullPointerException_whenHandlingGenericException() {
        // Given
        NullPointerException ex = new NullPointerException();
        
        // When
        handler.handleGenericException(ex, null, redirectAttributes);
        
        // Then
        assert redirectAttributes.getFlashAttributes().containsKey("errorMessage");
        String message = (String) redirectAttributes.getFlashAttributes().get("errorMessage");
        assert message.contains("Đã xảy ra lỗi: ");
    }
}

