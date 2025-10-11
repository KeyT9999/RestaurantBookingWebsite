package com.example.booking.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.domain.Notification;
import com.example.booking.domain.NotificationStatus;
import com.example.booking.domain.User;
import com.example.booking.domain.UserRole;
import com.example.booking.dto.notification.AdminNotificationSummary;
import com.example.booking.dto.notification.NotificationForm;
import com.example.booking.dto.notification.NotificationView;
import com.example.booking.repository.NotificationRepository;
import com.example.booking.repository.UserRepository;
import com.example.booking.service.NotificationService;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    // ============= ADMIN METHODS =============
    
    @Override
    public int sendToAll(NotificationForm form, UUID adminId) {
        List<User> activeUsers = userRepository.findAll().stream()
            .filter(User::isEnabled)
            .collect(Collectors.toList());
        
        return sendToUsers(form, activeUsers, adminId);
    }
    
    @Override
    public int sendToRoles(NotificationForm form, Set<UserRole> roles, UUID adminId) {
        List<User> usersByRoles = userRepository.findAll().stream()
            .filter(User::isEnabled)
            .filter(user -> roles.contains(user.getRole()))
            .collect(Collectors.toList());
        
        return sendToUsers(form, usersByRoles, adminId);
    }
    
    @Override
    public int sendToUsers(NotificationForm form, Set<UUID> userIds, UUID adminId) {
        List<User> users = userRepository.findAllById(userIds).stream()
            .filter(User::isEnabled)
            .collect(Collectors.toList());
        
        return sendToUsers(form, users, adminId);
    }
    
    private int sendToUsers(NotificationForm form, List<User> users, UUID adminId) {
        List<Notification> notifications = new ArrayList<>();
        
        for (User user : users) {
            Notification notification = new Notification();
            notification.setRecipientUserId(user.getId());
            notification.setType(form.getType());
            notification.setTitle(form.getTitle());
            notification.setContent(form.getContent());
            notification.setLinkUrl(form.getLinkUrl());
            notification.setPriority(form.getPriority());
            notification.setPublishAt(form.getPublishAt() != null ? form.getPublishAt() : LocalDateTime.now());
            notification.setExpireAt(form.getExpireAt());
            notification.setCreatedBy(adminId);
            notification.setStatus(NotificationStatus.SENT);
            
            notifications.add(notification);
        }
        
        notificationRepository.saveAll(notifications);
        return notifications.size();
    }

    @Override
    public void sendNotifications(NotificationForm form, UUID adminId) {
        if (form == null || form.getAudience() == null) {
            throw new IllegalArgumentException("Audience must be provided");
        }

        switch (form.getAudience()) {
            case ALL -> sendToAll(form, adminId);
            case ROLE -> {
                if (form.getTargetRoles() == null || form.getTargetRoles().isEmpty()) {
                    throw new IllegalArgumentException("Target roles must be provided when audience is ROLE");
                }
                sendToRoles(form, form.getTargetRoles(), adminId);
            }
            case USER -> {
                if (form.getTargetUserIds() == null || form.getTargetUserIds().isEmpty()) {
                    throw new IllegalArgumentException("Target user ids must be provided when audience is USER");
                }
                sendToUsers(form, form.getTargetUserIds(), adminId);
            }
            default -> throw new IllegalArgumentException("Unsupported audience type: " + form.getAudience());
        }
    }

    // ============= USER METHODS =============
    
    @Override
    public Page<NotificationView> findByUserId(UUID userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByRecipientUserIdAndStatusOrderByPublishAtDesc(
            userId, NotificationStatus.SENT, pageable);
        
        return notifications.map(this::toNotificationView);
    }

    @Override
    public Page<NotificationView> findByUserIdAndUnread(UUID userId, boolean unread, Pageable pageable) {
        Page<Notification> notifications;
        if (unread) {
            notifications = notificationRepository.findByRecipientUserIdAndStatusAndReadAtIsNullOrderByPublishAtDesc(
                userId, NotificationStatus.SENT, pageable);
        } else {
            notifications = notificationRepository.findByRecipientUserIdAndStatusAndReadAtIsNotNullOrderByPublishAtDesc(
                userId, NotificationStatus.SENT, pageable);
        }
        
        return notifications.map(this::toNotificationView);
    }

    @Override
    public long countUnreadByUserId(UUID userId) {
        return notificationRepository.countByRecipientUserIdAndStatusAndReadAtIsNull(
            userId, NotificationStatus.SENT);
    }

    @Override
    public void markAsRead(Integer notificationId, UUID userId) {
        notificationRepository.markAsRead(notificationId, userId, LocalDateTime.now());
    }

    @Override
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsRead(userId, LocalDateTime.now());
    }

    @Override
    public List<NotificationView> getLatestNotifications(UUID userId) {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("publishAt").descending());
        List<Notification> notifications = notificationRepository.findTop5ByRecipientUserIdAndStatusOrderByPublishAtDesc(
            userId, NotificationStatus.SENT, pageable);
        
        return notifications.stream()
            .map(this::toNotificationView)
            .collect(Collectors.toList());
    }

    // ============= ADMIN QUERY METHODS =============
    
    @Override
    public Page<NotificationView> findAllForAdmin(Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findAll(pageable);
        return notifications.map(this::toNotificationView);
    }

    @Override
    public Page<AdminNotificationSummary> findGroupedForAdmin(Pageable pageable) {
        Page<Object[]> rows = notificationRepository.findGroupedSummaries(pageable);
        // Preload role breakdowns
        java.util.Map<String, long[]> roleMap = new java.util.HashMap<>();
        for (Object[] rr : notificationRepository.countRecipientsByRoleForGroups()) {
            com.example.booking.domain.NotificationType type = (com.example.booking.domain.NotificationType) rr[0];
            String title = (String) rr[1];
            String content = (String) rr[2];
            java.time.LocalDateTime publishAt = (java.time.LocalDateTime) rr[3];
            com.example.booking.domain.UserRole role = (com.example.booking.domain.UserRole) rr[4];
            long count = ((Number) rr[5]).longValue();
            String key = type.name()+"|"+title+"|"+content+"|"+publishAt.toString();
            long[] arr = roleMap.computeIfAbsent(key, k -> new long[2]);
            if (role.isCustomer())
                arr[0] = count;
            if (role.isRestaurantOwner())
                arr[1] = count;
        }
        return rows.map(r -> {
            AdminNotificationSummary s = new AdminNotificationSummary();
            s.setId((Integer) r[0]);
            s.setType((com.example.booking.domain.NotificationType) r[1]);
            s.setTitle((String) r[2]);
            s.setContent((String) r[3]);
            s.setPublishAt((java.time.LocalDateTime) r[4]);
            String k = s.getType().name()+"|"+s.getTitle()+"|"+s.getContent()+"|"+s.getPublishAt().toString();
            long[] arr = roleMap.getOrDefault(k, new long[2]);
            s.setCustomerRecipients(arr[0]);
            s.setRestaurantOwnerRecipients(arr[1]);
            s.setTotalRecipients(arr[0] + arr[1]);
            return s;
        });
    }

    @Override
    public NotificationView findById(Integer id) {
        return notificationRepository.findById(id)
            .map(this::toNotificationView)
            .orElse(null);
    }

    @Override
    public long countTotalSent() {
        return notificationRepository.count();
    }

    @Override
    public long countTotalRead() {
        return notificationRepository.countByReadAtIsNotNull();
    }

    @Override
    public void expireNotification(Integer notificationId) {
        notificationRepository.expireNotification(notificationId, LocalDateTime.now());
    }

    // ============= HELPER METHODS =============
    
    private NotificationView toNotificationView(Notification notification) {
        NotificationView view = new NotificationView();
        view.setId(notification.getNotificationId());
        view.setType(notification.getType());
        view.setTitle(notification.getTitle());
        view.setContent(notification.getContent());
        view.setLinkUrl(notification.getLinkUrl());
        view.setPublishAt(notification.getPublishAt());
        view.setReadAt(notification.getReadAt());
        view.setPriority(notification.getPriority());
        view.setUnread(notification.getReadAt() == null);
        return view;
    }
} 