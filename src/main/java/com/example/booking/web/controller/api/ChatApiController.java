package com.example.booking.web.controller.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.booking.domain.ChatRoom;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.AdminChatDto;
import com.example.booking.dto.ChatMessageDto;
import com.example.booking.dto.ChatRoomDto;
import com.example.booking.dto.RestaurantChatDto;
import com.example.booking.service.ChatService;
import com.example.booking.service.RestaurantManagementService;
import com.example.booking.annotation.RateLimited;
import com.example.booking.service.SimpleUserService;

/**
 * REST API controller for chat functionality
 */
@RestController
@RequestMapping("/api/chat")
public class ChatApiController {
    
    @Autowired
    private ChatService chatService;
    
    @Autowired
    private RestaurantManagementService restaurantService;
    
    @Autowired
    private com.example.booking.service.RestaurantOwnerService restaurantOwnerService;
    
    @Autowired
    private SimpleUserService userService;
    

    /**
     * Get all restaurants available for chat
     */
    @GetMapping("/available-restaurants")
    @RateLimited(value = RateLimited.OperationType.CHAT, message = "Quá nhiều yêu cầu chat. Vui lòng thử lại sau.")
    public ResponseEntity<?> getAvailableRestaurants(Authentication authentication) {
        try {
            User user = getUserFromAuthentication(authentication);
            UserRole userRole = user.getRole();
            
            // Get restaurants based on user role
            List<RestaurantProfile> restaurants;
            switch (userRole) {
                case CUSTOMER:
                case customer:
                case ADMIN:
                case admin:
                    // Customers and admins can chat with any restaurant
                    restaurants = restaurantService.findAllRestaurants();

                    // Ensure AI Restaurant (ID = 37) is included for customers and put it first
                    if (userRole == UserRole.CUSTOMER || userRole == UserRole.customer) {
                        Optional<RestaurantProfile> aiRestaurant = restaurantService.findRestaurantById(37);
                        if (aiRestaurant.isPresent()) {
                            // Remove AI restaurant if it exists in the list
                            restaurants.removeIf(r -> r.getRestaurantId().equals(37));
                            // Add AI restaurant at the beginning of the list
                            restaurants.add(0, aiRestaurant.get());
                        }
                    }
                    break;
                case RESTAURANT_OWNER:
                case restaurant_owner:
                    // Restaurant owners can only chat with customers of their restaurants
                    restaurants = restaurantOwnerService.getRestaurantsByUserId(user.getId());
                    break;
                default:
                    return ResponseEntity.badRequest().body("Invalid user role: " + userRole);
            }
            
            // Convert to DTO to avoid Hibernate proxy issues
            List<RestaurantChatDto> restaurantDtos = restaurants.stream()
                    .map(restaurant -> {
                        // Check if there's an existing room for this restaurant and user
                        String existingRoomId = chatService.getExistingRoomId(user.getId(), userRole,
                                restaurant.getRestaurantId());

                        RestaurantChatDto dto = new RestaurantChatDto(
                                restaurant.getRestaurantId(),
                                restaurant.getRestaurantName(),
                                restaurant.getAddress(),
                                restaurant.getPhone(),
                                true // Default to active, can be enhanced later
                        );
                        dto.setRoomId(existingRoomId); // Set room ID if available

                        // If room exists, get unread count
                        if (existingRoomId != null) {
                            Map<String, Object> unreadData = chatService.getUnreadCountForRoom(existingRoomId,
                                    user.getId());
                            dto.setUnreadCount((Long) unreadData.get("unreadCount"));
                        } else {
                            dto.setUnreadCount(0L);
                        }

                        return dto;
                    })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(restaurantDtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Get available admins for restaurant owner to chat with
     */
    @GetMapping("/available-admins")
    public ResponseEntity<?> getAvailableAdmins(Authentication authentication) {
        try {
            User user = getUserFromAuthentication(authentication);
            UserRole userRole = user.getRole();
            
            // Only restaurant owners can see available admins
            if (userRole != UserRole.RESTAURANT_OWNER && userRole != UserRole.restaurant_owner) {
                return ResponseEntity.status(403).body("Only restaurant owners can view available admins");
            }
            
            List<User> admins = chatService.getAvailableAdmins();
            
            // Convert to DTO with restaurants
            List<AdminChatDto> adminDtos = admins.stream()
                    .map(admin -> {
                        // Get restaurants for this owner using the correct method
                        List<RestaurantProfile> ownerRestaurants = restaurantOwnerService
                                .getRestaurantsByUserId(user.getId());
                        List<com.example.booking.dto.AdminChatDto.RestaurantInfo> restaurantInfos = ownerRestaurants
                                .stream()
                                .map(restaurant -> new com.example.booking.dto.AdminChatDto.RestaurantInfo(
                                        restaurant.getRestaurantId().longValue(),
                                        restaurant.getName()))
                                .collect(Collectors.toList());

                        return new com.example.booking.dto.AdminChatDto(
                                admin.getId(),
                                admin.getFullName(),
                                admin.getEmail(),
                                true, // Default to active
                                restaurantInfos);
                    })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(adminDtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Get chat rooms for current user
     */
    @GetMapping("/rooms")
    @RateLimited(value = RateLimited.OperationType.CHAT, message = "Quá nhiều yêu cầu lấy danh sách chat. Vui lòng thử lại sau.")
    public ResponseEntity<?> getUserChatRooms(Authentication authentication) {
        try {
            User user = getUserFromAuthentication(authentication);
            List<ChatRoomDto> rooms = chatService.getUserChatRooms(user.getId(), user.getRole());
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Create or get existing chat room
     */
    @PostMapping("/rooms")
    @RateLimited(value = RateLimited.OperationType.CHAT, message = "Quá nhiều yêu cầu tạo phòng chat. Vui lòng thử lại sau.")
    public ResponseEntity<?> createChatRoom(@RequestParam Integer restaurantId, 
                                          Authentication authentication) {
        try {
            User user = getUserFromAuthentication(authentication);
            

            // Check if user can chat with this restaurant
            if (!chatService.canUserChatWithRestaurant(user.getId(), user.getRole(), restaurantId)) {
                return ResponseEntity.badRequest().body("Not authorized to chat with this restaurant");
            }
            
            // Create or get room
            String roomId;
            switch (user.getRole()) {
                case CUSTOMER:
                case customer:
                    chatService.createCustomerRestaurantRoom(user.getId(), restaurantId);
                    roomId = chatService.getRoomId(user.getId(), user.getRole(), restaurantId);
                    break;
                case ADMIN:
                case admin:
                    chatService.createAdminRestaurantRoom(user.getId(), restaurantId);
                    roomId = chatService.getRoomId(user.getId(), user.getRole(), restaurantId);
                    break;
                case RESTAURANT_OWNER:
                case restaurant_owner:
                    // For restaurant owners, we need to find existing rooms
                    roomId = chatService.getRoomId(user.getId(), user.getRole(), restaurantId);
                    break;
                default:
                    return ResponseEntity.badRequest().body("Invalid user role: " + user.getRole());
            }
            
            return ResponseEntity.ok(new CreateRoomResponse(roomId, restaurantId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Create chat room with admin (for restaurant owners)
     */
    @PostMapping("/rooms/admin")
    public ResponseEntity<?> createChatRoomWithAdmin(@RequestParam UUID adminId,
            @RequestParam(required = false) Long restaurantId,
                                                   Authentication authentication) {
        try {
            User user = getUserFromAuthentication(authentication);
            
            // Only restaurant owners can create chat with admin
            if (user.getRole() != UserRole.RESTAURANT_OWNER && user.getRole() != UserRole.restaurant_owner) {
                return ResponseEntity.status(403).body("Only restaurant owners can chat with admin");
            }
            
            // Create chat room with admin
            ChatRoom room = chatService.createRestaurantOwnerAdminRoom(user.getId(), adminId, restaurantId);
            
            return ResponseEntity.ok(new CreateRoomResponse(room.getRoomId(), room.getRestaurant().getRestaurantId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Get message history for a room
     */
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<?> getMessages(@PathVariable String roomId,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "50") int size,
                                       Authentication authentication) {
        try {
            User user = getUserFromAuthentication(authentication);
            
            // Validate user can access this room
            if (!chatService.canUserAccessRoom(roomId, user.getId(), user.getRole())) {
                return ResponseEntity.status(403).body("Not authorized to access this room");
            }
            
            List<ChatMessageDto> messages = chatService.getMessages(roomId, page, size);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Mark messages as read
     */
    @PostMapping("/rooms/{roomId}/read")
    public ResponseEntity<?> markMessagesAsRead(@PathVariable String roomId,
                                              Authentication authentication) {
        try {
            User user = getUserFromAuthentication(authentication);
            chatService.markMessagesAsRead(roomId, user.getId());
            return ResponseEntity.ok("Messages marked as read");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Get unread message count for current user
     */
    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(Authentication authentication) {
        try {
            User user = getUserFromAuthentication(authentication);
            long unreadCount = chatService.getUnreadMessageCount(user.getId());
            return ResponseEntity.ok(new UnreadCountResponse(unreadCount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Get unread message count for a specific room
     */
    @GetMapping("/rooms/{roomId}/unread-count")
    public ResponseEntity<?> getUnreadCountForRoom(@PathVariable String roomId,
                                                  Authentication authentication) {
        try {
            User user = getUserFromAuthentication(authentication);
            
            // Validate user can access this room
            if (!chatService.canUserAccessRoom(roomId, user.getId(), user.getRole())) {
                return ResponseEntity.status(403).body("Not authorized to access this room");
            }
            
            long unreadCount = chatService.getUnreadMessageCountForRoom(roomId, user.getId());
            return ResponseEntity.ok(new UnreadCountResponse(unreadCount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Archive a chat room
     */
    @PostMapping("/rooms/{roomId}/archive")
    public ResponseEntity<?> archiveChatRoom(@PathVariable String roomId,
                                           Authentication authentication) {
        try {
            User user = getUserFromAuthentication(authentication);
            
            // Validate user can access this room
            if (!chatService.canUserAccessRoom(roomId, user.getId(), user.getRole())) {
                return ResponseEntity.status(403).body("Not authorized to access this room");
            }
            
            chatService.archiveChatRoom(roomId);
            return ResponseEntity.ok("Room archived successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Get chat statistics (admin only)
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getChatStatistics(Authentication authentication) {
        try {
            User user = getUserFromAuthentication(authentication);
            
            // Only admins can view statistics
            if (user.getRole() != UserRole.ADMIN) {
                return ResponseEntity.status(403).body("Only admins can view statistics");
            }
            
            Map<String, Object> stats = chatService.getChatStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Response DTOs
    public static class CreateRoomResponse {
        private String roomId;
        private Integer restaurantId;
        
        public CreateRoomResponse(String roomId, Integer restaurantId) {
            this.roomId = roomId;
            this.restaurantId = restaurantId;
        }
        
        public String getRoomId() { return roomId; }
        public Integer getRestaurantId() { return restaurantId; }
    }
    
    public static class UnreadCountResponse {
        private long unreadCount;
        
        public UnreadCountResponse(long unreadCount) {
            this.unreadCount = unreadCount;
        }
        
        public long getUnreadCount() { return unreadCount; }
    }

    /**
     * Create chat room between admin and restaurant
     */
    @PostMapping("/rooms/restaurant")
    public ResponseEntity<?> createChatRoomWithRestaurant(@RequestParam Integer restaurantId,
            Authentication authentication) {
        try {
            System.out.println("=== Creating Admin-Restaurant Chat Room ===");
            System.out.println("Restaurant ID: " + restaurantId);

            User user = getUserFromAuthentication(authentication);
            System.out.println("User: " + user.getUsername() + " (Role: " + user.getRole() + ")");

            if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.admin) {
                System.err.println("ERROR: User is not admin");
                return ResponseEntity.status(403).body("Only admins can chat with restaurants");
            }

            System.out.println("Creating chat room...");
            ChatRoom room = chatService.createAdminRestaurantRoom(user.getId(), restaurantId);
            System.out.println("Room created successfully: " + room.getRoomId());

            return ResponseEntity.ok(new CreateRoomResponse(room.getRoomId(), room.getRestaurant().getRestaurantId()));
        } catch (Exception e) {
            System.err.println("ERROR creating admin-restaurant room: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
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