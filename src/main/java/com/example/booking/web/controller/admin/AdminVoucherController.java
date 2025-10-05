package com.example.booking.web.controller.admin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booking.domain.VoucherStatus;
import com.example.booking.domain.DiscountType;
import com.example.booking.dto.admin.VoucherCreateForm;
import com.example.booking.dto.admin.VoucherEditForm;
import com.example.booking.dto.admin.VoucherAssignForm;
import com.example.booking.service.VoucherService;
import com.example.booking.domain.Voucher;
import com.example.booking.service.CustomerService;
import com.example.booking.domain.Customer;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/vouchers")
// @PreAuthorize("hasRole('ADMIN')")
public class AdminVoucherController {
    
    static {
        System.out.println("=== AdminVoucherController class loaded ===");
    }

    @Autowired
    private VoucherService voucherService;
    
    @Autowired
    private CustomerService customerService;

    @GetMapping
    public String listVouchers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            Model model) {
        
        System.out.println("=== AdminVoucherController.listVouchers called ===");
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Get all vouchers from database
        List<Voucher> allVouchers = voucherService.getAllVouchers();
        
        // Calculate statistics
        long globalVouchers = allVouchers.stream().filter(v -> v.getRestaurant() == null).count();
        long restaurantVouchers = allVouchers.stream().filter(v -> v.getRestaurant() != null).count();
        long activeVouchers = allVouchers.stream().filter(v -> v.getStatus() == VoucherStatus.ACTIVE).count();
        
        // Apply search filter if provided
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            allVouchers = allVouchers.stream()
                .filter(v -> (v.getCode() != null && v.getCode().toLowerCase().contains(searchLower)) ||
                           (v.getDescription() != null && v.getDescription().toLowerCase().contains(searchLower)))
                .collect(Collectors.toList());
        }
        
        // Apply status filter if provided
        if (status != null && !status.equals("ALL") && !status.isEmpty()) {
            try {
                VoucherStatus statusEnum = VoucherStatus.valueOf(status.toUpperCase());
                allVouchers = allVouchers.stream()
                    .filter(v -> v.getStatus() == statusEnum)
                    .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                // Invalid status, show all
            }
        }
        
        // Apply sorting
        if (sortBy.equals("createdAt")) {
            allVouchers.sort((v1, v2) -> {
                int result = v1.getCreatedAt().compareTo(v2.getCreatedAt());
                return sortDir.equalsIgnoreCase("desc") ? -result : result;
            });
        } else if (sortBy.equals("code")) {
            allVouchers.sort((v1, v2) -> {
                int result = v1.getCode().compareTo(v2.getCode());
                return sortDir.equalsIgnoreCase("desc") ? -result : result;
            });
        }
        
        // Calculate pagination
        int totalVouchers = allVouchers.size();
        int totalPages = (int) Math.ceil((double) totalVouchers / size);
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalVouchers);
        
        List<Voucher> pageVouchers = allVouchers.subList(startIndex, endIndex);
        
        model.addAttribute("vouchers", pageVouchers);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalVouchers", totalVouchers);
        model.addAttribute("globalVouchers", globalVouchers);
        model.addAttribute("restaurantVouchers", restaurantVouchers);
        model.addAttribute("activeVouchers", activeVouchers);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        
        return "admin/vouchers/list-with-datetime";
    }
    
    @GetMapping("/test-edit/{id}")
    public String testEditForm(@PathVariable Integer id, Model model) {
        try {
            Voucher voucher = voucherService.getVoucherById(id);
            model.addAttribute("voucher", voucher);
            model.addAttribute("message", "Voucher found: " + voucher.getCode());
            return "test";
        } catch (Exception e) {
            model.addAttribute("message", "Error: " + e.getMessage());
            return "test";
        }
    }
    
    @GetMapping("/test")
    public String testListVouchers(Model model) {
        // Test endpoint without authentication
        List<Voucher> allVouchers = voucherService.getAllVouchers();
        
        model.addAttribute("vouchers", allVouchers);
        model.addAttribute("currentPage", 0);
        model.addAttribute("totalPages", 1);
        model.addAttribute("totalVouchers", allVouchers.size());
        model.addAttribute("search", "");
        model.addAttribute("status", "");
        model.addAttribute("sortBy", "createdAt");
        model.addAttribute("sortDir", "desc");
        
        return "admin/vouchers/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("voucherForm", new VoucherCreateForm());
        model.addAttribute("statuses", VoucherStatus.values());
        model.addAttribute("discountTypes", DiscountType.values());
        return "admin/vouchers/form-create";
    }
    

    @PostMapping("/new")
    public String createVoucher(@Valid @ModelAttribute("voucherForm") VoucherCreateForm form,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            return "admin/vouchers/form";
        }
        
        try {
            VoucherService.VoucherCreateDto dto = new VoucherService.VoucherCreateDto(
                form.getCode(),
                form.getDescription(),
                form.getDiscountType(),
                form.getDiscountValue(),
                form.getStartDate(),
                form.getEndDate(),
                form.getGlobalUsageLimit(),
                form.getPerCustomerLimit(),
                form.getMinOrderAmount(),
                form.getMaxDiscountAmount(),
                null, // restaurantId = null for admin vouchers
                form.getStatus()
            );
            
            voucherService.createAdminVoucher(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Voucher created successfully!");
            return "redirect:/admin/vouchers";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating voucher: " + e.getMessage());
            return "redirect:/admin/vouchers/new";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Integer id, Model model) {
        try {
            // Get voucher by ID
            Voucher voucher = voucherService.getVoucherById(id);
            if (voucher == null) {
                return "redirect:/admin/vouchers";
            }
            
            // Create form with voucher data
            VoucherCreateForm form = new VoucherCreateForm();
            form.setCode(voucher.getCode());
            form.setDescription(voucher.getDescription());
            form.setDiscountType(voucher.getDiscountType().name());
            form.setDiscountValue(voucher.getDiscountValue());
            form.setStartDate(voucher.getStartDate());
            form.setEndDate(voucher.getEndDate());
            form.setGlobalUsageLimit(voucher.getGlobalUsageLimit());
            form.setPerCustomerLimit(voucher.getPerCustomerLimit());
            form.setMinOrderAmount(voucher.getMinOrderAmount());
            form.setMaxDiscountAmount(voucher.getMaxDiscountAmount());
            form.setStatus(voucher.getStatus());
            
            // Add date/time information
            form.setCreatedAt(voucher.getCreatedAt());
            form.setUpdatedAt(voucher.getCreatedAt()); // Use createdAt as updatedAt since Voucher doesn't have updatedAt
            
            model.addAttribute("voucherForm", form);
            model.addAttribute("statuses", VoucherStatus.values());
            model.addAttribute("discountTypes", DiscountType.values());
            model.addAttribute("voucherId", id);
            
            return "admin/vouchers/form-edit";
        } catch (Exception e) {
            return "redirect:/admin/vouchers";
        }
    }

    @PostMapping("/{id}/edit")
    public String updateVoucher(@PathVariable Integer id,
                               @Valid @ModelAttribute("voucherForm") VoucherCreateForm form,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            // Re-populate form data for edit mode
            try {
                Voucher voucher = voucherService.getVoucherById(id);
                form.setCreatedAt(voucher.getCreatedAt());
                form.setUpdatedAt(voucher.getCreatedAt());
                redirectAttributes.addFlashAttribute("voucherForm", form);
                redirectAttributes.addFlashAttribute("statuses", VoucherStatus.values());
                redirectAttributes.addFlashAttribute("discountTypes", DiscountType.values());
                redirectAttributes.addFlashAttribute("voucherId", id);
            } catch (Exception e) {
                // Ignore error, just return to form
            }
            return "admin/vouchers/form-edit";
        }
        
        try {
            // Create DTO for update
            VoucherService.VoucherCreateDto dto = new VoucherService.VoucherCreateDto(
                form.getCode(),
                form.getDescription(),
                form.getDiscountType(),
                form.getDiscountValue(),
                form.getStartDate(),
                form.getEndDate(),
                form.getGlobalUsageLimit(),
                form.getPerCustomerLimit(),
                form.getMinOrderAmount(),
                form.getMaxDiscountAmount(),
                null, // restaurantId = null for admin vouchers
                form.getStatus()
            );
            
            // Update voucher in database
            voucherService.updateVoucher(id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Voucher updated successfully!");
            return "redirect:/admin/vouchers";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating voucher: " + e.getMessage());
            return "redirect:/admin/vouchers/" + id + "/edit";
        }
    }

    @GetMapping("/{id}/assign")
    public String showAssignForm(@PathVariable Integer id, Model model) {
        // TODO: Get voucher details
        model.addAttribute("assignForm", new VoucherAssignForm());
        model.addAttribute("voucherId", id);
        
        // Get all customers for assignment
        List<Customer> customers = customerService.findAllCustomers();
        model.addAttribute("customers", customers);
        
        return "admin/vouchers/assign";
    }

    @PostMapping("/{id}/assign")
    public String assignVoucher(@PathVariable Integer id,
                               @ModelAttribute("assignForm") VoucherAssignForm form,
                               RedirectAttributes redirectAttributes) {
        
        try {
            List<UUID> customerIds = form.getCustomerIds();
            voucherService.assignVoucherToCustomers(id, customerIds);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Voucher assigned to " + customerIds.size() + " customers successfully!");
            return "redirect:/admin/vouchers/" + id;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error assigning voucher: " + e.getMessage());
            return "redirect:/admin/vouchers/" + id + "/assign";
        }
    }

    @PostMapping("/{id}/pause")
    public String pauseVoucher(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            voucherService.pauseVoucher(id);
            redirectAttributes.addFlashAttribute("successMessage", "Voucher paused successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error pausing voucher: " + e.getMessage());
        }
        return "redirect:/admin/vouchers";
    }

    @PostMapping("/{id}/resume")
    public String resumeVoucher(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            voucherService.resumeVoucher(id);
            redirectAttributes.addFlashAttribute("successMessage", "Voucher resumed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error resuming voucher: " + e.getMessage());
        }
        return "redirect:/admin/vouchers";
    }

    @PostMapping("/{id}/expire")
    public String expireVoucher(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            voucherService.expireVoucher(id);
            redirectAttributes.addFlashAttribute("successMessage", "Voucher expired successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error expiring voucher: " + e.getMessage());
        }
        return "redirect:/admin/vouchers";
    }

    @PostMapping("/{id}/delete")
    public String deleteVoucher(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            voucherService.deleteVoucher(id);
            redirectAttributes.addFlashAttribute("successMessage", "Voucher deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting voucher: " + e.getMessage());
        }
        return "redirect:/admin/vouchers";
    }

    @GetMapping("/{id}")
    public String viewVoucher(@PathVariable Integer id, Model model) {
        try {
            // Get voucher details
            Voucher voucher = voucherService.getVoucherById(id);
            if (voucher == null) {
                return "redirect:/admin/vouchers";
            }
            
            model.addAttribute("voucher", voucher);
            model.addAttribute("voucherId", id);
            
            // TODO: Get voucher statistics (usage count, total discount, etc.)
            // For now, we'll use placeholder values
            model.addAttribute("usageCount", 0);
            model.addAttribute("totalDiscount", 0);
            model.addAttribute("usageRate", 0);
            model.addAttribute("remainingUses", voucher.getGlobalUsageLimit() != null ? voucher.getGlobalUsageLimit() : "Unlimited");
            
            return "admin/vouchers/detail";
            
        } catch (Exception e) {
            return "redirect:/admin/vouchers";
        }
    }
}
