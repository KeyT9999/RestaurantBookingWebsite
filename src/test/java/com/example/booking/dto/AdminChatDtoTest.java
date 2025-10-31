package com.example.booking.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AdminChatDto Test")
class AdminChatDtoTest {

    @Test
    @DisplayName("Should create AdminChatDto and set/get all fields")
    void testAdminChatDto_ShouldSetAndGetFields() {
        AdminChatDto dto = new AdminChatDto();
        UUID adminId = UUID.randomUUID();

        AdminChatDto.RestaurantInfo restaurantInfo = new AdminChatDto.RestaurantInfo();
        restaurantInfo.setRestaurantId(1L);
        restaurantInfo.setRestaurantName("Test Restaurant");

        dto.setAdminId(adminId);
        dto.setAdminName("Admin User");
        dto.setAdminEmail("admin@example.com");
        dto.setActive(true);
        dto.setRestaurants(Arrays.asList(restaurantInfo));

        assertEquals(adminId, dto.getAdminId());
        assertEquals("Admin User", dto.getAdminName());
        assertEquals("admin@example.com", dto.getAdminEmail());
        assertTrue(dto.isActive());
        assertEquals(1, dto.getRestaurants().size());
    }

    @Test
    @DisplayName("Should create AdminChatDto with constructor")
    void testAdminChatDto_Constructor() {
        UUID adminId = UUID.randomUUID();
        AdminChatDto.RestaurantInfo restaurantInfo = new AdminChatDto.RestaurantInfo(1L, "Test Restaurant");
        List<AdminChatDto.RestaurantInfo> restaurants = Arrays.asList(restaurantInfo);

        AdminChatDto dto = new AdminChatDto(adminId, "Admin User", "admin@example.com", true, restaurants);

        assertEquals(adminId, dto.getAdminId());
        assertEquals("Admin User", dto.getAdminName());
        assertEquals("admin@example.com", dto.getAdminEmail());
        assertTrue(dto.isActive());
        assertEquals(1, dto.getRestaurants().size());
    }

    @Test
    @DisplayName("Should test RestaurantInfo inner class")
    void testRestaurantInfo() {
        AdminChatDto.RestaurantInfo info = new AdminChatDto.RestaurantInfo();
        
        info.setRestaurantId(10L);
        info.setRestaurantName("Fine Dining");

        assertEquals(10L, info.getRestaurantId());
        assertEquals("Fine Dining", info.getRestaurantName());
    }

    @Test
    @DisplayName("Should create RestaurantInfo with constructor")
    void testRestaurantInfo_Constructor() {
        AdminChatDto.RestaurantInfo info = new AdminChatDto.RestaurantInfo(5L, "Pizza Place");

        assertEquals(5L, info.getRestaurantId());
        assertEquals("Pizza Place", info.getRestaurantName());
    }
}

