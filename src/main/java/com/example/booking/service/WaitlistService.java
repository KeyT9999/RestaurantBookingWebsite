package com.example.booking.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.domain.Customer;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.Waitlist;
import com.example.booking.domain.WaitlistStatus;
import com.example.booking.repository.WaitlistRepository;

@Service
@Transactional
public class WaitlistService {
    
    @Autowired
    private WaitlistRepository waitlistRepository;
    
    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private RestaurantManagementService restaurantService;
    
    // @Autowired
    // private NotificationService notificationService;
    
    /**
     * Thêm customer vào waitlist với validation cải thiện
     */
    public Waitlist addToWaitlist(Integer restaurantId, Integer partySize, UUID customerId) {
        System.out.println("🔍 Adding customer to waitlist: " + customerId + " for restaurant: " + restaurantId);
        
        // Validate inputs
        if (restaurantId == null || partySize == null || customerId == null) {
            throw new IllegalArgumentException("Restaurant ID, party size, and customer ID are required");
        }
        
        if (partySize < 1 || partySize > 20) {
            throw new IllegalArgumentException("Party size must be between 1 and 20");
        }
        
        // Check party size limits for waitlist
        if (partySize > 6) {
            throw new IllegalArgumentException("Groups larger than 6 people cannot join waitlist. Please call the restaurant directly.");
        }
        
        // Validate customer
        Customer customer = customerService.findById(customerId)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
            
        // Validate restaurant
        RestaurantProfile restaurant = restaurantService.findRestaurantById(restaurantId)
            .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
            
        // Check if customer already in waitlist
        if (waitlistRepository.existsByCustomerCustomerIdAndRestaurantIdAndStatus(
                customerId, restaurantId, WaitlistStatus.WAITING)) {
            throw new IllegalArgumentException("You are already on the waitlist for this restaurant");
        }
        
        // Check if customer already has a confirmed booking for the same time period
        // This is a basic check - could be enhanced with actual booking time validation
        List<Waitlist> existingWaitlists = waitlistRepository.findActiveWaitlistByCustomer(customerId);
        if (existingWaitlists.size() >= 3) {
            throw new IllegalArgumentException("You can only be on 3 waitlists at a time");
        }
        
        // Create waitlist entry
        Waitlist waitlist = new Waitlist();
        waitlist.setCustomer(customer);
        waitlist.setRestaurant(restaurant);
        waitlist.setPartySize(partySize);
        waitlist.setStatus(WaitlistStatus.WAITING);
        waitlist.setJoinTime(LocalDateTime.now());
        
        Waitlist saved = waitlistRepository.save(waitlist);
        System.out.println("✅ Waitlist entry created: " + saved.getWaitlistId());
        
        // Calculate and set estimated wait time
        int estimatedWaitTime = calculateEstimatedWaitTimeForCustomer(saved.getWaitlistId());
        saved.setEstimatedWaitTime(estimatedWaitTime);
        
        // Send notification (TODO: implement notification methods)
        // try {
        //     notificationService.sendWaitlistJoinedNotification(saved);
        // } catch (Exception e) {
        //     System.err.println("⚠️ Failed to send notification: " + e.getMessage());
        // }
        
        return saved;
    }
    
    /**
     * Lấy danh sách waitlist theo nhà hàng
     */
    public List<Waitlist> getWaitlistByRestaurant(Integer restaurantId) {
        return waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(
            restaurantId, WaitlistStatus.WAITING);
    }
    
    /**
     * Lấy waitlist entries của customer
     */
    public List<Waitlist> getWaitlistByCustomer(UUID customerId) {
        return waitlistRepository.findActiveWaitlistByCustomer(customerId);
    }
    
    /**
     * Gọi customer tiếp theo từ waitlist
     */
    public Waitlist callNextFromWaitlist(Integer restaurantId) {
        System.out.println("🔍 Calling next customer from waitlist for restaurant: " + restaurantId);
        
        Optional<Waitlist> nextCustomer = waitlistRepository
            .findFirstByRestaurantIdAndStatusOrderByJoinTimeAsc(restaurantId, WaitlistStatus.WAITING);
            
        if (nextCustomer.isPresent()) {
            Waitlist customer = nextCustomer.get();
            customer.setStatus(WaitlistStatus.CALLED);
            Waitlist updated = waitlistRepository.save(customer);
            
            System.out.println("✅ Called customer: " + customer.getCustomer().getUser().getFullName());
            
            // Send notification to customer (TODO: implement notification methods)
            // try {
            //     notificationService.sendWaitlistCallNotification(updated);
            // } catch (Exception e) {
            //     System.err.println("⚠️ Failed to send call notification: " + e.getMessage());
            // }
            
            return updated;
        }
        
        System.out.println("ℹ️ No customers in waitlist");
        return null;
    }
    
    /**
     * Customer confirm arrival và được assign table
     */
    public Waitlist seatCustomer(Integer waitlistId, Integer tableId) {
        System.out.println("🔍 Seating customer from waitlist: " + waitlistId);
        
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
            .orElseThrow(() -> new IllegalArgumentException("Waitlist entry not found"));
            
        waitlist.setStatus(WaitlistStatus.SEATED);
        Waitlist updated = waitlistRepository.save(waitlist);
        
        System.out.println("✅ Customer seated: " + updated.getCustomer().getUser().getFullName());
        return updated;
    }
    
    /**
     * Customer cancel waitlist
     */
    public void cancelWaitlist(Integer waitlistId) {
        System.out.println("🔍 Cancelling waitlist entry: " + waitlistId);
        
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
            .orElseThrow(() -> new IllegalArgumentException("Waitlist entry not found"));
            
        waitlist.setStatus(WaitlistStatus.CANCELLED);
        waitlistRepository.save(waitlist);
        
        System.out.println("✅ Waitlist cancelled: " + waitlist.getCustomer().getUser().getFullName());
    }
    
    /**
     * Tính estimated wait time với logic thông minh hơn
     */
    public int calculateEstimatedWaitTime(Integer restaurantId) {
        long queuePosition = waitlistRepository.countByRestaurantIdAndStatus(restaurantId, WaitlistStatus.WAITING);
        
        if (queuePosition == 0) {
            return 0; // Không có ai trong waitlist
        }
        
        // Lấy thông tin nhà hàng để tính toán chính xác hơn
        RestaurantProfile restaurant = restaurantService.findRestaurantById(restaurantId)
            .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        
        // Đếm số bàn thực tế của nhà hàng
        int totalTables = restaurant.getTables() != null ? restaurant.getTables().size() : 10;
        
        // Tính toán dựa trên thời gian hiện tại (peak hours vs off-peak)
        LocalDateTime now = LocalDateTime.now();
        int currentHour = now.getHour();
        boolean isPeakHour = currentHour >= 18 && currentHour <= 21;
        
        // Table turnover rate khác nhau theo giờ
        int averageTableTurnoverMinutes;
        if (isPeakHour) {
            // Peak hours: turnover nhanh hơn (1.5 hours)
            averageTableTurnoverMinutes = 90;
        } else {
            // Off-peak hours: turnover chậm hơn (2.5 hours)
            averageTableTurnoverMinutes = 150;
        }
        
        // Tính toán cơ bản
        int baseEstimatedMinutes = (int) (queuePosition * averageTableTurnoverMinutes / totalTables);
        
        // Điều chỉnh dựa trên party size trung bình trong waitlist
        List<Waitlist> currentWaitlist = getWaitlistByRestaurant(restaurantId);
        if (!currentWaitlist.isEmpty()) {
            double avgPartySize = currentWaitlist.stream()
                .mapToInt(Waitlist::getPartySize)
                .average()
                .orElse(2.0);
            
            // Party size lớn hơn -> chờ lâu hơn
            if (avgPartySize > 4) {
                baseEstimatedMinutes = (int) (baseEstimatedMinutes * 1.3);
            }
        }
        
        // Minimum 5 minutes, maximum 180 minutes (3 hours)
        return Math.max(5, Math.min(baseEstimatedMinutes, 180));
    }
    
    /**
     * Tính estimated wait time cho một customer cụ thể
     */
    public int calculateEstimatedWaitTimeForCustomer(Integer waitlistId) {
        Waitlist waitlist = findById(waitlistId);
        int queuePosition = getQueuePosition(waitlistId);
        
        if (queuePosition <= 0) {
            return 0;
        }
        
        // Tính toán dựa trên vị trí trong queue và party size
        int baseWaitTime = calculateEstimatedWaitTime(waitlist.getRestaurant().getRestaurantId());
        
        // Điều chỉnh dựa trên party size của customer này
        int partySize = waitlist.getPartySize();
        if (partySize > 6) {
            baseWaitTime = (int) (baseWaitTime * 1.5); // Nhóm lớn chờ lâu hơn
        } else if (partySize <= 2) {
            baseWaitTime = (int) (baseWaitTime * 0.8); // Nhóm nhỏ có thể được ưu tiên
        }
        
        return Math.max(5, Math.min(baseWaitTime, 180));
    }
    
    /**
     * Lấy queue position của customer
     */
    public int getQueuePosition(Integer waitlistId) {
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
            .orElseThrow(() -> new IllegalArgumentException("Waitlist entry not found"));
            
        List<Waitlist> queue = getWaitlistByRestaurant(waitlist.getRestaurant().getRestaurantId());
        
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).getWaitlistId().equals(waitlistId)) {
                return i + 1;
            }
        }
        
        return -1;
    }
    
    /**
     * Tìm waitlist entry by ID
     */
    public Waitlist findById(Integer waitlistId) {
        return waitlistRepository.findById(waitlistId)
            .orElseThrow(() -> new IllegalArgumentException("Waitlist entry not found"));
    }
    
    /**
     * Lấy called customers (for FOH)
     */
    public List<Waitlist> getCalledCustomers(Integer restaurantId) {
        return waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(
            restaurantId, WaitlistStatus.CALLED);
    }
    
    /**
     * Lấy tất cả waitlist entries của restaurant (for FOH management)
     */
    public List<Waitlist> getAllWaitlistByRestaurant(Integer restaurantId) {
        return waitlistRepository.findByRestaurantIdAndStatusIn(
            restaurantId, 
            Arrays.asList(WaitlistStatus.WAITING, WaitlistStatus.CALLED)
        );
    }

    /**
     * Lấy waitlist detail DTO cho customer
     */
    public com.example.booking.dto.WaitlistDetailDto getWaitlistDetailForCustomer(Integer waitlistId, UUID customerId) {
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new IllegalArgumentException("Waitlist entry not found"));

        // Verify ownership
        if (!waitlist.getCustomer().getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("You don't have permission to view this waitlist");
        }

        return convertToDetailDto(waitlist);
    }

    /**
     * Lấy waitlist detail DTO cho restaurant owner
     */
    public com.example.booking.dto.WaitlistDetailDto getWaitlistDetailForRestaurant(Integer waitlistId,
            Integer restaurantId) {
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new IllegalArgumentException("Waitlist entry not found"));

        // Verify restaurant ownership
        if (!waitlist.getRestaurant().getRestaurantId().equals(restaurantId)) {
            throw new IllegalArgumentException("You don't have permission to view this waitlist");
        }

        return convertToDetailDto(waitlist);
    }

    /**
     * Cập nhật waitlist cho customer (chỉ được update party size và special
     * requests)
     */
    public com.example.booking.dto.WaitlistDetailDto updateWaitlistForCustomer(Integer waitlistId, UUID customerId,
            Integer partySize, String specialRequests) {
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new IllegalArgumentException("Waitlist entry not found"));

        // Verify ownership
        if (!waitlist.getCustomer().getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("You don't have permission to edit this waitlist");
        }

        // Verify status allows editing
        if (waitlist.getStatus() != WaitlistStatus.WAITING) {
            throw new IllegalArgumentException("Cannot edit waitlist that is not in WAITING status");
        }

        // Validate party size
        if (partySize != null) {
            if (partySize < 1 || partySize > 20) {
                throw new IllegalArgumentException("Party size must be between 1 and 20");
            }
            if (partySize > 6) {
                throw new IllegalArgumentException("Groups larger than 6 people cannot join waitlist");
            }
            waitlist.setPartySize(partySize);
        }

        // Update special requests (if we add this field to Waitlist entity)
        // waitlist.setSpecialRequests(specialRequests);

        Waitlist updated = waitlistRepository.save(waitlist);

        // Recalculate estimated wait time
        int estimatedWaitTime = calculateEstimatedWaitTimeForCustomer(updated.getWaitlistId());
        updated.setEstimatedWaitTime(estimatedWaitTime);

        return convertToDetailDto(updated);
    }

    /**
     * Cập nhật waitlist cho restaurant owner (có thể update tất cả fields trừ
     * status)
     */
    public com.example.booking.dto.WaitlistDetailDto updateWaitlistForRestaurant(Integer waitlistId,
            Integer restaurantId,
            Integer partySize, String specialRequests, String notes) {
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new IllegalArgumentException("Waitlist entry not found"));

        // Verify restaurant ownership
        if (!waitlist.getRestaurant().getRestaurantId().equals(restaurantId)) {
            throw new IllegalArgumentException("You don't have permission to edit this waitlist");
        }

        // Verify status allows editing
        if (waitlist.getStatus() != WaitlistStatus.WAITING && waitlist.getStatus() != WaitlistStatus.CALLED) {
            throw new IllegalArgumentException("Cannot edit waitlist that is not in WAITING or CALLED status");
        }

        // Update fields
        if (partySize != null) {
            if (partySize < 1 || partySize > 20) {
                throw new IllegalArgumentException("Party size must be between 1 and 20");
            }
            waitlist.setPartySize(partySize);
        }

        // Update notes (if we add this field to Waitlist entity)
        // waitlist.setNotes(notes);

        Waitlist updated = waitlistRepository.save(waitlist);

        // Recalculate estimated wait time if party size changed
        if (partySize != null) {
            int estimatedWaitTime = calculateEstimatedWaitTimeForCustomer(updated.getWaitlistId());
            updated.setEstimatedWaitTime(estimatedWaitTime);
        }

        return convertToDetailDto(updated);
    }

    /**
     * Convert Waitlist entity to WaitlistDetailDto
     */
    private com.example.booking.dto.WaitlistDetailDto convertToDetailDto(Waitlist waitlist) {
        com.example.booking.dto.WaitlistDetailDto dto = new com.example.booking.dto.WaitlistDetailDto();

        dto.setWaitlistId(waitlist.getWaitlistId());
        dto.setCustomerId(waitlist.getCustomer().getCustomerId());
        dto.setCustomerName(waitlist.getCustomer().getUser().getFullName());
        dto.setCustomerPhone(waitlist.getCustomer().getUser().getPhoneNumber());
        dto.setCustomerEmail(waitlist.getCustomer().getUser().getEmail());
        dto.setRestaurantId(waitlist.getRestaurant().getRestaurantId());
        dto.setRestaurantName(waitlist.getRestaurant().getRestaurantName());
        dto.setPartySize(waitlist.getPartySize());
        dto.setJoinTime(waitlist.getJoinTime());
        dto.setStatus(waitlist.getStatus());
        dto.setEstimatedWaitTime(waitlist.getEstimatedWaitTime());
        dto.setQueuePosition(getQueuePosition(waitlist.getWaitlistId()));

        return dto;
    }
}
