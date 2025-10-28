package com.example.booking.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.booking.domain.Notification;
import com.example.booking.domain.NotificationStatus;
import com.example.booking.domain.NotificationType;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.notification.NotificationForm;
import com.example.booking.dto.notification.NotificationView;
import com.example.booking.repository.NotificationRepository;
import com.example.booking.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationServiceImpl Unit Tests")
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private NotificationForm form;
    private User mockUser;
    private Notification mockNotification;
    private UUID adminId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        adminId = UUID.randomUUID();
        userId = UUID.randomUUID();

        // Setup NotificationForm
        form = new NotificationForm();
        form.setTitle("Test Notification");
        form.setContent("Test content");
        form.setType(NotificationType.SYSTEM_ANNOUNCEMENT);
        form.setPriority(1);
        form.setPublishAt(LocalDateTime.now());

        // Setup User
        mockUser = new User();
        mockUser.setId(userId);
        mockUser.setEmailVerified(true);
        mockUser.setRole(UserRole.CUSTOMER);

        // Setup Notification
        mockNotification = new Notification();
        mockNotification.setNotificationId(1);
        mockNotification.setRecipientUserId(userId);
        mockNotification.setType(NotificationType.SYSTEM_ANNOUNCEMENT);
        mockNotification.setTitle("Test");
        mockNotification.setContent("Content");
        mockNotification.setStatus(NotificationStatus.SENT);
        mockNotification.setReadAt(null);
        mockNotification.setPublishAt(LocalDateTime.now());
    }

    // ==================== SEND TO ALL TESTS ====================

    @Test
    @DisplayName("testSendToAll_WithValidForm_ShouldSendToAllActiveUsers")
    void testSendToAll_WithValidForm_ShouldSendToAllActiveUsers() {
        // Given
        List<User> activeUsers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setEmailVerified(true);
            activeUsers.add(user);
        }
        
        when(userRepository.findAll()).thenReturn(activeUsers);
        when(notificationRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

        // When
        int result = notificationService.sendToAll(form, adminId);

        // Then
        assertEquals(10, result);
        verify(notificationRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("testSendToAll_WithEmptyActiveUsers_ShouldReturnZero")
    void testSendToAll_WithEmptyActiveUsers_ShouldReturnZero() {
        // Given
        when(userRepository.findAll()).thenReturn(new ArrayList<>());
        when(notificationRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

        // When
        int result = notificationService.sendToAll(form, adminId);

        // Then
        assertEquals(0, result);
        verify(notificationRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("testSendToAll_WithDisabledUsers_ShouldExcludeThem")
    void testSendToAll_WithDisabledUsers_ShouldExcludeThem() {
        // Given
        User enabledUser1 = new User();
        enabledUser1.setId(UUID.randomUUID());
        enabledUser1.setEmailVerified(true);

        User disabledUser = new User();
        disabledUser.setId(UUID.randomUUID());
        disabledUser.setEmailVerified(false);

        User enabledUser2 = new User();
        enabledUser2.setId(UUID.randomUUID());
        enabledUser2.setEmailVerified(true);

        when(userRepository.findAll()).thenReturn(Arrays.asList(enabledUser1, disabledUser, enabledUser2));
        when(notificationRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

        // When
        int result = notificationService.sendToAll(form, adminId);

        // Then
        assertEquals(2, result); // Only 2 enabled users
        verify(notificationRepository).saveAll(anyList());
    }

    // ==================== SEND TO ROLES TESTS ====================

    @Test
    @DisplayName("testSendToRoles_WithMultipleRoles_ShouldSendToMatchingUsers")
    void testSendToRoles_WithMultipleRoles_ShouldSendToMatchingUsers() {
        // Given
        Set<UserRole> roles = new HashSet<>(Arrays.asList(UserRole.CUSTOMER, UserRole.RESTAURANT_OWNER));
        
        List<User> allUsers = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setEmailVerified(true);
            user.setRole(UserRole.CUSTOMER);
            allUsers.add(user);
        }
        for (int i = 0; i < 3; i++) {
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setEmailVerified(true);
            user.setRole(UserRole.RESTAURANT_OWNER);
            allUsers.add(user);
        }
        
        when(userRepository.findAll()).thenReturn(allUsers);
        when(notificationRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

        // When
        int result = notificationService.sendToRoles(form, roles, adminId);

        // Then
        assertEquals(8, result);
        verify(notificationRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("testSendToRoles_WithSingleRole_ShouldFilterCorrectly")
    void testSendToRoles_WithSingleRole_ShouldFilterCorrectly() {
        // Given
        Set<UserRole> roles = new HashSet<>(Arrays.asList(UserRole.ADMIN));
        
        List<User> allUsers = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setEmailVerified(true);
            user.setRole(UserRole.ADMIN);
            allUsers.add(user);
        }
        
        when(userRepository.findAll()).thenReturn(allUsers);
        when(notificationRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

        // When
        int result = notificationService.sendToRoles(form, roles, adminId);

        // Then
        assertEquals(3, result);
        verify(notificationRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("testSendToRoles_WithEmptyRoles_ShouldReturnZero")
    void testSendToRoles_WithEmptyRoles_ShouldReturnZero() {
        // Given
        Set<UserRole> roles = new HashSet<>();
        when(userRepository.findAll()).thenReturn(new ArrayList<>());
        when(notificationRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

        // When
        int result = notificationService.sendToRoles(form, roles, adminId);

        // Then
        assertEquals(0, result);
        verify(notificationRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("testSendToRoles_WithDisabledUsers_ShouldSkipThem")
    void testSendToRoles_WithDisabledUsers_ShouldSkipThem() {
        // Given
        Set<UserRole> roles = new HashSet<>(Arrays.asList(UserRole.CUSTOMER));
        
        List<User> allUsers = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setEmailVerified(true);
            user.setRole(UserRole.CUSTOMER);
            allUsers.add(user);
        }
        
        User disabledUser1 = new User();
        disabledUser1.setId(UUID.randomUUID());
        disabledUser1.setEmailVerified(false);
        disabledUser1.setRole(UserRole.CUSTOMER);
        allUsers.add(disabledUser1);
        
        User disabledUser2 = new User();
        disabledUser2.setId(UUID.randomUUID());
        disabledUser2.setEmailVerified(false);
        disabledUser2.setRole(UserRole.CUSTOMER);
        allUsers.add(disabledUser2);
        
        when(userRepository.findAll()).thenReturn(allUsers);
        when(notificationRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

        // When
        int result = notificationService.sendToRoles(form, roles, adminId);

        // Then
        assertEquals(3, result); // Only 3 enabled users
        verify(notificationRepository).saveAll(anyList());
    }

    // ==================== SEND TO USERS TESTS ====================

    @Test
    @DisplayName("testSendToUsers_WithValidUserIds_ShouldSendToSpecifiedUsers")
    void testSendToUsers_WithValidUserIds_ShouldSendToSpecifiedUsers() {
        // Given
        Set<UUID> userIds = new HashSet<>(Arrays.asList(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));
        List<User> users = new ArrayList<>();
        for (UUID id : userIds) {
            User user = new User();
            user.setId(id);
            user.setEmailVerified(true);
            users.add(user);
        }
        
        when(userRepository.findAllById(userIds)).thenReturn(users);
        when(notificationRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

        // When
        int result = notificationService.sendToUsers(form, userIds, adminId);

        // Then
        assertEquals(3, result);
        verify(notificationRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("testSendToUsers_WithMixedActiveDisabled_ShouldOnlySendToActive")
    void testSendToUsers_WithMixedActiveDisabled_ShouldOnlySendToActive() {
        // Given
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        UUID userId3 = UUID.randomUUID();
        Set<UUID> userIds = new HashSet<>(Arrays.asList(userId1, userId2, userId3));
        
        User user1 = new User();
        user1.setId(userId1);
        user1.setEmailVerified(true);
        
        User user2 = new User();
        user2.setId(userId2);
        user2.setEmailVerified(false);
        
        User user3 = new User();
        user3.setId(userId3);
        user3.setEmailVerified(true);
        
        when(userRepository.findAllById(userIds)).thenReturn(Arrays.asList(user1, user2, user3));
        when(notificationRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

        // When
        int result = notificationService.sendToUsers(form, userIds, adminId);

        // Then
        assertEquals(2, result); // Only 2 active users
        verify(notificationRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("testSendToUsers_WithNonExistentUserIds_ShouldSkipThem")
    void testSendToUsers_WithNonExistentUserIds_ShouldSkipThem() {
        // Given
        UUID existentId = UUID.randomUUID();
        UUID nonExistentId = UUID.randomUUID();
        Set<UUID> userIds = new HashSet<>(Arrays.asList(existentId, nonExistentId));
        
        User user = new User();
        user.setId(existentId);
        user.setEmailVerified(true);
        
        when(userRepository.findAllById(userIds)).thenReturn(Arrays.asList(user));
        when(notificationRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

        // When
        int result = notificationService.sendToUsers(form, userIds, adminId);

        // Then
        assertEquals(1, result); // Only existing user
        verify(notificationRepository).saveAll(anyList());
    }


    // ==================== FIND BY USER ID TESTS ====================

    @Test
    @DisplayName("testFindByUserId_WithExistingNotifications_ShouldReturnPage")
    void testFindByUserId_WithExistingNotifications_ShouldReturnPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 5);
        List<Notification> notifications = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Notification notification = new Notification();
            notification.setNotificationId(i);
            notification.setRecipientUserId(userId);
            notification.setStatus(NotificationStatus.SENT);
            notification.setPublishAt(LocalDateTime.now().minusDays(i));
            notifications.add(notification);
        }
        
        Page<Notification> notificationPage = new PageImpl<>(notifications.subList(0, 5));
        when(notificationRepository.findByRecipientUserIdAndStatusOrderByPublishAtDesc(
            eq(userId), eq(NotificationStatus.SENT), any(Pageable.class)))
            .thenReturn(notificationPage);

        // When
        Page<NotificationView> result = notificationService.findByUserId(userId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(5, result.getContent().size());
    }

    @Test
    @DisplayName("testFindByUserId_WithNoNotifications_ShouldReturnEmptyPage")
    void testFindByUserId_WithNoNotifications_ShouldReturnEmptyPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 5);
        Page<Notification> emptyPage = new PageImpl<>(new ArrayList<>());
        
        when(notificationRepository.findByRecipientUserIdAndStatusOrderByPublishAtDesc(
            eq(userId), eq(NotificationStatus.SENT), any(Pageable.class)))
            .thenReturn(emptyPage);

        // When
        Page<NotificationView> result = notificationService.findByUserId(userId, pageable);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== COUNT UNREAD TESTS ====================

    @Test
    @DisplayName("testCountUnreadByUserId_WithUnreadNotifications_ShouldReturnCount")
    void testCountUnreadByUserId_WithUnreadNotifications_ShouldReturnCount() {
        // Given
        when(notificationRepository.countByRecipientUserIdAndStatusAndReadAtIsNull(
            eq(userId), eq(NotificationStatus.SENT)))
            .thenReturn(7L);

        // When
        long result = notificationService.countUnreadByUserId(userId);

        // Then
        assertEquals(7L, result);
    }

    @Test
    @DisplayName("testCountUnreadByUserId_WithAllRead_ShouldReturnZero")
    void testCountUnreadByUserId_WithAllRead_ShouldReturnZero() {
        // Given
        when(notificationRepository.countByRecipientUserIdAndStatusAndReadAtIsNull(
            eq(userId), eq(NotificationStatus.SENT)))
            .thenReturn(0L);

        // When
        long result = notificationService.countUnreadByUserId(userId);

        // Then
        assertEquals(0L, result);
    }

    // ==================== MARK AS READ TESTS ====================

    @Test
    @DisplayName("testMarkAsRead_WithValidNotification_ShouldMarkAsRead")
    void testMarkAsRead_WithValidNotification_ShouldMarkAsRead() {
        // When
        notificationService.markAsRead(1, userId);

        // Then
        verify(notificationRepository).markAsRead(eq(1), eq(userId), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("testMarkAsRead_WithNonExistentNotification_ShouldStillCall")
    void testMarkAsRead_WithNonExistentNotification_ShouldStillCall() {
        // When
        notificationService.markAsRead(999, userId);

        // Then
        verify(notificationRepository).markAsRead(eq(999), eq(userId), any(LocalDateTime.class));
    }

    // ==================== MARK ALL AS READ TESTS ====================

    @Test
    @DisplayName("testMarkAllAsRead_WithMultipleUnread_ShouldMarkAll")
    void testMarkAllAsRead_WithMultipleUnread_ShouldMarkAll() {
        // When
        notificationService.markAllAsRead(userId);

        // Then
        verify(notificationRepository).markAllAsRead(eq(userId), any(LocalDateTime.class));
    }

    // ==================== GET LATEST NOTIFICATIONS TESTS ====================

    @Test
    @DisplayName("testGetLatestNotifications_WithMultipleNotifications_ShouldReturnTop5")
    void testGetLatestNotifications_WithMultipleNotifications_ShouldReturnTop5() {
        // Given
        List<Notification> notifications = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Notification notification = new Notification();
            notification.setNotificationId(i);
            notification.setRecipientUserId(userId);
            notification.setStatus(NotificationStatus.SENT);
            notification.setPublishAt(LocalDateTime.now().minusMinutes(i));
            notifications.add(notification);
        }
        
        when(notificationRepository.findTop5ByRecipientUserIdAndStatusOrderByPublishAtDesc(
            eq(userId), eq(NotificationStatus.SENT), any(Pageable.class)))
            .thenReturn(notifications.subList(0, 5));

        // When
        List<NotificationView> result = notificationService.getLatestNotifications(userId);

        // Then
        assertNotNull(result);
        assertTrue(result.size() <= 5);
    }

    @Test
    @DisplayName("testGetLatestNotifications_WithLessThan5_ShouldReturnAll")
    void testGetLatestNotifications_WithLessThan5_ShouldReturnAll() {
        // Given
        List<Notification> notifications = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Notification notification = new Notification();
            notification.setNotificationId(i);
            notification.setRecipientUserId(userId);
            notification.setStatus(NotificationStatus.SENT);
            notification.setPublishAt(LocalDateTime.now().minusMinutes(i));
            notifications.add(notification);
        }
        
        when(notificationRepository.findTop5ByRecipientUserIdAndStatusOrderByPublishAtDesc(
            eq(userId), eq(NotificationStatus.SENT), any(Pageable.class)))
            .thenReturn(notifications);

        // When
        List<NotificationView> result = notificationService.getLatestNotifications(userId);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
    }
}

