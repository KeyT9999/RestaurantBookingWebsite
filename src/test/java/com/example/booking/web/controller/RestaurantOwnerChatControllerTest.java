package com.example.booking.web.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

import com.example.booking.domain.Booking;
import com.example.booking.domain.Customer;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.dto.ChatRoomDto;
import com.example.booking.service.BookingService;
import com.example.booking.service.ChatService;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.SimpleUserService;

/**
 * Unit tests for RestaurantOwnerChatController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RestaurantOwnerChatController Tests")
public class RestaurantOwnerChatControllerTest {

    @Mock
    private ChatService chatService;

    @Mock
    private SimpleUserService userService;

    @Mock
    private RestaurantOwnerService restaurantOwnerService;

    @Mock
    private BookingService bookingService;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private RestaurantOwnerChatController restaurantOwnerChatController;

    private User user;
    private UUID userId;
    private RestaurantProfile restaurant;
    private Booking booking;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setEmail("owner@test.com");

        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");

        booking = new Booking();
        booking.setBookingId(1);
        booking.setRestaurant(restaurant);

        when(authentication.getPrincipal()).thenReturn(user);
    }

    // ========== chatPage() Tests ==========

    @Test
    @DisplayName("shouldShowChatPage_successfully")
    void shouldShowChatPage_successfully() {
        // Given
        List<ChatRoomDto> chatRooms = Arrays.asList(new ChatRoomDto());
        List<RestaurantProfile> restaurants = Arrays.asList(restaurant);

        when(chatService.getUserChatRooms(userId, user.getRole())).thenReturn(chatRooms);
        when(restaurantOwnerService.getRestaurantsByUserId(userId)).thenReturn(restaurants);

        // When
        String view = restaurantOwnerChatController.chatPage(null, authentication, model);

        // Then
        assertEquals("restaurant-owner/chat", view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    @Test
    @DisplayName("shouldHandleBookingId_successfully")
    void shouldHandleBookingId_successfully() {
        // Given
        Customer customer = new Customer();
        customer.setUser(user);
        booking.setCustomer(customer);

        List<RestaurantProfile> restaurants = Arrays.asList(restaurant);
        when(bookingService.findBookingById(1)).thenReturn(Optional.of(booking));
        when(restaurantOwnerService.getRestaurantsByUserId(userId)).thenReturn(restaurants);
        com.example.booking.domain.ChatRoom mockRoom = new com.example.booking.domain.ChatRoom();
        mockRoom.setRoomId("room-123");
        when(chatService.getRoomId(any(), any(), anyInt())).thenReturn(null);
        when(chatService.createCustomerRestaurantRoom(any(), anyInt())).thenReturn(mockRoom);

        // When
        String view = restaurantOwnerChatController.chatPage(1, authentication, model);

        // Then
        assertEquals("redirect:/restaurant-owner/chat", view);
        verify(chatService, times(1)).createCustomerRestaurantRoom(any(), eq(1));
    }

    @Test
    @DisplayName("shouldHandleBookingId_whenRoomExists")
    void shouldHandleBookingId_whenRoomExists() {
        // Given
        Customer customer = new Customer();
        customer.setUser(user);
        booking.setCustomer(customer);

        List<RestaurantProfile> restaurants = Arrays.asList(restaurant);
        when(bookingService.findBookingById(1)).thenReturn(Optional.of(booking));
        when(restaurantOwnerService.getRestaurantsByUserId(userId)).thenReturn(restaurants);
        when(chatService.getRoomId(any(), any(), anyInt())).thenReturn("existing-room-123");

        // When
        String view = restaurantOwnerChatController.chatPage(1, authentication, model);

        // Then
        assertEquals("redirect:/restaurant-owner/chat", view);
        verify(chatService, never()).createCustomerRestaurantRoom(any(), anyInt());
    }

    @Test
    @DisplayName("shouldHandleBookingId_whenBookingNotFound")
    void shouldHandleBookingId_whenBookingNotFound() {
        // Given
        List<RestaurantProfile> restaurants = Arrays.asList(restaurant);
        when(bookingService.findBookingById(1)).thenReturn(Optional.empty());
        when(restaurantOwnerService.getRestaurantsByUserId(userId)).thenReturn(restaurants);

        // When
        String view = restaurantOwnerChatController.chatPage(1, authentication, model);

        // Then
        assertEquals("restaurant-owner/chat", view);
    }

    @Test
    @DisplayName("shouldHandleBookingId_whenNoRestaurants")
    void shouldHandleBookingId_whenNoRestaurants() {
        // Given
        when(restaurantOwnerService.getRestaurantsByUserId(userId)).thenReturn(new ArrayList<>());

        // When
        String view = restaurantOwnerChatController.chatPage(null, authentication, model);

        // Then
        assertEquals("restaurant-owner/chat", view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    @Test
    @DisplayName("shouldHandleBookingId_whenChatRoomsEmpty")
    void shouldHandleBookingId_whenChatRoomsEmpty() {
        // Given
        List<RestaurantProfile> restaurants = Arrays.asList(restaurant);
        when(chatService.getUserChatRooms(userId, user.getRole())).thenReturn(new ArrayList<>());
        when(restaurantOwnerService.getRestaurantsByUserId(userId)).thenReturn(restaurants);

        // When
        String view = restaurantOwnerChatController.chatPage(null, authentication, model);

        // Then
        assertEquals("restaurant-owner/chat", view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }
}

