package com.example.booking.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.booking.domain.RestaurantProfile;
import com.example.booking.service.RestaurantOwnerService;

@Controller
public class TestController {
    
    @Autowired
    private RestaurantOwnerService restaurantOwnerService;
    
    /**
     * Test page for favorites functionality
     */
    @GetMapping("/test-favorites")
    public String testFavorites(Model model) {
        try {
            List<RestaurantProfile> restaurants = restaurantOwnerService.getAllRestaurants();
            model.addAttribute("restaurants", restaurants);
            return "test-favorites";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading restaurants: " + e.getMessage());
            return "test-favorites";
        }
    }
}
