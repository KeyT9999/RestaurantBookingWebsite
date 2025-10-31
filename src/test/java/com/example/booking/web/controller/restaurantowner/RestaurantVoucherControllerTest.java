package com.example.booking.web.controller.restaurantowner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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

import com.example.booking.domain.DiscountType;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.Voucher;
import com.example.booking.domain.VoucherStatus;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.SimpleUserService;
import com.example.booking.service.VoucherService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RestaurantVoucherController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("RestaurantVoucherController Test Suite")
class RestaurantVoucherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VoucherService voucherService;

    @MockBean
    private RestaurantOwnerService restaurantOwnerService;

    @MockBean
    private SimpleUserService userService;

    private RestaurantProfile testRestaurant;
    private Voucher testVoucher;
    private List<RestaurantProfile> restaurantList;
    private List<Voucher> voucherList;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Setup test restaurant
        testRestaurant = new RestaurantProfile();
        testRestaurant.setRestaurantId(1);
        testRestaurant.setRestaurantName("Test Restaurant");
        restaurantList = Arrays.asList(testRestaurant);

        // Setup test voucher
        testVoucher = new Voucher();
        testVoucher.setVoucherId(1);
        testVoucher.setCode("TEST2024");
        testVoucher.setDescription("Test Voucher");
        testVoucher.setDiscountType(DiscountType.PERCENT);
        testVoucher.setDiscountValue(BigDecimal.valueOf(10.0));
        testVoucher.setStartDate(LocalDate.now().plusDays(1));
        testVoucher.setEndDate(LocalDate.now().plusDays(30));
        testVoucher.setGlobalUsageLimit(100);
        testVoucher.setPerCustomerLimit(1);
        testVoucher.setMinOrderAmount(BigDecimal.valueOf(100000.0));
        testVoucher.setMaxDiscountAmount(BigDecimal.valueOf(50000.0));
        testVoucher.setStatus(VoucherStatus.ACTIVE);
        testVoucher.setRestaurant(testRestaurant);
        testVoucher.setCreatedAt(LocalDateTime.now());

        voucherList = Arrays.asList(testVoucher);

        // Setup test user
        testUser = new User("owner", "owner@test.com", "password", "Owner Name");
        testUser.setId(UUID.randomUUID());
    }

    @Nested
    @DisplayName("listVouchers() Tests")
    class ListVouchersTests {

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should display vouchers list successfully")
        void shouldDisplayVouchersList() throws Exception {
            when(restaurantOwnerService.getRestaurantsByCurrentUser(any())).thenReturn(restaurantList);
            when(voucherService.getVouchersByRestaurant(1)).thenReturn(voucherList);

            mockMvc.perform(get("/restaurant-owner/vouchers"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("restaurant-owner/vouchers/list"))
                    .andExpect(model().attributeExists("vouchers"))
                    .andExpect(model().attributeExists("restaurants"))
                    .andExpect(model().attribute("selectedRestaurantId", 1));

            verify(restaurantOwnerService).getRestaurantsByCurrentUser(any());
            verify(voucherService).getVouchersByRestaurant(1);
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should handle empty restaurants list")
        void shouldHandleEmptyRestaurants() throws Exception {
            when(restaurantOwnerService.getRestaurantsByCurrentUser(any())).thenReturn(new ArrayList<>());

            mockMvc.perform(get("/restaurant-owner/vouchers"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("restaurant-owner/vouchers/list"))
                    .andExpect(model().attributeExists("errorMessage"));

            verify(restaurantOwnerService).getRestaurantsByCurrentUser(any());
            verify(voucherService, never()).getVouchersByRestaurant(anyInt());
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should filter by restaurantId parameter")
        void shouldFilterByRestaurantId() throws Exception {
            RestaurantProfile restaurant2 = new RestaurantProfile();
            restaurant2.setRestaurantId(2);
            restaurant2.setRestaurantName("Restaurant 2");
            List<RestaurantProfile> multipleRestaurants = Arrays.asList(testRestaurant, restaurant2);

            when(restaurantOwnerService.getRestaurantsByCurrentUser(any())).thenReturn(multipleRestaurants);
            when(voucherService.getVouchersByRestaurant(2)).thenReturn(voucherList);

            mockMvc.perform(get("/restaurant-owner/vouchers")
                    .param("restaurantId", "2"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("selectedRestaurantId", 2));

            verify(voucherService).getVouchersByRestaurant(2);
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should reject unauthorized restaurant access")
        void shouldRejectUnauthorizedRestaurant() throws Exception {
            when(restaurantOwnerService.getRestaurantsByCurrentUser(any())).thenReturn(restaurantList);

            mockMvc.perform(get("/restaurant-owner/vouchers")
                    .param("restaurantId", "999"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("errorMessage"));

            verify(voucherService, never()).getVouchersByRestaurant(999);
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should handle service exception")
        void shouldHandleServiceException() throws Exception {
            when(restaurantOwnerService.getRestaurantsByCurrentUser(any())).thenReturn(restaurantList);
            when(voucherService.getVouchersByRestaurant(anyInt())).thenThrow(new RuntimeException("Service error"));

            mockMvc.perform(get("/restaurant-owner/vouchers"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("errorMessage"));
        }
    }

    @Nested
    @DisplayName("showCreateForm() Tests")
    class ShowCreateFormTests {

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should display create form successfully")
        void shouldDisplayCreateForm() throws Exception {
            when(restaurantOwnerService.getRestaurantsByCurrentUser(any())).thenReturn(restaurantList);

            mockMvc.perform(get("/restaurant-owner/vouchers/new"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("restaurant-owner/vouchers/form"))
                    .andExpect(model().attributeExists("voucherForm"))
                    .andExpect(model().attributeExists("statuses"))
                    .andExpect(model().attributeExists("restaurants"));

            verify(restaurantOwnerService).getRestaurantsByCurrentUser(any());
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should handle empty restaurants in create form")
        void shouldHandleEmptyRestaurantsInCreateForm() throws Exception {
            when(restaurantOwnerService.getRestaurantsByCurrentUser(any())).thenReturn(new ArrayList<>());

            mockMvc.perform(get("/restaurant-owner/vouchers/new"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("restaurant-owner/vouchers/form"))
                    .andExpect(model().attributeExists("errorMessage"));
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should reject unauthorized restaurant in create form")
        void shouldRejectUnauthorizedRestaurantInCreateForm() throws Exception {
            when(restaurantOwnerService.getRestaurantsByCurrentUser(any())).thenReturn(restaurantList);

            mockMvc.perform(get("/restaurant-owner/vouchers/new")
                    .param("restaurantId", "999"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("errorMessage"));
        }
    }

    @Nested
    @DisplayName("createVoucher() Tests")
    class CreateVoucherTests {

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should create voucher successfully")
        void shouldCreateVoucherSuccessfully() throws Exception {
            when(restaurantOwnerService.getRestaurantsByCurrentUser(any())).thenReturn(restaurantList);
            doNothing().when(voucherService).createRestaurantVoucher(anyInt(), any());

            mockMvc.perform(post("/restaurant-owner/vouchers/new")
                    .with(csrf())
                    .param("code", "NEW2024")
                    .param("description", "New Voucher")
                    .param("discountType", "PERCENT")
                    .param("discountValue", "15.0")
                    .param("startDate", LocalDate.now().plusDays(1).toString())
                    .param("endDate", LocalDate.now().plusDays(30).toString())
                    .param("status", "ACTIVE"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/restaurant-owner/vouchers?restaurantId=1"))
                    .andExpect(flash().attributeExists("successMessage"));

            verify(voucherService).createRestaurantVoucher(eq(1), any());
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should handle validation errors")
        void shouldHandleValidationErrors() throws Exception {
            when(restaurantOwnerService.getRestaurantsByCurrentUser(any())).thenReturn(restaurantList);

            mockMvc.perform(post("/restaurant-owner/vouchers/new")
                    .with(csrf())
                    .param("code", "") // Empty code should fail validation
                    .param("description", "Test"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("/restaurant-owner/vouchers/new*"));
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should handle service exception during creation")
        void shouldHandleServiceExceptionDuringCreation() throws Exception {
            when(restaurantOwnerService.getRestaurantsByCurrentUser(any())).thenReturn(restaurantList);
            doThrow(new RuntimeException("Creation failed")).when(voucherService)
                    .createRestaurantVoucher(anyInt(), any());

            mockMvc.perform(post("/restaurant-owner/vouchers/new")
                    .with(csrf())
                    .param("code", "NEW2024")
                    .param("description", "New Voucher")
                    .param("discountType", "PERCENT")
                    .param("discountValue", "15.0")
                    .param("status", "ACTIVE"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(flash().attributeExists("errorMessage"));
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should reject unauthorized restaurant creation")
        void shouldRejectUnauthorizedRestaurantCreation() throws Exception {
            when(restaurantOwnerService.getRestaurantsByCurrentUser(any())).thenReturn(restaurantList);

            mockMvc.perform(post("/restaurant-owner/vouchers/new")
                    .with(csrf())
                    .param("restaurantId", "999")
                    .param("code", "NEW2024")
                    .param("description", "New Voucher")
                    .param("status", "ACTIVE"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(flash().attributeExists("errorMessage"));

            verify(voucherService, never()).createRestaurantVoucher(eq(999), any());
        }
    }

    @Nested
    @DisplayName("viewVoucher() Tests")
    class ViewVoucherTests {

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should display voucher details successfully")
        void shouldDisplayVoucherDetails() throws Exception {
            when(restaurantOwnerService.getRestaurantsByCurrentUser(any())).thenReturn(restaurantList);
            when(voucherService.getVoucherById(1)).thenReturn(testVoucher);

            mockMvc.perform(get("/restaurant-owner/vouchers/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("restaurant-owner/vouchers/detail"))
                    .andExpect(model().attributeExists("voucher"))
                    .andExpect(model().attributeExists("usedCount"))
                    .andExpect(model().attributeExists("totalLimit"))
                    .andExpect(model().attributeExists("usagePercentage"));

            verify(voucherService).getVoucherById(1);
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should handle voucher not found")
        void shouldHandleVoucherNotFound() throws Exception {
            when(restaurantOwnerService.getRestaurantsByCurrentUser(any())).thenReturn(restaurantList);
            when(voucherService.getVoucherById(999)).thenReturn(null);

            mockMvc.perform(get("/restaurant-owner/vouchers/999"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("restaurant-owner/vouchers/list"))
                    .andExpect(model().attributeExists("errorMessage"));
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should reject unauthorized voucher access")
        void shouldRejectUnauthorizedVoucherAccess() throws Exception {
            Voucher otherVoucher = new Voucher();
            otherVoucher.setVoucherId(2);
            RestaurantProfile otherRestaurant = new RestaurantProfile();
            otherRestaurant.setRestaurantId(999);
            otherVoucher.setRestaurant(otherRestaurant);

            when(restaurantOwnerService.getRestaurantsByCurrentUser(any())).thenReturn(restaurantList);
            when(voucherService.getVoucherById(2)).thenReturn(otherVoucher);

            mockMvc.perform(get("/restaurant-owner/vouchers/2"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("restaurant-owner/vouchers/list"))
                    .andExpect(model().attributeExists("errorMessage"));
        }
    }

    @Nested
    @DisplayName("showEditForm() Tests")
    class ShowEditFormTests {

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should display edit form successfully")
        void shouldDisplayEditForm() throws Exception {
            when(restaurantOwnerService.getRestaurantsByCurrentUser(any())).thenReturn(restaurantList);
            when(voucherService.getVoucherById(1)).thenReturn(testVoucher);

            mockMvc.perform(get("/restaurant-owner/vouchers/1/edit"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("restaurant-owner/vouchers/form_edit"))
                    .andExpect(model().attributeExists("voucherForm"))
                    .andExpect(model().attributeExists("voucher"))
                    .andExpect(model().attributeExists("statuses"));

            verify(voucherService).getVoucherById(1);
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should handle voucher not found in edit form")
        void shouldHandleVoucherNotFoundInEditForm() throws Exception {
            when(restaurantOwnerService.getRestaurantsByCurrentUser(any())).thenReturn(restaurantList);
            when(voucherService.getVoucherById(999)).thenReturn(null);

            mockMvc.perform(get("/restaurant-owner/vouchers/999/edit"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("restaurant-owner/vouchers/list"))
                    .andExpect(model().attributeExists("errorMessage"));
        }
    }

    @Nested
    @DisplayName("updateVoucher() Tests")
    class UpdateVoucherTests {

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should update voucher successfully")
        void shouldUpdateVoucherSuccessfully() throws Exception {
            when(restaurantOwnerService.getRestaurantsByCurrentUser(any())).thenReturn(restaurantList);
            when(voucherService.getVoucherById(1)).thenReturn(testVoucher);
            when(voucherService.updateVoucher(anyInt(), any(VoucherService.VoucherEditDto.class))).thenReturn(testVoucher);

            mockMvc.perform(post("/restaurant-owner/vouchers/1/edit")
                    .with(csrf())
                    .param("status", "PAUSED")
                    .param("description", "Updated Description")
                    .param("discountValue", "20.0"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("/restaurant-owner/vouchers/1*"))
                    .andExpect(flash().attributeExists("successMessage"));

            verify(voucherService).updateVoucher(eq(1), any(VoucherService.VoucherEditDto.class));
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should handle empty status")
        void shouldHandleEmptyStatus() throws Exception {
            when(restaurantOwnerService.getRestaurantsByCurrentUser(any())).thenReturn(restaurantList);
            when(voucherService.getVoucherById(1)).thenReturn(testVoucher);

            mockMvc.perform(post("/restaurant-owner/vouchers/1/edit")
                    .with(csrf())
                    .param("status", ""))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(flash().attributeExists("errorMessage"));

            verify(voucherService, never()).updateVoucher(anyInt(), any(VoucherService.VoucherEditDto.class));
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should handle service exception during update")
        void shouldHandleServiceExceptionDuringUpdate() throws Exception {
            when(restaurantOwnerService.getRestaurantsByCurrentUser(any())).thenReturn(restaurantList);
            when(voucherService.getVoucherById(1)).thenReturn(testVoucher);
            doThrow(new RuntimeException("Update failed")).when(voucherService).updateVoucher(anyInt(), any(VoucherService.VoucherEditDto.class));

            mockMvc.perform(post("/restaurant-owner/vouchers/1/edit")
                    .with(csrf())
                    .param("status", "ACTIVE"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(flash().attributeExists("errorMessage"));
        }
    }

    @Nested
    @DisplayName("pauseVoucher() Tests")
    class PauseVoucherTests {

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should pause voucher successfully")
        void shouldPauseVoucherSuccessfully() throws Exception {
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
        @DisplayName("Should handle exception during pause")
        void shouldHandleExceptionDuringPause() throws Exception {
            doThrow(new RuntimeException("Pause failed")).when(voucherService).pauseVoucher(1);

            mockMvc.perform(post("/restaurant-owner/vouchers/1/pause")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(flash().attributeExists("errorMessage"));
        }
    }

    @Nested
    @DisplayName("resumeVoucher() Tests")
    class ResumeVoucherTests {

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should resume voucher successfully")
        void shouldResumeVoucherSuccessfully() throws Exception {
            doNothing().when(voucherService).resumeVoucher(1);

            mockMvc.perform(post("/restaurant-owner/vouchers/1/resume")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/restaurant-owner/vouchers"))
                    .andExpect(flash().attributeExists("successMessage"));

            verify(voucherService).resumeVoucher(1);
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should handle exception during resume")
        void shouldHandleExceptionDuringResume() throws Exception {
            doThrow(new RuntimeException("Resume failed")).when(voucherService).resumeVoucher(1);

            mockMvc.perform(post("/restaurant-owner/vouchers/1/resume")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(flash().attributeExists("errorMessage"));
        }
    }

    @Nested
    @DisplayName("expireVoucher() Tests")
    class ExpireVoucherTests {

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should expire voucher successfully")
        void shouldExpireVoucherSuccessfully() throws Exception {
            doNothing().when(voucherService).expireVoucher(1);

            mockMvc.perform(post("/restaurant-owner/vouchers/1/expire")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/restaurant-owner/vouchers"))
                    .andExpect(flash().attributeExists("successMessage"));

            verify(voucherService).expireVoucher(1);
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should handle exception during expire")
        void shouldHandleExceptionDuringExpire() throws Exception {
            doThrow(new RuntimeException("Expire failed")).when(voucherService).expireVoucher(1);

            mockMvc.perform(post("/restaurant-owner/vouchers/1/expire")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(flash().attributeExists("errorMessage"));
        }
    }

    @Nested
    @DisplayName("deleteVoucher() Tests")
    class DeleteVoucherTests {

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should delete voucher successfully")
        void shouldDeleteVoucherSuccessfully() throws Exception {
            when(restaurantOwnerService.getRestaurantsByCurrentUser(any())).thenReturn(restaurantList);
            when(voucherService.getVoucherById(1)).thenReturn(testVoucher);
            doNothing().when(voucherService).deleteVoucher(1);

            mockMvc.perform(post("/restaurant-owner/vouchers/1/delete")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("/restaurant-owner/vouchers*"))
                    .andExpect(flash().attributeExists("successMessage"));

            verify(voucherService).deleteVoucher(1);
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should handle voucher not found during delete")
        void shouldHandleVoucherNotFoundDuringDelete() throws Exception {
            when(restaurantOwnerService.getRestaurantsByCurrentUser(any())).thenReturn(restaurantList);
            when(voucherService.getVoucherById(999)).thenReturn(null);

            mockMvc.perform(post("/restaurant-owner/vouchers/999/delete")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(flash().attributeExists("errorMessage"));

            verify(voucherService, never()).deleteVoucher(999);
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should reject unauthorized voucher deletion")
        void shouldRejectUnauthorizedVoucherDeletion() throws Exception {
            Voucher otherVoucher = new Voucher();
            otherVoucher.setVoucherId(2);
            RestaurantProfile otherRestaurant = new RestaurantProfile();
            otherRestaurant.setRestaurantId(999);
            otherVoucher.setRestaurant(otherRestaurant);

            when(restaurantOwnerService.getRestaurantsByCurrentUser(any())).thenReturn(restaurantList);
            when(voucherService.getVoucherById(2)).thenReturn(otherVoucher);

            mockMvc.perform(post("/restaurant-owner/vouchers/2/delete")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(flash().attributeExists("errorMessage"));

            verify(voucherService, never()).deleteVoucher(2);
        }
    }

    @Nested
    @DisplayName("debugVouchers() Tests")
    class DebugVouchersTests {

        @Test
        @DisplayName("Should display debug vouchers (no auth required)")
        void shouldDisplayDebugVouchers() throws Exception {
            when(voucherService.getVouchersByRestaurant(16)).thenReturn(voucherList);

            mockMvc.perform(get("/restaurant-owner/vouchers/debug"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("restaurant-owner/vouchers/list"))
                    .andExpect(model().attributeExists("vouchers"))
                    .andExpect(model().attribute("debugMode", true));

            verify(voucherService).getVouchersByRestaurant(16);
        }

        @Test
        @DisplayName("Should handle exception in debug endpoint")
        void shouldHandleExceptionInDebug() throws Exception {
            when(voucherService.getVouchersByRestaurant(16)).thenThrow(new RuntimeException("Debug error"));

            mockMvc.perform(get("/restaurant-owner/vouchers/debug"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("errorMessage"));
        }
    }

    @Nested
    @DisplayName("testSimple() Tests")
    class TestSimpleTests {

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should display test simple view")
        void shouldDisplayTestSimpleView() throws Exception {
            when(voucherService.getAllVouchers()).thenReturn(voucherList);

            mockMvc.perform(get("/restaurant-owner/vouchers/test-simple"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("restaurant-owner/vouchers/test_simple"))
                    .andExpect(model().attributeExists("vouchers"));

            verify(voucherService).getAllVouchers();
        }

        @Test
        @WithMockUser(roles = "RESTAURANT_OWNER")
        @DisplayName("Should handle null authentication principal")
        void shouldHandleNullAuthenticationPrincipal() throws Exception {
            mockMvc.perform(get("/restaurant-owner/vouchers/test-simple")
                    .principal(null))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login"));
        }
    }
}

