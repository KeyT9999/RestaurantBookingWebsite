package com.example.booking.web.controller.admin;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.booking.domain.NotificationType;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.notification.AdminNotificationSummary;
import com.example.booking.dto.notification.NotificationForm;
import com.example.booking.dto.notification.NotificationView;
import com.example.booking.repository.UserRepository;
import com.example.booking.service.NotificationService;

@Controller
@RequestMapping("/admin/notifications")
public class AdminNotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String listNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "publishAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<AdminNotificationSummary> notifications = notificationService.findGroupedForAdmin(pageable);
        
        model.addAttribute("notifications", notifications);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", notifications.getTotalPages());
        model.addAttribute("totalElements", notifications.getTotalElements());
        
        return "admin/notifications/list";
    }

    @GetMapping("/new")
    public String createNotificationForm(Model model) {
        model.addAttribute("notificationForm", new NotificationForm());
        model.addAttribute("notificationTypes", NotificationType.values());
        model.addAttribute("userRoles", UserRole.values());
        return "admin/notifications/form";
    }

    @PostMapping("/create")
    public String createNotification(
            NotificationForm form,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            UUID userId = getCurrentUserId(authentication);
            
            switch (form.getAudience()) {
                case ALL:
                    notificationService.sendToAll(form, userId);
                    break;
                case ROLE:
                    notificationService.sendToRoles(form, form.getTargetRoles(), userId);
                    break;
                case USER:
                    notificationService.sendToUsers(form, form.getTargetUserIds(), userId);
                    break;
            }
            
            redirectAttributes.addFlashAttribute("success", "Thông báo đã được gửi thành công!");
            return "redirect:/admin/notifications";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi gửi thông báo: " + e.getMessage());
            return "redirect:/admin/notifications/new";
        }
    }

    @GetMapping("/{id}")
    public String viewNotification(@PathVariable Integer id, Model model) {
        NotificationView notification = notificationService.findById(id);
        if (notification == null) {
            return "error/404";
        }
        
        model.addAttribute("notification", notification);
        return "admin/notifications/detail";
    }

    @PostMapping("/{id}/expire")
    public String expireNotification(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {
        
        try {
            notificationService.expireNotification(id);
            redirectAttributes.addFlashAttribute("success", "Thông báo đã được kết thúc!");
            return "redirect:/admin/notifications";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi kết thúc thông báo: " + e.getMessage());
            return "redirect:/admin/notifications";
        }
    }

    @GetMapping("/stats")
    public String notificationStats(Model model) {
        long totalSent = notificationService.countTotalSent();
        long totalRead = notificationService.countTotalRead();
        long totalUnread = totalSent - totalRead;
        
        model.addAttribute("totalSent", totalSent);
        model.addAttribute("totalRead", totalRead);
        model.addAttribute("totalUnread", totalUnread);
        
        return "admin/notifications/stats";
    }

    private UUID getCurrentUserId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            String email = oauth2User.getAttribute("email");
            User user = userRepository.findByEmail(email).orElse(null);
            return user != null ? user.getId() : null;
        } else {
            User user = (User) authentication.getPrincipal();
            return user.getId();
        }
    }
} 