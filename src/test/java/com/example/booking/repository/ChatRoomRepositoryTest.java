package com.example.booking.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.example.booking.domain.ChatRoom;
import com.example.booking.domain.Customer;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;

/**
 * Unit tests for ChatRoomRepository
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ChatRoomRepository Tests")
public class ChatRoomRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Test
    @DisplayName("shouldFindChatRoomById_successfully")
    void shouldFindChatRoomById_successfully() {
        // Given
        ChatRoom room = createTestChatRoom();
        entityManager.persistAndFlush(room);

        // When
        Optional<ChatRoom> found = chatRoomRepository.findById(room.getRoomId());

        // Then
        assertTrue(found.isPresent());
    }

    private ChatRoom createTestChatRoom() {
        User user = new User();
        user.setEmail("customer@test.com");
        entityManager.persist(user);

        Customer customer = new Customer();
        customer.setUser(user);
        entityManager.persist(customer);

        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantName("Test Restaurant");
        entityManager.persist(restaurant);

        ChatRoom room = new ChatRoom();
        room.setCustomer(customer);
        room.setRestaurant(restaurant);
        
        return room;
    }
}

