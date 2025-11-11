package com.example.booking.web.controller.restaurantowner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.domain.User;
import com.example.booking.domain.Voucher;
import com.example.booking.domain.VoucherStatus;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.dto.admin.VoucherCreateForm;
import com.example.booking.dto.admin.VoucherEditForm;
import com.example.booking.service.VoucherService;
import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.SimpleUserService;
import com.example.booking.repository.VoucherRedemptionRepository;
import com.example.booking.domain.VoucherRedemption;
import com.example.booking.repository.PaymentRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/restaurant-owner/vouchers")
public class RestaurantVoucherController {

    @Autowired
    private VoucherService voucherService;
    
    @Autowired
    private RestaurantOwnerService restaurantOwnerService;
    
    @Autowired
    private SimpleUserService userService;
    
    @Autowired
    private VoucherRedemptionRepository voucherRedemptionRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    /**
     * Helper method to get restaurants owned by current user
     */
    private List<RestaurantProfile> getRestaurantsFromAuth(Authentication authentication) {
        return restaurantOwnerService.getRestaurantsByCurrentUser(authentication);
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
            @RequestParam(required = false) Integer restaurantId,
            Authentication authentication,
            Model model) {
        
        try {
            System.out.println("🔍 RestaurantVoucherController.listVouchers called");
            System.out.println("   Authentication: " + (authentication != null ? authentication.getName() : "null"));
            
            // Get all restaurants owned by current user
            List<RestaurantProfile> restaurants = getRestaurantsFromAuth(authentication);
            System.out.println("   Restaurants found: " + restaurants.size());
            
            if (restaurants.isEmpty()) {
                model.addAttribute("errorMessage", "Không tìm thấy nhà hàng nào của bạn. Vui lòng tạo nhà hàng trước.");
                return "restaurant-owner/vouchers/list";
            }
            
            // If no specific restaurant selected, use the first one
            final Integer finalRestaurantId = (restaurantId == null) ? restaurants.get(0).getRestaurantId() : restaurantId;
            
            // Verify that the selected restaurant belongs to the current user
            boolean restaurantOwned = restaurants.stream()
                .anyMatch(r -> r.getRestaurantId().equals(finalRestaurantId));
            
            if (!restaurantOwned) {
                model.addAttribute("errorMessage", "Bạn không có quyền truy cập nhà hàng này.");
                return "restaurant-owner/vouchers/list";
            }
            
            // Get vouchers for the selected restaurant
            List<Voucher> vouchers = voucherService.getVouchersByRestaurant(finalRestaurantId);
            
            // Find and set current restaurant
            RestaurantProfile currentRestaurant = restaurants.stream()
                .filter(r -> r.getRestaurantId().equals(finalRestaurantId))
                .findFirst()
                .orElse(null);
            
            // Debug: Log voucher count and details
            System.out.println("DEBUG: Selected Restaurant ID: " + finalRestaurantId);
            System.out.println("DEBUG: Vouchers count: " + vouchers.size());
            for (Voucher v : vouchers) {
                System.out.println("DEBUG: Voucher - ID: " + v.getVoucherId() + ", Code: " + v.getCode() + ", Status: " + v.getStatus());
            }
        
            model.addAttribute("vouchers", vouchers);
            model.addAttribute("restaurants", restaurants);
            model.addAttribute("selectedRestaurantId", finalRestaurantId);
            model.addAttribute("restaurantId", finalRestaurantId);
            model.addAttribute("currentRestaurant", currentRestaurant);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", 0);
            model.addAttribute("search", search);
            model.addAttribute("status", status);
            
            return "restaurant-owner/vouchers/list";
            
        } catch (Exception e) {
            System.err.println("❌ Error in listVouchers: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Lỗi khi tải dữ liệu: " + e.getMessage());
            return "restaurant-owner/vouchers/list";
        }
    }
    

    @GetMapping("/new")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public String showCreateForm(
            @RequestParam(required = false) Integer restaurantId,
            Authentication authentication, 
            Model model) {
        
        // Get all restaurants owned by current user
        List<RestaurantProfile> restaurants = getRestaurantsFromAuth(authentication);
        
        if (restaurants.isEmpty()) {
            model.addAttribute("errorMessage", "Không tìm thấy nhà hàng nào của bạn. Vui lòng tạo nhà hàng trước.");
            return "restaurant-owner/vouchers/form";
        }
        
        // If no specific restaurant selected, use the first one
        final Integer finalRestaurantId = (restaurantId == null) ? restaurants.get(0).getRestaurantId() : restaurantId;
        
        // Verify that the selected restaurant belongs to the current user
        boolean restaurantOwned = restaurants.stream()
            .anyMatch(r -> r.getRestaurantId().equals(finalRestaurantId));
        
        if (!restaurantOwned) {
            model.addAttribute("errorMessage", "Bạn không có quyền truy cập nhà hàng này.");
            return "restaurant-owner/vouchers/form";
        }
        
        model.addAttribute("voucherForm", new VoucherCreateForm());
        model.addAttribute("statuses", VoucherStatus.values());
        model.addAttribute("restaurants", restaurants);
        model.addAttribute("selectedRestaurantId", finalRestaurantId);
        return "restaurant-owner/vouchers/form";
    }

    @PostMapping("/new")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public String createVoucher(@Valid @ModelAttribute("voucherForm") VoucherCreateForm form,
                               BindingResult bindingResult,
                               @RequestParam(required = false) Integer restaurantId,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        
        // Get restaurants and handle null restaurantId
        List<RestaurantProfile> restaurants = getRestaurantsFromAuth(authentication);
        if (restaurants.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy nhà hàng nào của bạn.");
            return "redirect:/restaurant-owner/vouchers";
        }
        
        // If no restaurantId provided, use the first restaurant
        final Integer finalRestaurantId = (restaurantId == null) ? restaurants.get(0).getRestaurantId() : restaurantId;
        
        if (bindingResult.hasErrors()) {
            // Get restaurants for form redisplay
            redirectAttributes.addFlashAttribute("restaurants", restaurants);
            redirectAttributes.addFlashAttribute("selectedRestaurantId", finalRestaurantId);
            return "redirect:/restaurant-owner/vouchers/new?restaurantId=" + finalRestaurantId;
        }
        
        try {
            // Verify that the selected restaurant belongs to the current user
            boolean restaurantOwned = restaurants.stream()
                .anyMatch(r -> r.getRestaurantId().equals(finalRestaurantId));
            
            if (!restaurantOwned) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền tạo voucher cho nhà hàng này.");
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
                finalRestaurantId, // restaurantId for restaurant vouchers
                form.getStatus()
            );
            
            voucherService.createRestaurantVoucher(finalRestaurantId, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Voucher created successfully!");
            return "redirect:/restaurant-owner/vouchers?restaurantId=" + finalRestaurantId;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating voucher: " + e.getMessage());
            return "redirect:/restaurant-owner/vouchers/new?restaurantId=" + finalRestaurantId;
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
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public String viewVoucher(@PathVariable Integer id,
                             @RequestParam(required = false) Integer restaurantId,
                             Authentication authentication, 
                             Model model) {
        try {
            // Get restaurants owned by current user
            List<RestaurantProfile> restaurants = getRestaurantsFromAuth(authentication);
            if (restaurants.isEmpty()) {
                model.addAttribute("errorMessage", "Không tìm thấy nhà hàng nào của bạn.");
                return "restaurant-owner/vouchers/list";
            }

            // Get voucher details
            Voucher voucher = voucherService.getVoucherById(id);
            if (voucher == null) {
                model.addAttribute("errorMessage", "Voucher không tồn tại.");
                return "restaurant-owner/vouchers/list";
            }

            // Check ownership
            boolean isOwned = restaurants.stream()
                .anyMatch(r -> r.getRestaurantId().equals(voucher.getRestaurant().getRestaurantId()));
            
            if (!isOwned) {
                model.addAttribute("errorMessage", "Bạn không có quyền xem voucher này.");
                return "restaurant-owner/vouchers/list";
            }

            // Get voucher usage statistics
            int usedCount = voucher.getUsedCount();
            int totalLimit = voucher.getGlobalUsageLimit() != null ? voucher.getGlobalUsageLimit() : 0;
            
            model.addAttribute("voucher", voucher);
            model.addAttribute("restaurants", restaurants);
            model.addAttribute("selectedRestaurantId", voucher.getRestaurant().getRestaurantId());
            model.addAttribute("usedCount", usedCount);
            model.addAttribute("totalLimit", totalLimit);
            model.addAttribute("usagePercentage", totalLimit > 0 ? (usedCount * 100.0 / totalLimit) : 0);
            
            return "restaurant-owner/vouchers/detail";
            
        } catch (Exception e) {
            System.err.println("❌ Error in viewVoucher: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Lỗi khi tải thông tin voucher: " + e.getMessage());
            return "restaurant-owner/vouchers/list";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public String showEditForm(@PathVariable Integer id,
                              @RequestParam(required = false) Integer restaurantId,
                              Authentication authentication,
                              Model model) {
        try {
            // Get restaurants owned by current user
            List<RestaurantProfile> restaurants = getRestaurantsFromAuth(authentication);
            if (restaurants.isEmpty()) {
                model.addAttribute("errorMessage", "Không tìm thấy nhà hàng nào của bạn.");
                return "restaurant-owner/vouchers/list";
            }

            // Get voucher details
            Voucher voucher = voucherService.getVoucherById(id);
            if (voucher == null) {
                model.addAttribute("errorMessage", "Voucher không tồn tại.");
                return "restaurant-owner/vouchers/list";
            }

            // Check ownership
            boolean isOwned = restaurants.stream()
                .anyMatch(r -> r.getRestaurantId().equals(voucher.getRestaurant().getRestaurantId()));
            
            if (!isOwned) {
                model.addAttribute("errorMessage", "Bạn không có quyền chỉnh sửa voucher này.");
                return "restaurant-owner/vouchers/list";
            }

            // Create edit form
            VoucherEditForm editForm = new VoucherEditForm();
            editForm.setCode(voucher.getCode());
            editForm.setDescription(voucher.getDescription());
            editForm.setDiscountType(voucher.getDiscountType().name());
            editForm.setDiscountValue(voucher.getDiscountValue());
            editForm.setStartDate(voucher.getStartDate());
            editForm.setEndDate(voucher.getEndDate());
            editForm.setGlobalUsageLimit(voucher.getGlobalUsageLimit());
            editForm.setPerCustomerLimit(voucher.getPerCustomerLimit());
            editForm.setMinOrderAmount(voucher.getMinOrderAmount());
            editForm.setMaxDiscountAmount(voucher.getMaxDiscountAmount());
            editForm.setStatus(voucher.getStatus().name());

            model.addAttribute("voucherForm", editForm);
            model.addAttribute("voucher", voucher);
            model.addAttribute("statuses", VoucherStatus.values());
            
            return "restaurant-owner/vouchers/form_edit";
            
        } catch (Exception e) {
            System.err.println("❌ Error in showEditForm: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Lỗi khi tải form chỉnh sửa: " + e.getMessage());
            return "restaurant-owner/vouchers/list";
        }
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public String updateVoucher(@PathVariable Integer id,
                               @RequestParam String status,
                               @RequestParam(required = false) String description,
                               @RequestParam(required = false) BigDecimal discountValue,
                               @RequestParam(required = false) BigDecimal minOrderAmount,
                               @RequestParam(required = false) BigDecimal maxDiscountAmount,
                               @RequestParam(required = false) Integer globalUsageLimit,
                               @RequestParam(required = false) Integer perCustomerLimit,
                               @RequestParam(required = false) String startDate,
                               @RequestParam(required = false) String endDate,
                               @RequestParam(required = false) Integer restaurantId,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        System.out.println("🔧 updateVoucher called for ID: " + id);
        System.out.println("🔧 Status: " + status);
        System.out.println("🔧 Description: " + description);
        System.out.println("🔧 Discount Value: " + discountValue);
        try {
            // Get restaurants owned by current user
            List<RestaurantProfile> restaurants = getRestaurantsFromAuth(authentication);
            if (restaurants.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy nhà hàng nào của bạn.");
                return "redirect:/restaurant-owner/vouchers";
            }

            // Get voucher details
            Voucher voucher = voucherService.getVoucherById(id);
            if (voucher == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Voucher không tồn tại.");
                return "redirect:/restaurant-owner/vouchers";
            }

            // Check ownership
            boolean isOwned = restaurants.stream()
                .anyMatch(r -> r.getRestaurantId().equals(voucher.getRestaurant().getRestaurantId()));
            
            if (!isOwned) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền chỉnh sửa voucher này.");
                return "redirect:/restaurant-owner/vouchers";
            }

            // Simple validation
            if (status == null || status.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Trạng thái không được để trống.");
                return "redirect:/restaurant-owner/vouchers/" + id + "/edit?restaurantId=" + voucher.getRestaurant().getRestaurantId();
            }

            // Parse dates
            LocalDate parsedStartDate = startDate != null && !startDate.trim().isEmpty() ? LocalDate.parse(startDate) : voucher.getStartDate();
            LocalDate parsedEndDate = endDate != null && !endDate.trim().isEmpty() ? LocalDate.parse(endDate) : voucher.getEndDate();
            
            // Update voucher
            VoucherService.VoucherEditDto dto = new VoucherService.VoucherEditDto(
                voucher.getCode(),
                description,
                voucher.getDiscountType().name(),
                discountValue,
                parsedStartDate,
                parsedEndDate,
                globalUsageLimit,
                perCustomerLimit,
                minOrderAmount,
                maxDiscountAmount,
                status
            );

            voucherService.updateVoucher(id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Voucher đã được cập nhật thành công!");
            return "redirect:/restaurant-owner/vouchers/" + id + "?restaurantId=" + voucher.getRestaurant().getRestaurantId();

        } catch (Exception e) {
            System.err.println("❌ Error in updateVoucher: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật voucher: " + e.getMessage());
            return "redirect:/restaurant-owner/vouchers/" + id + "/edit?restaurantId=" + restaurantId;
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public String deleteVoucher(@PathVariable Integer id,
                               @RequestParam(required = false) Integer restaurantId,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            // Get restaurants owned by current user
            List<RestaurantProfile> restaurants = getRestaurantsFromAuth(authentication);
            if (restaurants.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy nhà hàng nào của bạn.");
                return "redirect:/restaurant-owner/vouchers";
            }

            // Get voucher details
            Voucher voucher = voucherService.getVoucherById(id);
            if (voucher == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Voucher không tồn tại.");
                return "redirect:/restaurant-owner/vouchers";
            }

            // Check ownership
            boolean isOwned = restaurants.stream()
                .anyMatch(r -> r.getRestaurantId().equals(voucher.getRestaurant().getRestaurantId()));
            
            if (!isOwned) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền xóa voucher này.");
                return "redirect:/restaurant-owner/vouchers";
            }

            // Delete voucher
            voucherService.deleteVoucher(id);
            redirectAttributes.addFlashAttribute("successMessage", "Voucher đã được xóa thành công!");
            return "redirect:/restaurant-owner/vouchers?restaurantId=" + voucher.getRestaurant().getRestaurantId();

        } catch (Exception e) {
            System.err.println("❌ Error in deleteVoucher: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa voucher: " + e.getMessage());
            return "redirect:/restaurant-owner/vouchers";
        }
    }

    @GetMapping("/test-simple")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public String testSimple(Authentication authentication, Model model) {
        try {
            System.out.println("🔧 Testing simple template...");
            
            // Get current user
            User currentUser = (User) authentication.getPrincipal();
            if (currentUser == null) {
                return "redirect:/login";
            }

            // Get vouchers for testing
            List<Voucher> vouchers = voucherService.getAllVouchers();
            model.addAttribute("vouchers", vouchers);
            
            System.out.println("🔧 Found " + vouchers.size() + " vouchers");
            
            return "restaurant-owner/vouchers/test_simple";
            
        } catch (Exception e) {
            System.err.println("❌ Error in testSimple: " + e.getMessage());
            e.printStackTrace();
            return "error/500";
        }
    }
    
    /**
     * API endpoint to get list of booking IDs that used a specific voucher
     */
    @GetMapping("/{id}/bookings")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @ResponseBody
    @Transactional(readOnly = true)
    public List<Integer> getVoucherBookings(@PathVariable Integer id, Authentication authentication) {
        try {
            System.out.println("🔍 Getting bookings for voucher ID: " + id);
            
            // Get all restaurants owned by current user
            List<RestaurantProfile> restaurants = getRestaurantsFromAuth(authentication);
            if (restaurants.isEmpty()) {
                System.out.println("❌ No restaurants found for user");
                return List.of();
            }
            
            // Get voucher
            Voucher voucher = voucherService.getVoucherById(id);
            if (voucher == null) {
                System.out.println("❌ Voucher not found: " + id);
                return List.of();
            }
            
            // Check ownership if voucher is restaurant-specific
            if (voucher.getRestaurant() != null) {
                boolean restaurantOwned = restaurants.stream()
                    .anyMatch(r -> r.getRestaurantId().equals(voucher.getRestaurant().getRestaurantId()));
                if (!restaurantOwned) {
                    System.out.println("❌ User doesn't own this voucher's restaurant");
                    return List.of();
                }
            }
            
            // First, check total redemptions
            List<VoucherRedemption> allRedemptions = voucherRedemptionRepository.findByVoucher_VoucherId(id);
            System.out.println("📊 Total redemptions for voucher " + id + ": " + allRedemptions.size());
            
            // PRIORITY 1: Try query from Payment table directly (most reliable - payment.voucher_id is set when voucher is applied)
            System.out.println("🔍 Priority 1: Querying from payment table where payment.voucher_id = " + id);
            List<Integer> bookingIds = paymentRepository.findBookingIdsByVoucherId(id);
            System.out.println("✅ Payment table query found " + bookingIds.size() + " booking IDs");
            
            // PRIORITY 2: If empty, try from voucher_redemption.booking_id
            if (bookingIds.isEmpty()) {
                System.out.println("⚠️ No booking IDs from payment table, trying voucher_redemption.booking_id...");
                bookingIds = voucherRedemptionRepository.findBookingIdsByVoucherIdNative(id);
                System.out.println("📋 Native query (from booking_id column) found " + bookingIds.size() + " booking IDs");
            }
            
            // PRIORITY 3: Try from payment via JOIN with voucher_redemption
            if (bookingIds.isEmpty()) {
                System.out.println("⚠️ No booking IDs from voucher_redemption.booking_id, trying payment.booking_id via JOIN...");
                List<Integer> bookingIdsFromPayment = voucherRedemptionRepository.findBookingIdsByVoucherIdFromPayment(id);
                System.out.println("📋 Query from payment table (via JOIN) found " + bookingIdsFromPayment.size() + " booking IDs");
                
                if (!bookingIdsFromPayment.isEmpty()) {
                    bookingIds = bookingIdsFromPayment;
                }
            }
            
            // PRIORITY 4: Try direct query from payment table (alternative method)
            if (bookingIds.isEmpty()) {
                System.out.println("⚠️ No booking IDs from payment JOIN, trying direct payment.voucher_id query (alternative)...");
                List<Integer> bookingIdsFromPayment = voucherRedemptionRepository.findBookingIdsByVoucherIdFromPaymentDirect(id);
                System.out.println("📋 Query from payment table (direct alternative) found " + bookingIdsFromPayment.size() + " booking IDs");
                
                if (!bookingIdsFromPayment.isEmpty()) {
                    bookingIds = bookingIdsFromPayment;
                }
            }
            
            if (allRedemptions.isEmpty() && bookingIds.isEmpty()) {
                System.out.println("⚠️ No redemptions found for this voucher");
                return List.of();
            }
            
            // Debug: Check each redemption
            int redemptionsWithBooking = 0;
            int redemptionsWithoutBooking = 0;
            for (VoucherRedemption r : allRedemptions) {
                try {
                    // Try to access booking - this might trigger lazy loading
                    if (r.getBooking() != null) {
                        Integer bid = r.getBooking().getBookingId();
                        System.out.println("   ✅ Redemption ID: " + r.getRedemptionId() + ", Booking ID: " + bid);
                        redemptionsWithBooking++;
                    } else {
                        System.out.println("   ❌ Redemption ID: " + r.getRedemptionId() + ", Booking: NULL");
                        redemptionsWithoutBooking++;
                    }
                } catch (Exception e) {
                    System.out.println("   ⚠️ Redemption ID: " + r.getRedemptionId() + ", Error accessing booking: " + e.getMessage());
                    redemptionsWithoutBooking++;
                }
            }
            System.out.println("📈 Summary: " + redemptionsWithBooking + " with booking, " + redemptionsWithoutBooking + " without booking");
            
            // If native query returns empty, try JPQL query
            if (bookingIds.isEmpty() && redemptionsWithBooking > 0) {
                System.out.println("⚠️ Native query returned empty but found redemptions with booking, trying JPQL query...");
                try {
                    bookingIds = voucherRedemptionRepository.findBookingIdsByVoucherId(id);
                    System.out.println("📋 JPQL query found " + bookingIds.size() + " booking IDs");
                } catch (Exception e) {
                    System.out.println("❌ JPQL query failed: " + e.getMessage());
                }
            }
            
            if (!bookingIds.isEmpty()) {
                System.out.println("✅ Returning booking IDs: " + bookingIds);
            } else {
                System.out.println("⚠️ No booking IDs found. This voucher may have been used but booking_id was not set in voucher_redemption table.");
            }
            
            return bookingIds;
                
        } catch (Exception e) {
            System.err.println("❌ Error getting voucher bookings: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
}
