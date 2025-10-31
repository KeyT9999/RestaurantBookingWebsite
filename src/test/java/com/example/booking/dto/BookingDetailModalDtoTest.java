package com.example.booking.dto;

import com.example.booking.common.enums.BookingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BookingDetailModalDto Test")
class BookingDetailModalDtoTest {

    @Test
    @DisplayName("Should create BookingDetailModalDto and set/get all fields")
    void testBookingDetailModalDto_ShouldSetAndGetFields() {
        BookingDetailModalDto dto = new BookingDetailModalDto();
        LocalDateTime now = LocalDateTime.now();

        dto.setBookingId(1);
        dto.setStatus(BookingStatus.CONFIRMED);
        dto.setBookingTime(now);
        dto.setNumberOfGuests(4);
        dto.setNote("Window seat preferred");
        dto.setDepositAmount(BigDecimal.valueOf(200000));
        dto.setCreatedAt(now);
        dto.setUpdatedAt(now);

        BookingDetailModalDto.CustomerInfo customerInfo = new BookingDetailModalDto.CustomerInfo();
        customerInfo.setCustomerId("customer-uuid");
        customerInfo.setFullName("John Doe");
        customerInfo.setPhoneNumber("0123456789");
        customerInfo.setEmail("john@example.com");
        customerInfo.setAddress("123 Main St");

        BookingDetailModalDto.RestaurantInfo restaurantInfo = new BookingDetailModalDto.RestaurantInfo();
        restaurantInfo.setRestaurantId(1);
        restaurantInfo.setRestaurantName("Test Restaurant");

        BookingDetailModalDto.BookingTableInfo tableInfo = new BookingDetailModalDto.BookingTableInfo();
        tableInfo.setBookingTableId(1);
        tableInfo.setTableName("Table 5");
        tableInfo.setAssignedAt(now);

        dto.setCustomer(customerInfo);
        dto.setRestaurant(restaurantInfo);
        dto.setBookingTables(Arrays.asList(tableInfo));
        dto.setBookingDishes(Arrays.asList());
        dto.setBookingServices(Arrays.asList());
        dto.setInternalNotes(Arrays.asList());
        dto.setCommunicationHistory(Arrays.asList());

        assertEquals(1, dto.getBookingId());
        assertEquals(BookingStatus.CONFIRMED, dto.getStatus());
        assertEquals(now, dto.getBookingTime());
        assertEquals(4, dto.getNumberOfGuests());
        assertEquals("Window seat preferred", dto.getNote());
        assertEquals(BigDecimal.valueOf(200000), dto.getDepositAmount());
        assertNotNull(dto.getCustomer());
        assertNotNull(dto.getRestaurant());
        assertEquals(1, dto.getBookingTables().size());
    }

    @Test
    @DisplayName("Should test CustomerInfo inner class")
    void testCustomerInfo() {
        BookingDetailModalDto.CustomerInfo customerInfo = new BookingDetailModalDto.CustomerInfo();
        
        customerInfo.setCustomerId("uuid-123");
        customerInfo.setFullName("Jane Doe");
        customerInfo.setPhoneNumber("0987654321");
        customerInfo.setEmail("jane@example.com");
        customerInfo.setAddress("456 Oak St");

        assertEquals("uuid-123", customerInfo.getCustomerId());
        assertEquals("Jane Doe", customerInfo.getFullName());
        assertEquals("0987654321", customerInfo.getPhoneNumber());
        assertEquals("jane@example.com", customerInfo.getEmail());
        assertEquals("456 Oak St", customerInfo.getAddress());
    }

    @Test
    @DisplayName("Should test RestaurantInfo inner class")
    void testRestaurantInfo() {
        BookingDetailModalDto.RestaurantInfo restaurantInfo = new BookingDetailModalDto.RestaurantInfo();
        
        restaurantInfo.setRestaurantId(10);
        restaurantInfo.setRestaurantName("Fine Dining");

        assertEquals(10, restaurantInfo.getRestaurantId());
        assertEquals("Fine Dining", restaurantInfo.getRestaurantName());
    }

    @Test
    @DisplayName("Should test BookingTableInfo inner class")
    void testBookingTableInfo() {
        BookingDetailModalDto.BookingTableInfo tableInfo = new BookingDetailModalDto.BookingTableInfo();
        LocalDateTime assignedAt = LocalDateTime.now();
        
        tableInfo.setBookingTableId(5);
        tableInfo.setTableName("Table 12");
        tableInfo.setAssignedAt(assignedAt);

        assertEquals(5, tableInfo.getBookingTableId());
        assertEquals("Table 12", tableInfo.getTableName());
        assertEquals(assignedAt, tableInfo.getAssignedAt());
    }
}

