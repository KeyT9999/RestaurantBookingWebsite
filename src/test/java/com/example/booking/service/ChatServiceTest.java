package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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
        
        RestaurantOwner owner = new RestaurantOwner();
        owner.setUser(testUser);
        testRestaurant.setOwner(owner);
        
        testChatRoom = new ChatRoom(testRoomId, testCustomer, testRestaurant);
        
        // Initialize restaurants list for owner
        if (owner.getRestaurants() == null) {
            owner.setRestaurants(new ArrayList<>());
        }
        owner.getRestaurants().add(testRestaurant);
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
    @DisplayName("Should return existing room when room already exists")
    void shouldReturnExistingRoom_whenRoomAlreadyExists() {
        // Given
        when(customerRepository.findByUserId(testUserId)).thenReturn(Optional.of(testCustomer));
        when(chatRoomRepository.findByCustomerAndRestaurant(testUserId, testRestaurantId))
            .thenReturn(Optional.of(testChatRoom));
        
        // When
        ChatRoom result = chatService.createCustomerRestaurantRoom(testUserId, testRestaurantId);
        
        // Then
        assertNotNull(result);
        assertEquals(testChatRoom, result);
        verify(chatRoomRepository, never()).save(any(ChatRoom.class));
    }

    @Test
    @DisplayName("Should throw exception when restaurant not found")
    void shouldThrowException_whenRestaurantNotFound() {
        // Given
        when(customerRepository.findByUserId(testUserId)).thenReturn(Optional.of(testCustomer));
        when(chatRoomRepository.findByCustomerAndRestaurant(testUserId, testRestaurantId))
            .thenReturn(Optional.empty());
        when(restaurantProfileRepository.findById(testRestaurantId)).thenReturn(Optional.empty());
        
        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            chatService.createCustomerRestaurantRoom(testUserId, testRestaurantId);
        });
        
        assertTrue(ex.getMessage().contains("Restaurant not found"));
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

    // ========== createAdminRestaurantRoom() Tests ==========

    @Test
    @DisplayName("Should create admin restaurant room successfully")
    void createAdminRestaurantRoom_withValidData_shouldCreateRoom() {
        // Given
        UUID adminId = UUID.randomUUID();
        User admin = new User();
        admin.setId(adminId);
        admin.setRole(UserRole.ADMIN);
        
        when(chatRoomRepository.findByAdminAndRestaurant(adminId, testRestaurantId))
                .thenReturn(Optional.empty());
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(restaurantProfileRepository.findById(testRestaurantId))
                .thenReturn(Optional.of(testRestaurant));
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(testChatRoom);

        // When
        ChatRoom result = chatService.createAdminRestaurantRoom(adminId, testRestaurantId);

        // Then
        assertNotNull(result);
        verify(chatRoomRepository).save(any(ChatRoom.class));
    }

    @Test
    @DisplayName("Should return existing room when already exists")
    void createAdminRestaurantRoom_withExistingRoom_shouldReturnExisting() {
        // Given
        UUID adminId = UUID.randomUUID();
        when(chatRoomRepository.findByAdminAndRestaurant(adminId, testRestaurantId))
                .thenReturn(Optional.of(testChatRoom));

        // When
        ChatRoom result = chatService.createAdminRestaurantRoom(adminId, testRestaurantId);

        // Then
        assertNotNull(result);
        assertEquals(testChatRoom, result);
        verify(chatRoomRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when restaurant has no owner")
    void createAdminRestaurantRoom_withRestaurantNoOwner_shouldThrowException() {
        // Given
        UUID adminId = UUID.randomUUID();
        User admin = new User();
        admin.setId(adminId);
        
        testRestaurant.setOwner(null);
        when(chatRoomRepository.findByAdminAndRestaurant(adminId, testRestaurantId))
                .thenReturn(Optional.empty());
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(restaurantProfileRepository.findById(testRestaurantId))
                .thenReturn(Optional.of(testRestaurant));

        // When & Then
        assertThrows(RuntimeException.class, () ->
                chatService.createAdminRestaurantRoom(adminId, testRestaurantId)
        );
    }

    @Test
    @DisplayName("Should throw exception when admin not found")
    void createAdminRestaurantRoom_withAdminNotFound_shouldThrowException() {
        // Given
        UUID adminId = UUID.randomUUID();
        when(chatRoomRepository.findByAdminAndRestaurant(adminId, testRestaurantId))
                .thenReturn(Optional.empty());
        when(userRepository.findById(adminId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                chatService.createAdminRestaurantRoom(adminId, testRestaurantId)
        );
        assertTrue(ex.getMessage().contains("Admin not found"));
    }

    @Test
    @DisplayName("Should throw exception when restaurant not found")
    void createAdminRestaurantRoom_withRestaurantNotFound_shouldThrowException() {
        // Given
        UUID adminId = UUID.randomUUID();
        User admin = new User();
        admin.setId(adminId);
        
        when(chatRoomRepository.findByAdminAndRestaurant(adminId, testRestaurantId))
                .thenReturn(Optional.empty());
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(restaurantProfileRepository.findById(testRestaurantId))
                .thenReturn(Optional.empty());

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                chatService.createAdminRestaurantRoom(adminId, testRestaurantId)
        );
        assertTrue(ex.getMessage().contains("Restaurant not found"));
    }

    // ========== createRestaurantOwnerAdminRoom() Tests ==========

    @Test
    @DisplayName("Should create restaurant owner admin room successfully")
    void createRestaurantOwnerAdminRoom_withValidData_shouldCreateRoom() {
        // Given
        UUID ownerUserId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        Long restaurantIdLong = 1L;
        
        RestaurantOwner owner = new RestaurantOwner();
        owner.setUser(testUser);
        owner.getRestaurants().add(testRestaurant);
        
        User admin = new User();
        admin.setId(adminId);
        admin.setRole(UserRole.ADMIN);
        
        when(restaurantOwnerRepository.findByUserId(ownerUserId))
                .thenReturn(Optional.of(owner));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(chatRoomRepository.findByAdminAndRestaurant(adminId, testRestaurantId))
                .thenReturn(Optional.empty());
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(testChatRoom);

        // When
        ChatRoom result = chatService.createRestaurantOwnerAdminRoom(ownerUserId, adminId, restaurantIdLong);

        // Then
        assertNotNull(result);
        verify(chatRoomRepository).save(any(ChatRoom.class));
    }

    @Test
    @DisplayName("Should throw exception when admin role is not ADMIN")
    void createRestaurantOwnerAdminRoom_withNonAdminRole_shouldThrowException() {
        // Given
        UUID ownerUserId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        Long restaurantIdLong = 1L;
        
        RestaurantOwner owner = new RestaurantOwner();
        owner.setUser(testUser);
        owner.getRestaurants().add(testRestaurant);
        
        User nonAdmin = new User();
        nonAdmin.setId(adminId);
        nonAdmin.setRole(UserRole.CUSTOMER); // Not ADMIN
        
        when(restaurantOwnerRepository.findByUserId(ownerUserId))
                .thenReturn(Optional.of(owner));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(nonAdmin));

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                chatService.createRestaurantOwnerAdminRoom(ownerUserId, adminId, restaurantIdLong)
        );
        assertTrue(ex.getMessage().contains("not an admin"));
    }

    @Test
    @DisplayName("Should throw exception when restaurant owner not found")
    void createRestaurantOwnerAdminRoom_withOwnerNotFound_shouldThrowException() {
        // Given
        UUID ownerUserId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        Long restaurantIdLong = 1L;
        
        when(restaurantOwnerRepository.findByUserId(ownerUserId))
                .thenReturn(Optional.empty());

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                chatService.createRestaurantOwnerAdminRoom(ownerUserId, adminId, restaurantIdLong)
        );
        assertTrue(ex.getMessage().contains("Restaurant owner not found"));
    }

    @Test
    @DisplayName("Should throw exception when admin not found")
    void createRestaurantOwnerAdminRoom_withAdminNotFound_shouldThrowException() {
        // Given
        UUID ownerUserId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        Long restaurantIdLong = 1L;
        
        RestaurantOwner owner = new RestaurantOwner();
        owner.setUser(testUser);
        owner.getRestaurants().add(testRestaurant);
        
        when(restaurantOwnerRepository.findByUserId(ownerUserId))
                .thenReturn(Optional.of(owner));
        when(userRepository.findById(adminId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                chatService.createRestaurantOwnerAdminRoom(ownerUserId, adminId, restaurantIdLong)
        );
        assertTrue(ex.getMessage().contains("Admin not found"));
    }

    @Test
    @DisplayName("Should throw exception when restaurant owner has no restaurants")
    void createRestaurantOwnerAdminRoom_withNoRestaurants_shouldThrowException() {
        // Given
        UUID ownerUserId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        Long restaurantIdLong = 1L;
        
        RestaurantOwner owner = new RestaurantOwner();
        owner.setUser(testUser);
        owner.setRestaurants(new ArrayList<>()); // Empty list
        
        User admin = new User();
        admin.setId(adminId);
        admin.setRole(UserRole.ADMIN);
        
        when(restaurantOwnerRepository.findByUserId(ownerUserId))
                .thenReturn(Optional.of(owner));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                chatService.createRestaurantOwnerAdminRoom(ownerUserId, adminId, restaurantIdLong)
        );
        assertTrue(ex.getMessage().contains("no restaurants"));
    }

    @Test
    @DisplayName("Should throw exception when restaurant not found or not owned")
    void createRestaurantOwnerAdminRoom_withRestaurantNotOwned_shouldThrowException() {
        // Given
        UUID ownerUserId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        Long restaurantIdLong = 999L; // Restaurant ID not owned by owner
        
        RestaurantOwner owner = new RestaurantOwner();
        owner.setUser(testUser);
        owner.getRestaurants().add(testRestaurant); // Only has restaurant ID 1
        
        User admin = new User();
        admin.setId(adminId);
        admin.setRole(UserRole.ADMIN);
        
        when(restaurantOwnerRepository.findByUserId(ownerUserId))
                .thenReturn(Optional.of(owner));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                chatService.createRestaurantOwnerAdminRoom(ownerUserId, adminId, restaurantIdLong)
        );
        assertTrue(ex.getMessage().contains("Restaurant not found or not owned"));
    }

    @Test
    @DisplayName("Should return existing room when room already exists")
    void createRestaurantOwnerAdminRoom_withExistingRoom_shouldReturnExisting() {
        // Given
        UUID ownerUserId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        Long restaurantIdLong = 1L;
        
        RestaurantOwner owner = new RestaurantOwner();
        owner.setUser(testUser);
        owner.getRestaurants().add(testRestaurant);
        
        User admin = new User();
        admin.setId(adminId);
        admin.setRole(UserRole.ADMIN);
        
        when(restaurantOwnerRepository.findByUserId(ownerUserId))
                .thenReturn(Optional.of(owner));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(chatRoomRepository.findByAdminAndRestaurant(adminId, testRestaurantId))
                .thenReturn(Optional.of(testChatRoom));

        // When
        ChatRoom result = chatService.createRestaurantOwnerAdminRoom(ownerUserId, adminId, restaurantIdLong);

        // Then
        assertNotNull(result);
        assertEquals(testChatRoom, result);
        verify(chatRoomRepository, never()).save(any(ChatRoom.class));
    }

    @Test
    @DisplayName("Should use first restaurant when restaurantId is null")
    void createRestaurantOwnerAdminRoom_withNullRestaurantId_shouldUseFirstRestaurant() {
        // Given
        UUID ownerUserId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        
        RestaurantOwner owner = new RestaurantOwner();
        owner.setUser(testUser);
        owner.getRestaurants().add(testRestaurant);
        
        User admin = new User();
        admin.setId(adminId);
        admin.setRole(UserRole.ADMIN);
        
        when(restaurantOwnerRepository.findByUserId(ownerUserId))
                .thenReturn(Optional.of(owner));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(chatRoomRepository.findByAdminAndRestaurant(adminId, testRestaurantId))
                .thenReturn(Optional.empty());
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(testChatRoom);

        // When
        ChatRoom result = chatService.createRestaurantOwnerAdminRoom(ownerUserId, adminId, null);

        // Then
        assertNotNull(result);
        verify(chatRoomRepository).save(any(ChatRoom.class));
    }

    // ========== getUnreadMessageCount() Tests ==========

    @Test
    @DisplayName("Should return unread message count for user")
    void getUnreadMessageCount_withValidUserId_shouldReturnCount() {
        // Given
        when(messageRepository.countTotalUnreadMessagesByUserId(testUserId)).thenReturn(5L);

        // When
        long result = chatService.getUnreadMessageCount(testUserId);

        // Then
        assertEquals(5L, result);
        verify(messageRepository).countTotalUnreadMessagesByUserId(testUserId);
    }

    @Test
    @DisplayName("Should return zero when no unread messages")
    void getUnreadMessageCount_withNoUnreadMessages_shouldReturnZero() {
        // Given
        when(messageRepository.countTotalUnreadMessagesByUserId(testUserId)).thenReturn(0L);

        // When
        long result = chatService.getUnreadMessageCount(testUserId);

        // Then
        assertEquals(0L, result);
    }

    // ========== getUnreadMessageCountForRoom() Tests ==========

    @Test
    @DisplayName("Should return unread message count for specific room")
    void getUnreadMessageCountForRoom_withValidData_shouldReturnCount() {
        // Given
        when(messageRepository.countUnreadMessagesByRoomIdAndUserId(testRoomId, testUserId))
                .thenReturn(3L);

        // When
        long result = chatService.getUnreadMessageCountForRoom(testRoomId, testUserId);

        // Then
        assertEquals(3L, result);
        verify(messageRepository).countUnreadMessagesByRoomIdAndUserId(testRoomId, testUserId);
    }

    // ========== getChatRoomById() Tests ==========

    @Test
    @DisplayName("Should return chat room by ID")
    void getChatRoomById_withValidId_shouldReturnRoom() {
        // Given
        when(chatRoomRepository.findById(testRoomId)).thenReturn(Optional.of(testChatRoom));

        // When
        Optional<ChatRoom> result = chatService.getChatRoomById(testRoomId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testChatRoom, result.get());
    }

    @Test
    @DisplayName("Should return empty when room not found")
    void getChatRoomById_withInvalidId_shouldReturnEmpty() {
        // Given
        when(chatRoomRepository.findById(testRoomId)).thenReturn(Optional.empty());

        // When
        Optional<ChatRoom> result = chatService.getChatRoomById(testRoomId);

        // Then
        assertTrue(result.isEmpty());
    }

    // ========== archiveChatRoom() Tests ==========

    @Test
    @DisplayName("Should archive chat room successfully")
    void archiveChatRoom_withValidId_shouldArchiveRoom() {
        // Given
        testChatRoom.setIsActive(true);
        when(chatRoomRepository.findById(testRoomId)).thenReturn(Optional.of(testChatRoom));
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(testChatRoom);

        // When
        chatService.archiveChatRoom(testRoomId);

        // Then
        verify(chatRoomRepository).save(any(ChatRoom.class));
    }

    @Test
    @DisplayName("Should handle archiving non-existent room gracefully")
    void archiveChatRoom_withInvalidId_shouldHandleGracefully() {
        // Given
        when(chatRoomRepository.findById(testRoomId)).thenReturn(Optional.empty());

        // When & Then
        assertDoesNotThrow(() -> chatService.archiveChatRoom(testRoomId));
    }

    // ========== getChatStatistics() Tests ==========

    @Test
    @DisplayName("Should return chat statistics")
    void getChatStatistics_shouldReturnStats() {
        // Given
        when(chatRoomRepository.count()).thenReturn(10L);
        when(chatRoomRepository.countActiveRooms()).thenReturn(8L);
        when(messageRepository.count()).thenReturn(50L);
        when(messageRepository.countUnreadMessages()).thenReturn(5L);

        // When
        Map<String, Object> result = chatService.getChatStatistics();

        // Then
        assertNotNull(result);
        assertEquals(10L, result.get("totalRooms"));
        assertEquals(8L, result.get("activeRooms"));
        assertEquals(50L, result.get("totalMessages"));
        assertEquals(5L, result.get("unreadMessages"));
        verify(chatRoomRepository).count();
        verify(chatRoomRepository).countActiveRooms();
    }

    // ========== canUserChatWithRestaurant() Tests ==========

    @Test
    @DisplayName("Should return true for customer role")
    void canUserChatWithRestaurant_withCustomerRole_shouldReturnTrue() {
        // When
        boolean result = chatService.canUserChatWithRestaurant(testUserId, UserRole.CUSTOMER, testRestaurantId);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return true for admin role")
    void canUserChatWithRestaurant_withAdminRole_shouldReturnTrue() {
        // When
        boolean result = chatService.canUserChatWithRestaurant(testUserId, UserRole.ADMIN, testRestaurantId);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Should check ownership for restaurant owner role")
    void canUserChatWithRestaurant_withRestaurantOwnerRole_shouldCheckOwnership() {
        // Given
        List<RestaurantProfile> restaurants = List.of(testRestaurant);
        when(restaurantProfileRepository.findByOwnerUserId(testUserId)).thenReturn(restaurants);

        // When
        boolean result = chatService.canUserChatWithRestaurant(testUserId, UserRole.RESTAURANT_OWNER, testRestaurantId);

        // Then
        assertTrue(result);
        verify(restaurantProfileRepository).findByOwnerUserId(testUserId);
    }

    // ========== getAvailableAdmins() Tests ==========

    @Test
    @DisplayName("Should return available admin users")
    void getAvailableAdmins_shouldReturnAdmins() {
        // Given
        User admin1 = new User();
        admin1.setId(UUID.randomUUID());
        admin1.setRole(UserRole.ADMIN);
        User admin2 = new User();
        admin2.setId(UUID.randomUUID());
        admin2.setRole(UserRole.ADMIN);
        
        Page<User> adminPage = new PageImpl<>(List.of(admin1, admin2));
        when(userRepository.findByRole(eq(UserRole.ADMIN), any())).thenReturn(adminPage);

        // When
        List<User> result = chatService.getAvailableAdmins();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    // ========== getAvailableRestaurants() Tests ==========

    @Test
    @DisplayName("Should return available restaurants")
    void getAvailableRestaurants_shouldReturnRestaurants() {
        // Given
        List<RestaurantProfile> restaurants = List.of(testRestaurant);
        when(restaurantProfileRepository.findAll()).thenReturn(restaurants);

        // When
        List<com.example.booking.dto.RestaurantChatDto> result = chatService.getAvailableRestaurants();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(restaurantProfileRepository).findAll();
    }

    // ========== getUnreadCountForRoom() Tests ==========

    @Test
    @DisplayName("Should return unread count map for room")
    void getUnreadCountForRoom_shouldReturnCountMap() {
        // Given
        when(messageRepository.countUnreadMessagesByRoomIdAndUserId(testRoomId, testUserId))
                .thenReturn(3L);

        // When
        Map<String, Object> result = chatService.getUnreadCountForRoom(testRoomId, testUserId);

        // Then
        assertNotNull(result);
        assertEquals(testRoomId, result.get("roomId"));
        assertEquals(testUserId, result.get("userId"));
        assertEquals(3L, result.get("unreadCount"));
    }

    // ========== getTotalUnreadCountForUser() Tests ==========

    @Test
    @DisplayName("Should return total unread count map for user")
    void getTotalUnreadCountForUser_shouldReturnTotalCountMap() {
        // Given
        when(messageRepository.countTotalUnreadMessagesByUserId(testUserId)).thenReturn(10L);

        // When
        Map<String, Object> result = chatService.getTotalUnreadCountForUser(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(testUserId, result.get("userId"));
        assertEquals(10L, result.get("totalUnreadCount"));
    }

    // ========== getExistingRoomId() Tests ==========

    @Test
    @DisplayName("Should return existing room ID when room exists")
    void getExistingRoomId_withExistingRoom_shouldReturnRoomId() {
        // Given
        when(chatRoomRepository.findExistingRoom(testUserId, testRestaurantId))
                .thenReturn(Optional.of(testChatRoom));

        // When
        String result = chatService.getExistingRoomId(testUserId, UserRole.CUSTOMER, testRestaurantId);

        // Then
        assertNotNull(result);
        assertEquals(testRoomId, result);
    }

    @Test
    @DisplayName("Should return null when room does not exist")
    void getExistingRoomId_withNoExistingRoom_shouldReturnNull() {
        // Given
        when(chatRoomRepository.findExistingRoom(testUserId, testRestaurantId))
                .thenReturn(Optional.empty());

        // When
        String result = chatService.getExistingRoomId(testUserId, UserRole.CUSTOMER, testRestaurantId);

        // Then
        assertNull(result);
    }

    // ========== getRoomId() Tests ==========

    @Test
    @DisplayName("Should return room ID for customer role")
    void getRoomId_withCustomerRole_shouldReturnRoomId() {
        // Given
        when(chatRoomRepository.findByCustomerAndRestaurant(testUserId, testRestaurantId))
                .thenReturn(Optional.of(testChatRoom));

        // When
        String result = chatService.getRoomId(testUserId, UserRole.CUSTOMER, testRestaurantId);

        // Then
        assertNotNull(result);
        assertEquals(testRoomId, result);
    }

    @Test
    @DisplayName("Should return room ID for admin role")
    void getRoomId_withAdminRole_shouldReturnRoomId() {
        // Given
        UUID adminId = UUID.randomUUID();
        when(chatRoomRepository.findByAdminAndRestaurant(adminId, testRestaurantId))
                .thenReturn(Optional.of(testChatRoom));

        // When
        String result = chatService.getRoomId(adminId, UserRole.ADMIN, testRestaurantId);

        // Then
        assertNotNull(result);
        assertEquals(testRoomId, result);
    }

    // ========== getRestaurantOwnerId() Tests ==========

    @Test
    @DisplayName("Should return restaurant owner ID")
    void getRestaurantOwnerId_withValidRestaurant_shouldReturnOwnerId() {
        // Given
        UUID ownerUserId = UUID.randomUUID();
        RestaurantOwner owner = new RestaurantOwner();
        owner.setOwnerId(UUID.randomUUID());
        User ownerUser = new User();
        ownerUser.setId(ownerUserId);
        owner.setUser(ownerUser);
        testRestaurant.setOwner(owner);
        
        when(restaurantProfileRepository.findById(testRestaurantId))
                .thenReturn(Optional.of(testRestaurant));

        // When
        UUID result = chatService.getRestaurantOwnerId(testRestaurantId);

        // Then
        assertNotNull(result);
        assertEquals(ownerUserId, result);
    }

    @Test
    @DisplayName("Should return null when restaurant not found")
    void getRestaurantOwnerId_withInvalidRestaurant_shouldReturnNull() {
        // Given
        when(restaurantProfileRepository.findById(testRestaurantId))
                .thenReturn(Optional.empty());

        // When
        UUID result = chatService.getRestaurantOwnerId(testRestaurantId);

        // Then
        assertNull(result);
    }

    // ========== getUserChatRooms() - Additional Tests ==========

    @Test
    @DisplayName("Should return chat rooms for restaurant owner role")
    void getUserChatRooms_withRestaurantOwnerRole_shouldReturnRooms() {
        // Given
        List<ChatRoom> rooms = List.of(testChatRoom);
        when(chatRoomRepository.findByRestaurantOwnerId(testUserId)).thenReturn(rooms);

        // When
        List<ChatRoomDto> result = chatService.getUserChatRooms(testUserId, UserRole.RESTAURANT_OWNER);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(chatRoomRepository).findByRestaurantOwnerId(testUserId);
    }

    @Test
    @DisplayName("Should return chat rooms for admin role")
    void getUserChatRooms_withAdminRole_shouldReturnRooms() {
        // Given
        List<ChatRoom> rooms = List.of(testChatRoom);
        when(chatRoomRepository.findByAdminId(testUserId)).thenReturn(rooms);

        // When
        List<ChatRoomDto> result = chatService.getUserChatRooms(testUserId, UserRole.ADMIN);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(chatRoomRepository).findByAdminId(testUserId);
    }

    // ========== convertToMessageDto() Tests ==========

    @Test
    @DisplayName("shouldConvertToMessageDto_successfully")
    void shouldConvertToMessageDto_successfully() {
        // Given
        Message message = new Message();
        message.setMessageId(1);
        message.setRoom(testChatRoom);
        message.setContent("Test message");
        message.setMessageType(MessageType.TEXT);
        message.setSentAt(java.time.LocalDateTime.now());
        message.setIsRead(false);
        
        // Mock sender
        User sender = new User();
        sender.setId(testUserId);
        sender.setFullName("Test User");
        message.setSender(sender);

        List<Message> messages = Arrays.asList(message);
        org.springframework.data.domain.Page<Message> page = new org.springframework.data.domain.PageImpl<>(messages);
        
        when(messageRepository.findByRoomIdOrderBySentAtAsc(eq(testRoomId), any(org.springframework.data.domain.PageRequest.class)))
            .thenReturn(page);

        // When
        List<ChatMessageDto> result = chatService.getMessages(testRoomId, 0, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        ChatMessageDto dto = result.get(0);
        assertEquals(message.getMessageId(), dto.getMessageId());
        assertEquals(testRoomId, dto.getRoomId());
        assertEquals(testUserId, dto.getSenderId());
        assertEquals("Test message", dto.getContent());
    }

    // ========== getRestaurantOwnerId() - Exception Tests ==========

    @Test
    @DisplayName("shouldReturnNull_whenRestaurantOwnerIdHasException")
    void shouldReturnNull_whenRestaurantOwnerIdHasException() {
        // Given - Mock repository to throw exception
        when(restaurantProfileRepository.findById(testRestaurantId))
            .thenThrow(new RuntimeException("Database error"));

        // When
        UUID result = chatService.getRestaurantOwnerId(testRestaurantId);

        // Then - Should handle exception gracefully and return null
        assertNull(result);
    }

    @Test
    @DisplayName("shouldReturnNull_whenRestaurantNotFoundForOwnerId")
    void shouldReturnNull_whenRestaurantNotFoundForOwnerId() {
        // Given
        when(restaurantProfileRepository.findById(testRestaurantId))
            .thenReturn(Optional.empty());

        // When
        UUID result = chatService.getRestaurantOwnerId(testRestaurantId);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("shouldReturnNull_whenRestaurantHasNoOwner")
    void shouldReturnNull_whenRestaurantHasNoOwner() {
        // Given
        testRestaurant.setOwner(null);
        when(restaurantProfileRepository.findById(testRestaurantId))
            .thenReturn(Optional.of(testRestaurant));

        // When
        UUID result = chatService.getRestaurantOwnerId(testRestaurantId);

        // Then - Should handle null owner gracefully (exception caught)
        // The method will catch NullPointerException and return null
        assertNull(result);
    }
}

