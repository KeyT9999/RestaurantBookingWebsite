package com.example.booking.web.controller;

import com.example.booking.common.enums.RefundStatus;
import com.example.booking.domain.RefundRequest;
import com.example.booking.service.RestaurantApprovalService;
import com.example.booking.service.RestaurantBalanceService;
import com.example.booking.service.RefundService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardControllerTest {

    @InjectMocks
    private AdminDashboardController controller;

    @Mock
    private RestaurantBalanceService balanceService;

    @Mock
    private RestaurantApprovalService restaurantApprovalService;

    @Mock
    private RefundService refundService;

    private Model model;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
    }

    @Test
    void addCommonAttributes_shouldAddPendingRestaurantsForAdmin() {
        Authentication authentication = new TestingAuthenticationToken("admin", "pass", "ROLE_ADMIN");
        when(restaurantApprovalService.getPendingRestaurantCount()).thenReturn(5L);

        controller.addCommonAttributes(model, authentication);

        assertEquals(5L, model.getAttribute("pendingRestaurants"));
        verify(restaurantApprovalService).getPendingRestaurantCount();
    }

    @Test
    void addCommonAttributes_shouldIgnoreNonAdminUsers() {
        Authentication authentication = new TestingAuthenticationToken("user", "pass", "ROLE_USER");

        controller.addCommonAttributes(model, authentication);

        assertTrue(!model.containsAttribute("pendingRestaurants"));
        verify(restaurantApprovalService, never()).getPendingRestaurantCount();
    }

    @Test
    void adminDashboard_shouldPopulateModelAndReturnView() {
        when(balanceService.getCommissionToday()).thenReturn(BigDecimal.TEN);
        when(balanceService.getCommissionRate()).thenReturn(new BigDecimal("15"));
        when(balanceService.getWeeklyCommission()).thenReturn(new BigDecimal("70"));
        when(balanceService.getMonthlyCommission()).thenReturn(new BigDecimal("300"));
        when(balanceService.getTotalCommission()).thenReturn(new BigDecimal("1000"));
        when(balanceService.getAverageCommissionPerBooking()).thenReturn(new BigDecimal("20"));
        when(balanceService.getCompletedBookingsToday()).thenReturn(8L);

        String view = controller.adminDashboard(model);

        assertEquals("admin/dashboard", view);
        assertEquals(BigDecimal.TEN, model.getAttribute("commissionToday"));
        assertEquals(new BigDecimal("15"), model.getAttribute("commissionRate"));
        assertEquals(8L, model.getAttribute("todayBookings"));
    }

    @Test
    void adminDashboard_shouldHandleServiceFailure() {
        doThrow(new IllegalStateException("boom"))
                .when(balanceService).getCommissionToday();

        String view = controller.adminDashboard(model);

        assertEquals("admin/dashboard", view);
        assertTrue(model.containsAttribute("error"));
    }

    @Test
    void refundRequests_shouldLoadDataAndReturnView() {
        RefundRequest pending = new RefundRequest();
        pending.setAmount(new BigDecimal("50"));
        pending.setStatus(RefundStatus.PENDING);

        RefundRequest completed = new RefundRequest();
        completed.setAmount(new BigDecimal("80"));
        completed.setStatus(RefundStatus.COMPLETED);

        RefundRequest rejected = new RefundRequest();
        rejected.setAmount(new BigDecimal("20"));
        rejected.setStatus(RefundStatus.REJECTED);

        when(refundService.getPendingRefunds()).thenReturn(List.of(pending));
        when(refundService.getRefundsByStatus(RefundStatus.COMPLETED)).thenReturn(List.of(completed));
        when(refundService.getRefundsByStatus(RefundStatus.REJECTED)).thenReturn(List.of(rejected));

        String view = controller.refundRequests(model);

        assertEquals("admin/refund-requests", view);
        assertEquals(1L, model.getAttribute("pendingCount"));
        assertEquals(new BigDecimal("50"), model.getAttribute("pendingTotal"));
        assertNotNull(model.getAttribute("bankNameMap"));
    }

    @Test
    void getStatistics_shouldReturnAggregatedMetrics() {
        when(balanceService.getCommissionToday()).thenReturn(new BigDecimal("15"));
        when(balanceService.getWeeklyCommission()).thenReturn(new BigDecimal("77"));
        when(balanceService.getMonthlyCommission()).thenReturn(new BigDecimal("310"));
        when(balanceService.getTotalCommission()).thenReturn(new BigDecimal("1200"));
        when(balanceService.getCompletedBookingsToday()).thenReturn(12L);
        when(restaurantApprovalService.getPendingRestaurantCount()).thenReturn(4L);

        RefundRequest pendingRefund = new RefundRequest();
        pendingRefund.setAmount(new BigDecimal("25"));
        when(refundService.getPendingRefunds()).thenReturn(List.of(pendingRefund));

        ResponseEntity<Map<String, Object>> response = controller.getStatistics();

        assertEquals(200, response.getStatusCodeValue());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(new BigDecimal("15"), body.get("commissionToday"));
        assertEquals(1L, body.get("pendingRefunds"));
        assertEquals(new BigDecimal("25"), body.get("totalPendingRefundAmount"));
    }

    @Test
    void getStatistics_shouldReturnErrorResponseOnFailure() {
        doThrow(new RuntimeException("Service down"))
                .when(balanceService).getCommissionToday();

        ResponseEntity<Map<String, Object>> response = controller.getStatistics();

        assertEquals(500, response.getStatusCodeValue());
        assertTrue(response.getBody().containsKey("error"));
    }
}
 