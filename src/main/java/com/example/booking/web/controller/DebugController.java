package com.example.booking.web.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.booking.domain.User;
import com.example.booking.repository.UserRepository;

@Controller
@RequestMapping("/debug")
public class DebugController {
    
    private final UserRepository userRepository;
    
    public DebugController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @GetMapping("/auth")
    public String debugAuth(Model model, Authentication authentication) {
        System.out.println("🔍 Debug Auth Controller called");
        
        if (authentication != null) {
            System.out.println("🔍 User: " + authentication.getName());
            System.out.println("🔍 Authorities: " + authentication.getAuthorities());
            System.out.println("🔍 Principal: " + authentication.getPrincipal().getClass().getSimpleName());
            System.out.println("🔍 Authenticated: " + authentication.isAuthenticated());
            
            model.addAttribute("user", authentication.getName());
            model.addAttribute("authorities", authentication.getAuthorities());
            model.addAttribute("principal", authentication.getPrincipal().getClass().getSimpleName());
            model.addAttribute("authenticated", authentication.isAuthenticated());
        } else {
            System.out.println("🔍 Authentication is NULL!");
            model.addAttribute("user", "NULL");
            model.addAttribute("authorities", "NULL");
            model.addAttribute("principal", "NULL");
            model.addAttribute("authenticated", false);
        }
        
        return "debug/auth";
    }
    
    @GetMapping("/users")
    public String debugUsers(Model model) {
        System.out.println("🔍 Debug Users Controller called");
        
        List<User> users = userRepository.findAll();
        System.out.println("🔍 Found " + users.size() + " users");
        
        for (User user : users) {
            System.out.println("🔍 User: " + user.getUsername() + 
                             ", Role: " + user.getRole() + 
                             ", Authorities: " + user.getAuthorities() +
                             ", Active: " + user.getActive());
        }
        
        model.addAttribute("users", users);
        return "debug/users";
    }
} 