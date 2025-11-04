package com.example.booking.web.controller.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
import com.example.booking.domain.DiscountType;
import com.example.booking.domain.Customer;
import com.example.booking.dto.admin.VoucherCreateForm;
import com.example.booking.dto.admin.VoucherAssignForm;
import com.example.booking.service.VoucherService;
import com.example.booking.service.CustomerService;

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
        assertEquals("admin/vouchers/list-with-datetime", view);
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
        assertEquals("admin/vouchers/list-with-datetime", view);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    // ========== createForm() Tests ==========

    @Test
    @DisplayName("shouldShowCreateForm_successfully")
    void shouldShowCreateForm_successfully() {
        // When
        String view = controller.showCreateForm(model);

        // Then
        assertEquals("admin/vouchers/form-create", view);
        verify(model, times(1)).addAttribute(eq("voucherForm"), any(VoucherCreateForm.class));
        verify(model, times(1)).addAttribute("statuses", VoucherStatus.values());
        verify(model, times(1)).addAttribute("discountTypes", DiscountType.values());
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

    @Test
    @DisplayName("shouldHandleError_whenCreateVoucherFails")
    void shouldHandleError_whenCreateVoucherFails() {
        // Given
        VoucherCreateForm form = new VoucherCreateForm();
        form.setCode("NEW2024");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(voucherService.createAdminVoucher(any())).thenThrow(new RuntimeException("Service error"));

        // When
        String view = controller.createVoucher(form, bindingResult, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/vouchers/new", view);
        verify(redirectAttributes, times(1)).addFlashAttribute("errorMessage", anyString());
    }

    // ========== showEditForm() Tests ==========

    @Test
    @DisplayName("shouldShowEditForm_successfully")
    void shouldShowEditForm_successfully() {
        // Given
        voucher.setCreatedAt(LocalDateTime.now());
        voucher.setDescription("Test Description");
        voucher.setDiscountType(com.example.booking.domain.DiscountType.FIXED);
        voucher.setDiscountValue(new BigDecimal("50000"));
        when(voucherService.getVoucherById(1)).thenReturn(voucher);

        // When
        String view = controller.showEditForm(1, model);

        // Then
        assertEquals("admin/vouchers/form-edit", view);
        verify(model, times(1)).addAttribute(eq("voucherForm"), any(VoucherCreateForm.class));
        verify(model, times(1)).addAttribute("statuses", VoucherStatus.values());
        verify(model, times(1)).addAttribute("discountTypes", DiscountType.values());
        verify(model, times(1)).addAttribute("voucherId", 1);
    }

    @Test
    @DisplayName("shouldRedirect_whenEditFormVoucherNotFound")
    void shouldRedirect_whenEditFormVoucherNotFound() {
        // Given
        when(voucherService.getVoucherById(999)).thenThrow(new RuntimeException("Not found"));

        // When
        String view = controller.showEditForm(999, model);

        // Then
        assertEquals("redirect:/admin/vouchers", view);
    }

    // ========== updateVoucher() Tests ==========

    @Test
    @DisplayName("shouldUpdateVoucher_successfully")
    void shouldUpdateVoucher_successfully() {
        // Given
        VoucherCreateForm form = new VoucherCreateForm();
        form.setCode("UPDATED2024");
        form.setDescription("Updated Description");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(voucherService.getVoucherById(1)).thenReturn(voucher);
        when(voucherService.updateVoucher(eq(1), any(VoucherService.VoucherCreateDto.class))).thenReturn(voucher);

        // When
        String view = controller.updateVoucher(1, form, bindingResult, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/vouchers", view);
        verify(voucherService, times(1)).updateVoucher(eq(1), any(VoucherService.VoucherCreateDto.class));
        verify(redirectAttributes, times(1)).addFlashAttribute("successMessage", anyString());
    }

    @Test
    @DisplayName("shouldReturnForm_whenUpdateVoucherHasValidationErrors")
    void shouldReturnForm_whenUpdateVoucherHasValidationErrors() {
        // Given
        VoucherCreateForm form = new VoucherCreateForm();
        when(bindingResult.hasErrors()).thenReturn(true);
        when(voucherService.getVoucherById(1)).thenReturn(voucher);

        // When
        String view = controller.updateVoucher(1, form, bindingResult, redirectAttributes);

        // Then
        assertEquals("admin/vouchers/form-edit", view);
        verify(voucherService, never()).updateVoucher(eq(1), any(VoucherService.VoucherCreateDto.class));
    }

    @Test
    @DisplayName("shouldHandleError_whenUpdateVoucherFails")
    void shouldHandleError_whenUpdateVoucherFails() {
        // Given
        VoucherCreateForm form = new VoucherCreateForm();
        form.setCode("UPDATED2024");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(voucherService.updateVoucher(eq(1), any(VoucherService.VoucherCreateDto.class)))
            .thenThrow(new RuntimeException("Service error"));

        // When
        String view = controller.updateVoucher(1, form, bindingResult, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/vouchers/1/edit", view);
        verify(redirectAttributes, times(1)).addFlashAttribute("errorMessage", anyString());
    }

    // ========== deleteVoucher() Tests ==========

    @Test
    @DisplayName("shouldDeleteVoucher_successfully")
    void shouldDeleteVoucher_successfully() {
        // Given
        doNothing().when(voucherService).deleteVoucher(1);

        // When
        String view = controller.deleteVoucher(1, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/vouchers", view);
        verify(voucherService, times(1)).deleteVoucher(1);
        verify(redirectAttributes, times(1)).addFlashAttribute("successMessage", anyString());
    }

    @Test
    @DisplayName("shouldHandleError_whenDeleteVoucherFails")
    void shouldHandleError_whenDeleteVoucherFails() {
        // Given
        doThrow(new RuntimeException("Service error")).when(voucherService).deleteVoucher(1);

        // When
        String view = controller.deleteVoucher(1, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/vouchers", view);
        verify(redirectAttributes, times(1)).addFlashAttribute("errorMessage", anyString());
    }

    // ========== pauseVoucher() Tests ==========

    @Test
    @DisplayName("shouldPauseVoucher_successfully")
    void shouldPauseVoucher_successfully() {
        // Given
        doNothing().when(voucherService).pauseVoucher(1);

        // When
        String view = controller.pauseVoucher(1, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/vouchers", view);
        verify(voucherService, times(1)).pauseVoucher(1);
        verify(redirectAttributes, times(1)).addFlashAttribute("successMessage", anyString());
    }

    @Test
    @DisplayName("shouldHandleError_whenPauseVoucherFails")
    void shouldHandleError_whenPauseVoucherFails() {
        // Given
        doThrow(new RuntimeException("Service error")).when(voucherService).pauseVoucher(1);

        // When
        String view = controller.pauseVoucher(1, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/vouchers", view);
        verify(redirectAttributes, times(1)).addFlashAttribute("errorMessage", anyString());
    }

    // ========== resumeVoucher() Tests ==========

    @Test
    @DisplayName("shouldResumeVoucher_successfully")
    void shouldResumeVoucher_successfully() {
        // Given
        doNothing().when(voucherService).resumeVoucher(1);

        // When
        String view = controller.resumeVoucher(1, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/vouchers", view);
        verify(voucherService, times(1)).resumeVoucher(1);
        verify(redirectAttributes, times(1)).addFlashAttribute("successMessage", anyString());
    }

    @Test
    @DisplayName("shouldHandleError_whenResumeVoucherFails")
    void shouldHandleError_whenResumeVoucherFails() {
        // Given
        doThrow(new RuntimeException("Service error")).when(voucherService).resumeVoucher(1);

        // When
        String view = controller.resumeVoucher(1, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/vouchers", view);
        verify(redirectAttributes, times(1)).addFlashAttribute("errorMessage", anyString());
    }

    // ========== expireVoucher() Tests ==========

    @Test
    @DisplayName("shouldExpireVoucher_successfully")
    void shouldExpireVoucher_successfully() {
        // Given
        doNothing().when(voucherService).expireVoucher(1);

        // When
        String view = controller.expireVoucher(1, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/vouchers", view);
        verify(voucherService, times(1)).expireVoucher(1);
        verify(redirectAttributes, times(1)).addFlashAttribute("successMessage", anyString());
    }

    @Test
    @DisplayName("shouldHandleError_whenExpireVoucherFails")
    void shouldHandleError_whenExpireVoucherFails() {
        // Given
        doThrow(new RuntimeException("Service error")).when(voucherService).expireVoucher(1);

        // When
        String view = controller.expireVoucher(1, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/vouchers", view);
        verify(redirectAttributes, times(1)).addFlashAttribute("errorMessage", anyString());
    }

    // ========== viewVoucher() Tests ==========

    @Test
    @DisplayName("shouldViewVoucher_successfully")
    void shouldViewVoucher_successfully() {
        // Given
        voucher.setGlobalUsageLimit(100);
        when(voucherService.getVoucherById(1)).thenReturn(voucher);
        when(voucherService.countRedemptionsByVoucherId(1)).thenReturn(50L);

        // When
        String view = controller.viewVoucher(1, model);

        // Then
        assertEquals("admin/vouchers/detail", view);
        verify(model, times(1)).addAttribute("voucher", voucher);
        verify(model, times(1)).addAttribute("voucherId", 1);
        verify(model, times(1)).addAttribute("usageCount", 50L);
        verify(model, times(1)).addAttribute("remainingUses", "50");
        verify(model, times(1)).addAttribute("usageRate", anyString());
    }

    @Test
    @DisplayName("shouldViewVoucher_withUnlimitedUses")
    void shouldViewVoucher_withUnlimitedUses() {
        // Given
        voucher.setGlobalUsageLimit(null);
        when(voucherService.getVoucherById(1)).thenReturn(voucher);
        when(voucherService.countRedemptionsByVoucherId(1)).thenReturn(50L);

        // When
        String view = controller.viewVoucher(1, model);

        // Then
        assertEquals("admin/vouchers/detail", view);
        verify(model, times(1)).addAttribute("remainingUses", "Unlimited");
    }

    @Test
    @DisplayName("shouldRedirect_whenViewVoucherNotFound")
    void shouldRedirect_whenViewVoucherNotFound() {
        // Given
        when(voucherService.getVoucherById(999)).thenThrow(new RuntimeException("Not found"));

        // When
        String view = controller.viewVoucher(999, model);

        // Then
        assertEquals("redirect:/admin/vouchers", view);
    }

    // ========== showAssignForm() Tests ==========

    @Test
    @DisplayName("shouldShowAssignForm_successfully")
    void shouldShowAssignForm_successfully() {
        // Given
        List<Customer> customers = Arrays.asList(new Customer(), new Customer());
        when(customerService.findAllCustomers()).thenReturn(customers);

        // When
        String view = controller.showAssignForm(1, model);

        // Then
        assertEquals("admin/vouchers/assign", view);
        verify(model, times(1)).addAttribute(eq("assignForm"), any(VoucherAssignForm.class));
        verify(model, times(1)).addAttribute("voucherId", 1);
        verify(model, times(1)).addAttribute("customers", customers);
    }

    // ========== assignVoucher() Tests ==========

    @Test
    @DisplayName("shouldAssignVoucher_successfully")
    void shouldAssignVoucher_successfully() {
        // Given
        VoucherAssignForm form = new VoucherAssignForm();
        List<UUID> customerIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        form.setCustomerIds(customerIds);

        doNothing().when(voucherService).assignVoucherToCustomers(1, customerIds);

        // When
        String view = controller.assignVoucher(1, form, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/vouchers/1", view);
        verify(voucherService, times(1)).assignVoucherToCustomers(1, customerIds);
        verify(redirectAttributes, times(1)).addFlashAttribute("successMessage", anyString());
    }

    @Test
    @DisplayName("shouldHandleError_whenAssignVoucherFails")
    void shouldHandleError_whenAssignVoucherFails() {
        // Given
        VoucherAssignForm form = new VoucherAssignForm();
        form.setCustomerIds(Arrays.asList(UUID.randomUUID()));

        doThrow(new RuntimeException("Service error")).when(voucherService)
            .assignVoucherToCustomers(1, form.getCustomerIds());

        // When
        String view = controller.assignVoucher(1, form, redirectAttributes);

        // Then
        assertEquals("redirect:/admin/vouchers/1/assign", view);
        verify(redirectAttributes, times(1)).addFlashAttribute("errorMessage", anyString());
    }

    // ========== listVouchers() Additional Tests ==========

    @Test
    @DisplayName("shouldFilterVouchersByStatus_successfully")
    void shouldFilterVouchersByStatus_successfully() {
        // Given
        List<Voucher> vouchers = Arrays.asList(voucher);
        when(voucherService.getAllVouchers()).thenReturn(vouchers);
        when(voucherService.countRedemptionsByVoucherId(anyInt())).thenReturn(0L);

        // When
        String view = controller.listVouchers(0, 10, "createdAt", "desc", null, "ACTIVE", model);

        // Then
        assertEquals("admin/vouchers/list-with-datetime", view);
        verify(model, times(1)).addAttribute("status", "ACTIVE");
    }

    @Test
    @DisplayName("shouldSortVouchersByCode_successfully")
    void shouldSortVouchersByCode_successfully() {
        // Given
        Voucher voucher2 = new Voucher();
        voucher2.setVoucherId(2);
        voucher2.setCode("ABC2024");
        voucher2.setCreatedAt(LocalDateTime.now().minusDays(1));
        
        List<Voucher> vouchers = Arrays.asList(voucher, voucher2);
        when(voucherService.getAllVouchers()).thenReturn(vouchers);
        when(voucherService.countRedemptionsByVoucherId(anyInt())).thenReturn(0L);

        // When
        String view = controller.listVouchers(0, 10, "code", "asc", null, null, model);

        // Then
        assertEquals("admin/vouchers/list-with-datetime", view);
        verify(model, times(1)).addAttribute("sortBy", "code");
        verify(model, times(1)).addAttribute("sortDir", "asc");
    }
}

