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
import com.example.booking.domain.Booking;
import com.example.booking.domain.Customer;
import com.example.booking.domain.NotificationType;
import com.example.booking.dto.notification.NotificationView;
import com.example.booking.service.NotificationService;
import com.example.booking.service.BookingService;
import com.example.booking.repository.CustomerRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private CustomerRepository customerRepository;

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
        
        // For BOOKING_CONFIRMED notifications, try to get booking details
        String restaurantName = null;
        String tableNames = null;
        LocalDateTime bookingTime = null;
        if (notification.getType() == NotificationType.BOOKING_CONFIRMED) {
            String content = notification.getContent();
            Integer bookingId = null;
            Booking booking = null;
            
            // Try to extract booking ID from content (old format)
            if (content != null && content.contains("Booking ID:")) {
                try {
                    String bookingIdStr = content.substring(content.indexOf("Booking ID:") + 11).split(",")[0].trim();
                    bookingId = Integer.parseInt(bookingIdStr);
                } catch (Exception e) {
                    System.err.println("Error parsing Booking ID from old format: " + e.getMessage());
                }
            }
            
            // If we have booking ID, get booking details
            if (bookingId != null) {
                try {
                    var bookingOpt = bookingService.getBookingWithDetailsById(bookingId);
                    if (bookingOpt.isPresent()) {
                        booking = bookingOpt.get();
                    }
                } catch (Exception e) {
                    System.err.println("Error getting booking by ID: " + e.getMessage());
                }
            } else {
                // New format: try to find booking by parsing time from content and finding user's booking
                try {
                    if (content != null && content.contains("Thời gian:")) {
                        String timePart = content.substring(content.indexOf("Thời gian:") + 10).split(",")[0].trim();
                        LocalDateTime parsedTime = LocalDateTime.parse(timePart, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
                        
                        // Find customer by userId
                        var customerOpt = customerRepository.findByUserId(userId);
                        if (customerOpt.isPresent()) {
                            Customer customer = customerOpt.get();
                            // Find booking by customer and booking time (within 1 minute range)
                            List<Booking> bookings = bookingService.findBookingsByCustomer(customer.getCustomerId());
                            booking = bookings.stream()
                                .filter(b -> {
                                    LocalDateTime bt = b.getBookingTime();
                                    return bt != null && 
                                           Math.abs(java.time.Duration.between(bt, parsedTime).toMinutes()) <= 1;
                                })
                                .findFirst()
                                .orElse(null);
                            
                            // If not found by exact time, try to get most recent CONFIRMED booking
                            if (booking == null) {
                                booking = bookings.stream()
                                    .filter(b -> b.getStatus().name().equals("CONFIRMED"))
                                    .sorted((b1, b2) -> b2.getBookingTime().compareTo(b1.getBookingTime()))
                                    .findFirst()
                                    .orElse(null);
                            }
                        }
                    }
                } catch (DateTimeParseException e) {
                    System.err.println("Error parsing time from content: " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("Error finding booking by time: " + e.getMessage());
                }
            }
            
            // Extract information from booking if found
            if (booking != null) {
                try {
                    // Get restaurant name
                    if (booking.getRestaurant() != null) {
                        restaurantName = booking.getRestaurant().getRestaurantName();
                    }
                    // Get booking time
                    bookingTime = booking.getBookingTime();
                    // Get table names - force load
                    if (booking.getBookingTables() != null) {
                        int tableCount = booking.getBookingTables().size();
                        if (tableCount > 0) {
                            tableNames = booking.getBookingTables().stream()
                                .map(bt -> {
                                    if (bt.getTable() != null) {
                                        return bt.getTable().getTableName();
                                    }
                                    return null;
                                })
                                .filter(name -> name != null)
                                .collect(java.util.stream.Collectors.joining(", "));
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error extracting booking details: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // Add to model if found
            if (restaurantName != null) {
                model.addAttribute("restaurantName", restaurantName);
            }
            if (tableNames != null && !tableNames.isEmpty()) {
                model.addAttribute("tableNames", tableNames);
            }
            if (bookingTime != null) {
                model.addAttribute("bookingTime", bookingTime);
            }
        }
        
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

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<java.util.Map<String, Object>> getNotificationDetails(
            @PathVariable Integer id, 
            Authentication authentication) {
        UUID userId = getCurrentUserId(authentication);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        NotificationView notification = notificationService.findById(id);
        if (notification == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Mark as read when viewing
        notificationService.markAsRead(id, userId);
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("notification", notification);
        
        // For BOOKING_CONFIRMED notifications, try to get booking details
        if (notification.getType() == NotificationType.BOOKING_CONFIRMED) {
            String content = notification.getContent();
            Integer bookingId = null;
            Booking booking = null;
            
            // Try to extract booking ID from content (old format)
            if (content != null && content.contains("Booking ID:")) {
                try {
                    String bookingIdStr = content.substring(content.indexOf("Booking ID:") + 11).split(",")[0].trim();
                    bookingId = Integer.parseInt(bookingIdStr);
                } catch (Exception e) {
                    System.err.println("Error parsing Booking ID from old format: " + e.getMessage());
                }
            }
            
            // If we have booking ID, get booking details
            if (bookingId != null) {
                try {
                    var bookingOpt = bookingService.getBookingWithDetailsById(bookingId);
                    if (bookingOpt.isPresent()) {
                        booking = bookingOpt.get();
                    }
                } catch (Exception e) {
                    System.err.println("Error getting booking by ID: " + e.getMessage());
                }
            } else {
                // New format: try to find booking by parsing time from content and finding user's booking
                try {
                    if (content != null && content.contains("Thời gian:")) {
                        String timePart = content.substring(content.indexOf("Thời gian:") + 10).split(",")[0].trim();
                        LocalDateTime parsedTime = LocalDateTime.parse(timePart, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
                        
                        // Find customer by userId
                        var customerOpt = customerRepository.findByUserId(userId);
                        if (customerOpt.isPresent()) {
                            Customer customer = customerOpt.get();
                            // Find booking by customer and booking time (within 1 minute range)
                            List<Booking> bookings = bookingService.findBookingsByCustomer(customer.getCustomerId());
                            booking = bookings.stream()
                                .filter(b -> {
                                    LocalDateTime bt = b.getBookingTime();
                                    return bt != null && 
                                           Math.abs(java.time.Duration.between(bt, parsedTime).toMinutes()) <= 1;
                                })
                                .findFirst()
                                .orElse(null);
                            
                            // If not found by exact time, try to get most recent CONFIRMED booking
                            if (booking == null) {
                                booking = bookings.stream()
                                    .filter(b -> b.getStatus().name().equals("CONFIRMED"))
                                    .sorted((b1, b2) -> b2.getBookingTime().compareTo(b1.getBookingTime()))
                                    .findFirst()
                                    .orElse(null);
                            }
                        }
                    }
                } catch (DateTimeParseException e) {
                    System.err.println("Error parsing time from content: " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("Error finding booking by time: " + e.getMessage());
                }
            }
            
            // Extract information from booking if found
            if (booking != null) {
                try {
                    // Get restaurant name
                    if (booking.getRestaurant() != null) {
                        response.put("restaurantName", booking.getRestaurant().getRestaurantName());
                    }
                    // Get booking time
                    if (booking.getBookingTime() != null) {
                        response.put("bookingTime", booking.getBookingTime().toString());
                    }
                    // Get table names - force load
                    if (booking.getBookingTables() != null) {
                        int tableCount = booking.getBookingTables().size();
                        if (tableCount > 0) {
                            String tableNames = booking.getBookingTables().stream()
                                .map(bt -> {
                                    if (bt.getTable() != null) {
                                        return bt.getTable().getTableName();
                                    }
                                    return null;
                                })
                                .filter(name -> name != null)
                                .collect(java.util.stream.Collectors.joining(", "));
                            if (!tableNames.isEmpty()) {
                                response.put("tableNames", tableNames);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error extracting booking details: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        
        return ResponseEntity.ok(response);
    }

    private UUID getCurrentUserId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof OAuth2User) {
            // TODO: Find user by email
            return null;
        } else {
            User user = (User) authentication.getPrincipal();
            return user.getId();
        }
    }
} 