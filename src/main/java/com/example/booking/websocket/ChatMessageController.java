package com.example.booking.websocket;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.example.booking.domain.ChatRoom;
import com.example.booking.domain.Message;
import com.example.booking.domain.MessageType;
import com.example.booking.domain.User;
import com.example.booking.service.ChatService;
import com.example.booking.service.SimpleUserService;
import com.example.booking.annotation.RateLimited;
import com.example.booking.util.InputSanitizer;

import org.springframework.transaction.annotation.Transactional;

/**
 * WebSocket controller for handling real-time chat messages
 */
@Controller
@Transactional
public class ChatMessageController {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private ChatService chatService;
    
    @Autowired
    private SimpleUserService userService;
    
    @Autowired
    private InputSanitizer inputSanitizer;

    @Autowired
    private com.example.booking.service.AIService aiService;

    @Autowired
    private com.example.booking.service.AIResponseProcessorService aiResponseProcessorService;

    /**
     * Handle incoming chat messages - Optimized and safe version
     */
    @MessageMapping("/chat.sendMessage")
    @RateLimited(value = RateLimited.OperationType.CHAT, message = "Quá nhiều tin nhắn. Vui lòng thử lại sau.")
    public void sendMessage(@Payload ChatMessageRequest request, 
                           SimpMessageHeaderAccessor headerAccessor) {
        try {
            System.out.println("=== WebSocket Message Received ===");
            System.out.println("Request: " + request);
            
            // Validate request
            if (request.getRoomId() == null || request.getRoomId().trim().isEmpty()) {
                System.err.println("ERROR: Room ID is null or empty");
                sendErrorToUser("system", "Invalid room ID");
                return;
            }
            
            // Get user info from session first
            Principal principal = headerAccessor.getUser();
            if (principal == null) {
                System.err.println("ERROR: Principal is null - user not authenticated");
                sendErrorToUser("system", "User not authenticated");
                return;
            }
            
            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                System.err.println("ERROR: Message content is null or empty");
                sendErrorToUser(principal.getName(), "Tin nhắn không được để trống");
                return;
            }
            
            // Additional validation: check for whitespace-only messages
            String trimmedContent = request.getContent().trim();
            if (trimmedContent.isEmpty() || trimmedContent.matches("^\\s*$")) {
                System.err.println("ERROR: Message content is only whitespace");
                sendErrorToUser(principal.getName(), "Tin nhắn không được chỉ chứa dấu cách");
                return;
            }
            
            // Sanitize message content to prevent XSS
            String sanitizedContent = inputSanitizer.sanitizeChatMessage(trimmedContent);
            if (sanitizedContent == null || sanitizedContent.isEmpty()) {
                System.err.println("ERROR: Message content is empty after sanitization");
                sendErrorToUser(principal.getName(), "Tin nhắn chứa nội dung không hợp lệ");
                return;
            }
            
            // Get User object from principal (handles both User and OAuth2User)
            User user = getUserFromPrincipal(principal);
            UUID senderId = user.getId();
            
            System.out.println("User: " + user.getUsername() + " (ID: " + senderId + ")");
            System.out.println("Sending message to room: " + request.getRoomId());
            System.out.println("Content: " + request.getContent());
            
            // Validate user can access this room
            if (!chatService.canUserAccessRoom(request.getRoomId(), senderId, user.getRole())) {
                System.err.println("ERROR: User " + user.getUsername() + " not authorized to access room: " + request.getRoomId());
                sendErrorToUser(principal.getName(), "Not authorized to send message in this room");
                return;
            }
            
            // Check if this is a message to AI Restaurant (ID = 37)
            if (isAIRestaurantMessage(request.getRoomId())) {
                // Send customer message first
                Message customerMessage = chatService.sendMessage(
                        request.getRoomId(),
                        senderId,
                        sanitizedContent,
                        MessageType.TEXT);

                // Broadcast customer message
                messagingTemplate.convertAndSend("/topic/room/" + request.getRoomId(),
                        new ChatMessageResponse(customerMessage));

                System.out.println("Customer message sent to AI restaurant, processing AI response...");

                // Process AI response asynchronously
                processAIResponse(request.getRoomId(), sanitizedContent, user.getId());

                // Broadcast unread count updates
                broadcastUnreadCountUpdates(request.getRoomId(), senderId);
                return;
            }

            // Send message with sanitized content (normal chat)
            Message message = chatService.sendMessage(
                request.getRoomId(), 
                senderId, 
                sanitizedContent, 
                MessageType.TEXT
            );
            
            System.out.println("Message saved successfully: " + message.getMessageId());
            
            // Broadcast to room participants only
            messagingTemplate.convertAndSend("/topic/room/" + request.getRoomId(), 
                new ChatMessageResponse(message));
            
            System.out.println("Message broadcasted to /topic/room/" + request.getRoomId());
            
            // Broadcast unread count updates to all participants
            broadcastUnreadCountUpdates(request.getRoomId(), senderId);

        } catch (Exception e) {
            System.err.println("Error in sendMessage: " + e.getMessage());
            e.printStackTrace();
            
            // Send error message back to sender
            Principal user = headerAccessor.getUser();
            if (user != null) {
                sendErrorToUser(user.getName(), "Failed to send message: " + e.getMessage());
            } else {
                sendErrorToUser("system", "Failed to send message: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handle typing indicators
     */
    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload TypingRequest request, 
                           SimpMessageHeaderAccessor headerAccessor) {
        try {
            System.out.println("=== WebSocket Typing Received ===");
            System.out.println("Request: " + request);

            Principal principal = headerAccessor.getUser();
            if (principal == null) {
                System.err.println("ERROR: Principal is null for typing");
                return;
            }

            System.out.println("User: " + principal.getName() + " typing: " + request.isTyping());
            System.out.println("Room: " + request.getRoomId());

            // Broadcast typing indicator to room (except sender)
            messagingTemplate.convertAndSend("/topic/room/" + request.getRoomId() + "/typing",
                    new TypingResponse(principal.getName(), request.isTyping()));

            System.out.println("Typing indicator broadcasted to /topic/room/" + request.getRoomId() + "/typing");

        } catch (Exception e) {
            System.err.println("Error in handleTyping: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle user joining a room - Optimized and safe version
     */
    @MessageMapping("/chat.joinRoom")
    public void joinRoom(@Payload JoinRoomRequest request, 
                        SimpMessageHeaderAccessor headerAccessor) {
        Principal principal = headerAccessor.getUser();
        if (principal == null) {
            System.err.println("ERROR: Principal is null in joinRoom");
            return;
        }
        
        try {
            // Validate roomId
            if (request.getRoomId() == null || request.getRoomId().trim().isEmpty()) {
                System.err.println("ERROR: Room ID is null or empty");
                sendErrorToUser(principal.getName(), "Invalid room ID");
                return;
            }
            
            // Get User object from principal (handles both User and OAuth2User)
            User user = getUserFromPrincipal(principal);
            UUID userId = user.getId();
            
            System.out.println("User " + user.getUsername() + " joining room: " + request.getRoomId());
            
            // Validate user can access this room
            if (!chatService.canUserAccessRoom(request.getRoomId(), userId, user.getRole())) {
                System.err.println("ERROR: User " + user.getUsername() + " not authorized to access room: " + request.getRoomId());
                sendErrorToUser(principal.getName(), "Not authorized to access this room");
                return;
            }
            
            // Mark messages as read when user joins
            int updatedCount = chatService.markMessagesAsRead(request.getRoomId(), userId);
            System.out.println("Marked " + updatedCount + " messages as read for user " + user.getUsername());
            
            // Broadcast unread count updates after marking messages as read
            broadcastUnreadCountUpdates(request.getRoomId(), userId);

            // Notify room that user joined (only for actual new joins, not tab switches)
            // Commented out to prevent empty messages when switching tabs
            // messagingTemplate.convertAndSend("/topic/room/" + request.getRoomId(),
            //     new UserJoinedResponse(userId.toString()));
            
            System.out.println("User " + user.getUsername() + " successfully joined room: " + request.getRoomId());
                
        } catch (Exception e) {
            System.err.println("Error in joinRoom: " + e.getMessage());
            e.printStackTrace();
            sendErrorToUser(principal.getName(), "Failed to join room: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to send error messages to user
     */
    private void sendErrorToUser(String username, String errorMessage) {
        try {
            messagingTemplate.convertAndSendToUser(username, "/queue/errors",
                new ErrorResponse(errorMessage));
        } catch (Exception e) {
            System.err.println("Failed to send error message to user " + username + ": " + e.getMessage());
        }
    }
    
    /**
     * Broadcast unread count updates to all participants in a room
     */
    private void broadcastUnreadCountUpdates(String roomId, UUID senderId) {
        try {
            // Get room participants
            Optional<ChatRoom> roomOpt = chatService.getChatRoomById(roomId);
            if (!roomOpt.isPresent()) {
                return;
            }

            ChatRoom room = roomOpt.get();

            // Get all participants
            List<UUID> participantIds = new ArrayList<>();

            if (room.isCustomerRestaurantChat()) {
                // For customer-restaurant chat, extract participants from room ID to avoid
                // LazyInitializationException
                String[] parts = roomId.split("_");
                if (parts.length >= 2 && parts[0].equals("customer")) {
                    try {
                        UUID customerId = UUID.fromString(parts[1]);
                        participantIds.add(customerId);
                        System.out.println("Added customer participant: " + customerId);
                    } catch (Exception e) {
                        System.err.println("Error parsing customer ID from room ID: " + e.getMessage());
                    }
                }

                // For restaurant owner, extract restaurant ID and get owner
                if (parts.length >= 4 && parts[2].equals("restaurant")) {
                    try {
                        Integer restaurantId = Integer.parseInt(parts[3]);
                        // Get restaurant owner ID from restaurant ID
                        UUID restaurantOwnerId = chatService.getRestaurantOwnerId(restaurantId);
                        if (restaurantOwnerId != null) {
                            participantIds.add(restaurantOwnerId);
                            System.out.println("Added restaurant owner participant: " + restaurantOwnerId);
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing restaurant ID or getting owner: " + e.getMessage());
                    }
                }

            } else if (room.isAdminRestaurantChat()) {
                // For admin-restaurant chat, extract admin ID from room ID
                String[] parts = roomId.split("_");
                if (parts.length >= 2 && parts[0].equals("admin")) {
                    try {
                        UUID adminId = UUID.fromString(parts[1]);
                        participantIds.add(adminId);
                        System.out.println("Added admin participant: " + adminId);
                    } catch (Exception e) {
                        System.err.println("Error parsing admin ID from room ID: " + e.getMessage());
                    }
                }

                // For restaurant owner, we need to get from restaurant ID
                // This is a limitation - we need restaurant owner ID but only have restaurant
                // ID
                // For now, we'll skip restaurant owner to avoid LazyInitializationException
                System.out.println("Skipping restaurant owner to avoid LazyInitializationException");
            }

            // Send unread count updates to each participant
            for (UUID participantId : participantIds) {
                // Send to ALL participants including the sender (who joined the room)
                Map<String, Object> roomUnreadCount = chatService.getUnreadCountForRoom(roomId, participantId);
                Map<String, Object> totalUnreadCount = chatService.getTotalUnreadCountForUser(participantId);

                UnreadCountUpdate update = new UnreadCountUpdate(
                        roomId,
                        participantId,
                        (Long) roomUnreadCount.get("unreadCount"),
                        (Long) totalUnreadCount.get("totalUnreadCount"));

                // Extract username from room ID or use participant ID as fallback
                String username = extractUsernameFromRoomId(roomId, participantId);

                messagingTemplate.convertAndSendToUser(username, "/queue/unread-updates", update);
                System.out.println("Sent unread count update to user " + username + " (ID: " + participantId
                        + "): room=" +
                        roomUnreadCount.get("unreadCount") + ", total=" + totalUnreadCount.get("totalUnreadCount"));

                // Debug: Also try sending to participant ID as fallback
                try {
                    messagingTemplate.convertAndSendToUser(participantId.toString(), "/queue/unread-updates", update);
                    System.out.println("Also sent unread count update to participant ID " + participantId.toString());
                } catch (Exception e) {
                    System.err.println("Failed to send to participant ID: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Error broadcasting unread count updates: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Extract username from room ID for WebSocket messaging
     */
    private String extractUsernameFromRoomId(String roomId, UUID participantId) {
        try {
            // Format: "customer_" + customerId + "_restaurant_" + restaurantId
            // Format: "admin_" + adminId + "_restaurant_" + restaurantId

            if (roomId.startsWith("customer_")) {
                // Extract customer ID from room ID
                String[] parts = roomId.split("_");
                if (parts.length >= 2) {
                    String customerIdStr = parts[1];
                    try {
                        UUID customerId = UUID.fromString(customerIdStr);
                        // For now, use customer ID directly to avoid LazyInitializationException
                        // We can enhance this later with proper transaction handling
                        return customerIdStr;
                    } catch (Exception e) {
                        System.err.println("Error parsing customer ID " + customerIdStr + ": " + e.getMessage());
                    }
                    return customerIdStr; // Fallback to ID
                }
            } else if (roomId.startsWith("admin_")) {
                // Extract admin ID from room ID
                String[] parts = roomId.split("_");
                if (parts.length >= 2) {
                    String adminIdStr = parts[1];
                    // For admin, we can use the ID directly as username since it's from users table
                    return adminIdStr;
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting username from room ID " + roomId + ": " + e.getMessage());
        }

        // Fallback to participant ID
        return participantId.toString();
    }

    /**
     * Helper method to get User from Principal (handles User, UsernamePasswordAuthenticationToken, OAuth2User, and OAuth2AuthenticationToken)
     */
    private User getUserFromPrincipal(Principal principal) {
        System.out.println("=== Principal Type Debug ===");
        System.out.println("Principal class: " + principal.getClass().getName());
        System.out.println("Principal name: " + principal.getName());
        
        // Nếu là User object trực tiếp (regular login)
        if (principal instanceof User) {
            System.out.println("Found User entity directly");
            return (User) principal;
        }

        // Nếu là UsernamePasswordAuthenticationToken (form login)
        if (principal instanceof org.springframework.security.authentication.UsernamePasswordAuthenticationToken) {
            System.out.println("Found UsernamePasswordAuthenticationToken");
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken authToken = 
                (org.springframework.security.authentication.UsernamePasswordAuthenticationToken) principal;
            
            // Lấy User từ principal của UsernamePasswordAuthenticationToken
            Object authPrincipal = authToken.getPrincipal();
            System.out.println("AuthPrincipal class: " + authPrincipal.getClass().getName());
            
            if (authPrincipal instanceof User) {
                System.out.println("Found User in UsernamePasswordAuthenticationToken");
                return (User) authPrincipal;
            } else {
                // Fallback: tìm User bằng username
                String username = authToken.getName();
                System.out.println("UsernamePasswordAuthenticationToken username: " + username);
                
                try {
                    // Use loadUserByUsername with @Transactional to avoid lazy loading issues
                    User user = (User) userService.loadUserByUsername(username);
                    if (user != null) {
                        System.out.println("Found User in database: " + user.getId());
                        return user;
                    } else {
                        throw new RuntimeException("User not found for username: " + username);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading user by username: " + e.getMessage());
                    throw new RuntimeException("User not found for username: " + username +
                            ". Error: " + e.getMessage());
                }
            }
        }

        // Nếu là OAuth2AuthenticationToken (WebSocket OAuth2 login)
        if (principal instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
            System.out.println("Found OAuth2AuthenticationToken");
            org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken authToken = 
                (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) principal;
            
            String username = authToken.getName(); // username = email cho OAuth users
            System.out.println("OAuth2 username: " + username);

            // Tìm User thực tế từ database
            try {
                User user = (User) userService.loadUserByUsername(username);
                System.out.println("Found User in database: " + user.getId());
                return user;
            } catch (Exception e) {
                throw new RuntimeException("User not found for OAuth username: " + username +
                        ". Error: " + e.getMessage());
            }
        }

        // Nếu là OAuth2User hoặc OidcUser (OAuth2 login)
        if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            System.out.println("Found OAuth2User");
            String username = principal.getName(); // username = email cho OAuth users
            System.out.println("OAuth2User username: " + username);

            // Tìm User thực tế từ database
            try {
                User user = (User) userService.loadUserByUsername(username);
                System.out.println("Found User in database: " + user.getId());
                return user;
            } catch (Exception e) {
                throw new RuntimeException("User not found for OAuth username: " + username +
                        ". Error: " + e.getMessage());
            }
        }

        throw new RuntimeException("Unsupported principal type: " + principal.getClass().getName());
    }
    
    // Request/Response DTOs
    public static class ChatMessageRequest {
        private String roomId;
        private String content;
        
        public ChatMessageRequest() {}
        
        public ChatMessageRequest(String roomId, String content) {
            this.roomId = roomId;
            this.content = content;
        }
        
        public String getRoomId() { return roomId; }
        public void setRoomId(String roomId) { this.roomId = roomId; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
    
    public static class ChatMessageResponse {
        private Integer messageId;
        private String roomId;
        private String senderId;
        private String senderName;
        private String senderRole;
        private String content;
        private String messageType;
        private String sentAt;
        
        public ChatMessageResponse(Message message) {
            this.messageId = message.getMessageId();
            this.roomId = message.getRoom().getRoomId();
            this.senderId = message.getSender().getId().toString();
            this.senderName = message.getSenderName();
            this.senderRole = message.getSender().getRole().toString();
            this.content = message.getContent();
            this.messageType = message.getMessageType().getValue();
            this.sentAt = message.getSentAt().toString();
        }
        
        // Getters
        public Integer getMessageId() { return messageId; }
        public String getRoomId() { return roomId; }
        public String getSenderId() { return senderId; }
        public String getSenderName() { return senderName; }

        public String getSenderRole() {
            return senderRole;
        }
        public String getContent() { return content; }
        public String getMessageType() { return messageType; }
        public String getSentAt() { return sentAt; }
    }
    
    public static class TypingRequest {
        private String roomId;
        private boolean typing;
        
        public TypingRequest() {}
        
        public TypingRequest(String roomId, boolean typing) {
            this.roomId = roomId;
            this.typing = typing;
        }
        
        public String getRoomId() { return roomId; }
        public void setRoomId(String roomId) { this.roomId = roomId; }
        public boolean isTyping() { return typing; }
        public void setTyping(boolean typing) { this.typing = typing; }
    }
    
    public static class TypingResponse {
        private String userId;
        private boolean typing;
        
        public TypingResponse(String userId, boolean typing) {
            this.userId = userId;
            this.typing = typing;
        }
        
        public String getUserId() { return userId; }
        public boolean isTyping() { return typing; }
    }
    
    public static class JoinRoomRequest {
        private String roomId;
        
        public JoinRoomRequest() {}
        
        public JoinRoomRequest(String roomId) {
            this.roomId = roomId;
        }
        
        public String getRoomId() { return roomId; }
        public void setRoomId(String roomId) { this.roomId = roomId; }
    }
    
    public static class UserJoinedResponse {
        private String userId;
        
        public UserJoinedResponse(String userId) {
            this.userId = userId;
        }
        
        public String getUserId() { return userId; }
    }
    
    /**
     * Check if this is a message to AI Restaurant (ID = 37)
     */
    private boolean isAIRestaurantMessage(String roomId) {
        try {
            ChatRoom room = chatService.getRoomById(roomId);
            return room != null && room.getRestaurant() != null &&
                    room.getRestaurant().getRestaurantId().equals(37);
        } catch (Exception e) {
            System.err.println("Error checking AI restaurant message: " + e.getMessage());
            return false;
        }
    }

    /**
     * Process AI response asynchronously with action execution
     */
    @org.springframework.scheduling.annotation.Async
    public void processAIResponse(String roomId, String message, UUID userId) {
        try {
            System.out.println("Processing AI response for room: " + roomId);

            // Show typing indicator
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/typing",
                    new TypingResponse("AI Assistant", true));

            // Get user object for action processing
            User user = getUserFromUserId(userId);

            // Call AI service to get initial response
            String aiResponse = aiService.sendMessageToAI(message, userId.toString());

            // Process AI response and execute any detected actions
            String processedResponse = aiResponseProcessorService.processAIResponse(
                    aiResponse, user, message);

            // Hide typing indicator
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/typing",
                    new TypingResponse("AI Assistant", false));

            // Create AI message with processed response
            UUID aiUserId = chatService.getAIRestaurantOwnerId();
            if (aiUserId != null) {
                Message aiMessage = chatService.sendMessage(
                        roomId,
                        aiUserId,
                        processedResponse,
                        MessageType.TEXT);

                // Broadcast AI response
                messagingTemplate.convertAndSend("/topic/room/" + roomId,
                        new ChatMessageResponse(aiMessage));

                System.out.println("AI response with actions processed successfully");

                // Update unread counts
                broadcastUnreadCountUpdates(roomId, aiUserId);
            } else {
                System.err.println("AI restaurant owner ID not found");
            }

        } catch (Exception e) {
            System.err.println("Error processing AI response: " + e.getMessage());
            e.printStackTrace();

            // Send error message
            try {
                UUID aiUserId = chatService.getAIRestaurantOwnerId();
                if (aiUserId != null) {
                    Message errorMessage = chatService.sendMessage(
                            roomId,
                            aiUserId,
                            "Xin lỗi, có lỗi xảy ra khi xử lý tin nhắn của bạn.",
                            MessageType.TEXT);

                    messagingTemplate.convertAndSend("/topic/room/" + roomId,
                            new ChatMessageResponse(errorMessage));
                }
            } catch (Exception ex) {
                System.err.println("Error sending error message: " + ex.getMessage());
            }
        }
    }

    /**
     * Helper method to get User from user ID
     */
    private User getUserFromUserId(UUID userId) {
        try {
            return userService.findById(userId);
        } catch (Exception e) {
            System.err.println("Error getting user by ID: " + e.getMessage());
            throw new RuntimeException("User not found for ID: " + userId);
        }
    }

    public static class ErrorResponse {
        private String message;
        
        public ErrorResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() { return message; }
    }

    public static class UnreadCountUpdate {
        private String roomId;
        private String userId;
        private Long roomUnreadCount;
        private Long totalUnreadCount;

        public UnreadCountUpdate(String roomId, UUID userId, Long roomUnreadCount, Long totalUnreadCount) {
            this.roomId = roomId;
            this.userId = userId.toString();
            this.roomUnreadCount = roomUnreadCount;
            this.totalUnreadCount = totalUnreadCount;
        }

        public String getRoomId() {
            return roomId;
        }

        public String getUserId() {
            return userId;
        }

        public Long getRoomUnreadCount() {
            return roomUnreadCount;
        }

        public Long getTotalUnreadCount() {
            return totalUnreadCount;
        }
    }
}
