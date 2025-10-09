package com.example.booking.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller để serve trang admin test withdrawal
 */
@Controller
public class AdminTestWithdrawalController {
    
    /**
     * Trang test admin withdrawal
     * GET /admin-test-withdrawal
     */
    @GetMapping("/admin-test-withdrawal")
    public String adminTestWithdrawal() {
        return "admin-test-withdrawal";
    }
}
