package com.example.booking.web.advice;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.booking.domain.User;
import com.example.booking.dto.notification.NotificationView;
import com.example.booking.service.NotificationService;

@ControllerAdvice
@ConditionalOnBean(NotificationService.class)
public class NotificationHeaderAdvice {

	@Autowired
	private NotificationService notificationService;

	@ModelAttribute("unreadCount")
	public Long unreadCount(Authentication authentication) {
		UUID userId = getUserId(authentication);
		if (userId == null) return 0L;
		return notificationService.countUnreadByUserId(userId);
	}

	@ModelAttribute("latestNotifications")
	public List<NotificationView> latestNotifications(Authentication authentication) {
		UUID userId = getUserId(authentication);
		if (userId == null) return Collections.emptyList();
		return notificationService.getLatestNotifications(userId);
	}

	private UUID getUserId(Authentication authentication) {
		if (authentication == null) return null;
		Object principal = authentication.getPrincipal();
		if (principal instanceof OAuth2User) {
			// Not implemented: map OAuth2User email to local user
			return null;
		}
		if (principal instanceof User) {
			return ((User) principal).getId();
		}
		return null;
	}
} 
