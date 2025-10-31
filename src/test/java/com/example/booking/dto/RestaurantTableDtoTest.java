package com.example.booking.dto;

import com.example.booking.common.enums.TableStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RestaurantTableDto Test")
class RestaurantTableDtoTest {

    @Test
    @DisplayName("Should create RestaurantTableDto and set/get all fields")
    void testRestaurantTableDto_ShouldSetAndGetFields() {
        RestaurantTableDto dto = new RestaurantTableDto();

        dto.setTableId(1);
        dto.setTableName("Table 5");
        dto.setCapacity(4);
        dto.setStatus(TableStatus.AVAILABLE);
        dto.setDepositAmount(BigDecimal.valueOf(200000));
        dto.setRestaurantId(1);

        assertEquals(1, dto.getTableId());
        assertEquals("Table 5", dto.getTableName());
        assertEquals(4, dto.getCapacity());
        assertEquals(TableStatus.AVAILABLE, dto.getStatus());
        assertEquals(BigDecimal.valueOf(200000), dto.getDepositAmount());
        assertEquals(1, dto.getRestaurantId());
    }

    @Test
    @DisplayName("Should create RestaurantTableDto with constructor")
    void testRestaurantTableDto_Constructor() {
        RestaurantTableDto dto = new RestaurantTableDto(
            1, "Table 10", 6, TableStatus.OCCUPIED,
            BigDecimal.valueOf(300000), 1
        );

        assertEquals(1, dto.getTableId());
        assertEquals("Table 10", dto.getTableName());
        assertEquals(6, dto.getCapacity());
        assertEquals(TableStatus.OCCUPIED, dto.getStatus());
        assertEquals(BigDecimal.valueOf(300000), dto.getDepositAmount());
    }
}

