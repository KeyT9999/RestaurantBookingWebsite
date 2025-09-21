package com.example.booking.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Global controller advice to provide common model attributes for all controllers
 */
@ControllerAdvice
public class GlobalControllerAdvice {
    
    /**
     * Provides current request path to all templates
     * This replaces the deprecated #request.requestURI in Spring Boot 3.x
     * 
     * @param request HttpServletRequest to get URI from
     * @return current request URI
     */
    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }
    
    /**
     * Provides current request context path to all templates
     * Useful for building absolute URLs
     * 
     * @param request HttpServletRequest to get context path from
     * @return current context path
     */
    @ModelAttribute("contextPath")
    public String contextPath(HttpServletRequest request) {
        return request.getContextPath();
    }
    
    /**
     * Provides server info to all templates
     * Useful for debugging and footer information
     * 
     * @param request HttpServletRequest to get server info from
     * @return server info string
     */
    @ModelAttribute("serverInfo")
    public String serverInfo(HttpServletRequest request) {
        return request.getServerName() + ":" + request.getServerPort();
    }
} 