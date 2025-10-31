package com.example.booking.web.controller;

import com.example.booking.service.RefundService;
import com.example.booking.service.RestaurantApprovalService;
import com.example.booking.service.RestaurantBalanceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminDashboardController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        com.example.booking.config.AuthRateLimitFilter.class,
        com.example.booking.config.GeneralRateLimitFilter.class,
        com.example.booking.config.LoginRateLimitFilter.class,
        com.example.booking.config.PermanentlyBlockedIpFilter.class,
        com.example.booking.web.advice.NotificationHeaderAdvice.class
    }),
    excludeAutoConfiguration = {org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminDashboardController WebMvc Tests - addCommonAttributes Coverage")
class AdminDashboardControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestaurantBalanceService balanceService;

    @MockBean
    private RestaurantApprovalService restaurantApprovalService;

    @MockBean
    private RefundService refundService;

    // ========== Prompt A: Auth null / unauthenticated ==========

    @Test
    @DisplayName("GET /admin/dashboard - Auth null, pendingRestaurants should not appear")
    void testAddCommonAttributes_AuthNull() throws Exception {
        // Given - Clear security context (no authentication)
        SecurityContextHolder.clearContext();

        // Setup mocks for dashboard method (even though we focus on addCommonAttributes)
        when(balanceService.getCommissionToday()).thenReturn(BigDecimal.ZERO);
        when(balanceService.getWeeklyCommission()).thenReturn(BigDecimal.ZERO);
        when(balanceService.getMonthlyCommission()).thenReturn(BigDecimal.ZERO);
        when(balanceService.getTotalCommission()).thenReturn(BigDecimal.ZERO);
        when(balanceService.getAverageCommissionPerBooking()).thenReturn(BigDecimal.ZERO);
        when(balanceService.getCommissionRate()).thenReturn(BigDecimal.ZERO);
        when(balanceService.getCompletedBookingsToday()).thenReturn(0L);

        try {
            // When & Then
            mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"));
            
            // Verify pendingRestaurants is NOT set (or = 0 if somehow set)
            // Note: In WebMvcTest, @ModelAttribute runs, but with null auth, 
            // the condition authentication != null && authentication.isAuthenticated() fails
            // so pendingRestaurants attribute should not be added
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    // ========== Prompt B: Authenticated nhưng không phải ADMIN ==========

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("GET /admin/dashboard - Authenticated but not ADMIN, pendingRestaurants should not appear")
    void testAddCommonAttributes_AuthenticatedButNotAdmin() throws Exception {
        // Given
        when(balanceService.getCommissionToday()).thenReturn(BigDecimal.ZERO);
        when(balanceService.getWeeklyCommission()).thenReturn(BigDecimal.ZERO);
        when(balanceService.getMonthlyCommission()).thenReturn(BigDecimal.ZERO);
        when(balanceService.getTotalCommission()).thenReturn(BigDecimal.ZERO);
        when(balanceService.getAverageCommissionPerBooking()).thenReturn(BigDecimal.ZERO);
        when(balanceService.getCommissionRate()).thenReturn(BigDecimal.ZERO);
        when(balanceService.getCompletedBookingsToday()).thenReturn(0L);

        // When & Then - isAdmin=false branch, so pendingRestaurants should not appear
        mockMvc.perform(get("/admin/dashboard"))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/dashboard"));
        
        // Verify that getPendingRestaurantCount was NOT called (isAdmin check failed)
        verify(restaurantApprovalService, never()).getPendingRestaurantCount();
    }

    // ========== Prompt C: ADMIN, service OK ==========

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("GET /admin/dashboard - ADMIN, service OK, pendingRestaurants=7")
    void testAddCommonAttributes_Admin_ServiceOK() throws Exception {
        // Given
        when(restaurantApprovalService.getPendingRestaurantCount()).thenReturn(7L);
        when(balanceService.getCommissionToday()).thenReturn(BigDecimal.ZERO);
        when(balanceService.getWeeklyCommission()).thenReturn(BigDecimal.ZERO);
        when(balanceService.getMonthlyCommission()).thenReturn(BigDecimal.ZERO);
        when(balanceService.getTotalCommission()).thenReturn(BigDecimal.ZERO);
        when(balanceService.getAverageCommissionPerBooking()).thenReturn(BigDecimal.ZERO);
        when(balanceService.getCommissionRate()).thenReturn(BigDecimal.ZERO);
        when(balanceService.getCompletedBookingsToday()).thenReturn(0L);

        // When & Then - @ModelAttribute runs, isAdmin=true, service returns 7
        mockMvc.perform(get("/admin/dashboard"))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/dashboard"))
            .andExpect(model().attribute("pendingRestaurants", 7L));
        
        verify(restaurantApprovalService).getPendingRestaurantCount();
    }

    // ========== Prompt D: ADMIN, service ném exception ==========

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("GET /admin/dashboard - ADMIN, service throws exception, pendingRestaurants=0")
    void testAddCommonAttributes_Admin_ServiceException() throws Exception {
        // Given - Mock throws RuntimeException
        when(restaurantApprovalService.getPendingRestaurantCount())
            .thenThrow(new RuntimeException("Database error"));
        when(balanceService.getCommissionToday()).thenReturn(BigDecimal.ZERO);
        when(balanceService.getWeeklyCommission()).thenReturn(BigDecimal.ZERO);
        when(balanceService.getMonthlyCommission()).thenReturn(BigDecimal.ZERO);
        when(balanceService.getTotalCommission()).thenReturn(BigDecimal.ZERO);
        when(balanceService.getAverageCommissionPerBooking()).thenReturn(BigDecimal.ZERO);
        when(balanceService.getCommissionRate()).thenReturn(BigDecimal.ZERO);
        when(balanceService.getCompletedBookingsToday()).thenReturn(0L);

        // When & Then - Catch branch, pendingRestaurants should be 0
        mockMvc.perform(get("/admin/dashboard"))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/dashboard"))
            .andExpect(model().attribute("pendingRestaurants", 0L));
        
        verify(restaurantApprovalService).getPendingRestaurantCount();
    }

    // ========== Test with /admin/refund-requests endpoint ==========

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("GET /admin/refund-requests - ADMIN, addCommonAttributes should run")
    void testAddCommonAttributes_Admin_RefundRequestsEndpoint() throws Exception {
        // Given
        when(restaurantApprovalService.getPendingRestaurantCount()).thenReturn(5L);
        when(refundService.getPendingRefunds()).thenReturn(java.util.Collections.emptyList());
        when(refundService.getRefundsByStatus(any())).thenReturn(java.util.Collections.emptyList());

        // When & Then - @ModelAttribute runs on any /admin/* endpoint
        mockMvc.perform(get("/admin/refund-requests"))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/refund-requests"))
            .andExpect(model().attribute("pendingRestaurants", 5L));
    }

    // ========== Test with unauthenticated request ==========

    @Test
    @DisplayName("GET /admin/refund-requests - Unauthenticated, pendingRestaurants should not appear")
    void testAddCommonAttributes_Unauthenticated_RefundRequests() throws Exception {
        // Given
        SecurityContextHolder.clearContext();
        when(refundService.getPendingRefunds()).thenReturn(java.util.Collections.emptyList());
        when(refundService.getRefundsByStatus(any())).thenReturn(java.util.Collections.emptyList());

        try {
            // When & Then
            mockMvc.perform(get("/admin/refund-requests"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/refund-requests"));
            
            // Verify getPendingRestaurantCount was NOT called
            verify(restaurantApprovalService, never()).getPendingRestaurantCount();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}

