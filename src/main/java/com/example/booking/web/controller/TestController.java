package com.example.booking.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {
    
    @GetMapping("/test-simple")
    public String testSimple(Model model) {
        model.addAttribute("message", "Test successful!");
        return "test-simple";
    }
}