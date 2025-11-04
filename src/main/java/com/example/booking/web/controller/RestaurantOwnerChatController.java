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
            @RequestParam(required = false) Integer restaurantId,
            Authentication authentication, Model model) {
        User user = getUserFromAuthentication(authentication);
        
        // Get restaurants owned by this user
        List<RestaurantProfile> restaurants = restaurantOwnerService.getRestaurantsByUserId(user.getId());
        
        // Get all chat rooms for this restaurant owner
        List<ChatRoomDto> allChatRooms = chatService.getUserChatRooms(user.getId(), user.getRole());
        
        // Auto-select restaurant with most recent activity if restaurantId is not provided
        Integer selectedRestaurantId = restaurantId;
        if (selectedRestaurantId == null && !allChatRooms.isEmpty()) {
            // Find restaurant with the most recent message
            ChatRoomDto mostRecentRoom = allChatRooms.stream()
                    .filter(room -> room.getLastMessageAt() != null && room.getRestaurantId() != null)
                    .max((r1, r2) -> r1.getLastMessageAt().compareTo(r2.getLastMessageAt()))
                    .orElse(null);
            
            if (mostRecentRoom != null && mostRecentRoom.getRestaurantId() != null) {
                selectedRestaurantId = mostRecentRoom.getRestaurantId();
            } else if (restaurants.size() == 1) {
                // Fallback: if only one restaurant, select it
                selectedRestaurantId = restaurants.get(0).getRestaurantId();
            }
        } else if (selectedRestaurantId == null && restaurants.size() == 1) {
            // If no chat rooms but only one restaurant, select it
            selectedRestaurantId = restaurants.get(0).getRestaurantId();
        }
        
        // Filter chat rooms by restaurant if restaurantId is provided
        final Integer finalRestaurantId = selectedRestaurantId;
        List<ChatRoomDto> chatRooms;
        if (finalRestaurantId != null) {
            chatRooms = allChatRooms.stream()
                    .filter(room -> {
                        // Check if room's restaurant ID matches the selected restaurant
                        Integer roomRestaurantId = room.getRestaurantId();
                        return roomRestaurantId != null && roomRestaurantId.equals(finalRestaurantId);
                    })
                    .collect(java.util.stream.Collectors.toList());
        } else {
            // If still no restaurant selected, show empty list instead of all rooms
            chatRooms = java.util.Collections.emptyList();
        }
        
        // Determine current restaurant
        RestaurantProfile currentRestaurant = null;
        if (finalRestaurantId != null) {
            currentRestaurant = restaurants.stream()
                    .filter(r -> r.getRestaurantId().equals(finalRestaurantId))
                    .findFirst()
                    .orElse(null);
        } else if (restaurants.size() == 1) {
            currentRestaurant = restaurants.get(0);
        }
        
        // Update restaurantId for model
        restaurantId = finalRestaurantId;
        

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

            // Redirect to main chat page with restaurantId if available
            if (restaurantId != null) {
                return "redirect:/restaurant-owner/chat?restaurantId=" + restaurantId;
            } else {
                return "redirect:/restaurant-owner/chat";
            }
        }

        model.addAttribute("chatRooms", chatRooms);
        model.addAttribute("currentUser", user);
        model.addAttribute("restaurants", restaurants);
        model.addAttribute("currentRestaurant", currentRestaurant);
        model.addAttribute("restaurantId", restaurantId);

        // If only one restaurant, auto-select it
        if (restaurants.size() == 1 && currentRestaurant == null) {
            currentRestaurant = restaurants.get(0);
            model.addAttribute("autoSelectRestaurant", currentRestaurant);
            model.addAttribute("currentRestaurant", currentRestaurant);
            model.addAttribute("restaurantId", currentRestaurant.getRestaurantId());
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
