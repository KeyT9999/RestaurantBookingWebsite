package com.example.booking.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.domain.ChatRoom;
import com.example.booking.domain.Customer;
import com.example.booking.domain.Message;
import com.example.booking.domain.MessageType;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.ChatMessageDto;
import com.example.booking.dto.ChatRoomDto;
import com.example.booking.dto.RestaurantChatDto;
import com.example.booking.repository.ChatRoomRepository;
import com.example.booking.repository.CustomerRepository;
import com.example.booking.repository.MessageRepository;
import com.example.booking.repository.RestaurantOwnerRepository;
import com.example.booking.repository.RestaurantProfileRepository;
import com.example.booking.repository.UserRepository;

/**
 * Service for managing chat functionality
 */
@Service
@Transactional
public class ChatService {
    
    @Autowired
    private ChatRoomRepository chatRoomRepository;
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private RestaurantProfileRepository restaurantProfileRepository;
    
    @Autowired
    private RestaurantOwnerRepository restaurantOwnerRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Create chat room between customer and restaurant
     * Automatically creates Customer entity if it doesn't exist
     */
    public ChatRoom createCustomerRestaurantRoom(UUID userId, Integer restaurantId) {
        // Find customer by user ID, create if not exists
        Customer customer = customerRepository.findByUserId(userId).orElse(null);
        
        if (customer == null) {
            // Customer entity doesn't exist, create it automatically
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
            
            // Verify user has CUSTOMER role
            if (user.getRole() != UserRole.CUSTOMER && user.getRole() != UserRole.customer) {
                throw new RuntimeException("User is not a customer: " + userId);
            }
            
            // Create new Customer entity
            customer = new Customer();
            customer.setUser(user);
            customer.setFullName(user.getFullName() != null ? user.getFullName() : user.getUsername());
            customer = customerRepository.save(customer);
        }
        
        // Check if room already exists
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByCustomerAndRestaurant(customer.getUser().getId(), restaurantId);
        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }
        RestaurantProfile restaurant = restaurantProfileRepository.findById(restaurantId)
            .orElseThrow(() -> new RuntimeException("Restaurant not found"));
        
        // Create room ID
        String roomId = generateCustomerRestaurantRoomId(customer.getCustomerId(), restaurantId);
        
        // Create room
        ChatRoom room = new ChatRoom(roomId, customer, restaurant);
        return chatRoomRepository.save(room);
    }
    
    /**
     * Create chat room between admin and restaurant
     */
    public ChatRoom createAdminRestaurantRoom(UUID adminId, Integer restaurantId) {
        // Check if room already exists
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByAdminAndRestaurant(adminId, restaurantId);
        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }
        
        // Get entities
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        RestaurantProfile restaurant = restaurantProfileRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        // Verify restaurant has owner
        if (restaurant.getOwner() == null) {
            throw new RuntimeException("Restaurant has no owner: " + restaurantId);
        }
        
        // Create room ID
        String roomId = generateAdminRestaurantRoomId(adminId, restaurantId);
        
        // Create room
        ChatRoom room = new ChatRoom(roomId, admin, restaurant);
        ChatRoom savedRoom = chatRoomRepository.save(room);

        return savedRoom;
    }
    
    /**
     * Create chat room between restaurant owner and admin
     * This allows restaurant owners to initiate chat with admin
     */
    public ChatRoom createRestaurantOwnerAdminRoom(UUID restaurantOwnerId, UUID adminId, Long restaurantId) {
        // Find restaurant owner by user ID
        RestaurantOwner restaurantOwner = restaurantOwnerRepository.findByUserId(restaurantOwnerId)
            .orElseThrow(() -> new RuntimeException("Restaurant owner not found for user ID: " + restaurantOwnerId));

        // Get admin user
        User admin = userRepository.findById(adminId)
            .orElseThrow(() -> new RuntimeException("Admin not found"));
        
        // Verify admin has ADMIN role
        if (admin.getRole() != UserRole.ADMIN && admin.getRole() != UserRole.admin) {
            throw new RuntimeException("User is not an admin");
        }
        
        // Get restaurants of the owner
        List<RestaurantProfile> ownerRestaurants = restaurantOwner.getRestaurants();

        if (ownerRestaurants == null || ownerRestaurants.isEmpty()) {
            throw new RuntimeException("Restaurant owner has no restaurants");
        }

        RestaurantProfile restaurant;
        if (restaurantId != null) {
            // Find specific restaurant - convert Long to Integer
            Integer restaurantIdInt = restaurantId.intValue();
            restaurant = ownerRestaurants.stream()
                    .filter(r -> r.getRestaurantId().equals(restaurantIdInt))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Restaurant not found or not owned by this owner"));
        } else {
            // Use first restaurant as fallback
            restaurant = ownerRestaurants.get(0);
        }

        // Check if room already exists
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByAdminAndRestaurant(adminId, restaurant.getRestaurantId());
        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }
        
        // Create room ID
        String roomId = generateAdminRestaurantRoomId(adminId, restaurant.getRestaurantId());
        
        // Create room
        ChatRoom room = new ChatRoom(roomId, admin, restaurant);
        ChatRoom savedRoom = chatRoomRepository.save(room);

        return savedRoom;
    }
    
    /**
     * Get chat rooms for a user based on their role
     */
    @Transactional(readOnly = true)
    public List<ChatRoomDto> getUserChatRooms(UUID userId, UserRole role) {
        List<ChatRoom> rooms;
        
        switch (role) {
            case CUSTOMER:
            case customer:
                rooms = chatRoomRepository.findByCustomerId(userId);
                break;
            case RESTAURANT_OWNER:
            case restaurant_owner:
                rooms = chatRoomRepository.findByRestaurantOwnerId(userId);
                break;
            case ADMIN:
            case admin:
                rooms = chatRoomRepository.findByAdminId(userId);
                break;
            default:
                throw new RuntimeException("Invalid user role: " + role);
        }
        
        return rooms.stream().map(room -> convertToDto(room, userId)).collect(Collectors.toList());
    }
    
    /**
     * Send a message
     */
    public Message sendMessage(String roomId, UUID senderId, String content, MessageType messageType) {
        // Get room and sender
        ChatRoom room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Chat room not found"));
        User sender = userRepository.findById(senderId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Validate permission
        if (!canUserSendMessage(room, sender)) {
            throw new RuntimeException("User not authorized to send message in this room");
        }
        
        // Create message
        Message message = new Message(room, sender, content, messageType);

        // Set message data based on room type and sender role
        if (room.isCustomerRestaurantChat()) {
            // Customer-Restaurant Chat: Set both customer and owner
            Customer customer = room.getCustomer();
            if (customer != null) {
                customer.getUser().getFullName(); // Force load
                message.setCustomer(customer);
            }

            if (room.getRestaurant() != null && room.getRestaurant().getOwner() != null) {
                RestaurantOwner owner = room.getRestaurant().getOwner();
                owner.getUser().getFullName(); // Force load
                message.setOwner(owner);
            }
            
        } else if (room.isAdminRestaurantChat()) {
            // Admin-Restaurant Chat: Set owner only
            if (room.getRestaurant() != null && room.getRestaurant().getOwner() != null) {
                RestaurantOwner owner = room.getRestaurant().getOwner();
                owner.getUser().getFullName(); // Force load
                message.setOwner(owner);
            }

            // For admin-restaurant chat, determine customer based on sender role
            if (sender.getRole() == UserRole.CUSTOMER || sender.getRole() == UserRole.customer) {
                // If customer is sending in admin-restaurant room, find the customer
                Customer customer = customerRepository.findByUserId(senderId)
                        .orElseThrow(() -> new RuntimeException("Customer not found for user: " + senderId));
                message.setCustomer(customer);
            } else {
                // Admin or Restaurant Owner sending - find existing customer or use
                // restaurant's customer
                Customer customerForMessage = findCustomerForAdminRestaurantChat(room);
                message.setCustomer(customerForMessage);
            }
        }

        message = messageRepository.save(message);
        
        // Update room's last message time
        room.setLastMessageAt(LocalDateTime.now());
        chatRoomRepository.save(room);
        
        return message;
    }
    
    /**
     * Get message history for a room
     */
    @Transactional(readOnly = true)
    public List<ChatMessageDto> getMessages(String roomId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messagePage = messageRepository.findByRoomIdOrderBySentAtAsc(roomId, pageable);
        
        return messagePage.getContent().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Mark messages as read
     */
    @Transactional
    public int markMessagesAsRead(String roomId, UUID userId) {
        return messageRepository.markMessagesAsReadByRoomIdAndUserId(roomId, userId);
    }
    
    /**
     * Get unread message count for a user
     */
    @Transactional(readOnly = true)
    public long getUnreadMessageCount(UUID userId) {
        return messageRepository.countTotalUnreadMessagesByUserId(userId);
    }
    
    /**
     * Get unread message count for a specific room
     */
    @Transactional(readOnly = true)
    public long getUnreadMessageCountForRoom(String roomId, UUID userId) {
        return messageRepository.countUnreadMessagesByRoomIdAndUserId(roomId, userId);
    }
    
    /**
     * Get chat room by ID with validation
     */
    @Transactional(readOnly = true)
    public Optional<ChatRoom> getChatRoomById(String roomId) {
        return chatRoomRepository.findById(roomId);
    }
    
    /**
     * Validate if user can access a chat room
     */
    @Transactional(readOnly = true)
    public boolean canUserAccessRoom(String roomId, UUID userId, UserRole role) {
        Optional<ChatRoom> roomOpt = chatRoomRepository.findById(roomId);
        if (!roomOpt.isPresent()) {
            return false;
        }
        
        ChatRoom room = roomOpt.get();
        
        switch (role) {
            case CUSTOMER:
            case customer:
                return room.getCustomer() != null && 
                       room.getCustomer().getUser() != null &&
                       room.getCustomer().getUser().getId().equals(userId);
            case RESTAURANT_OWNER:
            case restaurant_owner:
                return room.getRestaurant() != null && 
                       room.getRestaurant().getOwner() != null &&
                       room.getRestaurant().getOwner().getUser() != null &&
                       room.getRestaurant().getOwner().getUser().getId().equals(userId);
            case ADMIN:
            case admin:
                return room.getAdmin() != null && room.getAdmin().getId().equals(userId);
            default:
                return false;
        }
    }
    
    /**
     * Archive a chat room (soft delete)
     */
    public void archiveChatRoom(String roomId) {
        Optional<ChatRoom> roomOpt = chatRoomRepository.findById(roomId);
        if (roomOpt.isPresent()) {
            ChatRoom room = roomOpt.get();
            room.setIsActive(false);
            chatRoomRepository.save(room);
        }
    }
    
    /**
     * Get chat statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getChatStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalRooms = chatRoomRepository.count();
        long activeRooms = chatRoomRepository.countActiveRooms();
        long totalMessages = messageRepository.count();
        long unreadMessages = messageRepository.countUnreadMessages();
        
        stats.put("totalRooms", totalRooms);
        stats.put("activeRooms", activeRooms);
        stats.put("totalMessages", totalMessages);
        stats.put("unreadMessages", unreadMessages);
        
        if (activeRooms > 0) {
            stats.put("avgMessagesPerRoom", (double) totalMessages / activeRooms);
        } else {
            stats.put("avgMessagesPerRoom", 0.0);
        }
        
        return stats;
    }
    
    /**
     * Check if user can chat with restaurant
     */
    @Transactional(readOnly = true)
    public boolean canUserChatWithRestaurant(UUID userId, UserRole role, Integer restaurantId) {
        switch (role) {
            case CUSTOMER:
            case customer:
                return true; // Customers can chat with any restaurant
            case RESTAURANT_OWNER:
            case restaurant_owner:
                // Restaurant owners can chat with customers of their restaurant
                List<RestaurantProfile> ownerRestaurants = restaurantProfileRepository.findByOwnerUserId(userId);
                return ownerRestaurants.stream()
                    .anyMatch(restaurant -> restaurant != null && restaurant.getRestaurantId().equals(restaurantId));
            case ADMIN:
            case admin:
                return true; // Admins can chat with any restaurant
            default:
                return false;
        }
    }
    
    /**
     * Get all admin users for restaurant owner to chat with
     */
    @Transactional(readOnly = true)
    public List<User> getAvailableAdmins() {
        return userRepository.findByRole(UserRole.ADMIN, org.springframework.data.domain.Pageable.unpaged()).getContent();
    }

    /**
     * Get available restaurants for admin to chat with
     */
    @Transactional(readOnly = true)
    public List<RestaurantChatDto> getAvailableRestaurants() {
        List<RestaurantProfile> restaurants = restaurantProfileRepository.findAll();
        return restaurants.stream()
                .map(restaurant -> new RestaurantChatDto(
                        restaurant.getRestaurantId(),
                        restaurant.getRestaurantName(),
                        restaurant.getOwner() != null ? restaurant.getOwner().getUser().getFullName() : "Unknown Owner",
                        restaurant.getOwner() != null ? restaurant.getOwner().getUser().getEmail() : "Unknown Email",
                        true))
                .collect(Collectors.toList());
    }

    /**
     * Get unread count for a specific room and user
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUnreadCountForRoom(String roomId, UUID userId) {
        Map<String, Object> result = new HashMap<>();
        long unreadCount = messageRepository.countUnreadMessagesByRoomIdAndUserId(roomId, userId);
        result.put("roomId", roomId);
        result.put("userId", userId);
        result.put("unreadCount", unreadCount);
        return result;
    }

    /**
     * Get total unread count for a user across all rooms
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getTotalUnreadCountForUser(UUID userId) {
        Map<String, Object> result = new HashMap<>();
        long totalUnreadCount = messageRepository.countTotalUnreadMessagesByUserId(userId);
        result.put("userId", userId);
        result.put("totalUnreadCount", totalUnreadCount);
        return result;
    }

    /**
     * Get existing room ID for participants if room already exists
     */
    @Transactional(readOnly = true)
    public String getExistingRoomId(UUID userId, UserRole userRole, Integer restaurantId) {
        try {
            Optional<ChatRoom> existingRoom = chatRoomRepository.findExistingRoom(userId, restaurantId);
            return existingRoom.map(ChatRoom::getRoomId).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get room ID for participants
     */
    @Transactional(readOnly = true)
    public String getRoomId(UUID userId, UserRole userRole, Integer restaurantId) {
        switch (userRole) {
            case CUSTOMER:
            case customer:
                // Find customer by user ID first
                Optional<ChatRoom> customerRoom = chatRoomRepository.findByCustomerAndRestaurant(userId, restaurantId);
                return customerRoom.map(ChatRoom::getRoomId).orElse(null);
            case RESTAURANT_OWNER:
            case restaurant_owner:
                // For restaurant owners, we need to find rooms where they are the restaurant owner
                List<ChatRoom> ownerRooms = chatRoomRepository.findByRestaurantOwnerId(userId);
                return ownerRooms.stream()
                    .filter(room -> room.getRestaurant() != null && room.getRestaurant().getRestaurantId().equals(restaurantId))
                    .map(ChatRoom::getRoomId)
                    .findFirst()
                    .orElse(null);
            case ADMIN:
            case admin:
                Optional<ChatRoom> adminRoom = chatRoomRepository.findByAdminAndRestaurant(userId, restaurantId);
                return adminRoom.map(ChatRoom::getRoomId).orElse(null);
            default:
                return null;
        }
    }
    
    // Helper methods
    private String generateCustomerRestaurantRoomId(UUID customerId, Integer restaurantId) {
        return "customer_" + customerId + "_restaurant_" + restaurantId;
    }
    
    private String generateAdminRestaurantRoomId(UUID adminId, Integer restaurantId) {
        return "admin_" + adminId + "_restaurant_" + restaurantId;
    }
    
    /**
     * Find customer for admin-restaurant chat to avoid foreign key constraint
     * violation
     * Strategy: Use admin_id from room to find User, then create Customer object
     */
    private Customer findCustomerForAdminRestaurantChat(ChatRoom room) {
        if (room == null || room.getAdmin() == null) {
            return null;
        }

        // Get admin User from room
        User adminUser = room.getAdmin();

        // Check if admin already has a customer record
        Customer existingCustomer = customerRepository.findByUserId(adminUser.getId()).orElse(null);
        if (existingCustomer != null) {
            return existingCustomer;
        }

        // Create new Customer record for admin
        Customer adminAsCustomer = new Customer();
        adminAsCustomer.setUser(adminUser);
        adminAsCustomer.setFullName(adminUser.getFullName());

        // Save to database first to get proper customer_id
        adminAsCustomer = customerRepository.save(adminAsCustomer);

        return adminAsCustomer;
    }

    private boolean canUserSendMessage(ChatRoom room, User sender) {
        UserRole senderRole = sender.getRole();

        if (room.isCustomerRestaurantChat()) {
            // Check if sender is customer
            boolean isCustomer = (senderRole == UserRole.CUSTOMER || senderRole == UserRole.customer) && 
                    room.getCustomer() != null &&
                    room.getCustomer().getUser() != null &&
                               room.getCustomer().getUser().getId().equals(sender.getId());
            
            // Check if sender is restaurant owner
            boolean isRestaurantOwner = (senderRole == UserRole.RESTAURANT_OWNER || senderRole == UserRole.restaurant_owner) && 
                    room.getRestaurant() != null &&
                    room.getRestaurant().getOwner() != null &&
                    room.getRestaurant().getOwner().getUser() != null &&
                                       room.getRestaurant().getOwner().getUser().getId().equals(sender.getId());

            return isCustomer || isRestaurantOwner;
        } else if (room.isAdminRestaurantChat()) {
            // Check if sender is admin
            boolean isAdmin = (senderRole == UserRole.ADMIN || senderRole == UserRole.admin) && 
                    room.getAdmin() != null &&
                             room.getAdmin().getId().equals(sender.getId());
            
            // Check if sender is restaurant owner
            boolean isRestaurantOwner = (senderRole == UserRole.RESTAURANT_OWNER || senderRole == UserRole.restaurant_owner) && 
                    room.getRestaurant() != null &&
                    room.getRestaurant().getOwner() != null &&
                    room.getRestaurant().getOwner().getUser() != null &&
                                       room.getRestaurant().getOwner().getUser().getId().equals(sender.getId());

            return isAdmin || isRestaurantOwner;
        }

        return false;
    }
    
    /**
     * Get chat room by ID
     */
    public ChatRoom getRoomById(String roomId) {
        return chatRoomRepository.findById(roomId).orElse(null);
    }

    /**
     * Get AI restaurant owner user ID
     */
    public UUID getAIRestaurantOwnerId() {
        RestaurantProfile aiRestaurant = restaurantProfileRepository.findById(37).orElse(null);
        if (aiRestaurant != null && aiRestaurant.getOwner() != null) {
            return aiRestaurant.getOwner().getUser().getId();
        }
        return null;
    }

    public ChatRoomDto convertToDto(ChatRoom room) {
        return convertToDto(room, null);
    }

    public ChatRoomDto convertToDto(ChatRoom room, UUID currentUserId) {
        // Get last message
        List<Message> lastMessages = messageRepository.findLastMessageByRoomId(room.getRoomId());
        Message lastMessage = lastMessages.isEmpty() ? null : lastMessages.get(0);
        String lastMessageContent = lastMessage != null ? lastMessage.getContent() : null;
        LocalDateTime lastMessageAt = lastMessage != null ? lastMessage.getSentAt() : room.getCreatedAt();
        
        // Calculate unread count for current user
        Long unreadCount = 0L;
        if (currentUserId != null) {
            unreadCount = messageRepository.countUnreadMessagesByRoomIdAndUserId(room.getRoomId(), currentUserId);
        }

        // Get participant info
        UUID participantId;
        String participantName;
        String participantRole;
        String participantAvatarUrl = null;
        
        if (room.isCustomerRestaurantChat()) {
            participantId = room.getCustomer() != null ? room.getCustomer().getUser().getId() : null;
            if (room.getCustomer() != null) {
                // For customers: ALWAYS prioritize user.fullName (from users table)
                // because customer.fullName might be outdated/unsynchronized
                String userFullName = room.getCustomer().getUser().getFullName();
                if (userFullName != null && !userFullName.trim().isEmpty()) {
                    participantName = userFullName;
                } else {
                    String customerName = room.getCustomer().getFullName();
                    participantName = customerName;
                }

                // If still null/empty, use email
                if (participantName == null || participantName.trim().isEmpty()) {
                    participantName = room.getCustomer().getUser().getEmail();
                }

                // Final fallback
                if (participantName == null || participantName.trim().isEmpty()) {
                    participantName = "Unknown Customer";
                }
                
                // Get avatar URL from user
                participantAvatarUrl = room.getCustomer().getUser().getProfileImageUrl();
            } else {
                participantName = "Unknown Customer";
            }
            participantRole = "CUSTOMER";
        } else if (room.isAdminRestaurantChat()) {
            participantId = room.getAdmin() != null ? room.getAdmin().getId() : null;
            if (room.getAdmin() != null) {
                participantName = room.getAdmin().getFullName();
                if (participantName == null || participantName.trim().isEmpty()) {
                    participantName = room.getAdmin().getEmail();
                }
                if (participantName == null || participantName.trim().isEmpty()) {
                    participantName = "Unknown Admin";
                }
                
                // Get avatar URL from admin (admin is User directly)
                participantAvatarUrl = room.getAdmin().getProfileImageUrl();
            } else {
                participantName = "Unknown Admin";
            }
            participantRole = "ADMIN";
        } else {
            participantId = null;
            participantName = "Unknown";
            participantRole = "UNKNOWN";
        }
        
        return new ChatRoomDto(
            room.getRoomId(),
            participantId,
            participantName,
            participantRole,
            room.getRestaurant() != null ? room.getRestaurant().getRestaurantId() : null,
            room.getRestaurant() != null ? room.getRestaurant().getRestaurantName() : "Unknown Restaurant",
            lastMessageContent,
            lastMessageAt,
            unreadCount,
            room.getIsActive(),
            participantAvatarUrl
        );
    }
    
    private ChatMessageDto convertToDto(Message message) {
        return new ChatMessageDto(
            message.getMessageId(),
            message.getRoom().getRoomId(),
            message.getSender().getId(),
            message.getSenderName(),
            message.getContent(),
            message.getMessageType().getValue(),
            message.getFileUrl(),
            message.getSentAt(),
            message.getIsRead()
        );
    }

    /**
     * Get restaurant owner ID from restaurant ID
     */
    public UUID getRestaurantOwnerId(Integer restaurantId) {
        try {
            Optional<RestaurantProfile> restaurantOpt = restaurantProfileRepository.findById(restaurantId);
            if (restaurantOpt.isPresent()) {
                RestaurantProfile restaurant = restaurantOpt.get();
                return restaurant.getOwner().getUser().getId();
            }
        } catch (Exception e) {
            // Log error silently
        }
        return null;
    }
}
