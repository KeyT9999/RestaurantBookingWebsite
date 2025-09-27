package com.example.booking.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Controller for handling home page and static pages
 */
@Controller
public class HomeController {
    
    /**
     * Home page - main landing page
     */
    @GetMapping("/")
    public String home(Model model) {
        // Add any model attributes needed for home page
        model.addAttribute("pageTitle", "Aurelius Fine Dining - Experience Culinary Excellence");
        return "home";
    }
    
    /**
     * About page
     */
    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("pageTitle", "About Us - Aurelius Fine Dining");
        return "about";
    }
    
    /**
     * Contact page  
     */
    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("pageTitle", "Contact Us - Aurelius Fine Dining");
        return "contact";
    }
    
    /**
     * Restaurants listing page
     */
    @GetMapping("/restaurants")
    public String restaurants(Model model) {
        model.addAttribute("pageTitle", "Our Restaurants - Aurelius Fine Dining");
        // TODO: Add restaurants data from service
        return "restaurants";
    }
    
    @GetMapping("/restaurants/{id}")
    public String restaurantDetail(@PathVariable String id, Model model) {
        model.addAttribute("pageTitle", "Restaurant Details - Aurelius Fine Dining");
        model.addAttribute("restaurantId", id);
        // TODO: Add restaurant detail data from service
        // For now, redirect to restaurants list
        return "redirect:/restaurants";
    }
}
