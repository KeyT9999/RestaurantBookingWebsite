package com.example.booking.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.domain.Booking;
import com.example.booking.domain.BookingDish;
import com.example.booking.domain.BookingService;
import com.example.booking.domain.BookingTable;
import com.example.booking.common.enums.BookingStatus;
import com.example.booking.domain.Customer;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.Waitlist;
import com.example.booking.domain.WaitlistStatus;
import com.example.booking.domain.WaitlistDish;
import com.example.booking.domain.WaitlistServiceItem;
import com.example.booking.domain.WaitlistTable;
import com.example.booking.domain.Dish;
import com.example.booking.domain.RestaurantService;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.dto.WaitlistDetailDto;
import com.example.booking.dto.BookingForm;
import com.example.booking.exception.BookingConflictException;
import com.example.booking.repository.WaitlistRepository;
import com.example.booking.repository.WaitlistDishRepository;
import com.example.booking.repository.WaitlistServiceRepository;
import com.example.booking.repository.WaitlistTableRepository;
import com.example.booking.repository.DishRepository;
import com.example.booking.repository.RestaurantServiceRepository;
import com.example.booking.repository.RestaurantTableRepository;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.BookingDishRepository;
import com.example.booking.repository.BookingServiceRepository;
import com.example.booking.repository.BookingTableRepository;
import com.example.booking.service.CustomerService;
import com.example.booking.service.RestaurantManagementService;

@Service
@Transactional
public class WaitlistService {
    
    @Autowired
    private WaitlistRepository waitlistRepository;
    
    @Autowired
    private WaitlistDishRepository waitlistDishRepository;

    @Autowired
    private WaitlistServiceRepository waitlistServiceRepository;

    @Autowired
    private WaitlistTableRepository waitlistTableRepository;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private RestaurantServiceRepository restaurantServiceRepository;

    @Autowired
    private RestaurantTableRepository restaurantTableRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingDishRepository bookingDishRepository;

    @Autowired
    private BookingServiceRepository bookingServiceRepository;

    @Autowired
    private BookingTableRepository bookingTableRepository;

    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private RestaurantManagementService restaurantService;

    @Autowired
    private BookingConflictService conflictService;

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
        // This would require additional logic to check booking times
        
        // Create waitlist entry
        Waitlist waitlist = new Waitlist(customer, restaurant, partySize, WaitlistStatus.WAITING);
        
        // Calculate estimated wait time based on current queue position
        long queuePosition = waitlistRepository.countByRestaurantIdAndStatus(restaurantId, WaitlistStatus.WAITING) + 1;
        int estimatedWaitMinutes = (int) (queuePosition * 30); // 30 minutes per position
        waitlist.setEstimatedWaitTime(estimatedWaitMinutes);
        
        System.out.println("🎯 Creating waitlist entry:");
        System.out.println("   Restaurant: " + restaurant.getRestaurantName());
        System.out.println("   Customer: " + customer.getUser().getUsername());
        System.out.println("   Party Size: " + partySize);
        System.out.println("   Queue Position: " + queuePosition);
        System.out.println("   Estimated Wait Time: " + estimatedWaitMinutes + " minutes");
        
        return waitlistRepository.save(waitlist);
    }
    
    /**
     * Lấy waitlist entries của customer
     */
    public List<Waitlist> getWaitlistByCustomer(UUID customerId) {
        return waitlistRepository.findByCustomerCustomerIdOrderByJoinTimeDesc(customerId);
    }
    
    /**
     * Lấy waitlist entries của restaurant
     */
    public List<Waitlist> getRestaurantWaitlist(Integer restaurantId) {
        return waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(restaurantId, WaitlistStatus.WAITING);
    }
    
    /**
     * Hủy waitlist entry
     */
    public void cancelWaitlist(Integer waitlistId, UUID customerId) {
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new IllegalArgumentException("Waitlist entry not found"));

        if (!waitlist.getCustomer().getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("You can only cancel your own waitlist entries");
        }

        if (waitlist.getStatus() != WaitlistStatus.WAITING) {
            throw new IllegalArgumentException("Cannot cancel waitlist entry that is not waiting");
        }
            
        waitlist.setStatus(WaitlistStatus.CANCELLED);
        waitlistRepository.save(waitlist);

        System.out.println("✅ Waitlist entry cancelled: " + waitlistId);
    }

    // Additional methods for compatibility with old controllers

    /**
     * Get all waitlist entries for restaurant (compatibility method)
     */
    public List<Waitlist> getAllWaitlistByRestaurant(Integer restaurantId) {
        return getRestaurantWaitlist(restaurantId);
    }

    /**
     * Get waitlist by restaurant (compatibility method)
     */
    public List<Waitlist> getWaitlistByRestaurant(Integer restaurantId) {
        return getRestaurantWaitlist(restaurantId);
    }

    /**
     * Get called customers (compatibility method)
     */
    public List<Waitlist> getCalledCustomers(Integer restaurantId) {
        return waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(restaurantId, WaitlistStatus.CALLED);
    }

    /**
     * Call next from waitlist (compatibility method)
     */
    public Waitlist callNextFromWaitlist(Integer restaurantId) {
        Optional<Waitlist> nextCustomer = waitlistRepository
                .findFirstByRestaurantIdAndStatusOrderByJoinTimeAsc(restaurantId, WaitlistStatus.WAITING);
        if (nextCustomer.isPresent()) {
            Waitlist waitlist = nextCustomer.get();
            waitlist.setStatus(WaitlistStatus.CALLED);
            return waitlistRepository.save(waitlist);
        }
        return null;
    }

    /**
     * Seat customer (compatibility method)
     */
    public Waitlist seatCustomer(Integer waitlistId, Integer tableId) {
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new IllegalArgumentException("Waitlist entry not found"));

        waitlist.setStatus(WaitlistStatus.SEATED);
        return waitlistRepository.save(waitlist);
    }

    /**
     * Calculate estimated wait time for customer (compatibility method)
     */
    public Integer calculateEstimatedWaitTimeForCustomer(Integer restaurantId) {
        long queuePosition = waitlistRepository.countByRestaurantIdAndStatus(restaurantId, WaitlistStatus.WAITING);
        return (int) (queuePosition * 30); // 30 minutes per position
    }

    /**
     * Get queue position (compatibility method)
     */
    public Integer getQueuePosition(Integer waitlistId) {
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new IllegalArgumentException("Waitlist entry not found"));

        // Calculate position based on join time
        List<Waitlist> earlierEntries = waitlistRepository.findByRestaurantIdAndStatusOrderByJoinTimeAsc(
                waitlist.getRestaurant().getRestaurantId(), WaitlistStatus.WAITING);
        
        for (int i = 0; i < earlierEntries.size(); i++) {
            if (earlierEntries.get(i).getWaitlistId().equals(waitlistId)) {
                return i + 1;
            }
        }
        return 1;
    }

    /**
     * Calculate estimated wait time (compatibility method)
     */
    public Integer calculateEstimatedWaitTime(Integer waitlistId) {
        Integer queuePosition = getQueuePosition(waitlistId);
        return queuePosition * 30; // 30 minutes per position
    }
    
    /**
     * Find by ID (compatibility method)
     */
    public Waitlist findById(Integer waitlistId) {
        return waitlistRepository.findById(waitlistId).orElse(null);
    }

    /**
     * Cancel waitlist (compatibility method - single parameter)
     */
    public void cancelWaitlist(Integer waitlistId) {
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new IllegalArgumentException("Waitlist entry not found"));

        waitlist.setStatus(WaitlistStatus.CANCELLED);
        waitlistRepository.save(waitlist);

        System.out.println("✅ Waitlist entry cancelled: " + waitlistId);
    }

    /**
     * Get waitlist detail for customer (compatibility method)
     */
    public WaitlistDetailDto getWaitlistDetailForCustomer(Integer waitlistId, UUID customerId) {
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new IllegalArgumentException("Waitlist entry not found"));

        if (!waitlist.getCustomer().getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("Access denied");
        }

        return convertToWaitlistDetailDto(waitlist);
    }

    /**
     * Update waitlist for customer (compatibility method)
     */
    public WaitlistDetailDto updateWaitlistForCustomer(Integer waitlistId, UUID customerId, Integer partySize,
            String specialRequests) {
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new IllegalArgumentException("Waitlist entry not found"));

        if (!waitlist.getCustomer().getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("Access denied");
        }

        if (waitlist.getStatus() != WaitlistStatus.WAITING) {
            throw new IllegalArgumentException("Cannot update waitlist entry that is not waiting");
        }

        if (partySize != null) {
            waitlist.setPartySize(partySize);
        }
        Waitlist savedWaitlist = waitlistRepository.save(waitlist);
        return convertToWaitlistDetailDto(savedWaitlist);
    }

    /**
     * Get waitlist detail for restaurant (compatibility method)
     */
    public WaitlistDetailDto getWaitlistDetailForRestaurant(Integer waitlistId, Integer restaurantId) {
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new IllegalArgumentException("Waitlist entry not found"));

        if (!waitlist.getRestaurant().getRestaurantId().equals(restaurantId)) {
            throw new IllegalArgumentException("Access denied");
        }

        return convertToWaitlistDetailDto(waitlist);
    }

    /**
     * Update waitlist for restaurant (compatibility method)
     */
    public WaitlistDetailDto updateWaitlist(Integer waitlistId, Integer partySize,
            String status, String notes) {
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new IllegalArgumentException("Waitlist entry not found"));

        if (partySize != null) {
            waitlist.setPartySize(partySize);
        }
        if (status != null) {
            try {
                waitlist.setStatus(WaitlistStatus.valueOf(status));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status: " + status);
            }
        }

        Waitlist savedWaitlist = waitlistRepository.save(waitlist);
        return convertToWaitlistDetailDto(savedWaitlist);
    }

    /**
     * Convert Waitlist to WaitlistDetailDto
     */
    private WaitlistDetailDto convertToWaitlistDetailDto(Waitlist waitlist) {
        WaitlistDetailDto dto = new WaitlistDetailDto();
        dto.setWaitlistId(waitlist.getWaitlistId());
        dto.setCustomerName(waitlist.getCustomer().getUser().getFullName());
        dto.setRestaurantName(waitlist.getRestaurant().getRestaurantName());
        dto.setPartySize(waitlist.getPartySize());
        dto.setJoinTime(waitlist.getJoinTime());
        dto.setStatus(waitlist.getStatus().toString());
        dto.setEstimatedWaitTime(waitlist.getEstimatedWaitTime());
        dto.setQueuePosition(getQueuePosition(waitlist.getWaitlistId()));
        dto.setSpecialRequests(
                waitlist.getPreferredBookingTime() != null ? waitlist.getPreferredBookingTime().toString() : null);

        // Map preferredBookingTime từ LocalDateTime sang String
        if (waitlist.getPreferredBookingTime() != null) {
            dto.setPreferredBookingTime(waitlist.getPreferredBookingTime().toString());
        }

        return dto;
    }

    /**
     * Thêm customer vào waitlist với dish, service, table data
     */
    public Waitlist addToWaitlistWithDetails(Integer restaurantId, Integer partySize, UUID customerId,
            String dishIds, String serviceIds, String tableIds, LocalDateTime preferredBookingTime) {
        System.out.println(
                "🔍 Adding customer to waitlist with details: " + customerId + " for restaurant: " + restaurantId);

        // Validate inputs
        if (restaurantId == null || partySize == null || customerId == null) {
            throw new IllegalArgumentException("Restaurant ID, party size, and customer ID are required");
        }

            if (partySize < 1 || partySize > 20) {
                throw new IllegalArgumentException("Party size must be between 1 and 20");
            }

            // Check party size limits for waitlist
            if (partySize > 6) {
                throw new IllegalArgumentException(
                        "Groups larger than 6 people cannot join waitlist. Please call the restaurant directly.");
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

            // Create waitlist entry
            Waitlist waitlist = new Waitlist(customer, restaurant, partySize, WaitlistStatus.WAITING,
                    preferredBookingTime);

            // Calculate estimated wait time based on current queue position
            long queuePosition = waitlistRepository.countByRestaurantIdAndStatus(restaurantId, WaitlistStatus.WAITING)
                    + 1;
            int estimatedWaitMinutes = (int) (queuePosition * 30); // 30 minutes per position
            waitlist.setEstimatedWaitTime(estimatedWaitMinutes);

            // Save waitlist first
            waitlist = waitlistRepository.save(waitlist);

            // Save dishes if provided
            if (dishIds != null && !dishIds.trim().isEmpty()) {
                saveWaitlistDishes(waitlist, dishIds);
        }

        // Save services if provided
        if (serviceIds != null && !serviceIds.trim().isEmpty()) {
            saveWaitlistServices(waitlist, serviceIds);
        }

        // Save tables if provided
        if (tableIds != null && !tableIds.trim().isEmpty()) {
            saveWaitlistTables(waitlist, tableIds);
        }

        System.out.println("🎯 Created waitlist entry with details:");
        System.out.println("   Restaurant: " + restaurant.getRestaurantName());
        System.out.println("   Customer: " + customer.getUser().getUsername());
        System.out.println("   Party Size: " + partySize);
        System.out.println("   Queue Position: " + queuePosition);
        System.out.println("   Estimated Wait Time: " + estimatedWaitMinutes + " minutes");
        System.out.println("   Dishes: " + dishIds);
        System.out.println("   Services: " + serviceIds);
        System.out.println("   Tables: " + tableIds);

        return waitlist;
    }

    /**
     * Save waitlist dishes
     */
    private void saveWaitlistDishes(Waitlist waitlist, String dishIds) {
        System.out.println("🍽️ Saving waitlist dishes for waitlist ID: " + waitlist.getWaitlistId());
        System.out.println("🍽️ Dish IDs string: " + dishIds);

        if (dishIds == null || dishIds.trim().isEmpty()) {
            System.out.println("🍽️ No dish IDs provided, skipping...");
            return;
        }

        String[] dishIdArray = dishIds.split(",");
        System.out.println("🍽️ Parsed dish IDs: " + java.util.Arrays.toString(dishIdArray));

        for (String dishIdStr : dishIdArray) {
            try {
                // Parse format "dishId:quantity" (e.g., "4:1")
                String[] parts = dishIdStr.trim().split(":");
                if (parts.length != 2) {
                    System.err.println("   ❌ Invalid dish format: " + dishIdStr + " (expected dishId:quantity)");
                    continue;
                }

                Integer dishId = Integer.parseInt(parts[0]);
                Integer quantity = Integer.parseInt(parts[1]);

                System.out.println("🍽️ Processing dish ID: " + dishId + " with quantity: " + quantity);

                Dish dish = dishRepository.findById(dishId)
                        .orElseThrow(() -> new IllegalArgumentException("Dish not found: " + dishId));

                System.out.println("🍽️ Found dish: " + dish.getName() + " with price: " + dish.getPrice());

                // Calculate total price for this dish
                java.math.BigDecimal totalPrice = dish.getPrice().multiply(java.math.BigDecimal.valueOf(quantity));

                WaitlistDish waitlistDish = new WaitlistDish(waitlist, dish, quantity, totalPrice);
                System.out.println("🍽️ Created WaitlistDish object with quantity: " + quantity + " and total price: "
                        + totalPrice);

                WaitlistDish savedDish = waitlistDishRepository.save(waitlistDish);
                System.out.println("🍽️ Saved WaitlistDish with ID: " + savedDish.getWaitlistDishId());

                System.out.println("   ✅ Saved dish: " + dish.getName() + " (qty: " + quantity + ")");
            } catch (NumberFormatException e) {
                System.err.println("   ❌ Invalid dish ID or quantity: " + dishIdStr);
            } catch (Exception e) {
                System.err.println("   ❌ Error saving dish " + dishIdStr + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Save waitlist services
     */
    private void saveWaitlistServices(Waitlist waitlist, String serviceIds) {
        String[] serviceIdArray = serviceIds.split(",");
        for (String serviceIdStr : serviceIdArray) {
            try {
                Integer serviceId = Integer.parseInt(serviceIdStr.trim());
                RestaurantService service = restaurantServiceRepository.findById(serviceId)
                        .orElseThrow(() -> new IllegalArgumentException("Service not found: " + serviceId));

                WaitlistServiceItem waitlistService = new WaitlistServiceItem(waitlist, service, 1, service.getPrice());
                waitlistServiceRepository.save(waitlistService);

                System.out.println("   ✅ Saved service: " + service.getName());
            } catch (NumberFormatException e) {
                System.err.println("   ❌ Invalid service ID: " + serviceIdStr);
            }
        }
    }

    /**
     * Save waitlist tables
     */
    private void saveWaitlistTables(Waitlist waitlist, String tableIds) {
        String[] tableIdArray = tableIds.split(",");
        for (String tableIdStr : tableIdArray) {
            try {
                Integer tableId = Integer.parseInt(tableIdStr.trim());
                RestaurantTable table = restaurantTableRepository.findById(tableId)
                        .orElseThrow(() -> new IllegalArgumentException("Table not found: " + tableId));

                WaitlistTable waitlistTable = new WaitlistTable(waitlist, table);
                waitlistTableRepository.save(waitlistTable);

                System.out.println("   ✅ Saved table: " + table.getTableName());
            } catch (NumberFormatException e) {
                System.err.println("   ❌ Invalid table ID: " + tableIdStr);
            }
        }
    }

    /**
     * Get waitlist details with dishes, services, and tables
     */
    public WaitlistDetailDto getWaitlistDetails(Integer waitlistId) {
        System.out.println("🔍 Getting waitlist details for ID: " + waitlistId);

        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new IllegalArgumentException("Waitlist not found: " + waitlistId));

        WaitlistDetailDto dto = new WaitlistDetailDto();
        dto.setWaitlistId(waitlist.getWaitlistId());
        dto.setCustomerName(waitlist.getCustomer().getUser().getUsername());
        dto.setRestaurantName(waitlist.getRestaurant().getRestaurantName());
        dto.setPartySize(waitlist.getPartySize());
        dto.setJoinTime(waitlist.getJoinTime());
        dto.setStatus(waitlist.getStatus().toString());
        dto.setEstimatedWaitTime(waitlist.getEstimatedWaitTime());
        dto.setQueuePosition(getQueuePosition(waitlistId));

        // Map preferredBookingTime từ LocalDateTime sang String
        if (waitlist.getPreferredBookingTime() != null) {
            dto.setPreferredBookingTime(waitlist.getPreferredBookingTime().toString());
        }

        // Load dishes
        List<WaitlistDish> waitlistDishes = waitlistDishRepository.findByWaitlistWaitlistId(waitlistId);
        List<WaitlistDetailDto.WaitlistDishDto> dishDtos = waitlistDishes.stream()
                .map(wd -> new WaitlistDetailDto.WaitlistDishDto(
                        wd.getDish().getName(),
                        wd.getDish().getDescription(),
                        wd.getQuantity(),
                        wd.getDish().getPrice(),
                        wd.getPrice()))
                .collect(java.util.stream.Collectors.toList());
        dto.setDishes(dishDtos);

        // Load services
        List<WaitlistServiceItem> waitlistServices = waitlistServiceRepository.findByWaitlistWaitlistId(waitlistId);
        List<WaitlistDetailDto.WaitlistServiceDto> serviceDtos = waitlistServices.stream()
                .map(ws -> new WaitlistDetailDto.WaitlistServiceDto(
                        ws.getService().getName(),
                        ws.getService().getDescription(),
                        ws.getPrice()))
                .collect(java.util.stream.Collectors.toList());
        dto.setServices(serviceDtos);

        // Load tables
        List<WaitlistTable> waitlistTables = waitlistTableRepository.findByWaitlistWaitlistId(waitlistId);
        List<WaitlistDetailDto.WaitlistTableDto> tableDtos = waitlistTables.stream()
                .map(wt -> new WaitlistDetailDto.WaitlistTableDto(
                        wt.getTable().getTableName(),
                        wt.getTable().getCapacity(),
                        wt.getTable().getStatus().toString()))
                .collect(java.util.stream.Collectors.toList());
        dto.setTables(tableDtos);

        System.out.println("✅ Waitlist details loaded:");
        System.out.println("   Customer: " + dto.getCustomerName());
        System.out.println("   Restaurant: " + dto.getRestaurantName());
        System.out.println("   Party Size: " + dto.getPartySize());
        System.out.println("   Dishes: " + dishDtos.size());
        System.out.println("   Services: " + serviceDtos.size());
        System.out.println("   Tables: " + tableDtos.size());

        return dto;
    }

    public WaitlistDetailDto getWaitlistDetail(Integer waitlistId) {
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new IllegalArgumentException("Waitlist entry not found"));
        return convertToWaitlistDetailDto(waitlist);
    }

    /**
     * Xác nhận Waitlist và tạo Booking từ thông tin Waitlist (với validation
     * restaurant ownership)
     */
    public Booking confirmWaitlistToBooking(Integer waitlistId, LocalDateTime confirmedBookingTime,
            Integer restaurantId) {
        System.out.println("🔄 Confirming waitlist to booking: " + waitlistId + " for restaurant: " + restaurantId);

        // Lấy waitlist entry
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new IllegalArgumentException("Waitlist entry not found"));

        // Kiểm tra status
        if (waitlist.getStatus() != WaitlistStatus.WAITING) {
            throw new IllegalArgumentException("Only WAITING waitlist entries can be confirmed to booking");
        }

        // Validate restaurant ownership - QUAN TRỌNG!
        if (!waitlist.getRestaurant().getRestaurantId().equals(restaurantId)) {
            throw new IllegalArgumentException("You can only confirm waitlist entries for your own restaurant");
        }

        // Validate booking time FIRST
        if (confirmedBookingTime == null) {
            throw new IllegalArgumentException("Confirmed booking time cannot be null");
        }
        if (confirmedBookingTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Confirmed booking time cannot be in the past");
        }

        // Validate conflicts AFTER basic validations
        System.out.println("🔍 Validating waitlist-to-booking conflicts...");
        try {
            // Create a temporary BookingForm for conflict validation
            BookingForm tempForm = new BookingForm();
            tempForm.setRestaurantId(restaurantId);
            tempForm.setBookingTime(confirmedBookingTime);
            tempForm.setGuestCount(waitlist.getPartySize());

            conflictService.validateBookingConflicts(tempForm, waitlist.getCustomer().getCustomerId());
            System.out.println("✅ No conflicts found, proceeding with waitlist confirmation");
        } catch (BookingConflictException e) {
            System.err.println("❌ Waitlist-to-booking conflict detected: " + e.getMessage());
            throw e; // Re-throw to be handled by controller
        }

        // Tạo booking từ waitlist
        Booking booking = new Booking();
        booking.setCustomer(waitlist.getCustomer());
        booking.setRestaurant(waitlist.getRestaurant());
        booking.setNumberOfGuests(waitlist.getPartySize());
        booking.setBookingTime(confirmedBookingTime);
        booking.setStatus(BookingStatus.CONFIRMED); // Xác nhận ngay từ khi tạo booking từ waitlist
        booking.setDepositAmount(BigDecimal.ZERO); // Sẽ được tính sau
        booking.setNote("Confirmed from waitlist");
        booking.setCreatedAt(LocalDateTime.now());

        // Lưu booking
        Booking savedBooking = bookingRepository.save(booking);
        System.out.println("✅ Booking created from waitlist: " + savedBooking.getBookingId());

        // Copy dishes từ waitlist
        if (waitlist.getWaitlistDishes() != null && !waitlist.getWaitlistDishes().isEmpty()) {
            for (WaitlistDish waitlistDish : waitlist.getWaitlistDishes()) {
                BookingDish bookingDish = new BookingDish();
                bookingDish.setBooking(savedBooking);
                bookingDish.setDish(waitlistDish.getDish());
                bookingDish.setQuantity(waitlistDish.getQuantity());
                bookingDish.setPrice(waitlistDish.getPrice());
                bookingDishRepository.save(bookingDish);
            }
            System.out.println("✅ Copied " + waitlist.getWaitlistDishes().size() + " dishes to booking");
        }

        // Copy services từ waitlist
        if (waitlist.getWaitlistServices() != null && !waitlist.getWaitlistServices().isEmpty()) {
            for (WaitlistServiceItem waitlistService : waitlist.getWaitlistServices()) {
                BookingService bookingService = new BookingService();
                bookingService.setBooking(savedBooking);
                bookingService.setService(waitlistService.getService());
                bookingService.setQuantity(waitlistService.getQuantity());
                bookingService.setPrice(waitlistService.getPrice());
                bookingServiceRepository.save(bookingService);
            }
            System.out.println("✅ Copied " + waitlist.getWaitlistServices().size() + " services to booking");
        }

        // Copy tables từ waitlist
        if (waitlist.getWaitlistTables() != null && !waitlist.getWaitlistTables().isEmpty()) {
            for (WaitlistTable waitlistTable : waitlist.getWaitlistTables()) {
                BookingTable bookingTable = new BookingTable();
                bookingTable.setBooking(savedBooking);
                bookingTable.setTable(waitlistTable.getTable());
                bookingTableRepository.save(bookingTable);
            }
            System.out.println("✅ Copied " + waitlist.getWaitlistTables().size() + " tables to booking");
        }

        // Tính tổng tiền
        BigDecimal totalAmount = calculateTotalAmount(savedBooking);
        savedBooking.setDepositAmount(totalAmount);
        bookingRepository.save(savedBooking);

        // Cập nhật waitlist status
        waitlist.setStatus(WaitlistStatus.SEATED);
        waitlistRepository.save(waitlist);

        System.out.println("✅ Waitlist confirmed to booking successfully");
        System.out.println("   Waitlist ID: " + waitlistId);
        System.out.println("   Booking ID: " + savedBooking.getBookingId());
        System.out.println("   Booking Time: " + confirmedBookingTime);
        System.out.println("   Total Amount: " + totalAmount);

        return savedBooking;
    }

    /**
     * Xác nhận Waitlist và tạo Booking từ thông tin Waitlist (legacy method - không
     * có validation)
     * 
     * @deprecated Sử dụng confirmWaitlistToBooking(Integer waitlistId,
     *             LocalDateTime confirmedBookingTime, Integer restaurantId) thay
     *             thế
     */
    @Deprecated
    public Booking confirmWaitlistToBooking(Integer waitlistId, LocalDateTime confirmedBookingTime) {
        System.out.println("⚠️ Using deprecated confirmWaitlistToBooking method without restaurant validation");

        // Lấy waitlist entry để lấy restaurant ID
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new IllegalArgumentException("Waitlist entry not found"));

        // Sử dụng method mới với restaurant ID từ waitlist
        return confirmWaitlistToBooking(waitlistId, confirmedBookingTime, waitlist.getRestaurant().getRestaurantId());
    }

    /**
     * Tính tổng tiền cho booking
     */
    private BigDecimal calculateTotalAmount(Booking booking) {
        BigDecimal total = BigDecimal.ZERO;

        // Tính tiền dishes
        List<BookingDish> bookingDishes = bookingDishRepository.findByBooking(booking);
        for (BookingDish bookingDish : bookingDishes) {
            total = total.add(bookingDish.getPrice().multiply(BigDecimal.valueOf(bookingDish.getQuantity())));
        }

        // Tính tiền services
        List<BookingService> bookingServices = bookingServiceRepository.findByBooking(booking);
        for (BookingService bookingService : bookingServices) {
            total = total.add(bookingService.getPrice().multiply(BigDecimal.valueOf(bookingService.getQuantity())));
        }

        return total;
    }
}
