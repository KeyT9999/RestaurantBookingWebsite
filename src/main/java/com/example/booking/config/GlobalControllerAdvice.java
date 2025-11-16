package com.example.booking.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.booking.service.RestaurantManagementService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Global controller advice to provide common model attributes for all controllers
 */
@ControllerAdvice
public class GlobalControllerAdvice {
    
    @Autowired
    private RestaurantManagementService restaurantManagementService;
    
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
    
    /**
     * Provides default filter values for header search dropdown
     * Ensures all pages with customer-header can display the search dropdown correctly
     * 
     * @param request HttpServletRequest to get query parameters from
     * @return default sortBy value or from request parameter
     */
    @ModelAttribute("sortBy")
    public String sortBy(HttpServletRequest request) {
        String sortBy = request.getParameter("sortBy");
        return sortBy != null ? sortBy : "restaurantName";
    }
    
    /**
     * Provides default sort direction for header search dropdown
     * 
     * @param request HttpServletRequest to get query parameters from
     * @return default sortDir value or from request parameter
     */
    @ModelAttribute("sortDir")
    public String sortDir(HttpServletRequest request) {
        String sortDir = request.getParameter("sortDir");
        return sortDir != null ? sortDir : "asc";
    }
    
    /**
     * Provides list of available cuisine types from database
     * Used for populating cuisine type filter dropdowns
     * 
     * @return list of distinct cuisine types from approved restaurants
     */
    @ModelAttribute("availableCuisineTypes")
    public List<String> availableCuisineTypes() {
        try {
            return restaurantManagementService.getAllCuisineTypes();
        } catch (Exception e) {
            // Return empty list if service fails (e.g., during startup)
            return List.of();
        }
    }
} 