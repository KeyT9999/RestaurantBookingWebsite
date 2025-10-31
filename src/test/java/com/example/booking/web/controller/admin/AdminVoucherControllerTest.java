package com.example.booking.web.controller.admin;

import com.example.booking.domain.Voucher;
import com.example.booking.domain.VoucherStatus;
import com.example.booking.domain.DiscountType;
import com.example.booking.dto.admin.VoucherCreateForm;
import com.example.booking.dto.admin.VoucherAssignForm;
import com.example.booking.service.VoucherService;
import com.example.booking.service.CustomerService;
import com.example.booking.domain.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminVoucherController.class)
class AdminVoucherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VoucherService voucherService;

    @MockBean
    private CustomerService customerService;

    private Voucher createTestVoucher() {
        Voucher voucher = new Voucher();
        voucher.setVoucherId(1);
        voucher.setCode("TEST2024");
        voucher.setDescription("Test Voucher");
        voucher.setDiscountType(DiscountType.PERCENT);
        voucher.setDiscountValue(new BigDecimal("50000"));
        voucher.setStartDate(LocalDate.now());
        voucher.setEndDate(LocalDate.now().plusDays(30));
        voucher.setGlobalUsageLimit(100);
        voucher.setStatus(VoucherStatus.ACTIVE);
        return voucher;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDisplayVouchersList() throws Exception {
        List<Voucher> vouchers = new ArrayList<>();
        vouchers.add(createTestVoucher());
        
        when(voucherService.getAllVouchers()).thenReturn(vouchers);
        when(voucherService.countRedemptionsByVoucherId(anyInt())).thenReturn(5L);

        mockMvc.perform(get("/admin/vouchers")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/vouchers/list-with-datetime"))
                .andExpect(model().attributeExists("vouchers"))
                .andExpect(model().attributeExists("voucherUsageMap"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFilterVouchersBySearch() throws Exception {
        List<Voucher> vouchers = new ArrayList<>();
        vouchers.add(createTestVoucher());
        
        when(voucherService.getAllVouchers()).thenReturn(vouchers);
        when(voucherService.countRedemptionsByVoucherId(anyInt())).thenReturn(0L);

        mockMvc.perform(get("/admin/vouchers")
                .param("search", "TEST"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/vouchers/list-with-datetime"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldFilterVouchersByStatus() throws Exception {
        List<Voucher> vouchers = new ArrayList<>();
        vouchers.add(createTestVoucher());
        
        when(voucherService.getAllVouchers()).thenReturn(vouchers);
        when(voucherService.countRedemptionsByVoucherId(anyInt())).thenReturn(0L);

        mockMvc.perform(get("/admin/vouchers")
                .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/vouchers/list-with-datetime"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDisplayCreateForm() throws Exception {
        mockMvc.perform(get("/admin/vouchers/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/vouchers/form-create"))
                .andExpect(model().attributeExists("voucherForm"))
                .andExpect(model().attributeExists("statuses"))
                .andExpect(model().attributeExists("discountTypes"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateVoucher() throws Exception {
        Voucher voucher = createTestVoucher();
        when(voucherService.createAdminVoucher(any())).thenReturn(voucher);

        mockMvc.perform(post("/admin/vouchers/new")
                .with(csrf())
                .param("code", "NEW2024")
                .param("description", "New Voucher")
                .param("discountType", "PERCENT")
                .param("discountValue", "50000")
                .param("startDate", LocalDate.now().toString())
                .param("endDate", LocalDate.now().plusDays(30).toString())
                .param("globalUsageLimit", "100")
                .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/vouchers"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(voucherService, times(1)).createAdminVoucher(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDisplayEditForm() throws Exception {
        Voucher voucher = createTestVoucher();
        when(voucherService.getVoucherById(1)).thenReturn(voucher);

        mockMvc.perform(get("/admin/vouchers/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/vouchers/form-edit"))
                .andExpect(model().attributeExists("voucherForm"))
                .andExpect(model().attributeExists("voucherId"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateVoucher() throws Exception {
        doNothing().when(voucherService).updateVoucher(anyInt(), any(VoucherService.VoucherEditDto.class));

        mockMvc.perform(post("/admin/vouchers/1/edit")
                .with(csrf())
                .param("code", "UPDATED2024")
                .param("description", "Updated Voucher")
                .param("discountType", "PERCENT")
                .param("discountValue", "75000")
                .param("startDate", LocalDate.now().toString())
                .param("endDate", LocalDate.now().plusDays(60).toString())
                .param("globalUsageLimit", "200")
                .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/vouchers"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(voucherService, times(1)).updateVoucher(eq(1), any(VoucherService.VoucherEditDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDisplayAssignForm() throws Exception {
        Voucher voucher = createTestVoucher();
        List<Customer> customers = new ArrayList<>();
        Customer customer = new Customer();
        customers.add(customer);
        
        when(voucherService.getVoucherById(1)).thenReturn(voucher);
        when(customerService.findAllCustomers()).thenReturn(customers);

        mockMvc.perform(get("/admin/vouchers/1/assign"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/vouchers/assign"))
                .andExpect(model().attributeExists("assignForm"))
                .andExpect(model().attributeExists("customers"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAssignVoucher() throws Exception {
        doNothing().when(voucherService).assignVoucherToCustomers(anyInt(), anyList());

        UUID customerId = UUID.randomUUID();
        mockMvc.perform(post("/admin/vouchers/1/assign")
                .with(csrf())
                .param("customerIds", customerId.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/vouchers/1"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(voucherService, times(1)).assignVoucherToCustomers(eq(1), anyList());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldPauseVoucher() throws Exception {
        doNothing().when(voucherService).pauseVoucher(1);

        mockMvc.perform(post("/admin/vouchers/1/pause")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/vouchers"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(voucherService, times(1)).pauseVoucher(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldResumeVoucher() throws Exception {
        doNothing().when(voucherService).resumeVoucher(1);

        mockMvc.perform(post("/admin/vouchers/1/resume")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/vouchers"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(voucherService, times(1)).resumeVoucher(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldHandleTestEndpoint() throws Exception {
        List<Voucher> vouchers = new ArrayList<>();
        when(voucherService.getAllVouchers()).thenReturn(vouchers);

        mockMvc.perform(get("/admin/vouchers/test"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/vouchers/list"));
    }
}
