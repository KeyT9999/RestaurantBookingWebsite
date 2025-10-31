package com.example.booking.web.controller.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.example.booking.domain.ChatRoom;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.dto.ChatRoomDto;
import com.example.booking.service.ChatService;
import com.example.booking.service.RestaurantManagementService;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.SimpleUserService;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for ChatApiController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChatApiController Tests")
public class ChatApiControllerTest {

    @Mock
    private ChatService chatService;

    @Mock
    private RestaurantManagementService restaurantService;

    @Mock
    private RestaurantOwnerService restaurantOwnerService;

    @Mock
    private SimpleUserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ChatApiController controller;

    private User user;
    private ChatRoom chatRoom;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");

        chatRoom = new ChatRoom();
        chatRoom.setRoomId("room-123");
    }

    // ========== createChatRoomWithRestaurant() Tests ==========

    @Test
    @DisplayName("shouldCreateChatRoomWithRestaurant_successfully")
    void shouldCreateChatRoomWithRestaurant_successfully() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        user.setRole(com.example.booking.domain.UserRole.ADMIN);
        
        com.example.booking.domain.RestaurantProfile restaurant = new com.example.booking.domain.RestaurantProfile();
        restaurant.setRestaurantId(1);
        chatRoom.setRestaurant(restaurant);
        
        when(chatService.createAdminRestaurantRoom(user.getId(), 1))
            .thenReturn(chatRoom);

        // When
        ResponseEntity<?> response = controller.createChatRoomWithRestaurant(1, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    // ========== getMessages() Tests ==========

    @Test
    @DisplayName("shouldGetMessages_successfully")
    void shouldGetMessages_successfully() {
        // Given
        List<com.example.booking.dto.ChatMessageDto> messages = new ArrayList<>();
        com.example.booking.dto.ChatMessageDto message = new com.example.booking.dto.ChatMessageDto();
        messages.add(message);

        when(authentication.getPrincipal()).thenReturn(user);
        when(chatService.canUserAccessRoom("room-123", user.getId(), user.getRole())).thenReturn(true);
        when(chatService.getMessages("room-123", 0, 200)).thenReturn(messages);

        // When
        ResponseEntity<?> response = controller.getMessages("room-123", 0, 200, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    // ========== getUnreadCount() Tests ==========

    @Test
    @DisplayName("shouldGetUnreadCount_successfully")
    void shouldGetUnreadCount_successfully() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        when(chatService.getUnreadMessageCount(user.getId())).thenReturn(5L);

        // When
        ResponseEntity<?> response = controller.getUnreadCount(authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("shouldReturn403_whenNotAdminForCreateChatRoom")
    void shouldReturn403_whenNotAdminForCreateChatRoom() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        user.setRole(com.example.booking.domain.UserRole.CUSTOMER);

        // When
        ResponseEntity<?> response = controller.createChatRoomWithRestaurant(1, authentication);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("shouldHandleError_whenCreateChatRoomFails")
    void shouldHandleError_whenCreateChatRoomFails() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        user.setRole(com.example.booking.domain.UserRole.ADMIN);
        when(chatService.createAdminRestaurantRoom(user.getId(), 1))
            .thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<?> response = controller.createChatRoomWithRestaurant(1, authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("shouldReturn403_whenCannotAccessRoom")
    void shouldReturn403_whenCannotAccessRoom() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        user.setRole(com.example.booking.domain.UserRole.CUSTOMER);
        when(chatService.canUserAccessRoom("room-123", user.getId(), user.getRole())).thenReturn(false);

        // When
        ResponseEntity<?> response = controller.getMessages("room-123", 0, 200, authentication);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("shouldHandleError_whenGetMessagesFails")
    void shouldHandleError_whenGetMessagesFails() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        when(chatService.canUserAccessRoom("room-123", user.getId(), user.getRole())).thenReturn(true);
        when(chatService.getMessages("room-123", 0, 200))
            .thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<?> response = controller.getMessages("room-123", 0, 200, authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("shouldHandleError_whenGetUnreadCountFails")
    void shouldHandleError_whenGetUnreadCountFails() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        when(chatService.getUnreadMessageCount(user.getId()))
            .thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<?> response = controller.getUnreadCount(authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ========== getAvailableRestaurants() Tests ==========

    @Test
    @DisplayName("shouldGetAvailableRestaurants_forCustomer")
    void shouldGetAvailableRestaurants_forCustomer() {
        // Given
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");

        when(authentication.getPrincipal()).thenReturn(user);
        user.setRole(UserRole.CUSTOMER);
        when(restaurantService.findAllRestaurants()).thenReturn(Arrays.asList(restaurant));
        when(chatService.getExistingRoomId(any(), any(), any())).thenReturn(null);

        // When
        ResponseEntity<?> response = controller.getAvailableRestaurants(authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("shouldGetAvailableRestaurants_forAdmin")
    void shouldGetAvailableRestaurants_forAdmin() {
        // Given
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");

        when(authentication.getPrincipal()).thenReturn(user);
        user.setRole(UserRole.ADMIN);
        when(restaurantService.findAllRestaurants()).thenReturn(Arrays.asList(restaurant));
        when(chatService.getExistingRoomId(any(), any(), any())).thenReturn(null);

        // When
        ResponseEntity<?> response = controller.getAvailableRestaurants(authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("shouldGetAvailableRestaurants_forRestaurantOwner")
    void shouldGetAvailableRestaurants_forRestaurantOwner() {
        // Given
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");

        when(authentication.getPrincipal()).thenReturn(user);
        user.setRole(UserRole.RESTAURANT_OWNER);
        when(restaurantOwnerService.getRestaurantsByUserId(user.getId())).thenReturn(Arrays.asList(restaurant));
        when(chatService.getExistingRoomId(any(), any(), any())).thenReturn(null);

        // When
        ResponseEntity<?> response = controller.getAvailableRestaurants(authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("shouldReturnError_forInvalidRoleInGetAvailableRestaurants")
    void shouldReturnError_forInvalidRoleInGetAvailableRestaurants() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        user.setRole(null); // Invalid role

        // When
        ResponseEntity<?> response = controller.getAvailableRestaurants(authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("shouldHandleError_whenGetAvailableRestaurantsFails")
    void shouldHandleError_whenGetAvailableRestaurantsFails() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        user.setRole(UserRole.CUSTOMER);
        when(restaurantService.findAllRestaurants())
            .thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<?> response = controller.getAvailableRestaurants(authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ========== getAvailableAdmins() Tests ==========

    @Test
    @DisplayName("shouldGetAvailableAdmins_successfully")
    void shouldGetAvailableAdmins_successfully() {
        // Given
        User admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setRole(UserRole.ADMIN);

        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);

        when(authentication.getPrincipal()).thenReturn(user);
        user.setRole(UserRole.RESTAURANT_OWNER);
        when(chatService.getAvailableAdmins()).thenReturn(Arrays.asList(admin));
        when(restaurantOwnerService.getRestaurantsByUserId(user.getId()))
            .thenReturn(Arrays.asList(restaurant));

        // When
        ResponseEntity<?> response = controller.getAvailableAdmins(authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("shouldReturn403_whenNotRestaurantOwner")
    void shouldReturn403_whenNotRestaurantOwner() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        user.setRole(UserRole.CUSTOMER);

        // When
        ResponseEntity<?> response = controller.getAvailableAdmins(authentication);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("shouldHandleError_whenGetAvailableAdminsFails")
    void shouldHandleError_whenGetAvailableAdminsFails() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        user.setRole(UserRole.RESTAURANT_OWNER);
        when(chatService.getAvailableAdmins())
            .thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<?> response = controller.getAvailableAdmins(authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ========== getUserChatRooms() Tests ==========

    @Test
    @DisplayName("shouldGetUserChatRooms_successfully")
    void shouldGetUserChatRooms_successfully() {
        // Given
        ChatRoomDto room = new ChatRoomDto();
        List<ChatRoomDto> rooms = Arrays.asList(room);

        when(authentication.getPrincipal()).thenReturn(user);
        when(chatService.getUserChatRooms(user.getId(), user.getRole())).thenReturn(rooms);

        // When
        ResponseEntity<?> response = controller.getUserChatRooms(authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("shouldHandleError_whenGetUserChatRoomsFails")
    void shouldHandleError_whenGetUserChatRoomsFails() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        when(chatService.getUserChatRooms(user.getId(), user.getRole()))
            .thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<?> response = controller.getUserChatRooms(authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ========== createChatRoom() Tests ==========

    @Test
    @DisplayName("shouldCreateChatRoom_forCustomer")
    void shouldCreateChatRoom_forCustomer() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        user.setRole(UserRole.CUSTOMER);
        when(chatService.canUserChatWithRestaurant(user.getId(), user.getRole(), 1)).thenReturn(true);
        doNothing().when(chatService).createCustomerRestaurantRoom(user.getId(), 1);
        when(chatService.getRoomId(user.getId(), user.getRole(), 1)).thenReturn("room-123");

        // When
        ResponseEntity<?> response = controller.createChatRoom(1, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("shouldCreateChatRoom_forAdmin")
    void shouldCreateChatRoom_forAdmin() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        user.setRole(UserRole.ADMIN);
        when(chatService.canUserChatWithRestaurant(user.getId(), user.getRole(), 1)).thenReturn(true);
        doNothing().when(chatService).createAdminRestaurantRoom(user.getId(), 1);
        when(chatService.getRoomId(user.getId(), user.getRole(), 1)).thenReturn("room-123");

        // When
        ResponseEntity<?> response = controller.createChatRoom(1, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("shouldReturnError_whenNotAuthorized")
    void shouldReturnError_whenNotAuthorized() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        user.setRole(UserRole.CUSTOMER);
        when(chatService.canUserChatWithRestaurant(user.getId(), user.getRole(), 1)).thenReturn(false);

        // When
        ResponseEntity<?> response = controller.createChatRoom(1, authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("shouldReturnError_forInvalidRoleInCreateChatRoom")
    void shouldReturnError_forInvalidRoleInCreateChatRoom() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        user.setRole(null); // Invalid role
        when(chatService.canUserChatWithRestaurant(user.getId(), user.getRole(), 1)).thenReturn(true);

        // When
        ResponseEntity<?> response = controller.createChatRoom(1, authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ========== createChatRoomWithAdmin() Tests ==========

    @Test
    @DisplayName("shouldCreateChatRoomWithAdmin_successfully")
    void shouldCreateChatRoomWithAdmin_successfully() {
        // Given
        UUID adminId = UUID.randomUUID();
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        chatRoom.setRestaurant(restaurant);

        when(authentication.getPrincipal()).thenReturn(user);
        user.setRole(UserRole.RESTAURANT_OWNER);
        when(chatService.createRestaurantOwnerAdminRoom(user.getId(), adminId, 1L))
            .thenReturn(chatRoom);

        // When
        ResponseEntity<?> response = controller.createChatRoomWithAdmin(adminId, 1L, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("shouldReturn403_whenNotRestaurantOwnerForAdminChat")
    void shouldReturn403_whenNotRestaurantOwnerForAdminChat() {
        // Given
        UUID adminId = UUID.randomUUID();
        when(authentication.getPrincipal()).thenReturn(user);
        user.setRole(UserRole.CUSTOMER);

        // When
        ResponseEntity<?> response = controller.createChatRoomWithAdmin(adminId, 1L, authentication);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    // ========== markMessagesAsRead() Tests ==========

    @Test
    @DisplayName("shouldMarkMessagesAsRead_successfully")
    void shouldMarkMessagesAsRead_successfully() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        doNothing().when(chatService).markMessagesAsRead("room-123", user.getId());

        // When
        ResponseEntity<?> response = controller.markMessagesAsRead("room-123", authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(chatService, times(1)).markMessagesAsRead("room-123", user.getId());
    }

    @Test
    @DisplayName("shouldHandleError_whenMarkMessagesAsReadFails")
    void shouldHandleError_whenMarkMessagesAsReadFails() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        doThrow(new RuntimeException("Service error"))
            .when(chatService).markMessagesAsRead("room-123", user.getId());

        // When
        ResponseEntity<?> response = controller.markMessagesAsRead("room-123", authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ========== getUnreadCountForRoom() Tests ==========

    @Test
    @DisplayName("shouldGetUnreadCountForRoom_successfully")
    void shouldGetUnreadCountForRoom_successfully() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        when(chatService.canUserAccessRoom("room-123", user.getId(), user.getRole())).thenReturn(true);
        when(chatService.getUnreadMessageCountForRoom("room-123", user.getId())).thenReturn(3L);

        // When
        ResponseEntity<?> response = controller.getUnreadCountForRoom("room-123", authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("shouldReturn403_whenCannotAccessRoomForUnreadCount")
    void shouldReturn403_whenCannotAccessRoomForUnreadCount() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        when(chatService.canUserAccessRoom("room-123", user.getId(), user.getRole())).thenReturn(false);

        // When
        ResponseEntity<?> response = controller.getUnreadCountForRoom("room-123", authentication);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    // ========== archiveChatRoom() Tests ==========

    @Test
    @DisplayName("shouldArchiveChatRoom_successfully")
    void shouldArchiveChatRoom_successfully() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        when(chatService.canUserAccessRoom("room-123", user.getId(), user.getRole())).thenReturn(true);
        doNothing().when(chatService).archiveChatRoom("room-123");

        // When
        ResponseEntity<?> response = controller.archiveChatRoom("room-123", authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(chatService, times(1)).archiveChatRoom("room-123");
    }

    @Test
    @DisplayName("shouldReturn403_whenCannotAccessRoomForArchive")
    void shouldReturn403_whenCannotAccessRoomForArchive() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        when(chatService.canUserAccessRoom("room-123", user.getId(), user.getRole())).thenReturn(false);

        // When
        ResponseEntity<?> response = controller.archiveChatRoom("room-123", authentication);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    // ========== getChatStatistics() Tests ==========

    @Test
    @DisplayName("shouldGetChatStatistics_successfully")
    void shouldGetChatStatistics_successfully() {
        // Given
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRooms", 10);
        stats.put("totalMessages", 100);

        when(authentication.getPrincipal()).thenReturn(user);
        user.setRole(UserRole.ADMIN);
        when(chatService.getChatStatistics()).thenReturn(stats);

        // When
        ResponseEntity<?> response = controller.getChatStatistics(authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("shouldReturn403_whenNotAdminForStatistics")
    void shouldReturn403_whenNotAdminForStatistics() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        user.setRole(UserRole.CUSTOMER);

        // When
        ResponseEntity<?> response = controller.getChatStatistics(authentication);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("shouldHandleError_whenGetChatStatisticsFails")
    void shouldHandleError_whenGetChatStatisticsFails() {
        // Given
        when(authentication.getPrincipal()).thenReturn(user);
        user.setRole(UserRole.ADMIN);
        when(chatService.getChatStatistics())
            .thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<?> response = controller.getChatStatistics(authentication);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
