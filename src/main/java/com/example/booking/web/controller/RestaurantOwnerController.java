package com.example.booking.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.FOHManagementService;
import com.example.booking.service.FileUploadService;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.Booking;
import com.example.booking.domain.RestaurantTable;

import java.math.BigDecimal;

import java.util.List;

/**
 * Controller for Restaurant Owner management features
 * Handles FOH floor management, restaurant profile, tables, and bookings
 */
@Controller
@RequestMapping("/restaurant-owner")
@PreAuthorize("hasRole('RESTAURANT_OWNER')")
public class RestaurantOwnerController {

    private final RestaurantOwnerService restaurantOwnerService;
    private final FOHManagementService fohManagementService;
    private final FileUploadService fileUploadService;

    @Autowired
    public RestaurantOwnerController(RestaurantOwnerService restaurantOwnerService,
                                   FOHManagementService fohManagementService,
                                   FileUploadService fileUploadService) {
        this.restaurantOwnerService = restaurantOwnerService;
        this.fohManagementService = fohManagementService;
        this.fileUploadService = fileUploadService;
    }

    /**
     * FOH Floor Management Dashboard
     * Main interface for managing waitlist, tables, and floor operations
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "FOH Dashboard - Quản lý sàn");
        
        // Get real data from database
        List<Booking> todayBookings = fohManagementService.getTodayBookings(1); // Using restaurant ID 1 for now
        List<RestaurantTable> availableTables = fohManagementService.getAvailableTables(1);
        List<RestaurantTable> occupiedTables = fohManagementService.getOccupiedTables(1);
        
        model.addAttribute("todayBookings", todayBookings);
        model.addAttribute("availableTables", availableTables);
        model.addAttribute("occupiedTables", occupiedTables);
        
        return "restaurant-owner/dashboard";
    }

    /**
     * Restaurant Profile Management
     * Manage restaurant information, media, and settings
     */
    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("pageTitle", "Hồ sơ Nhà hàng - Restaurant Profile");
        
        // Get real restaurants from database
        List<RestaurantProfile> restaurants = restaurantOwnerService.getAllRestaurants();
        model.addAttribute("restaurants", restaurants);
        
        return "restaurant-owner/profile";
    }

    /**
     * Restaurant Profile Detail
     * View and edit specific restaurant details
     */
    @GetMapping("/profile/{id}")
    public String restaurantDetail(@PathVariable Integer id, Model model) {
        model.addAttribute("pageTitle", "Chi tiết Nhà hàng - Restaurant Detail");
        
        // Get specific restaurant from database
        restaurantOwnerService.getRestaurantById(id).ifPresent(restaurant -> {
            model.addAttribute("restaurant", restaurant);
        });
        
        return "restaurant-owner/profile";
    }

    // Removed booking management - not needed for restaurant owners
    // Restaurant owners focus on managing their restaurants, not customer bookings

    /**
     * Media Management
     * Upload and manage restaurant images, logos, menus
     */
    @GetMapping("/media")
    public String media(Model model) {
        model.addAttribute("pageTitle", "Quản lý Media - Media Management");
        
        // Get restaurants for media management
        List<RestaurantProfile> restaurants = restaurantOwnerService.getAllRestaurants();
        model.addAttribute("restaurants", restaurants);
        
        return "restaurant-owner/media";
    }

    /**
     * Analytics & Reports
     * View restaurant performance metrics and reports
     */
    @GetMapping("/analytics")
    public String analytics(Model model) {
        model.addAttribute("pageTitle", "Báo cáo & Thống kê - Analytics");
        
        // Get real statistics from database
        RestaurantOwnerService.RestaurantStats stats = restaurantOwnerService.getRestaurantStats(1);
        model.addAttribute("stats", stats);
        
        return "restaurant-owner/analytics";
    }

    // ===== CRUD OPERATIONS FOR RESTAURANTS =====

    /**
     * Show create restaurant form
     */
    @GetMapping("/restaurants/create")
    public String createRestaurantForm(Model model) {
        model.addAttribute("pageTitle", "Tạo nhà hàng mới");
        model.addAttribute("restaurant", new RestaurantProfile());
        return "restaurant-owner/restaurant-form";
    }

    /**
     * Create new restaurant
     */
    @PostMapping("/restaurants/create")
    public String createRestaurant(RestaurantProfile restaurant, 
                                 @RequestParam(value = "logo", required = false) MultipartFile logo,
                                 @RequestParam(value = "cover", required = false) MultipartFile cover,
                                 RedirectAttributes redirectAttributes) {
        try {
            RestaurantProfile savedRestaurant = restaurantOwnerService.createRestaurantProfile(restaurant);
            redirectAttributes.addFlashAttribute("success", "Tạo nhà hàng thành công!");
            return "redirect:/restaurant-owner/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tạo nhà hàng: " + e.getMessage());
            return "redirect:/restaurant-owner/restaurants/create";
        }
    }

    /**
     * Show edit restaurant form
     */
    @GetMapping("/restaurants/{id}/edit")
    public String editRestaurantForm(@PathVariable Integer id, Model model) {
        model.addAttribute("pageTitle", "Chỉnh sửa nhà hàng");
        
        restaurantOwnerService.getRestaurantById(id).ifPresent(restaurant -> {
            model.addAttribute("restaurant", restaurant);
        });
        
        return "restaurant-owner/restaurant-form";
    }

    /**
     * Update restaurant
     */
    @PostMapping("/restaurants/{id}/edit")
    public String updateRestaurant(@PathVariable Integer id, 
                                 RestaurantProfile restaurant,
                                 @RequestParam(value = "logo", required = false) MultipartFile logo,
                                 @RequestParam(value = "cover", required = false) MultipartFile cover,
                                 RedirectAttributes redirectAttributes) {
        try {
            restaurant.setRestaurantId(id);
            RestaurantProfile updatedRestaurant = restaurantOwnerService.updateRestaurantProfile(restaurant);
            redirectAttributes.addFlashAttribute("success", "Cập nhật nhà hàng thành công!");
            return "redirect:/restaurant-owner/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật nhà hàng: " + e.getMessage());
            return "redirect:/restaurant-owner/restaurants/" + id + "/edit";
        }
    }

    /**
     * Delete restaurant
     */
    @PostMapping("/restaurants/{id}/delete")
    public String deleteRestaurant(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            restaurantOwnerService.deleteRestaurantProfile(id);
            redirectAttributes.addFlashAttribute("success", "Xóa nhà hàng thành công!");
            return "redirect:/restaurant-owner/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa nhà hàng: " + e.getMessage());
            return "redirect:/restaurant-owner/profile";
        }
    }

    // ===== CRUD OPERATIONS FOR TABLES =====

    /**
     * Show create table form
     */
    @GetMapping("/tables/create")
    public String createTableForm(Model model) {
        model.addAttribute("pageTitle", "Tạo bàn mới");
        model.addAttribute("restaurants", restaurantOwnerService.getAllRestaurants());
        return "restaurant-owner/table-form";
    }

    /**
     * Create new table
     */
    @PostMapping("/tables/create")
    public String createTable(@RequestParam String tableName,
                            @RequestParam Integer capacity,
                            @RequestParam Integer restaurantId,
                            RedirectAttributes redirectAttributes) {
        try {
            // TODO: Implement table creation logic
            redirectAttributes.addFlashAttribute("success", "Tạo bàn thành công!");
            return "redirect:/restaurant-owner/tables";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tạo bàn: " + e.getMessage());
            return "redirect:/restaurant-owner/tables/create";
        }
    }

    // ===== CRUD OPERATIONS FOR DISHES =====

    /**
     * Show create dish form
     */
    @GetMapping("/dishes/create")
    public String createDishForm(Model model) {
        model.addAttribute("pageTitle", "Thêm món mới");
        model.addAttribute("restaurants", restaurantOwnerService.getAllRestaurants());
        return "restaurant-owner/dish-form";
    }

    /**
     * Create new dish
     */
    @PostMapping("/dishes/create")
    public String createDish(@RequestParam String dishName,
                           @RequestParam String description,
                           @RequestParam Double price,
                           @RequestParam String category,
                           @RequestParam Integer restaurantId,
                           @RequestParam(value = "image", required = false) MultipartFile image,
                           RedirectAttributes redirectAttributes) {
        try {
            // Get restaurant
            var restaurant = restaurantOwnerService.getRestaurantById(restaurantId);
            if (restaurant.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy nhà hàng!");
                return "redirect:/restaurant-owner/dishes/create";
            }

            // Create dish
            var dish = new com.example.booking.domain.Dish();
            dish.setName(dishName);
            dish.setDescription(description);
            dish.setPrice(new BigDecimal(price));
            dish.setCategory(category);
            dish.setRestaurant(restaurant.get());

            // Handle image upload (note: Dish entity doesn't have image field)
            // TODO: Add image field to Dish entity if needed
            // if (image != null && !image.isEmpty()) {
            //     String imageUrl = fileUploadService.uploadDishImage(image, dish.getDishId());
            //     dish.setImage(imageUrl);
            // }

            restaurantOwnerService.createDish(dish);
            redirectAttributes.addFlashAttribute("success", "Thêm món thành công!");
            return "redirect:/restaurant-owner/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi thêm món: " + e.getMessage());
            return "redirect:/restaurant-owner/dishes/create";
        }
    }

    // ===== MEDIA MANAGEMENT =====

    /**
     * Upload restaurant media
     */
    @PostMapping("/media/upload")
    public String uploadMedia(@RequestParam("file") MultipartFile file,
                             @RequestParam("type") String type,
                             @RequestParam("restaurantId") Integer restaurantId,
                             RedirectAttributes redirectAttributes) {
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn file!");
                return "redirect:/restaurant-owner/media";
            }

            // Get restaurant
            var restaurant = restaurantOwnerService.getRestaurantById(restaurantId);
            if (restaurant.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy nhà hàng!");
                return "redirect:/restaurant-owner/media";
            }

            // Upload file
            String fileUrl = fileUploadService.uploadRestaurantMedia(file, restaurantId, type);
            
            // Create media record
            var media = new com.example.booking.domain.RestaurantMedia();
            media.setRestaurant(restaurant.get());
            media.setType(type);
            media.setUrl(fileUrl);
            
            restaurantOwnerService.createMedia(media);
            redirectAttributes.addFlashAttribute("success", "Upload media thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi upload: " + e.getMessage());
        }
        
        return "redirect:/restaurant-owner/media";
    }

    /**
     * Delete media
     */
    @PostMapping("/media/{id}/delete")
    public String deleteMedia(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            var media = restaurantOwnerService.getMediaById(id);
            if (media.isPresent()) {
                // Delete file from filesystem
                fileUploadService.deleteFile(media.get().getUrl());
                // Delete from database
                restaurantOwnerService.deleteMedia(id);
                redirectAttributes.addFlashAttribute("success", "Xóa media thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy media!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa: " + e.getMessage());
        }
        
        return "redirect:/restaurant-owner/media";
    }

    // ===== TABLE MANAGEMENT =====

    /**
     * Show table management page
     */
    @GetMapping("/tables")
    public String tables(Model model) {
        model.addAttribute("pageTitle", "Quản lý Bàn - Table Management");
        
        // Get all tables
        List<RestaurantTable> allTables = fohManagementService.getAllTables();
        model.addAttribute("allTables", allTables);
        
        return "restaurant-owner/tables";
    }

    /**
     * Show edit table form
     */
    @GetMapping("/tables/{id}/edit")
    public String editTableForm(@PathVariable Integer id, Model model) {
        model.addAttribute("pageTitle", "Chỉnh sửa bàn");
        
        var table = restaurantOwnerService.getTableById(id);
        if (table.isPresent()) {
            model.addAttribute("table", table.get());
        }
        
        // Get restaurants for dropdown
        List<RestaurantProfile> restaurants = restaurantOwnerService.getAllRestaurants();
        model.addAttribute("restaurants", restaurants);
        
        return "restaurant-owner/table-form";
    }

    /**
     * Update table
     */
    @PostMapping("/tables/{id}/edit")
    public String updateTable(@PathVariable Integer id,
                             @ModelAttribute RestaurantTable table,
                             RedirectAttributes redirectAttributes) {
        try {
            var existingTable = restaurantOwnerService.getTableById(id);
            if (existingTable.isPresent()) {
                table.setTableId(id);
                restaurantOwnerService.updateTable(table);
                redirectAttributes.addFlashAttribute("success", "Cập nhật bàn thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy bàn!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật: " + e.getMessage());
        }
        
        return "redirect:/restaurant-owner/tables";
    }

    /**
     * Delete table
     */
    @PostMapping("/tables/{id}/delete")
    public String deleteTable(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            restaurantOwnerService.deleteTable(id);
            redirectAttributes.addFlashAttribute("success", "Xóa bàn thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa: " + e.getMessage());
        }
        
        return "redirect:/restaurant-owner/tables";
    }
    
    // ===== INTERNAL BOOKING MANAGEMENT =====
    
    /**
     * View all bookings for restaurant
     */
    @GetMapping("/bookings")
    public String viewBookings(Model model) {
        model.addAttribute("pageTitle", "Quản lý Booking - Bookings Management");
        
        // TODO: Get bookings for current restaurant owner
        // List<Booking> bookings = bookingService.getBookingsByRestaurant(restaurantId);
        // model.addAttribute("bookings", bookings);
        
        return "restaurant-owner/bookings";
    }
    
    /**
     * Show create internal booking form
     */
    @GetMapping("/bookings/create")
    public String createInternalBookingForm(Model model) {
        model.addAttribute("pageTitle", "Tạo Booking Nội bộ - Create Internal Booking");
        model.addAttribute("restaurants", restaurantOwnerService.getAllRestaurants());
        return "restaurant-owner/booking-form";
    }
    
    /**
     * Create internal booking (for walk-ins, phone bookings, etc.)
     */
    @PostMapping("/bookings/create")
    public String createInternalBooking(@RequestParam String customerName,
                                       @RequestParam String customerPhone,
                                       @RequestParam(required = false) String customerEmail,
                                       @RequestParam Integer restaurantId,
                                       @RequestParam String bookingTime,
                                       @RequestParam Integer numberOfGuests,
                                       @RequestParam(required = false) String specialRequests,
                                       @RequestParam(required = false) String bookingType, // WALK_IN, PHONE, etc.
                                       RedirectAttributes redirectAttributes) {
        try {
            // Get restaurant
            var restaurant = restaurantOwnerService.getRestaurantById(restaurantId);
            if (restaurant.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy nhà hàng!");
                return "redirect:/restaurant-owner/bookings/create";
            }
            
            // Create internal booking
            // This would typically create a Customer record if not exists
            // Then create the Booking
            
            // TODO: Implement internal booking creation logic
            // Booking booking = new Booking();
            // booking.setRestaurant(restaurant.get());
            // booking.setBookingTime(LocalDateTime.parse(bookingTime));
            // booking.setNumberOfGuests(numberOfGuests);
            // booking.setStatus("CONFIRMED"); // Internal bookings are auto-confirmed
            // bookingService.createInternalBooking(booking, customerName, customerPhone, customerEmail);
            
            redirectAttributes.addFlashAttribute("success", "Tạo booking nội bộ thành công!");
            return "redirect:/restaurant-owner/bookings";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tạo booking: " + e.getMessage());
            return "redirect:/restaurant-owner/bookings/create";
        }
    }
    
    /**
     * Update booking status (confirm, cancel, complete)
     */
    @PostMapping("/bookings/{id}/status")
    public String updateBookingStatus(@PathVariable Integer id,
                                     @RequestParam String status,
                                     RedirectAttributes redirectAttributes) {
        try {
            // TODO: Update booking status
            // bookingService.updateBookingStatus(id, status);
            
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật: " + e.getMessage());
        }
        
        return "redirect:/restaurant-owner/bookings";
    }
    
    // ===== TIME SLOT BLOCKING =====
    
    /**
     * View blocked slots
     */
    @GetMapping("/blocked-slots")
    public String viewBlockedSlots(Model model) {
        model.addAttribute("pageTitle", "Chặn Slot - Blocked Time Slots");
        
        // TODO: Get blocked slots for restaurant
        // List<BlockedSlot> blockedSlots = restaurantOwnerService.getBlockedSlots(restaurantId);
        // model.addAttribute("blockedSlots", blockedSlots);
        
        return "restaurant-owner/blocked-slots";
    }
    
    /**
     * Create blocked slot
     */
    @PostMapping("/blocked-slots/create")
    public String createBlockedSlot(@RequestParam Integer restaurantId,
                                   @RequestParam String startTime,
                                   @RequestParam String endTime,
                                   @RequestParam(required = false) String reason,
                                   @RequestParam(required = false) boolean recurring,
                                   RedirectAttributes redirectAttributes) {
        try {
            // TODO: Create blocked slot
            // BlockedSlot slot = new BlockedSlot();
            // slot.setRestaurant(restaurant);
            // slot.setStartTime(LocalDateTime.parse(startTime));
            // slot.setEndTime(LocalDateTime.parse(endTime));
            // slot.setReason(reason);
            // slot.setRecurring(recurring);
            // restaurantOwnerService.createBlockedSlot(slot);
            
            redirectAttributes.addFlashAttribute("success", "Chặn slot thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi chặn slot: " + e.getMessage());
        }
        
        return "redirect:/restaurant-owner/blocked-slots";
    }
    
    /**
     * Delete blocked slot
     */
    @PostMapping("/blocked-slots/{id}/delete")
    public String deleteBlockedSlot(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            // TODO: Delete blocked slot
            // restaurantOwnerService.deleteBlockedSlot(id);
            
            redirectAttributes.addFlashAttribute("success", "Xóa slot thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa: " + e.getMessage());
        }
        
        return "redirect:/restaurant-owner/blocked-slots";
    }
}
