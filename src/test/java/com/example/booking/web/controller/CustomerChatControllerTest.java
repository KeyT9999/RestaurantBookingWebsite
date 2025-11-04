package com.example.booking.web.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
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

import com.example.booking.domain.User;
import com.example.booking.dto.ChatRoomDto;
import com.example.booking.service.ChatService;
import com.example.booking.service.SimpleUserService;

/**
 * Unit tests for CustomerChatController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerChatController Tests")
public class CustomerChatControllerTest {

    @Mock
    private ChatService chatService;

    @Mock
    private SimpleUserService userService;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CustomerChatController customerChatController;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setEmail("customer@test.com");

        when(authentication.getPrincipal()).thenReturn(user);
    }

    // ========== chatPage() Tests ==========

    @Test
    @DisplayName("shouldShowChatPage_successfully")
    void shouldShowChatPage_successfully() {
        // Given
        List<ChatRoomDto> chatRooms = Arrays.asList(new ChatRoomDto());
        when(chatService.getUserChatRooms(userId, user.getRole())).thenReturn(chatRooms);

        // When
        String view = customerChatController.chatPage(authentication, model);

        // Then
        assertEquals("customer/chat", view);
        verify(model, times(1)).addAttribute(eq("chatRooms"), eq(chatRooms));
        verify(model, times(1)).addAttribute(eq("currentUser"), eq(user));
    }

    @Test
    @DisplayName("shouldHandleOAuth2User_successfully")
    void shouldHandleOAuth2User_successfully() {
        // Given
        org.springframework.security.oauth2.core.user.OAuth2User oAuth2User = 
            mock(org.springframework.security.oauth2.core.user.OAuth2User.class);
        when(oAuth2User.getName()).thenReturn("customer@test.com");
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(userService.loadUserByUsername("customer@test.com")).thenReturn(user);
        
        List<ChatRoomDto> chatRooms = Arrays.asList(new ChatRoomDto());
        when(chatService.getUserChatRooms(userId, user.getRole())).thenReturn(chatRooms);

        // When
        String view = customerChatController.chatPage(authentication, model);

        // Then
        assertEquals("customer/chat", view);
    }
}

