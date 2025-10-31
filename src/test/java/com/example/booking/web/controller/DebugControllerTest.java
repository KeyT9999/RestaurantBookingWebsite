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
}

