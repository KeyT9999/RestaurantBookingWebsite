package com.example.booking.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

/**
 * Unit test for Message
 * Coverage: 100% - All constructors, getters/setters, getSenderName branches, isFrom methods
 */
@DisplayName("Message Tests")
class MessageTest {

    private Message message;

    @BeforeEach
    void setUp() {
        message = new Message();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("shouldCreateDefaultConstructor")
        void shouldCreateDefaultConstructor() {
            // When
            Message message = new Message();

            // Then
            assertNotNull(message);
            assertNotNull(message.getSentAt());
            assertEquals(MessageType.TEXT, message.getMessageType());
        }

        @Test
        @DisplayName("shouldCreateConstructorWithRoomSenderAndContent")
        void shouldCreateConstructorWithRoomSenderAndContent() {
            // Given
            ChatRoom room = new ChatRoom();
            User sender = new User();
            String content = "Test message";

            // When
            Message message = new Message(room, sender, content);

            // Then
            assertNotNull(message);
            assertEquals(room, message.getRoom());
            assertEquals(sender, message.getSender());
            assertEquals(content, message.getContent());
        }

        @Test
        @DisplayName("shouldCreateConstructorWithMessageType")
        void shouldCreateConstructorWithMessageType() {
            // Given
            ChatRoom room = new ChatRoom();
            User sender = new User();
            String content = "Test message";
            MessageType messageType = MessageType.IMAGE;

            // When
            Message message = new Message(room, sender, content, messageType);

            // Then
            assertNotNull(message);
            assertEquals(room, message.getRoom());
            assertEquals(sender, message.getSender());
            assertEquals(content, message.getContent());
            assertEquals(messageType, message.getMessageType());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("shouldGetAndSetMessageId")
        void shouldGetAndSetMessageId() {
            // Given
            Integer messageId = 1;

            // When
            message.setMessageId(messageId);

            // Then
            assertEquals(messageId, message.getMessageId());
        }

        @Test
        @DisplayName("shouldGetAndSetRoom")
        void shouldGetAndSetRoom() {
            // Given
            ChatRoom room = new ChatRoom();

            // When
            message.setRoom(room);

            // Then
            assertEquals(room, message.getRoom());
        }

        @Test
        @DisplayName("shouldGetAndSetSender")
        void shouldGetAndSetSender() {
            // Given
            User sender = new User();

            // When
            message.setSender(sender);

            // Then
            assertEquals(sender, message.getSender());
        }

        @Test
        @DisplayName("shouldGetAndSetContent")
        void shouldGetAndSetContent() {
            // Given
            String content = "Test content";

            // When
            message.setContent(content);

            // Then
            assertEquals(content, message.getContent());
        }

        @Test
        @DisplayName("shouldGetAndSetMessageType")
        void shouldGetAndSetMessageType() {
            // Given
            MessageType messageType = MessageType.FILE;

            // When
            message.setMessageType(messageType);

            // Then
            assertEquals(messageType, message.getMessageType());
        }

        @Test
        @DisplayName("shouldGetAndSetFileUrl")
        void shouldGetAndSetFileUrl() {
            // Given
            String fileUrl = "https://example.com/file.jpg";

            // When
            message.setFileUrl(fileUrl);

            // Then
            assertEquals(fileUrl, message.getFileUrl());
        }

        @Test
        @DisplayName("shouldGetAndSetSentAt")
        void shouldGetAndSetSentAt() {
            // Given
            LocalDateTime sentAt = LocalDateTime.now();

            // When
            message.setSentAt(sentAt);

            // Then
            assertEquals(sentAt, message.getSentAt());
        }

        @Test
        @DisplayName("shouldGetAndSetIsRead")
        void shouldGetAndSetIsRead() {
            // Given
            Boolean isRead = true;

            // When
            message.setIsRead(isRead);

            // Then
            assertEquals(isRead, message.getIsRead());
        }

        @Test
        @DisplayName("shouldGetAndSetCustomer")
        void shouldGetAndSetCustomer() {
            // Given
            Customer customer = new Customer();

            // When
            message.setCustomer(customer);

            // Then
            assertEquals(customer, message.getCustomer());
        }

        @Test
        @DisplayName("shouldGetAndSetOwner")
        void shouldGetAndSetOwner() {
            // Given
            RestaurantOwner owner = new RestaurantOwner();

            // When
            message.setOwner(owner);

            // Then
            assertEquals(owner, message.getOwner());
        }
    }

    @Nested
    @DisplayName("getSenderName() Tests")
    class GetSenderNameTests {

        @Test
        @DisplayName("shouldReturnUnknown_whenSenderIsNull")
        void shouldReturnUnknown_whenSenderIsNull() {
            // Given
            message.setSender(null);

            // When
            String result = message.getSenderName();

            // Then
            assertEquals("Unknown", result);
        }

        @Test
        @DisplayName("shouldReturnSenderFullName_whenCustomerAndFullNameExists")
        void shouldReturnSenderFullName_whenCustomerAndFullNameExists() {
            // Given
            User sender = new User();
            sender.setRole(UserRole.CUSTOMER);
            sender.setFullName("John Doe");
            sender.setEmail("john@example.com");
            
            Customer customer = new Customer();
            customer.setFullName("Customer Name");
            
            message.setSender(sender);
            message.setCustomer(customer);

            // When
            String result = message.getSenderName();

            // Then
            assertEquals("John Doe", result); // Should prioritize sender.fullName
        }

        @Test
        @DisplayName("shouldReturnCustomerFullName_whenCustomerAndSenderFullNameIsEmpty")
        void shouldReturnCustomerFullName_whenCustomerAndSenderFullNameIsEmpty() {
            // Given
            User sender = new User();
            sender.setRole(UserRole.CUSTOMER);
            sender.setFullName(""); // Empty
            sender.setEmail("john@example.com");
            
            Customer customer = new Customer();
            customer.setFullName("Customer Name");
            
            message.setSender(sender);
            message.setCustomer(customer);

            // When
            String result = message.getSenderName();

            // Then
            assertEquals("Customer Name", result); // Fallback to customer.fullName
        }

        @Test
        @DisplayName("shouldReturnEmail_whenCustomerAndBothFullNamesEmpty")
        void shouldReturnEmail_whenCustomerAndBothFullNamesEmpty() {
            // Given
            User sender = new User();
            sender.setRole(UserRole.CUSTOMER);
            sender.setFullName(""); // Empty
            sender.setEmail("john@example.com");
            
            Customer customer = new Customer();
            customer.setFullName(""); // Empty
            
            message.setSender(sender);
            message.setCustomer(customer);

            // When
            String result = message.getSenderName();

            // Then
            assertEquals("john@example.com", result); // Fallback to email
        }

        @Test
        @DisplayName("shouldReturnUnknownCustomer_whenCustomerAndEmailIsNull")
        void shouldReturnUnknownCustomer_whenCustomerAndEmailIsNull() {
            // Given
            User sender = new User();
            sender.setRole(UserRole.CUSTOMER);
            sender.setFullName(""); // Empty
            sender.setEmail(null);
            
            Customer customer = new Customer();
            customer.setFullName(""); // Empty
            
            message.setSender(sender);
            message.setCustomer(customer);

            // When
            String result = message.getSenderName();

            // Then
            assertEquals("Unknown Customer", result); // Final fallback
        }

        @Test
        @DisplayName("shouldReturnSenderFullName_whenRestaurantOwner")
        void shouldReturnSenderFullName_whenRestaurantOwner() {
            // Given
            User sender = new User();
            sender.setRole(UserRole.RESTAURANT_OWNER);
            sender.setFullName("Restaurant Owner");
            sender.setEmail("owner@example.com");
            
            message.setSender(sender);

            // When
            String result = message.getSenderName();

            // Then
            assertEquals("Restaurant Owner", result);
        }

        @Test
        @DisplayName("shouldReturnEmail_whenRestaurantOwnerAndFullNameEmpty")
        void shouldReturnEmail_whenRestaurantOwnerAndFullNameEmpty() {
            // Given
            User sender = new User();
            sender.setRole(UserRole.RESTAURANT_OWNER);
            sender.setFullName(""); // Empty
            sender.setEmail("owner@example.com");
            
            message.setSender(sender);

            // When
            String result = message.getSenderName();

            // Then
            assertEquals("owner@example.com", result);
        }

        @Test
        @DisplayName("shouldReturnUnknown_whenRestaurantOwnerAndEmailNull")
        void shouldReturnUnknown_whenRestaurantOwnerAndEmailNull() {
            // Given
            User sender = new User();
            sender.setRole(UserRole.RESTAURANT_OWNER);
            sender.setFullName(""); // Empty
            sender.setEmail(null);
            
            message.setSender(sender);

            // When
            String result = message.getSenderName();

            // Then
            assertEquals("Unknown", result);
        }

        @Test
        @DisplayName("shouldReturnSenderFullName_whenAdmin")
        void shouldReturnSenderFullName_whenAdmin() {
            // Given
            User sender = new User();
            sender.setRole(UserRole.ADMIN);
            sender.setFullName("Admin User");
            sender.setEmail("admin@example.com");
            
            message.setSender(sender);

            // When
            String result = message.getSenderName();

            // Then
            assertEquals("Admin User", result);
        }

        @Test
        @DisplayName("shouldReturnEmail_whenAdminAndFullNameEmpty")
        void shouldReturnEmail_whenAdminAndFullNameEmpty() {
            // Given
            User sender = new User();
            sender.setRole(UserRole.ADMIN);
            sender.setFullName(""); // Empty
            sender.setEmail("admin@example.com");
            
            message.setSender(sender);

            // When
            String result = message.getSenderName();

            // Then
            assertEquals("admin@example.com", result);
        }

        @Test
        @DisplayName("shouldReturnUnknown_whenAdminAndEmailNull")
        void shouldReturnUnknown_whenAdminAndEmailNull() {
            // Given
            User sender = new User();
            sender.setRole(UserRole.ADMIN);
            sender.setFullName(""); // Empty
            sender.setEmail(null);
            
            message.setSender(sender);

            // When
            String result = message.getSenderName();

            // Then
            assertEquals("Unknown", result);
        }

        @Test
        @DisplayName("shouldReturnSenderFullName_whenCustomerAndFullNameIsWhitespace")
        void shouldReturnSenderFullName_whenCustomerAndFullNameIsWhitespace() {
            // Given
            User sender = new User();
            sender.setRole(UserRole.CUSTOMER);
            sender.setFullName("   "); // Whitespace only
            sender.setEmail("john@example.com");
            
            Customer customer = new Customer();
            customer.setFullName("Customer Name");
            
            message.setSender(sender);
            message.setCustomer(customer);

            // When
            String result = message.getSenderName();

            // Then
            assertEquals("Customer Name", result); // Should fallback when fullName is whitespace
        }
    }

    @Nested
    @DisplayName("isFromCustomer() Tests")
    class IsFromCustomerTests {

        @Test
        @DisplayName("shouldReturnTrue_whenSenderIsCustomer")
        void shouldReturnTrue_whenSenderIsCustomer() {
            // Given
            User sender = new User();
            sender.setRole(UserRole.CUSTOMER);
            message.setSender(sender);

            // When
            boolean result = message.isFromCustomer();

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("shouldReturnFalse_whenSenderIsNotCustomer")
        void shouldReturnFalse_whenSenderIsNotCustomer() {
            // Given
            User sender = new User();
            sender.setRole(UserRole.RESTAURANT_OWNER);
            message.setSender(sender);

            // When
            boolean result = message.isFromCustomer();

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("shouldReturnFalse_whenSenderIsNull")
        void shouldReturnFalse_whenSenderIsNull() {
            // Given
            message.setSender(null);

            // When
            boolean result = message.isFromCustomer();

            // Then
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("isFromRestaurantOwner() Tests")
    class IsFromRestaurantOwnerTests {

        @Test
        @DisplayName("shouldReturnTrue_whenSenderIsRestaurantOwner")
        void shouldReturnTrue_whenSenderIsRestaurantOwner() {
            // Given
            User sender = new User();
            sender.setRole(UserRole.RESTAURANT_OWNER);
            message.setSender(sender);

            // When
            boolean result = message.isFromRestaurantOwner();

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("shouldReturnFalse_whenSenderIsNotRestaurantOwner")
        void shouldReturnFalse_whenSenderIsNotRestaurantOwner() {
            // Given
            User sender = new User();
            sender.setRole(UserRole.CUSTOMER);
            message.setSender(sender);

            // When
            boolean result = message.isFromRestaurantOwner();

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("shouldReturnFalse_whenSenderIsNull")
        void shouldReturnFalse_whenSenderIsNull() {
            // Given
            message.setSender(null);

            // When
            boolean result = message.isFromRestaurantOwner();

            // Then
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("isFromAdmin() Tests")
    class IsFromAdminTests {

        @Test
        @DisplayName("shouldReturnTrue_whenSenderIsAdmin")
        void shouldReturnTrue_whenSenderIsAdmin() {
            // Given
            User sender = new User();
            sender.setRole(UserRole.ADMIN);
            message.setSender(sender);

            // When
            boolean result = message.isFromAdmin();

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("shouldReturnFalse_whenSenderIsNotAdmin")
        void shouldReturnFalse_whenSenderIsNotAdmin() {
            // Given
            User sender = new User();
            sender.setRole(UserRole.CUSTOMER);
            message.setSender(sender);

            // When
            boolean result = message.isFromAdmin();

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("shouldReturnFalse_whenSenderIsNull")
        void shouldReturnFalse_whenSenderIsNull() {
            // Given
            message.setSender(null);

            // When
            boolean result = message.isFromAdmin();

            // Then
            assertFalse(result);
        }
    }
}

