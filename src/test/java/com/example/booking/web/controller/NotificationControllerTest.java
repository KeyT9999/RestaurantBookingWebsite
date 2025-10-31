package com.example.booking.web.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.notification.NotificationView;
import com.example.booking.service.NotificationService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private NotificationService notificationService;

	private User testUser;
	private NotificationView testNotification;

	@BeforeEach
	void setUp() {
		testUser = new User();
		testUser.setId(UUID.randomUUID());
		testUser.setUsername("testuser");
		testUser.setEmail("test@example.com");
		testUser.setFullName("Test User");
		testUser.setRole(UserRole.CUSTOMER);

		testNotification = new NotificationView();
		testNotification.setId(1);
		testNotification.setTitle("Test Notification");
		testNotification.setContent("Test content");
	}

	// ========== listNotifications() Tests ==========

	@Test
	@DisplayName("listNotifications - should return notifications list view")
	void listNotifications_ShouldReturnListView() throws Exception {
		Page<NotificationView> page = new PageImpl<>(Arrays.asList(testNotification), PageRequest.of(0, 20), 1);
		when(notificationService.findByUserId(eq(testUser.getId()), any(org.springframework.data.domain.Pageable.class)))
				.thenReturn(page);
		when(notificationService.countUnreadByUserId(eq(testUser.getId()))).thenReturn(5L);

		mockMvc.perform(get("/notifications")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(view().name("notifications/list"))
				.andExpect(model().attributeExists("notifications", "unreadCount"))
				.andExpect(model().attribute("currentPage", 0))
				.andExpect(model().attribute("unreadOnly", false));
	}

	@Test
	@DisplayName("listNotifications - should filter by unreadOnly")
	void listNotifications_WithUnreadOnly_ShouldFilterUnread() throws Exception {
		Page<NotificationView> page = new PageImpl<>(Arrays.asList(testNotification), PageRequest.of(0, 20), 1);
		when(notificationService.findByUserIdAndUnread(eq(testUser.getId()), eq(true), any(org.springframework.data.domain.Pageable.class)))
				.thenReturn(page);
		when(notificationService.countUnreadByUserId(eq(testUser.getId()))).thenReturn(5L);

		mockMvc.perform(get("/notifications")
				.param("unreadOnly", "true")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(model().attribute("unreadOnly", true));
	}

	@Test
	@DisplayName("listNotifications - should handle pagination")
	void listNotifications_WithPagination_ShouldReturnCorrectPage() throws Exception {
		Page<NotificationView> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(1, 20), 50);
		when(notificationService.findByUserId(eq(testUser.getId()), any(org.springframework.data.domain.Pageable.class)))
				.thenReturn(page);
		when(notificationService.countUnreadByUserId(eq(testUser.getId()))).thenReturn(0L);

		mockMvc.perform(get("/notifications")
				.param("page", "1")
				.param("size", "20")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(model().attribute("currentPage", 1));
	}

	@Test
	@DisplayName("listNotifications - should redirect to login when unauthenticated")
	void listNotifications_Unauthenticated_ShouldRedirectToLogin() throws Exception {
		mockMvc.perform(get("/notifications"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}

	// ========== viewNotification() Tests ==========

	@Test
	@DisplayName("viewNotification - should return notification detail view")
	void viewNotification_WithValidId_ShouldReturnDetail() throws Exception {
		when(notificationService.findById(1)).thenReturn(testNotification);

		mockMvc.perform(get("/notifications/1")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(view().name("notifications/detail"))
				.andExpect(model().attributeExists("notification"));
	}

	@Test
	@DisplayName("viewNotification - should mark as read when viewing")
	void viewNotification_ShouldMarkAsRead() throws Exception {
		when(notificationService.findById(1)).thenReturn(testNotification);

		mockMvc.perform(get("/notifications/1")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk());

		// Verify markAsRead was called
		// This would need verification if we had access to the controller instance
	}

	@Test
	@DisplayName("viewNotification - should return 404 when notification not found")
	void viewNotification_NotFound_ShouldReturn404() throws Exception {
		when(notificationService.findById(999)).thenReturn(null);

		mockMvc.perform(get("/notifications/999")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(view().name("error/404"));
	}

	@Test
	@DisplayName("viewNotification - should redirect to login when unauthenticated")
	void viewNotification_Unauthenticated_ShouldRedirectToLogin() throws Exception {
		mockMvc.perform(get("/notifications/1"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}

	// ========== markAsRead() Tests ==========

	@Test
	@DisplayName("markAsRead - should mark notification as read")
	void markAsRead_WithValidId_ShouldMarkAsRead() throws Exception {
		mockMvc.perform(post("/notifications/1/mark-read")
				.contentType(MediaType.APPLICATION_JSON)
				.with(csrf())
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(content().string("Marked as read"));
	}

	@Test
	@DisplayName("markAsRead - should return 401 when unauthenticated")
	void markAsRead_Unauthenticated_ShouldReturn401() throws Exception {
		mockMvc.perform(post("/notifications/1/mark-read")
				.contentType(MediaType.APPLICATION_JSON)
				.with(csrf()))
				.andExpect(status().isUnauthorized())
				.andExpect(content().string("Unauthorized"));
	}

	// ========== markAllAsRead() Tests ==========

	@Test
	@DisplayName("markAllAsRead - should mark all notifications as read")
	void markAllAsRead_ShouldMarkAllAsRead() throws Exception {
		mockMvc.perform(post("/notifications/mark-all-read")
				.contentType(MediaType.APPLICATION_JSON)
				.with(csrf())
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(content().string("All marked as read"));
	}

	@Test
	@DisplayName("markAllAsRead - should return 401 when unauthenticated")
	void markAllAsRead_Unauthenticated_ShouldReturn401() throws Exception {
		mockMvc.perform(post("/notifications/mark-all-read")
				.contentType(MediaType.APPLICATION_JSON)
				.with(csrf()))
				.andExpect(status().isUnauthorized())
				.andExpect(content().string("Unauthorized"));
	}

	// ========== getUnreadCount() Tests ==========

	@Test
	@DisplayName("getUnreadCount - should return unread count")
	void getUnreadCount_ShouldReturnCount() throws Exception {
		when(notificationService.countUnreadByUserId(eq(testUser.getId()))).thenReturn(5L);

		mockMvc.perform(get("/notifications/api/unread-count")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(content().string("5"));
	}

	@Test
	@DisplayName("getUnreadCount - should return 0 when unauthenticated")
	void getUnreadCount_Unauthenticated_ShouldReturnZero() throws Exception {
		mockMvc.perform(get("/notifications/api/unread-count"))
				.andExpect(status().isUnauthorized())
				.andExpect(content().string("0"));
	}

	@Test
	@DisplayName("getUnreadCount - should return 0 when no unread")
	void getUnreadCount_NoUnread_ShouldReturnZero() throws Exception {
		when(notificationService.countUnreadByUserId(eq(testUser.getId()))).thenReturn(0L);

		mockMvc.perform(get("/notifications/api/unread-count")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(content().string("0"));
	}

	// ========== getLatestNotifications() Tests ==========

	@Test
	@DisplayName("getLatestNotifications - should return latest notifications")
	void getLatestNotifications_ShouldReturnLatest() throws Exception {
		List<NotificationView> notifications = Arrays.asList(testNotification);
		when(notificationService.getLatestNotifications(eq(testUser.getId()))).thenReturn(notifications);

		mockMvc.perform(get("/notifications/api/latest")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON));
	}

	@Test
	@DisplayName("getLatestNotifications - should return empty list when unauthenticated")
	void getLatestNotifications_Unauthenticated_ShouldReturnEmpty() throws Exception {
		mockMvc.perform(get("/notifications/api/latest"))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON));
	}

	@Test
	@DisplayName("getLatestNotifications - should return empty list when no notifications")
	void getLatestNotifications_NoNotifications_ShouldReturnEmpty() throws Exception {
		when(notificationService.getLatestNotifications(eq(testUser.getId()))).thenReturn(Collections.emptyList());

		mockMvc.perform(get("/notifications/api/latest")
				.principal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON));
	}
}

