package com.example.booking.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller để serve trang test withdrawal system
 */
@Controller
public class TestWithdrawalController {
    
    /**
     * Trang test withdrawal system
     * GET /test-withdrawal
     */
    @GetMapping("/test-withdrawal")
    public String testWithdrawalSystem() {
        return "test-withdrawal-system";
    }
}
