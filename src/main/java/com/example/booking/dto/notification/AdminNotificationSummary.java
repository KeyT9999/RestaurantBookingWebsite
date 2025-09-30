package com.example.booking.dto.notification;

import java.time.LocalDateTime;

import com.example.booking.domain.NotificationType;

public class AdminNotificationSummary {
	private Integer id;
	private NotificationType type;
	private String title;
	private String content;
	private LocalDateTime publishAt;
	private long totalRecipients;
	private long customerRecipients;
	private long restaurantOwnerRecipients;

	public Integer getId() { return id; }
	public void setId(Integer id) { this.id = id; }

	public NotificationType getType() { return type; }
	public void setType(NotificationType type) { this.type = type; }

	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }

	public String getContent() { return content; }
	public void setContent(String content) { this.content = content; }

	public LocalDateTime getPublishAt() { return publishAt; }
	public void setPublishAt(LocalDateTime publishAt) { this.publishAt = publishAt; }

	public long getTotalRecipients() { return totalRecipients; }
	public void setTotalRecipients(long totalRecipients) { this.totalRecipients = totalRecipients; }

	public long getCustomerRecipients() { return customerRecipients; }
	public void setCustomerRecipients(long customerRecipients) { this.customerRecipients = customerRecipients; }

	public long getRestaurantOwnerRecipients() { return restaurantOwnerRecipients; }
	public void setRestaurantOwnerRecipients(long restaurantOwnerRecipients) { this.restaurantOwnerRecipients = restaurantOwnerRecipients; }
} 