package com.example.booking.web;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException ex, 
                                                Model model, 
                                                RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/booking/my";
    }
    
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, 
                                       Model model, 
                                       RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi: " + ex.getMessage());
        return "redirect:/booking/my";
    }
} 