package com.example.booking.web.controller;

import com.example.booking.domain.RestaurantMedia;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.service.FileUploadService;
import com.example.booking.service.ImageUploadService;
import com.example.booking.service.RestaurantApprovalService;
import com.example.booking.service.RestaurantOwnerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for handling restaurant file uploads
 * Handles business license, contract documents, and media uploads
 */
@Controller
@RequestMapping("/restaurant-owner/files")
@PreAuthorize("hasRole('RESTAURANT_OWNER')")
public class RestaurantFileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantFileUploadController.class);

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private ImageUploadService imageUploadService;

    @Autowired
    private RestaurantOwnerService restaurantOwnerService;

    @Autowired
    private RestaurantApprovalService restaurantApprovalService;

    /**
     * GET /restaurant-owner/files/business-license
     * Show business license upload page
     */
    @GetMapping("/business-license")
    public String businessLicenseUploadPage(Principal principal, Model model) {
        try {
            // Get restaurant for current owner
            Optional<RestaurantProfile> restaurantOpt = restaurantOwnerService.getRestaurantByOwnerUsername(principal.getName());
            if (restaurantOpt.isEmpty()) {
                model.addAttribute("error", "Không tìm thấy nhà hàng của bạn");
                return "restaurant-owner/file-upload";
            }

            RestaurantProfile restaurant = restaurantOpt.get();
            model.addAttribute("restaurant", restaurant);

            // Check if business license already exists
            String existingLicense = restaurant.getBusinessLicenseFile();
            model.addAttribute("existingLicense", existingLicense);

            // Get file info if exists
            if (existingLicense != null && !existingLicense.isEmpty()) {
                FileUploadService.FileInfo fileInfo = fileUploadService.getFileInfo(existingLicense);
                model.addAttribute("fileInfo", fileInfo);
            }

            return "restaurant-owner/business-license-upload";

        } catch (Exception e) {
            logger.error("Error loading business license upload page", e);
            model.addAttribute("error", "Lỗi khi tải trang upload: " + e.getMessage());
            return "restaurant-owner/file-upload";
        }
    }

    /**
     * POST /restaurant-owner/files/business-license
     * Upload business license file
     */
    @PostMapping("/business-license")
    public String uploadBusinessLicense(
            @RequestParam("file") MultipartFile file,
            @RequestParam("restaurantId") Integer restaurantId,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            logger.info("Uploading business license for restaurant ID: {}, owner: {}", restaurantId, principal.getName());

            // Verify ownership
            Optional<RestaurantProfile> restaurantOpt = restaurantOwnerService.getRestaurantById(restaurantId);
            if (restaurantOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy nhà hàng");
                return "redirect:/restaurant-owner/files/business-license";
            }

            RestaurantProfile restaurant = restaurantOpt.get();
            if (!restaurant.getOwner().getUser().getUsername().equals(principal.getName())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền upload cho nhà hàng này");
                return "redirect:/restaurant-owner/files/business-license";
            }

            // Upload file
            String fileUrl = fileUploadService.uploadBusinessLicense(file, restaurantId);

            // Update restaurant profile
            restaurant.setBusinessLicenseFile(fileUrl);
            restaurantOwnerService.updateRestaurantProfile(restaurant);

            // Create media record
            RestaurantMedia media = new RestaurantMedia();
            media.setRestaurant(restaurant);
            media.setType("business_license");
            media.setUrl(fileUrl);
            restaurantOwnerService.createMedia(media);

            // Notify admin about new business license
            restaurantApprovalService.notifyNewRestaurantRegistration(restaurant);

            redirectAttributes.addFlashAttribute("success", "Upload giấy phép kinh doanh thành công!");
            logger.info("Business license uploaded successfully for restaurant ID: {}", restaurantId);

            return "redirect:/restaurant-owner/files/business-license";

        } catch (IllegalArgumentException e) {
            logger.warn("Business license upload validation failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/restaurant-owner/files/business-license";

        } catch (IOException e) {
            logger.error("Error uploading business license", e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi upload file: " + e.getMessage());
            return "redirect:/restaurant-owner/files/business-license";

        } catch (Exception e) {
            logger.error("Unexpected error during business license upload", e);
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/restaurant-owner/files/business-license";
        }
    }

    /**
     * DELETE /restaurant-owner/files/business-license/{restaurantId}
     * Delete business license file
     */
    @PostMapping("/business-license/{restaurantId}/delete")
    public String deleteBusinessLicense(
            @PathVariable Integer restaurantId,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            logger.info("Deleting business license for restaurant ID: {}", restaurantId);

            // Verify ownership
            Optional<RestaurantProfile> restaurantOpt = restaurantOwnerService.getRestaurantById(restaurantId);
            if (restaurantOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy nhà hàng");
                return "redirect:/restaurant-owner/files/business-license";
            }

            RestaurantProfile restaurant = restaurantOpt.get();
            if (!restaurant.getOwner().getUser().getUsername().equals(principal.getName())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền xóa file của nhà hàng này");
                return "redirect:/restaurant-owner/files/business-license";
            }

            // Delete file
            String fileUrl = restaurant.getBusinessLicenseFile();
            if (fileUrl != null && !fileUrl.isEmpty()) {
                boolean deleted = fileUploadService.deleteFile(fileUrl);
                if (deleted) {
                    // Update restaurant profile
                    restaurant.setBusinessLicenseFile(null);
                    restaurantOwnerService.updateRestaurantProfile(restaurant);

                    // Delete media record
                    restaurantOwnerService.getMediaByRestaurantAndType(restaurant, "business_license")
                        .forEach(media -> restaurantOwnerService.deleteMedia(media.getMediaId()));

                    redirectAttributes.addFlashAttribute("success", "Xóa giấy phép kinh doanh thành công!");
                    logger.info("Business license deleted successfully for restaurant ID: {}", restaurantId);
                } else {
                    redirectAttributes.addFlashAttribute("error", "Không thể xóa file");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy file để xóa");
            }

            return "redirect:/restaurant-owner/files/business-license";

        } catch (Exception e) {
            logger.error("Error deleting business license", e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa file: " + e.getMessage());
            return "redirect:/restaurant-owner/files/business-license";
        }
    }

    /**
     * POST /restaurant-owner/files/contract
     * Upload contract document
     */
    @PostMapping("/contract")
    public String uploadContractDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("restaurantId") Integer restaurantId,
            @RequestParam("contractType") String contractType,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            logger.info("Uploading contract document for restaurant ID: {}, type: {}", restaurantId, contractType);

            // Verify ownership
            Optional<RestaurantProfile> restaurantOpt = restaurantOwnerService.getRestaurantById(restaurantId);
            if (restaurantOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy nhà hàng");
                return "redirect:/restaurant-owner/files/contract";
            }

            RestaurantProfile restaurant = restaurantOpt.get();
            if (!restaurant.getOwner().getUser().getUsername().equals(principal.getName())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền upload cho nhà hàng này");
                return "redirect:/restaurant-owner/files/contract";
            }

            // Upload file
            String fileUrl = fileUploadService.uploadContractDocument(file, restaurantId, contractType);

            // Create media record
            RestaurantMedia media = new RestaurantMedia();
            media.setRestaurant(restaurant);
            media.setType("contract_" + contractType);
            media.setUrl(fileUrl);
            restaurantOwnerService.createMedia(media);

            redirectAttributes.addFlashAttribute("success", "Upload hợp đồng thành công!");
            logger.info("Contract document uploaded successfully for restaurant ID: {}", restaurantId);

            return "redirect:/restaurant-owner/files/contract";

        } catch (Exception e) {
            logger.error("Error uploading contract document", e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi upload hợp đồng: " + e.getMessage());
            return "redirect:/restaurant-owner/files/contract";
        }
    }

    /**
     * AJAX endpoint for file upload progress
     */
    @PostMapping("/ajax/upload-progress")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadProgress(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type,
            @RequestParam("restaurantId") Integer restaurantId) {

        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate file
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "File không được để trống");
                return ResponseEntity.badRequest().body(response);
            }

            // Get file info
            FileUploadService.FileInfo fileInfo = new FileUploadService.FileInfo(
                file.getOriginalFilename(),
                file.getSize(),
                file.getContentType()
            );

            response.put("success", true);
            response.put("message", "File hợp lệ");
            response.put("fileInfo", fileInfo);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error validating file", e);
            response.put("success", false);
            response.put("message", "Lỗi khi validate file: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
