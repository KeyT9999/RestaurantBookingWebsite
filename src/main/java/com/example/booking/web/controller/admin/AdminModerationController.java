package com.example.booking.web.controller.admin;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booking.domain.ReviewReportStatus;
import com.example.booking.domain.User;
import com.example.booking.dto.ReviewReportView;
import com.example.booking.service.ReviewReportService;
import com.example.booking.service.SimpleUserService;

@Controller
@RequestMapping("/admin/moderation")
public class AdminModerationController {

    private final ReviewReportService reviewReportService;
    
    @Autowired
    private SimpleUserService userService;

    @Autowired
    public AdminModerationController(ReviewReportService reviewReportService) {
        this.reviewReportService = reviewReportService;
    }

    @GetMapping
    public String listReports(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size,
                              @RequestParam(name = "status", required = false) ReviewReportStatus status,
                              Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewReportView> reportPage = reviewReportService.getReportsForAdmin(Optional.ofNullable(status), pageable);

        model.addAttribute("pageTitle", "Content Moderation");
        model.addAttribute("reportPage", reportPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", ReviewReportStatus.values());

        return "admin/moderation";
    }

    @GetMapping("/{reportId}")
    public String viewReport(@PathVariable Long reportId, Model model) {
        ReviewReportView report = reviewReportService.getReportView(reportId);
        model.addAttribute("report", report);
        model.addAttribute("pageTitle", "Chi tiết báo cáo review");
        return "admin/moderation-detail";
    }

    @PostMapping("/{reportId}/resolve")
    public String resolveReport(@PathVariable Long reportId,
                                @RequestParam(name = "resolutionMessage", required = false) String resolutionMessage,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {

        try {
            UUID adminId = getUserIdFromAuthentication(authentication);

            reviewReportService.resolveReport(reportId, adminId, resolutionMessage);
            redirectAttributes.addFlashAttribute("success", "Đã phê duyệt và ẩn review");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể phê duyệt: " + e.getMessage());
        }

        return "redirect:/admin/moderation";
    }

    @PostMapping("/{reportId}/reject")
    public String rejectReport(@PathVariable Long reportId,
                               @RequestParam(name = "resolutionMessage", required = false) String resolutionMessage,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {

        try {
            UUID adminId = getUserIdFromAuthentication(authentication);

            reviewReportService.rejectReport(reportId, adminId, resolutionMessage);
            redirectAttributes.addFlashAttribute("success", "Đã từ chối báo cáo");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể từ chối báo cáo: " + e.getMessage());
        }

        return "redirect:/admin/moderation";
    }
    
    /**
     * Helper method để lấy User ID từ authentication (xử lý cả User và OAuth2User)
     */
    private UUID getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        
        // Nếu là User object trực tiếp (regular login)
        if (principal instanceof User) {
            return ((User) principal).getId();
        }
        
        // Nếu là OAuth2User hoặc OidcUser (OAuth2 login)
        if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            String username = authentication.getName(); // username = email cho OAuth users
            
            // Tìm User thực tế từ database
            try {
                User user = (User) userService.loadUserByUsername(username);
                return user != null ? user.getId() : null;
            } catch (Exception e) {
                System.err.println("❌ Error loading user by username in AdminModerationController: " + username + " - " + e.getMessage());
                return null;
            }
        }
        
        return null;
    }
}

