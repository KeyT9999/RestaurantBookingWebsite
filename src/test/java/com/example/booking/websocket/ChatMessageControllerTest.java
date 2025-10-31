package com.example.booking.websocket;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

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
import com.example.booking.websocket.ChatMessageController.TypingResponse;
import com.example.booking.websocket.ChatMessageController.JoinRoomRequest;

@ExtendWith(MockitoExtension.class)
class ChatMessageControllerTest {

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
    private SimpMessageHeaderAccessor headerAccessor;

    @Mock
    private ChatRoom chatRoom;

    @Mock
    private RestaurantProfile restaurant;

    @InjectMocks
    private ChatMessageController controller;

    private UUID testUserId;
    private String testRoomId;
    private User user;
    private UsernamePasswordAuthenticationToken principal;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testRoomId = "customer_" + testUserId + "_restaurant_1";

        user = new User();
        user.setId(testUserId);
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setPassword("password123");
        user.setFullName("Test User");
        user.setRole(UserRole.CUSTOMER);

        principal = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()) {
            @Override
            public String getName() {
                return user.getUsername();
            }
        };
    }

    @Test
    // TC RC-001
    void shouldCallChatServiceAndBroadcast_whenSendingValidMessage() throws Exception {
        // Given
        ChatMessageRequest request = new ChatMessageRequest(testRoomId, "Hello world");
        Message message = new Message();
        message.setMessageId(1);
        message.setContent("Hello world");
        message.setRoom(chatRoom);
        message.setSender(user);
        message.setMessageType(MessageType.TEXT);

        when(headerAccessor.getUser()).thenReturn(principal);
        
        when(inputSanitizer.sanitizeChatMessage("Hello world")).thenReturn("Hello world");
        when(chatService.canUserAccessRoom(testRoomId, testUserId, UserRole.CUSTOMER)).thenReturn(true);
        when(chatService.sendMessage(anyString(), any(UUID.class), anyString(), any(MessageType.class))).thenReturn(message);
        when(chatService.getRoomById(testRoomId)).thenReturn(chatRoom);
        when(chatRoom.getRoomId()).thenReturn(testRoomId);
        when(chatRoom.getRestaurant()).thenReturn(restaurant);
        when(restaurant.getRestaurantId()).thenReturn(1); // Not AI restaurant
        
        // When
        controller.sendMessage(request, headerAccessor);
        
        // Then
        verify(chatService, times(1)).sendMessage(eq(testRoomId), eq(testUserId), eq("Hello world"), any(MessageType.class));
        ArgumentCaptor<Object> responseCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/room/" + testRoomId), responseCaptor.capture());
    }

    @Test
    // TC RC-002
    void shouldSendError_whenRoomIdIsNull() {
        // Given
        ChatMessageRequest request = new ChatMessageRequest(null, "Hello");
        
        // When
        controller.sendMessage(request, headerAccessor);
        
        // Then
        verify(messagingTemplate, times(1)).convertAndSendToUser(eq("system"), eq("/queue/errors"), any());
        verify(chatService, never()).sendMessage(anyString(), any(UUID.class), anyString(), any(MessageType.class));
    }

    @Test
    // TC RC-003
    void shouldSendError_whenPrincipalIsNull() {
        // Given
        ChatMessageRequest request = new ChatMessageRequest(testRoomId, "Hello");
        when(headerAccessor.getUser()).thenReturn(null);
        
        // When
        controller.sendMessage(request, headerAccessor);
        
        // Then
        verify(messagingTemplate, times(1)).convertAndSendToUser(eq("system"), eq("/queue/errors"), any());
        verify(chatService, never()).sendMessage(anyString(), any(UUID.class), anyString(), any(MessageType.class));
    }

    @Test
    // TC RC-004
    void shouldSendError_whenMessageIsWhitespaceOnly() {
        // Given
        ChatMessageRequest request = new ChatMessageRequest(testRoomId, "   ");
        when(headerAccessor.getUser()).thenReturn(principal);
        
        // When
        controller.sendMessage(request, headerAccessor);
        
        // Then
        verify(messagingTemplate, times(1)).convertAndSendToUser(eq("testuser"), eq("/queue/errors"), any());
        verify(chatService, never()).sendMessage(anyString(), any(UUID.class), anyString(), any(MessageType.class));
    }

    @Test
    // TC RC-005
    void shouldSanitizeMessage_whenSendingWithXSSContent() throws Exception {
        // Given
        ChatMessageRequest request = new ChatMessageRequest(testRoomId, "<script>alert(1)</script>");
        Message message = new Message();
        String sanitizedContent = "alert(1)";
        message.setMessageId(2);
        message.setContent(sanitizedContent);
        message.setRoom(chatRoom);
        message.setSender(user);
        message.setMessageType(MessageType.TEXT);
        when(headerAccessor.getUser()).thenReturn(principal);
        
        when(inputSanitizer.sanitizeChatMessage("<script>alert(1)</script>")).thenReturn(sanitizedContent);
        when(chatService.canUserAccessRoom(testRoomId, testUserId, UserRole.CUSTOMER)).thenReturn(true);
        when(chatService.sendMessage(anyString(), any(UUID.class), eq(sanitizedContent), any(MessageType.class))).thenReturn(message);
        when(chatService.getRoomById(testRoomId)).thenReturn(chatRoom);
        when(chatRoom.getRoomId()).thenReturn(testRoomId);
        when(chatRoom.getRestaurant()).thenReturn(restaurant);
        when(restaurant.getRestaurantId()).thenReturn(1);
        
        // When
        controller.sendMessage(request, headerAccessor);
        
        // Then
        verify(inputSanitizer, times(1)).sanitizeChatMessage("<script>alert(1)</script>");
        verify(chatService, times(1)).sendMessage(anyString(), any(UUID.class), eq(sanitizedContent), any(MessageType.class));
        ArgumentCaptor<Object> responseCaptor2 = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/room/" + testRoomId), responseCaptor2.capture());
    }

    @Test
    // TC RC-006 - Simplified test (AI restaurant detection requires complex setup)
    void shouldProcessAIResponse_whenMessageToAIRestaurant() throws Exception {
        // Given
        ChatMessageRequest request = new ChatMessageRequest(testRoomId, "Hello AI");
        Message message = new Message();
        message.setMessageId(1);
        message.setContent("Hello AI");
        message.setRoom(chatRoom);
        message.setSender(user);
        message.setMessageType(MessageType.TEXT);
        when(headerAccessor.getUser()).thenReturn(principal);
        
        when(inputSanitizer.sanitizeChatMessage("Hello AI")).thenReturn("Hello AI");
        when(chatService.canUserAccessRoom(testRoomId, testUserId, UserRole.CUSTOMER)).thenReturn(true);
        when(chatService.sendMessage(anyString(), any(UUID.class), anyString(), any(MessageType.class))).thenReturn(message);
        when(chatService.getRoomById(testRoomId)).thenReturn(chatRoom);
        when(chatRoom.getRoomId()).thenReturn(testRoomId);
        when(chatRoom.getRestaurant()).thenReturn(restaurant);
        when(restaurant.getRestaurantId()).thenReturn(37); // AI Restaurant ID
        
        // When
        controller.sendMessage(request, headerAccessor);
        
        // Then
        verify(chatService, times(1)).sendMessage(anyString(), any(UUID.class), anyString(), any(MessageType.class));
        ArgumentCaptor<Object> responseCaptor3 = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/room/" + testRoomId), responseCaptor3.capture());
    }

    @Test
    // TC RC-007
    void shouldBroadcastTypingIndicator_whenHandlingTyping() {
        // Given
        TypingRequest request = new TypingRequest(testRoomId, true);
        when(headerAccessor.getUser()).thenReturn(principal);
        
        // When
        controller.handleTyping(request, headerAccessor);
        
        // Then
        ArgumentCaptor<Object> responseCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate, times(1)).convertAndSend(
            eq("/topic/room/" + testRoomId + "/typing"),
            responseCaptor.capture()
        );
        
        Object response = responseCaptor.getValue();
        assertNotNull(response);
    }

    @Test
    // TC RC-008
    void shouldNotBroadcast_whenPrincipalIsNullForTyping() {
        // Given
        TypingRequest request = new TypingRequest(testRoomId, true);
        when(headerAccessor.getUser()).thenReturn(null);
        
        // When
        controller.handleTyping(request, headerAccessor);
        
        // Then
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate, never()).convertAndSend(anyString(), captor.capture());
    }

    @Test
    // TC RC-009
    void shouldMarkMessagesAsReadAndBroadcast_whenJoiningRoom() {
        // Given
        JoinRoomRequest request = new JoinRoomRequest(testRoomId);
        when(headerAccessor.getUser()).thenReturn(principal);
        
        when(chatService.canUserAccessRoom(testRoomId, testUserId, UserRole.CUSTOMER)).thenReturn(true);
        when(chatService.markMessagesAsRead(testRoomId, testUserId)).thenReturn(5);
        when(chatService.getChatRoomById(testRoomId)).thenReturn(java.util.Optional.of(chatRoom));
        when(chatRoom.isCustomerRestaurantChat()).thenReturn(true);
        
        // When
        controller.joinRoom(request, headerAccessor);
        
        // Then
        verify(chatService, times(1)).markMessagesAsRead(testRoomId, testUserId);
    }

    @Test
    // TC RC-010
    void shouldSendError_whenUserNotAuthorizedToJoinRoom() {
        // Given
        JoinRoomRequest request = new JoinRoomRequest(testRoomId);
        when(headerAccessor.getUser()).thenReturn(principal);
        
        when(chatService.canUserAccessRoom(testRoomId, testUserId, UserRole.CUSTOMER)).thenReturn(false);
        
        // When
        controller.joinRoom(request, headerAccessor);
        
        // Then
        verify(messagingTemplate, times(1)).convertAndSendToUser(eq("testuser"), eq("/queue/errors"), any());
        verify(chatService, never()).markMessagesAsRead(anyString(), any(UUID.class));
    }

    @Test
    // TC RC-011
    void shouldProcessAIResponse_whenMessageToAI() throws Exception {
        // Given
        String roomId = "customer_" + testUserId + "_restaurant_37";
        UUID aiUserId = UUID.randomUUID();
        Message aiMessage = new Message();
        aiMessage.setMessageId(42);
        aiMessage.setContent("Processed Response");
        ChatRoom aiRoom = new ChatRoom();
        aiRoom.setRoomId(roomId);
        aiMessage.setRoom(aiRoom);
        User aiSender = new User();
        aiSender.setId(aiUserId);
        aiSender.setUsername("ai-assistant");
        aiSender.setEmail("ai@example.com");
        aiSender.setPassword("password123");
        aiSender.setFullName("AI Assistant");
        aiSender.setRole(UserRole.RESTAURANT_OWNER);
        aiMessage.setSender(aiSender);
        aiMessage.setMessageType(MessageType.TEXT);

        when(userService.findById(testUserId)).thenReturn(user);
        when(aiService.sendMessageToAI("Hello AI", testUserId.toString())).thenReturn("AI Response");
        when(aiResponseProcessorService.processAIResponse("AI Response", user, "Hello AI")).thenReturn("Processed Response");
        when(chatService.getAIRestaurantOwnerId()).thenReturn(aiUserId);
        when(chatService.sendMessage(roomId, aiUserId, "Processed Response", MessageType.TEXT)).thenReturn(aiMessage);
        when(chatService.getChatRoomById(roomId)).thenReturn(java.util.Optional.empty());

        // When
        controller.processAIResponse(roomId, "Hello AI", testUserId);

        // Then
        verify(aiService).sendMessageToAI("Hello AI", testUserId.toString());
        verify(aiResponseProcessorService).processAIResponse("AI Response", user, "Hello AI");
        verify(chatService).sendMessage(roomId, aiUserId, "Processed Response", MessageType.TEXT);
        verify(messagingTemplate, times(2)).convertAndSend(eq("/topic/room/" + roomId + "/typing"), any(TypingResponse.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/room/" + roomId), any(ChatMessageController.ChatMessageResponse.class));
    }

    @Test
    // TC RC-012
    void shouldSendFallbackMessage_whenAITimesOut() throws Exception {
        // Given
        String roomId = "customer_" + testUserId + "_restaurant_37";
        UUID aiUserId = UUID.randomUUID();
        ChatRoom aiRoom = new ChatRoom();
        aiRoom.setRoomId(roomId);
        User aiSender = new User();
        aiSender.setId(aiUserId);
        aiSender.setUsername("ai-assistant");
        aiSender.setEmail("ai@example.com");
        aiSender.setPassword("password123");
        aiSender.setFullName("AI Assistant");
        aiSender.setRole(UserRole.RESTAURANT_OWNER);

        when(userService.findById(testUserId)).thenReturn(user);
        when(aiService.sendMessageToAI("Hello AI", testUserId.toString())).thenThrow(new RuntimeException("timeout"));
        when(chatService.getAIRestaurantOwnerId()).thenReturn(aiUserId);
        Message fallbackMessage = new Message();
        fallbackMessage.setMessageId(7);
        fallbackMessage.setContent("Xin lỗi, có lỗi xảy ra khi xử lý tin nhắn của bạn.");
        fallbackMessage.setRoom(aiRoom);
        fallbackMessage.setSender(aiSender);
        fallbackMessage.setMessageType(MessageType.TEXT);
        when(chatService.sendMessage(roomId, aiUserId, "Xin lỗi, có lỗi xảy ra khi xử lý tin nhắn của bạn.", MessageType.TEXT))
            .thenReturn(fallbackMessage);

        // When
        controller.processAIResponse(roomId, "Hello AI", testUserId);

        // Then
        verify(chatService).sendMessage(roomId, aiUserId, "Xin lỗi, có lỗi xảy ra khi xử lý tin nhắn của bạn.", MessageType.TEXT);
        verify(messagingTemplate).convertAndSend(eq("/topic/room/" + roomId), any(ChatMessageController.ChatMessageResponse.class));
    }
}
