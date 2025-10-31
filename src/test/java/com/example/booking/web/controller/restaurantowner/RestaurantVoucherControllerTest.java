package com.example.booking.web.controller.restaurantowner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import com.example.booking.dto.admin.VoucherCreateForm;
import com.example.booking.dto.admin.VoucherEditForm;
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
}

