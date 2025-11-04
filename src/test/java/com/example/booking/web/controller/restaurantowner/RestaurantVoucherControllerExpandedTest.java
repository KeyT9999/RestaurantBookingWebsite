package com.example.booking.web.controller.restaurantowner;

import com.example.booking.domain.*;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.SimpleUserService;
import com.example.booking.service.VoucherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Expanded comprehensive tests for RestaurantVoucherController
 */
@WebMvcTest(RestaurantVoucherController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("RestaurantVoucherController Expanded Test Suite")
class RestaurantVoucherControllerExpandedTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VoucherService voucherService;

    @MockBean
    private RestaurantOwnerService restaurantOwnerService;

    @MockBean
    private SimpleUserService userService;

    private RestaurantProfile createMockRestaurant(Integer id) {
        RestaurantProfile restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(id);
        restaurant.setRestaurantName("Test Restaurant " + id);
        return restaurant;
    }

    private Voucher createMockVoucher(Integer id, Integer restaurantId) {
        Voucher voucher = new Voucher();
        voucher.setVoucherId(id);
        voucher.setCode("TEST" + id);
        voucher.setDescription("Test voucher");
        voucher.setDiscountType(DiscountType.PERCENT);
        voucher.setDiscountValue(new BigDecimal("10.00"));
        voucher.setStartDate(LocalDate.now());
        voucher.setEndDate(LocalDate.now().plusDays(30));
        voucher.setStatus(VoucherStatus.ACTIVE);
        voucher.setGlobalUsageLimit(100);
        voucher.setPerCustomerLimit(1);
        
        RestaurantProfile restaurant = createMockRestaurant(restaurantId);
        voucher.setRestaurant(restaurant);
        
        return voucher;
    }

    @Nested
    @DisplayName("listVouchers() Tests")
    class ListVouchersTests {

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should list vouchers successfully")
        void testListVouchers_WithValidData_ShouldDisplaySuccessfully() throws Exception {
            RestaurantProfile restaurant = createMockRestaurant(1);
            Voucher voucher = createMockVoucher(1, 1);

            when(restaurantOwnerService.getRestaurantsByCurrentUser(any()))
                    .thenReturn(Collections.singletonList(restaurant));
            when(voucherService.getVouchersByRestaurant(1))
                    .thenReturn(Collections.singletonList(voucher));

            mockMvc.perform(get("/restaurant-owner/vouchers"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("restaurant-owner/vouchers/list"))
                    .andExpect(model().attributeExists("vouchers"))
                    .andExpect(model().attributeExists("restaurants"));

            verify(restaurantOwnerService).getRestaurantsByCurrentUser(any());
            verify(voucherService).getVouchersByRestaurant(1);
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should handle empty restaurants list")
        void testListVouchers_WithNoRestaurants_ShouldShowErrorMessage() throws Exception {
            when(restaurantOwnerService.getRestaurantsByCurrentUser(any()))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/restaurant-owner/vouchers"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("restaurant-owner/vouchers/list"))
                    .andExpect(model().attributeExists("errorMessage"));

            verify(restaurantOwnerService).getRestaurantsByCurrentUser(any());
            verify(voucherService, never()).getVouchersByRestaurant(anyInt());
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should handle unauthorized restaurant access")
        void testListVouchers_WithUnauthorizedRestaurant_ShouldShowError() throws Exception {
            RestaurantProfile ownedRestaurant = createMockRestaurant(1);
            when(restaurantOwnerService.getRestaurantsByCurrentUser(any()))
                    .thenReturn(Collections.singletonList(ownedRestaurant));

            mockMvc.perform(get("/restaurant-owner/vouchers")
                    .param("restaurantId", "999"))  // Restaurant not owned by user
                    .andExpect(status().isOk())
                    .andExpect(view().name("restaurant-owner/vouchers/list"))
                    .andExpect(model().attributeExists("errorMessage"));

            verify(restaurantOwnerService).getRestaurantsByCurrentUser(any());
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should handle exception when listing vouchers")
        void testListVouchers_WhenServiceThrowsException_ShouldHandleError() throws Exception {
            RestaurantProfile restaurant = createMockRestaurant(1);
            when(restaurantOwnerService.getRestaurantsByCurrentUser(any()))
                    .thenReturn(Collections.singletonList(restaurant));
            when(voucherService.getVouchersByRestaurant(1))
                    .thenThrow(new RuntimeException("Database error"));

            mockMvc.perform(get("/restaurant-owner/vouchers"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("restaurant-owner/vouchers/list"))
                    .andExpect(model().attributeExists("errorMessage"));
        }
    }

    @Nested
    @DisplayName("showCreateForm() Tests")
    class ShowCreateFormTests {

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should show create form successfully")
        void testShowCreateForm_WithValidData_ShouldDisplayForm() throws Exception {
            RestaurantProfile restaurant = createMockRestaurant(1);
            when(restaurantOwnerService.getRestaurantsByCurrentUser(any()))
                    .thenReturn(Collections.singletonList(restaurant));

            mockMvc.perform(get("/restaurant-owner/vouchers/new"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("restaurant-owner/vouchers/form"))
                    .andExpect(model().attributeExists("voucherForm"))
                    .andExpect(model().attributeExists("restaurants"))
                    .andExpect(model().attributeExists("statuses"));

            verify(restaurantOwnerService).getRestaurantsByCurrentUser(any());
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should handle empty restaurants when showing form")
        void testShowCreateForm_WithNoRestaurants_ShouldShowError() throws Exception {
            when(restaurantOwnerService.getRestaurantsByCurrentUser(any()))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/restaurant-owner/vouchers/new"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("restaurant-owner/vouchers/form"))
                    .andExpect(model().attributeExists("errorMessage"));
        }
    }

    @Nested
    @DisplayName("createVoucher() Tests")
    class CreateVoucherTests {

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should create voucher successfully")
        void testCreateVoucher_WithValidData_ShouldSucceed() throws Exception {
            RestaurantProfile restaurant = createMockRestaurant(1);
            when(restaurantOwnerService.getRestaurantsByCurrentUser(any()))
                    .thenReturn(Collections.singletonList(restaurant));
            doNothing().when(voucherService).createRestaurantVoucher(anyInt(), any());

            mockMvc.perform(post("/restaurant-owner/vouchers/new")
                    .with(csrf())
                    .param("code", "TESTCODE")
                    .param("description", "Test voucher")
                    .param("discountType", "PERCENT")
                    .param("discountValue", "10.00")
                    .param("startDate", LocalDate.now().toString())
                    .param("endDate", LocalDate.now().plusDays(30).toString())
                    .param("globalUsageLimit", "100")
                    .param("perCustomerLimit", "1")
                    .param("status", "ACTIVE"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/vouchers*"))
                    .andExpect(flash().attributeExists("successMessage"));

            verify(voucherService).createRestaurantVoucher(eq(1), any());
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should handle validation errors")
        void testCreateVoucher_WithValidationErrors_ShouldReturnToForm() throws Exception {
            RestaurantProfile restaurant = createMockRestaurant(1);
            when(restaurantOwnerService.getRestaurantsByCurrentUser(any()))
                    .thenReturn(Collections.singletonList(restaurant));

            mockMvc.perform(post("/restaurant-owner/vouchers/new")
                    .with(csrf())
                    .param("code", "")  // Invalid: empty code
                    .param("discountValue", "-10.00"))  // Invalid: negative value
                    .andExpect(status().is3xxRedirection());

            verify(voucherService, never()).createRestaurantVoucher(anyInt(), any());
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should handle service exception")
        void testCreateVoucher_WhenServiceThrowsException_ShouldHandleError() throws Exception {
            RestaurantProfile restaurant = createMockRestaurant(1);
            when(restaurantOwnerService.getRestaurantsByCurrentUser(any()))
                    .thenReturn(Collections.singletonList(restaurant));
            doThrow(new RuntimeException("Service error")).when(voucherService)
                    .createRestaurantVoucher(anyInt(), any());

            mockMvc.perform(post("/restaurant-owner/vouchers/new")
                    .with(csrf())
                    .param("code", "TESTCODE")
                    .param("description", "Test voucher")
                    .param("discountType", "PERCENT")
                    .param("discountValue", "10.00"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(flash().attributeExists("errorMessage"));
        }
    }

    @Nested
    @DisplayName("pauseVoucher() / resumeVoucher() / expireVoucher() Tests")
    class VoucherStatusTests {

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should pause voucher successfully")
        void testPauseVoucher_WithValidId_ShouldSucceed() throws Exception {
            doNothing().when(voucherService).pauseVoucher(1);

            mockMvc.perform(post("/restaurant-owner/vouchers/1/pause")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/restaurant-owner/vouchers"))
                    .andExpect(flash().attributeExists("successMessage"));

            verify(voucherService).pauseVoucher(1);
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should resume voucher successfully")
        void testResumeVoucher_WithValidId_ShouldSucceed() throws Exception {
            doNothing().when(voucherService).resumeVoucher(1);

            mockMvc.perform(post("/restaurant-owner/vouchers/1/resume")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(flash().attributeExists("successMessage"));

            verify(voucherService).resumeVoucher(1);
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should expire voucher successfully")
        void testExpireVoucher_WithValidId_ShouldSucceed() throws Exception {
            doNothing().when(voucherService).expireVoucher(1);

            mockMvc.perform(post("/restaurant-owner/vouchers/1/expire")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(flash().attributeExists("successMessage"));

            verify(voucherService).expireVoucher(1);
        }
    }

    @Nested
    @DisplayName("viewVoucher() Tests")
    class ViewVoucherTests {

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should view voucher successfully")
        void testViewVoucher_WithValidId_ShouldDisplayDetails() throws Exception {
            RestaurantProfile restaurant = createMockRestaurant(1);
            Voucher voucher = createMockVoucher(1, 1);

            when(restaurantOwnerService.getRestaurantsByCurrentUser(any()))
                    .thenReturn(Collections.singletonList(restaurant));
            when(voucherService.getVoucherById(1)).thenReturn(voucher);

            mockMvc.perform(get("/restaurant-owner/vouchers/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("restaurant-owner/vouchers/detail"))
                    .andExpect(model().attributeExists("voucher"))
                    .andExpect(model().attributeExists("usedCount"));

            verify(voucherService).getVoucherById(1);
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should handle voucher not found")
        void testViewVoucher_WithInvalidId_ShouldShowError() throws Exception {
            RestaurantProfile restaurant = createMockRestaurant(1);
            when(restaurantOwnerService.getRestaurantsByCurrentUser(any()))
                    .thenReturn(Collections.singletonList(restaurant));
            when(voucherService.getVoucherById(999)).thenReturn(null);

            mockMvc.perform(get("/restaurant-owner/vouchers/999"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("restaurant-owner/vouchers/list"))
                    .andExpect(model().attributeExists("errorMessage"));
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should handle unauthorized voucher access")
        void testViewVoucher_WithUnauthorizedAccess_ShouldShowError() throws Exception {
            RestaurantProfile ownedRestaurant = createMockRestaurant(1);
            Voucher voucher = createMockVoucher(1, 999);  // Voucher belongs to restaurant 999

            when(restaurantOwnerService.getRestaurantsByCurrentUser(any()))
                    .thenReturn(Collections.singletonList(ownedRestaurant));
            when(voucherService.getVoucherById(1)).thenReturn(voucher);

            mockMvc.perform(get("/restaurant-owner/vouchers/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("restaurant-owner/vouchers/list"))
                    .andExpect(model().attributeExists("errorMessage"));
        }
    }

    @Nested
    @DisplayName("deleteVoucher() Tests")
    class DeleteVoucherTests {

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should delete voucher successfully")
        void testDeleteVoucher_WithValidId_ShouldSucceed() throws Exception {
            RestaurantProfile restaurant = createMockRestaurant(1);
            Voucher voucher = createMockVoucher(1, 1);

            when(restaurantOwnerService.getRestaurantsByCurrentUser(any()))
                    .thenReturn(Collections.singletonList(restaurant));
            when(voucherService.getVoucherById(1)).thenReturn(voucher);
            doNothing().when(voucherService).deleteVoucher(1);

            mockMvc.perform(post("/restaurant-owner/vouchers/1/delete")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(flash().attributeExists("successMessage"));

            verify(voucherService).deleteVoucher(1);
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should handle unauthorized deletion")
        void testDeleteVoucher_WithUnauthorizedAccess_ShouldShowError() throws Exception {
            RestaurantProfile ownedRestaurant = createMockRestaurant(1);
            Voucher voucher = createMockVoucher(1, 999);  // Voucher belongs to restaurant 999

            when(restaurantOwnerService.getRestaurantsByCurrentUser(any()))
                    .thenReturn(Collections.singletonList(ownedRestaurant));
            when(voucherService.getVoucherById(1)).thenReturn(voucher);

            mockMvc.perform(post("/restaurant-owner/vouchers/1/delete")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(flash().attributeExists("errorMessage"));

            verify(voucherService, never()).deleteVoucher(anyInt());
        }
    }
}

