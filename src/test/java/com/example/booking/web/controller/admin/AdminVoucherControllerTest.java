package com.example.booking.web.controller.admin;

import com.example.booking.domain.DiscountType;
import com.example.booking.domain.Voucher;
import com.example.booking.domain.VoucherStatus;
import com.example.booking.dto.admin.VoucherCreateForm;
import com.example.booking.service.VoucherService;
import com.example.booking.service.CustomerService;
import com.example.booking.service.EndpointRateLimitingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminVoucherController.class)
@DisplayName("AdminVoucherController Tests")
class AdminVoucherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VoucherService voucherService;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private EndpointRateLimitingService endpointRateLimitingService;

    private Voucher mockVoucher;
    private VoucherCreateForm mockForm;
    private List<Voucher> mockVoucherList;

    @BeforeEach
    void setUp() {
        // Setup mock voucher
        mockVoucher = new Voucher();
        mockVoucher.setVoucherId(1);
        mockVoucher.setCode("TEST2024");
        mockVoucher.setDescription("Test Voucher");
        mockVoucher.setDiscountType(DiscountType.PERCENT);
        mockVoucher.setDiscountValue(BigDecimal.valueOf(10));
        mockVoucher.setStartDate(LocalDate.now());
        mockVoucher.setEndDate(LocalDate.now().plusDays(30));
        mockVoucher.setGlobalUsageLimit(100);
        mockVoucher.setPerCustomerLimit(1);
        mockVoucher.setMinOrderAmount(BigDecimal.valueOf(100000));
        mockVoucher.setMaxDiscountAmount(BigDecimal.valueOf(50000));
        mockVoucher.setStatus(VoucherStatus.ACTIVE);
        mockVoucher.setCreatedAt(LocalDateTime.now());

        // Setup mock form
        mockForm = new VoucherCreateForm();
        mockForm.setCode("NEW2024");
        mockForm.setDescription("New Voucher");
        mockForm.setDiscountType("PERCENT");
        mockForm.setDiscountValue(BigDecimal.valueOf(20));
        mockForm.setStartDate(LocalDate.now());
        mockForm.setEndDate(LocalDate.now().plusDays(60));
        mockForm.setGlobalUsageLimit(200);
        mockForm.setPerCustomerLimit(2);
        mockForm.setMinOrderAmount(BigDecimal.valueOf(50000));
        mockForm.setMaxDiscountAmount(BigDecimal.valueOf(30000));
        mockForm.setStatus(VoucherStatus.ACTIVE);

        // Setup mock list
        mockVoucherList = new ArrayList<>();
        mockVoucherList.add(mockVoucher);
    }

    // Test TC AVC-001: List vouchers with default pagination
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("TC AVC-001: Should list vouchers with default pagination")
    void shouldListVouchersWithDefaultPagination() throws Exception {
        when(voucherService.getAllVouchers()).thenReturn(mockVoucherList);
        when(voucherService.countRedemptionsByVoucherId(anyInt())).thenReturn(5L);

        mockMvc.perform(get("/admin/vouchers"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/vouchers/list-with-datetime"))
                .andExpect(model().attributeExists("vouchers"))
                .andExpect(model().attributeExists("currentPage"))
                .andExpect(model().attributeExists("totalPages"))
                .andExpect(model().attributeExists("voucherUsageMap"));

        verify(voucherService, times(1)).getAllVouchers();
        verify(voucherService, times(mockVoucherList.size())).countRedemptionsByVoucherId(anyInt());
    }

    // Test TC AVC-002: List vouchers with search filter
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("TC AVC-002: Should filter vouchers by search term")
    void shouldFilterVouchersBySearchTerm() throws Exception {
        when(voucherService.getAllVouchers()).thenReturn(mockVoucherList);
        when(voucherService.countRedemptionsByVoucherId(anyInt())).thenReturn(0L);

        mockMvc.perform(get("/admin/vouchers")
                .param("search", "TEST2024"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/vouchers/list-with-datetime"))
                .andExpect(model().attributeExists("search"));

        verify(voucherService, times(1)).getAllVouchers();
    }

    // Test TC AVC-003: List vouchers with status filter
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("TC AVC-003: Should filter vouchers by status")
    void shouldFilterVouchersByStatus() throws Exception {
        when(voucherService.getAllVouchers()).thenReturn(mockVoucherList);
        when(voucherService.countRedemptionsByVoucherId(anyInt())).thenReturn(0L);

        mockMvc.perform(get("/admin/vouchers")
                .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/vouchers/list-with-datetime"));

        verify(voucherService, times(1)).getAllVouchers();
    }

    // Test TC AVC-004: Show create form
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("TC AVC-004: Should show create form with status and discount types")
    void shouldShowCreateForm() throws Exception {
        mockMvc.perform(get("/admin/vouchers/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/vouchers/form-create"))
                .andExpect(model().attributeExists("voucherForm"))
                .andExpect(model().attributeExists("statuses"))
                .andExpect(model().attributeExists("discountTypes"));
    }

    // Test TC AVC-005: Create voucher successfully
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("TC AVC-005: Should create voucher successfully")
    void shouldCreateVoucherSuccessfully() throws Exception {
        when(voucherService.createAdminVoucher(any())).thenReturn(mockVoucher);

        mockMvc.perform(post("/admin/vouchers/new")
                .param("code", "NEW2024")
                .param("description", "New Voucher")
                .param("discountType", "PERCENT")
                .param("discountValue", "20")
                .param("startDate", LocalDate.now().toString())
                .param("endDate", LocalDate.now().plusDays(60).toString())
                .param("globalUsageLimit", "200")
                .param("perCustomerLimit", "2")
                .param("minOrderAmount", "50000")
                .param("maxDiscountAmount", "30000")
                .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/vouchers"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(voucherService, times(1)).createAdminVoucher(any(VoucherService.VoucherCreateDto.class));
    }

    // Test TC AVC-006: Create voucher with validation errors
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("TC AVC-006: Should redirect with error when validation fails")
    void shouldHandleValidationErrors() throws Exception {
        mockMvc.perform(post("/admin/vouchers/new")
                .param("code", "")  // Empty code - validation should fail
                .param("discountType", "PERCENT")
                .param("discountValue", "20"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/vouchers/form"));

        verify(voucherService, never()).createAdminVoucher(any());
    }

    // Test TC AVC-007: Show edit form
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("TC AVC-007: Should show edit form with voucher data")
    void shouldShowEditForm() throws Exception {
        when(voucherService.getVoucherById(1)).thenReturn(mockVoucher);

        mockMvc.perform(get("/admin/vouchers/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/vouchers/form-edit"))
                .andExpect(model().attributeExists("voucherForm"))
                .andExpect(model().attributeExists("statuses"))
                .andExpect(model().attributeExists("discountTypes"))
                .andExpect(model().attribute("voucherId", 1));

        verify(voucherService, times(1)).getVoucherById(1);
    }

    // Test TC AVC-008: Update voucher successfully
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("TC AVC-008: Should update voucher successfully")
    void shouldUpdateVoucherSuccessfully() throws Exception {
        when(voucherService.getVoucherById(1)).thenReturn(mockVoucher);

        mockMvc.perform(post("/admin/vouchers/1/edit")
                .param("code", "UPDATED2024")
                .param("description", "Updated Voucher")
                .param("discountType", "FIXED")
                .param("discountValue", "50000")
                .param("startDate", LocalDate.now().toString())
                .param("endDate", LocalDate.now().plusDays(90).toString())
                .param("globalUsageLimit", "300")
                .param("perCustomerLimit", "3")
                .param("minOrderAmount", "100000")
                .param("maxDiscountAmount", "70000")
                .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/vouchers"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(voucherService, times(1)).updateVoucher(eq(1), any(VoucherService.VoucherCreateDto.class));
    }

    // Test TC AVC-009: View voucher details
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("TC AVC-009: Should view voucher details with usage statistics")
    void shouldViewVoucherDetails() throws Exception {
        when(voucherService.getVoucherById(1)).thenReturn(mockVoucher);
        when(voucherService.countRedemptionsByVoucherId(1)).thenReturn(15L);

        mockMvc.perform(get("/admin/vouchers/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/vouchers/detail"))
                .andExpect(model().attributeExists("voucher"))
                .andExpect(model().attributeExists("usageCount"))
                .andExpect(model().attributeExists("remainingUses"))
                .andExpect(model().attributeExists("usageRate"));

        verify(voucherService, times(1)).getVoucherById(1);
        verify(voucherService, times(1)).countRedemptionsByVoucherId(1);
    }

    // Test TC AVC-010: Pause voucher
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("TC AVC-010: Should pause voucher successfully")
    void shouldPauseVoucher() throws Exception {
        doNothing().when(voucherService).pauseVoucher(1);

        mockMvc.perform(post("/admin/vouchers/1/pause"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/vouchers"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(voucherService, times(1)).pauseVoucher(1);
    }

    // Test TC AVC-011: Resume voucher
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("TC AVC-011: Should resume voucher successfully")
    void shouldResumeVoucher() throws Exception {
        doNothing().when(voucherService).resumeVoucher(1);

        mockMvc.perform(post("/admin/vouchers/1/resume"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/vouchers"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(voucherService, times(1)).resumeVoucher(1);
    }

    // Test TC AVC-012: Expire voucher
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("TC AVC-012: Should expire voucher successfully")
    void shouldExpireVoucher() throws Exception {
        doNothing().when(voucherService).expireVoucher(1);

        mockMvc.perform(post("/admin/vouchers/1/expire"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/vouchers"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(voucherService, times(1)).expireVoucher(1);
    }

    // Test TC AVC-013: Delete voucher
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("TC AVC-013: Should delete voucher successfully")
    void shouldDeleteVoucher() throws Exception {
        doNothing().when(voucherService).deleteVoucher(1);

        mockMvc.perform(post("/admin/vouchers/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/vouchers"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(voucherService, times(1)).deleteVoucher(1);
    }

    // Test TC AVC-014: Pause voucher with exception
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("TC AVC-014: Should handle exception when pausing fails")
    void shouldHandlePauseException() throws Exception {
        doThrow(new RuntimeException("Pause failed")).when(voucherService).pauseVoucher(1);

        mockMvc.perform(post("/admin/vouchers/1/pause"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/vouchers"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(voucherService, times(1)).pauseVoucher(1);
    }

    // Test TC AVC-015: Create voucher with exception
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("TC AVC-015: Should handle exception when creating fails")
    void shouldHandleCreateException() throws Exception {
        when(voucherService.createAdminVoucher(any())).thenThrow(new RuntimeException("Create failed"));

        mockMvc.perform(post("/admin/vouchers/new")
                .param("code", "NEW2024")
                .param("description", "New Voucher")
                .param("discountType", "PERCENT")
                .param("discountValue", "20")
                .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/vouchers/new"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(voucherService, times(1)).createAdminVoucher(any());
    }

    // Test TC AVC-016: Access denied for non-admin
    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("TC AVC-016: Should deny access for non-admin users")
    void shouldDenyAccessForNonAdmin() throws Exception {
        mockMvc.perform(get("/admin/vouchers"))
                .andExpect(status().isForbidden());

        verify(voucherService, never()).getAllVouchers();
    }

    // Test TC AVC-017: View voucher - not found
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("TC AVC-017: Should redirect when voucher not found")
    void shouldRedirectWhenVoucherNotFound() throws Exception {
        when(voucherService.getVoucherById(999)).thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(get("/admin/vouchers/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/vouchers"));

        verify(voucherService, times(1)).getVoucherById(999);
    }

    // Test TC AVC-018: Sort vouchers by code
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("TC AVC-018: Should sort vouchers by code")
    void shouldSortVouchersByCode() throws Exception {
        when(voucherService.getAllVouchers()).thenReturn(mockVoucherList);
        when(voucherService.countRedemptionsByVoucherId(anyInt())).thenReturn(0L);

        mockMvc.perform(get("/admin/vouchers")
                .param("sortBy", "code")
                .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/vouchers/list-with-datetime"));

        verify(voucherService, times(1)).getAllVouchers();
    }
}

