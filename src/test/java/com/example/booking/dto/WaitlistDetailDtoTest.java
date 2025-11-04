package com.example.booking.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WaitlistDetailDto Test")
class WaitlistDetailDtoTest {

    @Test
    @DisplayName("Should create WaitlistDetailDto and set/get all fields")
    void testWaitlistDetailDto_ShouldSetAndGetFields() {
        WaitlistDetailDto dto = new WaitlistDetailDto();
        LocalDateTime joinTime = LocalDateTime.now();

        dto.setWaitlistId(1);
        dto.setCustomerName("John Doe");
        dto.setRestaurantName("Test Restaurant");
        dto.setPartySize(4);
        dto.setJoinTime(joinTime);
        dto.setStatus("WAITING");
        dto.setEstimatedWaitTime(30);
        dto.setQueuePosition(5);
        dto.setPreferredBookingTime("2024-12-25 19:00");
        dto.setSpecialRequests("Window seat");

        WaitlistDetailDto.WaitlistDishDto dishDto = new WaitlistDetailDto.WaitlistDishDto(
            "Pizza", "Delicious pizza", 2, BigDecimal.valueOf(100000), BigDecimal.valueOf(200000)
        );
        WaitlistDetailDto.WaitlistServiceDto serviceDto = new WaitlistDetailDto.WaitlistServiceDto(
            "VIP Service", "Premium service", BigDecimal.valueOf(50000)
        );
        WaitlistDetailDto.WaitlistTableDto tableDto = new WaitlistDetailDto.WaitlistTableDto(
            "Table 5", 4, "AVAILABLE"
        );

        dto.setDishes(Arrays.asList(dishDto));
        dto.setServices(Arrays.asList(serviceDto));
        dto.setTables(Arrays.asList(tableDto));

        assertEquals(1, dto.getWaitlistId());
        assertEquals("John Doe", dto.getCustomerName());
        assertEquals("Test Restaurant", dto.getRestaurantName());
        assertEquals(4, dto.getPartySize());
        assertEquals(joinTime, dto.getJoinTime());
        assertEquals("WAITING", dto.getStatus());
        assertEquals(30, dto.getEstimatedWaitTime());
        assertEquals(5, dto.getQueuePosition());
        assertEquals("2024-12-25 19:00", dto.getPreferredBookingTime());
        assertEquals("Window seat", dto.getSpecialRequests());
        assertEquals(1, dto.getDishes().size());
        assertEquals(1, dto.getServices().size());
        assertEquals(1, dto.getTables().size());
    }

    @Test
    @DisplayName("Should test WaitlistDishDto inner class")
    void testWaitlistDishDto() {
        WaitlistDetailDto.WaitlistDishDto dishDto = new WaitlistDetailDto.WaitlistDishDto();
        
        dishDto.setDishName("Pasta");
        dishDto.setDescription("Italian pasta");
        dishDto.setQuantity(1);
        dishDto.setPrice(BigDecimal.valueOf(150000));
        dishDto.setTotalPrice(BigDecimal.valueOf(150000));

        assertEquals("Pasta", dishDto.getDishName());
        assertEquals("Italian pasta", dishDto.getDescription());
        assertEquals(1, dishDto.getQuantity());
        assertEquals(BigDecimal.valueOf(150000), dishDto.getPrice());
        assertEquals(BigDecimal.valueOf(150000), dishDto.getTotalPrice());
    }

    @Test
    @DisplayName("Should test WaitlistServiceDto inner class")
    void testWaitlistServiceDto() {
        WaitlistDetailDto.WaitlistServiceDto serviceDto = new WaitlistDetailDto.WaitlistServiceDto();
        
        serviceDto.setServiceName("Live Music");
        serviceDto.setDescription("Jazz performance");
        serviceDto.setPrice(BigDecimal.valueOf(100000));

        assertEquals("Live Music", serviceDto.getServiceName());
        assertEquals("Jazz performance", serviceDto.getDescription());
        assertEquals(BigDecimal.valueOf(100000), serviceDto.getPrice());
    }

    @Test
    @DisplayName("Should test WaitlistTableDto inner class")
    void testWaitlistTableDto() {
        WaitlistDetailDto.WaitlistTableDto tableDto = new WaitlistDetailDto.WaitlistTableDto();
        
        tableDto.setTableName("Table 10");
        tableDto.setCapacity(6);
        tableDto.setStatus("OCCUPIED");

        assertEquals("Table 10", tableDto.getTableName());
        assertEquals(6, tableDto.getCapacity());
        assertEquals("OCCUPIED", tableDto.getStatus());
    }
}

