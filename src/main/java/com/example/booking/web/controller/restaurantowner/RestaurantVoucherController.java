package com.example.booking.web.controller.restaurantowner;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

import com.example.booking.domain.Voucher;
import com.example.booking.domain.VoucherStatus;
import com.example.booking.dto.admin.VoucherCreateForm;
import com.example.booking.dto.admin.VoucherEditForm;
import com.example.booking.service.VoucherService;
import com.example.booking.service.RestaurantOwnerService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/restaurant-owner/vouchers")
public class RestaurantVoucherController {

    @Autowired
    private VoucherService voucherService;
    
    @Autowired
    private RestaurantOwnerService restaurantOwnerService;
    
    /**
     * Helper method to get restaurant ID from authentication
     * Handles both UUID and username-based authentication
     */
    private Integer getRestaurantIdFromAuth(Authentication authentication) {
        String username = authentication.getName();
        
        // Try to parse as UUID first
        try {
            UUID ownerId = UUID.fromString(username);
            return restaurantOwnerService.getRestaurantIdByOwnerId(ownerId);
        } catch (IllegalArgumentException e) {
            // If not a UUID, this is likely a username
            // For development/testing, return a default restaurant ID
            return restaurantOwnerService.getRestaurantIdByOwnerId(null);
        }
    }
    
    /**
     * Helper method to get restaurant ID from authentication with error message
     * Handles both UUID and username-based authentication
     */
    private Integer getRestaurantIdFromAuth(Authentication authentication, Model model) {
        String username = authentication.getName();
        
        // Try to parse as UUID first
        try {
            UUID ownerId = UUID.fromString(username);
            return restaurantOwnerService.getRestaurantIdByOwnerId(ownerId);
        } catch (IllegalArgumentException e) {
            // If not a UUID, this is likely a username
            // For development/testing, return a default restaurant ID
            model.addAttribute("errorMessage", "Using development mode. Username: " + username + ". Please implement proper user-restaurant mapping.");
            
            // Return first available restaurant for testing
            return restaurantOwnerService.getRestaurantIdByOwnerId(null);
        }
    }

    @GetMapping("/debug")
    public String debugVouchers(Model model) {
        // Debug endpoint - no authentication required
        try {
            List<Voucher> allVouchers = voucherService.getVouchersByRestaurant(16); // Hardcoded restaurant ID for testing
            
            System.out.println("DEBUG: All vouchers count: " + allVouchers.size());
            for (Voucher v : allVouchers) {
                System.out.println("DEBUG: Voucher - ID: " + v.getVoucherId() + ", Code: " + v.getCode() + ", Status: " + v.getStatus() + ", Restaurant: " + (v.getRestaurant() != null ? v.getRestaurant().getRestaurantId() : "null"));
                System.out.println("DEBUG: Voucher - DiscountType: " + v.getDiscountType() + ", DiscountValue: " + v.getDiscountValue() + ", GlobalUsageLimit: " + v.getGlobalUsageLimit() + ", PerCustomerLimit: " + v.getPerCustomerLimit());
                System.out.println("DEBUG: Voucher - StartDate: " + v.getStartDate() + ", EndDate: " + v.getEndDate() + ", CreatedAt: " + v.getCreatedAt());
            }
            
            model.addAttribute("vouchers", allVouchers);
            model.addAttribute("debugMode", true);
            return "restaurant-owner/vouchers/list";
        } catch (Exception e) {
            System.out.println("DEBUG ERROR: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error: " + e.getMessage());
            model.addAttribute("vouchers", List.of());
            return "restaurant-owner/vouchers/list";
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public String listVouchers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            Authentication authentication,
            Model model) {
        
        // Get current restaurant owner's restaurant ID
        Integer restaurantId = getRestaurantIdFromAuth(authentication, model);
        
        if (restaurantId == null) {
            model.addAttribute("errorMessage", "Restaurant not found for this owner");
            return "restaurant-owner/vouchers/list";
        }
        
        // Sort sort = sortDir.equalsIgnoreCase("desc") ? 
        //     Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        // Pageable pageable = PageRequest.of(page, size, sort); // TODO: Use when implementing pagination
        
        // Get vouchers for this restaurant
        List<Voucher> vouchers = voucherService.getVouchersByRestaurant(restaurantId);
        
        // Debug: Log voucher count and details
        System.out.println("DEBUG: Restaurant ID: " + restaurantId);
        System.out.println("DEBUG: Vouchers count: " + vouchers.size());
        for (Voucher v : vouchers) {
            System.out.println("DEBUG: Voucher - ID: " + v.getVoucherId() + ", Code: " + v.getCode() + ", Status: " + v.getStatus());
        }
        
        model.addAttribute("vouchers", vouchers);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", 0);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("restaurantId", restaurantId);
        
        return "restaurant-owner/vouchers/list";
    }
    

    @GetMapping("/new")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public String showCreateForm(Authentication authentication, Model model) {
        // Get current restaurant owner's restaurant ID
        Integer restaurantId = getRestaurantIdFromAuth(authentication, model);
        
        if (restaurantId == null) {
            return "redirect:/restaurant-owner/dashboard?error=restaurant_not_found";
        }
        
        model.addAttribute("voucherForm", new VoucherCreateForm());
        model.addAttribute("statuses", VoucherStatus.values());
        model.addAttribute("restaurantId", restaurantId);
        return "restaurant-owner/vouchers/form";
    }

    @PostMapping("/new")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public String createVoucher(@Valid @ModelAttribute("voucherForm") VoucherCreateForm form,
                               BindingResult bindingResult,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            return "restaurant-owner/vouchers/form";
        }
        
        try {
            // Get current restaurant owner's restaurant ID
            Integer restaurantId = getRestaurantIdFromAuth(authentication);
            
            if (restaurantId == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Restaurant not found for this owner");
                return "redirect:/restaurant-owner/vouchers/new";
            }
            
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
                restaurantId, // restaurantId for restaurant vouchers
                form.getStatus()
            );
            
            voucherService.createRestaurantVoucher(restaurantId, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Voucher created successfully!");
            return "redirect:/restaurant-owner/vouchers";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating voucher: " + e.getMessage());
            return "redirect:/restaurant-owner/vouchers/new";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public String showEditForm(@PathVariable Integer id, 
                              Authentication authentication, 
                              Model model) {
        // TODO: Implement get voucher by ID and check ownership
        model.addAttribute("voucherForm", new VoucherEditForm());
        model.addAttribute("statuses", VoucherStatus.values());
        return "restaurant-owner/vouchers/form";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public String updateVoucher(@PathVariable Integer id,
                               @Valid @ModelAttribute("voucherForm") VoucherEditForm form,
                               BindingResult bindingResult,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            return "restaurant-owner/vouchers/form";
        }
        
        try {
            // TODO: Implement update voucher with ownership check
            redirectAttributes.addFlashAttribute("successMessage", "Voucher updated successfully!");
            return "redirect:/restaurant-owner/vouchers";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating voucher: " + e.getMessage());
            return "redirect:/restaurant-owner/vouchers/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/pause")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public String pauseVoucher(@PathVariable Integer id, 
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            // TODO: Add ownership check
            voucherService.pauseVoucher(id);
            redirectAttributes.addFlashAttribute("successMessage", "Voucher paused successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error pausing voucher: " + e.getMessage());
        }
        return "redirect:/restaurant-owner/vouchers";
    }

    @PostMapping("/{id}/resume")
    public String resumeVoucher(@PathVariable Integer id, 
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            // TODO: Add ownership check
            voucherService.resumeVoucher(id);
            redirectAttributes.addFlashAttribute("successMessage", "Voucher resumed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error resuming voucher: " + e.getMessage());
        }
        return "redirect:/restaurant-owner/vouchers";
    }

    @PostMapping("/{id}/expire")
    public String expireVoucher(@PathVariable Integer id, 
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            // TODO: Add ownership check
            voucherService.expireVoucher(id);
            redirectAttributes.addFlashAttribute("successMessage", "Voucher expired successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error expiring voucher: " + e.getMessage());
        }
        return "redirect:/restaurant-owner/vouchers";
    }

    @GetMapping("/{id}")
    public String viewVoucher(@PathVariable Integer id, 
                             Authentication authentication, 
                             Model model) {
        // TODO: Get voucher details and stats with ownership check
        model.addAttribute("voucherId", id);
        return "restaurant-owner/vouchers/detail";
    }
}
