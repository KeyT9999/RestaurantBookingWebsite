package com.example.booking.web.advice;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.booking.domain.User;
import com.example.booking.dto.notification.NotificationView;
import com.example.booking.service.NotificationService;
import com.example.booking.service.SimpleUserService;

@ControllerAdvice
public class NotificationHeaderAdvice {

	@Autowired
	private NotificationService notificationService;
	
	@Autowired
	private SimpleUserService userService;

	@ModelAttribute("unreadCount")
	public Long unreadCount(Authentication authentication) {
		try {
			UUID userId = getUserId(authentication);
			if (userId == null) return 0L;
			return notificationService.countUnreadByUserId(userId);
		} catch (Exception e) {
			System.err.println("❌ Error in unreadCount: " + e.getMessage());
			return 0L;
		}
	}

	@ModelAttribute("latestNotifications")
	public List<NotificationView> latestNotifications(Authentication authentication) {
		try {
			UUID userId = getUserId(authentication);
			if (userId == null) return Collections.emptyList();
			return notificationService.getLatestNotifications(userId);
		} catch (Exception e) {
			System.err.println("❌ Error in latestNotifications: " + e.getMessage());
			return Collections.emptyList();
		}
	}

	private UUID getUserId(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return null;
		}
		
		try {
			Object principal = authentication.getPrincipal();
			
			// Nếu là User object trực tiếp (regular login)
			if (principal instanceof User) {
				return ((User) principal).getId();
			}
			
			// Nếu là OAuth2User hoặc OidcUser (OAuth2 login)
			if (principal instanceof OAuth2User) {
				String username = authentication.getName(); // username = email cho OAuth users
				
				// Tìm User thực tế từ database
				try {
					User user = (User) userService.loadUserByUsername(username);
					return user != null ? user.getId() : null;
				} catch (Exception e) {
					System.err.println("❌ Error loading user by username in NotificationHeaderAdvice: " + username + " - " + e.getMessage());
					return null;
				}
			}
		} catch (Exception e) {
			System.err.println("❌ Error in getUserId: " + e.getMessage());
		}
		
		return null;
	}
} 