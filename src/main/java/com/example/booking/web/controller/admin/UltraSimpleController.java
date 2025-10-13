package com.example.booking.web.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Ultra simple test controller
 */
// @Controller
// @RequestMapping("/admin/rate-limiting")
public class UltraSimpleController {
    
    @GetMapping
    @ResponseBody
    public String test() {
        return "Rate Limiting Dashboard - Test OK!";
    }
    
    @GetMapping("/api/statistics")
    @ResponseBody
    public String statistics() {
        return "{\"status\":\"ok\",\"message\":\"Statistics API working\"}";
    }
}
