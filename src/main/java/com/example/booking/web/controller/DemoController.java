package com.example.booking.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Demo controller để xem các mẫu thiết kế (backup)
 */
@Controller
public class DemoController {
    
    /**
     * Demo trang home designs (backup)
     */
    @GetMapping("/demo/home-demo")
    public String homeDemo() {
        return "backup/home-demo";
    }
    
    /**
     * Demo trang home theo mẫu chonweb.vn (backup)
     */
    @GetMapping("/demo/restaurant-home-demo")
    public String restaurantHomeDemo() {
        return "backup/restaurant-home-demo";
    }
    
    /**
     * Demo trang home theo phong cách Resy.com (backup)
     */
    @GetMapping("/demo/resy-style-demo")
    public String resyStyleDemo() {
        return "backup/resy-style-demo";
    }
}
