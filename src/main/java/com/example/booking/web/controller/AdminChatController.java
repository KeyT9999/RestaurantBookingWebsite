package com.example.booking.web.controller;

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.service.ChatService;
import com.example.booking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/chat")
    public String adminChatPage(Authentication authentication, Model model) {
        try {
            // Get current admin user
            User admin = getUserFromAuthentication(authentication);
            
            // Verify admin role
            if (admin.getRole() != UserRole.ADMIN && admin.getRole() != UserRole.admin) {
                return "redirect:/error?message=Access denied. Admin role required.";
            }
            
            // Add admin info to model
            model.addAttribute("admin", admin);
            model.addAttribute("adminId", admin.getId());
            model.addAttribute("adminName", admin.getFullName());
            model.addAttribute("adminEmail", admin.getEmail());
            
            return "admin/chat";
            
        } catch (Exception e) {
            return "redirect:/error?message=Error loading admin chat: " + e.getMessage();
        }
    }

    private User getUserFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("No authentication found");
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof User) {
            return (User) principal;
        }
        
        if (principal instanceof org.springframework.security.authentication.UsernamePasswordAuthenticationToken) {
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken authToken = 
                (org.springframework.security.authentication.UsernamePasswordAuthenticationToken) principal;
            Object authPrincipal = authToken.getPrincipal();
            
            if (authPrincipal instanceof User) {
                return (User) authPrincipal;
            } else {
                String username = authToken.getName();
                return userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found for username: " + username));
            }
        }
        
        throw new RuntimeException("Unsupported authentication type: " + principal.getClass().getName());
    }
}
