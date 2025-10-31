package com.example.booking.web.controller.restaurantowner;

import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.Voucher;
import com.example.booking.domain.VoucherStatus;
import com.example.booking.dto.admin.VoucherCreateForm;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.SimpleUserService;
import com.example.booking.service.VoucherService;
import com.example.booking.service.EndpointRateLimitingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RestaurantVoucherController.class)
@DisplayName("RestaurantVoucherController Tests")
class RestaurantVoucherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VoucherService voucherService;

    @MockBean
    private RestaurantOwnerService restaurantOwnerService;

    @MockBean
    private SimpleUserService userService;

    @MockBean
    private EndpointRateLimitingService endpointRateLimitingService;

    private Voucher mockVoucher;
    private List<Voucher> mockVoucherList;
    private List<RestaurantProfile> mockRestaurants;
    private User mockUser;

    @BeforeEach
    void setUp() {
        // Setup mock user
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail("owner@test.com");
        mockUser.setFullName("Test Owner");

        // Setup mock restaurant
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");

        mockRestaurants = new ArrayList<>();
        mockRestaurants.add(restaurant);

        // Setup mock voucher
        mockVoucher = new Voucher();
        mockVoucher.setVoucherId(1);
        mockVoucher.setCode("REST_VOUCHER");
        mockVoucher.setDescription("Restaurant Voucher");
        mockVoucher.setStatus(VoucherStatus.ACTIVE);
        mockVoucher.setRestaurant(restaurant);
        mockVoucher.setGlobalUsageLimit(100);
        // Note: getUsedCount() is calculated from redemptions, not a setter

        mockVoucherList = new ArrayList<>();
        mockVoucherList.add(mockVoucher);

        // Setup default mocks
        when(restaurantOwnerService.getRestaurantsByCurrentUser(any(Authentication.class)))
                .thenReturn(mockRestaurants);
        when(voucherService.getVouchersByRestaurant(1)).thenReturn(mockVoucherList);
        when(voucherService.getVoucherById(1)).thenReturn(mockVoucher);
    }

    // Test TC RVC-001: List vouchers for restaurant owner
    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("TC RVC-001: Should list vouchers for restaurant owner")
    void shouldListVouchersForRestaurantOwner() throws Exception {
        mockMvc.perform(get("/restaurant-owner/vouchers"))
                .andExpect(status().isOk())
                .andExpect(view().name("restaurant-owner/vouchers/list"))
                .andExpect(model().attributeExists("vouchers"))
                .andExpect(model().attributeExists("restaurants"))
                .andExpect(model().attributeExists("selectedRestaurantId"));

        verify(voucherService, times(1)).getVouchersByRestaurant(1);
    }

    // Test TC RVC-002: Show create form
    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("TC RVC-002: Should show create form")
    void shouldShowCreateForm() throws Exception {
        mockMvc.perform(get("/restaurant-owner/vouchers/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("restaurant-owner/vouchers/form"))
                .andExpect(model().attributeExists("voucherForm"))
                .andExpect(model().attributeExists("statuses"))
                .andExpect(model().attributeExists("restaurants"));

        verify(restaurantOwnerService, atLeastOnce()).getRestaurantsByCurrentUser(any());
    }

    // Test TC RVC-003: Create voucher successfully
    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("TC RVC-003: Should create voucher successfully")
    void shouldCreateVoucherSuccessfully() throws Exception {
        when(voucherService.createRestaurantVoucher(anyInt(), any(VoucherService.VoucherCreateDto.class)))
                .thenReturn(mockVoucher);

        mockMvc.perform(post("/restaurant-owner/vouchers/new")
                .param("code", "NEW_VOUCHER")
                .param("description", "New Restaurant Voucher")
                .param("discountType", "PERCENTAGE")
                .param("discountValue", "15")
                .param("startDate", LocalDate.now().toString())
                .param("endDate", LocalDate.now().plusDays(30).toString())
                .param("globalUsageLimit", "50")
                .param("perCustomerLimit", "1")
                .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/restaurant-owner/vouchers?restaurantId=1"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(voucherService, times(1)).createRestaurantVoucher(anyInt(), any());
    }

    // Test TC RVC-004: View voucher details
    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("TC RVC-004: Should view voucher details")
    void shouldViewVoucherDetails() throws Exception {
        mockMvc.perform(get("/restaurant-owner/vouchers/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("restaurant-owner/vouchers/detail"))
                .andExpect(model().attributeExists("voucher"))
                .andExpect(model().attributeExists("usedCount"))
                .andExpect(model().attributeExists("totalLimit"))
                .andExpect(model().attributeExists("usagePercentage"));

        verify(voucherService, times(1)).getVoucherById(1);
    }

    // Test TC RVC-005: Pause voucher
    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("TC RVC-005: Should pause voucher successfully")
    void shouldPauseVoucher() throws Exception {
        doNothing().when(voucherService).pauseVoucher(1);

        mockMvc.perform(post("/restaurant-owner/vouchers/1/pause"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/restaurant-owner/vouchers"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(voucherService, times(1)).pauseVoucher(1);
    }

    // Test TC RVC-006: Resume voucher
    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("TC RVC-006: Should resume voucher successfully")
    void shouldResumeVoucher() throws Exception {
        doNothing().when(voucherService).resumeVoucher(1);

        mockMvc.perform(post("/restaurant-owner/vouchers/1/resume"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/restaurant-owner/vouchers"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(voucherService, times(1)).resumeVoucher(1);
    }

    // Test TC RVC-007: Expire voucher
    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("TC RVC-007: Should expire voucher successfully")
    void shouldExpireVoucher() throws Exception {
        doNothing().when(voucherService).expireVoucher(1);

        mockMvc.perform(post("/restaurant-owner/vouchers/1/expire"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/restaurant-owner/vouchers"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(voucherService, times(1)).expireVoucher(1);
    }

    // Test TC RVC-008: Delete voucher
    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("TC RVC-008: Should delete voucher successfully")
    void shouldDeleteVoucher() throws Exception {
        doNothing().when(voucherService).deleteVoucher(1);

        mockMvc.perform(post("/restaurant-owner/vouchers/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/restaurant-owner/vouchers?restaurantId=1"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(voucherService, times(1)).deleteVoucher(1);
    }

    // Test TC RVC-009: No restaurants owned
    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("TC RVC-009: Should show error when no restaurants owned")
    void shouldShowErrorWhenNoRestaurantsOwned() throws Exception {
        when(restaurantOwnerService.getRestaurantsByCurrentUser(any(Authentication.class)))
                .thenReturn(new ArrayList<>());

        mockMvc.perform(get("/restaurant-owner/vouchers"))
                .andExpect(status().isOk())
                .andExpect(view().name("restaurant-owner/vouchers/list"))
                .andExpect(model().attributeExists("errorMessage"));

        verify(voucherService, never()).getVouchersByRestaurant(anyInt());
    }

    // Test TC RVC-010: Show edit form
    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("TC RVC-010: Should show edit form")
    void shouldShowEditForm() throws Exception {
        mockMvc.perform(get("/restaurant-owner/vouchers/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("restaurant-owner/vouchers/form_edit"))
                .andExpect(model().attributeExists("voucherForm"))
                .andExpect(model().attributeExists("statuses"));

        verify(voucherService, times(1)).getVoucherById(1);
    }

    // Test TC RVC-011: Update voucher
    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("TC RVC-011: Should update voucher successfully")
    void shouldUpdateVoucherSuccessfully() throws Exception {
        when(voucherService.getVoucherById(1)).thenReturn(mockVoucher);

        mockMvc.perform(post("/restaurant-owner/vouchers/1/edit")
                .param("status", "ACTIVE")
                .param("description", "Updated Description")
                .param("discountValue", "25")
                .param("minOrderAmount", "150000")
                .param("maxDiscountAmount", "75000")
                .param("globalUsageLimit", "200")
                .param("perCustomerLimit", "2")
                .param("startDate", LocalDate.now().toString())
                .param("endDate", LocalDate.now().plusDays(60).toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/restaurant-owner/vouchers/1?restaurantId=1"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(voucherService, times(1)).updateVoucher(eq(1), any(com.example.booking.service.VoucherService.VoucherEditDto.class));
    }

    // Test TC RVC-012: Handle exception in list
    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("TC RVC-012: Should handle exception gracefully")
    void shouldHandleExceptionGracefully() throws Exception {
        when(voucherService.getVouchersByRestaurant(1))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/restaurant-owner/vouchers"))
                .andExpect(status().isOk())
                .andExpect(view().name("restaurant-owner/vouchers/list"))
                .andExpect(model().attributeExists("errorMessage"));

        verify(voucherService, times(1)).getVouchersByRestaurant(1);
    }

    // Test TC RVC-013: Access denied for non-owner
    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("TC RVC-013: Should deny access for non-restaurant-owner")
    void shouldDenyAccessForNonRestaurantOwner() throws Exception {
        mockMvc.perform(get("/restaurant-owner/vouchers"))
                .andExpect(status().isForbidden());

        verify(voucherService, never()).getVouchersByRestaurant(anyInt());
    }

    // Test TC RVC-014: Update with invalid data
    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("TC RVC-014: Should handle invalid update data")
    void shouldHandleInvalidUpdateData() throws Exception {
        mockMvc.perform(post("/restaurant-owner/vouchers/1/edit")
                .param("status", "")  // Empty status
                .param("description", "Updated"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorMessage"));

        verify(voucherService, never()).updateVoucher(anyInt(), any(com.example.booking.service.VoucherService.VoucherEditDto.class));
    }

    // Test TC RVC-015: Voucher not found
    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    @DisplayName("TC RVC-015: Should handle voucher not found")
    void shouldHandleVoucherNotFound() throws Exception {
        when(voucherService.getVoucherById(999)).thenReturn(null);

        mockMvc.perform(get("/restaurant-owner/vouchers/999"))
                .andExpect(status().isOk())
                .andExpect(view().name("restaurant-owner/vouchers/list"))
                .andExpect(model().attributeExists("errorMessage"));

        verify(voucherService, times(1)).getVoucherById(999);
    }
}

