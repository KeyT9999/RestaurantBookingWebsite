package com.example.booking.web.controller;

import com.example.booking.domain.Voucher;
import com.example.booking.service.VoucherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DebugController.class)
@DisplayName("DebugController Test")
class DebugControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VoucherService voucherService;

    private Voucher voucher;

    @BeforeEach
    void setUp() {
        voucher = new Voucher();
        voucher.setVoucherId(1);
        voucher.setCode("TEST001");
    }

    @Test
    @DisplayName("GET /debug/vouchers - Should return voucher debug info")
    void testDebugVouchers_ShouldReturnInfo() throws Exception {
        List<Voucher> restaurantVouchers = Arrays.asList(voucher);
        List<Voucher> allVouchers = Arrays.asList(voucher);

        when(voucherService.getVouchersByRestaurant(16))
            .thenReturn(restaurantVouchers);
        when(voucherService.getAllVouchers())
            .thenReturn(allVouchers);

        mockMvc.perform(get("/debug/vouchers"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Found 1 vouchers")));

        verify(voucherService).getVouchersByRestaurant(16);
        verify(voucherService).getAllVouchers();
    }

    @Test
    @DisplayName("GET /debug/vouchers - Should handle empty vouchers")
    void testDebugVouchers_WithEmptyList() throws Exception {
        when(voucherService.getVouchersByRestaurant(16))
            .thenReturn(Collections.emptyList());
        when(voucherService.getAllVouchers())
            .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/debug/vouchers"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Found 0 vouchers")));
    }

    @Test
    @DisplayName("GET /debug/vouchers - Should handle exception")
    void testDebugVouchers_ShouldHandleException() throws Exception {
        when(voucherService.getVouchersByRestaurant(16))
            .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/debug/vouchers"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Error")));
    }

    @Test
    @DisplayName("GET /debug/vouchers - Should handle vouchers with null restaurant")
    void testDebugVouchers_WithNullRestaurant() throws Exception {
        Voucher voucherWithNullRestaurant = new Voucher();
        voucherWithNullRestaurant.setVoucherId(2);
        voucherWithNullRestaurant.setCode("TEST002");
        voucherWithNullRestaurant.setRestaurant(null);

        List<Voucher> restaurantVouchers = Arrays.asList(voucherWithNullRestaurant);
        List<Voucher> allVouchers = Arrays.asList(voucherWithNullRestaurant);

        when(voucherService.getVouchersByRestaurant(16))
            .thenReturn(restaurantVouchers);
        when(voucherService.getAllVouchers())
            .thenReturn(allVouchers);

        mockMvc.perform(get("/debug/vouchers"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Found 1 vouchers")));

        verify(voucherService).getVouchersByRestaurant(16);
        verify(voucherService).getAllVouchers();
    }

    @Test
    @DisplayName("GET /debug/vouchers - Should handle exception from getAllVouchers")
    void testDebugVouchers_ShouldHandleExceptionFromGetAll() throws Exception {
        List<Voucher> restaurantVouchers = Arrays.asList(voucher);
        when(voucherService.getVouchersByRestaurant(16))
            .thenReturn(restaurantVouchers);
        when(voucherService.getAllVouchers())
            .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/debug/vouchers"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Error")));
    }

    @Test
    @DisplayName("GET /debug/vouchers - Should handle vouchers with restaurant")
    void testDebugVouchers_WithRestaurant() throws Exception {
        com.example.booking.domain.RestaurantProfile restaurant = new com.example.booking.domain.RestaurantProfile();
        restaurant.setRestaurantId(16);
        
        Voucher voucherWithRestaurant = new Voucher();
        voucherWithRestaurant.setVoucherId(3);
        voucherWithRestaurant.setCode("TEST003");
        voucherWithRestaurant.setRestaurant(restaurant);

        List<Voucher> restaurantVouchers = Arrays.asList(voucherWithRestaurant);
        List<Voucher> allVouchers = Arrays.asList(voucherWithRestaurant);

        when(voucherService.getVouchersByRestaurant(16))
            .thenReturn(restaurantVouchers);
        when(voucherService.getAllVouchers())
            .thenReturn(allVouchers);

        mockMvc.perform(get("/debug/vouchers"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Found 1 vouchers")));
    }

    @Test
    @DisplayName("GET /debug/vouchers - Should handle multiple vouchers")
    void testDebugVouchers_WithMultipleVouchers() throws Exception {
        Voucher voucher1 = new Voucher();
        voucher1.setVoucherId(1);
        voucher1.setCode("VOUCHER1");
        
        Voucher voucher2 = new Voucher();
        voucher2.setVoucherId(2);
        voucher2.setCode("VOUCHER2");

        List<Voucher> restaurantVouchers = Arrays.asList(voucher1, voucher2);
        List<Voucher> allVouchers = Arrays.asList(voucher1, voucher2);

        when(voucherService.getVouchersByRestaurant(16))
            .thenReturn(restaurantVouchers);
        when(voucherService.getAllVouchers())
            .thenReturn(allVouchers);

        mockMvc.perform(get("/debug/vouchers"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Found 2 vouchers")));
    }
}

