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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.booking.service.RestaurantOwnerService;
import com.example.booking.service.FOHManagementService;
import com.example.booking.service.ImageUploadService;
import com.example.booking.domain.RestaurantMedia;
import com.example.booking.domain.Dish;
import com.example.booking.domain.DishStatus;
import com.example.booking.dto.DishWithImageDto;
import com.example.booking.service.WaitlistService;
import com.example.booking.service.BookingService;
import com.example.booking.service.RestaurantManagementService;
import com.example.booking.service.SimpleUserService;
import com.example.booking.domain.Booking;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.RestaurantService;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.domain.Waitlist;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.User;
import com.example.booking.dto.BookingForm;
import com.example.booking.common.enums.BookingStatus;
import com.example.booking.dto.WaitlistDetailDto;

import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.security.core.Authentication;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for Restaurant Owner management features
 * Handles FOH floor management, restaurant profile, tables, and bookings
 */
@Controller
@RequestMapping("/restaurant-owner")
@PreAuthorize("hasRole('RESTAURANT_OWNER')")
public class RestaurantOwnerController {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantOwnerController.class);

    private final RestaurantOwnerService restaurantOwnerService;
    private final FOHManagementService fohManagementService;
    private final WaitlistService waitlistService;
    private final BookingService bookingService;
    private final RestaurantManagementService restaurantService;
    private final SimpleUserService userService;
    private final ImageUploadService imageUploadService;

    @Autowired
    public RestaurantOwnerController(RestaurantOwnerService restaurantOwnerService,
                                   FOHManagementService fohManagementService,
            WaitlistService waitlistService,
            BookingService bookingService,
            RestaurantManagementService restaurantService,
            SimpleUserService userService,
            ImageUploadService imageUploadService) {
        this.restaurantOwnerService = restaurantOwnerService;
        this.fohManagementService = fohManagementService;
        this.waitlistService = waitlistService;
        this.bookingService = bookingService;
        this.restaurantService = restaurantService;
        this.userService = userService;
        this.imageUploadService = imageUploadService;
    }

    /**
     * FOH Floor Management Dashboard
     * Main interface for managing waitlist, tables, and floor operations
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        // Kiểm tra user có thể truy cập dashboard không (có ít nhất 1 restaurant approved)
        if (!canAccessDashboard(authentication)) {
            // Redirect đến trang tạo restaurant nếu chưa có restaurant nào
            return "redirect:/restaurant-owner/restaurants/create?message=no_approved_restaurant";
        }
        model.addAttribute("pageTitle", "FOH Dashboard - Quản lý sàn");
        
        try {
            // Get all restaurants owned by current user
            List<RestaurantProfile> restaurants = getAllRestaurantsByOwner(authentication);

            if (restaurants.isEmpty()) {
                model.addAttribute("error", "Không tìm thấy nhà hàng nào của bạn. Vui lòng tạo nhà hàng trước.");
                return "restaurant-owner/dashboard";
            }

            // Use first restaurant for FOH data (can be enhanced to support multiple
            // restaurants)
            Integer restaurantId = restaurants.get(0).getRestaurantId();

            // Get real data from database
            List<Booking> todayBookings = fohManagementService.getTodayBookings(restaurantId);
            List<RestaurantTable> availableTables = fohManagementService.getAvailableTables(restaurantId);
            List<RestaurantTable> occupiedTables = fohManagementService.getOccupiedTables(restaurantId);

            // Get waitlist data
            List<Waitlist> waitingCustomers = waitlistService.getWaitlistByRestaurant(restaurantId);
            List<Waitlist> calledCustomers = waitlistService.getCalledCustomers(restaurantId);

            model.addAttribute("restaurants", restaurants);
            model.addAttribute("todayBookings", todayBookings);
            model.addAttribute("availableTables", availableTables);
            model.addAttribute("occupiedTables", occupiedTables);
            model.addAttribute("waitingCustomers", waitingCustomers);
            model.addAttribute("calledCustomers", calledCustomers);

        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tải dữ liệu: " + e.getMessage());
            System.err.println("❌ Error in dashboard: " + e.getMessage());
            e.printStackTrace();
        }

        return "restaurant-owner/dashboard";
    }

    /**
     * Restaurant Profile Management
     * Manage restaurant information, media, and settings
     */
    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        model.addAttribute("pageTitle", "Hồ sơ Nhà hàng - Restaurant Profile");
        
        try {
            // Get restaurants owned by current user
            List<RestaurantProfile> restaurants = getAllRestaurantsByOwner(authentication);
            model.addAttribute("restaurants", restaurants);
        } catch (Exception e) {
            logger.error("Error loading restaurants for profile: {}", e.getMessage(), e);
            model.addAttribute("error", "Lỗi khi tải danh sách nhà hàng: " + e.getMessage());
            model.addAttribute("restaurants", new ArrayList<>());
        }
        
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

    // ===== MEDIA MANAGEMENT REMOVED =====
    // Media management has been removed as requested

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

    // ===== RESTAURANT CREATION MOVED TO RestaurantRegistrationController =====
    // Các method createRestaurantForm và createRestaurant đã được chuyển sang 
    // RestaurantRegistrationController để cho phép CUSTOMER tạo nhà hàng

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
            @RequestParam(value = "businessLicense", required = false) MultipartFile businessLicense,
                                 RedirectAttributes redirectAttributes) {
        try {
            restaurant.setRestaurantId(id);

            // Xử lý upload logo nếu có
            if (logo != null && !logo.isEmpty()) {
                try {
                    // Lấy URL logo cũ
                    String oldLogoUrl = null;
                    List<RestaurantMedia> existingMedia = restaurant.getMedia();
                    if (existingMedia != null) {
                        for (RestaurantMedia media : existingMedia) {
                            if ("logo".equals(media.getType()) && media.getUrl() != null && media.getUrl().startsWith("http")) {
                                oldLogoUrl = media.getUrl();
                                break;
                            }
                        }
                        existingMedia.removeIf(media -> "logo".equals(media.getType()));
                    }

                    // Sử dụng updateRestaurantImage (tự động xóa cũ + upload mới)
                    String logoUrl = imageUploadService.updateRestaurantImage(logo, oldLogoUrl, id, "logo");

                    // Thêm logo mới
                    RestaurantMedia logoMedia = new RestaurantMedia();
                    logoMedia.setRestaurant(restaurant);
                    logoMedia.setType("logo");
                    logoMedia.setUrl(logoUrl);
                    if (restaurant.getMedia() == null) {
                        restaurant.setMedia(new ArrayList<>());
                    }
                    restaurant.getMedia().add(logoMedia);
                } catch (Exception e) {
                    logger.warn("Failed to upload logo: {}", e.getMessage());
                }
            }

            // Xử lý upload cover nếu có
            if (cover != null && !cover.isEmpty()) {
                try {
                    // Lấy URL cover cũ
                    String oldCoverUrl = null;
                    List<RestaurantMedia> existingMedia = restaurant.getMedia();
                    if (existingMedia != null) {
                        for (RestaurantMedia media : existingMedia) {
                            if ("cover".equals(media.getType()) && media.getUrl() != null && media.getUrl().startsWith("http")) {
                                oldCoverUrl = media.getUrl();
                                break;
                            }
                        }
                        existingMedia.removeIf(media -> "cover".equals(media.getType()));
                    }

                    // Sử dụng updateRestaurantImage (tự động xóa cũ + upload mới)
                    String coverUrl = imageUploadService.updateRestaurantImage(cover, oldCoverUrl, id, "cover");

                    // Thêm cover mới
                    RestaurantMedia coverMedia = new RestaurantMedia();
                    coverMedia.setRestaurant(restaurant);
                    coverMedia.setType("cover");
                    coverMedia.setUrl(coverUrl);
                    if (restaurant.getMedia() == null) {
                        restaurant.setMedia(new ArrayList<>());
                    }
                    restaurant.getMedia().add(coverMedia);
                } catch (Exception e) {
                    logger.warn("Failed to upload cover: {}", e.getMessage());
                }
            }

            // Xử lý upload business license nếu có
            if (businessLicense != null && !businessLicense.isEmpty()) {
                try {
                    // Xóa business license cũ nếu có
                    String oldBusinessLicenseUrl = restaurant.getBusinessLicenseFile();
                    if (oldBusinessLicenseUrl != null && !oldBusinessLicenseUrl.isEmpty()) {
                        imageUploadService.deleteImage(oldBusinessLicenseUrl);
                    }

                    // Upload business license mới
                    String businessLicenseUrl = imageUploadService.uploadBusinessLicense(businessLicense, id);
                    restaurant.setBusinessLicenseFile(businessLicenseUrl);
                } catch (Exception e) {
                    logger.warn("Failed to upload business license: {}", e.getMessage());
                }
            }

            restaurantOwnerService.updateRestaurantProfile(restaurant);
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

    // ===== OLD TABLE ENDPOINTS REMOVED =====
    // Old table endpoints have been replaced with restaurant-specific endpoints

    // ===== DISH MANAGEMENT =====

    /**
     * Show dish management page for specific restaurant
     */
    @GetMapping("/restaurants/{restaurantId}/dishes")
    public String restaurantDishes(@PathVariable Integer restaurantId, Model model) {
        model.addAttribute("pageTitle", "Quản lý Món ăn - Dish Management");

        // Get restaurant info
        var restaurant = restaurantOwnerService.getRestaurantById(restaurantId);
        if (restaurant.isEmpty()) {
            return "redirect:/restaurant-owner/profile?error=restaurant_not_found";
        }

        model.addAttribute("restaurant", restaurant.get());

        // Get dishes for this restaurant
        List<DishWithImageDto> dishes = restaurantOwnerService.getDishesByRestaurantWithImages(restaurantId);
        model.addAttribute("dishes", dishes);

        // Calculate statistics
        long totalDishes = dishes.size();
        long availableDishes = dishes.stream().filter(d -> d.getStatus() == DishStatus.AVAILABLE).count();
        long outOfStockDishes = dishes.stream().filter(d -> d.getStatus() == DishStatus.OUT_OF_STOCK).count();
        long discontinuedDishes = dishes.stream().filter(d -> d.getStatus() == DishStatus.DISCONTINUED).count();

        model.addAttribute("totalDishes", totalDishes);
        model.addAttribute("availableDishes", availableDishes);
        model.addAttribute("outOfStockDishes", outOfStockDishes);
        model.addAttribute("discontinuedDishes", discontinuedDishes);

        return "restaurant-owner/restaurant-dishes";
    }

    /**
     * Show create dish form for specific restaurant
     */
    @GetMapping("/restaurants/{restaurantId}/dishes/create")
    public String createDishForm(@PathVariable Integer restaurantId, Model model) {
        model.addAttribute("pageTitle", "Thêm món mới");

        // Get restaurant info
        var restaurant = restaurantOwnerService.getRestaurantById(restaurantId);
        if (restaurant.isEmpty()) {
            return "redirect:/restaurant-owner/profile?error=restaurant_not_found";
        }

        model.addAttribute("restaurant", restaurant.get());
        model.addAttribute("dish", new Dish());

        return "restaurant-owner/dish-form";
    }

    /**
     * Create new dish
     */
    @PostMapping("/restaurants/{restaurantId}/dishes/create")
            public String createDish(@PathVariable Integer restaurantId,
            @ModelAttribute Dish dish,
            @RequestParam(value = "dishImage", required = false) MultipartFile dishImage,
                           RedirectAttributes redirectAttributes) {
        try {
            // Get restaurant
            var restaurant = restaurantOwnerService.getRestaurantById(restaurantId);
            if (restaurant.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy nhà hàng!");
                return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/dishes";
            }

            // Set restaurant and default status
            dish.setRestaurant(restaurant.get());
            if (dish.getStatus() == null) {
                dish.setStatus(com.example.booking.domain.DishStatus.AVAILABLE);
            }

            // Save dish first to get dishId
            Dish savedDish = restaurantOwnerService.createDish(dish);

            // Handle image upload
            if (dishImage != null && !dishImage.isEmpty()) {
                try {
                    String imageUrl = imageUploadService.uploadDishImage(dishImage, restaurantId,
                            savedDish.getDishId());
                    restaurantOwnerService.saveDishImage(restaurantId, savedDish.getDishId(), imageUrl);
                    logger.info("Dish image uploaded successfully: {}", imageUrl);
                } catch (Exception e) {
                    logger.error("Failed to upload dish image: {}", e.getMessage(), e);
                    // Don't fail the dish creation if image upload fails
                }
            }

            redirectAttributes.addFlashAttribute("success", "Thêm món thành công!");
            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/dishes";
        } catch (Exception e) {
            logger.error("Error creating dish: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi thêm món: " + e.getMessage());
            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/dishes/create";
        }
    }

    /**
     * Show edit dish form
     */
    @GetMapping("/restaurants/{restaurantId}/dishes/{dishId}/edit")
    public String editDishForm(@PathVariable Integer restaurantId,
            @PathVariable Integer dishId,
            Model model) {
        model.addAttribute("pageTitle", "Chỉnh sửa món ăn");

        // Get restaurant info
        var restaurant = restaurantOwnerService.getRestaurantById(restaurantId);
        if (restaurant.isEmpty()) {
            return "redirect:/restaurant-owner/profile?error=restaurant_not_found";
        }

        // Get dish info
        var dish = restaurantOwnerService.getDishById(dishId);
        if (dish.isEmpty()) {
            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/dishes?error=dish_not_found";
        }

        model.addAttribute("restaurant", restaurant.get());
        model.addAttribute("dish", dish.get());

        // Get dish image URL
        String imageUrl = restaurantOwnerService.getDishImageUrl(restaurantId, dishId);
        model.addAttribute("dishImageUrl", imageUrl);

        return "restaurant-owner/dish-form";
    }

    /**
     * Update dish
     */
    @PostMapping("/restaurants/{restaurantId}/dishes/{dishId}/edit")
    public String updateDish(@PathVariable Integer restaurantId,
            @PathVariable Integer dishId,
            @ModelAttribute Dish dish,
            @RequestParam(value = "dishImage", required = false) MultipartFile dishImage,
            RedirectAttributes redirectAttributes) {
        try {
            // Get existing dish
            var existingDish = restaurantOwnerService.getDishById(dishId);
            if (existingDish.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy món ăn!");
                return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/dishes";
            }

            // Update dish properties
            Dish dishToUpdate = existingDish.get();
            dishToUpdate.setName(dish.getName());
            dishToUpdate.setDescription(dish.getDescription());
            dishToUpdate.setPrice(dish.getPrice());
            dishToUpdate.setCategory(dish.getCategory());
            dishToUpdate.setStatus(dish.getStatus());

            // Save updated dish
            restaurantOwnerService.updateDish(dishToUpdate);

            // Handle image upload/update
            if (dishImage != null && !dishImage.isEmpty()) {
                try {
                    // Upload new image and save (saveDishImage will handle deletion of old image)
                    String imageUrl = imageUploadService.uploadDishImage(dishImage, restaurantId, dishId);
                    restaurantOwnerService.saveDishImage(restaurantId, dishId, imageUrl);
                    logger.info("Dish image updated successfully: {}", imageUrl);
                } catch (Exception e) {
                    logger.error("Failed to update dish image: {}", e.getMessage(), e);
                    // Don't fail the dish update if image upload fails
                }
            }

            redirectAttributes.addFlashAttribute("success", "Cập nhật món thành công!");
            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/dishes";
        } catch (Exception e) {
            logger.error("Error updating dish: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật món: " + e.getMessage());
            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/dishes/" + dishId + "/edit";
        }
    }

    /**
     * Delete dish
     */
    @PostMapping("/restaurants/{restaurantId}/dishes/{dishId}/delete")
    public String deleteDish(@PathVariable Integer restaurantId,
            @PathVariable Integer dishId,
            RedirectAttributes redirectAttributes) {
        try {
            // Delete dish image first
            restaurantOwnerService.deleteDishImage(restaurantId, dishId);

            // Delete dish
            restaurantOwnerService.deleteDish(dishId);

            redirectAttributes.addFlashAttribute("success", "Xóa món thành công!");
            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/dishes";
        } catch (Exception e) {
            logger.error("Error deleting dish: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa món: " + e.getMessage());
            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/dishes";
        }
    }

    // ===== MEDIA MANAGEMENT ENDPOINTS REMOVED =====
    // All media management endpoints have been removed as requested

    // ===== TABLE MANAGEMENT =====

    /**
     * Show table management page for specific restaurant
     */
    @GetMapping("/restaurants/{restaurantId}/tables")
    public String restaurantTables(@PathVariable Integer restaurantId, Model model) {
        model.addAttribute("pageTitle", "Quản lý Bàn - Table Management");

        // Get restaurant info
        var restaurant = restaurantOwnerService.getRestaurantById(restaurantId);
        if (restaurant.isEmpty()) {
            return "redirect:/restaurant-owner/profile?error=restaurant_not_found";
        }

        model.addAttribute("restaurant", restaurant.get());

        // Get tables for this restaurant
        List<RestaurantTable> tables = restaurantService.findTablesByRestaurant(restaurantId);
        model.addAttribute("tables", tables);

        // Calculate statistics
        int totalCapacity = tables.stream().mapToInt(RestaurantTable::getCapacity).sum();
        long availableTables = tables.stream().filter(t -> "AVAILABLE".equals(t.getStatus())).count();
        long occupiedTables = tables.stream().filter(t -> "OCCUPIED".equals(t.getStatus())).count();

        model.addAttribute("totalCapacity", totalCapacity);
        model.addAttribute("availableTables", availableTables);
        model.addAttribute("occupiedTables", occupiedTables);

        return "restaurant-owner/restaurant-tables";
    }

    /**
     * Show create table form for specific restaurant
     */
    @GetMapping("/restaurants/{restaurantId}/tables/create")
    public String createTableForm(@PathVariable Integer restaurantId, Model model) {
        model.addAttribute("pageTitle", "Tạo bàn mới");

        // Get restaurant info
        var restaurant = restaurantOwnerService.getRestaurantById(restaurantId);
        if (restaurant.isEmpty()) {
            return "redirect:/restaurant-owner/profile?error=restaurant_not_found";
        }

        model.addAttribute("restaurant", restaurant.get());
        model.addAttribute("table", new RestaurantTable());

        return "restaurant-owner/table-form";
    }

    /**
     * Create new table for specific restaurant
     */
    @PostMapping("/restaurants/{restaurantId}/tables/create")
    public String createTable(@PathVariable Integer restaurantId,
            @ModelAttribute RestaurantTable table,
            @RequestParam(value = "tableImages", required = false) MultipartFile[] tableImages,
            RedirectAttributes redirectAttributes) {
        try {
            // Get restaurant
            var restaurant = restaurantOwnerService.getRestaurantById(restaurantId);
            if (restaurant.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy nhà hàng!");
                return "redirect:/restaurant-owner/profile";
            }

            // Set restaurant for table
            table.setRestaurant(restaurant.get());

            // Create table first to get tableId
            logger.info("DEBUG: Creating table with restaurantId: {}", restaurantId);
            RestaurantTable savedTable = restaurantOwnerService.createTable(table);
            logger.info("DEBUG: Table created successfully with tableId: {}", savedTable.getTableId());

            // Handle table images upload after table is created
            logger.info("DEBUG: Checking table images - tableImages: {}, length: {}", tableImages,
                    tableImages != null ? tableImages.length : 0);

            if (tableImages != null && tableImages.length > 0 && !tableImages[0].isEmpty()) {
                try {
                    // Upload multiple table images to restaurant_media
                    List<RestaurantMedia> uploadedImages = restaurantOwnerService.uploadTableImages(restaurantId, savedTable.getTableId(), tableImages);
                    logger.info("Uploaded {} table images for table {}", uploadedImages.size(), savedTable.getTableId());
                } catch (Exception e) {
                    logger.warn("Failed to upload table images: {}", e.getMessage());
                }
            }

            redirectAttributes.addFlashAttribute("success", "Tạo bàn thành công!");
            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/tables";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tạo bàn: " + e.getMessage());
            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/tables/create";
        }
    }

    /**
     * Show edit table form
     */
    @GetMapping("/restaurants/{restaurantId}/tables/{tableId}/edit")
    public String editTableForm(@PathVariable Integer restaurantId,
            @PathVariable Integer tableId,
            Model model) {
        model.addAttribute("pageTitle", "Chỉnh sửa bàn");
        
        // Get restaurant info
        var restaurant = restaurantOwnerService.getRestaurantById(restaurantId);
        if (restaurant.isEmpty()) {
            return "redirect:/restaurant-owner/profile?error=restaurant_not_found";
        }
        
        // Get table info
        var table = restaurantOwnerService.getTableById(tableId);
        if (table.isEmpty()) {
            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/tables?error=table_not_found";
        }

        model.addAttribute("restaurant", restaurant.get());
        model.addAttribute("table", table.get());
        
        return "restaurant-owner/table-form";
    }

    /**
     * Update table
     */
    @PostMapping("/restaurants/{restaurantId}/tables/{tableId}/edit")
    public String updateTable(@PathVariable Integer restaurantId,
            @PathVariable Integer tableId,
                             @ModelAttribute RestaurantTable table,
            @RequestParam(value = "tableImages", required = false) MultipartFile[] tableImages,
                             RedirectAttributes redirectAttributes) {
        try {
            // Get restaurant
            var restaurant = restaurantOwnerService.getRestaurantById(restaurantId);
            if (restaurant.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy nhà hàng!");
                return "redirect:/restaurant-owner/profile";
            }

            // Get existing table
            var existingTable = restaurantOwnerService.getTableById(tableId);
            if (existingTable.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy bàn!");
                return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/tables";
            }

            // Update table basic info
            table.setTableId(tableId);
            table.setRestaurant(restaurant.get());

            // Handle table images upload - Multiple images support
            if (tableImages != null && tableImages.length > 0 && !tableImages[0].isEmpty()) {
                try {
                    // Upload multiple table images to restaurant_media
                    List<RestaurantMedia> uploadedImages = restaurantOwnerService.uploadTableImages(restaurantId, tableId, tableImages);
                    logger.info("Uploaded {} table images for table {}", uploadedImages.size(), tableId);
                } catch (Exception e) {
                    logger.warn("Failed to upload table images: {}", e.getMessage());
                }
            }

            // Update table
            restaurantOwnerService.updateTable(table);

            redirectAttributes.addFlashAttribute("success", "Cập nhật bàn thành công!");
            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/tables";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật bàn: " + e.getMessage());
            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/tables/" + tableId + "/edit";
        }
    }

    /**
     * Delete specific table image
     */
    @PostMapping("/restaurants/{restaurantId}/tables/{tableId}/images/delete/{mediaId}")
    public String deleteTableImage(@PathVariable Integer restaurantId,
                                  @PathVariable Integer tableId,
                                  @PathVariable Integer mediaId,
                                  RedirectAttributes redirectAttributes) {
        try {
            // Verify ownership
            var restaurant = restaurantOwnerService.getRestaurantById(restaurantId);
            if (restaurant.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy nhà hàng!");
                return "redirect:/restaurant-owner/profile";
            }

            // Delete the specific table image
            restaurantOwnerService.deleteTableImage(mediaId);
            redirectAttributes.addFlashAttribute("success", "Xóa ảnh bàn thành công!");
            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/tables";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa ảnh bàn: " + e.getMessage());
            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/tables";
        }
    }

    /**
     * Delete table
     */
    @PostMapping("/restaurants/{restaurantId}/tables/{tableId}/delete")
    public String deleteTable(@PathVariable Integer restaurantId,
            @PathVariable Integer tableId,
            RedirectAttributes redirectAttributes) {
        try {
            // Check if table exists
            var table = restaurantOwnerService.getTableById(tableId);
            if (table.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy bàn!");
                return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/tables";
            }

            // Delete all table images from restaurant_media
            List<RestaurantMedia> tableImages = restaurantOwnerService.getTableImages(restaurantId, tableId);
            for (RestaurantMedia image : tableImages) {
                if (image.getUrl() != null && image.getUrl().startsWith("http")) {
                    imageUploadService.deleteImage(image.getUrl());
                }
            }

            // Delete table
            restaurantOwnerService.deleteTable(tableId);
            redirectAttributes.addFlashAttribute("success", "Xóa bàn thành công!");

            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/tables";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa bàn: " + e.getMessage());
            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/tables";
        }
    }
    
    // ===== RESTAURANT MEDIA MANAGEMENT =====

    /**
     * Show restaurant media management page
     */
    @GetMapping("/restaurants/{restaurantId}/media")
    public String showRestaurantMedia(@PathVariable Integer restaurantId, Model model, Authentication authentication) {
        try {
            // Verify ownership
            var restaurant = restaurantOwnerService.getRestaurantById(restaurantId);
            if (restaurant.isEmpty()) {
                return "redirect:/restaurant-owner/profile";
            }

            // Check if user owns this restaurant
            String username = authentication.getName();
            if (!restaurant.get().getOwner().getUser().getUsername().equals(username)) {
                return "redirect:/restaurant-owner/profile";
            }

            // Get media for management
            List<RestaurantMedia> mediaList = restaurantOwnerService.getRestaurantMediaForManagement(restaurantId);

            model.addAttribute("restaurant", restaurant.get());
            model.addAttribute("mediaList", mediaList);
            return "restaurant-owner/restaurant-media";
        } catch (Exception e) {
            logger.error("Error showing restaurant media: {}", e.getMessage(), e);
            return "redirect:/restaurant-owner/profile";
        }
    }

    /**
     * Show media upload form
     */
    @GetMapping("/restaurants/{restaurantId}/media/upload")
    public String showMediaUploadForm(@PathVariable Integer restaurantId, Model model, Authentication authentication) {
        try {
            // Verify ownership
            var restaurant = restaurantOwnerService.getRestaurantById(restaurantId);
            if (restaurant.isEmpty()) {
                return "redirect:/restaurant-owner/profile";
            }

            // Check if user owns this restaurant
            String username = authentication.getName();
            if (!restaurant.get().getOwner().getUser().getUsername().equals(username)) {
                return "redirect:/restaurant-owner/profile";
            }

            model.addAttribute("restaurant", restaurant.get());
            return "restaurant-owner/media-upload-form";
        } catch (Exception e) {
            logger.error("Error showing media upload form: {}", e.getMessage(), e);
            return "redirect:/restaurant-owner/profile";
        }
    }

    /**
     * Upload restaurant media
     */
    @PostMapping("/restaurants/{restaurantId}/media/upload")
    public String uploadMedia(@PathVariable Integer restaurantId,
                             @RequestParam("mediaType") String mediaType,
                             @RequestParam("mediaFiles") MultipartFile[] mediaFiles,
                             RedirectAttributes redirectAttributes,
                             Authentication authentication) {
        try {
            // Verify ownership
            var restaurant = restaurantOwnerService.getRestaurantById(restaurantId);
            if (restaurant.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy nhà hàng!");
                return "redirect:/restaurant-owner/profile";
            }

            // Check if user owns this restaurant
            String username = authentication.getName();
            if (!restaurant.get().getOwner().getUser().getUsername().equals(username)) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền upload media cho nhà hàng này!");
                return "redirect:/restaurant-owner/profile";
            }

            // Validate files
            if (mediaFiles == null || mediaFiles.length == 0) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn ít nhất một file!");
                return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/media/upload";
            }

            // Upload media
            List<RestaurantMedia> uploadedMedia = restaurantOwnerService.uploadRestaurantMedia(restaurantId, mediaType, mediaFiles);
            
            if (uploadedMedia.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không thể upload file nào!");
            } else {
                redirectAttributes.addFlashAttribute("success", "Upload thành công " + uploadedMedia.size() + " file!");
            }

            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/media";
        } catch (Exception e) {
            logger.error("Error uploading media: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi upload media: " + e.getMessage());
            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/media/upload";
        }
    }

    /**
     * Delete restaurant media
     */
    @PostMapping("/restaurants/{restaurantId}/media/{mediaId}/delete")
    public String deleteMedia(@PathVariable Integer restaurantId,
                             @PathVariable Integer mediaId,
                             RedirectAttributes redirectAttributes,
                             Authentication authentication) {
        try {
            // Verify ownership
            var restaurant = restaurantOwnerService.getRestaurantById(restaurantId);
            if (restaurant.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy nhà hàng!");
                return "redirect:/restaurant-owner/profile";
            }

            // Check if user owns this restaurant
            String username = authentication.getName();
            if (!restaurant.get().getOwner().getUser().getUsername().equals(username)) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền xóa media của nhà hàng này!");
                return "redirect:/restaurant-owner/profile";
            }

            // Delete media
            restaurantOwnerService.deleteRestaurantMedia(mediaId);
            redirectAttributes.addFlashAttribute("success", "Xóa media thành công!");

            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/media";
        } catch (Exception e) {
            logger.error("Error deleting media: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa media: " + e.getMessage());
            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/media";
        }
    }

    // ===== INTERNAL BOOKING MANAGEMENT =====
    
    /**
     * View all bookings for all restaurants owned by current user
     */
    @GetMapping("/bookings")
    public String viewAllBookings(@RequestParam(required = false) String status,
            @RequestParam(required = false) String date,
            Authentication authentication,
            Model model) {
        model.addAttribute("pageTitle", "Quản lý Booking - Tất cả nhà hàng");

        try {
            // Get all restaurants owned by current user
            List<RestaurantProfile> restaurants = getAllRestaurantsByOwner(authentication);

            if (restaurants.isEmpty()) {
                model.addAttribute("error", "Không tìm thấy nhà hàng nào của bạn. Vui lòng tạo nhà hàng trước.");
                return "restaurant-owner/bookings";
            }

            // Get all bookings from all restaurants
            List<Booking> allBookings = new ArrayList<>();
            for (RestaurantProfile restaurant : restaurants) {
                List<Booking> restaurantBookings = bookingService.getBookingsByRestaurant(restaurant.getRestaurantId());
                allBookings.addAll(restaurantBookings);
            }

            // Sort by booking time desc
            allBookings.sort((b1, b2) -> b2.getBookingTime().compareTo(b1.getBookingTime()));

            // Filter by status if provided
            if (status != null && !status.isEmpty()) {
                try {
                    BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
                    allBookings = allBookings.stream()
                            .filter(booking -> booking.getStatus() == bookingStatus)
                            .toList();
                } catch (IllegalArgumentException e) {
                    // Invalid status, ignore filter
                }
            }

            // Get statistics
            long totalBookings = allBookings.size();
            long pendingBookings = allBookings.stream().filter(b -> b.getStatus() == BookingStatus.PENDING).count();
            long confirmedBookings = allBookings.stream().filter(b -> b.getStatus() == BookingStatus.CONFIRMED).count();
            long cancelledBookings = allBookings.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED).count();

            model.addAttribute("bookings", allBookings);
            model.addAttribute("restaurants", restaurants);
            model.addAttribute("totalBookings", totalBookings);
            model.addAttribute("pendingBookings", pendingBookings);
            model.addAttribute("confirmedBookings", confirmedBookings);
            model.addAttribute("cancelledBookings", cancelledBookings);
            model.addAttribute("selectedStatus", status);
            model.addAttribute("selectedDate", date);
            model.addAttribute("isAllRestaurants", true);

        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tải dữ liệu: " + e.getMessage());
            System.err.println("❌ Error in viewAllBookings: " + e.getMessage());
            e.printStackTrace();
        }

        return "restaurant-owner/bookings";
    }

    /**
     * View bookings for a specific restaurant
     */
    @GetMapping("/restaurants/{id}/bookings")
    public String viewRestaurantBookings(@PathVariable Integer id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String date,
            Authentication authentication,
            Model model) {
        model.addAttribute("pageTitle", "Quản lý Booking - Nhà hàng cụ thể");

        try {
            // Verify restaurant ownership
            List<RestaurantProfile> ownedRestaurants = getAllRestaurantsByOwner(authentication);
            Optional<RestaurantProfile> targetRestaurant = ownedRestaurants.stream()
                    .filter(r -> r.getRestaurantId().equals(id))
                    .findFirst();

            if (targetRestaurant.isEmpty()) {
                model.addAttribute("error", "Bạn không có quyền truy cập nhà hàng này.");
                return "restaurant-owner/bookings";
            }

            // Get bookings for specific restaurant
            List<Booking> bookings = bookingService.getBookingsByRestaurant(id);

            // Filter by status if provided
            if (status != null && !status.isEmpty()) {
                try {
                    BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
                    bookings = bookings.stream()
                            .filter(booking -> booking.getStatus() == bookingStatus)
                            .toList();
                } catch (IllegalArgumentException e) {
                    // Invalid status, ignore filter
                }
            }

            // Get statistics
            long totalBookings = bookings.size();
            long pendingBookings = bookings.stream().filter(b -> b.getStatus() == BookingStatus.PENDING).count();
            long confirmedBookings = bookings.stream().filter(b -> b.getStatus() == BookingStatus.CONFIRMED).count();
            long cancelledBookings = bookings.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED).count();

            model.addAttribute("bookings", bookings);
            model.addAttribute("restaurants", ownedRestaurants);
            model.addAttribute("currentRestaurant", targetRestaurant.get());
            model.addAttribute("totalBookings", totalBookings);
            model.addAttribute("pendingBookings", pendingBookings);
            model.addAttribute("confirmedBookings", confirmedBookings);
            model.addAttribute("cancelledBookings", cancelledBookings);
            model.addAttribute("selectedStatus", status);
            model.addAttribute("selectedDate", date);
            model.addAttribute("restaurantId", id);
            model.addAttribute("isAllRestaurants", false);

        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tải dữ liệu: " + e.getMessage());
            System.err.println("❌ Error in viewRestaurantBookings: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "restaurant-owner/bookings";
    }
    
    /**
     * View booking detail
     */
    @GetMapping("/bookings/{id}")
    public String viewBookingDetail(@PathVariable Integer id, Model model) {
        model.addAttribute("pageTitle", "Chi tiết Booking - Booking Detail");

        // Get booking detail
        var booking = bookingService.getBookingDetailById(id);
        if (booking.isPresent()) {
            model.addAttribute("booking", booking.get());

            // Calculate total amount
            BigDecimal totalAmount = bookingService.calculateTotalAmount(booking.get());
            model.addAttribute("totalAmount", totalAmount);

            return "restaurant-owner/booking-detail";
        } else {
            model.addAttribute("error", "Không tìm thấy booking với ID: " + id);
            return "redirect:/restaurant-owner/bookings";
        }
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
            // Convert string to BookingStatus enum
            BookingStatus newStatus = BookingStatus.valueOf(status.toUpperCase());

            // Update booking status
            bookingService.updateBookingStatus(id, newStatus);
            
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái thành công!");
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Trạng thái không hợp lệ: " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật: " + e.getMessage());
        }
        
        return "redirect:/restaurant-owner/bookings";
    }
    
    /**
     * Cancel booking (Restaurant Owner)
     */
    @PostMapping("/bookings/{id}/cancel")
    public String cancelBooking(@PathVariable Integer id,
            @RequestParam String cancelReason,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            // Get current restaurant owner
            User user = getUserFromAuthentication(authentication);
            UUID restaurantOwnerId = user.getId();

            // Cancel booking with refund processing
            bookingService.cancelBookingByRestaurant(id, restaurantOwnerId, cancelReason);

            redirectAttributes.addFlashAttribute("success", "Đã hủy booking và tạo refund request!");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Không thể hủy booking: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi hủy booking: " + e.getMessage());
        }

        return "redirect:/restaurant-owner/bookings";
    }

    /**
     * API endpoint để cancel booking (Restaurant Owner)
     */
    @PostMapping("/api/bookings/{id}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelBookingApi(@PathVariable Integer id,
            @RequestParam String cancelReason,
            Authentication authentication) {
        try {
            // Get current restaurant owner
            User user = getUserFromAuthentication(authentication);
            UUID restaurantOwnerId = user.getId();

            // Cancel booking with refund processing
            bookingService.cancelBookingByRestaurant(id, restaurantOwnerId, cancelReason);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã hủy booking và tạo refund request!");
            response.put("bookingId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Không thể hủy booking: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Error cancelling booking for restaurant owner", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Lỗi khi hủy booking: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Show edit booking form for restaurant owner
     */
    @GetMapping("/bookings/{bookingId}/edit")
    public String showEditBookingForm(@PathVariable Integer bookingId,
            Model model,
            Authentication authentication) {
        System.out.println("🚀 RestaurantOwnerController.showEditBookingForm() called for booking ID: " + bookingId);
        try {
            // Get restaurant owner info
            User user = (User) authentication.getPrincipal();
            RestaurantOwner owner = restaurantOwnerService.getRestaurantOwnerByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Restaurant owner not found"));

            // Get restaurant ID
            Integer restaurantId = null;
            if (owner.getRestaurants() != null && !owner.getRestaurants().isEmpty()) {
                restaurantId = owner.getRestaurants().get(0).getRestaurantId();
            } else {
                restaurantId = restaurantOwnerService.getRestaurantIdByOwnerId(owner.getOwnerId());
            }

            // Get booking with details
            Booking booking = bookingService.getBookingWithDetailsById(bookingId)
                    .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

            // Validate restaurant ownership
            if (!booking.getRestaurant().getRestaurantId().equals(restaurantId)) {
                System.out.println("❌ Restaurant ownership mismatch. Booking restaurant: "
                        + booking.getRestaurant().getRestaurantId() + ", Owner restaurant: " + restaurantId);
                model.addAttribute("error", "You can only edit bookings for your own restaurant");
                return "redirect:/restaurant-owner/bookings";
            }

            // Validate booking can be edited
            if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.COMPLETED) {
                System.out.println("❌ Cannot edit booking: Status = " + booking.getStatus());
                model.addAttribute("error", "Cannot edit cancelled or completed bookings");
                return "redirect:/restaurant-owner/bookings";
            }

            // Load restaurants and tables
            List<RestaurantProfile> restaurants = restaurantService.findAllRestaurants();
            List<RestaurantTable> tables = restaurantService
                    .findTablesByRestaurant(booking.getRestaurant().getRestaurantId());

            // Load dishes and services for the restaurant
            List<com.example.booking.domain.Dish> dishes = restaurantService
                    .findDishesByRestaurant(booking.getRestaurant().getRestaurantId());
            List<com.example.booking.domain.RestaurantService> services = restaurantService
                    .findServicesByRestaurant(booking.getRestaurant().getRestaurantId());

            // Create form with current booking data
            BookingForm form = new BookingForm();
            form.setRestaurantId(booking.getRestaurant().getRestaurantId());
            form.setTableId(getCurrentTableId(booking));
            form.setTableIds(getCurrentTableIds(booking)); // Load multiple tables
            form.setGuestCount(booking.getNumberOfGuests());
            form.setBookingTime(booking.getBookingTime());
            form.setDepositAmount(booking.getDepositAmount());
            form.setNote(booking.getNote());

            // Load current dishes and services
            System.out.println("🔍 DEBUG - Loading current dishes and services...");
            String dishIds = getCurrentDishIds(booking);
            String serviceIds = getCurrentServiceIds(booking);
            System.out.println("🔍 DEBUG - dishIds: " + dishIds);
            System.out.println("🔍 DEBUG - serviceIds: " + serviceIds);
            form.setDishIds(dishIds);
            form.setServiceIds(serviceIds);

            // Debug: Log the data being sent to template
            System.out.println("🔍 DEBUG - Restaurant Owner Edit Booking Form Data:");
            System.out.println("   Booking ID: " + bookingId);
            System.out.println("   Restaurant ID: " + form.getRestaurantId());
            System.out.println("   Table ID: " + form.getTableId());
            System.out.println("   Table IDs: " + form.getTableIds());
            System.out.println("   Guest Count: " + form.getGuestCount());
            System.out.println("   Booking Time: " + form.getBookingTime());
            System.out.println("   Deposit Amount: " + form.getDepositAmount());
            System.out.println("   Dish IDs: " + form.getDishIds());
            System.out.println("   Service IDs: " + form.getServiceIds());
            System.out.println("   Note: " + form.getNote());
            System.out.println("   Available Tables: " + tables.size());
            System.out.println("   Available Dishes: " + dishes.size());
            System.out.println("   Available Services: " + services.size());

            model.addAttribute("bookingForm", form);
            model.addAttribute("restaurants", restaurants);
            model.addAttribute("tables", tables);
            model.addAttribute("dishes", dishes);
            model.addAttribute("services", services);
            model.addAttribute("booking", booking);
            model.addAttribute("bookingId", bookingId);
            model.addAttribute("pageTitle", "Chỉnh sửa đặt bàn #" + bookingId);
            model.addAttribute("isRestaurantOwner", true);

            return "booking/form";

        } catch (Exception e) {
            System.err.println("❌ Error showing edit form: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error loading booking: " + e.getMessage());
            return "redirect:/restaurant-owner/bookings";
        }
    }

    /**
     * Update booking for restaurant owner
     */
    @PostMapping("/bookings/{bookingId}/update")
    public String updateBooking(@PathVariable Integer bookingId,
            @Valid @ModelAttribute("bookingForm") BookingForm form,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Validation errors occurred");
            return "redirect:/restaurant-owner/bookings/" + bookingId + "/edit";
        }

        try {
            // Get restaurant owner info
            User user = (User) authentication.getPrincipal();
            RestaurantOwner owner = restaurantOwnerService.getRestaurantOwnerByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Restaurant owner not found"));

            // Get all restaurant IDs owned by this owner
            Set<Integer> ownedRestaurantIds = new HashSet<>();
            if (owner.getRestaurants() != null) {
                for (RestaurantProfile restaurant : owner.getRestaurants()) {
                    ownedRestaurantIds.add(restaurant.getRestaurantId());
                }
            }

            // Update booking
            Booking updatedBooking = bookingService.updateBookingForRestaurantOwner(bookingId, form,
                    ownedRestaurantIds);

            BigDecimal totalAmount = bookingService.calculateTotalAmount(updatedBooking);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Booking updated successfully! Total amount: " + totalAmount);

            return "redirect:/restaurant-owner/bookings/" + bookingId;

        } catch (Exception e) {
            System.err.println("❌ Error updating booking: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error updating booking: " + e.getMessage());
            return "redirect:/restaurant-owner/bookings/" + bookingId + "/edit";
        }
    }

    /**
     * Helper method to get current table ID from booking
     */
    private Integer getCurrentTableId(Booking booking) {
        if (booking.getBookingTables() != null && !booking.getBookingTables().isEmpty()) {
            return booking.getBookingTables().get(0).getTable().getTableId();
        }
        return null;
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



    // ==================== WAITLIST MANAGEMENT ====================

    /**
     * Waitlist management page
     */
    @GetMapping("/waitlist")
    public String waitlistManagement(Authentication authentication, Model model) {
        try {
            // Get all restaurants owned by this owner
            List<RestaurantProfile> restaurants = getAllRestaurantsByOwner(authentication);

            if (restaurants.isEmpty()) {
                model.addAttribute("error", "Không tìm thấy nhà hàng nào của bạn. Vui lòng tạo nhà hàng trước.");
                return "restaurant-owner/waitlist";
            }

            // Get waitlist data for all restaurants
            List<Waitlist> allWaitingCustomers = new ArrayList<>();
            List<Waitlist> allCalledCustomers = new ArrayList<>();
            Map<Integer, String> restaurantNames = new HashMap<>();

            for (RestaurantProfile restaurant : restaurants) {
                Integer restaurantId = restaurant.getRestaurantId();
                String restaurantName = restaurant.getRestaurantName();

                // Get waitlist for this restaurant
                List<Waitlist> waitingCustomers = waitlistService.getWaitlistByRestaurant(restaurantId);
                List<Waitlist> calledCustomers = waitlistService.getCalledCustomers(restaurantId);

                // Add to combined lists
                allWaitingCustomers.addAll(waitingCustomers);
                allCalledCustomers.addAll(calledCustomers);

                // Store restaurant names for display
                restaurantNames.put(restaurantId, restaurantName);
            }

            model.addAttribute("waitingCustomers", allWaitingCustomers);
            model.addAttribute("calledCustomers", allCalledCustomers);
            model.addAttribute("restaurants", restaurants);
            model.addAttribute("restaurantNames", restaurantNames);
            model.addAttribute("waitingCount", allWaitingCustomers.size());
            model.addAttribute("calledCount", allCalledCustomers.size());

            return "restaurant-owner/waitlist";

        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tải dữ liệu waitlist: " + e.getMessage());
            return "restaurant-owner/waitlist";
        }
    }

    /**
     * Call next customer from waitlist
     */
    @PostMapping("/waitlist/call-next")
    public String callNextFromWaitlist(@RequestParam Integer restaurantId, RedirectAttributes redirectAttributes) {
        try {
            Waitlist calledCustomer = waitlistService.callNextFromWaitlist(restaurantId);

            if (calledCustomer != null) {
                redirectAttributes.addFlashAttribute("success",
                        "Đã gọi khách hàng: " + calledCustomer.getCustomer().getUser().getFullName());
            } else {
                redirectAttributes.addFlashAttribute("info", "Không có khách hàng nào trong danh sách chờ");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi gọi khách hàng: " + e.getMessage());
        }

        return "redirect:/restaurant-owner/dashboard";
    }

    /**
     * Seat customer from waitlist
     */
    @PostMapping("/waitlist/seat/{waitlistId}")
    public String seatCustomer(@PathVariable Integer waitlistId,
            @RequestParam(required = false) Integer tableId,
            RedirectAttributes redirectAttributes) {
        try {
            Waitlist seatedCustomer = waitlistService.seatCustomer(waitlistId, tableId);

            redirectAttributes.addFlashAttribute("success",
                    "Đã xếp chỗ cho khách hàng: " + seatedCustomer.getCustomer().getUser().getFullName());

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xếp chỗ: " + e.getMessage());
        }

        return "redirect:/restaurant-owner/dashboard";
    }

    /**
     * Cancel waitlist entry
     */
    @PostMapping("/waitlist/cancel/{waitlistId}")
    public String cancelWaitlistEntry(@PathVariable Integer waitlistId, RedirectAttributes redirectAttributes) {
        try {
            waitlistService.cancelWaitlist(waitlistId);

            redirectAttributes.addFlashAttribute("success", "Đã hủy danh sách chờ");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi hủy: " + e.getMessage());
        }

        return "redirect:/restaurant-owner/dashboard";
    }

    /**
     * Get waitlist data for AJAX
     */
    @GetMapping("/waitlist/data")
    public String getWaitlistData(@RequestParam Integer restaurantId, Model model) {
        List<Waitlist> waitingCustomers = waitlistService.getWaitlistByRestaurant(restaurantId);
        List<Waitlist> calledCustomers = waitlistService.getCalledCustomers(restaurantId);

        model.addAttribute("waitingCustomers", waitingCustomers);
        model.addAttribute("calledCustomers", calledCustomers);

        return "restaurant-owner/fragments/waitlist-data :: waitlist-data";
    }

    /**
     * View waitlist detail for restaurant owner
     */
    @GetMapping("/waitlist/{waitlistId}")
    public String viewWaitlistDetail(@PathVariable Integer waitlistId,
            Authentication authentication,
            Model model) {
        try {
            com.example.booking.dto.WaitlistDetailDto detail = waitlistService.getWaitlistDetail(waitlistId);

            model.addAttribute("waitlistDetail", detail);
            model.addAttribute("pageTitle", "Chi tiết đơn chờ #" + waitlistId);

            return "restaurant-owner/waitlist-detail";

        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tải chi tiết waitlist: " + e.getMessage());
            return "restaurant-owner/waitlist";
        }
    }

    /**
     * API endpoint để lấy waitlist detail cho restaurant owner
     */
    @GetMapping("/waitlist/{waitlistId}/detail")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getWaitlistDetailApi(@PathVariable Integer waitlistId,
            Authentication authentication) {
        try {
            WaitlistDetailDto detail = waitlistService.getWaitlistDetail(waitlistId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("waitlist", detail);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * API endpoint để update waitlist cho restaurant owner
     */
    @PostMapping("/waitlist/{waitlistId}/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateWaitlistForRestaurant(@PathVariable Integer waitlistId,
            @RequestBody Map<String, Object> updateData,
            Authentication authentication) {
        try {
            // Get restaurant owner info
            User user = (User) authentication.getPrincipal();
            RestaurantOwner owner = restaurantOwnerService.getRestaurantOwnerByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Restaurant owner not found"));

            // Get all restaurant IDs owned by this owner
            Set<Integer> ownedRestaurantIds = new HashSet<>();
            if (owner.getRestaurants() != null) {
                for (RestaurantProfile restaurant : owner.getRestaurants()) {
                    ownedRestaurantIds.add(restaurant.getRestaurantId());
                }
            }

            if (ownedRestaurantIds.isEmpty()) {
                throw new IllegalArgumentException("Owner does not have any restaurants");
            }

            Integer partySize = updateData.get("partySize") != null
                    ? Integer.valueOf(updateData.get("partySize").toString())
                    : null;
            String specialRequests = updateData.get("specialRequests") != null
                    ? updateData.get("specialRequests").toString()
                    : null;
            String status = updateData.get("status") != null
                    ? updateData.get("status").toString()
                    : null;

            WaitlistDetailDto updated = waitlistService.updateWaitlist(waitlistId, partySize, status, specialRequests);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("waitlist", updated);
            response.put("message", "Waitlist updated successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * API endpoint để xác nhận waitlist thành booking
     */
    @PostMapping("/waitlist/{waitlistId}/confirm-to-booking")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> confirmWaitlistToBooking(@PathVariable Integer waitlistId,
            @RequestBody Map<String, Object> confirmData,
            Authentication authentication) {
        try {
            // Get restaurant owner info
            User user = (User) authentication.getPrincipal();
            RestaurantOwner owner = restaurantOwnerService.getRestaurantOwnerByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Restaurant owner not found"));

            // Lấy thời gian booking từ request
            String bookingTimeStr = confirmData.get("bookingTime").toString();
            LocalDateTime confirmedBookingTime = LocalDateTime.parse(bookingTimeStr);

            // Lấy waitlist để kiểm tra restaurant ownership
            com.example.booking.domain.Waitlist waitlist = waitlistService.findById(waitlistId);
            if (waitlist == null) {
                throw new IllegalArgumentException("Waitlist not found");
            }

            // Validate restaurant ownership - kiểm tra xem waitlist có thuộc về owner không
            Integer waitlistRestaurantId = waitlist.getRestaurant().getRestaurantId();
            boolean isOwnerOfRestaurant = owner.getRestaurants() != null &&
                    owner.getRestaurants().stream()
                            .anyMatch(r -> r.getRestaurantId().equals(waitlistRestaurantId));

            if (!isOwnerOfRestaurant) {
                throw new IllegalArgumentException("You can only confirm waitlist entries for your own restaurants");
            }

            // Xác nhận waitlist thành booking với restaurant ID từ waitlist
            Booking booking = waitlistService.confirmWaitlistToBooking(waitlistId, confirmedBookingTime,
                    waitlistRestaurantId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("bookingId", booking.getBookingId());
            response.put("bookingTime", booking.getBookingTime());
            response.put("partySize", booking.getNumberOfGuests());
            response.put("totalAmount", booking.getDepositAmount());
            response.put("message", "Waitlist confirmed to booking successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ===== HELPER METHODS =====

    /**
     * Get all restaurants owned by current authenticated user
     */
    private List<RestaurantProfile> getAllRestaurantsByOwner(Authentication authentication) {
        try {
            System.out.println("🔍 getAllRestaurantsByOwner called");
            String username = authentication.getName();
            System.out.println("   Username: " + username);

            // Get User from authentication
            User user = getUserFromAuthentication(authentication);
            System.out.println("✅ User found: " + user.getUsername());

            // Check if user has RESTAURANT_OWNER role
            if (!user.getRole().isRestaurantOwner()) {
                System.out.println("❌ User does not have RESTAURANT_OWNER role: " + user.getRole());
                return new ArrayList<>();
            }

            // Get RestaurantOwner record for this user
            Optional<RestaurantOwner> restaurantOwnerOpt = restaurantOwnerService
                    .getRestaurantOwnerByUserId(user.getId());

            System.out.println("🔍 Searching for RestaurantOwner with user ID: " + user.getId());
            System.out.println("🔍 RestaurantOwner found: " + restaurantOwnerOpt.isPresent());

            if (restaurantOwnerOpt.isEmpty()) {
                System.out.println("❌ No RestaurantOwner record found for user: " + username);
                System.out.println("❌ User ID: " + user.getId());
                return new ArrayList<>();
            }

            RestaurantOwner restaurantOwner = restaurantOwnerOpt.get();
            System.out.println("✅ RestaurantOwner found: " + restaurantOwner.getOwnerId());

            // Get restaurants owned by this owner
            List<RestaurantProfile> restaurants = restaurantOwnerService
                    .getRestaurantsByOwnerId(restaurantOwner.getOwnerId());
            System.out.println("✅ Found " + restaurants.size() + " restaurants for user: " + username);

            return restaurants;

        } catch (Exception e) {
            System.err.println("❌ Error getting restaurants by owner: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Helper method để lấy User từ authentication (xử lý cả User và OAuth2User)
     */
    private User getUserFromAuthentication(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        // Nếu là User object trực tiếp (regular login)
        if (principal instanceof User) {
            return (User) principal;
        }

        // Nếu là OAuth2User hoặc OidcUser (OAuth2 login)
        if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            String username = authentication.getName(); // username = email cho OAuth users

            // Tìm User thực tế từ database
            try {
                User user = (User) userService.loadUserByUsername(username);
                return user;
            } catch (Exception e) {
                throw new RuntimeException("User not found for OAuth username: " + username +
                        ". Error: " + e.getMessage());
            }
        }

        throw new RuntimeException("Unsupported authentication principal type: " + principal.getClass().getName());
    }
    
    /**
     * Helper method để kiểm tra user có active và có restaurant approved không
     */
    private boolean isUserActiveAndApproved(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        try {
            User user = getUserFromAuthentication(authentication);
            if (user == null || !Boolean.TRUE.equals(user.getActive())) {
                return false;
            }
            
            if (!user.getRole().isRestaurantOwner()) {
                return false;
            }
            
            // Kiểm tra có restaurant được approve không
            List<RestaurantProfile> restaurants = getAllRestaurantsByOwner(authentication);
            return restaurants.stream().anyMatch(restaurant -> restaurant.isApproved());
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Helper method để kiểm tra user có thể truy cập dashboard (có ít nhất 1 restaurant approved)
     */
    private boolean canAccessDashboard(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        try {
            User user = getUserFromAuthentication(authentication);
            if (user == null || !Boolean.TRUE.equals(user.getActive())) {
                return false;
            }
            
            if (!user.getRole().isRestaurantOwner()) {
                return false;
            }
            
            // Kiểm tra có restaurant được approve không
            List<RestaurantProfile> restaurants = getAllRestaurantsByOwner(authentication);
            return restaurants.stream().anyMatch(restaurant -> restaurant.isApproved());
            
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Helper method to get current table IDs from booking
     */
    private String getCurrentTableIds(Booking booking) {
        System.out.println("🔍 DEBUG - getCurrentTableIds called for booking: " + booking.getBookingId());
        if (booking.getBookingTables() != null && !booking.getBookingTables().isEmpty()) {
            String tableIds = booking.getBookingTables().stream()
                    .map(bt -> String.valueOf(bt.getTable().getTableId()))
                    .collect(Collectors.joining(","));
            System.out.println("   ✅ Found " + booking.getBookingTables().size() + " tables: " + tableIds);
            return tableIds;
        }
        System.out.println("   ❌ No tables found");
        return "";
    }

    /**
     * Helper method to get current dish IDs from booking
     */
    private String getCurrentDishIds(Booking booking) {
        System.out.println("🔍 DEBUG - getCurrentDishIds called for booking: " + booking.getBookingId());
        if (booking.getBookingDishes() != null && !booking.getBookingDishes().isEmpty()) {
            String dishIds = booking.getBookingDishes().stream()
                    .map(bd -> bd.getDish().getDishId() + ":" + bd.getQuantity())
                    .collect(Collectors.joining(","));
            System.out.println("   ✅ Found " + booking.getBookingDishes().size() + " dishes: " + dishIds);
            return dishIds;
        }
        System.out.println("   ❌ No dishes found");
        return "";
    }

    /**
     * Helper method to get current service IDs from booking
     */
    private String getCurrentServiceIds(Booking booking) {
        System.out.println("🔍 DEBUG - getCurrentServiceIds called for booking: " + booking.getBookingId());
        if (booking.getBookingServices() != null && !booking.getBookingServices().isEmpty()) {
            String serviceIds = booking.getBookingServices().stream()
                    .map(bs -> String.valueOf(bs.getService().getServiceId()))
                    .collect(Collectors.joining(","));
            System.out.println("   ✅ Found " + booking.getBookingServices().size() + " services: " + serviceIds);
            return serviceIds;
        }
        System.out.println("   ❌ No services found");
        return "";
    }

    // ===== RESTAURANT SERVICE MANAGEMENT =====

    /**
     * Show restaurant services management page
     */
    @GetMapping("/restaurants/{restaurantId}/services")
    public String showRestaurantServices(@PathVariable Integer restaurantId, Model model,
            Authentication authentication) {
        try {
            // Verify ownership
            var restaurant = restaurantOwnerService.getRestaurantById(restaurantId);
            if (restaurant.isEmpty()) {
                return "redirect:/restaurant-owner/profile";
            }

            // Check if user owns this restaurant
            String username = authentication.getName();
            if (!restaurant.get().getOwner().getUser().getUsername().equals(username)) {
                return "redirect:/restaurant-owner/profile";
            }

            // Get services for this restaurant
            List<RestaurantService> services = restaurantOwnerService.getServicesByRestaurant(restaurantId);

            // Debug: Check service media
            restaurantOwnerService.debugServiceMedia(restaurantId);

            // Clean up any duplicate service media records
            restaurantOwnerService.cleanupDuplicateServiceMedia(restaurantId);

            model.addAttribute("restaurant", restaurant.get());
            model.addAttribute("services", services);
            model.addAttribute("restaurantOwnerService", restaurantOwnerService);
            return "restaurant-owner/restaurant-services";
        } catch (Exception e) {
            logger.error("Error showing restaurant services: {}", e.getMessage(), e);
            return "redirect:/restaurant-owner/profile";
        }
    }

    /**
     * Show service form (create/edit)
     */
    @GetMapping("/restaurants/{restaurantId}/services/form")
    public String showServiceForm(@PathVariable Integer restaurantId,
            @RequestParam(required = false) Integer serviceId,
            Model model, Authentication authentication) {
        try {
            // Verify ownership
            var restaurant = restaurantOwnerService.getRestaurantById(restaurantId);
            if (restaurant.isEmpty()) {
                return "redirect:/restaurant-owner/profile";
            }

            // Check if user owns this restaurant
            String username = authentication.getName();
            if (!restaurant.get().getOwner().getUser().getUsername().equals(username)) {
                return "redirect:/restaurant-owner/profile";
            }

            RestaurantService service = new RestaurantService();
            if (serviceId != null) {
                // Edit mode
                var existingService = restaurantOwnerService.getRestaurantServiceById(serviceId);
                if (existingService.isPresent()) {
                    service = existingService.get();
                }
            }

            model.addAttribute("restaurant", restaurant.get());
            model.addAttribute("service", service);
            model.addAttribute("isEdit", serviceId != null);
            model.addAttribute("restaurantOwnerService", restaurantOwnerService);
            return "restaurant-owner/service-form";
        } catch (Exception e) {
            logger.error("Error showing service form: {}", e.getMessage(), e);
            return "redirect:/restaurant-owner/profile";
        }
    }

    /**
     * Create new service
     */
    @PostMapping("/restaurants/{restaurantId}/services")
    public String createService(@PathVariable Integer restaurantId,
            @ModelAttribute RestaurantService service,
            @RequestParam(value = "serviceImage", required = false) MultipartFile serviceImage,
            Model model, Authentication authentication) {
        try {
            // Verify ownership
            var restaurant = restaurantOwnerService.getRestaurantById(restaurantId);
            if (restaurant.isEmpty()) {
                return "redirect:/restaurant-owner/profile";
            }

            // Check if user owns this restaurant
            String username = authentication.getName();
            if (!restaurant.get().getOwner().getUser().getUsername().equals(username)) {
                return "redirect:/restaurant-owner/profile";
            }

            // Set restaurant and create service
            service.setRestaurant(restaurant.get());
            service.setCreatedAt(LocalDateTime.now());
            service.setUpdatedAt(LocalDateTime.now());

            RestaurantService createdService = restaurantOwnerService.createRestaurantService(service);

            // Upload image if provided
            if (serviceImage != null && !serviceImage.isEmpty()) {
                try {
                    restaurantOwnerService.uploadServiceImage(restaurantId, createdService.getServiceId(),
                            serviceImage);
                    logger.info("Service image uploaded successfully for service ID: {}",
                            createdService.getServiceId());
                } catch (Exception e) {
                    logger.error("Error uploading service image: {}", e.getMessage(), e);
                    // Continue without failing the service creation
                }
            }

            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/services?success=created";
        } catch (Exception e) {
            logger.error("Error creating service: {}", e.getMessage(), e);
            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/services?error=create_failed";
        }
    }

    /**
     * Update service
     */
    @PostMapping("/restaurants/{restaurantId}/services/{serviceId}")
    public String updateService(@PathVariable Integer restaurantId,
            @PathVariable Integer serviceId,
            @ModelAttribute RestaurantService service,
            @RequestParam(value = "serviceImage", required = false) MultipartFile serviceImage,
            Model model, Authentication authentication) {
        try {
            // Verify ownership
            var restaurant = restaurantOwnerService.getRestaurantById(restaurantId);
            if (restaurant.isEmpty()) {
                return "redirect:/restaurant-owner/profile";
            }

            // Check if user owns this restaurant
            String username = authentication.getName();
            if (!restaurant.get().getOwner().getUser().getUsername().equals(username)) {
                return "redirect:/restaurant-owner/profile";
            }

            // Get existing service and update
            var existingService = restaurantOwnerService.getRestaurantServiceById(serviceId);
            if (existingService.isPresent()) {
                RestaurantService existing = existingService.get();
                existing.setName(service.getName());
                existing.setCategory(service.getCategory());
                existing.setDescription(service.getDescription());
                existing.setPrice(service.getPrice());
                existing.setStatus(service.getStatus());
                existing.setUpdatedAt(LocalDateTime.now());

                restaurantOwnerService.updateRestaurantService(existing);

                // Update image if provided
                if (serviceImage != null && !serviceImage.isEmpty()) {
                    try {
                        restaurantOwnerService.updateServiceImage(restaurantId, serviceId, serviceImage);
                        logger.info("Service image updated successfully for service ID: {}", serviceId);
                    } catch (Exception e) {
                        logger.error("Error updating service image: {}", e.getMessage(), e);
                        // Continue without failing the service update
                    }
                }
            }

            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/services?success=updated";
        } catch (Exception e) {
            logger.error("Error updating service: {}", e.getMessage(), e);
            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/services?error=update_failed";
        }
    }

    /**
     * Delete service
     */
    @PostMapping("/restaurants/{restaurantId}/services/{serviceId}/delete")
    public String deleteService(@PathVariable Integer restaurantId,
            @PathVariable Integer serviceId,
            Model model, Authentication authentication) {
        try {
            // Verify ownership
            var restaurant = restaurantOwnerService.getRestaurantById(restaurantId);
            if (restaurant.isEmpty()) {
                return "redirect:/restaurant-owner/profile";
            }

            // Check if user owns this restaurant
            String username = authentication.getName();
            if (!restaurant.get().getOwner().getUser().getUsername().equals(username)) {
                return "redirect:/restaurant-owner/profile";
            }

            restaurantOwnerService.deleteRestaurantService(serviceId);

            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/services?success=deleted";
        } catch (Exception e) {
            logger.error("Error deleting service: {}", e.getMessage(), e);
            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/services?error=delete_failed";
        }
    }

    /**
     * Update service status
     */
    @PostMapping("/restaurants/{restaurantId}/services/{serviceId}/status")
    public String updateServiceStatus(@PathVariable Integer restaurantId,
            @PathVariable Integer serviceId,
            @RequestParam String status,
            Model model, Authentication authentication) {
        try {
            // Verify ownership
            var restaurant = restaurantOwnerService.getRestaurantById(restaurantId);
            if (restaurant.isEmpty()) {
                return "redirect:/restaurant-owner/profile";
            }

            // Check if user owns this restaurant
            String username = authentication.getName();
            if (!restaurant.get().getOwner().getUser().getUsername().equals(username)) {
                return "redirect:/restaurant-owner/profile";
            }

            com.example.booking.common.enums.ServiceStatus serviceStatus = com.example.booking.common.enums.ServiceStatus
                    .valueOf(status.toUpperCase());
            restaurantOwnerService.updateServiceStatus(serviceId, serviceStatus);

            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/services?success=status_updated";
        } catch (Exception e) {
            logger.error("Error updating service status: {}", e.getMessage(), e);
            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/services?error=status_update_failed";
        }
    }

    /**
     * Upload service image
     */
    @PostMapping("/restaurants/{restaurantId}/services/{serviceId}/image")
    public String uploadServiceImage(@PathVariable Integer restaurantId,
            @PathVariable Integer serviceId,
            @RequestParam("image") MultipartFile imageFile,
            Model model, Authentication authentication) {
        try {
            // Verify ownership
            var restaurant = restaurantOwnerService.getRestaurantById(restaurantId);
            if (restaurant.isEmpty()) {
                return "redirect:/restaurant-owner/profile";
            }

            // Check if user owns this restaurant
            String username = authentication.getName();
            if (!restaurant.get().getOwner().getUser().getUsername().equals(username)) {
                return "redirect:/restaurant-owner/profile";
            }

            if (imageFile.isEmpty()) {
                return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/services?error=no_image_selected";
            }

            restaurantOwnerService.uploadServiceImage(restaurantId, serviceId, imageFile);

            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/services?success=image_uploaded";
        } catch (Exception e) {
            logger.error("Error uploading service image: {}", e.getMessage(), e);
            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/services?error=image_upload_failed";
        }
    }

    /**
     * Update service image
     */
    @PostMapping("/restaurants/{restaurantId}/services/{serviceId}/image/update")
    public String updateServiceImage(@PathVariable Integer restaurantId,
            @PathVariable Integer serviceId,
            @RequestParam("image") MultipartFile imageFile,
            Model model, Authentication authentication) {
        try {
            // Verify ownership
            var restaurant = restaurantOwnerService.getRestaurantById(restaurantId);
            if (restaurant.isEmpty()) {
                return "redirect:/restaurant-owner/profile";
            }

            // Check if user owns this restaurant
            String username = authentication.getName();
            if (!restaurant.get().getOwner().getUser().getUsername().equals(username)) {
                return "redirect:/restaurant-owner/profile";
            }

            if (imageFile.isEmpty()) {
                return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/services?error=no_image_selected";
            }

            restaurantOwnerService.updateServiceImage(restaurantId, serviceId, imageFile);

            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/services?success=image_updated";
        } catch (Exception e) {
            logger.error("Error updating service image: {}", e.getMessage(), e);
            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/services?error=image_update_failed";
        }
    }

    /**
     * Delete service image
     */
    @PostMapping("/restaurants/{restaurantId}/services/{serviceId}/image/delete")
    public String deleteServiceImage(@PathVariable Integer restaurantId,
            @PathVariable Integer serviceId,
            Model model, Authentication authentication) {
        try {
            // Verify ownership
            var restaurant = restaurantOwnerService.getRestaurantById(restaurantId);
            if (restaurant.isEmpty()) {
                return "redirect:/restaurant-owner/profile";
            }

            // Check if user owns this restaurant
            String username = authentication.getName();
            if (!restaurant.get().getOwner().getUser().getUsername().equals(username)) {
                return "redirect:/restaurant-owner/profile";
            }

            restaurantOwnerService.deleteServiceImage(restaurantId, serviceId);

            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/services?success=image_deleted";
        } catch (Exception e) {
            logger.error("Error deleting service image: {}", e.getMessage(), e);
            return "redirect:/restaurant-owner/restaurants/" + restaurantId + "/services?error=image_delete_failed";
        }
    }
}

