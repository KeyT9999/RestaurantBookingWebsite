package com.example.booking.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.booking.domain.User;
import com.example.booking.dto.ChatRoomDto;
import com.example.booking.service.ChatService;
import com.example.booking.service.SimpleUserService;

@Controller
@RequestMapping("/customer")
public class CustomerChatController {
    
    @Autowired
    private ChatService chatService;
    
    @Autowired
    private SimpleUserService userService;
    
    @GetMapping("/chat")
    public String chatPage(Authentication authentication, Model model) {
        User user = getUserFromAuthentication(authentication);
        
        // Load chat rooms for customer
        List<ChatRoomDto> chatRooms = chatService.getUserChatRooms(user.getId(), user.getRole());
        
        model.addAttribute("chatRooms", chatRooms);
        model.addAttribute("currentUser", user);
        
        return "customer/chat";
    }
    
    /**
     * Helper method to get User from Authentication (handles both User and OAuth2User)
     */
    private User getUserFromAuthentication(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        // Nếu là User object trực tiếp (regular login)
        if (principal instanceof User) {
            return (User) principal;
        }

        // Nếu là OAuth2User hoặc OidcUser (OAuth2 login)
        if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            String username = authentication.getName(); // username = email cho OAuth users

            // Tìm User thực tế từ database
            try {
                User user = (User) userService.loadUserByUsername(username);
                return user;
            } catch (Exception e) {
                throw new RuntimeException("User not found for OAuth username: " + username +
                        ". Error: " + e.getMessage());
            }
        }

        throw new RuntimeException("Unsupported principal type: " + principal.getClass().getName());
    }
}
