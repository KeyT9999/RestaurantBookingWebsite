package com.example.booking.web.controller.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.booking.domain.Booking;
import com.example.booking.domain.Customer;
import com.example.booking.domain.RestaurantTable;
import com.example.booking.domain.Dish;
import com.example.booking.dto.RestaurantServiceDto;
import com.example.booking.domain.RestaurantService;
import com.example.booking.domain.User;
import com.example.booking.dto.BookingDetailsDto;
import com.example.booking.dto.BookingDishDto;
import com.example.booking.dto.BookingServiceDto;
import com.example.booking.dto.RestaurantTableDto;
import com.example.booking.dto.DishDto;
import com.example.booking.service.RestaurantManagementService;
import com.example.booking.service.SimpleUserService;
import java.util.Map;
import java.util.HashMap;
import com.example.booking.service.BookingService;
import com.example.booking.service.CustomerService;
import com.example.booking.domain.RestaurantMedia;
import com.example.booking.dto.RestaurantDto;

@RestController
@RequestMapping("/api/booking")
public class BookingApiController {
    
    @Autowired
    private RestaurantManagementService restaurantService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private SimpleUserService userService;
    
    /**
     * API endpoint để lấy table layouts theo nhà hàng
     */
    @GetMapping("/restaurants/{restaurantId}/table-layouts")
    public ResponseEntity<List<Map<String, Object>>> getTableLayoutsByRestaurant(
            @PathVariable("restaurantId") Integer restaurantId) {
        try {
            System.out.println("🔍 API: Getting table layouts for restaurant ID: " + restaurantId);

            List<RestaurantMedia> layouts = restaurantService.findMediaByRestaurantAndType(restaurantId,
                    "table_layout");

            System.out.println("✅ API: Found " + layouts.size() + " table layouts");

            // Convert to Map to avoid lazy loading issues
            List<Map<String, Object>> layoutMaps = layouts.stream()
                    .map(layout -> {
                        Map<String, Object> layoutMap = new HashMap<>();
                        layoutMap.put("mediaId", layout.getMediaId());
                        layoutMap.put("url", layout.getUrl());
                        layoutMap.put("type", layout.getType());
                        layoutMap.put("createdAt", layout.getCreatedAt());
                        return layoutMap;
                    })
                    .collect(Collectors.toList());

            System.out.println("✅ API: Returning " + layoutMaps.size() + " layout maps");

            return ResponseEntity.ok(layoutMaps);
        } catch (Exception e) {
            System.err.println("❌ API Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * API endpoint để lấy danh sách bàn theo nhà hàng
     */
    @GetMapping("/restaurants/{restaurantId}/tables")
    public ResponseEntity<List<Map<String, Object>>> getTablesByRestaurant(
            @PathVariable("restaurantId") Integer restaurantId) {
        try {
            System.out.println("🔍 API: Getting tables for restaurant ID: " + restaurantId);

            List<RestaurantTable> tables = restaurantService.findTablesByRestaurant(restaurantId);

            System.out.println("✅ API: Found " + tables.size() + " tables");

            // Convert to Map to include table images
            List<Map<String, Object>> tableMaps = tables.stream()
                    .map(table -> {
                        Map<String, Object> tableMap = new HashMap<>();
                        tableMap.put("tableId", table.getTableId());
                        tableMap.put("tableName", table.getTableName());
                        tableMap.put("capacity", table.getCapacity());
                        tableMap.put("status", table.getStatus());
                        tableMap.put("depositAmount", table.getDepositAmount());
                        tableMap.put("restaurantId", table.getRestaurant().getRestaurantId());
                        tableMap.put("mainImage", table.getMainTableImage());
                        tableMap.put("images", table.getTableImages().stream()
                                .map(img -> {
                                    Map<String, Object> imgMap = new HashMap<>();
                                    imgMap.put("url", img.getUrl());
                                    imgMap.put("mediaId", img.getMediaId());
                                    return imgMap;
                                })
                                .collect(Collectors.toList()));
                        return tableMap;
                    })
                    .collect(Collectors.toList());

            System.out.println("✅ API: Returning " + tableMaps.size() + " table maps");

            return ResponseEntity.ok(tableMaps);
        } catch (Exception e) {
            System.err.println("❌ API Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * API endpoint để lấy thông tin nhà hàng
     */
    @GetMapping("/restaurants/{restaurantId}")
    public ResponseEntity<RestaurantDto> getRestaurant(@PathVariable("restaurantId") Integer restaurantId) {
        try {
            return restaurantService.findRestaurantById(restaurantId)
                    .map(restaurant -> {
                        RestaurantDto dto = new RestaurantDto(
                                restaurant.getRestaurantId(),
                                restaurant.getRestaurantName(),
                                restaurant.getDescription(),
                                restaurant.getAddress(),
                                restaurant.getPhone(),
                                restaurant.getCuisineType(),
                                restaurant.getOpeningHours(),
                                restaurant.getAveragePrice(),
                                restaurant.getWebsiteUrl(),
                                restaurant.getCreatedAt(),
                                restaurant.getUpdatedAt(),
                                restaurant.getOwner() != null ? restaurant.getOwner().getOwnerId() : null);
                        return ResponseEntity.ok(dto);
                    })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.err.println("❌ API Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * API endpoint để lấy danh sách nhà hàng
     */
    @GetMapping("/restaurants")
    public ResponseEntity<List<RestaurantDto>> getAllRestaurants() {
        try {
            List<?> restaurants = restaurantService.findAllRestaurants();

            // Convert to DTO to avoid Hibernate proxy issues
            List<RestaurantDto> restaurantDtos = restaurants.stream()
                    .map(restaurant -> {
                        // Cast to RestaurantProfile (assuming that's the type)
                        var restaurantProfile = (com.example.booking.domain.RestaurantProfile) restaurant;
                        return new RestaurantDto(
                                restaurantProfile.getRestaurantId(),
                                restaurantProfile.getRestaurantName(),
                                restaurantProfile.getDescription(),
                                restaurantProfile.getAddress(),
                                restaurantProfile.getPhone(),
                                restaurantProfile.getCuisineType(),
                                restaurantProfile.getOpeningHours(),
                                restaurantProfile.getAveragePrice(),
                                restaurantProfile.getWebsiteUrl(),
                                restaurantProfile.getCreatedAt(),
                                restaurantProfile.getUpdatedAt(),
                                restaurantProfile.getOwner() != null ? restaurantProfile.getOwner().getOwnerId()
                                        : null);
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(restaurantDtos);
        } catch (Exception e) {
            System.err.println("❌ API Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * API endpoint để lấy danh sách món ăn theo nhà hàng
     */
    @GetMapping("/restaurants/{restaurantId}/dishes")
    public ResponseEntity<List<DishDto>> getDishesByRestaurant(@PathVariable("restaurantId") Integer restaurantId) {
        try {
            System.out.println("🔍 API: Getting dishes for restaurant ID: " + restaurantId);

            List<Dish> dishes = restaurantService.findDishesByRestaurant(restaurantId);
            System.out.println("✅ API: Found " + dishes.size() + " dishes");

            // Convert to DTO to avoid Hibernate proxy issues
            List<DishDto> dishDtos = dishes.stream()
                    .map(dish -> new DishDto(
                            dish.getDishId(),
                            dish.getName(),
                            dish.getDescription(),
                            dish.getPrice(),
                            dish.getCategory(),
                            dish.getStatus() != null ? dish.getStatus().toString() : "UNKNOWN",
                            dish.getRestaurant() != null ? dish.getRestaurant().getRestaurantId() : null))
                    .collect(Collectors.toList());

            System.out.println("✅ API: Returning " + dishDtos.size() + " dish DTOs");
            return ResponseEntity.ok(dishDtos);
        } catch (Exception e) {
            System.err.println("❌ API Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * API endpoint để lấy danh sách dịch vụ theo nhà hàng
     */
    @GetMapping("/restaurants/{restaurantId}/services")
    public ResponseEntity<List<RestaurantServiceDto>> getServicesByRestaurant(
            @PathVariable("restaurantId") Integer restaurantId) {
        try {
            System.out.println("🔍 API: Getting services for restaurant ID: " + restaurantId);

            List<RestaurantService> services = restaurantService.findServicesByRestaurant(restaurantId);
            System.out.println("✅ API: Found " + services.size() + " services");

            // Convert to DTO to avoid Hibernate proxy issues
            List<RestaurantServiceDto> serviceDtos = services.stream()
                    .map(service -> new RestaurantServiceDto(
                            service.getServiceId(),
                            service.getName(),
                            service.getCategory(),
                            service.getDescription(),
                            service.getPrice(),
                            service.getStatus() != null ? service.getStatus().toString() : "UNKNOWN",
                            service.getRestaurant() != null ? service.getRestaurant().getRestaurantId() : null))
                    .collect(Collectors.toList());

            System.out.println("✅ API: Returning " + serviceDtos.size() + " service DTOs");
            return ResponseEntity.ok(serviceDtos);
        } catch (Exception e) {
            System.err.println("❌ API Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * API endpoint để lấy chi tiết booking
     */
    @GetMapping("/{bookingId}/details")
    public ResponseEntity<BookingDetailsDto> getBookingDetails(
            @PathVariable("bookingId") Integer bookingId,
            Authentication authentication) {
        try {
            UUID customerId = getCurrentCustomerId(authentication);
            Optional<Booking> bookingOpt = bookingService.findBookingById(bookingId);

            if (bookingOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Booking booking = bookingOpt.get();

            // Validate ownership
            if (!booking.getCustomer().getCustomerId().equals(customerId)) {
                return ResponseEntity.status(403).build();
            }

            // Calculate total amount
            BigDecimal totalAmount = bookingService.calculateTotalAmount(booking);

            // Create BookingDetailsDto
            BookingDetailsDto detailsDto = new BookingDetailsDto();
            detailsDto.setBookingId(booking.getBookingId());
            detailsDto.setRestaurantName(booking.getRestaurant().getRestaurantName());

            // Get all table names
            if (booking.getBookingTables() != null && !booking.getBookingTables().isEmpty()) {
                List<String> tableNames = booking.getBookingTables().stream()
                        .map(bt -> bt.getTable().getTableName())
                        .collect(Collectors.toList());
                detailsDto.setTableNames(tableNames);

                // Set first table name for backward compatibility
                detailsDto.setTableName(tableNames.get(0));
            }

            // Get dishes
            if (booking.getBookingDishes() != null && !booking.getBookingDishes().isEmpty()) {
                List<BookingDishDto> dishDtos = booking.getBookingDishes().stream()
                        .map(bd -> {
                            BookingDishDto dto = new BookingDishDto();
                            dto.setDishId(bd.getDish().getDishId());
                            dto.setDishName(bd.getDish().getName());
                            dto.setDescription(bd.getDish().getDescription());
                            dto.setQuantity(bd.getQuantity());
                            dto.setPrice(bd.getDish().getPrice());
                            dto.setTotalPrice(bd.getDish().getPrice().multiply(BigDecimal.valueOf(bd.getQuantity())));
                            dto.setCategory(bd.getDish().getCategory());
                            return dto;
                        })
                        .collect(Collectors.toList());
                detailsDto.setDishes(dishDtos);
            }

            // Get services
            if (booking.getBookingServices() != null && !booking.getBookingServices().isEmpty()) {
                List<BookingServiceDto> serviceDtos = booking.getBookingServices().stream()
                        .map(bs -> {
                            BookingServiceDto dto = new BookingServiceDto();
                            dto.setServiceId(bs.getService().getServiceId());
                            dto.setServiceName(bs.getService().getName());
                            dto.setDescription(bs.getService().getDescription());
                            dto.setQuantity(1); // Services typically have quantity 1
                            dto.setPrice(bs.getService().getPrice());
                            dto.setTotalPrice(bs.getService().getPrice());
                            dto.setCategory(bs.getService().getCategory());
                            return dto;
                        })
                        .collect(Collectors.toList());
                detailsDto.setServices(serviceDtos);
            }

            detailsDto.setBookingTime(booking.getBookingTime());
            detailsDto.setGuestCount(booking.getNumberOfGuests());
            detailsDto.setDepositAmount(booking.getDepositAmount());
            detailsDto.setTotalAmount(totalAmount);
            detailsDto.setStatus(booking.getStatus());
            detailsDto.setCreatedAt(booking.getCreatedAt());
            detailsDto.setUpdatedAt(booking.getUpdatedAt());

            return ResponseEntity.ok(detailsDto);

        } catch (Exception e) {
            System.err.println("❌ Error getting booking details: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Helper method to get current customer ID from authentication
     */
    private UUID getCurrentCustomerId(Authentication authentication) {
        System.out.println("🔍 getCurrentCustomerId called");
        String username = authentication.getName();
        System.out.println("   Username: " + username);

        // Tìm customer theo username
        System.out.println("🔍 Looking for customer by username...");
        Optional<Customer> customerOpt = customerService.findByUsername(username);

        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            System.out.println("✅ Customer found: " + customer.getCustomerId());
            return customer.getCustomerId();
        }

        // Nếu chưa có Customer record, tạo mới
        System.out.println("ℹ️ Customer not found, creating new customer...");
        // Lấy User từ authentication - xử lý cả User và OAuth2User
        User user = getUserFromAuthentication(authentication);
        System.out.println("✅ User found: " + user.getUsername());

        // Tạo Customer mới
        System.out.println("🔍 Creating new customer...");
        Customer customer = new Customer(user);
        // updatedAt sẽ được set tự động bởi @PrePersist
        System.out.println("🔍 Saving new customer...");
        customer = customerService.save(customer);

        System.out.println("✅ Created new Customer record for user: " + username);
        return customer.getCustomerId();
    }

    /**
     * Helper method để lấy User từ authentication (xử lý cả User và OAuth2User)
     */
    private User getUserFromAuthentication(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        // Nếu là User object trực tiếp (regular login)
        if (principal instanceof User) {
            return (User) principal;
        }

        // Nếu là OAuth2User hoặc OidcUser (OAuth2 login)
        if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            String username = authentication.getName(); // username = email cho OAuth users

            // Thử tìm User trực tiếp từ UserService
            // Vì OAuth users có username = email, và UserService có thể tìm theo username
            try {
                User user = (User) userService.loadUserByUsername(username);
                return user;
            } catch (Exception e) {
                throw new RuntimeException("User not found for OAuth username: " + username +
                        ". Error: " + e.getMessage());
            }
        }

        throw new RuntimeException(
                "Unsupported authentication principal type: " + principal.getClass().getSimpleName());
    }
}
