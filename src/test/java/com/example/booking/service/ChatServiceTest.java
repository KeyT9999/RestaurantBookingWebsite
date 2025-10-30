package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

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

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RestaurantProfileRepository restaurantProfileRepository;

    @Mock
    private RestaurantOwnerRepository restaurantOwnerRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatService chatService;

    private UUID testUserId;
    private Integer testRestaurantId;
    private String testRoomId;
    private Customer testCustomer;
    private User testUser;
    private RestaurantProfile testRestaurant;
    private ChatRoom testChatRoom;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testRestaurantId = 1;
        testRoomId = "customer_" + testUserId + "_restaurant_" + testRestaurantId;
        
        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail("customer@test.com");
        testUser.setRole(UserRole.CUSTOMER);
        
        testCustomer = new Customer();
        testCustomer.setUser(testUser);
        
        testRestaurant = new RestaurantProfile();
        testRestaurant.setRestaurantId(testRestaurantId);
        testRestaurant.setRestaurantName("Test Restaurant");
        
        testChatRoom = new ChatRoom(testRoomId, testCustomer, testRestaurant);
    }

    @Test
    // TC RC-013
    void shouldCreateCustomerRestaurantRoom_whenRoomDoesNotExist() {
        // Given
        when(customerRepository.findByUserId(testUserId)).thenReturn(Optional.of(testCustomer));
        when(chatRoomRepository.findByCustomerAndRestaurant(testUserId, testRestaurantId)).thenReturn(Optional.empty());
        when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.of(testRestaurant));
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(testChatRoom);
        
        // When
        ChatRoom result = chatService.createCustomerRestaurantRoom(testUserId, testRestaurantId);
        
        // Then
        assertNotNull(result);
        assertEquals(testRoomId, result.getRoomId());
        verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));
    }

    @Test
    // TC RC-014
    void shouldThrowRuntimeException_whenCustomerNotFound() {
        // Given
        when(customerRepository.findByUserId(testUserId)).thenReturn(Optional.empty());
        
        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            chatService.createCustomerRestaurantRoom(testUserId, testRestaurantId);
        });
        
        assertTrue(ex.getMessage().contains("Customer not found"));
        verify(chatRoomRepository, never()).save(any(ChatRoom.class));
    }

    @Test
    // TC RC-015
    void shouldSendMessageAndUpdateLastMessageTime_whenValidInput() {
        // Given
        Message message = new Message();
        message.setMessageId(1);
        message.setContent("Hello");
        
        when(chatRoomRepository.findById(testRoomId)).thenReturn(Optional.of(testChatRoom));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(messageRepository.save(any(Message.class))).thenReturn(message);
        
        // Mock private canUserSendMessage via reflection or make it protected
        
        // When
        Message result = chatService.sendMessage(testRoomId, testUserId, "Hello", MessageType.TEXT);
        
        // Then
        assertNotNull(result);
        verify(messageRepository, times(1)).save(any(Message.class));
        verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));
    }

    @Test
    // TC RC-016
    void shouldThrowRuntimeException_whenChatRoomNotFound() {
        // Given
        when(chatRoomRepository.findById(testRoomId)).thenReturn(Optional.empty());
        
        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            chatService.sendMessage(testRoomId, testUserId, "Hello", MessageType.TEXT);
        });
        
        assertTrue(ex.getMessage().contains("Chat room not found"));
    }

    @Test
    // TC RC-018
    void shouldGetMessages_whenValidPagination() {
        // Given
        Message message1 = new Message();
        message1.setMessageId(1);
        Message message2 = new Message();
        message2.setMessageId(2);
        List<Message> messages = Arrays.asList(message1, message2);
        Page<Message> page = new PageImpl<>(messages);
        
        when(messageRepository.findByRoomIdOrderBySentAtAsc(eq(testRoomId), any(PageRequest.class))).thenReturn(page);
        
        // When
        List<ChatMessageDto> result = chatService.getMessages(testRoomId, 0, 10);
        
        // Then
        assertEquals(2, result.size());
    }

    @Test
    // TC RC-019
    void shouldReturnUpdatedCount_whenMarkingMessagesAsRead() {
        // Given
        when(messageRepository.markMessagesAsReadByRoomIdAndUserId(testRoomId, testUserId)).thenReturn(3);
        
        // When
        int count = chatService.markMessagesAsRead(testRoomId, testUserId);
        
        // Then
        assertEquals(3, count);
        verify(messageRepository, times(1)).markMessagesAsReadByRoomIdAndUserId(testRoomId, testUserId);
    }

    @Test
    // TC RC-020
    void shouldReturnTrue_whenCustomerCanAccessRoom() {
        // Given
        when(chatRoomRepository.findById(testRoomId)).thenReturn(Optional.of(testChatRoom));
        
        // When
        boolean result = chatService.canUserAccessRoom(testRoomId, testUserId, UserRole.CUSTOMER);
        
        // Then
        assertTrue(result);
    }

    @Test
    // TC RC-021
    void shouldReturnFalse_whenRoomHasNoCustomer() {
        // Given
        ChatRoom roomWithoutCustomer = new ChatRoom();
        when(chatRoomRepository.findById(testRoomId)).thenReturn(Optional.of(roomWithoutCustomer));
        
        // When
        boolean result = chatService.canUserAccessRoom(testRoomId, testUserId, UserRole.CUSTOMER);
        
        // Then
        assertTrue(!result);
    }

    @Test
    // TC RC-022
    void shouldReturnFalse_whenUserRoleIsUnknown() {
        // Given
        when(chatRoomRepository.findById(testRoomId)).thenReturn(Optional.of(testChatRoom));
        
        // When
        boolean result = chatService.canUserAccessRoom(testRoomId, testUserId, UserRole.admin);
        
        // Then
        assertTrue(!result);
    }

    @Test
    // TC RC-023
    void shouldGetUserChatRooms_whenCustomerRole() {
        // Given
        List<ChatRoom> rooms = Arrays.asList(testChatRoom);
        when(chatRoomRepository.findByCustomerId(testUserId)).thenReturn(rooms);
        
        // When
        List<ChatRoomDto> result = chatService.getUserChatRooms(testUserId, UserRole.CUSTOMER);
        
        // Then
        assertNotNull(result);
        verify(chatRoomRepository, times(1)).findByCustomerId(testUserId);
    }

    @Test
    // TC RC-024
    void shouldThrowRuntimeException_whenInvalidUserRole() {
        // Given - Using a role that doesn't exist in the switch statement
        UserRole invalidRole = null; // Force default case
        
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            chatService.getUserChatRooms(testUserId, invalidRole);
        });
    }
}

