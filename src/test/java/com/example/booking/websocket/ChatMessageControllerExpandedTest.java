package com.example.booking.websocket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.example.booking.domain.ChatRoom;
import com.example.booking.domain.Message;
import com.example.booking.domain.MessageType;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.service.ChatService;
import com.example.booking.service.SimpleUserService;
import com.example.booking.service.AIService;
import com.example.booking.service.AIResponseProcessorService;
import com.example.booking.util.InputSanitizer;

import com.example.booking.websocket.ChatMessageController.ChatMessageRequest;
import com.example.booking.websocket.ChatMessageController.TypingRequest;
import com.example.booking.websocket.ChatMessageController.JoinRoomRequest;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatMessageController Expanded Test Suite")
class ChatMessageControllerExpandedTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ChatService chatService;

    @Mock
    private SimpleUserService userService;

    @Mock
    private InputSanitizer inputSanitizer;

    @Mock
    private AIService aiService;

    @Mock
    private AIResponseProcessorService aiResponseProcessorService;

    @Mock
    private Principal principal;

    @Mock
    private SimpMessageHeaderAccessor headerAccessor;

    @InjectMocks
    private ChatMessageController controller;

    private User testUser;
    private UUID testUserId;
    private String testRoomId;
    private ChatRoom testChatRoom;
    private RestaurantProfile testRestaurant;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testRoomId = "customer_" + testUserId + "_restaurant_1";
        
        testUser = new User();
        testUser.setId(testUserId);
        testUser.setUsername("testuser");
        testUser.setRole(UserRole.CUSTOMER);
        
        testRestaurant = new RestaurantProfile();
        testRestaurant.setRestaurantId(1);
        
        testChatRoom = new ChatRoom();
        testChatRoom.setRoomId(testRoomId);
        testChatRoom.setRestaurant(testRestaurant);
        
        when(principal.getName()).thenReturn("testuser");
        when(headerAccessor.getUser()).thenReturn(principal);
    }

    @Nested
    @DisplayName("sendMessage() Expanded Tests")
    class SendMessageExpandedTests {

        @Test
        @DisplayName("Should handle null content")
        void testSendMessage_ShouldHandleNullContent() {
            ChatMessageRequest request = new ChatMessageRequest(testRoomId, null);
            
            controller.sendMessage(request, headerAccessor);
            
            verify(messagingTemplate, times(1)).convertAndSendToUser(eq("system"), eq("/queue/errors"), any());
            verify(chatService, never()).sendMessage(anyString(), any(UUID.class), anyString(), any(MessageType.class));
        }

        @Test
        @DisplayName("Should handle empty content after sanitization")
        void testSendMessage_ShouldHandleEmptyAfterSanitization() {
            ChatMessageRequest request = new ChatMessageRequest(testRoomId, "valid content");
            
            when(userService.loadUserByUsername("testuser")).thenReturn(testUser);
            when(inputSanitizer.sanitizeChatMessage("valid content")).thenReturn("");
            
            controller.sendMessage(request, headerAccessor);
            
            ArgumentCaptor<Object> errorCaptor = ArgumentCaptor.forClass(Object.class);
            verify(messagingTemplate, times(1)).convertAndSendToUser(
                eq("testuser"), eq("/queue/errors"), errorCaptor.capture());
            verify(chatService, never()).sendMessage(anyString(), any(UUID.class), anyString(), any(MessageType.class));
        }

        @Test
        @DisplayName("Should handle user not authorized to access room")
        void testSendMessage_ShouldHandleUnauthorizedAccess() {
            ChatMessageRequest request = new ChatMessageRequest(testRoomId, "Hello");
            
            when(userService.loadUserByUsername("testuser")).thenReturn(testUser);
            when(inputSanitizer.sanitizeChatMessage("Hello")).thenReturn("Hello");
            when(chatService.canUserAccessRoom(testRoomId, testUserId, UserRole.CUSTOMER)).thenReturn(false);
            
            controller.sendMessage(request, headerAccessor);
            
            verify(chatService, never()).sendMessage(anyString(), any(UUID.class), anyString(), any(MessageType.class));
            verify(messagingTemplate, times(1)).convertAndSendToUser(eq("testuser"), eq("/queue/errors"), any());
        }

        @Test
        @DisplayName("Should handle exception during message sending")
        void testSendMessage_ShouldHandleException() {
            ChatMessageRequest request = new ChatMessageRequest(testRoomId, "Hello");
            
            when(userService.loadUserByUsername("testuser")).thenReturn(testUser);
            when(inputSanitizer.sanitizeChatMessage("Hello")).thenReturn("Hello");
            when(chatService.canUserAccessRoom(testRoomId, testUserId, UserRole.CUSTOMER)).thenReturn(true);
            when(chatService.sendMessage(anyString(), any(UUID.class), anyString(), any(MessageType.class)))
                .thenThrow(new RuntimeException("Database error"));
            
            controller.sendMessage(request, headerAccessor);
            
            verify(messagingTemplate, atLeastOnce()).convertAndSendToUser(anyString(), eq("/queue/errors"), any());
        }

        @Test
        @DisplayName("Should broadcast unread count updates after sending message")
        void testSendMessage_ShouldBroadcastUnreadCountUpdates() {
            ChatMessageRequest request = new ChatMessageRequest(testRoomId, "Hello");
            Message message = new Message();
            message.setMessageId(1);
            
            when(userService.loadUserByUsername("testuser")).thenReturn(testUser);
            when(inputSanitizer.sanitizeChatMessage("Hello")).thenReturn("Hello");
            when(chatService.canUserAccessRoom(testRoomId, testUserId, UserRole.CUSTOMER)).thenReturn(true);
            when(chatService.sendMessage(anyString(), any(UUID.class), anyString(), any(MessageType.class))).thenReturn(message);
            when(chatService.getRoomById(testRoomId)).thenReturn(testChatRoom);
            when(chatService.getChatRoomById(testRoomId)).thenReturn(Optional.of(testChatRoom));
            when(testChatRoom.isCustomerRestaurantChat()).thenReturn(true);
            when(chatService.getRestaurantOwnerId(1)).thenReturn(UUID.randomUUID());
            when(chatService.getUnreadCountForRoom(anyString(), any(UUID.class))).thenReturn(
                Map.of("unreadCount", 0L));
            when(chatService.getTotalUnreadCountForUser(any(UUID.class))).thenReturn(
                Map.of("totalUnreadCount", 0L));
            
            controller.sendMessage(request, headerAccessor);
            
            verify(messagingTemplate, atLeastOnce()).convertAndSend(anyString(), any(Object.class));
        }

        @Test
        @DisplayName("Should handle Principal as UsernamePasswordAuthenticationToken")
        void testSendMessage_ShouldHandleUsernamePasswordAuthenticationToken() {
            ChatMessageRequest request = new ChatMessageRequest(testRoomId, "Hello");
            Message message = new Message();
            
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(testUser, null);
            when(headerAccessor.getUser()).thenReturn(authToken);
            when(inputSanitizer.sanitizeChatMessage("Hello")).thenReturn("Hello");
            when(chatService.canUserAccessRoom(testRoomId, testUserId, UserRole.CUSTOMER)).thenReturn(true);
            when(chatService.sendMessage(anyString(), any(UUID.class), anyString(), any(MessageType.class))).thenReturn(message);
            when(chatService.getRoomById(testRoomId)).thenReturn(testChatRoom);
            when(testRestaurant.getRestaurantId()).thenReturn(1);
            
            controller.sendMessage(request, headerAccessor);
            
            verify(chatService, times(1)).sendMessage(anyString(), eq(testUserId), anyString(), any(MessageType.class));
        }

        @Test
        @DisplayName("Should handle content with only tabs and newlines")
        void testSendMessage_ShouldHandleWhitespaceOnlyContent() {
            ChatMessageRequest request = new ChatMessageRequest(testRoomId, "\t\n  \r\n");
            
            controller.sendMessage(request, headerAccessor);
            
            verify(messagingTemplate, times(1)).convertAndSendToUser(eq("testuser"), eq("/queue/errors"), any());
            verify(chatService, never()).sendMessage(anyString(), any(UUID.class), anyString(), any(MessageType.class));
        }

        @Test
        @DisplayName("Should handle room ID with only spaces")
        void testSendMessage_ShouldHandleSpaceOnlyRoomId() {
            ChatMessageRequest request = new ChatMessageRequest("   ", "Hello");
            
            controller.sendMessage(request, headerAccessor);
            
            verify(messagingTemplate, times(1)).convertAndSendToUser(eq("system"), eq("/queue/errors"), any());
            verify(chatService, never()).sendMessage(anyString(), any(UUID.class), anyString(), any(MessageType.class));
        }
    }

    @Nested
    @DisplayName("handleTyping() Expanded Tests")
    class HandleTypingExpandedTests {

        @Test
        @DisplayName("Should broadcast typing indicator when typing is true")
        void testHandleTyping_ShouldBroadcastWhenTyping() {
            TypingRequest request = new TypingRequest(testRoomId, true);
            
            controller.handleTyping(request, headerAccessor);
            
            ArgumentCaptor<Object> responseCaptor = ArgumentCaptor.forClass(Object.class);
            verify(messagingTemplate, times(1)).convertAndSend(
                eq("/topic/room/" + testRoomId + "/typing"), responseCaptor.capture());
        }

        @Test
        @DisplayName("Should broadcast typing indicator when typing is false")
        void testHandleTyping_ShouldBroadcastWhenNotTyping() {
            TypingRequest request = new TypingRequest(testRoomId, false);
            
            controller.handleTyping(request, headerAccessor);
            
            ArgumentCaptor<Object> responseCaptor = ArgumentCaptor.forClass(Object.class);
            verify(messagingTemplate, times(1)).convertAndSend(
                eq("/topic/room/" + testRoomId + "/typing"), responseCaptor.capture());
        }

        @Test
        @DisplayName("Should handle exception during typing broadcast")
        void testHandleTyping_ShouldHandleException() {
            TypingRequest request = new TypingRequest(testRoomId, true);
            
            doThrow(new RuntimeException("Broadcast error")).when(messagingTemplate)
                .convertAndSend(anyString(), any(Object.class));
            
            assertDoesNotThrow(() -> controller.handleTyping(request, headerAccessor));
        }

        @Test
        @DisplayName("Should not broadcast when principal is null")
        void testHandleTyping_ShouldNotBroadcastWhenPrincipalNull() {
            TypingRequest request = new TypingRequest(testRoomId, true);
            when(headerAccessor.getUser()).thenReturn(null);
            
            controller.handleTyping(request, headerAccessor);
            
            verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
        }
    }

    @Nested
    @DisplayName("joinRoom() Expanded Tests")
    class JoinRoomExpandedTests {

        @Test
        @DisplayName("Should handle null room ID")
        void testJoinRoom_ShouldHandleNullRoomId() {
            JoinRoomRequest request = new JoinRoomRequest(null);
            
            controller.joinRoom(request, headerAccessor);
            
            verify(messagingTemplate, times(1)).convertAndSendToUser(eq("testuser"), eq("/queue/errors"), any());
            verify(chatService, never()).markMessagesAsRead(anyString(), any(UUID.class));
        }

        @Test
        @DisplayName("Should handle empty room ID")
        void testJoinRoom_ShouldHandleEmptyRoomId() {
            JoinRoomRequest request = new JoinRoomRequest("   ");
            
            controller.joinRoom(request, headerAccessor);
            
            verify(messagingTemplate, times(1)).convertAndSendToUser(eq("testuser"), eq("/queue/errors"), any());
            verify(chatService, never()).markMessagesAsRead(anyString(), any(UUID.class));
        }

        @Test
        @DisplayName("Should handle exception during join room")
        void testJoinRoom_ShouldHandleException() {
            JoinRoomRequest request = new JoinRoomRequest(testRoomId);
            
            when(userService.loadUserByUsername("testuser")).thenThrow(new RuntimeException("Service error"));
            
            controller.joinRoom(request, headerAccessor);
            
            verify(messagingTemplate, atLeastOnce()).convertAndSendToUser(anyString(), eq("/queue/errors"), any());
        }

        @Test
        @DisplayName("Should broadcast unread count updates after joining room")
        void testJoinRoom_ShouldBroadcastUnreadCountUpdates() {
            JoinRoomRequest request = new JoinRoomRequest(testRoomId);
            
            when(userService.loadUserByUsername("testuser")).thenReturn(testUser);
            when(chatService.canUserAccessRoom(testRoomId, testUserId, UserRole.CUSTOMER)).thenReturn(true);
            when(chatService.markMessagesAsRead(testRoomId, testUserId)).thenReturn(5);
            when(chatService.getChatRoomById(testRoomId)).thenReturn(Optional.of(testChatRoom));
            when(testChatRoom.isCustomerRestaurantChat()).thenReturn(true);
            when(chatService.getRestaurantOwnerId(1)).thenReturn(UUID.randomUUID());
            when(chatService.getUnreadCountForRoom(anyString(), any(UUID.class))).thenReturn(
                Map.of("unreadCount", 0L));
            when(chatService.getTotalUnreadCountForUser(any(UUID.class))).thenReturn(
                Map.of("totalUnreadCount", 0L));
            
            controller.joinRoom(request, headerAccessor);
            
            verify(chatService, times(1)).markMessagesAsRead(testRoomId, testUserId);
            verify(messagingTemplate, atLeastOnce()).convertAndSendToUser(anyString(), eq("/queue/unread-updates"), any());
        }

        @Test
        @DisplayName("Should handle zero updated messages count")
        void testJoinRoom_ShouldHandleZeroUpdatedMessages() {
            JoinRoomRequest request = new JoinRoomRequest(testRoomId);
            
            when(userService.loadUserByUsername("testuser")).thenReturn(testUser);
            when(chatService.canUserAccessRoom(testRoomId, testUserId, UserRole.CUSTOMER)).thenReturn(true);
            when(chatService.markMessagesAsRead(testRoomId, testUserId)).thenReturn(0);
            when(chatService.getChatRoomById(testRoomId)).thenReturn(Optional.of(testChatRoom));
            when(testChatRoom.isCustomerRestaurantChat()).thenReturn(true);
            when(chatService.getRestaurantOwnerId(1)).thenReturn(UUID.randomUUID());
            when(chatService.getUnreadCountForRoom(anyString(), any(UUID.class))).thenReturn(
                Map.of("unreadCount", 0L));
            when(chatService.getTotalUnreadCountForUser(any(UUID.class))).thenReturn(
                Map.of("totalUnreadCount", 0L));
            
            controller.joinRoom(request, headerAccessor);
            
            verify(chatService, times(1)).markMessagesAsRead(testRoomId, testUserId);
        }

        @Test
        @DisplayName("Should handle admin-restaurant chat room")
        void testJoinRoom_ShouldHandleAdminRestaurantChat() {
            String adminRoomId = "admin_" + testUserId + "_restaurant_1";
            JoinRoomRequest request = new JoinRoomRequest(adminRoomId);
            
            User adminUser = new User();
            adminUser.setId(testUserId);
            adminUser.setRole(UserRole.ADMIN);
            
            ChatRoom adminChatRoom = new ChatRoom();
            adminChatRoom.setRoomId(adminRoomId);
            adminChatRoom.setRestaurant(testRestaurant);
            
            when(userService.loadUserByUsername("testuser")).thenReturn(adminUser);
            when(chatService.canUserAccessRoom(adminRoomId, testUserId, UserRole.ADMIN)).thenReturn(true);
            when(chatService.markMessagesAsRead(adminRoomId, testUserId)).thenReturn(3);
            when(chatService.getChatRoomById(adminRoomId)).thenReturn(Optional.of(adminChatRoom));
            when(adminChatRoom.isAdminRestaurantChat()).thenReturn(true);
            when(chatService.getUnreadCountForRoom(anyString(), any(UUID.class))).thenReturn(
                Map.of("unreadCount", 0L));
            when(chatService.getTotalUnreadCountForUser(any(UUID.class))).thenReturn(
                Map.of("totalUnreadCount", 0L));
            
            controller.joinRoom(request, headerAccessor);
            
            verify(chatService, times(1)).markMessagesAsRead(adminRoomId, testUserId);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle exception when sending error message fails")
        void testErrorHandling_ShouldHandleErrorSendFailure() {
            ChatMessageRequest request = new ChatMessageRequest(null, "Hello");
            
            doThrow(new RuntimeException("Messaging error")).when(messagingTemplate)
                .convertAndSendToUser(anyString(), anyString(), any());
            
            // Should not throw exception
            assertDoesNotThrow(() -> controller.sendMessage(request, headerAccessor));
        }
    }

    @Nested
    @DisplayName("AI Restaurant Message Tests")
    class AIRestaurantMessageTests {

        @Test
        @DisplayName("Should process AI response when message sent to AI restaurant")
        void testSendMessage_ShouldProcessAIResponse() {
            String aiRoomId = "customer_" + testUserId + "_restaurant_37";
            ChatMessageRequest request = new ChatMessageRequest(aiRoomId, "Hello AI");
            Message customerMessage = new Message();
            customerMessage.setMessageId(1);
            
            ChatRoom aiChatRoom = new ChatRoom();
            RestaurantProfile aiRestaurant = new RestaurantProfile();
            aiRestaurant.setRestaurantId(37);
            aiChatRoom.setRoomId(aiRoomId);
            aiChatRoom.setRestaurant(aiRestaurant);
            
            when(userService.loadUserByUsername("testuser")).thenReturn(testUser);
            when(inputSanitizer.sanitizeChatMessage("Hello AI")).thenReturn("Hello AI");
            when(chatService.canUserAccessRoom(aiRoomId, testUserId, UserRole.CUSTOMER)).thenReturn(true);
            when(chatService.sendMessage(anyString(), any(UUID.class), anyString(), any(MessageType.class)))
                .thenReturn(customerMessage);
            when(chatService.getRoomById(aiRoomId)).thenReturn(aiChatRoom);
            
            controller.sendMessage(request, headerAccessor);
            
            verify(chatService, times(1)).sendMessage(eq(aiRoomId), eq(testUserId), eq("Hello AI"), any(MessageType.class));
            verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/room/" + aiRoomId), any(Object.class));
        }
    }
}

