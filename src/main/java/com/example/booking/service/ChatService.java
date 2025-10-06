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
     */
    public ChatRoom createCustomerRestaurantRoom(UUID userId, Integer restaurantId) {
        // Find customer by user ID
        Customer customer = customerRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Customer not found for user ID: " + userId));
        
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
        
        // Create room ID
        String roomId = generateAdminRestaurantRoomId(adminId, restaurantId);
        
        // Create room
        ChatRoom room = new ChatRoom(roomId, admin, restaurant);
        return chatRoomRepository.save(room);
    }
    
    /**
     * Create chat room between restaurant owner and admin
     * This allows restaurant owners to initiate chat with admin
     */
    public ChatRoom createRestaurantOwnerAdminRoom(UUID restaurantOwnerId, UUID adminId) {
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
        
        // Get the first restaurant of the owner (assuming one owner has one restaurant)
        List<RestaurantProfile> ownerRestaurants = restaurantOwner.getRestaurants();
        if (ownerRestaurants == null || ownerRestaurants.isEmpty()) {
            throw new RuntimeException("Restaurant owner has no restaurants");
        }
        
        RestaurantProfile restaurant = ownerRestaurants.get(0); // Get first restaurant
        
        // Check if room already exists
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByAdminAndRestaurant(adminId, restaurant.getRestaurantId());
        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }
        
        // Create room ID
        String roomId = generateAdminRestaurantRoomId(adminId, restaurant.getRestaurantId());
        
        // Create room
        ChatRoom room = new ChatRoom(roomId, admin, restaurant);
        return chatRoomRepository.save(room);
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
        
        return rooms.stream().map(this::convertToDto).collect(Collectors.toList());
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
        
        // Set customer and owner based on room type
        if (sender.getRole() == UserRole.CUSTOMER || sender.getRole() == UserRole.customer) {
            // Customer sending message to restaurant
            Customer customer = customerRepository.findByUserId(senderId)
                .orElseThrow(() -> new RuntimeException("Customer not found for user: " + senderId));
            message.setCustomer(customer);
            
            // Set owner as restaurant owner
            if (room.getRestaurant() != null && room.getRestaurant().getOwner() != null) {
                RestaurantOwner owner = room.getRestaurant().getOwner();
                if (owner.getOwnerId() == null) {
                    throw new RuntimeException("Restaurant owner ID is null for room: " + roomId);
                }
                message.setOwner(owner);
            } else {
                throw new RuntimeException("Restaurant owner not found for room: " + roomId);
            }
        } else if (sender.getRole() == UserRole.RESTAURANT_OWNER || sender.getRole() == UserRole.restaurant_owner) {
            // Restaurant owner sending message to customer
            // Find RestaurantOwner record for this user
            Optional<RestaurantOwner> ownerOpt = restaurantOwnerRepository.findByUserId(senderId);
            if (ownerOpt.isEmpty()) {
                throw new RuntimeException("Restaurant owner not found for user: " + senderId);
            }
            
            RestaurantOwner owner = ownerOpt.get();
            
            // Verify owner has valid ID
            if (owner.getOwnerId() == null) {
                throw new RuntimeException("Restaurant owner ID is null for user: " + senderId);
            }
            
            message.setOwner(owner);
            message.setCustomer(room.getCustomer());
        } else if (sender.getRole() == UserRole.ADMIN || sender.getRole() == UserRole.admin) {
            // Admin sending message - need to find restaurant owner for this room
            if (room.getRestaurant() != null && room.getRestaurant().getOwner() != null) {
                RestaurantOwner owner = room.getRestaurant().getOwner();
                if (owner.getOwnerId() == null) {
                    throw new RuntimeException("Restaurant owner ID is null for room: " + roomId);
                }
                message.setOwner(owner);
            } else {
                throw new RuntimeException("Restaurant owner not found for room: " + roomId);
            }
            message.setCustomer(room.getCustomer());
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
    
    private boolean canUserSendMessage(ChatRoom room, User sender) {
        UserRole senderRole = sender.getRole();
        
        System.out.println("=== Authorization Check ===");
        System.out.println("Room ID: " + room.getRoomId());
        System.out.println("Sender ID: " + sender.getId());
        System.out.println("Sender Role: " + senderRole);
        System.out.println("Room Type - Customer-Restaurant: " + room.isCustomerRestaurantChat());
        System.out.println("Room Type - Admin-Restaurant: " + room.isAdminRestaurantChat());
        
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
            
            System.out.println("Customer check: " + isCustomer);
            System.out.println("Restaurant Owner check: " + isRestaurantOwner);
            System.out.println("Room Customer ID: " + (room.getCustomer() != null && room.getCustomer().getUser() != null ? room.getCustomer().getUser().getId() : "NULL"));
            System.out.println("Room Restaurant Owner ID: " + (room.getRestaurant() != null && room.getRestaurant().getOwner() != null && room.getRestaurant().getOwner().getUser() != null ? room.getRestaurant().getOwner().getUser().getId() : "NULL"));
            
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
            
            System.out.println("Admin check: " + isAdmin);
            System.out.println("Restaurant Owner check: " + isRestaurantOwner);
            System.out.println("Room Admin ID: " + (room.getAdmin() != null ? room.getAdmin().getId() : "NULL"));
            System.out.println("Room Restaurant Owner ID: " + (room.getRestaurant() != null && room.getRestaurant().getOwner() != null && room.getRestaurant().getOwner().getUser() != null ? room.getRestaurant().getOwner().getUser().getId() : "NULL"));
            
            return isAdmin || isRestaurantOwner;
        }
        
        System.out.println("No matching room type found");
        return false;
    }
    
    private ChatRoomDto convertToDto(ChatRoom room) {
        // Get last message
        List<Message> lastMessages = messageRepository.findLastMessageByRoomId(room.getRoomId());
        Message lastMessage = lastMessages.isEmpty() ? null : lastMessages.get(0);
        String lastMessageContent = lastMessage != null ? lastMessage.getContent() : null;
        LocalDateTime lastMessageAt = lastMessage != null ? lastMessage.getSentAt() : room.getCreatedAt();
        
        // Get participant info
        UUID participantId;
        String participantName;
        String participantRole;
        
        if (room.isCustomerRestaurantChat()) {
            participantId = room.getCustomer() != null ? room.getCustomer().getUser().getId() : null;
            participantName = room.getCustomer() != null ? room.getCustomer().getFullName() : "Unknown Customer";
            participantRole = "CUSTOMER";
        } else if (room.isAdminRestaurantChat()) {
            participantId = room.getAdmin() != null ? room.getAdmin().getId() : null;
            participantName = room.getAdmin() != null ? room.getAdmin().getFullName() : "Unknown Admin";
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
            0L, // Will be calculated separately if needed
            room.getIsActive()
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
}
