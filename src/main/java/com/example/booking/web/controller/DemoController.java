package com.example.booking.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Demo controller để xem các mẫu thiết kế
 */
@Controller
public class DemoController {
    
    /**
     * Demo trang home designs
     */
    @GetMapping("/demo/home-demo")
    public String homeDemo() {
        return "demo/home-demo";
    }
    
    /**
     * Demo trang home theo mẫu chonweb.vn
     */
    @GetMapping("/demo/restaurant-home-demo")
    public String restaurantHomeDemo() {
        return "demo/restaurant-home-demo";
    }
    
    /**
     * Demo trang home theo phong cách Resy.com
     */
    @GetMapping("/demo/resy-style-demo")
    public String resyStyleDemo() {
        return "demo/resy-style-demo";
    }
}
