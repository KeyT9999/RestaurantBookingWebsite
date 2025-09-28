package com.example.booking.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.booking.domain.User;
import com.example.booking.repository.UserRepository;

@Controller
@RequestMapping("/test")
public class TestController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users")
    public String testUsers(Model model) {
        try {
            // Test database connection
            long userCount = userRepository.count();
            System.out.println("Total users in database: " + userCount);
            
            // Get all users
            var users = userRepository.findAll();
            System.out.println("Users found: " + users.size());
            
            // Print each user
            for (User user : users) {
                System.out.println("User: " + user.getUsername() + " - " + user.getEmail() + " - " + user.getRole());
            }
            
            model.addAttribute("userCount", userCount);
            model.addAttribute("users", users);
            model.addAttribute("message", "Database connection successful!");
            
        } catch (Exception e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Database error: " + e.getMessage());
        }
        
        return "test/users";
    }
    
    @GetMapping("/db")
    public String testDatabase(Model model) {
        try {
            // Simple database test
            long count = userRepository.count();
            model.addAttribute("message", "Database connected! User count: " + count);
            return "test/db";
        } catch (Exception e) {
            model.addAttribute("error", "Database error: " + e.getMessage());
            return "test/db";
        }
    }
} 