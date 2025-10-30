package com.example.booking.web.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.ChatRoomDto;
import com.example.booking.dto.RestaurantChatDto;
import com.example.booking.service.ChatService;
import com.example.booking.service.RestaurantManagementService;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.SimpleUserService;

@WebMvcTest(ChatApiController.class)
class ChatApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @MockBean
    private RestaurantManagementService restaurantManagementService;

    @MockBean
    private RestaurantOwnerService restaurantOwnerService;

    @MockBean
    private SimpleUserService userService;

    private User testUser;
    private UUID testUserId;
    private Integer testRestaurantId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testRestaurantId = 1;
        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail("customer@test.com");
        testUser.setRole(UserRole.CUSTOMER);
    }

    @Test
    // TC RC-025
    @WithMockUser(username = "customer@test.com", roles = {"CUSTOMER"})
    void shouldReturnAvailableRestaurants_whenCustomerRequests() throws Exception {
        // Given
        List<RestaurantChatDto> restaurants = new ArrayList<>();
        restaurants.add(new RestaurantChatDto(1, "Restaurant 1", "Address 1", "Phone 1", true));
        restaurants.add(new RestaurantChatDto(37, "AI Restaurant", "AI Address", "AI Phone", true));
        
        when(userService.loadUserByUsername(anyString())).thenReturn(testUser);
        when(restaurantManagementService.findAllRestaurants()).thenReturn(Arrays.asList());
        when(chatService.getExistingRoomId(any(UUID.class), eq(UserRole.CUSTOMER), any(Integer.class))).thenReturn(null);
        
        // When & Then
        mockMvc.perform(get("/api/chat/available-restaurants"))
                .andExpect(status().isOk());
    }

    @Test
    // TC RC-026
    @WithMockUser(username = "customer@test.com", roles = {"CUSTOMER"})
    void shouldSetUnreadCount_whenRestaurantHasExistingRoom() throws Exception {
        // Given
        String existingRoomId = "customer_" + testUserId + "_restaurant_" + testRestaurantId;
        Map<String, Object> unreadData = new HashMap<>();
        unreadData.put("unreadCount", 5L);
        
        when(userService.loadUserByUsername(anyString())).thenReturn(testUser);
        when(restaurantManagementService.findAllRestaurants()).thenReturn(Arrays.asList());
        when(chatService.getExistingRoomId(any(UUID.class), eq(UserRole.CUSTOMER), any(Integer.class))).thenReturn(existingRoomId);
        when(chatService.getUnreadCountForRoom(existingRoomId, testUserId)).thenReturn(unreadData);
        
        // When & Then
        mockMvc.perform(get("/api/chat/available-restaurants"))
                .andExpect(status().isOk());
    }

    @Test
    // TC RC-027
    @WithMockUser(username = "customer@test.com", roles = {"CUSTOMER"})
    void shouldCreateChatRoom_whenCustomerCreatesRoom() throws Exception {
        // Given
        String expectedRoomId = "customer_" + testUserId + "_restaurant_" + testRestaurantId;
        when(userService.loadUserByUsername(anyString())).thenReturn(testUser);
        when(chatService.canUserChatWithRestaurant(testUserId, UserRole.CUSTOMER, testRestaurantId)).thenReturn(true);
        when(chatService.createCustomerRestaurantRoom(testUserId, testRestaurantId)).thenReturn(null);
        when(chatService.getRoomId(testUserId, UserRole.CUSTOMER, testRestaurantId)).thenReturn(expectedRoomId);
        
        // When & Then
        mockMvc.perform(post("/api/chat/rooms")
                .param("restaurantId", testRestaurantId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    // TC RC-028
    @WithMockUser(username = "customer@test.com", roles = {"CUSTOMER"})
    void shouldReturnBadRequest_whenNotAuthorizedToChat() throws Exception {
        // Given
        when(userService.loadUserByUsername(anyString())).thenReturn(testUser);
        when(chatService.canUserChatWithRestaurant(testUserId, UserRole.CUSTOMER, testRestaurantId)).thenReturn(false);
        
        // When & Then
        mockMvc.perform(post("/api/chat/rooms")
                .param("restaurantId", testRestaurantId.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    // TC RC-029
    @WithMockUser(username = "customer@test.com", roles = {"CUSTOMER"})
    void shouldGetUserChatRooms_whenRequested() throws Exception {
        // Given
        List<ChatRoomDto> rooms = Arrays.asList(
            new ChatRoomDto("room1", testUserId, "User 1", "CUSTOMER", testRestaurantId, "Restaurant 1", "Last message", null, 0L, true)
        );
        when(userService.loadUserByUsername(anyString())).thenReturn(testUser);
        when(chatService.getUserChatRooms(testUserId, UserRole.CUSTOMER)).thenReturn(rooms);
        
        // When & Then
        mockMvc.perform(get("/api/chat/rooms"))
                .andExpect(status().isOk());
    }

    @Test
    // TC RC-032
    @WithMockUser(username = "customer@test.com", roles = {"CUSTOMER"})
    void shouldGetUnreadCount_whenRequested() throws Exception {
        // Given
        when(userService.loadUserByUsername(anyString())).thenReturn(testUser);
        when(chatService.getUnreadMessageCount(testUserId)).thenReturn(10L);
        
        // When & Then
        mockMvc.perform(get("/api/chat/unread-count"))
                .andExpect(status().isOk());
    }

    @Test
    // TC RC-033
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void shouldGetChatStatistics_whenAdminRequests() throws Exception {
        // Given
        User adminUser = new User();
        adminUser.setId(UUID.randomUUID());
        adminUser.setRole(UserRole.ADMIN);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRooms", 100L);
        stats.put("activeRooms", 50L);
        
        when(userService.loadUserByUsername(anyString())).thenReturn(adminUser);
        when(chatService.getChatStatistics()).thenReturn(stats);
        
        // When & Then
        mockMvc.perform(get("/api/chat/statistics"))
                .andExpect(status().isOk());
    }

    @Test
    // TC RC-034
    @WithMockUser(username = "customer@test.com", roles = {"CUSTOMER"})
    void shouldReturnForbidden_whenNonAdminRequestsStatistics() throws Exception {
        // Given
        when(userService.loadUserByUsername(anyString())).thenReturn(testUser);
        
        // When & Then
        mockMvc.perform(get("/api/chat/statistics"))
                .andExpect(status().isForbidden());
    }

    @Test
    // TC RC-030
    @WithMockUser(username = "customer@test.com", roles = {"CUSTOMER"})
    void shouldGetMessagesWithLimit_whenPageSizeIsMax() throws Exception {
        // Given
        String roomId = "customer_" + testUserId + "_restaurant_" + testRestaurantId;
        when(userService.loadUserByUsername(anyString())).thenReturn(testUser);
        when(chatService.canUserAccessRoom(roomId, testUserId, UserRole.CUSTOMER)).thenReturn(true);
        when(chatService.getMessages(roomId, 0, 200)).thenReturn(new ArrayList<>());
        
        // When & Then
        mockMvc.perform(get("/api/chat/rooms/" + roomId + "/messages")
                .param("page", "0")
                .param("size", "200"))
                .andExpect(status().isOk());
    }

    @Test
    // TC RC-031
    @WithMockUser(username = "customer@test.com", roles = {"CUSTOMER"})
    void shouldMarkMessagesAsRead_whenPostToReadEndpoint() throws Exception {
        // Given
        String roomId = "customer_" + testUserId + "_restaurant_" + testRestaurantId;
        when(userService.loadUserByUsername(anyString())).thenReturn(testUser);
        when(chatService.markMessagesAsRead(roomId, testUserId)).thenReturn(5);
        
        // When & Then
        mockMvc.perform(post("/api/chat/rooms/" + roomId + "/read"))
                .andExpect(status().isOk());
    }

    @Test
    // TC RC-035
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void shouldCreateAdminRestaurantRoom_whenAdminCreatesRoom() throws Exception {
        // Given
        User adminUser = new User();
        adminUser.setId(UUID.randomUUID());
        adminUser.setRole(UserRole.ADMIN);
        
        when(userService.loadUserByUsername(anyString())).thenReturn(adminUser);
        
        // When & Then
        mockMvc.perform(post("/api/chat/rooms/restaurant")
                .param("restaurantId", testRestaurantId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    // TC RC-036
    @WithMockUser(username = "customer@test.com", roles = {"CUSTOMER"})
    void shouldReturnForbidden_whenNonAdminCreatesRestaurantRoom() throws Exception {
        // Given
        when(userService.loadUserByUsername(anyString())).thenReturn(testUser);
        
        // When & Then
        mockMvc.perform(post("/api/chat/rooms/restaurant")
                .param("restaurantId", testRestaurantId.toString()))
                .andExpect(status().isForbidden());
    }
}

