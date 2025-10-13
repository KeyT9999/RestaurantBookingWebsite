package com.example.booking.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.common.enums.RestaurantApprovalStatus;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.repository.RestaurantProfileRepository;

/**
 * Service for Restaurant Approval Management
 */
@Service
@Transactional
public class RestaurantApprovalService {
    
    private static final Logger logger = LoggerFactory.getLogger(RestaurantApprovalService.class);
    
    @Autowired
    private RestaurantProfileRepository restaurantProfileRepository;
    
    @Autowired
    private RestaurantNotificationService restaurantNotificationService;
    
    /**
     * Lấy tất cả nhà hàng với thông tin approval
     */
    @Transactional(readOnly = true)
    public List<RestaurantProfile> getAllRestaurantsWithApprovalInfo() {
        logger.info("Getting all restaurants with approval info");
        return restaurantProfileRepository.findAll();
    }
    
    /**
     * Lấy nhà hàng theo trạng thái approval
     */
    @Transactional(readOnly = true)
    public List<RestaurantProfile> getRestaurantsByApprovalStatus(RestaurantApprovalStatus status) {
        logger.info("Getting restaurants with approval status: {}", status);
        return restaurantProfileRepository.findByApprovalStatus(status);
    }
    
    /**
     * Tìm kiếm nhà hàng theo tên hoặc thông tin khác
     */
    @Transactional(readOnly = true)
    public List<RestaurantProfile> searchRestaurants(List<RestaurantProfile> restaurants, String searchTerm) {
        logger.info("Searching restaurants with term: {}", searchTerm);
        
        String lowerSearchTerm = searchTerm.toLowerCase();
        
        return restaurants.stream()
            .filter(restaurant -> 
                restaurant.getRestaurantName().toLowerCase().contains(lowerSearchTerm) ||
                (restaurant.getAddress() != null && restaurant.getAddress().toLowerCase().contains(lowerSearchTerm)) ||
                (restaurant.getCuisineType() != null && restaurant.getCuisineType().toLowerCase().contains(lowerSearchTerm)) ||
                (restaurant.getOwner() != null && restaurant.getOwner().getUser() != null && 
                 restaurant.getOwner().getUser().getUsername() != null &&
                 restaurant.getOwner().getUser().getUsername().toLowerCase().contains(lowerSearchTerm))
            )
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy nhà hàng theo ID
     */
    @Transactional(readOnly = true)
    public Optional<RestaurantProfile> getRestaurantById(Integer id) {
        logger.info("Getting restaurant by ID: {}", id);
        return restaurantProfileRepository.findById(id);
    }
    
    /**
     * Đếm số lượng nhà hàng chờ duyệt
     */
    @Transactional(readOnly = true)
    public long getPendingRestaurantCount() {
        return restaurantProfileRepository.countByApprovalStatus(RestaurantApprovalStatus.PENDING);
    }
    
    /**
     * Đếm số lượng nhà hàng đã duyệt
     */
    @Transactional(readOnly = true)
    public long getApprovedRestaurantCount() {
        return restaurantProfileRepository.countByApprovalStatus(RestaurantApprovalStatus.APPROVED);
    }
    
    /**
     * Đếm số lượng nhà hàng bị từ chối
     */
    @Transactional(readOnly = true)
    public long getRejectedRestaurantCount() {
        return restaurantProfileRepository.countByApprovalStatus(RestaurantApprovalStatus.REJECTED);
    }
    
    /**
     * Đếm số lượng nhà hàng tạm dừng
     */
    @Transactional(readOnly = true)
    public long getSuspendedRestaurantCount() {
        return restaurantProfileRepository.countByApprovalStatus(RestaurantApprovalStatus.SUSPENDED);
    }
    
    /**
     * Duyệt nhà hàng
     */
    @Transactional
    public boolean approveRestaurant(Integer restaurantId, String approvedBy, String approvalReason) {
        try {
            logger.info("Approving restaurant ID: {} by admin: {}", restaurantId, approvedBy);
            
            Optional<RestaurantProfile> restaurantOpt = restaurantProfileRepository.findById(restaurantId);
            
            if (!restaurantOpt.isPresent()) {
                logger.warn("Restaurant not found with ID: {}", restaurantId);
                return false;
            }
            
            RestaurantProfile restaurant = restaurantOpt.get();
            
            // Kiểm tra có thể duyệt không
            if (!restaurant.canBeApproved()) {
                logger.warn("Restaurant ID: {} cannot be approved. Current status: {}", 
                    restaurantId, restaurant.getApprovalStatus());
                return false;
            }
            
            // Cập nhật trạng thái
            restaurant.setApprovalStatus(RestaurantApprovalStatus.APPROVED);
            restaurant.setApprovedBy(approvedBy);
            restaurant.setApprovedAt(LocalDateTime.now());
            restaurant.setApprovalReason(approvalReason);
            restaurant.setRejectionReason(null); // Xóa lý do từ chối cũ
            
            restaurantProfileRepository.save(restaurant);
            
            logger.info("Restaurant ID: {} approved successfully by admin: {}", restaurantId, approvedBy);
            
            // Gửi thông báo email và notification cho nhà hàng
            restaurantNotificationService.sendApprovalNotification(restaurant);
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error approving restaurant ID: {}", restaurantId, e);
            return false;
        }
    }
    
    /**
     * Từ chối nhà hàng
     */
    @Transactional
    public boolean rejectRestaurant(Integer restaurantId, String rejectedBy, String rejectionReason) {
        try {
            logger.info("Rejecting restaurant ID: {} by admin: {}", restaurantId, rejectedBy);
            
            Optional<RestaurantProfile> restaurantOpt = restaurantProfileRepository.findById(restaurantId);
            
            if (!restaurantOpt.isPresent()) {
                logger.warn("Restaurant not found with ID: {}", restaurantId);
                return false;
            }
            
            RestaurantProfile restaurant = restaurantOpt.get();
            
            // Kiểm tra có thể từ chối không
            if (!restaurant.canBeRejected()) {
                logger.warn("Restaurant ID: {} cannot be rejected. Current status: {}", 
                    restaurantId, restaurant.getApprovalStatus());
                return false;
            }
            
            // Cập nhật trạng thái
            restaurant.setApprovalStatus(RestaurantApprovalStatus.REJECTED);
            restaurant.setApprovedBy(rejectedBy);
            restaurant.setApprovedAt(LocalDateTime.now());
            restaurant.setRejectionReason(rejectionReason);
            restaurant.setApprovalReason(null); // Xóa lý do duyệt cũ
            
            restaurantProfileRepository.save(restaurant);
            
            logger.info("Restaurant ID: {} rejected successfully by admin: {}", restaurantId, rejectedBy);
            
            // Gửi thông báo email và notification cho nhà hàng
            restaurantNotificationService.sendRejectionNotification(restaurant, rejectionReason);
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error rejecting restaurant ID: {}", restaurantId, e);
            return false;
        }
    }
    
    /**
     * Gửi lại cho restaurant để chỉnh sửa (từ REJECTED về PENDING)
     */
    @Transactional
    public boolean resubmitRestaurant(Integer restaurantId, String resubmittedBy, String resubmitReason) {
        try {
            logger.info("Resubmitting restaurant ID: {} by admin: {}", restaurantId, resubmittedBy);
            
            Optional<RestaurantProfile> restaurantOpt = restaurantProfileRepository.findById(restaurantId);
            
            if (!restaurantOpt.isPresent()) {
                logger.warn("Restaurant not found with ID: {}", restaurantId);
                return false;
            }
            
            RestaurantProfile restaurant = restaurantOpt.get();
            
            // Chỉ có thể gửi lại từ REJECTED
            if (restaurant.getApprovalStatus() != RestaurantApprovalStatus.REJECTED) {
                logger.warn("Restaurant ID: {} cannot be resubmitted. Current status: {}", 
                    restaurantId, restaurant.getApprovalStatus());
                return false;
            }
            
            // Cập nhật trạng thái: REJECTED → PENDING
            restaurant.setApprovalStatus(RestaurantApprovalStatus.PENDING);
            restaurant.setApprovedBy(null); // Reset approved_by
            restaurant.setApprovedAt(null); // Reset approved_at
            restaurant.setApprovalReason(null); // Reset approval_reason
            restaurant.setRejectionReason(null); // Reset rejection_reason
            restaurant.setUpdatedAt(LocalDateTime.now());
            
            restaurantProfileRepository.save(restaurant);
            
            logger.info("Restaurant ID: {} resubmitted successfully by admin: {}", restaurantId, resubmittedBy);
            
            // Gửi thông báo email và notification cho restaurant owner
            restaurantNotificationService.sendResubmitNotification(restaurant, resubmitReason);
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error resubmitting restaurant ID: {}", restaurantId, e);
            return false;
        }
    }
    
    /**
     * Tạm dừng nhà hàng
     */
    @Transactional
    public boolean suspendRestaurant(Integer restaurantId, String suspendedBy, String suspensionReason) {
        try {
            logger.info("Suspending restaurant ID: {} by admin: {}", restaurantId, suspendedBy);
            
            Optional<RestaurantProfile> restaurantOpt = restaurantProfileRepository.findById(restaurantId);
            
            if (!restaurantOpt.isPresent()) {
                logger.warn("Restaurant not found with ID: {}", restaurantId);
                return false;
            }
            
            RestaurantProfile restaurant = restaurantOpt.get();
            
            // Kiểm tra có thể tạm dừng không
            if (!restaurant.canBeSuspended()) {
                logger.warn("Restaurant ID: {} cannot be suspended. Current status: {}", 
                    restaurantId, restaurant.getApprovalStatus());
                return false;
            }
            
            // Cập nhật trạng thái
            restaurant.setApprovalStatus(RestaurantApprovalStatus.SUSPENDED);
            restaurant.setApprovedBy(suspendedBy);
            restaurant.setApprovedAt(LocalDateTime.now());
            restaurant.setApprovalReason(suspensionReason != null ? suspensionReason : "Nhà hàng bị tạm dừng");
            
            restaurantProfileRepository.save(restaurant);
            
            logger.info("Restaurant ID: {} suspended successfully by admin: {}", restaurantId, suspendedBy);
            
            // Gửi thông báo email và notification cho nhà hàng
            restaurantNotificationService.sendSuspensionNotification(restaurant, suspensionReason);
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error suspending restaurant ID: {}", restaurantId, e);
            return false;
        }
    }
    
    /**
     * Kích hoạt lại nhà hàng (từ SUSPENDED về APPROVED)
     */
    @Transactional
    public boolean activateRestaurant(Integer restaurantId, String activatedBy, String activationReason) {
        try {
            logger.info("Activating restaurant ID: {} by admin: {}", restaurantId, activatedBy);
            
            Optional<RestaurantProfile> restaurantOpt = restaurantProfileRepository.findById(restaurantId);
            
            if (!restaurantOpt.isPresent()) {
                logger.warn("Restaurant not found with ID: {}", restaurantId);
                return false;
            }
            
            RestaurantProfile restaurant = restaurantOpt.get();
            
            // Chỉ có thể kích hoạt từ SUSPENDED
            if (restaurant.getApprovalStatus() != RestaurantApprovalStatus.SUSPENDED) {
                logger.warn("Restaurant ID: {} cannot be activated. Current status: {}", 
                    restaurantId, restaurant.getApprovalStatus());
                return false;
            }
            
            // Cập nhật trạng thái
            restaurant.setApprovalStatus(RestaurantApprovalStatus.APPROVED);
            restaurant.setApprovedBy(activatedBy);
            restaurant.setApprovedAt(LocalDateTime.now());
            restaurant.setApprovalReason(activationReason != null ? activationReason : "Nhà hàng được kích hoạt lại");
            
            restaurantProfileRepository.save(restaurant);
            
            logger.info("Restaurant ID: {} activated successfully by admin: {}", restaurantId, activatedBy);
            
            // Gửi thông báo email và notification cho nhà hàng
            restaurantNotificationService.sendActivationNotification(restaurant, activationReason);
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error activating restaurant ID: {}", restaurantId, e);
            return false;
        }
    }
    
    /**
     * Lấy thống kê approval
     */
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getApprovalStatistics() {
        logger.info("Getting approval statistics");
        
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        List<RestaurantProfile> allRestaurants = restaurantProfileRepository.findAll();
        
        long total = allRestaurants.size();
        long pending = allRestaurants.stream()
            .filter(r -> r.getApprovalStatus() == RestaurantApprovalStatus.PENDING)
            .count();
        long approved = allRestaurants.stream()
            .filter(r -> r.getApprovalStatus() == RestaurantApprovalStatus.APPROVED)
            .count();
        long rejected = allRestaurants.stream()
            .filter(r -> r.getApprovalStatus() == RestaurantApprovalStatus.REJECTED)
            .count();
        long suspended = allRestaurants.stream()
            .filter(r -> r.getApprovalStatus() == RestaurantApprovalStatus.SUSPENDED)
            .count();
        
        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("approved", approved);
        stats.put("rejected", rejected);
        stats.put("suspended", suspended);
        
        // Tính phần trăm
        if (total > 0) {
            stats.put("pendingPercentage", (pending * 100.0) / total);
            stats.put("approvedPercentage", (approved * 100.0) / total);
            stats.put("rejectedPercentage", (rejected * 100.0) / total);
            stats.put("suspendedPercentage", (suspended * 100.0) / total);
        } else {
            stats.put("pendingPercentage", 0.0);
            stats.put("approvedPercentage", 0.0);
            stats.put("rejectedPercentage", 0.0);
            stats.put("suspendedPercentage", 0.0);
        }
        
        return stats;
    }
    
    /**
     * Gửi thông báo khi có nhà hàng mới đăng ký
     */
    public void notifyNewRestaurantRegistration(RestaurantProfile restaurant) {
        try {
            logger.info("Notifying new restaurant registration: {}", restaurant.getRestaurantName());
            restaurantNotificationService.notifyAdminNewRegistration(restaurant);
        } catch (Exception e) {
            logger.error("Error sending new restaurant registration notification", e);
        }
    }
}
