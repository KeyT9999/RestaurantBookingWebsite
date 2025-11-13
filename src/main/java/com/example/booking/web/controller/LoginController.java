package com.example.booking.web.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    
    @GetMapping("/login")
    public String login(Authentication authentication) {
        // Nếu đã đăng nhập, redirect về trang chủ
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/";
        }
        // Nếu chưa đăng nhập, hiển thị trang login
        return "auth/login";
    }
}
