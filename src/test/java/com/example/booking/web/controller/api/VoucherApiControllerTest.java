package com.example.booking.web.controller.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.domain.Customer;
import com.example.booking.domain.DiscountType;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.Voucher;
import com.example.booking.domain.VoucherStatus;
import com.example.booking.service.CustomerService;
import com.example.booking.service.VoucherService;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VoucherApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("VoucherApiController Test Suite")
class VoucherApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VoucherService voucherService;

    @MockBean
    private CustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    private Voucher testVoucher;
    private Customer testCustomer;
    private User testUser;
    private RestaurantProfile testRestaurant;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User("customer", "customer@test.com", "password", "Customer Name");
        testUser.setId(UUID.randomUUID());

        // Setup test customer
        testCustomer = new Customer(testUser);
        testCustomer.setCustomerId(UUID.randomUUID());

        // Setup test restaurant
        testRestaurant = new RestaurantProfile();
        testRestaurant.setRestaurantId(1);
        testRestaurant.setRestaurantName("Test Restaurant");

        // Setup test voucher
        testVoucher = new Voucher();
        testVoucher.setVoucherId(1);
        testVoucher.setCode("TEST2024");
        testVoucher.setDescription("Test Voucher");
        testVoucher.setDiscountType(DiscountType.PERCENT);
        testVoucher.setDiscountValue(BigDecimal.valueOf(10.0));
        testVoucher.setStartDate(LocalDate.now().minusDays(1));
        testVoucher.setEndDate(LocalDate.now().plusDays(30));
        testVoucher.setGlobalUsageLimit(100);
        testVoucher.setPerCustomerLimit(1);
        testVoucher.setMinOrderAmount(BigDecimal.valueOf(100000.0));
        testVoucher.setMaxDiscountAmount(BigDecimal.valueOf(50000.0));
        testVoucher.setStatus(VoucherStatus.ACTIVE);
        testVoucher.setRestaurant(testRestaurant);
    }

    @Nested
    @DisplayName("validate() Tests")
    class ValidateTests {

        @Test
        @DisplayName("Should validate voucher successfully without authentication")
        void shouldValidateVoucherWithoutAuth() throws Exception {
            VoucherService.ValidationResult validationResult = new VoucherService.ValidationResult(
                    true, "VALID", BigDecimal.valueOf(10000.0), testVoucher);

            when(voucherService.validate(any(VoucherService.ValidationRequest.class)))
                    .thenReturn(validationResult);

            VoucherApiController.ValidateRequest request = new VoucherApiController.ValidateRequest(
                    "TEST2024", 1, LocalDateTime.now().plusDays(1), 2, BigDecimal.valueOf(100000.0));

            mockMvc.perform(post("/api/vouchers/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valid").value(true))
                    .andExpect(jsonPath("$.reason").value("VALID"))
                    .andExpect(jsonPath("$.calculatedDiscount").value(10000.0))
                    .andExpect(jsonPath("$.voucherId").value(1))
                    .andExpect(jsonPath("$.voucherCode").value("TEST2024"));

            verify(voucherService).validate(any(VoucherService.ValidationRequest.class));
        }

        @Test
        @WithMockUser(roles = "CUSTOMER", username = "customer")
        @DisplayName("Should validate voucher successfully with authentication")
        void shouldValidateVoucherWithAuth() throws Exception {
            when(customerService.findByUsername("customer")).thenReturn(Optional.of(testCustomer));

            VoucherService.ValidationResult validationResult = new VoucherService.ValidationResult(
                    true, "VALID", BigDecimal.valueOf(10000.0), testVoucher);

            when(voucherService.validate(any(VoucherService.ValidationRequest.class)))
                    .thenReturn(validationResult);

            VoucherApiController.ValidateRequest request = new VoucherApiController.ValidateRequest(
                    "TEST2024", 1, LocalDateTime.now().plusDays(1), 2, BigDecimal.valueOf(100000.0));

            mockMvc.perform(post("/api/vouchers/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valid").value(true));

            verify(customerService).findByUsername("customer");
            verify(voucherService).validate(any(VoucherService.ValidationRequest.class));
        }

        @Test
        @DisplayName("Should handle invalid voucher")
        void shouldHandleInvalidVoucher() throws Exception {
            VoucherService.ValidationResult validationResult = new VoucherService.ValidationResult(
                    false, "EXPIRED", null, null);

            when(voucherService.validate(any(VoucherService.ValidationRequest.class)))
                    .thenReturn(validationResult);

            VoucherApiController.ValidateRequest request = new VoucherApiController.ValidateRequest(
                    "EXPIRED2024", 1, LocalDateTime.now().plusDays(1), 2, BigDecimal.valueOf(100000.0));

            mockMvc.perform(post("/api/vouchers/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valid").value(false))
                    .andExpect(jsonPath("$.reason").value("EXPIRED"));
        }

        @Test
        @DisplayName("Should handle service exception")
        void shouldHandleServiceException() throws Exception {
            when(voucherService.validate(any(VoucherService.ValidationRequest.class)))
                    .thenThrow(new RuntimeException("Service error"));

            VoucherApiController.ValidateRequest request = new VoucherApiController.ValidateRequest(
                    "TEST2024", 1, LocalDateTime.now().plusDays(1), 2, BigDecimal.valueOf(100000.0));

            mockMvc.perform(post("/api/vouchers/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.valid").value(false))
                    .andExpect(jsonPath("$.reason").value("APPLICATION_ERROR"));
        }
    }

    @Nested
    @DisplayName("apply() Tests")
    class ApplyTests {

        @Test
        @DisplayName("Should reject unauthenticated requests")
        void shouldRejectUnauthenticatedRequests() throws Exception {
            VoucherApiController.ApplyRequest request = new VoucherApiController.ApplyRequest(
                    "TEST2024", 1, BigDecimal.valueOf(100000.0), 10);

            mockMvc.perform(post("/api/vouchers/apply")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Authentication required"));

            verify(voucherService, never()).applyToBooking(any());
        }

        @Test
        @WithMockUser(roles = "CUSTOMER", username = "customer")
        @DisplayName("Should apply voucher successfully")
        void shouldApplyVoucherSuccessfully() throws Exception {
            when(customerService.findByUsername("customer")).thenReturn(Optional.of(testCustomer));

            VoucherService.ApplyResult applyResult = new VoucherService.ApplyResult(
                    true, "APPLIED", BigDecimal.valueOf(10000.0), 1);

            when(voucherService.applyToBooking(any(VoucherService.ApplyRequest.class)))
                    .thenReturn(applyResult);

            VoucherApiController.ApplyRequest request = new VoucherApiController.ApplyRequest(
                    "TEST2024", 1, BigDecimal.valueOf(100000.0), 10);

            mockMvc.perform(post("/api/vouchers/apply")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.reason").value("APPLIED"));

            verify(customerService).findByUsername("customer");
            verify(voucherService).applyToBooking(any(VoucherService.ApplyRequest.class));
        }

        @Test
        @WithMockUser(roles = "CUSTOMER", username = "customer")
        @DisplayName("Should handle customer not found")
        void shouldHandleCustomerNotFound() throws Exception {
            when(customerService.findByUsername("customer")).thenReturn(Optional.empty());

            VoucherApiController.ApplyRequest request = new VoucherApiController.ApplyRequest(
                    "TEST2024", 1, BigDecimal.valueOf(100000.0), 10);

            mockMvc.perform(post("/api/vouchers/apply")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is5xxServerError());

            verify(voucherService, never()).applyToBooking(any());
        }
    }

    @Nested
    @DisplayName("getDemoVouchers() Tests")
    class GetDemoVouchersTests {

        @Test
        @DisplayName("Should return demo vouchers without authentication")
        void shouldReturnDemoVouchersWithoutAuth() throws Exception {
            when(voucherService.getAllVouchers()).thenReturn(Arrays.asList(testVoucher));
            when(voucherService.countRedemptionsByVoucherId(anyInt())).thenReturn(5L);

            mockMvc.perform(get("/api/vouchers/demo"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].voucherId").value(1))
                    .andExpect(jsonPath("$[0].code").value("TEST2024"));

            verify(voucherService).getAllVouchers();
        }

        @Test
        @DisplayName("Should filter by restaurantId")
        void shouldFilterByRestaurantId() throws Exception {
            Voucher globalVoucher = new Voucher();
            globalVoucher.setVoucherId(2);
            globalVoucher.setCode("GLOBAL2024");
            globalVoucher.setStatus(VoucherStatus.ACTIVE);
            globalVoucher.setRestaurant(null);

            when(voucherService.getAllVouchers()).thenReturn(Arrays.asList(testVoucher, globalVoucher));
            when(voucherService.countRedemptionsByVoucherId(anyInt())).thenReturn(0L);

            mockMvc.perform(get("/api/vouchers/demo")
                    .param("restaurantId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].voucherId").value(1));

            verify(voucherService).getAllVouchers();
        }

        @Test
        @WithMockUser(roles = "CUSTOMER", username = "customer")
        @DisplayName("Should check per-customer limit when authenticated")
        void shouldCheckPerCustomerLimitWhenAuthenticated() throws Exception {
            when(voucherService.getAllVouchers()).thenReturn(Arrays.asList(testVoucher));
            when(voucherService.countRedemptionsByVoucherId(anyInt())).thenReturn(0L);
            when(customerService.findByUsername("customer")).thenReturn(Optional.of(testCustomer));
            when(voucherService.countRedemptionsByVoucherIdAndCustomerId(anyInt(), any()))
                    .thenReturn(0L);

            mockMvc.perform(get("/api/vouchers/demo"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());

            verify(voucherService).countRedemptionsByVoucherIdAndCustomerId(anyInt(), any());
        }

        @Test
        @DisplayName("Should exclude expired vouchers")
        void shouldExcludeExpiredVouchers() throws Exception {
            Voucher expiredVoucher = new Voucher();
            expiredVoucher.setVoucherId(3);
            expiredVoucher.setCode("EXPIRED2024");
            expiredVoucher.setStatus(VoucherStatus.ACTIVE);
            expiredVoucher.setEndDate(LocalDate.now().minusDays(1)); // Expired

            when(voucherService.getAllVouchers()).thenReturn(Arrays.asList(testVoucher, expiredVoucher));
            when(voucherService.countRedemptionsByVoucherId(anyInt())).thenReturn(0L);

            mockMvc.perform(get("/api/vouchers/demo"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[?(@.code == 'EXPIRED2024')]").doesNotExist());
        }

        @Test
        @DisplayName("Should handle service exception")
        void shouldHandleServiceException() throws Exception {
            when(voucherService.getAllVouchers()).thenThrow(new RuntimeException("Service error"));

            mockMvc.perform(get("/api/vouchers/demo"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Error retrieving vouchers")));
        }
    }

    @Nested
    @DisplayName("getMyVouchers() Tests")
    class GetMyVouchersTests {

        @Test
        @WithMockUser(roles = "CUSTOMER", username = "customer")
        @DisplayName("Should return customer vouchers")
        void shouldReturnCustomerVouchers() throws Exception {
            when(customerService.findByUsername("customer")).thenReturn(Optional.of(testCustomer));
            when(voucherService.getVouchersByCustomer(testCustomer.getCustomerId()))
                    .thenReturn(Arrays.asList(testVoucher));

            mockMvc.perform(get("/api/vouchers/my"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].voucherId").value(1))
                    .andExpect(jsonPath("$[0].code").value("TEST2024"));

            verify(voucherService).getVouchersByCustomer(testCustomer.getCustomerId());
        }

        @Test
        @WithMockUser(roles = "CUSTOMER", username = "customer")
        @DisplayName("Should handle customer not found")
        void shouldHandleCustomerNotFound() throws Exception {
            when(customerService.findByUsername("customer")).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/vouchers/my"))
                    .andExpect(status().is5xxServerError());

            verify(voucherService, never()).getVouchersByCustomer(any());
        }

        @Test
        @WithMockUser(roles = "CUSTOMER", username = "customer")
        @DisplayName("Should handle service exception")
        void shouldHandleServiceException() throws Exception {
            when(customerService.findByUsername("customer")).thenReturn(Optional.of(testCustomer));
            when(voucherService.getVouchersByCustomer(any()))
                    .thenThrow(new RuntimeException("Service error"));

            mockMvc.perform(get("/api/vouchers/my"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("assignVoucherToMe() Tests")
    class AssignVoucherToMeTests {

        @Test
        @WithMockUser(roles = "CUSTOMER", username = "customer")
        @DisplayName("Should assign voucher successfully")
        void shouldAssignVoucherSuccessfully() throws Exception {
            when(customerService.findByUsername("customer")).thenReturn(Optional.of(testCustomer));
            doNothing().when(voucherService).assignVoucherToCustomers(anyInt(), anyList());

            mockMvc.perform(post("/api/vouchers/assign/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Voucher assigned successfully"));

            verify(voucherService).assignVoucherToCustomers(eq(1), anyList());
        }

        @Test
        @WithMockUser(roles = "CUSTOMER", username = "customer")
        @DisplayName("Should handle customer not found")
        void shouldHandleCustomerNotFound() throws Exception {
            when(customerService.findByUsername("customer")).thenReturn(Optional.empty());

            mockMvc.perform(post("/api/vouchers/assign/1"))
                    .andExpect(status().is5xxServerError());

            verify(voucherService, never()).assignVoucherToCustomers(anyInt(), anyList());
        }

        @Test
        @WithMockUser(roles = "CUSTOMER", username = "customer")
        @DisplayName("Should handle service exception")
        void shouldHandleServiceException() throws Exception {
            when(customerService.findByUsername("customer")).thenReturn(Optional.of(testCustomer));
            doThrow(new RuntimeException("Assignment failed")).when(voucherService)
                    .assignVoucherToCustomers(anyInt(), anyList());

            mockMvc.perform(post("/api/vouchers/assign/1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Error assigning voucher")));
        }
    }

    @Nested
    @DisplayName("getVoucherStats() Tests")
    class GetVoucherStatsTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return voucher stats for admin")
        void shouldReturnVoucherStatsForAdmin() throws Exception {
            VoucherService.VoucherUsageStats stats = new VoucherService.VoucherUsageStats(
                    1, "TEST2024", "Test Voucher", 10L, 5L, BigDecimal.valueOf(100000.0), LocalDateTime.now());

            when(voucherService.getVoucherUsageStats(1)).thenReturn(Arrays.asList(stats));

            mockMvc.perform(get("/api/vouchers/stats/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].voucherId").value(1))
                    .andExpect(jsonPath("$[0].code").value("TEST2024"));

            verify(voucherService).getVoucherUsageStats(1);
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should return voucher stats for restaurant owner")
        void shouldReturnVoucherStatsForRestaurantOwner() throws Exception {
            VoucherService.VoucherUsageStats stats = new VoucherService.VoucherUsageStats(
                    1, "TEST2024", "Test Voucher", 10L, 5L, BigDecimal.valueOf(100000.0), LocalDateTime.now());

            when(voucherService.getVoucherUsageStats(1)).thenReturn(Arrays.asList(stats));

            mockMvc.perform(get("/api/vouchers/stats/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());

            verify(voucherService).getVoucherUsageStats(1);
        }

        @Test
        @WithMockUser(roles = "CUSTOMER")
        @DisplayName("Should reject customer access")
        void shouldRejectCustomerAccess() throws Exception {
            mockMvc.perform(get("/api/vouchers/stats/1"))
                    .andExpect(status().isForbidden());

            verify(voucherService, never()).getVoucherUsageStats(anyInt());
        }
    }
}

