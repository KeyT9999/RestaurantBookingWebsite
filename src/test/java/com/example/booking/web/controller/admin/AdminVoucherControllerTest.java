package com.example.booking.web.controller.admin;

import com.example.booking.domain.Customer;
import com.example.booking.domain.DiscountType;
import com.example.booking.domain.Voucher;
import com.example.booking.domain.VoucherStatus;
import com.example.booking.dto.admin.VoucherAssignForm;
import com.example.booking.dto.admin.VoucherCreateForm;
import com.example.booking.service.CustomerService;
import com.example.booking.service.VoucherService;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminVoucherController.class)
@DisplayName("AdminVoucherController Test Suite")
class AdminVoucherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VoucherService voucherService;

    @MockBean
    private CustomerService customerService;

    private Voucher testVoucher;
    private List<Voucher> voucherList;
    private List<Customer> customerList;

    @BeforeEach
    void setUp() {
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
        testVoucher.setCreatedAt(LocalDateTime.now());

        voucherList = Arrays.asList(testVoucher);

        Customer customer1 = new Customer();
        customer1.setCustomerId(UUID.randomUUID());
        customer1.setFullName("Test Customer 1");

        Customer customer2 = new Customer();
        customer2.setCustomerId(UUID.randomUUID());
        customer2.setFullName("Test Customer 2");

        customerList = Arrays.asList(customer1, customer2);
    }

    @Nested
    @DisplayName("listVouchers() Tests")
    class ListVouchersTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should display vouchers list successfully")
        void shouldDisplayVouchersList() throws Exception {
            when(voucherService.getAllVouchers()).thenReturn(voucherList);
            when(voucherService.countRedemptionsByVoucherId(anyInt())).thenReturn(5L);

            mockMvc.perform(get("/admin/vouchers"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/vouchers/list-with-datetime"))
                    .andExpect(model().attributeExists("vouchers"))
                    .andExpect(model().attributeExists("voucherUsageMap"));

            verify(voucherService).getAllVouchers();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should filter vouchers by search term")
        void shouldFilterBySearch() throws Exception {
            when(voucherService.getAllVouchers()).thenReturn(voucherList);
            when(voucherService.countRedemptionsByVoucherId(anyInt())).thenReturn(0L);

            mockMvc.perform(get("/admin/vouchers")
                    .param("search", "TEST"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("vouchers"));

            verify(voucherService).getAllVouchers();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should filter vouchers by status")
        void shouldFilterByStatus() throws Exception {
            when(voucherService.getAllVouchers()).thenReturn(voucherList);
            when(voucherService.countRedemptionsByVoucherId(anyInt())).thenReturn(0L);

            mockMvc.perform(get("/admin/vouchers")
                    .param("status", "ACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("vouchers"));

            verify(voucherService).getAllVouchers();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should sort vouchers by code")
        void shouldSortByCode() throws Exception {
            when(voucherService.getAllVouchers()).thenReturn(voucherList);
            when(voucherService.countRedemptionsByVoucherId(anyInt())).thenReturn(0L);

            mockMvc.perform(get("/admin/vouchers")
                    .param("sortBy", "code")
                    .param("sortDir", "asc"))
                    .andExpect(status().isOk());

            verify(voucherService).getAllVouchers();
        }
    }

    @Nested
    @DisplayName("showCreateForm() Tests")
    class ShowCreateFormTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should display create form")
        void shouldDisplayCreateForm() throws Exception {
            mockMvc.perform(get("/admin/vouchers/new"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/vouchers/form-create"))
                    .andExpect(model().attributeExists("voucherForm"))
                    .andExpect(model().attributeExists("statuses"))
                    .andExpect(model().attributeExists("discountTypes"));
        }
    }

    @Nested
    @DisplayName("createVoucher() Tests")
    class CreateVoucherTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should create voucher successfully")
        void shouldCreateVoucherSuccessfully() throws Exception {
            VoucherCreateForm form = new VoucherCreateForm();
            form.setCode("NEW2024");
            form.setDescription("New Voucher");
            form.setDiscountType("PERCENT");
            form.setDiscountValue(BigDecimal.valueOf(15.0));
            form.setStartDate(LocalDate.now().plusDays(1));
            form.setEndDate(LocalDate.now().plusDays(30));
            form.setGlobalUsageLimit(50);
            form.setPerCustomerLimit(1);
            form.setMinOrderAmount(BigDecimal.valueOf(50000.0));
            form.setMaxDiscountAmount(BigDecimal.valueOf(30000.0));
            form.setStatus(VoucherStatus.ACTIVE);

            doNothing().when(voucherService).createAdminVoucher(any());

            mockMvc.perform(post("/admin/vouchers/new")
                    .with(csrf())
                    .param("code", form.getCode())
                    .param("description", form.getDescription())
                    .param("discountType", form.getDiscountType())
                    .param("discountValue", String.valueOf(form.getDiscountValue()))
                    .param("startDate", form.getStartDate().toString())
                    .param("endDate", form.getEndDate().toString())
                    .param("globalUsageLimit", String.valueOf(form.getGlobalUsageLimit()))
                    .param("perCustomerLimit", String.valueOf(form.getPerCustomerLimit()))
                    .param("minOrderAmount", String.valueOf(form.getMinOrderAmount()))
                    .param("maxDiscountAmount", String.valueOf(form.getMaxDiscountAmount()))
                    .param("status", form.getStatus().name()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/vouchers"))
                    .andExpect(flash().attributeExists("successMessage"));

            verify(voucherService).createAdminVoucher(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle validation errors")
        void shouldHandleValidationErrors() throws Exception {
            mockMvc.perform(post("/admin/vouchers/new")
                    .with(csrf())
                    .param("code", "")) // Invalid: empty code
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/vouchers/form"));

            verify(voucherService, never()).createAdminVoucher(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle service exception")
        void shouldHandleServiceException() throws Exception {
            doThrow(new RuntimeException("Service error")).when(voucherService).createAdminVoucher(any());

            mockMvc.perform(post("/admin/vouchers/new")
                    .with(csrf())
                    .param("code", "TEST")
                    .param("description", "Test")
                    .param("discountType", "PERCENTAGE")
                    .param("discountValue", "10")
                    .param("startDate", LocalDate.now().plusDays(1).toString())
                    .param("endDate", LocalDate.now().plusDays(30).toString())
                    .param("status", "ACTIVE"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/vouchers/new"))
                    .andExpect(flash().attributeExists("errorMessage"));
        }
    }

    @Nested
    @DisplayName("showEditForm() Tests")
    class ShowEditFormTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should display edit form")
        void shouldDisplayEditForm() throws Exception {
            when(voucherService.getVoucherById(1)).thenReturn(testVoucher);

            mockMvc.perform(get("/admin/vouchers/1/edit"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/vouchers/form-edit"))
                    .andExpect(model().attributeExists("voucherForm"))
                    .andExpect(model().attributeExists("statuses"))
                    .andExpect(model().attributeExists("discountTypes"));

            verify(voucherService).getVoucherById(1);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should redirect when voucher not found")
        void shouldRedirectWhenVoucherNotFound() throws Exception {
            when(voucherService.getVoucherById(999)).thenReturn(null);

            mockMvc.perform(get("/admin/vouchers/999/edit"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/vouchers"));
        }
    }

    @Nested
    @DisplayName("updateVoucher() Tests")
    class UpdateVoucherTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should update voucher successfully")
        void shouldUpdateVoucherSuccessfully() throws Exception {
            when(voucherService.getVoucherById(1)).thenReturn(testVoucher);
            when(voucherService.updateVoucher(eq(1), any(VoucherService.VoucherEditDto.class))).thenReturn(testVoucher);

            mockMvc.perform(post("/admin/vouchers/1/edit")
                    .with(csrf())
                    .param("code", "UPDATED2024")
                    .param("description", "Updated Voucher")
                    .param("discountType", "PERCENTAGE")
                    .param("discountValue", "20")
                    .param("startDate", LocalDate.now().plusDays(1).toString())
                    .param("endDate", LocalDate.now().plusDays(30).toString())
                    .param("status", "ACTIVE"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/vouchers"))
                    .andExpect(flash().attributeExists("successMessage"));

            verify(voucherService).updateVoucher(eq(1), any(VoucherService.VoucherEditDto.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should handle update exception")
        void shouldHandleUpdateException() throws Exception {
            when(voucherService.getVoucherById(1)).thenReturn(testVoucher);
            when(voucherService.updateVoucher(eq(1), any(VoucherService.VoucherEditDto.class)))
                    .thenThrow(new RuntimeException("Update error"));

            mockMvc.perform(post("/admin/vouchers/1/edit")
                    .with(csrf())
                    .param("code", "TEST")
                    .param("description", "Test")
                    .param("discountType", "PERCENTAGE")
                    .param("discountValue", "10")
                    .param("startDate", LocalDate.now().plusDays(1).toString())
                    .param("endDate", LocalDate.now().plusDays(30).toString())
                    .param("status", "ACTIVE"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/vouchers/1/edit"))
                    .andExpect(flash().attributeExists("errorMessage"));
        }
    }

    @Nested
    @DisplayName("showAssignForm() Tests")
    class ShowAssignFormTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should display assign form")
        void shouldDisplayAssignForm() throws Exception {
            when(customerService.findAllCustomers()).thenReturn(customerList);

            mockMvc.perform(get("/admin/vouchers/1/assign"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/vouchers/assign"))
                    .andExpect(model().attributeExists("assignForm"))
                    .andExpect(model().attributeExists("customers"));

            verify(customerService).findAllCustomers();
        }
    }

    @Nested
    @DisplayName("assignVoucher() Tests")
    class AssignVoucherTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should assign voucher successfully")
        void shouldAssignVoucherSuccessfully() throws Exception {
            List<UUID> customerIds = Arrays.asList(
                    customerList.get(0).getCustomerId(),
                    customerList.get(1).getCustomerId()
            );

            doNothing().when(voucherService).assignVoucherToCustomers(eq(1), anyList());

            mockMvc.perform(post("/admin/vouchers/1/assign")
                    .with(csrf())
                    .param("customerIds", customerIds.get(0).toString(), customerIds.get(1).toString()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/vouchers/1"))
                    .andExpect(flash().attributeExists("successMessage"));

            verify(voucherService).assignVoucherToCustomers(eq(1), anyList());
        }
    }

    @Nested
    @DisplayName("Voucher Actions Tests")
    class VoucherActionsTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should pause voucher successfully")
        void shouldPauseVoucher() throws Exception {
            doNothing().when(voucherService).pauseVoucher(1);

            mockMvc.perform(post("/admin/vouchers/1/pause")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/vouchers"))
                    .andExpect(flash().attributeExists("successMessage"));

            verify(voucherService).pauseVoucher(1);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should resume voucher successfully")
        void shouldResumeVoucher() throws Exception {
            doNothing().when(voucherService).resumeVoucher(1);

            mockMvc.perform(post("/admin/vouchers/1/resume")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/vouchers"))
                    .andExpect(flash().attributeExists("successMessage"));

            verify(voucherService).resumeVoucher(1);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should expire voucher successfully")
        void shouldExpireVoucher() throws Exception {
            doNothing().when(voucherService).expireVoucher(1);

            mockMvc.perform(post("/admin/vouchers/1/expire")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/vouchers"))
                    .andExpect(flash().attributeExists("successMessage"));

            verify(voucherService).expireVoucher(1);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should delete voucher successfully")
        void shouldDeleteVoucher() throws Exception {
            doNothing().when(voucherService).deleteVoucher(1);

            mockMvc.perform(post("/admin/vouchers/1/delete")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/vouchers"))
                    .andExpect(flash().attributeExists("successMessage"));

            verify(voucherService).deleteVoucher(1);
        }
    }

    @Nested
    @DisplayName("viewVoucher() Tests")
    class ViewVoucherTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should display voucher details")
        void shouldDisplayVoucherDetails() throws Exception {
            when(voucherService.getVoucherById(1)).thenReturn(testVoucher);
            when(voucherService.countRedemptionsByVoucherId(1)).thenReturn(25L);

            mockMvc.perform(get("/admin/vouchers/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/vouchers/detail"))
                    .andExpect(model().attributeExists("voucher"))
                    .andExpect(model().attributeExists("usageCount"))
                    .andExpect(model().attributeExists("remainingUses"));

            verify(voucherService).getVoucherById(1);
            verify(voucherService).countRedemptionsByVoucherId(1);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should redirect when voucher not found")
        void shouldRedirectWhenVoucherNotFound() throws Exception {
            when(voucherService.getVoucherById(999)).thenReturn(null);

            mockMvc.perform(get("/admin/vouchers/999"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/vouchers"));
        }
    }

    @Nested
    @DisplayName("Test Endpoints")
    class TestEndpointsTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should display test edit form")
        void shouldDisplayTestEditForm() throws Exception {
            when(voucherService.getVoucherById(1)).thenReturn(testVoucher);

            mockMvc.perform(get("/admin/vouchers/test-edit/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("test"))
                    .andExpect(model().attributeExists("voucher"));

            verify(voucherService).getVoucherById(1);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should display test list")
        void shouldDisplayTestList() throws Exception {
            when(voucherService.getAllVouchers()).thenReturn(voucherList);

            mockMvc.perform(get("/admin/vouchers/test"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/vouchers/list"))
                    .andExpect(model().attributeExists("vouchers"));

            verify(voucherService).getAllVouchers();
        }
    }
}

