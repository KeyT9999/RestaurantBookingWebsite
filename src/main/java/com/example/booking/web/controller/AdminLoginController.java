package com.example.booking.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller để serve trang admin login
 */
@Controller
public class AdminLoginController {
    
    /**
     * Trang đăng nhập admin
     * GET /admin-login
     */
    @GetMapping("/admin-login")
    public String adminLogin() {
        return "admin-login";
    }
}
