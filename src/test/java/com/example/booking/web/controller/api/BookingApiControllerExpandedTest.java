package com.example.booking.web.controller.api;

import com.example.booking.common.enums.TableStatus;
import com.example.booking.domain.*;
import com.example.booking.service.BookingService;
import com.example.booking.service.CustomerService;
import com.example.booking.service.RestaurantManagementService;
import com.example.booking.service.SimpleUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Expanded comprehensive tests for BookingApiController
 */
@WebMvcTest(BookingApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("BookingApiController Expanded Test Suite")
class BookingApiControllerExpandedTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestaurantManagementService restaurantService;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private SimpleUserService userService;

    @Nested
    @DisplayName("getTableLayoutsByRestaurant() Tests")
    class GetTableLayoutsTests {

        @Test
        @DisplayName("Should return table layouts successfully")
        void testGetTableLayouts_WithValidRestaurantId_ShouldReturnSuccess() throws Exception {
            RestaurantMedia media = new RestaurantMedia();
            media.setMediaId(1);
            media.setUrl("https://example.com/layout.jpg");
            media.setType("table_layout");
            media.setCreatedAt(LocalDateTime.now());

            when(restaurantService.findMediaByRestaurantAndType(eq(1), eq("table_layout")))
                    .thenReturn(Collections.singletonList(media));

            mockMvc.perform(get("/api/booking/restaurants/1/table-layouts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].mediaId").value(1))
                    .andExpect(jsonPath("$[0].url").exists())
                    .andExpect(jsonPath("$[0].type").value("table_layout"));

            verify(restaurantService).findMediaByRestaurantAndType(1, "table_layout");
        }

        @Test
        @DisplayName("Should return empty list when no layouts exist")
        void testGetTableLayouts_WithNoLayouts_ShouldReturnEmptyList() throws Exception {
            when(restaurantService.findMediaByRestaurantAndType(eq(1), eq("table_layout")))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/booking/restaurants/1/table-layouts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());

            verify(restaurantService).findMediaByRestaurantAndType(1, "table_layout");
        }

        @Test
        @DisplayName("Should handle exception when getting table layouts")
        void testGetTableLayouts_WhenServiceThrowsException_ShouldHandleError() throws Exception {
            when(restaurantService.findMediaByRestaurantAndType(eq(1), eq("table_layout")))
                    .thenThrow(new RuntimeException("Database error"));

            mockMvc.perform(get("/api/booking/restaurants/1/table-layouts"))
                    .andExpect(status().isBadRequest());

            verify(restaurantService).findMediaByRestaurantAndType(1, "table_layout");
        }
    }

    @Nested
    @DisplayName("getTablesByRestaurant() Tests")
    class GetTablesTests {

        @Test
        @DisplayName("Should return tables successfully")
        void testGetTables_WithValidRestaurantId_ShouldReturnSuccess() throws Exception {
            RestaurantTable table = new RestaurantTable();
            table.setTableId(1);
            table.setTableName("Table 1");
            table.setCapacity(4);
            table.setStatus(TableStatus.AVAILABLE);
            table.setDepositAmount(new BigDecimal("100.00"));

            RestaurantProfile restaurant = new RestaurantProfile();
            restaurant.setRestaurantId(1);
            table.setRestaurant(restaurant);

            when(restaurantService.findTablesByRestaurant(1))
                    .thenReturn(Collections.singletonList(table));

            mockMvc.perform(get("/api/booking/restaurants/1/tables"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].tableId").value(1))
                    .andExpect(jsonPath("$[0].tableName").value("Table 1"))
                    .andExpect(jsonPath("$[0].capacity").value(4))
                    .andExpect(jsonPath("$[0].restaurantId").value(1));

            verify(restaurantService).findTablesByRestaurant(1);
        }

        @Test
        @DisplayName("Should handle exception when getting tables")
        void testGetTables_WhenServiceThrowsException_ShouldHandleError() throws Exception {
            when(restaurantService.findTablesByRestaurant(1))
                    .thenThrow(new RuntimeException("Database error"));

            mockMvc.perform(get("/api/booking/restaurants/1/tables"))
                    .andExpect(status().isBadRequest());

            verify(restaurantService).findTablesByRestaurant(1);
        }
    }

    @Nested
    @DisplayName("getRestaurant() Tests")
    class GetRestaurantTests {

        @Test
        @DisplayName("Should return restaurant when found")
        void testGetRestaurant_WhenFound_ShouldReturnRestaurant() throws Exception {
            RestaurantProfile restaurant = new RestaurantProfile();
            restaurant.setRestaurantId(1);
            restaurant.setRestaurantName("Test Restaurant");
            restaurant.setAddress("123 Test St");
            restaurant.setPhone("0123456789");
            restaurant.setCuisineType("Vietnamese");
            restaurant.setAveragePrice(new BigDecimal("200000"));

            when(restaurantService.findRestaurantById(1))
                    .thenReturn(Optional.of(restaurant));

            mockMvc.perform(get("/api/booking/restaurants/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.restaurantId").value(1))
                    .andExpect(jsonPath("$.restaurantName").exists());

            verify(restaurantService).findRestaurantById(1);
        }

        @Test
        @DisplayName("Should return 404 when restaurant not found")
        void testGetRestaurant_WhenNotFound_ShouldReturn404() throws Exception {
            when(restaurantService.findRestaurantById(99))
                    .thenReturn(Optional.empty());

            mockMvc.perform(get("/api/booking/restaurants/99"))
                    .andExpect(status().isNotFound());

            verify(restaurantService).findRestaurantById(99);
        }
    }

    @Nested
    @DisplayName("getBookingDetails() Tests")
    class GetBookingDetailsTests {

        @Test
        @DisplayName("Should return booking details for authorized customer")
        void testGetBookingDetails_WithAuthorizedCustomer_ShouldReturnSuccess() throws Exception {
            User user = new User("testuser", "test@example.com", "password123", "Test User");
            user.setId(UUID.randomUUID());
            user.setRole(UserRole.CUSTOMER);

            Customer customer = new Customer(user);
            customer.setCustomerId(UUID.randomUUID());

            RestaurantProfile restaurant = new RestaurantProfile();
            restaurant.setRestaurantId(1);

            Booking booking = new Booking();
            booking.setBookingId(10);
            booking.setRestaurant(restaurant);
            booking.setCustomer(customer);
            booking.setBookingTime(LocalDateTime.now());
            booking.setNumberOfGuests(4);

            when(customerService.findByUsername("testuser"))
                    .thenReturn(Optional.of(customer));
            when(bookingService.findBookingById(10))
                    .thenReturn(Optional.of(booking));
            when(bookingService.calculateTotalAmount(any(Booking.class)))
                    .thenReturn(new BigDecimal("500000"));

            mockMvc.perform(get("/api/booking/10/details")
                    .principal(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bookingId").value(10))
                    .andExpect(jsonPath("$.restaurantId").value(1))
                    .andExpect(jsonPath("$.numberOfGuests").value(4));

            verify(bookingService).findBookingById(10);
            verify(bookingService).calculateTotalAmount(any(Booking.class));
        }

        @Test
        @DisplayName("Should return 404 when booking not found")
        void testGetBookingDetails_WhenNotFound_ShouldReturn404() throws Exception {
            User user = new User("testuser", "test@example.com", "password123", "Test User");
            user.setId(UUID.randomUUID());

            when(bookingService.findBookingById(99))
                    .thenReturn(Optional.empty());

            mockMvc.perform(get("/api/booking/99/details")
                    .principal(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())))
                    .andExpect(status().isNotFound());

            verify(bookingService).findBookingById(99);
        }
    }
}

