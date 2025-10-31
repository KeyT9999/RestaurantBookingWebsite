package com.example.booking.web.controller.admin;

import com.example.booking.service.VoucherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AdminVoucherAnalyticsControllerTest {

    @Mock
    private VoucherService voucherService;

    @InjectMocks
    private AdminVoucherAnalyticsController controller;

    private Model model;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
    }

    @Test
    void analyticsShouldPopulateDefaultSummaryWhenNoParamsProvided() {
        String view = controller.analytics(null, null, null, null, model);

        assertEquals("admin/vouchers/analytics", view);
        assertEquals(0, model.getAttribute("totalVouchers"));
        assertEquals(0, model.getAttribute("activeVouchers"));
        assertEquals(0, model.getAttribute("totalRedemptions"));
        assertEquals("₫0", model.getAttribute("totalDiscountGiven"));
        assertEquals(java.util.List.of(), model.getAttribute("topVouchers"));
        assertEquals(java.util.List.of(), model.getAttribute("restaurantStats"));

        LocalDate expectedStart = LocalDate.now().minusMonths(1);
        LocalDate expectedEnd = LocalDate.now();
        assertEquals(expectedStart, model.getAttribute("startDate"));
        assertEquals(expectedEnd, model.getAttribute("endDate"));
        assertEquals(null, model.getAttribute("voucherId"));
        assertEquals(null, model.getAttribute("restaurantId"));

        verifyNoInteractions(voucherService);
    }

    @Test
    void analyticsShouldRespectProvidedQueryParameters() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 31);

        String view = controller.analytics(start.toString(), end.toString(), 5, 10, model);

        assertEquals("admin/vouchers/analytics", view);
        assertEquals(start, model.getAttribute("startDate"));
        assertEquals(end, model.getAttribute("endDate"));
        assertEquals(5, model.getAttribute("voucherId"));
        assertEquals(10, model.getAttribute("restaurantId"));
    }
}
package com.example.booking.web.controller.admin;

import com.example.booking.service.VoucherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive tests for AdminVoucherAnalyticsController
 * 
 * Coverage Target: ≥80% Branch Coverage
 * 
 * Branches to cover:
 * - analytics(): startDate != null vs startDate == null (default to now().minusMonths(1))
 * - analytics(): endDate != null vs endDate == null (default to now())
 * - Edge cases: null parameters, empty strings, invalid dates
 * 
 * @author Senior SDET
 */
@WebMvcTest(AdminVoucherAnalyticsController.class)
@DisplayName("AdminVoucherAnalyticsController Tests")
class AdminVoucherAnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VoucherService voucherService;

    @Nested
    @DisplayName("analytics() - GET /admin/vouchers/analytics")
    class AnalyticsTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return analytics view with default dates - Happy path (null dates)")
        // Branch: startDate == null (uses default), endDate == null (uses default)
        void analytics_WithNullDates_ReturnsViewWithDefaultDates() throws Exception {
            // When/Then
            mockMvc.perform(get("/admin/vouchers/analytics"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/vouchers/analytics"))
                    .andExpect(model().attributeExists("totalVouchers"))
                    .andExpect(model().attributeExists("activeVouchers"))
                    .andExpect(model().attributeExists("totalRedemptions"))
                    .andExpect(model().attributeExists("totalDiscountGiven"))
                    .andExpect(model().attributeExists("topVouchers"))
                    .andExpect(model().attributeExists("restaurantStats"))
                    .andExpect(model().attributeExists("startDate"))
                    .andExpect(model().attributeExists("endDate"))
                    .andExpect(model().attribute("startDate", org.hamcrest.Matchers.any(LocalDate.class)))
                    .andExpect(model().attribute("endDate", org.hamcrest.Matchers.any(LocalDate.class)))
                    .andExpect(model().attribute("totalVouchers", 0))
                    .andExpect(model().attribute("activeVouchers", 0))
                    .andExpect(model().attribute("totalRedemptions", 0))
                    .andExpect(model().attribute("totalDiscountGiven", "₫0"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return analytics view with provided dates - Happy path (valid dates)")
        // Branch: startDate != null, endDate != null
        void analytics_WithValidDates_ReturnsViewWithProvidedDates() throws Exception {
            // Given
            String startDate = "2025-01-01";
            String endDate = "2025-01-31";

            // When/Then
            mockMvc.perform(get("/admin/vouchers/analytics")
                            .param("startDate", startDate)
                            .param("endDate", endDate))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/vouchers/analytics"))
                    .andExpect(model().attribute("startDate", LocalDate.parse(startDate)))
                    .andExpect(model().attribute("endDate", LocalDate.parse(endDate)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return analytics view with provided voucherId and restaurantId")
        // Edge: additional optional parameters
        void analytics_WithVoucherIdAndRestaurantId_ReturnsViewWithParameters() throws Exception {
            // Given
            Integer voucherId = 123;
            Integer restaurantId = 456;

            // When/Then
            mockMvc.perform(get("/admin/vouchers/analytics")
                            .param("voucherId", String.valueOf(voucherId))
                            .param("restaurantId", String.valueOf(restaurantId)))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/vouchers/analytics"))
                    .andExpect(model().attribute("voucherId", voucherId))
                    .andExpect(model().attribute("restaurantId", restaurantId));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle only startDate provided - Edge case")
        // Branch: startDate != null, endDate == null
        void analytics_WithOnlyStartDate_ReturnsViewWithDefaultEndDate() throws Exception {
            // Given
            String startDate = "2025-01-01";

            // When/Then
            mockMvc.perform(get("/admin/vouchers/analytics")
                            .param("startDate", startDate))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/vouchers/analytics"))
                    .andExpect(model().attribute("startDate", LocalDate.parse(startDate)))
                    .andExpect(model().attribute("endDate", org.hamcrest.Matchers.any(LocalDate.class)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle only endDate provided - Edge case")
        // Branch: startDate == null, endDate != null
        void analytics_WithOnlyEndDate_ReturnsViewWithDefaultStartDate() throws Exception {
            // Given
            String endDate = "2025-01-31";

            // When/Then
            mockMvc.perform(get("/admin/vouchers/analytics")
                            .param("endDate", endDate))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/vouchers/analytics"))
                    .andExpect(model().attribute("startDate", org.hamcrest.Matchers.any(LocalDate.class)))
                    .andExpect(model().attribute("endDate", LocalDate.parse(endDate)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle invalid date format - Error case")
        // Error: invalid date format causes DateTimeParseException
        void analytics_WithInvalidDate_ReturnsError() throws Exception {
            // Given
            String invalidDate = "invalid-date";

            // When/Then
            mockMvc.perform(get("/admin/vouchers/analytics")
                            .param("startDate", invalidDate))
                    .andExpect(status().isBadRequest());
        }
    }
}

