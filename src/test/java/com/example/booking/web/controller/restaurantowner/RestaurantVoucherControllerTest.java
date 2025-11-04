package com.example.booking.web.controller.restaurantowner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;
import com.example.booking.domain.Voucher;
import com.example.booking.domain.VoucherStatus;
import com.example.booking.domain.VoucherRedemption;
import com.example.booking.domain.DiscountType;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.example.booking.dto.admin.VoucherCreateForm;
import com.example.booking.service.VoucherService;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.SimpleUserService;

/**
 * Unit tests for RestaurantVoucherController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RestaurantVoucherController Tests")
public class RestaurantVoucherControllerTest {

    @Mock
    private VoucherService voucherService;

    @Mock
    private RestaurantOwnerService restaurantOwnerService;

    @Mock
    private SimpleUserService userService;

    @Mock
    private Authentication authentication;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private RestaurantVoucherController controller;

    private User owner;
    private RestaurantProfile restaurant;
    private Voucher voucher;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setUsername("owner");

        restaurant = new RestaurantProfile();
        restaurant.setRestaurantId(1);
        restaurant.setRestaurantName("Test Restaurant");

        voucher = new Voucher();
        voucher.setVoucherId(1);
        voucher.setCode("TEST2024");
        voucher.setStatus(VoucherStatus.ACTIVE);
        voucher.setRestaurant(restaurant);
    }

    // ========== listVouchers() Tests ==========

    @Test
    @DisplayName("shouldListVouchers_successfully")
    void shouldListVouchers_successfully() {
        // Given
        List<RestaurantProfile> restaurants = new ArrayList<>();
        restaurants.add(restaurant);

        List<Voucher> vouchers = new ArrayList<>();
        vouchers.add(voucher);

        when(authentication.getName()).thenReturn("owner");
        when(restaurantOwnerService.getRestaurantsByCurrentUser(authentication)).thenReturn(restaurants);
        when(voucherService.getVouchersByRestaurant(1)).thenReturn(vouchers);

        // When
        String view = controller.listVouchers(0, 10, "createdAt", "desc", null, null, null, authentication, model);

        // Then
        assertNotNull(view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    // ========== createForm() Tests ==========

    @Test
    @DisplayName("shouldShowCreateForm_successfully")
    void shouldShowCreateForm_successfully() {
        // Given
        List<RestaurantProfile> restaurants = new ArrayList<>();
        restaurants.add(restaurant);

        when(authentication.getName()).thenReturn("owner");
        when(restaurantOwnerService.getRestaurantsByCurrentUser(authentication)).thenReturn(restaurants);

        // When
        String view = controller.showCreateForm(null, authentication, model);

        // Then
        assertNotNull(view);
        verify(model, atLeastOnce()).addAttribute(eq("voucherForm"), any(VoucherCreateForm.class));
    }

    // ========== create() Tests ==========

    @Test
    @DisplayName("shouldCreateVoucher_successfully")
    void shouldCreateVoucher_successfully() {
        // Given
        VoucherCreateForm form = new VoucherCreateForm();
        form.setCode("NEW2024");

        List<RestaurantProfile> restaurants = new ArrayList<>();
        restaurants.add(restaurant);

        when(bindingResult.hasErrors()).thenReturn(false);
        when(authentication.getName()).thenReturn("owner");
        when(restaurantOwnerService.getRestaurantsByCurrentUser(authentication)).thenReturn(restaurants);
        when(voucherService.createRestaurantVoucher(eq(1), any())).thenReturn(voucher);

        // When
        String view = controller.createVoucher(form, bindingResult, null, authentication, redirectAttributes);

        // Then
        assertNotNull(view);
        verify(voucherService, times(1)).createRestaurantVoucher(eq(1), any());
    }

    // ========== editForm() Tests ==========

    @Test
    @DisplayName("shouldShowEditForm_successfully")
    void shouldShowEditForm_successfully() {
        // Given
        List<RestaurantProfile> restaurants = new ArrayList<>();
        restaurants.add(restaurant);

        when(voucherService.getVoucherById(1)).thenReturn(voucher);
        when(authentication.getName()).thenReturn("owner");
        when(restaurantOwnerService.getRestaurantsByCurrentUser(authentication)).thenReturn(restaurants);

        // When
        String view = controller.showEditForm(1, null, authentication, model);

        // Then
        assertNotNull(view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    // ========== Additional Edge Case Tests ==========

    @Test
    @DisplayName("shouldListVouchers_withEmptyRestaurants_returnsError")
    void shouldListVouchers_withEmptyRestaurants_returnsError() {
        // Given
        when(restaurantOwnerService.getRestaurantsByCurrentUser(authentication)).thenReturn(new ArrayList<>());

        // When
        String view = controller.listVouchers(0, 10, "createdAt", "desc", null, null, null, authentication, model);

        // Then
        assertEquals("restaurant-owner/vouchers/list", view);
        verify(model).addAttribute("errorMessage", "Không tìm thấy nhà hàng nào của bạn. Vui lòng tạo nhà hàng trước.");
    }

    @Test
    @DisplayName("shouldListVouchers_withRestaurantIdProvided")
    void shouldListVouchers_withRestaurantIdProvided() {
        // Given
        List<RestaurantProfile> restaurants = new ArrayList<>();
        restaurants.add(restaurant);
        RestaurantProfile restaurant2 = new RestaurantProfile();
        restaurant2.setRestaurantId(2);
        restaurants.add(restaurant2);

        List<Voucher> vouchers = new ArrayList<>();
        vouchers.add(voucher);

        when(restaurantOwnerService.getRestaurantsByCurrentUser(authentication)).thenReturn(restaurants);
        when(voucherService.getVouchersByRestaurant(1)).thenReturn(vouchers);

        // When
        String view = controller.listVouchers(0, 10, "createdAt", "desc", null, null, 1, authentication, model);

        // Then
        assertEquals("restaurant-owner/vouchers/list", view);
        verify(model).addAttribute("selectedRestaurantId", 1);
    }

    @Test
    @DisplayName("shouldListVouchers_withRestaurantNotOwned_returnsError")
    void shouldListVouchers_withRestaurantNotOwned_returnsError() {
        // Given
        List<RestaurantProfile> restaurants = new ArrayList<>();
        restaurants.add(restaurant);

        when(restaurantOwnerService.getRestaurantsByCurrentUser(authentication)).thenReturn(restaurants);

        // When - Try to access restaurant ID 999 which owner doesn't own
        String view = controller.listVouchers(0, 10, "createdAt", "desc", null, null, 999, authentication, model);

        // Then
        assertEquals("restaurant-owner/vouchers/list", view);
        verify(model).addAttribute("errorMessage", "Bạn không có quyền truy cập nhà hàng này.");
    }

    @Test
    @DisplayName("shouldListVouchers_withException")
    void shouldListVouchers_withException() {
        // Given
        when(restaurantOwnerService.getRestaurantsByCurrentUser(authentication))
            .thenThrow(new RuntimeException("Database error"));

        // When
        String view = controller.listVouchers(0, 10, "createdAt", "desc", null, null, null, authentication, model);

        // Then
        assertEquals("restaurant-owner/vouchers/list", view);
        verify(model).addAttribute(eq("errorMessage"), anyString());
    }

    @Test
    @DisplayName("shouldShowCreateForm_withEmptyRestaurants_returnsError")
    void shouldShowCreateForm_withEmptyRestaurants_returnsError() {
        // Given
        when(restaurantOwnerService.getRestaurantsByCurrentUser(authentication)).thenReturn(new ArrayList<>());

        // When
        String view = controller.showCreateForm(null, authentication, model);

        // Then
        assertEquals("restaurant-owner/vouchers/form", view);
        verify(model).addAttribute("errorMessage", "Không tìm thấy nhà hàng nào của bạn. Vui lòng tạo nhà hàng trước.");
    }

    @Test
    @DisplayName("shouldShowCreateForm_withRestaurantIdProvided")
    void shouldShowCreateForm_withRestaurantIdProvided() {
        // Given
        List<RestaurantProfile> restaurants = new ArrayList<>();
        restaurants.add(restaurant);

        when(restaurantOwnerService.getRestaurantsByCurrentUser(authentication)).thenReturn(restaurants);

        // When
        String view = controller.showCreateForm(1, authentication, model);

        // Then
        assertEquals("restaurant-owner/vouchers/form", view);
        verify(model).addAttribute("selectedRestaurantId", 1);
    }

    @Test
    @DisplayName("shouldCreateVoucher_withValidationErrors")
    void shouldCreateVoucher_withValidationErrors() {
        // Given
        VoucherCreateForm form = new VoucherCreateForm();
        List<RestaurantProfile> restaurants = new ArrayList<>();
        restaurants.add(restaurant);

        when(bindingResult.hasErrors()).thenReturn(true);
        when(restaurantOwnerService.getRestaurantsByCurrentUser(authentication)).thenReturn(restaurants);

        // When
        String view = controller.createVoucher(form, bindingResult, null, authentication, redirectAttributes);

        // Then
        assertTrue(view.contains("redirect:/restaurant-owner/vouchers/new"));
    }

    @Test
    @DisplayName("shouldCreateVoucher_withEmptyRestaurants")
    void shouldCreateVoucher_withEmptyRestaurants() {
        // Given
        VoucherCreateForm form = new VoucherCreateForm();
        when(bindingResult.hasErrors()).thenReturn(false);
        when(restaurantOwnerService.getRestaurantsByCurrentUser(authentication)).thenReturn(new ArrayList<>());

        // When
        String view = controller.createVoucher(form, bindingResult, null, authentication, redirectAttributes);

        // Then
        assertTrue(view.contains("redirect:/restaurant-owner/vouchers"));
    }

    @Test
    @DisplayName("shouldCreateVoucher_withRestaurantNotOwned")
    void shouldCreateVoucher_withRestaurantNotOwned() {
        // Given
        VoucherCreateForm form = new VoucherCreateForm();
        List<RestaurantProfile> restaurants = new ArrayList<>();
        restaurants.add(restaurant);

        when(bindingResult.hasErrors()).thenReturn(false);
        when(restaurantOwnerService.getRestaurantsByCurrentUser(authentication)).thenReturn(restaurants);

        // When - Try to create voucher for restaurant ID 999 which owner doesn't own
        String view = controller.createVoucher(form, bindingResult, 999, authentication, redirectAttributes);

        // Then
        assertTrue(view.contains("redirect:/restaurant-owner/vouchers/new"));
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Bạn không có quyền tạo voucher cho nhà hàng này.");
    }

    @Test
    @DisplayName("shouldCreateVoucher_withException")
    void shouldCreateVoucher_withException() {
        // Given
        VoucherCreateForm form = new VoucherCreateForm();
        List<RestaurantProfile> restaurants = new ArrayList<>();
        restaurants.add(restaurant);

        when(bindingResult.hasErrors()).thenReturn(false);
        when(restaurantOwnerService.getRestaurantsByCurrentUser(authentication)).thenReturn(restaurants);
        when(voucherService.createRestaurantVoucher(eq(1), any()))
            .thenThrow(new RuntimeException("Database error"));

        // When
        String view = controller.createVoucher(form, bindingResult, null, authentication, redirectAttributes);

        // Then
        assertTrue(view.contains("redirect:/restaurant-owner/vouchers/new"));
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }

    @Test
    @DisplayName("shouldPauseVoucher_successfully")
    void shouldPauseVoucher_successfully() {
        // Given
        doNothing().when(voucherService).pauseVoucher(1);

        // When
        String view = controller.pauseVoucher(1, authentication, redirectAttributes);

        // Then
        assertTrue(view.contains("redirect:/restaurant-owner/vouchers"));
        verify(redirectAttributes).addFlashAttribute("successMessage", "Voucher paused successfully!");
    }

    @Test
    @DisplayName("shouldPauseVoucher_withException")
    void shouldPauseVoucher_withException() {
        // Given
        doThrow(new RuntimeException("Database error")).when(voucherService).pauseVoucher(1);

        // When
        String view = controller.pauseVoucher(1, authentication, redirectAttributes);

        // Then
        assertTrue(view.contains("redirect:/restaurant-owner/vouchers"));
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }

    @Test
    @DisplayName("shouldResumeVoucher_successfully")
    void shouldResumeVoucher_successfully() {
        // Given
        doNothing().when(voucherService).resumeVoucher(1);

        // When
        String view = controller.resumeVoucher(1, authentication, redirectAttributes);

        // Then
        assertTrue(view.contains("redirect:/restaurant-owner/vouchers"));
        verify(redirectAttributes).addFlashAttribute("successMessage", "Voucher resumed successfully!");
    }

    @Test
    @DisplayName("shouldResumeVoucher_withException")
    void shouldResumeVoucher_withException() {
        // Given
        doThrow(new RuntimeException("Database error")).when(voucherService).resumeVoucher(1);

        // When
        String view = controller.resumeVoucher(1, authentication, redirectAttributes);

        // Then
        assertTrue(view.contains("redirect:/restaurant-owner/vouchers"));
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }

    @Test
    @DisplayName("shouldExpireVoucher_successfully")
    void shouldExpireVoucher_successfully() {
        // Given
        doNothing().when(voucherService).expireVoucher(1);

        // When
        String view = controller.expireVoucher(1, authentication, redirectAttributes);

        // Then
        assertTrue(view.contains("redirect:/restaurant-owner/vouchers"));
        verify(redirectAttributes).addFlashAttribute("successMessage", "Voucher expired successfully!");
    }

    @Test
    @DisplayName("shouldExpireVoucher_withException")
    void shouldExpireVoucher_withException() {
        // Given
        doThrow(new RuntimeException("Database error")).when(voucherService).expireVoucher(1);

        // When
        String view = controller.expireVoucher(1, authentication, redirectAttributes);

        // Then
        assertTrue(view.contains("redirect:/restaurant-owner/vouchers"));
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }

    @Test
    @DisplayName("shouldViewVoucher_successfully")
    void shouldViewVoucher_successfully() {
        // Given
        List<RestaurantProfile> restaurants = new ArrayList<>();
        restaurants.add(restaurant);
        // Setup redemptions to make getUsedCount() return 5
        List<VoucherRedemption> redemptions = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            redemptions.add(new VoucherRedemption());
        }
        voucher.setRedemptions(redemptions);
        voucher.setGlobalUsageLimit(100);

        when(voucherService.getVoucherById(1)).thenReturn(voucher);
        when(restaurantOwnerService.getRestaurantsByCurrentUser(authentication)).thenReturn(restaurants);

        // When
        String view = controller.viewVoucher(1, null, authentication, model);

        // Then
        assertEquals("restaurant-owner/vouchers/detail", view);
        verify(model).addAttribute("voucher", voucher);
        verify(model).addAttribute("usedCount", 5);
        verify(model).addAttribute("totalLimit", 100);
    }

    @Test
    @DisplayName("shouldViewVoucher_withVoucherNotFound")
    void shouldViewVoucher_withVoucherNotFound() {
        // Given
        List<RestaurantProfile> restaurants = new ArrayList<>();
        restaurants.add(restaurant);

        when(voucherService.getVoucherById(1)).thenReturn(null);
        when(restaurantOwnerService.getRestaurantsByCurrentUser(authentication)).thenReturn(restaurants);

        // When
        String view = controller.viewVoucher(1, null, authentication, model);

        // Then
        assertEquals("restaurant-owner/vouchers/list", view);
        verify(model).addAttribute("errorMessage", "Voucher không tồn tại.");
    }

    @Test
    @DisplayName("shouldViewVoucher_withRestaurantNotOwned")
    void shouldViewVoucher_withRestaurantNotOwned() {
        // Given
        RestaurantProfile otherRestaurant = new RestaurantProfile();
        otherRestaurant.setRestaurantId(999);
        voucher.setRestaurant(otherRestaurant);

        List<RestaurantProfile> restaurants = new ArrayList<>();
        restaurants.add(restaurant); // Owner only owns restaurant ID 1

        when(voucherService.getVoucherById(1)).thenReturn(voucher);
        when(restaurantOwnerService.getRestaurantsByCurrentUser(authentication)).thenReturn(restaurants);

        // When
        String view = controller.viewVoucher(1, null, authentication, model);

        // Then
        assertEquals("restaurant-owner/vouchers/list", view);
        verify(model).addAttribute("errorMessage", "Bạn không có quyền xem voucher này.");
    }

    @Test
    @DisplayName("shouldViewVoucher_withNullGlobalUsageLimit")
    void shouldViewVoucher_withNullGlobalUsageLimit() {
        // Given
        List<RestaurantProfile> restaurants = new ArrayList<>();
        restaurants.add(restaurant);
        // Setup redemptions to make getUsedCount() return 5
        List<VoucherRedemption> redemptions = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            redemptions.add(new VoucherRedemption());
        }
        voucher.setRedemptions(redemptions);
        voucher.setGlobalUsageLimit(null); // null limit

        when(voucherService.getVoucherById(1)).thenReturn(voucher);
        when(restaurantOwnerService.getRestaurantsByCurrentUser(authentication)).thenReturn(restaurants);

        // When
        String view = controller.viewVoucher(1, null, authentication, model);

        // Then
        assertEquals("restaurant-owner/vouchers/detail", view);
        verify(model).addAttribute("totalLimit", 0);
        verify(model).addAttribute("usagePercentage", 0.0);
    }

    @Test
    @DisplayName("shouldViewVoucher_withEmptyRestaurants")
    void shouldViewVoucher_withEmptyRestaurants() {
        // Given
        when(restaurantOwnerService.getRestaurantsByCurrentUser(authentication)).thenReturn(new ArrayList<>());

        // When
        String view = controller.viewVoucher(1, null, authentication, model);

        // Then
        assertEquals("restaurant-owner/vouchers/list", view);
        verify(model).addAttribute("errorMessage", "Không tìm thấy nhà hàng nào của bạn.");
    }

    @Test
    @DisplayName("shouldShowEditForm_withVoucherNotFound")
    void shouldShowEditForm_withVoucherNotFound() {
        // Given
        List<RestaurantProfile> restaurants = new ArrayList<>();
        restaurants.add(restaurant);

        when(voucherService.getVoucherById(1)).thenReturn(null);
        when(restaurantOwnerService.getRestaurantsByCurrentUser(authentication)).thenReturn(restaurants);

        // When
        String view = controller.showEditForm(1, null, authentication, model);

        // Then
        assertEquals("restaurant-owner/vouchers/list", view);
        verify(model).addAttribute("errorMessage", "Voucher không tồn tại.");
    }

    @Test
    @DisplayName("shouldShowEditForm_withRestaurantNotOwned")
    void shouldShowEditForm_withRestaurantNotOwned() {
        // Given
        RestaurantProfile otherRestaurant = new RestaurantProfile();
        otherRestaurant.setRestaurantId(999);
        voucher.setRestaurant(otherRestaurant);

        List<RestaurantProfile> restaurants = new ArrayList<>();
        restaurants.add(restaurant);

        when(voucherService.getVoucherById(1)).thenReturn(voucher);
        when(restaurantOwnerService.getRestaurantsByCurrentUser(authentication)).thenReturn(restaurants);

        // When
        String view = controller.showEditForm(1, null, authentication, model);

        // Then
        assertEquals("restaurant-owner/vouchers/list", view);
        verify(model).addAttribute("errorMessage", "Bạn không có quyền chỉnh sửa voucher này.");
    }

    @Test
    @DisplayName("shouldUpdateVoucher_successfully")
    void shouldUpdateVoucher_successfully() {
        // Given
        List<RestaurantProfile> restaurants = new ArrayList<>();
        restaurants.add(restaurant);
        voucher.setDiscountType(DiscountType.PERCENT);

        when(voucherService.getVoucherById(1)).thenReturn(voucher);
        when(restaurantOwnerService.getRestaurantsByCurrentUser(authentication)).thenReturn(restaurants);
        when(voucherService.updateVoucher(eq(1), any(VoucherService.VoucherEditDto.class))).thenReturn(voucher);

        // When
        String view = controller.updateVoucher(1, "ACTIVE", "New description", 
            BigDecimal.valueOf(10), null, null, null, null, null, null, null, 
            authentication, redirectAttributes);

        // Then
        assertTrue(view.contains("redirect:/restaurant-owner/vouchers/1"));
        verify(redirectAttributes).addFlashAttribute("successMessage", "Voucher đã được cập nhật thành công!");
    }

    @Test
    @DisplayName("shouldUpdateVoucher_withNullStatus_returnsError")
    void shouldUpdateVoucher_withNullStatus_returnsError() {
        // Given
        List<RestaurantProfile> restaurants = new ArrayList<>();
        restaurants.add(restaurant);

        when(voucherService.getVoucherById(1)).thenReturn(voucher);
        when(restaurantOwnerService.getRestaurantsByCurrentUser(authentication)).thenReturn(restaurants);

        // When
        String view = controller.updateVoucher(1, null, null, null, null, null, null, null, null, null, null, 
            authentication, redirectAttributes);

        // Then
        assertTrue(view.contains("redirect:/restaurant-owner/vouchers/1/edit"));
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Trạng thái không được để trống.");
    }

    @Test
    @DisplayName("shouldUpdateVoucher_withEmptyStatus_returnsError")
    void shouldUpdateVoucher_withEmptyStatus_returnsError() {
        // Given
        List<RestaurantProfile> restaurants = new ArrayList<>();
        restaurants.add(restaurant);

        when(voucherService.getVoucherById(1)).thenReturn(voucher);
        when(restaurantOwnerService.getRestaurantsByCurrentUser(authentication)).thenReturn(restaurants);

        // When
        String view = controller.updateVoucher(1, "  ", null, null, null, null, null, null, null, null, null, 
            authentication, redirectAttributes);

        // Then
        assertTrue(view.contains("redirect:/restaurant-owner/vouchers/1/edit"));
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Trạng thái không được để trống.");
    }

    @Test
    @DisplayName("shouldUpdateVoucher_withDateStrings")
    void shouldUpdateVoucher_withDateStrings() {
        // Given
        List<RestaurantProfile> restaurants = new ArrayList<>();
        restaurants.add(restaurant);
        voucher.setDiscountType(DiscountType.PERCENT);
        voucher.setStartDate(LocalDate.of(2024, 1, 1));
        voucher.setEndDate(LocalDate.of(2024, 12, 31));

        when(voucherService.getVoucherById(1)).thenReturn(voucher);
        when(restaurantOwnerService.getRestaurantsByCurrentUser(authentication)).thenReturn(restaurants);
        when(voucherService.updateVoucher(eq(1), any(VoucherService.VoucherEditDto.class))).thenReturn(voucher);

        // When - All parameters must be provided in correct order
        String view = controller.updateVoucher(1, "ACTIVE", 
            null, // description
            null, // discountValue (BigDecimal)
            null, // minOrderAmount (BigDecimal)
            null, // maxDiscountAmount (BigDecimal)
            null, // globalUsageLimit (Integer)
            null, // perCustomerLimit (Integer)
            "2024-01-01", // startDate (String)
            "2024-12-31", // endDate (String)
            null, // restaurantId (Integer)
            authentication, redirectAttributes);

        // Then
        assertTrue(view.contains("redirect:/restaurant-owner/vouchers/1"));
    }

    @Test
    @DisplayName("shouldUpdateVoucher_withException")
    void shouldUpdateVoucher_withException() {
        // Given
        List<RestaurantProfile> restaurants = new ArrayList<>();
        restaurants.add(restaurant);
        voucher.setDiscountType(DiscountType.PERCENT);

        when(voucherService.getVoucherById(1)).thenReturn(voucher);
        when(restaurantOwnerService.getRestaurantsByCurrentUser(authentication)).thenReturn(restaurants);
        when(voucherService.updateVoucher(eq(1), any(VoucherService.VoucherEditDto.class)))
            .thenThrow(new RuntimeException("Database error"));

        // When
        String view = controller.updateVoucher(1, "ACTIVE", null, null, null, null, null, null, null, null, null, 
            authentication, redirectAttributes);

        // Then
        assertTrue(view.contains("redirect:/restaurant-owner/vouchers/1/edit"));
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }

    @Test
    @DisplayName("shouldDeleteVoucher_successfully")
    void shouldDeleteVoucher_successfully() {
        // Given
        List<RestaurantProfile> restaurants = new ArrayList<>();
        restaurants.add(restaurant);

        when(voucherService.getVoucherById(1)).thenReturn(voucher);
        when(restaurantOwnerService.getRestaurantsByCurrentUser(authentication)).thenReturn(restaurants);
        doNothing().when(voucherService).deleteVoucher(1);

        // When
        String view = controller.deleteVoucher(1, null, authentication, redirectAttributes);

        // Then
        assertTrue(view.contains("redirect:/restaurant-owner/vouchers"));
        verify(redirectAttributes).addFlashAttribute("successMessage", "Voucher đã được xóa thành công!");
    }

    @Test
    @DisplayName("shouldDeleteVoucher_withVoucherNotFound")
    void shouldDeleteVoucher_withVoucherNotFound() {
        // Given
        List<RestaurantProfile> restaurants = new ArrayList<>();
        restaurants.add(restaurant);

        when(voucherService.getVoucherById(1)).thenReturn(null);
        when(restaurantOwnerService.getRestaurantsByCurrentUser(authentication)).thenReturn(restaurants);

        // When
        String view = controller.deleteVoucher(1, null, authentication, redirectAttributes);

        // Then
        assertTrue(view.contains("redirect:/restaurant-owner/vouchers"));
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Voucher không tồn tại.");
    }

    @Test
    @DisplayName("shouldDeleteVoucher_withRestaurantNotOwned")
    void shouldDeleteVoucher_withRestaurantNotOwned() {
        // Given
        RestaurantProfile otherRestaurant = new RestaurantProfile();
        otherRestaurant.setRestaurantId(999);
        voucher.setRestaurant(otherRestaurant);

        List<RestaurantProfile> restaurants = new ArrayList<>();
        restaurants.add(restaurant);

        when(voucherService.getVoucherById(1)).thenReturn(voucher);
        when(restaurantOwnerService.getRestaurantsByCurrentUser(authentication)).thenReturn(restaurants);

        // When
        String view = controller.deleteVoucher(1, null, authentication, redirectAttributes);

        // Then
        assertTrue(view.contains("redirect:/restaurant-owner/vouchers"));
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Bạn không có quyền xóa voucher này.");
    }

    @Test
    @DisplayName("shouldDeleteVoucher_withException")
    void shouldDeleteVoucher_withException() {
        // Given
        List<RestaurantProfile> restaurants = new ArrayList<>();
        restaurants.add(restaurant);

        when(voucherService.getVoucherById(1)).thenReturn(voucher);
        when(restaurantOwnerService.getRestaurantsByCurrentUser(authentication)).thenReturn(restaurants);
        doThrow(new RuntimeException("Database error")).when(voucherService).deleteVoucher(1);

        // When
        String view = controller.deleteVoucher(1, null, authentication, redirectAttributes);

        // Then
        assertTrue(view.contains("redirect:/restaurant-owner/vouchers"));
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }

    @Test
    @DisplayName("shouldDebugVouchers_successfully")
    void shouldDebugVouchers_successfully() {
        // Given
        List<Voucher> vouchers = new ArrayList<>();
        vouchers.add(voucher);
        when(voucherService.getVouchersByRestaurant(16)).thenReturn(vouchers);

        // When
        String view = controller.debugVouchers(model);

        // Then
        assertEquals("restaurant-owner/vouchers/list", view);
        verify(model).addAttribute("vouchers", vouchers);
        verify(model).addAttribute("debugMode", true);
    }

    @Test
    @DisplayName("shouldDebugVouchers_withException")
    void shouldDebugVouchers_withException() {
        // Given
        when(voucherService.getVouchersByRestaurant(16))
            .thenThrow(new RuntimeException("Database error"));

        // When
        String view = controller.debugVouchers(model);

        // Then
        assertEquals("restaurant-owner/vouchers/list", view);
        verify(model).addAttribute(eq("errorMessage"), anyString());
        verify(model).addAttribute("vouchers", java.util.List.of());
    }

    @Test
    @DisplayName("shouldTestSimple_successfully")
    void shouldTestSimple_successfully() {
        // Given
        when(authentication.getPrincipal()).thenReturn(owner);
        List<Voucher> vouchers = new ArrayList<>();
        vouchers.add(voucher);
        when(voucherService.getAllVouchers()).thenReturn(vouchers);

        // When
        String view = controller.testSimple(authentication, model);

        // Then
        assertEquals("restaurant-owner/vouchers/test_simple", view);
        verify(model).addAttribute("vouchers", vouchers);
    }

    @Test
    @DisplayName("shouldTestSimple_withNullPrincipal")
    void shouldTestSimple_withNullPrincipal() {
        // Given
        when(authentication.getPrincipal()).thenReturn(null);

        // When
        String view = controller.testSimple(authentication, model);

        // Then
        assertTrue(view.contains("redirect:/login"));
    }

    @Test
    @DisplayName("shouldTestSimple_withException")
    void shouldTestSimple_withException() {
        // Given
        when(authentication.getPrincipal()).thenReturn(owner);
        when(voucherService.getAllVouchers())
            .thenThrow(new RuntimeException("Database error"));

        // When
        String view = controller.testSimple(authentication, model);

        // Then
        assertEquals("error/500", view);
    }
}

