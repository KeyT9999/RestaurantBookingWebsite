package com.example.booking.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller để serve trang tạo test withdrawal request
 */
@Controller
public class CreateTestWithdrawalController {
    
    /**
     * Trang tạo test withdrawal request
     * GET /create-test-withdrawal
     */
    @GetMapping("/create-test-withdrawal")
    public String createTestWithdrawal() {
        return "create-test-withdrawal";
    }
}
