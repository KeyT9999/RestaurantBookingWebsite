package com.example.booking.websocket;

import java.security.Principal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.example.booking.domain.Message;
import com.example.booking.domain.MessageType;
import com.example.booking.domain.User;
import com.example.booking.service.ChatService;
import com.example.booking.service.SimpleUserService;

/**
 * WebSocket controller for handling real-time chat messages
 */
@Controller
public class ChatMessageController {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private ChatService chatService;
    
    @Autowired
    private SimpleUserService userService;
    
    /**
     * Handle incoming chat messages - Optimized and safe version
     */
    @MessageMapping("/chat.sendMessage")
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
            
            // Send message
            Message message = chatService.sendMessage(
                request.getRoomId(), 
                senderId, 
                request.getContent(), 
                MessageType.TEXT
            );
            
            System.out.println("Message saved successfully: " + message.getMessageId());
            
            // Broadcast to room participants
            messagingTemplate.convertAndSend("/topic/room/" + request.getRoomId(), 
                new ChatMessageResponse(message));
            
            System.out.println("Message broadcasted to /topic/room/" + request.getRoomId());
            
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
        Principal principal = headerAccessor.getUser();
        if (principal == null) return;
        
        // Broadcast typing indicator to room (except sender)
        messagingTemplate.convertAndSend("/topic/room/" + request.getRoomId() + "/typing",
            new TypingResponse(principal.getName(), request.isTyping()));
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
                    User user = (User) userService.loadUserByUsername(username);
                    System.out.println("Found User in database: " + user.getId());
                    return user;
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
        private String content;
        private String messageType;
        private String sentAt;
        
        public ChatMessageResponse(Message message) {
            this.messageId = message.getMessageId();
            this.roomId = message.getRoom().getRoomId();
            this.senderId = message.getSender().getId().toString();
            this.senderName = message.getSenderName();
            this.content = message.getContent();
            this.messageType = message.getMessageType().getValue();
            this.sentAt = message.getSentAt().toString();
        }
        
        // Getters
        public Integer getMessageId() { return messageId; }
        public String getRoomId() { return roomId; }
        public String getSenderId() { return senderId; }
        public String getSenderName() { return senderName; }
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
    
    public static class ErrorResponse {
        private String message;
        
        public ErrorResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() { return message; }
    }
}
