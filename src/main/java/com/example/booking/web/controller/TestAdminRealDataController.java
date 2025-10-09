package com.example.booking.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller để serve trang test admin withdrawal với dữ liệu thật
 */
@Controller
@RequestMapping("/test-admin-real-data")
public class TestAdminRealDataController {
    
    /**
     * Hiển thị trang test admin withdrawal với dữ liệu thật
     * GET /test-admin-real-data
     */
    @GetMapping
    public String testAdminRealData() {
        return "test-admin-real-data";
    }
}
