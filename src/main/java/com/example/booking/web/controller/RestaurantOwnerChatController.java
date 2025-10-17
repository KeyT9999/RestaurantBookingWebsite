package com.example.booking.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.booking.domain.User;
import com.example.booking.dto.ChatRoomDto;
import com.example.booking.service.ChatService;
import com.example.booking.service.SimpleUserService;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.domain.Booking;
import com.example.booking.service.BookingService;
import com.example.booking.domain.RestaurantProfile;

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
    
    @Autowired
    private RestaurantOwnerService restaurantOwnerService;

    @Autowired
    private BookingService bookingService;

    /**
     * Main chat page for restaurant owner
     */
    @GetMapping
    public String chatPage(@RequestParam(required = false) Integer bookingId,
            Authentication authentication, Model model) {
        User user = getUserFromAuthentication(authentication);
        
        // Get all chat rooms for this restaurant owner
        List<ChatRoomDto> chatRooms = chatService.getUserChatRooms(user.getId(), user.getRole());
        
        // Get restaurants owned by this user
        List<RestaurantProfile> restaurants = restaurantOwnerService.getRestaurantsByUserId(user.getId());

        // Handle bookingId parameter - create chat room with customer if not exists
        if (bookingId != null) {
            try {
                var bookingOpt = bookingService.findBookingById(bookingId);
                if (bookingOpt.isPresent()) {
                    Booking booking = bookingOpt.get();
                    if (booking.getCustomer() != null) {
                        // Check if booking belongs to one of user's restaurants
                        boolean isOwnerRestaurant = restaurants.stream()
                                .anyMatch(restaurant -> restaurant.getRestaurantId()
                                        .equals(booking.getRestaurant().getRestaurantId()));

                        if (isOwnerRestaurant) {
                            // Check if room already exists
                            String existingRoomId = chatService.getRoomId(booking.getCustomer().getUser().getId(),
                                    booking.getCustomer().getUser().getRole(),
                                    booking.getRestaurant().getRestaurantId());

                            if (existingRoomId == null) {
                                // Create new room only if it doesn't exist
                                chatService.createCustomerRestaurantRoom(booking.getCustomer().getUser().getId(),
                                        booking.getRestaurant().getRestaurantId());
                                System.out.println("✅ Created new chat room for booking " + bookingId);
                            } else {
                                System.out.println("ℹ️ Chat room already exists for booking " + bookingId);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Log error but continue to normal chat page
                System.err.println("Error handling bookingId: " + e.getMessage());
            }

            // Redirect to main chat page without bookingId parameter
            return "redirect:/restaurant-owner/chat";
        }

        model.addAttribute("chatRooms", chatRooms);
        model.addAttribute("currentUser", user);
        model.addAttribute("restaurants", restaurants);

        // If only one restaurant, auto-select it
        if (restaurants.size() == 1) {
            model.addAttribute("autoSelectRestaurant", restaurants.get(0));
        }
        
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
