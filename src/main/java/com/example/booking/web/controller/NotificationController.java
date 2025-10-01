package com.example.booking.web.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.booking.domain.User;
import com.example.booking.dto.notification.NotificationView;
import com.example.booking.service.NotificationService;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public String listNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            Model model,
            Authentication authentication) {
        
        UUID userId = getCurrentUserId(authentication);
        if (userId == null) {
            return "redirect:/login";
        }
        
        Sort sort = Sort.by("publishAt").descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<NotificationView> notifications;
        if (unreadOnly) {
            notifications = notificationService.findByUserIdAndUnread(userId, true, pageable);
        } else {
            notifications = notificationService.findByUserId(userId, pageable);
        }
        
        long unreadCount = notificationService.countUnreadByUserId(userId);
        
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("unreadOnly", unreadOnly);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", notifications.getTotalPages());
        
        return "notifications/list";
    }

    @GetMapping("/{id}")
    public String viewNotification(@PathVariable Integer id, Model model, Authentication authentication) {
        UUID userId = getCurrentUserId(authentication);
        if (userId == null) {
            return "redirect:/login";
        }
        
        NotificationView notification = notificationService.findById(id);
        if (notification == null) {
            return "error/404";
        }
        
        // Mark as read when viewing
        notificationService.markAsRead(id, userId);
        
        model.addAttribute("notification", notification);
        return "notifications/detail";
    }

    @PostMapping("/{id}/mark-read")
    @ResponseBody
    public ResponseEntity<String> markAsRead(@PathVariable Integer id, Authentication authentication) {
        UUID userId = getCurrentUserId(authentication);
        if (userId == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok("Marked as read");
    }

    @PostMapping("/mark-all-read")
    @ResponseBody
    public ResponseEntity<String> markAllAsRead(Authentication authentication) {
        UUID userId = getCurrentUserId(authentication);
        if (userId == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok("All marked as read");
    }

    @GetMapping("/api/unread-count")
    @ResponseBody
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        UUID userId = getCurrentUserId(authentication);
        if (userId == null) {
            return ResponseEntity.status(401).body(0L);
        }
        
        long count = notificationService.countUnreadByUserId(userId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/api/latest")
    @ResponseBody
    public ResponseEntity<List<NotificationView>> getLatestNotifications(Authentication authentication) {
        UUID userId = getCurrentUserId(authentication);
        if (userId == null) {
            return ResponseEntity.status(401).body(List.of());
        }
        
        List<NotificationView> notifications = notificationService.getLatestNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    private UUID getCurrentUserId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            String email = oauth2User.getAttribute("email");
            // TODO: Find user by email
            return null;
        } else {
            User user = (User) authentication.getPrincipal();
            return user.getId();
        }
    }
} 