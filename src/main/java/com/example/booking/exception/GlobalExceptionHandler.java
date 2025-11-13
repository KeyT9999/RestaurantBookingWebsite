package com.example.booking.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException ex, 
                                                Model model, 
                                                RedirectAttributes redirectAttributes,
                                                HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        System.err.println("❌ GlobalExceptionHandler caught IllegalArgumentException: " + ex.getMessage());
        System.err.println("   Request URI: " + requestUri);
        ex.printStackTrace();
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        
        // Don't redirect to home if we're on auth pages - preserve the current page context
        if (requestUri != null && requestUri.startsWith("/auth/")) {
            System.err.println("   ⚠️ Exception on auth page: " + requestUri);
            System.err.println("   ⚠️ This might be causing the redirect issue - check logs above");
            // For auth pages, try to return to the same page with error instead of redirecting
            if (requestUri.contains("register")) {
                return "auth/register"; // Return to register form with error
            }
            // For other auth pages, redirect to login with error
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/login";
        }
        
        return "redirect:/"; // Redirect về trang chủ thay vì /booking/my
    }
    
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, 
                                       Model model, 
                                       RedirectAttributes redirectAttributes,
                                       HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        System.err.println("❌ GlobalExceptionHandler caught Exception: " + ex.getMessage());
        System.err.println("   Request URI: " + requestUri);
        System.err.println("   Exception type: " + ex.getClass().getName());
        ex.printStackTrace();
        redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi: " + ex.getMessage());
        
        // Don't redirect to home if we're on auth pages - preserve the current page context
        if (requestUri != null && requestUri.startsWith("/auth/")) {
            System.err.println("   ⚠️ Exception on auth page: " + requestUri);
            System.err.println("   ⚠️ This might be causing the redirect issue - check logs above");
            // For auth pages, try to return to the same page with error instead of redirecting
            if (requestUri.contains("register")) {
                model.addAttribute("errorMessage", "Đã xảy ra lỗi: " + ex.getMessage());
                return "auth/register"; // Return to register form with error
            }
            // For other auth pages, redirect to login with error
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi: " + ex.getMessage());
            return "redirect:/login";
        }
        
        return "redirect:/"; // Redirect về trang chủ thay vì /booking/my
    }
}
