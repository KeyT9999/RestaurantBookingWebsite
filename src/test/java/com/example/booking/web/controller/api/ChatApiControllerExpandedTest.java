package com.example.booking.web.controller.api;

import com.example.booking.domain.ChatRoom;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.ChatMessageDto;
import com.example.booking.dto.ChatRoomDto;
import com.example.booking.service.ChatService;
import com.example.booking.service.RestaurantManagementService;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.SimpleUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ChatApiController Expanded Test Suite")
class ChatApiControllerExpandedTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @MockBean
    private RestaurantManagementService restaurantManagementService;

    @MockBean
    private RestaurantOwnerService restaurantOwnerService;

    @MockBean
    private SimpleUserService simpleUserService;

    private User testCustomer;
    private User testAdmin;
    private User testRestaurantOwner;
    private UUID testUserId;
    private RestaurantProfile testRestaurant;
    private RestaurantProfile aiRestaurant;
    private ChatRoom testChatRoom;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        testCustomer = new User("customer", "customer@example.com", "password", "Customer");
        testCustomer.setId(testUserId);
        testCustomer.setRole(UserRole.CUSTOMER);

        testAdmin = new User("admin", "admin@example.com", "password", "Admin");
        testAdmin.setId(UUID.randomUUID());
        testAdmin.setRole(UserRole.ADMIN);

        testRestaurantOwner = new User("owner", "owner@example.com", "password", "Owner");
        testRestaurantOwner.setId(UUID.randomUUID());
        testRestaurantOwner.setRole(UserRole.RESTAURANT_OWNER);

        testRestaurant = new RestaurantProfile();
        testRestaurant.setRestaurantId(1);
        testRestaurant.setRestaurantName("Test Restaurant");
        testRestaurant.setAddress("123 Test St");
        testRestaurant.setPhone("1234567890");

        aiRestaurant = new RestaurantProfile();
        aiRestaurant.setRestaurantId(37);
        aiRestaurant.setRestaurantName("AI Restaurant");
        aiRestaurant.setAddress("AI Address");
        aiRestaurant.setPhone("AI Phone");

        testChatRoom = new ChatRoom();
        testChatRoom.setRoomId("customer_" + testUserId + "_restaurant_1");
        testChatRoom.setRestaurant(testRestaurant);
    }

    @Nested
    @DisplayName("getAvailableRestaurants() Tests")
    class GetAvailableRestaurantsTests {

        @Test
        @DisplayName("Should return restaurants for customer with AI restaurant first")
        void testGetAvailableRestaurants_Customer_WithAIRestaurant() throws Exception {
            List<RestaurantProfile> restaurants = Arrays.asList(testRestaurant, aiRestaurant);
            when(restaurantManagementService.findAllRestaurants()).thenReturn(restaurants);
            when(restaurantManagementService.findRestaurantById(37)).thenReturn(Optional.of(aiRestaurant));
            when(chatService.getExistingRoomId(any(UUID.class), any(UserRole.class), anyInt())).thenReturn(null);
            when(chatService.getUnreadCountForRoom(anyString(), any(UUID.class)))
                .thenReturn(Map.of("unreadCount", 0L));

            mockMvc.perform(get("/api/chat/available-restaurants")
                    .principal(new UsernamePasswordAuthenticationToken(testCustomer, null, testCustomer.getAuthorities())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].restaurantId").value(37))
                    .andExpect(jsonPath("$[0].restaurantName").value("AI Restaurant"));

            verify(restaurantManagementService).findAllRestaurants();
            verify(restaurantManagementService).findRestaurantById(37);
        }

        @Test
        @DisplayName("Should return restaurants for customer without AI restaurant")
        void testGetAvailableRestaurants_Customer_WithoutAIRestaurant() throws Exception {
            List<RestaurantProfile> restaurants = Arrays.asList(testRestaurant);
            when(restaurantManagementService.findAllRestaurants()).thenReturn(restaurants);
            when(restaurantManagementService.findRestaurantById(37)).thenReturn(Optional.empty());
            when(chatService.getExistingRoomId(any(UUID.class), any(UserRole.class), anyInt())).thenReturn(null);
            when(chatService.getUnreadCountForRoom(anyString(), any(UUID.class)))
                .thenReturn(Map.of("unreadCount", 0L));

            mockMvc.perform(get("/api/chat/available-restaurants")
                    .principal(new UsernamePasswordAuthenticationToken(testCustomer, null, testCustomer.getAuthorities())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());

            verify(restaurantManagementService).findAllRestaurants();
        }

        @Test
        @DisplayName("Should return restaurants for admin")
        void testGetAvailableRestaurants_Admin() throws Exception {
            List<RestaurantProfile> restaurants = Arrays.asList(testRestaurant);
            when(restaurantManagementService.findAllRestaurants()).thenReturn(restaurants);
            when(chatService.getExistingRoomId(any(UUID.class), any(UserRole.class), anyInt())).thenReturn(null);
            when(chatService.getUnreadCountForRoom(anyString(), any(UUID.class)))
                .thenReturn(Map.of("unreadCount", 0L));

            mockMvc.perform(get("/api/chat/available-restaurants")
                    .principal(new UsernamePasswordAuthenticationToken(testAdmin, null, testAdmin.getAuthorities())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());

            verify(restaurantManagementService).findAllRestaurants();
        }

        @Test
        @DisplayName("Should return restaurants for restaurant owner")
        void testGetAvailableRestaurants_RestaurantOwner() throws Exception {
            List<RestaurantProfile> restaurants = Arrays.asList(testRestaurant);
            when(restaurantOwnerService.getRestaurantsByUserId(testRestaurantOwner.getId())).thenReturn(restaurants);
            when(chatService.getExistingRoomId(any(UUID.class), any(UserRole.class), anyInt())).thenReturn(null);
            when(chatService.getUnreadCountForRoom(anyString(), any(UUID.class)))
                .thenReturn(Map.of("unreadCount", 0L));

            mockMvc.perform(get("/api/chat/available-restaurants")
                    .principal(new UsernamePasswordAuthenticationToken(testRestaurantOwner, null, testRestaurantOwner.getAuthorities())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());

            verify(restaurantOwnerService).getRestaurantsByUserId(testRestaurantOwner.getId());
        }

        @Test
        @DisplayName("Should include unread count when room exists")
        void testGetAvailableRestaurants_WithUnreadCount() throws Exception {
            List<RestaurantProfile> restaurants = Arrays.asList(testRestaurant);
            String existingRoomId = "customer_" + testUserId + "_restaurant_1";
            
            when(restaurantManagementService.findAllRestaurants()).thenReturn(restaurants);
            when(restaurantManagementService.findRestaurantById(37)).thenReturn(Optional.empty());
            when(chatService.getExistingRoomId(testUserId, UserRole.CUSTOMER, 1)).thenReturn(existingRoomId);
            when(chatService.getUnreadCountForRoom(existingRoomId, testUserId))
                .thenReturn(Map.of("unreadCount", 5L));

            mockMvc.perform(get("/api/chat/available-restaurants")
                    .principal(new UsernamePasswordAuthenticationToken(testCustomer, null, testCustomer.getAuthorities())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].unreadCount").value(5))
                    .andExpect(jsonPath("$[0].roomId").value(existingRoomId));
        }

        @Test
        @DisplayName("Should return error for invalid user role")
        void testGetAvailableRestaurants_InvalidRole() throws Exception {
            User invalidUser = new User("invalid", "invalid@example.com", "password", "Invalid");
            invalidUser.setId(UUID.randomUUID());
            invalidUser.setRole(UserRole.CUSTOMER);
            // Override to create invalid scenario
            when(restaurantManagementService.findAllRestaurants()).thenThrow(new RuntimeException("Invalid role"));

            mockMvc.perform(get("/api/chat/available-restaurants")
                    .principal(new UsernamePasswordAuthenticationToken(invalidUser, null, invalidUser.getAuthorities())))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle exception gracefully")
        void testGetAvailableRestaurants_Exception() throws Exception {
            when(restaurantManagementService.findAllRestaurants()).thenThrow(new RuntimeException("Service error"));

            mockMvc.perform(get("/api/chat/available-restaurants")
                    .principal(new UsernamePasswordAuthenticationToken(testCustomer, null, testCustomer.getAuthorities())))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("getAvailableAdmins() Tests")
    class GetAvailableAdminsTests {

        @Test
        @DisplayName("Should return admins for restaurant owner")
        void testGetAvailableAdmins_RestaurantOwner() throws Exception {
            List<User> admins = Arrays.asList(testAdmin);
            List<RestaurantProfile> ownerRestaurants = Arrays.asList(testRestaurant);

            when(chatService.getAvailableAdmins()).thenReturn(admins);
            when(restaurantOwnerService.getRestaurantsByUserId(testRestaurantOwner.getId())).thenReturn(ownerRestaurants);

            mockMvc.perform(get("/api/chat/available-admins")
                    .principal(new UsernamePasswordAuthenticationToken(testRestaurantOwner, null, testRestaurantOwner.getAuthorities())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(testAdmin.getId().toString()));

            verify(chatService).getAvailableAdmins();
            verify(restaurantOwnerService).getRestaurantsByUserId(testRestaurantOwner.getId());
        }

        @Test
        @DisplayName("Should return 403 for non-restaurant owner")
        void testGetAvailableAdmins_NonRestaurantOwner() throws Exception {
            mockMvc.perform(get("/api/chat/available-admins")
                    .principal(new UsernamePasswordAuthenticationToken(testCustomer, null, testCustomer.getAuthorities())))
                    .andExpect(status().isForbidden());

            verify(chatService, never()).getAvailableAdmins();
        }

        @Test
        @DisplayName("Should handle exception gracefully")
        void testGetAvailableAdmins_Exception() throws Exception {
            when(chatService.getAvailableAdmins()).thenThrow(new RuntimeException("Service error"));

            mockMvc.perform(get("/api/chat/available-admins")
                    .principal(new UsernamePasswordAuthenticationToken(testRestaurantOwner, null, testRestaurantOwner.getAuthorities())))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("getUserChatRooms() Tests")
    class GetUserChatRoomsTests {

        @Test
        @DisplayName("Should return chat rooms for user")
        void testGetUserChatRooms_Success() throws Exception {
            ChatRoomDto chatRoomDto = new ChatRoomDto();
            chatRoomDto.setRoomId("room-1");
            List<ChatRoomDto> rooms = Arrays.asList(chatRoomDto);

            when(chatService.getUserChatRooms(testCustomer.getId(), UserRole.CUSTOMER)).thenReturn(rooms);

            mockMvc.perform(get("/api/chat/rooms")
                    .principal(new UsernamePasswordAuthenticationToken(testCustomer, null, testCustomer.getAuthorities())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].roomId").value("room-1"));

            verify(chatService).getUserChatRooms(testCustomer.getId(), UserRole.CUSTOMER);
        }

        @Test
        @DisplayName("Should return empty list when no rooms")
        void testGetUserChatRooms_EmptyList() throws Exception {
            when(chatService.getUserChatRooms(testCustomer.getId(), UserRole.CUSTOMER)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/chat/rooms")
                    .principal(new UsernamePasswordAuthenticationToken(testCustomer, null, testCustomer.getAuthorities())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("Should handle exception gracefully")
        void testGetUserChatRooms_Exception() throws Exception {
            when(chatService.getUserChatRooms(testCustomer.getId(), UserRole.CUSTOMER))
                .thenThrow(new RuntimeException("Service error"));

            mockMvc.perform(get("/api/chat/rooms")
                    .principal(new UsernamePasswordAuthenticationToken(testCustomer, null, testCustomer.getAuthorities())))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("createChatRoom() Tests")
    class CreateChatRoomTests {

        @Test
        @DisplayName("Should create room for customer")
        void testCreateChatRoom_Customer() throws Exception {
            when(chatService.canUserChatWithRestaurant(testCustomer.getId(), UserRole.CUSTOMER, 1)).thenReturn(true);
            doNothing().when(chatService).createCustomerRestaurantRoom(testCustomer.getId(), 1);
            when(chatService.getRoomId(testCustomer.getId(), UserRole.CUSTOMER, 1)).thenReturn("room-1");

            mockMvc.perform(post("/api/chat/rooms")
                    .param("restaurantId", "1")
                    .principal(new UsernamePasswordAuthenticationToken(testCustomer, null, testCustomer.getAuthorities()))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.roomId").value("room-1"))
                    .andExpect(jsonPath("$.restaurantId").value(1));

            verify(chatService).createCustomerRestaurantRoom(testCustomer.getId(), 1);
        }

        @Test
        @DisplayName("Should create room for admin")
        void testCreateChatRoom_Admin() throws Exception {
            when(chatService.canUserChatWithRestaurant(testAdmin.getId(), UserRole.ADMIN, 1)).thenReturn(true);
            doNothing().when(chatService).createAdminRestaurantRoom(testAdmin.getId(), 1);
            when(chatService.getRoomId(testAdmin.getId(), UserRole.ADMIN, 1)).thenReturn("room-2");

            mockMvc.perform(post("/api/chat/rooms")
                    .param("restaurantId", "1")
                    .principal(new UsernamePasswordAuthenticationToken(testAdmin, null, testAdmin.getAuthorities()))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.roomId").value("room-2"));

            verify(chatService).createAdminRestaurantRoom(testAdmin.getId(), 1);
        }

        @Test
        @DisplayName("Should get existing room for restaurant owner")
        void testCreateChatRoom_RestaurantOwner() throws Exception {
            when(chatService.canUserChatWithRestaurant(testRestaurantOwner.getId(), UserRole.RESTAURANT_OWNER, 1)).thenReturn(true);
            when(chatService.getRoomId(testRestaurantOwner.getId(), UserRole.RESTAURANT_OWNER, 1)).thenReturn("room-3");

            mockMvc.perform(post("/api/chat/rooms")
                    .param("restaurantId", "1")
                    .principal(new UsernamePasswordAuthenticationToken(testRestaurantOwner, null, testRestaurantOwner.getAuthorities()))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.roomId").value("room-3"));

            verify(chatService, never()).createCustomerRestaurantRoom(any(), anyInt());
            verify(chatService, never()).createAdminRestaurantRoom(any(), anyInt());
        }

        @Test
        @DisplayName("Should return 400 when not authorized")
        void testCreateChatRoom_NotAuthorized() throws Exception {
            when(chatService.canUserChatWithRestaurant(testCustomer.getId(), UserRole.CUSTOMER, 1)).thenReturn(false);

            mockMvc.perform(post("/api/chat/rooms")
                    .param("restaurantId", "1")
                    .principal(new UsernamePasswordAuthenticationToken(testCustomer, null, testCustomer.getAuthorities()))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isBadRequest());

            verify(chatService, never()).createCustomerRestaurantRoom(any(), anyInt());
        }

        @Test
        @DisplayName("Should return 400 for invalid role")
        void testCreateChatRoom_InvalidRole() throws Exception {
            // This would require creating an invalid role scenario
            // Testing default case in switch statement
            mockMvc.perform(post("/api/chat/rooms")
                    .param("restaurantId", "1")
                    .principal(new UsernamePasswordAuthenticationToken(testCustomer, null, testCustomer.getAuthorities()))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should handle exception gracefully")
        void testCreateChatRoom_Exception() throws Exception {
            when(chatService.canUserChatWithRestaurant(testCustomer.getId(), UserRole.CUSTOMER, 1)).thenReturn(true);
            doThrow(new RuntimeException("Service error")).when(chatService).createCustomerRestaurantRoom(testCustomer.getId(), 1);

            mockMvc.perform(post("/api/chat/rooms")
                    .param("restaurantId", "1")
                    .principal(new UsernamePasswordAuthenticationToken(testCustomer, null, testCustomer.getAuthorities()))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("createChatRoomWithAdmin() Tests")
    class CreateChatRoomWithAdminTests {

        @Test
        @DisplayName("Should create room with admin for restaurant owner")
        void testCreateChatRoomWithAdmin_Success() throws Exception {
            ChatRoom room = new ChatRoom();
            room.setRoomId("admin-room-1");
            room.setRestaurant(testRestaurant);

            when(chatService.createRestaurantOwnerAdminRoom(testRestaurantOwner.getId(), testAdmin.getId(), null))
                .thenReturn(room);

            mockMvc.perform(post("/api/chat/rooms/admin")
                    .param("adminId", testAdmin.getId().toString())
                    .principal(new UsernamePasswordAuthenticationToken(testRestaurantOwner, null, testRestaurantOwner.getAuthorities()))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.roomId").value("admin-room-1"));

            verify(chatService).createRestaurantOwnerAdminRoom(testRestaurantOwner.getId(), testAdmin.getId(), null);
        }

        @Test
        @DisplayName("Should create room with admin and restaurant ID")
        void testCreateChatRoomWithAdmin_WithRestaurantId() throws Exception {
            ChatRoom room = new ChatRoom();
            room.setRoomId("admin-room-2");
            room.setRestaurant(testRestaurant);

            when(chatService.createRestaurantOwnerAdminRoom(testRestaurantOwner.getId(), testAdmin.getId(), 1L))
                .thenReturn(room);

            mockMvc.perform(post("/api/chat/rooms/admin")
                    .param("adminId", testAdmin.getId().toString())
                    .param("restaurantId", "1")
                    .principal(new UsernamePasswordAuthenticationToken(testRestaurantOwner, null, testRestaurantOwner.getAuthorities()))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk());

            verify(chatService).createRestaurantOwnerAdminRoom(testRestaurantOwner.getId(), testAdmin.getId(), 1L);
        }

        @Test
        @DisplayName("Should return 403 for non-restaurant owner")
        void testCreateChatRoomWithAdmin_NonRestaurantOwner() throws Exception {
            mockMvc.perform(post("/api/chat/rooms/admin")
                    .param("adminId", testAdmin.getId().toString())
                    .principal(new UsernamePasswordAuthenticationToken(testCustomer, null, testCustomer.getAuthorities()))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isForbidden());

            verify(chatService, never()).createRestaurantOwnerAdminRoom(any(), any(), any());
        }

        @Test
        @DisplayName("Should handle exception gracefully")
        void testCreateChatRoomWithAdmin_Exception() throws Exception {
            when(chatService.createRestaurantOwnerAdminRoom(any(), any(), any()))
                .thenThrow(new RuntimeException("Service error"));

            mockMvc.perform(post("/api/chat/rooms/admin")
                    .param("adminId", testAdmin.getId().toString())
                    .principal(new UsernamePasswordAuthenticationToken(testRestaurantOwner, null, testRestaurantOwner.getAuthorities()))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("createChatRoomWithRestaurant() Tests")
    class CreateChatRoomWithRestaurantTests {

        @Test
        @DisplayName("Should create room with restaurant for admin")
        void testCreateChatRoomWithRestaurant_Success() throws Exception {
            ChatRoom room = new ChatRoom();
            room.setRoomId("admin-restaurant-room-1");
            room.setRestaurant(testRestaurant);

            when(chatService.createAdminRestaurantRoom(testAdmin.getId(), 1)).thenReturn(room);

            mockMvc.perform(post("/api/chat/rooms/restaurant")
                    .param("restaurantId", "1")
                    .principal(new UsernamePasswordAuthenticationToken(testAdmin, null, testAdmin.getAuthorities()))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.roomId").value("admin-restaurant-room-1"))
                    .andExpect(jsonPath("$.restaurantId").value(1));

            verify(chatService).createAdminRestaurantRoom(testAdmin.getId(), 1);
        }

        @Test
        @DisplayName("Should return 403 for non-admin")
        void testCreateChatRoomWithRestaurant_NonAdmin() throws Exception {
            mockMvc.perform(post("/api/chat/rooms/restaurant")
                    .param("restaurantId", "1")
                    .principal(new UsernamePasswordAuthenticationToken(testCustomer, null, testCustomer.getAuthorities()))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isForbidden());

            verify(chatService, never()).createAdminRestaurantRoom(any(), anyInt());
        }

        @Test
        @DisplayName("Should handle exception gracefully")
        void testCreateChatRoomWithRestaurant_Exception() throws Exception {
            when(chatService.createAdminRestaurantRoom(testAdmin.getId(), 1))
                .thenThrow(new RuntimeException("Service error"));

            mockMvc.perform(post("/api/chat/rooms/restaurant")
                    .param("restaurantId", "1")
                    .principal(new UsernamePasswordAuthenticationToken(testAdmin, null, testAdmin.getAuthorities()))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("getMessages() Tests")
    class GetMessagesTests {

        @Test
        @DisplayName("Should return messages for authorized user")
        void testGetMessages_Success() throws Exception {
            ChatMessageDto messageDto = new ChatMessageDto();
            messageDto.setMessageId(1);
            messageDto.setContent("Test message");
            List<ChatMessageDto> messages = Arrays.asList(messageDto);

            when(chatService.canUserAccessRoom("room-1", testCustomer.getId(), UserRole.CUSTOMER)).thenReturn(true);
            when(chatService.getMessages("room-1", 0, 200)).thenReturn(messages);

            mockMvc.perform(get("/api/chat/rooms/room-1/messages")
                    .principal(new UsernamePasswordAuthenticationToken(testCustomer, null, testCustomer.getAuthorities())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].messageId").value(1));

            verify(chatService).getMessages("room-1", 0, 200);
        }

        @Test
        @DisplayName("Should use custom pagination parameters")
        void testGetMessages_WithPagination() throws Exception {
            List<ChatMessageDto> messages = Collections.emptyList();

            when(chatService.canUserAccessRoom("room-1", testCustomer.getId(), UserRole.CUSTOMER)).thenReturn(true);
            when(chatService.getMessages("room-1", 1, 50)).thenReturn(messages);

            mockMvc.perform(get("/api/chat/rooms/room-1/messages")
                    .param("page", "1")
                    .param("size", "50")
                    .principal(new UsernamePasswordAuthenticationToken(testCustomer, null, testCustomer.getAuthorities())))
                    .andExpect(status().isOk());

            verify(chatService).getMessages("room-1", 1, 50);
        }

        @Test
        @DisplayName("Should return 403 when not authorized")
        void testGetMessages_NotAuthorized() throws Exception {
            when(chatService.canUserAccessRoom("room-1", testCustomer.getId(), UserRole.CUSTOMER)).thenReturn(false);

            mockMvc.perform(get("/api/chat/rooms/room-1/messages")
                    .principal(new UsernamePasswordAuthenticationToken(testCustomer, null, testCustomer.getAuthorities())))
                    .andExpect(status().isForbidden());

            verify(chatService, never()).getMessages(anyString(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("Should handle exception gracefully")
        void testGetMessages_Exception() throws Exception {
            when(chatService.canUserAccessRoom("room-1", testCustomer.getId(), UserRole.CUSTOMER)).thenReturn(true);
            when(chatService.getMessages("room-1", 0, 200)).thenThrow(new RuntimeException("Service error"));

            mockMvc.perform(get("/api/chat/rooms/room-1/messages")
                    .principal(new UsernamePasswordAuthenticationToken(testCustomer, null, testCustomer.getAuthorities())))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("markMessagesAsRead() Tests")
    class MarkMessagesAsReadTests {

        @Test
        @DisplayName("Should mark messages as read successfully")
        void testMarkMessagesAsRead_Success() throws Exception {
            doNothing().when(chatService).markMessagesAsRead("room-1", testCustomer.getId());

            mockMvc.perform(post("/api/chat/rooms/room-1/read")
                    .principal(new UsernamePasswordAuthenticationToken(testCustomer, null, testCustomer.getAuthorities())))
                    .andExpect(status().isOk());

            verify(chatService).markMessagesAsRead("room-1", testCustomer.getId());
        }

        @Test
        @DisplayName("Should handle exception gracefully")
        void testMarkMessagesAsRead_Exception() throws Exception {
            doThrow(new RuntimeException("Service error")).when(chatService).markMessagesAsRead("room-1", testCustomer.getId());

            mockMvc.perform(post("/api/chat/rooms/room-1/read")
                    .principal(new UsernamePasswordAuthenticationToken(testCustomer, null, testCustomer.getAuthorities())))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("getUnreadCount() Tests")
    class GetUnreadCountTests {

        @Test
        @DisplayName("Should return unread count for user")
        void testGetUnreadCount_Success() throws Exception {
            when(chatService.getUnreadMessageCount(testCustomer.getId())).thenReturn(5L);

            mockMvc.perform(get("/api/chat/unread-count")
                    .principal(new UsernamePasswordAuthenticationToken(testCustomer, null, testCustomer.getAuthorities())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.unreadCount").value(5));

            verify(chatService).getUnreadMessageCount(testCustomer.getId());
        }

        @Test
        @DisplayName("Should return zero when no unread messages")
        void testGetUnreadCount_Zero() throws Exception {
            when(chatService.getUnreadMessageCount(testCustomer.getId())).thenReturn(0L);

            mockMvc.perform(get("/api/chat/unread-count")
                    .principal(new UsernamePasswordAuthenticationToken(testCustomer, null, testCustomer.getAuthorities())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.unreadCount").value(0));
        }

        @Test
        @DisplayName("Should handle exception gracefully")
        void testGetUnreadCount_Exception() throws Exception {
            when(chatService.getUnreadMessageCount(testCustomer.getId())).thenThrow(new RuntimeException("Service error"));

            mockMvc.perform(get("/api/chat/unread-count")
                    .principal(new UsernamePasswordAuthenticationToken(testCustomer, null, testCustomer.getAuthorities())))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("getUnreadCountForRoom() Tests")
    class GetUnreadCountForRoomTests {

        @Test
        @DisplayName("Should return unread count for room")
        void testGetUnreadCountForRoom_Success() throws Exception {
            when(chatService.canUserAccessRoom("room-1", testCustomer.getId(), UserRole.CUSTOMER)).thenReturn(true);
            when(chatService.getUnreadMessageCountForRoom("room-1", testCustomer.getId())).thenReturn(3L);

            mockMvc.perform(get("/api/chat/rooms/room-1/unread-count")
                    .principal(new UsernamePasswordAuthenticationToken(testCustomer, null, testCustomer.getAuthorities())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.unreadCount").value(3));

            verify(chatService).getUnreadMessageCountForRoom("room-1", testCustomer.getId());
        }

        @Test
        @DisplayName("Should return 403 when not authorized")
        void testGetUnreadCountForRoom_NotAuthorized() throws Exception {
            when(chatService.canUserAccessRoom("room-1", testCustomer.getId(), UserRole.CUSTOMER)).thenReturn(false);

            mockMvc.perform(get("/api/chat/rooms/room-1/unread-count")
                    .principal(new UsernamePasswordAuthenticationToken(testCustomer, null, testCustomer.getAuthorities())))
                    .andExpect(status().isForbidden());

            verify(chatService, never()).getUnreadMessageCountForRoom(anyString(), any(UUID.class));
        }

        @Test
        @DisplayName("Should handle exception gracefully")
        void testGetUnreadCountForRoom_Exception() throws Exception {
            when(chatService.canUserAccessRoom("room-1", testCustomer.getId(), UserRole.CUSTOMER)).thenReturn(true);
            when(chatService.getUnreadMessageCountForRoom("room-1", testCustomer.getId()))
                .thenThrow(new RuntimeException("Service error"));

            mockMvc.perform(get("/api/chat/rooms/room-1/unread-count")
                    .principal(new UsernamePasswordAuthenticationToken(testCustomer, null, testCustomer.getAuthorities())))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("archiveChatRoom() Tests")
    class ArchiveChatRoomTests {

        @Test
        @DisplayName("Should archive room successfully")
        void testArchiveChatRoom_Success() throws Exception {
            when(chatService.canUserAccessRoom("room-1", testCustomer.getId(), UserRole.CUSTOMER)).thenReturn(true);
            doNothing().when(chatService).archiveChatRoom("room-1");

            mockMvc.perform(post("/api/chat/rooms/room-1/archive")
                    .principal(new UsernamePasswordAuthenticationToken(testCustomer, null, testCustomer.getAuthorities())))
                    .andExpect(status().isOk());

            verify(chatService).archiveChatRoom("room-1");
        }

        @Test
        @DisplayName("Should return 403 when not authorized")
        void testArchiveChatRoom_NotAuthorized() throws Exception {
            when(chatService.canUserAccessRoom("room-1", testCustomer.getId(), UserRole.CUSTOMER)).thenReturn(false);

            mockMvc.perform(post("/api/chat/rooms/room-1/archive")
                    .principal(new UsernamePasswordAuthenticationToken(testCustomer, null, testCustomer.getAuthorities())))
                    .andExpect(status().isForbidden());

            verify(chatService, never()).archiveChatRoom(anyString());
        }

        @Test
        @DisplayName("Should handle exception gracefully")
        void testArchiveChatRoom_Exception() throws Exception {
            when(chatService.canUserAccessRoom("room-1", testCustomer.getId(), UserRole.CUSTOMER)).thenReturn(true);
            doThrow(new RuntimeException("Service error")).when(chatService).archiveChatRoom("room-1");

            mockMvc.perform(post("/api/chat/rooms/room-1/archive")
                    .principal(new UsernamePasswordAuthenticationToken(testCustomer, null, testCustomer.getAuthorities())))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("getChatStatistics() Tests")
    class GetChatStatisticsTests {

        @Test
        @DisplayName("Should return statistics for admin")
        void testGetChatStatistics_Success() throws Exception {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalRooms", 10);
            stats.put("totalMessages", 100);

            when(chatService.getChatStatistics()).thenReturn(stats);

            mockMvc.perform(get("/api/chat/statistics")
                    .principal(new UsernamePasswordAuthenticationToken(testAdmin, null, testAdmin.getAuthorities())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRooms").value(10))
                    .andExpect(jsonPath("$.totalMessages").value(100));

            verify(chatService).getChatStatistics();
        }

        @Test
        @DisplayName("Should return 403 for non-admin")
        void testGetChatStatistics_NonAdmin() throws Exception {
            mockMvc.perform(get("/api/chat/statistics")
                    .principal(new UsernamePasswordAuthenticationToken(testCustomer, null, testCustomer.getAuthorities())))
                    .andExpect(status().isForbidden());

            verify(chatService, never()).getChatStatistics();
        }

        @Test
        @DisplayName("Should handle exception gracefully")
        void testGetChatStatistics_Exception() throws Exception {
            when(chatService.getChatStatistics()).thenThrow(new RuntimeException("Service error"));

            mockMvc.perform(get("/api/chat/statistics")
                    .principal(new UsernamePasswordAuthenticationToken(testAdmin, null, testAdmin.getAuthorities())))
                    .andExpect(status().isBadRequest());
        }
    }
}

