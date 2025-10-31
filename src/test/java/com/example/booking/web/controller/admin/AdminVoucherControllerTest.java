package com.example.booking.web.controller.admin;

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
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booking.domain.Voucher;
import com.example.booking.domain.VoucherStatus;
import com.example.booking.dto.admin.VoucherCreateForm;
import com.example.booking.dto.admin.VoucherEditForm;
import com.example.booking.dto.admin.VoucherAssignForm;
import com.example.booking.service.VoucherService;
import com.example.booking.service.CustomerService;
import com.example.booking.domain.Customer;

/**
 * Unit tests for AdminVoucherController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminVoucherController Tests")
public class AdminVoucherControllerTest {

    @Mock
    private VoucherService voucherService;

    @Mock
    private CustomerService customerService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private AdminVoucherController controller;

    private Voucher voucher;

    @BeforeEach
    void setUp() {
        voucher = new Voucher();
        voucher.setVoucherId(1);
        voucher.setCode("TEST2024");
        voucher.setStatus(VoucherStatus.ACTIVE);
    }

    // ========== listVouchers() Tests ==========

    @Test
    @DisplayName("shouldListVouchers_successfully")
    void shouldListVouchers_successfully() {
        // Given
        List<Voucher> vouchers = new ArrayList<>();
        vouchers.add(voucher);

        when(voucherService.getAllVouchers()).thenReturn(vouchers);

        // When
        String view = controller.listVouchers(0, 10, "createdAt", "desc", null, null, model);

        // Then
        assertEquals("admin/vouchers/list", view);
        verify(model, times(1)).addAttribute(eq("vouchers"), any());
    }

    @Test
    @DisplayName("shouldFilterVouchersBySearch_successfully")
    void shouldFilterVouchersBySearch_successfully() {
        // Given
        List<Voucher> vouchers = new ArrayList<>();
        vouchers.add(voucher);

        when(voucherService.getAllVouchers()).thenReturn(vouchers);

        // When
        String view = controller.listVouchers(0, 10, "createdAt", "desc", "TEST", null, model);

        // Then
        assertEquals("admin/vouchers/list", view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    // ========== createForm() Tests ==========

    @Test
    @DisplayName("shouldShowCreateForm_successfully")
    void shouldShowCreateForm_successfully() {
        // When
        String view = controller.showCreateForm(model);

        // Then
        assertEquals("admin/vouchers/form", view);
        verify(model, times(1)).addAttribute(eq("voucherForm"), any(VoucherCreateForm.class));
    }

    // ========== createVoucher() Tests ==========

    @Test
    @DisplayName("shouldCreateVoucher_successfully")
    void shouldCreateVoucher_successfully() {
        // Given
        VoucherCreateForm form = new VoucherCreateForm();
        form.setCode("NEW2024");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(voucherService.createAdminVoucher(any())).thenReturn(voucher);

        // When
        String view = controller.createVoucher(form, bindingResult, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/vouchers", view);
        verify(voucherService, times(1)).createAdminVoucher(any());
    }

    @Test
    @DisplayName("shouldReturnForm_whenValidationErrors")
    void shouldReturnForm_whenValidationErrors() {
        // Given
        VoucherCreateForm form = new VoucherCreateForm();

        when(bindingResult.hasErrors()).thenReturn(true);

        // When
        String view = controller.createVoucher(form, bindingResult, redirectAttributes);

        // Then
        assertEquals("admin/vouchers/form", view);
        verify(voucherService, never()).createAdminVoucher(any());
    }

    // ========== showEditForm() Tests ==========

    @Test
    @DisplayName("shouldShowEditForm_successfully")
    void shouldShowEditForm_successfully() {
        // Given
        when(voucherService.getVoucherById(1)).thenReturn(voucher);

        // When
        String view = controller.showEditForm(1, model);

        // Then
        assertEquals("admin/vouchers/form-edit", view);
        verify(model, times(1)).addAttribute(eq("voucherForm"), any(VoucherCreateForm.class));
    }
}

