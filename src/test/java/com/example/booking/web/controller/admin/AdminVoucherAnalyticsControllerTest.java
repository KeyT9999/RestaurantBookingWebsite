package com.example.booking.web.controller.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.service.VoucherService;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminVoucherAnalyticsController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminVoucherAnalyticsController Test Suite")
class AdminVoucherAnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VoucherService voucherService;

    @Nested
    @DisplayName("analytics() Tests")
    class AnalyticsTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should display analytics page successfully")
        void shouldDisplayAnalyticsPage() throws Exception {
            mockMvc.perform(get("/admin/vouchers/analytics"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/vouchers/analytics"))
                    .andExpect(model().attributeExists("totalVouchers"))
                    .andExpect(model().attributeExists("activeVouchers"))
                    .andExpect(model().attributeExists("totalRedemptions"))
                    .andExpect(model().attributeExists("totalDiscountGiven"))
                    .andExpect(model().attributeExists("topVouchers"))
                    .andExpect(model().attributeExists("restaurantStats"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should use default date range when no dates provided")
        void shouldUseDefaultDateRange() throws Exception {
            mockMvc.perform(get("/admin/vouchers/analytics"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("startDate"))
                    .andExpect(model().attributeExists("endDate"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should parse custom start date")
        void shouldParseCustomStartDate() throws Exception {
            LocalDate testDate = LocalDate.now().minusDays(7);
            
            mockMvc.perform(get("/admin/vouchers/analytics")
                    .param("startDate", testDate.toString()))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("startDate", testDate));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should parse custom end date")
        void shouldParseCustomEndDate() throws Exception {
            LocalDate testDate = LocalDate.now();
            
            mockMvc.perform(get("/admin/vouchers/analytics")
                    .param("endDate", testDate.toString()))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("endDate", testDate));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle voucherId filter")
        void shouldHandleVoucherIdFilter() throws Exception {
            mockMvc.perform(get("/admin/vouchers/analytics")
                    .param("voucherId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("voucherId", 1));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle restaurantId filter")
        void shouldHandleRestaurantIdFilter() throws Exception {
            mockMvc.perform(get("/admin/vouchers/analytics")
                    .param("restaurantId", "5"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("restaurantId", 5));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle all filter parameters together")
        void shouldHandleAllFilterParameters() throws Exception {
            LocalDate startDate = LocalDate.now().minusDays(30);
            LocalDate endDate = LocalDate.now();
            
            mockMvc.perform(get("/admin/vouchers/analytics")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString())
                    .param("voucherId", "1")
                    .param("restaurantId", "5"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("startDate", startDate))
                    .andExpect(model().attribute("endDate", endDate))
                    .andExpect(model().attribute("voucherId", 1))
                    .andExpect(model().attribute("restaurantId", 5));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should reject non-admin users")
        void shouldRejectNonAdminUsers() throws Exception {
            mockMvc.perform(get("/admin/vouchers/analytics"))
                    .andExpect(status().isForbidden());
        }
    }
}

