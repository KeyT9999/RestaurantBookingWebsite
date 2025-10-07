package com.example.booking.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.booking.domain.User;
import com.example.booking.dto.ChatRoomDto;
import com.example.booking.service.ChatService;
import com.example.booking.service.SimpleUserService;

/**
 * Controller for restaurant owner chat functionality
 */
@Controller
@RequestMapping("/restaurant-owner/chat")
public class RestaurantOwnerChatController {
    
    @Autowired
    private ChatService chatService;
    
    @Autowired
    private SimpleUserService userService;
    
    /**
     * Main chat page for restaurant owner
     */
    @GetMapping
    public String chatPage(Authentication authentication, Model model) {
        User user = getUserFromAuthentication(authentication);
        
        // Get all chat rooms for this restaurant owner
        List<ChatRoomDto> chatRooms = chatService.getUserChatRooms(user.getId(), user.getRole());
        
        model.addAttribute("chatRooms", chatRooms);
        model.addAttribute("currentUser", user);
        
        return "restaurant-owner/chat";
    }
    
    /**
     * Chat room detail page
     */
    @GetMapping("/room/{roomId}")
    public String chatRoom(@PathVariable String roomId, Authentication authentication, Model model) {
        User user = getUserFromAuthentication(authentication);
        
        // Validate user can access this room
        if (!chatService.canUserAccessRoom(roomId, user.getId(), user.getRole())) {
            return "error/403";
        }
        
        // Get room details
        var roomOpt = chatService.getChatRoomById(roomId);
        if (!roomOpt.isPresent()) {
            return "error/404";
        }
        
        // Get room DTO with participant info
        ChatRoomDto roomDto = chatService.convertToDto(roomOpt.get());

        model.addAttribute("roomId", roomId);
        model.addAttribute("currentUser", user);
        model.addAttribute("room", roomDto);
        model.addAttribute("participantName", roomDto.getParticipantName());
        model.addAttribute("participantRole", roomDto.getParticipantRole());
        model.addAttribute("restaurantName", roomDto.getRestaurantName());
        
        return "restaurant-owner/chat-room";
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
